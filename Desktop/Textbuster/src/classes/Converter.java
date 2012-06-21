	package classes;

import android.util.Log;

public class Converter {
	
	String TAG = "TEX";
	
//	Some methods to to convert numbers and strings
	
	public int convertDouble(double d){
		String str = String.valueOf(d);
		
		if (str.charAt(1) == '.') {
			str = new StringBuffer(str).insert(0, "0").toString();
		}
		
		
		String str2=str.replace(".","");
		while (str2.length()!=8){
			if (str2.length()>8){
				str2=removeCharAt(str2,8);
				
			}
			
			if (str2.length()<8){
				str2 = appendChar(str2);
			
			}
		}

		
		int i = Integer.parseInt(str2);
		return i;
	}
	
	public static String removeCharAt(String s, int pos) {
		   StringBuffer sb = new StringBuffer( s.length() - 1 );
		   sb.append( s.substring(0,pos) ).append( s.substring(pos+1) );
		   return sb.toString();
		}
	
	public static String appendChar(String s){
		StringBuffer sb = new StringBuffer(s);
		char c='0';
		sb.append(c);
		return sb.toString();

	}

	public String normalizeDateString(String dateString) {
		String nDateString=dateString;
		
		if (dateString.length()==28) {
			nDateString=removeCharAt(nDateString, 19);
			nDateString=removeCharAt(nDateString, 19);
			nDateString=removeCharAt(nDateString, 19);
		}
		
		if (dateString.length()==29) {
			nDateString=removeCharAt(nDateString, 19);
			nDateString=removeCharAt(nDateString, 19);
			nDateString=removeCharAt(nDateString, 19);
			nDateString=removeCharAt(nDateString, 19);
		}
		
		if (dateString.length()==27) {
			nDateString=removeCharAt(nDateString, 19);
			nDateString=removeCharAt(nDateString, 19);

		}
		
		return nDateString;
	}

	
	
	
	
	// geht nicht!
	public int checkLatLon (int value) {
		
		String v = String.valueOf(value);
		String x = "0";
		Log.i(TAG, "CONVERTER: " + v);
		if (String.valueOf(v.charAt(1)).equals(".")) {
			
			x=x.concat(v);
			x=removeCharAt(x, x.length()-1);
			value = Integer.parseInt(x);
			Log.i(TAG, String.valueOf(x));
		}
		return value; 
	}
	
	public String imei (String imei) {
		String fimei = imei;
				
		while (fimei.length()<65) {
			fimei = fimei.concat("0");
		}
		
		
		return fimei;
	}
	
	
}
