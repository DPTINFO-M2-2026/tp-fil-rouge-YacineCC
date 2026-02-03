# 🤖 BotDiscord - API REST de gestion Discord

[![Java CI with Maven](https://github.com/YacineCC/tp-fil-rouge-YacineCC/workflows/Java%20CI%20with%20Maven/badge.svg)](https://github.com/YacineCC/tp-fil-rouge-YacineCC/actions)
[![Quarkus](https://img.shields.io/badge/Quarkus-3.28-blue)](https://quarkus.io/)
[![Java](https://img.shields.io/badge/Java-21-orange)](https://openjdk.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue)](https://www.postgresql.org/)

API REST complète pour la gestion d'utilisateurs, serveurs (guildes), canaux, rôles et messages, inspirée de l'architecture Discord. Développée avec **Quarkus** (framework Java cloud-native), l'application suit une architecture en couches respectant les principes **SOLID** et les bonnes pratiques d'ingénierie logicielle.

---

## 📋 Table des matières

- [Vue d'ensemble](#-vue-densemble)
- [Architecture](#-architecture)
- [Prérequis](#-prérequis)
- [Installation](#-installation)
- [Utilisation](#-utilisation)
- [Documentation de l'API](#-documentation-de-lapi)
- [Modèle de données](#-modèle-de-données)
- [Structure du projet](#-structure-du-projet)
- [Tests](#-tests)
- [Contribuer](#-contribuer)

---

## 🎯 Vue d'ensemble

Ce projet est un TP fil rouge de révision avancée en Java qui implémente :

- ✅ **API REST complète** avec JAX-RS (CRUD pour Users, Guilds, Roles, Channels, Messages)
- ✅ **Architecture en couches** (Resources → Services → Repositories → Entities)
- ✅ **Persistance JPA** avec Hibernate et Panache (PostgreSQL en production, H2 pour les tests)
- ✅ **DTOs séparés des entités** pour une API robuste et sécurisée
- ✅ **Validation** à plusieurs niveaux (Bean Validation + validation métier)
- ✅ **Gestion d'erreurs standardisée** via GlobalExceptionHandler
- ✅ **Tests unitaires et d'intégration** (JUnit 5 + Mockito)
- ✅ **Dockerisation complète** (Dockerfile multi-stage + Docker Compose)
- ✅ **CI/CD** avec GitHub Actions
- ✅ **Documentation** complète (Javadoc, README, Architecture, UML)

### Fonctionnalités principales

- Gestion complète des utilisateurs (création, modification, suppression, recherche)
- Gestion des serveurs Discord (guildes) avec propriétaires et membres
- Système de rôles avec permissions granulaires
- Canaux de communication (TEXT, VOICE, CATEGORY, NEWS, THREAD)
- Messages avec support d'embeds et attachements
- Suppression logique des messages (soft delete)
- Validation métier (unicité email, discordId, contraintes métier)
- Pagination et filtres sur les listes

---

## 🏗 Architecture

L'application suit une **architecture en couches** avec séparation stricte des responsabilités :

```
┌─────────────────────────────────────────┐
│    Clients (HTTP/REST)                  │
└─────────────────────────────────────────┘
              │
              ▼
┌─────────────────────────────────────────┐
│  Resources (UserResource, etc.)         │  ← Couche Présentation
│  • Validation syntaxique                │
│  • Transformation HTTP ↔ DTO            │
└─────────────────────────────────────────┘
              │
              ▼
┌─────────────────────────────────────────┐
│  Services (UserService, etc.)           │  ← Couche Métier
│  • Logique métier                       │
│  • Validation métier                    │
│  • Transformation Entity ↔ DTO          │
└─────────────────────────────────────────┘
              │
              ▼
┌─────────────────────────────────────────┐
│  Repositories & Entities                │  ← Couche Persistance
│  • JPA + Panache                        │
└─────────────────────────────────────────┘
              │
              ▼
┌─────────────────────────────────────────┐
│  PostgreSQL                             │  ← Base de données
└─────────────────────────────────────────┘
```

**Documentation détaillée** : Consultez [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) pour une description complète des patterns et principes SOLID appliqués.

---

## 🔧 Prérequis

### Pour Docker (recommandé)

- [Docker](https://www.docker.com/) 20.10+
- [Docker Compose](https://docs.docker.com/compose/) v2+

### Pour installation locale

- [Java 21](https://openjdk.org/) (OpenJDK ou Oracle JDK)
- [Maven 3.9+](https://maven.apache.org/)
- [PostgreSQL 16](https://www.postgresql.org/) (optionnel, H2 peut être utilisé)

---

## 📦 Installation

### Avec Docker (recommandé)

#### 1. Cloner le dépôt

```bash
git clone https://github.com/YacineCC/tp-fil-rouge-YacineCC.git
cd tp-fil-rouge-YacineCC
```

#### 2. Lancer l'application avec Docker Compose

**Mode développement** (avec hot reload) :

```bash
docker compose -f docker-compose.dev.yml up --build
```

L'application sera accessible sur :
- API : http://localhost:8080
- Dev UI Quarkus : http://localhost:8080/q/dev/
- Debug Java : port 5005 (optionnel)

**Arrêter l'application** :

```bash
docker compose -f docker-compose.dev.yml down
```

**Réinitialiser complètement** (supprime la base de données) :

```bash
docker compose -f docker-compose.dev.yml down -v
```

#### 3. Build d'une image de production

```bash
docker build --target runtime -t botdiscord:runtime .
```

Exécuter l'image :

```bash
docker run --rm -p 8080:8080 botdiscord:runtime
```

---

### Installation locale

#### 1. Cloner le dépôt

```bash
git clone https://github.com/YacineCC/tp-fil-rouge-YacineCC.git
cd tp-fil-rouge-YacineCC
```

#### 2. Configurer PostgreSQL (optionnel)

Si vous utilisez PostgreSQL localement, créez une base de données :

```sql
CREATE DATABASE discord_bot_db;
CREATE USER discord_bot WITH PASSWORD 'discord_password';
GRANT ALL PRIVILEGES ON DATABASE discord_bot_db TO discord_bot;
```

Modifiez `src/main/resources/application.properties` si nécessaire.

#### 3. Compiler et démarrer

```bash
./mvnw quarkus:dev
```

L'application démarre sur http://localhost:8080

---

## 🚀 Utilisation

### Démarrage en mode développement

Le mode développement de Quarkus offre le **hot reload** (rechargement automatique du code) :

```bash
./mvnw quarkus:dev
```

Fonctionnalités disponibles en mode dev :
- Rechargement automatique des modifications Java
- Dev UI : http://localhost:8080/q/dev/
- Debugging distant (port 5005)

### Tests

#### Exécuter tous les tests

```bash
./mvnw test
```

#### Exécuter un test spécifique

```bash
./mvnw test -Dtest=UserServiceTest
```

Les rapports de tests se trouvent dans `target/surefire-reports/`.

### Build de production

#### JAR standard

```bash
./mvnw package
java -jar target/quarkus-app/quarkus-run.jar
```

#### Uber-JAR (JAR exécutable autonome)

```bash
./mvnw package -Dquarkus.package.jar.type=uber-jar
java -jar target/*-runner.jar
```

#### Exécutable natif (GraalVM)

```bash
./mvnw package -Dnative
./target/BotDiscord-0.0.0-SNAPSHOT-runner
```

---

## 📚 Documentation de l'API

### Endpoints principaux

#### 👤 Utilisateurs (`/api/users`)

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| `GET` | `/api/users` | Liste tous les utilisateurs |
| `GET` | `/api/users/{id}` | Récupère un utilisateur par ID |
| `POST` | `/api/users` | Crée un nouvel utilisateur |
| `PUT` | `/api/users/{id}` | Met à jour un utilisateur |
| `DELETE` | `/api/users/{id}` | Supprime un utilisateur |
| `GET` | `/api/users/username/{username}` | Recherche par nom d'utilisateur |
| `GET` | `/api/users/bots` | Liste des comptes bots |

**Exemple de création** :

```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "alice",
    "discriminator": "1234",
    "email": "alice@example.com",
    "isBot": false
  }'
```

#### 🏰 Guildes (`/api/guilds`)

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| `GET` | `/api/guilds` | Liste toutes les guildes |
| `GET` | `/api/guilds/{id}` | Récupère une guilde par ID |
| `POST` | `/api/guilds` | Crée une nouvelle guilde |
| `PUT` | `/api/guilds/{id}` | Met à jour une guilde |
| `DELETE` | `/api/guilds/{id}` | Supprime une guilde |

#### 📝 Messages (`/api/messages`)

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| `GET` | `/api/messages?limit=50` | Liste les messages (avec pagination) |
| `GET` | `/api/messages/{id}` | Récupère un message par ID |
| `POST` | `/api/messages` | Crée un nouveau message |
| `PUT` | `/api/messages/{id}` | Modifie un message |
| `DELETE` | `/api/messages/{id}` | Supprime un message (soft delete) |

#### 🎭 Rôles (`/api/roles`)

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| `GET` | `/api/roles` | Liste tous les rôles |
| `GET` | `/api/roles/{id}` | Récupère un rôle par ID |
| `POST` | `/api/roles` | Crée un nouveau rôle |

#### 📢 Canaux (`/api/channels`)

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| `GET` | `/api/channels` | Liste tous les canaux |
| `GET` | `/api/channels/{id}` | Récupère un canal par ID |
| `POST` | `/api/channels` | Crée un nouveau canal |

### Format des réponses d'erreur

Toutes les erreurs suivent un format standardisé :

```json
{
  "timestamp": "2026-02-03T14:30:45",
  "status": 404,
  "error": "Not Found",
  "message": "Utilisateur avec id=123 introuvable",
  "path": "/api/users/123"
}
```

**Codes HTTP utilisés** :
- `200 OK` : Requête réussie
- `201 Created` : Ressource créée
- `204 No Content` : Suppression réussie
- `400 Bad Request` : Données invalides
- `404 Not Found` : Ressource introuvable
- `422 Unprocessable Entity` : Violation de règle métier
- `500 Internal Server Error` : Erreur serveur

---

## 🗄 Modèle de données

### Diagramme UML

Consultez le diagramme complet dans [docs/ENTITY_MODEL.md](docs/ENTITY_MODEL.md) ou [docs/entity-model.puml](docs/entity-model.puml) (PlantUML).

### Entités principales

#### User (Utilisateur)
- Identifiant unique, username, discriminator
- Email (unique), avatarUrl, discordId (unique)
- Relations : messages, guilds, roles

#### Guild (Serveur)
- Identifiant unique, nom, description, icône
- Propriétaire (User), membres (ManyToMany)
- Relations : channels, roles

#### Channel (Canal)
- Identifiant unique, nom, type (TEXT/VOICE/etc.)
- Appartient à une guilde
- Contient des messages

#### Role (Rôle)
- Identifiant unique, nom, couleur
- Permissions granulaires
- Assigné à des utilisateurs (ManyToMany)

#### Message
- Contenu, auteur, canal
- Support embeds et attachments
- Suppression logique (isDeleted)

---

## 📁 Structure du projet

```
tp-fil-rouge-YacineCC/
├── docs/                           # Documentation
│   ├── ARCHITECTURE.md             # Architecture détaillée
│   ├── ENTITY_MODEL.md             # Modèle de données
│   └── entity-model.puml           # Diagramme UML PlantUML
├── src/
│   ├── main/java/.../projet/
│   │   ├── entity/                 # Entités JPA
│   │   ├── dto/                    # Data Transfer Objects
│   │   ├── repository/             # Repositories Panache
│   │   ├── service/                # Services métier
│   │   │   ├── mapper/             # Mappers Entity ↔ DTO
│   │   │   └── exception/          # Exceptions métier
│   │   ├── resource/               # Ressources REST
│   │   └── exception/              # Gestion globale des exceptions
│   └── test/                       # Tests unitaires et d'intégration
├── docker-compose.dev.yml          # Docker Compose (dev)
├── Dockerfile                      # Dockerfile multi-stage
└── pom.xml                         # Configuration Maven
```

---

## 🧪 Tests

Le projet inclut une suite de tests complète :

- **Tests d'entités** : Validation contraintes JPA, lifecycle callbacks
- **Tests de services** : Logique métier, validation métier, exceptions
- **Tests de ressources** : Endpoints REST, codes HTTP
- **Tests d'intégration** : Scénarios complets avec H2

**Couverture** : >80% sur les services et ressources principales

---

## 🤝 Contribuer

Les contributions sont bienvenues ! Pour contribuer :

1. Forkez le projet
2. Créez une branche de feature
3. Committez vos changements
4. Poussez vers la branche
5. Ouvrez une Pull Request

**Conventions** : Respecter SOLID, ajouter tests, documenter avec Javadoc

---

## 🔗 Liens utiles

- [Quarkus Documentation](https://quarkus.io/guides/)
- [Jakarta EE](https://jakarta.ee/)
- [Hibernate ORM](https://hibernate.org/orm/)
- [PostgreSQL](https://www.postgresql.org/docs/)
- [Docker](https://docs.docker.com/)

---

## 👨‍💻 Auteur

**Yacine HAOUAS**  
Université de Toulon - Master Informatique  
Encadré par Emmanuel BRUNO

---

## 🙏 Remerciements

- Emmanuel BRUNO pour l'encadrement du projet
- Communauté Quarkus pour l'excellent framework
- Discord pour l'inspiration du modèle de données
