package com.example.memorize;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.memorize.Collection;
import com.example.memorize.R;

import java.util.List;

public class CollectionsAdapter extends RecyclerView.Adapter<CollectionsAdapter.ViewHolder> {

    private List<Collection> collections;

    public CollectionsAdapter(List<Collection> collections) {
        this.collections = collections;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_collection, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Collection collection = collections.get(position);
        holder.textViewCollectionName.setText(collection.getName());
        // Configurar mais elementos da CardView aqui, se necessário

        // Define o OnClickListener
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), CollectionDetailsActivity.class);
                intent.putExtra("collectionId", collection.getId());
                intent.putExtra("collectionName", collection.getName());
                v.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return collections.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewCollectionName;

        public ViewHolder(View view) {
            super(view);
            textViewCollectionName = view.findViewById(R.id.textViewCollectionName);
            // Configure outros elementos da view aqui, se necessário
        }
    }
}
