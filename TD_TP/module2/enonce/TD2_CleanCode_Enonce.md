# TD 2 — Clean Code & Analyse Statique

## Génie Logiciel et Qualité — M1 MIAGE
**Durée : 1h | Projet : BiblioTech**

---

## Objectifs du TD

- Identifier les **code smells** dans du code legacy
- Appliquer les **règles de nommage** Clean Code
- Reconnaître les **violations SOLID**
- Interpréter un **rapport SonarQube**

---

## Contexte

Vous intervenez sur le projet **BiblioTech**, un système de gestion de bibliothèque universitaire. Le code a été développé il y a plusieurs années sans respect des bonnes pratiques. Votre mission : diagnostiquer les problèmes avant de procéder au refactoring (Module 3).

---

## Exercice 1 : Chasse aux code smells (20 min)

### Consignes

Analysez les 5 extraits de code suivants. Pour chaque extrait, identifiez :
1. Le(s) **code smell(s)** présent(s)
2. La **règle Clean Code** violée
3. Une **proposition de correction** (en pseudo-code ou description)

---

### Extrait 1 — Variables mystérieuses

```java
public class LibraryManager {
    private static final double p = 0.50;
    private static final int d1 = 14;
    private static final int d2 = 30;
    private static final int d3 = 21;
    
    public double calc(Member m, int d) {
        double r = 0;
        String t = m.getType();
        if (t.equals("STUDENT")) {
            r = d * p * 0.5;
        } else if (t.equals("TEACHER")) {
            r = 0;
        } else {
            r = d * p;
        }
        return r;
    }
}
```

**Questions :**
- a) Listez tous les noms de variables/constantes problématiques
- b) Proposez des noms explicites pour chacun
- c) Quel autre code smell identifiez-vous dans la méthode `calc()` ?

---

### Extrait 2 — Méthode trop longue

```java
public String createLoan(String memberId, String bookId) {
    // Récupérer le membre
    Member member = members.get(memberId);
    if (member == null) {
        throw new RuntimeException("Membre non trouvé : " + memberId);
    }
    
    // Vérifier si le membre est actif
    if (!member.isActive()) {
        throw new RuntimeException("Le membre " + memberId + " n'est pas actif");
    }
    
    // Vérifier si l'adhésion n'est pas expirée
    if (member.getMembershipExpiryDate() != null && 
        member.getMembershipExpiryDate().before(new Date())) {
        throw new RuntimeException("L'adhésion du membre " + memberId + " a expiré");
    }
    
    // Vérifier les pénalités impayées
    double unpaidPenalties = 0;
    for (Loan loan : loans.values()) {
        if (loan.getMemberId().equals(memberId) && loan.getPenaltyAmount() > 0) {
            unpaidPenalties += loan.getPenaltyAmount();
        }
    }
    if (unpaidPenalties > 10) {
        throw new RuntimeException("Pénalités impayées trop élevées : " + unpaidPenalties + "€");
    }
    
    // Vérifier le quota d'emprunts
    int activeLoans = 0;
    for (Loan loan : loans.values()) {
        if (loan.getMemberId().equals(memberId) && 
            (loan.getStatus().equals("ACTIVE") || loan.getStatus().equals("OVERDUE"))) {
            activeLoans++;
        }
    }
    int maxLoans = 3; // Par défaut
    if (member.getType().equals("STUDENT")) { maxLoans = 5; }
    else if (member.getType().equals("TEACHER")) { maxLoans = 10; }
    else if (member.getType().equals("STAFF")) { maxLoans = 7; }
    
    if (activeLoans >= maxLoans) {
        throw new RuntimeException("Quota d'emprunts atteint");
    }
    
    // ... encore 50 lignes de code ...
}
```

**Questions :**
- a) Combien de responsabilités distinctes identifiez-vous ?
- b) Proposez un découpage en méthodes avec des noms explicites
- c) Quel principe SOLID est violé ?

---

### Extrait 3 — God Class

