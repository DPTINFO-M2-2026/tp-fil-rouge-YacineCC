# 🐳 Documentation Docker Multi-Étages - Discord Bot

## Vue d'ensemble

Ce projet utilise une architecture Docker multi-étapes optimisée pour une application Quarkus Java 21. L'approche multi-étages permet de :

- **Séparer la compilation de l'exécution** pour des images plus légères
- **Optimiser le cache Docker** pour des builds plus rapides  
- **Améliorer la sécurité** avec des images runtime minimales
- **Réduire la surface d'attaque** en excluant les outils de build

## 🏗️ Architecture Multi-Étapes

### Étape 1 : Builder (Compilation)
```dockerfile
FROM registry.access.redhat.com/ubi8/openjdk-21:1.20 AS builder
```

**Responsabilités :**
- Installation de Maven 3.9.8
- Téléchargement des dépendances (optimisation cache)
- Compilation du code source Java
- Packaging de l'application Quarkus

**Optimisations :**
- Copie du `pom.xml` en premier pour utiliser le cache Docker
- Pré-téléchargement des dépendances avec `dependency:go-offline`
- Build sans tests pour accélérer le processus

### Étape 2 : Runtime (Exécution)
```dockerfile
FROM registry.access.redhat.com/ubi8/openjdk-21-runtime:1.20 AS runtime
```

**Responsabilités :**
- JRE 21 optimisé (pas de JDK complet)
- Configuration sécurisée avec utilisateur non-root
- Copie sélective des artefacts compilés
- Configuration des health checks et monitoring

**Optimisations :**
- Image runtime légère (JRE seulement)
- Utilisateur `discord-bot` (UID 1001) pour la sécurité
- Volumes pour logs et configuration
- Health checks intégrés

## 📁 Structure des fichiers

### Dockerfile principal
```
/Dockerfile                     # Dockerfile multi-étages principal
/docker-build.sh               # Script de build avec options avancées
/docker-stack.sh               # Gestion Docker Compose
/.dockerignore                 # Exclusions pour optimiser le contexte
/docker-compose.yml            # Stack complète avec BDD et monitoring
```

### Fichiers de configuration Docker
```
src/main/docker/
├── Dockerfile.jvm             # Dockerfile Quarkus JVM (existant)
├── Dockerfile.native          # Dockerfile Quarkus Native (existant)  
├── Dockerfile.legacy-jar      # Dockerfile Legacy JAR (existant)
└── Dockerfile.native-micro    # Dockerfile Native Micro (existant)
```

## 🚀 Utilisation

### Build simple
```bash
# Construction basique
docker build -t discord-bot:latest .

# Construction avec notre script optimisé
./docker-build.sh -t latest -e prod
```

### Build avec options avancées
```bash
# Mode développement avec debug
./docker-build.sh -t dev -e dev -v

# Mode production optimisé
./docker-build.sh -t prod -e prod -c -n

# Build jusqu'à l'étape builder seulement
./docker-build.sh -t builder --stage builder
```

### Exécution

#### Mode basique
```bash
docker run --rm -p 8080:8080 discord-bot:latest
```

#### Mode développement avec debug
```bash
docker run --rm \
  -p 8080:8080 \
  -p 5005:5005 \
  -e JAVA_OPTS_APPEND="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005" \
  discord-bot:dev
```

#### Mode production avec volumes
```bash
docker run -d \
  --name discord-bot-prod \
  -p 8080:8080 \
  -v discord-bot-logs:/app/logs \
  -v discord-bot-config:/app/config \
  -e QUARKUS_PROFILE=prod \
  --restart unless-stopped \
  discord-bot:latest
```

## 🔧 Docker Compose Stack

### Démarrage de la stack complète
```bash
# Stack basique (app + PostgreSQL + PgAdmin)
./docker-stack.sh up

# Stack avec monitoring (+ Prometheus + Grafana)
./docker-stack.sh monitor

# Mode développement
./docker-stack.sh dev

# Mode production
./docker-stack.sh prod
```

### Gestion des services
```bash
# Statut des services
./docker-stack.sh status

# Logs en temps réel
./docker-stack.sh logs -f

# Logs d'un service spécifique
./docker-stack.sh logs -s discord-bot

# Redémarrage
./docker-stack.sh restart
```

### Services inclus dans la stack

| Service | Port | Description | URL |
|---------|------|-------------|-----|
| **discord-bot** | 8080 | Application principale | http://localhost:8080 |
| **postgres** | 5432 | Base de données PostgreSQL 15 | localhost:5432 |
| **pgadmin** | 8081 | Interface d'administration BDD | http://localhost:8081 |
| **prometheus** | 9090 | Monitoring des métriques | http://localhost:9090 |
| **grafana** | 3000 | Dashboards et visualisation | http://localhost:3000 |

### Credentials par défaut
```
PgAdmin:    admin@discord-bot.local / admin_secure_2024
Grafana:    admin / grafana_admin_2024
PostgreSQL: discord_bot / discord_password_secure_2024
```

## 🔒 Sécurité

