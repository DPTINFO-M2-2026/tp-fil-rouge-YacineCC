#!/bin/bash

# ================================
# Script de Gestion Docker Compose
# ================================
# Gestion simplifiée de la stack Discord Bot

set -e

# ================================
# CONFIGURATION
# ================================

COMPOSE_FILE="docker-compose.yml"
PROJECT_NAME="discord-bot"

# Couleurs
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

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

show_help() {
    cat << EOF
Script de Gestion Docker Compose - Discord Bot

USAGE:
    $0 <COMMAND> [OPTIONS]

COMMANDS:
    up              Démarrer la stack complète
    down            Arrêter la stack
    restart         Redémarrer la stack
    build           Construire les images
    rebuild         Reconstruire les images sans cache
    logs            Afficher les logs
    status          Afficher le statut des services
    clean           Nettoyer (containers, volumes, images)
    db              Commandes base de données
    monitor         Démarrer avec monitoring
    dev             Mode développement
    prod            Mode production
    backup          Sauvegarder la base de données
    restore         Restaurer la base de données

OPTIONS:
    -f, --follow    Suivre les logs en temps réel
    -s, --service   Service spécifique
    -v, --verbose   Mode verbeux
    -h, --help      Afficher cette aide

EXEMPLES:
    $0 up                    # Démarrer la stack
    $0 logs -f               # Suivre les logs
    $0 logs -s discord-bot   # Logs du service app
    $0 monitor               # Démarrer avec Prometheus/Grafana
    $0 db backup             # Sauvegarder la BDD
    $0 clean --all           # Nettoyage complet

SERVICES DISPONIBLES:
    discord-bot     Application principale
    postgres        Base de données PostgreSQL
    pgadmin         Interface d'administration BDD
    prometheus      Monitoring des métriques
    grafana         Dashboards et visualisation
EOF
}

# ================================
# FONCTIONS PRINCIPALES
# ================================

start_stack() {
    local profile=""
    if [ "$1" = "monitoring" ]; then
        profile="--profile monitoring"
    fi
    
    print_info "Démarrage de la stack Discord Bot..."
    docker-compose -f $COMPOSE_FILE -p $PROJECT_NAME up -d $profile
    
    print_info "Attente du démarrage des services..."
    sleep 10
    
    show_status
    show_urls
}

stop_stack() {
    print_info "Arrêt de la stack Discord Bot..."
    docker-compose -f $COMPOSE_FILE -p $PROJECT_NAME down
    print_success "Stack arrêtée"
}

restart_stack() {
    print_info "Redémarrage de la stack..."
    stop_stack
    sleep 5
    start_stack
}

build_images() {
    local no_cache=""
    if [ "$1" = "rebuild" ]; then
        no_cache="--no-cache"
    fi
    
    print_info "Construction des images Docker..."
    docker-compose -f $COMPOSE_FILE -p $PROJECT_NAME build $no_cache
    print_success "Images construites"
}

show_logs() {
    local follow=""
    local service=""
    
    while [[ $# -gt 0 ]]; do
        case $1 in
            -f|--follow)
                follow="-f"
                shift
                ;;
            -s|--service)
                service="$2"
                shift 2
                ;;
            *)
                break
                ;;
        esac
    done
    
    print_info "Affichage des logs..."
    docker-compose -f $COMPOSE_FILE -p $PROJECT_NAME logs $follow $service
}

show_status() {
    print_info "Statut des services:"
    docker-compose -f $COMPOSE_FILE -p $PROJECT_NAME ps
    
    print_info "État de santé:"
    docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}" --filter "label=com.docker.compose.project=$PROJECT_NAME"
}

show_urls() {
    print_success "Services disponibles:"
    echo -e "${GREEN}• Application Discord Bot:${NC} http://localhost:8080"
    echo -e "${GREEN}• API Health Check:${NC} http://localhost:8080/q/health"
    echo -e "${GREEN}• API Documentation:${NC} http://localhost:8080/q/swagger-ui"
    echo -e "${GREEN}• PgAdmin:${NC} http://localhost:8081"
    echo -e "${GREEN}• Prometheus:${NC} http://localhost:9090 (si monitoring activé)"
    echo -e "${GREEN}• Grafana:${NC} http://localhost:3000 (si monitoring activé)"
    echo ""
    echo -e "${YELLOW}Credentials par défaut:${NC}"
    echo -e "• PgAdmin: admin@discord-bot.local / admin_secure_2024"
    echo -e "• Grafana: admin / grafana_admin_2024"
}

