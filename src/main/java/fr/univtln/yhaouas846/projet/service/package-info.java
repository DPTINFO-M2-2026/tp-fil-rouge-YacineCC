/**
 * Package contenant les services métier de l'application.
 *
 * <p>Les services métier encapsulent la logique applicative et constituent
 * la couche intermédiaire entre les ressources REST et la couche de persistance.
 * Cette architecture respecte les principes SOLID, notamment :</p>
 *
 * <ul>
 *   <li><b>Single Responsibility Principle (SRP)</b> : chaque service gère un domaine métier spécifique</li>
 *   <li><b>Dependency Inversion Principle (DIP)</b> : les services peuvent être injectés et testés indépendamment</li>
 *   <li><b>Open/Closed Principle</b> : extensibles sans modification du code existant</li>
 * </ul>
 *
 * <h2>Responsabilités des services</h2>
 * <ul>
 *   <li>Validation métier complexe (au-delà de la validation Bean Validation)</li>
 *   <li>Orchestration des opérations sur plusieurs entités</li>
 *   <li>Transformation entre entités JPA et DTOs</li>
 *   <li>Gestion des transactions (@Transactional)</li>
 *   <li>Logging des opérations métier</li>
 *   <li>Gestion des erreurs métier (BusinessException)</li>
 * </ul>
 *
 * <h2>Patterns utilisés</h2>
 * <ul>
 *   <li><b>Service Layer Pattern</b> : séparation claire entre présentation et logique métier</li>
 *   <li><b>Mapper Pattern</b> : transformation entité ↔ DTO via des classes dédiées</li>
 *   <li><b>Repository Pattern</b> : accès aux données via Panache repositories</li>
 * </ul>
 *
 * @see fr.univtln.yhaouas846.projet.resource
 * @see fr.univtln.yhaouas846.projet.dto
 * @see fr.univtln.yhaouas846.projet.entity
 * @since 1.0
 */
package fr.univtln.yhaouas846.projet.service;
