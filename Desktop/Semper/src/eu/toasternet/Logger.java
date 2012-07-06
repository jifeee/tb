package eu.toasternet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Hashtable;
import java.util.TreeMap;

import android.util.Log;

/**
 * The Logger class provides an easy interface to prepare and store events locally, then send them to a server for storage in packages.
 * A server-side service is available, written in C and using forks to handle a multitude of requests at once.
 * @author Sascha Klick
 *
 */
public class Logger{
	private static final String TAG = "Logger";
	

	
	/* The data types understood by the server. The integer values for each type must correspond to the one set in the server,
	 * or else the finger print check will fail. 
	 */
	
	private static final int FIELD_ILLEGAL		= -1;
	private static final int FIELD_TINYINT		= 0;
	private static final int FIELD_SMALLINT		= 1;
	private static final int FIELD_INT			= 2;
	private static final int FIELD_BIGINT		= 3;
	private static final int FIELD_FLOAT		= 10;
	private static final int FIELD_DOUBLE		= 11;
	private static final int FIELD_TEXT			= 20;
	
	/* The server config. The address needs to be reachable, the port must be open to the public and running the logger's server service.
	 */
	
	private static final String INET_ADDRESS = "textbuster.mobilezapp.de";
	private static final int INET_PORT = 12348;
	
	/* Add all locations in the devices file system where you would like to have a copy of the log be to stored in.
	 * The logger will gather events from all log files upon send, but never send a duplicate event.
	 * - It makes sense to choose both an internal and external storage location.
	 * - Use hidden directories if possible. 
	 */
	
	private static final String[] LOG_DIRS = new String[]{
		"/sdcard/data/eu.toasternet",
		"/data/data/eu.toasternet"
		//"/data/data/com.toasternet.logapp/backup",
		//Environment.getExternalStorageDirectory() + "/log"
	};
	
	/* The maximum size that a data package can grow to. Events that do not fit into one send package will be send in the next package.
	 */
	
	private int MAX_DATASIZE = 1048576;
	
	/* The maximum single event size. If you plan on submitting very big binary blobs or strings make sure you choose an according size.
	 * The event still has to fit into a package and the server needs to be prepared for such large packages as well.
	 */
	
	private int MAX_EVENTSIZE = 1024 * 64;
	
	/* Fixed binary package sizes in byte. When the overall protocol is changed these need to be adapted as well as must be the server. 
	 */
	
	private int HEADER_SIZE	= 100;
	private int INDEX_SIZE	= 16;
	private int BITFIELD_SIZE = 1;
	private int RESPONSE_SIZE = 16;

	/* Some header fields. Not evaluated by the server at the moment.
	 * Might be used later for versioning of the protocol. 
	 */
	
	private static final short HEADER_MAGIC 	= 0x4E54;
	private static final short HEADER_VERSION	= 1;
	
	
	/* The log sets are defined here. The server provides these on startup.
	 * Do not configure these directly but copy and paste the definition that the server provides on startup.
	 */
	
	private static final String[][] LOGSET_DEFINITIONS = {
		{"state", "TINYINT", "TINYINT", "TINYINT", "TINYINT", "TINYINT", "TEXT"},
		{"gps", "BIGINT", "DOUBLE", "DOUBLE", "DOUBLE", "DOUBLE", "DOUBLE", "DOUBLE"}
	};

		
	/* Some globals used elsewhere in the class.
	 */
	
	private static final Hashtable<String, Integer> LOG_SETS = new Hashtable<String, Integer>();
	private int[][] LOG_FIELDS;
	private Object[][] LOG_VALUES;
	
	private long LAST_LOG = 0;
	private byte[] SET_HASH;
	
	private byte[] uuid = new byte[68];
	
	/**
	 * Helper class to store event information in the events Treemap.
	 */
	
	private class Event{
		public byte[] data;
		public int type;
		
		public Event(byte[] data, int type){
			this.data = data;
			this.type = type;
		}
	};
	
	/**
	 * @param uuid Some kind of identifier that is send to the server and serves to recognize the device.
	 */
	
