#!/bin/bash

# Script de lancement du ThrasherBot
# Lance le bot Discord qui poste des covers aléatoires de Thrasher Magazine

echo "🛹 Lancement du ThrasherBot..."
echo "================================"

# Vérifier que le fichier ThrasherBot.java existe
THRASHER_BOT="src/main/java/fr/univtln/yhaouas846/discord4j/ThrasherBot.java"
if [ ! -f "$THRASHER_BOT" ]; then
    echo "❌ Erreur: ThrasherBot.java introuvable"
    echo "   Attendu : $THRASHER_BOT"
    exit 1
fi

# Vérifier que le token Discord a été configuré
if grep -q '"TOKEN"' "$THRASHER_BOT"; then
    echo "⚠️  ATTENTION: Token Discord pas configuré !"
    echo "   Modifie la ligne 38 de ThrasherBot.java"
    echo "   Remplace \"TOKEN\" par ton vrai token Discord"
    echo ""
    read -p "Continuer quand même ? (y/N) " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        exit 1
    fi
fi

# Vérifier que le dossier des covers existe
COVERS_DIR="discord-bot-resources/thrasher-covers"
if [ ! -d "$COVERS_DIR" ]; then
    echo "❌ Erreur: Dossier des covers introuvable"
    echo "   Attendu : $COVERS_DIR"
    exit 1
fi

# Vérifier qu'il y a des images dans le dossier
IMAGE_COUNT=$(find "$COVERS_DIR" -type f \( -iname "*.jpg" -o -iname "*.jpeg" -o -iname "*.png" -o -iname "*.gif" \) | wc -l)
if [ "$IMAGE_COUNT" -eq 0 ]; then
    echo "⚠️  ATTENTION: Aucune image trouvée dans $COVERS_DIR"
    echo "   Le bot ne pourra pas poster de covers !"
    echo "   Ajoute des images (.jpg, .png, .gif) dans ce dossier"
else
    echo "✅ $IMAGE_COUNT image(s) trouvée(s) dans $COVERS_DIR"
fi

# Vérifier que le dossier hellbomb existe
HELLBOMB_DIR="discord-bot-resources/thrasher-hellbomb"
if [ ! -d "$HELLBOMB_DIR" ]; then
    echo "⚠️  Warning: Dossier hellbomb introuvable ($HELLBOMB_DIR)"
    echo "   Crée-le pour utiliser la commande !thrasher hellbomb"
else
    # Vérifier qu'il y a des vidéos dans le dossier
    VIDEO_COUNT=$(find "$HELLBOMB_DIR" -type f \( -iname "*.mp4" -o -iname "*.mov" -o -iname "*.avi" -o -iname "*.webm" \) | wc -l)
    if [ "$VIDEO_COUNT" -eq 0 ]; then
        echo "⚠️  ATTENTION: Aucune vidéo trouvée dans $HELLBOMB_DIR"
        echo "   Ajoute des vidéos (.mp4, .mov, .avi, .webm) pour !thrasher hellbomb"
    else
        echo "✅ $VIDEO_COUNT vidéo(s) trouvée(s) dans $HELLBOMB_DIR"
    fi
fi

# Ne quitter que si ni images ni vidéos
if [ "$IMAGE_COUNT" -eq 0 ] && [ "$VIDEO_COUNT" -eq 0 ]; then
    echo ""
    echo "❌ Aucun contenu disponible (ni images ni vidéos) !"
    read -p "Continuer quand même ? (y/N) " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        exit 1
    fi
fi

echo ""
echo "📦 Compilation du projet..."
mvn compile

if [ $? -ne 0 ]; then
    echo "❌ Erreur lors de la compilation"
    exit 1
fi

echo ""
echo "🚀 Démarrage du ThrasherBot..."
echo "   Commandes Discord disponibles :"
if [ "$IMAGE_COUNT" -gt 0 ]; then
    echo "   - !thrasher          : Poste une cover aléatoire"
fi
if [ "$VIDEO_COUNT" -gt 0 ]; then
    echo "   - !thrasher hellbomb : Poste une vidéo Hellbomb aléatoire"
fi
echo "   - !thrasher help     : Affiche l'aide"
echo ""
echo "   Appuie sur Ctrl+C pour arrêter le bot"
echo "================================"
echo ""

# Lancer le bot
mvn exec:java -Dexec.mainClass="fr.univtln.yhaouas846.discord4j.ThrasherBot"
