package com.example.aqualife.ui.about;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.example.aqualife.R;

public class AboutUsFragment extends Fragment {

    private static final String TAG = "AboutUsFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_about_us, container, false);

        Glide.with(this).asGif().load(R.drawable.about_us_aquarium).into((ImageView) view.findViewById(R.id.image_aquarium));
        Glide.with(this).asGif().load(R.drawable.about_us_fish).into((ImageView) view.findViewById(R.id.image_fish));
        Glide.with(this).asGif().load(R.drawable.about_us_food).into((ImageView) view.findViewById(R.id.image_food));
        Glide.with(this).asGif().load(R.drawable.about_us_medicine).into((ImageView) view.findViewById(R.id.image_medicine));

        return view;
    }
}