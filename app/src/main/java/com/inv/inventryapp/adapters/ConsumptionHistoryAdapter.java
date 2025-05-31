package com.inv.inventryapp.adapters;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.inv.inventryapp.R;
import com.inv.inventryapp.models.History;
import com.inv.inventryapp.models.MainItem;
import com.inv.inventryapp.room.MainItemDao;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConsumptionHistoryAdapter extends RecyclerView.Adapter<ConsumptionHistoryAdapter.ViewHolder> {

    private List<History> historyList = new ArrayList<>();
    private MainItemDao mainItemDao;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    public ConsumptionHistoryAdapter(MainItemDao mainItemDao) {
        this.mainItemDao = mainItemDao;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_consumption_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        History historyItem = historyList.get(position);

        holder.historyDateTextView.setText(historyItem.getDate().format(dateFormatter));

        // Set a placeholder text while loading
        holder.historyItemNameTextView.setText("読み込み中...");

        executor.execute(() -> {
            // Background thread
            final MainItem item = mainItemDao.getMainItemById(historyItem.getItemId());

            mainThreadHandler.post(() -> {
                // Main thread
                // Check if the ViewHolder is still bound to the same item.
                if (holder.getBindingAdapterPosition() == position) {
                    holder.historyItemNameTextView.setText(item != null ? item.getName() : "不明な商品");
                }
            });
        });

        String quantityPrefix = "output".equals(historyItem.getType()) || "delete".equals(historyItem.getType()) ? "-" : "+";
        holder.historyQuantityTextView.setText(String.format(Locale.getDefault(), "%s%d個", quantityPrefix, historyItem.getQuantity()));
        holder.historyReasonTextView.setText(historyItem.getConsumptionReason() != null ? historyItem.getConsumptionReason() : historyItem.getType());
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    public void submitList(List<History> newHistoryList) {
        historyList.clear();
        if (newHistoryList != null) {
            historyList.addAll(newHistoryList);
        }
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView historyDateTextView;
        TextView historyItemNameTextView;
        TextView historyQuantityTextView;
        TextView historyReasonTextView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            historyDateTextView = itemView.findViewById(R.id.historyDateTextView);
            historyItemNameTextView = itemView.findViewById(R.id.historyItemNameTextView);
            historyQuantityTextView = itemView.findViewById(R.id.historyQuantityTextView);
            historyReasonTextView = itemView.findViewById(R.id.historyReasonTextView);
        }
    }
}

