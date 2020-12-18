package tests;

import indexing.Author;
import indexing.AuthorDictionary;
import indexing.Word;
import indexing.WordsDictionary;
import org.junit.jupiter.api.Test;
import searching.Search;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class SearchTest {

    @Test
    void authorSearchTest(){
        ConcurrentHashMap<String, Author> dictionary = new ConcurrentHashMap<>();
        AuthorDictionary authorDictionary = new AuthorDictionary(dictionary);

        ConcurrentHashMap<String, Word> dictionary1 = new ConcurrentHashMap<>();
        WordsDictionary wordsDictionary = new WordsDictionary(dictionary1);

        List<String> authors1 = Arrays.stream(new String[] {"a", "b"}).collect(Collectors.toList());
        List<String> authors2 = Arrays.stream(new String[] {"s", "d"}).collect(Collectors.toList());

        List<String> path1 = Arrays.stream(new String[] {"1path1a", "1path2a"}).collect(Collectors.toList());
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

        Search search = new Search(authorDictionary, wordsDictionary);
        List<String> out1 = search.searchAuthor("a");
        assertEquals(new HashSet<>(path1), new HashSet<>(out1));

        List<String> out2 = search.searchAuthor("s");
        assertEquals(new HashSet<>(path2), new HashSet<>(out2));
    }

    @Test
    void searchOneWordTest(){
        ConcurrentHashMap<String, Author> dictionary1 = new ConcurrentHashMap<>();
        AuthorDictionary authorDictionary = new AuthorDictionary(dictionary1);

        ConcurrentHashMap<String, Word> dictionary = new ConcurrentHashMap<>();
        WordsDictionary wordsDictionary = new WordsDictionary(dictionary);

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

        Search search = new Search(authorDictionary, wordsDictionary);
        List<String> out1 = search.searchOneWord("a");

        assertEquals(new HashSet<>(paths1), new HashSet<>(out1));

        List<String> out2 = search.searchOneWord("s");

        assertEquals(new HashSet<>(paths2), new HashSet<>(out2));
    }

    @Test
    void searchOneWordTest2(){
        ConcurrentHashMap<String, Author> dictionary1 = new ConcurrentHashMap<>();
        AuthorDictionary authorDictionary = new AuthorDictionary(dictionary1);

        ConcurrentHashMap<String, Word> dictionary = new ConcurrentHashMap<>();
        WordsDictionary wordsDictionary = new WordsDictionary(dictionary);

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

        Search search = new Search(authorDictionary, wordsDictionary);
        List<String> out1 = search.searchOneWord("a");

        assertEquals(new HashSet<>(paths1), new HashSet<>(out1));

        List<String> out2 = search.searchOneWord("s");

        assertEquals(new HashSet<>(paths2), new HashSet<>(out2));
    }


    @Test
    void searchOneWordAndFilterByAuthorTest(){
        ConcurrentHashMap<String, Author> dictionary1 = new ConcurrentHashMap<>();
        AuthorDictionary authorDictionary = new AuthorDictionary(dictionary1);

        ConcurrentHashMap<String, Word> dictionary = new ConcurrentHashMap<>();
        WordsDictionary wordsDictionary = new WordsDictionary(dictionary);

        List<String> words1 = Arrays.stream(new String[] {"a", "b", "c", "d"}).collect(Collectors.toList());
        List<String> words2 = Arrays.stream(new String[] {"s", "d", "f", "g"}).collect(Collectors.toList());

        List<String> paths1 = Arrays.stream(new String[] {"1path1a", "1path2a", "1path3a"}).collect(Collectors.toList());
        List<String> paths2 = Arrays.stream(new String[] {"2path1a", "2path2a", "2path3a"}).collect(Collectors.toList());

        List<Integer> positions1 = Arrays.stream(new Integer[] {1, 1, 4}).collect(Collectors.toList());
        List<Integer> positions2 = Arrays.stream(new Integer[] {2, 3, 5, 10}).collect(Collectors.toList());
        List<Integer> positions3 = Arrays.stream(new Integer[] {2, 3, 5, 10, 100}).collect(Collectors.toList());

        List<String> authors1 = Arrays.stream(new String[] {"a", "b"}).collect(Collectors.toList());
        List<String> authors2 = Arrays.stream(new String[] {"s", "d"}).collect(Collectors.toList());

        Thread thread1 = new Thread(new Runnable() {
            @Override
            public void run() {
                for(String a : words1){
                    for(Integer x : positions3){
                        wordsDictionary.add(a, paths1.get(0), x);
                    }
                    for(Integer x : positions2){
                        wordsDictionary.add(a, paths1.get(1), x);
                    }
                    for(Integer x : positions1){
                        wordsDictionary.add(a, paths1.get(2), x);
                    }
                }
            }
        });

        Thread thread2 = new Thread(new Runnable() {
            @Override
            public void run() {
                for(String a : words2){
                    for(Integer x : positions3){
                        wordsDictionary.add(a, paths2.get(0), x);
                    }
                    for(Integer x : positions2){
                        wordsDictionary.add(a, paths2.get(1), x);
                    }
                    for(Integer x : positions1){
                        wordsDictionary.add(a, paths2.get(2), x);
                    }
                }
            }
        });

        Thread thread3 = new Thread(new Runnable() {
            @Override
            public void run() {
                for(String a : authors2){
                    for (String b : paths1){
                        authorDictionary.add(a, b);
                    }
                }
            }
        });

        Thread thread4 = new Thread(new Runnable() {
            @Override
            public void run() {
                for(String a : authors1){
                    for (String b : paths2){
                        authorDictionary.add(a, b);
                    }
                }
            }
        });

        thread1.start();
        thread2.start();
        thread3.start();
        thread4.start();

        try {
            thread1.join();
            thread2.join();
            thread3.join();
            thread4.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Search search = new Search(authorDictionary, wordsDictionary);
        List<String> out1 = search.searchOneWordAndFilterByAuthor("a", "s");

        assertEquals(paths1, out1);

        List<String> out2 = search.searchOneWordAndFilterByAuthor("s", "a");

        assertEquals(paths2, out2);

        for (String b : paths1){
            authorDictionary.add("a", b);
        }

        List<String> out3 = search.searchOneWordAndFilterByAuthor("a", "a");
        assertEquals(paths1, out3);
    }

    @Test
    void searchOneWordAndFilterByAuthorTest2(){
        ConcurrentHashMap<String, Author> dictionary1 = new ConcurrentHashMap<>();
        AuthorDictionary authorDictionary = new AuthorDictionary(dictionary1);

        ConcurrentHashMap<String, Word> dictionary = new ConcurrentHashMap<>();
        WordsDictionary wordsDictionary = new WordsDictionary(dictionary);

        List<String> words1 = Arrays.stream(new String[] {"a", "b", "c", "d"}).collect(Collectors.toList());
        List<String> words2 = Arrays.stream(new String[] {"s", "d", "f", "g"}).collect(Collectors.toList());

        List<String> paths1 = Arrays.stream(new String[] {"1path1a", "1path2a", "1path3a"}).collect(Collectors.toList());
        List<String> paths2 = Arrays.stream(new String[] {"2path1a", "2path2a", "2path3a"}).collect(Collectors.toList());

        List<Integer> positions1 = Arrays.stream(new Integer[] {1, 1, 4}).collect(Collectors.toList());
        List<Integer> positions2 = Arrays.stream(new Integer[] {2, 3, 5, 10}).collect(Collectors.toList());
        List<Integer> positions3 = Arrays.stream(new Integer[] {2, 3, 5, 10, 100}).collect(Collectors.toList());

        List<String> authors1 = Arrays.stream(new String[] {"a", "b"}).collect(Collectors.toList());
        List<String> authors2 = Arrays.stream(new String[] {"s", "d"}).collect(Collectors.toList());

        Thread thread1 = new Thread(new Runnable() {
            @Override
            public void run() {
                for(String a : words1){
                    for(Integer x : positions3){
                        wordsDictionary.add(a, paths1.get(2), x);
                    }
                    for(Integer x : positions2){
                        wordsDictionary.add(a, paths1.get(1), x);
                    }
                    for(Integer x : positions1){
                        wordsDictionary.add(a, paths1.get(0), x);
                    }
                }
            }
        });

        Thread thread2 = new Thread(new Runnable() {
            @Override
            public void run() {
                for(String a : words2){
                    for(Integer x : positions3){
                        wordsDictionary.add(a, paths2.get(2), x);
                    }
                    for(Integer x : positions2){
                        wordsDictionary.add(a, paths2.get(1), x);
                    }
                    for(Integer x : positions1){
                        wordsDictionary.add(a, paths2.get(0), x);
                    }
                }
            }
        });

        Thread thread3 = new Thread(new Runnable() {
            @Override
            public void run() {
                for(String a : authors2){
                    for (String b : paths1){
                        authorDictionary.add(a, b);
                    }
                }
            }
        });

        Thread thread4 = new Thread(new Runnable() {
            @Override
            public void run() {
                for(String a : authors1){
                    for (String b : paths2){
                        authorDictionary.add(a, b);
                    }
                }
            }
        });

        thread1.start();
        thread2.start();
        thread3.start();
        thread4.start();

        try {
            thread1.join();
            thread2.join();
            thread3.join();
            thread4.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Search search = new Search(authorDictionary, wordsDictionary);
        List<String> out1 = search.searchOneWordAndFilterByAuthor("a", "s");

        assertEquals(Arrays.stream(new String[] {"1path3a", "1path2a", "1path1a"}).collect(Collectors.toList()), out1);

        List<String> out2 = search.searchOneWordAndFilterByAuthor("s", "a");

        assertEquals(Arrays.stream(new String[] {"2path3a", "2path2a", "2path1a"}).collect(Collectors.toList()), out2);

        for (String b : paths1){
            authorDictionary.add("a", b);
        }

        List<String> out3 = search.searchOneWordAndFilterByAuthor("a", "a");
        assertEquals(Arrays.stream(new String[] {"1path3a", "1path2a", "1path1a"}).collect(Collectors.toList()), out3);
    }


    @Test
    void searchPhraseTest(){

    }


    @Test
    void searchPhraseAndFilterByAuthorTest(){

    }
}