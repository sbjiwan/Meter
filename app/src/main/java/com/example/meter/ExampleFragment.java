package com.example.meter;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.meter.databinding.FragmentExampleBinding;

public class ExampleFragment extends Fragment {
    private FragmentExampleBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentExampleBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }
}
