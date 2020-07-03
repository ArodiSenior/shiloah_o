package com.projects.padie.shiloah.activities;

import android.content.Intent;
import androidx.core.view.GravityCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.zxing.Result;
import com.projects.padie.shiloah.fragments.HomeFragment;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class ScannerActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {
    private ZXingScannerView mScannerView;

    String CALLING_ACT = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CALLING_ACT = getIntent().getStringExtra("CALLING_ACT");

        if (CALLING_ACT.equals("")) {
            Intent intent = new Intent(ScannerActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }

//        setContentView(R.layout.activity_scanner);

        // Programmatically initialize the scanner view
        mScannerView = new ZXingScannerView(this);
        setContentView(mScannerView);
    }


    @Override
    public void handleResult(Result rawResult) {
        // Do something with the result here
        Log.v("TAG", rawResult.getText()); // Prints scan results
        // Prints the scan format (qrcode, pdf417 etc.)
        Log.v("TAG", rawResult.getBarcodeFormat().toString());

        mScannerView.stopCamera();

        if (CALLING_ACT.equals(CreateAssetActivity.class.getSimpleName())){

            Intent intent = new Intent(this, CreateAssetActivity.class);
            intent.putExtra("SERIAL_NO", rawResult.getText());
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();

        } else if (CALLING_ACT.equals(HomeFragment.class.getSimpleName())) {

            Intent intent = new Intent(this, DrawerActivity.class);
            intent.putExtra("fragment", "home");
            intent.putExtra("serial", rawResult.getText());
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();

        } else {

            Intent intent = new Intent(ScannerActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();

        }


        // If you would like to resume scanning, call this method below:
//        mScannerView.resumeCameraPreview(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Register ourselves as a handler for scan results.
        mScannerView.setResultHandler(this);
        // Start camera on resume
        mScannerView.startCamera();
    }

    @Override
    public void onPause() {
        super.onPause();
        // Stop camera on pause
        if (mScannerView!=null) {

            mScannerView.stopCamera();
        }

    }

    @Override
    public void onBackPressed() {

        if (CALLING_ACT.equals(CreateAssetActivity.class.getSimpleName())){

            Intent intent = new Intent(this, CreateAssetActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();

        } else if (CALLING_ACT.equals(HomeFragment.class.getSimpleName())) {

            Intent intent = new Intent(this, DrawerActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();

        } else {

            Intent intent = new Intent(ScannerActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();

        }


        super.onBackPressed();
    }
}