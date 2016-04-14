package com.think.sit;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class LaunchActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    // DB Class to perform DB related operations
    DBController controller = new DBController(this);
    // Progress Dialog Object
    ProgressDialog prgDialog;

    DatabaseHelper helper;
    public static String user_name;
    HashMap<String, String> queryValues;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);


        //get username
        user_name = checkEntry();
        setTitle(user_name);


        // Get User records from SQLite DB
        ArrayList<HashMap<String, String>> userList = controller.getAllUsers();
        // If users exists in SQLite DB
        if (userList.size() != 0) {
            // Set the User Array list in ListView
            ListAdapter adapter = new SimpleAdapter(LaunchActivity.this, userList, R.layout.view_user_entry, new String[]{
                    "userId", "userName", "sender"}, new int[]{R.id.userId, R.id.userName, R.id.sender});
            ListView myList = (ListView) findViewById(android.R.id.list);
            myList.setAdapter(adapter);
            myList.setOnItemClickListener((AdapterView.OnItemClickListener) this);
        }
        // Initialize Progress Dialog properties
        prgDialog = new ProgressDialog(this);
        prgDialog.setMessage("Transferring Data from Remote MySQL DB and Syncing SQLite. Please wait...");
        prgDialog.setCancelable(false);
        // BroadCase Receiver Intent Object
        Intent alarmIntent = new Intent(getApplicationContext(), SampleBC.class);
        // Pending Intent Object
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        // Alarm Manager Object
        AlarmManager alarmManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        // Alarm Manager calls BroadCast for every Ten seconds (10 * 1000), BroadCase further calls service to check if new records are inserted in
        // Remote MySQL DB
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, Calendar.getInstance().getTimeInMillis() + 5000, 10 * 1000, pendingIntent);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    // Options Menu (ActionBar Menu)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    // When Options Menu is selected
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here.
        int id = item.getItemId();
        // When Sync action button is clicked
        if (id == R.id.refresh) {
            // Transfer data from remote MySQL DB to SQLite on Android and perform Sync
            syncSQLiteMySQLDB();
            return true;
        } else if (id == R.id.add) {
            Intent i = new Intent(this, ChatActivity.class);
            startActivity(i);
        }
        return super.onOptionsItemSelected(item);
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    // Method to Sync MySQL to SQLite DB
    public void syncSQLiteMySQLDB() {
        // Create AsycHttpClient object
        AsyncHttpClient client = new AsyncHttpClient();
        // Http Request Params Object
        RequestParams params = new RequestParams();
        // Show ProgressBar
        prgDialog.show();

        params.put("User", user_name);
        // Make Http call to getusers.php
        client.post("http://mosambitech.com/testapp/SIT/getusers.php", params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(String response) {
                // Hide ProgressBar
                prgDialog.hide();
                // Update SQLite DB with response sent by getusers.php
                updateSQLite(response);
            }

            // When error occured
            @Override
            public void onFailure(int statusCode, Throwable error, String content) {
                // TODO Auto-generated method stub
                // Hide ProgressBar
                prgDialog.hide();
                if (statusCode == 404) {
                    Toast.makeText(getApplicationContext(), "Requested resource not found", Toast.LENGTH_LONG).show();
                } else if (statusCode == 500) {
                    Toast.makeText(getApplicationContext(), "Something went wrong at server end", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Unexpected Error occcured! [Most common Error: Device might not be connected to Internet]",
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void updateSQLite(String response) {
        ArrayList<HashMap<String, String>> usersynclist;
        usersynclist = new ArrayList<HashMap<String, String>>();
        // Create GSON object
        Gson gson = new GsonBuilder().create();
        try {
            // Extract JSON array from the response
            JSONArray arr = new JSONArray(response);
            System.out.println(arr.length());
            // If no of array elements is not zero
            if (arr.length() != 0) {
                // Loop through each array element, get JSON object which has userid and username
                for (int i = 0; i < arr.length(); i++) {
                    // Get JSON object
                    JSONObject obj = (JSONObject) arr.get(i);
                    System.out.println(obj.get("userId"));
                    System.out.println(obj.get("sender"));
                    System.out.println(obj.get("userName"));
                    // DB QueryValues Object to insert into SQLite
                    queryValues = new HashMap<String, String>();
                    // Add userID extracted from Object
                    queryValues.put("userId", obj.get("userId").toString());
                    // Add userName extracted from Object
                    queryValues.put("userName", obj.get("userName").toString());
                    // ADD sender name
                    queryValues.put("sender", obj.get("sender").toString());
                    // Insert User into SQLite DB
                    controller.insertUser(queryValues);
                    HashMap<String, String> map = new HashMap<String, String>();
                    // Add status for each User in Hashmap
                    map.put("Id", obj.get("userId").toString());
                    map.put("status", "1");
                    usersynclist.add(map);
                }
                // Inform Remote MySQL DB about the completion of Sync activity by passing Sync status of Users
                updateMySQLSyncSts(gson.toJson(usersynclist));
                // Reload the Main Activity
                reloadActivity();
                setTitle(user_name);
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    // Method to inform remote MySQL DB about completion of Sync activity
    public void updateMySQLSyncSts(String json) {
        System.out.println(json);
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("syncsts", json);
        params.put("User", user_name);
        // Make Http call to updatesyncsts.php with JSON parameter which has Sync statuses of Users
        client.post("http://mosambitech.com/testapp/SIT/updatesyncsts.php", params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(String response) {
                Toast.makeText(getApplicationContext(), "MySQL DB has been informed about Sync activity", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(int statusCode, Throwable error, String content) {
                Toast.makeText(getApplicationContext(), "Error Occured", Toast.LENGTH_LONG).show();
            }
        });
    }

    // Reload MainActivity
    public void reloadActivity() {
        Intent objIntent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(objIntent);
    }


    public String checkEntry() {

        helper = new DatabaseHelper(getApplicationContext());


        SQLiteDatabase db = helper.getReadableDatabase();


        //SELECT
        String[] columns = {"username"};


        Cursor cursor = null;
        try {


            cursor = db.query(DatabaseHelper.Activity_UserInfo_TABLE_NAME, columns, null, null, null, null, null);
//	Toast.makeText(getApplicationContext(), "3", Toast.LENGTH_SHORT).show();
            startManagingCursor(cursor);
        } catch (Exception e) {
            //	Toast.makeText(getApplicationContext(), "error1:" + e.getMessage(), Toast.LENGTH_LONG).show();
        }
        int numberOfRows = cursor.getCount();

        if (numberOfRows <= 0) {

            //Toast.makeText(getApplicationContext(), "Invalid Password", Toast.LENGTH_SHORT).show();

        } else {
            //	Toast.makeText(getApplicationContext(), "Login Success", Toast.LENGTH_SHORT).show();


            cursor.moveToFirst();


            String str_uname = cursor.getString(cursor.getColumnIndex("username"));
            //     Toast.makeText(getApplicationContext(), "Username: " + str_uname, Toast.LENGTH_SHORT).show();

            return str_uname;


        }
        return null;

    }



    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
        TextView c = (TextView) v.findViewById(R.id.userId);
        TextView c1 = (TextView) v.findViewById(R.id.userName);
        TextView c2 = (TextView) v.findViewById(R.id.sender);
        Intent i = new Intent(LaunchActivity.this, ViewActivity.class);
        i.putExtra("id", c.getText().toString());
        i.putExtra("user", c1.getText().toString());
        i.putExtra("sender", c2.getText().toString());
        startActivity(i);
    }
}

