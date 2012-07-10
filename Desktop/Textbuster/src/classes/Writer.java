package classes;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import android.util.Log;

public class Writer {
	
	String TAG = "TEX";

	public void appendLog(String text)
	{
	   File logFile = new File("/sdcard/TBlogv2.file");
	   if (!logFile.exists())
	   {
	      try
	      {
	         logFile.createNewFile();
	      } 
	      catch (IOException e)
	      {
	         e.printStackTrace();
	         Log.i(TAG, "Writer appendLog: " + e.toString());
	      }
	   }
	   try
	   {
	     
	      BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true)); 
	      buf.append(text);
	      buf.newLine();
	      buf.close();
	   }
	   catch (IOException e)
	   {
	      e.printStackTrace();
	   }
	}
	
	public void sendLog() {
		
		Socket s = new Socket();
		BufferedWriter out;

		try {
			
			FileInputStream fs = new FileInputStream("/sdcard/TBlog.file");

			  DataInputStream in = new DataInputStream(fs);
			  BufferedReader br = new BufferedReader(new InputStreamReader(in));
			  String str;
			  StringBuilder sb = new StringBuilder();

			  while ((str = br.readLine()) != null)   {
		
				  Log.i(TAG, "From Logfile: " + str);
				  sb.append(str);
			  }
			
			InetAddress serverAddress = InetAddress.getByName("192.168.0.40");
			s.connect(new InetSocketAddress(serverAddress,10656),3000);
			out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
			
			out.write(sb.toString());

		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.i(TAG, "Writer sendLog: " + e.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.i(TAG, "Writer sendLog: " + e.toString());
		}
        
        
	}
}
