package tests;

import indexing.AuthorDictionary;
import indexing.WordsDictionary;
import org.junit.jupiter.api.Test;
import controller.Controller;

import static org.junit.jupiter.api.Assertions.*;

class ControllerTest {

    @Test
    void writeAndReadObjectsTest(){
        Controller controller = new Controller();
        controller.createIndex("./src/tests/testFiles");

        boolean b;

        b = controller.writeDictionariesToFile("./src/tests/testFiles/dictionaries.bin");
        assertTrue(b);

        WordsDictionary wordsDictionary1 = controller.getWordsDictionary();
        AuthorDictionary authorDictionary1 = controller.getAuthorDictionary();

        Controller controller2 = new Controller();
        controller2.createIndex("./src/tests/testFiles");

        b = controller2.readDictionariesFromFile("./src/tests/testFiles/dictionaries.bin");
        assertTrue(b);

        WordsDictionary wordsDictionary2 = controller2.getWordsDictionary();
        AuthorDictionary authorDictionary2 = controller2.getAuthorDictionary();

        assertEquals(wordsDictionary1.getDictionary(), wordsDictionary2.getDictionary());
        assertEquals(authorDictionary1.getDictionary(), authorDictionary2.getDictionary());
    }

}