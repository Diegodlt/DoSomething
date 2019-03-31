package group6.spring16.cop4656.floridapoly;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Calendar;
import java.util.Date;

import group6.spring16.cop4656.floridapoly.event.Event;
import group6.spring16.cop4656.floridapoly.picker.DatePickerEditText;
import group6.spring16.cop4656.floridapoly.picker.TimePickerEditText;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link EventCreatorFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link EventCreatorFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EventCreatorFragment extends Fragment implements OnMapReadyCallback {

    private static final int REQUEST_LOCATION = 1;
    private static final String EXTRA_LOCATION = "location";

    private OnFragmentInteractionListener mListener;

    private EditText nameText;
    private EditText descriptionText;
    private EditText maxAttendeesText;
    private Button   locationButton;

    private MapView   mapView;
    private GoogleMap map;
    private LatLng    eventLocation;

    private DatePickerEditText dateText;
    private TimePickerEditText timeText;

    public EventCreatorFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment EventCreatorFragment.
     */
    public static EventCreatorFragment newInstance() {
        EventCreatorFragment fragment = new EventCreatorFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_event_creator, container, false);

        // Get UI elements
        nameText         = view.findViewById(R.id.event_name_text);
        locationButton   = view.findViewById(R.id.event_location_select_button);
        descriptionText  = view.findViewById(R.id.event_description_text);
        maxAttendeesText = view.findViewById(R.id.event_max_attendees_text);

        dateText = new DatePickerEditText();
        dateText.setEditText(getActivity().getSupportFragmentManager(), (EditText)view.findViewById(R.id.event_date_text));

        timeText = new TimePickerEditText();
        timeText.setEditText(getActivity().getSupportFragmentManager(), (EditText)view.findViewById(R.id.event_time_text));

        // Set Location button
        locationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent intent = new Intent(getActivity(), LocationSelectorActivity.class);
                startActivityForResult(intent, REQUEST_LOCATION);
            }
        });

        // Create Event button
        view.findViewById(R.id.event_creator_button_create).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(nameText.getText())) {
                    Toast.makeText(getActivity(), "Please enter an event name", Toast.LENGTH_SHORT).show();
                }
                else if (TextUtils.isEmpty(dateText.getEditText().getText())) {
                    Toast.makeText(getActivity(), "Please enter an event date", Toast.LENGTH_SHORT).show();
                }
                else if (TextUtils.isEmpty(timeText.getEditText().getText())) {
                    Toast.makeText(getActivity(), "Please enter an event time", Toast.LENGTH_SHORT).show();
                }
                else if (TextUtils.isEmpty(maxAttendeesText.getText())) {
                    Toast.makeText(getActivity(), "Please enter an attendee limit", Toast.LENGTH_SHORT).show();
                }
                else if (eventLocation == null) {
                    Toast.makeText(getActivity(), "Please enter an event location", Toast.LENGTH_SHORT).show();
                }
                else {
                    createEvent();
                }
            }
        });

        // Create the map
        mapView = view.findViewById(R.id.event_creator_map);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_LOCATION && resultCode == Activity.RESULT_OK) {
            final LatLng location = data.getParcelableExtra(EXTRA_LOCATION);
            eventLocation = location;
            if (map != null) {
                MarkerOptions opt = new MarkerOptions();
                opt.position(location);
                map.addMarker(opt);
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15));
            }
        }
    }

    private void createEvent() {
        final String name         = nameText.getText().toString();
        final Date   date         = dateText.getDate();
        final Date   time         = timeText.getTime();
        final String maxAttendees = maxAttendeesText.getText().toString();
        final String description  = descriptionText.getText().toString();

        // Combine the two dates into one calendar
        Calendar dateCal = Calendar.getInstance();
        Calendar timeCal = Calendar.getInstance();
        dateCal.setTime(date);
        timeCal.setTime(time);
        dateCal.set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY));
        dateCal.set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE));

        // Create the event
        Event event = new Event(name, dateCal, eventLocation, Integer.parseInt(maxAttendees));
        event.setDescription(description);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (googleMap != null) {
            map = googleMap;
        }
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
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
    public void onDetach() {
        super.onDetach();
        mListener = null;
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
