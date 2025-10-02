-- Données de test pour les tests unitaires et d'intégration

-- Utilisateurs de test
INSERT INTO discord_user (id, username, discriminator, email, avatar_url, created_at, is_bot) VALUES 
(100, 'TestUser1', '1111', 'test1@example.com', 'https://example.com/avatar1.png', CURRENT_TIMESTAMP, false),
(101, 'TestUser2', '2222', 'test2@example.com', 'https://example.com/avatar2.png', CURRENT_TIMESTAMP, false),
(102, 'TestBot', '0000', 'bot@example.com', 'https://example.com/bot.png', CURRENT_TIMESTAMP, true),
(103, 'AdminTest', '9999', 'admin@example.com', 'https://example.com/admin.png', CURRENT_TIMESTAMP, false);

-- Guildes de test
INSERT INTO guild (id, name, description, icon_url, owner_id, created_at, member_limit) VALUES 
(200, 'Test Guild 1', 'Guilde de test numéro 1', 'https://example.com/guild1.png', 100, CURRENT_TIMESTAMP, 100),
(201, 'Test Guild 2', 'Guilde de test numéro 2', 'https://example.com/guild2.png', 101, CURRENT_TIMESTAMP, 200);

-- Canaux de test
INSERT INTO channel (id, name, description, type, guild_id, created_at, position, is_nsfw) VALUES 
(300, 'test-general', 'Canal général de test', 'TEXT', 200, CURRENT_TIMESTAMP, 0, false),
(301, 'test-voice', 'Canal vocal de test', 'VOICE', 200, CURRENT_TIMESTAMP, 1, false),
(302, 'test-announcements', 'Canal d''annonces de test', 'TEXT', 201, CURRENT_TIMESTAMP, 0, false);

-- Rôles de test
INSERT INTO role (id, name, color, guild_id, position, created_at, can_manage_channels, can_manage_roles, can_manage_messages, can_kick_members, can_ban_members, can_send_messages, can_read_messages) VALUES 
(400, 'Test Admin', '#FF0000', 200, 3, CURRENT_TIMESTAMP, true, true, true, true, true, true, true),
(401, 'Test Member', '#00FF00', 200, 1, CURRENT_TIMESTAMP, false, false, false, false, false, true, true),
(402, 'Test Moderator', '#0000FF', 201, 2, CURRENT_TIMESTAMP, true, false, true, true, false, true, true);

-- Messages de test
INSERT INTO message (id, content, author_id, channel_id, created_at, is_edited, is_deleted) VALUES 
(500, 'Message de test 1', 100, 300, CURRENT_TIMESTAMP, false, false),
(501, 'Message de test 2', 101, 300, CURRENT_TIMESTAMP, false, false),
(502, 'Message supprimé', 100, 300, CURRENT_TIMESTAMP, false, true),
(503, 'Message édité', 101, 302, CURRENT_TIMESTAMP, true, false);

-- Associations guildes-membres
INSERT INTO guild_members (guild_id, user_id) VALUES 
(200, 100), (200, 101), (200, 102),
(201, 101), (201, 103);

-- Associations utilisateurs-rôles
INSERT INTO user_roles (role_id, user_id) VALUES 
(400, 100), (401, 101), (401, 102),
(402, 101), (401, 103);