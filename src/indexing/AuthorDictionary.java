package indexing;

import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class AuthorDictionary implements Dictionary {
    private ConcurrentHashMap<String, Author> dictionary;

    public AuthorDictionary(ConcurrentHashMap<String, Author> dictionary){
        this.dictionary = dictionary;
    }

    public void add(String author, String path){
        /*Author obj = dictionary.get(author);
        if(obj == null){
            obj = new Author(author);
        }

        obj.add(path);
        dictionary.put(author, obj);*/

        Author x = dictionary.putIfAbsent(author, new Author(author, path));
        if (x != null) x.add(path);
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
        StringBuilder text = new StringBuilder("Author Dictionary: " + System.lineSeparator());
        for (String x : dictionary.keySet()){
            text.append(dictionary.get(x).toString());
            text.append(System.lineSeparator());
        }
        return text.toString();
    }


}
