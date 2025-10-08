#!/bin/bash

# ================================
# Script de Seeding Base de Données
# ================================

set -e

echo "🌱 Discord Bot Data Seeder"
echo "=========================="
echo ""

# Couleurs
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Fonction d'affichage
info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

success() {
    echo -e "${GREEN}✅ $1${NC}"
}

warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

error() {
    echo -e "${RED}❌ $1${NC}"
}

# Vérifier si l'application tourne
info "Vérification que l'application Discord Bot est accessible..."

if curl -s -f http://localhost:8080/api/users > /dev/null 2>&1; then
    success "Application Discord Bot accessible !"
else
    error "L'application n'est pas accessible sur http://localhost:8080"
    warning "Assurez-vous que l'application tourne avec:"
    echo "   docker compose up -d"
    echo "   OU"
    echo "   mvn quarkus:dev"
    exit 1
fi

echo ""
info "Compilation du seeder..."

# Compiler uniquement SimpleDataSeeder
javac -d target/classes \
    -cp target/classes \
    src/main/java/fr/univtln/yhaouas846/projet/SimpleDataSeeder.java

echo ""
info "Lancement du seeding de la base de données..."
echo ""

# Exécuter le seeder simple (sans dépendances Quarkus)
java -cp target/classes fr.univtln.yhaouas846.projet.SimpleDataSeeder

SEED_STATUS=$?

echo ""
if [ $SEED_STATUS -eq 0 ]; then
    success "Seeding terminé avec succès !"
    echo ""
    info "Vous pouvez maintenant:"
    echo "   - Accéder à l'API : http://localhost:8080/api/users"
    echo "   - Voir PgAdmin : http://localhost:8081"
    echo "   - Tester les endpoints : ./test-api.sh"
else
    error "Le seeding a échoué avec le code: $SEED_STATUS"
    exit 1
fi
