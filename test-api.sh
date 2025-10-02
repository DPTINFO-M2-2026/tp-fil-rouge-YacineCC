#!/bin/bash

# Script de test des API du Bot Discord
# Assurez-vous que l'application tourne sur http://localhost:8080

BASE_URL="http://localhost:8080/api"

echo "🧪 Tests des API du Bot Discord"
echo "==============================="

# Test de santé
echo ""
echo "1. 🏥 Test de santé du service"
curl -s "$BASE_URL/bot/health" | jq '.' || echo "❌ Service non disponible"

# Lister les utilisateurs
echo ""
echo "2. 👥 Liste des utilisateurs"
curl -s "$BASE_URL/users" | jq '.[0:3]' || echo "❌ Erreur lors de la récupération des utilisateurs"

# Lister les guildes
echo ""
echo "3. 🏰 Liste des guildes"
curl -s "$BASE_URL/guilds" | jq '.' || echo "❌ Erreur lors de la récupération des guildes"

# Lister les canaux
echo ""
echo "4. 📺 Liste des canaux"
curl -s "$BASE_URL/channels" | jq '.' || echo "❌ Erreur lors de la récupération des canaux"

# Lister les messages récents
echo ""
echo "5. 💬 Messages récents (limite 5)"
curl -s "$BASE_URL/messages?limit=5" | jq '.' || echo "❌ Erreur lors de la récupération des messages"

# Test d'envoi de message
echo ""
echo "6. 📤 Test d'envoi de message"
curl -s -X POST "$BASE_URL/bot/messages?authorId=1&channelId=1&content=Test message from API" | jq '.' || echo "❌ Erreur lors de l'envoi du message"

# Test de création d'utilisateur
echo ""
echo "7. 👤 Test de création d'utilisateur"
curl -s -X POST "$BASE_URL/users" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "TestAPIUser",
    "discriminator": "9999",
    "email": "testapi@discord.test"
  }' | jq '.' || echo "❌ Erreur lors de la création de l'utilisateur"

# Messages d'un canal spécifique
echo ""
echo "8. 📋 Messages du canal général (ID: 1)"
curl -s "$BASE_URL/bot/messages/channel/1?limit=3" | jq '.' || echo "❌ Erreur lors de la récupération des messages du canal"

# Test de permissions utilisateur
echo ""
echo "9. 🔐 Vérification des permissions utilisateur (ID: 1)"
curl -s "$BASE_URL/bot/users/1/permissions" | jq '.' || echo "❌ Erreur lors de la vérification des permissions"

# Test de récupération des guildes d'un utilisateur
echo ""
echo "10. 🏠 Guildes de l'utilisateur (ID: 1)"
curl -s "$BASE_URL/bot/users/1/guilds" | jq '.' || echo "❌ Erreur lors de la récupération des guildes de l'utilisateur"

echo ""
echo "✅ Tests terminés !"
echo ""
echo "💡 Pour plus de tests, utilisez :"
echo "   - PgAdmin: http://localhost:8081"
echo "   - Dev UI: http://localhost:8080/q/dev/"
echo "   - Documentation API: Vérifiez le README_DISCORD.md"