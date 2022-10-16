package yoyon.smartlock.standalone.adapter;

import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by QinBin on 2018/6/28.
 */

public class PopupWindowViewPagerAdapter extends PagerAdapter {
    private List<View> views;
    public PopupWindowViewPagerAdapter(List<View> views) {
        this.views = views;
    }
    @Override
    public int getCount() {
        return views.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }
    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        // return super.instantiateItem(container, position);
        View v= views.get(position);
        ViewGroup parent = (ViewGroup) v.getParent();
        //Log.i("ViewPaperAdapter", parent.toString());
        if (parent != null) {
            parent.removeAllViews();
        }
        container.addView(v);
        return v;
    }
    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        //super.destroyItem(container, position, object);
        container.removeView(views.get(position));
    }

}
