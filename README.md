# NewTwitter

Une application Android de microblogging inspirée de Twitter, permettant aux utilisateurs de partager des posts, d'interagir avec d'autres utilisateurs et de gérer leur profil.

## Présentation de l'application

### Captures d'écran

<div align="center">
  <img src="ScreenshotPresentation.jpg" alt="Capture d'écran de l'écran d'accueil" width="300"/>
  <img src="ScreenshotPresentation2.jpg" alt="Capture d'écran du détail d'un post" width="300"/>
</div>

### Vidéo de démonstration

[Cliquez ici pour voir la vidéo de démonstration](Présentation_App.mp4)

## Fonctionnalités

### Version 1.5.7
- **Amélioration de l'expérience utilisateur** : Uniformisation de l'identité visuelle de l'application

### Version 1.5.6
- **Suppression des données mock** : Élimination des données de test du code de production
- **Amélioration de la qualité du code** : Nettoyage du code et suppression des fonctionnalités de test
- **Documentation complète** : Ajout de documentation JavaDoc pour tous les fragments et classes principales

### Version 1.5.5
- **Documentation des fragments** : Ajout de documentation JavaDoc pour tous les fragments
- **Amélioration de la maintenabilité** : Clarification du code avec des commentaires détaillés
- **Standardisation** : Utilisation cohérente du format JavaDoc dans tous les fragments

### Version 1.5.4
- **Documentation des ViewModels** : Ajout de documentation JavaDoc pour les classes ViewModel
- **Amélioration de la maintenabilité** : Clarification du code avec des commentaires détaillés
- **Standardisation** : Utilisation cohérente du format JavaDoc dans tous les ViewModels

### Version 1.5.3
- **Correction de la navigation** : Résolution des problèmes de navigation entre les profils utilisateurs
- **Amélioration de la stabilité** : Correction des erreurs lors de la navigation dans l'application
- **Optimisation des performances** : Réduction des temps de chargement et amélioration de la réactivité

### Version 1.5.2
- **Correction de la navigation** : Résolution du problème de navigation depuis l'accueil vers les profils utilisateurs
- **Amélioration de la stabilité** : Correction des erreurs lors du chargement des profils

### Version 1.5.1
- **Correction des imports** : Résolution des problèmes d'imports pour CircleImageView
- **Amélioration de la stabilité** : Correction des erreurs de compilation

### Version 1.5.0
- **Photos de profil dans les posts** : Affichage des photos de profil de chaque utilisateur sur leurs posts
- **Navigation vers les profils** : Possibilité de cliquer sur le nom d'utilisateur ou la photo de profil pour accéder au profil
- **Interface utilisateur améliorée** : Meilleure présentation des posts avec les photos de profil

### Version 1.4.3
- **Correction des bugs** : Résolution des problèmes liés aux icônes et couleurs de profil
- **Amélioration de la stabilité** : Correction des erreurs lors de la sauvegarde des profils
- **Optimisation des performances** : Réduction des temps de chargement des profils

### Version 1.4.2
- **Débogage amélioré** : Ajout de logs détaillés pour faciliter le débogage
- **Correction des bugs** : Résolution des problèmes liés aux icônes et couleurs de profil

### Version 1.4.1
- **Icônes de profil personnalisables** : Choix parmi plusieurs icônes pour personnaliser son profil
- **Couleurs de profil personnalisables** : Sélection de couleurs pour les icônes de profil
- **Interface utilisateur améliorée** : Meilleure présentation des profils avec les icônes colorées

### Version 1.3.0
- **Listes d'abonnés et d'abonnements** : Visualisation des utilisateurs qui suivent ou sont suivis
- **Navigation améliorée** : Accès direct aux profils depuis les listes d'abonnés/abonnements
- **Interface utilisateur interactive** : Possibilité de suivre/ne plus suivre directement depuis les listes

### Version 1.2.0
- **Système de suivi** : Suivre/ne plus suivre d'autres utilisateurs
- **Compteurs de followers et following** : Affichage du nombre d'abonnés et d'abonnements
- **Interface utilisateur améliorée** : Boutons stylisés pour les actions de suivi

### Version 1.1.0
- **Profils utilisateurs** : Affichage et modification des informations de profil
- **Images de profil et bannières** : Personnalisation de l'apparence du profil
- **Biographie** : Ajout d'une description personnelle
- **Posts personnels** : Affichage des posts de l'utilisateur sur son profil

