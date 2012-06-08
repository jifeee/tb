package com.access2;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class SetupPasswordActivity extends Activity {
	
	String TAG="TEX";
	Context ctx;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setup);
		ctx=this;

		final Button confirm = (Button)findViewById(R.id.button1);
		
        final EditText email = (EditText) findViewById(R.id.editText1);
        email.setOnEditorActionListener(new OnEditorActionListener() {        
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId==EditorInfo.IME_ACTION_DONE){
                	findViewById(R.id.editText2).requestFocus();
                }
            return false;
            }
        });

        
        
        final EditText pass = (EditText) findViewById(R.id.editText2);
        pass.setOnEditorActionListener(new OnEditorActionListener() {        
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId==EditorInfo.IME_ACTION_DONE){
                	findViewById(R.id.editText3).requestFocus();
                }
            return false;
            }
        });

        
        
        
        final EditText passrep = (EditText) findViewById(R.id.editText3);
        passrep.setOnEditorActionListener(new OnEditorActionListener() {        
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId==EditorInfo.IME_ACTION_DONE){
                    findViewById(R.id.button1).requestFocus();
                }
            return false;
            }
        });


        
        confirm.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {   
            		String e = email.getText().toString();
            		String p = pass.getText().toString();
            		String pr = passrep.getText().toString();
            		
            		if (p.equals(pr)) {
            			Log.i(TAG, "Passwords match");
            			
                	    Intent resultIntent = new Intent();
                		setResult(RESULT_OK, resultIntent);
                    	finish();
            			
            			
            		}
            		
            		else {
            			showAlert();
            		}

        		}
            });
		
	}
	
	public void showAlert() {
		final AlertDialog ad = new AlertDialog.Builder(ctx).create();
		ad.setTitle("Error");
		ad.setMessage("The passwords do not match.");
		ad.setButton("OK", new DialogInterface.OnClickListener() {
	  	      public void onClick(DialogInterface dialog, int which) {
	  	    	  ad.dismiss();
	  	    	  
	  	    } }); 
	  	 
	  	ad.show();
	}

}
