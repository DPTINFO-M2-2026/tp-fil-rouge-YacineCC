-- Insertion d'utilisateurs de test
INSERT INTO discord_user (id, username, discriminator, email, avatar_url, created_at, is_bot) VALUES 
(1, 'AdminUser', '0001', 'admin@discord.test', 'https://cdn.discord.com/avatars/1/admin.png', CURRENT_TIMESTAMP, false),
(2, 'TestUser', '0002', 'user@discord.test', 'https://cdn.discord.com/avatars/2/user.png', CURRENT_TIMESTAMP, false),
(3, 'BotHelper', '0000', 'bot@discord.test', 'https://cdn.discord.com/avatars/3/bot.png', CURRENT_TIMESTAMP, true);

-- Insertion de guildes de test
INSERT INTO guild (id, name, description, icon_url, owner_id, created_at, member_limit) VALUES 
(1, 'Test Server', 'Un serveur de test pour le développement', 'https://cdn.discord.com/icons/1/server.png', 1, CURRENT_TIMESTAMP, 100),
(2, 'Gaming Community', 'Communauté de gamers', 'https://cdn.discord.com/icons/2/gaming.png', 2, CURRENT_TIMESTAMP, 500);

-- Insertion de canaux de test
INSERT INTO channel (id, name, description, type, guild_id, created_at, position, is_nsfw) VALUES 
(1, 'general', 'Canal général pour discussions', 'TEXT', 1, CURRENT_TIMESTAMP, 0, false),
(2, 'announcements', 'Annonces importantes', 'TEXT', 1, CURRENT_TIMESTAMP, 1, false),
(3, 'voice-general', 'Canal vocal général', 'VOICE', 1, CURRENT_TIMESTAMP, 2, false),
(4, 'gaming-chat', 'Discussion sur les jeux', 'TEXT', 2, CURRENT_TIMESTAMP, 0, false);

-- Insertion de rôles de test
INSERT INTO role (id, name, color, guild_id, position, created_at, can_manage_channels, can_manage_roles, can_manage_messages, can_kick_members, can_ban_members, can_send_messages, can_read_messages) VALUES 
(1, 'Admin', '#FF0000', 1, 3, CURRENT_TIMESTAMP, true, true, true, true, true, true, true),
(2, 'Moderator', '#00FF00', 1, 2, CURRENT_TIMESTAMP, true, false, true, true, false, true, true),
(3, 'Member', '#0099FF', 1, 1, CURRENT_TIMESTAMP, false, false, false, false, false, true, true),
(4, 'Gamer', '#FF6600', 2, 1, CURRENT_TIMESTAMP, false, false, false, false, false, true, true);

-- Insertion de messages de test
INSERT INTO message (id, content, author_id, channel_id, created_at, is_edited, is_deleted) VALUES 
(1, 'Bienvenue sur le serveur !', 1, 1, CURRENT_TIMESTAMP, false, false),
(2, 'Merci pour l''invitation !', 2, 1, CURRENT_TIMESTAMP, false, false),
(3, 'N''oubliez pas de lire les règles', 3, 2, CURRENT_TIMESTAMP, false, false),
(4, 'Quelqu''un veut jouer ?', 2, 4, CURRENT_TIMESTAMP, false, false);

-- Association des membres aux guildes
INSERT INTO guild_members (guild_id, user_id) VALUES 
(1, 1), (1, 2), (1, 3),
(2, 2), (2, 1);

-- Association des utilisateurs aux rôles
INSERT INTO user_roles (role_id, user_id) VALUES 
(1, 1), (2, 2), (3, 3),
(4, 2), (3, 1);