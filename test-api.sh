#!/bin/bash

# =============================================================================
# test-api.sh — Tests fonctionnels de l'API REST (CRUD complet)
# Lance des requêtes HTTP et vérifie les codes de retour.
# Prérequis : l'application doit tourner sur http://localhost:8080
# =============================================================================

set -euo pipefail

BASE_URL="http://localhost:8080/api"
PASS=0
FAIL=0

# --- Couleurs ----------------------------------------------------------------
GREEN='\033[0;32m'
RED='\033[0;31m'
CYAN='\033[0;36m'
BOLD='\033[1m'
NC='\033[0m'

# --- Helpers -----------------------------------------------------------------

# assert_status <label> <expected_code> <curl_args...>
assert_status() {
    local label="$1"
    local expected="$2"
    shift 2

    local response
    response=$(curl -s -w "\n%{http_code}" "$@") || true
    local code
    code=$(echo "$response" | tail -n1)
    local body
    body=$(echo "$response" | sed '$d')

    if [ "$code" = "$expected" ]; then
        echo -e "  ${GREEN}✔${NC} $label  ${CYAN}(HTTP $code)${NC}"
        PASS=$((PASS + 1))
    else
        echo -e "  ${RED}✘ $label  (attendu $expected, reçu $code)${NC}"
        [ -n "$body" ] && echo "    $body" | head -3
        FAIL=$((FAIL + 1))
    fi

    # expose body for callers that need to extract IDs
    LAST_BODY="$body"
}

# extract_id — extrait "id" du dernier body JSON
extract_id() {
    echo "$LAST_BODY" | grep -o '"id":[0-9]*' | head -1 | cut -d: -f2
}

# --- Vérification que l'API est joignable ------------------------------------

echo ""
echo -e "${BOLD}🧪 Tests fonctionnels de l'API${NC}"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

if ! curl -sf "$BASE_URL/users" > /dev/null 2>&1; then
    echo -e "${RED}❌ L'API n'est pas accessible sur $BASE_URL${NC}"
    echo "   Lance d'abord ./start-app.sh"
    exit 1
fi
echo -e "  ${GREEN}✔${NC} API accessible"
echo ""

# =============================================================================
# 1. HEALTH
# =============================================================================
echo -e "${BOLD}1 · Health${NC}"
assert_status "GET  /api/bot/health" 200 "$BASE_URL/bot/health"
echo ""

# =============================================================================
# 2. USERS — CRUD complet
# =============================================================================
echo -e "${BOLD}2 · Users (CRUD)${NC}"

assert_status "GET  /api/users (liste)" 200 "$BASE_URL/users"

assert_status "POST /api/users (créer)" 201 \
    -X POST "$BASE_URL/users" \
    -H "Content-Type: application/json" \
    -d '{"username":"testapi_user","discriminator":"0001","email":"testapi@test.local","isBot":false}'
USER_ID=$(extract_id)

if [ -n "$USER_ID" ]; then
    assert_status "GET  /api/users/$USER_ID (lire)" 200 "$BASE_URL/users/$USER_ID"

    assert_status "PUT  /api/users/$USER_ID (modifier)" 200 \
        -X PUT "$BASE_URL/users/$USER_ID" \
        -H "Content-Type: application/json" \
        -d '{"username":"testapi_updated","discriminator":"0001","email":"testapi@test.local","isBot":false}'

    assert_status "GET  /api/users/username/testapi_updated" 200 "$BASE_URL/users/username/testapi_updated"

    assert_status "DELETE /api/users/$USER_ID (supprimer)" 204 \
        -X DELETE "$BASE_URL/users/$USER_ID"

    assert_status "GET  /api/users/$USER_ID (vérifié supprimé)" 404 "$BASE_URL/users/$USER_ID"
else
    echo -e "  ${RED}✘ Impossible d'extraire l'ID utilisateur, tests CRUD partiels ignorés${NC}"
    FAIL=$((FAIL + 1))
fi

assert_status "GET  /api/users/bots" 200 "$BASE_URL/users/bots"
echo ""

# =============================================================================
# 3. GUILDS — CRUD complet
# =============================================================================
echo -e "${BOLD}3 · Guilds (CRUD)${NC}"

# Créer un owner temporaire pour la guild
assert_status "POST /api/users (owner temporaire)" 201 \
    -X POST "$BASE_URL/users" \
    -H "Content-Type: application/json" \
    -d '{"username":"guild_owner","discriminator":"0002","email":"owner@test.local","isBot":false}'
OWNER_ID=$(extract_id)

assert_status "GET  /api/guilds (liste)" 200 "$BASE_URL/guilds"

