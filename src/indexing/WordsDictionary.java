package indexing;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class WordsDictionary implements Dictionary{
    private ConcurrentHashMap<String, Word> dictionary;

    public WordsDictionary(ConcurrentHashMap<String, Word> dictionary){
        this.dictionary = dictionary;
    }

    public void add(String word, String path, int positionInFile){
        /*Word obj = dictionary.get(word);
        if(obj == null){
            obj = new Word(word);
            dictionary.put(word, obj);
        }

        obj.add(path, positionInFile);*/

        Word x = dictionary.putIfAbsent(word, new Word(word, path, positionInFile));
        if (x != null) x.add(path, positionInFile);
    }

    public Map<String, List<Integer>> getPositionsOfTheWord(String word){
        return dictionary.get(word).getAllFilesWithPositions();
    }

    @Override
    public HashMap<String, ? extends PDFComponent> getDictionary() {
        return new HashMap<>(dictionary);
    }

    @Override
    public Set<String> getAllUniqueKeys() {
        return dictionary.keySet();
    }

    @Override
    public Set<String> getFilesWith(String object) {
        return dictionary.get(object).getFilesConnectedWith();
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
