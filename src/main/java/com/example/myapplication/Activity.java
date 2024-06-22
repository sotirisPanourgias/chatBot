package com.example.myapplication;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Activity  extends AppCompatActivity  {
    private EditText editTextName, editTextID;
    private Button buttonSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonSubmit=findViewById(R.id.buttonSubmit);

        buttonSubmit.setOnClickListener(v -> {
            Intent intent = new Intent(Activity.this, Activity2.class);
            startActivity(intent);

        });
    }
}


