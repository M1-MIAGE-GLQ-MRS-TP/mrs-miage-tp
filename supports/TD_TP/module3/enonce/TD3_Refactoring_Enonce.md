# TD 3 — Planifier le Refactoring

## Génie Logiciel et Qualité — M1 MIAGE
**Durée : 1h | Projet : BiblioTech**

---

## Objectifs du TD

- **Identifier** les candidats au refactoring dans du code legacy
- **Planifier** une séquence de refactorings sécurisée
- **Concevoir** des tests de caractérisation
- **Appliquer** les patterns de modernisation (Strangler Fig)

---

## Contexte

Après l'analyse du Module 2, vous avez identifié de nombreux problèmes dans BiblioTech. Avant de refactorer, vous devez **planifier** vos interventions pour minimiser les risques de régression.

---

## Exercice 1 : Identifier les candidats (20 min)

### Diagramme de classes BiblioTech (simplifié)

```
┌─────────────────────────────────────────────────────────────────┐
│                      LibraryManager                              │
├─────────────────────────────────────────────────────────────────┤
│ - books: Map<String, Book>                                       │
│ - members: Map<String, Member>                                   │
│ - loans: Map<String, Loan>                                       │
│ - reservations: Map<String, Reservation>                         │
├─────────────────────────────────────────────────────────────────┤
│ + addBook(), getBook(), searchBooks(), deleteBook()              │
│ + addMember(), getMember(), deleteMember()                       │
│ + createLoan(), returnBook(), renewLoan()                        │
│ + calculatePenalty(member, days)                                 │
│ + createReservation(), cancelReservation()                       │
│ + sendNotification(), sendDueReminders()                         │
│ + generateLoanReport(), generateInventoryReport()                │
└─────────────────────────────────────────────────────────────────┘
            │
            │ utilise
            ▼
┌──────────────────┐  ┌──────────────────┐  ┌──────────────────┐
│      Book        │  │     Member       │  │      Loan        │
├──────────────────┤  ├──────────────────┤  ├──────────────────┤
│ - id             │  │ - id             │  │ - id             │
│ - title          │  │ - firstName      │  │ - memberId       │
│ - author         │  │ - lastName       │  │ - bookId         │
│ - isbn           │  │ - email          │  │ - loanDate       │
│ - copies         │  │ - type           │  │ - dueDate        │
│ - availableCopies│  │ - active         │  │ - status         │
│ - category       │  │ - lateReturns    │  │ - penaltyAmount  │
└──────────────────┘  └──────────────────┘  └──────────────────┘
```

### Méthode `createLoan()` (extrait)

```java
public String createLoan(String memberId, String bookId) {
    Member member = members.get(memberId);
    if (member == null) {
        throw new RuntimeException("Membre non trouvé");
    }
    if (!member.isActive()) {
        throw new RuntimeException("Membre inactif");
    }
    if (member.getMembershipExpiryDate().before(new Date())) {
        throw new RuntimeException("Adhésion expirée");
    }
    
    // Calcul pénalités impayées
    double unpaid = 0;
    for (Loan loan : loans.values()) {
        if (loan.getMemberId().equals(memberId)) {
            unpaid += loan.getPenaltyAmount();
        }
    }
    if (unpaid > 10) {
        throw new RuntimeException("Pénalités impayées");
    }
    
    // Vérification quota
    int active = 0;
    for (Loan loan : loans.values()) {
        if (loan.getMemberId().equals(memberId) && 
            loan.getStatus().equals("ACTIVE")) {
            active++;
        }
    }
    int max = 3;
    if (member.getType().equals("STUDENT")) max = 5;
    else if (member.getType().equals("TEACHER")) max = 10;
    
    if (active >= max) {
        throw new RuntimeException("Quota atteint");
    }
    
    // ... suite : vérification livre, création emprunt ...
}
```

### Méthode `calculatePenalty()`

```java
public double calculatePenalty(Member member, int daysOverdue) {
    String type = member.getType();
    double rate = 0.50;
    
    switch (type) {
        case "STUDENT": return daysOverdue * rate * 0.5;
        case "TEACHER": return 0;
        case "STAFF": return daysOverdue * rate * 0.75;
        case "EXTERNAL": return daysOverdue * rate * 1.5;
        default: return daysOverdue * rate;
    }
}
```

---

### Questions

**1.1 Extract Method (6 points)**

Identifiez **3 blocs de code** dans `createLoan()` qui devraient être extraits en méthodes séparées. Pour chaque bloc :

| # | Lignes concernées | Nom de méthode proposé | Responsabilité |
|---|-------------------|------------------------|----------------|
| 1 | | | |
| 2 | | | |
| 3 | | | |

---

**1.2 Extract Class (4 points)**

Identifiez **2 classes** qui devraient être extraites de `LibraryManager`. Pour chaque classe :

| Nouvelle classe | Méthodes à déplacer | Données à déplacer |
|-----------------|--------------------|--------------------|
| | | |
| | | |

---

