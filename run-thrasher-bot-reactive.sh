#!/bin/bash

# Script pour lancer le ThrasherBot Reactive (version avec Mono)

echo "🛹 Lancement du ThrasherBot Reactive (Mono)..."
echo "================================"

# Vérifier si le token est configuré
if grep -q '"TOKEN"' src/main/java/fr/univtln/yhaouas846/discord4j/ThrasherBotReactive.java; then
    echo "⚠️  ATTENTION: Token Discord pas configuré !"
    echo "   Modifie la ligne 40 de ThrasherBotReactive.java"
    echo "   Remplace \"TOKEN\" par ton vrai token Discord"
    echo ""
    read -p "Continuer quand même ? (y/N) " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        echo "❌ Annulé"
        exit 1
    fi
fi

# Vérifier que les dossiers existent
if [ ! -d "discord-bot-resources/thrasher-covers" ]; then
    echo "⚠️  Le dossier discord-bot-resources/thrasher-covers n'existe pas !"
    echo "📁 Création du dossier..."
    mkdir -p discord-bot-resources/thrasher-covers
fi

if [ ! -d "discord-bot-resources/thrasher-hellbomb" ]; then
    echo "⚠️  Le dossier discord-bot-resources/thrasher-hellbomb n'existe pas !"
    echo "📁 Création du dossier..."
    mkdir -p discord-bot-resources/thrasher-hellbomb
fi

# Compter les fichiers
IMAGE_COUNT=$(find discord-bot-resources/thrasher-covers -type f \( -iname "*.jpg" -o -iname "*.jpeg" -o -iname "*.png" -o -iname "*.gif" \) 2>/dev/null | wc -l)
VIDEO_COUNT=$(find discord-bot-resources/thrasher-hellbomb -type f \( -iname "*.mp4" -o -iname "*.mov" -o -iname "*.avi" -o -iname "*.webm" \) 2>/dev/null | wc -l)

echo "✅ $IMAGE_COUNT image(s) trouvée(s) dans discord-bot-resources/thrasher-covers"
echo "✅ $VIDEO_COUNT vidéo(s) trouvée(s) dans discord-bot-resources/thrasher-hellbomb"
echo ""

# Compiler le projet
echo "📦 Compilation du projet..."
echo ""
mvn clean compile -q

if [ $? -ne 0 ]; then
    echo "❌ Erreur de compilation !"
    exit 1
fi

echo ""
echo "🚀 Démarrage du ThrasherBot Reactive..."
echo "   Commandes Discord disponibles :"
echo "   - !thrasher          : Poste une cover aléatoire"
echo "   - !thrasher hellbomb : Poste une vidéo Hellbomb aléatoire"
echo "   - !thrasher help     : Affiche l'aide"
echo ""
echo "   Appuie sur Ctrl+C pour arrêter le bot"
echo "================================"
echo ""

# Lancer le bot
mvn exec:java -Dexec.mainClass="fr.univtln.yhaouas846.discord4j.ThrasherBotReactive"
