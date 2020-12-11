package indexing;

import java.util.*;

public class WordsDictionary implements Dictionary{
    private HashMap<String, Word> dictionary;

    public WordsDictionary(HashMap<String, Word> dictionary){
        this.dictionary = dictionary;
    }

    public synchronized void add(String word, String path, int positionInFile){
        Word obj = dictionary.get(word);
        if(obj == null){
            obj = new Word(word);
        }

        obj.add(path, positionInFile);
        dictionary.put(word, obj);
    }

    public Map<String, List<Integer>> getPositionsOfTheWord(String word){
        return dictionary.get(word).getAllFilesWithPositions();
    }

    @Override
    public HashMap<String, ? extends PDFComponent> getDictionary() {
        return dictionary;
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
