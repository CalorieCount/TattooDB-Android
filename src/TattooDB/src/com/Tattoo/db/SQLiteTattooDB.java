package com.Tattoo.db;

import com.Tattoo.db.TattooDB.DBImplementation;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class SQLiteTattooDB implements DBImplementation {
	private static final String TAG = "TattooDB";
	private static final String CHECK_IF_TABLE_EXISTS = "select count() from sqlite_master where name = '%s'";
    private static final String CREATE_TABLE = "create table if not exists [%s] (rowId text, blob text, indexValue text);";
    private SQLiteDatabase db;
	
	public SQLiteTattooDB(SQLiteDatabase value) {
		db = value;
	}
	
	@Override
	public void verifyStore(String name) {
		String tableCheckSql = String.format(CHECK_IF_TABLE_EXISTS, name);
		long count = db.compileStatement(tableCheckSql).simpleQueryForLong();
		if (count <= 0) {
			// this store does not exist, let's make it
			Log.i(TAG, String.format("creating table '%s'", name));
			
			String tableCreateSql = String.format(CREATE_TABLE, name);
			db.execSQL(tableCreateSql);
		}
	}

	@Override
	public String query(String id, String storeName) {
		Cursor c = this.db.query(
				prepareStoreName(storeName), 
				new String[] { "blob" }, 
				"rowId=?", 
				new String[] { id }, 
				null, 
				null, 
				null);
		
		String result = null;
		
		if (c.moveToFirst()) {
			result = c.getString(0);
		}
		c.close();
		return result;
	} 
	
	@Override
	public String[] queryIn(String[] ids, String storeName) {
		String[] qs = new String[ids.length];
		for(int i=0;i<ids.length;i++) qs[i] = "?"; 
				
		Cursor c = this.db.query(
				prepareStoreName(storeName),
				new String[] { "blob" },
				String.format("rowId in (%s)", ConversionUtil.join(qs, ",")),
				ids,
				null, null, null);
		
		String[] results = new String[c.getCount()];
		
		int i =0;
		while(c.moveToNext()) {
			results[i] = c.getString(0);
			i++;
		}
		c.close();
		
		return results;
	}


	@Override
	public Boolean insert(String id, String storeName, String blob) {
		
		ContentValues values = new ContentValues();
		values.put("rowId", id);
		values.put("blob", blob);
		long newid = this.db.insert(prepareStoreName(storeName), null, values);
		
		return newid >= 0;
	}

	@Override
	public Boolean update(String id, String storeName, String blob) {
		String where = "rowId=?";
		String[] whereArgs = {id.toString()};

		ContentValues values = new ContentValues();
		values.put("blob", blob);
		
		int affected = this.db.update(prepareStoreName(storeName), values, where, whereArgs);
		
		return affected > 0;
	}

	@Override
	public Boolean delete(String id, String storeName) {
		String where = "rowId=?";
		String[] whereArgs = {id.toString()};
		
		int affected = this.db.delete(prepareStoreName(storeName), where, whereArgs);
		
		return affected > 0;
	}

	private String prepareStoreName(String value) {
		return String.format("[%s]", value);
	}
}
