package group6.spring16.cop4656.floridapoly;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class Login_Activity extends AppCompatActivity {

    private Button loginButton;
    private TextView emailField;
    private TextView passwordField;
    private FirebaseAuth mAuth;

    private String email;
    private String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        loginButton = (Button)findViewById(R.id.loginButton);

        loginButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                selectTextFields();
                getTextFromFields();
                loginUser();
            }
        });

    }

    private void selectTextFields(){
        emailField = (TextView)findViewById(R.id.email);
        passwordField = (TextView)findViewById(R.id.password);
    }

    private void getTextFromFields(){
        email = emailField.getText().toString().trim();
        password = passwordField.getText().toString().trim();
    }

    private void loginUser(){
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success
                            Toast.makeText(Login_Activity.this, "Authentication success",
                                    Toast.LENGTH_LONG).show();
                            Intent homeScreen = new Intent(Login_Activity.this, MainScreen.class);
                            startActivity(homeScreen);
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(Login_Activity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