	public Logger(String uuid){
		
		Log.d(TAG, uuid);
		
		/* Turn the UUID string into a byte array.
		 */
		
		for(int i = 0; i < uuid.length(); i++){
			this.uuid[i] = uuid.getBytes()[i];
		}
		
		/* Parse the set definition and create a more simple int-array-representaion of its information.
		 */
		
		LOG_FIELDS = new int[LOGSET_DEFINITIONS.length][];
		LOG_VALUES = new Object[LOGSET_DEFINITIONS.length][];
	
		for(int i = 0; i < LOGSET_DEFINITIONS.length; i++){
			String[] set = LOGSET_DEFINITIONS[i];
			String set_name = set[0];
			int[] fields = new int[set.length - 1];
			
			for(int f = 0; f < set.length - 1; f++){
				String field_type = set[f + 1]; 
				
				if(field_type.equals("TINYINT")){
					fields[f] = FIELD_TINYINT; 
				}else if(field_type.equals("SMALLINT")){
					fields[f] = FIELD_SMALLINT; 
				}else if(field_type.equals("INT")){
					fields[f] = FIELD_INT; 
				}else if(field_type.equals("BIGINT")){
					fields[f] = FIELD_BIGINT; 
				}else if(field_type.equals("FLOAT")){
					fields[f] = FIELD_FLOAT; 
				}else if(field_type.equals("DOUBLE")){
					fields[f] = FIELD_DOUBLE; 
				}else if(field_type.equals("TEXT")){
					fields[f] = FIELD_TEXT;
				}else{
					fields[f] = FIELD_ILLEGAL;
					Log.d(TAG, "Unknown field '" + field_type + "' in set '" + set_name + "'");
					continue;
				}
				
			}
			
			LOG_SETS.put(set_name, i);
			LOG_FIELDS[i] = fields;
		}
		
		/* Calculate the set's finger print. 
		 * The finger print is the SHA-1 hash (20 bytes) of a string created by iterating through the set, putting together a string containing the relevant structure of the set.
		 * The finger print is send in the header of each package. The server will reject a package send with a finger print that does not match its internal set's finger print,
		 * which is created by the server the same way as it is by this class.
		 */
		
		String set_fingerprint = "";
		
		int set_i = 0;
		for(int[] set : LOG_FIELDS){
			set_fingerprint += "[" + set_i + "] ";
			for(int field : set){
				set_fingerprint += field + " ";
			}
			set_i++;
		}
		
		MessageDigest set_digest = null;
		try {
			set_digest = MessageDigest.getInstance("SHA-1");
			byte[] set_hash = set_digest.digest(set_fingerprint.getBytes("UTF-8"));
			
			Log.d(TAG, "Set Fingerprint: \"" + set_fingerprint + "\"");
			Log.d(TAG, "Set Hash       : \"" + new BigInteger(1, set_hash).toString(16).toUpperCase() + "\"");
			
			this.SET_HASH = set_hash;
			
		} catch (NoSuchAlgorithmException e) {
			Log.d(TAG, "Illegal hash algorithm");
		} catch (UnsupportedEncodingException e) {
			Log.d(TAG, "Illegal encoding");
		}			
		
		/* Clear the sets - even though none should be set - just to be sure.
		 */
		clear();
	}
	
