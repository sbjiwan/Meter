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

import com.example.meter.databinding.FragmentHistoryBinding;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
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
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class HistoryFragment extends Fragment {
    private ArrayList<Integer> avg;
    private ArrayList<Integer> lineavg;
    private ArrayList<Integer> comp;
    private ArrayList<String> xlabel;
    private FragmentHistoryBinding binding;
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
    private ArrayList<Entry> lineEntries;
    private ArrayList<Entry> complineEntries;

    @SuppressLint({"ClickableViewAccessibility", "NonConstantResourceId"})
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHistoryBinding.inflate(inflater, container, false);

        load_date();
        load_line();

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
                    .document(month + "")
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

        for(int i = 1; i <= now_date;i ++) {
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
    public void load_date() {
        barEntries = new ArrayList<>();
        avg = new ArrayList<>();
        int ck = 0;

        if (year == now_year && month == now_month) {
            calendar.add(Calendar.MONTH, -1);
            int t_date = calendar.getActualMaximum(Calendar.DATE);

            if(t_date > now_date) {
                ck = t_date - now_date + 1;
                year = calendar.get(Calendar.YEAR);
                month = calendar.get(Calendar.MONTH);
                for (int i = now_date; i <= t_date; i++) {
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

                                    barEntries.add(new BarEntry((final_date - now_date) * 1F, meter * 1F));
                                    avg.add(meter);
                                } else {
                                    Log.w(TAG, "Error getting documents.", task.getException());
                                }
                            });
                }
            }
            calendar.add(month,1);
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
            /*
            -> 2023년 4월 28일 을 기준
            1. 현재 출력해야하는 달이 4월인지 아닌지 판별.
            2. 4월의 경우
                2.1 3월 28일으로 add month -1 -> 3월 28일이 해당하는 주 부터 시작. 3월 28일의 dow 를 계산 해서 add date 1 - dow -> 현재 날짜까지 진행 if( date == now_date)
            3. 4월이 아닌 경우 해당 월의 1일로 돌아가 set date 1 -> 1일이 해당하는 주 부터 시작. add date 1 - dow ->
            마지막 날짜가 해당하는 주 까지 진행 (4월이 아닌 경우로 왔을 때 getmaxdate 로 가서 t_dow(마지막 날의 dow) 를 저장 해서 1 - dow 부터 maxdate + t_dow 까지 진행
             */

        barEntries = new ArrayList<>();
        avg = new ArrayList<>();

        if (year == now_year && month == now_month) {
            calendar.set(Calendar.MONTH, month-1);
            int dow = calendar.get(Calendar.DAY_OF_WEEK);
            calendar.add(Calendar.DATE, 1 - dow);
            int c_month = calendar.get(Calendar.MONTH);
            int c_date = calendar.get(Calendar.DATE);
            int t_date = calendar.getActualMaximum(Calendar.DATE);
            AtomicInteger sum = new AtomicInteger();
            AtomicInteger c = new AtomicInteger();
            int t_month = c_month;
            calendar.set(year, month, date);

            for(int i = 1; i <= t_date - c_date + date; i ++) {
                int day = i;

                if(i == t_date - c_date + 2)
                    t_month ++;

                if(i <= t_date - c_date + 1)
                    day += c_date - 1;
                else if(i > t_date - c_date + 1)
                    day -= t_date - c_date + 1;

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

                                if(finalI == t_date - c_date + date) {
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
                            if (finalI == 2) {
                                IntSummaryStatistics statistics = lineavg.stream().mapToInt(num -> num).summaryStatistics();
                                double Sum = statistics.getSum();
                                barEntries.add(new BarEntry(finalI*1F, (int) Sum));
                                avg.add((int)Sum);
                                draw("month");
                            }
                            else {
                                barEntries.add(new BarEntry(finalI * 1F, meter * 1F));
                                avg.add(meter);
                            }
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
        xlabel = new ArrayList<>();
        List<Integer> color = new ArrayList<>();
        BarData data1 = null;

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
                    calendar.add(month, 1);
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
                BarDataSet dataSet1 = new BarDataSet(barEntries, "");
                dataSet1.setColors(color);
                dataSet1.setDrawValues(false);

                data1 = new BarData(dataSet1);

                binding.barchart.getAxisLeft().setGranularity(50F);
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

                BarDataSet dataSet1 = new BarDataSet(barEntries, "");
                dataSet1.setColors(color);
                dataSet1.setValueTextSize(16F);

                data1 = new BarData(dataSet1);
                binding.barchart.getAxisLeft().setDrawLabels(false);
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

                data1 = new BarData(dataSet1);
                binding.barchart.getAxisLeft().setDrawLabels(false);
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

        LineDataSet dataSet = new LineDataSet(lineEntries, "4월");

        dataSet.setColors(Color.rgb(80,200,255));
        dataSet.setValueTextColor(Color.WHITE);

        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setCircleRadius(1);
        dataSet.setCircleColor(Color.parseColor("#FFA1B4DC"));
        dataSet.setCircleHoleColor(Color.BLUE);

        LineDataSet compdataSet = new LineDataSet(complineEntries, "3월");
        compdataSet.setColors(Color.rgb(230,230,230));
        compdataSet.setValueTextColor(Color.WHITE);
        compdataSet.setFillColor(Color.rgb(230,230,230));
        compdataSet.setDrawFilled(true);

        compdataSet.setDrawCircles(false);

        xlabel = new ArrayList<>();

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
