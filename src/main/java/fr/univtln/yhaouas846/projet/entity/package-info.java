/**
 * Modèle de données (JPA/Hibernate) de l'application.
 *
 * <p>
 * Les classes de ce package représentent les entités persistées en base.
 * Elles portent également des contraintes de validation (Jakarta Validation)
 * utilisées par Quarkus pour valider les payloads et/ou lors de la persistance.
 * </p>
 *
 * <h2>Bonnes pratiques</h2>
 * <ul>
 *   <li>Respecter la cohérence entre contraintes de validation et schéma SQL.</li>
 *   <li>Être prudent avec la sérialisation JSON des associations (lazy loading).</li>
 * </ul>
 */
package fr.univtln.yhaouas846.projet.entity;
