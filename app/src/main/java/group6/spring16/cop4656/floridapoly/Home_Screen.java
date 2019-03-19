package group6.spring16.cop4656.floridapoly;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.transition.Transition;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.dynamic.SupportFragmentWrapper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Home_Screen extends AppCompatActivity {

    private FragmentManager fragmentManager;

    private BottomNavigationView navBar;

    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private Button signOutButton;
    private TextView username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);

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

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();

        signOutButton = findViewById(R.id.signOutButton);
        username = findViewById(R.id.username);

        if (mUser != null) {
            username.setText(mUser.getEmail());
        }

        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                Intent mainScreen = new Intent(Home_Screen.this, MainActivity.class);
                startActivity(mainScreen);
            }
        });
    }

    private void startHomeScreenFragment() {
        Toast.makeText(this, "Home Screen Placeholder", Toast.LENGTH_SHORT).show();
    }

    private void startDiscoverFragment() {
        Toast.makeText(this, "Discover Screen Placeholder", Toast.LENGTH_SHORT).show();

    }

    private void startEventCreatorFragment() {
        Toast.makeText(this, "Event Creator Screen Placeholder", Toast.LENGTH_SHORT).show();

    }

    private void startSettingsFragment() {
        Toast.makeText(this, "Settings Screen Placeholder", Toast.LENGTH_SHORT).show();

    }

    private void openFragment(Fragment frag) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, frag);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
