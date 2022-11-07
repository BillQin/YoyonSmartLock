package yoyon.smartlock.standalone;

import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;

import com.scwang.smartrefresh.layout.footer.ClassicsFooter;
import com.scwang.smartrefresh.layout.header.ClassicsHeader;

import yoyon.smartlock.standalone.utils.SharedPreferencesUtil;
import yoyon.smartlock.standalone.utils.SoundPlayUtils;

/**
 * Created by QinBin on 2019/1/8.
 */

public class MyApplication extends Application{
    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        internationalizationSmartRefreshLayout();
        SoundPlayUtils.init(mContext);
    }

    public static Context getmContext() {
        return mContext;
    }

    /**
     * SmartRefreshLayout国际化
     */
    private void internationalizationSmartRefreshLayout(){
        ClassicsHeader.REFRESH_HEADER_PULLDOWN = getString(R.string.header_pull_down);//"下拉可以刷新";
        ClassicsHeader.REFRESH_HEADER_REFRESHING = getString(R.string.header_refreshing);//"正在刷新...";
        ClassicsHeader.REFRESH_HEADER_LOADING = getString(R.string.header_loading);//"正在加载...";
        ClassicsHeader.REFRESH_HEADER_RELEASE = getString(R.string.header_release);//"释放立即刷新";
        ClassicsHeader.REFRESH_HEADER_FINISH = getString(R.string.header_finish);//"刷新完成";
        ClassicsHeader.REFRESH_HEADER_FAILED = getString(R.string.header_failed);//"刷新失败";
        ClassicsHeader.REFRESH_HEADER_LASTTIME = getString(R.string.header_update);//"上次更新 M-d HH:mm";

        ClassicsFooter.REFRESH_FOOTER_PULLUP = getString(R.string.footer_pull_up);//"上拉加载更多";
        ClassicsFooter.REFRESH_FOOTER_RELEASE = getString(R.string.footer_release);//"释放立即加载";
        ClassicsFooter.REFRESH_FOOTER_LOADING = getString(R.string.footer_loading);//"正在刷新...";
        ClassicsFooter.REFRESH_FOOTER_REFRESHING = getString(R.string.footer_refreshing);//"正在加载...";
        ClassicsFooter.REFRESH_FOOTER_FINISH = getString(R.string.footer_finish);//"加载完成";
        ClassicsFooter.REFRESH_FOOTER_FAILED = getString(R.string.footer_failed);//"加载失败";
        ClassicsFooter.REFRESH_FOOTER_ALLLOADED = getString(R.string.footer_all_loaded);//"全部加载完成";
    }


}
