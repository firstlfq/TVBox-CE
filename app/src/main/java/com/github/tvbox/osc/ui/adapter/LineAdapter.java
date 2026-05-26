package com.github.tvbox.osc.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.github.tvbox.osc.R;
import com.github.tvbox.osc.bean.Subscription;

import org.jetbrains.annotations.NotNull;

public class LineAdapter extends ListAdapter<Subscription.Line, LineAdapter.ViewHolder> {

    public interface LineInterface {
        void onLineClick(Subscription.Line item, int index);
    }

    private LineInterface listener;
    private int selectedIndex = -1;

    public LineAdapter(LineInterface listener) {
        super(new DiffUtil.ItemCallback<Subscription.Line>() {
            @Override
            public boolean areItemsTheSame(@NonNull Subscription.Line oldItem, @NonNull Subscription.Line newItem) {
                return oldItem.getUrl() != null && oldItem.getUrl().equals(newItem.getUrl());
            }

            @Override
            public boolean areContentsTheSame(@NonNull Subscription.Line oldItem, @NonNull Subscription.Line newItem) {
                if (oldItem.getName() == null || newItem.getName() == null) return false;
                return oldItem.getName().equals(newItem.getName()) && java.util.Objects.equals(oldItem.getUrl(), newItem.getUrl());
            }
        });
        this.listener = listener;
    }

    public void setSelectedIndex(int index) {
        selectedIndex = index;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return getCurrentList().size();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_line, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Subscription.Line item = getItem(position);
        boolean isSelected = (position == selectedIndex);

        holder.tvCheck.setText(isSelected ? "✓" : "");
        holder.tvCheck.setVisibility(isSelected ? View.VISIBLE : View.INVISIBLE);
        holder.tvLineName.setText(item.getName() != null ? item.getName() : "");
        holder.tvLineUrl.setText(item.getUrl() != null ? item.getUrl() : "");

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onLineClick(item, position);
        });
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCheck;
        TextView tvLineName;
        TextView tvLineUrl;

        ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            tvCheck = itemView.findViewById(R.id.tvCheck);
            tvLineName = itemView.findViewById(R.id.tvLineName);
            tvLineUrl = itemView.findViewById(R.id.tvLineUrl);
        }
    }
}
