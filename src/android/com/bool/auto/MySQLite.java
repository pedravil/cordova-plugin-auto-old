package com.bool.auto;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

class MySQLite extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    private String DATABASE_PATH;
    private static final String DATABASE_NAME = "keyvalueb.db";

    MySQLite(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        String filesDir = context.getFilesDir().getPath();

        DATABASE_PATH = filesDir.substring(0, filesDir.lastIndexOf("/")) + "/databases/"; // /data/data/com.package.nom/databases/
        if (!checkdatabase()) {
            Log.v("TNC", "La base n'existe pas ");
        } else {
            Log.v("TNC", "La base existe ");
        }
    }

    private boolean checkdatabase() {
        // retourne true/false si la bdd existe dans le dossier de l'app
        File dbfile = new File(DATABASE_PATH + DATABASE_NAME);
        return dbfile.exists();
    }

    public void onCreate(SQLiteDatabase db) {}

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public String getTokenUser() {
        SQLiteDatabase db = this.getReadableDatabase();
        String reqsql = "SELECT * FROM blobkey WHERE k='account.owner'";
        Cursor cursor = db.rawQuery(reqsql, null);
        cursor.moveToFirst();
        String ret = null;
        try {
            JSONObject account = new JSONObject(cursor.getString(cursor.getColumnIndex("v")));
            ret = account.getString ("token");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return ret;
    }
}