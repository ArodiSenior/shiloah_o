package com.projects.padie.shiloah.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.projects.padie.shiloah.R;
import com.projects.padie.shiloah.fragments.HomeFragment;
import com.projects.padie.shiloah.fragments.LocationFragment;
import com.projects.padie.shiloah.fragments.ModelFragment;
import com.projects.padie.shiloah.fragments.StatusFragment;
import com.projects.padie.shiloah.fragments.SupplierFragment;
import com.projects.padie.shiloah.helpers.AppConfig;
import com.projects.padie.shiloah.helpers.AppController;
import com.projects.padie.shiloah.helpers.CircleTransform;
import com.projects.padie.shiloah.helpers.SessionManager;

import org.json.JSONObject;

import java.util.HashMap;

public class DrawerActivity extends AppCompatActivity {
    
    private String TAG = DrawerActivity.class.getSimpleName();
    
    private NavigationView navigationView;
    private DrawerLayout drawer;
    private View navHeader;
    private ImageView imgNavHeaderBg, imgProfile;
    private TextView txtName, txtWebsite;
    private Toolbar toolbar;
    private FloatingActionButton fab;
    
    // urls to load navigation header background image
    // and profile image
    private static final String urlNavHeaderBg = "https://api.androidhive.info/images/nav-menu-header-bg.jpg";
    private static final String urlProfileImg = "https://lh3.googleusercontent.com/eCtE_G34M9ygdkmOpYvCag1vBARCmZwnVS6rS5t4JLzJ6QgQSBquM0nuTsCpLhYbKljoyS-txg";
    
    // index to identify current nav menu item
    public static int navItemIndex = 0;
    
    // tags used to attach the fragments
    private static final String TAG_HOME = "home";
    
    private static final String TAG_NEW_MODEL = "new_model";
    private static final String TAG_NEW_SUPPLIER = "new_supplier";
    private static final String TAG_NEW_STATUS = "new_status";
    private static final String TAG_NEW_LOCATION = "new_location";
    
    
    public static String CURRENT_TAG = TAG_HOME;
    
    // toolbar titles respected to selected nav menu item
    private String[] activityTitles;
    
    // flag to load home fragment when user presses back key
    private boolean shouldLoadHomeFragOnBackPress = true;
    private Handler mHandler;
    
    // Session Manager Class
    SessionManager session;
    
    HashMap<String, String> user;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);
        
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        
        // Session Manager
        session = new SessionManager(getApplicationContext());
        user = session.getUserDetails();
        
        mHandler = new Handler();
        
        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        fab = findViewById(R.id.fab);
        
        // Navigation view header
        navHeader = navigationView.getHeaderView(0);
        txtName = navHeader.findViewById(R.id.name);
        txtWebsite = navHeader.findViewById(R.id.website);
        imgNavHeaderBg = navHeader.findViewById(R.id.img_header_bg);
        imgProfile = navHeader.findViewById(R.id.img_profile);
        
        // load toolbar titles from string resources
        activityTitles = getResources().getStringArray(R.array.nav_item_activity_titles);
        
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DrawerActivity.this, CreateAssetActivity.class);
                startActivity(intent);
            }
        });
        
        // load nav menu header data
        loadNavHeader();
        
        // initializing navigation menu
        setUpNavigationView();
        
        if (savedInstanceState == null) {
            navItemIndex = 0;
            CURRENT_TAG = TAG_HOME;
            loadHomeFragment();
        }
        
        
        if (getIntent().getStringExtra("fragment") != null && getIntent().getStringExtra("fragment").equals("home")) {
//            navItemIndex = 0;
//            CURRENT_TAG = TAG_HOME;
//            loadHomeFragment();
            
            verifyProduct(getIntent().getStringExtra("serial"));
        }
    }
    
    /***
     * Load navigation menu header information
     * like background image, profile image
     * name, website, notifications action view (dot)
     */
    private void loadNavHeader() {
        // name, website
        txtName.setText("Techsavanna");
        txtWebsite.setText("www.techsavanna.technology");
        
        // loading header background image
        Glide.with(this).load(R.drawable.background1)
                .crossFade()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imgNavHeaderBg);
        
        // Loading profile image
        Glide.with(this).load(R.drawable.techsavanna)
                .crossFade()
                .thumbnail(0.5f)
                .bitmapTransform(new CircleTransform(this))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imgProfile);
        
        // showing dot next to notifications label
