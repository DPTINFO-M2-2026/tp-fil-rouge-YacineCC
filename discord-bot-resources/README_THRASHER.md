# 🛹 Thrasher Bot - Bot Discord de Covers Aléatoires

Ce bot Discord poste des covers aléatoires de Thrasher Magazine quand tu tapes `!thrasher`.

## 📁 Structure

```
discord-bot-resources/
└── thrasher-covers/          ← Mets tes images de covers ici !
    ├── cover1.jpg
    ├── cover2.png
    ├── cover3.jpg
    └── ...
```

## 🚀 Installation et Configuration

### 1️⃣ Ajoute tes covers Thrasher

Place tes images dans le dossier `discord-bot-resources/thrasher-covers/`

Formats acceptés : **JPG, JPEG, PNG, GIF**

### 2️⃣ Configure le token

Dans `ThrasherBot.java`, ligne 38, remplace :
```java
String token = "TOKEN";
```

Par ton vrai token Discord.

### 3️⃣ Lance le bot

```bash
# Méthode simple avec le script
./run-thrasher-bot.sh

# Ou avec Maven directement
mvn exec:java -Dexec.mainClass="fr.univtln.yhaouas846.discord4j.ThrasherBot"
```

## 💬 Commandes

| Commande | Description |
|----------|-------------|
| `!thrasher` | Poste une cover aléatoire de Thrasher Magazine |
| `!thrasher help` | Affiche l'aide |

## 📋 Exemple d'utilisation

**Dans Discord :**
```
Toi: !thrasher
Bot: 🛹 **Thrasher Magazine Cover** 🛹
     [Image d'une cover aléatoire]
```

## 🎨 Où trouver des covers Thrasher ?

- Site officiel Thrasher Magazine
- Archives en ligne
- Tes propres magazines scannés
- Collections de fans

**Conseil :** Nomme tes fichiers de manière organisée :
- `thrasher_1981_01.jpg` (année + mois)
- `thrasher_tony_hawk.jpg` (nom du skateur)
- `thrasher_vintage_01.jpg` (thème)

## 🔧 Personnalisation

### Changer le dossier des images

Dans `ThrasherBot.java`, ligne 33 :
```java
private static final String COVERS_DIRECTORY = "discord-bot-resources/thrasher-covers";
```

### Ajouter d'autres commandes

Tu peux ajouter :
- `!thrasher stats` : Nombre d'images disponibles
- `!thrasher random 5` : Poster 5 images d'un coup
- `!thrasher vintage` : Seulement les anciennes covers

## 🐛 Troubleshooting

**Aucune image n'est postée :**
- ✅ Vérifie que le dossier `discord-bot-resources/thrasher-covers/` existe
- ✅ Vérifie qu'il contient des images (.jpg, .png, .gif)
- ✅ Vérifie que le bot a les permissions "Attach Files"

**Le bot ne voit pas les images :**
- ✅ Vérifie les extensions de fichiers (jpg, png, gif)
- ✅ Vérifie que les fichiers ne sont pas corrompus
- ✅ Regarde les logs du bot pour voir les erreurs

**Erreur d'upload :**
- ✅ Les images ne doivent pas dépasser 8 MB (limite Discord)
- ✅ Le bot doit avoir la permission "Attach Files" sur le channel

## 📊 Informations affichées au démarrage

Quand tu lances le bot, tu verras :
```
════════════════════════════════════
  🛹 Thrasher Bot Démarré 🛹
════════════════════════════════════
✅ Bot connecté : ThrasherBot#1234
📁 Dossier covers : discord-bot-resources/thrasher-covers
🖼️  Images disponibles : 42
📝 Commandes disponibles :
   !thrasher      → Poste une cover aléatoire
   !thrasher help → Affiche l'aide
════════════════════════════════════
```

## 🎯 Améliorations futures possibles

- [ ] Système de favoris
- [ ] Base de données pour tracker les covers postées
- [ ] Statistiques (cover la plus demandée)
- [ ] Filtrage par année/skateur
- [ ] Rotation automatique toutes les X heures
- [ ] Embed avec infos sur la cover (date, skateur...)

Skate on! 🛹✨
