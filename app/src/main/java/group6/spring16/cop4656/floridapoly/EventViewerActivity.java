package group6.spring16.cop4656.floridapoly;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import group6.spring16.cop4656.floridapoly.event.Event;

//TODO: add the ability to join an event
public class EventViewerActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String EXTRA_EVENT = "event";

    // The event to display the details of
    private Event event;

    // Firebase
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    // Google Maps
    private MapView mapView;
    private GoogleMap map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_viewer);

        // Get the event
        event = (Event)getIntent().getSerializableExtra(EXTRA_EVENT);


        // Initialize database and auth objects
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();


        // Create the toolbar
        Toolbar toolbar = findViewById(R.id.event_viewer_toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(event.getTitle());
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }


        // Set the date/time
        final SimpleDateFormat dateFmt = new SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault());
        final SimpleDateFormat timeFmt = new SimpleDateFormat("h:mm a", Locale.getDefault());

        View dateView = findViewById(R.id.event_viewer_date_view);
        View timeView = findViewById(R.id.event_viewer_time_view);

        ((ImageView)dateView.findViewById(R.id.icon)).setImageResource(R.drawable.ic_today_black_24dp);
        ((ImageView)timeView.findViewById(R.id.icon)).setImageResource(R.drawable.ic_time_black_24dp);

        TextView dateTitle   = dateView.findViewById(R.id.title);
        TextView dateContent = dateView.findViewById(R.id.content);
        dateTitle.setText(R.string.event_date);
        dateContent.setText(dateFmt.format(event.getDate()));

        TextView timeTitle   = timeView.findViewById(R.id.title);
        TextView timeContent = timeView.findViewById(R.id.content);
        timeTitle.setText(R.string.event_time);
        timeContent.setText(timeFmt.format(event.getDate()));


        // Set the event attendees
        View attendeesView = findViewById(R.id.event_viewer_attendees_view);

        ((ImageView)attendeesView.findViewById(R.id.icon)).setImageResource(R.drawable.ic_person_black_24dp);

        TextView attendeesTitle   = attendeesView.findViewById(R.id.title);
        TextView attendeesContent = attendeesView.findViewById(R.id.content);
        attendeesTitle.setText(R.string.event_attendees);

        String max     = String.valueOf(event.getMaxAttendees());
        String current = String.valueOf(1); /*TODO: get number of current attendees*/
        final String attendeesString = current + " / " + max;
        attendeesContent.setText(attendeesString);


        // Set the event description
        View descriptionView = findViewById(R.id.event_viewer_desc_view);

        ((ImageView)descriptionView.findViewById(R.id.icon)).setImageResource(R.drawable.ic_comment_black_24dp);

        TextView descriptionTitle   = descriptionView.findViewById(R.id.title);
        TextView descriptionContent = descriptionView.findViewById(R.id.content);
        descriptionTitle.setText(R.string.event_description);
        descriptionContent.setText(event.getDescription());


        // Create the map
        mapView = findViewById(R.id.event_viewer_map);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.event_viewer_menu, menu);

        //TODO: check if the user has joined this event and set the join or leave button visible
        menu.findItem(R.id.event_viewer_join_event).setVisible(true);
        menu.findItem(R.id.event_viewer_leave_event).setVisible(false);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                finish();
                break;
            }
            case R.id.event_viewer_join_event: {
                joinEvent();
                break;
            }
            case R.id.event_viewer_leave_event: {
                leaveEvent();
                break;
            }
            default: break;
        }
        return true;
    }

    private void joinEvent() {
        //TODO: join the event
    }

    private void leaveEvent() {
        //TODO: leave the event
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
