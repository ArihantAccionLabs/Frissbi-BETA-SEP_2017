package com.frissbi.frissbi;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by KNPL003 on 27-06-2015.
 */
public class ChatLocalStorageDB extends SQLiteOpenHelper {
    private static final String LOG = ChatLocalStorageDB.class.getName();

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "frissbi_chatDb";

    // Table Names
    private static final String tbl_frissbifriendchatsessions = "tbl_frissbifriendchatsessions";
    private static final String tbl_frissbichat = "tbl_frissbichat";
    private static final String tbl_frissbigroup = "tabl_frissbigroup";

    // tbl_frissbifriendchatsessions column names
    public static  String SESSION_ID="sessionid";
    private static final String FROM_USERID = "fromuserid";
    private static final String TO_USERID = "touserid";
    private static final String GROUP_ID = "groupid";

    // tbl_frissbichat Table - column nmaes
    private static final String CHATMESSAGE_ID = "chatmessageid";
    private static final String SENTMSG_DATETIME = "sentmsgdatetime";
    private static final String RECEIVEMSG_DATETIME = "receivemsgdatetime";
    private static final String TEXT_MASSEGE = "textmassege";
    private static final String DELIVER_STAUS = "status";

    // tabl_frissbigroup - column names
    private static final String GROUP_NAME = "groupname";
    private static final String GROUP_ADMIN = "groupadmin";
    private static final String RECORDDATETIME = "recorddatetime";
    private static final String PLAYERS = "players";


    // tbl_frissbifriendchatsessions create statement
 static final String CREATE_tbl_frissbifriendchatsessions = "CREATE TABLE "
            + tbl_frissbifriendchatsessions + "(" + SESSION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + FROM_USERID
            + " INTEGER, " + TO_USERID + " INTEGER, " + GROUP_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + ")";

    // tbl_frissbichat Table create statement
    static final String CREATE_tbl_frissbichat = "CREATE TABLE "
            + tbl_frissbichat + "(" + CHATMESSAGE_ID + " INTEGER PRIMARY KEY  AUTOINCREMENT, " + FROM_USERID
            + " INTEGER, " + TO_USERID + " INTEGER, " + SENTMSG_DATETIME + " TEXT, " + RECEIVEMSG_DATETIME + " TEXT, "
            + TEXT_MASSEGE + " TEXT, " + GROUP_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + DELIVER_STAUS + "INTEGER, " + SESSION_ID + "INTEGER PRIMARY KEY AUTOINCREMENT, " + ")";
    // tbl_frissbichat Table create statement
    static final String CREATE_tbl_frissbigroup = "CREATE TABLE "
            + tbl_frissbigroup + "(" + GROUP_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + GROUP_NAME + " TEXT, " + PLAYERS + " TEXT "
            + GROUP_ADMIN + " TEXT " + RECORDDATETIME + "TEXT, " + ")";


    public ChatLocalStorageDB(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_tbl_frissbichat);
        db.execSQL(CREATE_tbl_frissbifriendchatsessions);
        db.execSQL(CREATE_tbl_frissbigroup);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + CREATE_tbl_frissbifriendchatsessions);
        db.execSQL("DROP TABLE IF EXISTS " + CREATE_tbl_frissbichat);
        db.execSQL("DROP TABLE IF EXISTS " + CREATE_tbl_frissbigroup);

        // create new tables
        onCreate(db);

    }

    public void insert(int fromuserid, int touserid, String sentmsgdatetime, String receivemsgdatetime, String textmassege, int status) {
        long rowId = -1;
        try {

            SQLiteDatabase db = getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(FROM_USERID, fromuserid);
            values.put(TO_USERID, touserid);
            values.put(SENTMSG_DATETIME, sentmsgdatetime);
            values.put(RECEIVEMSG_DATETIME, receivemsgdatetime);
            values.put(TEXT_MASSEGE, textmassege);
            values.put(DELIVER_STAUS, status);
            rowId = db.insert(tbl_frissbichat, null, values);


        } catch (SQLiteException e) {
            Log.e(LOG, "insert()", e);
        } finally {
            Log.d(LOG, "insert(): rowId=" + rowId);
        }

    }

    public Cursor get(int fromuserid, int touserid) {

        SQLiteDatabase db = getReadableDatabase();
        String SELECT_QUERY = "SELECT SESSION_ID FROM " + tbl_frissbifriendchatsessions + " WHERE " + FROM_USERID + " ='" + fromuserid + "' AND " + TO_USERID + " = '" + touserid;
        return db.rawQuery(SELECT_QUERY, null);
       // return db.query(TABLE_NAME_MESSAGES, null, MESSAGE_SENDER + " LIKE ? OR " + MESSAGE_SENDER + " LIKE ?", sender , null, null, _ID + " ASC");




    }
}
