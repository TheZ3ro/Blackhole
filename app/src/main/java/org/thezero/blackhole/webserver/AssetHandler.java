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
import org.nikkii.embedhttp.impl.HttpStatus;
import org.thezero.blackhole.utility.Utility;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;

public class AssetHandler implements HttpRequestHandler {
	private Context context = null;

	public AssetHandler(Context context){
		this.context = context;
	}

	@Override
	public void handle(HttpRequest request, HttpResponse response, HttpContext httpContext) throws HttpException, IOException {
		String contentType;
        Integer code;
        Uri uri = Uri.parse(request.getRequestLine().getUri());
        String fileUri = URLDecoder.decode(uri.getLastPathSegment());

        final byte[] r;
        byte[] resp;
        AssetManager mgr = context.getAssets();
        try {
            resp = Utility.loadInputStreamAsByte(mgr.open(fileUri));
            contentType = Utility.getMimeTypeForFile(fileUri);
            code = HttpStatus.OK.getCode();
        } catch (IOException e){
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
		response.setEntity(entity);
	}

}
