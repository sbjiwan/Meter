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

import com.example.meter.databinding.FragmentDetailBinding;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class DetailFragment extends Fragment {
    private FragmentDetailBinding binding;
    private final Source source = Source.CACHE;
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final CollectionReference reference = FirebaseFirestore.getInstance().collection("Water_User");
    private final ArrayList<BarEntry> barEntries = new ArrayList<>();
    private final ArrayList<String> xlabel = new ArrayList<>();
    private int year = 0, month = 0, date = 0;
    private final Map<String, Integer> data = new HashMap<>();

    @SuppressLint({"ClickableViewAccessibility", "NonConstantResourceId"})
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDetailBinding.inflate(inflater, container, false);

        Bundle bundle = getArguments();

        data.put("morning", 0);
        data.put("midday", 0);
        data.put("evening", 0);
        data.put("night", 0);

        if (bundle != null) {
            String select = bundle.get("select").toString();
            String[] selects = select.split("/");

            year = Integer.parseInt(selects[0]);
            month = Integer.parseInt(selects[1]);
            date = Integer.parseInt(selects[2]);
        }

        AtomicInteger sum = new AtomicInteger();
        for(int i = 0; i < 288; i ++) {
            int k = i*5;
            int h = k/60;
            int m = k%60;

            reference.document(Objects.requireNonNull(Objects.requireNonNull(auth.getCurrentUser()).getEmail()))
                    .collection(year + "")
                    .document((month+1) + "")
                    .collection(date + "")
                    .document(h + ":" + m)
                    .get(source)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            int meter = Integer.parseInt(Objects.requireNonNull(Objects.requireNonNull(
                                    document.getData()).getOrDefault("meter", 0)).toString());

                            sum.addAndGet(meter);

                            if(m == 0) {
                                if(sum.get() == 0)
                                    xlabel.add("");
                                else
                                    xlabel.add(h + "");

                                if(h >= 6 && h < 10)
                                    data.put("morning", data.get("morning") + sum.get());
                                else if(h >= 10 && h < 15)
                                    data.put("midday", data.get("midday") + sum.get());
                                else if(h >= 15 && h < 20)
                                    data.put("evening", data.get("evening") + sum.get());
                                else if(h >= 20)
                                    data.put("night", data.get("night") + sum.get());

                                barEntries.add(new BarEntry(h * 1F, sum.get() * 1F));
                                sum.set(0);
                            }
                            if(h == 23 && m == 55)
                                draw();
                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    });
        }
        return binding.getRoot();
    }

    @SuppressLint("SetTextI18n")
    public void draw() {
        System.out.println();
        List<Map.Entry<String, Integer>> entries;

        entries = data.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .collect(Collectors.toList());

        binding.title.setText(year + "년 " + (month + 1) + "월 " + date + "일");

        switch (entries.get(3).getKey()) {
            case "morning":
                binding.comparison.setText("아침에 가장 많이 사용해요");
                break;
            case "midday":
                binding.comparison.setText("낮에 가장 많이 사용해요");
                break;
            case "evening":
                binding.comparison.setText("저녁에 가장 많이 사용해요");
                break;
            case "night":
                binding.comparison.setText("밤에 가장 많이 사용해요");
                break;
        }

        BarDataSet dataSet = new BarDataSet(barEntries, "");

        dataSet.setDrawValues(false);

        BarData barData = new BarData();
        barData.addDataSet(dataSet);

        binding.barchart.getXAxis().setLabelCount(23);
        binding.barchart.getXAxis().setGranularity(1F);
        binding.barchart.getXAxis().setDrawLabels(true);
        binding.barchart.getXAxis().setDrawAxisLine(false);
        binding.barchart.getXAxis().setDrawGridLines(false);
        binding.barchart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        binding.barchart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(xlabel));

        binding.barchart.getAxisRight().setDrawLabels(false);
        binding.barchart.getAxisRight().setDrawAxisLine(false);
        binding.barchart.getAxisRight().setDrawGridLines(false);
        binding.barchart.getAxisLeft().setDrawAxisLine(false);
        binding.barchart.getAxisLeft().setDrawGridLines(false);

        binding.barchart.setClickable(false);
        binding.barchart.setDoubleTapToZoomEnabled(false);
        binding.barchart.setDrawGridBackground(false);
        binding.barchart.getLegend().setEnabled(false);
        binding.barchart.getDescription().setEnabled(false);
        binding.barchart.setData(barData);
        binding.barchart.invalidate();
    }
}
