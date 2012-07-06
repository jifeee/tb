package eu.toasternet;


import eu.toasternet.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class SemperActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        Context ctx = this.getBaseContext();
        Intent service = new Intent(ctx, SemperService.class);
        ctx.startService(service);
        
        final Button change = (Button) findViewById(R.id.button1);
        change.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	
    	        finish();

            }
        });

    }
}