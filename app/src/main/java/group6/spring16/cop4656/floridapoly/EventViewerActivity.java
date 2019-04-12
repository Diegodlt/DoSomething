package group6.spring16.cop4656.floridapoly;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import group6.spring16.cop4656.floridapoly.event.Event;

//TODO: add the ability to join an event
public class EventViewerActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String EXTRA_EVENT = "event";

    private Event event;

    private MapView mapView;
    private GoogleMap map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_viewer);

        // Get the event
        event = (Event)getIntent().getSerializableExtra(EXTRA_EVENT);

        // Create the toolbar
        Toolbar toolbar = findViewById(R.id.event_viewer_toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(event.getTitle());
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Set the date/time
        SimpleDateFormat dateFmt = new SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault());
        SimpleDateFormat timeFmt = new SimpleDateFormat("h:mm a", Locale.getDefault());

        View dateView = findViewById(R.id.event_viewer_date_view);
        View timeView = findViewById(R.id.event_viewer_time_view);

        TextView dateTitle = dateView.findViewById(R.id.title);
        TextView dateContent = dateView.findViewById(R.id.content);
        dateTitle.setText(R.string.event_date);
        dateContent.setText(dateFmt.format(event.getDate()));

        TextView timeTitle = timeView.findViewById(R.id.title);
        TextView timeContent = timeView.findViewById(R.id.content);
        timeTitle.setText(R.string.event_time);
        timeContent.setText(timeFmt.format(event.getDate()));

        // Set the event attendees
        View attendeesView = findViewById(R.id.event_viewer_attendees_view);
        TextView attendeesTitle = attendeesView.findViewById(R.id.title);
        TextView attendeesContent = attendeesView.findViewById(R.id.content);
        attendeesTitle.setText(R.string.event_attendees);

        String max = String.valueOf(event.getMaxAttendees());
        String current = String.valueOf(1); /*TODO: get number of current attendees*/
        final String attendeesString = current + " / " + max;
        attendeesContent.setText(attendeesString);

        // Set the event description
        View descriptionView = findViewById(R.id.event_viewer_desc_view);
        TextView descriptionTitle = descriptionView.findViewById(R.id.title);
        TextView descriptionContent = descriptionView.findViewById(R.id.content);
        descriptionTitle.setText(R.string.event_description);
        descriptionContent.setText(event.getDescription());

        // Create the map
        mapView = findViewById(R.id.event_viewer_map);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                finish();
                break;
            }
            default: break;
        }
        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (googleMap != null) {
            map = googleMap;

            // Add marker
            MarkerOptions opt = new MarkerOptions();
            opt.position(event.location());
            map.addMarker(opt);

            // Move to marker
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(opt.getPosition(), 15));
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
}
