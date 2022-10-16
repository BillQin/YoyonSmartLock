package yoyon.smartlock.standalone.utils;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import yoyon.smartlock.standalone.R;
public class SoundPlayUtils {
    public static final int FINGER_EXIST = 0;
    public static final int FINGER_FULL = 1;
    public static final int PRESS_FINGER = 2;
    public static final int PRESS_FINGER_AGAIN = 3;
    public static final int EXIST = 4;
    public static final int PROXIMITY_CARD_EXIST = 5;
    public static final int PROXIMITY_CARD_FULL = 6;
    public static final int SWIPE_CARD = 7;
    public static final int AUTHORIZE = 8;

    // SoundPool对象
    public static SoundPool mSoundPool= new SoundPool(10, AudioManager.STREAM_MUSIC, 5);
    public static SoundPlayUtils soundPlayUtils;
    // 上下文
    private static Context mContext;
    private static Map<String,Integer> soundIdMap;
    /**
     * 初始化
     * 初始化音乐池  播发喇叭的   BaseApplication初始化 方法里面用
     * @param context
     */
    public static SoundPlayUtils init(Context context) {
        if (soundPlayUtils == null) {
            soundPlayUtils = new SoundPlayUtils();
        }

        // 初始化声音
        mContext = context;
        soundIdMap = new HashMap<>();
        /**
         * 参数1:加载音乐流第多少个 (只用了俩个音乐) 2:设置音乐的质量 音乐流 3:资源的质量  0
         */
        soundIdMap.put("en"+FINGER_EXIST,mSoundPool.load(mContext, R.raw.en_fingerprint_exist, 1));
        soundIdMap.put("en"+FINGER_FULL,mSoundPool.load(mContext, R.raw.en_fingerprint_full, 1));
        soundIdMap.put("en"+PRESS_FINGER,mSoundPool.load(mContext, R.raw.en_press_finger, 1));
        soundIdMap.put("en"+PRESS_FINGER_AGAIN,mSoundPool.load(mContext, R.raw.en_press_finger_again, 1));
        soundIdMap.put("en"+EXIST,mSoundPool.load(mContext, R.raw.en_exist, 1));
        soundIdMap.put("en"+PROXIMITY_CARD_FULL,mSoundPool.load(mContext, R.raw.en_proximity_card_full, 1));
        soundIdMap.put("en"+SWIPE_CARD,mSoundPool.load(mContext, R.raw.en_swipe_card, 1));
        soundIdMap.put("en"+AUTHORIZE,mSoundPool.load(mContext, R.raw.en_authorize, 1));

        soundIdMap.put("zh"+FINGER_EXIST,mSoundPool.load(mContext, R.raw.zh_fingerprint_exist, 1));
        soundIdMap.put("zh"+FINGER_FULL,mSoundPool.load(mContext, R.raw.zh_fingerprint_full, 1));
        soundIdMap.put("zh"+PRESS_FINGER,mSoundPool.load(mContext, R.raw.zh_press_finger, 1));
        soundIdMap.put("zh"+PRESS_FINGER_AGAIN,mSoundPool.load(mContext, R.raw.zh_press_finger_again, 1));
        soundIdMap.put("zh"+EXIST,mSoundPool.load(mContext, R.raw.zh_exist, 1));
        soundIdMap.put("zh"+PROXIMITY_CARD_FULL,mSoundPool.load(mContext, R.raw.zh_proximity_card_full, 1));
        soundIdMap.put("zh"+SWIPE_CARD,mSoundPool.load(mContext, R.raw.zh_swipe_card, 1));
        soundIdMap.put("zh"+AUTHORIZE,mSoundPool.load(mContext, R.raw.zh_authorize, 1));
        return soundPlayUtils;
    }

    private static int lastSoundID;
    /**
     * 播放声音
     *
     * @param soundID  设置声音
     */
    public static void play(int soundID) {
        mSoundPool.stop(lastSoundID);
        if(isZh(mContext)){
            lastSoundID = mSoundPool.play(soundIdMap.get("zh"+soundID), 1, 1, 0, 0, 1);
        }else {
            lastSoundID = mSoundPool.play(soundIdMap.get("en"+soundID), 1, 1, 0, 0, 1);
        }
    }

    private static boolean isZh(Context context) {
        Locale locale = context.getResources().getConfiguration().locale;
        String language = locale.getLanguage();
        if (language.endsWith("zh"))
            return true;
        else
            return false;
    }
}
