#!/bin/bash

# Script pour exécuter tous les tests du Bot Discord

echo "🧪 Exécution des tests du Bot Discord"
echo "====================================="

# Démarrer PostgreSQL pour les tests d'intégration
echo "📦 Démarrage de PostgreSQL pour les tests..."
docker-compose up -d postgres

# Attendre que PostgreSQL soit prêt
echo "⏳ Attente que PostgreSQL soit prêt..."
sleep 5

# Fonction pour afficher les résultats des tests
display_test_results() {
    local test_type=$1
    local exit_code=$2
    
    if [ $exit_code -eq 0 ]; then
        echo "✅ $test_type: RÉUSSI"
    else
        echo "❌ $test_type: ÉCHEC"
    fi
}

# Variables pour suivre les résultats
unit_tests_result=0
integration_tests_result=0
functional_tests_result=0

echo ""
echo "1. 🔬 Exécution des tests unitaires..."
echo "------------------------------------"
./mvnw test -Dtest="*Test" -DfailIfNoTests=false
unit_tests_result=$?

echo ""
echo "2. 🔧 Exécution des tests d'intégration..."
echo "------------------------------------------"
./mvnw test -Dtest="*ResourceTest,*ServiceTest" -DfailIfNoTests=false
integration_tests_result=$?

echo ""
echo "3. 🚀 Exécution des tests fonctionnels..."
echo "-----------------------------------------"
./mvnw test -Dtest="*FunctionalTest,*IntegrationTest" -DfailIfNoTests=false
functional_tests_result=$?

echo ""
echo "📊 RÉSUMÉ DES TESTS"
echo "=================="
display_test_results "Tests unitaires" $unit_tests_result
display_test_results "Tests d'intégration" $integration_tests_result
display_test_results "Tests fonctionnels" $functional_tests_result

# Calcul du résultat global
total_result=$((unit_tests_result + integration_tests_result + functional_tests_result))

echo ""
if [ $total_result -eq 0 ]; then
    echo "🎉 TOUS LES TESTS RÉUSSIS !"
    echo ""
    echo "📋 Couverture de tests :"
    echo "  - Entités JPA avec validation"
    echo "  - Services métier"  
    echo "  - Endpoints REST"
    echo "  - Scénarios fonctionnels complets"
    echo "  - Tests d'intégration avec base de données"
    echo ""
    echo "💡 Pour voir les détails :"
    echo "  - Rapports Surefire : target/surefire-reports/"
    echo "  - Logs de test dans : target/test-classes/"
else
    echo "⚠️  CERTAINS TESTS ONT ÉCHOUÉ"
    echo ""
    echo "🔍 Pour investiguer :"
    echo "  - Vérifiez les logs : target/surefire-reports/"
    echo "  - Lancez un test spécifique : ./mvnw test -Dtest=NomDuTest"
    echo "  - Mode debug : ./mvnw test -Dmaven.surefire.debug"
fi

echo ""
echo "🛠️  Commandes utiles :"
echo "  - Tests spécifiques entités : ./mvnw test -Dtest='*entity*Test'"
echo "  - Tests spécifiques resources : ./mvnw test -Dtest='*resource*Test'"  
echo "  - Tests avec profil dev : ./mvnw test -Pdev"
echo "  - Tests en mode natif : ./mvnw test -Pnative"

# Arrêter PostgreSQL
echo ""
echo "🧹 Nettoyage..."
docker-compose down

exit $total_result