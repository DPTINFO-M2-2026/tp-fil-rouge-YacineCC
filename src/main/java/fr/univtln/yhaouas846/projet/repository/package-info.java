/**
 * Couche d'accès aux données.
 *
 * <p>
 * Les repositories encapsulent les opérations de persistance/lecture afin de :
 * </p>
 * <ul>
 *   <li>centraliser les requêtes et règles de chargement</li>
 *   <li>rendre la couche supérieure testable (mockable via Mockito)</li>
 *   <li>réduire l'utilisation directe d'appels statiques Panache côté métier</li>
 * </ul>
 */
package fr.univtln.yhaouas846.projet.repository;
