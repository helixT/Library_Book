
import java.util.Calendar;
import java.util.Objects;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Honza
 */
public class Borrowing {
    Long id;
    Calendar borrowedFrom;
    Calendar borrowedTo;
    Calendar expectedBorrowedTo;
    Reader reader;
    Book book;

    public Long getId() {
        return id;
    }

    public Calendar getBorrowedFrom() {
        return borrowedFrom;
    }

    public Calendar getBorrowedTo() {
        return borrowedTo;
    }

    public Calendar getExpectedBorrowedTo() {
        return expectedBorrowedTo;
    }

    public Reader getReader() {
        return reader;
    }

    public Book getBook() {
        return book;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setBorrowedFrom(Calendar borrowedFrom) {
        this.borrowedFrom = borrowedFrom;
    }

    public void setBorrowedTo(Calendar borrowedTo) {
        this.borrowedTo = borrowedTo;
    }

    public void setExpectedBorrowedTo(Calendar expectedBorrowedTo) {
        this.expectedBorrowedTo = expectedBorrowedTo;
    }

    public void setReader(Reader reader) {
        this.reader = reader;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 71 * hash + Objects.hashCode(this.id);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Borrowing other = (Borrowing) obj;
        return Objects.equals(this.id, other.id);
    }

    @Override
    public String toString() {
        return "Borrowing{" + "id=" + id + ", borrowedFrom=" + borrowedFrom + ", borrowedTo=" + borrowedTo + ", expectedBorrowedTo=" + expectedBorrowedTo + ", reader=" + reader + ", book=" + book + '}';
    }
}
