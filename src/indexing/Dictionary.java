package indexing;

import java.util.HashMap;
import java.util.Set;

public interface Dictionary {
    public HashMap<String, ? extends PDFComponent> getDictionary();

    public Set<String> getAllUniqueKeys();

    public Set<String> getFilesWith(String object);
}
