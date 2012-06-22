package classes;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.access2.R;

public class CustomBaseAdapter extends BaseAdapter {

private ArrayList<TripListItem> items;
private LayoutInflater layoutInflater;
private Context context;

public CustomBaseAdapter(Context context, ArrayList<TripListItem> results) {
    this.items = results;
    //this.context = context;
    layoutInflater = LayoutInflater.from(context);
}

@Override
public int getCount() {
    return items.size();
}

@Override
public Object getItem(int position) {
    return items.get(position);
}

@Override
public long getItemId(int position) {
    return position;
}

@Override
public View getView(int position, View convertView, ViewGroup parent) {
    ViewHolder holder;

    if(convertView == null) {
        convertView = layoutInflater.inflate(R.layout.triplistdetails, null);

        holder = new ViewHolder();
        holder.dateDistance = (TextView) convertView.findViewById(R.id.textView1);
        holder.from = (TextView) convertView.findViewById(R.id.textView2);
        holder.to = (TextView) convertView.findViewById(R.id.textView3);


        convertView.setTag(holder);
    }
    else {
        holder = (ViewHolder) convertView.getTag();
    }

    
    String startDate = items.get(position).getStartTime().toString("MM/dd/yy");
    holder.dateDistance.setText(startDate + ", " + items.get(position).getDistance() + " miles");
    holder.from.setText(items.get(position).getStartLocation());
    holder.to.setText(items.get(position).getEndLocation());

    return convertView;
}

class ViewHolder {
    TextView dateDistance;
    TextView from;
    TextView to;
}

}
