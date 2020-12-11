package indexing;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Author implements PDFComponent{
    private String author;
    private List<String> files = new LinkedList<>();

    public Author(String author){
        this.author = author;
    }

    public synchronized void add(String path){
        files.add(path);
    }

    @Override
    public Set<String> getFilesConnectedWith(){
        return new HashSet<>(files);
    }

    @Override
    public String toString() {
        return  author + "; " + files.toString();
    }
}
