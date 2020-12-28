package tests;

import indexing.Author;
import indexing.AuthorDictionary;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class AuthorDictionaryTest {
    @Test
    void authorDictionaryTest(){
        AuthorDictionary authorDictionary = new AuthorDictionary();

        List<String> authors1 = Arrays.stream(new String[] {"a", "b", "c", "d"}).collect(Collectors.toList());
        List<String> authors2 = Arrays.stream(new String[] {"s", "d", "f", "g"}).collect(Collectors.toList());

        List<String> path1 = Arrays.stream(new String[] {"1path1a", "1path2a", "1path3a", "1path4a"}).collect(Collectors.toList());
        List<String> path2 = Arrays.stream(new String[] {"2path1a", "2path2a", "2path3a", "2path4a"}).collect(Collectors.toList());

        Thread thread1 = new Thread(new Runnable() {
            @Override
            public void run() {
                for(String a : authors1){
                    for(String b : path1){
                        authorDictionary.add(a, b);
                    }
                }
            }
        });

        Thread thread2 = new Thread(new Runnable() {
            @Override
            public void run() {
                for(String a : authors2){
                    for(String b : path2){
                        authorDictionary.add(a, b);
                    }
                }
            }
        });

        thread1.start();
        thread2.start();

        try {
            thread1.join();
            thread2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertEquals(new HashSet<>(path1), authorDictionary.getFilesWith("a"));
        assertEquals(new HashSet<>(path2), authorDictionary.getFilesWith("s"));

        Set<String> check1 = new HashSet<>();
        check1.addAll(authors1);
        check1.addAll(authors2);
        assertEquals(check1, authorDictionary.getAllUniqueKeys());

        HashMap<String, Author> check2 = authorDictionary.getDictionary();
        assertEquals(check1, check2.keySet());

        Author check3 = check2.get("a");
        Author check4 = new Author("a");
        for (String x : path1){
            check4.add(x);
        }
        assertEquals(check4.getFilesConnectedWith(), check3.getFilesConnectedWith());

    }
}