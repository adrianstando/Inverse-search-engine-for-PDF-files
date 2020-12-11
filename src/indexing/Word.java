package indexing;

import java.util.*;

public class Word implements PDFComponent{
    private String word;
    private HashMap<String, List<Integer>> position = new HashMap<>();

    public Word(String word){
        this.word = word;
    }

    public synchronized void add(String path, int positionInFile){
        List<Integer> obj = position.get(path);
        if (obj == null){
            obj = new LinkedList<>();
        }
        obj.add(positionInFile);
        position.put(path, obj);
    }

    public List<Integer> getPositionsOfTheWordInFile(String path){
        return position.get(path);
    }

    @Override
    public Set<String> getFilesConnectedWith(){
        return position.keySet();
    }

    public Map<String, List<Integer>> getAllFilesWithPositions(){
        return position;
    }

    @Override
    public String toString() {
        return  word + "; " + position.toString();
    }
}
