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
import yoyon.smartlock.standalone.model.YoyonKey;


/**
 * Created by user on 17/11/2.
 */

public class LockKeyListViewAdapter extends BaseAdapter{
    private List<YoyonKey> yoyonKeys;
    private LayoutInflater inflater;
    private Context context;
    public LockKeyListViewAdapter(Context context, List<YoyonKey> yoyonKeys){
        this.yoyonKeys = yoyonKeys;
        inflater = LayoutInflater.from(context);
        this.context = context;
    }
    @Override
    public int getCount() {
        if(yoyonKeys != null){
            return yoyonKeys.size();
        }else{
            return 0;
        }
    }

    @Override
    public YoyonKey getItem(int position) {
        return yoyonKeys.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder holder=null;
        if(convertView==null){
            holder = new Holder();
            convertView= inflater.inflate(R.layout.item_lock_key,parent,false);
            holder.keyIcon = convertView.findViewById(R.id.bluetoothDeviceKeyList_icon);
            holder.keyName = convertView.findViewById(R.id.bluetoothDeviceKeyList_keyName);
            holder.keyID = convertView.findViewById(R.id.bluetoothDeviceKeyList_keyID);
            holder.keyProperty = convertView.findViewById(R.id.bluetoothDeviceKeyList_keyProperty);
            convertView.setTag(holder);
        }else {
            holder= (Holder)convertView.getTag();
        }

        holder.keyID.setText(context.getResources().getString(R.string.id)+":"+yoyonKeys.get(position).getIndexNumber());
        holder.keyProperty.setText(yoyonKeys.get(position).getRole()==YoyonKey.ADMIN ? context.getResources().getString(R.string.admin):context.getResources().getString(R.string.user));
        switch (yoyonKeys.get(position).getType()){
            case YoyonKey.FINGERPRINT_KEY:
                holder.keyIcon.setBackgroundResource(R.drawable.item_icon_fingerprint);
                holder.keyName.setText(yoyonKeys.get(position).getName() == null || yoyonKeys.get(position).getName().equalsIgnoreCase("")?context.getResources().getString(R.string.fingerprint)+"-"+yoyonKeys.get(position).getIndexNumber():yoyonKeys.get(position).getName());
                break;
            case YoyonKey.PASSWORD_KEY:
                holder.keyIcon.setBackgroundResource(R.drawable.item_icon_password);
                holder.keyName.setText(yoyonKeys.get(position).getName() == null || yoyonKeys.get(position).getName().equalsIgnoreCase("")?context.getResources().getString(R.string.password)+"-"+yoyonKeys.get(position).getIndexNumber():yoyonKeys.get(position).getName());
                break;
            case YoyonKey.RFIC_KEY:
                holder.keyIcon.setBackgroundResource(R.drawable.item_icon_iccard);
                holder.keyName.setText(yoyonKeys.get(position).getName() == null || yoyonKeys.get(position).getName().equalsIgnoreCase("")?context.getResources().getString(R.string.proximity_card)+"-"+yoyonKeys.get(position).getIndexNumber():yoyonKeys.get(position).getName());
                break;
            case YoyonKey.BLUETOOTH_KEY:
                holder.keyIcon.setBackgroundResource(R.drawable.item_icon_bluetooth);
                holder.keyName.setText(yoyonKeys.get(position).getName() == null || yoyonKeys.get(position).getName().equalsIgnoreCase("")?context.getResources().getString(R.string.bluetooth)+"-"+yoyonKeys.get(position).getIndexNumber():yoyonKeys.get(position).getName());
                break;
            case YoyonKey.PHYSICS_KEY:
                holder.keyIcon.setBackgroundResource(R.drawable.item_icon_key);
                holder.keyName.setText(yoyonKeys.get(position).getName() == null || yoyonKeys.get(position).getName().equalsIgnoreCase("")?context.getResources().getString(R.string.lockkey)+"-"+yoyonKeys.get(position).getIndexNumber():yoyonKeys.get(position).getName());
                break;
            default:
                break;
        }
        return convertView;
    }
    class Holder{
        ImageView keyIcon;
        TextView keyName;
        TextView keyID;
        TextView keyProperty;
    }
}
