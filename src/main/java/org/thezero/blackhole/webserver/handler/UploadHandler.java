package org.thezero.blackhole.webserver.handler;

import android.content.Context;
import android.content.res.AssetManager;
import android.net.Uri;

import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.nikkii.embedhttp.impl.HttpHeader;
import org.nikkii.embedhttp.impl.HttpMethod;
import org.nikkii.embedhttp.impl.HttpStatus;
import org.thezero.blackhole.R;
import org.thezero.blackhole.Utility;
import org.thezero.blackhole.webserver.MFile;
import org.thezero.blackhole.webserver.MMessages;
import org.thezero.blackhole.webserver.ResponseForger;
import org.thezero.multipart.Multipart;
import org.thezero.multipart.Part;
import org.thezero.multipart.ValueParser;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UploadHandler implements HttpRequestHandler {
	private Context context = null;

    public static final int NOTIFICATION_SHARED_ID = 2;

	public UploadHandler(Context context){
		this.context = context;
	}

	@Override
	public void handle(HttpRequest request, HttpResponse response, HttpContext httpContext) throws HttpException, IOException {
        final ArrayList<MMessages> mess = new ArrayList<>();

        ResponseForger responseForger;
        AssetManager mgr = context.getAssets();
        try {
            responseForger = new ResponseForger(Utility.loadInputStreamAsString(mgr.open("index.html")),Utility.MIME_TYPES.get("html"),HttpStatus.OK);
        } catch (IOException e){
            responseForger = new ResponseForger(Utility.loadInputStreamAsByte(mgr.open("notfound.html")),Utility.MIME_TYPES.get("html"),HttpStatus.NOT_FOUND);
        }

        String method = request.getRequestLine().getMethod();
        ArrayList<String> uploaded = new ArrayList<>();
        if(method.equals(HttpMethod.POST)) {
            try {
                String contentT = request.getFirstHeader(HttpHeader.CONTENT_TYPE).getValue();
                if (!contentT.startsWith("multipart/")) {
                    throw new IOException("Multipart content required");
                }
                HttpEntity entity = ((HttpEntityEnclosingRequest) request).getEntity();
                Map<String, String> parms = ValueParser.parse(contentT);
                String encoding = parms.get("charset");
                if (encoding == null) {
                    encoding = "ISO-8859-1";
                }
                InputStream in = entity.getContent();
                try {
                    byte boundary[] = parms.get("boundary").getBytes(encoding);
                    Multipart mp = new Multipart(in, encoding, boundary);
                    for (Part part = mp.nextPart(); part != null; part = mp.nextPart()) {
                        InputStream is = part.getBody();
                        String cd = part.getFirstValue("content-disposition");
                        Map<String, String> cdParms = ValueParser.parse(cd);
                        // Sorry, one file a time
                        String fieldName = cdParms.get("name");
                        if(fieldName.equals("myfile")) {
                            File toFile = new File(Utility.BLACKHOLE_PATH + File.separator + cdParms.get("filename"));
                            try {
                                Utility.copy(is, toFile);
                                if (toFile.exists()) {
                                    uploaded.add(toFile.getName());
                                    String title=context.getString(R.string.file_upl);
                                    String text=context.getString(R.string.file_name)+" "+toFile.getName();
                                    mess.add(new MMessages(title,text,"success"));
                                    response.setHeader(HttpHeader.LOCATION,"/");
                                    responseForger.setStatusCode(HttpStatus.OK);
                                }
                            }catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                } finally {
                    in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
                response.setHeader(HttpHeader.LOCATION,"/");
                responseForger.setStatusCode(HttpStatus.BAD_REQUEST);
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        }else{
            response.setHeader(HttpHeader.LOCATION, "/");
            responseForger.setStatusCode(HttpStatus.FOUND);
        }

        final ArrayList<MFile> fl = new ArrayList<>();

        Uri uri = Uri.parse(request.getRequestLine().getUri());
        String folder="/";
        if(uri.getPath().contains("~")) {
            List<String> s = uri.getPathSegments();
            String path = File.separator;
            for(int i=1;i<s.size()-1;i++) {
                path+=s.get(i)+File.separator;
            }
            if(s.size()>1) {
                fl.add(new MFile("..", "/~" + path, "-", "DIR"));
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
                    fl.add(new MFile(inFile.getName(),"/~"+folder+inFile.getName(),"-","DIR"));
                } else {
                    if(uploaded.contains(inFile.getName())){
                        fl.add(new MFile(inFile.getName(), "/file" + folder + inFile.getName(), Utility.getFileSize(inFile),true));
                    }else {
                        fl.add(new MFile(inFile.getName(), "/file" + folder + inFile.getName(), Utility.getFileSize(inFile)));
                    }
                }
            }
        }
        final String wfolder = folder;

        response.setStatusCode(responseForger.getStatusCode());
        response.setEntity(responseForger.forgeResponse(new Object() {
            Object filelist = fl;
            String title = context.getString(R.string.app_title);
            String wd = wfolder;
            Object messages = mess;
        }));
	}
}
