package com.github.tvbox.osc.ui.adapter;

import android.text.TextUtils;
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

import java.util.ArrayList;
import java.util.List;

public class SubscriptionAdapter extends ListAdapter<Subscription, SubscriptionAdapter.ViewHolder> {

    public interface SubscriptionInterface {
        void click(Subscription item);
        void del(Subscription item);
    }

    private ArrayList<Subscription> data = new ArrayList<>();
    private SubscriptionInterface listener;

    public SubscriptionAdapter(SubscriptionInterface listener) {
        super(new DiffUtil.ItemCallback<Subscription>() {
            @Override
            public boolean areItemsTheSame(@NonNull Subscription oldItem, @NonNull Subscription newItem) {
                return oldItem.getUrl().equals(newItem.getUrl());
            }

            @Override
            public boolean areContentsTheSame(@NonNull Subscription oldItem, @NonNull Subscription newItem) {
                return oldItem.getName().equals(newItem.getName()) && oldItem.isChecked() == newItem.isChecked();
            }
        });
        this.listener = listener;
    }

    public void setData(List<Subscription> newData) {
        data.clear();
        data.addAll(newData);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_subscription, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Subscription item = data.get(position);
        String prefix = item.isChecked() ? "✓ " : "  ";
        String multiTag = !TextUtils.isEmpty(item.getMultiUrl()) ? "[多仓] " : "";
        holder.tvName.setText(prefix + multiTag + item.getName());
        holder.tvUrl.setText(item.getUrl());
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.click(item);
        });
        holder.tvDel.setOnClickListener(v -> {
            if (listener != null) listener.del(item);
        });
        holder.ivPushpin.setVisibility(item.isTop() ? View.VISIBLE : View.GONE);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        TextView tvUrl;
        TextView tvDel;
        TextView ivPushpin;

        ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvUrl = itemView.findViewById(R.id.tvUrl);
            tvDel = itemView.findViewById(R.id.tvDel);
            ivPushpin = itemView.findViewById(R.id.ivPushpin);
        }
    }
}
