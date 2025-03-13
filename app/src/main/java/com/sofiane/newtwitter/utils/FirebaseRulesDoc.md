# Documentation des Règles de Sécurité Firebase

Ce document explique les règles de sécurité Firebase utilisées dans l'application NewTwitter.

## Structure des Règles

Les règles de sécurité Firebase sont définies pour chaque collection de données dans la base de données Realtime Database :

### 1. Posts (Publications)

```json
"posts": {
  ".read": true,
  ".write": "auth != null",
  "$postId": {
    ".read": true,
    ".write": "auth != null && (newData.child('userId').val() === auth.uid || data.child('userId').val() === auth.uid)"
  }
}
```

- **Lecture** : Tout le monde peut lire les posts (même les utilisateurs non connectés)
- **Écriture** : Seuls les utilisateurs authentifiés peuvent créer des posts
- **Modification/Suppression** : Un post spécifique ne peut être modifié ou supprimé que par son auteur

### 2. Likes (J'aime)

```json
"likes": {
  ".read": true,
  ".write": "auth != null",
  "$likeId": {
    ".read": true,
    ".write": "auth != null && $likeId.contains(auth.uid)"
  }
}
```

- **Lecture** : Tout le monde peut voir les likes
- **Écriture** : Seuls les utilisateurs authentifiés peuvent liker
- **Modification/Suppression** : Un utilisateur ne peut modifier ou supprimer que ses propres likes

### 3. Retweets (Partages)

```json
"retweets": {
  ".read": true,
  ".write": "auth != null",
  "$retweetId": {
    ".read": true,
    ".write": "auth != null && $retweetId.contains(auth.uid)"
  }
}
```

- **Lecture** : Tout le monde peut voir les retweets
- **Écriture** : Seuls les utilisateurs authentifiés peuvent retweeter
- **Modification/Suppression** : Un utilisateur ne peut modifier ou supprimer que ses propres retweets

### 4. Users (Utilisateurs)

```json
"users": {
  ".read": true,
  ".write": "auth != null",
  "$userId": {
    ".read": true,
    ".write": "auth != null && $userId === auth.uid"
  }
}
```

- **Lecture** : Tout le monde peut voir les profils utilisateurs
- **Écriture** : Seuls les utilisateurs authentifiés peuvent créer des profils
- **Modification/Suppression** : Un utilisateur ne peut modifier ou supprimer que son propre profil

### 5. Comments (Commentaires)

```json
"comments": {
  ".read": true,
  ".write": "auth != null",
  "$commentId": {
    ".read": true,
    ".write": "auth != null && (newData.child('userId').val() === auth.uid || data.child('userId').val() === auth.uid)"
  }
}
```

- **Lecture** : Tout le monde peut lire les commentaires
- **Écriture** : Seuls les utilisateurs authentifiés peuvent commenter
- **Modification/Suppression** : Un commentaire spécifique ne peut être modifié ou supprimé que par son auteur

### 6. Follows (Abonnements)

```json
"follows": {
  ".read": true,
  ".write": "auth != null",
  "$userId": {
    ".read": true,
    ".write": "auth != null && ($userId === auth.uid || root.child('follows').child(auth.uid).child('following').child($userId).exists())"
  }
}
```

- **Lecture** : Tout le monde peut voir les relations d'abonnement
- **Écriture** : Seuls les utilisateurs authentifiés peuvent suivre/ne plus suivre
- **Modification** : Un utilisateur ne peut modifier que ses propres relations d'abonnement ou celles des utilisateurs qu'il suit

## Bonnes Pratiques de Sécurité

1. **Validation des données** : Toujours valider les données côté client avant de les envoyer à Firebase
2. **Authentification** : Utiliser l'authentification Firebase pour sécuriser l'accès aux données
3. **Règles spécifiques** : Définir des règles précises pour chaque nœud de données
4. **Tests** : Tester régulièrement les règles de sécurité pour s'assurer qu'elles fonctionnent comme prévu

## Remarques

- Ces règles sont conçues pour une application sociale où la plupart des données sont publiques
- Pour une application avec des exigences de confidentialité plus strictes, il faudrait adapter ces règles
- Les règles peuvent être mises à jour dans la console Firebase 