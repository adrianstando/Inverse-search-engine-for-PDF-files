package main.java.inverted_index_search_engine.indexing;

import java.util.Set;

/**
 * Interface represents some parts of PDF file, like it's author or words, and stores files connected with it.
 */
public interface PDFComponent {
    public Set<String> getFilesConnectedWith();
}
