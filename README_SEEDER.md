# 🌱 Data Seeder - Documentation

## 📖 Vue d'ensemble

Le **DataSeeder** est un outil Java qui initialise la base de données Discord Bot en utilisant les **endpoints REST** au lieu d'un fichier SQL statique.

### ✅ Avantages par rapport à `import.sql` :

| Critère | `import.sql` | `DataSeeder.java` |
|---------|--------------|-------------------|
| **Validation** | ❌ Aucune | ✅ Validation complète via API |
| **Relations** | ⚠️ Manuelles (IDs hardcodés) | ✅ Gérées automatiquement |
| **Erreurs** | ❌ Difficiles à déboguer | ✅ Stack traces claires |
| **Réutilisable** | ❌ Spécifique à la DB | ✅ Utilise l'API REST |
| **Test** | ❌ Pas testable | ✅ Peut être testé |
| **Flexibilité** | ⚠️ Statique | ✅ Programmable |

---

## 🚀 Utilisation

### **Méthode 1 : Script Automatisé (Recommandé)**

```bash
# Lancer l'application d'abord
docker compose up -d
# OU
mvn quarkus:dev

# Attendre que l'API soit prête (15 secondes)
sleep 15

# Lancer le seeding
./seed-data.sh
```

### **Méthode 2 : Maven Exec**

```bash
mvn exec:java \
  -Dexec.mainClass="fr.univtln.yhaouas846.projet.DataSeeder" \
  -Dexec.classpathScope=compile
```

### **Méthode 3 : JAR Production**

```bash
# Compiler l'application
mvn clean package

# Lancer l'application
java -jar target/quarkus-app/quarkus-run.jar &

# Lancer le seeder
java -jar target/quarkus-app/quarkus-run.jar seed
```

---

## 📊 Données Créées

Le seeder crée automatiquement :

### **1. Utilisateurs (3)**
- **AdminUser** (`admin@discord.test`) - Administrateur
- **TestUser** (`user@discord.test`) - Utilisateur standard
- **BotHelper** (`bot@discord.test`) - Bot automatisé

### **2. Serveur Discord (1)**
- **Test Server** avec 100 membres max
- Propriétaire : AdminUser

### **3. Canaux (3)**
- **#general** (TEXT) - Discussions générales
- **#announcements** (TEXT) - Annonces
- **🔊 voice-general** (VOICE) - Canal vocal

### **4. Rôles (3)**
- **Admin** (rouge) - Toutes permissions
- **Moderator** (vert) - Gestion limitée
- **Member** (bleu) - Permissions basiques

### **5. Messages (4)**
- Messages de bienvenue
- Messages de test
- Annonces

---

## 🔧 Architecture Technique

### **Workflow du Seeder**

```
┌─────────────────┐
│  DataSeeder     │
│  (Main Java)    │
└────────┬────────┘
         │
         │ 1. Vérification santé API
         ▼
┌─────────────────┐
│  HTTP Client    │──────► GET /q/health/ready
└────────┬────────┘
         │
         │ 2. Création entités (ordre dépendances)
         ▼
┌─────────────────┐
│  POST /api/users│──────► Créer AdminUser, TestUser, BotUser
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ POST /api/guilds│──────► Créer Test Server (owner=AdminUser)
└────────┬────────┘
         │
         ▼
┌──────────────────┐
│POST /api/channels│──────► Créer general, announcements, voice
└────────┬─────────┘
         │
         ▼
┌─────────────────┐
│ POST /api/roles │──────► Créer Admin, Moderator, Member
└────────┬────────┘
         │
         ▼
┌──────────────────┐
│POST /api/messages│──────► Créer messages de bienvenue
└──────────────────┘
```

### **Gestion des Dépendances**

Le seeder respecte automatiquement l'ordre de création :

1. **Users** (indépendants)
2. **Guild** (dépend de User owner)
3. **Channels** (dépend de Guild)
4. **Roles** (dépend de Guild)
5. **Messages** (dépend de User author + Channel)

---

## 🛠️ Code Clé

