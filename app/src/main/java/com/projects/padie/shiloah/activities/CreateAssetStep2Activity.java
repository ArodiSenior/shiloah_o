package com.projects.padie.shiloah.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.material.textfield.TextInputLayout;
import com.projects.padie.shiloah.R;
import com.projects.padie.shiloah.helpers.AppConfig;
import com.projects.padie.shiloah.helpers.AppController;
import com.projects.padie.shiloah.helpers.NetworkState;
import com.projects.padie.shiloah.helpers.SessionManager;
import com.projects.padie.shiloah.holders.Locations;
import com.projects.padie.shiloah.holders.Statuses;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CreateAssetStep2Activity extends AppCompatActivity implements View.OnClickListener {
    
    private String TAG = CreateAssetStep2Activity.class.getSimpleName();
    private TextInputLayout etPurchaseCost, etWarranty, etNotes;
    private Spinner spStatus, spLocation;
    private CheckBox cbUserMayRequest;
    private Button btnSubmitProduct;
    
    public boolean valid = false;
    
    private String assetTag, serial, assetName, purchaseDate, orderNo, model, supplier;
    
    ArrayList<Statuses> statusesArrayList;
    ArrayList<Locations> locationsArrayList;
    
    private String selected_status_id, selected_location_id;
    
    String mayRequest = "0";
    
    // Session Manager Class
    SessionManager session;
    
    HashMap<String, String> user;
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_asset_step2);
        
        // Session Manager
        session = new SessionManager(getApplicationContext());
        user = session.getUserDetails();
        
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(user.get(SessionManager.KEY_COMPANY_NAME) + " New Asset Step 2");
        
        
        etPurchaseCost = findViewById(R.id.et_purchase_cost);
        etWarranty = findViewById(R.id.et_warranty);
        etNotes = findViewById(R.id.et_notes);
        spStatus = findViewById(R.id.sp_status);
        spLocation = findViewById(R.id.sp_location);
        cbUserMayRequest = findViewById(R.id.cb_user_may_request);
        cbUserMayRequest.setOnClickListener(this);
        
        btnSubmitProduct = findViewById(R.id.btn_submit_product);
        btnSubmitProduct.setOnClickListener(this);
        
        assetTag = getIntent().getStringExtra("assetTag");
        serial = getIntent().getStringExtra("serial");
        assetName = getIntent().getStringExtra("assetName");
        purchaseDate = getIntent().getStringExtra("purchaseDate");
        orderNo = getIntent().getStringExtra("orderNo");
        model = getIntent().getStringExtra("model");
        supplier = getIntent().getStringExtra("supplier");
        
        spStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                
                selected_status_id = statusesArrayList.get(i).getId();
                
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            
            }
        });
        
        spLocation.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                
                selected_location_id = locationsArrayList.get(i).getId();
                
                
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            
            }
        });
        
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        
        if (NetworkState.getInstance(this).isConnected()) {
            
            fillStatusSpinner();
            fillLocationSpinner();
        } else {
            Toast.makeText(getApplicationContext(), "Ensure Internet Connection", Toast.LENGTH_LONG).show();
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
                
                Intent i = new Intent(this, CreateAssetActivity.class);
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
    
    private void fillStatusSpinner() {
            boolean error;
           
                final List<String> list = new ArrayList<String>();
                statusesArrayList = new ArrayList<>();
                statusesArrayList.clear();
                
                StringRequest req = new StringRequest(Request.Method.POST, AppConfig.TECH_SERVER_URL + "fill_statuses.php",
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(final String response) {
                                System.out.println("resppp"+response);
                                try {
                                    // Json parsing from response
                                    JSONArray jsonArray = new JSONArray(response);
                                    
                                    if (jsonArray.length()!=0) {
                                        
                                        for (int i = 0; i < jsonArray.length(); i++) {
                                            JSONObject object = jsonArray.getJSONObject(i);
                                            Statuses statuses = new Statuses(
                                                    object.getString("id"),
                                                    object.getString("name"));
                                            statusesArrayList.add(statuses);
                                        }
                                        
                                        
                                        for (int i = 0; i < statusesArrayList.size(); i++) {
                                            list.add(statusesArrayList.get(i).getName());
                                        }
                                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, list);
                                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                        spStatus.setAdapter(adapter);
                                    } else {
                                        Toast.makeText(getApplicationContext(), "No status available", Toast.LENGTH_LONG).show();
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
    
    private void fillLocationSpinner() {
        final List<String> list = new ArrayList<String>();
        locationsArrayList = new ArrayList<>();
        locationsArrayList.clear();
        
        StringRequest req = new StringRequest(Request.Method.POST, AppConfig.TECH_SERVER_URL + "fill_locations.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(final String response) {
                        System.out.println("respppp"+response);
                        try {
                            // Json parsing from response
                            JSONArray jsonArray = new JSONArray(response);
                            
                            if (jsonArray.length()!=0) {
                             
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject object = jsonArray.getJSONObject(i);
                                    Locations locations = new Locations(
                                            object.getString("id"),
                                            object.getString("name"));
                                    locationsArrayList.add(locations);
                                }
                                
                                
                                for (int i = 0; i < locationsArrayList.size(); i++) {
                                    list.add(locationsArrayList.get(i).getName());
                                }
                                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, list);
                                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                spLocation.setAdapter(adapter);
                            } else {
                                Toast.makeText(getApplicationContext(), "No location available", Toast.LENGTH_LONG).show();
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
    
    private void submitProduct() {
    
        final ProgressDialog progressDialog = new ProgressDialog(CreateAssetStep2Activity.this);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Submitting Product...");
        progressDialog.show();
        
        
        StringRequest req = new StringRequest(Request.Method.POST, AppConfig.TECH_SERVER_URL + "add_product.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(final String response) {
                        progressDialog.dismiss();
                        System.out.println("respopooo"+response);
                        try {
                            
                            // Json parsing from response
                            JSONObject object = new JSONObject(response);
                            
                            if (object.getString("success").equals("1")) {
                                Toast.makeText(CreateAssetStep2Activity.this, object.getString("message"), Toast.LENGTH_LONG).show();
                                Intent i = new Intent(CreateAssetStep2Activity.this, DrawerActivity.class);
                                startActivity(i);
                                finish();
                                
                                
                            } else {
                                Toast.makeText(getBaseContext(), object.getString("message"), Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                Toast.makeText(CreateAssetStep2Activity.this, "No connection to host", Toast.LENGTH_SHORT).show();
            }
        }) {
            
            @Override
            public Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<String, String>();
                params.put("schema", user.get(SessionManager.KEY_SCHEMA));
                params.put("name", assetName);
                params.put("asset_tag", assetTag);
                params.put("model_id", model);
                params.put("serial", serial);
                params.put("purchase_date", purchaseDate);
                params.put("purchase_cost", etPurchaseCost.getEditText().getText().toString());
                params.put("order_number", orderNo);
                params.put("assigned_to", "");
                params.put("notes", etNotes.getEditText().getText().toString());
                params.put("user_id", "");
                params.put("created_at", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Calendar.getInstance().getTime()));
                params.put("updated_at", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Calendar.getInstance().getTime()));
                params.put("physical", "");
                params.put("deleted_at", "");
                params.put("status_id", selected_status_id);
                params.put("archived", "");
                params.put("warranty_months", etWarranty.getEditText().getText().toString());
                params.put("depreciate", "");
                params.put("supplier_id", supplier);
                params.put("requestable", mayRequest);
                params.put("rtd_location_id", selected_location_id);
                params.put("accepted", "");
                return params;
            }
            
        };
        
        // add the request object to the queue to be executed
        AppController.getInstance().addToRequestQueue(req);
    }
    
    @Override
    public void onClick(View view) {
        
        if (view.equals(btnSubmitProduct)) {
            if (validate()) {
                submitProduct();
            }
        }
        
        if (view.equals(cbUserMayRequest)) {
            if (cbUserMayRequest.isChecked()) {
                mayRequest = "1";
            } else {
                mayRequest = "0";
            }
        }
    }
    
    public boolean validate() {
        valid = true;
        
        if (etPurchaseCost.getEditText().getText().toString().equals("")) {
            etPurchaseCost.setError("Please enter purchase cost");
            valid = false;
        } else {
            etPurchaseCost.setError("");
        }
        
        if (etWarranty.getEditText().getText().toString().equals("")) {
            etWarranty.setError("Please enter warranty");
            valid = false;
        } else {
            etWarranty.setError("");
        }
        
        if (spStatus.getSelectedItem() == null) {
            Toast.makeText(CreateAssetStep2Activity.this, "Status is required!", Toast.LENGTH_SHORT).show();
            valid = false;
        }
        
        if (spLocation.getSelectedItem() == null) {
            Toast.makeText(CreateAssetStep2Activity.this, "Location is required!", Toast.LENGTH_SHORT).show();
            valid = false;
        }
        
        if (etNotes.getEditText().getText().toString().equals("")) {
            etNotes.setError("Please enter notes");
            valid = false;
        } else {
            etNotes.setError("");
        }
        return valid;
    }
}
