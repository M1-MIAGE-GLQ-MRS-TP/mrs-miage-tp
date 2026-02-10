package fr.amu.univ.miage.m1.glq;

import fr.amu.univ.miage.m1.glq.model.Book;
import fr.amu.univ.miage.m1.glq.model.Member;
import fr.amu.univ.miage.m1.glq.service.LibraryManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests pour le LibraryManager.
 * 
 * ⚠️ CES TESTS SONT INSUFFISANTS ⚠️
 * 
 * Problèmes à corriger :
 * 1. Très faible couverture
 * 2. Tests trop basiques (happy path uniquement)
 * 3. Pas de tests des cas d'erreur
 * 4. Assertions trop faibles
 * 5. Nommage non descriptif
 * 6. Dépendance au singleton (difficile à isoler)
 * 7. Pas de tests paramétrés
 * 8. Données de test en dur
 * 
 * OBJECTIF DU TP : Améliorer ces tests et en ajouter de nouveaux
 */
class LibraryManagerTest {
    
    private LibraryManager manager;
    
    @BeforeEach
    void setUp() {
        // Reset le singleton avant chaque test
        LibraryManager.resetInstance();
        manager = LibraryManager.getInstance();
    }
    
    // ==================== TESTS LIVRES ====================
    
    @Test
    void testAddBook() {
        // Problème : teste seulement que ça ne plante pas
        String id = manager.addBook("Test Book", "Test Author", "1234567890", 2023, 1, "TECHNIQUE");
        assertNotNull(id);
    }
    
    @Test
    void testGetBook() {
        // Problème : dépend des données de test initialisées dans le constructeur
        Book book = manager.getBook("B00001");
        assertNotNull(book);
        assertEquals("Clean Code", book.getTitle());
    }
    
    @Test
    void testSearchBooks() {
        // Problème : assertion trop faible
        var results = manager.searchBooks("Clean");
        assertFalse(results.isEmpty());
    }
    
    // ==================== TESTS MEMBRES ====================
    
    @Test
    void testAddMember() {
        String id = manager.addMember("Test", "User", "test@test.com", "STUDENT");
        assertNotNull(id);
    }
    
    @Test
    void testGetMember() {
        Member member = manager.getMember("M00001");
        assertNotNull(member);
    }
    
    // ==================== TESTS EMPRUNTS ====================
    
    @Test
    void testCreateLoan() {
        // Problème : utilise des IDs en dur qui dépendent de l'ordre d'initialisation
        String loanId = manager.createLoan("M00001", "B00001");
        assertNotNull(loanId);
    }
    
    @Test
    void testCreateLoanMemberNotFound() {
        // Test d'erreur basique
        assertThrows(RuntimeException.class, () -> {
            manager.createLoan("INVALID", "B00001");
        });
    }
    
    @Test
    void testReturnLoan() {
        // Créer un emprunt puis le retourner
        // Problème : le test dépend de l'état global
        String loanId = manager.createLoan("M00002", "B00002");
        manager.returnLoan(loanId);
        
        var loan = manager.getLoan(loanId);
        assertEquals("RETURNED", loan.getStatus());
    }
    
    // ==================== TESTS MANQUANTS ====================
    
    // TODO: Ajouter des tests pour :
    // - Quota d'emprunts atteint
    // - Livre non disponible
    // - Renouvellement d'emprunt
    // - Calcul des pénalités
    // - Réservations
    // - Cas limites (null, chaînes vides, etc.)
    // - Membres inactifs
    // - Adhésions expirées
    // - Différents types de membres
}
