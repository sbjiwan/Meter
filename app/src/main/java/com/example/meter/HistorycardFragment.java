package com.example.meter;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.meter.databinding.FragmentHistorycardBinding;

public class HistorycardFragment extends Fragment {

    private FragmentHistorycardBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHistorycardBinding.inflate(inflater, container, false);

        Bundle bundle = getArguments();
        Bundle sbundle = new Bundle();

        if(bundle != null) {
            String select = bundle.get("select").toString();
            sbundle.putString("select", select);
        }

        FragmentStateAdapter pagerAdapter = new ScreenSlidePagerAdapter(getActivity(), sbundle);
        binding.pager.setAdapter(pagerAdapter);

        return binding.getRoot();
    }

    private static class ScreenSlidePagerAdapter extends FragmentStateAdapter {
        Bundle bundle;
        public ScreenSlidePagerAdapter(FragmentActivity fa, Bundle bundle) {
            super(fa);
            this.bundle = bundle;
        }
        @Override
        public Fragment createFragment(int position) {
            if(position == 0) {
                HistorycostFragment historycostFragment = new HistorycostFragment();
                historycostFragment.setArguments(bundle);
                return historycostFragment;
            }
            else {
                HistorytreeFragment historytreeFragment = new HistorytreeFragment();
                historytreeFragment.setArguments(bundle);
                return historytreeFragment;
            }
        }

        @Override
        public int getItemCount() {
            return 2;
        }
    }
}