if [ -n "$OWNER_ID" ]; then
    assert_status "POST /api/guilds (créer)" 201 \
        -X POST "$BASE_URL/guilds" \
        -H "Content-Type: application/json" \
        -d "{\"name\":\"Test Guild\",\"description\":\"Guild de test API\",\"owner\":{\"id\":$OWNER_ID}}"
    GUILD_ID=$(extract_id)

    if [ -n "$GUILD_ID" ]; then
        assert_status "GET  /api/guilds/$GUILD_ID (lire)" 200 "$BASE_URL/guilds/$GUILD_ID"

        assert_status "PUT  /api/guilds/$GUILD_ID (modifier)" 200 \
            -X PUT "$BASE_URL/guilds/$GUILD_ID" \
            -H "Content-Type: application/json" \
            -d "{\"name\":\"Test Guild Updated\",\"description\":\"Mise à jour\",\"owner\":{\"id\":$OWNER_ID}}"

        assert_status "DELETE /api/guilds/$GUILD_ID (supprimer)" 204 \
            -X DELETE "$BASE_URL/guilds/$GUILD_ID"
    fi
fi
echo ""

# =============================================================================
# 4. CHANNELS — CRUD complet
# =============================================================================
echo -e "${BOLD}4 · Channels (CRUD)${NC}"

# Créer une guild pour le channel
if [ -n "$OWNER_ID" ]; then
    assert_status "POST /api/guilds (pour channel)" 201 \
        -X POST "$BASE_URL/guilds" \
        -H "Content-Type: application/json" \
        -d "{\"name\":\"Channel Test Guild\",\"owner\":{\"id\":$OWNER_ID}}"
    CH_GUILD_ID=$(extract_id)

    assert_status "GET  /api/channels (liste)" 200 "$BASE_URL/channels"

    if [ -n "$CH_GUILD_ID" ]; then
        assert_status "POST /api/channels (créer)" 201 \
            -X POST "$BASE_URL/channels" \
            -H "Content-Type: application/json" \
            -d "{\"name\":\"test-channel\",\"type\":\"TEXT\",\"guild\":{\"id\":$CH_GUILD_ID}}"
        CHANNEL_ID=$(extract_id)

        if [ -n "$CHANNEL_ID" ]; then
            assert_status "GET  /api/channels/$CHANNEL_ID (lire)" 200 "$BASE_URL/channels/$CHANNEL_ID"

            assert_status "PUT  /api/channels/$CHANNEL_ID (modifier)" 200 \
                -X PUT "$BASE_URL/channels/$CHANNEL_ID" \
                -H "Content-Type: application/json" \
                -d "{\"name\":\"test-channel-updated\",\"type\":\"TEXT\",\"guild\":{\"id\":$CH_GUILD_ID}}"

            assert_status "GET  /api/channels/guild/$CH_GUILD_ID" 200 "$BASE_URL/channels/guild/$CH_GUILD_ID"
            assert_status "GET  /api/channels/type/TEXT" 200 "$BASE_URL/channels/type/TEXT"

            assert_status "DELETE /api/channels/$CHANNEL_ID (supprimer)" 204 \
                -X DELETE "$BASE_URL/channels/$CHANNEL_ID"
        fi

        # Nettoyage guild
        assert_status "DELETE /api/guilds/$CH_GUILD_ID (nettoyage)" 204 \
            -X DELETE "$BASE_URL/guilds/$CH_GUILD_ID"
    fi
fi
echo ""

# =============================================================================
# 5. ROLES — CRUD complet
# =============================================================================
echo -e "${BOLD}5 · Roles (CRUD)${NC}"

if [ -n "$OWNER_ID" ]; then
    assert_status "POST /api/guilds (pour roles)" 201 \
        -X POST "$BASE_URL/guilds" \
        -H "Content-Type: application/json" \
        -d "{\"name\":\"Role Test Guild\",\"owner\":{\"id\":$OWNER_ID}}"
    ROLE_GUILD_ID=$(extract_id)

    assert_status "GET  /api/roles (liste)" 200 "$BASE_URL/roles"

    if [ -n "$ROLE_GUILD_ID" ]; then
        assert_status "POST /api/roles (créer)" 201 \
            -X POST "$BASE_URL/roles" \
            -H "Content-Type: application/json" \
            -d "{\"name\":\"Test Role\",\"color\":\"#FF5500\",\"guild\":{\"id\":$ROLE_GUILD_ID}}"
        ROLE_ID=$(extract_id)

        if [ -n "$ROLE_ID" ]; then
            assert_status "GET  /api/roles/$ROLE_ID (lire)" 200 "$BASE_URL/roles/$ROLE_ID"

            assert_status "PUT  /api/roles/$ROLE_ID (modifier)" 200 \
                -X PUT "$BASE_URL/roles/$ROLE_ID" \
                -H "Content-Type: application/json" \
                -d "{\"name\":\"Test Role Updated\",\"color\":\"#00FF00\",\"guild\":{\"id\":$ROLE_GUILD_ID}}"

            assert_status "GET  /api/roles/guild/$ROLE_GUILD_ID" 200 "$BASE_URL/roles/guild/$ROLE_GUILD_ID"

            assert_status "DELETE /api/roles/$ROLE_ID (supprimer)" 204 \
                -X DELETE "$BASE_URL/roles/$ROLE_ID"
        fi

        # Nettoyage guild
        assert_status "DELETE /api/guilds/$ROLE_GUILD_ID (nettoyage)" 204 \
            -X DELETE "$BASE_URL/guilds/$ROLE_GUILD_ID"
    fi
