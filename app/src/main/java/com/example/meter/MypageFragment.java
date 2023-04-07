package com.example.meter;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.meter.databinding.FragmentMypageBinding;

public class MypageFragment extends Fragment {
    private FragmentMypageBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentMypageBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }
}
