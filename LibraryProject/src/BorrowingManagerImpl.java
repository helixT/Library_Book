
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Logger;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Honza
 */
public class BorrowingManagerImpl implements BorrowingManager{

    public static final Logger logger = Logger.getLogger(BorrowingManagerImpl.class.getName());
    private Connection conn;
    
    BorrowingManagerImpl(Connection conn){
        this.conn = conn;
    }

    @Override
    public void addBorrowing(Borrowing borrowing) throws ServiceFailureException {
        correctInputBorrowing(borrowing);
        if(borrowing.getId() != null) {
            throw new IllegalArgumentException("Borrowing's id isn't null!");            
        }
        
        try(PreparedStatement st = conn.prepareStatement(
                    "INSERT INTO BORROWING (bookborrowedfrom,bookborrowedto,readerid,bookid)"
                            + " VALUES (?,?,?,?)", Statement.RETURN_GENERATED_KEYS);){
            st.setTimestamp(1, new Timestamp(borrowing.getBookBorrowedFrom().getTimeInMillis()));
            st.setTimestamp(2, new Timestamp(borrowing.getBookBorrowedTo().getTimeInMillis()));
            st.setLong(3, borrowing.getReader().getId());
            st.setLong(4, borrowing.getBook().getId());
            
            int addedRows = st.executeUpdate();
            if(addedRows != 1){
                throw new ServiceFailureException("More rows inserted when tryig insert reader" + borrowing);
            }
            
            ResultSet keys = st.getGeneratedKeys();
            borrowing.setId(getKey(keys,borrowing));
            
        } catch (SQLException ex) {
            throw new ServiceFailureException("Error when inserting borrowing " + borrowing, ex);
        }
    }

    @Override
    public void updateBorrowing(Borrowing borrowing) throws ServiceFailureException  {
        correctInputBorrowing(borrowing);
        if(borrowing.getId() == null){
            throw new IllegalArgumentException("Borrowing's id isn't set!");
        }
        
        try(PreparedStatement st = conn.prepareStatement(
                    "UPDATE BORROWING SET bookborrowedfrom=?,bookborrowedto=?,readerid=?,bookid=?"
                            + " WHERE id=?");){
            st.setTimestamp(1, new Timestamp(borrowing.getBookBorrowedFrom().getTimeInMillis()));
            st.setTimestamp(2, new Timestamp(borrowing.getBookBorrowedTo().getTimeInMillis()));
            st.setLong(3, borrowing.getReader().getId());
            st.setLong(4, borrowing.getBook().getId());
            st.setLong(5, borrowing.getId());
            
            int updatedRows = st.executeUpdate();
            if((updatedRows != 1) && (updatedRows != 0)){
                throw new ServiceFailureException("Error: was edit more than one borrowing!");
            }
            
        } catch (SQLException ex) {
            throw new ServiceFailureException("Error when updating borrowing " + borrowing, ex);
        }
    }

    @Override
    public void deleteBorrowing(Borrowing borrowing) throws ServiceFailureException {
        if(borrowing == null){
            throw new IllegalArgumentException("Input borrowing is null!");
        }
        if(borrowing.getId() == null){
            throw new IllegalArgumentException("Input borrowing's id is null!");
        }
        
        try(PreparedStatement st = conn.prepareStatement("DELETE FROM BORROWING "
                + "WHERE id=? AND bookborrowedfrom=? AND bookborrowedto=? AND readerid=? AND bookid=?")){
            st.setLong(1, borrowing.getId());
            st.setTimestamp(2, new Timestamp(borrowing.getBookBorrowedFrom().getTimeInMillis()));
            st.setTimestamp(3, new Timestamp(borrowing.getBookBorrowedTo().getTimeInMillis()));
            st.setLong(4, borrowing.getReader().getId());
            st.setLong(5, borrowing.getBook().getId());
            
            int deletedReaders = st.executeUpdate();
            
            if((deletedReaders != 1) && (deletedReaders != 0)){
                throw new ServiceFailureException("Was deleted more than one borrowing!");
            }
            
        } catch (SQLException ex) {
            throw new ServiceFailureException("Error when deleting reader " + borrowing, ex);
        }
    }

    @Override
    public List<Borrowing> findAllBorrowing() {
        try(PreparedStatement st = conn.prepareStatement(
                "SELECT id,bookborrowedfrom,bookborrowedto,readerid,bookid FROM BORROWING")){
            
            ResultSet rs = st.executeQuery();
            List<Borrowing> listOfBorrowings = new ArrayList<>();
            
            while(rs.next()){
                listOfBorrowings.add(resultToBorrowing(rs));
            }
            
            return listOfBorrowings;
        } catch (SQLException ex) {
            throw new ServiceFailureException("Error when finding Borrowings", ex);
        }
    }

