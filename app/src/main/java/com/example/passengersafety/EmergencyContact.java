package com.example.passengersafety;

import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Locale;

public class EmergencyContact extends AppCompatActivity {

    //private static final long START_TIME_IN_MILLIS = 600000; //Timer set for 10 minutes
    private long mTimeLeftInMillis = START_TIME_IN_MILLIS;

    private static final long START_TIME_IN_MILLIS = 10000;

    private TextView mTextViewCountDown;
    private Button mButtonStop;
    private CountDownTimer mCountDownTimer;
    private boolean mTimerRunning = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_contact);

        mTextViewCountDown = findViewById(R.id.text_view_countdown);
        mButtonStop = findViewById(R.id.button_stop);

        startTimer();

        mButtonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCountDownTimer.cancel();
                mTimerRunning = false;
                mButtonStop.setVisibility(View.INVISIBLE);
                Intent intent = new Intent(EmergencyContact.this, PairedDevices.class);
                startActivity(intent);
            }
        });
        updateCountDownText();
    }
    private void startTimer() {
        mCountDownTimer = new CountDownTimer(mTimeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                mTimeLeftInMillis = millisUntilFinished;
                updateCountDownText();
                mButtonStop.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFinish() {
                mTimerRunning = false;
                mButtonStop.setVisibility(View.INVISIBLE);

                Toast.makeText(EmergencyContact.this, "Calling", Toast.LENGTH_SHORT).show();

                callNumber();

            }
        }.start();

        mTimerRunning = true;
        mButtonStop.setText("Stop");
    }

    @SuppressLint("MissingPermission")
    private void callNumber()
    {
        Intent phoneIntent = new Intent(Intent.ACTION_CALL);
        phoneIntent.setData(Uri.parse("tel:5088631994"));
        startActivity(phoneIntent);
    }

    private void updateCountDownText() {
        int minutes = (int) (mTimeLeftInMillis / 1000) / 60;
        int seconds = (int) (mTimeLeftInMillis / 1000) % 60;

        String timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        mTextViewCountDown.setText(timeLeftFormatted);
    }

}
