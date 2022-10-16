package yoyon.smartlock.standalone.activity;

import android.Manifest;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.widget.Toast;

import com.ashokvarma.bottomnavigation.BottomNavigationBar;
import com.ashokvarma.bottomnavigation.BottomNavigationItem;

import java.util.ArrayList;
import java.util.List;

import yoyon.smartlock.standalone.R;
import yoyon.smartlock.standalone.adapter.MainActivityFragmentPagerAdapter;
import yoyon.smartlock.standalone.fragment.HomeFragment;
import yoyon.smartlock.standalone.fragment.OperationLogFragment;
import yoyon.smartlock.standalone.fragment.MineFragment;
import yoyon.smartlock.standalone.utils.YoyonUtils;

/**
 * Created by QinBin on 16/12/30.
 */
public class MainActivity extends FragmentActivity {
    private ViewPager vp_fragment;
    private BottomNavigationBar bottomNavigationBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        YoyonUtils.setTransparentStatusBar(this);
        initComponent();
        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
    }

    private void initComponent(){
        initFragment();
        initBottomNavigationBar();
    }
    private Fragment homeFragment,operationLogFragment,mineFragment;
    private void initFragment(){
        vp_fragment = findViewById(R.id.mainActivity_viewpager);
        FragmentManager fragmentManager = getSupportFragmentManager();
        List<Fragment> fragments = new ArrayList<>();
        homeFragment = new HomeFragment();
        operationLogFragment = new OperationLogFragment();
        mineFragment = new MineFragment();
        fragments.add(homeFragment);
        fragments.add(operationLogFragment);
        fragments.add(mineFragment);
        MainActivityFragmentPagerAdapter fragmentPagerAdapter = new MainActivityFragmentPagerAdapter(fragmentManager,fragments);
        vp_fragment.setAdapter(fragmentPagerAdapter);
        vp_fragment.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                bottomNavigationBar.selectTab(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }
    /**
     * 设置底部导航栏
     */
    private void initBottomNavigationBar() {
        bottomNavigationBar =  findViewById(R.id.mainActivity_bottomNavigationBar);
        //设置Icon和文字的显示效果
        bottomNavigationBar.setMode(BottomNavigationBar.MODE_FIXED);
        //设置是否有水波纹效果
        bottomNavigationBar.setBackgroundStyle(BottomNavigationBar.BACKGROUND_STYLE_STATIC);
        //设置背景颜色
        bottomNavigationBar.setBarBackgroundColor(android.R.color.white);
        //设置字体未被选择时颜色
        bottomNavigationBar.setInActiveColor(R.color.bottomNavigationBarInActive);
        //设置字体被选择时颜色
        bottomNavigationBar.setActiveColor(R.color.bottomNavigationBarActive);
        bottomNavigationBar
                .addItem(new BottomNavigationItem(R.drawable.home_select, R.string.home).setInactiveIconResource(R.drawable.home_unselect))
                .addItem(new BottomNavigationItem(R.drawable.operation_log_select, R.string.message).setInactiveIconResource(R.drawable.operation_log_unselect))
                .addItem(new BottomNavigationItem(R.drawable.mine_select, R.string.me).setInactiveIconResource(R.drawable.mine_unselect))
                .initialise();
        bottomNavigationBar.setTabSelectedListener(new BottomNavigationBar.OnTabSelectedListener() {
            @Override
            public void onTabSelected(int position) {
                switch (position) {
                    case 0:
                        vp_fragment.setCurrentItem(0);
                        break;
                    case 1:
                        vp_fragment.setCurrentItem(1);
                        break;
                    case 2:
                        vp_fragment.setCurrentItem(2);
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onTabUnselected(int position) {

            }

            @Override
            public void onTabReselected(int position) {

            }
        });
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        ((HomeFragment)homeFragment).updateListView();
        ((OperationLogFragment)operationLogFragment).updateListView();
    }
    private long exitTime = 0;

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
            mExit();
            return true;
        } else {
            return super.dispatchKeyEvent(event);
        }
    }

    private void mExit() {
        if ((System.currentTimeMillis() - exitTime) > 2000) {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.exitConfirm), Toast.LENGTH_SHORT).show();
            exitTime = System.currentTimeMillis();
        } else{
            finish();
            System.exit(0);
        }
    }
}
