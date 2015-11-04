package com.example.shriya.recordaudio;

/**
 * Created by shriya on 1/29/2015.
 */ import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class LoginDataBaseAdapter {
    String logged="";
    static final String DATABASE_NAME = "login.db";
    static final int DATABASE_VERSION = 1;
    public static final int NAME_COLUMN = 3;

    static final String DATABASE_CREATE = "create table " + "LOGIN" +
            "( " + "ID integer primary key autoincrement," + "USERNAME text," + "PASSWORD text," + "LOGGED text" + ") ";

    public SQLiteDatabase db;
    private final Context context;
    private DataBaseHelper dbHelper;

    public LoginDataBaseAdapter(Context _context) {
        context = _context;
        dbHelper = new DataBaseHelper(context, DATABASE_NAME, null, DATABASE_VERSION);

    }


    public LoginDataBaseAdapter open() throws SQLException {
        db = dbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        db.close();
    }

    public SQLiteDatabase getDatabaseInstance() {
        return db;
    }

    public void insertEntry(String username, String password) {
        ContentValues newValues = new ContentValues();
        String loggedIn="yes";
        newValues.put("USERNAME", username);
        newValues.put("PASSWORD", password);
        newValues.put("LOGGED", loggedIn);

        db.insert("LOGIN", null, newValues);
    }

    public int deleteEntry(String password) {
        String where = "PASSWORD=?";
        int numberOFEntriesDeleted = db.delete("LOGIN", where, new String[]{password});
        return numberOFEntriesDeleted;
    }

    public String getSingleEntry(String username, String password) {
        ContentValues args = new ContentValues();
        args.put("LOGGED", "yes");
        db.update("LOGIN", args,"USERNAME=?", new String[]{username});

        Cursor cursor = db.rawQuery("select PASSWORD from LOGIN where USERNAME=? AND PASSWORD=?", new String[]{username, password});


        if (cursor.getCount() < 1) // UserName Not Exist
        {
            cursor.close();
            return "INVALID";
        }
        cursor.moveToFirst();
        cursor.close();
        return "SUCCESS";
    }

    public String getLoggedIn() {

        Cursor cursor = db.rawQuery("select LOGGED from LOGIN where LOGGED=?", new String[]{"yes"});


        //Cursor cursor=db.query("LOGIN", null, " PASSWORD=?", new String[]{password}, null, null, null);
        if (cursor.getCount() < 1) // UserName Not Exist
        {
            cursor.close();
            return "register";
        }
        cursor.moveToFirst();
        cursor.close();
        return "logged";
    }

    public void logout()
    {
        String LOGGED="LOGGED";
        ContentValues args = new ContentValues();
        args.put("LOGGED", "no");
        db.update("LOGIN", args, null, null);

        Log.w("app","Logged OUT NOW!!!!");
    }
    public int getLogged()
    {
        Cursor cursor = db.query("LOGIN", null, "LOGGED=?", new String[]{"yes"}, null, null, null);
        if (cursor.getCount() < 1) // UserName Not Exist
        {   Log.w("app","No one's Logged in Yet!");
            cursor.close();
            return 0;
        }
        else {
            Log.w("app","Someone's logged in");
            cursor.moveToFirst();
            cursor.close();
            return 1;
        }

    }
}
