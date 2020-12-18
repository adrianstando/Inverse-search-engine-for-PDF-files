package tests;

import files.FileContent;
import files.FileFinder;
import files.FileReader;
import indexing.*;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class InvertedIndexTest {
    private final String poison = "THIS_IS_THE_END.non_existing_extension";

    @Test
    void invertedIndexTest(){
        BlockingQueue<FileContent> filesContent = new LinkedBlockingQueue<>();
        try {
            filesContent.put(new FileContent(new File("path0"), "content0 bla bla bla", "author0"));
            filesContent.put(new FileContent(new File("path1"), "content0 xd xd xd", "author0"));
            filesContent.put(new FileContent(new File("path2"), "content0 ee ee ee", "author1"));
            filesContent.put(new FileContent(new File("path3"), "content0 pl pl pl", "author1"));

            filesContent.put(new FileContent(new File(poison), "", ""));

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ConcurrentHashMap<String, Word> map = new ConcurrentHashMap<>();
        WordsDictionary wordsDictionary = new WordsDictionary(map);

        ConcurrentHashMap<String, Author> map1 = new ConcurrentHashMap<>();
        AuthorDictionary authorDictionary = new AuthorDictionary(map1);

        InvertedIndex invertedIndex = new InvertedIndex(filesContent, wordsDictionary, authorDictionary);

        Thread thread = new Thread(invertedIndex);
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertEquals(0, filesContent.size());
        assertEquals(Arrays.stream(new String[] {"content0", "bla", "xd", "pl", "ee"}).collect(Collectors.toSet()),
                wordsDictionary.getAllUniqueKeys());

        assertEquals(Arrays.stream(new String[] {"author0", "author1"}).collect(Collectors.toSet()),
                authorDictionary.getAllUniqueKeys());

        assertEquals(Arrays.stream(new String[] {"path2", "path3"}).collect(Collectors.toSet()),
                authorDictionary.getFilesWith("author1"));

        assertEquals(Arrays.stream(new String[] {"path0", "path1"}).collect(Collectors.toSet()),
                authorDictionary.getFilesWith("author0"));

        HashMap<String, List<Integer>> check = new HashMap<>();
        check.put("path0", Arrays.stream(new Integer[] {0}).collect(Collectors.toList()));
        check.put("path1", Arrays.stream(new Integer[] {0}).collect(Collectors.toList()));
        check.put("path2", Arrays.stream(new Integer[] {0}).collect(Collectors.toList()));
        check.put("path3", Arrays.stream(new Integer[] {0}).collect(Collectors.toList()));
        assertEquals(check , wordsDictionary.getPositionsOfTheWord("content0"));
    }

    @Test
    void runWholeProcedureWithFewThreads(){
        String pathZero = "./src/tests/testFiles";
        int numberOfReadingThreads = 2;
        int numberOfIndexingThreads = 3;


        BlockingQueue<File> files = new ArrayBlockingQueue<>(1);
        BlockingQueue<FileContent> filesContent = new LinkedBlockingQueue<>();


        FileFinder fileFinder = new FileFinder(pathZero, numberOfReadingThreads, files);
        Thread fileFinderThread = new Thread(fileFinder);

        Thread[] fileReaderThreads = new Thread[numberOfReadingThreads];
        FileReader[] fileReaders = new FileReader[numberOfReadingThreads];
        for(int i = 0; i < numberOfReadingThreads; i++){
            fileReaders[i] = new FileReader(files, filesContent);
            fileReaderThreads[i] = new Thread(fileReaders[i]);
        }

        // jesli mamy wiecej indeksujacych - w przypadku tego testu TAK
        if(numberOfIndexingThreads > numberOfReadingThreads){
            fileReaders[0].putExtraPoison(numberOfIndexingThreads - numberOfReadingThreads);
        }

        fileFinderThread.start();
        for(Thread thread : fileReaderThreads){
            thread.start();
        }



        ConcurrentHashMap<String, Word> map = new ConcurrentHashMap<>();
        WordsDictionary wordsDictionary = new WordsDictionary(map);

        ConcurrentHashMap<String, Author> map1 = new ConcurrentHashMap<>();
        AuthorDictionary authorDictionary = new AuthorDictionary(map1);

        Thread[] indexingThreads = new Thread[numberOfIndexingThreads];
        for(int i = 0; i < numberOfIndexingThreads; i++){
            indexingThreads[i] = new Thread(new InvertedIndex(filesContent, wordsDictionary, authorDictionary));
            indexingThreads[i].start();
        }

        try {
            for(Thread thread : indexingThreads){
                thread.join();
            }
            fileFinderThread.join();
            for(Thread thread : fileReaderThreads) {
                thread.join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertEquals(0, filesContent.size());

        assertEquals(2, authorDictionary.getAllUniqueKeys().size());
        assertTrue(authorDictionary.getDictionary().containsKey("Autor Pliku nr 1"));
        assertTrue(authorDictionary.getDictionary().containsKey("Autor pliku nr 2"));

        assertEquals(Arrays.stream(new String[] {"./src/tests/testFiles/folder2/test1.pdf"}).collect(Collectors.toSet()),
                authorDictionary.getFilesWith("Autor pliku nr 2"));

        assertEquals(Arrays.stream(new String[] {"./src/tests/testFiles/folder1/test.pdf"}).collect(Collectors.toSet()),
                authorDictionary.getFilesWith("Autor Pliku nr 1"));

        assertEquals(3, wordsDictionary.getPositionsOfWordInPath("bla", "./src/tests/testFiles/folder1/test.pdf").size());
        assertEquals(6, wordsDictionary.getPositionsOfWordInPath("bla", "./src/tests/testFiles/folder2/test1.pdf").size());

        assertEquals(Thread.State.TERMINATED, fileFinderThread.getState());
        for(Thread thread : fileReaderThreads){
            assertEquals(Thread.State.TERMINATED, thread.getState());
        }
        for(int i = 0; i < numberOfIndexingThreads; i++){
            assertEquals(Thread.State.TERMINATED, indexingThreads[i].getState());
        }
    }

}