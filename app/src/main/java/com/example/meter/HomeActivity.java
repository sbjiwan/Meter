package com.example.meter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import com.example.meter.databinding.ActivityHomeBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class HomeActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private final FragmentManager fragmentManager = getSupportFragmentManager();
    private HomeFragment frag1;
    private StatisticsFragment frag2;
    private HistoryFragment frag3;
    private ExampleFragment frag4;
    private MypageFragment frag5;

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        com.example.meter.databinding.ActivityHomeBinding binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        BottomNavigationView bottomNavigationView = binding.bottomNavi;

        bottomNavigationView.setOnItemReselectedListener(menuItem -> {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            switch (menuItem.getItemId()) {
                case R.id.action_home:
                    transaction.replace(R.id.content_main, frag1).commit();
                    break;
                case R.id.action_history1:
                    transaction.replace(R.id.content_main, frag2).commit();
                    break;
                case R.id.action_history2:
                    transaction.replace(R.id.content_main, frag3).commit();
                    break;
                case R.id.action:
                    transaction.replace(R.id.content_main, frag4).commit();
                    break;
                case R.id.action_mypage:
                    transaction.replace(R.id.content_main, frag5).commit();
                    break;
            }
        });

        frag1 = new HomeFragment();
        frag2 = new StatisticsFragment();
        frag3 = new HistoryFragment();
        frag4 = new ExampleFragment();
        frag5 = new MypageFragment();

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.content_main, frag1).commit();
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

}