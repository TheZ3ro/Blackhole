package org.thezero.blackhole.webserver;

import android.content.Context;
import android.content.res.AssetManager;
import android.net.Uri;

import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;

import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.entity.ContentProducer;
import org.apache.http.entity.EntityTemplate;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.nikkii.embedhttp.impl.HttpStatus;
import org.thezero.blackhole.FileL;
import org.thezero.blackhole.R;
import org.thezero.blackhole.utility.Utility;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class ListingHandler implements HttpRequestHandler {
	private Context context = null;
	
	public ListingHandler(Context context){
		this.context = context;
	}

	@Override
	public void handle(HttpRequest request, HttpResponse response, HttpContext httpContext) throws HttpException, IOException {
		String contentType = Utility.MIME_TYPES.get("html");
        Integer code;

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

}
