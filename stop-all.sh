#!/bin/bash

# Script d'arrêt de l'environnement complet Docker

echo "🛑 Arrêt de tous les services Docker..."

docker compose -f docker-compose.full.yml down

echo "✅ Services arrêtés"
echo ""
echo "💡 Pour aussi supprimer les volumes (données):"
echo "   docker compose -f docker-compose.full.yml down -v"
