package com.example.quade_laptop.coachcountry;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.List;

public class SessionRVAdapter extends RecyclerView.Adapter<SessionRVAdapter.PersonViewHolder> {

    List<CCSession> sessions;
    public static class PersonViewHolder extends RecyclerView.ViewHolder {
        CardView cv;
        TextView sessionDistance;
        TextView sessionDate;
        TextView sessionPace;
        TextView sessionDuration;

        PersonViewHolder(View itemView) {
            super(itemView);
            cv = (CardView) itemView.findViewById(R.id.cv);
            sessionDistance = (TextView) itemView.findViewById(R.id.sessionDistance);
            sessionDate = (TextView) itemView.findViewById(R.id.sessionDate);
            sessionDuration = (TextView) itemView.findViewById(R.id.sessionDuration);
            sessionPace = (TextView) itemView.findViewById(R.id.sessionPace);
        }


    }

    SessionRVAdapter(List<CCSession> sessions){
        this.sessions = sessions;
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
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }
}
