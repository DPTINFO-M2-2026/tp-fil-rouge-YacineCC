/**
 * Intégration Discord (Discord4J) et exécution du bot.
 *
 * <p>
 * Ce package regroupe :
 * </p>
 * <ul>
 *   <li>un point d'entrée (classe main / bootstrap) pour démarrer le bot</li>
 *   <li>des services d'orchestration (ex: logique d'interaction Discord)</li>
 *   <li>des clients externes (ex: IA via LangChain4j / Ollama)</li>
 * </ul>
 *
 * <p>
 * Les classes de cette zone doivent rester découplées de la couche REST, afin
 * de permettre des tests unitaires et une exécution indépendante si nécessaire.
 * </p>
 */
package fr.univtln.yhaouas846.discord4j;
