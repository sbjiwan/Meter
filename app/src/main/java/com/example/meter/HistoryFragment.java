package com.example.meter;

import static android.content.ContentValues.TAG;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.meter.databinding.FragmentHistoryBinding;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Objects;

public class HistoryFragment extends Fragment {
    private final ArrayList<BarEntry> barEntries = new ArrayList<>();
    private FragmentHistoryBinding binding;
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final CollectionReference reference = FirebaseFirestore.getInstance().collection("Water_User");
    private final int year = LocalDate.now().getYear();
    private final int month = LocalDate.now().getMonthValue();
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHistoryBinding.inflate(inflater, container, false);

        load();

        return binding.getRoot();
    }

    public void load() {
        for(int date = 1; date < 31; date++) {
            final int fdate = date;

            Source source = Source.CACHE;

            reference.document(Objects.requireNonNull(Objects.requireNonNull(auth.getCurrentUser()).getEmail()))
                    .collection(year+"-"+month+"-"+fdate)
                    .document("METER")
                    .get(source)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            int meter = Integer.parseInt(Objects.requireNonNull(Objects.requireNonNull(
                                    document.getData()).getOrDefault("meter", 0)).toString());

                            barEntries.add(new BarEntry(fdate * 1F, meter * 1F));

                            if(fdate == 30)
                                draw();
                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    });
        }
    }

    public void draw() {
        if(barEntries.size() == 30) {
            BarDataSet dataSet = new BarDataSet(barEntries, year + "-" + month);
            dataSet.setColors(ColorTemplate.PASTEL_COLORS);
            dataSet.setValueTextColor(Color.WHITE);

            BarData data = new BarData(dataSet);

            binding.barchart.setOnClickListener(view -> {

            });

            binding.barchart.setFitBars(true);
            binding.barchart.setData(data);
            binding.barchart.getDescription().setText("TEST");
            binding.barchart.animateY(2000);
            binding.barchart.invalidate();
        }
    }
}
