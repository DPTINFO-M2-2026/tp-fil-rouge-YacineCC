#!/bin/bash

# ================================
# Script de Build Docker Multi-Étapes
# ================================
# Ce script automatise la construction de l'image Docker
# avec différentes options et configurations

set -e  # Arrêt en cas d'erreur

# ================================
# CONFIGURATION
# ================================

# Nom de l'image
IMAGE_NAME="discord-bot"
TAG="latest"
BUILD_ARGS=""

# Couleurs pour l'affichage
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# ================================
# FONCTIONS UTILITAIRES
# ================================

print_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

print_separator() {
    echo "================================"
}

show_help() {
    cat << EOF
Script de Build Docker Multi-Étapes pour Discord Bot

USAGE:
    $0 [OPTIONS]

OPTIONS:
    -t, --tag TAG          Tag de l'image (défaut: latest)
    -e, --env ENV          Environnement (dev|prod|test)
    -c, --clean           Nettoyage avant build
    -p, --push            Push vers le registry après build
    -n, --no-cache        Build sans utiliser le cache
    -s, --stage STAGE     Build jusqu'à une étape spécifique (builder|runtime)
    -v, --verbose         Mode verbeux
    -h, --help            Afficher cette aide

EXEMPLES:
    $0                              # Build basique
    $0 -t v1.0.0 -e prod           # Build version 1.0.0 pour production
    $0 -c -n --stage builder       # Build clean sans cache jusqu'à l'étape builder
    $0 -t dev -e dev -v            # Build version dev avec logs verbeux

ÉTAPES DISPONIBLES:
    builder               Étape de compilation (Maven + JDK)
    runtime               Étape d'exécution (JRE optimisé)

ENVIRONNEMENTS:
    dev                   Développement (debug activé)
    test                  Tests (profil test)
    prod                  Production (optimisé)
EOF
}

# ================================
# ANALYSE DES ARGUMENTS
# ================================

ENVIRONMENT=""
CLEAN=false
PUSH=false
NO_CACHE=false
STAGE=""
VERBOSE=false

while [[ $# -gt 0 ]]; do
    case $1 in
        -t|--tag)
            TAG="$2"
            shift 2
            ;;
        -e|--env)
            ENVIRONMENT="$2"
            shift 2
            ;;
        -c|--clean)
            CLEAN=true
            shift
            ;;
        -p|--push)
            PUSH=true
            shift
            ;;
        -n|--no-cache)
            NO_CACHE=true
            shift
            ;;
        -s|--stage)
            STAGE="$2"
            shift 2
            ;;
        -v|--verbose)
            VERBOSE=true
            shift
            ;;
        -h|--help)
            show_help
            exit 0
            ;;
        *)
            print_error "Option inconnue: $1"
            show_help
            exit 1
            ;;
    esac
done

# ================================
# CONFIGURATION DE L'ENVIRONNEMENT
# ================================

case $ENVIRONMENT in
    dev)
        BUILD_ARGS="$BUILD_ARGS --build-arg JAVA_OPTS_APPEND='-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 -Dquarkus.profile=dev'"
        TAG="${TAG}-dev"
        ;;
    test)
        BUILD_ARGS="$BUILD_ARGS --build-arg JAVA_OPTS_APPEND='-Dquarkus.profile=test'"
        TAG="${TAG}-test"
        ;;
    prod)
        BUILD_ARGS="$BUILD_ARGS --build-arg JAVA_OPTS_APPEND='-Xms256m -Xmx512m -Dquarkus.profile=prod'"
        TAG="${TAG}-prod"
        ;;
esac

# Configuration du cache
if [ "$NO_CACHE" = true ]; then
    BUILD_ARGS="$BUILD_ARGS --no-cache"
fi

# Configuration du stage
if [ -n "$STAGE" ]; then
    BUILD_ARGS="$BUILD_ARGS --target $STAGE"
    TAG="${TAG}-${STAGE}"
fi