//        navigationView.getMenu().getItem(3).setActionView(R.layout.menu_dot);
    }
    
    /***
     * Returns respected fragment that user
     * selected from navigation menu
     */
    private void loadHomeFragment() {
        // selecting appropriate nav menu item
        selectNavMenu();
        
        // set toolbar title
        setToolbarTitle();
        
        // if user select the current navigation menu again, don't do anything
        // just close the navigation drawer
        if (getSupportFragmentManager().findFragmentByTag(CURRENT_TAG) != null) {
            drawer.closeDrawers();
            
            // show or hide the fab button
            toggleFab();
            return;
        }
        
        // Sometimes, when fragment has huge data, screen seems hanging
        // when switching between navigation menus
        // So using runnable, the fragment is loaded with cross fade effect
        // This effect can be seen in GMail app
        Runnable mPendingRunnable = new Runnable() {
            @Override
            public void run() {
                // update the main content by replacing fragments
                Fragment fragment = getHomeFragment();
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.setCustomAnimations(android.R.anim.fade_in,
                        android.R.anim.fade_out);
                fragmentTransaction.replace(R.id.frame, fragment, CURRENT_TAG);
                fragmentTransaction.commitAllowingStateLoss();
            }
        };
        
        // If mPendingRunnable is not null, then add to the message queue
        if (mPendingRunnable != null) {
            mHandler.post(mPendingRunnable);
        }
        
        // show or hide the fab button
        toggleFab();
        
        //Closing drawer on item click
        drawer.closeDrawers();
        
        // refresh toolbar menu
        invalidateOptionsMenu();
    }
    
    private Fragment getHomeFragment() {
        switch (navItemIndex) {
            case 0:
                // home
                HomeFragment homeFragment = new HomeFragment();
                return homeFragment;
            
            
            case 1:
                
                ModelFragment modelFragment = new ModelFragment();
                return modelFragment;
            
            case 2:
                SupplierFragment supplierFragment = new SupplierFragment();
                return supplierFragment;
            
            case 3:
                
                StatusFragment statusFragment = new StatusFragment();
                return statusFragment;
            
            case 4:
                
                LocationFragment locationFragment = new LocationFragment();
                return locationFragment;
            
            
            default:
                return new HomeFragment();
        }
    }
    
    private void setToolbarTitle() {
        getSupportActionBar().setTitle(activityTitles[navItemIndex]);
    }
    
    private void selectNavMenu() {
        navigationView.getMenu().getItem(navItemIndex).setChecked(true);
    }
    
    private void setUpNavigationView() {
        //Setting Navigation View Item Selected Listener to handle the item click of the navigation menu
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            
            // This method will trigger on item Click of navigation menu
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                
                //Check to see which item was being clicked and perform appropriate action
                switch (menuItem.getItemId()) {
                    //Replacing the main content with ContentFragment Which is our Inbox View;
                    case R.id.nav_home:
                        navItemIndex = 0;
                        CURRENT_TAG = TAG_HOME;
                        break;
                    
                    case R.id.nav_new_model:
                        navItemIndex = 1;
                        CURRENT_TAG = TAG_NEW_MODEL;
                        break;
                    case R.id.nav_new_supplier:
                        navItemIndex = 2;
                        CURRENT_TAG = TAG_NEW_SUPPLIER;
                        break;
                    case R.id.nav_new_status:
                        navItemIndex = 3;
                        CURRENT_TAG = TAG_NEW_STATUS;
                        break;
                    case R.id.nav_new_location:
                        navItemIndex = 4;
                        CURRENT_TAG = TAG_NEW_LOCATION;
                        break;
                    
                    
                    default:
                        navItemIndex = 0;
                }
                
                //Checking if the item is in checked state or not, if not make it in checked state
                if (menuItem.isChecked()) {
                    menuItem.setChecked(false);
                } else {
                    menuItem.setChecked(true);
                }
                menuItem.setChecked(true);
                
                loadHomeFragment();
                
                return true;
            }
        });
        
        
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.openDrawer, R.string.closeDrawer) {
            
            @Override
            public void onDrawerClosed(View drawerView) {
                // Code here will be triggered once the drawer closes as we dont want anything to happen so we leave this blank
                super.onDrawerClosed(drawerView);
            }
            
            @Override
            public void onDrawerOpened(View drawerView) {
                // Code here will be triggered once the drawer open as we dont want anything to happen so we leave this blank
                super.onDrawerOpened(drawerView);
            }
        };
        
        //Setting the actionbarToggle to drawer layout
        drawer.setDrawerListener(actionBarDrawerToggle);
        
        //calling sync state is necessary or else your hamburger icon wont show up
        actionBarDrawerToggle.syncState();
    }
    
    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawers();
            return;
        }
        
        // This code loads home fragment when back key is pressed
        // when user is in other fragment than home
        if (shouldLoadHomeFragOnBackPress) {
            // checking if user is on other navigation menu
            // rather than home
            if (navItemIndex != 0) {
                navItemIndex = 0;
                CURRENT_TAG = TAG_HOME;
                loadHomeFragment();
                return;
            }
        }
        
        super.onBackPressed();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        
        // show menu only when home fragment is selected
        if (navItemIndex == 0) {
            getMenuInflater().inflate(R.menu.main, menu);
        }
        
        if (navItemIndex == 1) {
            getMenuInflater().inflate(R.menu.main, menu);
        }
        if (navItemIndex == 2) {
            getMenuInflater().inflate(R.menu.main, menu);
        }
        if (navItemIndex == 3) {
            getMenuInflater().inflate(R.menu.main, menu);
        }
        
        if (navItemIndex == 4) {
            getMenuInflater().inflate(R.menu.main, menu);
        }

