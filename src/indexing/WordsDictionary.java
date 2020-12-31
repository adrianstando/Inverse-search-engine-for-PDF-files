package indexing;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class represents dictionary of words - it stores all words and information about them.
 */

public class WordsDictionary implements Dictionary, Serializable {
    private ConcurrentHashMap<String, Word> dictionary = new ConcurrentHashMap<>();

    public WordsDictionary(){

    }

    /**
     * This method adds to dictionary new information about a word.
     *
     * @param word
     * @param path
     * @param positionInFile
     */
    public synchronized void add(String word, String path, int positionInFile){
        if (word.equals("")) return;

        Word x = dictionary.putIfAbsent(word, new Word(word, path, positionInFile));
        if (x != null) x.add(path, positionInFile);
    }

    public Map<String, List<Integer>> getPositionsOfTheWord(String word){
        return dictionary.get(word).getAllFilesWithPositions();
    }

    public HashMap<String, Word> getDictionary() {
        return new HashMap<>(dictionary);
    }

    @Override
    public Set<String> getAllUniqueKeys() {
        return dictionary.keySet();
    }

    @Override
    public Set<String> getFilesWith(String object) {
        Word word = dictionary.get(object);
        if (word == null) return new HashSet<>();
        else return word.getFilesConnectedWith();
    }

    public List<Integer> getPositionsOfWordInPath(String word, String path){
        Word wordObject = dictionary.get(word);
        if (wordObject == null) return new ArrayList<>();
        else return wordObject.getPositionsOfTheWordInFile(path);
    }

    @Override
    public String toString() {
        StringBuilder text = new StringBuilder("Words Dictionary: " + System.lineSeparator());
        for (String x : dictionary.keySet()){
            text.append(dictionary.get(x).toString());
            text.append(System.lineSeparator());
        }
        return text.toString();
    }

}
