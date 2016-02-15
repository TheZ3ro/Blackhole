package org.thezero.blackhole.webserver.handler;

import android.content.Context;
import android.content.res.AssetManager;

import org.thezero.zip.ZipManager;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.nikkii.embedhttp.impl.HttpHeader;
import org.nikkii.embedhttp.impl.HttpStatus;
import org.thezero.blackhole.Utility;
import org.thezero.blackhole.app.AppLog;
import org.thezero.blackhole.webserver.ResponseForger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DownloadAllHandler implements HttpRequestHandler {
	private Context context = null;

	public DownloadAllHandler(Context context){
		this.context = context;
	}

	@Override
	public void handle(HttpRequest request, HttpResponse response, HttpContext httpContext) throws HttpException, IOException {

        String zip = Utility.SD_PATH+"/blackhole_all.zip";

        ZipManager zipManager = new ZipManager();
        zipManager.zipFolder(Utility.BLACKHOLE_PATH, zip);

        File a = new File(zip);

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
	}

    private List<String> recursiveListing(File dir) {
        List<String> ls = new ArrayList<>();
        File[] files = dir.listFiles();
        for (File inFile : files) {
            if (inFile.isDirectory()) {
                ls.addAll(recursiveListing(inFile));
            } else {
                ls.add(inFile.getAbsolutePath());
                AppLog.i(inFile.getAbsolutePath());
            }
        }
        return ls;
    }

}
