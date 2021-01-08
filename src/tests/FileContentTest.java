package tests;


import main.java.inverted_index_search_engine.files.FileContent;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class FileContentTest {

    FileContent fileContent = new FileContent(new File("XXX"), "it is file content", "i am the author");
    FileContent fileContent2 = new FileContent(new File("/wyklad/wykład1.pdf"), "witam na wykładzie", "i am the author");

    @Test
    void getFile() {
        assertEquals(fileContent.getFile(), new File("XXX"));
        assertNotEquals(fileContent.getFile(), new File("XXX/XXX"));

        assertEquals(fileContent2.getFile(), new File("/wyklad/wykład1.pdf"));
        assertNotEquals(fileContent2.getFile(), new File("wykład1.pdf"));
    }

    @Test
    void getContent() {
        assertEquals(fileContent.getContent(), "it is file content");
        assertNotEquals(fileContent.getContent(), "i am the author");

        assertEquals(fileContent2.getContent(), "witam na wykładzie");
        assertNotEquals(fileContent2.getContent(), "i am the author");
    }

    @Test
    void getAuthor() {
        assertNotEquals(fileContent.getAuthor(), "it is file content");
        assertEquals(fileContent.getAuthor(), "i am the author");

        assertNotEquals(fileContent2.getAuthor(), "witam na wykładzie");
        assertEquals(fileContent2.getAuthor(), "i am the author");
    }
}