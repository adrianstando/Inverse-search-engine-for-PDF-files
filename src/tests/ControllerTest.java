package tests;

import main.java.inverted_index_search_engine.controller.Controller;
import main.java.inverted_index_search_engine.indexing.AuthorDictionary;
import main.java.inverted_index_search_engine.indexing.WordsDictionary;
import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.*;

class ControllerTest {

    @Test
    void writeAndReadObjectsTest(){
        Controller controller = new Controller(true);
        controller.createIndex("./src/tests/testFiles");

        boolean b;

        b = controller.writeDictionariesToFile("./src/tests/testFiles/dictionaries.ser");
        assertTrue(b);

        WordsDictionary wordsDictionary1 = controller.getWordsDictionary();
        AuthorDictionary authorDictionary1 = controller.getAuthorDictionary();

        Controller controller2 = new Controller(true);
        controller2.createIndex("./src/tests/testFiles");

        b = controller2.readDictionariesFromFile("./src/tests/testFiles/dictionaries.ser");
        assertTrue(b);

        WordsDictionary wordsDictionary2 = controller2.getWordsDictionary();
        AuthorDictionary authorDictionary2 = controller2.getAuthorDictionary();

        assertEquals(wordsDictionary1.getDictionary(), wordsDictionary2.getDictionary());
        assertEquals(authorDictionary1.getDictionary(), authorDictionary2.getDictionary());
        assertEquals(controller.getCurrentPath(), controller2.getCurrentPath());
        assertEquals(controller.isEnableStemmer(), controller2.isEnableStemmer());
    }

    @Test
    void readFromWrongFile(){
        Controller controller = new Controller(true);
        boolean b = controller.readDictionariesFromFile("./src/tests/testFiles/x.ser");
        assertFalse(b);

        assertNull(controller.getCurrentPath());
    }

}