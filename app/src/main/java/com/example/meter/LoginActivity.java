package com.example.meter;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.meter.databinding.ActivityLoginBinding;
import com.google.firebase.auth.FirebaseAuth;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;
    private FirebaseAuth mAuth;
    private boolean check_email, check_pw = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.signin.setOnClickListener(view -> {
            Intent intent = new Intent(LoginActivity.this, SigninActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        binding.idInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Pattern pattern = Pattern.compile("\\w+@\\w+" + ".com");
                Matcher matcher = pattern.matcher(charSequence);

                if(!matcher.find()) {
                    binding.wrongId.setText(R.string.wrong_id);
                    check_email = false;
                }
                else {
                    binding.wrongId.setText("");
                    check_email = true;
                }
                binding.login.setEnabled(check_email && check_pw);}
            @Override
            public void afterTextChanged(Editable editable) {}
        });

        binding.passwordInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(i < 7) {
                    binding.wrongPw.setText(R.string.wrong_pw);
                    check_pw = false;
                }
                else {
                    binding.wrongPw.setText("");
                    check_pw = true;
                }
                binding.login.setEnabled(check_email && check_pw);}
            @Override
            public void afterTextChanged(Editable editable) {}
        });

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
        mAuth.signInWithEmailAndPassword(id, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // 로그인 성공시
                        mAuth.getCurrentUser();
                        Toast.makeText(binding.getRoot().getContext(), "로그인에 성공하였습니다.",
                                Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(this, HomeActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    } else {
                        // 로그인 실패시
                        Toast.makeText(this, "로그인에 실패했습니다.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
