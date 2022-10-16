package yoyon.smartlock.standalone.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by pyh on 2017/10/24.
 */

public class YoyonDBHelper extends SQLiteOpenHelper {
    private static final int DB_VERSION = 1;
    private static final String DB_NAME = "yoyon.db";
    public static final String YOYON_LOCK_TABLE_NAME = "YoyonLocks";
    public static final String YOYON_KEY_TABLE_NAME = "YoyonKeys";
    public static final String YOYON_OPERATION_LOG_TABLE_NAME = "YoyonOperationLogs";

    public YoyonDBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String sql1 = "create table if not exists " + YOYON_LOCK_TABLE_NAME + " (mac text primary key, name text, userKey text, capability text, role Integer, tempPasswordSeed text, tempPasswordRandom text)";
        String sql2 = "create table if not exists " + YOYON_KEY_TABLE_NAME + " (id Integer primary key autoincrement, attachLockMac text, name text, indexNumber Integer, finger Integer, type Integer, role Integer)";
        String sql3 = "create table if not exists " + YOYON_OPERATION_LOG_TABLE_NAME + " (id Integer primary key autoincrement, attachLockMac text, attachKeyIndex Integer, attachKeyType Integer, type Integer, date text, content text)";
        sqLiteDatabase.execSQL(sql1);
        sqLiteDatabase.execSQL(sql2);
        sqLiteDatabase.execSQL(sql3);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        String sql1 = "DROP TABLE IF EXISTS " + YOYON_LOCK_TABLE_NAME;
        String sql2 = "DROP TABLE IF EXISTS " + YOYON_KEY_TABLE_NAME;
        String sql3 = "DROP TABLE IF EXISTS " + YOYON_OPERATION_LOG_TABLE_NAME;
        sqLiteDatabase.execSQL(sql1);
        sqLiteDatabase.execSQL(sql2);
        sqLiteDatabase.execSQL(sql3);
        onCreate(sqLiteDatabase);
    }
}
