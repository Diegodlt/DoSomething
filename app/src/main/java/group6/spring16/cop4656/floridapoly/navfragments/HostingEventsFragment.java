package group6.spring16.cop4656.floridapoly.navfragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import group6.spring16.cop4656.floridapoly.EventRecyclerAdapter;
import group6.spring16.cop4656.floridapoly.EventRecyclerTouchListener;
import group6.spring16.cop4656.floridapoly.MainScreen;
import group6.spring16.cop4656.floridapoly.R;
import group6.spring16.cop4656.floridapoly.event.Event;
import group6.spring16.cop4656.floridapoly.event.EventViewerActivity;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link HostingEventsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link HostingEventsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HostingEventsFragment extends Fragment {

    private OnFragmentInteractionListener mListener;

    // User data
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser mUser;

    private RecyclerView hostingEventsView;
    private LinearLayoutManager hostingLayoutManager;
    private EventRecyclerAdapter hostingAdapter;
    private List<Event> hostingEvents = new ArrayList<>();

    private FloatingActionButton sharedFab;

    public HostingEventsFragment() {
        // Required empty public constructor
    }


    public static HostingEventsFragment newInstance() {
        HostingEventsFragment fragment = new HostingEventsFragment();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_hosting_events, container, false);

        // Get firebase data
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        mUser = mAuth.getCurrentUser();

        // Get event recycler view
        View hostingView = view.findViewById(R.id.hosting_events_recycler_view);

        // Get the event RecyclerView
        hostingEventsView = hostingView.findViewById(R.id.recycler_view);

        // Set the event view's layout manager
        hostingLayoutManager = new LinearLayoutManager(getActivity());
        hostingEventsView.setLayoutManager(hostingLayoutManager);

        // Create and set the adapters for the event view
        hostingAdapter = new EventRecyclerAdapter(hostingEvents);
        hostingEventsView.setAdapter(hostingAdapter);

        // Set the event view's touch listener
        EventRecyclerTouchListener hostingTouchListener = new EventRecyclerTouchListener(getActivity(), hostingEventsView);
        hostingTouchListener.setClickListener(new EventRecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                Intent intent = new Intent(getActivity(), EventViewerActivity.class);
                intent.putExtra(EventViewerActivity.EXTRA_EVENT, hostingEvents.get(position));
                startActivity(intent);
            }
        });

        hostingEventsView.addOnItemTouchListener(hostingTouchListener);

        // Fetch events from the database and update the adapters
        updateEvents();

        return view;
    }

    public void shareFab(FloatingActionButton fab) {
        if (fab == null) {
            if (sharedFab != null) {
                sharedFab.setOnClickListener(null);
            }
            sharedFab = null;
        }
        else {
            sharedFab = fab;
            sharedFab.setImageResource(R.drawable.ic_add_white_24dp);
            sharedFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getActivity(), EventViewerActivity.class);
                    startActivity(intent);
                }
            });
        }
    }

    private void updateEvents() {
        db.collection("users")
                .document(mUser.getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        final ArrayList<String> hostingIds = (ArrayList<String>)documentSnapshot.get("hosting");

                        // Add events
                        if (hostingIds != null) {
                            fetchEvents(hostingIds, hostingEvents, hostingAdapter);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("DB", "Failed to get data for user with ID: " + mUser.getUid(), e);
                    }
                });
    }

    private void fetchEvents(@NonNull final List<String> ids, @NonNull final List<Event> events, @NonNull final EventRecyclerAdapter adapter) {
        for (final String id : ids) {
            db.collection("events")
                    .document(id)
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            Event event = documentSnapshot.toObject(Event.class);
                            if (event != null) {
                                events.add(event);
                                adapter.notifyDataSetChanged();
                            }
                        }
                    });
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
