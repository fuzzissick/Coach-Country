package com.example.quade_laptop.coachcountry;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
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
import com.google.android.gms.tasks.Task;
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

    private boolean running;
    private long timeWhenStopped;
    private Polyline runningRoute;

    private long previousTime;
    private long currentTime;
    private Pace currentPace;
    private Double distanceTraveled;

    private BroadcastReceiver broadcastReceiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coach_county_session);

        chronometer = (Chronometer) findViewById(R.id.chronometer);
        pace = findViewById(R.id.paceField);
        distance = findViewById(R.id.distanceField);
        distanceTraveled = 0.0;

        currentPace = new Pace(0,0);
        runningFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.runningMap);
        runningFragment.getMapAsync(this);


        running = false;
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
                startService(i);
               // testMap();
                startChronometer();

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
                        Double paceVal = Pace.calculatePace(Double.parseDouble(intent.getExtras().get("distance").toString()),currentTime,previousTime);
                        distanceTraveled = (distanceTraveled) + (Double.parseDouble(intent.getExtras().get("distance").toString()) * 0.00062137);
                        distance.setText((new DecimalFormat("$#.00").format(distanceTraveled)));
                        currentPace.setMinute((int)Math.round(paceVal));
                        currentPace.setSeconds((int)(paceVal - currentPace.getMinute()) * 60);
                        pace.setText(currentPace.getMinute() + ":" + currentPace.getSeconds());
                    }
                    drawRoute(newPoint);
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

    private void calculateBetweenPoints(LatLng pointA, LatLng pointB, double timeBetween){
        Double distance = SphericalUtil.computeDistanceBetween(pointA, pointB) * 0.00062137;

    }


    public void testMap() {
        LatLng one = new LatLng(42.620973, -87.821310);
        LatLng two = new LatLng(42.620404, -87.821567);
        LatLng three = new LatLng(42.619267, -87.822639);
        LatLng four = new LatLng(42.618699, -87.822983);
        LatLng five = new LatLng(42.618857, -87.823883);
        LatLng six = new LatLng(42.620152, -87.824656);
        LatLng seven = new LatLng(42.621383, -87.824913);
        LatLng eight = new LatLng(42.621983, -87.824656);
        LatLng nine = new LatLng(42.623499, -87.823969);
        LatLng ten = new LatLng(42.624699, -87.822253);
        LatLng eleven = new LatLng(42.624730, -87.820709);
        LatLng twelve = new LatLng(42.623846, -87.820109);
        LatLng thirteen = new LatLng(42.622646, -87.820409);
        LatLng fourteen = new LatLng(42.621446, -87.820966);
        final List<LatLng> testPoints = new ArrayList<LatLng>();
        testPoints.add(one);
        testPoints.add(two);
        testPoints.add(three);
        testPoints.add(four);
        testPoints.add(five);
        testPoints.add(six);
        testPoints.add(seven);
        testPoints.add(eight);
        testPoints.add(nine);
        testPoints.add(ten);
        testPoints.add(eleven);
        testPoints.add(twelve);
        testPoints.add(thirteen);
        testPoints.add(fourteen);

        Handler handler = new Handler();

        handler.postDelayed(
                new Runnable() {
                public void run() {
            drawRoute(testPoints.get(0));
            testPoints.remove(0);
                }
            }, 0);

        handler.postDelayed(
                new Runnable() {
                    public void run() {
                        drawRoute(testPoints.get(0));
                        testPoints.remove(0);
                    }
                }, 10000);
        handler.postDelayed(
                new Runnable() {
                    public void run() {
                        drawRoute(testPoints.get(0));
                        testPoints.remove(0);
                    }
                }, 20000);
        handler.postDelayed(
                new Runnable() {
                    public void run() {
                        drawRoute(testPoints.get(0));
                        testPoints.remove(0);
                    }
                }, 30000);
        handler.postDelayed(
                new Runnable() {
                    public void run() {
                        drawRoute(testPoints.get(0));
                        testPoints.remove(0);
                    }
                }, 40000);
        handler.postDelayed(
                new Runnable() {
                    public void run() {
                        drawRoute(testPoints.get(0));
                        testPoints.remove(0);
                    }
                }, 50000);
        handler.postDelayed(
                new Runnable() {
                    public void run() {
                        drawRoute(testPoints.get(0));
                        testPoints.remove(0);
                    }
                }, 60000);
        handler.postDelayed(
                new Runnable() {
                    public void run() {
                        drawRoute(testPoints.get(0));
                        testPoints.remove(0);
                    }
                }, 70000);
        handler.postDelayed(
                new Runnable() {
                    public void run() {
                        drawRoute(testPoints.get(0));
                        testPoints.remove(0);
                    }
                }, 80000);
        handler.postDelayed(
                new Runnable() {
                    public void run() {
                        drawRoute(testPoints.get(0));
                        testPoints.remove(0);
                    }
                }, 90000);
        handler.postDelayed(
                new Runnable() {
                    public void run() {
                        drawRoute(testPoints.get(0));
                        testPoints.remove(0);
                    }
                }, 100000);
        handler.postDelayed(
                new Runnable() {
                    public void run() {
                        drawRoute(testPoints.get(0));
                        testPoints.remove(0);
                    }
                }, 110000);
        handler.postDelayed(
                new Runnable() {
                    public void run() {
                        drawRoute(testPoints.get(0));
                        testPoints.remove(0);
                    }
                }, 120000);
        handler.postDelayed(
                new Runnable() {
                    public void run() {
                        drawRoute(testPoints.get(0));
                        testPoints.remove(0);
                    }
                }, 130000);

    }
}
