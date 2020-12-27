package indexing;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Class represents author and the list of .*pdf files created by him / her.
 */

public class Author implements PDFComponent, Serializable {
    private String author;
    private LinkedBlockingQueue<String> files = new LinkedBlockingQueue<>();

    private HashSet<String> output;

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
        this.output = new HashSet<>(files);
        return this.output;
    }

    @Override
    public String toString() {
        return  author + "; " + files.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Author author1 = (Author) o;
        return Objects.equals(author, author1.author) &&
                Objects.equals(output, author1.output);
    }

    @Override
    public int hashCode() {
        return Objects.hash(author, output);
    }
}
