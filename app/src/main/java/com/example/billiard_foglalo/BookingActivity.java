package com.example.billiard_foglalo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//alarm manager, crud
public class BookingActivity extends AppCompatActivity {

    private CalendarView calendarView;
    private Spinner timeSlotSpinner, gameTypeSpinner, tableNumberSpinner;
    private Button bookButton;

    private Date selectedDate;
    private FirebaseFirestore db;


    // lifecycle
    @Override
    protected void onResume() {
        super.onResume();
        // Ha van kiválasztott dátum és asztal, frissítsük az időpontokat
        if (selectedDate != null && tableNumberSpinner.getSelectedItem() != null) {
            String selectedTable = tableNumberSpinner.getSelectedItem().toString();
            updateAvailableTimeSlots(selectedDate, selectedTable);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);

        calendarView = findViewById(R.id.calendarView);
        calendarView.setMinDate(System.currentTimeMillis());

        Button profileButton = findViewById(R.id.profileButton);
        profileButton.setOnClickListener(v -> {
            Intent intent = new Intent(BookingActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        Calendar maxDate = Calendar.getInstance();
        maxDate.add(Calendar.DAY_OF_MONTH, 14);
        calendarView.setMaxDate(maxDate.getTimeInMillis());

        timeSlotSpinner = findViewById(R.id.timeSlotSpinner);
        gameTypeSpinner = findViewById(R.id.gameTypeSpinner);
        tableNumberSpinner = findViewById(R.id.tableNumberSpinner);
        bookButton = findViewById(R.id.bookButton);
        db = FirebaseFirestore.getInstance();
        selectedDate = new Date(calendarView.getDate());

        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                Calendar cal = Calendar.getInstance();
                cal.set(year, month, dayOfMonth, 0, 0, 0);
                selectedDate = cal.getTime();

                String selectedTable = tableNumberSpinner.getSelectedItem().toString();
                updateAvailableTimeSlots(selectedDate, selectedTable);
            }
        });

        tableNumberSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedTable = tableNumberSpinner.getSelectedItem().toString();
                updateAvailableTimeSlots(selectedDate, selectedTable);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        if (tableNumberSpinner.getSelectedItem() != null) {
            String initialTable = tableNumberSpinner.getSelectedItem().toString();
            updateAvailableTimeSlots(selectedDate, initialTable);
        }

        bookButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ScheduleExactAlarm")
            @Override
            public void onClick(View v) {

                String timeSlot = timeSlotSpinner.getSelectedItem() != null
                        ? timeSlotSpinner.getSelectedItem().toString()
                        : "";

                String gameType = gameTypeSpinner.getSelectedItem() != null
                        ? gameTypeSpinner.getSelectedItem().toString()
                        : "";

                String tableNumber = tableNumberSpinner.getSelectedItem() != null
                        ? tableNumberSpinner.getSelectedItem().toString()
                        : "";

                if (selectedDate == null || timeSlot.isEmpty() || gameType.isEmpty() || tableNumber.isEmpty()) {
                    Toast.makeText(BookingActivity.this, "Please fill all fields.", Toast.LENGTH_SHORT).show();
                    return;
                }

                Calendar startCal = Calendar.getInstance();
                startCal.setTime(selectedDate);
                startCal.set(Calendar.HOUR_OF_DAY, 0);
                startCal.set(Calendar.MINUTE, 0);
                startCal.set(Calendar.SECOND, 0);
                startCal.set(Calendar.MILLISECOND, 0);
                Date startOfDay = startCal.getTime();

                Calendar endCal = Calendar.getInstance();
                endCal.setTime(selectedDate);
                endCal.set(Calendar.HOUR_OF_DAY, 23);
                endCal.set(Calendar.MINUTE, 59);
                endCal.set(Calendar.SECOND, 59);
                endCal.set(Calendar.MILLISECOND, 999);
                Date endOfDay = endCal.getTime();

                String userEmail = FirebaseAuth.getInstance().getCurrentUser() != null
                        ? FirebaseAuth.getInstance().getCurrentUser().getEmail()
                        : "Unknown";

                db.collection("reservations")
                        .whereGreaterThanOrEqualTo("date", startOfDay)
                        .whereLessThanOrEqualTo("date", endOfDay)
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            boolean conflict = false;
                            for (var doc : queryDocumentSnapshots) {
                                String existingTimeSlot = doc.getString("timeSlot");
                                String existingGameType = doc.getString("gameType");
                                String existingTableNumber = doc.getString("tableNumber");
                                if (timeSlot.equals(existingTimeSlot)
                                        && gameType.equals(existingGameType)
                                        && tableNumber.equals(existingTableNumber)) {
                                    conflict = true;
                                    break;
                                }
                            }
                            if (conflict) {
                                Toast.makeText(BookingActivity.this, "This slot is already booked.", Toast.LENGTH_LONG).show();
                            } else {
                                Map<String, Object> booking = new HashMap<>();
                                booking.put("date", selectedDate);
                                booking.put("timeSlot", timeSlot);
                                booking.put("gameType", gameType);
                                booking.put("tableNumber", tableNumber);
                                booking.put("userName", userEmail);

                                db.collection("reservations")
                                        .add(booking)
                                        .addOnSuccessListener(documentReference -> {
                                            String reservationId = documentReference.getId(); // get generated reservation id
                                            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                                            // Update user's document in "users" collection by adding this reservation id to 'reservations' array
                                            db.collection("users").document(userId)
                                                    .update("reservations", FieldValue.arrayUnion(reservationId))
                                                    .addOnSuccessListener(aVoid -> {
                                                        Toast.makeText(BookingActivity.this, "Booking successful!", Toast.LENGTH_SHORT).show();
                                                        updateAvailableTimeSlots(selectedDate, tableNumber);
                                                        scheduleReminder(BookingActivity.this, selectedDate, timeSlot);

                                                    })
                                                    .addOnFailureListener(e -> {
                                                        Toast.makeText(BookingActivity.this, "Booking saved but failed to update user reservations: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                                    });
                                        })
                                        .addOnFailureListener(e ->
                                                Toast.makeText(BookingActivity.this, "Failed to book: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                                        );

                                updateAvailableTimeSlots(selectedDate, tableNumber);
                            }
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(BookingActivity.this, "Error checking reservation: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            Log.d("BookingActivity", e.getMessage());
                        });
            }
        });

    }

    private void updateAvailableTimeSlots(Date date, String tableNumber) {
        String[] allSlotsArray = getResources().getStringArray(R.array.time_slots);
        List<String> allSlots = new ArrayList<>(Arrays.asList(allSlotsArray));

        Calendar startCal = Calendar.getInstance();
        startCal.setTime(date);
        startCal.set(Calendar.HOUR_OF_DAY, 0);
        startCal.set(Calendar.MINUTE, 0);
        startCal.set(Calendar.SECOND, 0);
        startCal.set(Calendar.MILLISECOND, 0);
        Date startOfDay = startCal.getTime();

        Calendar endCal = Calendar.getInstance();
        endCal.setTime(date);
        endCal.set(Calendar.HOUR_OF_DAY, 23);
        endCal.set(Calendar.MINUTE, 59);
        endCal.set(Calendar.SECOND, 59);
        endCal.set(Calendar.MILLISECOND, 999);
        Date endOfDay = endCal.getTime();

        db.collection("reservations")
                .whereGreaterThanOrEqualTo("date", startOfDay)
                .whereLessThanOrEqualTo("date", endOfDay)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> reservedSlots = new ArrayList<>();
                    for (var doc : queryDocumentSnapshots) {
                        String existingTableNumber = doc.getString("tableNumber");
                        if (tableNumber.equals(existingTableNumber)) {
                            String existingTimeSlot = doc.getString("timeSlot");
                            reservedSlots.add(existingTimeSlot);
                        }
                    }
                    allSlots.removeAll(reservedSlots);

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(BookingActivity.this,
                            android.R.layout.simple_spinner_item, allSlots);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    timeSlotSpinner.setAdapter(adapter);

                    if (allSlots.isEmpty()) {
                        Toast.makeText(BookingActivity.this, "No available time slots for this date and table.", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(BookingActivity.this, "Error loading slots: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.d("BookingActivity", e.getMessage());
                });
    }

    @SuppressLint("ScheduleExactAlarm")
    @RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
    private void scheduleReminder(Context context, Date reservationDate, String timeSlot) {
        try {
            String[] timeParts = timeSlot.split(":");
            int hour = Integer.parseInt(timeParts[0]);
            int minute = Integer.parseInt(timeParts[1].substring(0, 2));

            Calendar reminderTime = Calendar.getInstance();
            reminderTime.setTime(reservationDate);
            reminderTime.set(Calendar.HOUR_OF_DAY, hour);
            reminderTime.set(Calendar.MINUTE, minute);
            reminderTime.set(Calendar.SECOND, 0);
            reminderTime.set(Calendar.MILLISECOND, 0);

            reminderTime.add(Calendar.HOUR_OF_DAY, -1);

            Intent intent = new Intent(context, AlarmReceiver.class);
            intent.putExtra("message", "Emlékeztető: Foglalásod hamarosan kezdődik!");

            int requestCode = (int) System.currentTimeMillis(); // unique ID
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_IMMUTABLE);

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
            if (alarmManager != null) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, reminderTime.getTimeInMillis(), pendingIntent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
