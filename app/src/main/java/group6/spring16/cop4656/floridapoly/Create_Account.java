package group6.spring16.cop4656.floridapoly;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Create_Account extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private Button signUpButton;
    private TextView firstNameField;
    private TextView lastNameField;
    private TextView userEmailField;
    private TextView passwordField;
    private TextView confirmPasswordField;

    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String confirmPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_account_activity);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        firstNameField = findViewById(R.id.firstName);
        lastNameField  = findViewById(R.id.lastName);
        userEmailField = findViewById(R.id.email);
        passwordField  = findViewById(R.id.password);
        confirmPasswordField = findViewById(R.id.confirmPassword);
        signUpButton   = findViewById(R.id.signUpButton);

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getTextFromFields();

                if (!firstName.isEmpty() && !lastName.isEmpty() &&
                    !email.isEmpty() && !password.isEmpty() && !confirmPassword.isEmpty()) {
                    if (password.equals(confirmPassword)){
                        createUser();

                    }
                    else {
                        Toast.makeText(Create_Account.this,
                                "Password fields do not match",
                                Toast.LENGTH_SHORT).show();
                    }
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
        confirmPassword = confirmPasswordField.getText().toString().trim();
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
                            FirebaseUser user = mAuth.getCurrentUser();
                            addUserToDB(user.getUid());
                            Intent homeScreen = new Intent(Create_Account.this, MainScreen.class);
                            startActivity(homeScreen);

                        }else{
                            // If sign in fails, display a message to the user.
                            Log.w("User", "createUserWithEmail:failure", task.getException());
                            Toast.makeText(Create_Account.this, "Account Creation Failed",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void addUserToDB(String userId){
        Map<String, Object> user = new HashMap<>();
        user.put("first", firstName);
        user.put("last", lastName);
        db.collection("users")
                .document(userId)
                .set(user);
    }

}
