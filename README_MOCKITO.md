# Guide d'utilisation de Mockito dans le projet Discord Bot

## Vue d'ensemble

Mockito a été intégré dans le projet pour permettre les tests unitaires avec des objets simulés (mocks). Cette documentation présente les patterns et exemples d'utilisation.

## Configuration

### Dépendances Maven

```xml
<dependency>
    <groupId>org.mockito</groupId>
    <artifactId>mockito-core</artifactId>
    <version>5.15.0</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.mockito</groupId>
    <artifactId>mockito-junit-jupiter</artifactId>
    <version>5.15.0</version>
    <scope>test</scope>
</dependency>
```

### Annotation de classe

```java
@ExtendWith(MockitoExtension.class)
class MonTest {
    @Mock
    MonService mockService;
}
```

## Patterns Mockito implémentés

### 1. Mocks basiques

```java
// Création d'un mock
List<String> mockList = mock(List.class);

// Configuration du comportement
when(mockList.size()).thenReturn(5);

// Utilisation
assertEquals(5, mockList.size());

// Vérification
verify(mockList).size();
```

### 2. ArgumentMatchers

```java
// Matcher générique
when(mockService.findById(any(Long.class))).thenReturn(entity);

// Matchers spécifiques
when(mockService.findByName(anyString())).thenReturn(entity);
when(mockService.createEntity(eq("test"))).thenReturn(entity);
```

### 3. ArgumentCaptor

```java
@Captor
ArgumentCaptor<String> stringCaptor;

// Utilisation
mockList.add("test");
verify(mockList).add(stringCaptor.capture());

// Vérification de la valeur capturée
assertEquals("test", stringCaptor.getValue());
```

### 4. Vérifications avec times()

```java
// Vérifier le nombre d'appels
verify(mockService, times(2)).methodCall();
verify(mockService, never()).methodNeverCalled();
verify(mockService, atLeast(1)).methodAtLeastOnce();
verify(mockService, atMost(3)).methodAtMostThree();
```

### 5. Ordre des appels (InOrder)

```java
InOrder inOrder = inOrder(mockService1, mockService2);

// Les appels doivent être dans cet ordre
inOrder.verify(mockService1).firstCall();
inOrder.verify(mockService2).secondCall();
inOrder.verify(mockService1).thirdCall();
```

### 6. Exceptions simulées

```java
// Lancer une exception
when(mockService.riskyMethod()).thenThrow(new RuntimeException("Erreur simulée"));

// Tester la gestion d'exception
assertThrows(RuntimeException.class, () -> {
    mockService.riskyMethod();
});
```

### 7. Comportements multiples

```java
// Différents retours selon les appels
when(mockService.getValue())
    .thenReturn("première fois")
    .thenReturn("deuxième fois")
    .thenThrow(new RuntimeException("troisième fois"));
```

### 8. Vérification de tous les appels

```java
// S'assurer qu'aucune interaction non vérifiée n'a eu lieu
verifyNoMoreInteractions(mockService);
```

### 9. Reset des mocks

```java
// Remettre à zéro un mock
Mockito.reset(mockService);
```

## Fichiers de tests Mockito

### 1. MockitoServiceIntegrationTest.java

Fichier principal démontrant tous les patterns Mockito :

- Tests de base avec when/verify
- ArgumentMatchers et ArgumentCaptor
- Vérifications avec times()
- Gestion des exceptions
- Ordre des appels (InOrder)
- Comportements multiples
- Reset des mocks

### 2. BotResourceMockTest.java

Tests d'intégration montrant l'utilisation de Mockito avec RestAssured :

```java
@ExtendWith(MockitoExtension.class)
class BotResourceMockTest {
    @Mock
    DiscordBotService discordBotService;

    @Test
    void testWithMockService() {
        when(discordBotService.someMethod()).thenReturn("result");
        
        given()
            .when().get("/api/endpoint")
            .then()
            .statusCode(200);
            
        verify(discordBotService).someMethod();
    }
}
```

## Bonnes pratiques

### 1. Nommage des mocks

```java
// ✅ Bon
@Mock
UserService mockUserService;

// ❌ Éviter
@Mock
UserService userService; // Confond avec le vrai service
```

### 2. Utilisation des ArgumentMatchers

```java
// ✅ Tous les paramètres doivent utiliser des matchers
when(service.method(any(), eq("test"))).thenReturn(result);

// ❌ Mélange matchers et valeurs littérales
when(service.method(any(), "test")).thenReturn(result);
```

### 3. Vérifications spécifiques

```java
// ✅ Vérifications précises
verify(mockService, times(1)).specificMethod(eq("expectedValue"));

// ❌ Vérifications trop générales
verify(mockService).specificMethod(any());
```

### 4. Gestion des champs publics JPA

⚠️ **Important** : Mockito ne peut pas vérifier l'accès aux champs publics des entités JPA.

```java
// ❌ Ne fonctionne pas
verify(mockEntity).publicField; // Erreur de compilation

// ✅ Alternative : tester les getters si disponibles
verify(mockEntity).getPublicField();

// ✅ Ou tester les méthodes qui utilisent ces champs
verify(mockService).methodThatUsesEntity(mockEntity);
```

## Exécution des tests

### Tous les tests Mockito

```bash
mvn test -Dtest=MockitoServiceIntegrationTest
```

### Test spécifique

```bash
mvn test -Dtest=MockitoServiceIntegrationTest#testArgumentCaptor
```

### Compilation

```bash
mvn test-compile
```

## Résultats

- ✅ 11 tests Mockito passent avec succès
- ✅ Intégration complète avec JUnit 5
- ✅ Compatibilité avec Quarkus
- ✅ Exemples complets de tous les patterns principaux

## Avertissements

Les warnings concernant l'agent Java de Mockito sont normaux et n'affectent pas le fonctionnement :

```
WARNING: A Java agent has been loaded dynamically
WARNING: Dynamic loading of agents will be disallowed by default in a future release
```

Pour les éviter en production, ajouter Mockito comme agent explicite selon la documentation officielle.

---

Cette intégration de Mockito fournit une base solide pour les tests unitaires avec des mocks dans votre projet Discord Bot.