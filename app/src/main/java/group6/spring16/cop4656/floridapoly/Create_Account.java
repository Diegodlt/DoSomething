package group6.spring16.cop4656.floridapoly;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Create_Account extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private Button signUpButton;
    private TextView firstNameField;
    private TextView lastNameField;
    private TextView userEmailField;
    private TextView passwordField;

    private String firstName;
    private String lastName;
    private String email;
    private String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create__account_activity);

        mAuth = FirebaseAuth.getInstance();


        firstNameField = findViewById(R.id.firstName);
        lastNameField  = findViewById(R.id.lastName);
        userEmailField = findViewById(R.id.email);
        passwordField  = findViewById(R.id.password);
        signUpButton   = findViewById(R.id.signUpButton);

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getTextFromFields();

                if (!firstName.isEmpty() && !lastName.isEmpty() &&
                    !email.isEmpty() && !password.isEmpty()) {
                    createUser();
                }
                else {
                    Toast.makeText(Create_Account.this,
                            "Please fill out every form first",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void getTextFromFields(){
        firstName = firstNameField.getText().toString().trim();
        lastName  = lastNameField.getText().toString().trim();
        email     = userEmailField.getText().toString().trim();
        password  = passwordField.getText().toString().trim();
    }

    private void createUser(){
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(Create_Account.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Log.d("User", "createUserWithEmail:success");
                            Toast.makeText(Create_Account.this, "Account Creation Success",
                                    Toast.LENGTH_LONG).show();
                        }else{
                            // If sign in fails, display a message to the user.
                            Log.w("User", "createUserWithEmail:failure", task.getException());
                            Toast.makeText(Create_Account.this, "Account Creation Failed",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

}
