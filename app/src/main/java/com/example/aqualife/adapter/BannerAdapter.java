package com.example.aqualife.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.aqualife.R;

import java.util.List;

public class BannerAdapter extends RecyclerView.Adapter<BannerAdapter.BannerViewHolder> {
    private List<Integer> bannerList;
    private Context context;

    public BannerAdapter(Context context, List<Integer> bannerList) {
        this.context = context;
        this.bannerList = bannerList;
    }

    @NonNull
    @Override
    public BannerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.banner_item, parent, false);
        return new BannerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BannerViewHolder holder, int position) {
        try {
            // Using Glide to handle both static images and GIFs
            Glide.with(context)
                    .load(bannerList.get(position))
                    .into(holder.bannerImage);
        } catch (Resources.NotFoundException e) {
            Log.e("BannerAdapter", "Resource not found: " + e.getMessage());
        } catch (Exception e) {
            Log.e("BannerAdapter", "Error loading image: " + e.getMessage());
        }
    }

    @Override
    public int getItemCount() {
        return bannerList != null ? bannerList.size() : 0;
    }

    static class BannerViewHolder extends RecyclerView.ViewHolder {
        ImageView bannerImage;

        public BannerViewHolder(@NonNull View itemView) {
            super(itemView);
            bannerImage = itemView.findViewById(R.id.bannerImage);
        }
    }
}