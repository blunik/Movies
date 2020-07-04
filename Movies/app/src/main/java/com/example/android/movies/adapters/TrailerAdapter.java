package com.example.android.movies.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android.movies.R;
import com.example.android.movies.data.Trailer;

import java.util.ArrayList;

public class  TrailerAdapter  extends RecyclerView.Adapter<TrailerAdapter.TrailerViewHolder> {

    private ArrayList<Trailer> trailers;
    private OnTrailerClickListener onTrailerClickListener;

    public void setOnTrailerClickListener(OnTrailerClickListener onTrailerClickListener) {
        this.onTrailerClickListener = onTrailerClickListener;
    }

    public void setTrailers(ArrayList<Trailer> trailers) {
        this.trailers = trailers;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TrailerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.trailer_item, parent, false);
        return new TrailerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TrailerViewHolder holder, int position) {
        Trailer trailer = trailers.get(position);
        holder.textViewName.setText(trailer.getName());
    }

    @Override
    public int getItemCount() {
        return trailers.size();
    }

    public interface OnTrailerClickListener{
       void onTrailerClick(String url);
    }

    class TrailerViewHolder extends RecyclerView.ViewHolder {

        private TextView textViewName;


        public TrailerViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.name);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onTrailerClickListener != null){
                        onTrailerClickListener.onTrailerClick(trailers.get(getAdapterPosition()).getKey());
                    }

                }
            });
        }
    }
}
