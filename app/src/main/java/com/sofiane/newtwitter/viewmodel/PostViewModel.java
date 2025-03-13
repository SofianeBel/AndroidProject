package com.sofiane.newtwitter.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;
import com.sofiane.newtwitter.model.Post;
import com.sofiane.newtwitter.repository.PostRepository;
import java.util.List;

/**
 * ViewModel responsable de la gestion des posts (tweets).
 * Cette classe utilise PostRepository pour interagir avec Firebase et fournit des données
 * observables sur les posts et les messages d'erreur.
 * Elle gère également les opérations CRUD sur les posts, ainsi que les interactions comme
 * les likes, les retweets et les réponses.
 */
public class PostViewModel extends ViewModel {
    private final PostRepository postRepository;
    private final MediatorLiveData<List<Post>> posts = new MediatorLiveData<>();
    private final MediatorLiveData<String> errorMessage = new MediatorLiveData<>();

    /**
     * Constructeur qui initialise le PostRepository et configure les sources de données observables.
     */
    public PostViewModel() {
        postRepository = PostRepository.getInstance();
        
        // Observe posts from repository
        posts.addSource(postRepository.getAllPostsLiveData(), posts::setValue);
        
        // Observe error messages from repository
        errorMessage.addSource(postRepository.getErrorMessageLiveData(), errorMessage::setValue);
    }

    /**
     * Obtient le LiveData contenant la liste des posts.
     *
     * @return LiveData contenant la liste des posts
     */
    public LiveData<List<Post>> getPosts() {
        return posts;
    }

    /**
     * Obtient le LiveData contenant les messages d'erreur.
     *
     * @return LiveData contenant les messages d'erreur
     */
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    /**
     * Charge tous les posts depuis le repository.
     * Met à jour le LiveData posts avec les résultats.
     */
    public void loadPosts() {
        try {
            postRepository.loadAllPosts();
        } catch (Exception e) {
            errorMessage.setValue("Error loading posts: " + e.getMessage());
        }
    }

    /**
     * Crée un nouveau post pour l'utilisateur actuellement connecté.
     * Vérifie que le contenu n'est pas vide avant de créer le post.
     *
     * @param content Le contenu du post
     */
    public void createPost(String content) {
        if (content == null || content.trim().isEmpty()) {
            errorMessage.setValue("Post content cannot be empty");
            return;
        }

        postRepository.createPost(content);
    }
    
    /**
     * Crée un nouveau post pour un utilisateur spécifique.
     * Utilisé principalement pour les tests ou lorsque les informations de l'utilisateur sont connues.
     * Vérifie que le contenu n'est pas vide avant de créer le post.
     *
     * @param userId L'identifiant de l'auteur du post
     * @param username Le nom d'utilisateur de l'auteur
     * @param content Le contenu du post
     */
    public void createPost(String userId, String username, String content) {
        if (content == null || content.trim().isEmpty()) {
            errorMessage.setValue("Post content cannot be empty");
            return;
        }

        postRepository.createPost(userId, username, content);
    }

    /**
     * Crée une réponse à un post existant.
     * Vérifie que le contenu et l'ID du post parent ne sont pas vides.
     *
     * @param content Le contenu de la réponse
     * @param parentPostId L'identifiant du post parent
     */
    public void createReply(String content, String parentPostId) {
        if (content == null || content.trim().isEmpty()) {
            errorMessage.setValue("Reply content cannot be empty");
            return;
        }

        if (parentPostId == null || parentPostId.trim().isEmpty()) {
            errorMessage.setValue("Parent post ID cannot be empty");
            return;
        }

        postRepository.createReply(content, parentPostId);
    }

    /**
     * Crée un retweet d'un post existant.
     * Vérifie que le post n'est pas null avant de le retweeter.
     *
     * @param post Le post original à retweeter
     */
    public void retweetPost(Post post) {
        if (post == null) {
            errorMessage.setValue("Cannot retweet null post");
            return;
        }

        postRepository.retweetPost(post);
    }

    /**
     * Ajoute ou supprime un like sur un post.
     *
     * @param postId L'identifiant du post à liker/unliker
     */
    public void likePost(String postId) {
        postRepository.likePost(postId);
    }

    /**
     * Supprime un post et ses références associées.
     *
     * @param postId L'identifiant du post à supprimer
     */
    public void deletePost(String postId) {
        postRepository.deletePost(postId);
    }
} 