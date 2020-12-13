import files.*;
import indexing.*;

import java.io.File;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Class which will be something like API for the Inverted Index program.
 */
public class Controller {
    private final static String poison = "THIS_IS_THE_END.non_existing_extension";

    public static void main(String[] args){
        String pathZero = "./dysk0";
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
            fileReaders[0].PutExtraPoison(numberOfIndexingThreads - numberOfReadingThreads);
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

    }
}
