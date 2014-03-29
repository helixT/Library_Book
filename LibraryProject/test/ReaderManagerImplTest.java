/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jduda
 */
public class ReaderManagerImplTest {
    
    ReaderManagerImpl manager;
    private Connection conn;
    
    @Before
    public void setUp() throws SQLException {
        conn = DriverManager.getConnection("jdbc:derby:memory:readerManagerImplDB;create=true");
        conn.prepareStatement("CREATE TABLE READER ("
                + "id bigint primary key generated always as identity,"
                + "fullname varchar(255) not null,"
                + "adress varchar(255) not null,"
                + "phonenumber int)").executeUpdate();
        manager = new ReaderManagerImpl(conn);
    }

    @After
    public void tearDown() throws SQLException {
        conn.prepareStatement("DROP TABLE READER").executeUpdate();        
        conn.close();
    }
    
    @Test
    public void testAddReader(){
        System.out.println("addReader");
        
        assertTrue("Database isn't empty", manager.findAllReaders().isEmpty());
        
        Reader reader = createReader("Petr", "Botanicka 1", 123456789);
        manager.addReader(reader);
        
        Long readerId = reader.getId();
        assertNotNull(readerId);
        
        List<Reader> readers = manager.findAllReaders();
        assertEquals("Count of readers isn't same!", 1, readers.size());
        Reader returnReader = manager.findReaderById(readerId);
        assertEquals("Reader isn't same!", reader, returnReader);
        assertNotSame(reader, returnReader);
        assertAllParametrsEquals(reader, returnReader);
        
        try{
           manager.addReader(null);
           fail("You can add null.");
        } catch(NullPointerException npe){}
        
        try{
            manager.addReader(reader);
            fail("You can input same reader two times!");
        } catch(IllegalArgumentException iae){}
        
        try{
            reader = createReader(null, "Kounicova 4", 234987651);
            manager.addReader(reader);
            fail("Name can't be null!");
        } catch(IllegalArgumentException iae){}
        
        try{
            reader = createReader("Karel", null, 234987651);
            manager.addReader(reader);
            fail("Adress can't be null!");
        } catch(IllegalArgumentException iae){}
        
        try{
            reader = createReader("Karel", "Kounicova 4", -234987651);
            manager.addReader(reader);
            fail("Phone number can't be negative!");
        } catch(IllegalArgumentException iae){}
        
        try{
            reader = createReader("Karel", "Kounicova 4", 43215);
            manager.addReader(reader);
            fail("Phone number must have 9 digits!");
        } catch(IllegalArgumentException iae){}
        
        try{
            reader = createReader("Karel", "Kounicova 4", 0);
            manager.addReader(reader);
            fail("Phone number can't be zero!");
        } catch(IllegalArgumentException iae){}
        
        reader = createReader("Pepa", "Rusna 12", null);
        manager.addReader(reader);
        Reader result = manager.findReaderById(reader.getId()); 
        assertNotNull(result);
    }
    
    @Test
    public void testEditReader(){
        System.out.println("editReader");
        
        Reader reader1 = createReader("Petr", "Botanicka 2", 123456789);
        Reader reader2 = createReader("Karel", "Rusna 4", 362781243);
        manager.addReader(reader1);
        manager.addReader(reader2);
        Long readerId = reader1.getId();
        
        Reader reader = manager.findReaderById(readerId);
        reader.setFullName("Vasek");
        manager.editReader(reader);
        assertEquals("Reader's name isn't same after edit", "Vasek", manager.findReaderById(readerId).getFullName());
        assertEquals("Was updated adress but we want change name", "Botanicka 2", manager.findReaderById(readerId).getAdress());
        assertEquals("Was updated phone number but we want change name", new Integer(123456789), manager.findReaderById(readerId).getPhoneNumber());

        reader = manager.findReaderById(readerId);
        reader.setAdress("Kounicova 3");
        manager.editReader(reader);
        assertEquals("Was updated name but we want change adress", "Vasek", manager.findReaderById(readerId).getFullName());
        assertEquals("Reader's adress isn't same after edit", "Kounicova 3", manager.findReaderById(readerId).getAdress());
        assertEquals("Was updated phone number but we want change adress", new Integer(123456789), manager.findReaderById(readerId).getPhoneNumber());

        reader = manager.findReaderById(readerId);
        reader.setPhoneNumber(987654321);
        manager.editReader(reader);
        assertEquals("Was updated name but we want change phone number", "Vasek", manager.findReaderById(readerId).getFullName());
        assertEquals("Was updated adress but we want change phone number", "Botanicka 2", manager.findReaderById(readerId).getAdress());
        assertEquals("Reader's phone number isn't same after edit", new Integer(987654321), manager.findReaderById(readerId).getPhoneNumber());

        reader = manager.findReaderById(readerId);
        reader.setPhoneNumber(null);
        manager.editReader(reader);
        assertEquals("Was updated name but we want change phone number", "Vasek", manager.findReaderById(readerId).getFullName());
        assertEquals("Was updated adress but we want change phone number", "Botanicka 2", manager.findReaderById(readerId).getAdress());
        assertNull("Reader's phone number isn't null after edit", manager.findReaderById(readerId).getPhoneNumber());

        assertAllParametrsEquals(reader2, manager.findReaderById(reader2.getId()));
    }
    
