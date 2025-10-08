#!/bin/bash

# =====================================
# Script de lancement du bot Discord4J
# =====================================

echo "🤖 Lancement du bot Discord de test..."
echo ""

# Vérifier que le token a été remplacé
if grep -q 'String token = "TOKEN"' src/main/java/fr/univtln/yhaouas846/discord4j/MyBot.java; then
    echo "⚠️  ATTENTION : Tu dois remplacer 'TOKEN' par ton vrai token Discord !"
    echo ""
    echo "📝 Pour obtenir un token :"
    echo "   1. Va sur https://discord.com/developers/applications"
    echo "   2. Crée une application et un bot"
    echo "   3. Copie le token"
    echo "   4. Remplace-le dans MyBot.java ligne 26"
    echo "   5. Active MESSAGE CONTENT INTENT dans Bot Settings"
    echo ""
    read -p "Appuie sur Entrée pour continuer quand même (ou Ctrl+C pour annuler)..."
fi

echo ""
echo "Compilation du projet..."
mvn compile -q

if [ $? -eq 0 ]; then
    echo "✅ Compilation réussie"
    echo ""
    echo "🚀 Démarrage du bot..."
    echo "   (Utilise Ctrl+C pour arrêter)"
    echo ""
    mvn exec:java -Dexec.mainClass="fr.univtln.yhaouas846.discord4j.MyBot" -q
else
    echo "❌ Erreur de compilation"
    exit 1
fi
