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


	
	public String imei (String imei) {
		String fimei = imei;
				
		while (fimei.length()<64) {
			fimei = fimei.concat("0");
		}
		
		
		return fimei;
	}
	
	
}
