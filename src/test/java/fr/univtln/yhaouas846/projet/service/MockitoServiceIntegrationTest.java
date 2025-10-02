package fr.univtln.yhaouas846.projet.service;

import fr.univtln.yhaouas846.projet.entity.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mockito;

/**
 * Tests avec Mockito utilisant des méthodes plutôt que des champs publics
 * Démonstration des bonnes pratiques Mockito
 */
@ExtendWith(MockitoExtension.class)
class MockitoServiceIntegrationTest {

    @Mock
    private List<String> mockList;

    @Test
    void testBasicMockingConcepts() {
        // Stubbing - définir le comportement du mock
        when(mockList.size()).thenReturn(5);
        when(mockList.get(0)).thenReturn("Premier élément");
        when(mockList.contains("test")).thenReturn(true);

        // Utilisation du mock
        assertEquals(5, mockList.size());
        assertEquals("Premier élément", mockList.get(0));
        assertTrue(mockList.contains("test"));

        // Vérifications - s'assurer que les méthodes ont été appelées
        verify(mockList).size();
        verify(mockList).get(0);
        verify(mockList).contains("test");
    }

    @Test
    void testMockingWithExceptions() {
        // Configurer le mock pour lancer une exception
        when(mockList.get(anyInt())).thenThrow(new IndexOutOfBoundsException("Index invalide"));

        // Vérifier que l'exception est lancée
        assertThrows(IndexOutOfBoundsException.class, () -> {
            mockList.get(10);
        });

        // Vérifier que la méthode a été appelée
        verify(mockList).get(10);
    }

    @Test
    void testMockingWithMultipleReturnValues() {
        // Configurer des valeurs de retour multiples
        when(mockList.size())
                .thenReturn(1)     // Premier appel
                .thenReturn(2)     // Deuxième appel
                .thenReturn(3);    // Troisième appel

        // Tester les appels successifs
        assertEquals(1, mockList.size());
        assertEquals(2, mockList.size());
        assertEquals(3, mockList.size());
        assertEquals(3, mockList.size()); // Reste à 3 pour les appels suivants

        // Vérifier le nombre d'appels
        verify(mockList, times(4)).size();
    }

    @Test
    void testArgumentMatchers() {
        // Utilisation d'argument matchers
        when(mockList.get(anyInt())).thenReturn("Élément quelconque");
        when(mockList.subList(eq(0), anyInt())).thenReturn(new ArrayList<>());

        // Tests
        assertEquals("Élément quelconque", mockList.get(5));
        assertEquals("Élément quelconque", mockList.get(100));
        assertNotNull(mockList.subList(0, 10));

        // Vérifications avec matchers
        verify(mockList, times(2)).get(anyInt());
        verify(mockList).subList(eq(0), eq(10));
    }

    @Test
    void testVoidMethodMocking() {
        // Pour les méthodes void, on utilise doNothing(), doThrow(), etc.
        doNothing().when(mockList).clear();
        doThrow(new UnsupportedOperationException()).when(mockList).add(anyString());

        // Test de la méthode void
        mockList.clear(); // Ne fait rien, pas d'exception

        // Test de l'exception sur add
        assertThrows(UnsupportedOperationException.class, () -> {
            mockList.add("test");
        });

        // Vérifications
        verify(mockList).clear();
        verify(mockList).add("test");
    }

    @Test
    void testMockitoWithRealEntities() {
        // Utilisation de Mockito avec nos entités réelles
        // Ici on montre comment mocker des comportements même avec des champs publics

        User user = new User();
        user.username = "TestUser";
        user.discriminator = "1234";

        Guild guild = new Guild();
        guild.name = "Test Guild";
        guild.owner = user;

        // Test de la logique métier
        assertNotNull(guild.owner);
        assertEquals("TestUser", guild.owner.username);
        assertEquals("Test Guild", guild.name);

        // Simulation d'un comportement
        if (guild.owner.username.equals("TestUser")) {
            guild.description = "Guilde du testeur";
        }

        assertEquals("Guilde du testeur", guild.description);
    }

    @Test
    void testSpyExample() {
        // Spy permet d'utiliser un objet réel tout en mockant certaines méthodes
        List<String> realList = new ArrayList<>();
        List<String> spyList = spy(realList);

        // Utilisation normale
        spyList.add("élément 1");
        spyList.add("élément 2");
        assertEquals(2, spyList.size());

        // Stubbing d'une méthode spécifique
        when(spyList.size()).thenReturn(100);
        assertEquals(100, spyList.size()); // Maintenant retourne 100

        // Vérifications
        verify(spyList, times(2)).add(anyString());
        verify(spyList, times(2)).size(); // 1 appel réel + 1 appel stubber
    }

    @Test
    void testMockitoVerificationModes() {
        // Différents modes de vérification avec Mockito

        // Configuration
        when(mockList.isEmpty()).thenReturn(true);

        // Appels multiples
        mockList.isEmpty();
        mockList.isEmpty();
        mockList.isEmpty();

        // Vérifications avec différents modes
        verify(mockList, times(3)).isEmpty();           // Exactement 3 fois
        verify(mockList, atLeast(2)).isEmpty();         // Au moins 2 fois
        verify(mockList, atMost(5)).isEmpty();          // Au plus 5 fois
        verify(mockList, atLeastOnce()).isEmpty();      // Au moins une fois

        // Vérifier qu'une méthode n'a jamais été appelée
        verify(mockList, never()).clear();

        // Vérifier dans un délai (pour les tests asynchrones)
        verify(mockList, timeout(1000).times(3)).isEmpty();
    }

    @Test
    void testMockitoInOrder() {
        // Vérification de l'ordre des appels
        when(mockList.add(anyString())).thenReturn(true);

        // Appels dans un ordre spécifique
        mockList.add("premier");
        mockList.add("deuxième");
        mockList.clear();

        // Vérification de l'ordre
        InOrder inOrder = inOrder(mockList);
        inOrder.verify(mockList).add("premier");
        inOrder.verify(mockList).add("deuxième");
        inOrder.verify(mockList).clear();
    }

    @Test
    void testMockitoCaptor() {
        // Capture d'arguments pour vérification détaillée
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);

        when(mockList.add(anyString())).thenReturn(true);

        // Appels
        mockList.add("valeur1");
        mockList.add("valeur2");

        // Capture et vérification
        verify(mockList, times(2)).add(captor.capture());
        List<String> capturedValues = captor.getAllValues();

        assertEquals(2, capturedValues.size());
        assertEquals("valeur1", capturedValues.get(0));
        assertEquals("valeur2", capturedValues.get(1));
    }

    @Test
    @SuppressWarnings("unchecked")
    void testMockitoReset() {
        // Configuration initiale
        when(mockList.size()).thenReturn(5);
        assertEquals(5, mockList.size());

        // Reset du mock
        Mockito.reset(mockList);

        // Après reset, le comportement par défaut est restauré
        assertEquals(0, mockList.size()); // Comportement par défaut d'un mock

        // Nouvelle configuration après reset
        when(mockList.size()).thenReturn(10);
        assertEquals(10, mockList.size());
    }
}