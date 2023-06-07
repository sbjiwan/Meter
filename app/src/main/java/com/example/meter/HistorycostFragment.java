package com.example.meter;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.meter.databinding.FragmentHistorycostBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;

import java.text.DecimalFormat;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class HistorycostFragment extends Fragment {
    private FragmentHistorycostBinding binding;
    private final Source source = Source.CACHE;
    private final CollectionReference reference = FirebaseFirestore.getInstance().collection("Water_User");
    private final String id = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail();

    @SuppressLint("SetTextI18n")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHistorycostBinding.inflate(inflater, container, false);

        Bundle bundle = getArguments();
        if(bundle != null) {
            String select = bundle.get("select").toString();
            String[] selects = select.split("/");

            final int year = Integer.parseInt(selects[0]);
            final int month = Integer.parseInt(selects[1]);
            AtomicInteger sum = new AtomicInteger();
            AtomicInteger data = new AtomicInteger();
            AtomicInteger start_year = new AtomicInteger();
            AtomicInteger start_month = new AtomicInteger();

            assert id != null;
            reference.document(id)
                    .get(source)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();

                            String start = Objects.requireNonNull(document.get("start")).toString();
                            String[] starts = start.split("/");

                            start_year.set(Integer.parseInt(starts[0]));
                            start_month.set(Integer.parseInt(starts[1]));

                            int k = (year - start_year.get()) * 12 + month - start_month.get();

                            for(int i = 0; i <= k; i ++) {
                                int finalI = i;

                                reference.document(id)
                                        .collection((start_year.get() + i/12) +"")
                                        .document((start_month.get() + i%12) + "")
                                        .collection("SUM")
                                        .document("METER")
                                        .get(source)
                                        .addOnCompleteListener(tasks -> {
                                            if(tasks.isSuccessful()) {
                                                DocumentSnapshot document1 = tasks.getResult();
                                                int meter = Integer.parseInt(Objects.requireNonNull(Objects.requireNonNull(
                                                        document1.getData()).getOrDefault("meter", 0)).toString());

                                                if((year == (start_year.get() + finalI /12)) && (month == (start_month.get() + finalI %12))) {
                                                    data.set(meter);
                                                }
                                                sum.set(sum.get() + meter);

                                                @SuppressLint("DefaultLocale") String meter1 = String.format("%.1f" ,data.get()/1000.0);
                                                binding.meter.setText(month + " 월 사용량은 " + data.get() + " L 예요");

                                                @SuppressLint("DefaultLocale") String avg = String.format("%.1f", sum.get()/k/1000.0);

                                                binding.avgusage.setText(avg + " t");
                                                binding.textView2.setText(month +" 월");
                                                binding.thisusage.setText(meter1 + " t");

                                                DecimalFormat df = new DecimalFormat("###,###");

                                                String avgcost = df.format(Double.parseDouble(avg) * 580);
                                                String thiscost = df.format(Double.parseDouble(meter1) * 580);
                                                String ckcost;
                                                if(Double.parseDouble(avg) < Double.parseDouble(meter1)) {
                                                    ckcost = df.format(Double.parseDouble(meter1) * 580 - Double.parseDouble(avgcost) * 580);
                                                    binding.cost.setText("한달 평균 요금 보다 " + ckcost +" 원 더 사용했어요");
                                                }
                                                else {
                                                    ckcost = df.format(Double.parseDouble(avg) * 580 - Double.parseDouble(meter1) * 580);
                                                    binding.cost.setText("한달 평균 요금 보다 " + ckcost +" 원 덜 사용했어요");
                                                }

                                                binding.avgcost.setText(avgcost + " 원");
                                                binding.thiscost.setText(thiscost + " 원");

                                            }
                                        });
                            }
                        }
            });
        }
        return binding.getRoot();
    }
}