	public boolean send(){
		
		/* Get as many new events from as many log files as possible.
		 */
		
		TreeMap<Long, Event> events = gatherEvents();
		
		/* Do not do anything if no new events are available.
		 */
		
		if(events.size() == 0){
			return false;
		}
		
		/* Put together the first part of the header's binary as it will be send over the socket.
		 */
		
		byte[] header_bytes = new byte[HEADER_SIZE];
		ByteBuffer header_buf = ByteBuffer.wrap(header_bytes);
		header_buf.order(java.nio.ByteOrder.LITTLE_ENDIAN);
		
		header_buf.putShort(HEADER_MAGIC);
		header_buf.putShort(HEADER_VERSION);
		header_buf.putInt(events.size());
		
		/* Iterate over the list of events, putting together the event index's binary in the process.
		 * TreeMap makes sure that they are in chronological order.
		 */
		
		byte[] index_bytes = new byte[events.size() * INDEX_SIZE];
		ByteBuffer index_buf = ByteBuffer.wrap(index_bytes);
		index_buf.order(java.nio.ByteOrder.LITTLE_ENDIAN);
		
		int data_total_size = 0;
		
		for (TreeMap.Entry<Long, Event> event : events.entrySet())
		{
			
			/* Put together the index data.
			 */
			
			index_buf.putLong(event.getKey());
		    Event value = event.getValue();
			
		    if(value == null){
		    	index_buf.putInt(0);
		    }else{
		    	index_buf.putInt(value.data.length);
		    	data_total_size += value.data.length;
		    }
			index_buf.putInt(value.type);
	
		}
		
		/* Put together the second part of the header in binary.
		 */
		
		header_buf.putInt(data_total_size);
		
		header_buf.put(uuid, 0, 68);
		header_buf.put(SET_HASH, 0, 20);
		
		/* Put together the data binary package as it will be send over the socket.
		 */
		
		byte[] data_bytes = new byte[data_total_size];
		ByteBuffer data_buf = ByteBuffer.wrap(data_bytes);
		
		for (TreeMap.Entry<Long, Event> event : events.entrySet())
		{
			byte[] event_data = event.getValue().data;
			
			if(event_data != null){
				data_buf.put(event_data);
			}
		}		
	
		try {
			
			/* Open a socket to the server.
			 */
			
			Socket clientSocket = new Socket(INET_ADDRESS, INET_PORT);
			OutputStream out = clientSocket.getOutputStream();
			DataInputStream in = new DataInputStream(clientSocket.getInputStream());
				
			/* Send the binary header, index and data packages.
			 */
			
			out.write(header_bytes);
			out.write(index_bytes);
			out.write(data_bytes);
			
			/* Receive the response from the server.
			 */
			
			byte[] res_bytes = new byte[RESPONSE_SIZE];
			ByteBuffer res_buf = ByteBuffer.wrap(res_bytes);
			res_buf.order(java.nio.ByteOrder.LITTLE_ENDIAN);
			
			in.read(res_bytes);
			
			long res_state = res_buf.getLong(0);
			long res_last_log = res_buf.getLong(8);
			
			if(res_state == 0){
				/* If the server reports no error, do a garbage collection in the log files.
				 */
				
				LAST_LOG = res_last_log;
				
				this.log_gc();
			}else{
				/* If the server reports an error, purging the log files becomes necessary.
				 * Since log files cannot be repaired if the server reports that they are broken, they must be deleted or else the sending of newer logs will never succeed.
				 */
				
				Log.d(TAG, "Server reported an error (" + res_state + ")");
				
				if(res_state == 10){
					purge();
				}
			}
			
		}	
		catch(IOException ioe) {
			/* This is likely to happen if the server is unavailable.
			 */
			Log.d(TAG, "Sending status failed");
			return false; 
			
		}

		
		return true;
		
	}

	/**
	 * Write the event including all prepared sets to the log files. Use event type (0 = undefined vanilla event). 
	 */
	public void write(){
		this.write(0);
	}
	
	/**
	 * Write the event including all prepared sets to the log files.
	 * @param event_type Use this to identify the reason why an event was send, e.g. by a repeating timer, a broadcast listener, due to an error or user input. The event type will be stored along with the other basic event data instead of in a set on the server
	 */
	
