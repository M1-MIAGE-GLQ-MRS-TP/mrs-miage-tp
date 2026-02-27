# TP 3 — Refactorer BiblioTech

## Génie Logiciel et Qualité — M1 MIAGE
**Durée : 2h | Projet : BiblioTech**

---

## Objectifs du TP

- Écrire des **tests de caractérisation** sur du code legacy
- Appliquer les **refactorings** planifiés en TD
- Utiliser le **polymorphisme** pour remplacer les conditionnels
- Pratiquer les **Sprout Method/Class** pour ajouter des fonctionnalités

---

## Prérequis

- TP2 terminé (outils configurés)
- Tests existants au vert
- IntelliJ IDEA avec raccourcis refactoring

---

## Partie 1 : Tests de caractérisation (30 min)

### Objectif

Avant de modifier `calculatePenalty()` et `createLoan()`, nous devons **capturer leur comportement actuel** avec des tests de caractérisation.

### 1.1 Créer la classe de test

Créez `src/test/java/com/bibliotech/service/CharacterizationTest.java` :

```java
package com.bibliotech.service;

import model.fr.amu.univ.miage.m1.glq.Member;
import fr.amu.univ.miage.m1.glq.service.LibraryManager;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests de caractérisation pour le code legacy.
 *
 * Ces tests capturent le comportement ACTUEL du code,
 * même si ce comportement contient des bugs.
 *
 * Objectif : détecter toute régression pendant le refactoring.
 */
class CharacterizationTest {

    private LibraryManager manager;

    @BeforeEach
    void setUp() {
        LibraryManager.resetInstance();
        manager = LibraryManager.getInstance();
    }

    // ════════════════════════════════════════════════════════════════
    // Tests de caractérisation pour calculatePenalty()
    // ════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("Caractérisation: Étudiant - pénalité à 50%")
    void student_penalty_is_half_rate() {
        // Arrange
        Member student = new Member("M001", "Alice", "Martin",
                "alice@univ.fr", "STUDENT");
        int daysOverdue = 10;

        // Act
        double penalty = manager.calculatePenalty(student, daysOverdue);

        // Assert - Capturer la valeur ACTUELLE
        // TODO: Exécuter une première fois pour voir la valeur
        // puis écrire l'assertion
        assertThat(penalty).isEqualTo(/* valeur observée */);
    }

    @Test
    @DisplayName("Caractérisation: Enseignant - pas de pénalité")
    void teacher_has_no_penalty() {
        // TODO: Compléter
        Member teacher = new Member("M002", "Bob", "Dupont",
                "bob@univ.fr", "TEACHER");

        double penalty = manager.calculatePenalty(teacher, 30);

        assertThat(penalty).isEqualTo(/* valeur observée */);
    }

    @Test
    @DisplayName("Caractérisation: Personnel - pénalité à 75%")
    void staff_penalty_is_75_percent() {
        // TODO: Compléter
    }

    @Test
    @DisplayName("Caractérisation: Externe - pénalité à 150%")
    void external_penalty_is_150_percent() {
        // TODO: Compléter
    }

    @Test
    @DisplayName("Caractérisation: Type inconnu - pénalité par défaut")
    void unknown_type_uses_default_penalty() {
        Member unknown = new Member("M005", "Eve", "Unknown",
                "eve@mail.com", "UNKNOWN_TYPE");

        double penalty = manager.calculatePenalty(unknown, 10);

        assertThat(penalty).isEqualTo(/* valeur observée */);
    }

    @Test
    @DisplayName("Caractérisation: 0 jours de retard")
    void zero_days_overdue_returns_zero() {
        Member student = new Member("M001", "Alice", "Martin",
                "alice@univ.fr", "STUDENT");

        double penalty = manager.calculatePenalty(student, 0);

        assertThat(penalty).isEqualTo(/* valeur observée */);
    }

    // ════════════════════════════════════════════════════════════════
    // Tests de caractérisation pour canBorrow() / validation membre
    // ════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("Caractérisation: Membre actif peut emprunter")
    void active_member_can_borrow() {
        // TODO: Tester avec un membre actif valide
    }

    @Test
    @DisplayName("Caractérisation: Membre inactif ne peut pas emprunter")
    void inactive_member_cannot_borrow() {
        // TODO: Tester qu'une exception est levée
    }

    @Test
    @DisplayName("Caractérisation: Quota étudiant = 5 emprunts")
    void student_quota_is_5() {
        // TODO: Vérifier le comportement du quota
    }

    // ════════════════════════════════════════════════════════════════
    // Tests de caractérisation pour les cas limites
    // ════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("Caractérisation: Pénalité négative (jours négatifs)")
    void negative_days_behavior() {
        Member student = new Member("M001", "Alice", "Martin",
                "alice@univ.fr", "STUDENT");

        // Que se passe-t-il avec des jours négatifs ?
        double penalty = manager.calculatePenalty(student, -5);

        // Capturer le comportement actuel (peut-être un bug !)
        assertThat(penalty).isEqualTo(/* valeur observée */);
    }
}
```

