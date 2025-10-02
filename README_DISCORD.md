# Bot Discord - Architecture Client-Serveur

Ce projet implémente un bot Discord avec une architecture client-serveur utilisant Quarkus, JPA/Hibernate et des endpoints REST.

## 🏗️ Architecture

### Entités JPA

Le projet comprend 5 entités principales avec validation de contraintes :

1. **User** (`discord_user`) - Utilisateurs Discord
   - Username (2-32 caractères)
   - Discriminator (4 chiffres)
   - Email (optionnel, validation email)
   - Avatar URL
   - Flag bot

2. **Guild** (`guild`) - Serveurs Discord
   - Nom (2-100 caractères)
   - Description (max 1024 caractères)
   - Propriétaire
   - Limite de membres (2-800000)

3. **Channel** (`channel`) - Canaux de communication
   - Nom (1-100 caractères)
   - Type (TEXT, VOICE, CATEGORY, NEWS, THREAD)
   - Position, NSFW flag
   - Appartient à une guilde

4. **Role** (`role`) - Rôles et permissions
   - Nom (1-100 caractères)
   - Couleur hexadécimale
   - Permissions granulaires
   - Position dans la hiérarchie

5. **Message** (`message`) - Messages envoyés
   - Contenu (1-2000 caractères)
   - Auteur, canal
   - Support des embeds et pièces jointes
   - Gestion de l'édition et suppression

### Endpoints REST

Chaque entité dispose d'une ressource REST complète :

- `GET /api/users` - Liste des utilisateurs
- `POST /api/users` - Création d'utilisateur
- `GET /api/users/{id}` - Détails d'un utilisateur
- `PUT /api/users/{id}` - Mise à jour
- `DELETE /api/users/{id}` - Suppression

Et de même pour `/api/guilds`, `/api/channels`, `/api/roles`, `/api/messages`.

Endpoints spécialisés :
- `GET /api/messages/channel/{channelId}` - Messages d'un canal
- `POST /api/guilds/{guildId}/members/{userId}` - Ajout de membre
- `GET /api/channels/guild/{guildId}` - Canaux d'une guilde

## 🚀 Démarrage rapide

### Prérequis
- Java 21
- Docker et Docker Compose
- Maven

### 1. Démarrer la base de données

```bash
docker-compose up -d postgres
```

### 2. Lancer l'application en mode développement

```bash
./mvnw compile quarkus:dev
```

L'application sera disponible sur http://localhost:8080

### 3. Interface d'administration

- **Dev UI Quarkus** : http://localhost:8080/q/dev/
- **PgAdmin** : http://localhost:8081 (admin@discord.test / admin)

## 📊 Données de test

Le projet inclut des données de test dans `import.sql` :
- 3 utilisateurs (admin, user, bot)
- 2 guildes
- 4 canaux
- 4 rôles
- 4 messages d'exemple

## 🔧 Configuration

La configuration se trouve dans `application.properties` :
- Base de données PostgreSQL
- CORS activé pour le développement
- Validation des contraintes
- Logging configuré

## 🛠️ Technologies utilisées

- **Quarkus** - Framework Java moderne
- **Hibernate ORM with Panache** - Persistence JPA simplifiée
- **Jakarta REST** - API REST
- **Hibernate Validator** - Validation des contraintes
- **PostgreSQL** - Base de données relationnelle
- **Docker** - Containerisation

## 📝 Validation des contraintes

Les entités incluent des validations complètes :
- `@NotBlank`, `@NotNull` pour les champs obligatoires
- `@Size` pour les limites de taille
- `@Email` pour la validation email
- `@Pattern` pour les formats spécifiques (discriminator, couleur hex)
- `@Min`, `@Max` pour les valeurs numériques

## 🚀 Déploiement

### Build pour production

```bash
./mvnw package
java -jar target/quarkus-app/quarkus-run.jar
```

### Build natif (optionnel)

```bash
./mvnw package -Dnative
./target/BotDiscord-0.0.0-SNAPSHOT-runner
```

## 📚 API Documentation

Une fois l'application démarrée, vous pouvez tester les endpoints avec :
- Swagger UI (si activé)
- Postman/Insomnia
- curl ou httpie

Exemple d'appel :
```bash
# Lister tous les utilisateurs
curl http://localhost:8080/api/users

# Créer un nouvel utilisateur
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"username":"TestUser","discriminator":"1234","email":"test@example.com"}'
```