	public void write(int event_type){
		
		Long time_stamp = new Date().getTime();
		
		ByteBuffer data_buf = ByteBuffer.allocateDirect(1024);
		data_buf.order(java.nio.ByteOrder.LITTLE_ENDIAN);
		
		for (String logdir: LOG_DIRS)
	    {

			File dir_log = new File(logdir);
			if(!dir_log.exists()){
				if(dir_log.mkdirs() == false){
					Log.d(TAG, "Could not create " + logdir);
					continue;
				}
				Log.d(TAG, "Created " + logdir);
			}
			
			String fn_index = logdir + "/l1";
			String fn_data = logdir + "/l2";
			
			File file_index = new File(fn_index);
			File file_data = new File(fn_data);
			
			if(!file_index.exists()){
				try {
					file_index.createNewFile();
					file_data.delete();
				} catch (IOException e) {
					Log.d(TAG, "Failed to create " + fn_index);
					continue;
				}
			}
			
			if(!file_data.exists()){
				try {
					file_data.createNewFile();
					file_index.delete();
				} catch (IOException e) {
					Log.d(TAG, "Failed to create " + fn_data);
					continue;
				}
			}
		
			DataOutputStream out_index = null;
			FileOutputStream out_data = null;
			try {
				out_index = new DataOutputStream(new FileOutputStream(file_index, true));
				
				data_buf.clear();
				
				// Assemble gathered LOG_VALUES data into a byte 
				
				for(int i = 0; i < BITFIELD_SIZE; i++){
					data_buf.put((byte) 0);
				}
				
				for(int i = 0; i < LOG_FIELDS.length; i++){
					if(LOG_VALUES[i] == null){
						continue;
					}
					
					byte bitfield = (byte)(data_buf.get(i / 8) | (1 << (i % 8)));
					data_buf.put(i / 8, new Byte(bitfield));
					
					Object[] values = LOG_VALUES[i]; 
					int[] set = LOG_FIELDS[i];
					for(int f = 0; f < set.length; f++){
						int field_type = set[f];
						
						if(field_type == FIELD_TINYINT){
							data_buf.put((Byte)values[f]);
						}else if(field_type == FIELD_SMALLINT){
							data_buf.putShort((Short)values[f]);
						}else if(field_type == FIELD_INT){
							data_buf.putInt((Integer)values[f]);
						}else if(field_type == FIELD_BIGINT){
							data_buf.putLong((Long)values[f]);
						}else if(field_type == FIELD_FLOAT){
							data_buf.putFloat((Float)values[f]);
						}else if(field_type == FIELD_DOUBLE){
							data_buf.putDouble((Double)values[f]);
						}else if(field_type == FIELD_TEXT){
							String text = (String)values[f];
							data_buf.put(text.getBytes("UTF-8"));
							data_buf.put((byte)0);
						}
					}
					
				}
				
				data_buf.flip();
				
				out_index.writeLong(time_stamp);
				out_index.writeInt(data_buf.limit());
				out_index.writeInt(event_type);
				
				out_data = new FileOutputStream(file_data, true);
				out_data.getChannel().write(data_buf);
				
			} catch (FileNotFoundException e) {
				Log.d(TAG, "Failed to find for writing " + e);
				continue;
			} catch (IOException e) {
				Log.d(TAG, "Failed to write " + e);
				continue;
			} finally {
				
				if (out_index != null) {
					try {
						out_index.close();
					} catch (IOException e) {
						Log.d(TAG, "Failed to close " + fn_index);
						continue;
					}
				}
				
				if (out_data != null) {
					try {
						out_data.close();
					} catch (IOException e) {
						Log.d(TAG, "Failed to close " + fn_data);
						continue;
					}
				}
			}

			//Log.d(TAG, fn_index + " " + new Long(file_index.length()).toString());
	    }
		
		clear();
	
	}
	
	/**
	 * Prepare a set for the next event. If the same set if prepared a second time before the event is written, the new values will overwrite the old values.
	 * @param set_name Use a valid set name as defined in the set definitions hard-coded into the class. An invalid set name will yield an error.
	 * @param values The values must be cast into the corresponding data type objects (e.g. "Short" for SMALLINT). Type checking is rigorous. 
	 * @return Whether preparing the set succeed. An invalid set name or values not adhering to the set definition will produce a negative return.
	 */
	
