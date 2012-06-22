package com.access2;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

public class PrivacyActivity extends Activity{
	
	TextView tv;
	Button back; 
	ScrollView sv;
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.privacy);
        
        tv = (TextView) findViewById(R.id.textView2);
        back = (Button) findViewById(R.id.button1);
        sv = (ScrollView)findViewById(R.id.scrollView1);
        
        sv.setFillViewport(false);
        
        tv.setText(R.string.lorem);
        
        back.setOnClickListener(new OnClickListener() {
        	
            public void onClick(View v) {   

        	    Intent resultIntent = new Intent();
        		setResult(RESULT_OK, resultIntent);
            	finish();
        		}
            });
        
    }

}
