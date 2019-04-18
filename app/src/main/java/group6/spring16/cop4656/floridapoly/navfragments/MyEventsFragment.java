package group6.spring16.cop4656.floridapoly.navfragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import group6.spring16.cop4656.floridapoly.R;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MyEventsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MyEventsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MyEventsFragment extends Fragment {

    private OnFragmentInteractionListener mListener;

    // User data
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser mUser;
    private TextView username;

    private FloatingActionButton eventsFab;

    public MyEventsFragment() {
        // Required empty public constructor
    }

    public static MyEventsFragment newInstance() {
        return new MyEventsFragment();
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
        View view = inflater.inflate(R.layout.fragment_my_events, container, false);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        mUser = mAuth.getCurrentUser();

        username = view.findViewById(R.id.username);

        if (mUser != null) {
            username.setText(mUser.getEmail());
        }

        // Get the FAB
        eventsFab = view.findViewById(R.id.my_events_fab);

        // Create the tab bar and view pager
        TabLayout tabLayout = view.findViewById(R.id.my_events_tab_bar);
        final ViewPager viewPager = view.findViewById(R.id.my_events_view_pager);

        final EventPagerAdapter adapter = new EventPagerAdapter(getChildFragmentManager());

        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);

        // Add a page change listener for changing the events FAB
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {
            }

            @Override
            public void onPageSelected(int i) {
            }

            @Override
            public void onPageScrollStateChanged(int i) {
                if (i == ViewPager.SCROLL_STATE_DRAGGING) {
                    eventsFab.hide();
                }
                else if (i == ViewPager.SCROLL_STATE_IDLE) {
                    switch (viewPager.getCurrentItem()) {
                        case 0:
                            adapter.attendingFrag.shareFab(null);
                            adapter.hostingFrag.shareFab(eventsFab);
                            break;
                        case 1:
                        default:
                            adapter.hostingFrag.shareFab(null);
                            adapter.attendingFrag.shareFab(eventsFab);
                            break;
                    }

                    eventsFab.show();
                }
            }
        });

        return view;
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

    public class EventPagerAdapter extends FragmentPagerAdapter {
        public HostingEventsFragment hostingFrag = HostingEventsFragment.newInstance();
        public AttendingEventsFragment attendingFrag = AttendingEventsFragment.newInstance();

        public EventPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return hostingFrag;
                case 1:
                default:
                    return attendingFrag;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Hosting";
                case 1:
                default:
                    return "Attending";
            }
        }
    }
}
