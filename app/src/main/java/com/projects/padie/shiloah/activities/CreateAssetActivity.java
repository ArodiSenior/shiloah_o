package com.projects.padie.shiloah.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.projects.padie.shiloah.R;
import com.projects.padie.shiloah.helpers.AppConfig;
import com.projects.padie.shiloah.helpers.AppController;
import com.projects.padie.shiloah.helpers.NetworkState;
import com.projects.padie.shiloah.helpers.SessionManager;
import com.projects.padie.shiloah.holders.Models;
import com.projects.padie.shiloah.holders.Suppliers;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class CreateAssetActivity extends AppCompatActivity implements View.OnClickListener{

    private String TAG = CreateAssetActivity.class.getSimpleName();

    private ZXingScannerView scannerView;

    private TextInputLayout etAssetTag, etSerial, etAssetName, etPurchaseDate, etOrderNo;
    private Spinner spModel, spSupplier;
    private MaterialButton btnNext;
    
    public TextInputEditText textInputEditText;

    ArrayList<Models> modelsArrayList;
    ArrayList<Suppliers> suppliersArrayList;

    private String selected_model_id, selected_supplier_id;
    
    public boolean valid = false;

    // Session Manager Class
    SessionManager session;

    HashMap<String, String> user;

    String CALLING_ACT = "", SERIAL_NO="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_asset);

        // Session Manager
        session = new SessionManager(getApplicationContext());
        user = session.getUserDetails();

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(user.get(SessionManager.KEY_COMPANY_NAME) + " New Asset Step 1");
    
        etAssetTag = findViewById(R.id.et_asset_tag);
        etSerial = findViewById(R.id.et_serial);
        etAssetName = findViewById(R.id.et_asset_name);
        etPurchaseDate = findViewById(R.id.et_purchase_date);
        etOrderNo = findViewById(R.id.et_order_no);
        spModel = findViewById(R.id.sp_model);
        spSupplier = findViewById(R.id.sp_supplier);
        btnNext = findViewById(R.id.btn_next);
        btnNext.setOnClickListener(this);
        textInputEditText = findViewById(R.id.editDisplayDate);
        textInputEditText.setOnClickListener(this);

        SERIAL_NO = getIntent().getStringExtra("SERIAL_NO");

        if (getIntent().getStringExtra("SERIAL_NO") != null) {
           etAssetTag.getEditText().setText(SERIAL_NO);
           etAssetTag.setEnabled(false);
            etSerial.getEditText().setText(SERIAL_NO);
            etSerial.setEnabled(false);
        }
        

        spModel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                selected_model_id = modelsArrayList.get(i).getId();

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        spSupplier.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                selected_supplier_id = suppliersArrayList.get(i).getId();


            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


    }
    
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.editDisplayDate:
                DatePicker();
                break;
            case R.id.btn_next:
                if (validate()){
                    Intent intent = new Intent(CreateAssetActivity.this, CreateAssetStep2Activity.class);
                    intent.putExtra("assetTag", etAssetTag.getEditText().getText().toString());
                    intent.putExtra("serial", etSerial.getEditText().getText().toString());
                    intent.putExtra("assetName", etAssetName.getEditText().getText().toString());
                    intent.putExtra("purchaseDate", etPurchaseDate.getEditText().getText().toString());
                    intent.putExtra("orderNo", etOrderNo.getEditText().getText().toString());
                    intent.putExtra("model", selected_model_id);
                    intent.putExtra("supplier", selected_supplier_id);
                    startActivity(intent);
                }
                break;
                
                
        }
    }
    
    private void DatePicker() {
        final Calendar currentDate = Calendar.getInstance();
        
        DatePickerDialog.OnDateSetListener dateSetListener = new
                DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {
                        Calendar calendar = Calendar.getInstance();
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, monthOfYear);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        
                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                        
                        String currentDateString = format.format(calendar.getTime());
                        etPurchaseDate.getEditText().setText(currentDateString);
                        
                    }
                };
        DatePickerDialog datePickerDialog = new
                DatePickerDialog(CreateAssetActivity.this, dateSetListener,
                currentDate.get(Calendar.YEAR),
                currentDate.get(Calendar.MONTH),
                currentDate.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
        
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
    }
    

    public void scanCode( View v) {

        Intent intent = new Intent(this, ScannerActivity.class);
        intent.putExtra("CALLING_ACT", CreateAssetActivity.class.getSimpleName());
        startActivity(intent);
        finish();


    }

    @Override
    protected void onPause() {
        super.onPause();

        if (scannerView!=null) {

            scannerView.stopCamera();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case android.R.id.home:

                Intent i= new Intent(this, DrawerActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
                finish();

                break;

//            case R.id.action_logout:
//                session.logoutUser();
//                break;


            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }

        return true;
    }
    

    @Override
    protected void onResume() {
        super.onResume();

        if (NetworkState.getInstance(this).isConnected()) {

            fillModelSpinner();
            fillSupplierSpinner();

        } else {
            Toast.makeText(getApplicationContext(), "Ensure Internet Connection", Toast.LENGTH_LONG).show();
        }


    }

    private void fillModelSpinner() {
                final List<String> list = new ArrayList<String>();
                modelsArrayList = new ArrayList<>();
                modelsArrayList.clear();

                StringRequest req = new StringRequest(Request.Method.POST, AppConfig.TECH_SERVER_URL + "fill_models.php",
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(final String response) {
                                try {
                                    // Json parsing from response
                                    JSONArray jsonArray = new JSONArray(response);
                                    System.out.println("respppp" + response);
                                    if (jsonArray.length() != 0) {
                                        
                                        for (int i = 0; i < jsonArray.length(); i++) {
                                            JSONObject object = jsonArray.getJSONObject(i);
                                            Models models = new Models(
                                                    object.getString("id"),
                                                    object.getString("name"),
                                                    object.getString("modelno"),
                                                    object.getString("manufacturer_id"),
                                                    object.getString("category_id"));
                                            modelsArrayList.add(models);
                                        }
    
    
                                        for (int i = 0; i < modelsArrayList.size(); i++) {
                                            list.add(modelsArrayList.get(i).getName());
                                        }
                                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, list);
                                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                        spModel.setAdapter(adapter);
                                    } else {
                                        Toast.makeText(getApplicationContext(), "No model found!", Toast.LENGTH_LONG).show();
                                    }
    
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        NetworkResponse errorRes = error.networkResponse;
                        Toast.makeText(getApplicationContext(), "Please Check Your Internet Connection", Toast.LENGTH_LONG).show();
                    }
                }) {
    
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        HashMap<String, String> params = new HashMap<String, String>();
                        params.put("schema", user.get(SessionManager.KEY_SCHEMA));
                        return params;
                    }
                };

                // add the request object to the queue to be executed
                AppController.getInstance().addToRequestQueue(req);
            
    }

    private void fillSupplierSpinner() {
                final List<String> list = new ArrayList<String>();
                suppliersArrayList = new ArrayList<>();
                suppliersArrayList.clear();
                
                StringRequest req = new StringRequest(Request.Method.POST, AppConfig.TECH_SERVER_URL + "fill_suppliers.php",
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(final String response) {
                                System.out.println("resppp"+response);
                                try {
                                    // Json parsing from response
                                    JSONArray jsonArray = new JSONArray(response);
                                    // check error
                                   if (jsonArray.length()!=0){
                                        for (int i = 0; i < jsonArray.length(); i++) {
                                            JSONObject object = jsonArray.getJSONObject(i);
                                            Suppliers suppliers = new Suppliers  (
                                                    object.getString("id"),
                                                    object.getString("name"));
                                            suppliersArrayList.add(suppliers);
                                        }


                                        for (int i = 0; i < suppliersArrayList.size(); i++) {
                                            list.add(suppliersArrayList.get(i).getName());
                                            // list.add(locationList.get(i).getId());
                                        }
                                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, list);
                                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                        spSupplier.setAdapter(adapter);
                                    } else {
                                        Toast.makeText(getApplicationContext(), "No supplier found", Toast.LENGTH_LONG).show();
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), "Please Check Your Internet Connection", Toast.LENGTH_LONG).show();


                        
                    }
                }) {

                    @Override
                    public Map<String, String> getParams() throws AuthFailureError {
                        HashMap<String, String> params = new HashMap<String, String>();
                        params.put("schema", user.get(SessionManager.KEY_SCHEMA));
                        return params;
                    }

                };

                // add the request object to the queue to be executed
                AppController.getInstance().addToRequestQueue(req);
            
    }
    
    public boolean validate(){
        valid = true;
        
        if (etAssetTag.getEditText().getText().toString().equals("")){
            etAssetTag.setError("Please enter asset tag");
            valid = false;
        }else {
            etAssetTag.setError("");
        }
    
        if (etSerial.getEditText().getText().toString().equals("")){
            etSerial.setError("Please enter serial");
            valid = false;
        }else {
            etSerial.setError("");
        }
    
        if (etAssetName.getEditText().getText().toString().equals("")){
            etAssetName.setError("Please enter asset name");
            valid = false;
        }else {
            etAssetName.setError("");
        }
        
        if (spModel.getSelectedItem() ==null){
            Toast.makeText(this, "Model is required!", Toast.LENGTH_SHORT).show();
            valid = false;
        }
        
        
        if (etPurchaseDate.getEditText().getText().toString().equals("")){
            etPurchaseDate.setError("Please enter purchase date");
            valid = false;
        }else {
            etPurchaseDate.setError("");
        }
    
        if (spSupplier.getSelectedItem() ==null){
            Toast.makeText(this, "Supplier is required!", Toast.LENGTH_SHORT).show();
            valid = false;
        }
        if (etOrderNo.getEditText().getText().toString().equals("")){
            etOrderNo.setError("Please enter order number");
            valid = false;
        }else {
            etOrderNo.setError("");
        }
        
        return valid;
        
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
