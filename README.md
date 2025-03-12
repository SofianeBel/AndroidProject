# H3 Project - Application Twitter-like

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

Version actuelle : 1.2.0 