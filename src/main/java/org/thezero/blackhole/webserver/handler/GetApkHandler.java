package org.thezero.blackhole.webserver.handler;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.nikkii.embedhttp.impl.HttpHeader;
import org.nikkii.embedhttp.impl.HttpStatus;
import org.thezero.applist.model.InstalledApk;
import org.thezero.blackhole.Utility;
import org.thezero.blackhole.webserver.ResponseForger;

import java.io.File;
import java.io.IOException;

import static org.thezero.applist.InstalledAppsHelper.getInstalledApk;

public class GetApkHandler implements HttpRequestHandler {
	private Context context = null;

	public GetApkHandler(Context context){
		this.context = context;
	}

	@Override
	public void handle(HttpRequest request, HttpResponse response, HttpContext httpContext) throws HttpException, IOException {

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

            ResponseForger responseForger;
            if(a.exists()){
                responseForger = new ResponseForger(Utility.loadFileAsByte(a.getAbsolutePath()),Utility.getMimeTypeForFile(a.getName()),HttpStatus.OK);
                a.delete();
            }else{
                AssetManager mgr = context.getAssets();
                responseForger = new ResponseForger(Utility.loadInputStreamAsByte(mgr.open("notfound.html")),Utility.MIME_TYPES.get("html"),HttpStatus.NOT_FOUND);
            }

            response.setStatusCode(responseForger.getStatusCode());
            response.addHeader(HttpHeader.CONTENT_DISPOSITION, "attachment");
            response.setEntity(responseForger.forgeResponse());

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

	}

}
