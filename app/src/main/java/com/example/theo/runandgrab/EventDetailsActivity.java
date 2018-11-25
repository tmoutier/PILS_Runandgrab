package com.example.theo.runandgrab;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.widget.*;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView.OnItemClickListener;

public class EventDetailsActivity extends ListActivity {

    // Progress Dialog
    private ProgressDialog pDialog;
    String pid;
    // Creating JSON Parser object
    JSONParser jParser = new JSONParser();

    ArrayList<HashMap<String, String>> productsList;

    // url to get all products list
    private static String url_all_products = "http://10.0.2.2/get_event_detail.php";
    private static String add_participant = "http://10.0.2.2/add_participant.php";

    // JSON Node names
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_ville = "ville";
    private static final String TAG_event = "event";
    private static final String TAG_PID = "idevent";
    private static final String TAG_lieu = "lieu";
    private static final String TAG_comm = "commentaires";
    private static final String TAG_participant = "nb_participant";
    Button btninscription;

    // products JSONArray
    JSONArray lieu = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_products);
        pid = getIntent().getStringExtra("idevent");

        // Hashmap for ListView
        productsList = new ArrayList<HashMap<String, String>>();

        // Loading products in Background Thread
        new LoadAllProducts().execute();

        // Get listview
        ListView lv = getListView();

        // Create button
        Button btninscription = (Button) findViewById(R.id.btninscription);

        // button click event
        btninscription.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                // creating new product in background thread
                new AddParticipant().execute();
            }
        });
    }

    /**
     * Background Async Task to Load all product by making HTTP Request
     * */
    class LoadAllProducts extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(EventDetailsActivity.this);
            pDialog.setMessage("Loading events. Please wait...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        /**
         * getting All products from url
         * */
        protected String doInBackground(String... args) {
            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            // getting JSON string from URL

            params.add(new BasicNameValuePair("idevent", pid));
            JSONObject json = jParser.makeHttpRequest(url_all_products, "GET", params);

            // Check your log cat for JSON reponse
            Log.d("All Events: ", json.toString());

            try {
                // Checking for SUCCESS TAG
                int success = json.getInt(TAG_SUCCESS);

                if (success == 1) {
                    // products found
                    // Getting Array of Products
                    lieu = json.getJSONArray(TAG_event);

                    // looping through All Products
                    for (int i = 0; i < lieu.length(); i++) {
                        JSONObject c = lieu.getJSONObject(i);

                        // Storing each json item in variable
                        String idevent = c.getString(TAG_PID);
                        String ville = c.getString(TAG_ville);
                        String lieu = c.getString(TAG_lieu);
                        String date = c.getString("date");
                        String heure = c.getString("heure");
                        String commentaires = c.getString("commentaires");
                        String nb_participant = c.getString(TAG_participant);

                        // creating new HashMap
                        HashMap<String, String> map = new HashMap<String, String>();

                        // adding each child node to HashMap key => value
                        map.put(TAG_PID, idevent);
                        map.put(TAG_ville, ville);
                        map.put(TAG_lieu, lieu);
                        map.put("date", date);
                        map.put("heure", heure);
                        map.put(TAG_comm, commentaires);
                        map.put(TAG_participant, nb_participant);

                        // adding HashList to ArrayList
                        productsList.add(map);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         * **/
        protected void onPostExecute(String file_url) {
            // dismiss the dialog after getting all products
            pDialog.dismiss();
            // updating UI from Background Thread
            runOnUiThread(new Runnable() {
                public void run() {
                    /**
                     * Updating parsed JSON data into ListView
                     * */
                    ListAdapter adapter = new SimpleAdapter(
                            EventDetailsActivity.this, productsList,
                            R.layout.activity_event_details, new String[] { TAG_PID, TAG_ville,
                            TAG_lieu, "date", "heure", TAG_comm, TAG_participant},
                            new int[] { R.id.pid, R.id.ville, R.id.lieu, R.id.date, R.id.heure, R.id.commentaires, R.id.nbParticipant });
                    // updating listview
                    setListAdapter(adapter);
                }
            });

        }

    }

    class AddParticipant extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(EventDetailsActivity.this);
            pDialog.setMessage("Inscription à l'event");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }
        protected String doInBackground(String... args) {
            List<NameValuePair> params = new ArrayList<NameValuePair>();

            params.add(new BasicNameValuePair("idevent", pid));
            // getting JSON Object
            // Note that create product url accepts POST method
            JSONObject json = jParser.makeHttpRequest(add_participant,
                    "GET", params);

            // check log cat fro response
            Log.d("Create Response", json.toString());

            // check for success tag
            try {
                int success = json.getInt(TAG_SUCCESS);

                if (success == 1) {
                    // successfully created product
                    Intent i = new Intent(getApplicationContext(), AllProductsActivity.class);
                    startActivity(i);

                    // closing this screen
                    finish();
                } else {
                    // failed to create product
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         * **/
        protected void onPostExecute(String file_url) {
            // dismiss the dialog once done
            pDialog.dismiss();
        }

    }
}