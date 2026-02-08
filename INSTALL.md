# 📦 Installation

## Prérequis

- **Docker** 20.10+ & **Docker Compose** v2+
- (Optionnel) Un token Discord pour le bot → à mettre dans un fichier `.env`

---

## Lancer l'application depuis zéro

```bash
# 1. Cloner le projet
git clone https://github.com/YacineCC/tp-fil-rouge-YacineCC.git
cd tp-fil-rouge-YacineCC

# 2. (Optionnel) Configurer le bot Discord
echo "DISCORD_TOKEN=votre_token_ici" > .env

# 3. Lancer toute la stack (PostgreSQL + API + Ollama + Bot)
./start-app.sh
```

C'est tout. Le script build les images Docker et démarre tous les services automatiquement.

### Accès

| Service        | URL                          |
|----------------|------------------------------|
| API REST       | http://localhost:8080         |
| PostgreSQL     | `localhost:5432`             |
| Ollama (LLM)   | http://localhost:11434       |

---

## Arrêter l'application

```bash
# Arrêter (les données sont conservées)
./stop-app.sh

# Arrêter et supprimer toutes les données
docker compose -f docker-compose.full.yml down -v
```

---

## Tester l'API

```bash
# Lancer le script de test
./test-api.sh

# Ou manuellement
curl http://localhost:8080/api/users
```

---

## Lancer les tests unitaires

```bash
./mvnw test
```
