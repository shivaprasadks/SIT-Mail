package com.think.sit;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class SettingActivity extends AppCompatActivity {
    EditText cur_pass, new_pass, repeat_pass;
    Button chg;
    DatabaseHelper helper;
    String u_name,string_pass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);



        //set layout title
        setTitle("Settings");

        //initialise edittext
        cur_pass = (EditText) findViewById(R.id.editTextUserName);
        new_pass = (EditText) findViewById(R.id.editTextPassword);
        repeat_pass = (EditText) findViewById(R.id.editTextRePassword);

        //initialization of button
        chg = (Button) findViewById(R.id.btnchg);

        //get user name
        u_name = checkEntry();


        //change password
        chg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String s = cur_pass.getText().toString();
                String s1 = new_pass.getText().toString();
                String s2 = repeat_pass.getText().toString();

                if (s.equals("") || s1.equals("") || s2.equals("")) {
                    Snackbar.make(v, "Provoid proper data", Snackbar.LENGTH_LONG).setAction("Action", null).show();

                } else if (s1.equals(s2)) {

                    string_pass = new_pass.getText().toString();


                    serverCall(u_name, string_pass);


                } else if(!s1.equals(s2)){
                    Snackbar.make(v, "New password doesn't match", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                }
            }
        });
    }

    private void serverCall(final String username, String password) {

        class LoginAsync extends AsyncTask<String, Void, String> {

            private Dialog loadingDialog;


            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                loadingDialog = ProgressDialog.show(SettingActivity.this, "Please wait", "Updating...");
            }


            @Override
            protected String doInBackground(String... params) {
                String uname = params[0];
                String pass = params[1];

                InputStream is = null;
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("username", uname));
                nameValuePairs.add(new BasicNameValuePair("password", pass));
                String result = null;

                try {
                    HttpClient httpClient = new DefaultHttpClient();
                    HttpPost httpPost = new HttpPost(
                            "http://sitmail.in/change_pass.php");
                    httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                    HttpResponse response = httpClient.execute(httpPost);

                    HttpEntity entity = response.getEntity();

                    is = entity.getContent();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"), 8);
                    StringBuilder sb = new StringBuilder();

                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    result = sb.toString();
                } catch (ClientProtocolException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return result;
            }


            @Override
            protected void onPostExecute(String result) {
                String s = result.trim();
                loadingDialog.dismiss();
                if (s.equalsIgnoreCase("success")) {

                    Toast.makeText(getApplicationContext(), "Password Updated", Toast.LENGTH_LONG).show();


                } else {

                    Toast.makeText(getApplicationContext(), "Password Invalidated", Toast.LENGTH_LONG).show();

                }
            }
        }

        LoginAsync la = new LoginAsync();
        la.execute(username, password);

    }

    //show password
    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch (view.getId()) {
            case R.id.radio_pirates:
                if (checked) {
                    new_pass.setInputType(InputType.TYPE_CLASS_TEXT);
                    repeat_pass.setInputType(InputType.TYPE_CLASS_TEXT);
                } else {
                    new_pass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    repeat_pass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

                }
                // Pirates are the best
                break;

        }
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
}