```java
public class LibraryManager {
    // Gestion des livres
    public String addBook(...) { }
    public Book getBook(String id) { }
    public List<Book> searchBooks(String query) { }
    public void updateBook(Book book) { }
    public void deleteBook(String id) { }
    
    // Gestion des membres
    public String addMember(...) { }
    public Member getMember(String id) { }
    public void updateMember(Member member) { }
    public void deleteMember(String id) { }
    
    // Gestion des emprunts
    public String createLoan(...) { }
    public void returnBook(...) { }
    public void renewLoan(...) { }
    
    // Calcul des pénalités
    public double calculatePenalty(...) { }
    
    // Notifications
    private void sendNotification(...) { }
    public void sendDueReminders() { }
    public void sendOverdueNotifications() { }
    
    // Rapports
    public String generateLoanReport() { }
    public String generateInventoryReport() { }
    
    // Réservations
    public String createReservation(...) { }
    public void cancelReservation(...) { }
}
```

**Questions :**
- a) Combien de responsabilités différentes cette classe gère-t-elle ?
- b) Proposez un découpage en classes distinctes (noms uniquement)
- c) Pour chaque nouvelle classe, indiquez sa responsabilité unique

---

### Extrait 4 — Switch sur le type

```java
public double calculatePenalty(Member member, int daysOverdue) {
    String type = member.getType();
    double rate = PENALTY_RATE_PER_DAY;
    
    switch (type) {
        case "STUDENT":
            return daysOverdue * rate * 0.5;
        case "TEACHER":
            return 0;
        case "STAFF":
            return daysOverdue * rate * 0.75;
        case "EXTERNAL":
            return daysOverdue * rate * 1.5;
        default:
            return daysOverdue * rate;
    }
}

public int getMaxLoans(Member member) {
    switch (member.getType()) {
        case "STUDENT": return 5;
        case "TEACHER": return 10;
        case "STAFF": return 7;
        case "EXTERNAL": return 3;
        default: return 3;
    }
}

public int getLoanDuration(Member member) {
    switch (member.getType()) {
        case "STUDENT": return 14;
        case "TEACHER": return 30;
        case "STAFF": return 21;
        case "EXTERNAL": return 14;
        default: return 21;
    }
}
```

**Questions :**
- a) Quel problème pose cette approche si on ajoute un nouveau type de membre ?
- b) Quel principe SOLID est violé ?
- c) Quelle solution orientée objet permettrait d'éliminer ces switch ?

---

### Extrait 5 — Commentaires compensatoires

```java
// Vérifie si le membre peut emprunter un livre
public boolean chk(String mId, String bId) {
    // Récupérer le membre
    Member m = members.get(mId);
    // Vérifier que le membre existe
    if (m == null) return false;
    // Vérifier que le membre est actif
    if (!m.isActive()) return false;
    // Récupérer le livre
    Book b = books.get(bId);
    // Vérifier que le livre existe
    if (b == null) return false;
    // Vérifier que le livre est disponible
    if (b.getAvailableCopies() <= 0) return false;
    // Tout est OK
    return true;
}
```

**Questions :**
- a) Pourquoi ces commentaires sont-ils un code smell ?
- b) Réécrivez cette méthode sans commentaires, en utilisant des noms explicites
- c) Combien de lignes de commentaires deviennent inutiles ?

---

## Exercice 2 : Réécriture Clean Code (25 min)

### Consignes

Réécrivez la méthode `calculatePenalty()` suivante en appliquant les principes Clean Code :

```java
// Calcule la pénalité de retard pour un emprunt
public double calculatePenalty(Member member, int daysOverdue) {
    double p = 0;
    String t = member.getType();
    
    // Appliquer le taux selon le type
    if (t.equals("STUDENT")) {
        // Les étudiants paient 50% du tarif normal
        p = daysOverdue * 0.50 * 0.5;
    } else if (t.equals("TEACHER")) {
        // Les enseignants ne paient pas de pénalité
        p = 0;
    } else if (t.equals("STAFF")) {
        // Le personnel paie 75% du tarif
        p = daysOverdue * 0.50 * 0.75;
    } else if (t.equals("EXTERNAL")) {
        // Les externes paient 150% du tarif
        p = daysOverdue * 0.50 * 1.5;
    } else {
        // Tarif par défaut
        p = daysOverdue * 0.50;
    }
    
    // Appliquer le plafond de 50€
    if (p > 50) {
        p = 50;
    }
    
    // Arrondir à 2 décimales
    p = Math.round(p * 100.0) / 100.0;
    
    return p;
}
```

