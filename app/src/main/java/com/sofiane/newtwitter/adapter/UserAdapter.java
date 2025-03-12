package com.sofiane.newtwitter.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.sofiane.newtwitter.R;
import com.sofiane.newtwitter.model.User;
import com.sofiane.newtwitter.utils.FollowManager;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<User> userList;
    private Context context;
    private OnUserInteractionListener listener;
    private FollowManager followManager;
    private String currentUserId;

    public interface OnUserInteractionListener {
        void onUserClicked(User user);
        void onFollowClicked(User user, boolean isFollowing);
    }

    public UserAdapter(Context context, OnUserInteractionListener listener) {
        this.userList = new ArrayList<>();
        this.context = context;
        this.listener = listener;
        this.followManager = new FollowManager();
        this.currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null ? 
                FirebaseAuth.getInstance().getCurrentUser().getUid() : "";
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        
        holder.nameText.setText(user.getUsername());
        holder.usernameText.setText("@" + user.getUsername().toLowerCase().replace(" ", ""));
        
        // Load profile image
        if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(user.getProfileImageUrl())
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .into(holder.profileImage);
        } else {
            holder.profileImage.setImageResource(R.drawable.ic_launcher_foreground);
        }
        
        // Hide follow button for current user
        if (user.getUserId().equals(currentUserId)) {
            holder.followButton.setVisibility(View.GONE);
        } else {
            holder.followButton.setVisibility(View.VISIBLE);
            
            // Check follow status
            followManager.checkFollowStatus(user.getUserId(), new FollowManager.FollowStatusListener() {
                @Override
                public void onStatus(boolean isFollowing) {
                    if (isFollowing) {
                        holder.followButton.setText(R.string.unfollow);
                        holder.followButton.setBackgroundResource(R.drawable.button_outline_background);
                    } else {
                        holder.followButton.setText(R.string.follow);
                        holder.followButton.setBackgroundResource(R.drawable.button_primary_background);
                    }
                    
                    // Set follow button click listener
                    holder.followButton.setOnClickListener(v -> {
                        if (holder.followButton.getText().toString().equals(context.getString(R.string.follow))) {
                            listener.onFollowClicked(user, false);
                        } else {
                            listener.onFollowClicked(user, true);
                        }
                    });
                }
                
                @Override
                public void onError(String message) {
                    // Default to follow button
                    holder.followButton.setText(R.string.follow);
                    holder.followButton.setBackgroundResource(R.drawable.button_primary_background);
                }
            });
        }
        
        // Set click listener for the whole item
        holder.itemView.setOnClickListener(v -> listener.onUserClicked(user));
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }
    
    public void setUsers(List<User> users) {
        this.userList = users;
        notifyDataSetChanged();
    }
    
    public void addUser(User user) {
        if (!userList.contains(user)) {
            userList.add(user);
            notifyItemInserted(userList.size() - 1);
        }
    }
    
    public void removeUser(User user) {
        int position = userList.indexOf(user);
        if (position != -1) {
            userList.remove(position);
            notifyItemRemoved(position);
        }
    }
    
    public void clear() {
        userList.clear();
        notifyDataSetChanged();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        CircleImageView profileImage;
        TextView nameText;
        TextView usernameText;
        Button followButton;
        View divider;

        UserViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.profileImage);
            nameText = itemView.findViewById(R.id.nameText);
            usernameText = itemView.findViewById(R.id.usernameText);
            followButton = itemView.findViewById(R.id.followButton);
            divider = itemView.findViewById(R.id.divider);
        }
    }
} 