# Module 4 — Design Patterns
# Travaux Dirigés : Conception et Analyse

**Génie Logiciel et Qualité — M1 MIAGE**

| Durée | Objectifs | Prérequis |
|-------|-----------|-----------|
| 1h | Reconnaître et concevoir des patterns | CM Module 4, Module 3 (Refactoring) |

---

## Introduction

Ce TD vous prépare à l'application pratique des Design Patterns sur le projet **BiblioTech**. Vous allez analyser le code legacy, identifier les opportunités d'amélioration, et concevoir des solutions basées sur les patterns GoF.

### Contexte : Le code legacy BiblioTech

Le fichier `LibraryManager.java` (781 lignes) est une **God Class** qui cumule les responsabilités :
- Gestion des livres, membres, emprunts, réservations
- Calcul des pénalités
- Envoi de notifications
- Génération de rapports

**Extrait problématique — Calcul des quotas :**

```java
// Dans LibraryManager.createLoan() - lignes 219-229
int quota;
if (member.getMemberType().equals("STUDENT")) {
    quota = 3;
} else if (member.getMemberType().equals("TEACHER")) {
    quota = 10;
} else if (member.getMemberType().equals("STAFF")) {
    quota = 5;
} else {
    quota = 2;  // EXTERNAL
}
```

**Extrait problématique — Calcul des pénalités :**

```java
// Dans LibraryManager.calculatePenalty() - lignes 384-410
double rate;
double maxPenalty;

if (member.getMemberType().equals("STUDENT")) {
    rate = 0.25;
    maxPenalty = 10.0;
} else if (member.getMemberType().equals("TEACHER")) {
    rate = 0.0;
    maxPenalty = 0.0;
} else if (member.getMemberType().equals("STAFF")) {
    rate = 0.25;
    maxPenalty = 15.0;
} else {
    rate = 0.50;
    maxPenalty = 25.0;
}
```

> **Question préliminaire :** Quel principe SOLID est violé par ces structures conditionnelles répétées ?

---

## Exercice 1 — Reconnaissance de patterns (20 min)

### 1.1 Analyse du Singleton existant

Observez l'implémentation actuelle du Singleton dans `LibraryManager` :

```java
// LibraryManager.java - lignes 46-85
public class LibraryManager {
    
    private static LibraryManager instance;
    
    private Map<String, Book> books = new HashMap<>();
    private Map<String, Member> members = new HashMap<>();
    private Map<String, Loan> loans = new HashMap<>();
    
    private LibraryManager() {
        initTestData();
    }
    
    public static LibraryManager getInstance() {
        if (instance == null) {
            instance = new LibraryManager();
        }
        return instance;
    }
    
    public static void resetInstance() {
        instance = null;
    }
}
```

**Questions :**

**a)** Identifiez **3 problèmes** avec cette implémentation du Singleton :

| Problème | Description                   |
|----------|-------------------------------|
| 1. | Thread safety (Race condition) |
| 2. | SRP & Testabilité             |
| 3. | Etat partagé                  |

**b)** Cette classe est-elle un bon candidat pour le pattern Singleton ? Justifiez.

```
Réponse :
Testabilité réduite (mutabilité)
Concept métier____________________________________________________________
OCP ____________________________________________________________
____________________________________________________________
```

**c)** Proposez une alternative au Singleton pour gérer les dépendances :

```
Réponse :
DIP____________________________________________________________
```

---

### 1.2 Identification des patterns manquants

Analysez les extraits de code suivants et identifiez le pattern le plus approprié pour les améliorer :

**Extrait A — Notifications**

```java
// LibraryManager.java - lignes 579-586
private void sendNotification(String email, String subject, String body) {
    System.out.println("=== EMAIL ===");
    System.out.println("To: " + email);
    System.out.println("Subject: " + subject);
    System.out.println("Body: " + body);
    System.out.println("=============");
}
```

Problème : Comment ajouter SMS, notifications push, Slack sans modifier cette méthode ?

**Pattern approprié :** ____________________

**Justification :**
```
____________________________________________________________
____________________________________________________________
```

---

**Extrait B — Gestion des statuts d'emprunt**

```java
// Dispersé dans LibraryManager.java
if (loan.getStatus().equals("ACTIVE")) {
    // Logique pour emprunt actif
} else if (loan.getStatus().equals("OVERDUE")) {
    // Logique pour emprunt en retard
} else if (loan.getStatus().equals("RETURNED")) {
    // Logique pour emprunt retourné
}
```

Problème : Les conditions sur le statut sont dispersées dans plusieurs méthodes.

**Pattern approprié :** ____________________

**Justification :**
```
____________________________________________________________
____________________________________________________________
```

---

**Extrait C — Notification de disponibilité**

