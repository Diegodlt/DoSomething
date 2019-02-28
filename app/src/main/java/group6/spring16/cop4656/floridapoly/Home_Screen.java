package group6.spring16.cop4656.floridapoly;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

public class Home_Screen extends AppCompatActivity {

    Button signOutButton;
    TextView username;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);

        signOutButton = (Button)findViewById(R.id.signOutButton);
        mAuth = FirebaseAuth.getInstance();
        username = findViewById(R.id.username);

        username.setText(mAuth.getCurrentUser().getEmail());

        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                Intent mainScreen = new Intent(Home_Screen.this, MainActivity.class);
                startActivity(mainScreen);
            }
        });
    }
}
