package yoyon.smartlock.standalone.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import yoyon.smartlock.standalone.model.YoyonKey;

/**
 * Created by QinBin on 2019/1/10.
 */
public class YoyonKeyDao {
    private static final String TAG = "YoyonKeyDao";
    // 列定义
    private final String[] DEVICE_COLUMNS = new String[] {"id", "attachLockMac", "name", "indexNumber", "type", "role"};
    private Context context;
    private YoyonDBHelper yoyonDBHelper;

    public static YoyonKeyDao sInstance;
    public static synchronized YoyonKeyDao getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new YoyonKeyDao(context);
        }
        return sInstance;
    }
    private YoyonKeyDao(Context context) {
        this.context = context;
        yoyonDBHelper = new YoyonDBHelper(context);
    }
    /**
     * 判断表中是否有数据
     */
    public boolean isKeyExist(){
        int count = 0;

        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = yoyonDBHelper.getReadableDatabase();
            // select count(Id) from Orders
            cursor = db.query(YoyonDBHelper.YOYON_KEY_TABLE_NAME, new String[]{"COUNT(id)"}, null, null, null, null, null);

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
     * 判断表中是否有相同mac地址和相同indexNumber的YoyonKey
     */
    public boolean isTheKeyExist(String attachLockMac, int indexNumber){

        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = yoyonDBHelper.getReadableDatabase();
            cursor = db.query(YoyonDBHelper.YOYON_KEY_TABLE_NAME, DEVICE_COLUMNS, null, null, null, null, null);

            while (cursor.moveToNext()) {
                if(cursor.getString(cursor.getColumnIndex("attachLockMac")).equalsIgnoreCase(attachLockMac) && (cursor.getInt(cursor.getColumnIndex("indexNumber")) == indexNumber)){
                    return true;
                }
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
        return false;
    }

    /**
     * 判断表中是否有相同mac地址和相同indexNumber的YoyonKey
     */
    public String getTheKeyName(String attachLockMac, int indexNumber){

        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = yoyonDBHelper.getReadableDatabase();
            cursor = db.query(YoyonDBHelper.YOYON_KEY_TABLE_NAME, DEVICE_COLUMNS, null, null, null, null, null);

            while (cursor.moveToNext()) {
                if(cursor.getString(cursor.getColumnIndex("attachLockMac")).equalsIgnoreCase(attachLockMac) && (cursor.getInt(cursor.getColumnIndex("indexNumber")) == 2)){
                    return parseYoyonKey(cursor).getName();
                }
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
        return null;
    }
    /**
     * 查询数据库中所有数据
     */
    public List<YoyonKey> getAllKey(){
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = yoyonDBHelper.getReadableDatabase();
            // select * from Orders
            cursor = db.query(YoyonDBHelper.YOYON_KEY_TABLE_NAME, DEVICE_COLUMNS, null, null, null, null, null);

            if (cursor.getCount() > 0) {
                List<YoyonKey> yoyonKeyList = new ArrayList<>(cursor.getCount());
                while (cursor.moveToNext()) {
                    yoyonKeyList.add(parseYoyonKey(cursor));
                }
                return yoyonKeyList;
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

        return null;
    }

    /**
     * 查询数据库中一把锁的所有钥匙数据
     */
    public List<YoyonKey> getAllKeyOfLock(String attachLockMac){
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = yoyonDBHelper.getReadableDatabase();
            // select * from Orders
            cursor = db.query(YoyonDBHelper.YOYON_KEY_TABLE_NAME, DEVICE_COLUMNS, "attachLockMac = ?", new String[]{attachLockMac}, null, null, null);

            if (cursor.getCount() > 0) {
                List<YoyonKey> yoyonKeyList = new ArrayList<>(cursor.getCount());
                while (cursor.moveToNext()) {
                    yoyonKeyList.add(parseYoyonKey(cursor));
                }
                return yoyonKeyList;
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
     * 查询数据库中一把锁的特定的钥匙数据
     */
    public YoyonKey getTheKeyOfLock(String attachLockMac,int indexNumber){
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = yoyonDBHelper.getReadableDatabase();
            // select * from Orders
            cursor = db.query(YoyonDBHelper.YOYON_KEY_TABLE_NAME, DEVICE_COLUMNS, "attachLockMac = ? and indexNumber = ?", new String[]{attachLockMac, String.valueOf(indexNumber)}, null, null, null);

            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    return parseYoyonKey(cursor);
                }
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

        return null;
    }
    /**
     * 新增一条数据
     */
    public boolean insertKey(YoyonKey yoyonKey){
        SQLiteDatabase db = null;

        try {
            db = yoyonDBHelper.getWritableDatabase();
            db.beginTransaction();

            ContentValues cv = new ContentValues();
            cv.put("attachLockMac",yoyonKey.getAttachLockMac());
            cv.put("name", yoyonKey.getName());
            cv.put("indexNumber", yoyonKey.getIndexNumber());
            cv.put("type", yoyonKey.getType());
            cv.put("role", yoyonKey.getRole());
            db.insertOrThrow(YoyonDBHelper.YOYON_KEY_TABLE_NAME, null, cv);
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
     * 删除一把锁的所有钥匙数据
     */
    public boolean deleteKeyOfLock(String attachLockMac) {
        SQLiteDatabase db = null;

        try {
            db = yoyonDBHelper.getWritableDatabase();
            db.beginTransaction();

            db.delete(YoyonDBHelper.YOYON_KEY_TABLE_NAME, "attachLockMac = ?", new String[]{attachLockMac});
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
     * 删除一条数据
     */
    public boolean deleteKey(String attachLockMac, int indexNumber) {
        SQLiteDatabase db = null;

        try {
            db = yoyonDBHelper.getWritableDatabase();
            db.beginTransaction();

            db.delete(YoyonDBHelper.YOYON_KEY_TABLE_NAME, "attachLockMac = ? and indexNumber = ?", new String[]{attachLockMac, String.valueOf(indexNumber)});
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
     * 更新一条数据
     */
    public boolean updateKey(YoyonKey yoyonKey){
        SQLiteDatabase db = null;
        try {
            db = yoyonDBHelper.getWritableDatabase();
            db.beginTransaction();
            ContentValues cv = new ContentValues();
            cv.put("attachLockMac",yoyonKey.getAttachLockMac());
            cv.put("name", yoyonKey.getName());
            cv.put("indexNumber", yoyonKey.getIndexNumber());
            cv.put("type", yoyonKey.getType());
            cv.put("role", yoyonKey.getRole());
            db.update(YoyonDBHelper.YOYON_KEY_TABLE_NAME, cv, "attachLockMac = ? and indexNumber = ?", new String[]{yoyonKey.getAttachLockMac(), String.valueOf(yoyonKey.getIndexNumber())});
            db.setTransactionSuccessful();
            return true;
        }
        catch (Exception e) {
            Log.e(TAG, "", e);
        }
        finally {
            if (db != null) {
                db.endTransaction();
                db.close();
            }
        }
        return false;
    }

    /**
     * 将查找到的数据转换成YoyonKey类
     */
    private YoyonKey parseYoyonKey(Cursor cursor){
        YoyonKey yoyonKey = new YoyonKey();
        yoyonKey.setAttachLockMac(cursor.getString(cursor.getColumnIndex("attachLockMac")));
        yoyonKey.setName(cursor.getString(cursor.getColumnIndex("name")));
        yoyonKey.setIndexNumber(cursor.getInt(cursor.getColumnIndex("indexNumber")));
        yoyonKey.setType(cursor.getInt(cursor.getColumnIndex("type")));
        yoyonKey.setRole(cursor.getInt(cursor.getColumnIndex("role")));
        return yoyonKey;
    }
}