### Version 1.0.0
- **Authentification** : Inscription et connexion des utilisateurs
- **Création de posts** : Publication de messages courts
- **Timeline** : Affichage des posts récents
- **Interactions** : Like, retweet et réponse aux posts
- **Détail des posts** : Vue détaillée d'un post avec ses réponses

## Technologies utilisées

- Firebase Authentication pour la gestion des utilisateurs
- Firebase Realtime Database pour le stockage des données
- Firebase Storage pour le stockage des images
- Glide pour le chargement des images
- CircleImageView pour les images de profil rondes
- Architecture MVVM (Model-View-ViewModel)
- Navigation Component pour la navigation entre les écrans

## Installation

1. Clonez ce dépôt
2. Ouvrez le projet dans Android Studio
3. Connectez l'application à votre propre projet Firebase
4. Exécutez l'application sur un émulateur ou un appareil Android (Java 17 requis)

## Prochaines fonctionnalités

- Recherche d'utilisateurs et de posts
- Notifications en temps réel
- Messages privés entre utilisateurs
- Hashtags et tendances
- Mode sombre

## Résolution du problème de chargement infini

Si vous rencontrez un problème de chargement infini dans l'écran d'accueil, suivez ces étapes pour le résoudre :

### 1. Vérifier les règles de sécurité Firebase

1. Connectez-vous à la [console Firebase](https://console.firebase.google.com/)
2. Sélectionnez votre projet "newtwitter-65ad1"
3. Dans le menu de gauche, cliquez sur "Realtime Database"
4. Cliquez sur l'onglet "Règles"
5. Remplacez les règles existantes par celles-ci :

```json
{
  "rules": {
    "posts": {
      ".read": true,
      ".write": "auth != null",
      "$postId": {
        ".read": true,
        ".write": "auth != null && (newData.child('userId').val() === auth.uid || data.child('userId').val() === auth.uid)"
      }
    },
    "likes": {
      ".read": true,
      ".write": "auth != null",
      "$likeId": {
        ".read": true,
        ".write": "auth != null && $likeId.contains(auth.uid)"
      }
    },
    "retweets": {
      ".read": true,
      ".write": "auth != null",
      "$retweetId": {
        ".read": true,
        ".write": "auth != null && $retweetId.contains(auth.uid)"
      }
    },
    "users": {
      ".read": true,
      ".write": "auth != null",
      "$userId": {
        ".read": true,
        ".write": "auth != null && $userId === auth.uid"
      }
    },
    "comments": {
      ".read": true,
      ".write": "auth != null",
      "$commentId": {
        ".read": true,
        ".write": "auth != null && (newData.child('userId').val() === auth.uid || data.child('userId').val() === auth.uid)"
      }
    },
    "follows": {
      ".read": true,
      ".write": "auth != null",
      "$userId": {
        ".read": true,
        ".write": "auth != null && ($userId === auth.uid || root.child('follows').child(auth.uid).child('following').child($userId).exists())"
      }
    }
  }
}
```

6. Cliquez sur "Publier"

### 2. Vérifier la connexion Internet

Assurez-vous que votre appareil est connecté à Internet. L'application nécessite une connexion Internet pour récupérer les données depuis Firebase.

### 3. Effacer les données de l'application

1. Allez dans les paramètres de votre appareil
2. Sélectionnez "Applications" ou "Gestionnaire d'applications"
3. Trouvez l'application "H3 Project"
4. Cliquez sur "Stockage" ou "Stockage et cache"
5. Cliquez sur "Effacer les données" et "Effacer le cache"
6. Redémarrez l'application

### 4. Vérifier les logs

Si le problème persiste, vérifiez les logs de l'application pour identifier d'éventuelles erreurs :

1. Connectez votre appareil à votre ordinateur
2. Activez le débogage USB sur votre appareil
3. Exécutez la commande suivante dans un terminal :
   ```
   adb logcat | grep "PostRepository\|HomeFragment"
   ```
4. Recherchez des erreurs liées à Firebase ou au chargement des posts

## Fonctionnalités de l'application

- Affichage d'un fil d'actualité avec des posts
- Création de nouveaux posts
- Possibilité de liker, retweeter et répondre à des posts
- Navigation entre différentes sections (Accueil, Profil, Paramètres)
- Personnalisation du profil avec des icônes et couleurs
- Système de suivi d'utilisateurs
- Affichage des profils utilisateurs avec leurs posts

## Prérequis techniques

- Android Studio
- Java 17 (requis pour le plugin Android Gradle)
- Connexion Internet pour accéder à Firebase

## Version

Version actuelle : 1.5.6 
