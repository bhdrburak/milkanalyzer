package com.example.milkanalyzer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.example.milkanalyzer.databinding.LayoutEventAdapterBinding;
import com.example.milkanalyzer.object.TakenMilk;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.ViewHolder> {

    public interface onItemClickListener {
        void onSelected();
    }

    private EventAdapter.onItemClickListener mOnItemClickListener;
    private List<TakenMilk> takenMilkList;
    private Context context;

    @NonNull
    @Override
    public EventAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        LayoutEventAdapterBinding binding = LayoutEventAdapterBinding.inflate(inflater, parent, false);
        return new EventAdapter.ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull EventAdapter.ViewHolder holder, int position) {
        holder.bindTo(context, takenMilkList.get(holder.getAdapterPosition()), mOnItemClickListener);
    }

    public List<TakenMilk> getTakenMilkList() {
        return takenMilkList;
    }

    public void setTakenMilkList(List<TakenMilk> takenMilkList) {
        this.takenMilkList = takenMilkList;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return takenMilkList == null ? 0 : takenMilkList.size();
    }

    public EventAdapter(Context context, List<TakenMilk> takenMilkList, EventAdapter.onItemClickListener onItemClickListener) {
        this.takenMilkList = takenMilkList;
        this.context = context;
        this.mOnItemClickListener = onItemClickListener;

    }


    public static class ViewHolder extends RecyclerView.ViewHolder {

        private LayoutEventAdapterBinding binding;
        private Context context;
        private TakenMilk takenMilk;
        private EventAdapter.onItemClickListener mOnItemClickListener;

        public ViewHolder(LayoutEventAdapterBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

        }


        void bindTo(Context context, TakenMilk takenMilk, EventAdapter.onItemClickListener onItemClickListener) {
            this.context = context;
            this.takenMilk = takenMilk;
            mOnItemClickListener = onItemClickListener;
            binding.cardviewEvent.setOnClickListener(view -> mOnItemClickListener.onSelected());
            binding.userId.setText(takenMilk.getUserId());
            binding.takenMilkSize.setText(takenMilk.getTakenMilk());
            binding.takenDate.setText(takenMilk.getTakenMilkDate());
            String countString = String.valueOf(getAdapterPosition() + 1);
            binding.counter.setText(countString);
        }

    }
}