fi
echo ""

# =============================================================================
# 6. MESSAGES — CRUD complet
# =============================================================================
echo -e "${BOLD}6 · Messages (CRUD)${NC}"

if [ -n "$OWNER_ID" ]; then
    # Créer guild + channel pour envoyer un message
    assert_status "POST /api/guilds (pour messages)" 201 \
        -X POST "$BASE_URL/guilds" \
        -H "Content-Type: application/json" \
        -d "{\"name\":\"Msg Test Guild\",\"owner\":{\"id\":$OWNER_ID}}"
    MSG_GUILD_ID=$(extract_id)

    assert_status "POST /api/channels (pour messages)" 201 \
        -X POST "$BASE_URL/channels" \
        -H "Content-Type: application/json" \
        -d "{\"name\":\"msg-channel\",\"type\":\"TEXT\",\"guild\":{\"id\":$MSG_GUILD_ID}}"
    MSG_CHANNEL_ID=$(extract_id)

    assert_status "GET  /api/messages (liste)" 200 "$BASE_URL/messages?limit=5"

    assert_status "POST /api/messages (créer)" 201 \
        -X POST "$BASE_URL/messages" \
        -H "Content-Type: application/json" \
        -d "{\"content\":\"Hello from test-api.sh!\",\"author\":{\"id\":$OWNER_ID},\"channel\":{\"id\":$MSG_CHANNEL_ID}}"
    MSG_ID=$(extract_id)

    if [ -n "$MSG_ID" ]; then
        assert_status "GET  /api/messages/$MSG_ID (lire)" 200 "$BASE_URL/messages/$MSG_ID"

        assert_status "PUT  /api/messages/$MSG_ID (modifier)" 200 \
            -X PUT "$BASE_URL/messages/$MSG_ID" \
            -H "Content-Type: application/json" \
            -d "{\"content\":\"Edited message!\",\"author\":{\"id\":$OWNER_ID},\"channel\":{\"id\":$MSG_CHANNEL_ID}}"

        assert_status "GET  /api/messages/channel/$MSG_CHANNEL_ID" 200 "$BASE_URL/messages/channel/$MSG_CHANNEL_ID"
        assert_status "GET  /api/messages/user/$OWNER_ID" 200 "$BASE_URL/messages/user/$OWNER_ID"
        assert_status "GET  /api/messages/search?content=Edited" 200 "$BASE_URL/messages/search?content=Edited"

        assert_status "DELETE /api/messages/$MSG_ID (soft-delete)" 204 \
            -X DELETE "$BASE_URL/messages/$MSG_ID"
    fi

    # Nettoyage
    [ -n "$MSG_CHANNEL_ID" ] && curl -s -X DELETE "$BASE_URL/channels/$MSG_CHANNEL_ID" > /dev/null 2>&1
    [ -n "$MSG_GUILD_ID" ] && curl -s -X DELETE "$BASE_URL/guilds/$MSG_GUILD_ID" > /dev/null 2>&1
fi
echo ""

# =============================================================================
# 7. Nettoyage final
# =============================================================================
echo -e "${BOLD}7 · Nettoyage${NC}"
if [ -n "$OWNER_ID" ]; then
    assert_status "DELETE /api/users/$OWNER_ID (owner temporaire)" 204 \
        -X DELETE "$BASE_URL/users/$OWNER_ID"
fi
echo ""

# =============================================================================
# Résumé
# =============================================================================
TOTAL=$((PASS + FAIL))
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
if [ "$FAIL" -eq 0 ]; then
    echo -e "${GREEN}${BOLD}✅ $PASS/$TOTAL tests passés — tout est OK !${NC}"
else
    echo -e "${RED}${BOLD}❌ $PASS/$TOTAL passés, $FAIL échoué(s)${NC}"
fi
echo ""
exit "$FAIL"