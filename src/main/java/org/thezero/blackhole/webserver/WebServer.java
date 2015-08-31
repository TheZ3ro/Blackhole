package org.thezero.blackhole.webserver;

import android.content.Context;

import org.apache.http.HttpException;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.impl.DefaultHttpServerConnection;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.BasicHttpProcessor;
import org.apache.http.protocol.HttpRequestHandlerRegistry;
import org.apache.http.protocol.HttpService;
import org.apache.http.protocol.ResponseConnControl;
import org.apache.http.protocol.ResponseContent;
import org.apache.http.protocol.ResponseDate;
import org.apache.http.protocol.ResponseServer;
import org.thezero.blackhole.app.AppSettings;
import org.thezero.blackhole.webserver.handler.AssetHandler;
import org.thezero.blackhole.webserver.handler.FileHandler;
import org.thezero.blackhole.webserver.handler.GetApkHandler;
import org.thezero.blackhole.webserver.handler.ListingHandler;
import org.thezero.blackhole.webserver.handler.UploadHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class WebServer extends Thread {
	private static final String SERVER_NAME = "BlackholeServer";
    public static final int DEFAULT_SERVER_PORT = 8081;
	private static final String ALL_PATTERN = "*";
    private static final String NONE_PATTERN = "/";
    private static final String DIR_PATTERN = "/~/*";
    private static final String HOME_PATTERN = "/index.html";
	private static final String FILE_PATTERN = "/file/*";
    private static final String GETAPK_PATTERN = "/blackhole.apk";
    private static final String UPLOAD_PATTERN = "/upload";
	
	private boolean isRunning = false;
	private Context context = null;
	private int serverPort = 0;
	
	private BasicHttpProcessor httpproc = null;
	private BasicHttpContext httpContext = null;
	private HttpService httpService = null;
	private HttpRequestHandlerRegistry registry = null;
	
	public WebServer(Context context){
		super(SERVER_NAME);
		
		this.setContext(context);
		
		serverPort = WebServer.DEFAULT_SERVER_PORT;
		httpproc = new BasicHttpProcessor();
		httpContext = new BasicHttpContext();
		
        httpproc.addInterceptor(new ResponseDate());
        httpproc.addInterceptor(new ResponseServer());
        httpproc.addInterceptor(new ResponseContent());
        httpproc.addInterceptor(new ResponseConnControl());

        httpService = new HttpService(httpproc, 
        									new DefaultConnectionReuseStrategy(),
        									new DefaultHttpResponseFactory());

		
        registry = new HttpRequestHandlerRegistry();
        
        registry.register(ALL_PATTERN, new AssetHandler(context));
        registry.register(HOME_PATTERN, new ListingHandler(context));
        registry.register(NONE_PATTERN, new ListingHandler(context));
        registry.register(DIR_PATTERN, new ListingHandler(context));
        registry.register(FILE_PATTERN, new FileHandler(context));
        registry.register(GETAPK_PATTERN, new GetApkHandler(context));
        registry.register(UPLOAD_PATTERN, new UploadHandler(context));
        
        httpService.setHandlerResolver(registry);
	}
	
	@Override
	public void run() {
		super.run();
		
		try {
			ServerSocket serverSocket = new ServerSocket(serverPort);
			
			serverSocket.setReuseAddress(true);
            
			while(isRunning){
				try {
					final Socket socket = serverSocket.accept();
					
					DefaultHttpServerConnection serverConnection = new DefaultHttpServerConnection();
		        	
					serverConnection.bind(socket, new BasicHttpParams());
					
					httpService.handleRequest(serverConnection, httpContext);
					
					serverConnection.shutdown();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (HttpException e) {
					e.printStackTrace();
				}
			}
			
			serverSocket.close();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public synchronized void startThread() {
		isRunning = true;
		super.start();
	}
	
	public synchronized void stopThread(){
		isRunning = false;
        AppSettings.setServiceStarted(context, false);
	}

	public void setContext(Context context) {
		this.context = context;
	}

	public Context getContext() {
		return context;
	}

}
