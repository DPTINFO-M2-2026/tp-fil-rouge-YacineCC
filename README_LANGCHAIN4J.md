# LangChain4J avec Ollama - Configuration

Ce projet utilise **LangChain4J** pour communiquer avec un serveur **Ollama** sur la machine hôte.

## Prérequis

1. **Ollama** doit être installé et en cours d'exécution sur votre machine hôte (192.168.56.1)
2. Un modèle doit être disponible (par exemple `mistral`, `llama2`, etc.)

## Installation d'Ollama sur la machine hôte

```bash
# Sur Linux/Mac
curl -fsSL https://ollama.com/install.sh | sh

# Démarrer Ollama
ollama serve

# Télécharger un modèle
ollama pull mistral
```

## Configuration

Le fichier `langchain4j.properties` contient la configuration :

```properties
LANGCHAIN4J_HOST_IP=192.168.56.1
LANGCHAIN4J_OLLAMA_PORT=11434
LANGCHAIN4J_MODEL_NAME=mistral
```

**Note:** Ce fichier est dans `.gitignore` pour éviter de versionner votre configuration locale.

## Utilisation

### Test simple

```bash
# Compiler et tester le client
mvn clean compile
mvn exec:java -Dexec.mainClass="fr.univtln.yhaouas846.discord4j.LangChain4jClient"
```

### Dans votre code

```java
LangChain4jClient client = new LangChain4jClient();
String response = client.ask("Explique-moi la programmation orientée objet");
System.out.println(response);

// Ou avec un modèle spécifique
LangChain4jClient client2 = new LangChain4jClient("llama2");
```

### Intégration dans les bots Discord

Vous pouvez facilement intégrer LangChain4J dans vos bots Discord :

```java
// Dans ThrasherBot ou ThrasherBotReactive
else if (content.toLowerCase().startsWith("!ollama ")) {
    String question = content.substring(8).trim();
    LangChain4jClient client = new LangChain4jClient();
    String answer = client.ask(question);
    message.getChannel().block().createMessage("🤖 Ollama : " + answer).block();
}
```

## Modèles disponibles

Liste des modèles populaires pour Ollama :
- `mistral` - Modèle français performant
- `llama2` - Meta's Llama 2
- `codellama` - Spécialisé pour le code
- `phi` - Petit mais efficace
- `orca-mini` - Modèle léger

Pour voir tous les modèles disponibles :
```bash
ollama list
```

## Dépannage

### Erreur de connexion

Si vous obtenez une erreur de connexion :

1. Vérifiez qu'Ollama est bien lancé sur la machine hôte :
   ```bash
   curl http://192.168.56.1:11434/api/version
   ```

2. Vérifiez que le pare-feu n'est pas bloqué sur le port 11434

3. Vérifiez l'IP de votre machine hôte :
   ```bash
   # Sur la VM
   ip route | grep default
   ```

### Timeout

Si les requêtes prennent trop de temps, vous pouvez augmenter le timeout dans `LangChain4jClient.java` :

```java
.timeout(Duration.ofSeconds(120)) // au lieu de 60
```

## Documentation

- [LangChain4J](https://github.com/langchain4j/langchain4j)
- [Ollama](https://ollama.com/)
- [Ollama Models](https://ollama.com/library)
