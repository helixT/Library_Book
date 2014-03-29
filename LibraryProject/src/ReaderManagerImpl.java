/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jduda
 */
public class ReaderManagerImpl implements ReaderManager {
    public static final Logger logger = Logger.getLogger(ReaderManagerImpl.class.getName());
    private Connection conn;
    
    ReaderManagerImpl(Connection conn){
        this.conn = conn;
    }

    @Override
    public void addReader(Reader reader) throws ServiceFailureException {
        correctInputReader(reader);
        if(reader.getId() != null) {
            throw new IllegalArgumentException("Reader's id isn't null!");            
        }
        
        try(PreparedStatement st = conn.prepareStatement(
                    "INSERT INTO READER (fullname,adress,phonenumber) VALUES (?,?,?)", Statement.RETURN_GENERATED_KEYS);){
            st.setString(1, reader.getFullName());
            st.setString(2, reader.getAdress());
            if(reader.getPhoneNumber() == null){
                st.setNull(3, java.sql.Types.INTEGER);
            } else{
                st.setInt(3, reader.getPhoneNumber());
            }
            
            int addedRows = st.executeUpdate();
            if(addedRows != 1){
                throw new ServiceFailureException("More rows inserted when tryig insert reader" + reader);
            }
            
            ResultSet keys = st.getGeneratedKeys();
            reader.setId(getKey(keys,reader));
            
        } catch (SQLException ex) {
            throw new ServiceFailureException("Error when inserting reader " + reader, ex);
        }
    }

    @Override
    public void updateReader(Reader reader) throws ServiceFailureException {
        correctInputReader(reader);
        if(reader.getId() == null) {
            throw new IllegalArgumentException("Readed's id is null!");            
        }
        
        try(PreparedStatement st = conn.prepareStatement(
                "UPDATE READER SET fullname=?,adress=?,phonenumber=? WHERE id=?")){
            
            st.setString(1, reader.getFullName());
            st.setString(2, reader.getAdress());
            if(reader.getPhoneNumber() == null){
                st.setNull(3, java.sql.Types.INTEGER);
            } else{
                st.setInt(3, reader.getPhoneNumber());
            }
            st.setLong(4, reader.getId());
            
            int updatedRows = st.executeUpdate();
            if((updatedRows != 1) && (updatedRows != 0)){
                throw new ServiceFailureException("Error: was edit more than one reader.");
            }
                        
        }catch(SQLException ex){
            throw new ServiceFailureException("Error when editing reader " + reader,ex);
        }
    }

    @Override
    public void deleteReader(Reader reader) throws ServiceFailureException {
        if(reader == null){
            throw new IllegalArgumentException("Input reader is null!");
        }
        if(reader.getId() == null){
            throw new IllegalArgumentException("Input reader's id is null!");
        }
        
        try(PreparedStatement st = conn.prepareStatement("DELETE FROM READER WHERE id=? AND fullname=? AND adress=? AND phonenumber=?")){
            st.setLong(1, reader.getId());
            st.setString(2, reader.getFullName());
            st.setString(3, reader.getAdress());
            st.setInt(4, reader.getPhoneNumber());
            
            int deletedReaders = st.executeUpdate();
            
            if((deletedReaders != 1) && (deletedReaders != 0)){
                throw new ServiceFailureException("Was deleted more than one reader!");
            }
            
        } catch (SQLException ex) {
            throw new ServiceFailureException("Error when deleting reader " + reader, ex);
        }
    }

    @Override
    public Reader findReaderById(Long id) throws ServiceFailureException {
        if(id.intValue() <= 0){
            throw new IllegalArgumentException("Input id isn't positive number!");
        }
        
        try(PreparedStatement st = conn.prepareStatement(
                "SELECT id,fullname,adress,phonenumber FROM reader WHERE id=?")){
            st.setLong(1, id);
            ResultSet rs = st.executeQuery();
            
            if (rs.next()) {
                Reader reader = resultToReader(rs);

                if (rs.next()) {
                    throw new ServiceFailureException(
                            "Error: Was founded more than one reader "
                            + "(Input id: " + id + " and were found " + reader + " and " + resultToReader(rs));
                }            
                
                return reader;
            } else {
                return null;
            }
            
        } catch (SQLException ex) {
            throw new ServiceFailureException("Error when finding reader with id " + id, ex);
        }          
    }

    @Override
    public List<Reader> findReaderByName(String name) throws ServiceFailureException {
        if(name == null){
            throw new IllegalArgumentException("Input name is null!");
        }
        
        try(PreparedStatement st = conn.prepareStatement(
                "SELECT id,fullname,adress,phonenumber FROM reader WHERE fullname=?")){
            
            st.setString(1, name);
            ResultSet rs = st.executeQuery();
            List<Reader> listOfReaders = new ArrayList<>();
            
            while(rs.next()){
                listOfReaders.add(resultToReader(rs));
            }
            
            return listOfReaders;
        } catch (SQLException ex) {
            throw new ServiceFailureException("Error when finding readers with name: " + name, ex);
        }
    }

    @Override
    public List<Reader> findAllReaders() throws ServiceFailureException {
        
        try(PreparedStatement st = conn.prepareStatement(
                "SELECT id,fullname,adress,phonenumber FROM reader")){
            
            ResultSet rs = st.executeQuery();
            List<Reader> listOfReaders = new ArrayList<>();
            
            while(rs.next()){
                listOfReaders.add(resultToReader(rs));
            }
            
            return listOfReaders;
        } catch (SQLException ex) {
            throw new ServiceFailureException("Error when finding readers", ex);
        }
    }

    private void correctInputReader(Reader reader) {
        if(reader == null) {
            throw new IllegalArgumentException("Input reader is null");            
        }
        if(reader.getFullName() == null) {
            throw new IllegalArgumentException("Reader's name is null");            
        }
        if(reader.getAdress() == null) {
            throw new IllegalArgumentException("Reader's adress is null");            
        }
        if(reader.getPhoneNumber() != null){
            if(reader.getPhoneNumber().intValue() <= 0){
                throw new IllegalArgumentException("Reader's phone number is neative!");
            }
            if(reader.getPhoneNumber().toString().length() != 9){
                throw new IllegalArgumentException("Reader's phone number hasn't got 9 digits!");
            }
        }
    }
    
    private Long getKey(ResultSet keys, Reader reader) throws ServiceFailureException, SQLException {
        if (keys.next()) {
            if (keys.getMetaData().getColumnCount() != 1) {
                throw new ServiceFailureException("Internal Error: Generated key"
                        + "retriving failed when trying to insert reader " + reader
                        + " - wrong key fields count: " + keys.getMetaData().getColumnCount());
            }
            Long result = keys.getLong(1);
            if (keys.next()) {
                throw new ServiceFailureException("Internal Error: Generated key"
                        + "retriving failed when trying to insert reader " + reader
                        + " - more keys found");
            }
            return result;
        } else {
            throw new ServiceFailureException("Internal Error: Generated key"
                    + "retriving failed when trying to insert reader " + reader
                    + " - no key found");
        }
    }
    
    private Reader resultToReader(ResultSet rs) throws SQLException {
        Reader reader = new Reader();
        reader.setId(rs.getLong("id"));
        reader.setFullName(rs.getString("fullname"));
        reader.setAdress(rs.getString("adress"));
        if(rs.getInt("phonenumber") == 0){
            reader.setPhoneNumber(null);
        }else{
            reader.setPhoneNumber(rs.getInt("phonenumber"));
        }
        return reader;
    }
}
