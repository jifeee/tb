package com.access2;

import java.util.Timer;
import java.util.TimerTask;

import classes.Logger;

import android.app.Activity;
import android.os.Bundle;

public class LogAppActivity extends Activity {
	Logger log; 
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        super.loadUrl("file:///android_asset/www/index.html");
        
        Timer t1 = new Timer();
        TimerTask send = new TimerTask() {
            public void run() { 
                	log.send();
            }	
        };
        t1.scheduleAtFixedRate(send, 0, 3000);
        
        Timer t2 = new Timer();
        TimerTask write = new TimerTask() {
            public void run() {
                log.set("gps", 12345L, 23.23, 34.56, 12.43, 45.34, 45.23, 98.56);
                //log.write();
            }	
        };
        t2.scheduleAtFixedRate(write, 0, 10000);
       
//        this.appView.setOnTouchListener(new OnTouchListener() {
//
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                if (event.getAction() == MotionEvent.ACTION_DOWN) {
//                	log.set("state", (byte)1, (byte)2, (byte)3, (byte)4, (byte)5);
//                	log.set("click", (int)event.getX(), (int)event.getY());
//                    log.write();
//                }
//                return true;
//            }
//        });
        
    }

}
