#!/bin/bash

# Arrête toute la stack lancée par start-dev.sh

echo "🛑 Arrêt de tous les services..."

docker compose -f docker-compose.full.yml down

echo "✅ Stack arrêtée"
echo "💾 Les données (PostgreSQL, modèles Ollama) sont conservées dans les volumes"
echo ""
echo "💡 Pour tout supprimer (données incluses) :"
echo "   docker compose -f docker-compose.full.yml down -v"
