package com.example.meter;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.meter.databinding.FragmentHomeBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;

public class HomeFragment extends Fragment {
    private ArrayList<String> selectMonth;
    private FragmentHomeBinding binding;
    private final Source source = Source.CACHE;
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final CollectionReference reference = FirebaseFirestore.getInstance().collection("Water_User");
    private final Calendar t_calendar = Calendar.getInstance();
    private final int now_month = t_calendar.get(Calendar.MONTH);
    private final int now_year = t_calendar.get(Calendar.YEAR);
    private HistoryhomeFragment historyhomeFragment;

    @SuppressLint({"ClickableViewAccessibility", "NonConstantResourceId"})
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);

        reference.document(Objects.requireNonNull(Objects.requireNonNull(auth.getCurrentUser()).getEmail()))
                .get(source)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        String ym = Objects.requireNonNull(Objects.requireNonNull(
                                document.getData()).getOrDefault("start", 0)).toString();
                        String[] yms = ym.split("/");

                        int syear = Integer.parseInt(yms[0]);
                        int smonth = Integer.parseInt(yms[1]);

                        selectMonth = new ArrayList<>();
                        selectMonth.add(syear +"년 " + smonth + "월");

                        while (syear != now_year || smonth != now_month + 1) {
                            smonth ++;
                            if(smonth == 13) {
                                smonth = 1;
                                syear ++;
                            }
                            selectMonth.add(syear +"년 " + smonth + "월");
                        }

                        ArrayAdapter<String> adapter;
                        if(getContext() != null) {
                            adapter = new ArrayAdapter<>(getContext(),
                                    android.R.layout.simple_spinner_item, selectMonth);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            binding.spinner.setAdapter(adapter);
                        }

                        binding.spinner.setSelection(selectMonth.size()-1);
                        binding.spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                                System.out.println(selectMonth.get(position));
                                Bundle bundle = new Bundle();
                                bundle.putString("select", selectMonth.get(position));
                                historyhomeFragment = new HistoryhomeFragment();
                                historyhomeFragment.setArguments(bundle);
                                setCurrentFragment(historyhomeFragment);
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> adapterView) {

                            }
                        });
                    }
                });
        historyhomeFragment = new HistoryhomeFragment();
        return binding.getRoot();
    }

    private void setCurrentFragment(Fragment fragment) {
        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.history_container, fragment).commit();
    }
}
