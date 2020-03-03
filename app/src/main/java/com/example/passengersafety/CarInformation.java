package com.example.passengersafety;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class CarInformation extends AppCompatActivity {

    public EditText editCarBrand, editModel, editLicensePlate;
    public String carBrand, carModel, carLicensePlate;
    public Button confirmInformation;
    public Intent i;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_information);

        editCarBrand = findViewById(R.id.brandEditText);
        editModel = findViewById(R.id.modelEditText);
        editLicensePlate = findViewById(R.id.licensePlateText);
        confirmInformation = findViewById(R.id.confirmButton);

        confirmInformation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                i = new Intent(CarInformation.this,PairedDevices.class);
                carBrand = editCarBrand.getText().toString();
                carModel = editModel.getText().toString();
                carLicensePlate = editLicensePlate.getText().toString();
                i.putExtra("Brand",carBrand);
                i.putExtra("Model",carModel);
                i.putExtra("Plate",carLicensePlate);
                startActivity(i);
            }
        });
    }
}
