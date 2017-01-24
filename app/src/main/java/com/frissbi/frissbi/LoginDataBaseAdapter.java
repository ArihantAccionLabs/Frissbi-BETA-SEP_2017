package com.frissbi.frissbi;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class LoginDataBaseAdapter
{
	/*	static final String DATABASE_NAME = "login.db";
		static final int DATABASE_VERSION = 1;
		public static final int NAME_COLUMN = 1;
		// TODO: Create public field for each column in your table.
		// SQL Statement to create a1 new database.
		static final String DATABASE_CREATE = "create table "+"LOGIN"+
		                             "( " +"ID"+" integer primary key autoincrement,"+ "USERNAME  text,PASSWORD text); ";
		// Variable to hold the database instance*/





	private static final int DATABASE_VERSION = 1;
	public static final int NAME_COLUMN = 1;
	// Database Name
	private static final String DATABASE_NAME = "frissbi_chatDb1";

	// Table Names
	private static final String tbl_frissbifriendchatsessions = "tbl_frissbifriendchatsessions";
	private static final String tbl_frissbichatOne = "tbl_frissbichat";
	private static final String tbl_frissbigroup = "tabl_frissbigroup";

	// tbl_frissbifriendchatsessions column names
	public static  Integer SESSION_ID;
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

	static final String DATABASE_CREATE = "create table "+"tbl_frissbichat"+
			"( " +CHATMESSAGE_ID+" integer primary key autoincrement,"+ "FROM_USERID  text,TO_USERID text,TEXT_MASSEGE text); ";


	public SQLiteDatabase db;
		// Context of the application using the database.
		private final Context context;
		// Database open/upgrade helper

		private DataBaseHelper dbHelper;
		public  LoginDataBaseAdapter(Context _context)
		{
			context = _context;
			dbHelper = new DataBaseHelper(context, DATABASE_NAME, null, DATABASE_VERSION);
		}
		public  LoginDataBaseAdapter open() throws SQLException
		{
			db = dbHelper.getWritableDatabase();
			return this;
		}
		public void close()
		{
			db.close();
		}

		public SQLiteDatabase getDatabaseInstance()
		{
			return db;
		}

		public void insertEntry(String fromuserid,String touserid,String textmassege)
		{
	       ContentValues newValues = new ContentValues();
			// Assign values for each row.
			newValues.put(FROM_USERID, fromuserid);
			newValues.put(TO_USERID,touserid);

			newValues.put(TEXT_MASSEGE,textmassege);


			// Insert the row into your table
			db.insert("tbl_frissbichat", null, newValues);
			///Toast.makeText(context, "Reminder Is Successfully Saved", Toast.LENGTH_LONG).show();
		}

		public String getSinlgeEntry(String fromuserId)
		{
			String password = null;
				Cursor cursor=db.query("tbl_frissbichat", null, " FROM_USERID=?", new String[]{fromuserId}, null, null, null);



			if (cursor.moveToFirst()) {
				do {
					 password= cursor.getString(cursor.getColumnIndex("TEXT_MASSEGE"));
					cursor.close();
				} while (cursor.moveToNext());
			}
			return password;



		}

}

