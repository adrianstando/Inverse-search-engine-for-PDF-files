package main.java.inverted_index_search_engine.controller;


import main.java.inverted_index_search_engine.files.*;
import main.java.inverted_index_search_engine.files.FileReader;
import main.java.inverted_index_search_engine.indexing.AuthorDictionary;
import main.java.inverted_index_search_engine.indexing.InvertedIndex;
import main.java.inverted_index_search_engine.indexing.WordsDictionary;
import main.java.inverted_index_search_engine.searching.Search;
import org.tartarus.snowball.ext.PorterStemmer;


import java.io.*;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

/**
 * Class which is something like API for the Inverted Index program.
 */
public class Controller {
    private final static String poison = "THIS_IS_THE_END.non_existing_extension";
    private WordsDictionary wordsDictionary;
    private AuthorDictionary authorDictionary;
    private Search searchObject;
    private boolean dictionariesCreated = false;
    private boolean enableStemmer;

    private String currentPath;

    private PorterStemmer porterStemmer = new PorterStemmer();

    private static List<String> commonWords = Arrays.stream(new String [] {"a", "able", "about", "all", "an", "and", "any", "are", "aren't", "isn't", "as", "at", "be", "been", "by",
            "can", "can't", "could", "couldn't", "do", "does", "doesn't", "don't", "down", "has", "hasn't", "have", "haven't", "he", "here", "his", "how",
            "I", "I'm", "if", "in", "is", "it", "its", "it's", "just", "like", "many", "much", "no", "not", "now", "of", "on", "one",
            "or", "she", "so", "than", "that", "the", "them", "then", "there", "these", "they", "this", "those", "to", "too", "up", "very", "was", "we", "were",
            "what", "when", "where", "which", "who", "will", "won't", "would", "you", "you'd", "you'll"}).map(String::toLowerCase)
            .map(r -> r.replaceAll("[^\\p{IsAlphabetic})]+", "")).collect(Collectors.toList());


    public Controller(boolean enableStemmer){
        this.enableStemmer = enableStemmer;
    }

     //Creating index part.

