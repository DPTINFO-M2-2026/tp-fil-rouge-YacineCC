# 📦 Installation & Utilisation

## Prérequis

### Docker (recommandé)
- Docker 20.10+
- Docker Compose v2+

### Local (sans Docker)
- Java 21 (OpenJDK / Temurin)
- Maven 3.9+ (ou utiliser `./mvnw`)
- PostgreSQL 16

---

## Démarrage rapide (Docker)

```bash
# Cloner le dépôt
git clone https://github.com/YacineCC/tp-fil-rouge-YacineCC.git
cd tp-fil-rouge-YacineCC

# Lancer PostgreSQL + Quarkus en mode dev
docker compose -f docker-compose.dev.yml up --build
```

L'application est accessible sur :
- **API** : http://localhost:8080
- **Dev UI** : http://localhost:8080/q/dev/
- **Debug** : port 5005

```bash
# Arrêter
docker compose -f docker-compose.dev.yml down

# Arrêter + supprimer les données
docker compose -f docker-compose.dev.yml down -v
```

---

## Démarrage local

```bash
# 1. Cloner
git clone https://github.com/YacineCC/tp-fil-rouge-YacineCC.git
cd tp-fil-rouge-YacineCC

# 2. Lancer PostgreSQL (Docker ou local)
docker compose -f docker-compose.dev.yml up -d db

# 3. Démarrer Quarkus (hot-reload)
./mvnw quarkus:dev
```

> Quarkus utilise automatiquement la configuration de `src/main/resources/application.properties`.

### Configuration PostgreSQL (si local sans Docker)

```sql
CREATE DATABASE discord_bot_db;
CREATE USER discord_bot WITH PASSWORD 'discord_password';
GRANT ALL PRIVILEGES ON DATABASE discord_bot_db TO discord_bot;
```

---

## Scripts utilitaires

| Script | Description |
|--------|-------------|
| `./start-dev.sh` | Lance PostgreSQL Docker + Quarkus dev avec vérifications |
| `./stop-dev.sh` | Arrête les conteneurs de développement |
| `./test-api.sh` | Teste les principaux endpoints de l'API |

---

## Tests

```bash
# Tous les tests (H2 en mémoire, pas besoin de PostgreSQL)
./mvnw test

# Test spécifique
./mvnw test -Dtest=UserServiceTest

# Tests par catégorie
./mvnw test -Dtest="*entity*"     # Tests d'entités
./mvnw test -Dtest="*Resource*"   # Tests d'API REST
./mvnw test -Dtest="*Service*"    # Tests de services
```

Les rapports sont dans `target/surefire-reports/`.

---

## Build de production

### JAR

```bash
./mvnw package
java -jar target/quarkus-app/quarkus-run.jar
```

### Uber-JAR

```bash
./mvnw package -Dquarkus.package.jar.type=uber-jar
java -jar target/*-runner.jar
```

### Image Docker

```bash
docker build --target runtime -t botdiscord:latest .
docker run --rm -p 8080:8080 botdiscord:latest
```

### Docker Compose production

```bash
docker compose -f docker-compose.prod.yml up -d
```

---

## Tester l'API

```bash
# Créer un utilisateur
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","discriminator":"1234","email":"alice@example.com","isBot":false}'

# Lister les utilisateurs
curl http://localhost:8080/api/users

# Récupérer un utilisateur
curl http://localhost:8080/api/users/1

# Mettre à jour
curl -X PUT http://localhost:8080/api/users/1 \
  -H "Content-Type: application/json" \
  -d '{"username":"alice_updated","discriminator":"1234","email":"alice@example.com","isBot":false}'

# Supprimer
curl -X DELETE http://localhost:8080/api/users/1

# Messages avec pagination
curl "http://localhost:8080/api/messages?limit=10"
```

---

## Variables d'environnement (production)

Créer un fichier `.env` à la racine :

```bash
DB_PASSWORD=mon_mot_de_passe
APP_PORT=8080
LOG_LEVEL=INFO
```

---

## Structure des fichiers Docker

| Fichier | Usage |
|---------|-------|
| `Dockerfile` | Build multi-stage (dev + runtime + bot) |
| `docker-compose.dev.yml` | Développement (PostgreSQL + hot-reload) |
| `docker-compose.prod.yml` | Production (PostgreSQL + app + pgadmin) |
| `docker-compose.full.yml` | Stack complète (PostgreSQL + app + bot Discord + Ollama LLM) |
