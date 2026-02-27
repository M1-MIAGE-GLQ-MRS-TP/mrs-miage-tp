# TD 3 — Solutions

## Exercice 1 : Identifier les candidats

### 1.1 Extract Method

| # | Lignes | Nom de méthode | Responsabilité |
|---|--------|----------------|----------------|
| 1 | 2-8 | `findMemberOrThrow(memberId)` | Récupérer le membre ou lever une exception |
| 2 | 10-22 | `calculateUnpaidPenalties(memberId)` | Calculer le total des pénalités impayées |
| 3 | 24-35 | `countActiveLoansForMember(memberId)` | Compter les emprunts actifs du membre |
| 4 | 36-40 | `getMaxLoansForMemberType(type)` | Déterminer le quota selon le type |
| 5 | 9 | `ensureMemberIsActive(member)` | Vérifier que le membre est actif |

### 1.2 Extract Class

| Nouvelle classe | Méthodes à déplacer | Données |
|-----------------|---------------------|---------|
| `BookService` | addBook, getBook, searchBooks, updateBook, deleteBook | Map<String, Book> books |
| `MemberService` | addMember, getMember, getMemberByEmail, updateMember, deleteMember | Map<String, Member> members |
| `LoanService` | createLoan, returnBook, renewLoan | Map<String, Loan> loans |
| `PenaltyCalculator` | calculatePenalty | PENALTY_RATE_PER_DAY |
| `NotificationService` | sendNotification, sendDueReminders, sendOverdueNotifications | - |
| `ReportGenerator` | generateLoanReport, generateInventoryReport | - |

### 1.3 Replace Conditional with Polymorphism

**a) Diagramme de classes :**

```
                    ┌─────────────────────┐
                    │   «interface»       │
                    │   PenaltyPolicy     │
                    ├─────────────────────┤
                    │ +calculate(days,    │
                    │           rate)     │
                    │ +getMaxLoans()      │
                    │ +getLoanDuration()  │
                    └─────────────────────┘
                              ▲
          ┌───────────────────┼───────────────────┐
          │                   │                   │
┌─────────┴─────────┐ ┌───────┴───────┐ ┌─────────┴─────────┐
│ StudentPenalty    │ │ TeacherPenalty│ │ StaffPenaltyPolicy│
│ Policy            │ │ Policy        │ │                   │
├───────────────────┤ ├───────────────┤ ├───────────────────┤
│ calculate():      │ │ calculate():  │ │ calculate():      │
│  days*rate*0.5    │ │  return 0     │ │  days*rate*0.75   │
│ maxLoans: 5       │ │ maxLoans: 10  │ │ maxLoans: 7       │
│ duration: 14      │ │ duration: 30  │ │ duration: 21      │
└───────────────────┘ └───────────────┘ └───────────────────┘
```

**b) Signature de l'interface :**

```java
public interface PenaltyPolicy {
    double calculatePenalty(int daysOverdue, double baseRate);
    int getMaxLoans();
    int getLoanDurationDays();
}
```

**c) Classes d'implémentation :**
- `StudentPenaltyPolicy`
- `TeacherPenaltyPolicy`
- `StaffPenaltyPolicy`
- `ExternalPenaltyPolicy`
- `DefaultPenaltyPolicy`

### 1.4 Autres refactorings

| Refactoring | Cible | Justification |
|-------------|-------|---------------|
| **Rename** | Variable `m` → `member` | Clarté, convention |
| **Rename** | Constante `p` → `PENALTY_RATE_PER_DAY` | Révèle l'intention |
| **Move Method** | `calculatePenalty()` vers `PenaltyCalculator` | Cohésion, SRP |
| **Move Method** | `sendNotification()` vers `NotificationService` | SRP |
| **Introduce Parameter Object** | Paramètres de `addMember()` → `MemberRegistration` | Trop de paramètres (>3) |
| **Replace Magic Number** | `10` (seuil pénalités) → `MAX_UNPAID_PENALTIES` | Lisibilité |

---

## Exercice 2 : Séquence de refactoring

### 2.1 Étapes ordonnées

| Ordre | Étape |
|-------|-------|
| **1** | Écrire des tests de caractérisation pour `calculatePenalty()` |
| **2** | Créer l'interface `PenaltyPolicy` |
| **3** | Créer `StudentPenaltyPolicy` |
| **4** | Créer les autres implémentations (Teacher, Staff, External) |
| **5** | Créer une factory ou un registre de policies |
| **6** | Injecter la policy dans le calcul |
| **7** | Supprimer le switch dans `calculatePenalty()` |
| **8** | Vérifier que tous les tests passent |

**Principe clé :** Toujours commencer par les tests, finir par la suppression du code legacy.

### 2.2 Messages de commit

```bash
# Commit 1
git commit -m "Test: Add characterization tests for calculatePenalty"

# Commit 2
git commit -m "Refactor: Create PenaltyPolicy interface"

# Commit 3
git commit -m "Refactor: Implement StudentPenaltyPolicy, TeacherPenaltyPolicy, StaffPenaltyPolicy, ExternalPenaltyPolicy"

# Commit 4
git commit -m "Refactor: Add PenaltyPolicyFactory to resolve policy by member type"

# Commit 5
git commit -m "Refactor: Replace switch in calculatePenalty with polymorphic dispatch"
```

### 2.3 Tests de caractérisation

