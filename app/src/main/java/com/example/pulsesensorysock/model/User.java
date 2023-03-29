package com.example.pulsesensorysock.model;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class User extends SQLiteOpenHelper {

    static String DB="tibializer.db";
    static String user="user";
    Context context;

    private int defaultUserId = 1;

    public User(Context context) {
        super(context, DB, null, 1);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub

        //user table
        String sql="CREATE TABLE "+user+"(user_id integer primary key, " +
                "user_name varchar(30), " +
                "user_age varchar(30), " +
                "user_bday varchar(30), " +
                "user_sex varchar(30), " +
                "user_contact varchar(30), " +
                "user_address varchar(30), " +
                "user_emerg_name1 varchar(30), " +
                "user_emerg_contact1 varchar(30)," +
                "user_emerg_name2 varchar(30)," +
                "user_emerg_contact2 varchar(30), " +
                "user_photo varchar(100))";
        db.execSQL(sql);
    }

    public long addUSer(String array[]) {
        long result = 0;
        //open the database for writing record
        SQLiteDatabase db = this.getWritableDatabase();
        //use ORM(object relational  mapper) for managing database
        ContentValues cv = new ContentValues();

        cv.put("user_id", defaultUserId);
        cv.put("user_name", array[0]);
        cv.put("user_age", array[1]);
        cv.put("user_bday", array[2]);
        cv.put("user_sex", array[3]);
        cv.put("user_contact", array[4]);
        cv.put("user_address", array[5]);
        cv.put("user_emerg_name1", array[6]);
        cv.put("user_emerg_contact1", array[7]);
        cv.put("user_emerg_name2", array[8]);
        cv.put("user_emerg_contact2", array[9]);
        cv.put("user_photo", array[10]);

        //write the orm content to the database
        result=db.insert(user, null, cv);
        db.close();

        return result;
    }

    public long editUSer(String array[], String userId) {
        long result = 0;
        //open the database for writing record
        SQLiteDatabase db = this.getWritableDatabase();
        //use ORM(object relational  mapper) for managing database
        ContentValues cv = new ContentValues();

        cv.put("user_name", array[0]);
        cv.put("user_age", array[1]);
        cv.put("user_bday", array[2]);
        cv.put("user_sex", array[3]);
        cv.put("user_contact", array[4]);
        cv.put("user_address", array[5]);
        cv.put("user_emerg_name1", array[6]);
        cv.put("user_emerg_contact1", array[7]);
        cv.put("user_emerg_name2", array[8]);
        cv.put("user_emerg_contact2", array[9]);
        cv.put("user_photo", array[10]);

        //write the orm content to the database
        result=db.update(user, cv, "user_id=?", new String[]{ userId });
        db.close();

        return result;
    }

    public ArrayList<String> getUser() {
        ArrayList<String> list = new ArrayList<String>();
        SQLiteDatabase db=this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + user + " WHERE user_id=?", new String[]{String.valueOf(defaultUserId)});
        if (c.moveToFirst()) {
            for(int i=0; i < c.getColumnCount(); i++){
                String val =c.getString(i);
                list.add(val);
            }
        }
        db.close();
        return list;
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
