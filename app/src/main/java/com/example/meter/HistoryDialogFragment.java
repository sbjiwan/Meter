package com.example.meter;


import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.meter.databinding.FragmentDialogBinding;

public class HistoryDialogFragment extends DialogFragment {
    private FragmentDialogBinding binding;

    @SuppressLint({"ClickableViewAccessibility", "NonConstantResourceId"})
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDialogBinding.inflate(inflater, container, false);

        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getDialog().getWindow().setGravity(Gravity.BOTTOM);

        Bundle bundle = getArguments();
        Bundle bundle1 = new Bundle();
        int year =0 ,month =0 ,date = 0;
        String select = "";

        if (bundle != null) {
            select = bundle.get("select").toString();
            String[] selects = select.split("/");
            bundle1.putString("select", select);

            year = Integer.parseInt(selects[0]);
            month = Integer.parseInt(selects[1]);
            date = Integer.parseInt(selects[2]);
        }

        binding.date.setText(year + "년 " + (month+1) + "월 " + date + "일");

        binding.detail.setOnClickListener(view -> {
            DetailFragment detailFragment = new DetailFragment();
            detailFragment.setArguments(bundle1);
            setCurrentFragment(detailFragment);
        });

        binding.cancel.setOnClickListener(view -> {
            dismiss();
        });
        return binding.getRoot();
    }

    private void setCurrentFragment(Fragment fragment) {
        HomeActivity activity = (HomeActivity) getContext();
        activity.getSupportFragmentManager().beginTransaction().replace(R.id.content_main, fragment).commit();
    }
}
