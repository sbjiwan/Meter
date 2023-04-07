package com.example.meter;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.meter.databinding.ActivitySigninBinding;
import com.google.firebase.auth.FirebaseAuth;

public class SigninActivity extends AppCompatActivity {
    private ActivitySigninBinding binding;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySigninBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.back.setOnClickListener(view -> startActivity(new Intent(SigninActivity.this, LoginActivity.class)));

        binding.signIn.setOnClickListener(view -> {
            String id = binding.idEdit.getText().toString();
            String password = binding.passwordEdit.getText().toString();
            String passwordConfirm = binding.passwordConfirmed.getText().toString();

            binding.idEdit.setText("");
            binding.passwordEdit.setText("");
            binding.passwordConfirmed.setText("");

            boolean verifiedGo = true;

            if (id.isEmpty() || 20 < id.length()) {
                Toast.makeText(binding.getRoot().getContext(),"올바른 id를 입력해주세요",Toast.LENGTH_SHORT).show();
                verifiedGo = false;
            } else if (password.isEmpty()) {
                Toast.makeText(binding.getRoot().getContext(),"password를 입력해주세요",Toast.LENGTH_SHORT).show();
                verifiedGo = false;
            } else if (passwordConfirm.isEmpty()) {
                Toast.makeText(binding.getRoot().getContext(),"password확인을 입력해주세요",Toast.LENGTH_SHORT).show();
                verifiedGo = false;
            } else if (!password.equals(passwordConfirm)) {
                Toast.makeText(binding.getRoot().getContext(),"비밀번호를 똑같이 입력해 주세요",Toast.LENGTH_SHORT).show();
                verifiedGo = false;
            } else if (password.length() < 6) {
                Toast.makeText(binding.getRoot().getContext(),"6자리 이상 입력해주세요",Toast.LENGTH_SHORT).show();
                verifiedGo = false;
            }
            if(verifiedGo)
                doSigin(id,password);
        });
    }

    private void doSigin(String email, String password) {
        mAuth = FirebaseAuth.getInstance();
        email += "@sns.com";
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(SigninActivity.this, task -> {
                    if (task.isSuccessful()) {
                        // 회원가입 성공시, 사용자 정보 업데이트
                        mAuth.getCurrentUser();
                        Toast.makeText(binding.getRoot().getContext(), "회원가입에 성공하였습니다.",
                                Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(SigninActivity.this, LoginActivity.class));
                    } else {
                        // 회원가입 실패시 사용자에게 보여줄 메세지
                        Toast.makeText(binding.getRoot().getContext(), "회원가입에 실패했습니다.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
