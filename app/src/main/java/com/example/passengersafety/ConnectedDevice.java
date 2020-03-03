package com.example.passengersafety;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ConnectedDevice extends AppCompatActivity {

    //region Property Intialization
    public static final String CHANNEL_1_ID = "channel1";
    private NotificationManagerCompat notificationManager;
    private TextView temperatureText, temperatureIncoming, data;
    public ReadInput mReadThread = null;
    private BluetoothDevice remoteDevice;
    private Button btnDis;
    private String address = null;
    private String brand, model, licensePlate, message;
    private ProgressDialog progress;
    private BluetoothAdapter myBluetooth = null;
    private BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    private BluetoothDisconnect bluetoothDisconnect;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final long START_TIME_IN_MILLIS = 10000;
    private long mTimeLeftInMillis = START_TIME_IN_MILLIS;
    private TextView mTextViewCountDown;
    private Button mButtonStop;
    private CountDownTimer mCountDownTimer;
    private boolean mTimerRunning = true;

    public static String BaseUrl = "http://api.openweathermap.org/";
    public static String AppId = "de554215e874f8c7b32de90fad23cf69";
    public static String Zip = "02747";
    public static String lat = "41.63";
    public static String lon = "-71";
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connected_device);

        Intent intent = getIntent();
        address = intent.getStringExtra("Device Address");
        brand = intent.getStringExtra("Brand");
        model = intent.getStringExtra("Model");
        licensePlate = intent.getStringExtra("Plate");

        btnDis = findViewById(R.id.disconnect_button);
        temperatureText = findViewById(R.id.temperature_text);
        temperatureIncoming = findViewById(R.id.tempReceived);
        mTextViewCountDown = findViewById(R.id.text_view_countdown);
        mButtonStop = findViewById(R.id.button_stop);
        data = findViewById(R.id.weatherData);

        createNotificationChannel();
        notificationManager = NotificationManagerCompat.from(this);

        new ConnectBT().execute();

        btnDis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DisconnectOnBtnClick();
            }
        });

        bluetoothDisconnect = new BluetoothDisconnect();
        IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        registerReceiver(bluetoothDisconnect, intentFilter);

        mButtonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCountDownTimer.cancel();
                mTimerRunning = false;
                mButtonStop.setVisibility(View.INVISIBLE);
                Intent intent = new Intent(ConnectedDevice.this, PairedDevices.class);
                startActivity(intent);
            }
        });
    }

    protected  void onDestroy() {
        super.onDestroy();
        unregisterReceiver(bluetoothDisconnect);
}

    public void getCurrentData(){

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BaseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        WeatherService service = retrofit.create(WeatherService.class);
        Call<WeatherResponse> call = service.getCurrentWeatherData(Zip,AppId);
        call.enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                if(response.code() == 200){
                    WeatherResponse weatherResponse = response.body();
                    assert weatherResponse != null;

                    double tempInFah = (weatherResponse.main.temp - 273.15) * 9/5 + 32;

                    String stringBuilder = String.format("Temperature:  %.2f" ,tempInFah );
                    data.setText(stringBuilder);
                }
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                data.setText(t.getMessage());
            }
        });
}

    private void DisconnectOnBtnClick() {

        if (mReadThread != null) {
            mReadThread.stop();
            do {

            } while (mReadThread.isRunning());
            mReadThread = null;
        }
        try {
            btSocket.close();
        }
        catch(IOException e)
        {
            Toast.makeText(getApplicationContext(), "Could not disconnect", Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    private void sendSMS() {
        try {
            message = brand + " " + model + " " + licensePlate;
            SmsManager sms = SmsManager.getDefault();
            sms.sendTextMessage("5089715596",null,message,null,null);
            Toast.makeText(this,"Message Sent",Toast.LENGTH_SHORT).show();
        }
        catch (Exception e) {
            Toast.makeText(this, "Could not send message",Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_1_ID, "Passenger Safety", importance);
            channel.setDescription("This is Channel 1");
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private class BluetoothDisconnect extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {

            //SendNotification();
            sendSMS();
            startTimer();

            try {
                btSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
                Toast.makeText(ConnectedDevice.this, "Calling", Toast.LENGTH_SHORT).show();
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

    private void SendNotification()
    {
        Intent landingIntent = new Intent(this, ConnectedDevice.class);
        //landingIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent landingPendingIntent = PendingIntent.getActivity(ConnectedDevice.this,
                0, landingIntent,PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(ConnectedDevice.this,CHANNEL_1_ID)
                .setSmallIcon(R.drawable.ic_bluetooth)
                .setContentTitle("Bluetooth Notification")
                .setStyle(new NotificationCompat.BigTextStyle().bigText("Your Bluetooth Connection has been lost from the device. Calling Emergency Contact in 10 minutes"))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentIntent(landingPendingIntent);
        notificationManager.notify(1,builder.build());
    }

    private class ConnectBT extends AsyncTask<Void, Void, Void> {
        private boolean ConnectSuccess = true;

        @Override
        protected  void onPreExecute () {
            progress = ProgressDialog.show(ConnectedDevice.this, "Connecting...", "Please Wait!!!");
        }

        @Override
        protected Void doInBackground (Void... devices) {
            try {
                if ( btSocket==null || !isBtConnected ) {
                    myBluetooth = BluetoothAdapter.getDefaultAdapter();
                    remoteDevice = myBluetooth.getRemoteDevice(address);
                    btSocket = remoteDevice.createInsecureRfcommSocketToServiceRecord(myUUID);
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();
                }
            }
            catch (IOException e) {
                ConnectSuccess = false;
            }
            return null;
        }

        @Override
        protected void onPostExecute (Void result) {
            super.onPostExecute(result);

            if (!ConnectSuccess) {
                Toast.makeText(getApplicationContext(), "Connection Failed. Make sure your device is in range", Toast.LENGTH_SHORT).show();
                finish();
            }
            else {
                isBtConnected = true;
                mReadThread = new ReadInput();
                getCurrentData();
            }
            progress.dismiss();
        }
    }

    private class ReadInput implements Runnable {

    private boolean bStop = false;
    private String strInput;

    private Thread f2t = new Thread(this, "Input Thread");

    public ReadInput() {
        f2t.start();
    }

    public boolean isRunning() {
        return f2t.isAlive();
    }

    public void run() {
        try {
            InputStream inputStream = btSocket.getInputStream();
            while (!bStop) {
                byte[] buffer = new byte[256];
                if (inputStream.available() > 0) {
                    inputStream.read(buffer);
                    int i = 0;
                    while (i < buffer.length && buffer[i] != 0) {
                        i++;
                    }
                    strInput = new String(buffer, 0, i);
                    temperatureText.post(new Runnable() {
                        public void run(){
                            temperatureIncoming.setText(strInput);
                        }
                    });
                }
                 Thread.sleep(500);
            }
        } catch (IOException e){
            Toast.makeText(ConnectedDevice.this,"Error" + e.toString(),Toast.LENGTH_SHORT ).show();
        } catch (InterruptedException e2){
            e2.printStackTrace();
        }
    }

    public void stop(){
        bStop = true;
    }
}
}