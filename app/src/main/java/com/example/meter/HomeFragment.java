package com.example.meter;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.meter.databinding.FragmentHomeBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;
    private int meter = 0;
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final Calendar calendar = Calendar.getInstance();
    int year = calendar.get(Calendar.YEAR);
    int month = calendar.get(Calendar.MONTH);
    int date = calendar.get(Calendar.DATE);
    private final Source source = Source.CACHE;

    private final CollectionReference reference = FirebaseFirestore.getInstance().collection("Water_User");
    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);

        reference.document(Objects.requireNonNull(Objects.requireNonNull(auth.getCurrentUser()).getEmail()))
                .collection(year+"")
                .document(month + "")
                .collection(date + "")
                .document("METER")
                .get(source)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();

                        meter = Integer.parseInt(Objects.requireNonNull(Objects.requireNonNull(
                                document.getData()).getOrDefault("meter", 0)).toString());

                        Log.d(TAG, "Cached document data: " + meter);
                        binding.meter.setText(meter + "L");
                        if(meter > 600) {
                            resize(binding.water1, 300);
                            resize(binding.water2, 300);
                            if(meter > 900)
                                resize(binding.water3, 300);
                            else
                                resize(binding.water3, meter - 600);
                        }
                        else if(meter > 300) {
                            resize(binding.water1, 300);
                            resize(binding.water2, meter - 300);
                            resize(binding.water3, 0);
                        }
                        else {
                            resize(binding.water1, meter);
                            resize(binding.water2, 0);
                            resize(binding.water3, 0);
                        }
                    } else {
                        Log.w(TAG, "Error getting documents.", task.getException());
                    }
                });
        return binding.getRoot();
    }

    private void resize(View view, int meter) {
        ViewGroup.LayoutParams params = view.getLayoutParams();

        params.width = meter;
        params.height = meter;
        view.setLayoutParams(params);
    }
    public void setup(int month) {
        Random random = new Random();
        int monthsum = 0;

        calendar.set(2023, month-1, 1);
        int d = calendar.getActualMaximum(Calendar.DATE);
        int dow = calendar.get(Calendar.DAY_OF_WEEK);
        int p = 7 - dow;

        for (int i = 1; i <= d; i++) {
            int sum = 0;
            for (int j = 0; j < 288; j++) {
                final int k = j * 5;
                int h = k / 60;
                int m = k % 60;
                Map<String, Object> map = new HashMap<>();
                int v = 0;

                if(i > 3) {
                    if ((i - p)%7 == 0 || (i-p)%7 == 1) {
                        if (h == 8)
                            v = random.nextInt(5);
                        else if (h == 12)
                            v = random.nextInt(7);
                        else if (h >= 18 && h < 20)
                            v = random.nextInt(15);
                    }else {
                        if (h == 8)
                            v = random.nextInt(8);
                        else if (h == 12)
                            v = random.nextInt(4);
                        else if (h >= 18 && h < 20)
                            v = random.nextInt(13);
                    }
                }
                else {
                    if (h == 8)
                        v = random.nextInt(8);
                    else if (h == 12)
                        v = random.nextInt(4);
                    else if (h >= 18 && h < 20)
                        v = random.nextInt(13);
                }

                map.put("meter", v);
                sum += v;
                int finalI = i;

                reference.document(Objects.requireNonNull(Objects.requireNonNull(auth.getCurrentUser()).getEmail()))
                        .collection("2023")
                        .document(month + "")
                        .collection(i+"")
                        .document(h+":"+m)
                        .set(map)
                        .addOnSuccessListener(aVoid -> Log.d(TAG, finalI + "DocumentSnapshot successfully written!"))
                        .addOnFailureListener(e -> Log.w(TAG, "Error writing document", e));
            }
            Map<String, Object> map = new HashMap<>();
            map.put("meter", sum);
            monthsum += sum;
            reference.document(Objects.requireNonNull(Objects.requireNonNull(auth.getCurrentUser()).getEmail()))
                    .collection("2023")
                    .document(month +"")
                    .collection(i+"")
                    .document("METER")
                    .set(map)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "dayend DocumentSnapshot successfully written!"))
                    .addOnFailureListener(e -> Log.w(TAG, "Error writing document", e));
        }

        Map<String, Object> map = new HashMap<>();
        map.put("meter", monthsum);
        reference.document(Objects.requireNonNull(Objects.requireNonNull(auth.getCurrentUser()).getEmail()))
                .collection("2023")
                .document(month +"")
                .collection("SUM")
                .document("METER")
                .set(map)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "END DocumentSnapshot successfully written!"))
                .addOnFailureListener(e -> Log.w(TAG, "Error writing document", e));
    }
}
