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
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.meter.databinding.FragmentHistoryhomeBinding;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class HistoryhomeFragment extends Fragment {
    private ArrayList<Integer> avg;
    private FragmentHistoryhomeBinding binding;
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
    private ArrayList<BarEntry> barEntries;

    @SuppressLint({"ClickableViewAccessibility", "NonConstantResourceId"})
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHistoryhomeBinding.inflate(inflater, container, false);

        Bundle bundle = getArguments();
        if(bundle != null) {
            String select = bundle.get("select").toString();
            String[] selects = select.split("\\s+");

            year = Integer.parseInt(selects[0].substring(0, selects[0].length()-1));
            month = Integer.parseInt(selects[1].substring(0, selects[1].length()-1)) - 1;

            calendar.set(year, month, date);
            if(year != now_year || month != now_month) {
                date = calendar.getActualMaximum(Calendar.DATE);
                calendar.set(Calendar.DATE, date);
                Bundle sbundle = new Bundle();
                sbundle.putString("select", year + "/" + (month+1));
                HistorycardFragment historycardFragment = new HistorycardFragment();
                historycardFragment.setArguments(sbundle);
                setCurrentFragment(historycardFragment);
            }
            else {
                Bundle sbundle = new Bundle();
                sbundle.putString("select", year + "/" + month);
                HistorylineFragment historylineFragment = new HistorylineFragment();
                historylineFragment.setArguments(sbundle);
                setCurrentFragment(historylineFragment);
            }
        }

        load_month();

        binding.btnsCategory.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if(isChecked) {
                switch (binding.btnsCategory.getCheckedButtonId()) {
                    case R.id.button_date: {
                        load_date();
                        break;
                    }
                    case R.id.button_week: {
                        load_week();
                        break;
                    }
                    case R.id.button_month: {
                        load_month();
                        break;
                    }
                }
            }
        });
        return binding.getRoot();
    }
    public void load_date() {
        barEntries = new ArrayList<>();
        avg = new ArrayList<>();
        int ck = 0;

        if (year == now_year && month == now_month) {
            calendar.add(Calendar.MONTH, -1);
            int t_date = calendar.getActualMaximum(Calendar.DATE);

            if(t_date > date) {
                ck = t_date - date + 1;
                year = calendar.get(Calendar.YEAR);
                month = calendar.get(Calendar.MONTH);
                for (int i = date; i <= t_date; i++) {
                    final int final_date = i;

                    reference.document(Objects.requireNonNull(Objects.requireNonNull(auth.getCurrentUser()).getEmail()))
                            .collection(year + "")
                            .document((month+1) + "")
                            .collection(final_date + "")
                            .document("METER")
                            .get(source)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot document = task.getResult();
                                    int meter = Integer.parseInt(Objects.requireNonNull(Objects.requireNonNull(
                                            document.getData()).getOrDefault("meter", 0)).toString());

                                    barEntries.add(new BarEntry((final_date - date) * 1F, meter * 1F));
                                    avg.add(meter);
                                } else {
                                    Log.w(TAG, "Error getting documents.", task.getException());
                                }
                            });
                }
            }
            calendar.set(year,month+1,date);
            year = calendar.get(Calendar.YEAR);
            month = calendar.get(Calendar.MONTH);
        }

        for (int i = 1; i <= date; i++) {
            final int final_date = i;
            int finalDate = date;
            int finalCk = ck;
            reference.document(Objects.requireNonNull(Objects.requireNonNull(auth.getCurrentUser()).getEmail()))
                    .collection(year + "")
                    .document((month+1) + "")
                    .collection(final_date + "")
                    .document("METER")
                    .get(source)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            int meter = Integer.parseInt(Objects.requireNonNull(Objects.requireNonNull(
                                    document.getData()).getOrDefault("meter", 0)).toString());

                            barEntries.add(new BarEntry((final_date + finalCk - 1) * 1F, meter * 1F));

                            avg.add(meter);
                            if (final_date == finalDate)
                                draw("date");
                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    });
        }
    }
    public void load_week() {
        barEntries = new ArrayList<>();
        avg = new ArrayList<>();

        if (year == now_year && month == now_month) {
            calendar.set(Calendar.MONTH, month-1);
            int dow = calendar.get(Calendar.DAY_OF_WEEK);
            int j_date = calendar.getActualMaximum(Calendar.DATE);
            calendar.add(Calendar.DATE, 1 - dow);
            int c_month = calendar.get(Calendar.MONTH);
            int c_date = calendar.get(Calendar.DATE);
            int t_date = calendar.getActualMaximum(Calendar.DATE);
            AtomicInteger sum = new AtomicInteger();
            AtomicInteger c = new AtomicInteger();
            int t_month = c_month;
            calendar.set(year, month, date);

            for(int i = 1; i <= j_date + dow; i ++) {
                int day = i;

                if(i == t_date - c_date + 2 || i == t_date - c_date + 2 + j_date)
                    t_month ++;

                if(i <= t_date - c_date + 1)
                    day += c_date - 1;
                else if(i < t_date - c_date + j_date + 2)
                    day -= t_date - c_date + 1;
                else
                    day -= t_date - c_date + j_date + 1;

                int finalI = i;

                int finalT_month = t_month;
                int finalDay = day;
                reference.document(Objects.requireNonNull(Objects.requireNonNull(auth.getCurrentUser()).getEmail()))
                        .collection(year + "")
                        .document((t_month+1) + "")
                        .collection(day + "")
                        .document("METER")
                        .get(source)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                int meter = Integer.parseInt(Objects.requireNonNull(Objects.requireNonNull(
                                        document.getData()).getOrDefault("meter", 0)).toString());
                                sum.addAndGet(meter);

                                if(finalT_month == now_month && finalDay == now_date) {
                                    avg.add(sum.get());
                                    barEntries.add(new BarEntry((c.get())*1F, sum.get()*1F));
                                    draw("week");
                                }
                                else if(finalI % 7 == 0) {
                                    avg.add(sum.get());
                                    barEntries.add(new BarEntry((c.get())*1F, sum.get()*1F));
                                    c.getAndIncrement();
                                    sum.set(0);
                                }
                            } else {
                                Log.w(TAG, "Error getting documents.", task.getException());
                            }
                        });
            }


        }
        else {
            calendar.set(Calendar.DATE, calendar.getActualMaximum(Calendar.DATE));
            int t_dow = calendar.get(Calendar.DAY_OF_WEEK);
            calendar.add(Calendar.DATE, 7 - t_dow);
            int t_date = calendar.get(Calendar.DATE);
            calendar.set(Calendar.MONTH, 2);
            calendar.set(Calendar.DATE, 1);
            int dow = calendar.get(Calendar.DAY_OF_WEEK);
            calendar.add(Calendar.DATE, 1 - dow);
            int c_month = calendar.get(Calendar.MONTH);
            int c_date = calendar.get(Calendar.DATE);
            AtomicInteger sum = new AtomicInteger();
            AtomicInteger c = new AtomicInteger();

            calendar.set(year,month,date);

            int t_month = c_month;

            for(int i = 1; i <= t_date + date + 7 - dow; i ++) {
                int day = i;

                if(i == 8 - dow)
                    t_month ++;
                else if(i == date - dow + 8)
                    t_month ++;

                if(i <= 7 - dow)
                    day += c_date - 1;
                else if(i > date - dow + 7)
                    day -= date - dow + 7;
                else if(i > 7 - dow)
                    day -= 7 - dow;

                int finalI = i;

                reference.document(Objects.requireNonNull(Objects.requireNonNull(auth.getCurrentUser()).getEmail()))
                        .collection(year + "")
                        .document((t_month+1) + "")
                        .collection(day + "")
                        .document("METER")
                        .get(source)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                int meter = Integer.parseInt(Objects.requireNonNull(Objects.requireNonNull(
                                        document.getData()).getOrDefault("meter", 0)).toString());
                                sum.addAndGet(meter);

                                if(finalI == t_date + date + 7 - dow) {
                                    avg.add(sum.get());
                                    barEntries.add(new BarEntry((c.get())*1F, sum.get()*1F));
                                    draw("week");
                                }
                                else if(finalI % 7 == 0) {
                                    avg.add(sum.get());
                                    barEntries.add(new BarEntry((c.get())*1F, sum.get()*1F));
                                    c.getAndIncrement();
                                    sum.set(0);
                                }
                            } else {
                                Log.w(TAG, "Error getting documents.", task.getException());
                            }
                        });
            }
        }
    }
    public void load_month() {
        barEntries = new ArrayList<>();
        avg = new ArrayList<>();

        for (int i = 0; i < 3; i ++) {
            int finalI = i;
            reference.document(Objects.requireNonNull(Objects.requireNonNull(auth.getCurrentUser()).getEmail()))
                    .collection(year + "")
                    .document((month - 1 + i) + "")
                    .collection("SUM")
                    .document("METER")
                    .get(source)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            int meter = Integer.parseInt(Objects.requireNonNull(Objects.requireNonNull(
                                    document.getData()).getOrDefault("meter", 0)).toString());
                            barEntries.add(new BarEntry(finalI * 1F, meter * 1F));
                            avg.add(meter);
                            if (finalI == 2) draw("month");
                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    });
        }
        calendar.set(year, month, date);
    }
    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    public void draw(String type) {
        IntSummaryStatistics statistics = avg.stream().mapToInt(num -> num).summaryStatistics();

        double Avg = statistics.getAverage();

        int ck = 0;
        int ck_day = 0;
        ArrayList<String> xlabel = new ArrayList<>();
        List<Integer> color = new ArrayList<>();
        CombinedData data1 = null;

        switch (type) {
            case "date": {
                if (year == now_year && month == now_month) {
                    calendar.add(Calendar.MONTH, -1);
                    int t_date = calendar.getActualMaximum(Calendar.DATE);
                    if (t_date > now_date) {
                        ck = t_date - now_date + 1;
                        int dow = calendar.get(Calendar.DAY_OF_WEEK);
                        year = calendar.get(Calendar.YEAR);
                        month = calendar.get(Calendar.MONTH);
                        for (int i = now_date; i <= t_date; i++) {
                            if (dow % 7 == 1)
                                xlabel.add((month + 1) + "." + i);
                            else
                                xlabel.add("");
                            dow++;
                            ck_day++;
                        }
                    }
                    calendar.add(Calendar.MONTH, 1);
                    year = calendar.get(Calendar.YEAR);
                    month = calendar.get(Calendar.MONTH);
                }
                calendar.set(Calendar.DATE, 1);
                int dow = calendar.get(Calendar.DAY_OF_WEEK);
                calendar.set(Calendar.DATE, date);
                for (int i = 1; i <= date; i++) {
                    if(i == date) {
                        xlabel.add("오늘");
                        ck_day++;
                        break;
                    }
                    if (dow % 7 == 1)
                        xlabel.add((month + 1) + "." + i);
                    else
                        xlabel.add("");
                    dow++;
                    ck_day++;
                }

                if (year == now_year && month == now_month) {
                    int com = (int) barEntries.get(ck_day - 1).getY();
                    binding.title2.setText("하루에 " + String.format("%.1f", Avg) + "L 정도 써요");
                    binding.comparison2.setText("오늘은 " + com + "L 썼어요");
                    for (int i = 0; i < ck_day; i++) {
                        if (ck > i)
                            color.add(Color.rgb(230, 230, 230));
                        else
                            color.add(Color.rgb(80, 200, 255));
                    }
                } else {
                    binding.title2.setText((month + 1) + "월에는 하루\n" + String.format("%.1f", Avg) + "L 정도 썼어요");
                    binding.comparison2.setText("");
                    for (int i = 0; i < ck_day; i++) {
                        color.add(Color.rgb(140, 140, 160));
                    }
                }

                ArrayList<Entry> entries = new ArrayList<>();

                for (int index = 0; index < barEntries.size(); index++)
                    entries.add(new Entry(index, 276));

                LineData d = new LineData();

                LineDataSet set = new LineDataSet(entries, "Line DataSet");
                set.setColor(Color.RED);
                set.setLineWidth(1f);
                set.setDrawCircles(false);
                set.setDrawValues(false);
                set.setHighLightColor(Color.MAGENTA);
                set.setAxisDependency(YAxis.AxisDependency.LEFT);

                d.addDataSet(set);

                BarDataSet dataSet1 = new BarDataSet(barEntries, "");
                dataSet1.setColors(color);
                dataSet1.setDrawValues(false);

                BarData data = new BarData();
                data.addDataSet(dataSet1);
                data1 = new CombinedData();
                data1.setData(data);
                data1.setData(d);
                binding.barchart.getAxisLeft().setGranularity(50F);

                binding.barchart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
                    @Override
                    public void onValueSelected(Entry e, Highlight h) {
                        HistoryDialogFragment dialogFragment = new HistoryDialogFragment();
                        Bundle bundle = new Bundle();
                        calendar.add(Calendar.DATE,  (int) e.getX() + 1 - barEntries.size());
                        int k_year, k_month, k_date;
                        k_year = calendar.get(Calendar.YEAR);
                        k_month = calendar.get(Calendar.MONTH);
                        k_date = calendar.get(Calendar.DATE);
                        calendar.set(year, month, date);
                        bundle.putString("select", k_year + "/" + k_month + "/" + k_date);
                        dialogFragment.setArguments(bundle);
                        dialogFragment.show(getChildFragmentManager(), "detail");
                    }
                    @Override
                    public void onNothingSelected() {
                    }
                });
                break;
            }
            case "week": {
                int com = (int) barEntries.get(barEntries.size() - 1).getY();

                if (year == now_year && month == now_month) {
                    binding.title2.setText("일주일 " + (int) Avg + "L 정도 써요");
                    if((int) Avg > com)
                        binding.comparison2.setText("이번 주엔 " + ((int) Avg - com) + "L 덜 썼어요");
                    else if((int) Avg < com)
                        binding.comparison2.setText("이번 주엔 " + (com - (int)Avg) + "L 더 썼어요");
                    else
                        binding.comparison2.setText("이번 주엔 평균만큼 썼어요");
                    calendar.add(Calendar.MONTH, -1);
                    int t_dow = calendar.get(Calendar.DAY_OF_WEEK);
                    calendar.add(Calendar.DATE, 1 - t_dow);

                    int i = 1;
                    while (true) {
                        if (calendar.get(Calendar.DATE) == now_date && calendar.get(Calendar.MONTH) == now_month) {
                            xlabel.add("이번 주");
                            ck_day++;
                            break;
                        } else if (i % 7 == 0) {
                            xlabel.add("~ " + (calendar.get(Calendar.MONTH) + 1) + "." + calendar.get(Calendar.DATE));
                            ck_day++;
                        }
                        calendar.add(Calendar.DATE, 1);
                        i++;
                    }
                    for(i = 1; i <= ck_day; i ++) {
                        if (i == ck_day)
                            color.add(Color.rgb(80, 200, 255));
                        else
                            color.add(Color.rgb(230, 230, 230));
                    }
                } else {
                    int t_dow = calendar.get(Calendar.DAY_OF_WEEK);
                    calendar.add(Calendar.DATE, 7 - t_dow);
                    int t_month = calendar.get(Calendar.MONTH);
                    int t_date = calendar.get(Calendar.DATE);
                    calendar.set(year, month, 1);
                    int dow = calendar.get(Calendar.DAY_OF_WEEK);
                    calendar.add(Calendar.DATE, 1 - dow);

                    int i = 1;
                    while (true) {
                        if (calendar.get(Calendar.DATE) == t_date && calendar.get(Calendar.MONTH) == t_month) {
                            xlabel.add("~ " + (t_month + 1) + "." + t_date);
                            ck_day++;
                            break;
                        } else if (i % 7 == 0) {
                            xlabel.add("~ " + (calendar.get(Calendar.MONTH) + 1) + "." + calendar.get(Calendar.DATE));
                            ck_day++;
                        }
                        calendar.add(Calendar.DATE, 1);
                        i++;
                    }
                    for(i = 0;i < ck_day; i ++)
                        color.add(Color.rgb(140, 140, 160));
                }

                ArrayList<Entry> entries = new ArrayList<>();

                for (int index = 0; index < barEntries.size(); index++)
                    entries.add(new Entry(index, 276*7));

                LineData d = new LineData();

                LineDataSet set = new LineDataSet(entries, "Line DataSet");
                set.setColor(Color.RED);
                set.setLineWidth(1f);
                set.setDrawCircles(false);
                set.setDrawValues(false);
                set.setHighLightColor(Color.MAGENTA);
                set.setAxisDependency(YAxis.AxisDependency.LEFT);

                d.addDataSet(set);

                BarDataSet dataSet1 = new BarDataSet(barEntries, "");
                dataSet1.setColors(color);
                dataSet1.setValueTextSize(16F);

                BarData data = new BarData();
                data.setBarWidth(0.2F);
                data.addDataSet(dataSet1);
                data1 = new CombinedData();
                data1.setData(data);
                data1.setData(d);
                binding.barchart.getAxisLeft().setDrawLabels(false);
                Legend legend = binding.barchart.getLegend();
                legend.setXOffset(10f);
                binding.barchart.setExtraOffsets(5f,5f,5f,5f);
                break;
            }
            case "month": {
                ck_day = 3;

                for (int i = 1; i <= 3; i++) {
                    if(year == now_year && month == now_month && i == 3)
                        xlabel.add("이번 달");
                    else
                        xlabel.add((calendar.get(Calendar.MONTH) - 2 + i) + "월");
                }

                int com = (int) barEntries.get(barEntries.size() - 1).getY();

                if (year == now_year && month == now_month) {
                    binding.title2.setText("한달에 " + (int) Avg + "L 정도 써요");
                    binding.comparison2.setText("이번 달엔 " + com + "L 썼어요");
                    for (int i = 0; i < 3; i++) {
                        if (i == 2)
                            color.add(Color.rgb(80, 200, 255));
                        else
                            color.add(Color.rgb(230, 230, 230));
                    }
                } else {
                    if ((int) Avg > com)
                        binding.title2.setText((month + 1) + "월에는 " + ((int) Avg - com) + "L 덜 썼어요");
                    else if ((int) Avg == com)
                        binding.title2.setText((month + 1) + "월에는 평균만큼 썼어요");
                    else
                        binding.title2.setText((month + 1) + "월에는 " + (com - (int) Avg) + "L 더 썼어요");

                    binding.comparison2.setText("한달에 평균 " + (int) Avg + "L 정도 써요");

                    for (int i = 0; i < 3; i++) {
                        color.add(Color.rgb(140, 140, 160));
                    }
                }
                BarDataSet dataSet1 = new BarDataSet(barEntries, "");
                dataSet1.setColors(color);
                dataSet1.setValueTextSize(16F);

                LineData d = new LineData();

                ArrayList<Entry> entries = new ArrayList<>();

                for (int index = 0; index < 3; index++)
                    entries.add(new Entry(index, 276*30));

                LineDataSet set = new LineDataSet(entries, "Line DataSet");
                set.setColor(Color.RED);
                set.setLineWidth(1f);
                set.setDrawCircles(false);
                set.setDrawValues(false);
                set.setHighLightColor(Color.MAGENTA);
                set.setAxisDependency(YAxis.AxisDependency.LEFT);

                d.addDataSet(set);

                BarData data = new BarData();
                data.addDataSet(dataSet1);
                data1 = new CombinedData();
                data1.setData(data);
                data1.setData(d);

                binding.barchart.getAxisLeft().setDrawLabels(false);

                binding.barchart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
                    @Override
                    public void onValueSelected(Entry e, Highlight h) {
                        int x = (int) e.getX();

                        System.out.println(month -1 + x);

                        Bundle sbundle = new Bundle();
                        if((month - 1 + x) != (now_month + 1)) {
                            sbundle.putString("select", year + "/" + (month -1 + x));
                            HistorycardFragment historycardFragment = new HistorycardFragment();
                            historycardFragment.setArguments(sbundle);

                            setCurrentFragment(historycardFragment);
                        }
                        else {
                            sbundle.putString("select", year + "/" + (month -2 +x));
                            HistorylineFragment historylineFragment = new HistorylineFragment();
                            historylineFragment.setArguments(sbundle);

                            setCurrentFragment(historylineFragment);
                        }
                    }

                    @Override
                    public void onNothingSelected() {

                    }
                });
                break;
            }
        }
        binding.barchart.getXAxis().setLabelCount(ck_day);
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
        binding.barchart.setData(data1);
        binding.barchart.invalidate();
    }

    private void setCurrentFragment(Fragment fragment) {
        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.historyline_container, fragment).commit();
    }
}
