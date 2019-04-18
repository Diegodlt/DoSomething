package group6.spring16.cop4656.floridapoly.navfragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
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
    private StorageReference userStorageRef;
    private StorageReference pictureReference;
    private TextView username;
    private String userValue;

    // Widgets
    private CircleImageView profilePicture;
    private TextView userNameTextView;

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

    @SuppressLint("CutPasteId")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_my_events, container, false);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        mUser = mAuth.getCurrentUser();
        userStorageRef = FirebaseStorage.getInstance().getReference();
        assert mUser != null;
        userValue = mUser.getUid();

        pictureReference = userStorageRef.child("users/" +
                Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid() + "/" + "profilePicture");

        profilePicture = view.findViewById(R.id.profilePicture);
        userNameTextView = view.findViewById(R.id.userNameTextView);

        try {
            downloadUserInfo();
        } catch (IOException e) {
            e.printStackTrace();
        }

        username = view.findViewById(R.id.userNameTextView);

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
        adapter.hostingFrag.shareFab(eventsFab);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {
            }

            @Override
            public void onPageSelected(int i) {
                switch (i) {
                    case 0:
                        adapter.hostingFrag.shareFab(eventsFab);
                        eventsFab.show();
                    default:
                        eventsFab.hide();
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {
                if (i == ViewPager.SCROLL_STATE_DRAGGING) {
                    eventsFab.hide();
                }
                else if (i == ViewPager.SCROLL_STATE_IDLE) {
                    switch (viewPager.getCurrentItem()) {
                        case 0:
                            adapter.hostingFrag.shareFab(eventsFab);
                            eventsFab.show();
                            break;
                        default:
                            adapter.hostingFrag.shareFab(null);
                            break;
                    }
                }
            }
        });

        return view;
    }

    public void downloadUserInfo() throws IOException {
        final File userImage = File.createTempFile("image", "jpg");
        pictureReference.getFile(userImage).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {

                String userImagePath = userImage.getPath();
                Bitmap userImageBitmap = BitmapFactory.decodeFile(userImagePath);
                profilePicture.setImageBitmap(userImageBitmap);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.i("DownloadImage", "No image found");
            }
        });

        db.collection("users").document(userValue).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot document = task.getResult();
                assert document != null;
                String userName = document.getString("User Name");
                userNameTextView.setText(userName);
                userNameTextView.setVisibility(View.VISIBLE);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.i("Download user id", "No user id found");
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
