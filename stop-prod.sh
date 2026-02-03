#!/bin/bash

# Script d'arrêt de l'environnement de production

echo "🛑 Arrêt de l'environnement de production..."

# Arrêter tous les services
docker compose -f docker-compose.prod.yml down

echo "✅ Environnement de production arrêté"
echo "💾 Les données PostgreSQL sont conservées dans le volume Docker"
echo ""
echo "💡 Pour supprimer aussi les volumes (données): docker compose -f docker-compose.prod.yml down -v"
