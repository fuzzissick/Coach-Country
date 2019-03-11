package com.example.quade_laptop.coachcountry;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.util.List;

public class RunnerRVAdapter extends RecyclerView.Adapter<RunnerRVAdapter.PersonViewHolder> {

    List<Runner> runners;
    static Activity activity;
    public static class PersonViewHolder extends RecyclerView.ViewHolder {
        CardView cv;
        Activity ActivityContext;
        TextView full_name;
        TextView event_group;
        TextView year_in_school;
        TextView current_status;
        ImageView blip_color;
        String documentID;

        PersonViewHolder(View itemView) {
            super(itemView);
            cv = (CardView) itemView.findViewById(R.id.cv);
            full_name = (TextView) itemView.findViewById(R.id.full_name);
            event_group = (TextView) itemView.findViewById(R.id.event_group);
            year_in_school = (TextView) itemView.findViewById(R.id.year_in_school);
            current_status = (TextView) itemView.findViewById(R.id.current_status);
            blip_color = itemView.findViewById(R.id.blip_color);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    Intent i = new Intent(v.getContext(), SessionView.class);
//                    i.putExtra("documentID", documentID);
//                    activity.getApplicationContext().startActivity(i);

                    Toast.makeText(v.getContext(), "Click!", Toast.LENGTH_LONG);
                }
            });

        }


    }

    RunnerRVAdapter(List<Runner> runners, Activity activity){
        this.runners = runners;
        this.activity = activity;
    }

    @Override
    public int getItemCount() {
        return runners.size();
    }

    @Override
    public PersonViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.runner_card, viewGroup, false);
        PersonViewHolder pvh = new PersonViewHolder(v);
        return pvh;
    }

    @Override
    public void onBindViewHolder(PersonViewHolder personViewHolder, int i) {
        personViewHolder.full_name.setText(runners.get(i).getFullName());
        personViewHolder.event_group.setText("Event: " + runners.get(i).getEvent());
        personViewHolder.year_in_school.setText("Year: " + runners.get(i).getYear());
        personViewHolder.current_status.setText("Status: " + "Yeet"); //*****************************8
        personViewHolder.documentID = runners.get(i).getDocumentID();
        personViewHolder.blip_color.setBackgroundColor(Color.HSVToColor(new float[] { Integer.parseInt(runners.get(i).getColor()), 1.0f, 1.0f }));


    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }
}
