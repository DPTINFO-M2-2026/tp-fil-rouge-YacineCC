# 🛹 Thrasher Bot — Bot Discord Réactif

Bot Discord réactif construit avec **Discord4J + Project Reactor**.
Combine commandes fun (covers Thrasher, vidéos Hellbomb), administration CRUD synchronisée avec l'API REST, modération Discord, et intégration LLM via LangChain4J + Ollama.

## 📁 Structure des ressources

```
discord-bot-resources/
├── thrasher-covers/          ← Images de covers Thrasher Magazine
│   ├── cover1.jpg
│   └── ...
└── thrasher-hellbomb/        ← Vidéos Hellbomb
    ├── hellbomb1.mp4
    └── ...
```

## 🚀 Lancement

### Via Docker Compose (recommandé)

```bash
# Stack complète : DB + API + Bot + Ollama
docker compose -f docker-compose.full.yml up --build -d

# Vérifier les logs du bot
docker logs -f discord-bot-thrasher
```

Le bot attend que l'API Quarkus soit `healthy` avant de démarrer (`depends_on: app: condition: service_healthy`).

### Variables d'environnement

| Variable | Description | Valeur par défaut |
|----------|-------------|-------------------|
| `DISCORD_TOKEN` | Token du bot Discord | *obligatoire* |
| `LANGCHAIN4J_HOST_IP` | Hôte Ollama | `ollama` |
| `LANGCHAIN4J_OLLAMA_PORT` | Port Ollama | `11434` |
| `LANGCHAIN4J_MODEL_NAME` | Modèle LLM | `llama3:8b` |

## 💬 Commandes

### Commandes fun

| Commande | Description |
|----------|-------------|
| `!thrasher` | 🛹 Poste une cover aléatoire de Thrasher Magazine |
| `!thrasher hellbomb` | 💣 Poste une vidéo Hellbomb aléatoire |
| `!thrasher help` | 📖 Affiche l'aide complète |

### Commandes Admin (CRUD & Synchronisation)

| Commande | Description |
|----------|-------------|
| `!admin scan` | 🔄 Scanne le serveur et synchronise vers la BDD (users, guilds, channels, roles, messages) |
| `!admin createGuild <nom>` | 🏗️ Crée une guilde avec 3 canaux + 2 rôles par défaut |
| `!admin delete <messageId>` | 🗑️ Supprime un message (BDD + Discord) |
| `!admin role add <@user> <rôle>` | ➕ Assigne un rôle (BDD + Discord) |
| `!admin role remove <@user> <rôle>` | ➖ Retire un rôle (BDD + Discord) |

### Commandes Admin (Modération Discord)

| Commande | Description |
|----------|-------------|
| `!admin mute <@user>` | 🔇 Mute un utilisateur dans le channel courant |
| `!admin unmute <@user>` | 🔊 Unmute un utilisateur dans le channel courant |
| `!admin nick <@user> <pseudo>` | ✏️ Change le pseudo d'un membre |
| `!admin ban <@user> [raison]` | 🔨 Bannit un utilisateur du serveur |
| `!admin unban <userId>` | ✅ Débannit un utilisateur du serveur |

### Commandes LLM (Ollama)

| Commande | Description |
|----------|-------------|
| `/ask <texte>` | 🤖 Pose une question au modèle |
| `/translate <texte>` | 🌍 Traduit un texte en français |
| `/summarize` | 📝 Résume les 20 derniers messages du salon |
| `/moderate <message>` | 🛡️ Analyse la toxicité d'un message |
| `/analyze` | 📊 Analyse le sentiment des 30 derniers messages |
| `/translate-auto <texte>` | 🔄 Traduction avec détection auto de langue |
| `/define <mot>` | 📚 Définition d'un mot ou concept |
| `/weather <ville>` | ⛅ Météo d'une ville |

## 🔄 Scan automatique

Le bot scanne **automatiquement** tous les serveurs Discord toutes les **60 secondes** et persiste :
- 👤 Users (upsert par discordId)
- 🏰 Guilds (upsert par discordId)
- 💬 Channels (upsert par discordId)
- 🎭 Roles (upsert par nom + guild)
- 📨 Messages (100 derniers par channel, upsert par discordId)

## ⚠️ Permissions Discord requises

Le rôle du bot doit être **positionné au-dessus** des rôles des utilisateurs ciblés dans la hiérarchie Discord pour que les commandes de modération fonctionnent (mute, nick, ban, role).

Permissions nécessaires :
- `MANAGE_CHANNELS` (mute/unmute)
- `MANAGE_NICKNAMES` (nick)
- `BAN_MEMBERS` (ban/unban)
- `MANAGE_ROLES` (role add/remove)
- `MANAGE_MESSAGES` (delete)
- `ATTACH_FILES` (thrasher covers/hellbomb)

## 📁 Ressources média

**Formats acceptés (covers) :** JPG, JPEG, PNG, GIF
**Formats acceptés (vidéos) :** MP4, MOV, AVI, WEBM
**Limite de taille :** 8 MB (sans Nitro) / 50 MB (avec Nitro)

### Compression vidéo (FFmpeg)

```bash
ffmpeg -i input.mp4 -vcodec h264 -crf 28 output.mp4
```
