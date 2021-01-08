package tests;


import main.java.inverted_index_search_engine.indexing.Word;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class WordTest {
    @Test
    void wordTest(){
        List<String> list1 = Arrays.stream(new String[] {"a", "b", "c", "d"}).collect(Collectors.toList());
        List<String> list2 = Arrays.stream(new String[] {"e", "f", "g", "h"}).collect(Collectors.toList());

        List<Integer> listInteger1 = Arrays.stream(new Integer[] {1, 3, 5, 7}).collect(Collectors.toList());
        List<Integer> listInteger2 = Arrays.stream(new Integer[] {18, 10, 12, 14}).collect(Collectors.toList());

        Word word = new Word("VeryDifficultWord");

        Thread thread1 = new Thread(new Runnable() {
            @Override
            public void run() {
                for(String elem : list1){
                    for (Integer x : listInteger1) {
                        word.add(elem, x);
                    }
                }
            }
        });

        Thread thread2 = new Thread(new Runnable() {
            @Override
            public void run() {
                for(String elem : list2){
                    for (Integer x : listInteger2) {
                        word.add(elem, x);
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

        Set<String> set = word.getFilesConnectedWith();
        for (String elem : list1){
            assertTrue(set.contains(elem));
        }
        for (String elem : list2){
            assertTrue(set.contains(elem));
        }

        for(String p : set) {
            if (list1.contains(p)){
                assertEquals(word.getPositionsOfTheWordInFile(p), listInteger1);
            } else {
                assertEquals(word.getPositionsOfTheWordInFile(p), listInteger2);
            }

        }

    }

}