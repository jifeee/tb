package com.access2;

import com.access2.R;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.EditText;

public class LoginActivity extends Activity{
	
	String TAG="TEX";
	Context ctx;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
		ctx=this;
		
		
        final EditText pass = (EditText) findViewById(R.id.editText1);
        pass.setOnKeyListener(new OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                 
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                    (keyCode == KeyEvent.KEYCODE_ENTER)) {
                   
                	findViewById(R.id.button1).requestFocus();
                  return true;
                }
                return false;
            }
        });
        
        final Button login = (Button) findViewById(R.id.button1);
        login.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {

            }
        });
        
        final Button offline = (Button) findViewById(R.id.button2);
        offline.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {

            }
        });
        
        final Button forgot = (Button) findViewById(R.id.button3);
        forgot.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {

            }
        });
		
		
		
	}	

}
