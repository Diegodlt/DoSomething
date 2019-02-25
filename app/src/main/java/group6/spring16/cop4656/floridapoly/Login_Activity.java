package group6.spring16.cop4656.floridapoly;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class Login_Activity extends AppCompatActivity {

    // URL for server
    final String url = "https://apis-diegodltl.c9users.io/";
    Button loginButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        getMessage();
        loginButton = (Button)findViewById(R.id.loginButton);

        loginButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
//                Intent mapIntent = new Intent(Login_Activity.this, EventsActivity.class);
//                startActivity(mapIntent);
            }
        });


    }

    private void getMessage(){
        RequestParams params = new RequestParams();

        AsyncHttpClient client = new AsyncHttpClient();

        client.get(url, null, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response){
                Toast.makeText(Login_Activity.this, response.toString(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject response){
                Toast.makeText(Login_Activity.this, statusCode, Toast.LENGTH_LONG).show();
            }
        });
    }
}
