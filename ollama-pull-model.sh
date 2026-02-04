#!/bin/bash

# Script pour télécharger un modèle Ollama dans le conteneur Docker

MODEL="${1:-llama3:8b}"

echo "📥 Téléchargement du modèle $MODEL dans Ollama (Docker)..."
echo "   Cela peut prendre plusieurs minutes selon la taille du modèle..."
echo ""

docker exec -it discord-bot-ollama ollama pull "$MODEL"

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ Modèle $MODEL téléchargé avec succès !"
    echo ""
    echo "📋 Liste des modèles disponibles:"
    docker exec discord-bot-ollama ollama list
else
    echo "❌ Erreur lors du téléchargement du modèle"
    exit 1
fi