```java
// LibraryManager.java - lignes 556-571
private void notifyNextReservation(String bookId) {
    // Trouver la prochaine réservation en attente
    Reservation nextReservation = null;
    for (Reservation res : reservations.values()) {
        if (res.getBookId().equals(bookId) && res.getStatus().equals("PENDING")) {
            if (nextReservation == null || 
                res.getReservationDate().before(nextReservation.getReservationDate())) {
                nextReservation = res;
            }
        }
    }
    
    if (nextReservation != null) {
        Member member = members.get(nextReservation.getMemberId());
        Book book = books.get(bookId);
        sendNotification(member.getEmail(), "Livre disponible",
            "Le livre que vous avez réservé est disponible : " + book.getTitle());
    }
}
```

Problème : Comment ajouter d'autres actions quand un livre devient disponible (statistiques, logs, etc.) ?

**Pattern approprié :** ____________________

**Justification :**
```
____________________________________________________________
____________________________________________________________
```

---

### 1.3 Association Pattern ↔ Problème

Associez chaque problème BiblioTech au pattern le plus approprié :

| # | Problème dans BiblioTech | | Pattern |
|---|--------------------------|---|---------|
| 1 | Calcul de pénalité différent selon le type de membre | | A. Builder |
| 2 | Notifications multi-canaux combinables (email + SMS + push) | | B. Factory Method |
| 3 | Création d'un `Loan` avec de nombreux paramètres | | C. Strategy |
| 4 | États d'un emprunt avec transitions (ACTIVE → OVERDUE → RETURNED) | | D. Observer |
| 5 | Alerter plusieurs composants quand un livre devient disponible | | E. Decorator |
| 6 | Créer différents types de rapports (PDF, Excel, texte) | | F. State |

**Réponses :** 

| Problème | Pattern |
|----------|---------|
| 1 | |
| 2 | |
| 3 | |
| 4 | |
| 5 | |
| 6 | |

---

## Exercice 2 — Conception Strategy : LoanPolicy (15 min)

### Contexte

Les règles d'emprunt varient selon le type de membre :

| Type | Quota | Durée | Renouvellements | Taux pénalité | Plafond |
|------|-------|-------|-----------------|---------------|---------|
| STUDENT | 3 | 14 jours | 2 | 0.25€/jour | 10€ |
| TEACHER | 10 | 30 jours | illimité | 0€ | 0€ |
| STAFF | 5 | 21 jours | 2 | 0.25€/jour | 15€ |
| EXTERNAL | 2 | 7 jours | 0 | 0.50€/jour | 25€ |

### 2.1 Diagramme de classes UML

Dessinez le diagramme de classes pour implémenter le pattern **Strategy** :

```
┌─────────────────────────────────────────────────────────────────┐
│                                                                 │
│                                                                 │
│                                                                 │
│                                                                 │
│                    [VOTRE DIAGRAMME ICI]                        │
│                                                                 │
│                                                                 │
│                                                                 │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

**Éléments attendus :**
- [ ] Interface `LoanPolicy` avec ses méthodes
- [ ] 4 classes concrètes (StudentLoanPolicy, TeacherLoanPolicy, etc.)
- [ ] Classe contexte qui utilise la stratégie
- [ ] Relations (implémentation, association)

### 2.2 Définition de l'interface

Complétez l'interface `LoanPolicy` avec les méthodes nécessaires :

```java
public interface LoanPolicy {
    
    /**
     * Retourne le quota maximum d'emprunts simultanés.
     */
    _______________ getMaxLoans();
    
    /**
     * Retourne la durée d'emprunt en jours.
     */
    _______________ getLoanDurationDays();
    
    /**
     * Retourne le nombre maximum de renouvellements (-1 = illimité).
     */
    _______________ getMaxRenewals();
    
    /**
     * Calcule la pénalité pour un nombre de jours de retard.
     */
    _______________ calculatePenalty(int daysOverdue);
    
    /**
     * Vérifie si un membre peut emprunter.
     */
    _______________ canBorrow(Member member);
}
```

### 2.3 Respect de l'OCP

Expliquez comment ce design respecte l'**Open/Closed Principle** :

```
____________________________________________________________
____________________________________________________________
____________________________________________________________
____________________________________________________________
```

**Scénario :** On doit ajouter un nouveau type de membre `SENIOR` avec ses propres règles. Quelles modifications sont nécessaires ?

```
____________________________________________________________
____________________________________________________________
```

---

## Exercice 3 — Conception Observer : Disponibilité des livres (15 min)

### Contexte

Quand un livre réservé devient disponible (après un retour), plusieurs actions doivent se produire :
1. Notifier le premier membre de la file de réservation
2. Mettre à jour les statistiques de circulation
3. Logger l'événement pour audit
4. (Futur) Envoyer des analytics

### 3.1 Identification des rôles

Complétez le tableau :

| Rôle | Élément dans BiblioTech |
|------|-------------------------|
| **Subject** (Observable) | |
| **Observer 1** | |
| **Observer 2** | |
| **Observer 3** | |
| **Événement** | |

### 3.2 Diagramme de classes

Dessinez le diagramme de classes pour le pattern Observer :

```
┌─────────────────────────────────────────────────────────────────┐
│                                                                 │
│                                                                 │
│                                                                 │
│                    [VOTRE DIAGRAMME ICI]                        │
│                                                                 │
│                                                                 │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### 3.3 Séquence d'événements

