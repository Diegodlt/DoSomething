package group6.spring16.cop4656.floridapoly.util;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import group6.spring16.cop4656.floridapoly.R;

public class LocationSelectorActivity extends AppCompatActivity implements OnMapReadyCallback {

    private final String TAG = this.getClass().getSimpleName();

    private static final String EXTRA_LOCATION = "location";

    private static final int REQUEST_AUTOCOMPLETE_RESULT = 1;

    private static final int    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final int    DEFAULT_ZOOM = 12;
    private static final LatLng DEFAULT_LOCATION = new LatLng(40.7127, -74.0059); //NYC

    private Toolbar toolbar;

    private MapView mapView;
    private GoogleMap map;

    private boolean locationPermission = false;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Location lastLocation;

    private LatLng selectedLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_selector);
        Places.initialize(this, getString(R.string.google_cloud_api_key));

        toolbar = findViewById(R.id.location_selector_toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(null);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        mapView = findViewById(R.id.location_selector_map);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
    }

    private void returnLocation(int resultCode) {
        Intent intent = new Intent();
        setResult(resultCode, intent);
        if (resultCode == RESULT_OK) {
            intent.putExtra(EXTRA_LOCATION, selectedLocation);
        }
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.location_selector_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                returnLocation(RESULT_CANCELED);
                break;
            }
            case R.id.location_selector_places_search: {
                final List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS);
                Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields).build(this);
                startActivityForResult(intent, REQUEST_AUTOCOMPLETE_RESULT);
                break;
            }
            case R.id.location_selector_checkmark: {
                if (selectedLocation != null) {
                    returnLocation(RESULT_OK);
                }
                else {
                    returnLocation(RESULT_CANCELED);
                }
                break;
            }
            default: break;
        }
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_AUTOCOMPLETE_RESULT) {
            if (resultCode == RESULT_OK) {
                selectedLocation = Autocomplete.getPlaceFromIntent(data).getLatLng();
            }
            else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                Status status = Autocomplete.getStatusFromIntent(data);
                Log.e(TAG, status.toString());
            }
        }
    }

    @Override
    public void onResume() {
        mapView.onResume();
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (googleMap != null) {
            map = googleMap;
            map.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                @Override
                public void onMapLongClick(LatLng latLng) {
                    selectedLocation = latLng;

                    MarkerOptions opt = new MarkerOptions();
                    opt.position(latLng);

                    map.clear();
                    map.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                    map.addMarker(opt);
                }
            });

            getLocationPermission();
            if (locationPermission) {
                moveMapToUser();
            }
            else {
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, DEFAULT_ZOOM));
            }
            setMapLocationEnabled(locationPermission);
        }
    }

    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationPermission = true;
        }
        else {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    private void setMapLocationEnabled(boolean state) {
        try {
            if (map != null) {
                map.setMyLocationEnabled(state && locationPermission);
                map.getUiSettings().setMyLocationButtonEnabled(state && locationPermission);
            }
        }
        catch (SecurityException ex) {
            Log.e("Exception: %s", ex.getMessage());
        }
    }

    private void moveMapToUser() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (locationPermission) {
                Task locationResult = fusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            lastLocation = (Location)task.getResult();
                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(lastLocation.getLatitude(),
                                            lastLocation.getLongitude()),
                                    DEFAULT_ZOOM));
                        }
                        else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, DEFAULT_ZOOM));
                        }
                    }
                });
            }
        }
        catch(SecurityException e)  {
            Log.e(TAG,"Exception: %s", e);
        }
    }
}
