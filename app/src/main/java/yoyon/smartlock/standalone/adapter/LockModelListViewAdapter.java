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

/**
 * Created by QinBin on 2017/10/4.
 */

public class LockModelListViewAdapter extends BaseAdapter {
    private Context context;
    private List<String> lockModels;
    public LockModelListViewAdapter(Context context,List<String> lockModels) {
        this.context = context;
        this.lockModels = lockModels;
    }

    @Override
    public int getCount() {
        return lockModels.size();
    }

    @Override
    public Object getItem(int position) {
        return lockModels.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder holder = null;
        if(convertView == null){
            holder = new Holder();
            convertView = LayoutInflater.from(context).inflate(R.layout.item_lock_model,parent,false);
            holder.device_icon = convertView.findViewById(R.id.item_lock_model_icon);
            holder.device_name = convertView.findViewById(R.id.item_lock_model_name);
            holder.device_icon.setImageResource(R.drawable.lock_icon);
            holder.device_name.setText(lockModels.get(position));
            convertView.setTag(holder);
        }else {
            holder = (Holder) convertView.getTag();
        }

        return convertView;
    }
    class Holder{
        ImageView device_icon;
        TextView device_name;
    }
}
