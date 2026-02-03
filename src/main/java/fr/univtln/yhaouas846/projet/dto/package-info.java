/**
 * Package contenant les Data Transfer Objects (DTOs) pour l'API REST.
 *
 * <p>Les DTOs permettent de séparer la représentation des données exposées via l'API
 * des entités JPA internes. Cette séparation offre plusieurs avantages :</p>
 *
 * <ul>
 *   <li><b>Sécurité</b> : évite d'exposer des champs sensibles ou internes</li>
 *   <li><b>Flexibilité</b> : permet d'adapter la structure des données sans modifier les entités</li>
 *   <li><b>Validation</b> : validation spécifique aux opérations (création, mise à jour)</li>
 *   <li><b>Performance</b> : projections et chargement optimisé des données</li>
 *   <li><b>Versioning API</b> : facilite l'évolution de l'API sans impacter le modèle de données</li>
 * </ul>
 *
 * <h2>Convention de nommage</h2>
 * <ul>
 *   <li>{@code XxxDTO} : DTO complet avec tous les champs</li>
 *   <li>{@code CreateXxxDTO} : DTO pour la création (sans ID, sans champs auto-générés)</li>
 *   <li>{@code UpdateXxxDTO} : DTO pour la mise à jour (avec ID, champs modifiables uniquement)</li>
 *   <li>{@code XxxSummaryDTO} : DTO léger pour les listes (projection minimale)</li>
 * </ul>
 *
 * <p>Les DTOs utilisent les annotations de validation Jakarta Bean Validation
 * ({@code @NotNull}, {@code @Size}, {@code @Email}, etc.) pour garantir l'intégrité
 * des données en entrée.</p>
 *
 * @see fr.univtln.yhaouas846.projet.entity
 * @since 1.0
 */
package fr.univtln.yhaouas846.projet.dto;
