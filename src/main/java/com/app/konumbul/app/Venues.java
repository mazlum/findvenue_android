package com.app.konumbul.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


public class Venues extends Activity {

    List<Venue> venues = new ArrayList<Venue>();
    LocationManager lm;
    boolean network_enabled;

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
        if(prefs.getString("token", "").equals("")){
            Intent loginIntent = new Intent(this, Login.class);
            loginIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(loginIntent);
        }

        setContentView(R.layout.activity_venues);

        venues.add(new Venue("Havaalanı", "airport"));
        venues.add(new Venue("Hastahane", "hospital"));
        venues.add(new Venue("Banka", "bank"));
        venues.add(new Venue("Cami", "mosque"));
        venues.add(new Venue("Kafe", "cafe"));

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
}
