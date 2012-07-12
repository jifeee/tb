package classes;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.os.*;
import android.telephony.*;

//class to retrieve imei, just in case TelephonyManager returns invalid result; TODO: find out how to get SystemProperties

public class Imei {
	
	boolean gotValidIMEI = false; 
	
	public String getImei (Context ctx) {
		String imei = "unknown";
		
		TelephonyManager tm = (TelephonyManager)ctx.getSystemService(Context.TELEPHONY_SERVICE);
		
		if (tm!=null && !tm.getDeviceId().equals("") && !tm.getDeviceId().equals("000000000000000")) {
			imei = tm.getDeviceId();
			return imei;
		}
		
		else {
//			imei = android.os.SystemProperties.get(android.telephony.TelephonyProperties.PROPERTY_IMEI);
		}

		return imei;
	}

}
