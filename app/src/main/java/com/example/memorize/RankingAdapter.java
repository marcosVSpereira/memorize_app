package com.example.memorize;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class RankingAdapter extends RecyclerView.Adapter<RankingAdapter.ViewHolder> {

    private List<User> rankingList;

    public RankingAdapter(List<User> rankingList) {
        this.rankingList = rankingList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.ranking_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = rankingList.get(position);
        holder.bind(user, position);
    }

    @Override
    public int getItemCount() {
        return rankingList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewRank, textViewUsername, textViewScore;

        ViewHolder(View itemView) {
            super(itemView);
            textViewRank = itemView.findViewById(R.id.textViewRank);
            textViewUsername = itemView.findViewById(R.id.textViewUsername);
            textViewScore = itemView.findViewById(R.id.textViewScore);
        }

        void bind(User user, int position) {
            textViewRank.setText(String.format("%d.", position + 1));
            textViewUsername.setText(user.getNickname());
            textViewScore.setText(String.valueOf(user.getScore()));
        }
    }
}
