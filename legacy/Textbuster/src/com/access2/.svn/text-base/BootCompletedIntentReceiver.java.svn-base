package com.access2;  
  
import android.content.BroadcastReceiver;  
import android.content.Context;  
import android.content.Intent;  
import android.content.SharedPreferences;  
import android.preference.PreferenceManager;  
  
import com.access2.BusterService;  
  
public class BootCompletedIntentReceiver extends BroadcastReceiver {  
	@Override  
	public void onReceive(Context context, Intent intent) {  
		if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {  
		Intent i = new Intent(context, BusterService.class);  
		context.startService(i);
	}  
}  
}  
