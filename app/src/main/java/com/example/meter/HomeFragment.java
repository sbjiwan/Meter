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

import java.time.LocalDate;
import java.util.Objects;

public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;
    private int meter = 0;
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final CollectionReference reference = FirebaseFirestore.getInstance().collection("Water_User");
    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);

        int year = LocalDate.now().getYear();
        int month = LocalDate.now().getMonthValue();
        int date = LocalDate.now().getDayOfMonth();

        Source source = Source.CACHE;

        reference.document(Objects.requireNonNull(Objects.requireNonNull(auth.getCurrentUser()).getEmail()))
                .collection(year+"-"+month+"-"+date)
                .document("METER")
                .get(source)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        DocumentSnapshot document = task.getResult();
                        meter = Integer.parseInt(Objects.requireNonNull(Objects.requireNonNull(document.getData()).getOrDefault("meter", 0)).toString());
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
}