    @Override
    public Borrowing findBorrowingById(Long id) {
        if(id.intValue() <= 0){
            throw new IllegalArgumentException("Input id isn't positive number!");
        }
        
        try(PreparedStatement st = conn.prepareStatement(
                "SELECT id,bookborrowedfrom,bookborrowedto,readerid,bookid FROM BORROWING WHERE id=?")){
            st.setLong(1, id);
            ResultSet rs = st.executeQuery();
            
            if (rs.next()) {
                Borrowing borrowing = resultToBorrowing(rs);

                if (rs.next()) {
                    throw new ServiceFailureException(
                            "Error: Was founded more than one borrowing "
                            + "(Input id: " + id + " and were found " + borrowing + " and " + resultToBorrowing(rs));
                }            
                
                return borrowing;
            } else {
                return null;
            }
            
        } catch (SQLException ex) {
            throw new ServiceFailureException("Error when finding borrowing with id " + id, ex);
        }
    }

    @Override
    public List<Borrowing> findBorrowingByReader(Reader reader) {
        if(reader == null){
            throw new IllegalArgumentException("Input reader is null!");
        }
        
        try(PreparedStatement st = conn.prepareStatement(
                "SELECT id,bookborrowedfrom,bookborrowedto,readerid,bookid FROM BORROWING WHERE readerid=?")){
            
            st.setLong(1, reader.getId());
            ResultSet rs = st.executeQuery();
            List<Borrowing> listOfBorrowings = new ArrayList<>();
            
            while(rs.next()){
                listOfBorrowings.add(resultToBorrowing(rs));
            }
            
            return listOfBorrowings;
        } catch (SQLException ex) {
            throw new ServiceFailureException("Error when finding Borrowings with reader: " + reader, ex);
        }
    }

    private void correctInputBorrowing(Borrowing borrowing) {
        if(borrowing == null){
            throw new IllegalArgumentException("Input Borrowing is null!");
        }
        if(borrowing.getBookBorrowedFrom() == null){
            throw new IllegalArgumentException("Not set when the book was borrowed!");
        }
        if(borrowing.getBookBorrowedTo() == null){
            throw new IllegalArgumentException("Not set when the book will be returned!");
        }
        if(borrowing.getReader() == null){
            throw new IllegalArgumentException("Reader who borrowed the book isn't set!");
        }
        if(borrowing.getBook() == null){
            throw new IllegalArgumentException("The book that is borrowed isn't set!");
        }
    }

    private Long getKey(ResultSet keys, Borrowing borrowing) throws SQLException {
        if (keys.next()) {
            if (keys.getMetaData().getColumnCount() != 1) {
                throw new ServiceFailureException("Internal Error: Generated key"
                        + "retriving failed when trying to insert reader " + borrowing
                        + " - wrong key fields count: " + keys.getMetaData().getColumnCount());
            }
            Long result = keys.getLong(1);
            if (keys.next()) {
                throw new ServiceFailureException("Internal Error: Generated key"
                        + "retriving failed when trying to insert reader " + borrowing
                        + " - more keys found");
            }
            return result;
        } else {
            throw new ServiceFailureException("Internal Error: Generated key"
                    + "retriving failed when trying to insert reader " + borrowing
                    + " - no key found");
        }
    }
    
    private Borrowing resultToBorrowing(ResultSet rs) throws SQLException {
        Borrowing borrowing = new Borrowing();
        borrowing.setId(rs.getLong("id"));
        
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(rs.getTimestamp("bookborrowedFrom").getTime());
        borrowing.setBookBorrowedFrom(cal);
        
        cal = Calendar.getInstance();
        cal.setTimeInMillis(rs.getTimestamp("bookborrowedTo").getTime());
        borrowing.setBookBorrowedTo(cal);
        
        ReaderManager managerOfReaders = new ReaderManagerImpl(conn);
        Reader reader = managerOfReaders.findReaderById(rs.getLong("readerid"));
        borrowing.setReader(reader);
        
        BookManager managerOfBooks = new BookManagerImpl(conn);
        Book book = managerOfBooks.getBookById(rs.getLong("bookid"));
        borrowing.setBook(book);
        
        return borrowing;
    }
    
}
