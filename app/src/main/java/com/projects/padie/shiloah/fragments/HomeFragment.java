package com.projects.padie.shiloah.fragments;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.cardview.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.zxing.Result;
import com.projects.padie.shiloah.R;
import com.projects.padie.shiloah.activities.DrawerActivity;
import com.projects.padie.shiloah.activities.ScannerActivity;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment implements View.OnClickListener{

    private View rootView;
    private ZXingScannerView scannerView;
    CardView cvVerify;

    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_home, container, false);

        initGUI();

        return rootView;
    }

    private void initGUI () {

        cvVerify = rootView.findViewById(R.id.cv_verify);
        cvVerify.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {

        Intent intent = new Intent(getActivity(), ScannerActivity.class);
        intent.putExtra("CALLING_ACT", HomeFragment.class.getSimpleName());
        startActivity(intent);
        getActivity().finish();

//        scannerView = new ZXingScannerView(getActivity());
//        scannerView.setResultHandler(new ZXingScannerResultHandler(getActivity()));
//        getActivity().setContentView(scannerView);
//        scannerView.startCamera();
//
//        Toast.makeText(getActivity(), "Ensure Considerable Distance For Appropriate Scanning", Toast.LENGTH_LONG).show();


    }

    @Override
    public void onPause() {
        super.onPause();

        if (scannerView!=null) {
            scannerView.stopCamera();
        }
    }

    class  ZXingScannerResultHandler implements ZXingScannerView.ResultHandler {


        private Activity activity;

        public ZXingScannerResultHandler(Activity activity) {

            this.activity=activity;
        }

        @Override
        public void handleResult(Result result) {

            final String scannerResult = result.getText();


            Intent intent = new Intent(activity, DrawerActivity.class);
            intent.putExtra("fragment", "home");
            intent.putExtra("serial", scannerResult);
            startActivity(intent);
            activity.finish();

//            activity.setContentView(R.layout.fragment_home);
//            initGUI();


            scannerView.stopCamera();
        }
    }


}
