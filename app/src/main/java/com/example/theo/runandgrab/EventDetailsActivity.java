package com.example.theo.runandgrab;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

import android.provider.CalendarContract;
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
    private static String url_all_products = "http://runandgrab.xyz/get_event_detail.php";
    private static String add_participant = "http://runandgrab.xyz/add_participant.php";

    // JSON Node names
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_ville = "ville";
    private static final String TAG_event = "event";
    private static final String TAG_PID = "idevent";
    private static final String TAG_lieu = "lieu";
    private static final String TAG_comm = "commentaires";
    private static final String TAG_participant = "nb_participant";
    private static final String TAG_heure = "heure";
    Button btninscription;
    public String date_calendar = null;
    public String heure_calendar = null;
    public String description_calendar = null;
    public String lieu_calendar = null;
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

        //Create button
        Button btninscription = (Button) findViewById(R.id.btninscription);
        btninscription.setVisibility(View.VISIBLE);
        // button click event
        btninscription.setOnClickListener(new View.OnClickListener() {
            @Override

            public void onClick(View view) {
                // creating new product in background thread
                new AddParticipant().execute();
            }
        });

        Button btncalendar = (Button) findViewById(R.id.btncalendar);
        btncalendar.setVisibility(View.VISIBLE);
        // button click event
        btncalendar.setOnClickListener(new View.OnClickListener() {
            @Override

            public void onClick(View view) {
                // creating new product in background thread
                Intent intent = new Intent(Intent.ACTION_INSERT);
                intent.setType("vnd.android.cursor.item/event");
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                try {
                    Date d = formatter.parse(date_calendar);

                    long startTime = d.getTime();
                    long endTime = d.getTime() + 60 * 60 * 1000;


                intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startTime);
                intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime);
                intent.putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, true);

                intent.putExtra(CalendarContract.Events.TITLE, "Plogging with Run and Grab");
                intent.putExtra(CalendarContract.Events.DESCRIPTION, description_calendar);
                intent.putExtra(CalendarContract.Events.EVENT_LOCATION, lieu_calendar);


                startActivity(intent);
                }
                catch(Exception e){
                    e.printStackTrace();
                }
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
                        String heure = c.getString(TAG_heure);
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
                        date_calendar = date;
                        heure_calendar = heure;
                        description_calendar = commentaires;
                        lieu_calendar = lieu;

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
                    Intent i = new Intent(getApplicationContext(), FilterEvent.class);
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