	public boolean set(String set_name, Object ... values){
		
		/* Check if the set specified by the name exists. 
		 */
		
		if(!LOG_SETS.containsKey(set_name))
		{
			Log.d(TAG, "Illegal set '" + set_name + "'");
			return false;
		}
		
		/* Check the number of fields in the values array.
		 */
		
		int set_id = LOG_SETS.get(set_name);
		
		int[] set = LOG_FIELDS[set_id];
		
		if(set.length != values.length){
			Log.d(TAG, "Number of field values (" + values.length + ") not equal number of fields (" + set.length + ") for set '" + set_name + "'");
			return false;
		}
		
		/* Iterate through the field of the set, checking if the provided objects are acceptable for the field type.
		 */
		
		boolean valid = true;
		
		for(int i = 0; i < set.length; i++){
			int field_type = set[i];
			Object field_object = values[i];
			String field_classname = field_object.getClass().getName();
			
			if(field_type == FIELD_TINYINT){
				if(!field_classname.equals("java.lang.Byte")){
					Log.d(TAG, "Illegal value of type '" + field_classname + "' for TINYINT field " + i + " in set '" + set_name + "'");
					valid = false;
					continue;
				}
			}else if(field_type == FIELD_SMALLINT){
				if(!field_classname.equals("java.lang.Short")){
					Log.d(TAG, "Illegal value of type '" + field_classname + "' for SMALLINT field " + i + " in set '" + set_name + "'");
					valid = false;
					continue;
				}
			}else if(field_type == FIELD_INT){
				if(!field_classname.equals("java.lang.Integer")){
					Log.d(TAG, "Illegal value of type '" + field_classname + "' for INT field " + i + " in set '" + set_name + "'");
					valid = false;
					continue;
				}
			}else if(field_type == FIELD_BIGINT){
				if(!field_classname.equals("java.lang.Long")){
					Log.d(TAG, "Illegal value of type '" + field_classname + "' for BIGINT field " + i + " in set '" + set_name + "'");
					valid = false;
					continue;
				}
			}else if(field_type == FIELD_FLOAT){
				if(!field_classname.equals("java.lang.Float")){
					Log.d(TAG, "Illegal value of type '" + field_classname + "' for FLOAT field " + i + " in set '" + set_name + "'");
					valid = false;
					continue;
				}
			}else if(field_type == FIELD_DOUBLE){
				if(!field_classname.equals("java.lang.Double")){
					Log.d(TAG, "Illegal value of type '" + field_classname + "' for DOUBLE field " + i + " in set '" + set_name + "'");
					valid = false;
					continue;
				}
			}else if(field_type == FIELD_TEXT){
				if(!field_classname.equals("java.lang.String")){
					Log.d(TAG, "Illegal value of type '" + field_classname + "' for TEXT field " + i + " in set '" + set_name + "'");
					valid = false;
					continue;
				}
			}else{
				Log.d(TAG, "Illegal field type " + field_type + " in set '" + set_name + "' field " + i);
				valid = false;
				continue;
			}
		
		}
		
		/* Store the values if they ALL were valid according to the set definition. 
		 */
		
		if(valid){
			LOG_VALUES[set_id] = values;
		}
		
		return valid;
		
	}
	
	/**
	 * Clear all prepared sets. Writing an event after this will yield an event with only a time stamp and event type.
	 */
	
	public void clear(){
		for(int i = 0; i < LOG_VALUES.length; i++){
			LOG_VALUES[i] = null;
		}
	}
	
	/**
	 * Collect events from the log files, returning a Treemap that contains inly unique events and only as many as would fit into the maximum data size.
	 * @return The least recent events gathered from all log files.
	 */
	private TreeMap<Long, Event> gatherEvents(){
		
		/* Iterate over each log file location.
		 */
		
		TreeMap<Long, Event> events = new TreeMap<Long, Event>();
		int total_data_size = 0;
		
		for (String logdir: LOG_DIRS){
			
			/* Prepare a single set of log files for analysis.
			 */
			
			String fn_index = logdir + "/l1";
			String fn_data = logdir + "/l2";
			
			File file_index = new File(fn_index);
			File file_data = new File(fn_data);
			int index_count = 0;
			long time_stamp = 0;
			
			try {
				DataInputStream in = new DataInputStream(new FileInputStream(file_index));
				FileInputStream data_in = new FileInputStream(file_data);
				
				/* Walk through all events in the log file.
				 */
				
				while(index_count < file_index.length() / INDEX_SIZE){
					
					time_stamp = in.readLong();
					int data_size = in.readInt();
					int event_type = in.readInt();
					
					/* Ignore all events that are older than the newest log the server has reported he has received from this device.
					 */
					
					if(time_stamp > LAST_LOG){
						
						/* Maybe we should rather check here if the event already exists the Treemap at this point.
						 */
						
						/* Events that contain extra data in form of sets will have their data stored alongside its type in the Event object.
						 */
						
						if(data_size > 0){
							
							/* Ignore events that would break the limit of the maximum data size. These will be send later.
							 */
							
							if(total_data_size + data_size > MAX_DATASIZE){
								break;
							}
							
							byte data[] = new byte[data_size];
							if(data_in.read(data) != data.length){
								Log.d(TAG, "Data file reading failed " + fn_data);
								break;
							}
							
							Event res = events.put(time_stamp, new Event(data, event_type));
							
							/* Only if the event was not already read from a previous log will it's data size count towards the grant total.
							 */
							
							if(res == null){
								total_data_size += data_size;
							}
						}else{
							events.put(time_stamp, new Event(null, event_type));
						}
						
					}else{
						
						/* If the event was ignored it's data size must still be taken into account when reading thre data file.
						 */
						
						if(data_size > 0){
							data_in.skip(data_size);
						}
						
					}
					
					index_count++;
				}
				
			} catch (FileNotFoundException e) {
				// This is more or less expected after a purge, so we do not report it in logcat.
				//Log.d(TAG, "Log file not found " + e);
				continue;
			} catch (IOException e) {
				Log.d(TAG, "Read error in " + e);
				continue;
			}
		}
		
		return events;
		
	}
	
