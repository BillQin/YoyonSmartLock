package yoyon.smartlock.standalone.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import yoyon.smartlock.standalone.model.YoyonKey;
import yoyon.smartlock.standalone.model.YoyonOperationLog;

/**
 * Created by QinBin on 2019/1/10.
 */
public class YoyonOperationLogDao {
    private static final String TAG = "YoyonOperationLogDao";
    // 列定义
    private final String[] DEVICE_COLUMNS = new String[] {"id", "attachLockMac", "attachKeyIndex", "attachKeyType", "type", "date", "content"};
    private Context context;
    private YoyonDBHelper yoyonDBHelper;
    public static YoyonOperationLogDao sInstance;
    public static synchronized YoyonOperationLogDao getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new YoyonOperationLogDao(context);
        }
        return sInstance;
    }
    public YoyonOperationLogDao(Context context) {
        this.context = context;
        yoyonDBHelper = new YoyonDBHelper(context);
    }
    /**
     * 判断表中是否有数据
     */
    public boolean isOperationLogExist(){
        int count = 0;

        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = yoyonDBHelper.getReadableDatabase();
            // select count(Id) from Orders
            cursor = db.query(YoyonDBHelper.YOYON_OPERATION_LOG_TABLE_NAME, new String[]{"COUNT(id)"}, null, null, null, null, null);

            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
            if (count > 0) return true;
        }
        catch (Exception e) {
            Log.e(TAG, "", e);
        }
        finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return false;
    }
    /**
     * 查询数据库中所有数据
     */
    public List<YoyonOperationLog> getAllOperationLog(){
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = yoyonDBHelper.getReadableDatabase();
            // select * from Orders
            cursor = db.query(YoyonDBHelper.YOYON_OPERATION_LOG_TABLE_NAME, DEVICE_COLUMNS, null, null, null, null, "id desc");

            if (cursor.getCount() > 0) {
                List<YoyonOperationLog> yoyonOperationLogList = new ArrayList<>(cursor.getCount());
                while (cursor.moveToNext()) {
                    yoyonOperationLogList.add(parseYoyonOperationLog(cursor));
                }
                return yoyonOperationLogList;
            }
        }
        catch (Exception e) {
            Log.e(TAG, "", e);
        }
        finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }

        return new ArrayList<>();
    }

    /**
     * 查询数据库中一把锁的所有操作日志数据
     */
    public List<YoyonOperationLog> getAllOperationLogOfLock(String attachLockMac){
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = yoyonDBHelper.getReadableDatabase();
            // select * from Orders
            cursor = db.query(YoyonDBHelper.YOYON_OPERATION_LOG_TABLE_NAME, DEVICE_COLUMNS, "attachLockMac = ?", new String[]{attachLockMac}, null, null, "id desc");

            if (cursor.getCount() > 0) {
                List<YoyonOperationLog> yoyonOperationLogList = new ArrayList<>(cursor.getCount());
                while (cursor.moveToNext()) {
                    yoyonOperationLogList.add(parseYoyonOperationLog(cursor));
                }
                return yoyonOperationLogList;
            }
        }
        catch (Exception e) {
            Log.e(TAG, "", e);
        }
        finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }

        return new ArrayList<>();
    }
    /**
     * 新增一条数据
     */
    public boolean insertOperationLog(YoyonOperationLog yoyonOperationLog){
        SQLiteDatabase db = null;

        try {
            db = yoyonDBHelper.getWritableDatabase();
            db.beginTransaction();

            ContentValues cv = new ContentValues();
            cv.put("attachLockMac",yoyonOperationLog.getAttachLockMac());
            cv.put("attachKeyIndex", yoyonOperationLog.getAttachKeyIndex());
            cv.put("attachKeyType", yoyonOperationLog.getAttachKeyType());
            cv.put("type", yoyonOperationLog.getType());
            cv.put("date", yoyonOperationLog.getDate());
            cv.put("content", yoyonOperationLog.getContent());
            db.insertOrThrow(YoyonDBHelper.YOYON_OPERATION_LOG_TABLE_NAME, null, cv);
            db.setTransactionSuccessful();
            return true;
        }catch (SQLiteConstraintException e){
        }catch (Exception e){
            Log.e(TAG, "", e);
        }finally {
            if (db != null) {
                db.endTransaction();
                db.close();
            }
        }
        return false;
    }

    /**
     * 删除一把锁的所有操作日志数据
     */
    public boolean deleteOperationLogOfLock(String attachLockMac) {
        SQLiteDatabase db = null;

        try {
            db = yoyonDBHelper.getWritableDatabase();
            db.beginTransaction();

            db.delete(YoyonDBHelper.YOYON_OPERATION_LOG_TABLE_NAME, "attachLockMac = ?", new String[]{attachLockMac});
            db.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            Log.e(TAG, "", e);
        } finally {
            if (db != null) {
                db.endTransaction();
                db.close();
            }
        }
        return false;
    }


    /**
     * 将查找到的数据转换成YoyonOperationLog类
     */
    private YoyonOperationLog parseYoyonOperationLog(Cursor cursor){
        YoyonOperationLog yoyonOperationLog = new YoyonOperationLog();
        yoyonOperationLog.setAttachLockMac(cursor.getString(cursor.getColumnIndex("attachLockMac")));
        yoyonOperationLog.setAttachKeyIndex(cursor.getInt(cursor.getColumnIndex("attachKeyIndex")));
        yoyonOperationLog.setAttachKeyType(cursor.getInt(cursor.getColumnIndex("attachKeyType")));
        yoyonOperationLog.setType(cursor.getInt(cursor.getColumnIndex("type")));
        yoyonOperationLog.setDate(cursor.getString(cursor.getColumnIndex("date")));
        yoyonOperationLog.setContent(cursor.getString(cursor.getColumnIndex("content")));
        return yoyonOperationLog;
    }
}
