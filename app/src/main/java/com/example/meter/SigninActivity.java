package com.example.meter;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.meter.databinding.ActivitySigninBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SigninActivity extends AppCompatActivity {
    private ActivitySigninBinding binding;
    private FirebaseAuth mAuth;
    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private final CollectionReference userReference = firestore.collection("user");
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
                    binding.wrongSignPw.setTextColor(Color.RED);
                    binding.wrongSignPw.setText(R.string.wrong_pw);
                    check_pw = false;}
                else {
                    binding.wrongSignPw.setTextColor(Color.GRAY);
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
                    binding.wrongSignPwcf.setTextColor(Color.RED);
                    binding.wrongSignPwcf.setText(R.string.ckpw_1);
                    check_pwcf = false;}
                else {
                    binding.wrongSignPwcf.setTextColor(Color.GRAY);
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
        SecureRandom sr = new SecureRandom();
        byte[] s = new byte[20];
        sr.nextBytes(s);

        StringBuilder b = new StringBuilder();
        for(byte bb : s)
            b.append(String.format("%02x", bb));

        String salt = b.toString();

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update((password + salt).getBytes(StandardCharsets.UTF_8));
            byte[] pwdsalt = md.digest();

            StringBuilder sb = new StringBuilder();
            for(byte bb : pwdsalt)
                sb.append(String.format("%02x", bb));

            Map<String, Object> map = new HashMap<>();
            map.put("salt", salt);

            mAuth = FirebaseAuth.getInstance();
            mAuth.createUserWithEmailAndPassword(email, sb.toString())
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            // 회원가입 성공시, 사용자 정보 업데이트
                            mAuth.getCurrentUser();
                            Toast.makeText(binding.getRoot().getContext(), "회원가입에 성공하였습니다.",
                                    Toast.LENGTH_SHORT).show();
                            upload(email, sb.toString(), map);
                        } else {
                            // 회원가입 실패시 사용자에게 보여줄 메세지
                            Toast.makeText(binding.getRoot().getContext(), "회원가입에 실패했습니다.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
    private void upload(String email, String password, Map<String, Object> map) {
        userReference.document(email).set(map);
        doLogin(email, password);
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
