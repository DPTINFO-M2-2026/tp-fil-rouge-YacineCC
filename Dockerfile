# ================================
# Dockerfile Multi-Étapes pour Application Discord Bot
# ================================
# Ce Dockerfile utilise une approche multi-étapes pour :
# 1. Builder : Compilation et construction de l'application
# 2. Runtime : Exécution optimisée de l'application
#
# Usage:
# docker build -t discord-bot:latest .
# docker run -p 8080:8080 discord-bot:latest

# ================================
# ÉTAPE 1 : BUILD
# ================================
# Image de base pour la compilation avec Maven et JDK 21
FROM registry.access.redhat.com/ubi8/openjdk-21:1.20 AS builder

# Métadonnées
LABEL stage="builder" \
      description="Étape de compilation de l'application Discord Bot" \
      maintainer="yhaouas846@univtln.fr"

# Variables d'environnement pour Maven
ENV MAVEN_VERSION=3.9.8
ENV MAVEN_HOME=/opt/maven
ENV PATH=$MAVEN_HOME/bin:$PATH

# Installation de Maven
USER root
RUN microdnf install -y curl tar gzip && \
    curl -fsSL https://archive.apache.org/dist/maven/maven-3/$MAVEN_VERSION/binaries/apache-maven-$MAVEN_VERSION-bin.tar.gz | \
    tar -xzC /opt && \
    mv /opt/apache-maven-$MAVEN_VERSION /opt/maven && \
    microdnf clean all

# Création du répertoire de travail
WORKDIR /app

# Copie des fichiers de configuration Maven en premier (pour le cache Docker)
COPY pom.xml ./
COPY mvnw ./
COPY mvnw.cmd ./
COPY .mvn ./.mvn

# Pré-téléchargement des dépendances (optimisation du cache Docker)
RUN chmod +x ./mvnw && \
    ./mvnw dependency:go-offline -B

# Copie du code source
COPY src ./src

# Compilation et packaging de l'application
RUN ./mvnw clean package -DskipTests -B && \
    echo "Build terminé avec succès" && \
    ls -la target/

# ================================
# ÉTAPE 2 : RUNTIME
# ================================
# Image optimisée pour l'exécution avec JRE 21
FROM registry.access.redhat.com/ubi8/openjdk-21-runtime:1.20 AS runtime

# Métadonnées
LABEL stage="runtime" \
      description="Environnement d'exécution pour l'application Discord Bot" \
      version="1.0.0" \
      maintainer="yhaouas846@univtln.fr" \
      application="Discord Bot" \
      framework="Quarkus 3.28.1"

# Variables d'environnement
ENV LANGUAGE='en_US:en'
ENV JAVA_OPTS_APPEND="-Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager"
ENV JAVA_APP_JAR="/deployments/quarkus-run.jar"

# Configuration de l'utilisateur non-root pour la sécurité
USER root

# Installation des outils nécessaires et création de l'utilisateur
RUN microdnf install -y shadow-utils && \
    groupadd -r discord-bot --gid=1001 && \
    useradd -r -g discord-bot --uid=1001 --home-dir=/app --shell=/sbin/nologin discord-bot && \
    microdnf clean all

# Création des répertoires avec permissions appropriées
RUN mkdir -p /deployments /app/logs /app/config && \
    chown -R discord-bot:discord-bot /deployments /app

# Copie des artefacts depuis l'étape de build
COPY --from=builder --chown=discord-bot:discord-bot /app/target/quarkus-app/lib/ /deployments/lib/
COPY --from=builder --chown=discord-bot:discord-bot /app/target/quarkus-app/*.jar /deployments/
COPY --from=builder --chown=discord-bot:discord-bot /app/target/quarkus-app/app/ /deployments/app/
COPY --from=builder --chown=discord-bot:discord-bot /app/target/quarkus-app/quarkus/ /deployments/quarkus/

# Vérification des fichiers copiés
RUN echo "Contenu de /deployments:" && \
    ls -la /deployments/ && \
    echo "Taille des fichiers:" && \
    du -sh /deployments/*

# Configuration des volumes pour la persistance
VOLUME ["/app/logs", "/app/config"]

# Ports exposés
EXPOSE 8080 8443 5005

# Configuration du healthcheck
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/q/health/ready || exit 1

# Basculement vers l'utilisateur non-root
USER discord-bot

# Point d'entrée avec script de démarrage optimisé
ENTRYPOINT ["java", \
           "-Djava.util.logging.manager=org.jboss.logmanager.LogManager", \
           "-Dquarkus.http.host=0.0.0.0", \
           "-Dquarkus.http.port=8080", \
           "-jar", "/deployments/quarkus-run.jar"]

# ================================
# MÉTADONNÉES SUPPLÉMENTAIRES
# ================================
# Instructions d'utilisation

# Construction de l'image :
# docker build -t discord-bot:latest .
# docker build -t discord-bot:dev --target builder .  # Pour debug

# Exécution en mode développement :
# docker run -it --rm \
#   -p 8080:8080 \
#   -v $(pwd)/logs:/app/logs \
#   -e QUARKUS_PROFILE=dev \
#   discord-bot:latest

# Exécution en mode production :
# docker run -d \
#   --name discord-bot-prod \
#   -p 8080:8080 \
#   -p 8443:8443 \
#   -v discord-bot-logs:/app/logs \
#   -v discord-bot-config:/app/config \
#   -e QUARKUS_PROFILE=prod \
#   -e JAVA_OPTS_APPEND="-Xms256m -Xmx512m" \
#   --restart unless-stopped \
#   discord-bot:latest

# Debug avec JVM :
# docker run -it --rm \
#   -p 8080:8080 \
#   -p 5005:5005 \
#   -e JAVA_OPTS_APPEND="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005" \
#   discord-bot:latest