package com.example.meter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.example.meter.databinding.ActivityHomeBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeActivity extends AppCompatActivity {

    private HomeFragment frag1;
    private StatisticsFragment frag2;
    private HistoryFragment frag3;
    private ExampleFragment frag4;
    private MypageFragment frag5;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        com.example.meter.databinding.ActivityHomeBinding binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView bottomNavigationView = binding.bottomNavi;

        bottomNavigationView.setOnItemReselectedListener(menuItem -> {
            switch (menuItem.getItemId()) {
                case R.id.action_home:
                    setFrag(1);
                    break;
                case R.id.action_history1:
                    setFrag(2);
                    break;
                case R.id.action_history2:
                    setFrag(3);
                    break;
                case R.id.action:
                    setFrag(4);
                    break;
                case R.id.action_mypage:
                    setFrag(5);
                    break;
            }
        });

        frag1 = new HomeFragment();
        frag2 = new StatisticsFragment();
        frag3 = new HistoryFragment();
        frag4 = new ExampleFragment();
        frag5 = new MypageFragment();
        setFrag(1);
    }

    public void setFrag(int n) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        switch (n)
        {
            case 1:
                ft.replace(R.id.content_main,frag1);
                ft.commit();
                break;
            case 2:
                ft.replace(R.id.content_main,frag2);
                ft.commit();
                break;
            case 3:
                ft.replace(R.id.content_main,frag3);
                ft.commit();
                break;
            case 4:
                ft.replace(R.id.content_main,frag4);
                ft.commit();
                break;
            case 5:
                ft.replace(R.id.content_main,frag5);
                ft.commit();
                break;
        }
    }
}