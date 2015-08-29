package org.thezero.blackhole.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.util.Linkify;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.thezero.blackhole.R;
import org.thezero.blackhole.service.HTTPService;
import org.thezero.blackhole.utility.Utility;
import org.thezero.blackhole.webserver.WebServer;

import java.io.File;
import java.io.IOException;

public class AWSActivity extends ActionBarActivity {
    private static final int PREFERENCE_REQUEST_CODE = 1001;
    public static final String ACTION_DISMISS = "dismiss";

    volatile boolean stopWorker;
	Thread workerThread;
    
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.primary_text));
        toolbar.setLogo(R.drawable.ic_launcher);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        
        prepareViews();
        setButtonHandlers();
        
        boolean isRunning = AppSettings.isServiceStarted(this);
        
        setButtonText(isRunning);
        setInfoText(isRunning);

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        Log.w("",action);

        File folder = new File(Utility.BLACKHOLE_PATH);
        boolean success = true;
        if(!folder.exists()) {
            success = folder.mkdir();
        }
        if(!success) {
            Toast.makeText(AWSActivity.this, getString(R.string.storage_error), Toast.LENGTH_LONG).show();
        }

        if(ACTION_DISMISS.equals(action)){
            Intent i1 = new Intent(AWSActivity.this,HTTPService.class);
            stopService(i1);
            AppSettings.setServiceStarted(AWSActivity.this, false);
            setButtonText(false);
            setInfoText(false);
        }
        if(Intent.ACTION_SEND.equals(action) && type != null) {
            Uri shareUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
            if(folder.exists()) {
                String path = Utility.getFilePathFromUri(this,shareUri);
                File shareFile = new File(path);
                File toFile = new File(Utility.BLACKHOLE_PATH+File.separator+shareFile.getName());
                try {
                    Utility.copy(shareFile,toFile);
                    if(toFile.exists()){
                        Toast.makeText(AWSActivity.this, getString(R.string.share_success), Toast.LENGTH_LONG).show();
                        Intent service = new Intent(AWSActivity.this,HTTPService.class);
                        if(!AppSettings.isServiceStarted(AWSActivity.this)){
                            startService(service);
                            AppSettings.setServiceStarted(AWSActivity.this, true);
                            setButtonText(true);
                            setInfoText(true);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
     	
    }
     
	@Override
	public void onStop() {
		super.onStop();
	}

    @Override
    public void onDestroy(){
        AppSettings.setServiceStarted(AWSActivity.this, false);
        super.onDestroy();
    }
	
	@Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!AppSettings.isServiceStarted(AWSActivity.this)){
            setButtonText(false);
            setInfoText(false);
        }
        else{
            setButtonText(true);
            setInfoText(true);
        }
    }
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		switch(requestCode){
			case PREFERENCE_REQUEST_CODE:{
				break;
			}
		}
	}
	
	private void setButtonHandlers() {
		((Button)findViewById(R.id.btnStartStop)).setOnClickListener(btnClick);
	}

	private void prepareViews() {
		TextView txtInfo = (TextView)findViewById(R.id.txtInfo);
		
		Linkify.addLinks(txtInfo, Linkify.ALL);
	}
	
	private void setButtonText(boolean isServiceRunning){
		((Button)findViewById(R.id.btnStartStop)).setText(
				getString(isServiceRunning ? R.string.stop_caption : R.string.start_caption));
	}
	
	private void setInfoText(boolean isServiceRunning){
		TextView txtLog = (TextView)findViewById(R.id.txtLog);
		String text = getString(R.string.log_notrunning);
		
		if(isServiceRunning){
			text = getString(R.string.log_running) + "\nhttp://" + Utility.getIPAddress(true) + ":" + WebServer.DEFAULT_SERVER_PORT;
		}
		
		txtLog.setText(text);
		Log.i(getString(R.string.app_name), text);
	}
	
	private View.OnClickListener btnClick = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			switch(v.getId()){
				case R.id.btnStartStop:{
					Intent intent = new Intent(AWSActivity.this,HTTPService.class);
					
					if(AppSettings.isServiceStarted(AWSActivity.this)){
						stopService(intent);
						
						AppSettings.setServiceStarted(AWSActivity.this, false);
						setButtonText(false);
						setInfoText(false);
					}
					else{
						startService(intent);
						
						AppSettings.setServiceStarted(AWSActivity.this, true);
						setButtonText(true);
						setInfoText(true);
					}
					break;
				}
			}
		}
	};
}