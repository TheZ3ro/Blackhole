package org.thezero.blackhole.webserver;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;

import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.entity.ContentProducer;
import org.apache.http.entity.EntityTemplate;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.thezero.applist.model.InstalledApk;
import org.thezero.blackhole.utility.Utility;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import static org.thezero.applist.InstalledAppsHelper.getInstalledApk;

public class GetApkHandler implements HttpRequestHandler {
	private Context context = null;

	public GetApkHandler(Context context){
		this.context = context;
	}

	@Override
	public void handle(HttpRequest request, HttpResponse response, HttpContext httpContext) throws HttpException, IOException {
		String contentType;
        Integer code;

        try {
            InstalledApk apk = getInstalledApk(this.context,this.context.getPackageName());
            File a = new File(Utility.BLACKHOLE_PATH+"/blackhole.apk");
            if(!a.exists()){
                try {
                    Utility.copy(apk.getApkFile(), a);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            final byte[] r;
            byte[] resp;
            if(a.exists()){
                resp = Utility.loadFileAsByte(a.getPath());
                contentType = Utility.getMimeTypeForFile(a.getPath());
                code = 200;
                a.delete();
            }else{
                AssetManager mgr = context.getAssets();
                resp = Utility.loadInputStreamAsByte(mgr.open("notfound.html"));
                contentType = "text/html";
                code = 404;
            }
            r=resp;

            HttpEntity entity = new EntityTemplate(new ContentProducer() {
                public void writeTo(final OutputStream outstream) throws IOException {
                    outstream.write(r);
                }
            });

            ((EntityTemplate)entity).setContentType(contentType);
            response.setStatusCode(code);
            response.addHeader("Content-Disposition", "attachment");
            response.setEntity(entity);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }


	}

}