### 1.2 Processus de caractérisation

Pour chaque test :

1. **Exécuter** le test sans assertion → observer la valeur dans la console
2. **Copier** la valeur observée dans l'assertion
3. **Vérifier** que le test passe
4. **Ne pas corriger** les bugs découverts (pour l'instant)

### 1.3 Travail demandé

Complétez les tests et remplissez ce tableau :

| Test | Valeur observée | Bug potentiel ? |
|------|-----------------|-----------------|
| student_penalty (10 jours) | | |
| teacher_penalty (30 jours) | | |
| staff_penalty (10 jours) | | |
| external_penalty (10 jours) | | |
| unknown_type (10 jours) | | |
| zero_days | | |
| negative_days | | |

**📝 Livrable :** Tous les tests de caractérisation passent au vert.

---

## Partie 2 : Refactorings guidés (60 min)

### 2.1 Extract Method sur `createLoan()` (20 min)

Découpez la méthode `createLoan()` en sous-méthodes cohérentes.

**Étape 1 :** Extraire la validation du membre

```java
// AVANT (dans createLoan)
Member member = members.get(memberId);
if (member == null) {
    throw new RuntimeException("Membre non trouvé");
}
if (!member.isActive()) {
    throw new RuntimeException("Membre inactif");
}
// ...

// APRÈS
private Member findAndValidateMember(String memberId) {
    Member member = findMemberOrThrow(memberId);
    ensureMemberIsActive(member);
    ensureMembershipNotExpired(member);
    ensureNoPendingPenalties(member);
    ensureLoanQuotaNotReached(member);
    return member;
}

private Member findMemberOrThrow(String memberId) {
    Member member = members.get(memberId);
    if (member == null) {
        throw new MemberNotFoundException(memberId);
    }
    return member;
}

private void ensureMemberIsActive(Member member) {
    if (!member.isActive()) {
        throw new InactiveMemberException(member.getId());
    }
}
```

**Raccourci :** Sélectionnez le bloc de code → `Ctrl + Alt + M`

**Étape 2 :** Extraire le calcul des pénalités impayées

```java
private double calculateUnpaidPenalties(String memberId) {
    return loans.values().stream()
        .filter(loan -> loan.getMemberId().equals(memberId))
        .mapToDouble(Loan::getPenaltyAmount)
        .sum();
}
```

**Étape 3 :** Extraire le comptage des emprunts actifs

```java
private int countActiveLoans(String memberId) {
    return (int) loans.values().stream()
        .filter(loan -> loan.getMemberId().equals(memberId))
        .filter(loan -> "ACTIVE".equals(loan.getStatus()) 
                     || "OVERDUE".equals(loan.getStatus()))
        .count();
}
```

**Étape 4 :** Résultat final

```java
public String createLoan(String memberId, String bookId) {
    Member member = findAndValidateMember(memberId);
    Book book = findAndValidateBook(bookId);
    
    return processLoanCreation(member, book);
}
```

**📝 À faire :** Après chaque extraction, lancez les tests !

---

### 2.2 Rename (5 min)

Renommez les éléments suivants avec `Shift + F6` :

| Ancien nom | Nouveau nom |
|------------|-------------|
| `calc()` | `calculatePenalty()` |
| `chk()` | `canMemberBorrowBook()` |
| Variable `m` | `member` |
| Variable `b` | `book` |
| Constante `p` | `PENALTY_RATE_PER_DAY` |

---

### 2.3 Extract Class : `MemberValidator` (15 min)

Créez une classe dédiée à la validation des membres :

```java
package com.bibliotech.service;

import model.fr.amu.univ.miage.m1.glq.Member;

import java.util.Date;

public class MemberValidator {

    private final LoanRepository loanRepository;
    private final MemberPolicyProvider policyProvider;

    public MemberValidator(LoanRepository loanRepository,
                           MemberPolicyProvider policyProvider) {
        this.loanRepository = loanRepository;
        this.policyProvider = policyProvider;
    }

    public void validateCanBorrow(Member member) {
        ensureIsActive(member);
        ensureMembershipNotExpired(member);
        ensureNoPendingPenalties(member);
        ensureLoanQuotaNotReached(member);
    }

    private void ensureIsActive(Member member) {
        if (!member.isActive()) {
            throw new InactiveMemberException(member.getId());
        }
    }

    private void ensureMembershipNotExpired(Member member) {
        if (member.getMembershipExpiryDate() != null
                && member.getMembershipExpiryDate().before(new Date())) {
            throw new ExpiredMembershipException(member.getId());
        }
    }

    private void ensureNoPendingPenalties(Member member) {
        double unpaid = loanRepository.calculateUnpaidPenalties(member.getId());
        if (unpaid > 10.0) {
            throw new UnpaidPenaltiesException(member.getId(), unpaid);
        }
    }

    private void ensureLoanQuotaNotReached(Member member) {
        int activeLoans = loanRepository.countActiveLoans(member.getId());
        int maxLoans = policyProvider.getMaxLoans(member);

        if (activeLoans >= maxLoans) {
            throw new LoanQuotaReachedException(member.getId(), maxLoans);
        }
    }
}
```

**📝 À faire :**
1. Créez les exceptions personnalisées (ou utilisez `IllegalStateException`)
2. Créez l'interface `LoanRepository`
3. Mettez à jour `LibraryManager` pour utiliser `MemberValidator`

---

### 2.4 Replace Conditional with Polymorphism (20 min)

Transformez le switch de `calculatePenalty()` en polymorphisme.

**Étape 1 :** Créer l'interface

```java
package com.bibliotech.policy;

public interface PenaltyPolicy {
    
    double calculatePenalty(int daysOverdue, double baseRate);
    
    int getMaxLoans();
    
    int getLoanDurationDays();
}
```

**Étape 2 :** Créer les implémentations

```java
package com.bibliotech.policy;

public class StudentPenaltyPolicy implements PenaltyPolicy {
    
    private static final double DISCOUNT_RATE = 0.5;
    
    @Override
    public double calculatePenalty(int daysOverdue, double baseRate) {
        return daysOverdue * baseRate * DISCOUNT_RATE;
    }
    
    @Override
    public int getMaxLoans() {
        return 5;
    }
    
    @Override
    public int getLoanDurationDays() {
        return 14;
    }
}
```

```java
public class TeacherPenaltyPolicy implements PenaltyPolicy {
    
    @Override
    public double calculatePenalty(int daysOverdue, double baseRate) {
        return 0; // Enseignants exemptés
    }
    
    @Override
    public int getMaxLoans() {
        return 10;
    }
    
    @Override
    public int getLoanDurationDays() {
        return 30;
    }
}
```

**Étape 3 :** Créer la factory

```java
package com.bibliotech.policy;

import java.util.Map;

public class PenaltyPolicyFactory {
    
    private static final Map<String, PenaltyPolicy> POLICIES = Map.of(
        "STUDENT", new StudentPenaltyPolicy(),
        "TEACHER", new TeacherPenaltyPolicy(),
        "STAFF", new StaffPenaltyPolicy(),
        "EXTERNAL", new ExternalPenaltyPolicy()
    );
    
    private static final PenaltyPolicy DEFAULT = new DefaultPenaltyPolicy();
    
    public static PenaltyPolicy forMemberType(String type) {
        return POLICIES.getOrDefault(type, DEFAULT);
    }
}
```

**Étape 4 :** Refactorer `calculatePenalty()`

```java
public class PenaltyCalculator {
    
    private static final double BASE_RATE = 0.50;
    
    public double calculate(Member member, int daysOverdue) {
        PenaltyPolicy policy = PenaltyPolicyFactory.forMemberType(member.getType());
        return policy.calculatePenalty(daysOverdue, BASE_RATE);
    }
}
```

**📝 Vérification :** Tous les tests de caractérisation doivent toujours passer !

---

## Partie 3 : Sprout Class (30 min)

### Contexte

On vous demande d'ajouter une fonctionnalité : **envoyer des notifications SMS** en plus des emails, mais SANS modifier le code legacy de notification.

### 3.1 Créer le service SMS (Sprout Class)

```java
package com.bibliotech.notification;

public class SmsNotificationService {
    
    private final SmsGateway smsGateway;
    
    public SmsNotificationService(SmsGateway smsGateway) {
        this.smsGateway = smsGateway;
    }
    
    public void sendSms(String phoneNumber, String message) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            return; // Pas de numéro = pas d'envoi
        }
        
        String formattedMessage = truncateIfNeeded(message, 160);
        smsGateway.send(phoneNumber, formattedMessage);
    }
    
    private String truncateIfNeeded(String message, int maxLength) {
        if (message.length() <= maxLength) {
            return message;
        }
        return message.substring(0, maxLength - 3) + "...";
    }
}

// Interface pour le gateway (permet le mock en test)
public interface SmsGateway {
    void send(String phoneNumber, String message);
}
```

### 3.2 Créer l'orchestrateur de notifications

```java
package com.bibliotech.notification;

public class NotificationOrchestrator {
    
    private final LibraryManager legacyManager; // Code legacy
    private final SmsNotificationService smsService; // Nouveau code testé
    
    public NotificationOrchestrator(LibraryManager legacyManager,
                                     SmsNotificationService smsService) {
        this.legacyManager = legacyManager;
        this.smsService = smsService;
    }
    
    public void notifyMember(Member member, String subject, String body) {
        // Appel au code legacy pour l'email
        legacyManager.sendNotificationToMember(member.getEmail(), subject, body);
        
        // Sprout : nouveau code pour le SMS
        if (member.getPhoneNumber() != null) {
            String smsMessage = subject + ": " + summarize(body);
            smsService.sendSms(member.getPhoneNumber(), smsMessage);
        }
    }
    
    private String summarize(String body) {
        // Créer un résumé pour le SMS
        return body.length() > 100 ? body.substring(0, 100) + "..." : body;
    }
}
```

### 3.3 Écrire les tests

```java
package com.bibliotech.notification;

import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;

class SmsNotificationServiceTest {
    
    @Test
    void should_send_sms_when_phone_number_provided() {
        // Arrange
        SmsGateway mockGateway = mock(SmsGateway.class);
        SmsNotificationService service = new SmsNotificationService(mockGateway);
        
        // Act
        service.sendSms("+33612345678", "Votre livre est disponible");
        
        // Assert
        verify(mockGateway).send("+33612345678", "Votre livre est disponible");
    }
    
    @Test
    void should_not_send_sms_when_phone_number_is_null() {
        // TODO: Compléter
    }
    
    @Test
    void should_truncate_message_if_too_long() {
        // TODO: Compléter
    }
}
```

**📝 Livrable :** 
- Nouveau service SMS avec tests complets
- Intégration via orchestrateur
- Code legacy NON modifié

---

## Livrables finaux

À la fin du TP, vous devez avoir :

| # | Livrable | Fichier(s) |
|---|----------|------------|
| 1 | Tests de caractérisation | `CharacterizationTest.java` |
| 2 | Méthode `createLoan()` découpée | `LibraryManager.java` |
| 3 | Classe `MemberValidator` | `MemberValidator.java` |
| 4 | Interface `PenaltyPolicy` | `PenaltyPolicy.java` |
| 5 | Implémentations (4 policies) | `*PenaltyPolicy.java` |
| 6 | Factory | `PenaltyPolicyFactory.java` |
| 7 | Service SMS (Sprout) | `SmsNotificationService.java` |
| 8 | Tous les tests au vert | `mvn test` ✅ |

---

## Barème indicatif

| Partie | Points |
|--------|--------|
| Tests de caractérisation | 5 pts |
| Extract Method | 4 pts |
| Rename | 1 pt |
| Extract Class | 3 pts |
| Replace with Polymorphism | 5 pts |
| Sprout Class | 2 pts |
| **Total** | **20 pts** |

---

## Ressources

- 📖 IntelliJ Refactoring : https://www.jetbrains.com/help/idea/refactoring-source-code.html
- 📖 Refactoring Guru : https://refactoring.guru/refactoring
- 📖 Michael Feathers - Working Effectively with Legacy Code
