#!/bin/bash

# Script de démarrage pour le Bot Discord

echo "🚀 Démarrage du Bot Discord..."

# Vérifier si Docker est installé
if ! command -v docker &> /dev/null; then
    echo "❌ Docker n'est pas installé. Veuillez l'installer d'abord."
    exit 1
fi

# Vérifier si Docker Compose est installé
if ! command -v docker-compose &> /dev/null; then
    echo "❌ Docker Compose n'est pas installé. Veuillez l'installer d'abord."
    exit 1
fi

# Démarrer PostgreSQL
echo "📦 Démarrage de PostgreSQL..."
docker-compose up -d postgres

# Attendre que PostgreSQL soit prêt
echo "⏳ Attente que PostgreSQL soit prêt..."
sleep 10

# Vérifier la connexion à PostgreSQL
docker-compose exec postgres pg_isready -U discord_bot -d discord_bot_db

if [ $? -eq 0 ]; then
    echo "✅ PostgreSQL est prêt !"
else
    echo "❌ Erreur : PostgreSQL n'est pas prêt. Vérifiez les logs avec : docker-compose logs postgres"
    exit 1
fi

# Compiler et démarrer l'application
echo "🔨 Compilation de l'application..."
./mvnw compile

if [ $? -eq 0 ]; then
    echo "✅ Compilation réussie !"
    echo "🚀 Démarrage de l'application Quarkus..."
    echo ""
    echo "📝 Informations importantes :"
    echo "   - Application : http://localhost:8080"
    echo "   - Dev UI : http://localhost:8080/q/dev/"
    echo "   - PgAdmin : http://localhost:8081 (admin@discord.test / admin)"
    echo "   - API Base : http://localhost:8080/api/"
    echo ""
    echo "🔗 Endpoints principaux :"
    echo "   - GET  /api/users - Liste des utilisateurs"
    echo "   - GET  /api/guilds - Liste des guildes" 
    echo "   - GET  /api/channels - Liste des canaux"
    echo "   - GET  /api/messages - Liste des messages"
    echo "   - POST /api/bot/messages - Envoyer un message"
    echo "   - GET  /api/bot/health - Vérification de santé"
    echo ""
    echo "📋 Pour arrêter :"
    echo "   - Ctrl+C dans ce terminal pour arrêter Quarkus"
    echo "   - docker-compose down pour arrêter PostgreSQL"
    echo ""
    
    # Démarrer l'application
    ./mvnw quarkus:dev
else
    echo "❌ Erreur lors de la compilation !"
    exit 1
fi