package com.example.myapplication;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Activity3 extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ChatAdapter adapter;
    private EditText editTextMessage;
    private OkHttpClient client;
    private Map<String, String> intentResponses;

    private boolean dateSelected = false;
    private boolean cancel = false;

    private boolean performanceSelected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity3);

        recyclerView = findViewById(R.id.recyclerView);
        editTextMessage = findViewById(R.id.editTextMessage);
        Button buttonSend = findViewById(R.id.buttonSend);

        // Ορισμός του layout manager για το RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true); // Εμφάνιση των μηνυμάτων από το κάτω μέρος
        recyclerView.setLayoutManager(layoutManager);

        // Δημιουργία του adapter
        adapter = new ChatAdapter();
        recyclerView.setAdapter(adapter);

        // Αρχικοποίηση του HTTP client
        client = new OkHttpClient();

        // Αρχικοποίηση των intent responses
        initializeIntentResponses();

        String initialBotMessage = "Καλησπέρα σας, είμαι ο εικονικός σας βοηθός. Πώς θα θέλατε να σας εξυπηρετήσω;";
        adapter.addMessage(new Message(initialBotMessage, false));

        buttonSend.setOnClickListener(v -> {
            // Λήψη του μηνύματος που έχει εισαχθεί από τον χρήστη
            String userMessage = editTextMessage.getText().toString().trim();

            if (!userMessage.isEmpty()) {
                // Προσθήκη του μηνύματος του χρήστη στο RecyclerView
                adapter.addMessage(new Message(userMessage, true));
                // Καθαρισμός του EditText
                editTextMessage.setText("");
                // Κύλιση του RecyclerView στο τελευταίο μήνυμα
                recyclerView.smoothScrollToPosition(adapter.getItemCount() - 1);

                // Κλήση της μεθόδου για την απόκριση από το wit.ai
                getBotReply(userMessage);
            }
        });
    }

    private void initializeIntentResponses() {
        intentResponses = new HashMap<>();
        intentResponses.put("booking", "Θέλετε να κάνετε μια κράτηση; Ενημερώστε με για τις λεπτομέρειες.");
        intentResponses.put("cancel", "Επιθυμείτε να ακυρώσετε κάτι; Παρακαλω πειτε μου το ονομα σας προκειμενου να προχωρησω στην ακυρωση.");
        intentResponses.put("complaints", "Ευχαριστουμε,λαμβανουμε υποψη την γνωμη σας.Θα προσπαθησουμε να το φτιαξουμε"+"\n" +
                "Αν μπορούσα να σας εξυπηρετήσω παραπάνω ειμαι στην διαθεση σας.");
        intentResponses.put("info", "Στο θέατρο μας θα βρείτε την παράσταση θέατρο σε δυο παραστάσεις την μέρα μια απογευματινή 17:00 και μια βραδινή 21:00.\n" +
                "\n" + "Αν μπορούσα να σας εξυπηρετήσω παραπάνω ειμαι στην διαθεση σας.");
        intentResponses.put("negative", "Λυπάμαι που το ακούω αυτό. Τι μπορώ να κάνω για να βοηθήσω;");
        intentResponses.put("positive", "Χαίρομαι που το ακούω! Πώς αλλιώς μπορώ να σας βοηθήσω;");
    }

    private void getBotReply(String userMessage) {
        String url = "https://api.wit.ai/message?v=20230315&q=" + Uri.encode(userMessage);
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer NBLMPBSOGBRKKCVS5GC3J2BJREP7W4JD")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String responseBody = response.body().string();
                        JSONObject jsonObject = new JSONObject(responseBody);
                        JSONArray intents = jsonObject.getJSONArray("intents");

                        if (intents.length() > 0) {
                            String intent = intents.getJSONObject(0).getString("name");
                            String botReply = intentResponses.getOrDefault(intent, "Συγγνώμη, δεν καταλαβαίνω.");

                            runOnUiThread(() -> {
                                if (intent.equals("booking")) {
                                    // Εμφάνιση του DatePickerDialog
                                    DialogFragment datePickerFragment = new DatePickerFragment(dateSetListener);
                                    datePickerFragment.show(getSupportFragmentManager(), "datePicker");
                                } else if (intent.equals("cancel")) {
                                    adapter.addMessage(new Message("Επιθυμείτε να ακυρώσετε κάτι; Παρακαλω πειτε μου το κωδικο κρατησης σας προκειμενου να προχωρησω στην ακυρωση.", false));

                                    showUserDetailDialog();

                                } else {
                                    // Προσθήκη της απάντησης του μποτ στο RecyclerView
                                    adapter.addMessage(new Message(botReply, false));
                                    // Κύλιση του RecyclerView στο τελευταίο μήνυμα
                                    recyclerView.smoothScrollToPosition(adapter.getItemCount() - 1);
                                }
                            });
                        } else {
                            runOnUiThread(() -> {
                                // Προσθήκη της απάντησης του μποτ στο RecyclerView
                                adapter.addMessage(new Message("Συγγνώμη, δεν καταλαβαίνω.", false));
                                // Κύλιση του RecyclerView στο τελευταίο μήνυμα
                                recyclerView.smoothScrollToPosition(adapter.getItemCount() - 1);
                            });
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
    private void showUserDetailDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Εισάγετε τον κωδικό κράτησης σας");

        final EditText editTextCode = new EditText(this);
        editTextCode.setHint("Κωδικός Κράτησης");
        builder.setView(editTextCode);

        // Προσθήκη κουμπιών στο AlertDialog
        builder.setPositiveButton("Υποβολή", null);

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String code = editTextCode.getText().toString().trim();

                        // Έλεγχος για τον κωδικό (να ξεκινάει με # και να ακολουθούν 5 αριθμοί)
                        if (!code.matches("^#\\d{5}$")) {
                            editTextCode.setError("Ο κωδικός πρέπει να ξεκινά με # και να έχει 5 αριθμούς");
                        } else {
                            adapter.addMessage(new Message(code, true));

                            // Εμφάνιση εξατομικευμένου μηνύματος ακύρωσης με το όνομα του χρήστη
                            String cancelMessage = "Ευχαριστούμε, η ακύρωση του εισιτηρίου σας με κωδικό " + code + " ολοκληρώθηκε.";
                            adapter.addMessage(new Message(cancelMessage, false));

                            // Κύλιση του RecyclerView στο τελευταίο μήνυμα
                            recyclerView.smoothScrollToPosition(adapter.getItemCount() - 1);
                            dialog.dismiss();
                        }
                    }
                });
            }
        });

        // Εμφάνιση του AlertDialog
        dialog.show();
    }

    private void showPerformanceSelectionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Επιλογή Παράστασης")
                .setMessage("Παρακαλώ επιλέξτε την παράσταση που επιθυμείτε:")
                .setPositiveButton("Θεατρο 17:00", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Προσθήκη μηνύματος στο RecyclerView για την επιλογή "Θεατρο 17:00"
                        adapter.addMessage(new Message("Επιλέξατε την παράσταση: Θεατρο 17:00", false));
                        recyclerView.smoothScrollToPosition(adapter.getItemCount() - 1);

                        // Κλήση της μεθόδου για την επιλογή της τιμής και αριθμού εισιτηρίων
                        showTicketSelectionDialog();
                        performanceSelected = true;
                    }
                })
                .setNegativeButton("Θεατρο 21:00", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Προσθήκη μηνύματος στο RecyclerView για την επιλογή "Θεατρο 21:00"
                        adapter.addMessage(new Message("Επιλέξατε την παράσταση: Θεατρο 21:00", false));
                        recyclerView.smoothScrollToPosition(adapter.getItemCount() - 1);

                        // Κλήση της μεθόδου για την επιλογή της τιμής και αριθμού εισιτηρίων
                        showTicketSelectionDialog();
                        performanceSelected = true;
                    }
                })
                .setCancelable(false)
                .show();
    }

    private void showTicketSelectionDialog() {
        // Δημιουργία του AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Επιλογή Τιμής και Αριθμού Εισιτηρίων");
        builder.setMessage("Παρακαλώ επιλέξτε την τιμή της παράστασης και τον αριθμό εισιτηρίων:");

        // Δημιουργία ενός LinearLayout για να περιέχει τα EditTexts
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        // Δημιουργία πεδίου κειμένου (EditText) για κάθε κατηγορία τιμής
        final EditText editTextPrice24 = new EditText(this);
        editTextPrice24.setHint("Αριθμός εισιτηρίων για 24€");
        layout.addView(editTextPrice24);

        final EditText editTextPrice35 = new EditText(this);
        editTextPrice35.setHint("Αριθμός εισιτηρίων για 35€");
        layout.addView(editTextPrice35);

        final EditText editTextPrice45 = new EditText(this);
        editTextPrice45.setHint("Αριθμός εισιτηρίων για 45€");
        layout.addView(editTextPrice45);

        // Προσθήκη του LinearLayout στο AlertDialog
        builder.setView(layout);

        // Προσθήκη κουμπιών στο AlertDialog
        builder.setPositiveButton("Επιβεβαίωση", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Αν ο χρήστης κάνει κλικ στο κουμπί "Επιβεβαίωση", εδώ θα πρέπει να επεξεργαστείς την είσοδο
                String numTickets24 = editTextPrice24.getText().toString();
                String numTickets35 = editTextPrice35.getText().toString();
                String numTickets45 = editTextPrice45.getText().toString();
                int n24 = Integer.parseInt(numTickets24);
                int n35 = Integer.parseInt(numTickets35);
                int n45 = Integer.parseInt(numTickets45);
                // Εμφάνιση μηνύματος με τις επιλογές του χρήστη
                String message = "Επιλέξατε:\n" +
                        "24€: " + numTickets24 + " εισιτήρια\n" +
                        "35€: " + numTickets35 + " εισιτήρια\n" +
                        "45€: " + numTickets45 + " εισιτήρια";

                // Προσθήκη του μηνύματος στο RecyclerView
                adapter.addMessage(new Message(message, false));
                recyclerView.smoothScrollToPosition(adapter.getItemCount() - 1);

                // Εμφάνιση του διαλόγου εισαγωγής στοιχείων για κάθε εισιτήριο
                showUserDetailsDialog(n24+n35+n45);
            }
        });

        // Εμφάνιση του AlertDialog
        builder.show();
    }

    private DatePickerDialog.OnDateSetListener dateSetListener = (view, year, month, dayOfMonth) -> {
        // Format the date and send it as a message
        String selectedDate = String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year);
        adapter.addMessage(new Message("Επιλέξατε την ημερομηνία: " + selectedDate, false));
        recyclerView.smoothScrollToPosition(adapter.getItemCount() - 1);

        // Εμφάνιση AlertDialog για την επιλογή παραστάσεων
        if (!dateSelected) {
            showPerformanceSelectionDialog();
            dateSelected = true;
        }
    };
    private void showUserDetailsDialog(int totalTickets) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Στοιχεία Εισιτηρίων");

        // Δημιουργία ενός LinearLayout για να περιέχει τα EditTexts
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        // Δημιουργία πεδίου κειμένου για όνομα
        final EditText editTextName = new EditText(this);
        editTextName.setHint("Όνοματεπώνυμο");
        layout.addView(editTextName);

        // Δημιουργία πεδίου κειμένου για τηλέφωνο
        final EditText editTextPhone = new EditText(this);
        editTextPhone.setHint("Τηλέφωνο");
        layout.addView(editTextPhone);

        // Δημιουργία πεδίου κειμένου για email
        final EditText editTextEmail = new EditText(this);
        editTextEmail.setHint("Email");
        layout.addView(editTextEmail);

        // Προσθήκη του LinearLayout στο AlertDialog
        builder.setView(layout);

        // Προσθήκη κουμπιών στο AlertDialog
        builder.setPositiveButton("Επιβεβαίωση", null);

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        boolean isValid = true;

                        // Λήψη των τιμών από τα EditTexts
                        String name = editTextName.getText().toString().trim();
                        String phone = editTextPhone.getText().toString().trim();
                        String email = editTextEmail.getText().toString().trim();

                        // Έλεγχος για το όνομα (να περιέχει δύο λέξεις και να μην έχει αριθμούς)
                        if (name.isEmpty()) {
                            editTextName.setError("Παρακαλώ εισάγετε το όνομά σας");
                            isValid = false;
                        } else if (!name.matches("^[a-zA-Zα-ωΑ-Ω]+(\\s[a-zA-Zα-ωΑ-Ω]+)+$")) {
                            editTextName.setError("Το όνομα πρέπει να περιέχει δύο λέξεις και να μην έχει αριθμούς");
                            isValid = false;
                        }

                        // Έλεγχος για το τηλέφωνο (να περιέχει 10 ψηφία)
                        if (phone.isEmpty()) {
                            editTextPhone.setError("Παρακαλώ εισάγετε τον αριθμό τηλεφώνου σας");
                            isValid = false;
                        } else if (!phone.matches("\\d{10}")) {
                            editTextPhone.setError("Ο αριθμός τηλεφώνου πρέπει να έχει 10 ψηφία");
                            isValid = false;
                        }

                        // Έλεγχος για το email (να είναι έγκυρη διεύθυνση email)
                        if (email.isEmpty()) {
                            editTextEmail.setError("Παρακαλώ εισάγετε το email σας");
                            isValid = false;
                        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                            editTextEmail.setError("Παρακαλώ εισάγετε μια έγκυρη διεύθυνση email");
                            isValid = false;
                        }

                        if (isValid) {
                            // Δημιουργία μηνύματος για όλα τα εισιτήρια
                            StringBuilder messageBuilder = new StringBuilder();
                            for (int i = 1; i <= totalTickets; i++) {
                                messageBuilder.append("Εισιτήριο #").append(i).append(":\n")
                                        .append("Όνομα: ").append(name).append("\n")
                                        .append("Τηλέφωνο: ").append(phone).append("\n")
                                        .append("Email: ").append(email).append("\n\n");
                            }

                            String message = messageBuilder.toString();

                            // Προσθήκη του μηνύματος στο RecyclerView
                            adapter.addMessage(new Message(message, false));
                            recyclerView.smoothScrollToPosition(adapter.getItemCount() - 1);
                            dialog.dismiss();
                        }
                    }
                });
            }
        });

        // Εμφάνιση του AlertDialog
        dialog.show();
    }

}