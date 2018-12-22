package com.steps.targe;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.HashMap;

public class TestDatabase {

    public static final String KEY_CID = "contact_id";
    public static final String KEY_NAME = "name";
    public static final String KEY_TRUST = "trust_value";
    public static final String KEY_CIN = "in_call";
    public static final String KEY_COUT = "out_call";
    public static final String KEY_DIN = "in_dur";
    public static final String KEY_DOUT = "out_dur";
    private static final String DATABASE_NAME = "Targe";
    private static final String DATABASE_TABLE = "trust_data_targe";
    private static final String KEY_LAST_CONTACTED="last_contact_date";
    public static final int DATABASE_VERSION = 1;
    private DbHelper ourHelper;
    private final Context ourContext;
    private SQLiteDatabase ourDatabase;

    private static class DbHelper extends SQLiteOpenHelper {
        public DbHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);

        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            // TODO Auto-generated method stub
            db.execSQL("CREATE TABLE " + DATABASE_TABLE + " (" +
                    KEY_CID + " VARCHAR PRIMARY KEY, " +
                    KEY_NAME + " VARCHAR NOT NULL, " +
                    KEY_TRUST + " DOUBLE NOT NULL,"+
                    KEY_CIN+" INTEGER,"+
                    KEY_COUT+" INTEGER, "+
                    KEY_DIN+" INTEGER ,"+
                    KEY_DOUT+" INTEGER,"+
                    KEY_LAST_CONTACTED+" LONG)"
            );
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // TODO Auto-generated method stub
            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
            onCreate(db);
        }

    }

    public TestDatabase(Context c) {
        ourContext = c;

    }

    public TestDatabase open() {
        ourHelper = new DbHelper(ourContext);
        ourDatabase = ourHelper.getWritableDatabase();
        return this;

    }

    public void close() {
        ourHelper.close();
    }

    //this will overwrite previous records as it assumes preivous database-record changes have been made in updates being fed
    public long createEntry(Person p) {
        ContentValues cv = new ContentValues();
       //String sql="insert or replace into "+DATABASE_TABLE+"values ("+p.getNumber()+","+p.getName"+"+)
        cv.put(KEY_CID, p.getNumber());
        cv.put(KEY_NAME, p.getName());
        cv.put(KEY_TRUST, p.getTrust());
        cv.put(KEY_CIN,p.getCin());
        cv.put(KEY_COUT,p.getCout());
        cv.put(KEY_DIN,p.getDin());
        cv.put(KEY_DOUT,p.getDout());
        cv.put(KEY_LAST_CONTACTED,p.getLastcallstamp());
        return ourDatabase.insertWithOnConflict(DATABASE_TABLE, null, cv,SQLiteDatabase.CONFLICT_REPLACE);

    }

    public void updateEntry(Person p)
    {
        ContentValues cv = new ContentValues();
        cv.put(KEY_TRUST, p.getTrust());
        cv.put(KEY_CIN,p.getCin());
        cv.put(KEY_COUT,p.getCout());
        cv.put(KEY_DIN,p.getDin());
        cv.put(KEY_DOUT,p.getDout());
        cv.put(KEY_LAST_CONTACTED,p.getLastcallstamp());
        int k= ourDatabase.update(DATABASE_TABLE,cv,KEY_CID+" = "+p.getNumber(),null);
        if(k==0)
        {
            cv.put(KEY_CID, p.getNumber());
            cv.put(KEY_NAME, p.getName());
            ourDatabase.insert(DATABASE_TABLE, null, cv);
        }

    }
    public ArrayList<Person> getData(String query) {
        // TODO Auto-generated method stub
        //String[] columns = new String[]{KEY_CID, KEY_NAME, KEY_TRUST,KEY_CIN,KEY_COUT,KEY_DIN,KEY_DOUT};
        String selection = KEY_NAME + " LIKE ?";
        String[] columns={KEY_NAME,KEY_CID};
        String[] selectionArgs = new String[]{"%"+query + "%"};
        Cursor c = ourDatabase.rawQuery("SELECT * from "+DATABASE_TABLE+" WHERE "+KEY_NAME+" LIKE %"+query+"% OR "+KEY_CID+" LIKE %"+query+"%",null);
        //ourDatabase.query(DATABASE_TABLE,null, selection, selectionArgs, null, null, null);

        int icid = c.getColumnIndex(KEY_CID);
        int iname = c.getColumnIndex(KEY_NAME);
        int itrust = c.getColumnIndex(KEY_TRUST);
        int icin=c.getColumnIndex(KEY_CIN);
        int idin=c.getColumnIndex(KEY_DIN);
        int icout=c.getColumnIndex(KEY_COUT);
        int idout=c.getColumnIndex(KEY_DOUT);
        int ilcs=c.getColumnIndex(KEY_LAST_CONTACTED);
        ArrayList<Person> results = new ArrayList<Person>();
        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
            Person result = new Person(c.getString(icid), c.getString(iname), c.getDouble(itrust),c.getLong(ilcs));
            result.setCin(c.getInt(icin));
            result.setCout(c.getInt(icout));
            result.setDin(c.getInt(idin));
            result.setDout(c.getInt(idout));
            results.add(result);
        }
        c.close();
        return results;
    }

    public ArrayList<Person> getData(String s,String o) {
        // TODO Auto-generated method stub
        //String[] columns = new String[]{KEY_CID, KEY_NAME, KEY_TRUST,KEY_CIN,KEY_COUT,KEY_DIN,KEY_DOUT};
        Cursor c = ourDatabase.query(DATABASE_TABLE, null, null, null, null, null,s+" "+o,null);
        int icid = c.getColumnIndex(KEY_CID);
        int iname = c.getColumnIndex(KEY_NAME);
        int itrust = c.getColumnIndex(KEY_TRUST);
        int icin=c.getColumnIndex(KEY_CIN);
        int idin=c.getColumnIndex(KEY_DIN);
        int icout=c.getColumnIndex(KEY_COUT);
        int idout=c.getColumnIndex(KEY_DOUT);
        int ilcs=c.getColumnIndex(KEY_LAST_CONTACTED);
        ArrayList<Person> results = new ArrayList<Person>();
        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
            Person result = new Person(c.getString(icid), c.getString(iname), c.getDouble(itrust),c.getLong(ilcs));
            result.setCin(c.getInt(icin));
            result.setCout(c.getInt(icout));
            result.setDin(c.getInt(idin));
            result.setDout(c.getInt(idout));
            results.add(result);
        }
        c.close();
        return results;
    }
    public HashMap<String,Person> getData(Long timer) {
        // TODO Auto-generated method stub
       // String[] columns = new String[]{KEY_CID, KEY_NAME, KEY_TRUST,KEY_CIN,KEY_COUT,KEY_DIN,KEY_DOUT};
        String where=KEY_LAST_CONTACTED + " >  "+ timer ;
        Cursor c = ourDatabase.query(DATABASE_TABLE, null, where, null, null, null,null,null);
        int icid = c.getColumnIndex(KEY_CID);
        int iname = c.getColumnIndex(KEY_NAME);
        int itrust = c.getColumnIndex(KEY_TRUST);
        int icin=c.getColumnIndex(KEY_CIN);
        int idin=c.getColumnIndex(KEY_DIN);
        int icout=c.getColumnIndex(KEY_COUT);
        int idout=c.getColumnIndex(KEY_DOUT);
        int ilcs=c.getColumnIndex(KEY_LAST_CONTACTED);
        HashMap<String,Person> results = new HashMap<String,Person>();
        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
            Person result = new Person(c.getString(icid), c.getString(iname), c.getDouble(itrust),c.getLong(ilcs));
            result.setCin(c.getInt(icin));
            result.setCout(c.getInt(icout));
            result.setDin(c.getInt(idin));
            result.setDout(c.getInt(idout));
            results.put(result.getNumber(),result);
        }
        c.close();
        return results;
    }

}