### Utilisateur non-root
```dockerfile
# Création d'un utilisateur dédié
RUN groupadd -r discord-bot --gid=1001 && \
    useradd -r -g discord-bot --uid=1001 --home-dir=/app discord-bot

# Basculement vers l'utilisateur sécurisé  
USER discord-bot
```

### Ports exposés
```dockerfile
EXPOSE 8080    # API REST principale
EXPOSE 8443    # HTTPS (optionnel)
EXPOSE 5005    # Debug JVM (développement uniquement)
```

### Health checks
```dockerfile
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/q/health/ready || exit 1
```

## ⚡ Optimisations

### Cache Docker maximisé
1. **Copie du pom.xml en premier** → Cache des layers de dépendances
2. **Pré-téléchargement des dépendances** → Évite les re-téléchargements
3. **Séparation source/dépendances** → Invalidation cache minimale

### Taille d'image optimisée
- **Builder** : ~800MB (Maven + JDK + sources)
- **Runtime** : ~350MB (JRE + application seulement)
- **Ratio** : 56% de réduction de taille

### Performance runtime
```dockerfile
ENV JAVA_OPTS_APPEND="-Xms256m -Xmx512m -XX:+UseG1GC -XX:MaxGCPauseMillis=100"
```

## 📊 Monitoring et Observabilité

### Health checks Quarkus
```
http://localhost:8080/q/health          # Global health
http://localhost:8080/q/health/ready    # Readiness probe
http://localhost:8080/q/health/live     # Liveness probe
```

### Métriques Prometheus
```
http://localhost:8080/q/metrics         # Métriques application
```

### Configuration Grafana
- Dashboards pré-configurés pour JVM et Quarkus
- Alerting sur les métriques critiques
- Monitoring base de données PostgreSQL

## 🛠️ Développement

### Build en local pour debug
```bash
# Construction jusqu'à l'étape builder
docker build --target builder -t discord-bot:builder .

# Exécution du builder pour debug
docker run -it --rm discord-bot:builder /bin/bash
```

### Hot reload avec volumes
```bash
# Mount du code source pour développement
docker run -it --rm \
  -p 8080:8080 \
  -v $(pwd)/src:/app/src \
  -e QUARKUS_PROFILE=dev \
  discord-bot:dev
```

### Debug distant
```bash
# Activation debug JVM
docker run --rm \
  -p 8080:8080 \
  -p 5005:5005 \
  -e JAVA_OPTS_APPEND="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005" \
  discord-bot:latest
```

## 🔧 Configuration avancée

### Variables d'environnement

| Variable | Défaut | Description |
|----------|--------|-------------|
| `QUARKUS_PROFILE` | - | Profil Quarkus (dev/test/prod) |
| `JAVA_OPTS_APPEND` | - | Options JVM additionnelles |
| `QUARKUS_HTTP_HOST` | 0.0.0.0 | Interface d'écoute |
| `QUARKUS_HTTP_PORT` | 8080 | Port d'écoute HTTP |

### Profils Quarkus supportés
```properties
# application-dev.properties    - Développement (debug, hot reload)
# application-test.properties   - Tests (H2, mocks) 
# application-prod.properties   - Production (PostgreSQL, optimisé)
```

### Personnalisation du build
```bash
# Build avec arguments personnalisés
docker build \
  --build-arg MAVEN_VERSION=3.9.9 \
  --build-arg JAVA_OPTS="-Xms512m" \
  -t discord-bot:custom .
```

## 📋 Checklist de déploiement

### Pré-requis
- [ ] Docker 20.10+ installé
- [ ] Docker Compose 2.0+ installé  
- [ ] Ports 8080, 5432, 8081 disponibles
- [ ] 2GB RAM minimum disponible
- [ ] 5GB espace disque pour volumes

### Build et test
- [ ] `./mvnw clean package` réussit
- [ ] `./docker-build.sh -t test` réussit  
- [ ] `docker run --rm -p 8080:8080 discord-bot:test` démarre
- [ ] `curl http://localhost:8080/q/health` retourne 200

### Déploiement production
- [ ] Configuration des secrets (mots de passe BDD)
- [ ] Configuration des volumes persistants
- [ ] Configuration du reverse proxy/load balancer
- [ ] Configuration des sauvegardes automatiques
- [ ] Configuration monitoring et alerting

## 🐛 Dépannage

### Erreurs courantes

**Build échoue avec "Maven not found"**
```bash
# Solution : Vérifier que le wrapper Maven est exécutable
chmod +x mvnw
```

**Application ne démarre pas**
```bash
# Vérifier les logs
docker logs <container-id>

# Vérifier la configuration
docker exec -it <container-id> env | grep QUARKUS
```

**Connexion BDD échoue**
```bash
# Vérifier que PostgreSQL est démarré
docker-compose ps postgres

# Tester la connexion
docker-compose exec postgres psql -U discord_bot -d discord_bot_db
```

### Logs utiles
```bash
# Logs application
docker-compose logs -f discord-bot

# Logs base de données  
docker-compose logs -f postgres

# Logs tous services
docker-compose logs -f
```

---

Cette documentation couvre tous les aspects du Dockerfile multi-étages et de son utilisation dans le contexte de votre application Discord Bot. 🚀