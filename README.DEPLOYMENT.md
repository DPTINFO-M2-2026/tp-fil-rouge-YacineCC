# 🚀 Guide de Déploiement

Ce guide détaille les différentes méthodes de déploiement du projet BotDiscord.

---

## 📋 Table des matières

- [Prérequis](#-prérequis)
- [Variables d'environnement](#-variables-denvironnement)
- [Déploiement Development](#-déploiement-development)
- [Déploiement Production](#-déploiement-production)
- [Déploiement Full Stack (Docker complet)](#-déploiement-full-stack-docker-complet)
- [Vérification du déploiement](#-vérification-du-déploiement)
- [Arrêt des services](#-arrêt-des-services)
- [Troubleshooting](#-troubleshooting)
- [Maintenance](#-maintenance)

---

## 🔧 Prérequis

### Minimum requis

- **Docker** 20.10+ avec Docker Compose v2+
- **Connexion Internet** (pour télécharger les images et modèles)
- **8 GB RAM minimum** (16 GB recommandé pour Ollama)
- **20 GB d'espace disque** (pour les images Docker et modèles LLM)

### Optionnel (pour développement local)

- **Java JDK 21+** (Temurin, Oracle, ou OpenJDK)
- **Maven 3.9+** (ou utiliser le wrapper `./mvnw` fourni)
- **PostgreSQL 16** (si vous ne souhaitez pas utiliser Docker)

---

## 🔐 Variables d'environnement

### Fichier .env requis

Créez un fichier `.env` à la racine du projet avec les variables suivantes :

```bash
# Discord Bot Token (OBLIGATOIRE)
DISCORD_TOKEN=votre_token_discord_ici

# Base de données (optionnel, valeurs par défaut fournies)
DB_PASSWORD=discord_password
DB_PORT=5432

# Application (optionnel)
APP_PORT=8080
LOG_LEVEL=INFO

# PgAdmin (optionnel, uniquement en mode tools)
PGADMIN_EMAIL=admin@discord-bot.local
PGADMIN_PASSWORD=admin_password
PGADMIN_PORT=5050
```

### Obtenir un token Discord

1. Rendez-vous sur https://discord.com/developers/applications
2. Créez une nouvelle application ou sélectionnez-en une existante
3. Allez dans l'onglet "Bot"
4. Cliquez sur "Reset Token" ou "Copy" pour obtenir votre token
5. **⚠️ Ne partagez JAMAIS ce token !**

---

## 🛠 Déploiement Development

Mode recommandé pour le développement avec hot-reload.

### Caractéristiques
- PostgreSQL en Docker
- Quarkus en mode dev (hot-reload activé)
- Port debug ouvert (5005)
- Dev UI accessible

### Démarrage

```bash
# Lancer l'environnement de développement
./start-dev.sh
```

### Services disponibles
- 🌐 **API REST** : http://localhost:8080
- 🎯 **Dev UI** : http://localhost:8080/q/dev/
- 🐘 **PostgreSQL** : localhost:5432
- 🐛 **Debug** : Port 5005

### Arrêt

```bash
# Arrêter PostgreSQL
docker compose -f docker-compose.dev.yml down

# Ctrl+C dans le terminal Quarkus
```

---

## 🏭 Déploiement Production

Déploiement optimisé pour la production avec images minimales.

### Caractéristiques
- Application compilée et packagée
- Images Docker optimisées (multi-stage build)
- Healthchecks configurés
- Restart automatique

### Démarrage

```bash
# Lancer en production
./start-prod.sh

# Ou avec Docker Compose directement
docker compose -f docker-compose.prod.yml up -d
```

### Services disponibles
- 🌐 **API REST** : http://localhost:8080
- 🐘 **PostgreSQL** : localhost:5432

### Avec PgAdmin (optionnel)

```bash
# Lancer avec PgAdmin
docker compose -f docker-compose.prod.yml --profile tools up -d

# PgAdmin accessible sur http://localhost:5050
```

### Arrêt

```bash
# Arrêter
./stop-prod.sh

# Ou avec Docker Compose
docker compose -f docker-compose.prod.yml down
```

---

## 🌟 Déploiement Full Stack (Docker complet)

**⭐ MODE RECOMMANDÉ** - Tout en Docker incluant Ollama et le Discord Bot.

### Caractéristiques
- ✅ PostgreSQL
- ✅ Ollama avec LLaMA3:8b
- ✅ API REST Quarkus
- ✅ Discord Bot avec IA
- ✅ Tout en un seul réseau Docker isolé

### Démarrage

```bash
# Lancer tout le stack
./start-all.sh
```

**Note :** Le premier démarrage prendra ~10-15 minutes pour télécharger le modèle LLaMA3:8b (4.7 GB).

### Services disponibles
- 🌐 **API REST** : http://localhost:8080
- 🎯 **Dev UI** : http://localhost:8080/q/dev
- 🤖 **Ollama API** : http://localhost:11434
- 🐘 **PostgreSQL** : localhost:5432
- 🤖 **Discord Bot** : Connecté sur Discord

### Télécharger d'autres modèles Ollama

```bash
# Télécharger un modèle spécifique
./ollama-pull-model.sh mistral
./ollama-pull-model.sh codellama

# Lister les modèles disponibles
docker exec discord-bot-ollama ollama list
```

### Arrêt

```bash
# Arrêter tous les services
./stop-all.sh

# Arrêter et supprimer les volumes (⚠️ perte de données)
docker compose -f docker-compose.full.yml down -v
```

---

## ✅ Vérification du déploiement

### Vérifier l'état des services

```bash
# Mode development
docker compose -f docker-compose.dev.yml ps

# Mode production
docker compose -f docker-compose.prod.yml ps

# Mode full stack
docker compose -f docker-compose.full.yml ps
```

### Vérifier les logs

```bash
# Logs de l'application
docker compose -f docker-compose.full.yml logs -f app

# Logs du bot Discord
docker compose -f docker-compose.full.yml logs -f bot

# Logs d'Ollama
docker compose -f docker-compose.full.yml logs -f ollama

# Tous les logs
docker compose -f docker-compose.full.yml logs -f
```

### Tests de santé

```bash
# Vérifier l'API
curl http://localhost:8080/q/health

# Vérifier Ollama
curl http://localhost:11434/api/version

# Vérifier PostgreSQL
docker compose -f docker-compose.full.yml exec db pg_isready -U discord_bot
```

### Tester l'API REST

```bash
# Lister les utilisateurs
curl http://localhost:8080/api/users | jq

# Créer un utilisateur
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "discriminator": "0001",
    "email": "test@example.com"
  }' | jq
```

### Tester le Discord Bot

Dans Discord, utilisez les commandes suivantes :

```
!thrasher              # Poste une cover aléatoire
!thrasher hellbomb     # Poste une vidéo Hellbomb
!thrasher help         # Affiche l'aide

/ask Comment fonctionne Java ?        # Question à l'IA
/translate Hello, how are you?        # Traduction en français
```

---

## 🛑 Arrêt des services

### Arrêt simple (conserve les données)

```bash
# Development
docker compose -f docker-compose.dev.yml down

# Production
docker compose -f docker-compose.prod.yml down

# Full stack
docker compose -f docker-compose.full.yml down
# ou
./stop-all.sh
```

### Arrêt complet (supprime les volumes)

```bash
# ⚠️ ATTENTION : Supprime toutes les données de la base !
docker compose -f docker-compose.full.yml down -v
```

### Nettoyage complet

```bash
# Supprimer les conteneurs, volumes et réseaux
docker compose -f docker-compose.full.yml down -v

# Supprimer les images construites
docker rmi tp-fil-rouge-yacinecc-app tp-fil-rouge-yacinecc-bot

# Nettoyer le système Docker
docker system prune -a --volumes
```

---

## 🔧 Troubleshooting

### Le bot Discord ne démarre pas

**Symptôme :** Le conteneur `discord-bot-thrasher` redémarre en boucle

**Solutions :**
```bash
# Vérifier les logs
docker logs discord-bot-thrasher

# Vérifier que le token est configuré
grep DISCORD_TOKEN .env

# Le token ne doit pas être vide
cat .env | grep DISCORD_TOKEN
```

### Ollama ne se connecte pas

**Symptôme :** Erreur de connexion à Ollama

**Solutions :**
```bash
# Vérifier qu'Ollama est en cours d'exécution
docker ps | grep ollama

# Vérifier les logs d'Ollama
docker logs discord-bot-ollama

# Vérifier la connectivité
curl http://localhost:11434/api/version
```

### Le modèle LLM n'est pas téléchargé

**Symptôme :** Erreur "model not found"

**Solutions :**
```bash
# Télécharger manuellement le modèle
docker exec discord-bot-ollama ollama pull llama3:8b

# Vérifier les modèles disponibles
docker exec discord-bot-ollama ollama list

# Utiliser le script fourni
./ollama-pull-model.sh llama3:8b
```

### Port déjà utilisé

**Symptôme :** `Error: Address already in use`

**Solutions :**
```bash
# Identifier le processus utilisant le port 8080
sudo lsof -i :8080

# Ou pour le port 11434 (Ollama)
sudo lsof -i :11434

# Arrêter Ollama local si nécessaire
sudo systemctl stop ollama
# ou
pkill -f ollama
```

### Base de données corrompue

**Symptôme :** Erreurs de connexion PostgreSQL

**Solutions :**
```bash
# Arrêter et recréer la base
docker compose -f docker-compose.full.yml down -v
docker compose -f docker-compose.full.yml up -d db

# Vérifier les logs
docker logs discord-bot-db
```

### Problème de mémoire avec Ollama

**Symptôme :** Ollama crash ou lent

**Solutions :**
```bash
# Allouer plus de RAM à Docker (Docker Desktop)
# Settings > Resources > Memory : 8GB minimum

# Utiliser un modèle plus léger
./ollama-pull-model.sh llama3.2:1b
```

### Application ne build pas

**Symptôme :** Erreur lors du `docker compose build`

**Solutions :**
```bash
# Nettoyer le cache Maven
./mvnw clean

# Rebuild sans cache
docker compose -f docker-compose.full.yml build --no-cache

# Vérifier Java version
java -version  # Doit être 21+
```

---

## 🔄 Maintenance

### Mise à jour des dépendances

```bash
# Mettre à jour les dépendances Maven
./mvnw versions:display-dependency-updates

# Mettre à jour les images Docker
docker compose -f docker-compose.full.yml pull
```

### Backup de la base de données

```bash
# Créer un backup
docker exec discord-bot-db pg_dump -U discord_bot discord_bot_db > backup.sql

# Restaurer un backup
cat backup.sql | docker exec -i discord-bot-db psql -U discord_bot -d discord_bot_db
```

### Monitoring des logs

```bash
# Suivre les logs en temps réel
docker compose -f docker-compose.full.yml logs -f

# Logs des dernières 24h
docker compose -f docker-compose.full.yml logs --since 24h

# Logs d'un service spécifique
docker compose -f docker-compose.full.yml logs -f bot
```

### Redémarrage d'un service spécifique

```bash
# Redémarrer uniquement le bot
docker compose -f docker-compose.full.yml restart bot

# Redémarrer Ollama
docker compose -f docker-compose.full.yml restart ollama

# Redémarrer l'API
docker compose -f docker-compose.full.yml restart app
```

---

## 📊 Monitoring et Performances

### Utilisation des ressources

```bash
# Statistiques en temps réel
docker stats

# Statistiques d'un conteneur spécifique
docker stats discord-bot-thrasher
```

### Healthcheck status

```bash
# Vérifier le healthcheck de l'API
docker inspect discord-bot-app | jq '.[0].State.Health'

# Vérifier tous les healthchecks
docker compose -f docker-compose.full.yml ps
```

---

## 🔒 Sécurité en Production

### Recommandations

1. **Ne jamais exposer publiquement** :
   - PostgreSQL (port 5432)
   - Ollama (port 11434) - sauf si nécessaire

2. **Utiliser des secrets forts** :
   - Changer `DB_PASSWORD` en production
   - Utiliser un gestionnaire de secrets (Docker Secrets, Vault)

3. **Limiter les permissions** :
   ```bash
   # Limiter l'accès au fichier .env
   chmod 600 .env
   ```

4. **Activer HTTPS** :
   - Utiliser un reverse proxy (Nginx, Traefik)
   - Configurer des certificats SSL/TLS

5. **Monitoring** :
   - Mettre en place Prometheus + Grafana
   - Configurer des alertes

---

## 📞 Support

En cas de problème non résolu :

1. Vérifiez les logs : `docker compose logs -f`
2. Consultez la documentation : [README.md](README.md)
3. Vérifiez les issues GitHub du projet
4. Créez une nouvelle issue avec :
   - Description du problème
   - Logs pertinents
   - Configuration système
   - Étapes pour reproduire

---

**Dernière mise à jour :** 4 février 2026  
**Version :** 1.0.0
