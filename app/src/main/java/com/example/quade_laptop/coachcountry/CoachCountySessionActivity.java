package com.example.quade_laptop.coachcountry;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Session;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.model.Document;
import com.google.maps.android.SphericalUtil;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CoachCountySessionActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "CCSession";

    private Button pause;
    private Button start;
    private Button resume;
    private Button stopWorkout;
    private Chronometer chronometer;
    private GoogleMap runningMap;
    private SupportMapFragment runningFragment;
    private TextView pace;
    private TextView distance;
    private int sessionNum;

    private boolean running;
    private boolean started;
    private long timeWhenStopped;
    private Polyline runningRoute;

    private long previousTime;
    private long currentTime;
    private Pace currentPace;
    private List<Double> paces;
    private Double distanceTraveled;

    private CCSession FinalSession;
    private LiveSession currentSession;

    private FirebaseFirestore db;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private DocumentReference userDocRef;

    private BroadcastReceiver broadcastReceiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coach_county_session);

        chronometer = (Chronometer) findViewById(R.id.chronometer);
        pace = findViewById(R.id.paceField);
        distance = findViewById(R.id.distanceField);
        runningFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.runningMap);
        runningFragment.getMapAsync(this);

        FinalSession = new CCSession();
        currentSession = new LiveSession();

        distanceTraveled = 0.0;
        currentPace = new Pace(0,0);
        running = false;
        started = false;
        paces = new ArrayList<Double>();

        db = FirebaseFirestore.getInstance();

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

       userDocRef = db.collection("runners").document(mFirebaseUser.getUid());

       userDocRef.collection("sessions").orderBy("sessionNum",Query.Direction.DESCENDING).limit(1).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful())
                {
                    QuerySnapshot result = task.getResult();
                    List<DocumentSnapshot> results = result.getDocuments();
                    if(results.size() == 0)
                        sessionNum = 1;
                    else
                        sessionNum = Integer.parseInt(results.get(0).get("sessionNum").toString()) + 1;
                }
            }
        });




        if(!runtime_permissions()) {
            setButtons();
        }
    }

    private boolean runtime_permissions() {
        if(Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){

            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},100);

            return true;
        }
        return false;
    }



    private void setButtons(){
        pause = (Button) findViewById(R.id.pauseSession);
        start = (Button) findViewById(R.id.start);
        resume = (Button) findViewById(R.id.resumeSession);
        stopWorkout = (Button) findViewById(R.id.finishWorkout);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i =new Intent(getApplicationContext(),GPSService.class);
                if(start.isEnabled() == true){
                    start.setEnabled(false);
                    start.setVisibility(View.INVISIBLE);
                    pause.setEnabled(true);
                    pause.setVisibility(View.VISIBLE);
                }
                currentSession.setRunning(true);
                currentSession.update(userDocRef);
                startService(i);
                startChronometer();
                PolylineOptions lineOptions = new PolylineOptions().width(5).color(Color.RED);
                runningRoute = runningMap.addPolyline(lineOptions);
            }
        });

        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent i = new Intent(getApplicationContext(),GPSService.class);
                if(pause.isEnabled() == true){
                    pause.setEnabled(false);
                    pause.setVisibility(View.INVISIBLE);
                    resume.setEnabled(true);
                    resume.setVisibility(View.VISIBLE);
                }
                currentSession.setRunning(false);
                currentSession.update(userDocRef);
                stopService(i);
                stopChronometer();

            }
        });

        resume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent i = new Intent(getApplicationContext(),GPSService.class);
                if(resume.isEnabled() == true){
                    resume.setEnabled(false);
                    resume.setVisibility(View.INVISIBLE);
                    pause.setEnabled(true);
                    pause.setVisibility(View.VISIBLE);
                }
                currentSession.setRunning(true);
                currentSession.update(userDocRef);
                startService(i);
               // testMap();
                startChronometer();

            }
        });

        stopWorkout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(running){
                    stopService(new Intent(getApplicationContext(),GPSService.class));
                    stopChronometer();
                    FinalSession.setSessionPace(Pace.calculateAveragePace(paces));
                    FinalSession.setSessionDuration(currentTime);
                    FinalSession.setSessionNum(sessionNum);
                }
                currentSession.setRunning(false);
                currentSession.update(userDocRef);
                userDocRef.collection("sessions").document("session" + sessionNum).set(FinalSession).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully written!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                    }
                });
                Intent i = new Intent(CoachCountySessionActivity.this, SessionSummary.class);
                i.putExtra("sessionNum", sessionNum);
                startActivity(i);
                finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(broadcastReceiver != null){
            unregisterReceiver(broadcastReceiver);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 100){
            if( grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED){
                setButtons();
            }else {
                runtime_permissions();
            }
        }
    }

    protected void onResume() {
        super.onResume();
        if(broadcastReceiver == null){
            broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    previousTime = currentTime;
                    currentTime = SystemClock.elapsedRealtime() - chronometer.getBase();
                    LatLng newPoint = new LatLng(Double.parseDouble(intent.getExtras().get("latitude").toString()), Double.parseDouble(intent.getExtras().get("longitude").toString()));
                    if(intent.getExtras().get("distance") != null) {
                        Log.d(TAG, intent.getExtras().get("distance").toString());

                        //Calculations
                        Double paceVal = Pace.calculatePace(Double.parseDouble(intent.getExtras().get("distance").toString()),currentTime,previousTime);
                        Double prevDist = distanceTraveled;
                        distanceTraveled = (distanceTraveled) + (Double.parseDouble(intent.getExtras().get("distance").toString()) * 0.00062137);

                        //Update textViews
                        distance.setText((new DecimalFormat("#.00mi").format(distanceTraveled)));
                        pace.setText(currentPace.getPaceString());

                        //Update local variables
                        currentPace.setMinute((int)Math.floor(paceVal));
                        currentPace.setSeconds((int)Math.round(((paceVal - currentPace.getMinute()) * 60)));
                        paces.add(paceVal);

                        //Update Live Database
                        currentSession.setCurrentDistance(distanceTraveled);
                        currentSession.setCurrentDuration(currentTime);
                        currentSession.setCurrentLocation(newPoint);
                        currentSession.setCurrentPace(currentPace);
                        currentSession.update(userDocRef);

                        //notify user of mile ran
                        if(Math.floor(distanceTraveled) > Math.floor(prevDist)) {
                            Toast.makeText(context, "You ran " + (new DecimalFormat("#.00mi").format(distanceTraveled)), Toast.LENGTH_LONG).show();
                        }

                        drawRoute(newPoint);

                        //update Final Session
                        FinalSession.setSessionDistance(distanceTraveled);
                        FinalSession.addLocation(newPoint);
                    }

                }
            };
        }
        registerReceiver(broadcastReceiver,new IntentFilter("location_update"));
    }

    protected void startChronometer(){
     if(!running){
         chronometer.start();
         chronometer.setBase(SystemClock.elapsedRealtime() + timeWhenStopped);
         currentTime = SystemClock.elapsedRealtime() - chronometer.getBase();
         started = true;
         running = true;
     }
    }

    protected void stopChronometer(){
        if(running){
            chronometer.stop();
            timeWhenStopped = chronometer.getBase() - SystemClock.elapsedRealtime();
            running = false;
        }
    }

    protected void drawRoute(LatLng newPoint){
        List<LatLng> points = runningRoute.getPoints();
        points.add(newPoint);
        runningRoute.setPoints(points);
        runningMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newPoint,18));
    }

    @Override
    public void onMapReady(GoogleMap map) {
        // DO WHATEVER YOU WANT WITH GOOGLEMAP
        runningMap = map;
        runningMap.setIndoorEnabled(true);
    }


}
