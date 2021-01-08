package tests;


import main.java.inverted_index_search_engine.files.FileContent;
import main.java.inverted_index_search_engine.files.FileFinder;
import main.java.inverted_index_search_engine.files.FileReader;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static org.junit.jupiter.api.Assertions.*;

class FileReaderTest {

    private final String poison = "THIS_IS_THE_END.non_existing_extension";

    @Test
    void extraPillsTest(){
        BlockingQueue<File> files = new LinkedBlockingQueue<>();
        BlockingQueue<FileContent> filesContent = new LinkedBlockingQueue<>();
        int intPutExtraPoison = 2;

        FileFinder fileFinder = new FileFinder("./src/tests/testFiles", 1,  files);

        Thread thread = new Thread(fileFinder);
        thread.start();

        FileReader fileReader = new FileReader(files, filesContent);
        fileReader.putExtraPoison(intPutExtraPoison);

        Thread threadReading = new Thread(fileReader);
        threadReading.start();

        try {
            thread.join();
            threadReading.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        List<FileContent> list = new ArrayList<>(filesContent);
        File filePoison = new File(poison);

        int k = 0;
        for (FileContent x : list){
            if (x.getFile().equals(filePoison)){
                assertEquals("", x.getAuthor());
                assertEquals("", x.getContent());
                k++;
            }
        }
        assertEquals(3, k);
    }

    @Test
    void contentTest(){
        BlockingQueue<File> files = new LinkedBlockingQueue<>();
        BlockingQueue<FileContent> filesContent = new LinkedBlockingQueue<>();
        int intPutExtraPoison = 2;
        int numberOfReadingThreads = 2;

        FileFinder fileFinder = new FileFinder("./src/tests/testFiles", numberOfReadingThreads,  files);

        Thread thread = new Thread(fileFinder);
        thread.start();

        FileReader[] fileReader = new FileReader[numberOfReadingThreads];
        for (int i = 0; i < numberOfReadingThreads; i++){
            fileReader[i] = new FileReader(files, filesContent);
        }

        fileReader[0].putExtraPoison(intPutExtraPoison);

        Thread[] threadReading = new Thread[numberOfReadingThreads];
        for (int i = 0; i < numberOfReadingThreads; i++){
            threadReading[i] = new Thread(fileReader[i]);
            threadReading[i].start();
        }


        try {
            thread.join();
            for (int i = 0; i < numberOfReadingThreads; i++){
                threadReading[i].join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        List<FileContent> list = new ArrayList<>(filesContent);
        File file1 = new File("./src/tests/testFiles/folder1/test.pdf");
        File file2 = new File("./src/tests/testFiles/folder2/test1.pdf");
        File file3 = new File("./src/tests/testFiles/folder3/x.pdf");
        File filePoison = new File(poison);

        int k = 0;
        for (FileContent x : list){
            if (x.getFile().equals(filePoison)){
                assertEquals("", x.getAuthor());
                assertEquals("", x.getContent());
                k++;
            } else if (x.getFile().equals(file1)){
                String conent = "THE BOY WHO LIVED\n" +
                        "Mr. and Mrs. Dursley, of number four, Privet Drive, \n" +
                        "were proud to say that they were perfectly normal, \n" +
                        "thank you very much. They were the last people you’d \n" +
                        "expect to be involved in anything strange or \n" +
                        "mysterious, because they just didn’t hold with such \n" +
                        "nonsense.\n";
                assertEquals(x.getContent(), conent);

                String author = "author 1";
                assertEquals(x.getAuthor(), author);
            } else if (x.getFile().equals(file2)){
                String conent = "THE BOY WHO LIVED\n" +
                        "Mr. and Mrs. Dursley, of number four, Privet Drive, \n" +
                        "were proud to say that they were perfectly normal, \n" +
                        "thank you very much. They were the last people you’d \n" +
                        "expect to be involved in anything strange or \n" +
                        "mysterious, because they just didn’t hold with such \n" +
                        "nonsense.\n" +
                        "Boulevard of Broken Dreams\n" +
                        "I walk a lonely road\n" +
                        "The only one that I have ever known\n" +
                        "Don't know where it goes\n" +
                        "But it's home to me, and I walk alone\n" +
                        "I walk this empty street\n" +
                        "On the Boulevard of Broken Dreams\n" +
                        "Where the city sleeps\n" +
                        "And I'm the only one, and I walk alone\n" +
                        "And some additional words in this file:\n" +
                        "perfect normal proud were\n" +
                        "thank you people\n";

                assertEquals(x.getContent(), conent);

                String author = "author 2";
                //assertEquals(x.getAuthor(), author);
            } else if (x.getFile().equals(file3)){
                String conent = "Boulevard of Broken Dreams\n" +
                        "I walk a lonely road\n" +
                        "The only one that I have ever known\n" +
                        "Don't know where it goes\n" +
                        "But it's home to me, and I walk alone\n" +
                        "I walk this empty street\n" +
                        "On the Boulevard of Broken Dreams\n" +
                        "Where the city sleeps\n" +
                        "And I'm the only one, and I walk alone\n";

                assertEquals(x.getContent(), conent);

                String author = "";
                assertEquals(x.getAuthor(), author);
            }
            else {
                assertEquals(1, 0);
            }
        }

        // number of poison pills in the output
        assertEquals(intPutExtraPoison + numberOfReadingThreads, k); // three files and two poison pills from intPutExtraPoison
    }

    @Test
    void threadTerminatedAtTheEnd(){
        BlockingQueue<File> files = new LinkedBlockingQueue<>();
        BlockingQueue<FileContent> filesContent = new LinkedBlockingQueue<>();
        int intPutExtraPoison = 2;
        int numberOfReadingThreads = 2;

        FileFinder fileFinder = new FileFinder("./src/tests/testFiles", numberOfReadingThreads,  files);

        Thread thread = new Thread(fileFinder);
        thread.start();

        FileReader[] fileReader = new FileReader[numberOfReadingThreads];
        for (int i = 0; i < numberOfReadingThreads; i++){
            fileReader[i] = new FileReader(files, filesContent);
        }

        Thread[] threadReading = new Thread[numberOfReadingThreads];
        for (int i = 0; i < numberOfReadingThreads; i++){
            threadReading[i] = new Thread(fileReader[i]);
            threadReading[i].start();
        }


        try {
            thread.join();
            for (int i = 0; i < numberOfReadingThreads; i++){
                threadReading[i].join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertEquals(Thread.State.TERMINATED, thread.getState());
        for (int i = 0; i < numberOfReadingThreads; i++){
            assertEquals(Thread.State.TERMINATED, threadReading[i].getState());
        }
    }
}