# Configuration verbose
if [ "$VERBOSE" = true ]; then
    BUILD_ARGS="$BUILD_ARGS --progress=plain"
fi

# ================================
# FONCTIONS DE BUILD
# ================================

cleanup() {
    print_info "Nettoyage des artefacts précédents..."
    
    # Nettoyage Maven
    if [ -f "mvnw" ]; then
        ./mvnw clean -q
    fi
    
    # Suppression des images obsolètes
    docker image prune -f > /dev/null 2>&1 || true
    
    print_success "Nettoyage terminé"
}

pre_build_checks() {
    print_info "Vérifications pré-build..."
    
    # Vérification de Docker
    if ! command -v docker &> /dev/null; then
        print_error "Docker n'est pas installé ou non accessible"
        exit 1
    fi
    
    # Vérification des fichiers essentiels
    if [ ! -f "Dockerfile" ]; then
        print_error "Dockerfile non trouvé"
        exit 1
    fi
    
    if [ ! -f "pom.xml" ]; then
        print_error "pom.xml non trouvé"
        exit 1
    fi
    
    print_success "Vérifications OK"
}

build_image() {
    local full_image_name="${IMAGE_NAME}:${TAG}"
    
    print_separator
    print_info "Construction de l'image Docker"
    print_info "Image: $full_image_name"
    print_info "Arguments: $BUILD_ARGS"
    print_separator
    
    # Commande de build
    local build_cmd="docker build -t $full_image_name $BUILD_ARGS ."
    
    if [ "$VERBOSE" = true ]; then
        print_info "Commande: $build_cmd"
    fi
    
    # Exécution du build
    eval $build_cmd
    
    print_success "Image construite: $full_image_name"
    
    # Affichage des informations de l'image
    print_info "Informations de l'image:"
    docker images | grep "$IMAGE_NAME" | grep "$TAG"
}

push_image() {
    local full_image_name="${IMAGE_NAME}:${TAG}"
    
    print_info "Push de l'image vers le registry..."
    docker push "$full_image_name"
    print_success "Image pushée: $full_image_name"
}

show_usage_examples() {
    print_separator
    print_info "Exemples d'utilisation de l'image:"
    
    local full_image_name="${IMAGE_NAME}:${TAG}"
    
    echo -e "${YELLOW}# Exécution basique:${NC}"
    echo "docker run --rm -p 8080:8080 $full_image_name"
    echo ""
    
    if [[ "$ENVIRONMENT" == "dev" ]]; then
        echo -e "${YELLOW}# Mode développement avec debug:${NC}"
        echo "docker run --rm -p 8080:8080 -p 5005:5005 $full_image_name"
    fi
    
    echo -e "${YELLOW}# Avec volumes persistants:${NC}"
    echo "docker run -d --name discord-bot \\"
    echo "  -p 8080:8080 \\"
    echo "  -v discord-bot-logs:/app/logs \\"
    echo "  -v discord-bot-config:/app/config \\"
    echo "  --restart unless-stopped \\"
    echo "  $full_image_name"
    echo ""
    
    echo -e "${YELLOW}# Health check:${NC}"
    echo "curl http://localhost:8080/q/health"
    
    print_separator
}

# ================================
# EXÉCUTION PRINCIPALE
# ================================

main() {
    print_separator
    print_info "Début du build Docker Multi-Étapes"
    print_info "Image: ${IMAGE_NAME}:${TAG}"
    print_info "Environnement: ${ENVIRONMENT:-"défaut"}"
    print_separator
    
    # Vérifications
    pre_build_checks
    
    # Nettoyage si demandé
    if [ "$CLEAN" = true ]; then
        cleanup
    fi
    
    # Construction de l'image
    build_image
    
    # Push si demandé
    if [ "$PUSH" = true ]; then
        push_image
    fi
    
    # Affichage des exemples d'utilisation
    show_usage_examples
    
    print_success "Build terminé avec succès !"
}

# Point d'entrée
main "$@"