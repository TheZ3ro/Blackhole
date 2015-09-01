package org.thezero.blackhole.webserver.handler;

import android.content.Context;
import android.content.res.AssetManager;
import android.net.Uri;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.nikkii.embedhttp.impl.HttpHeader;
import org.nikkii.embedhttp.impl.HttpStatus;
import org.thezero.blackhole.Utility;
import org.thezero.blackhole.webserver.ResponseForger;

import java.io.File;
import java.io.IOException;
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

        ResponseForger responseForger;
        if(file.exists()){
            responseForger = new ResponseForger(Utility.loadFileAsByte(file.getAbsolutePath()),Utility.getMimeTypeForFile(file.getName()),HttpStatus.OK);
        }else{
            AssetManager mgr = context.getAssets();
            responseForger = new ResponseForger(Utility.loadInputStreamAsByte(mgr.open("notfound.html")),Utility.MIME_TYPES.get("html"),HttpStatus.NOT_FOUND);
        }

        response.setStatusCode(responseForger.getStatusCode());
        response.addHeader(HttpHeader.CONTENT_DISPOSITION, "attachment");
        response.setEntity(responseForger.forgeResponse());
	}
}
