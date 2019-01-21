package com.example.quade_laptop.coachcountry;

import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class SessionView extends AppCompatActivity implements OnMapReadyCallback {

    private TextView distanceField;
    private TextView timeField;
    private TextView averagePaceField;
    private Button home;
    private GoogleMap runningMap;
    private SupportMapFragment runningFragment;
    private Polyline runningRoute;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session_summary);
        runningFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.summaryMap);
        runningFragment.getMapAsync(this);

        Intent i = getIntent();

        String documentID = i.getStringExtra("documentID");

        if (documentID == ""){
            Toast.makeText(getApplicationContext(), "Couldn't retrieve session.", Toast.LENGTH_LONG);
        } else {
            distanceField = findViewById(R.id.distanceField);
            timeField = findViewById(R.id.timeField);
            averagePaceField = findViewById(R.id.paceField);
            setSessionSummary(documentID);
        }
    }

        private void setSessionSummary(String documentID){
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
            FirebaseUser mFirebaseUser = mFirebaseAuth.getCurrentUser();

            db.collection("runners").document(mFirebaseUser.getUid()).collection("sessions").document(documentID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful())
                    {
                        DocumentSnapshot session = task.getResult();
                        Pace pace = new Pace(0,0);
                        Double distDbl = Double.parseDouble(session.get("sessionDistance").toString());
                        List<GeoPoint> geopoints;
                        List<LatLng> route = new ArrayList<LatLng>();
                        geopoints = (List<GeoPoint>) session.get("locations");

                        for(GeoPoint geopoint: geopoints){
                            route.add(new LatLng(geopoint.getLatitude(),geopoint.getLongitude()));
                        }
                        distanceField.setText(new DecimalFormat("#.00mi").format(distDbl));
                        timeField.setText(session.get("sessionDuration").toString());
                        averagePaceField.setText(session.get("sessionPace.paceString").toString());

                        PolylineOptions lineOptions = new PolylineOptions().width(5).color(Color.RED);
                        runningRoute = runningMap.addPolyline(lineOptions);

                        runningRoute.setPoints(route);

                        LatLngBounds.Builder builder = new LatLngBounds.Builder();
                        builder.include(route.get(0));
                        builder.include(route.get(route.size()-1));
                        LatLngBounds bounds = builder.build();

                        int padding = 0; // padding around start and end marker
                        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                        runningMap.animateCamera(cu);
                    }
                }
            });

        }

        @Override
        public void onMapReady(GoogleMap map) {
            // DO WHATEVER YOU WANT WITH GOOGLEMAP
            runningMap = map;
            runningMap.setIndoorEnabled(true);

        }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }
}
