package com.example.weatherapp.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.weatherapp.Domains.Hourly;
import com.example.weatherapp.R;

import java.util.ArrayList;

public class HourlyAdapter extends RecyclerView.Adapter<HourlyAdapter.viewHolder> {
    ArrayList<Hourly> item;
    Context context;

    public HourlyAdapter(ArrayList<Hourly> item) {
        this.item = item;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context= parent.getContext();
        return new viewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_hourly, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        holder.hour.setText(item.get(position).getHour());
        holder.temp.setText(String.format("%d",(int)(item.get(position).getTemp())).toString()+"°C");

        int drawableResourceId= holder.pic.getResources().getIdentifier(item.get(position).getPicPath(),"drawable", holder.pic.getContext().getPackageName());

        Glide.with(context)
                .load(drawableResourceId)
                .into(holder.pic);
    }

    @Override
    public int getItemCount() {
        return item.size();
    }

    public class viewHolder extends RecyclerView.ViewHolder {
        TextView hour, temp;
        ImageView pic;
        public viewHolder(@NonNull View itemView) {
            super(itemView);

            hour= itemView.findViewById(R.id.hourTxt);
            pic= itemView.findViewById(R.id.pic);
            temp= itemView.findViewById(R.id.tempTxt);
        }
    }
}
