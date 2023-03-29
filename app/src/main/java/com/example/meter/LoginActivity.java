package com.example.meter;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.meter.databinding.ActivityLoginBinding;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, SigninActivity.class));
            }
        });
        //binding.signin.setOnClickListener(view -> startActivity(new Intent(LoginActivity.this, SigninActivity.class)));

        binding.login.setOnClickListener(view -> {
            String id = binding.idInput.getText().toString();
            String password = binding.passwordInput.getText().toString();

            doLogin(id, password);
        });
    }

    private void doLogin(String id, String password) {
        binding.idInput.setText("");
        binding.passwordInput.setText("");
        mAuth = FirebaseAuth.getInstance();

        id += "@sns.com";

        mAuth.signInWithEmailAndPassword(id, password)
                .addOnCompleteListener(LoginActivity.this, task -> {
                    if (task.isSuccessful()) {
                        // 로그인 성공시
                        mAuth.getCurrentUser();
                        Toast.makeText(binding.getRoot().getContext(), "로그인에 성공하였습니다.",
                                Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                    } else {
                        // 로그인 실패시
                        Toast.makeText(LoginActivity.this, "로그인에 실패했습니다.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