### Critères à respecter

1. **Nommage** : Variables et constantes explicites
2. **Fonctions** : Extraire les sous-responsabilités
3. **Lisibilité** : Supprimer les commentaires inutiles
4. **DRY** : Éliminer la duplication du taux de base

### Format de réponse

Écrivez votre solution sur papier ou pseudo-code. Indiquez :
- Les constantes extraites
- Les méthodes extraites
- Le code final de `calculatePenalty()`

---

## Exercice 3 : Lecture rapport SonarQube (15 min)

### Rapport SonarQube (extrait)

```
════════════════════════════════════════════════════════════════
                    SONARQUBE ANALYSIS REPORT
                    Project: BiblioTech
                    Date: 2024-01-15
════════════════════════════════════════════════════════════════

QUALITY GATE: ❌ FAILED

────────────────────────────────────────────────────────────────
METRICS SUMMARY
────────────────────────────────────────────────────────────────
Bugs:                  12 (8 Critical, 4 Major)
Vulnerabilities:       3 (1 Critical, 2 Major)  
Code Smells:          47 (12 Critical, 23 Major, 12 Minor)
Coverage:             23.4% (Target: 80%)
Duplications:         8.7% (Target: 3%)
Technical Debt:       4d 2h

────────────────────────────────────────────────────────────────
TOP ISSUES
────────────────────────────────────────────────────────────────

[CRITICAL BUG] LibraryManager.java:245
  "Null pointer dereference; 'member' could be null"
  
[CRITICAL BUG] LibraryManager.java:312
  "Possible SQL injection in query parameter"

[CRITICAL VULNERABILITY] DatabaseConnection.java:45
  "Hardcoded password in source code"

[CRITICAL CODE SMELL] LibraryManager.java
  "Class has 47 methods, exceeds maximum of 20"
  
[CRITICAL CODE SMELL] LibraryManager.java:201
  "Method 'createLoan' has 127 lines, exceeds maximum of 30"

[MAJOR CODE SMELL] Multiple files
  "12 instances of duplicated code blocks detected"
  
[MAJOR CODE SMELL] LibraryManager.java:489
  "Cyclomatic complexity of 23, exceeds maximum of 10"

────────────────────────────────────────────────────────────────
DUPLICATION HOTSPOTS
────────────────────────────────────────────────────────────────
LibraryManager.java:120-145  ↔  LibraryManager.java:380-405
LibraryManager.java:230-242  ↔  LibraryManager.java:350-362
Member.java:45-67            ↔  Book.java:52-74

────────────────────────────────────────────────────────────────
```

### Questions

**a) Priorisation (5 min)**

Classez les 5 problèmes suivants par ordre de priorité de correction (1 = le plus urgent) :

- [ ] Duplication de code (8.7%)
- [ ] Couverture de tests (23.4%)
- [ ] Mot de passe en dur dans le code
- [ ] Méthode de 127 lignes
- [ ] Complexité cyclomatique de 23

Justifiez votre classement.

**b) Plan d'action (5 min)**

Pour chaque catégorie, proposez une action corrective :

| Problème | Action corrective |
|----------|-------------------|
| Null pointer dereference | |
| SQL injection | |
| Hardcoded password | |
| God Class (47 méthodes) | |
| Méthode trop longue | |

**c) Quality Gate (5 min)**

Le projet échoue à la Quality Gate. Quelles métriques doivent être améliorées en priorité pour passer ? Proposez des seuils réalistes à atteindre pour un premier sprint de refactoring.

---

## Barème indicatif

| Exercice | Points |
|----------|--------|
| Exercice 1 : Code smells | 8 pts |
| Exercice 2 : Réécriture | 8 pts |
| Exercice 3 : SonarQube | 4 pts |
| **Total** | **20 pts** |

---

## Pour aller plus loin

- 📖 *Clean Code* de Robert C. Martin (Chapitres 2, 3, 4)
- 📖 *Refactoring* de Martin Fowler (Catalogue des code smells)
- 🔗 Documentation SonarQube : rules.sonarsource.com
