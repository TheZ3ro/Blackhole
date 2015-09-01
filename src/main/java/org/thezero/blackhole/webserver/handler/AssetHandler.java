package org.thezero.blackhole.webserver.handler;

import android.content.Context;
import android.content.res.AssetManager;
import android.net.Uri;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.nikkii.embedhttp.impl.HttpStatus;
import org.thezero.blackhole.Utility;
import org.thezero.blackhole.webserver.ResponseForger;

import java.io.IOException;
import java.net.URLDecoder;

public class AssetHandler implements HttpRequestHandler {
	private Context context = null;

	public AssetHandler(Context context){
		this.context = context;
	}

	@Override
	public void handle(HttpRequest request, HttpResponse response, HttpContext httpContext) throws HttpException, IOException {
        Uri uri = Uri.parse(request.getRequestLine().getUri());
        String fileUri = URLDecoder.decode(uri.getLastPathSegment());

        ResponseForger responseForger;
        AssetManager mgr = context.getAssets();
        try {
            responseForger = new ResponseForger(Utility.loadInputStreamAsByte(mgr.open(fileUri)),Utility.getMimeTypeForFile(fileUri),HttpStatus.OK);
        } catch (IOException e){
            responseForger = new ResponseForger(Utility.loadInputStreamAsByte(mgr.open("notfound.html")),Utility.MIME_TYPES.get("html"),HttpStatus.NOT_FOUND);
        }

        response.setStatusCode(responseForger.getStatusCode());
		response.setEntity(responseForger.forgeResponse());
	}

}
