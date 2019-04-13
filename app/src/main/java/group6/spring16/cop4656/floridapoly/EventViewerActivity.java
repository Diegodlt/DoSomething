package group6.spring16.cop4656.floridapoly;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import group6.spring16.cop4656.floridapoly.event.Event;

//TODO: add the ability to join an event
//TODO: enable editing when the user is the host

public class EventViewerActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String EXTRA_EVENT = "event";

    // The event to display the details of
    private Event event;

    // Is the current user the event host?
    private boolean eventHost = false;

    private Menu toolbarMenu;

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

        // Check if the user is the event host
        final FirebaseUser user = mAuth.getCurrentUser();
        if (user != null && event.getHostId().equals(user.getUid())) {
            eventHost = true;
        }


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
        updateAttendees();


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
        toolbarMenu = menu;
        updateToolbarMenu();
        return true;
    }

    private void updateToolbarMenu() {
        final FirebaseUser user = mAuth.getCurrentUser();

        MenuItem joinButton  = toolbarMenu.findItem(R.id.event_viewer_join_event);
        MenuItem leaveButton = toolbarMenu.findItem(R.id.event_viewer_leave_event);

        if (eventHost) {
            joinButton.setVisible(false);
            leaveButton.setVisible(false);
        }
        else if (event != null && user != null) {
            if (event.getAttendees().contains(user.getUid())) {
                joinButton.setVisible(false);
                leaveButton.setVisible(true);
            }
            else {
                joinButton.setVisible(true);
                leaveButton.setVisible(false);
            }
        }
    }

    private void updateAttendees() {
        View attendeesView = findViewById(R.id.event_viewer_attendees_view);

        ((ImageView)attendeesView.findViewById(R.id.icon)).setImageResource(R.drawable.ic_person_black_24dp);

        TextView attendeesTitle   = attendeesView.findViewById(R.id.title);
        TextView attendeesContent = attendeesView.findViewById(R.id.content);
        attendeesTitle.setText(R.string.event_attendees);

        String max     = String.valueOf(event.getMaxAttendees());
        String current = String.valueOf(event.getAttendees().size());
        final String attendeesString = current + " / " + max;
        attendeesContent.setText(attendeesString);
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
            case R.id.event_viewer_open_maps: {
                final String url = "https://www.google.com/maps/search/?api=1&query="
                        + event.getLatitude() + "%2C" + event.getLongitude();

                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
                break;
            }
            case R.id.event_viewer_directions: {
                final String url = "https://www.google.com/maps/dir/?api=1&destination="
                                   + event.getLatitude() + "%2C" + event.getLongitude();

                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
                break;
            }
            default: break;
        }
        return true;
    }

    private void joinEvent() {
        final FirebaseUser user = mAuth.getCurrentUser();

        if (!eventHost && user != null && !event.getAttendees().contains(user.getUid())) {
            event.addAttendee(user.getUid());

            db.collection("events")
                    .document(event.getEventId())
                    .set(event)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(EventViewerActivity.this, "Joined event", Toast.LENGTH_SHORT).show();
                            updateToolbarMenu();
                            updateAttendees();

                            // Add the event to the user's "attending" array
                            db.collection("users")
                                    .document(user.getUid())
                                    .update("attending", FieldValue.arrayUnion(event.getEventId()));
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e("DB", "Failed to join event", e);
                            event.removeAttendee(user.getUid());
                        }
                    });
        }
    }

    private void leaveEvent() {
        final FirebaseUser user = mAuth.getCurrentUser();

        if (!eventHost && user != null && event.getAttendees().contains(user.getUid())) {
            event.removeAttendee(user.getUid());

            db.collection("events")
                    .document(event.getEventId())
                    .set(event)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(EventViewerActivity.this, "Left event", Toast.LENGTH_SHORT).show();
                            updateToolbarMenu();
                            updateAttendees();

                            // Remove the event from the user's "attending" array
                            db.collection("users")
                                    .document(user.getUid())
                                    .update("attending", FieldValue.arrayRemove(event.getEventId()));
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e("DB", "Failed to leave event", e);
                            event.addAttendee(user.getUid());
                        }
                    });
        }
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
