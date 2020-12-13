package indexing;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Class represents author and the list of .*pdf files created by him / her.
 */

public class Author implements PDFComponent{
    private String author;
    private LinkedBlockingQueue<String> files = new LinkedBlockingQueue<>();

    public Author(String author){
        this.author = author;
    }

    public Author(String author, String path){
        this.author = author;
        add(path);
    }

    /**
     * This method adds new file created by author.
     *
     * @param path
     */
    public void add(String path){
        try {
            files.put(path);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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
