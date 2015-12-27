package com.app.konumbul.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class Venues extends Activity {

    List<Venue> venues = new ArrayList<Venue>();
    LocationManager lm;
    boolean network_enabled;
    Button btnLogout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if(!network_enabled){
            //Toast.makeText(getApplicationContext(), "olmadi", Toast.LENGTH_LONG).show();
            new AlertDialog.Builder(Venues.this)
                    .setTitle(R.string.no_location)
                    .setMessage(R.string.select_proc)
                    .setPositiveButton(R.string.open_location_settings, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Intent viewIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(viewIntent);
                            finish();
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .show();
            return;
        }

        SharedPreferences prefs = getSharedPreferences("registiration", MODE_PRIVATE);
        String token = prefs.getString("token", "");
        if(token.equals("")){
            Intent loginIntent = new Intent(this, Login.class);
            loginIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(loginIntent);
        }else{
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("key", token);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            PostData postData = new PostData(jsonObject, getApplicationContext());
            postData.execute();
        }

        setContentView(R.layout.activity_venues);

        btnLogout = (Button)findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedpreferences = getSharedPreferences("registiration", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putString("token", "");
                editor.commit();

                final Intent loginIntent = new Intent(Venues.this, Venues.class);
                loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(loginIntent);
                finish();
            }
        });
        venues.add(new Venue("Havaalanı", "airport"));
        venues.add(new Venue("Hastahane", "hospital"));
        venues.add(new Venue("Banka", "bank"));
        venues.add(new Venue("Cami", "mosque"));
        venues.add(new Venue("Kafe", "cafe"));
        venues.add(new Venue("Müze", "museum"));
        venues.add(new Venue("Kütüphane", "library"));
        venues.add(new Venue("Otobüs Durağı", "bus_station"));
        venues.add(new Venue("Tiyatro", "movie_theater"));
        venues.add(new Venue("Hayvanat Bahçesi", "zoo"));
        venues.add(new Venue("Benzin İstasyonu", "gas_station"));
        venues.add(new Venue("Sergi Salonu", "art_gallery"));
        venues.add(new Venue("Üniversite", "university"));
        venues.add(new Venue("Restöranlar", "restaurant"));
        venues.add(new Venue("Okul", "school"));
        venues.add(new Venue("Alışveriş Merkezi", "shopping_mall"));

        final ListView venueListView = (ListView) findViewById(R.id.listViewVenues);
        VenueAdapter adapter = new VenueAdapter(this, venues, getApplicationContext());
        venueListView.setAdapter(adapter);

        venueListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Toast.makeText(getApplicationContext(), venues.get(position).getVenueString(), Toast.LENGTH_LONG).show();
                Intent mapIntent = new Intent(Venues.this, MapVenue.class);
                mapIntent.putExtra("place", venues.get(position).getVenueType());
                startActivity(mapIntent);
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_venues, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private class PostData extends AsyncTask<String, String, String> {

        HttpURLConnection urlConnection;

        JSONObject jsonData;
        Context context;
        public PostData(JSONObject jsonData, Context context){
            this.jsonData = jsonData;
            this.context=context;
        }

        @Override
        protected String doInBackground(String... args) {

            StringBuilder result = new StringBuilder();

            try {
                URL url = new URL(Global.webServerUrl + "/control/");
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "application/json");
                OutputStreamWriter out = new OutputStreamWriter(urlConnection.getOutputStream());
                out.write(jsonData.toString());
                out.flush();

                InputStream in = new BufferedInputStream(urlConnection.getInputStream());

                BufferedReader reader = new BufferedReader(new InputStreamReader(in));

                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }

            }catch( Exception e) {
                e.printStackTrace();
            }
            finally {
                urlConnection.disconnect();
            }


            return result.toString();
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject resultObject = new JSONObject(result);
                if(resultObject.getString("status").equals("1")) {
                    if(resultObject.getString("control").equals("0")) {
                        final Intent loginIntent = new Intent(Venues.this, Login.class);
                        loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(loginIntent);
                        finish();
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

    }
}
