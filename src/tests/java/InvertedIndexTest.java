package tests.java;


import main.java.inverted_index_search_engine.files.FileContent;
import main.java.inverted_index_search_engine.files.FileFinder;
import main.java.inverted_index_search_engine.files.FileReader;
import main.java.inverted_index_search_engine.indexing.AuthorDictionary;
import main.java.inverted_index_search_engine.indexing.InvertedIndex;
import main.java.inverted_index_search_engine.indexing.WordsDictionary;
import org.junit.jupiter.api.Test;
import org.tartarus.snowball.ext.PorterStemmer;

import java.io.File;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class InvertedIndexTest {
    private final String poison = "THIS_IS_THE_END.non_existing_extension";

    private static List<String> commonWords = Arrays.stream(new String [] {"a", "able", "about", "all", "an", "and", "any", "are", "aren't", "isn't", "as", "at", "be", "been", "by",
            "can", "can't", "could", "couldn't", "do", "does", "doesn't", "don't", "down", "has", "hasn't", "have", "haven't", "he", "here", "his", "how",
            "I", "I'm", "if", "in", "is", "it", "its", "it's", "just", "like", "many", "much", "no", "not", "now", "of", "on", "one",
            "or", "she", "so", "than", "that", "the", "them", "then", "there", "these", "they", "this", "those", "to", "too", "up", "very", "was", "we", "were",
            "what", "when", "where", "which", "who", "will", "won't", "would", "you", "you'd", "you'll"}).map(String::toLowerCase)
            .map(r -> r.replaceAll("[^\\p{IsAlphabetic})]+", "")).collect(Collectors.toList());

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

        WordsDictionary wordsDictionary = new WordsDictionary();

        AuthorDictionary authorDictionary = new AuthorDictionary();

        InvertedIndex invertedIndex = new InvertedIndex(filesContent, wordsDictionary, authorDictionary, commonWords, true);

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
        String pathZero = "./src/tests/resources";
        int numberOfReadingThreads = 2;
        int numberOfIndexingThreads = 3;


        BlockingQueue<File> files = new ArrayBlockingQueue<>(1);
        BlockingQueue<FileContent> filesContent = new LinkedBlockingQueue<>();


        FileFinder fileFinder = new FileFinder(pathZero, numberOfReadingThreads, files, 200);
        Thread fileFinderThread = new Thread(fileFinder);

        Thread[] fileReaderThreads = new Thread[numberOfReadingThreads];
        FileReader[] fileReaders = new FileReader[numberOfReadingThreads];
        for(int i = 0; i < numberOfReadingThreads; i++){
            fileReaders[i] = new FileReader(files, filesContent);
            fileReaderThreads[i] = new Thread(fileReaders[i]);
        }

        // in case we have more indexing threads
        if(numberOfIndexingThreads > numberOfReadingThreads){
            fileReaders[0].putExtraPoison(numberOfIndexingThreads - numberOfReadingThreads);
        }

        fileFinderThread.start();
        for(Thread thread : fileReaderThreads){
            thread.start();
        }



        WordsDictionary wordsDictionary = new WordsDictionary();

        AuthorDictionary authorDictionary = new AuthorDictionary();

        Thread[] indexingThreads = new Thread[numberOfIndexingThreads];
        for(int i = 0; i < numberOfIndexingThreads; i++){
            indexingThreads[i] = new Thread(new InvertedIndex(filesContent, wordsDictionary, authorDictionary, commonWords, true));
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
        assertTrue(authorDictionary.getDictionary().containsKey("author 1"));
        assertTrue(authorDictionary.getDictionary().containsKey("author 2"));

        assertEquals(Arrays.stream(new String[] {"./src/tests/resources/folder2/test1.pdf"}).collect(Collectors.toSet()),
                authorDictionary.getFilesWith("author 2"));

        assertEquals(Arrays.stream(new String[] {"./src/tests/resources/folder1/test.pdf"}).collect(Collectors.toSet()),
                authorDictionary.getFilesWith("author 1"));

        assertEquals(1, wordsDictionary.getPositionsOfWordInPath(stemWord("dursley"), "./src/tests/resources/folder1/test.pdf").size());
        assertEquals(2, wordsDictionary.getPositionsOfWordInPath(stemWord("boulevard"), "./src/tests/resources/folder2/test1.pdf").size());

        assertEquals(Thread.State.TERMINATED, fileFinderThread.getState());
        for(Thread thread : fileReaderThreads){
            assertEquals(Thread.State.TERMINATED, thread.getState());
        }
        for(int i = 0; i < numberOfIndexingThreads; i++){
            assertEquals(Thread.State.TERMINATED, indexingThreads[i].getState());
        }
    }

    @Test
    void checkStemming(){
        String pathZero = "./src/tests/resources/folder3";
        int numberOfReadingThreads = 2;
        int numberOfIndexingThreads = 3;


        BlockingQueue<File> files = new ArrayBlockingQueue<>(1);
        BlockingQueue<FileContent> filesContent = new LinkedBlockingQueue<>();


        FileFinder fileFinder = new FileFinder(pathZero, numberOfReadingThreads, files, 200);
        Thread fileFinderThread = new Thread(fileFinder);

        Thread[] fileReaderThreads = new Thread[numberOfReadingThreads];
        FileReader[] fileReaders = new FileReader[numberOfReadingThreads];
        for(int i = 0; i < numberOfReadingThreads; i++){
            fileReaders[i] = new FileReader(files, filesContent);
            fileReaderThreads[i] = new Thread(fileReaders[i]);
        }

        // in case we have more indexing threads
        if(numberOfIndexingThreads > numberOfReadingThreads){
            fileReaders[0].putExtraPoison(numberOfIndexingThreads - numberOfReadingThreads);
        }

        fileFinderThread.start();
        for(Thread thread : fileReaderThreads){
            thread.start();
        }



        WordsDictionary wordsDictionary = new WordsDictionary();

        AuthorDictionary authorDictionary = new AuthorDictionary();

        Thread[] indexingThreads = new Thread[numberOfIndexingThreads];
        for(int i = 0; i < numberOfIndexingThreads; i++){
            indexingThreads[i] = new Thread(new InvertedIndex(filesContent, wordsDictionary, authorDictionary, commonWords, true));
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

        // no author
        assertEquals(0, authorDictionary.getAllUniqueKeys().size());

        assertEquals(19, wordsDictionary.getDictionary().keySet().size());

        assertEquals(2, wordsDictionary.getDictionary().get(stemWord("alone")).
                getPositionsOfTheWordInFile("./src/tests/resources/folder3/x.pdf").size());
    }


    private PorterStemmer porterStemmer = new PorterStemmer();
    private String stemWord(String word){
        porterStemmer.setCurrent(word);
        porterStemmer.stem();
        return porterStemmer.getCurrent();
    }

}