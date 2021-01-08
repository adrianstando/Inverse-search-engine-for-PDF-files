package tests;


import main.java.inverted_index_search_engine.indexing.Author;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class AuthorTest {
    @Test
    void authorTest(){
        List<String> list1 = Arrays.stream(new String[] {"a", "b", "c", "d"}).collect(Collectors.toList());
        List<String> list2 = Arrays.stream(new String[] {"e", "f", "g", "h"}).collect(Collectors.toList());

        Author author = new Author("imię i nazwisko autora");

        Thread thread1 = new Thread(new Runnable() {
            @Override
            public void run() {
                for(String elem : list1){
                    author.add(elem);
                }
            }
        });

        Thread thread2 = new Thread(new Runnable() {
            @Override
            public void run() {
                for(String elem : list2){
                    author.add(elem);
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

        Set<String> set = author.getFilesConnectedWith();
        for (String elem : list1){
            assertTrue(set.contains(elem));
        }
        for (String elem : list2){
            assertTrue(set.contains(elem));
        }
    }
}