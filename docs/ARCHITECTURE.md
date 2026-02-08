# Architecture du projet BotDiscord

## Vue d'ensemble

Ce projet suit une **architecture en couches (Layered Architecture)** avec une séparation claire des responsabilités. L'application est construite avec **Quarkus** (framework Java cloud-native) et respecte les principes **SOLID** pour garantir la maintenabilité et l'extensibilité du code.

## Diagrammes UML

Tous les diagrammes sont en **PlantUML** dans le dossier `docs/` :

| Diagramme | Fichier | Description |
|-----------|---------|-------------|
| Modèle de données | [`entity-model.puml`](entity-model.puml) | Classes d'entités JPA, relations, contraintes |
| Architecture Docker | [`architecture-docker.puml`](architecture-docker.puml) | Services, réseaux, dépendances de démarrage |
| Cas d'utilisation | [`use-cases.puml`](use-cases.puml) | Acteurs, fonctionnalités API REST et Bot |
| Séquence — Créer un utilisateur | [`sequence-create-user.puml`](sequence-create-user.puml) | Flux complet POST /api/users (succès + erreur) |
| Séquence — Envoyer un message | [`sequence-send-message.puml`](sequence-send-message.puml) | Vérification de permissions + scan auto |

> 💡 Pour visualiser : installer l'extension **PlantUML** dans VS Code, ou coller le contenu sur [plantuml.com](https://www.plantuml.com/plantuml/uml).

## Architecture globale

```
┌─────────────────────────────────────────────────────────────┐
│                     Clients (HTTP/REST)                       │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│              Couche Présentation (Resources)                  │
│  • UserResource, GuildResource, ChannelResource, etc.        │
│  • Validation des entrées (Bean Validation)                  │
│  • Transformation HTTP ↔ DTO                                 │
│  • Gestion des réponses (codes HTTP, CORS)                   │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│          Couche Service (Business Logic Layer)               │
│  • UserService, GuildService, ChannelService,                │
│    RoleService, MessageService, DiscordBotService            │
│  • Logique métier et règles de gestion                       │
│  • Validation métier complexe                                │
│  • Orchestration des opérations                              │
│  • Transformation Entity ↔ DTO (via Mappers)                 │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│         Couche Persistance (Data Access Layer)               │
│  • Repositories (UserRepository, GuildRepository, etc.)      │
│  • Entités JPA (User, Guild, Role, Channel, Message)        │
│  • Panache (Active Record pattern)                           │
│  • Gestion des transactions (@Transactional)                 │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                  Base de données PostgreSQL                   │
└─────────────────────────────────────────────────────────────┘
```

## Structure des packages

```
fr.univtln.yhaouas846.projet/
│
├── entity/                      # Entités JPA (modèle de données)
│   ├── User.java
│   ├── Guild.java
│   ├── Role.java
│   ├── Channel.java
│   └── Message.java
│
├── dto/                         # Data Transfer Objects (API contracts)
│   ├── UserDTO.java
│   ├── CreateUserDTO.java
│   ├── UpdateUserDTO.java
│   ├── UserSummaryDTO.java
│   ├── GuildDTO.java
│   └── ... (DTOs pour chaque entité)
│
├── repository/                  # Repositories Panache
│   ├── UserRepository.java
│   ├── GuildRepository.java
│   └── ... (un repository par entité)
│
├── service/                     # Services métier
│   ├── UserService.java
│   ├── GuildService.java
│   ├── ChannelService.java
│   ├── RoleService.java
│   ├── MessageService.java
│   ├── DiscordBotService.java
│   ├── mapper/                  # Mappers Entity ↔ DTO
│   │   ├── UserMapper.java
│   │   ├── GuildMapper.java
│   │   ├── ChannelMapper.java
│   │   ├── RoleMapper.java
│   │   └── MessageMapper.java
│   └── exception/               # Exceptions métier
│       ├── BusinessException.java
│       └── ResourceNotFoundException.java
│
├── resource/                    # Ressources REST (contrôleurs)
│   ├── UserResource.java
│   ├── GuildResource.java
│   ├── RoleResource.java
│   ├── ChannelResource.java
│   └── MessageResource.java
│
└── exception/                   # Gestion globale des exceptions
    ├── GlobalExceptionHandler.java
    └── ErrorResponse.java
```

