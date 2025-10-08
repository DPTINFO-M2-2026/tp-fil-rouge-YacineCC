# 🤖 Test Discord4J - Bot Ping/Pong

Ce dossier contient un bot Discord simple pour apprendre Discord4J.

## 📋 Prérequis

### 1. Créer une application Discord Bot

1. Va sur [Discord Developer Portal](https://discord.com/developers/applications)
2. Clique sur **"New Application"**
3. Donne-lui un nom (ex: "MonBotTest")
4. Va dans l'onglet **"Bot"**
5. Clique sur **"Add Bot"**
6. **IMPORTANT** : Active **"MESSAGE CONTENT INTENT"** dans **Privileged Gateway Intents**
7. Copie le **TOKEN** (clique sur "Reset Token" si besoin)

### 2. Inviter le bot sur ton serveur Discord

1. Dans le Developer Portal, va dans **"OAuth2"** > **"URL Generator"**
2. Coche ces scopes :
   - ✅ `bot`
   - ✅ `applications.commands`
3. Coche ces permissions bot :
   - ✅ `Send Messages`
   - ✅ `Read Message History`
   - ✅ `View Channels`
4. Copie l'URL générée et ouvre-la dans ton navigateur
5. Sélectionne ton serveur Discord et autorise le bot

## 🚀 Utilisation

### Étape 1 : Remplace le token

Ouvre `MyBot.java` et remplace cette ligne :

```java
String token = "TOKEN";
```

Par ton vrai token :

```java
String token = "TON_VRAI_TOKEN_ICI";
```

⚠️ **ATTENTION** : Ne partage JAMAIS ton token ! Ne le commit pas sur Git !

### Étape 2 : Lance le bot

```bash
# Compile le projet
mvn compile

# Lance le bot
mvn exec:java -Dexec.mainClass="fr.univtln.yhaouas846.discord4j.MyBot"
```

Tu devrais voir :

```
======================
  Bot Discord Démarré
======================
✅ Bot connecté : MonBotTest#1234
📝 En attente de messages 'ping ?'...
```

### Étape 3 : Teste le bot

Sur ton serveur Discord, écris dans un channel :

```
ping ?
```

Le bot devrait répondre :

```
pong !
```

## 📝 Comment ça marche

```java
// 1. Créer le client Discord
DiscordClient client = DiscordClient.create(token);

// 2. Se connecter
GatewayDiscordClient gateway = client.login().block();

// 3. Écouter l'event "Ready" (bot connecté)
gateway.on(ReadyEvent.class).subscribe(event -> {
    // Le bot est prêt !
});

// 4. Écouter les messages
gateway.on(MessageCreateEvent.class).subscribe(event -> {
    Message message = event.getMessage();
    
    // Si le message est "ping ?"
    if (content.equalsIgnoreCase("ping ?")) {
        // Répondre "pong !"
        message.getChannel().block().createMessage("pong !").block();
    }
});
```

## 🎓 Pour aller plus loin

Maintenant que tu as le ping/pong qui marche, tu peux essayer :

### 1. Ajouter plus de commandes

```java
if (content.equalsIgnoreCase("!hello")) {
    message.getChannel().block().createMessage("👋 Salut !").block();
}

if (content.startsWith("!echo ")) {
    String texte = content.substring(6);
    message.getChannel().block().createMessage(texte).block();
}
```

### 2. Réagir avec un emoji

```java
message.addReaction(ReactionEmoji.unicode("✅")).block();
```

### 3. Envoyer un message embed

```java
message.getChannel().block()
    .createMessage(EmbedCreateSpec.builder()
        .title("Titre cool")
        .description("Description ici")
        .color(Color.BLUE)
        .build())
    .block();
```

## 📚 Documentation Discord4J

- [Discord4J Documentation](https://docs.discord4j.com/)
- [Discord4J GitHub](https://github.com/Discord4J/Discord4J)
- [Discord Developer Docs](https://discord.com/developers/docs)

## ⚠️ Notes importantes

- **Ne commit JAMAIS ton token Discord** dans Git
- Active bien **MESSAGE CONTENT INTENT** sinon le bot ne verra pas les messages
- Le bot doit avoir les bonnes permissions sur ton serveur
- Pour arrêter le bot : `Ctrl+C`

## 🐛 Problèmes courants

**Le bot ne répond pas :**
- ✅ Vérifie que MESSAGE CONTENT INTENT est activé
- ✅ Vérifie que le bot a la permission "Send Messages"
- ✅ Vérifie que le token est correct

**Erreur de connexion :**
- ✅ Vérifie ta connexion internet
- ✅ Vérifie que le token n'a pas expiré

**Le bot se déconnecte tout de suite :**
- ✅ Vérifie les logs d'erreur dans la console
- ✅ Peut-être que le token est invalide

Bon apprentissage ! 🚀
