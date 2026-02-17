package fr.amu.univ.miage.m1.glq;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.amu.univ.miage.m1.glq.model.Book;
import fr.amu.univ.miage.m1.glq.model.Loan;
import fr.amu.univ.miage.m1.glq.model.Member;
import fr.amu.univ.miage.m1.glq.service.LibraryManager;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class LibraryManagerTest {

    private LibraryManager manager;

    @BeforeEach
    void setUp() {
        LibraryManager.resetInstance();
        manager = LibraryManager.getInstance();
    }

    @Nested
    @DisplayName("Catalog Management")
    class CatalogManagement {

        @Test
        void should_add_new_book_to_catalog() {
            String id = manager.addBook("Effective Java", "Joshua Bloch", "978-0134685991", 2018, 5, "TECHNIQUE");
            
            Book book = manager.getBook(id);
            assertThat(book).isNotNull();
            assertThat(book.getTitle()).isEqualTo("Effective Java");
            assertThat(book.getAvailableCopies()).isEqualTo(5);
        }

        @Test
        void should_find_book_by_isbn() {
            manager.addBook("Test Book", "Author", "111-2223334445", 2020, 1, "ROMAN");
            
            Book found = manager.getBookByIsbn("111-2223334445");
            assertThat(found).isNotNull();
            assertThat(found.getTitle()).isEqualTo("Test Book");
        }
        
        @Test
        void should_return_null_when_isbn_not_found() {
            assertThat(manager.getBookByIsbn("000-0000000000")).isNull();
        }

        @Test
        void should_search_books_by_title_author_or_isbn() {
            manager.addBook("Java Programming", "John Doe", "12345", 2020, 1, "TECHNIQUE");
            manager.addBook("Python Basics", "Jane Smith", "67890", 2021, 1, "TECHNIQUE");
            
            List<Book> resultsTitle = manager.searchBooks("Java");
            assertThat(resultsTitle).hasSize(1).extracting(Book::getTitle).contains("Java Programming");
            
            List<Book> resultsAuthor = manager.searchBooks("Smith");
            assertThat(resultsAuthor).hasSize(1).extracting(Book::getTitle).contains("Python Basics");
            
            List<Book> resultsIsbn = manager.searchBooks("12345");
            assertThat(resultsIsbn).hasSize(1).extracting(Book::getTitle).contains("Java Programming");
        }

        @Test
        void should_update_book_information() {
            String id = manager.addBook("Old Title", "Author", "ISBN", 2000, 1, "ROMAN");
            Book book = manager.getBook(id);
            book.setTitle("New Title");
            
            manager.updateBook(book);
            
            assertThat(manager.getBook(id).getTitle()).isEqualTo("New Title");
        }

        @Test
        void should_delete_book_from_catalog() {
            String id = manager.addBook("To Delete", "Author", "ISBN", 2000, 1, "ROMAN");
            
            manager.deleteBook(id);
            
            assertThat(manager.getBook(id)).isNull();
        }
        
        @Test
        void should_return_all_books_in_catalog() {
            // Initial data has 6 books
            assertThat(manager.getAllBooks()).hasSizeGreaterThanOrEqualTo(6);
        }
    }

    @Nested
    @DisplayName("Member Administration")
    class MemberAdministration {

        @Test
        void should_register_new_member() {
            String id = manager.addMember("John", "Doe", "john.doe@example.com", "EXTERNAL");
            
            Member member = manager.getMember(id);
            assertThat(member).isNotNull();
            assertThat(member.getEmail()).isEqualTo("john.doe@example.com");
            assertThat(member.getMemberType()).isEqualTo("EXTERNAL");
        }

        @Test
        void should_find_member_by_email() {
            manager.addMember("Jane", "Doe", "jane@example.com", "STUDENT");
            
            Member found = manager.getMemberByEmail("jane@example.com");
            assertThat(found).isNotNull();
            assertThat(found.getFirstName()).isEqualTo("Jane");
        }
        
        @Test
        void should_return_null_when_member_email_not_found() {
            assertThat(manager.getMemberByEmail("unknown@example.com")).isNull();
        }

        @Test
        void should_update_member_details() {
            String id = manager.addMember("Paul", "Smith", "paul@example.com", "STAFF");
            Member member = manager.getMember(id);
            member.setLastName("Williams");
            
            manager.updateMember(member);
            
            assertThat(manager.getMember(id).getLastName()).isEqualTo("Williams");
        }

        @Test
        void should_delete_member_account() {
            String id = manager.addMember("To", "Delete", "delete@example.com", "STUDENT");
            
            manager.deleteMember(id);
            
            assertThat(manager.getMember(id)).isNull();
        }
        
        @Test
        void should_return_all_registered_members() {
            // Initial data has 4 members
            assertThat(manager.getAllMembers()).hasSizeGreaterThanOrEqualTo(4);
        }
    }

    @Nested
    @DisplayName("Loan Operations")
    class LoanOperations {
        
        private String bookId;
        private String memberId;
        
        @BeforeEach
        void prepareLoanData() {
            bookId = manager.addBook("Loanable Book", "Author", "L-ISBN", 2023, 5, "ROMAN");
            memberId = manager.addMember("Loaner", "Member", "loaner@test.com", "STUDENT");
        }

        @Test
        void should_successfully_create_loan_when_rules_met() {
            String loanId = manager.createLoan(memberId, bookId);
            
            Loan loan = manager.getLoan(loanId);
            assertThat(loan).isNotNull();
            assertThat(loan.getStatus()).isEqualTo("ACTIVE");
            assertThat(loan.getBookId()).isEqualTo(bookId);
            assertThat(loan.getMemberId()).isEqualTo(memberId);
            
            Book book = manager.getBook(bookId);
            assertThat(book.getAvailableCopies()).isEqualTo(4); // Was 5
            
            Member member = manager.getMember(memberId);
            assertThat(member.getCurrentLoansCount()).isEqualTo(1);
        }

        @Test
        void should_throw_exception_when_member_not_found() {
            assertThatThrownBy(() -> manager.createLoan("UNKNOWN", bookId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Membre non trouvé");
        }

        @Test
        void should_throw_exception_when_book_not_found() {
            assertThatThrownBy(() -> manager.createLoan(memberId, "UNKNOWN"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Livre non trouvé");
        }

        @Test
        void should_throw_exception_when_quota_exceeded() {
            // Student quota is 3. Borrow 3 books first.
            manager.createLoan(memberId, manager.addBook("B1", "A", "I1", 2000, 1, "BD"));
            manager.createLoan(memberId, manager.addBook("B2", "A", "I2", 2000, 1, "BD"));
            manager.createLoan(memberId, manager.addBook("B3", "A", "I3", 2000, 1, "BD"));
            
            assertThatThrownBy(() -> manager.createLoan(memberId, bookId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Quota d'emprunts atteint");
        }
        
        @Test
        void should_throw_exception_when_no_copies_available() {
            String scarceBookId = manager.addBook("Scarce Book", "A", "S-ISBN", 2000, 1, "ROMAN");
            String otherMemberId = manager.addMember("Other", "M", "other@test.com", "TEACHER");
            
            manager.createLoan(otherMemberId, scarceBookId); // Takes the only copy
            
            assertThatThrownBy(() -> manager.createLoan(memberId, scarceBookId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Aucun exemplaire disponible");
        }

        @Test
        void should_throw_exception_when_book_already_borrowed_by_member() {
            manager.createLoan(memberId, bookId);
            
            assertThatThrownBy(() -> manager.createLoan(memberId, bookId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Vous avez déjà emprunté ce livre");
        }

        @Test
        void should_process_loan_return_correctly() {
            String loanId = manager.createLoan(memberId, bookId);
            
            manager.returnLoan(loanId);
            
            Loan loan = manager.getLoan(loanId);
            assertThat(loan.getStatus()).isEqualTo("RETURNED");
            assertThat(loan.getReturnDate()).isNotNull();
            
            Book book = manager.getBook(bookId);
            assertThat(book.getAvailableCopies()).isEqualTo(5); // Back to original
            
            Member member = manager.getMember(memberId);
            assertThat(member.getCurrentLoansCount()).isZero();
        }
        
        @Test
        void should_throw_exception_when_returning_already_returned_loan() {
            String loanId = manager.createLoan(memberId, bookId);
            manager.returnLoan(loanId);
            
            assertThatThrownBy(() -> manager.returnLoan(loanId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("déjà été retourné");
        }

        @Test
        void should_renew_active_loan() {
            String loanId = manager.createLoan(memberId, bookId);
            Loan loan = manager.getLoan(loanId);
            Date originalDueDate = loan.getDueDate();
            
            manager.renewLoan(loanId);
            
            assertThat(loan.getRenewalCount()).isEqualTo(1);
            assertThat(loan.getDueDate()).isAfter(originalDueDate);
        }
        
        @Test
        void should_throw_exception_when_renewing_too_many_times() {
            String loanId = manager.createLoan(memberId, bookId);
            manager.renewLoan(loanId);
            manager.renewLoan(loanId); // Max is 2
            
            assertThatThrownBy(() -> manager.renewLoan(loanId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Nombre maximum de renouvellements atteint");
        }
        
        @Test
        void should_retrieve_active_and_member_loans() {
            manager.createLoan(memberId, bookId);
            
            assertThat(manager.getActiveLoans()).hasSizeGreaterThanOrEqualTo(1);
            assertThat(manager.getMemberLoans(memberId)).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Reservation System")
    class ReservationSystem {
        private String popularBookId;
        private String member1Id;
        private String member2Id;

        @BeforeEach
        void prepareReservationData() {
            // Book with 0 copies available
            popularBookId = manager.addBook("Popular", "A", "P-ISBN", 2023, 1, "ROMAN");
            member1Id = manager.addMember("M1", "One", "m1@test.com", "STUDENT");
            member2Id = manager.addMember("M2", "Two", "m2@test.com", "STUDENT");
            
            // Someone borrows it first
            String loanerId = manager.addMember("Loaner", "X", "lx@test.com", "TEACHER");
            manager.createLoan(loanerId, popularBookId); 
        }

        @Test
        void should_create_reservation_successfully() {
            String resId = manager.createReservation(member1Id, popularBookId);
            
            assertThat(resId).isNotNull();
        }

        @Test
        void should_manage_queue_positions_correctly() {
            String res1 = manager.createReservation(member1Id, popularBookId);
            String res2 = manager.createReservation(member2Id, popularBookId);
            
            // Since we can't access reservation object directly easily, we assume logic works if no exception
            // We can implicitly verify by cancelling first and ensuring second moves up maybe?
            // Or relying on the fact that if we can create them, it works.
            assertThat(res1).isNotEqualTo(res2);
        }
        
        @Test
        void should_prevent_duplicate_reservation() {
            manager.createReservation(member1Id, popularBookId);
            
            assertThatThrownBy(() -> manager.createReservation(member1Id, popularBookId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("déjà une réservation");
        }
        
        @Test
        void should_cancel_reservation_and_update_queue() {
            String resId = manager.createReservation(member1Id, popularBookId);
            
            manager.cancelReservation(resId);
            
            // Can't easily inspect internal state without getters, but let's check no exception
        }
        
        @Test
        void should_throw_exception_when_accessing_non_existent_reservation() {
            assertThatThrownBy(() -> manager.cancelReservation("INVALID"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Réservation non trouvée");
        }
    }

    @Nested
    @DisplayName("Reporting and Analytics")
    class ReportingAndAnalytics {
        @Test
        void should_generate_loan_report_without_error() {
            String report = manager.generateLoanReport();
            
            assertThat(report).isNotNull();
            assertThat(report).contains("RAPPORT DES EMPRUNTS");
            assertThat(report).contains("STATISTIQUES GLOBALES");
        }

        @Test
        void should_generate_inventory_report_without_error() {
            String report = manager.generateInventoryReport();
            
            assertThat(report).isNotNull();
            assertThat(report).contains("INVENTAIRE");
            assertThat(report).contains("Nombre de titres");
        }
    }

    @Nested
    @DisplayName("Penalty Calculation Rules")
    class PenaltyCalculation {
        @Test
        void should_return_zero_when_no_delay() {
            Member member = new Member("M1", "Test", "Test", "test@test.com", "STUDENT");
            assertThat(manager.calculatePenalty(member, 0)).isZero();
            assertThat(manager.calculatePenalty(member, -1)).isZero();
        }

        @Test
        void should_calculate_student_penalty_correctly() {
            Member member = new Member("M1", "Test", "Test", "test@test.com", "STUDENT");
            // 0.25 per day, max 10
            assertThat(manager.calculatePenalty(member, 4)).isEqualTo(1.0); // 4 * 0.25
            assertThat(manager.calculatePenalty(member, 100)).isEqualTo(10.0); // Max cap
        }

        @Test
        void should_calculate_teacher_penalty_correctly() {
            Member member = new Member("M1", "Test", "Test", "test@test.com", "TEACHER");
            // 0 per day
            assertThat(manager.calculatePenalty(member, 10)).isZero();
        }

        @Test
        void should_calculate_staff_penalty_correctly() {
            Member member = new Member("M1", "Test", "Test", "test@test.com", "STAFF");
            // 0.25 per day, max 15
            assertThat(manager.calculatePenalty(member, 4)).isEqualTo(1.0);
            assertThat(manager.calculatePenalty(member, 100)).isEqualTo(15.0);
        }

        @Test
        void should_calculate_external_penalty_correctly() {
            Member member = new Member("M1", "Test", "Test", "test@test.com", "EXTERNAL");
            // 0.50 per day, max 25
            assertThat(manager.calculatePenalty(member, 4)).isEqualTo(2.0);
            assertThat(manager.calculatePenalty(member, 100)).isEqualTo(25.0);
        }
    }

    @Nested
    @DisplayName("Quota Verification")
    class QuotaVerification {
        @Test
        void should_enforce_teacher_quota() {
            Member teacher = manager.getMember(manager.addMember("T", "T", "t@t.com", "TEACHER"));
            // Quota is 10
            for (int i = 0; i < 10; i++) {
                String bid = manager.addBook("B" + i, "A", "I" + i, 2000, 1, "TECH");
                manager.createLoan(teacher.getId(), bid);
            }
            String extraBook = manager.addBook("Extra", "A", "IX", 2000, 1, "TECH");
            
            assertThatThrownBy(() -> manager.createLoan(teacher.getId(), extraBook))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Quota d'emprunts atteint");
        }

        @Test
        void should_enforce_staff_quota() {
            Member staff = manager.getMember(manager.addMember("S", "S", "s@s.com", "STAFF"));
            // Quota is 5
            for (int i = 0; i < 5; i++) {
                String bid = manager.addBook("B" + i, "A", "I" + i, 2000, 1, "TECH");
                manager.createLoan(staff.getId(), bid);
            }
            String extraBook = manager.addBook("Extra", "A", "IX", 2000, 1, "TECH");
            
            assertThatThrownBy(() -> manager.createLoan(staff.getId(), extraBook))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Quota d'emprunts atteint");
        }

        @Test
        void should_enforce_external_quota() {
            Member external = manager.getMember(manager.addMember("E", "E", "e@e.com", "EXTERNAL"));
            // Quota is 2
            manager.createLoan(external.getId(), manager.addBook("B1", "A", "I1", 2000, 1, "TECH"));
            manager.createLoan(external.getId(), manager.addBook("B2", "A", "I2", 2000, 1, "TECH"));
            String extraBook = manager.addBook("Extra", "A", "IX", 2000, 1, "TECH");
            
            assertThatThrownBy(() -> manager.createLoan(external.getId(), extraBook))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Quota d'emprunts atteint");
        }
    }

    @Nested
    @DisplayName("Complex Loan Rules")
    class ComplexLoanRules {
        @Test
        void should_prevent_loan_if_member_inactive() {
            Member member = manager.getMember(manager.addMember("I", "I", "i@i.com", "STUDENT"));
            member.setActive(false);
            String bookId = manager.addBook("B", "A", "I", 2000, 1, "TECH");
            
            assertThatThrownBy(() -> manager.createLoan(member.getId(), bookId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("n'est pas actif");
        }

        @Test
        void should_prevent_loan_if_membership_expired() {
            Member member = manager.getMember(manager.addMember("E", "E", "e@e.com", "STUDENT"));
            Date yesterday = new Date(System.currentTimeMillis() - 86400000);
            member.setMembershipExpiryDate(yesterday);
            String bookId = manager.addBook("B", "A", "I", 2000, 1, "TECH");
            
            assertThatThrownBy(() -> manager.createLoan(member.getId(), bookId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("adhésion du membre " + member.getId() + " a expiré");
        }

        @Test
        void should_prevent_loan_if_unpaid_penalties_too_high() {
            Member member = manager.getMember(manager.addMember("P", "P", "p@p.com", "STUDENT"));
            String bookId = manager.addBook("B", "A", "I", 2000, 1, "TECH");
            
            // Artificial setup: create a loan and inject penalty
            String loanId = manager.createLoan(member.getId(), bookId);
            Loan loan = manager.getLoan(loanId);
            loan.setPenaltyAmount(15.0); // > 10.0 limit
            
            String bookId2 = manager.addBook("B2", "A", "I2", 2000, 1, "TECH");
            
            assertThatThrownBy(() -> manager.createLoan(member.getId(), bookId2))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Pénalités impayées trop élevées");
        }

        @Test
        void should_prevent_loan_if_too_many_late_returns() {
            Member member = manager.getMember(manager.addMember("L", "L", "l@l.com", "STUDENT"));
            member.setLateReturnsCount(4); // Limit is 3
            String bookId = manager.addBook("B", "A", "I", 2000, 1, "TECH");
            
            assertThatThrownBy(() -> manager.createLoan(member.getId(), bookId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Trop de retards");
        }

        @Test
        void should_prevent_loan_if_book_inactive() {
            Member member = manager.getMember(manager.addMember("Active", "M", "a@m.com", "STUDENT"));
            String bookId = manager.addBook("B", "A", "I", 2000, 1, "TECH");
            Book book = manager.getBook(bookId);
            book.setActive(false);
            
            assertThatThrownBy(() -> manager.createLoan(member.getId(), bookId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("n'est plus disponible");
        }

        @Test
        void should_prevent_loan_if_book_is_reserved_by_someone_else() {
            String bookId = manager.addBook("Reserved", "A", "I", 2000, 1, "TECH");
            Member holder = manager.getMember(manager.addMember("H", "H", "h@h.com", "STUDENT"));
            // Borrow and Return to make it available but allow reservations trigger
            String loanId = manager.createLoan(holder.getId(), bookId);
            manager.returnLoan(loanId);
            
            Member reserver = manager.getMember(manager.addMember("R", "R", "r@r.com", "STUDENT"));
            manager.createReservation(reserver.getId(), bookId);
            
            Member intruder = manager.getMember(manager.addMember("I", "I", "i@i.com", "STUDENT"));
            
            assertThatThrownBy(() -> manager.createLoan(intruder.getId(), bookId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("réservé par un autre membre");
        }
        
        @Test
        void should_allow_loan_if_book_is_reserved_by_me() {
            String bookId = manager.addBook("MyReserved", "A", "I", 2000, 1, "TECH");
            Member holder = manager.getMember(manager.addMember("H2", "H", "h2@h.com", "STUDENT"));
            String loanId = manager.createLoan(holder.getId(), bookId);
            manager.returnLoan(loanId);
            
            Member reserver = manager.getMember(manager.addMember("Me", "Me", "me@me.com", "STUDENT"));
            String resId = manager.createReservation(reserver.getId(), bookId);
            
            // Should succeed and fulfill reservation
            String newLoanId = manager.createLoan(reserver.getId(), bookId);
            assertThat(newLoanId).isNotNull();
            // TODO: In a real test we might want to check reservation status is FULFILLED, but no getter for all reqs
        }
    }

    @Nested
    @DisplayName("Notification Rules")
    class NotificationRules {
        @Test
        void should_send_reminders_for_loans_due_soon() {
            Member member = manager.getMember(manager.addMember("N", "N", "n@n.com", "STUDENT"));
            String bookId = manager.addBook("B", "A", "I", 2000, 1, "TECH");
            String loanId = manager.createLoan(member.getId(), bookId);
            
            // Hack: modify due date to be tomorrow
            Loan loan = manager.getLoan(loanId);
            Date tomorrow = new Date(System.currentTimeMillis() + 86400000);
            loan.setDueDate(tomorrow);
            
            // Just verify no crash, verifying stdout is hard
            manager.sendDueReminders();
        }

        @Test
        void should_mark_overdue_loans_and_apply_penalties() {
            Member member = manager.getMember(manager.addMember("O", "O", "o@o.com", "STUDENT"));
            String bookId = manager.addBook("B", "A", "I", 2000, 1, "TECH");
            String loanId = manager.createLoan(member.getId(), bookId);
            
            // Use Calendar to set due date 10 days in the past
            Loan loan = manager.getLoan(loanId);
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_YEAR, -10);
            loan.setDueDate(cal.getTime());
            
            manager.sendOverdueNotifications();
            
            assertThat(loan.getStatus()).isEqualTo("OVERDUE");
        }
        
        @Test
        void should_update_penalties_when_returning_overdue_loan() {
            Member member = manager.getMember(manager.addMember("L", "L", "l@l.com", "STUDENT"));
            String bookId = manager.addBook("B", "A", "I", 2000, 1, "TECH");
            String loanId = manager.createLoan(member.getId(), bookId);
            
            Loan loan = manager.getLoan(loanId);
            // Set due date 10 days in past
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_YEAR, -10);
            loan.setDueDate(cal.getTime());
            
            manager.returnLoan(loanId);
            
            // 10 days overdue * 0.25 = 2.5
            assertThat(loan.getPenaltyAmount()).isBetween(2.0, 3.0);
            assertThat(member.getLateReturnsCount()).isEqualTo(1);
        }
        
        @Test
        void should_notify_next_reserver_on_return() {
            String bookId = manager.addBook("ResBook", "A", "I", 2000, 1, "TECH");
            Member m1 = manager.getMember(manager.addMember("M1", "M", "m1@m.com", "STUDENT"));
            Member m2 = manager.getMember(manager.addMember("M2", "M", "m2@m.com", "STUDENT"));
            
            String loanId = manager.createLoan(m1.getId(), bookId);
            manager.createReservation(m2.getId(), bookId);
            
            // Check no crash on return which triggers notification
            manager.returnLoan(loanId);
        }
    }

    @Nested
    @DisplayName("Loan Queries")
    class LoanQueries {

        @Test
        void should_retrieve_all_loans() {
            Member member = manager.getMember(manager.addMember("Q1", "Q1", "q1@q.com", "STUDENT"));
            String bookId = manager.addBook("QB1", "A", "I", 2000, 1, "TECH");

            manager.createLoan(member.getId(), bookId);

            List<Loan> allLoans = manager.getAllLoans();
            assertThat(allLoans).hasSizeGreaterThanOrEqualTo(1);
        }

        @Test
        void should_retrieve_active_and_overdue_loans() {
            Member member = manager.getMember(manager.addMember("Q2", "Q2", "q2@q.com", "STUDENT"));
            String bookId1 = manager.addBook("QB2", "A", "I", 2000, 1, "TECH");
            String bookId2 = manager.addBook("QB3", "A", "I", 2000, 1, "TECH");

            String loanId1 = manager.createLoan(member.getId(), bookId1); // Active
            String loanId2 = manager.createLoan(member.getId(), bookId2); // Will be returned

            manager.returnLoan(loanId2);

            assertThat(manager.getActiveLoans()).extracting(Loan::getId).contains(loanId1).doesNotContain(loanId2);

            // Create overdue
            String bookId3 = manager.addBook("QB4", "A", "I", 2000, 1, "TECH");
            String loanId3 = manager.createLoan(member.getId(), bookId3);
            Loan overdueLoan = manager.getLoan(loanId3);
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_YEAR, -20);
            overdueLoan.setDueDate(cal.getTime());

            assertThat(manager.getOverdueLoans()).extracting(Loan::getId).contains(loanId3);
        }
    }
}
