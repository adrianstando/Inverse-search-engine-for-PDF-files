package tests;

import files.FileContent;
import files.FileFinder;
import files.FileReader;
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
        File filePoison = new File(poison);

        int k = 0;
        for (FileContent x : list){
            if (x.getFile().equals(filePoison)){
                assertEquals("", x.getAuthor());
                assertEquals("", x.getContent());
                k++;
            } else if (x.getFile().equals(file1)){
                //String conent = "to jest przykładowy plik pdf stworzony na potrzeby testów programu inverted index search engine a tu kilka kolejnych słów bla bla bla mini pw 123 g56 woda źródlana boże narodzenie i jeszcze kilka innych słów które można dodać ale już nie dodam bo to jest przykładowy plik pdf dla programu inverted index search engine";
                String conent = "To jest przykładowy plik PDF stworzony na potrzeby testów programu Inverted index search \n" +
                        "engine.\n" +
                        "A tu kilka kolejnych słów:\n" +
                        "bla bla bla\n" +
                        "MiNI\n" +
                        "PW\n" +
                        "123 g56\n" +
                        "woda źródlana\n" +
                        "Boże Narodzenie\n" +
                        "I jeszcze kilka innych słów, które można dodać, ale już nie dodam, bo to jest przykładowy plik PDF\n" +
                        "dla programu Inverted Index search enGine.\n";
                assertEquals(x.getContent(), conent);

                String author = "Autor Pliku nr 1";
                assertEquals(x.getAuthor(), author);
            } else if (x.getFile().equals(file2)){
                //String conent = "to jest przykładowy plik pdf stworzony na potrzeby testów programu inverted index search engine a tu kilka kolejnych słów bla bla bla mini pw 123 g56 woda źródlana boże narodzenie i jeszcze kilka innych słów które można dodać ale już nie dodam bo to jest przykładowy plik pdf dla programu inverted index search engine a to jest drugi przykładowy plik z dodatkowymi linijkami bla bla bla mini pw woda qqqq\n";
                String conent = "To jest przykładowy plik PDF stworzony na potrzeby testów programu Inverted index search \n" +
                        "engine.\n" +
                        "A tu kilka kolejnych słów:\n" +
                        "bla bla bla\n" +
                        "MiNI\n" +
                        "PW\n" +
                        "123 g56\n" +
                        "woda źródlana\n" +
                        "Boże Narodzenie\n" +
                        "I jeszcze kilka innych słów, które można dodać, ale już nie dodam, bo to jest przykładowy plik PDF\n" +
                        "dla programu Inverted Index search enGine.\n" +
                        "A to jest drugi przykładowy plik z dodatkowymi linijkami.\n" +
                        "Bla bla bla\n" +
                        "mini pw\n" +
                        "woda \n" +
                        "qqqq\n";

                assertEquals(x.getContent(), conent);

                String author = "Autor pliku nr 2";
                assertEquals(x.getAuthor(), author);
            } else {
                assertEquals(1, 0);
            }
        }
        assertEquals(4, k); // dwa pliki i dwa poisony z intPutExtraPoison
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