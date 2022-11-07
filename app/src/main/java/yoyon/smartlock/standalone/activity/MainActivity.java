package yoyon.smartlock.standalone.activity;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
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
import yoyon.smartlock.standalone.utils.SharedPreferencesUtil;
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
        showPrivacyPolicyDialog();
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

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void showPrivacyPolicyDialog() {
        if (!agreedPrivacyPolicy()) {
            View view = LayoutInflater.from(this).inflate(R.layout.dialog_uaapp, null);
            AlertDialog alertDialog = new AlertDialog.Builder(this)
                    .setTitle("温馨提示")
                    .setCancelable(false)
                    .setView(view)
                    .setPositiveButton("同意", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            SharedPreferencesUtil.getInstance().saveBoolean("agreed_privacy_policy", true);
                        }
                    })
                    .setNegativeButton("拒绝", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            finish();
                        }
                    })
                    .create();
            TextView message = view.findViewById(R.id.uaapp_message);
            message.setMovementMethod(LinkMovementMethod.getInstance());
            SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder("在您使用该应用前，请您务必认真阅读《用户协议》和《隐私政策》全部条款。您同意并接受全部协议条款后，Smart门锁管家才能竭诚为您服务。");
            ClickableSpan userAgreementClickableSpan = new ClickableSpan() {
                @Override
                public void onClick(@NonNull View view) {
                    startActivity(new Intent(getBaseContext(), UserAgreementActivity.class));
                }
            };
            ClickableSpan privacyPolicyClickableSpan = new ClickableSpan() {
                @Override
                public void onClick(@NonNull View view) {
                    startActivity(new Intent(getBaseContext(), PrivacyPolicyActivity.class));
                }
            };
            spannableStringBuilder.setSpan(userAgreementClickableSpan, 18, 22, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannableStringBuilder.setSpan(privacyPolicyClickableSpan, 25, 29, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            message.setText(spannableStringBuilder);
            alertDialog.show();
        }
    }

    private boolean agreedPrivacyPolicy() {
        return SharedPreferencesUtil.getInstance().getBoolean("agreed_privacy_policy");
    }


}
