import files.*;
import files.FileReader;
import indexing.*;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

/**
 * Class which will be something like API for the Inverted Index program.
 */

// pamietaj tutaj o nullach
public class Controller {
    private final static String poison = "THIS_IS_THE_END.non_existing_extension";
    private WordsDictionary wordsDictionary;
    private AuthorDictionary authorDictionary;

    private static List<String> commonWords = Arrays.stream(new String [] {"a", "able", "about", "all", "an", "and", "any", "are", "as", "at", "be", "been", "by",
            "can", "can't", "could", "couldn't", "do", "does", "doesn't", "don't", "down", "has", "hasn't", "have", "haven't", "he", "here", "his", "how",
            "I", "I'm", "if", "in", "is", "it", "its", "it's", "just", "like", "many", "much", "no", "not", "now", "of", "on", "one",
            "or", "she", "so", "than", "that", "the", "them", "then", "there", "these", "they", "this", "those", "to", "too", "up", "very", "was", "we", "were",
            "what", "when", "where", "which", "who", "will", "won't", "would", "you", "you'd", "you'll"}).map(String::toLowerCase).map(r -> r.replaceAll("\\p{Punct} | ’", "")).collect(Collectors.toList());


    public static void main(String[] args){
        //String pathZero = "./dysk0";
        String pathZero = "./src/tests/testFiles/folder3";
        int numberOfReadingThreads = 2;
        int numberOfIndexingThreads = 4;


        BlockingQueue<File> files = new ArrayBlockingQueue<>(5);
        BlockingQueue<FileContent> filesContent = new LinkedBlockingQueue<>();


        FileFinder fileFinder = new FileFinder(pathZero, numberOfReadingThreads, files);
        Thread fileFinderThread = new Thread(fileFinder);

        Thread[] fileReaderThreads = new Thread[numberOfReadingThreads];
        FileReader[] fileReaders = new FileReader[numberOfReadingThreads];
        for(int i = 0; i < numberOfReadingThreads; i++){
            fileReaders[i] = new FileReader(files, filesContent);
            fileReaderThreads[i] = new Thread(fileReaders[i]);
        }

        // jesli mamy wiecej indeksujacych
        if(numberOfIndexingThreads > numberOfReadingThreads){
            fileReaders[0].putExtraPoison(numberOfIndexingThreads - numberOfReadingThreads);
        }

        fileFinderThread.start();
        for(Thread thread : fileReaderThreads){
            thread.start();
        }

        /*try {

            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/

        /*for(FileContent content : filesContent){
            //System.out.println(content.getFile().getPath());
            //System.out.println(content.getContent());
            System.out.println(content.getAuthor());
        }*/










        ConcurrentHashMap<String, Word> map = new ConcurrentHashMap<>();
        WordsDictionary wordsDictionary = new WordsDictionary(map);

        ConcurrentHashMap<String, Author> map1 = new ConcurrentHashMap<>();
        AuthorDictionary authorDictionary = new AuthorDictionary(map1);

        Thread[] indexingThreads = new Thread[numberOfIndexingThreads];
        for(int i = 0; i < numberOfIndexingThreads; i++){
            indexingThreads[i] = new Thread(new InvertedIndex(filesContent, wordsDictionary, authorDictionary, commonWords));
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

        /*if(numberOfIndexingThreads > numberOfReadingThreads){
            for(int i = 0; i < numberOfIndexingThreads - numberOfReadingThreads; i++){
                try {
                    filesContent.put(new FileContent(new File(poison), "", ""));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }*/

        System.out.println(authorDictionary.toString());
        System.out.println(wordsDictionary.toString());

        /*String out  = "aren't able parents' x''d''s";

        out = out.replaceAll("[^(\n | \\p{Blank} | \\p{IsAlphabetic} | \\p{Digit} | \\p{Punct})]", " ");
        out = out.replaceAll("\\p{Punct}", "");
        out = out.replaceAll("[^(\n | \\p{IsAlphabetic} | \\p{Digit})]+", " ");
        // i tam gdzie nowa linia, dodajmy spację, by rozdzielic slowa
        out = out.replaceAll("\n", " \n");
        //System.out.println("’".matches("(\n | \\p{Blank} | \\p{IsAlphabetic} | \\p{Digit} | \\p{Punct})")); */
    }






    public boolean writeDictionariesToFile(String path){
        // Create SerializableDictionaries object
        SerializableDictionaries object = new SerializableDictionaries();
        object.setAuthorDictionary(authorDictionary);
        object.setWordsDictionary(wordsDictionary);

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

            return true;
        } catch (IOException | ClassNotFoundException e) {
            return false;
        }
    }

    private static class SerializableDictionaries implements Serializable {
        private AuthorDictionary authorDictionary;
        private WordsDictionary wordsDictionary;

        public SerializableDictionaries() {
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
