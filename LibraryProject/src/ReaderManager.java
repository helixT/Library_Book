/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package projekt;

import java.util.List;

/**
 *
 * @author jduda
 */
public interface ReaderManager {
    void addReader(Reader reader);
    void editReader(Reader reader);
    void deleteReader(Reader reader);
    Reader findReaderById(Long id);
    List<Reader> findReaderByName(String name);
    List<Reader> findAllReaders();
}
