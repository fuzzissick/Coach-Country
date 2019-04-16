package com.example.quade_laptop.coachcountry;

import android.app.Activity;
import android.content.Intent;
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

import io.grpc.Context;

public class SessionRVAdapter extends RecyclerView.Adapter<SessionRVAdapter.PersonViewHolder> {

    List<CCSession> sessions;
    static Class<?> callbackClass;
    static Activity activity;
    static String runnerID;
    public static class PersonViewHolder extends RecyclerView.ViewHolder {
        CardView cv;
        Activity ActivityContext;
        TextView sessionDistance;
        TextView sessionDate;
        TextView sessionPace;
        TextView sessionDuration;
        String documentID;
        String runnerID;

        PersonViewHolder(View itemView) {
            super(itemView);
            cv = (CardView) itemView.findViewById(R.id.cv);
            sessionDistance = (TextView) itemView.findViewById(R.id.sessionDistance);
            sessionDate = (TextView) itemView.findViewById(R.id.sessionDate);
            sessionDuration = (TextView) itemView.findViewById(R.id.sessionDuration);
            sessionPace = (TextView) itemView.findViewById(R.id.sessionPace);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(v.getContext(), callbackClass);
                    i.putExtra("documentID", documentID);
                    i.putExtra("runnerID", runnerID);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    activity.getApplicationContext().startActivity(i);
                }
            });

        }


    }

    SessionRVAdapter(List<CCSession> sessions, Activity activity, String runnerID, Class<?> cls){
        this.callbackClass = cls;
        this.sessions = sessions;
        this.activity = activity;
        this.runnerID = runnerID;
    }

    @Override
    public int getItemCount() {
        return sessions.size();
    }

    @Override
    public PersonViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.session_card, viewGroup, false);
        PersonViewHolder pvh = new PersonViewHolder(v);
        return pvh;
    }

    @Override
    public void onBindViewHolder(PersonViewHolder personViewHolder, int i) {
        personViewHolder.sessionDate.setText(sessions.get(i).getSessionDate().toDate().toString());
        personViewHolder.sessionDistance.setText("Distance: " + new DecimalFormat("#.00mi").format(Double.parseDouble(sessions.get(i).getSessionDistance().toString())));
        personViewHolder.sessionPace.setText("Pace: " + sessions.get(i).getSessionPace().getPaceString());
        personViewHolder.sessionDuration.setText("Duration: " + sessions.get(i).getSessionDuration());
        personViewHolder.documentID = sessions.get(i).getDocumentID();
        personViewHolder.runnerID = this.runnerID;


    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }
}
