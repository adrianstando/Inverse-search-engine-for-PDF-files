package indexing;

import files.FileContent;

import java.util.concurrent.BlockingQueue;
import java.util.stream.Stream;

/**
 * Class builds inverted index from given data.
 */
public class InvertedIndex implements Runnable{
    private BlockingQueue<FileContent> filesContent;
    private WordsDictionary wordsDictionary;
    private AuthorDictionary authorDictionary;

    private final String poison = "THIS_IS_THE_END.non_existing_extension";
    private boolean running = true;
    private int intPositionInFile = -1;

    /*private final List<String> commonWords = Arrays.stream(new String [] {"ja", "ty", "on", "oni", "my", "wy", "albo", "bo", "lub", "ponieważ", "i", "oraz", "ale", "z", "w"})
            .map(String::toLowerCase)
            .collect(Collectors.toList());*/

    public InvertedIndex(BlockingQueue<FileContent> filesContent, WordsDictionary wordsDictionary, AuthorDictionary authorDictionary){
        this.filesContent = filesContent;
        this.wordsDictionary = wordsDictionary;
        this.authorDictionary = authorDictionary;
    }

    /**
     * Method build Inverted Index from the objects from fileContent queue and puts data
     * to wordDictionary and authorDictionary.
     *
     * @throws InterruptedException
     */
    private void buildIndex() throws InterruptedException {
        FileContent fileContent = filesContent.take();
        if (fileContent.getFile().getName().equals(poison)){
            stop();
        } else{
            String path = fileContent.getFile().getPath();

            String text = fileContent.getContent();
            text = text.replaceAll("[^(\\p{Blank} | \\p{IsAlphabetic} )]", " ");
            text = text.replaceAll("\\p{Punct}", " ");
            text = text.replaceAll("\\s+"," ");
            //System.out.println(text);
            Stream.of(text.split(" "))
                    .map(String::toLowerCase)
                    //.filter(r -> !commonWords.contains(r))
                    .forEach(s -> wordsDictionary.add(s, path, iterator()));

            authorDictionary.add(fileContent.getAuthor(), path);
        }

        intPositionInFile = -1;
    }

    /**
     * Program builds index in it's own thread.
     */
    @Override
    public void run() {
        while (running){
            try {
                buildIndex();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * Method stops building index.
     */
    public void stop(){
        running = false;
    }

    /**
     * Additional method required for stream in buildIndex() methos.
     * Returns position in file which is being read.
     *
     * @return position in file
     */
    private int iterator(){
        intPositionInFile++;
        return intPositionInFile;
    }
}
