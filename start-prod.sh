#!/bin/bash

# Script de démarrage pour l'environnement de production
# Tout est dockerisé

set -e

echo "🚀 Démarrage de l'environnement de production..."

# Vérifier que Docker est disponible
if ! command -v docker &> /dev/null; then
    echo "❌ Docker n'est pas installé ou n'est pas dans le PATH"
    exit 1
fi

# Vérifier si .env existe, sinon utiliser .env.example
if [ ! -f .env ]; then
    echo "⚠️  Fichier .env non trouvé, copie de .env.example"
    cp .env.example .env
    echo "📝 Veuillez éditer .env et configurer les mots de passe avant la production !"
fi

# Build de l'image production
echo "🔨 Build de l'image Docker..."
docker build --target runtime -t discord-bot:latest .

# Démarrage des services
echo "🐳 Démarrage des conteneurs..."
docker compose -f docker-compose.prod.yml up -d

# Attendre que l'application soit prête
echo "⏳ Attente du démarrage de l'application..."
sleep 5

# Vérifier le statut
echo ""
echo "📊 Statut des services:"
docker compose -f docker-compose.prod.yml ps

echo ""
echo "✅ Environnement de production démarré"
echo ""
echo "🌐 Services disponibles:"
echo "   API: http://localhost:8080"
echo "   PostgreSQL: localhost:5432"
echo ""
echo "💡 Pour voir les logs: docker compose -f docker-compose.prod.yml logs -f"
echo "💡 Pour arrêter: ./stop-prod.sh"
echo "💡 Pour PgAdmin: docker compose -f docker-compose.prod.yml --profile tools up -d pgadmin"
