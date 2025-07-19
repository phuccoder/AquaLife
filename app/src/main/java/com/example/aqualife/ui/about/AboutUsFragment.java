package com.example.aqualife.ui.about;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import android.Manifest;
import com.example.aqualife.R;
import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

public class AboutUsFragment extends Fragment {

    private static final String TAG = "AboutUsFragment";
    private static final int REQUEST_CALL_PERMISSION = 1;
    private final String supportPhone = "0967630810";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_about_us, container, false);

        Glide.with(this).asGif().load(R.drawable.about_us_aquarium).into((ImageView) view.findViewById(R.id.image_aquarium));
        Glide.with(this).asGif().load(R.drawable.about_us_fish).into((ImageView) view.findViewById(R.id.image_fish));
        Glide.with(this).asGif().load(R.drawable.about_us_food).into((ImageView) view.findViewById(R.id.image_food));
        Glide.with(this).asGif().load(R.drawable.about_us_medicine).into((ImageView) view.findViewById(R.id.image_medicine));

        MapView mapView = view.findViewById(R.id.mapView);
        Configuration.getInstance().setUserAgentValue(requireContext().getPackageName());
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(17.0);
        GeoPoint storeLocation = new GeoPoint(10.7991, 106.6848);
        mapView.getController().setCenter(storeLocation);

        Marker marker = new Marker(mapView);
        marker.setPosition(storeLocation);
        marker.setTitle("Cửa Hàng AquaLife");
        mapView.getOverlays().add(marker);

        View mapOverlay = view.findViewById(R.id.map_overlay);
        mapOverlay.setOnClickListener(v -> {
            double lat = 10.7991;
            double lng = 106.6848;
            String label = "Cửa Hàng AquaLife";
            String uri = "https://www.google.com/maps/dir/?api=1"
                    + "&destination=" + lat + "," + lng
                    + "&destination_place_id="
                    + "&travelmode=driving";
            Intent intent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(uri));
            intent.setPackage("com.google.android.apps.maps");
            startActivity(intent);
        });

        Button btnCallStore = view.findViewById(R.id.btn_call_store);
        btnCallStore.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_CALL);
            intent.setData(Uri.parse("tel:" + supportPhone));

            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CALL_PHONE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(),
                        new String[]{Manifest.permission.CALL_PHONE},
                        REQUEST_CALL_PERMISSION);
            } else {
                startActivity(intent);
            }
        });

        return view;
    }
}