clean_stack() {
    local clean_all=false
    
    if [ "$1" = "--all" ]; then
        clean_all=true
    fi
    
    print_warning "Nettoyage de la stack..."
    
    # Arrêt des services
    docker-compose -f $COMPOSE_FILE -p $PROJECT_NAME down
    
    if [ "$clean_all" = true ]; then
        print_warning "Nettoyage complet (volumes et images)..."
        
        # Suppression des volumes
        docker-compose -f $COMPOSE_FILE -p $PROJECT_NAME down -v
        
        # Suppression des images
        docker images | grep "$PROJECT_NAME" | awk '{print $3}' | xargs -r docker rmi -f
        
        # Nettoyage Docker général
        docker system prune -f
    fi
    
    print_success "Nettoyage terminé"
}

# ================================
# GESTION BASE DE DONNÉES
# ================================

manage_database() {
    case $1 in
        backup)
            backup_database
            ;;
        restore)
            restore_database "$2"
            ;;
        reset)
            reset_database
            ;;
        shell)
            database_shell
            ;;
        *)
            echo "Commandes DB disponibles: backup, restore <file>, reset, shell"
            ;;
    esac
}

backup_database() {
    local backup_file="backup_discord_bot_$(date +%Y%m%d_%H%M%S).sql"
    
    print_info "Sauvegarde de la base de données..."
    
    docker-compose -f $COMPOSE_FILE -p $PROJECT_NAME exec postgres pg_dump \
        -U discord_bot -d discord_bot_db > "$backup_file"
    
    print_success "Sauvegarde créée: $backup_file"
}

restore_database() {
    local backup_file="$1"
    
    if [ -z "$backup_file" ]; then
        print_error "Fichier de sauvegarde requis"
        return 1
    fi
    
    if [ ! -f "$backup_file" ]; then
        print_error "Fichier non trouvé: $backup_file"
        return 1
    fi
    
    print_warning "Restauration de la base de données..."
    print_warning "ATTENTION: Cela effacera toutes les données existantes!"
    
    read -p "Continuer? (y/N): " -n 1 -r
    echo
    
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        docker-compose -f $COMPOSE_FILE -p $PROJECT_NAME exec -T postgres psql \
            -U discord_bot -d discord_bot_db < "$backup_file"
        print_success "Base de données restaurée"
    else
        print_info "Restauration annulée"
    fi
}

database_shell() {
    print_info "Connexion à la base de données..."
    docker-compose -f $COMPOSE_FILE -p $PROJECT_NAME exec postgres psql \
        -U discord_bot -d discord_bot_db
}

# ================================
# MODES SPÉCIFIQUES
# ================================

dev_mode() {
    export QUARKUS_PROFILE=dev
    export JAVA_ENABLE_DEBUG=true
    
    print_info "Démarrage en mode développement..."
    print_warning "Debug JVM activé sur le port 5005"
    
    start_stack
}

prod_mode() {
    export QUARKUS_PROFILE=prod
    export JAVA_ENABLE_DEBUG=false
    
    print_info "Démarrage en mode production..."
    start_stack
}

monitor_mode() {
    print_info "Démarrage avec monitoring (Prometheus + Grafana)..."
    start_stack monitoring
}

# ================================
# ANALYSE DES ARGUMENTS
# ================================

if [ $# -eq 0 ]; then
    show_help
    exit 1
fi

COMMAND="$1"
shift

case $COMMAND in
    up)
        start_stack "$@"
        ;;
    down)
        stop_stack
        ;;
    restart)
        restart_stack
        ;;
    build)
        build_images
        ;;
    rebuild)
        build_images rebuild
        ;;
    logs)
        show_logs "$@"
        ;;
    status|ps)
        show_status
        ;;
    clean)
        clean_stack "$@"
        ;;
    db)
        manage_database "$@"
        ;;
    monitor)
        monitor_mode
        ;;
    dev)
        dev_mode
        ;;
    prod)
        prod_mode
        ;;
    urls)
        show_urls
        ;;
    -h|--help)
        show_help
        ;;
    *)
        print_error "Commande inconnue: $COMMAND"
        show_help
        exit 1
        ;;
esac