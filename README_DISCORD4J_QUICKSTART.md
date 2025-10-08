# 🤖 Discord4J - Guide de démarrage rapide

Ce fichier t'explique comment tester Discord4J avec un bot simple "ping/pong".

## 🎯 Objectif

Créer un bot Discord qui répond "pong !" quand tu écris "ping ?"

## 📁 Fichiers créés

- `src/main/java/fr/univtln/yhaouas846/discord4j/MyBot.java` - Le code du bot
- `src/main/java/fr/univtln/yhaouas846/discord4j/README_DISCORD4J.md` - Documentation complète
- `run-discord-bot.sh` - Script pour lancer facilement le bot

## 🚀 Démarrage rapide (3 étapes)

### 1️⃣ Crée ton bot Discord

Va sur https://discord.com/developers/applications et :
1. Clique "New Application"
2. Va dans "Bot" → "Add Bot"
3. **IMPORTANT** : Active "MESSAGE CONTENT INTENT" 
4. Copie le TOKEN

### 2️⃣ Configure le token

Ouvre `src/main/java/fr/univtln/yhaouas846/discord4j/MyBot.java`

Remplace ligne 26 :
```java
String token = "TOKEN";  // ← Change ici
```

Par :
```java
String token = "ton_vrai_token_ici";
```

### 3️⃣ Lance le bot

```bash
./run-discord-bot.sh
```

OU

```bash
mvn exec:java -Dexec.mainClass="fr.univtln.yhaouas846.discord4j.MyBot"
```

## 💬 Teste le bot

Dans Discord, écris :
```
ping ?
```

Le bot répond :
```
pong !
```

## 📚 Documentation complète

Consulte `src/main/java/fr/univtln/yhaouas846/discord4j/README_DISCORD4J.md` pour :
- Instructions détaillées
- Comment inviter le bot sur ton serveur
- Exemples de code supplémentaires
- Troubleshooting

## ⚠️ IMPORTANT

**NE COMMIT JAMAIS TON TOKEN DISCORD SUR GIT !**

Le fichier `.gitignore` est configuré pour éviter ça, mais fais attention.

## 🔗 Liens utiles

- [Discord Developer Portal](https://discord.com/developers/applications)
- [Discord4J Documentation](https://docs.discord4j.com/)
- [Tutoriel Discord4J](https://docs.discord4j.com/getting-started/)

Bon apprentissage ! 🎓