    /**
     * Method which builds index from pathZero.
     * @param pathZero
     */
    public void createIndex(String pathZero){

        // number of threads - read from config.properties file
        int numberOfReadingThreads;
        int numberOfIndexingThreads;
        int maxFileSizeInMb;

        Properties properties = new Properties();
        try {
            properties.load(getClass().getResourceAsStream("/main/resources/config.properties"));

            numberOfReadingThreads = Integer.parseInt(properties.getProperty("numberOfReadingThreads", "2"));
            numberOfIndexingThreads = Integer.parseInt(properties.getProperty("numberOfIndexingThreads", "4"));
            maxFileSizeInMb = Integer.parseInt(properties.getProperty("maxSizeOfPdfFileInMb", "200"));

            numberOfReadingThreads = Math.max(1, numberOfReadingThreads);
            numberOfIndexingThreads = Math.max(1, numberOfIndexingThreads);
            maxFileSizeInMb = Math.max(0, maxFileSizeInMb);
        } catch (IOException e) {
            // default values in case of exception
            numberOfReadingThreads = 2;
            numberOfIndexingThreads = 4;
            maxFileSizeInMb = 200;
        }

        // initialize queues
        BlockingQueue<File> files = new LinkedBlockingQueue<>();
        BlockingQueue<FileContent> filesContent = new LinkedBlockingQueue<>();

        // and program creates and starts threads
        FileFinder fileFinder = new FileFinder(pathZero, numberOfReadingThreads, files, maxFileSizeInMb);
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
        this.wordsDictionary = new WordsDictionary();

        this.authorDictionary = new AuthorDictionary();

        // start threads building InvertedIndex
        Thread[] indexingThreads = new Thread[numberOfIndexingThreads];
        for(int i = 0; i < numberOfIndexingThreads; i++){
            indexingThreads[i] = new Thread(new InvertedIndex(filesContent, wordsDictionary, authorDictionary, commonWords, enableStemmer));
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

        this.searchObject = new Search(authorDictionary, wordsDictionary);
        dictionariesCreated = true;
        currentPath = pathZero;
    }

     // Searching part.

    /**
     * Method searches words or phrases from given input text (as word) and filters by author (if author is given).
     * Method processes input text (from word).
     * If input text is empty ("") and author not, method returns list of all files created by author.
     *
     * @param input
     * @param author
     * @return
     */
    public List<String> search(String input, String author){
        if(input == null || author == null) return new ArrayList<>();
        if(!dictionariesCreated) return new ArrayList<>();

        List<String> splitText = processInputText(input);

        if (author.equals("")){
            if (!(input.equals(""))){
                if(splitText.size() == 1){
                    return searchOneWord(splitText.get(0));
                }
                else return searchPhrase(splitText);
            }
        } else {
            // !(author.equals("")) and !(author!=null)
            if (!(input.equals(""))){
                if(splitText.size() == 1) return searchOneWordAndFilterByAuthor(input, author);
                else return searchPhraseAndFilterByAuthor(splitText, author);
            } else {
                // !(author.equals("")) and !(author!=null) and (input.equals(""))
                return searchAuthor(author);
            }
        }

        return new ArrayList<>();
    }

    /**
     * Method processes input text for search method.
     * @param text
     * @return
     */
    private List<String> processInputText(String text){
        if(text == null) return new ArrayList<>();

        if(enableStemmer){
            return Arrays.stream(text.split("\\s+"))
                    .filter(r -> !commonWords.contains(r))
                    .filter(r -> !(r.equals("") || r.equals(" ")))
                    .map(r -> r.replaceAll(" ", ""))
                    .map(String::toLowerCase)
                    .map(this::stemWord)
                    .collect(Collectors.toList());
        } else {
            return Arrays.stream(text.split("\\s+"))
                    .filter(r -> !commonWords.contains(r))
                    .filter(r -> !(r.equals("") || r.equals(" ")))
                    .map(r -> r.replaceAll(" ", ""))
                    .map(String::toLowerCase)
                    .collect(Collectors.toList());
        }
    }

    /**
     * Method stems word.
     * @param word
     * @return
     */
    private String stemWord(String word) {
        porterStemmer.setCurrent(word);
        porterStemmer.stem();
        return porterStemmer.getCurrent();
    }

    /**
     * Method searches files created by author.
     * @param author
     * @return list
     */
    private List<String> searchAuthor(String author){
        if(!dictionariesCreated) return new ArrayList<>();
        List<String> list = searchObject.searchAuthor(author);
        if (list == null) return new ArrayList<>();
        else return list;
    }

    /**
     * Method searches files which contains word. Method returns list of file paths sorted by number of occurrences.
     * @param word
     * @return
     */
    private List<String> searchOneWord(String word){
        if(!dictionariesCreated) return new ArrayList<>();
        List<String> list = searchObject.searchOneWord(word);
        if (list == null) return new ArrayList<>();
        else return list;
    }

    /**
     * Method searches files which contains phrase. Method returns list of file paths sorted by number of occurrences.
     * @param words
     * @return
     */
    private List<String> searchPhrase(List<String> words){
        if(!dictionariesCreated) return new ArrayList<>();
        List<String> list = searchObject.searchPhrase(words);
        if (list == null) return new ArrayList<>();
        else return list;
    }

    /**
     * Method searches files which contains word and which are created by author. Method returns list of file paths sorted by number of occurrences.
     * @param word
     * @param author
     * @return
     */
    private List<String> searchOneWordAndFilterByAuthor(String word, String author){
        if(!dictionariesCreated) return new ArrayList<>();
        List<String> list = searchObject.searchOneWordAndFilterByAuthor(word, author);
        if (list == null) return new ArrayList<>();
        else return list;
    }

    /**
     * Method searches files which contains phrase and which are created by author. Method returns list of file paths sorted by number of occurrences.
     * @param words
     * @param author
     * @return
     */
    private List<String> searchPhraseAndFilterByAuthor(List<String> words, String author){
        if(!dictionariesCreated) return new ArrayList<>();
        List<String> list = searchObject.searchPhraseAndFilterByAuthor(words, author);
        if (list == null) return new ArrayList<>();
        else return list;
    }

    // Reading from file and writing to file part.

    /**
     * Method enables to save created index to file.
     * @param path
     * @return
     */
    public boolean writeDictionariesToFile(String path){
        if(!dictionariesCreated) return false;
        // Create SerializableDictionaries object
        SerializableDictionaries object = new SerializableDictionaries(wordsDictionary, authorDictionary,
                enableStemmer, currentPath);

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
            enableStemmer = object.isEnableStemmer();
            currentPath = object.getCurrentPath();

            this.searchObject = new Search(authorDictionary, wordsDictionary);
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

    public void setEnableStemmer(boolean enableStemmer) {
        this.enableStemmer = enableStemmer;
    }

    public boolean isEnableStemmer() {
        return enableStemmer;
    }

    public String getCurrentPath() {
        return currentPath;
    }

    public void setCurrentPath(String currentPath) {
        this.currentPath = currentPath;
    }

    /**
     * Additional class, required in reading and writing dictionaries from and to file.
     */
    private static class SerializableDictionaries implements Serializable {
        private AuthorDictionary authorDictionary;
        private WordsDictionary wordsDictionary;
        private boolean enableStemmer;
        private String currentPath;

        public SerializableDictionaries(WordsDictionary wordsDictionary, AuthorDictionary authorDictionary,
                                        boolean enableStemmer, String currentPath) {
            this.wordsDictionary = wordsDictionary;
            this.authorDictionary = authorDictionary;
            this.enableStemmer = enableStemmer;
            this.currentPath = currentPath;
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

        public boolean isEnableStemmer() {
            return enableStemmer;
        }

        public void setEnableStemmer(boolean enableStemmer) {
            this.enableStemmer = enableStemmer;
        }

        public String getCurrentPath() {
            return currentPath;
        }

        public void setCurrentPath(String currentPath) {
            this.currentPath = currentPath;
        }
    }
}
