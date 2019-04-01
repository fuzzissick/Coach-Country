package com.example.quade_laptop.coachcountry;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class CurrentRunnersAdapter extends BaseAdapter {
    private Context mContext;
    private List<Runner> runners;

    // Gets the context so it can be used later
    public CurrentRunnersAdapter(List<Runner> runners, Context c) {
        this.runners = runners;
        mContext = c;
    }

    // Total number of things contained within the adapter
    public int getCount() {
        return runners.size();
    }

    // Require for structure, not really used in my code.
    public Object getItem(int position) {
        return null;
    }

    // Require for structure, not really used in my code. Can
    // be used to get the id of an item in the adapter for
    // manual control.
    public long getItemId(int position) {
        return position;
    }

    public View getView(int position,
                        View convertView, ViewGroup parent) {

        ViewHolderItem viewHolder;


        if (convertView == null) {

            // inflate the layout
            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            convertView = inflater.inflate(R.layout.runner_new_card, parent, false);

            // well set up the ViewHolder
            viewHolder = new ViewHolderItem();
            viewHolder.textViewItem = (TextView) convertView.findViewById(R.id.textView);
            viewHolder.imageViewItem = (ImageView) convertView.findViewById(R.id.runner_presence);
            //viewHolder.findRunnerBtn = convertView.findViewById(R.id.findRunnerBtn);

           // viewHolder.findRunnerBtn.setId(position);
           // viewHolder.findRunnerBtn.setTag(runners.get(position).getDocumentID());

            // store the holder with the view.
            convertView.setTag(viewHolder);


        } else {
            // we've just avoided calling findViewById() on resource everytime
            // just use the viewHolder
            viewHolder = (ViewHolderItem) convertView.getTag();
        }

        // object item based on the position

        // get the TextView from the ViewHolder and then set the text (item name) and tag (item ID) values

        viewHolder.textViewItem.setTag(position);
        convertView.setBackgroundColor(Color.HSVToColor(new float[] { Integer.parseInt(runners.get(position).getColor()), 1.0f, 1.0f }));


        viewHolder.imageViewItem.setBackgroundColor(Color.parseColor((runners.get(position).isOnline() ? "#42f450" : "#FF0000")));
        //viewHolder.imageViewItem.setBackgroundColor(Color.HSVToColor(new float[] { Integer.parseInt(runners.get(position).getColor()), 1.0f, 1.0f }));;

        viewHolder.textViewItem.setText(runners.get(position).getFullName());
        
        return convertView;
    }

    static class ViewHolderItem {
        TextView textViewItem;
        ImageView imageViewItem;
        Button findRunnerBtn;
    }
}