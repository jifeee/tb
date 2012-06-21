package classes;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.joda.time.DateTime;

import com.google.common.io.LittleEndianDataOutputStream;

import android.app.ActivityManager;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

public class Reporter implements LocationListener {

	String TAG="TEX";
	
	public static final int BLUETOOTH_NOTAVAILABLE	= 0;
	public static final int BLUETOOTH_OFF			= 1;
	public static final int BLUETOOTH_ON			= 2;
	public static final int BLUETOOTH_SCANNING		= 3;
	
	public static final int LOCATION_NOTAVAILABLE	= 0;
	public static final int LOCATION_OFF			= 1;
	public static final int LOCATION_ON				= 2;
	public static final int LOCATION_NEW			= 3;
	
	private BluetoothAdapter bluetoothAdapter = null;
	private ActivityManager activityManager = null;
	private LocationManager locationManager = null;
	

	private Long time; 
	private int bt;
	private int gps;
	private int scr;
	private double lat;
	private double lon;
	private double alt;
	int eventCount = 0;

	
	
	Service service;
	
	public Reporter(Service service){
		
		this.service = service;
		activityManager = (ActivityManager) service.getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		locationManager = (LocationManager) service.getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
//		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1, 1, locationListener);
	}
	
	
	public void collectData () throws IOException  {
		
		Log.i(TAG, "collectData");
		
		
		time=new Date().getTime();
		scr=getScreenState();
		bt=getBluetoothState();
		gps=getLocationState();
		
		if (getLocation()!=null) {
		lat=getLocation().getLatitude();
		lon=getLocation().getLongitude();
		alt=getLocation().getAltitude();
		}
		
		else {
			lat=0;
			lon=0;
			alt=0;
		}
		

		Log.i(TAG, "added " + new Date().getTime() + " " + getScreenState() + " " + getBluetoothState() + " " + 
				getLocationState() + " " + lat + " " + lon + " " + 
				alt);
		
		
		eventCount++;
		Log.i(TAG, "evc " + eventCount);
		
	}
	
	public void writeData () throws IOException {
		
		Log.i(TAG, "writeData");
		
		
		
		File f = new File("sdcard/TBevents.file");

		   if (!f.exists())
		   {
		      try
		      {
		
		         f.createNewFile();
		      } 
		      catch (IOException e)
		      {
		    	  Log.i(TAG, e.toString());
		         e.printStackTrace();
		      }
		   }  
		   
		   else {
			   
		   }
	   
		
		LittleEndianDataOutputStream dos = new LittleEndianDataOutputStream(new FileOutputStream(f, true));
	    //event

	      
		      dos.writeLong(time);
		      dos.writeByte(scr);											
		      dos.writeByte(bt);											
		      dos.writeByte(gps);											
		      dos.writeDouble(lat);
		      dos.writeDouble(lon);
		      dos.writeDouble(alt); 
		      
	      
	      dos.close();
	
	}
	
	public void sendData () throws UnknownHostException, IOException {
		
		boolean success = true; 
		
		//create new file
		File f = new File("sdcard/TBwhole.file");
		
		f.delete();
		   if (!f.exists())
		   {
		      try
		      {
		
		         f.createNewFile();
		      } 
		      catch (IOException e)
		      {
		    	  Log.i(TAG, e.toString());
		         e.printStackTrace();
		         success=false; 
		      }
		   }  
		   
		   else {
			   
		   }
		   
			//write header
		   
			 LittleEndianDataOutputStream dos = new LittleEndianDataOutputStream(new FileOutputStream(f));
		      dos.writeBytes("tb");
		      
		      for (int i=0; i<67; i++) {
		    	  dos.writeByte(0);
		      }
		      
		      
		      
		      dos.writeLong(35*eventCount);
		      
		      
		      
		      try
              {
                      //create FileInputStream object for source file
                      FileInputStream fin = new FileInputStream("sdcard/TBevents.file");
                     
                      //create FileOutputStream object for destination file
                      FileOutputStream fout = new FileOutputStream("sdcard/TBwhole.file", true);
                     
                      byte[] b = new byte[40960];
                      int noOfBytes = 0;

                     
                      //read bytes from source file and write to destination file
                      while( (noOfBytes = fin.read(b)) != -1 )
                      {
                              fout.write(b, 0, noOfBytes);
                      }

                     
                      //close the streams
                      fin.close();
                      fout.close();   

                     
              }
              catch(FileNotFoundException fnf)
              {
                      Log.i(TAG, "" + fnf);
                      success=false; 
              }
              catch(IOException ioe)
              {
                     Log.i(TAG, "" + ioe);
                     success=false; 
              }
	   
		
		Log.i(TAG, "sendData");
		
		//write out
	      DataOutputStream os=null;
		try {
			int TCP_SERVER_PORT=8888;
			Socket s = new Socket("192.168.0.40", TCP_SERVER_PORT);

			os = new DataOutputStream(
			      new BufferedOutputStream(s.getOutputStream()));
     
			  
			final BufferedInputStream inStream = new BufferedInputStream(new FileInputStream("/sdcard/TBwhole.file"));

			final byte[] buffer = new byte[40960];
			    for (int read = inStream.read(buffer); read >= 0; read = inStream.read(buffer))
			        os.write(buffer, 0, read);
		} catch (Exception e) {
			success=false; 
		}

        
        if (os!=null) {
        os.flush();
        }
    
        Log.i(TAG, "succ; " + success);
        if (success) {
        
		File f1 = new File("sdcard/TBevents.file");
		File f2 = new File("sdcard/TBwhole.file");
		f1.delete();
		f2.delete();
        
        eventCount = 0;
	
        }
	}
	
	
	
	public String getTopActivity(){
		
		List<ActivityManager.RunningTaskInfo> runningTasks = activityManager.getRunningTasks(4096);	
		return runningTasks.get(0).topActivity.getClassName();
		
	}
	
	public int getBluetoothState(){
		
		if(bluetoothAdapter == null){
			return BLUETOOTH_NOTAVAILABLE;
		} else
			
		if(bluetoothAdapter.isDiscovering()){
			return BLUETOOTH_SCANNING;
		} else	
		
		if(bluetoothAdapter.getState() == BluetoothAdapter.STATE_ON){
			return BLUETOOTH_ON;
		} else
		
		return BLUETOOTH_OFF;

	}
	
	public int getLocationState(){
		
		return 2;
		
//		if(locationManager == null){
//			return LOCATION_NOTAVAILABLE;
//		} 
//		
//		else if 
//			
//		{
//			locationManager.isProviderEnabled( LocationManager.GPS_PROVIDER)
//		}
//		
//		return LOCATION_OFF;

	}
	
	public int getScreenState () {
		return 1;
	}
	

	
	
	
	public String toString(){
		return "BT : " + new Integer(getBluetoothState()).toString() +
			   " | Front : " + getTopActivity();
	}
	
	@Override
	public void onLocationChanged(Location arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderDisabled(String arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderEnabled(String arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		// TODO Auto-generated method stub

	}
	
	public Location getLocation() {
		
		Location l = null;
		Location l2 = null;
		
		locationManager = (LocationManager) service.getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
		if (locationManager != null) {

			
			Criteria criteria = new Criteria();
			
			
			l = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));

			
//			if (l!=null) {
//				Log.i(TAG, d.toString() + l.toString());
//
//			}	
//			
//			if (l2!=null) {
//				Log.i(TAG, d2.toString() + l2.toString());
//
//			}
			

		}
//		
//		else {
//			
//		}
		return l;
	}

	

	}

