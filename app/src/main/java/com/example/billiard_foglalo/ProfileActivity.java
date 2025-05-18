package com.example.billiard_foglalo;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.*;

public class ProfileActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private TextView emailTextView;
    private LinearLayout reservationsContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        emailTextView = findViewById(R.id.emailTextView);
        reservationsContainer = findViewById(R.id.reservationsContainer);

        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        emailTextView.setText(auth.getCurrentUser().getEmail());

        loadUserReservations();
    }

    private void loadUserReservations() {
        String userId = auth.getCurrentUser().getUid();

        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(userDoc -> {
                    if (!userDoc.exists()) {
                        Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    List<String> reservationIds = (List<String>) userDoc.get("reservations");
                    if (reservationIds == null || reservationIds.isEmpty()) {
                        reservationsContainer.removeAllViews();
                        TextView tv = new TextView(this);
                        tv.setText("Nincsenek foglalásaid.");
                        reservationsContainer.addView(tv);
                        return;
                    }

                    fetchReservationsByIds(reservationIds);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load reservations: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void fetchReservationsByIds(List<String> reservationIds) {
        reservationsContainer.removeAllViews();

        Date now = new Date();

        for (String resId : reservationIds) {
            db.collection("reservations").document(resId)
                    .get()
                    .addOnSuccessListener(resDoc -> {
                        if (!resDoc.exists()) return;

                        Date resDate = resDoc.getDate("date");
                        String timeSlot = resDoc.getString("timeSlot");
                        String gameType = resDoc.getString("gameType");
                        String tableNumber = resDoc.getString("tableNumber");


                        View reservationView = LayoutInflater.from(this).inflate(R.layout.item_reservation, reservationsContainer, false);

                        TextView infoText = reservationView.findViewById(R.id.reservationInfo);
                        infoText.setText(String.format(Locale.getDefault(),
                                "%1$td-%1$tm-%1$tY | %2$s | %3$s | Asztal: %4$s",
                                resDate, timeSlot, gameType, tableNumber));

                        Button btnModify = reservationView.findViewById(R.id.btnModify);
                        Button btnDelete = reservationView.findViewById(R.id.btnDelete);

                        btnModify.setOnClickListener(v -> showModifyDialog(resId, resDate, timeSlot, gameType, tableNumber));
                        btnDelete.setOnClickListener(v -> showDeleteConfirmation(resId));

                        boolean isFuture = resDate != null && resDate.after(now);

                        if (isFuture) {
                            btnModify.setVisibility(View.VISIBLE);
                            btnDelete.setVisibility(View.VISIBLE);
                        } else {
                            btnModify.setVisibility(View.GONE);
                            btnDelete.setVisibility(View.GONE);
                        }


                        reservationsContainer.addView(reservationView);

                    })
                    .addOnFailureListener(e -> Log.e("ProfileActivity", "Failed to get reservation: " + e.getMessage()));
        }
    }

    private void showDeleteConfirmation(String reservationId) {
        new AlertDialog.Builder(this)
                .setTitle("Foglalás törlése")
                .setMessage("Biztosan törölni szeretnéd ezt a foglalást?")
                .setPositiveButton("Igen", (dialog, which) -> deleteReservation(reservationId))
                .setNegativeButton("Mégse", null)
                .show();
    }

    private void deleteReservation(String reservationId) {
        String userId = auth.getCurrentUser().getUid();

        // Delete reservation document
        db.collection("reservations").document(reservationId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    // Remove reservationId from user's reservations array
                    db.collection("users").document(userId)
                            .update("reservations", FieldValue.arrayRemove(reservationId))
                            .addOnSuccessListener(aVoid1 -> {
                                Toast.makeText(this, "Foglalás törölve", Toast.LENGTH_SHORT).show();
                                loadUserReservations();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Hiba a foglalás törlése közben: " + e.getMessage(), Toast.LENGTH_LONG).show()
                            );
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Nem sikerült törölni a foglalást: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void showModifyDialog(String reservationId, Date date, String currentTimeSlot, String currentGameType, String currentTableNumber) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_modify_reservation, null);
        Spinner spinnerTimeSlot = dialogView.findViewById(R.id.spinnerTimeSlot);
        Spinner spinnerGameType = dialogView.findViewById(R.id.spinnerGameType);
        Spinner spinnerTableNumber = dialogView.findViewById(R.id.spinnerTableNumber);

        // Setup adapters (you can replace with your resources)
        ArrayAdapter<CharSequence> timeSlotAdapter = ArrayAdapter.createFromResource(this,
                R.array.time_slots, android.R.layout.simple_spinner_item);
        timeSlotAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTimeSlot.setAdapter(timeSlotAdapter);

        ArrayAdapter<CharSequence> gameTypeAdapter = ArrayAdapter.createFromResource(this,
                R.array.game_types, android.R.layout.simple_spinner_item);
        gameTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGameType.setAdapter(gameTypeAdapter);

        ArrayAdapter<CharSequence> tableAdapter = ArrayAdapter.createFromResource(this,
                R.array.table_numbers, android.R.layout.simple_spinner_item);
        tableAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTableNumber.setAdapter(tableAdapter);

        // Set current values as selected
        setSpinnerSelection(spinnerTimeSlot, currentTimeSlot);
        setSpinnerSelection(spinnerGameType, currentGameType);
        setSpinnerSelection(spinnerTableNumber, currentTableNumber);

        new AlertDialog.Builder(this)
                .setTitle("Foglalás módosítása")
                .setView(dialogView)
                .setPositiveButton("Mentés", (dialog, which) -> {
                    String newTimeSlot = spinnerTimeSlot.getSelectedItem().toString();
                    String newGameType = spinnerGameType.getSelectedItem().toString();
                    String newTableNumber = spinnerTableNumber.getSelectedItem().toString();

                    updateReservation(reservationId, date, newTimeSlot, newGameType, newTableNumber);
                })
                .setNegativeButton("Mégse", null)
                .show();
    }

    private void setSpinnerSelection(Spinner spinner, String value) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equals(value)) {
                spinner.setSelection(i);
                break;
            }
        }
    }

    private void updateReservation(String reservationId, Date date, String timeSlot, String gameType, String tableNumber) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("timeSlot", timeSlot);
        updates.put("gameType", gameType);
        updates.put("tableNumber", tableNumber);
        updates.put("date", date);

        db.collection("reservations").document(reservationId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Foglalás frissítve", Toast.LENGTH_SHORT).show();
                    loadUserReservations();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Hiba a foglalás frissítése közben: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }
}
