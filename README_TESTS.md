# Tests du Bot Discord 🧪

Ce projet contient une suite complète de tests couvrant tous les aspects du bot Discord.

## 📋 Types de Tests

### 1. **Tests Unitaires** (`*Test.java`)
Tests des composants individuels avec mocking et validation.

#### 🏗️ **Tests d'Entités** (`src/test/java/.../entity/`)
- **`UserTest.java`** - Validation des contraintes utilisateur
- **`GuildTest.java`** - Validation des contraintes de guilde  
- **`ChannelTest.java`** - Validation des contraintes de canal
- **`RoleTest.java`** - Validation des contraintes de rôle
- **`MessageTest.java`** - Validation des contraintes de message

Couvrent :
- Validation des annotations (`@NotBlank`, `@Size`, `@Email`, etc.)
- Valeurs par défaut
- Lifecycle callbacks (`@PrePersist`, `@PreUpdate`)
- Relations entre entités

#### 🔧 **Tests de Services** (`src/test/java/.../service/`)
- **`DiscordBotServiceTest.java`** - Logique métier du bot

Couvrent :
- Création automatique de guildes avec canaux/rôles
- Gestion des permissions utilisateur
- Envoi de messages avec vérification
- Gestion des membres de guilde
- Tests de sécurité et autorisations

### 2. **Tests d'Intégration** (`*ResourceTest.java`)
Tests des endpoints REST avec base H2 en mémoire.

#### 🌐 **Tests d'API REST** (`src/test/java/.../resource/`)
- **`UserResourceTest.java`** - CRUD utilisateurs via API
- **`GuildResourceTest.java`** - CRUD guildes via API
- **`BotResourceTest.java`** - Endpoints spécialisés du bot

Couvrent :
- Codes de statut HTTP
- Validation JSON
- Sérialisation/désérialisation
- Gestion des erreurs
- Endpoints spécifiques métier

### 3. **Tests Fonctionnels** (`*FunctionalTest.java`, `*IntegrationTest.java`)
Tests de scénarios complets end-to-end.

#### 🚀 **Tests Complets** (`src/test/java/.../`)
- **`DiscordBotFunctionalTest.java`** - Scénario utilisateur complet
- **`DiscordBotIntegrationTest.java`** - Tests avec TestContainers

Couvrent :
- Flux utilisateur complet (création → utilisation → nettoyage)
- Intégration entre tous les composants
- Persistance et cohérence des données
- Tests avec vraie base PostgreSQL

## 🚀 Exécution des Tests

### **Méthode Simple**
```bash
# Tous les tests automatiquement
./run-tests.sh
```

### **Méthodes Spécifiques**

```bash
# Tests unitaires seulement
./mvnw test -Dtest="*Test"

# Tests d'intégration seulement  
./mvnw test -Dtest="*ResourceTest,*ServiceTest"

# Tests fonctionnels seulement
./mvnw test -Dtest="*FunctionalTest,*IntegrationTest"

# Test spécifique
./mvnw test -Dtest="UserTest"

# Tests par catégorie
./mvnw test -Dtest="*entity*Test"  # Tests d'entités
./mvnw test -Dtest="*resource*Test" # Tests d'API
./mvnw test -Dtest="*service*Test"  # Tests de services
```

### **Modes Avancés**

```bash
# Mode debug
./mvnw test -Dmaven.surefire.debug

# Tests avec profil spécifique
./mvnw test -Pnative

# Tests avec couverture
./mvnw test jacoco:report

# Tests en parallèle
./mvnw test -DforkCount=4
```

## 📊 Configuration des Tests

### **Base de Données de Test**
- **Tests unitaires/intégration** : H2 en mémoire
- **Tests fonctionnels** : PostgreSQL via TestContainers
- **Données** : `test-data.sql` avec jeu de données cohérent

### **Configuration**
```properties
# src/test/resources/application.properties
quarkus.datasource.db-kind=h2
quarkus.datasource.jdbc.url=jdbc:h2:mem:testdb
quarkus.hibernate-orm.database.generation=drop-and-create
quarkus.hibernate-orm.sql-load-script=test-data.sql
```