//        // when fragment is notifications, load the menu created for notifications
//        if (navItemIndex == 3) {
//            getMenuInflater().inflate(R.menu.notifications, menu);
//        }
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_logout) {
            Toast.makeText(getApplicationContext(), "Logout user!", Toast.LENGTH_LONG).show();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
    
    // show or hide the fab
    private void toggleFab() {
        
        fab.show();
//        if (navItemIndex == 0)
//            fab.show();
//        else
//            fab.hide();
    }
    
    private void verifyProduct(final String serial) {
        final ProgressDialog progressDialog = new ProgressDialog(DrawerActivity.this);
        progressDialog.setMessage("Verifying Serial " + serial + " ...");
        progressDialog.show();
        
        StringRequest req = new StringRequest(Request.Method.POST, AppConfig.TECH_SERVER_URL + "verify_product.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(final String response) {
                        progressDialog.hide();
                        System.out.println("resppp" + response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            if (jsonObject.getString("success").equals("1")) {
                                
                                JSONObject object = new JSONObject(jsonObject.getString("user"));
                                
                                AlertDialog.Builder alertDialog2 = new AlertDialog.Builder(
                                        DrawerActivity.this, R.style.Theme_AppCompat_DayNight_Dialog_Alert);
                                alertDialog2.setTitle("Product Verified");
                                alertDialog2.setMessage(
                                        "Serial        " + object.getString("serial") + "\n" +
                                                "Name          " + object.getString("name") + "\n" +
                                                "Model         " + object.getString("model_name") + "\n" +
                                                "Status        " + object.getString("status_name") + "\n" +
                                                "Supplier      " + object.getString("supplier_name") + "\n" +
                                                "Location      " + object.getString("location_name") + "\n" +
                                                "Purchase Cost " + object.getString("purchase_cost") + "\n");
                                alertDialog2.setCancelable(false);
                                
                                alertDialog2.setPositiveButton("Got It",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                
                                                dialog.dismiss();
                                                
                                            }
                                        });
                                alertDialog2.show();
                                
                            }else {
                                Toast.makeText(DrawerActivity.this, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                            }
                            
                            
                        } catch (Exception e) {
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
            public HashMap<String, String> getParams() {
                HashMap<String, String> params = new HashMap<String, String>();
                params.put("schema", user.get(SessionManager.KEY_SCHEMA));
                params.put("serial", serial);
                return params;
            }
            
        };
        // add the request object to the queue to be executed
        AppController.getInstance().addToRequestQueue(req);
    }
    
}
