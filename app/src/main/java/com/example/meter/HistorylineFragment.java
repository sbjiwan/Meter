package com.example.meter;


import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.example.meter.databinding.FragmentHistorylineBinding;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.IntSummaryStatistics;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class HistorylineFragment extends Fragment {
    private ArrayList<Integer> lineavg;
    private ArrayList<Integer> comp;
    private FragmentHistorylineBinding binding;
    private final Source source = Source.CACHE;
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final CollectionReference reference = FirebaseFirestore.getInstance().collection("Water_User");
    private final Calendar calendar = Calendar.getInstance();
    private final Calendar t_calendar = Calendar.getInstance();
    private int date = calendar.get(Calendar.DATE);
    private int month = calendar.get(Calendar.MONTH);
    private int year = calendar.get(Calendar.YEAR);
    private final int now_date = t_calendar.get(Calendar.DATE);
    private final int now_month = t_calendar.get(Calendar.MONTH);
    private final int now_year = t_calendar.get(Calendar.YEAR);
    private ArrayList<Entry> lineEntries;
    private ArrayList<Entry> complineEntries;

    @SuppressLint({"ClickableViewAccessibility", "NonConstantResourceId"})
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHistorylineBinding.inflate(inflater, container, false);

        Bundle bundle = getArguments();
        if (bundle != null) {
            String select = bundle.get("select").toString();
            String[] selects = select.split("/");

            year = Integer.parseInt(selects[0]);
            month = Integer.parseInt(selects[1]);

            calendar.set(year, month, date);
            if (year != now_year || month != now_month) {
                date = calendar.getActualMaximum(Calendar.DATE);
                calendar.set(Calendar.DATE, date);
            }
        }
        load_line();

        return binding.getRoot();
    }

    public void load_line() {
        lineEntries = new ArrayList<>();
        complineEntries = new ArrayList<>();
        lineavg = new ArrayList<>();
        comp = new ArrayList<>();

        calendar.add(Calendar.MONTH, -1);
        int t_date = calendar.getActualMaximum(Calendar.DATE);
        calendar.add(Calendar.MONTH, 1);

        AtomicInteger sum = new AtomicInteger();

        for(int i = 1; i <= t_date; i ++) {
            int finalI = i;
            reference.document(Objects.requireNonNull(Objects.requireNonNull(auth.getCurrentUser()).getEmail()))
                    .collection(year + "")
                    .document(month+ "")
                    .collection(finalI + "")
                    .document("METER")
                    .get(source)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            int meter = Integer.parseInt(Objects.requireNonNull(Objects.requireNonNull(
                                    document.getData()).getOrDefault("meter", 0)).toString());

                            sum.addAndGet(meter);

                            complineEntries.add(new Entry((finalI - 1) * 1F, sum.get() * 1F));
                            comp.add(meter);
                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    });
        }

        AtomicInteger sum1 = new AtomicInteger();

        for(int i = 1; i <= date;i ++) {
            int finalI = i;
            reference.document(Objects.requireNonNull(Objects.requireNonNull(auth.getCurrentUser()).getEmail()))
                    .collection(year + "")
                    .document((month+1) + "")
                    .collection( finalI + "")
                    .document("METER")
                    .get(source)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            int meter = Integer.parseInt(Objects.requireNonNull(Objects.requireNonNull(
                                    document.getData()).getOrDefault("meter", 0)).toString());

                            sum1.addAndGet(meter);
                            lineEntries.add(new Entry((finalI - 1) * 1F, sum1.get() * 1F));

                            lineavg.add(meter);
                            if (finalI == now_date)
                                draw_line();
                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    });
        }
    }

    @SuppressLint("SetTextI18n")
    public void draw_line() {
        IntSummaryStatistics statistics = lineavg.stream().mapToInt(num -> num).summaryStatistics();
        if (comp.size() - 1 > now_date) {
            comp.subList(now_date, comp.size() - 1).clear();
        }
        IntSummaryStatistics statistics1 = comp.stream().mapToInt(num -> num).summaryStatistics();

        double Sum = statistics.getSum();
        double compSum = statistics1.getSum();

        binding.title.setText("오늘까지 " + (int) Sum + "L 썼어요");
        if(Sum > compSum)
            binding.comparison.setText("지난달 이맘때보다 " + (int) (Sum - compSum) + "L 더 썼어요");
        else if(compSum > Sum)
            binding.comparison.setText("지난달 이맘때보다 " + (int) (compSum - Sum) + "L 덜 썼어요");

        LineDataSet dataSet = new LineDataSet(lineEntries, (month + 1) + "월");

        dataSet.setColors(Color.rgb(80,200,255));
        dataSet.setValueTextColor(Color.WHITE);

        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setCircleRadius(1);
        dataSet.setCircleColor(Color.parseColor("#FFA1B4DC"));
        dataSet.setCircleHoleColor(Color.BLUE);

        LineDataSet compdataSet = new LineDataSet(complineEntries, month + "월");
        compdataSet.setColors(Color.rgb(230,230,230));
        compdataSet.setValueTextColor(Color.WHITE);
        compdataSet.setFillColor(Color.rgb(230,230,230));
        compdataSet.setDrawFilled(true);

        compdataSet.setDrawCircles(false);

        ArrayList<String> xlabel = new ArrayList<>();

        for(int i = 1; i <= calendar.getActualMaximum(Calendar.DATE); i ++) {
            if(i == 1 || i == now_date || i == calendar.getActualMaximum(Calendar.DATE))
                xlabel.add((month+1) + "." + i);
            else
                xlabel.add("");
        }
        LineData data = new LineData();
        data.addDataSet(compdataSet);

        data.addDataSet(dataSet);

        binding.linechart.getXAxis().setGranularity(1F);
        binding.linechart.getXAxis().setDrawLabels(true);
        binding.linechart.getXAxis().setDrawAxisLine(false);
        binding.linechart.getXAxis().setDrawGridLines(false);
        binding.linechart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        binding.linechart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(xlabel));

        binding.linechart.getAxisRight().setDrawLabels(false);
        binding.linechart.getAxisRight().setDrawAxisLine(false);
        binding.linechart.getAxisRight().setDrawGridLines(false);
        binding.linechart.getAxisLeft().setDrawLabels(false);
        binding.linechart.getAxisLeft().setDrawAxisLine(false);
        binding.linechart.getAxisLeft().setDrawGridLines(false);

        binding.linechart.setDoubleTapToZoomEnabled(false);
        binding.linechart.getLegend().setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        binding.linechart.setDrawGridBackground(false);
        binding.linechart.getDescription().setText("");
        binding.linechart.setData(data);
        binding.linechart.invalidate();
    }
}