```java
@Test
void characterization_student_penalty() {
    // Arrange
    Member student = new Member("M001", "Alice", "Test", "a@test.com", "STUDENT");
    LibraryManager manager = LibraryManager.getInstance();
    
    // Act
    double penalty = manager.calculatePenalty(student, 10);
    
    // Assert - Valeur observée : 10 * 0.50 * 0.5 = 2.50
    assertThat(penalty).isEqualTo(2.50);
}

@Test
void characterization_teacher_penalty() {
    // Arrange
    Member teacher = new Member("M002", "Bob", "Test", "b@test.com", "TEACHER");
    LibraryManager manager = LibraryManager.getInstance();
    
    // Act
    double penalty = manager.calculatePenalty(teacher, 30);
    
    // Assert - Les enseignants ne paient pas : 0
    assertThat(penalty).isEqualTo(0.0);
}

@Test
void characterization_default_penalty() {
    // Arrange
    Member unknown = new Member("M003", "Eve", "Test", "e@test.com", "UNKNOWN");
    LibraryManager manager = LibraryManager.getInstance();
    
    // Act
    double penalty = manager.calculatePenalty(unknown, 10);
    
    // Assert - Tarif par défaut : 10 * 0.50 = 5.00
    assertThat(penalty).isEqualTo(5.00);
}
```

---

## Exercice 3 : Stratégie Legacy

### 3.1 Strangler Fig Pattern

**Phase 1 - Nouvelle implémentation :**

```java
public class ModernDateUtils {
    
    public static long daysBetween(String date1, String date2) {
        LocalDate d1 = LocalDate.parse(date1);
        LocalDate d2 = LocalDate.parse(date2);
        return ChronoUnit.DAYS.between(d1, d2);
    }
    
    public static String addDays(String date, int days) {
        LocalDate d = LocalDate.parse(date);
        return d.plusDays(days).toString();
    }
    
    public static boolean isWeekend(String date) {
        LocalDate d = LocalDate.parse(date);
        DayOfWeek day = d.getDayOfWeek();
        return day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
    }
}
```

**Phase 2 - Façade de routage :**

```java
public class DateUtilsFacade {
    
    // Feature flag (peut être externalisé dans config)
    private static boolean useModernImplementation = false;
    
    // Compteur pour A/B testing
    private static final AtomicLong legacyCalls = new AtomicLong();
    private static final AtomicLong modernCalls = new AtomicLong();
    
    public static int daysBetween(String date1, String date2) {
        if (useModernImplementation) {
            modernCalls.incrementAndGet();
            return (int) ModernDateUtils.daysBetween(date1, date2);
        } else {
            legacyCalls.incrementAndGet();
            return DateUtils.daysBetween(date1, date2);
        }
    }
    
    // Mode comparaison pour valider
    public static int daysBetweenWithComparison(String date1, String date2) {
        int legacyResult = DateUtils.daysBetween(date1, date2);
        int modernResult = (int) ModernDateUtils.daysBetween(date1, date2);
        
        if (legacyResult != modernResult) {
            log.warn("DIFF: legacy={}, modern={} for ({}, {})", 
                     legacyResult, modernResult, date1, date2);
        }
        
        return legacyResult; // Retourner legacy pendant la transition
    }
    
    public static void enableModernImplementation() {
        useModernImplementation = true;
    }
}
```

**Phase 3 - Migration progressive :**

a) **Test en parallèle** : Utiliser `daysBetweenWithComparison()` en production pendant 1-2 semaines, logger les différences

b) **Basculement progressif** : 
   - Semaine 1 : 10% du trafic vers moderne
   - Semaine 2 : 50% du trafic
   - Semaine 3 : 100% si pas d'erreur

c) **Surveillance** :
   - Monitorer les exceptions
   - Comparer les métriques business
   - Alerter si différence > 0.1%

d) **Désactivation legacy** :
   - Supprimer le flag après 1 mois stable
   - Supprimer `DateUtils` après 3 mois
   - Documenter la migration

### 3.2 Sprout Class pour SMS

```java
// Code legacy NON MODIFIÉ
private void sendNotification(String email, String subject, String body) {
    // Code SMTP legacy - on ne touche pas !
}

// NOUVEAU : Sprout Class pour SMS
public class SmsNotificationService {
    private final SmsGateway gateway;
    
    public SmsNotificationService(SmsGateway gateway) {
        this.gateway = gateway;
    }
    
    public void sendReminder(Member member, String message) {
        if (member.getPhoneNumber() == null) return;
        
        String truncated = message.length() > 160 
            ? message.substring(0, 157) + "..." 
            : message;
            
        gateway.send(member.getPhoneNumber(), truncated);
    }
}

// NOUVEAU : Orchestrateur qui appelle les deux
public class NotificationOrchestrator {
    private final LibraryManager legacy;
    private final SmsNotificationService smsService;
    
    public void notifyMember(Member member, String subject, String body) {
        // Appel legacy (email)
        legacy.sendNotificationPublic(member.getEmail(), subject, body);
        
        // Sprout (SMS)
        smsService.sendReminder(member, subject + ": " + body);
    }
}
```

**Avantages :**
- ✅ Code legacy non modifié (risque minimal)
- ✅ Nouveau code 100% testé
- ✅ Séparation claire des responsabilités
- ✅ Facile à désactiver si problème
