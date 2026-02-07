#!/bin/bash

# Lance toute la stack : PostgreSQL + Ollama/LLM + API Quarkus + Bot Discord
# Utilise docker-compose.full.yml

set -e

echo "🚀 Démarrage de la stack complète..."
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

# --- Vérifications -----------------------------------------------------------

if ! command -v docker &> /dev/null; then
    echo "❌ Docker n'est pas installé"
    exit 1
fi

# Charger .env si présent
if [ -f .env ]; then
    export $(grep -v '^#' .env | xargs 2>/dev/null)
fi

if [ -z "$DISCORD_TOKEN" ]; then
    echo "⚠️  DISCORD_TOKEN non défini — le bot Discord ne démarrera pas."
    echo "   Créez un fichier .env avec : DISCORD_TOKEN=votre_token"
    echo ""
    read -p "Continuer sans le bot ? (Y/n) " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Nn]$ ]]; then exit 1; fi
fi

echo "✅ Docker disponible"

# --- Build & lancement -------------------------------------------------------

echo ""
echo "🔨 Build des images Docker..."
docker compose -f docker-compose.full.yml build

echo ""
echo "🐳 Démarrage des services..."
docker compose -f docker-compose.full.yml up -d

# --- Attente PostgreSQL -------------------------------------------------------

echo ""
echo "⏳ Attente de PostgreSQL..."
until docker compose -f docker-compose.full.yml exec -T db pg_isready -U discord_bot -d discord_bot_db &> /dev/null 2>&1; do
    printf "."
    sleep 1
done
echo " ✅"

# --- Attente API --------------------------------------------------------------

echo "⏳ Attente de l'API Quarkus..."
for i in $(seq 1 60); do
    if curl -sf http://localhost:8080/q/health/ready &> /dev/null; then
        echo " ✅"
        break
    fi
    printf "."
    sleep 2
done

# --- Résumé -------------------------------------------------------------------

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "✅ Stack démarrée"
echo ""
echo "  🐘 PostgreSQL   : localhost:5432"
echo "  🌐 API REST     : http://localhost:8080"
echo "  🤖 Ollama (LLM) : http://localhost:11434"
echo "  🛹 Bot Discord  : actif (si token configuré)"
echo ""
echo "📋 Commandes utiles :"
echo "  Logs        : docker compose -f docker-compose.full.yml logs -f"
echo "  Logs API    : docker compose -f docker-compose.full.yml logs -f app"
echo "  Logs bot    : docker compose -f docker-compose.full.yml logs -f bot"
echo "  Arrêter     : ./stop-dev.sh"
echo "  Test API    : ./test-api.sh"