Décrivez la séquence quand `returnLoan()` est appelé et qu'une réservation existe :

```
1. LoanService.returnLoan(loanId)
2. _______________________________________________
3. _______________________________________________
4. _______________________________________________
5. _______________________________________________
```

### 3.4 Avantages

Listez **3 avantages** de cette approche par rapport au code actuel :

| # | Avantage |
|---|----------|
| 1 | |
| 2 | |
| 3 | |

---

## Exercice 4 — Conception State : États d'un emprunt (10 min)

### Contexte

Un emprunt (`Loan`) traverse plusieurs états avec des règles de transition :

- **ACTIVE** : L'emprunt est en cours
- **OVERDUE** : La date de retour est dépassée
- **RETURNED** : Le livre a été rendu

### 4.1 Diagramme d'états

Complétez le diagramme d'états UML :

```
                    createLoan()
                         │
                         ▼
                   ┌──────────┐
                   │          │
                   │  ACTIVE  │
                   │          │
                   └────┬─────┘
                        │
          ┌─────────────┼─────────────┐
          │             │             │
     [_________]   [_________]   [_________]
          │             │             │
          ▼             ▼             ▼
    ┌──────────┐  ┌──────────┐  ┌──────────┐
    │          │  │          │  │          │
    │          │  │          │  │          │
    │          │  │          │  │          │
    └──────────┘  └──────────┘  └──────────┘
```

**Conditions de transition à compléter :**
- ACTIVE → _________ : _________________________________
- ACTIVE → _________ : _________________________________
- OVERDUE → _________ : ________________________________

### 4.2 Matrice des comportements

Complétez la matrice des comportements par état :

| État | `renew()` | `returnBook()` | `checkOverdue()` |
|------|-----------|----------------|------------------|
| **ACTIVE** | | | |
| **OVERDUE** | | | |
| **RETURNED** | | | |

**Légende :**
- ✓ = Action autorisée (décrire l'effet)
- ✗ = Action interdite (lance exception)
- — = Sans effet

### 4.3 Avantage du pattern State

Pourquoi le pattern State est-il préférable à un switch/if sur le statut ?

```
____________________________________________________________
____________________________________________________________
____________________________________________________________
```

---

## Synthèse

### Récapitulatif des patterns identifiés pour BiblioTech

| Problème | Pattern | Bénéfice principal |
|----------|---------|-------------------|
| Règles d'emprunt par type | Strategy | |
| Notifications multi-canaux | Decorator | |
| États d'un emprunt | State | |
| Disponibilité des livres | Observer | |
| Création de Loan complexe | Builder | |

### Checklist de préparation au TP

Avant le TP, assurez-vous de :

- [ ] Comprendre l'interface `LoanPolicy` et ses implémentations
- [ ] Savoir dessiner le diagramme de classes du pattern Decorator
- [ ] Comprendre les transitions d'état d'un emprunt
- [ ] Identifier le Subject et les Observers pour les notifications

---

## Annexe : Code de référence BiblioTech

### Structure des classes principales

```java
// Member.java - Champs principaux
public class Member {
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String memberType;  // "STUDENT", "TEACHER", "STAFF", "EXTERNAL"
    private boolean isActive;
    private int currentLoansCount;
    private int lateReturnsCount;
    private Date membershipExpiryDate;
}

// Book.java - Champs principaux
public class Book {
    private String id;
    private String title;
    private String author;
    private String isbn;
    private int copies;
    private int availableCopies;
    private boolean isActive;
}

// Loan.java - Champs principaux
public class Loan {
    private String id;
    private Member member;
    private Book book;
    private Date loanDate;
    private Date dueDate;
    private Date returnDate;
    private String status;  // "ACTIVE", "OVERDUE", "RETURNED"
    private int renewalCount;
    private double penaltyAmount;
}

// Reservation.java - Champs principaux
public class Reservation {
    private String id;
    private String memberId;
    private String bookId;
    private Date reservationDate;
    private String status;  // "PENDING", "FULFILLED", "CANCELLED"
}
```

---

**Fin du TD — Préparez-vous pour le TP !**
