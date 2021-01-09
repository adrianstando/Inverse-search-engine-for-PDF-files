package tests.java;


import main.java.inverted_index_search_engine.indexing.Word;
import main.java.inverted_index_search_engine.indexing.WordsDictionary;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class WordsDictionaryTest {
    @Test
    void wordDictionaryTest(){
        WordsDictionary wordsDictionary = new WordsDictionary();

        List<String> words1 = Arrays.stream(new String[] {"a", "b", "c", "d"}).collect(Collectors.toList());
        List<String> words2 = Arrays.stream(new String[] {"s", "d", "f", "g"}).collect(Collectors.toList());

        List<String> paths1 = Arrays.stream(new String[] {"1path1a", "1path2a", "1path3a", "1path4a"}).collect(Collectors.toList());
        List<String> paths2 = Arrays.stream(new String[] {"2path1a", "2path2a", "2path3a", "2path4a"}).collect(Collectors.toList());

        List<Integer> positions1 = Arrays.stream(new Integer[] {5, 6, 7, 23, 45}).collect(Collectors.toList());
        List<Integer> positions2 = Arrays.stream(new Integer[] {2, 4, 9, 10, 100}).collect(Collectors.toList());

        Thread thread1 = new Thread(new Runnable() {
            @Override
            public void run() {
                for(String a : words1){
                    for(String b : paths1){
                        for(Integer c : positions1) {
                            wordsDictionary.add(a, b, c);
                        }
                    }
                }
            }
        });

        Thread thread2 = new Thread(new Runnable() {
            @Override
            public void run() {
                for(String a : words2){
                    for(String b : paths2){
                        for(Integer c : positions2) {
                            wordsDictionary.add(a, b, c);
                        }
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

        assertEquals(new HashSet<>(paths1), wordsDictionary.getFilesWith("a"));
        assertEquals(new HashSet<>(paths2), wordsDictionary.getFilesWith("s"));

        assertEquals(new HashSet<>(positions1) , new HashSet<>(wordsDictionary.getPositionsOfWordInPath("a", "1path1a")));
        assertEquals(new HashSet<>(positions1) , new HashSet<>(wordsDictionary.getPositionsOfWordInPath("a", "1path4a")));
        assertEquals(new HashSet<>(positions2) , new HashSet<>(wordsDictionary.getPositionsOfWordInPath("f", "2path4a")));
        assertEquals(new HashSet<>(positions2) , new HashSet<>(wordsDictionary.getPositionsOfWordInPath("g", "2path4a")));

        Set<String> check1 = new HashSet<>();
        check1.addAll(words1);
        check1.addAll(words2);
        assertEquals(check1, wordsDictionary.getAllUniqueKeys());

        HashMap<String, Word> check2 = wordsDictionary.getDictionary();
        assertEquals(check1, check2.keySet());

        Word check3 = check2.get("a");
        Word check4 = new Word("a");
        for (String x : paths1){
            for (Integer y : positions1) {
                check4.add(x, y);
            }
        }
        assertEquals(check4.getFilesConnectedWith(), check3.getFilesConnectedWith());

    }

}