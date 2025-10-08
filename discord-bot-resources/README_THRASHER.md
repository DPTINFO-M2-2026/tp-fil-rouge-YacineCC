# 🛹 Thrasher Bot - Bot Discord de Covers et Vidéos Hellbomb

Ce bot Discord poste des covers aléatoires de Thrasher Magazine et des vidéos Hellbomb !

## 📁 Structure

```
discord-bot-resources/
├── thrasher-covers/          ← Mets tes images de covers ici !
│   ├── cover1.jpg
│   ├── cover2.png
│   └── ...
└── thrasher-hellbomb/        ← Mets tes vidéos Hellbomb ici !
    ├── hellbomb1.mp4
    ├── hellbomb2.mp4
    └── ...
```

## 🚀 Installation et Configuration

### 1️⃣ Ajoute tes covers et vidéos Thrasher

**Images :** Place-les dans `discord-bot-resources/thrasher-covers/`
- Formats acceptés : **JPG, JPEG, PNG, GIF**

**Vidéos :** Place-les dans `discord-bot-resources/thrasher-hellbomb/`
- Formats acceptés : **MP4, MOV, AVI, WEBM**
- ⚠️ **Limite de taille :** 8 MB (sans Nitro) / 50 MB (avec Nitro)

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
| `!thrasher` | Poste une cover aléatoire de Thrasher Magazine 🖼️ |
| `!thrasher hellbomb` | Poste une vidéo Hellbomb aléatoire 💣 |
| `!thrasher help` | Affiche l'aide |

## 📋 Exemple d'utilisation

**Dans Discord :**
```
Toi: !thrasher
Bot: 🛹 **Thrasher Magazine Cover** 🛹
     [Image d'une cover aléatoire]

Toi: !thrasher hellbomb
Bot: 💣 **HELLBOMB!** 💣
     🛹 Thrasher Magazine
     [Vidéo d'un trick fou]
```

## 🎨 Où trouver du contenu Thrasher ?

**Covers :**
- Site officiel Thrasher Magazine
- Archives en ligne
- Tes propres magazines scannés
- Collections de fans

**Vidéos Hellbomb :**
- Chaîne YouTube officielle Thrasher
- Compilations de tricks
- Vidéos de skate parts

**Conseil :** Nomme tes fichiers de manière organisée :
- `thrasher_1981_01.jpg` (année + mois)
- `thrasher_tony_hawk.jpg` (nom du skateur)
- `hellbomb_jaws_25stairs.mp4` (nom du trick/spot)
- `hellbomb_ali_boulala_lyon.mp4` (skateur + spot)

## 💡 Compression des vidéos

Si tes vidéos dépassent 8 MB, utilise **FFmpeg** :

```bash
# Compresser une vidéo à ~5 MB
ffmpeg -i input.mp4 -vcodec h264 -acodec mp3 -b:v 500k output.mp4

# Compresser avec une qualité décente
ffmpeg -i input.mp4 -vcodec h264 -crf 28 output.mp4
```

## 🔧 Personnalisation

### Changer les dossiers

Dans `ThrasherBot.java`, lignes 38-39 :
```java
private static final String COVERS_DIRECTORY = "discord-bot-resources/thrasher-covers";
private static final String HELLBOMB_DIRECTORY = "discord-bot-resources/thrasher-hellbomb";
```

### Ajouter d'autres commandes

Tu peux ajouter :
- `!thrasher stats` : Nombre d'images/vidéos disponibles
- `!thrasher random 5` : Poster 5 images d'un coup
- `!thrasher vintage` : Seulement les anciennes covers
- `!thrasher skater [nom]` : Filtrer par skateur

## 🐛 Troubleshooting

**Aucune image/vidéo n'est postée :**
- ✅ Vérifie que les dossiers existent :
  - `discord-bot-resources/thrasher-covers/`
  - `discord-bot-resources/thrasher-hellbomb/`
- ✅ Vérifie qu'ils contiennent des fichiers
- ✅ Vérifie que le bot a les permissions "Attach Files"

**Le bot ne voit pas les fichiers :**
- ✅ Vérifie les extensions (jpg, png, gif pour images / mp4, mov, avi, webm pour vidéos)
- ✅ Vérifie que les fichiers ne sont pas corrompus
- ✅ Regarde les logs du bot pour voir les erreurs

**Erreur d'upload :**
- ✅ Les images ne doivent pas dépasser 8 MB (limite Discord)
- ✅ Les vidéos ne doivent pas dépasser 8 MB (sans Nitro) / 50 MB (avec Nitro)
- ✅ Le bot doit avoir la permission "Attach Files" sur le channel
- ✅ Compresse tes vidéos si nécessaire (voir section compression)

**Vidéo trop grande :**
- Le bot t'avertira si la vidéo dépasse 8 MB
- Solution : Compresse la vidéo avec FFmpeg (voir section ci-dessus)

## 📊 Informations affichées au démarrage

Quand tu lances le bot, tu verras :
```
════════════════════════════════════
  🛹 Thrasher Bot Démarré 🛹
════════════════════════════════════
✅ Bot connecté : ThrasherBot
📁 Dossier covers : discord-bot-resources/thrasher-covers
� Dossier hellbomb : discord-bot-resources/thrasher-hellbomb
�🖼️  Images disponibles : 42
🎥 Vidéos disponibles : 15
📝 Commandes disponibles :
   !thrasher          → Poste une cover aléatoire
   !thrasher hellbomb → Poste une vidéo Hellbomb aléatoire
   !thrasher help     → Affiche l'aide
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
