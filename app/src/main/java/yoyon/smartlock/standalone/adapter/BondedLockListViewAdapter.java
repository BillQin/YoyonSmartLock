package yoyon.smartlock.standalone.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;


import java.util.List;

import yoyon.smartlock.standalone.R;
import yoyon.smartlock.standalone.model.YoyonLock;

/**
 * Created by user on 17/10/11.
 */

public class BondedLockListViewAdapter extends BaseAdapter {
    private Context context;
    private List<YoyonLock> yoyonLocks;

    public BondedLockListViewAdapter(Context context, List<YoyonLock> yoyonLocks) {
        this.context = context;
        this.yoyonLocks = yoyonLocks;
    }

    @Override
    public int getCount() {
        return yoyonLocks.size();
    }

    @Override
    public Object getItem(int position) {
        return yoyonLocks.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder holder = null;
        if (convertView == null) {
            holder = new Holder();
            convertView = LayoutInflater.from(context).inflate(R.layout.item_bonded_lock, parent, false);
            holder.lock_thumbnail = convertView.findViewById(R.id.item_bonded_lock_thumbnail);
            holder.lock_name = convertView.findViewById(R.id.item_bonded_lock_name);
            holder.lock_onlineState = convertView.findViewById(R.id.item_bonded_lock_onlineState);
            holder.lock_onlineStatePoint = convertView.findViewById(R.id.item_bonded_lock_onlineStatePoint);
            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }
        holder.lock_thumbnail.setImageResource(R.drawable.lock_icon);
        holder.lock_name.setText(yoyonLocks.get(position).getName());
        return convertView;
    }

    class Holder {
        ImageView lock_thumbnail;
        TextView lock_name;
        TextView lock_onlineState;
        ImageView lock_onlineStatePoint;
    }
}