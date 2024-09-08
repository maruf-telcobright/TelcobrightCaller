package com.example.telcobrightcaller;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {

    private EditText phoneNumberInput;
    private Button startCallButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        phoneNumberInput = findViewById(R.id.phoneNumberInput);
        startCallButton = findViewById(R.id.startCall);

        startCallButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startCall();
            }
        });
    }

    private void startCall() {
        String phoneNumber = phoneNumberInput.getText().toString();
        if (TextUtils.isEmpty(phoneNumber)) {
            Toast.makeText(this, "Please enter a phone number", Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent(getApplicationContext(), OutgoingCall.class);
            intent.putExtra("receiverNumber", phoneNumber);
            intent.putExtra("contactName", "Remon");
            startActivity(intent);
        }
    }
}