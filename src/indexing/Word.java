package indexing;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class represents single word and the map, that stores path of the file, in which we can find this word,
 * and it's positions inside this file.
 */

public class Word implements PDFComponent{
    private String word;
    private ConcurrentHashMap<String, List<Integer>> position = new ConcurrentHashMap<>();

    /**
     * Może zrobić BlockingQueue zamiast List<Integer>
     */

    public Word(String word){
        this.word = word;
    }

    public Word(String word, String path, int positionInFile){
        this.word = word;
        add(path, positionInFile);
    }

    /**
     * This method adds new position to the map.
     *
     * @param path
     * @param positionInFile
     */
    public void add(String path, int positionInFile){
        /*List<Integer> obj = position.get(path);
        if (obj == null){
            obj = new LinkedList<>();
        }
        obj.add(positionInFile);
        position.put(path, obj); */

        /*if (position.containsKey(path)){
            position.get(path).add(positionInFile);
        } else {
            position.put(path, new LinkedList<Integer>(Collections.singleton((Integer) positionInFile)));
        }*/

        List<Integer> x = position.putIfAbsent(path, new LinkedList<Integer>(Collections.singleton(positionInFile)));
        if (x != null) x.add(positionInFile);
    }

    public List<Integer> getPositionsOfTheWordInFile(String path){
        return position.get(path);
    }

    @Override
    public Set<String> getFilesConnectedWith(){
        return position.keySet();
    }

    public HashMap<String, List<Integer>> getAllFilesWithPositions(){
        return new HashMap<>(position);
    }

    @Override
    public String toString() {
        return  word + "; " + position.toString();
    }
}
