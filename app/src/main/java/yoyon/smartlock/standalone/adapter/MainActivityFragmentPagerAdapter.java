package yoyon.smartlock.standalone.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.List;

/**
 * Created by QinBin on 2018/11/27.
 */

public class MainActivityFragmentPagerAdapter extends FragmentStatePagerAdapter {
    private List<Fragment> fragments;
    public MainActivityFragmentPagerAdapter(FragmentManager fragmentManager, List<Fragment> fragments){
        super(fragmentManager);
        this.fragments = fragments;
        fragmentManager.beginTransaction().commitAllowingStateLoss();
    }
    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }
}