	// Remove all old events from log files.
	private int log_gc(){
		
		/* Iterate over each log file location.
		 */
		
		int collected_bytes = 0;
		
		for (String logdir: LOG_DIRS){

			try{
			
				String fn_index = logdir + "/l1";
				String fn_data = logdir + "/l2";
	
				File file_index = new File(fn_index);
				File file_data = new File(fn_data);
				
				if(!file_index.exists()){
					file_data.delete();
				}
				
				if(!file_data.exists()){
					file_index.delete();
				}
	
				long index_length = file_index.length();
				long data_length = file_data.length();
	
				RandomAccessFile index_in = new RandomAccessFile(file_index, "r");
				
				long last_index = index_length - INDEX_SIZE;
				long last_data = data_length;
	
				while(last_index > 0){
					
					index_in.seek(last_index);
					
					long time_stamp = index_in.readLong();
					int data_size = index_in.readInt();
					
					if(time_stamp <= LAST_LOG) break;
					
					last_data -= data_size;
					last_index -= INDEX_SIZE;
				}
	
				last_index = last_index + INDEX_SIZE;
	
				FileInputStream data_in = new FileInputStream(file_data);
	
				File index_tmp = new File(fn_index + ".tmp");
				File data_tmp = new File(fn_data + ".tmp");
	
				FileOutputStream index_tmp_fos = new FileOutputStream(index_tmp);
				FileOutputStream data_tmp_fos = new FileOutputStream(data_tmp);
				
				index_in.getChannel().transferTo(last_index, index_length - last_index, index_tmp_fos.getChannel());
				data_in.getChannel().transferTo(last_data, data_length - last_data, data_tmp_fos.getChannel());
				
				index_in.close();
				index_tmp_fos.close();
				data_in.close();
				data_tmp_fos.close();
	
				file_index.delete();
				file_data.delete();
	
				index_tmp.renameTo(file_index);
				data_tmp.renameTo(file_data);
				
				collected_bytes += last_index + last_data;
			
			}catch(FileNotFoundException e){
				Log.d(TAG, "GC: File not found" + e);
			} catch (IOException e) {
				Log.d(TAG, "GC: Could not writ/read file" + e);
			}
		}
		
		Log.d(TAG, "GC: Removed " + collected_bytes + " bytes from before " + LAST_LOG);
		
		return collected_bytes;
		
	}
	
	/**
	 * Delete all log files without sending them to the server. Use this if a user actively requests the purging of the persisting logs or storage problems make the logs too much of a burden.
	 * Purging the files becomes necessary when they are corrupted. It is unlikely to extract valid event data from corrupt files, so purging is a valid option at that point. 
	 */
	
	public void purge(){
		for (String logdir: LOG_DIRS){
	
			String fn_index = logdir + "/l1";
			String fn_data = logdir + "/l2";
			
			new File(fn_index).delete();
			new File(fn_data).delete();
			
		}
		
		Log.d(TAG, "Logs purged");
	}
	
}