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

public class SubscriptionAdapter extends ListAdapter<Subscription, SubscriptionAdapter.ViewHolder> {

    public interface SourceInterface {
        void onSourceClick(Subscription item);
        void onSourceDelete(Subscription item);
    }

    private SourceInterface listener;

    public SubscriptionAdapter(SourceInterface listener) {
        super(new DiffUtil.ItemCallback<Subscription>() {
            @Override
            public boolean areItemsTheSame(@NonNull Subscription oldItem, @NonNull Subscription newItem) {
                String oldKey = oldItem.getMultiUrl() != null ? oldItem.getMultiUrl() : oldItem.getUrl();
                String newKey = newItem.getMultiUrl() != null ? newItem.getMultiUrl() : newItem.getUrl();
                return oldKey != null && oldKey.equals(newKey);
            }

            @Override
            public boolean areContentsTheSame(@NonNull Subscription oldItem, @NonNull Subscription newItem) {
                return oldItem.getSelectedIndex() == newItem.getSelectedIndex()
                        && java.util.Objects.equals(oldItem.getName(), newItem.getName());
            }
        });
        this.listener = listener;
    }

    @Override
    public int getItemCount() {
        return getCurrentList().size();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_source, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Subscription item = getItem(position);
        int lineCount = item.getLineCount();
        String firstLineUrl = (lineCount == 1 && item.getLines().get(0) != null) ? item.getLines().get(0).getUrl() : null;
        boolean isMulti = lineCount > 1 || (lineCount == 1 && !TextUtils.isEmpty(item.getMultiUrl()) && !item.getMultiUrl().equals(firstLineUrl));

        holder.tvMultiTag.setText(isMulti ? "📦" : "📄");
        holder.tvName.setText(item.getName());

        Subscription.Line selected = item.getSelectedLine();
        String subtitle;
        if (lineCount == 0) {
            subtitle = "未解析";
        } else if (lineCount == 1) {
            subtitle = "1条线路";
        } else {
            subtitle = lineCount + "条线路";
        }
        if (selected != null) {
            subtitle += " · 当前: " + selected.getName();
        } else if (lineCount > 0) {
            subtitle += " · 未选择";
        }
        holder.tvSubtitle.setText(subtitle);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onSourceClick(item);
        });

        holder.tvDel.setOnClickListener(v -> {
            if (listener != null) listener.onSourceDelete(item);
        });
        holder.tvDel.setVisibility(isMulti || lineCount > 0 ? View.VISIBLE : View.VISIBLE);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMultiTag;
        TextView tvName;
        TextView tvSubtitle;
        TextView tvDel;

        ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            tvMultiTag = itemView.findViewById(R.id.tvMultiTag);
            tvName = itemView.findViewById(R.id.tvName);
            tvSubtitle = itemView.findViewById(R.id.tvSubtitle);
            tvDel = itemView.findViewById(R.id.tvDel);
        }
    }
}
