package com.example.meter;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.meter.databinding.ActivityLoginBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;
    private FirebaseAuth mAuth;
    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private final Source source = Source.CACHE;
    private final CollectionReference userReference = firestore.collection("user");
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

            getSalt(id, password);
        });
    }
    private void getSalt(String email, String password) {
        userReference.document(email).get(source).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                doLogin(email, password, String.valueOf(Objects.requireNonNull(document.getData()).getOrDefault("salt", "")));
            } else {
                System.out.println("false get salt");
                Log.w(TAG, "Error getting documents.", task.getException());
            }
        });
    }
    private void doLogin(String email, String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update((password + salt).getBytes(StandardCharsets.UTF_8));
            byte[] pwdsalt = md.digest();

            StringBuilder sb = new StringBuilder();
            for(byte bb : pwdsalt)
                sb.append(String.format("%02x", bb));

            mAuth = FirebaseAuth.getInstance();
            mAuth.signInWithEmailAndPassword(email, sb.toString())
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

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
