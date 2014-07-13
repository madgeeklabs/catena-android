package es.goofyahead.mykeys.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.google.inject.Inject;

import java.util.ArrayList;

import es.goofyahead.mykeys.R;
import es.goofyahead.mykeys.models.Device;
import roboguice.RoboGuice;
import roboguice.inject.RoboInjector;

/**
 * Created by goofyahead on 7/12/14.
 */
public class DevicesAdapter extends BaseAdapter{
    private LayoutInflater mInflater;
    private ArrayList<Device> elements;
    private Context mContext;
    @Inject
    ImageLoader loader;

    public DevicesAdapter(Context mContext, ArrayList<Device> orders) {
        mInflater = LayoutInflater.from(mContext);
        this.mContext = mContext;
        this.elements = orders;
        final RoboInjector injector = RoboGuice.getInjector(mContext);
        injector.injectMembersWithoutViews(this);
    }

    @Override
    public int getCount() {
        return elements.size();
    }

    @Override
    public Object getItem(int position) {
        return elements.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Device current = (Device) getItem(position);
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.device_item_layout, null);
            holder.name = (TextView) convertView.findViewById(R.id.name);
            holder.price = (TextView) convertView.findViewById(R.id.price);
            holder.image = (NetworkImageView) convertView.findViewById(R.id.item_image);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.name.setText(current.getName());
        holder.price.setText("" + current.getCost());
        holder.image.setImageUrl(current.getImageUrl(), loader);
        return convertView;
    }

    private class ViewHolder {
        private NetworkImageView image;
        private TextView name;
        private TextView price;
    }

}
