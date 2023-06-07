package com.example.meter;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.meter.databinding.FragmentHistorytreeBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;

import java.util.Objects;

public class HistorytreeFragment extends Fragment {

    private FragmentHistorytreeBinding binding;
    private final Source source = Source.CACHE;
    private final CollectionReference reference = FirebaseFirestore.getInstance().collection("Water_User");
    private final String id = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHistorytreeBinding.inflate(inflater, container, false);

        Bundle bundle = getArguments();
        if(bundle != null) {
            String select = bundle.get("select").toString();
            String[] selects = select.split("/");

            final int year = Integer.parseInt(selects[0]);
            final int month = Integer.parseInt(selects[1]);

            reference.document(id)
                    .collection(year + "")
                    .document(month + "")
                    .collection("SUM")
                    .document("METER")
                    .get(source)
                    .addOnCompleteListener(tasks -> {
                        if (tasks.isSuccessful()) {
                            DocumentSnapshot document1 = tasks.getResult();
                            int meter = Integer.parseInt(Objects.requireNonNull(Objects.requireNonNull(
                                    document1.getData()).getOrDefault("meter", 0)).toString());

                            binding.meter.setText(month + " 월 사용량은 " + meter + " L 예요");

                            double gas = meter*0.332;
                            int tree = (int)gas/980;

                            binding.tree.setText(meter + " L 를 사용해 배출한 온실가스양은\n소나무 " + tree + "그루가 흡수하는 양과 비슷해요");
                        }
                    });
        }
        return binding.getRoot();
    }
}
