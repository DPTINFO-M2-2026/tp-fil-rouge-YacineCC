# 🤖 BotDiscord — API REST de gestion Discord

[![Java CI with Maven](https://github.com/YacineCC/tp-fil-rouge-YacineCC/workflows/Java%20CI%20with%20Maven/badge.svg)](https://github.com/YacineCC/tp-fil-rouge-YacineCC/actions)
![Java 21](https://img.shields.io/badge/Java-21-orange)
![Quarkus 3.28](https://img.shields.io/badge/Quarkus-3.28-blue)
![PostgreSQL 16](https://img.shields.io/badge/PostgreSQL-16-336791)

> TP fil rouge — Révision avancée Java · Université de Toulon  
> **Auteur :** Yacine HAOUAS · **Encadrant :** Emmanuel BRUNO

---

## Présentation

API REST complète modélisant l'écosystème Discord (utilisateurs, serveurs, canaux, rôles, messages).  
Le projet met en œuvre une **architecture en couches**, les **principes SOLID**, et un pipeline CI/CD complet.

### Fonctionnalités

| Domaine | Détails |
|---------|---------|
| **CRUD complet** | Users, Guilds, Channels, Roles, Messages |
| **Validation** | Bean Validation (syntaxique) + validation métier |
| **Erreurs standardisées** | `GlobalExceptionHandler` → réponses JSON normalisées |
| **DTOs** | Séparation entités / contrats API (Create, Update, Summary) |
| **Suppression logique** | Soft-delete sur les messages |
| **Pagination & filtres** | `limit`, `offset`, filtres par canal, auteur, recherche textuelle |
| **Bot Discord** | ThrasherBot réactif (Discord4J + Project Reactor) |
| **Modération** | Mute/unmute, changement de pseudo, ban/unban |
| **IA (Ollama)** | Intégration LangChain4J pour requêtes LLM |
| **Dockerisation** | Dockerfile multi-stage + Docker Compose (dev / prod / full) |
| **CI/CD** | GitHub Actions (build + tests automatiques) |
| **Tests** | JUnit 5, Mockito, RestAssured — entités, services, resources, intégration |

---

## Architecture

```
Clients (HTTP)
      │
      ▼
┌──────────────────────────┐
│  Resources  (JAX-RS)     │  Couche Présentation
│  validation + HTTP       │
└──────────┬───────────────┘
           ▼
┌──────────────────────────┐
│  Services                │  Couche Métier
│  logique + transactions  │
│  + Mappers Entity↔DTO   │
└──────────┬───────────────┘
           ▼
┌──────────────────────────┐
│  Repositories (Panache)  │  Couche Persistance
│  + Entities JPA          │
└──────────┬───────────────┘
           ▼
       PostgreSQL
```

### Packages

```
fr.univtln.yhaouas846.projet
├── entity/          # Entités JPA (User, Guild, Channel, Role, Message)
├── dto/             # Data Transfer Objects (Create*, Update*, *Summary)
├── repository/      # Repositories Panache
├── service/         # Services métier
│   ├── mapper/      # Mappers Entity ↔ DTO
│   └── exception/   # BusinessException, ResourceNotFoundException
├── resource/        # Endpoints REST
└── exception/       # GlobalExceptionHandler, ErrorResponse

fr.univtln.yhaouas846.discord4j
├── ThrasherBotReactive.java   # Bot Discord réactif
├── services/                  # DiscordBotService, LangChain4jClient, OllamaClient
└── examples/                  # Exemples pédagogiques
```

### Principes SOLID

| Principe | Application |
|----------|-------------|
| **SRP** | 1 classe = 1 responsabilité (Service ≠ Resource ≠ Mapper) |
| **OCP** | Extension sans modification (nouveaux endpoints, exceptions) |
| **LSP** | Héritage cohérent via PanacheEntity / PanacheRepository |
| **ISP** | DTOs spécialisés par cas d'usage |
| **DIP** | Injection CDI, dépendances vers abstractions |

### Design Patterns

Service Layer · Repository · DTO · Mapper · Exception Handler · Active Record (Panache) · Dependency Injection (CDI)

---

## Modèle de données

5 entités avec relations JPA complètes :

| Entité | Description | Relations clés |
|--------|-------------|----------------|
| **User** | Utilisateur Discord | → Messages (1-N), ↔ Guilds (N-N), ↔ Roles (N-N) |
| **Guild** | Serveur Discord | → Owner (N-1), → Channels (1-N), → Roles (1-N) |
| **Channel** | Canal (TEXT, VOICE, CATEGORY…) | → Guild (N-1), → Messages (1-N) |
| **Role** | Rôle avec permissions | → Guild (N-1), ↔ Users (N-N) |
| **Message** | Message avec embeds/attachments | → Author (N-1), → Channel (N-1) |

> Diagramme UML complet : [docs/ENTITY_MODEL.md](docs/ENTITY_MODEL.md) et [docs/entity-model.puml](docs/entity-model.puml)

---

## API REST

### Endpoints principaux

| Ressource | Endpoints |
|-----------|-----------|
| `/api/users` | `GET` · `POST` · `PUT /{id}` · `DELETE /{id}` · `GET /username/{u}` · `GET /bots` |
| `/api/guilds` | `GET` · `POST` · `PUT /{id}` · `DELETE /{id}` |
| `/api/channels` | `GET` · `POST` · `GET /{id}` · `PUT /{id}` · `DELETE /{id}` · `GET /guild/{guildId}` · `GET /type/{type}` |
| `/api/roles` | `GET` · `POST` · `GET /{id}` · `PUT /{id}` · `DELETE /{id}` · `GET /guild/{guildId}` · `POST /{roleId}/users/{userId}` · `DELETE /{roleId}/users/{userId}` |
| `/api/messages` | `GET ?limit=` · `POST` · `GET /{id}` · `PUT /{id}` · `DELETE /{id}` · `GET /channel/{cId}` · `GET /user/{uId}` · `GET /search?content=` |
| `/api/bot` | `GET /health` · `POST /guilds` · `POST /messages` · `DELETE /messages/{id}` · `GET /messages/channel/{cId}` · `POST /guilds/{gId}/members/{uId}` · `GET /users/{uId}/guilds` · `GET /users/{uId}/permissions` |

### Format d'erreur standardisé

```json
{
  "timestamp": "2026-02-07T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Utilisateur avec id=42 introuvable",
  "path": "/api/users/42"
}
```

---

## Tests

141 tests unitaires et d'intégration (JUnit 5 + Mockito + RestAssured) :

| Type | Fichiers | Couverture |
|------|----------|------------|
| Entités | `UserTest`, `GuildTest`, `ChannelTest`, `RoleTest`, `MessageTest` | Validation, lifecycle, relations |
| Services | `UserServiceTest`, `DiscordBotServiceMockTest`, `UserMapperTest` | Logique métier, mocks Mockito |
| Resources | `UserResourceTest`, `GuildResourceTest`, `ChannelResourceTest`, `RoleResourceTest`, `BotResourceTest`, `BotResourceMockTest` | Endpoints REST, codes HTTP |
| Intégration | `MessageResourceIntegrationTest` | Scénarios end-to-end (H2) |
| Annotation | `LoggedInterceptorTest` | Processeur d'annotation `@Logged` |

```bash
./mvnw test                         # Tous les tests
./mvnw test -Dtest=UserServiceTest  # Test spécifique
```

---

## Stack technique

| Couche | Technologie |
|--------|-------------|
| Framework | Quarkus 3.28 |
| API REST | Jakarta REST (JAX-RS) |
| Persistance | Hibernate ORM + Panache |
| Base de données | PostgreSQL 16 (prod) / H2 (tests) |
| Validation | Jakarta Bean Validation |
| Tests | JUnit 5 + Mockito + RestAssured |
| Bot Discord | Discord4J + Project Reactor |
| IA | LangChain4J + Ollama |
| Conteneurs | Docker + Docker Compose |
| CI/CD | GitHub Actions |

---

## Documentation

| Fichier | Contenu |
|---------|---------|
| **[README.md](README.md)** | Ce fichier — vue d'ensemble du projet |
| **[INSTALL.md](INSTALL.md)** | Installation, démarrage, déploiement |
| [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) | Architecture détaillée, SOLID, patterns |
| [docs/ENTITY_MODEL.md](docs/ENTITY_MODEL.md) | Diagramme UML et relations |
| [docs/entity-model.puml](docs/entity-model.puml) | Modèle de données PlantUML |
| [docs/architecture-docker.puml](docs/architecture-docker.puml) | Architecture Docker (services, réseaux) |
| [docs/use-cases.puml](docs/use-cases.puml) | Cas d'utilisation (acteurs, fonctionnalités) |
| [docs/sequence-create-user.puml](docs/sequence-create-user.puml) | Séquence — création d'un utilisateur |
| [docs/sequence-send-message.puml](docs/sequence-send-message.puml) | Séquence — envoi de message avec permissions |
