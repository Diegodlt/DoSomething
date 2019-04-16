package group6.spring16.cop4656.floridapoly.navfragments;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import group6.spring16.cop4656.floridapoly.event.EventViewerActivity;
import group6.spring16.cop4656.floridapoly.R;
import group6.spring16.cop4656.floridapoly.event.Event;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link DiscoverFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link DiscoverFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DiscoverFragment extends Fragment implements
        OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener {

    // Constants - Tag
    private final String TAG = this.getClass().getSimpleName();

    // Constants - Intents
    private final int REQUEST_EVENT_MODIFIED = 1;

    // Constants - Map
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final int DEFAULT_ZOOM = 12;
    private static final LatLng DEFAULT_LOCATION = new LatLng(40.7127, -74.0059); //NYC

    private OnFragmentInteractionListener mListener;

    // Map
    private MapView mapView;
    private GoogleMap map;

    // Location
    private boolean locationPermission = false;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Location lastLocation;

    // List of events
    private List<Event> events = new ArrayList<>();

    // DB and Auth config
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;


    public DiscoverFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment DiscoverFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DiscoverFragment newInstance() {
        DiscoverFragment fragment = new DiscoverFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_discover, container, false);

        mapView = view.findViewById(R.id.discover_map);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());

        return view;
    }

    private void refreshEvents() {
        events.clear();
        map.clear();

        db.collection("events")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot doc : task.getResult()) {
                                Event event = doc.toObject(Event.class);
                                events.add(event);
                                addEventMarker(event);
                            }
                        }
                        else {
                            Log.w("DB error", "Error getting documents.", task.getException());
                        }
                    }
                });
    }

    private void addEventMarker(@NonNull Event event) {
        MarkerOptions opt = new MarkerOptions();
        opt.title(event.getTitle());
        opt.position(event.location());

        Marker mark = map.addMarker(opt);
        mark.setTag(event);
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {
        final Event event = (Event)marker.getTag();

        // Start the event viewer
        Intent intent = new Intent(getActivity(), EventViewerActivity.class);
        intent.putExtra(EventViewerActivity.EXTRA_EVENT, event);
        startActivityForResult(intent, REQUEST_EVENT_MODIFIED);

        // Return false to indicate that we have not consumed the event and that we wish
        // for the default behavior to occur (which is for the camera to move such that the
        // marker is centered and for the marker's info window to open, if it has one).
        return false;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationPermission = true;
        }
        else {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("RESULT", "Request:" + String.valueOf(requestCode) + " Result:" + String.valueOf(resultCode));
        if (requestCode == REQUEST_EVENT_MODIFIED && resultCode == EventViewerActivity.RESULT_MODIFIED) {
            events.clear();
            refreshEvents();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        locationPermission = false;
        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationPermission = true;
                setMapLocationEnabled(true);
                moveMapToUser();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
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
    public void onDetach() {
        super.onDetach();
        mListener = null;
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
            map.setOnMarkerClickListener(this);

            getLocationPermission();
            if (locationPermission) {
                moveMapToUser();
            }
            else {
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, DEFAULT_ZOOM));
            }
            setMapLocationEnabled(locationPermission);

            // Add the event markers to the map
            refreshEvents();
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
                locationResult.addOnCompleteListener(getActivity(), new OnCompleteListener() {
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

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
