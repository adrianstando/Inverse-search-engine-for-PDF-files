package indexing;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Class represents single word and the map, that stores path of the file, in which we can find this word,
 * and it's positions inside this file.
 */

public class Word implements PDFComponent, Serializable {
    private String word;
    private ConcurrentHashMap<String, LinkedBlockingQueue<Integer>> position = new ConcurrentHashMap<>();

    private HashMap<String, List<Integer>> outputPositions = new HashMap<>();
    private AtomicBoolean outputPositionsNeedsUpdate = new AtomicBoolean(true);

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
    public void add(String path, int positionInFile) {
        LinkedBlockingQueue<Integer> x = position.putIfAbsent(path, new LinkedBlockingQueue<Integer>(Collections.singleton(positionInFile)));
        if (x != null) {
            try {
                x.put(positionInFile);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        outputNeedToBeUpdated();
    }

    public List<Integer> getPositionsOfTheWordInFile(String path){
        if(outputPositionsNeedsUpdate.get()) getAllFilesWithPositions();

        return outputPositions.get(path);
    }

    @Override
    public Set<String> getFilesConnectedWith(){
        if(outputPositionsNeedsUpdate.get()) getAllFilesWithPositions();

        return outputPositions.keySet();
    }

    public HashMap<String, List<Integer>> getAllFilesWithPositions(){
        HashMap<String, LinkedBlockingQueue<Integer>> tmp = new HashMap<>(position);
        HashMap<String, List<Integer>> out = new HashMap<>();

        for(Map.Entry<String, LinkedBlockingQueue<Integer>> entry : tmp.entrySet()){
            out.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }

        this.outputPositionsNeedsUpdate.set(false);
        this.outputPositions = out;
        return out;
    }

    @Override
    public String toString() {
        if(outputPositionsNeedsUpdate.get()) getAllFilesWithPositions();

        return  word + "; " + outputPositions.toString();
    }

    /**
     * If positions were added after creating output Hashmap, we can inform about it.
     */
    private void outputNeedToBeUpdated(){
        this.outputPositionsNeedsUpdate.set(true);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Word word1 = (Word) o;
        return Objects.equals(word, word1.word) &&
                Objects.equals(outputPositions, word1.outputPositions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(word, outputPositions);
    }
}
