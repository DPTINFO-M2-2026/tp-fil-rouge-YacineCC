/**
 * Couche "projet" de l'application Quarkus.
 *
 * <p>
 * Ce module regroupe l'API REST (package {@code resource}), le modèle de données JPA
 * (package {@code entity}) et la couche d'accès aux données (package {@code repository}).
 * L'objectif principal est de fournir une base stable et testable pour la logique
 * métier autour d'un bot Discord, tout en restant compatible avec Quarkus.
 * </p>
 *
 * <h2>Organisation</h2>
 * <ul>
 *   <li>{@code fr.univtln.yhaouas846.projet.resource} : endpoints HTTP (Jakarta REST)</li>
 *   <li>{@code fr.univtln.yhaouas846.projet.entity} : entités JPA + contraintes de validation</li>
 *   <li>{@code fr.univtln.yhaouas846.projet.repository} : accès aux données (repositories injectables)</li>
 * </ul>
 */
package fr.univtln.yhaouas846.projet;
