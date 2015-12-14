package com.app.konumbul.app;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class VenueAdapter extends BaseAdapter{

    private LayoutInflater mInflater;
    private List<Venue> venueList;
    private Context context;

    public VenueAdapter(Activity activity, List<Venue> venueList, Context context) {
        mInflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.venueList = venueList;
        this.context = context;
    }
    @Override
    public int getCount() {
        return venueList.size();
    }

    @Override
    public Venue getItem(int position) {
        return venueList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View satirView;

        satirView = mInflater.inflate(R.layout.activity_venue, null);

        TextView textView =
                (TextView) satirView.findViewById(R.id.listImageText);
        ImageView imageView =
                (ImageView) satirView.findViewById(R.id.listImageView);

        Venue venue = venueList.get(position);

        textView.setText(venue.getVenueString());

        imageView.setImageResource(context.getResources().getIdentifier(venue.getVenueType(), "drawable",  context.getPackageName()));

        return satirView;
    }
}