## 🔍 Couverture de Tests

### **Entités JPA**
- ✅ Validation des contraintes complète
- ✅ Relations bidirectionnelles
- ✅ Lifecycle callbacks
- ✅ Valeurs par défaut

### **Services Métier**
- ✅ Logique de création de guilde
- ✅ Gestion des permissions
- ✅ Envoi de messages sécurisé
- ✅ Gestion des membres
- ✅ Tests de sécurité

### **API REST**  
- ✅ CRUD complet pour toutes les entités
- ✅ Validation des données d'entrée
- ✅ Codes de statut appropriés
- ✅ Gestion des erreurs
- ✅ Endpoints métier spécialisés

### **Scénarios Fonctionnels**
- ✅ Cycle de vie utilisateur complet
- ✅ Création et gestion de guilde
- ✅ Envoi et gestion de messages
- ✅ Système de permissions
- ✅ Intégrité des données

## 📁 Structure des Tests

```
src/test/java/fr/univtln/yhaouas846/projet/
├── entity/                    # Tests unitaires entités
│   ├── UserTest.java
│   ├── GuildTest.java  
│   ├── ChannelTest.java
│   ├── RoleTest.java
│   └── MessageTest.java
├── service/                   # Tests unitaires services
│   └── DiscordBotServiceTest.java
├── resource/                  # Tests d'intégration API
│   ├── UserResourceTest.java
│   ├── GuildResourceTest.java
│   └── BotResourceTest.java
├── DiscordBotFunctionalTest.java    # Tests fonctionnels
├── DiscordBotIntegrationTest.java   # Tests d'intégration
└── GreetingResourceTest.java        # Test de sanité

src/test/resources/
├── application.properties     # Config tests
└── test-data.sql             # Données de test
```

## 🎯 Bonnes Pratiques Implementées

### **Organisation**
- Tests organisés par couche (entité, service, resource)
- Noms explicites et conventions cohérentes
- Ordre d'exécution maîtrisé (`@Order`)

### **Données de Test**
- Jeu de données cohérent et réaliste
- IDs fixes pour les tests prévisibles
- Isolation entre les tests

### **Assertions**
- Vérifications complètes des contraintes
- Tests de cas limites et d'erreur
- Validation des relations entre entités

### **Performance**
- Base H2 en mémoire pour la rapidité
- `@TestTransaction` pour l'isolation
- Parallélisation possible

## 🛠️ Outils Utilisés

- **JUnit 5** - Framework de test principal
- **RestAssured** - Tests d'API REST
- **Hibernate Validator** - Tests de validation
- **TestContainers** - Tests d'intégration avec vraie DB
- **H2 Database** - Base en mémoire pour tests rapides
- **Quarkus Test** - Intégration native avec Quarkus

## 📈 Métriques et Rapports

### **Génération de Rapports**
```bash
# Rapport de couverture
./mvnw jacoco:report

# Rapports Surefire
ls target/surefire-reports/

# Logs détaillés
ls target/test-classes/
```

### **CI/CD Integration**
Les tests sont prêts pour l'intégration dans une pipeline CI/CD :
- Scripts automatisés
- Codes de sortie appropriés  
- Rapports XML/HTML
- Configuration Docker incluse

## 🚨 Dépannage

### **Erreurs Communes**

**Port déjà utilisé**
```bash
docker-compose down
./run-tests.sh
```

**Base de données verrouillée**
```bash
./mvnw clean test
```

**Problèmes de permissions**
```bash
chmod +x run-tests.sh
chmod +x test-api.sh
```

**Mémoire insuffisante**
```bash
export MAVEN_OPTS="-Xmx2g"
./mvnw test
```

## 📚 Documentation Complémentaire

- **README_DISCORD.md** - Documentation générale du projet
- **Javadoc** - Documentation du code dans les sources
- **Quarkus Testing Guide** - https://quarkus.io/guides/getting-started-testing