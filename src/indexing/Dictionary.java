package indexing;

import java.util.HashMap;
import java.util.Set;

/**
 * Interface represents dictionary needed for indexing. It stores for each PDFComponent list or map of connected files.
 */
public interface Dictionary {
    // public HashMap<String, ? extends PDFComponent> getDictionary();

    public Set<String> getAllUniqueKeys();

    public Set<String> getFilesWith(String object);
}
