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
import org.thezero.blackhole.R;
import org.thezero.blackhole.Utility;
import org.thezero.blackhole.webserver.MFile;
import org.thezero.blackhole.webserver.ResponseForger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ListingHandler implements HttpRequestHandler {
	private Context context = null;
	
	public ListingHandler(Context context){
		this.context = context;
	}

	@Override
	public void handle(HttpRequest request, HttpResponse response, HttpContext httpContext) throws HttpException, IOException {
        ResponseForger responseForger;
        AssetManager mgr = context.getAssets();
        try {
            responseForger = new ResponseForger(Utility.loadInputStreamAsString(mgr.open("index.html")),Utility.MIME_TYPES.get("html"),HttpStatus.OK);
        } catch (IOException e){
            responseForger = new ResponseForger(Utility.loadInputStreamAsByte(mgr.open("notfound.html")),Utility.MIME_TYPES.get("html"),HttpStatus.NOT_FOUND);
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
                    fl.add(new MFile(inFile.getName(),"/file"+folder+inFile.getName(),Utility.getFileSize(inFile)));
                }
            }
        }
        final String wfolder = folder;

        response.setStatusCode(responseForger.getStatusCode());
        response.setEntity(responseForger.forgeResponse(new Object() {
            Object filelist = fl;
            String title = context.getString(R.string.app_title);
            String wd = wfolder;
        }));
	}

}