## Principes SOLID appliqués

### 1. **Single Responsibility Principle (SRP)**

Chaque classe a une responsabilité unique :

- **Entities** : représentation du modèle de données
- **DTOs** : contrats d'API (entrée/sortie)
- **Repositories** : accès aux données
- **Services** : logique métier
- **Resources** : gestion des requêtes HTTP
- **Mappers** : transformation Entity ↔ DTO

**Exemple** : `UserService` gère uniquement la logique métier utilisateur, tandis que `UserResource` gère uniquement les requêtes HTTP.

### 2. **Open/Closed Principle (OCP)**

Les classes sont ouvertes à l'extension, fermées à la modification.

- Les services peuvent être étendus sans modifier le code existant
- L'ajout de nouveaux endpoints n'impacte pas les existants
- Le `GlobalExceptionHandler` peut gérer de nouveaux types d'exceptions sans modification

### 3. **Liskov Substitution Principle (LSP)**

Les abstractions sont respectées :

- Toutes les entités étendent `PanacheEntity` de manière cohérente
- Les repositories implémentent `PanacheRepository` avec le même comportement

### 4. **Interface Segregation Principle (ISP)**

Les DTOs sont spécialisés par cas d'usage :

- `CreateUserDTO` pour la création (pas d'ID)
- `UpdateUserDTO` pour la mise à jour (champs optionnels)
- `UserDTO` pour les réponses (complet)
- `UserSummaryDTO` pour les listes (léger)

### 5. **Dependency Inversion Principle (DIP)**

Les dépendances pointent vers des abstractions :

- Les services sont injectés via CDI (`@Inject`)
- Les ressources dépendent des services (pas directement des repositories)
- Utilisation d'interfaces (repositories) plutôt que d'implémentations concrètes

## Patterns de conception utilisés

### 1. **Layered Architecture (Architecture en couches)**

Séparation stricte en couches avec dépendances unidirectionnelles :
`Presentation → Service → Data Access → Database`

### 2. **Service Layer Pattern**

Encapsulation de la logique métier dans des services dédiés, séparés de la couche présentation.

### 3. **Repository Pattern**

Abstraction de l'accès aux données via des repositories Panache.

### 4. **Data Transfer Object (DTO) Pattern**

Séparation entre le modèle de données interne (entités) et les contrats d'API (DTOs).

### 5. **Mapper Pattern**

Classes dédiées (`UserMapper`, etc.) pour transformer les entités en DTOs et vice-versa.

### 6. **Exception Handler Pattern**

Gestion centralisée des exceptions via `GlobalExceptionHandler` pour des réponses d'erreur standardisées.

### 7. **Active Record Pattern (via Panache)**

Les entités héritent de `PanacheEntity` et ont des méthodes de persistance directement disponibles.

### 8. **Dependency Injection (CDI)**

Injection automatique des dépendances via Jakarta CDI pour faciliter les tests et la modularité.

## Flow de données typique

### Exemple : Création d'un utilisateur

```
1. Client → POST /api/users + CreateUserDTO (JSON)
           │
2. UserResource.createUser(CreateUserDTO)
           │
           ├─ Validation Bean Validation (@Valid)
           │
3. UserService.createUser(CreateUserDTO)
           │
           ├─ Validation métier (unicité email, discordId)
           │
           ├─ UserMapper.toEntity(CreateUserDTO) → User
           │
           ├─ User.persist() → Base de données
           │
           ├─ UserMapper.toDTO(User) → UserDTO
           │
4. UserResource → Response HTTP 201 + UserDTO (JSON)
           │
5. Client ← UserDTO
```

### Gestion des erreurs

```
Exception levée dans UserService
           │
           ▼
GlobalExceptionHandler.toResponse(Exception)
           │
           ├─ ResourceNotFoundException → 404
           ├─ BusinessException → 422
           ├─ ConstraintViolationException → 400 + détails
           └─ Exception générique → 500
           │
           ▼
ErrorResponse (JSON standardisé)
           │
           ▼
Client ← HTTP Error + ErrorResponse
```

## Technologies utilisées

| Couche | Technologie | Description |
|--------|-------------|-------------|
| **Framework** | Quarkus 3.28 | Framework Java cloud-native |
| **API REST** | JAX-RS (REST) | Spécification Jakarta EE pour REST |
| **Validation** | Jakarta Bean Validation | Validation déclarative des DTOs |
| **Persistance** | Hibernate ORM + Panache | ORM avec pattern Active Record |
| **Base de données** | PostgreSQL 16 | Base relationnelle (prod), H2 (tests) |
| **Injection de dépendances** | Jakarta CDI | Conteneur IoC |
| **Logging** | JBoss Logging | Logging structuré |
| **Tests** | JUnit 5 + Mockito | Tests unitaires et d'intégration |
| **Containerisation** | Docker + Docker Compose | Déploiement conteneurisé |

## Séparation des responsabilités

### 1. **Resources (Couche Présentation)**

**Responsabilités :**
- Réception des requêtes HTTP
- Validation syntaxique des données (`@Valid`)
- Appel des services métier
- Transformation des résultats en réponses HTTP
- Gestion des codes de statut HTTP

**Ne fait PAS :**
- Logique métier
- Accès direct à la base de données
- Transformation complexe des données

### 2. **Services (Couche Métier)**

**Responsabilités :**
- Validation métier complexe
- Orchestration des opérations
- Gestion des transactions
- Transformation Entity ↔ DTO
- Logging des opérations importantes
- Levée d'exceptions métier

**Ne fait PAS :**
- Manipulation de requêtes/réponses HTTP
- Accès direct aux tables (passe par repositories)

### 3. **Repositories (Couche Accès Données)**

**Responsabilités :**
- Requêtes personnalisées (au-delà du CRUD basique)
- Abstraction de la persistance

**Ne fait PAS :**
- Logique métier
- Transformation DTO

### 4. **Entities (Modèle de Données)**

**Responsabilités :**
- Représentation du modèle de données
- Mapping JPA (annotations `@Entity`, `@ManyToOne`, etc.)
- Lifecycle callbacks (`@PrePersist`, `@PreUpdate`)

**Ne fait PAS :**
- Logique métier complexe
- Validation métier (uniquement validation JPA/Bean Validation)

## Avantages de cette architecture

### 1. **Testabilité**
- Chaque couche peut être testée indépendamment
- Les services peuvent être mockés dans les tests de ressources
- Les repositories peuvent être mockés dans les tests de services

### 2. **Maintenabilité**
- Séparation claire des responsabilités
- Code modulaire et découplé
- Facile de localiser et corriger les bugs

### 3. **Extensibilité**
- Ajout de nouveaux endpoints sans impact sur l'existant
- Ajout de nouvelles entités en suivant le même pattern
- Évolution de l'API sans modifier le modèle de données

### 4. **Sécurité**
- Les entités JPA ne sont jamais exposées directement
- Les DTOs contrôlent exactement quelles données sont exposées
- Validation à plusieurs niveaux (Bean Validation + métier)

### 5. **Performance**
- DTOs légers (`SummaryDTO`) pour les listes
- Chargement optimisé des relations JPA
- Possibilité de caching au niveau service

## Conventions de nommage

| Type | Convention | Exemple |
|------|------------|---------|
| **Entity** | Nom singulier | `User`, `Guild` |
| **DTO** | Nom + DTO | `UserDTO`, `CreateUserDTO` |
| **Service** | Nom + Service | `UserService` |
| **Repository** | Nom + Repository | `UserRepository` |
| **Resource** | Nom + Resource | `UserResource` |
| **Mapper** | Nom + Mapper | `UserMapper` |
| **Exception** | Nom + Exception | `BusinessException` |

## Points d'attention

### 1. **Transactions**
- Les méthodes de service qui modifient des données doivent être annotées `@Transactional`
- Les transactions sont gérées au niveau service, pas au niveau resource

### 2. **Validation**
- Validation syntaxique (Bean Validation) dans les DTOs
- Validation métier (règles complexes) dans les services

### 3. **Gestion des erreurs**
- Utiliser `ResourceNotFoundException` pour les ressources introuvables
- Utiliser `BusinessException` pour les violations de règles métier
- Le `GlobalExceptionHandler` transforme automatiquement en réponses HTTP

### 4. **Logging**
- DEBUG : opérations de lecture
- INFO : opérations de création/modification/suppression
- WARN : tentatives d'opérations invalides
- ERROR : erreurs inattendues

## Évolutions futures possibles

1. **Caching** : Ajout de cache (Redis) au niveau service
2. **Event-Driven** : Émission d'événements lors des opérations métier
3. **API versioning** : Support de plusieurs versions d'API
4. **GraphQL** : Ajout d'une couche GraphQL en complément REST
5. **Audit** : Traçabilité des modifications (qui, quand, quoi)
6. **Security** : Authentification JWT et autorisation basée sur les rôles

## Architecture Docker

> Voir le diagramme complet : [`architecture-docker.puml`](architecture-docker.puml)

### Dockerfile — build multi-stage (5 étapes)

| Étape | Image de base | Rôle |
|-------|--------------|------|
| **deps** | `maven:3.9.9-eclipse-temurin-21` | Pré-télécharge les dépendances Maven (cache Docker) |
| **build** | `maven:3.9.9-eclipse-temurin-21` | Compile et package le fast-jar Quarkus |
| **dev** | `maven:3.9.9-eclipse-temurin-21` | Mode développement avec hot-reload (ports 8080, 5005) |
| **runtime** | `eclipse-temurin:21-jre` | Image de production minimale (JRE, non-root) |
| **bot** | `maven:3.9.9-eclipse-temurin-21` | Exécution standalone du bot Discord4J |

### Docker Compose — 3 variantes

| Fichier | Services | Usage |
|---------|----------|-------|
| `docker-compose.dev.yml` | db + app (dev) | Développement local avec hot-reload |
| `docker-compose.prod.yml` | db + app (runtime) + pgadmin | Production avec healthchecks |
| `docker-compose.full.yml` | db + app + bot + ollama + ollama-pull | Stack complète (API + Bot + IA) |

### Services détaillés (full stack)

```
┌───────────────┐     ┌───────────────┐     ┌───────────────┐
│  PostgreSQL   │◄────│  Quarkus API  │◄────│ ThrasherBot   │
│  :5432        │     │  :8080        │     │ (Discord4J)   │
└───────────────┘     └───────┬───────┘     └───────┬───────┘
                              │                     │
                              │              ┌──────┴───────┐
                              │              │ Discord API  │
                              │              │ (WebSocket)  │
                              │              └──────────────┘
                      ┌───────┴───────┐
                      │  Ollama LLM   │◄─── (LangChain4J)
                      │  :11434 (GPU) │
                      └───────────────┘
```

**Healthcheck API** : `GET /api/users` (pas de SmallRye Health)
**Scan automatique** : toutes les 60 secondes (users, guilds, channels, roles, messages)
**Commandes admin** : scan, createGuild, delete, role, mute/unmute, nick, ban/unban

## Processeur d'annotation custom

Le projet inclut un **processeur d'annotation JSR 269** (`@Logged`) qui intervient à **deux niveaux** :

### Compile-time — `LoggedProcessor`
- Valide que `@Logged` est utilisé sur des méthodes publiques non-statiques
- Génère un rapport `META-INF/logged-methods.txt` listant toutes les méthodes annotées

### Runtime — `LoggedInterceptor` (CDI)
- Intercepte les appels aux méthodes annotées `@Logged`
- Log automatique : ➡ entrée (classe, méthode, arguments), ✅ sortie (durée), ❌ erreur
- Transparent : ne modifie pas le comportement métier
