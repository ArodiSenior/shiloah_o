package com.projects.padie.shiloah.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.google.android.material.textfield.TextInputLayout;
import com.projects.padie.shiloah.R;
import com.projects.padie.shiloah.helpers.AppConfig;
import com.projects.padie.shiloah.helpers.AppController;
import com.projects.padie.shiloah.helpers.SessionManager;
import com.projects.padie.shiloah.holders.CompanySession;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class LoginActivity extends AppCompatActivity {
    
    private String TAG = LoginActivity.class.getSimpleName();
    
    
    private static final int PERMISSION_CALLBACK_CONSTANT = 100;
    private static final int REQUEST_PERMISSION_SETTING = 101;
    String[] permissionsRequired = new String[]{
            Manifest.permission.INTERNET,
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.READ_PHONE_STATE
        
    };
    private SharedPreferences permissionStatus;
    private boolean sentToSettings = false;
    
    TextInputLayout _emailText;
    TextInputLayout _passwordText;
    Button _loginButton;
    
    private Spinner spCompany;
    
    ArrayList<CompanySession> companySessionArrayList;
    
    // Session Manager Class
    SessionManager session;
    
    HashMap<String, String> user;
    
    private String selectedCompany;
    
    String schema = "shiloah";
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        
        permissionStatus = getSharedPreferences("permissionStatus", MODE_PRIVATE);
        
        _emailText = findViewById(R.id.input_email);
        _passwordText = findViewById(R.id.input_password);
        _loginButton = findViewById(R.id.btn_login);
        
        spCompany = findViewById(R.id.sp_company);
        fillCompanySpinner();
        
        // Session Manager
        session = new SessionManager(getApplicationContext());
        session.clearSession();
//        user = session.getUserDetails();

//        if (session.isLoggedIn()) {
//            _emailText.setText(user.get(SessionManager.KEY_EMAIL));
//
//        }
        
        spCompany.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                
                selectedCompany = companySessionArrayList.get(i).getName();
                
                
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            
            }
        });
        
        
        if (
                ActivityCompat.checkSelfPermission(LoginActivity.this, permissionsRequired[0]) != PackageManager.PERMISSION_GRANTED
                        || ActivityCompat.checkSelfPermission(LoginActivity.this, permissionsRequired[1]) != PackageManager.PERMISSION_GRANTED
                        || ActivityCompat.checkSelfPermission(LoginActivity.this, permissionsRequired[2]) != PackageManager.PERMISSION_GRANTED
                        || ActivityCompat.checkSelfPermission(LoginActivity.this, permissionsRequired[3]) != PackageManager.PERMISSION_GRANTED
                        || ActivityCompat.checkSelfPermission(LoginActivity.this, permissionsRequired[4]) != PackageManager.PERMISSION_GRANTED
                        || ActivityCompat.checkSelfPermission(LoginActivity.this, permissionsRequired[5]) != PackageManager.PERMISSION_GRANTED
                        || ActivityCompat.checkSelfPermission(LoginActivity.this, permissionsRequired[6]) != PackageManager.PERMISSION_GRANTED
                        || ActivityCompat.checkSelfPermission(LoginActivity.this, permissionsRequired[7]) != PackageManager.PERMISSION_GRANTED) {
            if (
                    ActivityCompat.shouldShowRequestPermissionRationale(LoginActivity.this, permissionsRequired[0])
                            || ActivityCompat.shouldShowRequestPermissionRationale(LoginActivity.this, permissionsRequired[1])
                            || ActivityCompat.shouldShowRequestPermissionRationale(LoginActivity.this, permissionsRequired[2])
                            || ActivityCompat.shouldShowRequestPermissionRationale(LoginActivity.this, permissionsRequired[3])
                            || ActivityCompat.shouldShowRequestPermissionRationale(LoginActivity.this, permissionsRequired[4])
                            || ActivityCompat.shouldShowRequestPermissionRationale(LoginActivity.this, permissionsRequired[5])
                            || ActivityCompat.shouldShowRequestPermissionRationale(LoginActivity.this, permissionsRequired[6])
                            || ActivityCompat.shouldShowRequestPermissionRationale(LoginActivity.this, permissionsRequired[7])
            ) {
                //Show Information about why you need the permission
                AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                builder.setTitle("Need Multiple Permissions");
                builder.setMessage("Shiloah requires these permissions for normal functionality.");
                builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        ActivityCompat.requestPermissions(LoginActivity.this, permissionsRequired, PERMISSION_CALLBACK_CONSTANT);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            } else if (permissionStatus.getBoolean(permissionsRequired[0], false)) {
                //Previously Permission Request was cancelled with 'Dont Ask Again',
                // Redirect to Settings after showing Information about why you need the permission
                AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                builder.setTitle("Permissions Required");
                builder.setMessage("Shiloah requires these permissions for normal functionality.");
                builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        sentToSettings = true;
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivityForResult(intent, REQUEST_PERMISSION_SETTING);
                        Toast.makeText(getBaseContext(), "Click on Permissions and Grant Access", Toast.LENGTH_LONG).show();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            } else {
                //just request the permission
                ActivityCompat.requestPermissions(LoginActivity.this, permissionsRequired, PERMISSION_CALLBACK_CONSTANT);
            }
            
            SharedPreferences.Editor editor = permissionStatus.edit();
            editor.putBoolean(permissionsRequired[0], true);
            editor.apply();
        } else {
            
            
            //You already have the permission, just go ahead.
//            proceedAfterPermission();
        
        }
        
        
        _loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Login");
                
                if (validate()) {
                    loginFxn();
                }
                
                
            }
        });
        
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CALLBACK_CONSTANT) {
            //check if all permissions are granted
            boolean allgranted = false;
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    allgranted = true;
                } else {
                    allgranted = false;
                    break;
                }
            }
            
            if (allgranted) {
                proceedAfterPermission();
            } else if (
                    ActivityCompat.shouldShowRequestPermissionRationale(LoginActivity.this, permissionsRequired[0])
                            || ActivityCompat.shouldShowRequestPermissionRationale(LoginActivity.this, permissionsRequired[1])
                            || ActivityCompat.shouldShowRequestPermissionRationale(LoginActivity.this, permissionsRequired[2])
                            || ActivityCompat.shouldShowRequestPermissionRationale(LoginActivity.this, permissionsRequired[3])
                            || ActivityCompat.shouldShowRequestPermissionRationale(LoginActivity.this, permissionsRequired[4])
                            || ActivityCompat.shouldShowRequestPermissionRationale(LoginActivity.this, permissionsRequired[5])
                            || ActivityCompat.shouldShowRequestPermissionRationale(LoginActivity.this, permissionsRequired[6])
                            || ActivityCompat.shouldShowRequestPermissionRationale(LoginActivity.this, permissionsRequired[7])
            
            ) {
                AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                builder.setTitle("Need Multiple Permissions");
                builder.setMessage("Track OOH requires these permissions for normal functionality.");
                builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        ActivityCompat.requestPermissions(LoginActivity.this, permissionsRequired, PERMISSION_CALLBACK_CONSTANT);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            } else {
                Toast.makeText(getBaseContext(), "Unable to get Permission", Toast.LENGTH_LONG).show();
            }
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PERMISSION_SETTING) {
            if (ActivityCompat.checkSelfPermission(LoginActivity.this, permissionsRequired[0]) == PackageManager.PERMISSION_GRANTED) {
                //Got Permission
//                proceedAfterPermission();
            }
        }
    }
    
    private void proceedAfterPermission() {
        
        Toast.makeText(getBaseContext(), "Thank You For Accepting Required Permissions", Toast.LENGTH_LONG).show();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            
            return;
        }
        
    }
    
    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (sentToSettings) {
            if (ActivityCompat.checkSelfPermission(LoginActivity.this, permissionsRequired[0]) == PackageManager.PERMISSION_GRANTED) {
                //Got Permission

//                proceedAfterPermission();
            }
        }
    }
    
    public boolean validate() {
        boolean valid = true;
        if (_emailText.getEditText().getText().toString().trim().isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(_emailText.getEditText().getText().toString().trim()).matches()) {
            _emailText.setError("enter a valid email address");
            valid = false;
        } else {
            _emailText.setError(null);
        }
        if (_passwordText.getEditText().getText().toString().trim().isEmpty() || _passwordText.getEditText().getText().toString().trim().length() < 6) {
            _passwordText.setError("enter at least 6 alphanumeric characters");
            valid = false;
        } else {
            _passwordText.setError(null);
        }
        
        if (spCompany.getSelectedItem() == null) {
            valid = false;
            Toast.makeText(LoginActivity.this, "Company is required!", Toast.LENGTH_SHORT).show();
        }
        return valid;
    }
    
    private void loginFxn() {
        final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this);
        progressDialog.setMessage("Authenticating...");
        progressDialog.show();
        
        StringRequest req = new StringRequest(Request.Method.POST, AppConfig.TECH_SERVER_URL + "login.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(final String response) {
                        progressDialog.dismiss();
                        Log.d(TAG, " On Response: " + response.toString());
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String success = jsonObject.getString("success");
                            
                            if (success.equals("1")) {
                                JSONObject user = new JSONObject(jsonObject.getString("user"));
                                
                                String uid = user.getString("id");
                                String name = user.getString("first_name") + " " + user.getString("last_name");
                                String phone = user.getString("phone");
                                String email = user.getString("email");
                                
                                session.createLoginSession(name, email, uid, selectedCompany, schema);
                                Intent intent = new Intent(LoginActivity.this, DrawerActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(LoginActivity.this, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                                
                            }
                            
                        } catch (JSONException e) {
                            Toast.makeText(LoginActivity.this, "Login request failed", Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.hide();
                Toast.makeText(getApplicationContext(), "Ensure You are connected to the Internet", Toast.LENGTH_LONG).show();
                
            }
        }) {
            
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> hashMap = new HashMap<>();
                hashMap.put("email", _emailText.getEditText().getText().toString().trim());
                hashMap.put("password", _passwordText.getEditText().getText().toString().trim());
                hashMap.put("schema", schema);
                return hashMap;
                
            }
        };
        // add the request object to the queue to be executed
        AppController.getInstance().addToRequestQueue(req);
        
    }
    
    
    private void fillCompanySpinner() {
        final List<String> list = new ArrayList<String>();
        companySessionArrayList = new ArrayList<>();
        companySessionArrayList.clear();
        
        HashMap<String, String> params = new HashMap<String, String>();
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST,
                AppConfig.TECH_SERVER_URL + "fill_companies.php", new JSONObject(params),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(final JSONObject response) {
                        System.out.println("Response:" + response.toString());
                        try {
                            // Json parsing from response
                            JSONObject object = new JSONObject(response.toString());
                            // check error
                            final boolean error = object.getBoolean("error");
                            if (!error) {
                                // iterate over array users as declared in php script
                                JSONArray jArray = object.getJSONArray("result");
                                Log.d(TAG, "Array: " + jArray.toString());
                                
                                for (int i = 0; i < jArray.length(); i++) {
                                    JSONObject singleRow = jArray.getJSONObject(i);
                                    CompanySession companySession = new CompanySession(singleRow.getString("name"));
                                    companySessionArrayList.add(companySession);
                                }
                                
                                
                                for (int i = 0; i < companySessionArrayList.size(); i++) {
                                    list.add(companySessionArrayList.get(i).getName());
                                }
                                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.spinner_item, list);
                                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                spCompany.setAdapter(adapter);
                            }
                            
                            VolleyLog.v("Response:%n %s", response.toString(4));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkResponse errorRes = error.networkResponse;
                Toast.makeText(getApplicationContext(), "Please Check Your Internet Connection", Toast.LENGTH_LONG).show();
                
                String stringData = "";
                if (errorRes != null && errorRes.data != null) {
                    try {
                        stringData = new String(errorRes.data, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
                Log.e("Error", stringData);
                
                Log.e(TAG, "On ErrorResponse: " + error.getMessage());
                VolleyLog.e("Error: ", error.getMessage());
            }
        }) {
            
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Accept", "application/json");
                headers.put("Content-Type", "application/json; charset=utf-8");
                
                return headers;
            }
            
        };
        
        // add the request object to the queue to be executed
        AppController.getInstance().addToRequestQueue(req);
    }
    
}

