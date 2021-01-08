package main.java.inverted_index_search_engine.files;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.BlockingQueue;

/**
 * Class created so program can find all *.pdf files in pathZero directory an its subdirectories
 * and put them into files queue.
 */

public class FileFinder implements Runnable{
    private String pathZero;
    private BlockingQueue<File> files;
    private int numberOfReaderThreads;

    private final String poison = "THIS_IS_THE_END.non_existing_extension";

    public FileFinder(String pathZero, int numberOfReaderThreads, BlockingQueue<File> files){
        this.pathZero = pathZero;
        this.files = files;
        this.numberOfReaderThreads = numberOfReaderThreads;
    }

    /**
     * This method searches for files with PDF extension.
     *
     * @param directory
     * @throws IOException
     */
    public void findAndAddAllPDFFiles(String directory) throws IOException {
        Path start = Paths.get(directory);
        Files.walk(start, Integer.MAX_VALUE)
                .unordered()
                .parallel()
                .map(Path::toFile)
                //.peek(e -> System.out.println(e.getAbsolutePath()))
                .filter(e -> e.getAbsolutePath().toLowerCase().endsWith(".pdf"))
                .forEach(e -> {
                    try {
                        files.put(e);
                    } catch (InterruptedException interruptedException) {
                        interruptedException.printStackTrace();
                    }
                });
    }

    /**
     * Function searches *.pdf files in its own thread.
     */
    @Override
    public void run() {
        try {
            findAndAddAllPDFFiles(pathZero);
        } catch (IOException e) {
            e.printStackTrace();
        }
        for(int i = 0; i < numberOfReaderThreads; i++){
            try {
                files.put(new File(poison));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
}
