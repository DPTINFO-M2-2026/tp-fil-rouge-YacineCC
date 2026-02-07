package fr.univtln.yhaouas846.projet.annotation;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.Writer;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Processeur d'annotations exécuté à la compilation.
 *
 * <p>Ce processeur traite l'annotation {@link Logged} et effectue deux tâches :</p>
 * <ol>
 *   <li><b>Vérification</b> : s'assure que {@code @Logged} est placée sur des
 *       méthodes publiques ou sur des classes/interfaces. Émet un warning si
 *       l'annotation est utilisée sur une méthode non-publique.</li>
 *   <li><b>Génération de rapport</b> : produit un fichier
 *       {@code META-INF/logged-methods.txt} listant toutes les méthodes annotées,
 *       utile pour l'audit et la documentation.</li>
 * </ol>
 *
 * <h2>Enregistrement</h2>
 * <p>Le processeur est déclaré dans
 * {@code META-INF/services/javax.annotation.processing.Processor} pour être
 * découvert automatiquement par {@code javac}.</p>
 *
 * @see Logged
 * @since 1.0
 */
@SupportedAnnotationTypes("fr.univtln.yhaouas846.projet.annotation.Logged")
@SupportedSourceVersion(SourceVersion.RELEASE_21)
public class LoggedProcessor extends AbstractProcessor {

    /** Liste des méthodes annotées découvertes pendant la compilation. */
    private final List<String> loggedMethods = new ArrayList<>();

    /** Indique si le rapport a déjà été généré (pour éviter les doublons). */
    private boolean reportGenerated = false;

    /**
     * Traite les éléments annotés {@link Logged} découverts par le compilateur.
     *
     * <p>Pour chaque élément annoté, le processeur :</p>
     * <ul>
     *   <li>Vérifie que l'annotation est correctement placée</li>
     *   <li>Collecte les méthodes pour le rapport</li>
     *   <li>Émet des warnings si nécessaire</li>
     * </ul>
     *
     * @param annotations ensemble des types d'annotations à traiter
     * @param roundEnv    environnement du round de compilation courant
     * @return {@code false} pour ne pas revendiquer l'annotation (laisser
     *         d'autres processeurs la traiter si besoin)
     */
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Messager messager = processingEnv.getMessager();

        for (TypeElement annotation : annotations) {
            for (Element element : roundEnv.getElementsAnnotatedWith(annotation)) {

                if (element.getKind() == ElementKind.METHOD) {
                    ExecutableElement method = (ExecutableElement) element;
                    TypeElement enclosingClass = (TypeElement) method.getEnclosingElement();

                    // Vérification : la méthode doit être publique
                    if (!method.getModifiers().contains(Modifier.PUBLIC)) {
                        messager.printMessage(
                                Diagnostic.Kind.WARNING,
                                "@Logged est conçu pour des méthodes publiques. "
                                        + "Sur une méthode non-publique, l'intercepteur CDI ne sera pas déclenché.",
                                element
                        );
                    }

                    // Vérification : pas sur une méthode statique
                    if (method.getModifiers().contains(Modifier.STATIC)) {
                        messager.printMessage(
                                Diagnostic.Kind.ERROR,
                                "@Logged ne peut pas être utilisé sur une méthode statique "
                                        + "(les intercepteurs CDI ne fonctionnent pas sur les méthodes statiques).",
                                element
                        );
                    }

                    String entry = enclosingClass.getQualifiedName() + "." + method.getSimpleName();
                    loggedMethods.add(entry);

                    messager.printMessage(
                            Diagnostic.Kind.NOTE,
                            "[@Logged] Méthode enregistrée : " + entry,
                            element
                    );

                } else if (element.getKind() == ElementKind.CLASS
                        || element.getKind() == ElementKind.INTERFACE) {
                    TypeElement classElement = (TypeElement) element;
                    String entry = classElement.getQualifiedName() + " (toutes les méthodes)";
                    loggedMethods.add(entry);

                    messager.printMessage(
                            Diagnostic.Kind.NOTE,
                            "[@Logged] Classe enregistrée : " + classElement.getQualifiedName(),
                            element
                    );
                }
            }
        }

        // Générer le rapport à la fin du dernier round
        if (roundEnv.processingOver() && !loggedMethods.isEmpty() && !reportGenerated) {
            generateReport();
            reportGenerated = true;
        }

        return false;
    }

    /**
     * Génère le fichier rapport {@code META-INF/logged-methods.txt}.
     *
     * <p>Ce fichier contient la liste de toutes les méthodes annotées {@code @Logged}
     * découvertes pendant la compilation, avec un horodatage.</p>
     */
    private void generateReport() {
        try {
            FileObject resource = processingEnv.getFiler().createResource(
                    StandardLocation.CLASS_OUTPUT,
                    "",
                    "META-INF/logged-methods.txt"
            );

            try (Writer writer = resource.openWriter()) {
                writer.write("# Méthodes annotées @Logged\n");
                writer.write("# Généré automatiquement par LoggedProcessor\n");
                writer.write("# Date : " + LocalDateTime.now() + "\n");
                writer.write("#\n");

                for (String method : loggedMethods) {
                    writer.write(method + "\n");
                }

                writer.write("\n# Total : " + loggedMethods.size() + " élément(s)\n");
            }

            processingEnv.getMessager().printMessage(
                    Diagnostic.Kind.NOTE,
                    "[@Logged] Rapport généré : META-INF/logged-methods.txt (" + loggedMethods.size() + " éléments)"
            );

        } catch (IOException e) {
            processingEnv.getMessager().printMessage(
                    Diagnostic.Kind.WARNING,
                    "[@Logged] Impossible de générer le rapport : " + e.getMessage()
            );
        }
    }
}
