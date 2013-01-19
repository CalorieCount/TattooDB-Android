package com.Tattoo.db;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.util.Log;

import com.google.gson.Gson;

/**
 * Origin of the name
 * https://twitter.com/spboyer/statuses/237650818550091776
 */
public final class TattooDB {
	private static final String TAG = "TattooDB";
	private DBImplementation db;
	private Gson gson = new Gson();
	private String prefix;
	private Exception lastException = null;
	
	public Exception getLastException() { return this.lastException; }
	
	public TattooDB(String prefixValue, DBImplementation value) {
		this.prefix = prefixValue;
		this.db = value;
	}
	
	public Boolean put(Calendar key, Object value) {
		return put(key.getTimeInMillis(), value);
	}
	
	public Boolean put(Date key, Object value) {
		return put(key.getTime(), value);
	}
	
	public Boolean put(long key, Object value) {
		return put(Long.toString(key), value);
	}
	
	public Boolean put(int key, Object value) {
		return put(Integer.toString(key), value);
	}
	
	public Boolean put(String key, Object value) {
		if (key == null || value == null) {
			Log.w(TAG, "Cannot put a null value");
			return false;
		}
		
		String normalizedId = normalizeId(key);
		String typename = value.getClass().getName();
		
		try {
			// serialize the object
			String payload = gson.toJson(value);
			
			// get the typename and verify the store exists
			this.db.verifyStore(typename);
			
			// query by ID
			String existing = this.db.query(normalizedId, typename);
			
			// if exists, update
			Boolean wasSuccessful = false;
			
			if (existing != null) {
				wasSuccessful = this.db.update(normalizedId, typename, payload);

				if (wasSuccessful) Log.v(TAG, String.format("update '%s' in %s: %s", normalizedId, typename, payload));
				else Log.w(TAG, String.format("failed to update '%s' in %s: %s", normalizedId, typename, payload));
			}
			else {
				wasSuccessful = this.db.insert(normalizedId, typename, payload);

				if (wasSuccessful) Log.v(TAG, String.format("inserted '%s' in %s: %s", normalizedId, typename, payload));
				else Log.w(TAG, String.format("failed to insert '%s' in %s: %s", normalizedId, typename, payload));
			}
			
			return wasSuccessful;
		}
		catch (Exception ex) {
			Log.e(TAG, String.format("error putting '%s' in %s: %s", normalizedId, typename, ex.getMessage()));
			this.lastException = ex;
			return false;
		}
	}
	
	public <T> T get(Calendar id, Class<? extends T> impl) {
		return get(id.getTimeInMillis(), impl);
	}
	
	public <T> T get(Date id, Class<? extends T> impl) {
		return get(id.getTime(), impl);
	}
	
	public <T> T get(long id, Class<? extends T> impl) {
		return get(Long.toString(id), impl);
	}
	
	public <T> T get(int id, Class<? extends T> impl) {
		return get(Integer.toString(id), impl);
	}
	
	public <T> T get(String id, Class<? extends T> impl) {
		String normalizedId = normalizeId(id);
		String typename = impl.getName();
		
		try {
			// query by id
			String result = this.db.query(normalizedId, typename);
			
			if (result == null) {
				Log.v(TAG, String.format("did not find '%s' in %s", normalizedId, typename));
				return null;
			}

			Log.v(TAG, String.format("found '%s' in %s: %s", normalizedId, typename, result));
			
			// deserialize the result
			T deserialized = gson.fromJson(result, impl);
			
			return deserialized;
		}
		catch (Exception ex) {
			Log.e(TAG, String.format("error looking for '%s' in %s: %s", normalizedId, typename, ex.getMessage()));
			this.lastException = ex;
			return null;
		}
	}
	
	public <T> List<T> get(String[] ids, Class<? extends T> impl) {
		String typename = impl.getName();
		
		try {
			// normalize the IDs passed in
			String[] normalizedIds = new String[ids.length];
			for(int i=0;i<ids.length;i++) {
				normalizedIds[i] = normalizeId(ids[i]);
			}
			
			
			String[] results = this.db.queryIn(normalizedIds, typename);
		
			ArrayList<T> serializedList = new ArrayList<T>(results.length);
			
			for(String result : results) {
				T deserialized = gson.fromJson(result, impl);
				serializedList.add(deserialized);
			}
			
			return serializedList;
		}
		catch(Exception ex) {
			Log.e(TAG, String.format("Error querying for Ids in %s", typename));
			this.lastException = ex;
			return new ArrayList<T>(0);
		}
	}
	
	public <T> Boolean delete(String id, Class<? extends T> impl) {
		String normalizedId = normalizeId(id);
		String typename = impl.getName();
		
		try {	
			// pass delete to implementation
			Boolean result = this.db.delete(normalizedId, typename);
			
			if (result) Log.v(TAG, String.format("deleted '%s' in %s", normalizedId, typename));
			else Log.w(TAG, String.format("failed to delete '%s' in %s", normalizedId, typename));
			
			return result;
		}
		catch (Exception ex) {
			Log.e(TAG, String.format("error deleting for '%s' in %s: %s", normalizedId, typename, ex.getMessage()));
			this.lastException = ex;
			return false;
		}
	}
	
	private String normalizeId(String id) {
		return this.prefix + "|" + id;
	}
	
	public interface DBImplementation {
		public void verifyStore(String name);
		public String query(String id, String storeName);
		public String[] queryIn(String[] ids, String storeName);
		public Boolean insert(String id, String storeName, String blob);
		public Boolean update(String id, String storeName, String blob);
		public Boolean delete(String id, String storeName);
	}
}
