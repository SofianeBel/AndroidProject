# NewTwitter

Une application Android de microblogging inspirée de Twitter, permettant aux utilisateurs de partager des posts, d'interagir avec d'autres utilisateurs et de gérer leur profil.

## Fonctionnalités

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
4. Exécutez l'application sur un émulateur ou un appareil Android

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
    ".read": true,
    ".write": true,
    "posts": {
      ".read": true,
      ".write": true,
      "$postId": {
        ".read": true,
        ".write": true
      }
    },
    "likes": {
      ".read": true,
      ".write": true,
      "$likeId": {
        ".read": true,
        ".write": true
      }
    },
    "users": {
      ".read": true,
      ".write": true,
      "$userId": {
        ".read": true,
        ".write": true
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
- Possibilité de liker des posts
- Navigation entre différentes sections (Accueil, Profil, Paramètres)

## Version

Version actuelle : 1.3.0 