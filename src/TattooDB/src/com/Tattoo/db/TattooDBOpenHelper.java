package com.Tattoo.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

class TattooDBOpenHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;

    private static SQLiteOpenHelper instance;

    public static synchronized SQLiteOpenHelper getHelper(Context context)
    {
        if (instance == null)
            instance = new TattooDBOpenHelper(context);

        return instance;
    }

    TattooDBOpenHelper(Context context) {
        super(context, "TattooDB", null, DATABASE_VERSION);
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {
    }

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
	}
}