package com.example.meter;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.meter.databinding.ActivitySigninBinding;
import com.google.firebase.auth.FirebaseAuth;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SigninActivity extends AppCompatActivity {
    private ActivitySigninBinding binding;
    private FirebaseAuth mAuth;
    private boolean check_email, check_pw, check_pwcf = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySigninBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.signId.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Pattern pattern = Pattern.compile("\\w+@\\w+" + ".com");
                Matcher matcher = pattern.matcher(charSequence);
                if(!matcher.find()) {
                    binding.wrongSignId.setText(R.string.wrong_id);
                    check_email = false;}
                else {
                    binding.wrongSignId.setText("");
                    check_email = true;}
                binding.signIn.setEnabled(check_email && check_pw && check_pwcf);
            }
            @Override
            public void afterTextChanged(Editable editable) {}
        });
        binding.signPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(i < 7) {
                    binding.wrongSignPw.setText(R.string.wrong_pw);
                    check_pw = false;}
                else {
                    binding.wrongSignPw.setText(R.string.right_pw);
                    check_pw = true;}
                binding.signIn.setEnabled(check_email && check_pw && check_pwcf);
            }
            @Override
            public void afterTextChanged(Editable editable) {}
        });
        binding.signPasswordConfirm.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!(binding.signPassword.getText().toString().equals(charSequence.toString()))) {
                    binding.wrongSignPwcf.setText(R.string.ckpw_1);
                    check_pwcf = false;}
                else {
                    binding.wrongSignPwcf.setText(R.string.ckpw_2);
                    check_pwcf = true;}
                binding.signIn.setEnabled(check_email && check_pw && check_pwcf);
            }
            @Override
            public void afterTextChanged(Editable editable) {}
        });

        binding.signIn.setOnClickListener(view -> {
            String id = binding.signId.getText().toString();
            String password = binding.signPassword.getText().toString();

            doSign_in(id, password);
        });
    }

    private void doSign_in(String email, String password) {
        mAuth = FirebaseAuth.getInstance();
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // 회원가입 성공시, 사용자 정보 업데이트
                        mAuth.getCurrentUser();
                        Toast.makeText(binding.getRoot().getContext(), "회원가입에 성공하였습니다.",
                                Toast.LENGTH_SHORT).show();
                        doLogin(email, password);
                    } else {
                        // 회원가입 실패시 사용자에게 보여줄 메세지
                        Toast.makeText(binding.getRoot().getContext(), "회원가입에 실패했습니다.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void doLogin(String email, String password) {
        mAuth = FirebaseAuth.getInstance();
        mAuth.signInWithEmailAndPassword(email, password)
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
