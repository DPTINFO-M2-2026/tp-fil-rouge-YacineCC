#!/bin/bash

# Script de démarrage pour l'environnement de développement local
# PostgreSQL en Docker + Quarkus en local pour un hot reload optimal

set -e

echo "🚀 Démarrage de l'environnement de développement..."

# Vérifier que Docker est disponible
if ! command -v docker &> /dev/null; then
    echo "❌ Docker n'est pas installé ou n'est pas dans le PATH"
    exit 1
fi

# Vérifier que Java 21 est disponible
if ! command -v java &> /dev/null; then
    echo "❌ Java n'est pas installé ou n'est pas dans le PATH"
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | head -n 1 | awk -F '"' '{print $2}' | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 21 ]; then
    echo "❌ Java 21 ou supérieur est requis (version actuelle: $JAVA_VERSION)"
    exit 1
fi

echo "✅ Docker et Java 21+ détectés"

# Démarrer PostgreSQL en Docker
echo "🐘 Démarrage de PostgreSQL..."
docker compose -f docker-compose.dev.yml up -d db

# Attendre que PostgreSQL soit prêt
echo "⏳ Attente de PostgreSQL..."
until docker compose -f docker-compose.dev.yml exec -T db pg_isready -U discord_bot -d discord_bot_db &> /dev/null; do
    printf "."
    sleep 1
done
echo ""
echo "✅ PostgreSQL est prêt"

# Afficher les infos de connexion
echo ""
echo "📊 Base de données PostgreSQL:"
echo "   URL: jdbc:postgresql://localhost:5432/discord_bot_db"
echo "   User: discord_bot"
echo "   Password: discord_password"
echo ""

# Lancer Quarkus en mode dev
echo "⚡ Démarrage de Quarkus en mode développement..."
echo "   API: http://localhost:8080"
echo "   Dev UI: http://localhost:8080/q/dev/"
echo "   Debug port: 5005"
echo ""
echo "💡 Appuyez sur Ctrl+C pour arrêter"
echo ""

./mvnw quarkus:dev \
    -Dquarkus.datasource.jdbc.url=jdbc:postgresql://localhost:5432/discord_bot_db \
    -Dquarkus.datasource.username=discord_bot \
    -Dquarkus.datasource.password=discord_password