**1.3 Replace Conditional with Polymorphism (4 points)**

Pour la méthode `calculatePenalty()` :

a) Dessinez le diagramme de classes après refactoring (interface + implémentations)

b) Écrivez la signature de l'interface :

```java
public interface ______________ {
    // méthode(s)
}
```

c) Listez les classes d'implémentation nécessaires :

---

**1.4 Autres refactorings (6 points)**

Identifiez dans le code ou le diagramme :

| Refactoring | Cible | Justification |
|-------------|-------|---------------|
| Rename | | |
| Move Method | | |
| Introduce Parameter Object | | |

---

## Exercice 2 : Séquence de refactoring (25 min)

### Contexte

Vous devez refactorer la méthode `calculatePenalty()` pour éliminer le switch et utiliser le polymorphisme.

### 2.1 Étapes ordonnées (10 points)

Ordonnez les étapes suivantes (numérotez de 1 à 8) :

| Ordre | Étape |
|-------|-------|
| ___ | Créer l'interface `PenaltyPolicy` |
| ___ | Écrire des tests de caractérisation pour `calculatePenalty()` |
| ___ | Supprimer le switch dans `calculatePenalty()` |
| ___ | Créer `StudentPenaltyPolicy` |
| ___ | Injecter la policy dans le calcul |
| ___ | Créer les autres implémentations (Teacher, Staff, External) |
| ___ | Vérifier que tous les tests passent |
| ___ | Créer une factory ou un registre de policies |

---

### 2.2 Messages de commit (5 points)

Écrivez les messages de commit Git pour chaque étape majeure :

```bash
# Commit 1
git commit -m "___________________________________"

# Commit 2
git commit -m "___________________________________"

# Commit 3
git commit -m "___________________________________"

# Commit 4
git commit -m "___________________________________"

# Commit 5
git commit -m "___________________________________"
```

---

### 2.3 Tests de caractérisation (5 points)

Écrivez **3 tests de caractérisation** pour `calculatePenalty()` :

```java
@Test
void characterization_student_penalty() {
    // TODO : Compléter
}

@Test
void characterization_teacher_penalty() {
    // TODO : Compléter
}

@Test
void characterization_default_penalty() {
    // TODO : Compléter
}
```

**Rappel :** Un test de caractérisation capture le comportement **actuel**, pas le comportement attendu.

---

## Exercice 3 : Stratégie Legacy (15 min)

### Contexte

La classe `DateUtils` contient 200 lignes de code incompréhensible pour calculer des différences de dates. Vous devez la moderniser sans casser le système.

```java
public class DateUtils {
    /**
     * Calcule le nombre de jours entre deux dates.
     * ⚠️ Attention : gestion des années bissextiles approximative !
     */
    public static int daysBetween(String date1, String date2) {
        // 200 lignes de code legacy avec des bugs connus
        // mais dont dépend tout le système...
    }
    
    public static String addDays(String date, int days) {
        // 100 lignes de code legacy
    }
    
    public static boolean isWeekend(String date) {
        // 50 lignes de code legacy
    }
}
```

---

### 3.1 Strangler Fig Pattern (10 points)

Proposez une stratégie de migration en utilisant le pattern **Strangler Fig** :

**Phase 1 :** Créer la nouvelle implémentation

```java
public class ModernDateUtils {
    // Utiliser java.time.*
}
```

**Phase 2 :** Créer la façade de routage

```java
public class DateUtilsFacade {
    private static boolean useModernImplementation = false;
    
    public static int daysBetween(String date1, String date2) {
        // TODO : Comment router vers l'ancienne ou nouvelle implémentation ?
    }
}
```

**Phase 3 :** Migration progressive

Décrivez comment vous allez :
- a) Tester la nouvelle implémentation en parallèle
- b) Basculer progressivement le trafic
- c) Surveiller les différences de comportement
- d) Désactiver l'ancienne implémentation

---

### 3.2 Sprout Class (5 points)

Une nouvelle fonctionnalité est demandée : envoyer des **SMS** en plus des emails pour les rappels.

Expliquez comment utiliser **Sprout Class** pour ajouter cette fonctionnalité sans modifier le code legacy de `sendNotification()`.

```java
// Code legacy qu'on ne veut pas toucher
private void sendNotification(String email, String subject, String body) {
    // Envoi d'email SMTP complexe
}

// Comment ajouter l'envoi SMS avec Sprout Class ?
```

---

## Barème indicatif

| Exercice | Points |
|----------|--------|
| Exercice 1 : Identification | 20 pts |
| Exercice 2 : Séquence | 20 pts |
| Exercice 3 : Stratégie Legacy | 15 pts |
| Clarté et justifications | 5 pts |
| **Total** | **60 pts** (ramené à 20) |

---

## Pour aller plus loin

- 📖 *Refactoring* de Martin Fowler (2ème édition)
- 📖 *Working Effectively with Legacy Code* de Michael Feathers
- 🔗 Refactoring Guru : https://refactoring.guru/refactoring/catalog
