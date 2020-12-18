package tests;

import files.FileFinder;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static org.junit.jupiter.api.Assertions.*;

class FileFinderTest {
    private final String poison = "THIS_IS_THE_END.non_existing_extension";

    @Test
    void findFiles(){
        BlockingQueue<File> files = new LinkedBlockingQueue<>();

        FileFinder fileFinder = new FileFinder("./src/tests/testFiles", 1,  files);

        Thread thread = new Thread(fileFinder);
        thread.start();

        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ArrayList<File> list = new ArrayList<>(files);
        assertEquals(4, list.size()); // three files plus poison pill

        File file1 = new File("./src/tests/testFiles/folder1/test.pdf");
        File file2 = new File("./src/tests/testFiles/folder2/test1.pdf");
        File filePoison = new File(poison);

        assertTrue(list.contains(file1));
        assertTrue(list.contains(file2));
        assertTrue(list.contains(filePoison));

    }

    @Test
    void findFilesMoreThreads(){
        BlockingQueue<File> files = new LinkedBlockingQueue<>();

        FileFinder fileFinder = new FileFinder("./src/tests/testFiles", 3,  files);

        Thread thread = new Thread(fileFinder);
        thread.start();

        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ArrayList<File> list = new ArrayList<>(files);
        assertEquals(6, list.size()); // three files plus three poison pills

        File file1 = new File("./src/tests/testFiles/folder1/test.pdf");
        File file2 = new File("./src/tests/testFiles/folder2/test1.pdf");
        File filePoison = new File(poison);

        assertTrue(list.contains(file1));
        assertTrue(list.contains(file2));

        int k = 0;
        for (File x : list){
            if (x.equals(filePoison)){
                k++;
            }
        }
        assertEquals(3, k);

    }
}