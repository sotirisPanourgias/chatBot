package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Activity2 extends AppCompatActivity {
    private EditText editTextName;
    private Button buttonSubmit;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity2);
        editTextName = findViewById(R.id.editTextName);
        buttonSubmit = findViewById(R.id.buttonSubmit);

        buttonSubmit.setOnClickListener(v -> {
            String name = editTextName.getText().toString().trim();
            //String id = editTextID.getText().toString().trim();

            // Εμφάνιση μηνύματος με τα στοιχεία που δόθηκαν
            Toast.makeText(Activity2.this, "Όνομα: " + name , Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(Activity2.this, Activity3.class);
            startActivity(intent);
    });}
    }
