package classes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.UUID;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Address;
import android.location.Geocoder;
import android.telephony.SmsManager;
import android.util.Log;

public class SmsSender {
	
	String TAG = "TEX";
	boolean gotStartAdr = false;
	boolean gotEndAdr = false;
	Context ctx;
	ArrayList<TripForSms> trips = new ArrayList<TripForSms>();

	
	
	public void addTrip (TripForSms tfs, Context ctx) {
		this.ctx = ctx;
		
		trips.add(tfs);
		Log.d(TAG, "Added: " + tfs.toString());
		
		
	}
	
	public void sendTrip (TripForSms tfs) {
		
		Log.d(TAG, "Trying to send: " + tfs.toString());
		
		getAddress(tfs);
		
		Log.d(TAG, "startAdr: " + tfs.getStartAdr());
		Log.d(TAG, "endAdr: " + tfs.getEndAdr());
		
		if (tfs.getStartAdr() != null && tfs.getEndAdr() != null && !tfs.getStartAdr().equals("") && !tfs.getEndAdr().equals("")) {
		
			sendSms(tfs);
		}
		
		
	}
	
	public void getAddress (TripForSms tfs) {
		Log.d(TAG, "Get address for : " + tfs.toString());
		
		Geocoder gc = new Geocoder(ctx, Locale.US);
		
		try {
			Address sa = gc.getFromLocation(tfs.getStartLoc().getLatitude(), tfs.getStartLoc().getLongitude(), 1).get(0);
			Log.d(TAG, sa.toString());
			
			String startAdr="";
			int i=0;
			while (sa.getAddressLine(i)!=null && i<2) {
				startAdr = startAdr.concat(sa.getAddressLine(i) + ", ");
				i++;
			}
			
			tfs.setStartAdr(startAdr);
			
			Address ea = gc.getFromLocation(tfs.getEndLoc().getLatitude(), tfs.getEndLoc().getLongitude(), 1).get(0);
			Log.d(TAG, ea.toString());
			
			String endAdr="";
			int j=0;
			while (ea.getAddressLine(j)!=null && j<2) {
				endAdr = endAdr.concat(ea.getAddressLine(j) + ", ");
				j++;
			}
			
			tfs.setEndAdr(endAdr);
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.d(TAG, e.toString());
			e.printStackTrace();
		}

	}
	
	public void sendSms (TripForSms tfs) {
		
		Log.d(TAG, "Sending: " + tfs.toString());
		
      PendingIntent sentIntent = PendingIntent.getBroadcast(ctx, 0,new Intent("TRIP_SMS_SENT").putExtra("UUID", tfs.getId().toString()), 0);
   
      ctx.registerReceiver(new BroadcastReceiver(){
          @Override
          public void onReceive(Context context, Intent intent) {
              Log.d(TAG, "SMS sent intent received.");
              
              String id = intent.getStringExtra("UUID");
              Log.d(TAG, "ID from intent: " + id + " ID from tfs: " + trips.get(0).getId());
              remove (id);
   
          }
      }, new IntentFilter("TRIP_SMS_SENT"));
      SmsManager sm = SmsManager.getDefault();
       String number = "01622896226";
       sm.sendTextMessage(number, null, "Trip from " + tfs.getStartAdr() + "to " + tfs.getEndAdr(), sentIntent, null);
		
	}
	
	public void check (Context ctx) {
		Log.d(TAG, "checking, size:  " + trips.size());
		
		this.ctx = ctx;
		
		if (trips.size()==0) {
			return;
		}
		
		else {
			
			for (int i=0; i<trips.size(); i++) {
				sendTrip(trips.get(i));
			}
			
		}
	}
	
	public void remove (String id) {
		
		for (int i=0; i<trips.size(); i++) {
			Log.d(TAG, "In remove: , id: " + id + " id in tfs: " + trips.get(i).getId());
			if (id.equals( trips.get(i).getId())) {
				
				Log.d(TAG, "Removing: " + trips.get(i).toString());
				trips.remove(i);
				
				break;
			}
			
		}
		
	}

}
