package yoyon.smartlock.standalone.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.Toast;

import com.yoyon.ynblelib.yoyon.YnBleCapability;

import java.util.ArrayList;
import java.util.List;

import yoyon.smartlock.standalone.model.YoyonLock;
import yoyon.smartlock.standalone.utils.YoyonUtils;

/**
 * Created by QinBin on 2019/1/10.
 */
public class YoyonLockDao {
    private static final String TAG = "YoyonLockDao";
    // 列定义
    private final String[] DEVICE_COLUMNS = new String[] {"mac", "name", "userKey", "capability", "role", "tempPasswordSeed", "tempPasswordRandom"};
    private Context context;
    private YoyonDBHelper yoyonDBHelper;

    public static YoyonLockDao sInstance;
    public static synchronized YoyonLockDao getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new YoyonLockDao(context);
        }
        return sInstance;
    }
    private YoyonLockDao(Context context) {
        this.context = context;
        yoyonDBHelper = new YoyonDBHelper(context);
    }
    /**
     * 判断表中是否有数据
     */
    public boolean isLockExist(){
        int count = 0;

        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = yoyonDBHelper.getReadableDatabase();
            // select count(Id) from Orders
            cursor = db.query(YoyonDBHelper.YOYON_LOCK_TABLE_NAME, new String[]{"COUNT(mac)"}, null, null, null, null, null);

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
     * 判断表中是否有相同mac地址的YoyonLock
     */
    public boolean isTheLockExist(String mac){

        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = yoyonDBHelper.getReadableDatabase();
            // select count(Id) from Orders
            cursor = db.query(YoyonDBHelper.YOYON_LOCK_TABLE_NAME, DEVICE_COLUMNS, null, null, null, null, null);

            while (cursor.moveToNext()) {
                if(cursor.getString(cursor.getColumnIndex("mac")).equalsIgnoreCase(mac)){
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
     * 查询数据库中所有数据
     */
    public List<YoyonLock> getAllLock(){
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = yoyonDBHelper.getReadableDatabase();
            // select * from Orders
            cursor = db.query(YoyonDBHelper.YOYON_LOCK_TABLE_NAME, DEVICE_COLUMNS, null, null, null, null, null);

            if (cursor.getCount() > 0) {
                List<YoyonLock> yoyonLockList = new ArrayList<>(cursor.getCount());
                while (cursor.moveToNext()) {
                    yoyonLockList.add(parseYoyonLock(cursor));
                }
                return yoyonLockList;
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
     * 根据mac查询Lock
     */
    public YoyonLock getTheLock(String mac){
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = yoyonDBHelper.getReadableDatabase();
            // select * from Orders
            cursor = db.query(YoyonDBHelper.YOYON_LOCK_TABLE_NAME, DEVICE_COLUMNS, "mac = ?", new String[]{mac}, null, null, null);

            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    YoyonLock yoyonLock;
                    yoyonLock = parseYoyonLock(cursor);
                    return yoyonLock;
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
    public boolean insertLock(YoyonLock yoyonLock){
        SQLiteDatabase db = null;

        try {
            db = yoyonDBHelper.getWritableDatabase();
            db.beginTransaction();

            ContentValues cv = new ContentValues();
            cv.put("mac",yoyonLock.getMac());
            cv.put("name", yoyonLock.getName());
            cv.put("userKey", yoyonLock.getUserKey());
            cv.put("capability", YoyonUtils.bytesToHexString(yoyonLock.getCapability().toCapabilityBitMap()));
            cv.put("role", yoyonLock.getRole());
            cv.put("tempPasswordSeed", yoyonLock.getTempPasswordSeed());
            cv.put("tempPasswordRandom", yoyonLock.getTempPasswordRandom());
            db.insertOrThrow(YoyonDBHelper.YOYON_LOCK_TABLE_NAME, null, cv);
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
     * 删除一条数据
     */
    public boolean deleteLock(String mac) {
        SQLiteDatabase db = null;

        try {
            db = yoyonDBHelper.getWritableDatabase();
            db.beginTransaction();

            db.delete(YoyonDBHelper.YOYON_LOCK_TABLE_NAME, "mac = ?", new String[]{mac});
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
    public boolean updateLock(YoyonLock yoyonLock){
        SQLiteDatabase db = null;
        try {
            db = yoyonDBHelper.getWritableDatabase();
            db.beginTransaction();
            ContentValues cv = new ContentValues();
            cv.put("mac",yoyonLock.getMac());
            cv.put("name", yoyonLock.getName());
            cv.put("userKey", yoyonLock.getUserKey());
            cv.put("capability", YoyonUtils.bytesToHexString(yoyonLock.getCapability().toCapabilityBitMap()));
            cv.put("role", yoyonLock.getRole());
            cv.put("tempPasswordSeed", yoyonLock.getTempPasswordSeed());
            cv.put("tempPasswordRandom", yoyonLock.getTempPasswordRandom());
            db.update(YoyonDBHelper.YOYON_LOCK_TABLE_NAME, cv, "mac = ?", new String[]{yoyonLock.getMac()});
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
     * 将查找到的数据转换成YoyonLock类
     */
    private YoyonLock parseYoyonLock(Cursor cursor){
        YoyonLock yoyonLock = new YoyonLock();
        yoyonLock.setMac(cursor.getString(cursor.getColumnIndex("mac")));
        yoyonLock.setName(cursor.getString(cursor.getColumnIndex("name")));
        yoyonLock.setUserKey(cursor.getString(cursor.getColumnIndex("userKey")));
        yoyonLock.setCapability(new YnBleCapability(YoyonUtils.hexStringToBytes(cursor.getString(cursor.getColumnIndex("capability")))));
        yoyonLock.setRole(cursor.getInt(cursor.getColumnIndex("role")));
        yoyonLock.setTempPasswordSeed(cursor.getString(cursor.getColumnIndex("tempPasswordSeed")));
        yoyonLock.setTempPasswordRandom(cursor.getString(cursor.getColumnIndex("tempPasswordRandom")));
        return yoyonLock;
    }
}
