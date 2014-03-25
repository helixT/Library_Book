

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Vojtech Bayer
 * @version 1.0
 */
public class BookManagerImpl implements BookManager {
    
    public static final Logger logger = Logger.getLogger(BookManagerImpl.class.getName());
    private Connection conn;
    
    public BookManagerImpl(Connection conn){
        this.conn = conn;
    }

    @Override
    public void addBook(Book book) {
        
        if(book == null){
            throw new IllegalArgumentException("Book is null.");
        }
        if(book.getId() != null){
            throw new IllegalArgumentException("ID is already used.");
        }     
        if(book.getQuantity() < 0){
            throw new IllegalArgumentException("quantity is negative value");
        }
        if(book.getTitle() == null){
            throw new IllegalArgumentException("Title is null.");
        }
       
        
        try(PreparedStatement st = conn.prepareStatement(
                "INSERT INTO BOOK (author,genre,title,isbn,quantity) VALUES(?,?,?,?,?)", 
                    Statement.RETURN_GENERATED_KEYS)){
            st.setString(1, book.getAuthor());
            st.setString(2, book.getGenre());
            st.setString(3, book.getTitle());
            st.setInt(4, book.getIsbn());
            st.setInt(5, book.getQuantity());
            
            int addedRows = st.executeUpdate();
            if(addedRows != 1){
                throw new ServiceFailureException("More rows inserted when tryig insert book" + book);
            }
            
            ResultSet keyRS = st.getGeneratedKeys();
            book.setId(getKey(keyRS,book));
            
        }catch(SQLException ex){
            logger.log(Level.SEVERE, null, ex);
            throw new ServiceFailureException("Error when inserting book " + book, ex);
        }
                
        
    }
    
    private Long getKey(ResultSet keyRS, Book book) throws ServiceFailureException, SQLException {
        if (keyRS.next()) {
            if (keyRS.getMetaData().getColumnCount() != 1) {
                throw new ServiceFailureException("Internal Error: Generated key"
                        + "retriving failed when trying to insert grave " + book
                        + " - wrong key fields count: " + keyRS.getMetaData().getColumnCount());
            }
            Long result = keyRS.getLong(1);
            if (keyRS.next()) {
                throw new ServiceFailureException("Internal Error: Generated key"
                        + "retriving failed when trying to insert grave " + book
                        + " - more keys found");
            }
            return result;
        } else {
            throw new ServiceFailureException("Internal Error: Generated key"
                    + "retriving failed when trying to insert grave " + book
                    + " - no key found");
        }
    }

    @Override
    public void updateBook(Book book) {
        if(book == null){
            throw new IllegalArgumentException("Book is null.");
        }
        if(book.getId() == null){
            throw new IllegalArgumentException("ID doesnt exists.");
        }
        
        try(PreparedStatement st = conn.prepareStatement(
                "UPDATE BOOK set id= ? author= ? genre= ? title= ? isbn= ? quantity= ? WHERE id = ?")){
            st.setString(1, book.getAuthor());
            st.setString(2, book.getGenre());
            st.setString(3, book.getTitle());
            st.setInt(4, book.getIsbn());
            st.setInt(5, book.getQuantity());
            
            st.executeUpdate();                                           
                        
        }catch(SQLException ex){
            logger.log(Level.SEVERE, null, ex);
            throw new ServiceFailureException("Error when updating book." + book,ex);
            
        }
    }

    @Override
    public void deleteBook(Book book) {
        if(book == null){
            throw new IllegalArgumentException("Book is null.");
        }
        if(book.getId() == null){
            throw new IllegalArgumentException("ID doesnt exists.");
        }
        
        try(PreparedStatement st = conn.prepareStatement("DELETE BOOK WHERE id = ?")){
            st.setLong(1, book.getId());
            st.executeUpdate();                                           
                        
        }catch(SQLException ex){
            logger.log(Level.SEVERE, null, ex);
            throw new ServiceFailureException("Error when updating book." + book,ex);
            
        }
    }

    @Override
    public List<Book> findAllBooks() {
        try(PreparedStatement st = conn.prepareStatement(
                "SELECT id, author, genre, title, isbn, quantity FROM book")){
            ResultSet rs = st.executeQuery();
            List<Book> result = new ArrayList<>();
            
            while(rs.next()){
                result.add(resultSetToBook(rs));
            }
            return result;  
            
            
        }catch(SQLException ex){
            logger.log(Level.SEVERE, null, ex);
            throw new ServiceFailureException("Error when retrieving book with id.",ex);
            
        }
    }

    @Override
    public List<Book> findBooksByAuthor(String author) {        
        try(PreparedStatement st = conn.prepareStatement(
                "SELECT id, author, genre, title, isbn, quantity WHERE author = ?")){
            st.setString(1, "author");
            ResultSet rs = st.executeQuery();
            List<Book> result = new ArrayList<>();
            
            while(rs.next()){
                result.add(resultSetToBook(rs));
            }
            return result;  
            
            
        }catch(SQLException ex){
            logger.log(Level.SEVERE, null, ex);
            throw new ServiceFailureException("Error when retrieving book with by author.",ex);
            
        }
    }

    @Override
    public List<Book> findBooksByGenre(String genre) {
        try(PreparedStatement st = conn.prepareStatement(
                "SELECT id, author, genre, title, isbn, quantity WHERE genre = ?")){
            st.setString(1, "genre");
            ResultSet rs = st.executeQuery();
            List<Book> result = new ArrayList<>();
            
            while(rs.next()){
                result.add(resultSetToBook(rs));
            }
            return result;  
            
            
        }catch(SQLException ex){
            logger.log(Level.SEVERE, null, ex);
            throw new ServiceFailureException("Error when retrieving book with by genre.",ex);
            
        }
    }

    @Override
    public List<Book> findBooksByTitle(String title) {
        try(PreparedStatement st = conn.prepareStatement(
                "SELECT id, author, genre, title, isbn, quantity WHERE title = ?")){
            st.setString(1, "title");
            ResultSet rs = st.executeQuery();
            List<Book> result = new ArrayList<>();
            
            while(rs.next()){
                result.add(resultSetToBook(rs));
            }
            return result;  
            
            
        }catch(SQLException ex){
            logger.log(Level.SEVERE, null, ex);
            throw new ServiceFailureException("Error when retrieving book with by title.",ex);
            
        }
    }

    @Override
    public Book getBookById(Long id) {
        try(PreparedStatement st = conn.prepareStatement(
                "SELECT id, author, genre, title, isbn, quantity FROM book WHERE id = ? ")){
            st.setLong(1, id);
            ResultSet rs = st.executeQuery();
            
            if(rs.next()){
                Book book = resultSetToBook(rs);
                
                if(rs.next()){
                    throw new ServiceFailureException("More entities with the same id found"
                            + id + book + resultSetToBook(rs) );
                }
                
                return book;
            } else {
                return null;
            }
            
        }catch(SQLException ex){
            logger.log(Level.SEVERE, null, ex);
            throw new ServiceFailureException("Error when retrieving book with id.",ex);
            
        }
    }
    
    private Book resultSetToBook(ResultSet rs) throws SQLException{
        Book book = new Book();
        
        book.setId(rs.getLong("id"));
        book.setAuthor(rs.getString("author"));
        book.setGenre(rs.getString("genre"));
        book.setTitle(rs.getString("title"));
        book.setIsbn(rs.getInt("isbn"));
        book.setQuantity(rs.getInt("quantity"));
        
        return book;
    }
    
}
