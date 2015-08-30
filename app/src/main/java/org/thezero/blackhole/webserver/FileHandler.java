package org.thezero.blackhole.webserver;

import android.content.Context;
import android.content.res.AssetManager;
import android.net.Uri;

import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.entity.ContentProducer;
import org.apache.http.entity.EntityTemplate;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.nikkii.embedhttp.impl.HttpHeader;
import org.nikkii.embedhttp.impl.HttpStatus;
import org.thezero.blackhole.utility.Utility;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class FileHandler implements HttpRequestHandler{
	private Context context = null;
	
	public FileHandler(Context context){
		this.context = context;
	}
	
	@Override
	public void handle(HttpRequest request, HttpResponse response, HttpContext httpContext) throws HttpException, IOException {
        String contentType;
        Integer code;
        Uri uri = Uri.parse(request.getRequestLine().getUri());

        String folder;
        List<String> s = uri.getPathSegments();
        String path = File.separator;
        for(int i=1;i<s.size()-1;i++) {
            path+=s.get(i)+File.separator;
        }
        folder = path;
        final File file = new File(Utility.BLACKHOLE_PATH +folder+s.get(s.size()-1));

        final byte[] r;
        byte[] resp;
        if(file.exists()){
            resp = Utility.loadFileAsByte(file.getAbsolutePath());
            contentType = Utility.getMimeTypeForFile(file.getName());
            code = HttpStatus.OK.getCode();
        }else{
            AssetManager mgr = context.getAssets();
            resp = Utility.loadInputStreamAsByte(mgr.open("notfound.html"));
            contentType = Utility.MIME_TYPES.get("html");
            code = HttpStatus.NOT_FOUND.getCode();
        }
        r=resp;

        HttpEntity entity = new EntityTemplate(new ContentProducer() {
            public void writeTo(final OutputStream outstream) throws IOException {
                outstream.write(r);
            }
        });

        ((EntityTemplate)entity).setContentType(contentType);
        response.setStatusCode(code);
        response.addHeader(HttpHeader.CONTENT_DISPOSITION, "attachment");
        response.setEntity(entity);
	}
}
