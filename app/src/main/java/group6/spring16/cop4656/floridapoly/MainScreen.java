package group6.spring16.cop4656.floridapoly;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

public class MainScreen extends AppCompatActivity
    implements HomeFragment.OnFragmentInteractionListener,
               DiscoverFragment.OnFragmentInteractionListener,
               EventCreatorFragment.OnFragmentInteractionListener,
               SettingsFragment.OnFragmentInteractionListener {

    private static final String BACK_STACK_ROOT_TAG = "root_frag";

    private FragmentManager fragmentManager;
    private BottomNavigationView navBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);

        fragmentManager = getSupportFragmentManager();

        navBar = findViewById(R.id.navigation_bar);
        navBar.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {

            // Returns true if the item should be shown as selected, false if not
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                if (menuItem.getItemId() == navBar.getSelectedItemId()) {
                    return true; //don't do anything if the selected item is the current item
                }

                switch (menuItem.getItemId()) {
                    case R.id.nav_item_home_screen:
                        startHomeScreenFragment();
                        return true;

                    case R.id.nav_item_discover:
                        startDiscoverFragment();
                        return true;

                    case R.id.nav_item_event_creator:
                        startEventCreatorFragment();
                        return true;

                    case R.id.nav_item_settings:
                        startSettingsFragment();
                        return true;

                    default:
                        return false;
                }
            }
        });

        // Start the default fragment (Home)
        Fragment frag = fragmentManager.findFragmentById(R.id.fragment_container);
        if (frag == null) {
            startHomeScreenFragment();
        }
    }

    private void startHomeScreenFragment() {
        HomeFragment frag = HomeFragment.newInstance();
        openTab(frag);
    }

    private void startDiscoverFragment() {
        DiscoverFragment frag = DiscoverFragment.newInstance();
        openTab(frag);
    }

    private void startEventCreatorFragment() {
        EventCreatorFragment frag = EventCreatorFragment.newInstance();
        openTab(frag);
    }

    private void startSettingsFragment() {
        SettingsFragment frag = SettingsFragment.newInstance();
        openTab(frag);
    }

    private void openTab(Fragment frag) {
        fragmentManager.popBackStack(BACK_STACK_ROOT_TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE);

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, frag);
        transaction.addToBackStack(BACK_STACK_ROOT_TAG);
        transaction.commit();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
        // Handle fragment interaction
        // https://developer.android.com/training/basics/fragments/communicating.html
    }
}
