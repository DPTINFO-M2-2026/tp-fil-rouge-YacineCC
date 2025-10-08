# 🛹 ThrasherBot Reactive - Version avec Mono

## 📖 Description

Version **réactive** du ThrasherBot utilisant le pattern **Mono** de Project Reactor, recommandé par Discord4J pour une gestion optimale des événements asynchrones.

## 🆚 Différences avec la version classique

### Version Classique (`ThrasherBot.java`)
```java
// ❌ Bloquant - Attend la réponse de Discord
GatewayDiscordClient gateway = client.login().block();

gateway.on(MessageCreateEvent.class).subscribe(event -> {
    // Code bloquant avec .block()
    message.getChannel().block()
        .createMessage("Hello!")
        .block();
});
```

### Version Reactive (`ThrasherBotReactive.java`)
```java
// ✅ Non-bloquant - Pattern réactif
Mono<Void> login = client.withGateway((GatewayDiscordClient gateway) -> {
    return gateway.on(MessageCreateEvent.class, event -> {
        // Code non-bloquant avec flatMap
        return message.getChannel()
            .flatMap(channel -> channel.createMessage("Hello!"))
            .then();
    }).then();
});

login.block(); // Bloque uniquement pour garder le bot actif
```

## ✨ Avantages de la version réactive

### 🚀 Performance
- **Non-bloquant** : Ne bloque pas le thread pendant les opérations I/O
- **Scalable** : Peut gérer plus de requêtes simultanées
- **Efficient** : Meilleure utilisation des ressources CPU

### 🎯 Bonnes pratiques Discord4J
- Pattern officiel recommandé par Discord4J
- Gestion native des erreurs avec Mono
- Chaînage d'opérations asynchrones avec `flatMap()`

### 🔧 Code plus propre
- Composition fonctionnelle avec `Mono.fromRunnable()`
- Combinaison d'événements avec `.and()`
- Pas de nested `.block()` qui bloquent le thread

## 🐛 Problème résolu : Gestion des fichiers

### ❌ Problème initial
```java
// Le FileInputStream se ferme AVANT que Discord ne lise les données !
try (FileInputStream stream = new FileInputStream(file)) {
    return channel.createMessage(...)  // Exécuté plus tard (async)
        .build();
} // Stream fermé ICI ⚠️
```

### ✅ Solution implémentée
```java
// Lire le fichier complet en mémoire puis créer un ByteArrayInputStream
byte[] fileData = Files.readAllBytes(file.toPath());
ByteArrayInputStream stream = new ByteArrayInputStream(fileData);

return channel.createMessage(...)
    .addFile(fileName, stream)  // Le stream ne dépend plus du fichier
    .build();
```

**Pourquoi ça marche ?**
- `Files.readAllBytes()` : Opération **synchrone** qui charge tout en mémoire
- `ByteArrayInputStream` : Stream qui lit depuis la mémoire, pas depuis un fichier
- Les données sont disponibles même après la fermeture du fichier original

## 🎮 Utilisation

### Lancer le bot
```bash
./run-thrasher-bot-reactive.sh
```

### Configuration
1. Ouvre `src/main/java/fr/univtln/yhaouas846/discord4j/ThrasherBotReactive.java`
2. Remplace `"TOKEN"` par ton token Discord (ligne 40)
3. Lance le script

### Commandes Discord
- `!thrasher` - Poste une cover aléatoire de Thrasher Magazine
- `!thrasher hellbomb` - Poste une vidéo Hellbomb aléatoire 💣
- `!thrasher help` - Affiche l'aide

## 📂 Structure du code

### Event Handlers (Retournent `Mono<Void>`)

#### 1. ReadyEvent - Bot connecté
```java
Mono<Void> printOnReady = gateway.on(ReadyEvent.class, event -> 
    Mono.fromRunnable(() -> {
        // Actions sans retour
        System.out.println("✅ Bot connecté");
    })
).then();
```

#### 2. MessageCreateEvent - Message reçu
```java
Mono<Void> handleCommands = gateway.on(MessageCreateEvent.class, event -> {
    if (content.equals("!thrasher")) {
        return handleThrasherCommand(message, coversDir);
    }
    return Mono.empty(); // Rien à faire
}).then();
```

#### 3. Combinaison des événements
```java
return printOnReady.and(handleCommands); // Les deux en parallèle
```

### Méthodes de gestion des commandes

#### Pattern commun
```java
private static Mono<Void> handleCommand(Message message) {
    return message.getChannel()
        .flatMap(channel -> {
            // Logique métier
            return channel.createMessage("Response");
        })
        .then(); // Convertit Mono<Message> en Mono<Void>
}
```

## 📊 Comparaison technique

| Aspect | Version Classique | Version Reactive |
|--------|------------------|------------------|
| **Connection** | `login().block()` | `withGateway()` |
| **Event Handlers** | `void` | `Mono<Void>` |
| **Opérations async** | `.block()` | `.flatMap()` |
| **Gestion erreurs** | `try-catch` | `onErrorResume()` |
| **Performance** | Bloquant | Non-bloquant |
| **Scalabilité** | Limitée | Excellente |

## 🔍 Concepts clés

### Mono<Void>
- Représente une opération asynchrone **sans valeur de retour**
- Utilisé pour les side-effects (envoi de messages, logs, etc.)
- `.then()` : Convertit `Mono<T>` en `Mono<Void>`

### flatMap()
- Transforme et **aplatit** les Mono imbriqués
- Permet de chaîner des opérations asynchrones
- Alternative non-bloquante à `.block()`

### withGateway()
- Pattern recommandé par Discord4J
- Gère automatiquement la connexion et déconnexion
- Prend une Function qui retourne `Mono<Void>`

## ⚠️ Important

### Token Discord
**Ne JAMAIS commit le token Discord sur Git !**

Le token doit être :
- Remplacé localement dans le code
- Stocké dans un fichier `.env` (en production)
- Ajouté au `.gitignore`

### Limites Discord
- **8 MB** : Limite par défaut sans Nitro
- **50 MB** : Limite avec Discord Nitro
- Le bot vérifie automatiquement la taille avant envoi

## 📚 Ressources

- [Discord4J Documentation](https://docs.discord4j.com/)
- [Project Reactor](https://projectreactor.io/docs)
- [Reactive Programming Guide](https://www.reactivemanifesto.org/)

## 🎯 Tests réalisés

✅ **Commande !thrasher**
- Image envoyée correctement
- Message de confirmation dans Discord
- Logs dans la console

✅ **Commande !thrasher hellbomb**
- Vidéo envoyée correctement (6 MB testée)
- Vérification de taille fonctionnelle
- Logs dans la console

✅ **Commande !thrasher help**
- Message d'aide affiché
- Formatage Markdown correct

## 🛠️ Développement futur

### Améliorations possibles
- [ ] Utiliser des variables d'environnement pour le token
- [ ] Ajouter des commandes admin (clear, ban, etc.)
- [ ] Système de cooldown pour éviter le spam
- [ ] Base de données pour tracker les covers postées
- [ ] Intégration avec Thrasher API (si disponible)
- [ ] Commandes slash (/) au lieu de prefix (!)

### Pattern avancés
- [ ] Utiliser `Flux<T>` pour les listes
- [ ] Retry avec backoff exponentiel
- [ ] Circuit breaker pour la résilience
- [ ] Metrics avec Micrometer

---

**Développé avec** ❤️ **et** 🛹 **pour la communauté skate**

Skate on! 🤙
