/**
 * API HTTP (Jakarta REST) exposée par l'application.
 *
 * <p>
 * Chaque classe {@code *Resource} est un contrôleur REST responsable de :
 * </p>
 * <ul>
 *   <li>définir les routes (paths) et formats (JSON)</li>
 *   <li>appliquer une validation d'entrée (via Bean Validation)</li>
 *   <li>déléguer la logique métier à des services et/ou repositories</li>
 * </ul>
 *
 * <p>
 * Les ressources doivent rester fines : pas de logique métier complexe ici.
 * </p>
 */
package fr.univtln.yhaouas846.projet.resource;
