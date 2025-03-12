package com.sofiane.newtwitter.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.sofiane.newtwitter.R;
import com.sofiane.newtwitter.model.Post;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {
    private List<Post> posts = new ArrayList<>();
    private OnPostInteractionListener listener;

    public interface OnPostInteractionListener {
        void onPostLiked(Post post);
        void onPostClicked(Post post);
    }

    public PostAdapter(OnPostInteractionListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = posts.get(position);
        holder.bind(post);
    }

    @Override
    public int getItemCount() {
        return posts != null ? posts.size() : 0;
    }

    public void setPosts(List<Post> posts) {
        this.posts = posts != null ? posts : new ArrayList<>();
        notifyDataSetChanged();
    }

    class PostViewHolder extends RecyclerView.ViewHolder {
        private CircleImageView profileImageView;
        private TextView usernameTextView;
        private TextView timeTextView;
        private TextView contentTextView;
        private MaterialButton likeButton;
        private TextView likeCountTextView;
        private MaterialButton commentButton;
        private TextView commentCountTextView;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImageView = itemView.findViewById(R.id.profileImageView);
            usernameTextView = itemView.findViewById(R.id.usernameTextView);
            timeTextView = itemView.findViewById(R.id.timeTextView);
            contentTextView = itemView.findViewById(R.id.contentTextView);
            likeButton = itemView.findViewById(R.id.likeButton);
            likeCountTextView = itemView.findViewById(R.id.likeCountTextView);
            commentButton = itemView.findViewById(R.id.commentButton);
            commentCountTextView = itemView.findViewById(R.id.commentCountTextView);

            // Set click listeners
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onPostClicked(posts.get(position));
                }
            });

            likeButton.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onPostLiked(posts.get(position));
                }
            });
        }

        public void bind(Post post) {
            // Set profile image
            profileImageView.setImageResource(R.drawable.ic_person);
            
            usernameTextView.setText(post.getUsername());
            timeTextView.setText(post.getRelativeTime());
            contentTextView.setText(post.getContent());
            likeCountTextView.setText(String.valueOf(post.getLikeCount()));
            commentCountTextView.setText(String.valueOf(post.getCommentCount()));
        }
    }
} 