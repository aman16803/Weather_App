package com.example.weatherapp.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.weatherapp.Domains.Future;
import com.example.weatherapp.R;

import java.util.ArrayList;

public class FutureAdapter extends RecyclerView.Adapter<FutureAdapter.viewHolder> {

    ArrayList<Future> arrayList;
    Context context;

    public FutureAdapter(ArrayList<Future> arrayList) {
        this.arrayList = arrayList;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context= parent.getContext();
        return new viewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_future, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        holder.day.setText(arrayList.get(position).getDay());
        holder.status.setText(arrayList.get(position).getStatus());
        holder.highTemp.setText(arrayList.get(position).getHighTemp()+"°");
        holder.lowTemp.setText(arrayList.get(position).getLowTemp()+"°");

        int drawableResource= holder.img.getResources().getIdentifier(arrayList.get(position).getPicPath(), "drawable", holder.img.getContext().getPackageName());

        Glide.with(context)
                .load(drawableResource)
                .into(holder.img);
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class viewHolder extends RecyclerView.ViewHolder{
        TextView day, status, highTemp, lowTemp;
        ImageView img;

        public viewHolder(@NonNull View itemView) {
            super(itemView);

            day= itemView.findViewById(R.id.day);
            status= itemView.findViewById(R.id.status);
            highTemp= itemView.findViewById(R.id.highTemp);
            lowTemp= itemView.findViewById(R.id.lowTemp);
            img= itemView.findViewById(R.id.image);
        }
    }
}
