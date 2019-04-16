package group6.spring16.cop4656.floridapoly.navfragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.card.MaterialCardView;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import group6.spring16.cop4656.floridapoly.EventRecyclerAdapter;
import group6.spring16.cop4656.floridapoly.EventRecyclerTouchListener;
import group6.spring16.cop4656.floridapoly.MainActivity;
import group6.spring16.cop4656.floridapoly.R;
import group6.spring16.cop4656.floridapoly.event.Event;
import group6.spring16.cop4656.floridapoly.event.EventViewerActivity;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link HomeFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {

    private static final String EXTRA_EVENT = "event";

    private OnFragmentInteractionListener mListener;

    // User data
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser mUser;
    private TextView username;

    private RecyclerView hostingEventsView;
    private LinearLayoutManager hostingLayoutManager;
    private EventRecyclerAdapter hostingAdapter;

    private RecyclerView attendingEventsView;
    private LinearLayoutManager attendingLayoutManager;
    private EventRecyclerAdapter attendingAdapter;

    private List<Event> hostingEvents = new ArrayList<>();
    private List<Event> attendingEvents = new ArrayList<>();

    public HomeFragment() {
        // Required empty public constructor
    }

    public static HomeFragment newInstance() {
        return new HomeFragment();
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
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        mUser = mAuth.getCurrentUser();

        username = view.findViewById(R.id.username);

        if (mUser != null) {
            username.setText(mUser.getEmail());
        }

        // Create the recycler views and populate them
        setupEventRecyclerViews(view);

        return view;
    }

    private void setupEventRecyclerViews(@NonNull View view) {
        View hostingView = view.findViewById(R.id.home_hosting_events);
        View attendingView = view.findViewById(R.id.home_attending_events);

        // Set the titles
        TextView hostingTitle = hostingView.findViewById(R.id.recycler_title);
        TextView attendingTitle = attendingView.findViewById(R.id.recycler_title);
        hostingTitle.setText("Hosting");
        attendingTitle.setText("Attending");

        // Get the event RecyclerViews
        hostingEventsView = hostingView.findViewById(R.id.recycler_view);
        attendingEventsView = attendingView.findViewById(R.id.recycler_view);

        // Set the event view's layout managers
        hostingLayoutManager = new LinearLayoutManager(getActivity());
        attendingLayoutManager = new LinearLayoutManager(getActivity());
        hostingEventsView.setLayoutManager(hostingLayoutManager);
        attendingEventsView.setLayoutManager(attendingLayoutManager);

        // Create and set the adapters for the event views
        hostingAdapter = new EventRecyclerAdapter(hostingEvents);
        attendingAdapter = new EventRecyclerAdapter(attendingEvents);
        hostingEventsView.setAdapter(hostingAdapter);
        attendingEventsView.setAdapter(attendingAdapter);

        // Set the event view's touch listener
        EventRecyclerTouchListener hostingTouchListener = new EventRecyclerTouchListener(getActivity(), hostingEventsView);
        hostingTouchListener.setClickListener(new EventRecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                Intent intent = new Intent(getActivity(), EventViewerActivity.class);
                intent.putExtra(EXTRA_EVENT, hostingEvents.get(position));
                startActivity(intent);
            }
        });

        EventRecyclerTouchListener attendingTouchListener = new EventRecyclerTouchListener(getActivity(), attendingEventsView);
        attendingTouchListener.setClickListener(new EventRecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                Intent intent = new Intent(getActivity(), EventViewerActivity.class);
                intent.putExtra(EXTRA_EVENT, attendingEvents.get(position));
                startActivity(intent);
            }
        });

        hostingEventsView.addOnItemTouchListener(hostingTouchListener);
        attendingEventsView.addOnItemTouchListener(attendingTouchListener);

        db.collection("users")
                .document(mUser.getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        final ArrayList<String> hostingIds = (ArrayList<String>)documentSnapshot.get("hosting");
                        final ArrayList<String> attendingIds = (ArrayList<String>)documentSnapshot.get("attending");

                        // Add hosting events
                        if (hostingIds != null) {
                            for (final String id : hostingIds) {
                                db.collection("events")
                                        .document(id)
                                        .get()
                                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                            @Override
                                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                Event event = documentSnapshot.toObject(Event.class);
                                                if (event != null) {
                                                    hostingEvents.add(event);
                                                    hostingAdapter.notifyDataSetChanged();
                                                }
                                            }
                                        });
                            }
                        }

                        // Add attending events
                        if (attendingIds != null) {
                            for (final String id : attendingIds) {
                                db.collection("events")
                                        .document(id)
                                        .get()
                                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                            @Override
                                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                Event event = documentSnapshot.toObject(Event.class);
                                                if (event != null) {
                                                    attendingEvents.add(event);
                                                    attendingAdapter.notifyDataSetChanged();
                                                }
                                            }
                                        });
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //TODO: log error
                    }
                });
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
