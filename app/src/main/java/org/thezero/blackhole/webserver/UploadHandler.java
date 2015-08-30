package org.thezero.blackhole.webserver;

import android.content.Context;
import android.content.res.AssetManager;
import android.net.Uri;
import android.util.Log;

import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;

import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.entity.ContentProducer;
import org.apache.http.entity.EntityTemplate;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.nikkii.embedhttp.impl.HttpFileUpload;
import org.nikkii.embedhttp.impl.HttpHeader;
import org.nikkii.embedhttp.impl.HttpMethod;
import org.nikkii.embedhttp.impl.HttpStatus;
import org.nikkii.embedhttp.util.HttpUtil;
import org.nikkii.embedhttp.util.MultipartReader;
import org.thezero.blackhole.FileL;
import org.thezero.blackhole.R;
import org.thezero.blackhole.utility.Utility;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UploadHandler implements HttpRequestHandler {
	private Context context = null;

	public UploadHandler(Context context){
		this.context = context;
	}

	@Override
	public void handle(HttpRequest request, HttpResponse response, HttpContext httpContext) throws HttpException, IOException {
		String contentType = Utility.MIME_TYPES.get("html");
        Integer code;

        String method = request.getRequestLine().getMethod();
        if(method.equals(HttpMethod.POST)) {
            HttpEntity entity = ((HttpEntityEnclosingRequest)request).getEntity();
            String contentTypeHeader = request.getFirstHeader(HttpHeader.CONTENT_TYPE).getValue();

            if(contentTypeHeader.contains(Utility.MIME_MULTIPART)){
                String boundary = contentTypeHeader.substring(contentTypeHeader.indexOf(';')).trim();
                boundary = boundary.substring(boundary.indexOf('=') + 1);

                HttpFileUpload hfu = (HttpFileUpload)readMultipartData(entity.getContent(),boundary).get("datafile1");
                File fu = hfu.getTempFile();
                File toFile = new File(Utility.BLACKHOLE_PATH+File.separator+hfu.getFileName());
                try {
                    Utility.copy(fu,toFile);
                    if(toFile.exists()){
                        Log.w("TAG3", "YEEEEEEEEEP");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


        final byte[] r;
        String resp;
        AssetManager mgr = context.getAssets();
        try {
            resp = Utility.loadInputStreamAsString(mgr.open("index.html"));
            code = HttpStatus.OK.getCode();
        } catch (IOException e){
            resp = Utility.loadInputStreamAsString(mgr.open("notfound.html"));
            code = HttpStatus.NOT_FOUND.getCode();
        }

        final ArrayList<FileL> fl = new ArrayList<>();

        Uri uri = Uri.parse(request.getRequestLine().getUri());
        String folder="/";
        if(uri.getPath().contains("~")) {
            List<String> s = uri.getPathSegments();
            String path = File.separator;
            for(int i=1;i<s.size()-1;i++) {
                path+=s.get(i)+File.separator;
            }
            if(s.size()>1) {
                fl.add(new FileL("..", "/~" + path, "-", "DIR"));
                folder = path + s.get(s.size() - 1) + File.separator;
            }else{
                folder = path;
            }
        }
        File f = new File(Utility.BLACKHOLE_PATH +folder);
        if(f.isDirectory()) {
            File[] files = f.listFiles();
            for (File inFile : files) {
                if (inFile.isDirectory()) {
                    fl.add(new FileL(inFile.getName(),"/~"+folder+inFile.getName(),"-","DIR"));
                } else {
                    fl.add(new FileL(inFile.getName(),"/file"+folder+inFile.getName(),Utility.getFileSize(inFile)));
                }
            }
        }
        final String wfolder = folder;

        Template tmpl = Mustache.compiler().compile(resp);
        r=tmpl.execute(new Object() {
            Object filelist = fl;
            String title = context.getString(R.string.app_title);
            String wd = wfolder;
        }).getBytes();

        HttpEntity entity = new EntityTemplate(new ContentProducer() {
            public void writeTo(final OutputStream outstream) throws IOException {
                outstream.write(r);
            }
        });
		
		((EntityTemplate)entity).setContentType(contentType);
        response.setStatusCode(code);
		response.setEntity(entity);
	}

    /**
     * Read the data from a multipart/form-data request
     *
     * @param boundary
     *            The boundary specified by the client
     * @return A map of the POST data.
     * @throws IOException
     *             If an error occurs
     */
    public Map<String, Object> readMultipartData(InputStream in, String boundary) throws IOException {
        // Boundaries are '--' + the boundary.
        boundary = "--" + boundary;
        // Form data
        Map<String, Object> form = new HashMap<String, Object>();
        // Implementation of a reader to parse out form boundaries.
        MultipartReader reader = new MultipartReader(in, boundary);
        String l;
        while ((l = reader.readLine()) != null) {
            if (!l.startsWith(boundary) || l.startsWith(boundary) && l.endsWith("--")) {
                break;
            }
            // Read headers
            Map<String, String> props = new HashMap<String, String>();
            while ((l = reader.readLine()) != null && l.trim().length() > 0) {
                // Properties
                String key = HttpUtil.capitalizeHeader(l.substring(0, l.indexOf(':')));
                String value = l.substring(l.indexOf(':') + 1);
                if (value.charAt(0) == ' ')
                    value = value.substring(1);

                props.put(key, value);
            }
            // Check if the line STILL isn't null
            if (l != null) {
                String contentDisposition = props.get(HttpHeader.CONTENT_DISPOSITION);
                Map<String, String> disposition = new HashMap<String, String>();
                String[] dis = contentDisposition.split("; ");
                for (String s : dis) {
                    int eqIdx = s.indexOf('=');
                    if (eqIdx != -1) {
                        String key = s.substring(0, eqIdx);
                        String value = s.substring(eqIdx + 1).trim();
                        if (value.charAt(0) == '"') {
                            value = value.substring(1, value.length() - 1);
                        }
                        disposition.put(key, value);
                    }
                }
                String name = disposition.get("name");
                if (props.containsKey(HttpHeader.CONTENT_TYPE)) {
                    String fileName = disposition.get("filename");
                    // Create a temporary file, this'll hopefully be deleted
                    // when the request object has finalize() called
                    File tmp = File.createTempFile("upload", fileName);
                    // Open an output stream to the new file
                    FileOutputStream output = new FileOutputStream(tmp);
                    // Read the file data right from the connection, NO MEMORY
                    // CACHE.
                    byte[] buffer = new byte[1024];
                    while (true) {
                        int read = reader.readUntilBoundary(buffer, 0, buffer.length);
                        if (read == -1) {
                            break;
                        }
                        if(read<buffer.length && buffer[read-1]=='\r'){ read-=1; }
                        output.write(buffer, 0, read);
                    }
                    // Close it
                    output.close();
                    // Put the temp file
                    form.put(name, new HttpFileUpload(fileName, tmp));
                } else {
                    String value = "";
                    // String value
                    while ((l = reader.readLineUntilBoundary()) != null) {
                        value += l;
                    }
                    form.put(name, value);
                }
            }
        }
        return form;
    }
}
