package main.java.inverted_index_search_engine.indexing;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class represents dictionary of authors - it stores all authors and information about them.
 */
public class AuthorDictionary implements Dictionary, Serializable {
    private ConcurrentHashMap<String, Author> dictionary = new ConcurrentHashMap<>();

    public AuthorDictionary(){

    }

    /**
     * This method adds to dictionary new information about an author.
     *
     * @param author
     * @param path
     */
    public void add(String author, String path){
        if (author.equals("")) return;

        Author x = dictionary.putIfAbsent(author, new Author(author, path));
        if (x != null) x.add(path);
    }

    public HashMap<String, Author> getDictionary() {
        return new HashMap<>(dictionary);
    }

    @Override
    public Set<String> getAllUniqueKeys() {
        return dictionary.keySet();
    }

    @Override
    public Set<String> getFilesWith(String object) {
        Author author = dictionary.get(object);
        if (author == null) return new HashSet<>();
        else return author.getFilesConnectedWith();
    }

    @Override
    public String toString() {
        StringBuilder text = new StringBuilder("Author Dictionary: " + System.lineSeparator());
        for (String x : dictionary.keySet()){
            text.append(dictionary.get(x).toString());
            text.append(System.lineSeparator());
        }
        return text.toString();
    }

}
