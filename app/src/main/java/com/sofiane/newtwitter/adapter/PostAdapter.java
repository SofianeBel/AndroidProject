package com.sofiane.newtwitter.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sofiane.newtwitter.R;
import com.sofiane.newtwitter.model.Post;
import com.sofiane.newtwitter.utils.ProfileIconHelper;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {
    private List<Post> posts = new ArrayList<>();
    private OnPostInteractionListener listener;
    private Random random = new Random(); // Pour générer des nombres aléatoires pour les compteurs
    private DatabaseReference usersRef;

    public interface OnPostInteractionListener {
        void onPostLiked(Post post);
        void onPostClicked(Post post);
        void onPostRetweeted(Post post);
        void onPostShared(Post post);
        void onPostReplied(Post post);
        void onUserProfileClicked(String userId);
    }

    public PostAdapter(OnPostInteractionListener listener) {
        this.listener = listener;
        // Initialiser la référence à la base de données des utilisateurs
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://newtwitter-65ad1-default-rtdb.europe-west1.firebasedatabase.app");
        usersRef = database.getReference("users");
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
        private TextView handleTextView;
        private TextView timeTextView;
        private TextView contentTextView;
        private ImageView likeIcon;
        private TextView likeCountTextView;
        private ImageView commentIcon;
        private TextView commentCountTextView;
        private ImageView retweetIcon;
        private TextView retweetCountTextView;
        private ImageView shareIcon;
        private ImageView moreOptionsButton;
        private TextView retweetedByTextView;
        private TextView replyingToTextView;
        private View retweetedByLayout;
        private View replyingToLayout;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImageView = itemView.findViewById(R.id.profileImageView);
            usernameTextView = itemView.findViewById(R.id.usernameTextView);
            handleTextView = itemView.findViewById(R.id.handleTextView);
            timeTextView = itemView.findViewById(R.id.timeTextView);
            contentTextView = itemView.findViewById(R.id.contentTextView);
            likeIcon = itemView.findViewById(R.id.likeIcon);
            likeCountTextView = itemView.findViewById(R.id.likeCountTextView);
            commentIcon = itemView.findViewById(R.id.commentIcon);
            commentCountTextView = itemView.findViewById(R.id.commentCountTextView);
            retweetIcon = itemView.findViewById(R.id.retweetIcon);
            retweetCountTextView = itemView.findViewById(R.id.retweetCountTextView);
            shareIcon = itemView.findViewById(R.id.shareIcon);
            moreOptionsButton = itemView.findViewById(R.id.moreOptionsButton);
            
            retweetedByTextView = itemView.findViewById(R.id.retweetedByTextView);
            replyingToTextView = itemView.findViewById(R.id.replyingToTextView);
            retweetedByLayout = itemView.findViewById(R.id.retweetedByLayout);
            replyingToLayout = itemView.findViewById(R.id.replyingToLayout);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onPostClicked(posts.get(position));
                }
            });

            likeIcon.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onPostLiked(posts.get(position));
                }
            });

            retweetIcon.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onPostRetweeted(posts.get(position));
                }
            });

            shareIcon.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onPostShared(posts.get(position));
                }
            });

            commentIcon.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onPostReplied(posts.get(position));
                }
            });

            profileImageView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    Post post = posts.get(position);
                    String userId = post.isRetweet() ? post.getOriginalUserId() : post.getUserId();
                    listener.onUserProfileClicked(userId);
                }
            });

            usernameTextView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    Post post = posts.get(position);
                    String userId = post.isRetweet() ? post.getOriginalUserId() : post.getUserId();
                    listener.onUserProfileClicked(userId);
                }
            });

            handleTextView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    Post post = posts.get(position);
                    String userId = post.isRetweet() ? post.getOriginalUserId() : post.getUserId();
                    listener.onUserProfileClicked(userId);
                }
            });
        }

        public void bind(Post post) {
            // Par défaut, utiliser l'icône de personne
            profileImageView.setImageResource(R.drawable.ic_profile_person);
            
            // Déterminer quel userId utiliser pour charger l'icône de profil
            String userIdToLoad;
            if (post.isRetweet() && retweetedByLayout != null) {
                retweetedByLayout.setVisibility(View.VISIBLE);
                retweetedByTextView.setText(post.getUsername() + " a retweeté");
                
                usernameTextView.setText(post.getOriginalUsername());
                handleTextView.setText("@" + post.getOriginalUsername().toLowerCase().replace(" ", ""));
                
                // Pour un retweet, charger l'icône de profil de l'utilisateur original
                userIdToLoad = post.getOriginalUserId();
            } else {
                if (retweetedByLayout != null) {
                    retweetedByLayout.setVisibility(View.GONE);
                }
                
                usernameTextView.setText(post.getUsername());
                handleTextView.setText("@" + post.getUsername().toLowerCase().replace(" ", ""));
                
                // Pour un post normal, charger l'icône de profil de l'auteur
                userIdToLoad = post.getUserId();
            }
            
            // Charger les informations de profil de l'utilisateur
            loadUserProfileIcon(userIdToLoad, profileImageView);
            
            if (post.isReply() && replyingToLayout != null) {
                replyingToLayout.setVisibility(View.VISIBLE);
                String replyingTo = post.getParentUsername() != null && !post.getParentUsername().isEmpty() 
                    ? "@" + post.getParentUsername().toLowerCase().replace(" ", "") 
                    : post.getParentId();
                replyingToTextView.setText("En réponse à " + replyingTo);
            } else {
                if (replyingToLayout != null) {
                    replyingToLayout.setVisibility(View.GONE);
                }
            }
            
            timeTextView.setText(post.getRelativeTime());
            
            contentTextView.setText(post.getContent());
            
            likeCountTextView.setText(String.valueOf(post.getLikeCount()));
            commentCountTextView.setText(String.valueOf(post.getCommentCount()));
            retweetCountTextView.setText(String.valueOf(post.getRetweetCount()));
            
            if (post.isRetweet()) {
                retweetIcon.setColorFilter(itemView.getContext().getResources().getColor(R.color.twitter_blue));
            } else {
                retweetIcon.setColorFilter(itemView.getContext().getResources().getColor(R.color.twitter_dark_gray));
            }
        }
    }
    
    /**
     * Charge l'icône de profil d'un utilisateur à partir de Firebase
     * @param userId L'ID de l'utilisateur
     * @param imageView La vue d'image à mettre à jour
     */
    private void loadUserProfileIcon(String userId, CircleImageView imageView) {
        if (userId == null || userId.isEmpty()) {
            // Si l'ID utilisateur est invalide, utiliser l'icône par défaut
            imageView.setImageResource(R.drawable.ic_profile_person);
            return;
        }
        
        usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    try {
                        // Récupérer les index d'icône et de couleur
                        Integer iconIndex = snapshot.child("profileIconIndex").getValue(Integer.class);
                        Integer colorIndex = snapshot.child("profileColorIndex").getValue(Integer.class);
                        
                        if (iconIndex != null && colorIndex != null) {
                            Context context = imageView.getContext();
                            // Obtenir l'icône colorée
                            Drawable coloredIcon = ProfileIconHelper.getColoredProfileIcon(
                                    context, iconIndex, colorIndex);
                            
                            // Mettre à jour l'image de profil
                            imageView.setImageDrawable(coloredIcon);
                        } else {
                            // Utiliser l'icône par défaut si les index sont null
                            imageView.setImageResource(R.drawable.ic_profile_person);
                        }
                    } catch (Exception e) {
                        // En cas d'erreur, utiliser l'icône par défaut
                        imageView.setImageResource(R.drawable.ic_profile_person);
                    }
                } else {
                    // Si l'utilisateur n'existe pas, utiliser l'icône par défaut
                    imageView.setImageResource(R.drawable.ic_profile_person);
                }
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // En cas d'erreur, utiliser l'icône par défaut
                imageView.setImageResource(R.drawable.ic_profile_person);
            }
        });
    }
} 