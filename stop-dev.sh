#!/bin/bash

# Script d'arrêt de l'environnement de développement

echo "🛑 Arrêt de l'environnement de développement..."

# Arrêter PostgreSQL
docker compose -f docker-compose.dev.yml down

echo "✅ Environnement de développement arrêté"
echo "💾 Les données PostgreSQL sont conservées dans le volume Docker"