    @Test
    public void testEditReaderWithWrongParametrs(){
        System.out.println("editReaderWithWrongParametrs");
        Reader reader1 = createReader("Petr", "Botanicka 2", 123456789);
        Reader reader2 = createReader("Karel", "Rusna 4", 362781243);
        manager.addReader(reader1);
        manager.addReader(reader2);
        Long readerId = reader1.getId();
        
        try{
            manager.editReader(null);
            fail("You can edit null!");
        }catch(IllegalArgumentException iae){}
        
        try{
            Reader reader = manager.findReaderById(readerId);
            reader.setId(null);
            manager.editReader(reader);
            fail("You can change id of reader to null!");
        }catch(IllegalArgumentException iae){}
        
        try{
            Reader reader = manager.findReaderById(readerId);
            reader.setId(new Long(4));
            manager.editReader(reader);
            fail("You can change id of reader!");
        }catch(IllegalArgumentException iae){}
        
        try{
            Reader reader = manager.findReaderById(readerId);
            reader.setFullName(null);
            manager.editReader(reader);
            fail("You can change name of reader to null!");
        }catch(IllegalArgumentException iae){}
        
        try{
            Reader reader = manager.findReaderById(readerId);
            reader.setAdress(null);
            manager.editReader(reader);
            fail("You can change adress of reader to null!");
        }catch(IllegalArgumentException iae){}
        
        try{
            Reader reader = manager.findReaderById(readerId);
            reader.setPhoneNumber(12334);
            manager.editReader(reader);
            fail("You can change phone number of reader to wrong format!");
        }catch(IllegalArgumentException iae){}
    }
    
    @Test
    public void testDeleteReader(){
        System.out.println("deleteReader");
        Reader reader1 = createReader("Petr", "Botanicka 2", 123456789);
        Reader reader2 = createReader("Adam", "Kounicova 1", 987654321);
        manager.addReader(reader1);
        manager.addReader(reader2);
        
        Reader failedReader = createReader("Karel", "Srbska 4", 123);
        
        try{
            manager.deleteReader(null);
            fail("You can delete null from database!");
        }catch(NullPointerException npe){}
        
        try{
            manager.deleteReader(failedReader);
            fail("You can delete reader who isn't in database!");
        } catch(IllegalArgumentException iae){}
        
        manager.deleteReader(reader2);
        assertNull("Reader does't delete from database", manager.findReaderById(reader2.getId()));
        List<Reader> readers = manager.findAllReaders();
        assertEquals("Count of readers isn't same!", 1, readers.size());
        Reader returnReader = manager.findReaderById(reader1.getId());
        assertEquals("Reader isn't same!", reader1, returnReader);
        assertAllParametrsEquals(reader1, returnReader);
        
        Reader reader = createReader("Franta", "Rusna 3", 564738291);
        manager.addReader(reader);
        Long readerId = reader.getId();
                
        try{
            reader = manager.findReaderById(readerId);
            reader.setId(readerId - 1);
            manager.deleteReader(reader);
            fail("You can delete reader with wrong id!");
        } catch(IllegalArgumentException iae){}
        
        try{
            reader = manager.findReaderById(readerId);
            reader.setFullName("Pepa");
            manager.deleteReader(reader);
            fail("You can delete reader with wrong name!");
        } catch(IllegalArgumentException iae){}
        
        try{
            reader = manager.findReaderById(readerId);
            reader.setAdress("Masna 5");
            manager.deleteReader(reader);
            fail("You can delete reader with wrong adress!");
        } catch(IllegalArgumentException iae){}
        
        try{
            reader = manager.findReaderById(readerId);
            reader.setPhoneNumber(394827104);
            manager.deleteReader(reader);
            fail("You can delete reader with wrong phone number!");
        } catch(IllegalArgumentException iae){}
    }
    
