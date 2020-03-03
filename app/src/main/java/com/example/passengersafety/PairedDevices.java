package com.example.passengersafety;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Set;

public class PairedDevices extends AppCompatActivity {

    //region Property Initialization
    private Button btnPaired;
    private ListView devices;
    private BluetoothAdapter myBluetooth = null;
    private Set<BluetoothDevice> pairedDevices;
    //public static String EXTRA_ADDRESS = "device_address";
    public String brand, model, licensePlate, address;
    public int permissionRequestCode = 1;
    public String[] PERMISSIONS ={Manifest.permission.CALL_PHONE, Manifest.permission.SEND_SMS};
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paired_devices);

        btnPaired = findViewById(R.id.pair_device_button);
        devices = findViewById(R.id.select_device_list);

        Intent carInformation = getIntent();

        brand = carInformation.getStringExtra("Brand");
        model = carInformation.getStringExtra("Model");
        licensePlate = carInformation.getStringExtra("Plate");
        address = carInformation.getStringExtra("Device Address");


        checkPermission(this,PERMISSIONS);

        if(!checkPermission(this,PERMISSIONS)){
            ActivityCompat.requestPermissions(this,PERMISSIONS,permissionRequestCode);
        }
        turnBluetoothOn();
        btnPaired.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pairedDevicesList();
            }
        });
    }

    private void turnBluetoothOn()
    {
        myBluetooth = BluetoothAdapter.getDefaultAdapter();
        if ( myBluetooth==null ) {
            Toast.makeText(getApplicationContext(), "Bluetooth device not available", Toast.LENGTH_SHORT).show();
            finish();
        } else if ( !myBluetooth.isEnabled() ) {
            Intent turnBTon = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnBTon, 1);
        }
    }
    private void pairedDevicesList () {
        pairedDevices = myBluetooth.getBondedDevices();
        ArrayList list = new ArrayList();

        if ( pairedDevices.size() > 0 ) {
            for ( BluetoothDevice bt : pairedDevices ) {
                list.add(bt.getName() + "\n" + bt.getAddress());
            }
        } else {
            Toast.makeText(getApplicationContext(), "No Paired Bluetooth Devices Found.", Toast.LENGTH_SHORT).show();
        }

        final ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, list);
        devices.setAdapter(adapter);
        devices.setOnItemClickListener(myListClickListener);
    }

    private AdapterView.OnItemClickListener myListClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String info = ((TextView) view).getText().toString();
            String address = info.substring(info.length()-17);

            Intent i = new Intent(PairedDevices.this, ConnectedDevice.class);
            i.putExtra("Device Address", address);
            i.putExtra("Brand", brand);
            i.putExtra("Model", model);
            i.putExtra("Plate", licensePlate);
            startActivity(i);
        }
    };

    boolean checkPermission(Context context,String[] PERMISSIONS)
    {
        if(context != null && PERMISSIONS != null){
            for(String permission: PERMISSIONS){
                if(ActivityCompat.checkSelfPermission(PairedDevices.this,permission) != PackageManager.PERMISSION_GRANTED){
                    return false;
                }
            }
        }
        return true;
    }
}


