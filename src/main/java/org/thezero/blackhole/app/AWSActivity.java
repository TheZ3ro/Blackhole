package org.thezero.blackhole.app;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.util.Linkify;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.thezero.blackhole.R;
import org.thezero.blackhole.Utility;
import org.thezero.blackhole.service.HTTPService;

import java.io.File;
import java.io.IOException;

public class AWSActivity extends ActionBarActivity {
    private static final int PREFERENCE_REQUEST_CODE = 1001;

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
        
        boolean isRunning = isMyServiceRunning(HTTPService.class);//AppSettings.isServiceStarted(this);
        AppSettings.setServiceStarted(AWSActivity.this, isRunning);
        
        setButtonText(isRunning);
        setInfoText(isRunning);
        AppLog.i(String.valueOf(isRunning));

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        File folder = new File(Utility.BLACKHOLE_PATH);
        boolean success = true;
        if(!folder.exists()) {
            success = folder.mkdir();
        }
        if(!success) {
            Toast.makeText(AWSActivity.this, getString(R.string.storage_error), Toast.LENGTH_LONG).show();
        }

        if(Intent.ACTION_SEND.equals(action) && type != null) {
            AppLog.i("now it's true");
            getSharedFile(intent);
        }
     	
    }

    @Override
    public void onNewIntent(Intent i){
        super.onNewIntent(i);

        String action = i.getAction();
        String type = i.getType();

        if(Intent.ACTION_SEND.equals(action) && type != null) {
            getSharedFile(i);
        }

        /*Bundle data = i.getExtras();

        if (data != null && data.containsKey(BlackNotification.ACTION_KEY)) {
            String act = data.getString(BlackNotification.ACTION_KEY);
            if(act.equals(BlackNotification.ACTION_N_STOP)) {
                AppLog.i(data.getString(BlackNotification.ACTION_KEY));
                Toast.makeText(AWSActivity.this, getString(R.string.stop_success), Toast.LENGTH_LONG).show();
                Intent i1 = new Intent(AWSActivity.this, HTTPService.class);
                stopService(i1);
                AppSettings.setServiceStarted(AWSActivity.this, false);
                setButtonText(false);
                setInfoText(false);
            }
        }*/
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
        if(AppSettings.isServiceStarted(AWSActivity.this)){
            setButtonText(true);
            setInfoText(true);
        }
        else {
            setButtonText(false);
            setInfoText(false);
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

    private void getSharedFile(Intent intent) {
        Uri shareUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
        File folder = new File(Utility.BLACKHOLE_PATH);
        if(folder.exists()) {
            String path = Utility.getPath(this,shareUri);
            if(path!=null) {
                File shareFile = new File(path);
                File toFile = new File(Utility.BLACKHOLE_PATH + File.separator + shareFile.getName());
                try {
                    Utility.copy(shareFile, toFile);
                    if (toFile.exists()) {
                        Toast.makeText(AWSActivity.this, getString(R.string.share_success), Toast.LENGTH_LONG).show();
                        /*Intent service = new Intent(AWSActivity.this, HTTPService.class);
                        if (!AppSettings.isServiceStarted(AWSActivity.this)) {
                            startService(service);
                            AppSettings.setServiceStarted(AWSActivity.this, true);
                            setButtonText(true);
                            setInfoText(true);
                        }*/
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else{
                Toast.makeText(AWSActivity.this, getString(R.string.share_failed), Toast.LENGTH_LONG).show();
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
		
		if(isServiceRunning) {
            text = getString(R.string.log_running) + "\n" + Utility.getHost();
        }
		
		txtLog.setText(text);
	}
	
	private View.OnClickListener btnClick = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			switch(v.getId()){
				case R.id.btnStartStop:{
					Intent intent = new Intent(AWSActivity.this,HTTPService.class);
					
					if(isMyServiceRunning(HTTPService.class)){
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

    public boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}