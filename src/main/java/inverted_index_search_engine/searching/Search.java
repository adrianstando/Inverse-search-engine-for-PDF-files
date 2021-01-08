package main.java.inverted_index_search_engine.searching;


import javafx.util.Pair;
import main.java.inverted_index_search_engine.indexing.Author;
import main.java.inverted_index_search_engine.indexing.AuthorDictionary;
import main.java.inverted_index_search_engine.indexing.Word;
import main.java.inverted_index_search_engine.indexing.WordsDictionary;

import java.util.*;
import java.util.stream.Collectors;

public class Search {
    private WordsDictionary wordsDictionary;
    private AuthorDictionary authorDictionary;

    private HashMap<String, Word> wordsHashMap;
    private HashMap<String, Author> authorHashMap;

    public Search(AuthorDictionary authorDictionary, WordsDictionary wordsDictionary) {
        this.authorDictionary = authorDictionary;
        this.wordsDictionary = wordsDictionary;
        updateDictionaries();
    }

    /**
     * Method updates content from dictionaries.
     */
    public void updateDictionaries() {
        this.wordsHashMap = wordsDictionary.getDictionary();
        this.authorHashMap = authorDictionary.getDictionary();
    }

    /**
     * Method searches files created by author.
     * @param author
     * @return
     */
    public List<String> searchAuthor(String author) {
        if (author == null) return new ArrayList<>();

        Author author1 = authorHashMap.get(author);
        if (author1 == null) return new ArrayList<>();

        Set<String> set = author1.getFilesConnectedWith();
        return new ArrayList<>(set);
    }

    /**
     * Method searches files which contains word. Method returns list of file paths sorted by number of occurrences.
     * @param word
     * @return
     */
    public List<String> searchOneWord(String word) {
        if (word == null) return new ArrayList<>();

        Word word1 = wordsHashMap.get(word);
        if (word1 == null) return new ArrayList<>();

        HashMap<String, List<Integer>> map = word1.getAllFilesWithPositions();

        ArrayList<String> out = map.entrySet().stream()
                .sorted((a, b) -> -(a.getValue().size() - b.getValue().size()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toCollection(ArrayList::new));

        return out;
    }

    /**
     * Method searches files which contains phrase. Method returns list of file paths sorted by number of occurrences.
     * @param words
     * @return
     */
    public List<String> searchPhrase(List<String> words) {
        if (words == null) return new ArrayList<>();
        if (words.size() == 0) return new ArrayList<>();

        // getting Word objects of required words
        List<Word> listOfWordObjects =
                wordsHashMap.entrySet().stream()
                        .parallel().unordered()
                        .filter(s -> words.contains(s.getKey()))
                        .map(Map.Entry::getValue)
                        .collect(Collectors.toList());

        // when we have less words in set than required for full phrase
        if (listOfWordObjects.size() != words.size()) return new ArrayList<>();

        // get all paths from first word;
        // we are looking for common files and lines, so if any of the Word object doesn't have one path, it's not a problem -
        // we wouldn't use it in anyway
        Set<String> paths = listOfWordObjects.get(0).getFilesConnectedWith();

        // output list
        // path -> how many matches
        List<Pair<String, Integer>> out = new LinkedList<>();

        // iterate through each path from first word
        for (String path : paths) {
            // get positions of first word in path
            // we iterate through paths from first word, so commonPositions can't be null (for now)
            List<Integer> commonPositionsExtracted = listOfWordObjects.get(0).getPositionsOfTheWordInFile(path);
            List<Integer> commonPositions = new ArrayList<>(commonPositionsExtracted);
            boolean wasInLastPath = true;
            for (int i = 1; i < listOfWordObjects.size(); i++) {
                List<Integer> tmp = listOfWordObjects.get(i).getPositionsOfTheWordInFile(path);
                if (tmp == null) {
                    wasInLastPath = false;
                    break;
                }
                commonPositions.retainAll(tmp);
            }

            // if there is no path in any Word object
            if (!wasInLastPath) continue;
            // if there are zero common positions
            if (commonPositions.size() == 0) continue;

            // if we have a match
            out.add(new Pair<>(path, commonPositions.size()));
        }

        // sort out, retrieve path and collect to list
        List<String> outputList = out.stream()
                .sorted((a, b) -> b.getValue() - a.getValue())
                .map(Pair::getKey)
                .collect(Collectors.toList());

        return outputList;
    }

    /**
     * Method searches files which contains word and which are created by author. Method returns list of file paths sorted by number of occurrences.
     * @param word
     * @param author
     * @return
     */
    public List<String> searchOneWordAndFilterByAuthor(String word, String author) {
        List<String> wordList = searchOneWord(word);
        List<String> authorList = searchAuthor(author);

        wordList.retainAll(authorList);
        return wordList;
    }

    /**
     * Method searches files which contains phrase and which are created by author. Method returns list of file paths sorted by number of occurrences.
     * @param words
     * @param author
     * @return
     */
    public List<String> searchPhraseAndFilterByAuthor(List<String> words, String author) {
        List<String> wordList = searchPhrase(words);
        List<String> authorList = searchAuthor(author);

        wordList.retainAll(authorList);
        return wordList;
    }
}
