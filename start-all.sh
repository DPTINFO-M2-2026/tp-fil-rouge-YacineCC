#!/bin/bash

# Script de démarrage complet - Tout en Docker
# Lance: PostgreSQL + Ollama + Quarkus API + Discord Bot

set -e

echo "🚀 Démarrage de l'environnement complet en Docker..."
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

# Vérifier que Docker est disponible
if ! command -v docker &> /dev/null; then
    echo "❌ Docker n'est pas installé ou n'est pas dans le PATH"
    exit 1
fi

# Charger les variables du fichier .env si présent
if [ -f .env ]; then
    echo "📝 Chargement des variables depuis .env..."
    export $(grep -v '^#' .env | xargs)
else
    echo "⚠️  Fichier .env non trouvé"
fi

# Vérifier le token Discord
if [ -z "$DISCORD_TOKEN" ]; then
    echo "❌ DISCORD_TOKEN n'est pas défini !"
    echo "   Créez un fichier .env avec votre token:"
    echo "   DISCORD_TOKEN=votre_token_ici"
    exit 1
fi

echo "✅ Docker disponible"
echo "✅ Token Discord configuré"
echo ""

# Build des images
echo "🔨 Construction des images Docker..."
docker compose -f docker-compose.full.yml build

echo ""
echo "🐳 Démarrage des services..."
echo "   - PostgreSQL (port 5432)"
echo "   - Ollama + LLaMA3:8b (port 11434) - en Docker"
echo "   - Quarkus REST API (port 8080)"
echo "   - Discord Bot (ThrasherBot)"
echo ""

# Lancement
docker compose -f docker-compose.full.yml up -d

echo ""
echo "⏳ Attente du démarrage des services..."
sleep 5

# Vérifier les logs
echo ""
echo "📊 État des services:"
docker compose -f docker-compose.full.yml ps

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "✅ Environnement démarré !"
echo ""
echo "📌 Services disponibles:"
echo "   🌐 API REST:    http://localhost:8080"
echo "   🎯 Dev UI:      http://localhost:8080/q/dev"
echo "   🤖 Ollama:      http://localhost:11434"
echo "   🐘 PostgreSQL:  localhost:5432"
echo ""
echo "📋 Commandes utiles:"
echo "   Logs du bot:        docker compose -f docker-compose.full.yml logs -f bot"
echo "   Logs de l'API:      docker compose -f docker-compose.full.yml logs -f app"
echo "   Logs Ollama:        docker compose -f docker-compose.full.yml logs -f ollama"
echo "   Tous les logs:      docker compose -f docker-compose.full.yml logs -f"
echo "   Arrêter:            docker compose -f docker-compose.full.yml down"
echo "   Arrêter + cleanup:  docker compose -f docker-compose.full.yml down -v"
echo ""
echo "💡 Pour suivre les logs du bot en direct:"
echo "   docker compose -f docker-compose.full.yml logs -f bot"
echo ""
