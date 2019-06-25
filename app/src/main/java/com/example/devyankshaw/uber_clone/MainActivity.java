package com.example.devyankshaw.uber_clone;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    @Override
    public void onClick(View v) {

        if(edtDriverPassenger.getText().toString().equals("Driver") || edtDriverPassenger.getText().toString().equals("Passenger")){

            if(ParseUser.getCurrentUser() != null){//if there are no users logged in then creates a anonymous user
                ParseAnonymousUtils.logIn(new LogInCallback() {
                    @Override
                    public void done(ParseUser user, ParseException e) {
                        if (user != null && e == null){
                            Toast.makeText(MainActivity.this, "We have an anonymous user", Toast.LENGTH_LONG).show();

                            user.put("as", edtDriverPassenger.getText().toString());

                            user.saveInBackground(new SaveCallback() {//When saveInBackground process is finished then done() is executed
                                @Override
                                public void done(ParseException e) {
                                    transitionToPassengerActivity();
                                }
                            });//must have to call saveInBackground() otherwise the value of edtDriverPassenger will not inserted/save on "as" column
                        }
                    }
                });
            }
        }else{
            Toast.makeText(this, "Are you a Driver or Passenger?", Toast.LENGTH_LONG).show();
        }
    }

    //This enum is used to declare our own data type whose variable is State which holds only two values SIGNUP and LOGIN
    enum State{
        SIGNUP, LOGIN;
    }

    private State state;//Declare to initialise the state
    private Button btnSignUpLogIn, btnOneTimeLogIn;
    private RadioButton rdbPassenger,rdbDriver;
    private EditText edtUsername, edtPassword, edtDriverPassenger;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Save the current Installation to Back4App
        ParseInstallation.getCurrentInstallation().saveInBackground();
        //If there is a user who is either logged in or signed up then below method is executed
        if(ParseUser.getCurrentUser() != null){
            transitionToPassengerActivity();
        }

        //Initialising the state to Signup at the initial
        state = State.SIGNUP;

        btnSignUpLogIn = findViewById(R.id.btnSignUpLogIn);
        btnOneTimeLogIn = findViewById(R.id.btnOneTimeLogin);

        rdbDriver = findViewById(R.id.rdbDriver);
        rdbPassenger = findViewById(R.id.rdbPassenger);

        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        edtDriverPassenger = findViewById(R.id.edtDriverPassenger);

        btnOneTimeLogIn.setOnClickListener(MainActivity.this);

        btnSignUpLogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                        if (state == State.SIGNUP) {

                            if (rdbDriver.isChecked() == false && rdbPassenger.isChecked() == false) {
                                Toast.makeText(MainActivity.this, "Are you a driver or passenger?", Toast.LENGTH_SHORT).show();
                                return;//This return statement means do not execute the codes after this if statement
                            }
                            ParseUser appUser = new ParseUser();
                            appUser.setUsername(edtUsername.getText().toString());
                            appUser.setPassword(edtPassword.getText().toString());
                            if (rdbDriver.isChecked()) {
                                appUser.put("as", "Driver");//if radio button of driver is checked then put "Driver" in "as" column
                            } else if (rdbPassenger.isChecked()) {
                                appUser.put("as", "Passenger");
                            }
                            appUser.signUpInBackground(new SignUpCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if (e == null) {//If there is no error then show a toast message
                                        Toast.makeText(MainActivity.this, "Signed Up", Toast.LENGTH_LONG).show();
                                        transitionToPassengerActivity();
                                    }
                                }
                            });
                        } else if (state == State.LOGIN) {

                            ParseUser.logInInBackground(edtUsername.getText().toString(), edtPassword.getText().toString(), new LogInCallback() {
                                @Override
                                public void done(ParseUser user, ParseException e) {
                                    if (user != null && e == null) {//If there is a user and there is no error then show a toast message
                                        Toast.makeText(MainActivity.this, "Logged In", Toast.LENGTH_LONG).show();
                                        transitionToPassengerActivity();
                                    }
                                }
                            });
                        }
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_signup_activity, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()){

            case R.id.loginItem:

                if(state == State.SIGNUP){
                    state = State.LOGIN;
                    item.setTitle("Sign Up");
                    btnSignUpLogIn.setText("Log In");
                }else if(state == State.LOGIN){
                    state = State.SIGNUP;
                    item.setTitle("Log In");
                    btnSignUpLogIn.setText("Sign Up");
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void transitionToPassengerActivity(){

        if(ParseUser.getCurrentUser() != null){
            if(ParseUser.getCurrentUser().get("as").equals("Passenger")){

                Intent intent = new Intent(MainActivity.this, PassengerActivity.class);
                startActivity(intent);
            }
        }
    }
}