### **Création d'une Entité**

```java
private Long createEntity(String endpoint, Map<String, Object> data) {
    Response response = client.target(BASE_URL + endpoint)
            .request(MediaType.APPLICATION_JSON)
            .post(Entity.json(data));
    
    if (response.getStatus() == 201 || response.getStatus() == 200) {
        Map<String, Object> result = response.readEntity(Map.class);
        return ((Number) result.get("id")).longValue();
    }
    throw new RuntimeException("Échec création " + endpoint);
}
```

### **Exemple : Créer un Utilisateur**

```java
private Long createAdminUser() {
    Map<String, Object> user = new HashMap<>();
    user.put("username", "AdminUser");
    user.put("discriminator", "0001");
    user.put("email", "admin@discord.test");
    user.put("avatarUrl", "https://cdn.discord.com/avatars/1/admin.png");
    user.put("isBot", false);
    
    return createEntity("/users", user);
}
```

---

## 🐛 Troubleshooting

### **Problème : "L'API n'est pas accessible"**

```bash
# Vérifier que l'application tourne
curl http://localhost:8080/q/health/ready

# Si erreur, lancer l'application
docker compose up -d
# OU
mvn quarkus:dev
```

### **Problème : "Échec création /api/users"**

- Vérifier les validations dans `User.java`
- Consulter les logs de l'application
- Vérifier la base de données PostgreSQL

### **Problème : "Duplicate key"**

```bash
# Vider la base avant re-seeding
docker compose down -v
docker compose up -d
sleep 15
./seed-data.sh
```

---

## 📝 Personnalisation

### **Ajouter de Nouvelles Données**

```java
// 1. Créer la méthode dans DataSeeder.java
private Long createPremiumUser() {
    Map<String, Object> user = new HashMap<>();
    user.put("username", "PremiumUser");
    user.put("discriminator", "0999");
    user.put("email", "premium@discord.test");
    user.put("avatarUrl", "https://cdn.discord.com/avatars/999/premium.png");
    user.put("isBot", false);
    
    return createEntity("/users", user);
}

// 2. Appeler dans run()
Long premiumUserId = createPremiumUser();
```

### **Changer l'URL de l'API**

```java
// Dans DataSeeder.java
private static final String BASE_URL = "http://localhost:8080/api";
// Changer en :
private static final String BASE_URL = "http://production.example.com/api";
```

---

## 🎯 Comparaison Avant/Après

### **AVANT (import.sql)**

```sql
INSERT INTO discord_user (id, username, discriminator, email, avatar_url, created_at, is_bot) 
VALUES (1, 'AdminUser', '0001', 'admin@discord.test', '...', NOW(), false);

INSERT INTO guild (id, name, description, icon_url, owner_id, created_at, member_limit) 
VALUES (1, 'Test Server', '...', '...', 1, NOW(), 100);
-- Problème : Pas de validation, relations manuelles
```

### **APRÈS (DataSeeder.java)**

```java
// Création avec validation automatique
Long adminId = createAdminUser();
Long guildId = createGuild(adminId);  // Relations gérées !

// Avantages :
// ✅ Validation Jakarta automatique
// ✅ Relations JPA vérifiées
// ✅ Logs clairs en cas d'erreur
// ✅ Pas d'IDs hardcodés
```

---

## 📚 Ressources

- **Code source** : `src/main/java/fr/univtln/yhaouas846/projet/DataSeeder.java`
- **Script** : `seed-data.sh`
- **API Documentation** : `README_DISCORD.md`

---

## ✅ Checklist Déploiement

- [ ] Application Discord Bot lancée
- [ ] PostgreSQL accessible
- [ ] Base de données vide (ou `docker compose down -v`)
- [ ] Script `seed-data.sh` exécutable
- [ ] Exécuter `./seed-data.sh`
- [ ] Vérifier : `curl http://localhost:8080/api/users`
- [ ] Consulter PgAdmin : http://localhost:8081

**Le seeding via API REST est beaucoup plus robuste et maintenable ! 🚀**
