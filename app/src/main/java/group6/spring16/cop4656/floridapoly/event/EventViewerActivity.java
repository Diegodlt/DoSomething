package group6.spring16.cop4656.floridapoly.event;

import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.TouchDelegate;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import group6.spring16.cop4656.floridapoly.R;
import group6.spring16.cop4656.floridapoly.util.LocationSelectorActivity;
import group6.spring16.cop4656.floridapoly.util.picker.DatePickerEditText;
import group6.spring16.cop4656.floridapoly.util.picker.TimePickerEditText;

//TODO: enable editing when the user is the host

public class EventViewerActivity extends AppCompatActivity implements OnMapReadyCallback {

    public static final String EXTRA_EVENT = "event";
    public static final int RESULT_MODIFIED = 1;

    private static final int REQUEST_LOCATION = 1;

    // The date/time display format
    private final SimpleDateFormat dateFmt = new SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault());
    private final SimpleDateFormat timeFmt = new SimpleDateFormat("h:mm a", Locale.getDefault());

    // The event to display the details of
    private Event event;

    // Is the current user the event host?
    private boolean eventHost = false;

    // Did the user modify the event?
    private boolean modified = false;

    // Is the event being edited
    private boolean editing = false;

    // Is this a new event?
    private boolean newEvent = false;

    private Menu toolbarMenu;
    private MenuItem joinButton;
    private MenuItem leaveButton;
    private MenuItem editButton;
    private MenuItem saveButton;

    // Firebase
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser user;

    // Google Maps
    private MapView mapView;
    private GoogleMap map;

    private EditText eventTitle;
    private Drawable eventTitleBackground;

    private View dateView;
    private TextView dateTitle;
    private EditText dateContent;
    private DatePickerEditText datePicker;

    private View timeView;
    private TextView timeTitle;
    private EditText timeContent;
    private TimePickerEditText timePicker;

    private View attendeesView;
    private TextView attendeesTitle;
    private TextView attendeesContent;

    private View descriptionView;
    private TextView descriptionTitle;
    private EditText descriptionContent;

    private FloatingActionButton locationButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_viewer);

        // Initialize database and auth objects
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();


        // Get the event
        event = (Event)getIntent().getSerializableExtra(EXTRA_EVENT);

        // Perform the setup actions common for new and existing events
        generalSetup(savedInstanceState);

        if (event == null) {
            newEvent = true;
            eventHost = true;
            setupForNewEvent(savedInstanceState);
        }
        else {
            newEvent = false;

            // Check if the user is the event host
            final FirebaseUser user = mAuth.getCurrentUser();
            if (user != null && event.hostId.equals(user.getUid())) {
                eventHost = true;
            }

            setupForExistingEvent(savedInstanceState);
        }

        // Create the toolbar
        Toolbar toolbar = findViewById(R.id.event_viewer_toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            //getSupportActionBar().setTitle(event.title);
            eventTitle.setText(event.title);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    private void generalSetup(Bundle savedInstanceState) {
        eventTitle = findViewById(R.id.event_viewer_title);
        eventTitleBackground = eventTitle.getBackground();
        eventTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editing) {
                    event.title = editable.toString();
                }
            }
        });

        // Date/Time
        dateView = findViewById(R.id.event_viewer_date_view);
        timeView = findViewById(R.id.event_viewer_time_view);

        dateTitle   = dateView.findViewById(R.id.title);
        dateContent = dateView.findViewById(R.id.content);

        timeTitle   = timeView.findViewById(R.id.title);
        timeContent = timeView.findViewById(R.id.content);

        ((ImageView)dateView.findViewById(R.id.icon)).setImageResource(R.drawable.ic_today_black_24dp);
        ((ImageView)timeView.findViewById(R.id.icon)).setImageResource(R.drawable.ic_time_black_24dp);

        dateTitle.setText(R.string.event_date);
        timeTitle.setText(R.string.event_time);

        // Create the date picker
        datePicker = new DatePickerEditText();
        datePicker.setEditText(getSupportFragmentManager(), dateContent);
        datePicker.setOnContentChangedListener(new DatePickerEditText.OnContentChangedListener() {
            @Override
            public void onDateChanged(@NonNull Date date, @Nullable EditText editText) {
                event.day(date);
            }
        });

        // Create the time picker
        timePicker = new TimePickerEditText();
        timePicker.setEditText(getSupportFragmentManager(), timeContent);
        timePicker.setOnContentChangedListener(new TimePickerEditText.OnContentChangedListener() {
            @Override
            public void onTimeChanged(@NonNull Date date, @Nullable EditText editText) {
                event.time(date);
            }
        });


        // Attendees
        attendeesView    = findViewById(R.id.event_viewer_attendees_view);
        attendeesTitle   = attendeesView.findViewById(R.id.title);
        attendeesContent = attendeesView.findViewById(R.id.content);

        ((ImageView)attendeesView.findViewById(R.id.icon)).setImageResource(R.drawable.ic_person_black_24dp);
        attendeesTitle.setText(R.string.event_attendees);
        attendeesContent.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_NORMAL);

        attendeesContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (!editing) {
                    return;
                }

                try {
                    event.maxAttendees = Integer.parseInt(editable.toString());
                }
                catch (NumberFormatException e) {
                    Log.e("EventViewer", "Error parsing max attendees", e);
                }
            }
        });


        // Description
        descriptionView    = findViewById(R.id.event_viewer_desc_view);
        descriptionTitle   = descriptionView.findViewById(R.id.title);
        descriptionContent = descriptionView.findViewById(R.id.content);

        ((ImageView)descriptionView.findViewById(R.id.icon)).setImageResource(R.drawable.ic_comment_black_24dp);
        descriptionTitle.setText(R.string.event_description);
        descriptionContent.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);

        descriptionContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                event.description = editable.toString();
            }
        });


        // Location
        locationButton = findViewById(R.id.event_viewer_location_button);
        locationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(EventViewerActivity.this, LocationSelectorActivity.class);
                startActivityForResult(intent, REQUEST_LOCATION);
            }
        });


        // Create the map
        mapView = findViewById(R.id.event_viewer_map);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);


        // Set the onClick listeners for the content views to call their text field's onClick method
        setupContentViewHitboxes();
    }

    private void setupForNewEvent(Bundle savedInstanceState) {
        // Setup default event state
        event = new Event();
        event.hostId = user.getUid();

        eventTitle.setHint("Enter an event title");
        dateContent.setHint("Select a date");
        timeContent.setHint("Select a time");
        descriptionContent.setHint("Enter an event description");

        //TODO: maybe flag map to not add marker

        // Make the event editable
        setEditable(true);
    }

    private void setupForExistingEvent(Bundle savedInstanceState) {
        // Set the date
        dateContent.setText(dateFmt.format(event.date));

        // Set the time
        timeContent.setText(timeFmt.format(event.date));

        // Set the event attendees
        updateAttendeesText();

        // Set the event description
        descriptionContent.setText(event.description);

        // Disable editing by default
        setEditable(false);
    }

    private void finishViewer() {
        if (modified) {
            setResult(RESULT_MODIFIED);
        }
        finish();
    }

    private boolean validateEventFields() {
        boolean valid = true;

        if (event.title == null || event.title.isEmpty()) {
            Toast.makeText(EventViewerActivity.this, "Please enter a title", Toast.LENGTH_SHORT).show();
            valid = false;
        }
        else if (event.date.equals(new Date(0L))) {
            Toast.makeText(EventViewerActivity.this, "Please enter a date/time", Toast.LENGTH_SHORT).show();
            valid = false;
        }
        else if (event.maxAttendees <= 0) {
            Toast.makeText(EventViewerActivity.this, "Please a valid number of maximum attendees", Toast.LENGTH_SHORT).show();
            valid = false;
        }
        else if (event.maxAttendees < event.numCurrentAttendees()) {
            Toast.makeText(EventViewerActivity.this,
                    "Cannot set max attendees lower than current number of attendees",
                    Toast.LENGTH_SHORT).show();

            event.maxAttendees = event.numCurrentAttendees();
            attendeesContent.setText(String.valueOf(event.maxAttendees));
            valid = false;
        }
        else if (event.latitude == 0 && event.longitude == 0) {
            Toast.makeText(EventViewerActivity.this, "Please enter an event location", Toast.LENGTH_SHORT).show();
            valid = false;
        }
        else if (event.description == null || event.description.isEmpty()) {
            Toast.makeText(EventViewerActivity.this, "Please enter an event description", Toast.LENGTH_SHORT).show();
            valid = false;
        }

        return valid;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.event_viewer_menu, menu);

        toolbarMenu = menu;
        joinButton  = toolbarMenu.findItem(R.id.event_viewer_join_event);
        leaveButton = toolbarMenu.findItem(R.id.event_viewer_leave_event);
        editButton  = toolbarMenu.findItem(R.id.event_viewer_edit_event);
        saveButton  = toolbarMenu.findItem(R.id.event_viewer_save_event);

        editButton.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                setEditable(true);
                getFetchUpdatedEventTask();
                return false;
            }
        });

        saveButton.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if (!validateEventFields()) {
                    return false;
                }

                modified = true;
                setEditable(false);

                getUploadEventTask().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        if (newEvent) {
                            // Add the event to the user's "hosting" array
                            db.collection("users")
                                    .document(user.getUid())
                                    .update("hosting", FieldValue.arrayUnion(event.eventId));

                            newEvent = false; //this is no longer a new event
                        }

                        Toast.makeText(EventViewerActivity.this, "Event saved", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("DB", "Failed to save event", e);
                        Toast.makeText(EventViewerActivity.this, "Failed to save event: server error", Toast.LENGTH_SHORT).show();
                    }
                });

                return false;
            }
        });

        updateToolbarMenu();
        return true;
    }

    private void updateToolbarMenu() {
        if (toolbarMenu == null || event == null || user == null) {
            return;
        }

        joinButton.setVisible(false);
        leaveButton.setVisible(false);

        if (eventHost) {
            if (newEvent || editing) {
                saveButton.setVisible(true);
            }
            else {
                editButton.setVisible(true);
            }
        }
        else if (event.isUserAttending(user.getUid())) {
            leaveButton.setVisible(true);
        }
        else {
            joinButton.setVisible(true);
        }
    }

    private void setupContentViewHitboxes() {

        // Set the touch area for the dateContent EditText to be that of its parent Layout
        dateView.post(new Runnable() {
            @Override
            public void run() {
                Rect delegateArea = new Rect();
                ((View)dateContent.getParent()).getHitRect(delegateArea);

                TouchDelegate touchDelegate = new TouchDelegate(delegateArea, dateContent);
                dateView.setTouchDelegate(touchDelegate);
            }
        });

        timeView.post(new Runnable() {
            @Override
            public void run() {
                Rect delegateArea = new Rect();
                ((View)timeContent.getParent()).getHitRect(delegateArea);

                TouchDelegate touchDelegate = new TouchDelegate(delegateArea, timeContent);
                timeView.setTouchDelegate(touchDelegate);
            }
        });

        attendeesView.post(new Runnable() {
            @Override
            public void run() {
                Rect delegateArea = new Rect();
                ((View)attendeesContent.getParent()).getHitRect(delegateArea);

                TouchDelegate touchDelegate = new TouchDelegate(delegateArea, attendeesContent);
                attendeesView.setTouchDelegate(touchDelegate);
            }
        });

        descriptionView.post(new Runnable() {
            @Override
            public void run() {
                Rect delegateArea = new Rect();
                ((View)descriptionContent.getParent() ).getHitRect(delegateArea);

                TouchDelegate touchDelegate = new TouchDelegate(delegateArea, descriptionContent);
                descriptionView.setTouchDelegate(touchDelegate);
            }
        });
    }

    private void setEditable(boolean editable) {
        if (editable) {
            editing = true;

            if (eventHost && toolbarMenu != null) {
                editButton.setVisible(false);
                saveButton.setVisible(true);
            }

            eventTitle.setClickable(true);
            eventTitle.setFocusableInTouchMode(true);
            eventTitle.setBackground(eventTitleBackground);

            dateContent.setClickable(true);
            dateContent.setFocusable(false);

            timeContent.setClickable(true);
            timeContent.setFocusable(false);

            attendeesTitle.setText(R.string.event_max_attendees);
            attendeesContent.setText(String.valueOf(event.maxAttendees));
            attendeesContent.setClickable(true);
            attendeesContent.setFocusableInTouchMode(true);

            descriptionContent.setClickable(true);
            descriptionContent.setFocusableInTouchMode(true);

            locationButton.show();
        }
        else {
            editing = false;

            if (eventHost && toolbarMenu != null) {
                editButton.setVisible(true);
                saveButton.setVisible(false);
            }

            eventTitle.setClickable(false);
            eventTitle.setFocusable(false);
            eventTitle.setBackgroundResource(android.R.color.transparent);

            dateContent.setClickable(false);
            dateContent.setFocusable(false);

            timeContent.setClickable(false);
            timeContent.setFocusable(false);

            attendeesTitle.setText(R.string.event_attendees);
            attendeesContent.setClickable(false);
            attendeesContent.setFocusable(false);
            updateAttendeesText();

            descriptionContent.setClickable(false);
            descriptionContent.setFocusable(false);

            locationButton.hide();
        }
    }

    private void updateAttendeesText() {
        String max = String.valueOf(event.maxAttendees);
        String current = String.valueOf(event.numCurrentAttendees());
        final String attendeesString = current + " / " + max;
        attendeesContent.setText(attendeesString);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                finishViewer();
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
                        + event.latitude + "%2C" + event.longitude;

                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
                break;
            }
            case R.id.event_viewer_directions: {
                final String url = "https://www.google.com/maps/dir/?api=1&destination="
                                   + event.latitude + "%2C" + event.longitude;

                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
                break;
            }
            default: break;
        }
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_LOCATION && resultCode == RESULT_OK) {
            final LatLng location = data.getParcelableExtra(LocationSelectorActivity.EXTRA_LOCATION);
            event.location(location);
            if (map != null) {
                updateEventMarker();
            }
        }
    }

    private Task<DocumentSnapshot> getFetchUpdatedEventTask() {
        modified = true;

        Task<DocumentSnapshot> task = db.collection("events")
                .document(event.eventId)
                .get();

        task.addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    Event updated = documentSnapshot.toObject(Event.class);
                    if (updated == null) {
                        Log.e("DB", "Failed to convert DocumentSnapshot into Event");
                    }
                    else {
                        event = updated;
                    }
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e("DB", "Failed to get event with ID" + event.eventId, e);
                }
            })
            .addOnCanceledListener(new OnCanceledListener() {
                @Override
                public void onCanceled() {
                    Log.w("DB", "Failed to get event with ID " + event.eventId + " (task canceled)");
                }
            });

        return task;
    }

    private Task<Void> getUploadEventTask() {
        modified = true;

        Task<Void> task = db.collection("events")
                .document(event.eventId)
                .set(event);


        task.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e("DB", "Failed to update event with ID " + event.eventId, e);
                }
            })
            .addOnCanceledListener(new OnCanceledListener() {
                @Override
                public void onCanceled() {
                    Log.w("DB", "Failed to update event with ID " + event.eventId + " (task canceled)");
                }
            });

        return task;
    }

    private void joinEvent() {
        if (eventHost || user == null || event.isUserAttending(user.getUid())) {
            return;
        }

        getFetchUpdatedEventTask().continueWithTask(new Continuation<DocumentSnapshot, Task<Void>>() {
            @Override
            public Task<Void> then(@NonNull Task<DocumentSnapshot> task) throws Exception {
                if (event.full()) {
                    Toast.makeText(EventViewerActivity.this,
                            "Failed to join: event is full",
                            Toast.LENGTH_SHORT).show();
                }
                else if (!event.isUserAttending(user.getUid())) {
                    event.addAttendee(user.getUid());
                }

                return getUploadEventTask();
            }
        })
        .addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(EventViewerActivity.this, "Joined event", Toast.LENGTH_SHORT).show();
                updateToolbarMenu();
                updateAttendeesText();

                // Add the event to the user's "attending" array
                db.collection("users")
                        .document(user.getUid())
                        .update("attending", FieldValue.arrayUnion(event.eventId));
            }
        })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(EventViewerActivity.this,
                        "Failed to join event: server error",
                        Toast.LENGTH_SHORT).show();

                updateToolbarMenu();
                updateAttendeesText();
            }
        });
    }

    private void leaveEvent() {
        if (eventHost || user == null || !event.isUserAttending(user.getUid())) {
            return;
        }

        getFetchUpdatedEventTask().continueWithTask(new Continuation<DocumentSnapshot, Task<Void>>() {
            @Override
            public Task<Void> then(@NonNull Task<DocumentSnapshot> task) throws Exception {
                event.removeAttendee(user.getUid());
                return getUploadEventTask();
            }
        })
        .addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(EventViewerActivity.this, "Left event", Toast.LENGTH_SHORT).show();
                updateToolbarMenu();
                updateAttendeesText();

                // Remove the event from the user's "attending" array
                db.collection("users")
                        .document(user.getUid())
                        .update("attending", FieldValue.arrayRemove(event.eventId));
            }
        })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("DB", "Failed to leave event: server error", e);
                event.addAttendee(user.getUid());
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (googleMap != null) {
            map = googleMap;
            updateEventMarker();
        }
    }

    private void updateEventMarker() {
        // Add marker
        MarkerOptions opt = new MarkerOptions();
        opt.position(event.location());
        map.addMarker(opt);

        // Move to marker
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(opt.getPosition(), 15));
    }

    @Override
    public void onBackPressed() {
        finishViewer();
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