    @Test
    public void testFindReaderById(){
        System.out.println("findReaderById");
        Reader reader1 = createReader("Petr", "Botanicka 2", 123456789);
        Reader reader2 = createReader("Adam", "Kounicova 1", 987654321);
        manager.addReader(reader1);
        manager.addReader(reader2);
        
        Reader foundedReader = manager.findReaderById(new Long(1));
        assertEquals("Reader isn't same!", reader1, foundedReader);
        assertAllParametrsEquals(reader1, foundedReader);
        
        assertNull("Was found reader who does't exist", manager.findReaderById(new Long(5)));
        
        try{
            Reader negativeIdReader = manager.findReaderById(new Long(-1));
            fail("Was founded reader with negative id");
        } catch(IllegalArgumentException iae){}
    }
    
    @Test
    public void testFindReaderByName(){
        System.out.println("findReaderByName");
        Reader reader1 = createReader("Petr", "Botanicka 2", 123456789);
        Reader reader2 = createReader("Petr", "Kounicova 1", 987654321);
        Reader reader3 = createReader("Pepa", "Srbska 3", 543216789);
        manager.addReader(reader1);
        manager.addReader(reader2);
        manager.addReader(reader3);
        
        List<Reader> expReaders = Arrays.asList(reader1, reader2);
        List<Reader> foundedReaders = manager.findReaderByName("Petr");
        
        Collections.sort(expReaders, idComparator);
        Collections.sort(foundedReaders, idComparator);
        
        assertEquals("Size of lists isn't same!", expReaders.size(), foundedReaders.size());
        assertEquals("Reader isn't same!", expReaders, foundedReaders);
        assertAllReadersEquals(expReaders, foundedReaders);
        
        assertNull("Was founded reader who doesn't exist!", manager.findReaderByName("Karel"));
        
        try{
            manager.findReaderByName(null);
            fail("Was founded reader with name null!");
        } catch(IllegalArgumentException iae){}
    }
    
    @Test
    public void testFindAllReaders(){
        System.out.println("getAllReaders");
        
        assertEquals("Someone is in database!", 0, manager.findAllReaders().size());
        
        Reader reader1 = createReader("Petr", "Botanicka 2", 123456789);
        Reader reader2 = createReader("Karel", "Kounicova 1", 987654321);
        Reader reader3 = createReader("Pepa", "Srbska 3", 543216789);
        manager.addReader(reader1);
        manager.addReader(reader2);
        manager.addReader(reader3);
        
        List<Reader> expReaders = Arrays.asList(reader1, reader2, reader3);
        List<Reader> returnedReaders = manager.findAllReaders();
        
        Collections.sort(expReaders,idComparator);
        Collections.sort(returnedReaders,idComparator);
        
        assertEquals("Count of readers isn't same!", expReaders.size(), returnedReaders.size());
        assertEquals("Lists aren't same!", expReaders, returnedReaders);
        assertAllReadersEquals(expReaders, returnedReaders);
    }

    private void assertAllParametrsEquals(Reader expected, Reader actual) {
        assertEquals("Id of readers isn't same!", expected.getId(), actual.getId());
        assertEquals("Name of readers isn't same!", expected.getFullName(), actual.getFullName());
        assertEquals("Adress of readers isn't same!", expected.getAdress(), actual.getAdress());
        assertEquals("Phone number of readers isn't same!", expected.getPhoneNumber(), actual.getPhoneNumber());
    }

    private void assertAllReadersEquals(List<Reader> expReaders, List<Reader> actualReaders) {
        for (int i = 0; i < expReaders.size(); i++) {
            Reader expReader = expReaders.get(i);
            Reader actualReader = actualReaders.get(i);
            assertAllParametrsEquals(expReader, actualReader);
        }
    }
    
    private static Comparator<Reader> idComparator = new Comparator<Reader>() {

        @Override
        public int compare(Reader reader1, Reader reader2) {
            return Long.valueOf(reader1.getId()).compareTo(Long.valueOf(reader2.getId()));
        }
    };

    private Reader createReader(String fullName, String adress, Integer phoneNumber){
        Reader reader = new Reader();
        reader.setFullName(fullName);
        reader.setAdress(adress);
        reader.setPhoneNumber(phoneNumber);
        return reader;
    }
}
