package com.example.quade_laptop.coachcountry;

public class Pace {

    private int seconds;
    private int minute;

    public int getMinute() {
        return minute;
    }

    public int getSeconds() {
        return seconds;
    }


    public void setMinute(int minute) {
        this.minute = minute;
    }

    public void setSeconds(int seconds) {
        this.seconds = seconds;
    }

    Pace(int minute, int seconds){
        this.minute = minute;
        this.seconds = seconds;
    }

    public static Double calculatePace(Double distance, long currentTime, long previousTime){
        Double timeInHour = ((currentTime - previousTime)/1000.0)/3600.0;
        Double inMiles = distance/1609.344;
        Double mph = inMiles/timeInHour;
        Double pace = 60/mph;
        return pace;
    }
}
