package group6.spring16.cop4656.floridapoly.event;

import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import group6.spring16.cop4656.floridapoly.R;
import group6.spring16.cop4656.floridapoly.util.picker.DatePickerEditText;
import group6.spring16.cop4656.floridapoly.util.picker.TimePickerEditText;

//TODO: enable editing when the user is the host

public class EventViewerActivity extends AppCompatActivity implements OnMapReadyCallback {

    public static final String EXTRA_EVENT = "event";
    public static final int RESULT_MODIFIED = 1;

    // The event to display the details of
    private Event event;

    // Is the current user the event host?
    private boolean eventHost = false;

    // Did the user modify the event?
    private boolean modified = false;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_viewer);

        // Get the event
        event = (Event)getIntent().getSerializableExtra(EXTRA_EVENT);


        // Initialize database and auth objects
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

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

        dateView = findViewById(R.id.event_viewer_date_view);
        timeView = findViewById(R.id.event_viewer_time_view);

        ((ImageView)dateView.findViewById(R.id.icon)).setImageResource(R.drawable.ic_today_black_24dp);
        ((ImageView)timeView.findViewById(R.id.icon)).setImageResource(R.drawable.ic_time_black_24dp);


        // Set the date objects
        dateTitle   = dateView.findViewById(R.id.title);
        dateContent = dateView.findViewById(R.id.content);
        dateTitle.setText(R.string.event_date);
        dateContent.setText(dateFmt.format(event.getDate()));

        // Create the date picker
        datePicker = new DatePickerEditText();
        datePicker.setEditText(getSupportFragmentManager(), dateContent);
        datePicker.setOnContentChangedListener(new DatePickerEditText.OnContentChangedListener() {
            @Override
            public void onDateChanged(@NonNull Date date, @Nullable EditText editText) {
                event.day(date);
            }
        });


        // Set the time objects
        timeTitle   = timeView.findViewById(R.id.title);
        timeContent = timeView.findViewById(R.id.content);
        timeTitle.setText(R.string.event_time);
        timeContent.setText(timeFmt.format(event.getDate()));

        // Create the time picker
        timePicker = new TimePickerEditText();
        timePicker.setEditText(getSupportFragmentManager(), timeContent);
        timePicker.setOnContentChangedListener(new TimePickerEditText.OnContentChangedListener() {
            @Override
            public void onTimeChanged(@NonNull Date date, @Nullable EditText editText) {
                event.time(date);
            }
        });


        // Set the event attendees
        attendeesView = findViewById(R.id.event_viewer_attendees_view);

        ((ImageView)attendeesView.findViewById(R.id.icon)).setImageResource(R.drawable.ic_person_black_24dp);

        attendeesTitle   = attendeesView.findViewById(R.id.title);
        attendeesContent = attendeesView.findViewById(R.id.content);
        attendeesTitle.setText(R.string.event_attendees);
        attendeesContent.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_NORMAL);

        updateAttendees();


        // Set the event description
        descriptionView = findViewById(R.id.event_viewer_desc_view);

        ((ImageView)descriptionView.findViewById(R.id.icon)).setImageResource(R.drawable.ic_comment_black_24dp);

        descriptionTitle   = descriptionView.findViewById(R.id.title);
        descriptionContent = descriptionView.findViewById(R.id.content);
        descriptionTitle.setText(R.string.event_description);
        descriptionContent.setText(event.getDescription());
        descriptionContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                event.setDescription(editable.toString());
            }
        });


        // Create the map
        mapView = findViewById(R.id.event_viewer_map);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);


        // Set the onClick listeners for the content views to call their text field's onClick method
        setupContentViewHitboxes();


        // Disable editing by default
        setEditable(false);
    }

    private void finishViewer() {
        if (modified) {
            setResult(RESULT_MODIFIED);
        }
        finish();
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
                modified = true;
                setEditable(false);

                getUploadEventTask().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
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
            if (eventHost && toolbarMenu != null) {
                editButton.setVisible(false);
                saveButton.setVisible(true);
            }

            dateContent.setClickable(true);
            dateContent.setFocusable(false);

            timeContent.setClickable(true);
            timeContent.setFocusable(false);

            attendeesTitle.setText("Max Attendees");
            attendeesContent.setText(String.valueOf(event.getMaxAttendees()));
            attendeesContent.setClickable(true);
            attendeesContent.setFocusableInTouchMode(true);

            descriptionContent.setClickable(true);
            descriptionContent.setFocusableInTouchMode(true);
        }
        else {
            if (eventHost && toolbarMenu != null) {
                editButton.setVisible(true);
                saveButton.setVisible(false);
            }

            dateContent.setClickable(false);
            dateContent.setFocusable(false);

            timeContent.setClickable(false);
            timeContent.setFocusable(false);

            attendeesTitle.setText("Attendees");
            attendeesContent.setClickable(false);
            attendeesContent.setFocusable(false);
            updateAttendees();

            descriptionContent.setClickable(false);
            descriptionContent.setFocusable(false);
        }
    }

    private void updateToolbarMenu() {
        if (toolbarMenu == null) {
            return;
        }

        joinButton.setVisible(false);
        leaveButton.setVisible(false);
        editButton.setVisible(false);
        saveButton.setVisible(false);

        if (event != null && user != null) {
            if (eventHost) {
                editButton.setVisible(true);
            }
            else if (event.isUserAttending(user.getUid())) {
                leaveButton.setVisible(true);
                setEditable(false);
            }
            else {
                joinButton.setVisible(true);
                setEditable(false);
            }
        }
    }

    private void updateAttendees() {
        String max = String.valueOf(event.getMaxAttendees());
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

    private Task<DocumentSnapshot> getFetchUpdatedEventTask() {
        modified = true;

        Task<DocumentSnapshot> task = db.collection("events")
                .document(event.getEventId())
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
                    Log.e("DB", "Failed to get event with ID" + event.getEventId(), e);
                }
            })
            .addOnCanceledListener(new OnCanceledListener() {
                @Override
                public void onCanceled() {
                    Log.w("DB", "Failed to get event with ID " + event.getEventId() + " (task canceled)");
                }
            });

        return task;
    }

    private Task<Void> getUploadEventTask() {
        modified = true;

        Task<Void> task = db.collection("events")
                .document(event.getEventId())
                .set(event);


        task.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e("DB", "Failed to update event with ID " + event.getEventId(), e);
                }
            })
            .addOnCanceledListener(new OnCanceledListener() {
                @Override
                public void onCanceled() {
                    Log.w("DB", "Failed to update event with ID " + event.getEventId() + " (task canceled)");
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
                Toast.makeText(EventViewerActivity.this,
                        "Failed to join event: server error",
                        Toast.LENGTH_SHORT).show();

                updateToolbarMenu();
                updateAttendees();
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
                Log.e("DB", "Failed to leave event: server error", e);
                event.addAttendee(user.getUid());
            }
        });
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
