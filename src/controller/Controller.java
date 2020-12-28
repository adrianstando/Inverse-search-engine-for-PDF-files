package controller;

import files.*;
import files.FileReader;
import indexing.*;
import searching.Search;

import java.io.*;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

/**
 * Class which is something like API for the Inverted Index program.
 */
public class Controller {
    private final static String poison = "THIS_IS_THE_END.non_existing_extension";
    private WordsDictionary wordsDictionary;
    private AuthorDictionary authorDictionary;
    private Search search;
    private boolean dictionariesCreated = false;

    private static List<String> commonWords = Arrays.stream(new String [] {"a", "able", "about", "all", "an", "and", "any", "are", "aren't", "isn't", "as", "at", "be", "been", "by",
            "can", "can't", "could", "couldn't", "do", "does", "doesn't", "don't", "down", "has", "hasn't", "have", "haven't", "he", "here", "his", "how",
            "I", "I'm", "if", "in", "is", "it", "its", "it's", "just", "like", "many", "much", "no", "not", "now", "of", "on", "one",
            "or", "she", "so", "than", "that", "the", "them", "then", "there", "these", "they", "this", "those", "to", "too", "up", "very", "was", "we", "were",
            "what", "when", "where", "which", "who", "will", "won't", "would", "you", "you'd", "you'll"}).map(String::toLowerCase)
            .map(r -> r.replaceAll("[^\\p{IsAlphabetic})]+", "")).collect(Collectors.toList());


    public Controller(){

    }

    /**
     * Method which builds index from pathZero.
     * @param pathZero
     */
    public void createIndex(String pathZero){
        // set number of threads
        int numberOfReadingThreads = 2;
        int numberOfIndexingThreads = 4;

        // initialize queues
        BlockingQueue<File> files = new ArrayBlockingQueue<>(20);
        BlockingQueue<FileContent> filesContent = new LinkedBlockingQueue<>();

        // and program creates and starts threads
        FileFinder fileFinder = new FileFinder(pathZero, numberOfReadingThreads, files);
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

        // create dictionaries
        ConcurrentHashMap<String, Word> map = new ConcurrentHashMap<>();
        this.wordsDictionary = new WordsDictionary(map);

        ConcurrentHashMap<String, Author> map1 = new ConcurrentHashMap<>();
        this.authorDictionary = new AuthorDictionary(map1);

        // start threads building InvertedIndex
        Thread[] indexingThreads = new Thread[numberOfIndexingThreads];
        for(int i = 0; i < numberOfIndexingThreads; i++){
            indexingThreads[i] = new Thread(new InvertedIndex(filesContent, wordsDictionary, authorDictionary, commonWords));
            indexingThreads[i].start();
        }

        // wait until threads stop
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

        this.search = new Search(authorDictionary, wordsDictionary);
        dictionariesCreated = true;
    }

    /**
     * Method searches files created by author.
     * @param author
     * @return list
     */
    public List<String> searchAuthor(String author){
        if(!dictionariesCreated) return new ArrayList<>();
        List<String> list = search.searchAuthor(author);
        if (list == null) return new ArrayList<>();
        else return list;
    }

    /**
     * Method searches files which contains word. Method returns list of file paths sorted by number of occurrences.
     * @param word
     * @return
     */
    public List<String> searchOneWord(String word){
        if(!dictionariesCreated) return new ArrayList<>();

        List<String> list = search.searchOneWord(word);
        if (list == null) return new ArrayList<>();
        else return list;
    }

    /**
     * Method searches files which contains phrase. Method returns list of file paths sorted by number of occurrences.
     * @param words
     * @return
     */
    public List<String> searchPhrase(List<String> words){
        if(!dictionariesCreated) return new ArrayList<>();
        List<String> list = search.searchPhrase(words);
        if (list == null) return new ArrayList<>();
        else return list;
    }

    /**
     * Method searches files which contains word and which are created by author. Method returns list of file paths sorted by number of occurrences.
     * @param word
     * @param author
     * @return
     */
    public List<String> searchOneWordAndFilterByAuthor(String word, String author){
        if(!dictionariesCreated) return new ArrayList<>();
        List<String> list = search.searchOneWordAndFilterByAuthor(word, author);
        if (list == null) return new ArrayList<>();
        else return list;
    }

    /**
     * Method searches files which contains phrase and which are created by author. Method returns list of file paths sorted by number of occurrences.
     * @param words
     * @param author
     * @return
     */
    public List<String> searchPhraseAndFilterByAuthor(List<String> words, String author){
        if(!dictionariesCreated) return new ArrayList<>();
        List<String> list = search.searchPhraseAndFilterByAuthor(words, author);
        if (list == null) return new ArrayList<>();
        else return list;
    }

    /**
     * Method enables to save created index to file.
     * @param path
     * @return
     */
    public boolean writeDictionariesToFile(String path){
        if(!dictionariesCreated) return false;
        // Create SerializableDictionaries object
        SerializableDictionaries object = new SerializableDictionaries(wordsDictionary, authorDictionary);

        //Saving object to file
        FileOutputStream file = null;
        try {
            file = new FileOutputStream(path);
            ObjectOutputStream out = new ObjectOutputStream(file);
            out.writeObject(object);

            out.close();
            file.close();

            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Method enables to read built index from file.
     * @param path
     * @return
     */
    public boolean readDictionariesFromFile(String path){
        // Create Stream
        FileInputStream file = null;
        try {
            file = new FileInputStream(path);
            ObjectInputStream in = new ObjectInputStream(file);

            // Create SerializableDictionaries object
            SerializableDictionaries object = null;
            object = (SerializableDictionaries)in.readObject();

            in.close();
            file.close();

            // Save data to this class attributes
            authorDictionary = object.getAuthorDictionary();
            wordsDictionary = object.getWordsDictionary();

            this.search = new Search(authorDictionary, wordsDictionary);
            dictionariesCreated = true;

            return true;
        } catch (IOException | ClassNotFoundException e) {
            return false;
        }
    }

    public WordsDictionary getWordsDictionary() {
        return wordsDictionary;
    }

    public AuthorDictionary getAuthorDictionary() {
        return authorDictionary;
    }

    /**
     * Additional class, required in reading and writing dictionaries from and to file.
     */
    private static class SerializableDictionaries implements Serializable {
        private AuthorDictionary authorDictionary;
        private WordsDictionary wordsDictionary;

        public SerializableDictionaries(WordsDictionary wordsDictionary, AuthorDictionary authorDictionary) {
            this.wordsDictionary = wordsDictionary;
            this.authorDictionary = authorDictionary;
        }

        public AuthorDictionary getAuthorDictionary() {
            return authorDictionary;
        }

        public void setAuthorDictionary(AuthorDictionary authorDictionary) {
            this.authorDictionary = authorDictionary;
        }

        public WordsDictionary getWordsDictionary() {
            return wordsDictionary;
        }

        public void setWordsDictionary(WordsDictionary wordsDictionary) {
            this.wordsDictionary = wordsDictionary;
        }
    }
}
