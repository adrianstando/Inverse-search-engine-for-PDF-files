package indexing;

import files.FileContent;
import org.tartarus.snowball.ext.PorterStemmer;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Stream;

/**
 * Class builds inverted index from given data.
 */
public class InvertedIndex implements Runnable{
    private BlockingQueue<FileContent> filesContent;
    private WordsDictionary wordsDictionary;
    private AuthorDictionary authorDictionary;

    private PorterStemmer porterStemmer = new PorterStemmer();

    private final String poison = "THIS_IS_THE_END.non_existing_extension";
    private boolean running = true;
    private int intPositionInFile = 0;

    private final List<String> commonWords;

    public InvertedIndex(BlockingQueue<FileContent> filesContent, WordsDictionary wordsDictionary, AuthorDictionary authorDictionary, List<String> commonWords){
        this.filesContent = filesContent;
        this.wordsDictionary = wordsDictionary;
        this.authorDictionary = authorDictionary;
        this.commonWords = commonWords;
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
            //System.out.println(text);
            text = convertInputText(text);
            //System.out.println(text);

            Stream.of(text.split(" "))
                    .map(String::toLowerCase)
                    .filter(r -> ! (r.equals(" ") || r.equals("")))
                    .filter(r -> !commonWords.contains(r))
                    .map(this::stemWord)
                    .forEach(s -> wordsDictionary.add(s.replaceAll("\n", ""), path, linesIterator(s)));

            /*Stream.of(text.split(" "))
                    .map(String::toLowerCase)
                    .filter(r -> ! (r.equals(" ") || r.equals("")))
                    .map(r -> r.replaceAll("\n", "!")).forEach(System.out::println);*/

            /*Stream.of(text.split(" "))
                    .map(String::toLowerCase)
                    .filter(r -> ! (r.equals(" ") || r.equals("")))
                    .filter(r -> !commonWords.contains(r))
                    .map(this::stemWord)
                    .forEach(System.out::println);*/

            authorDictionary.add(fileContent.getAuthor(), path);
        }

        intPositionInFile = 0;
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
    private int linesIterator(String text){
        if (text == null) return -1;
        else if(text.contains("\n")){
            intPositionInFile++;
        }
        return intPositionInFile;
    }

    /**
     * Method convert input text, so it doesn't contain unnecessary chars.
     *
     * @param input
     * @return
     */
    private String convertInputText(String input){
        if (input == null) return "";

        String out = input;
        // to simplify task below, change single space to double space
        out = out.replaceAll(" ", "  ");

        // remove all unwanted chars
        out = out.replaceAll("[^(\n | \\p{Blank} | \\p{IsAlphabetic} | \\p{Digit} | \\p{Punct}| ’)]", " ");

        // and punctuation marks and apostrophes
        out = out.replaceAll("\\p{Punct}", "");
        out = out.replaceAll("’", "");

        // replace multiple blank chars (without new line char)
        out = out.replaceAll("[^(\n | \\p{IsAlphabetic} | \\p{Digit})]+", "");

        // add space before new line char (\n) so the words won't mix
        out = out.replaceAll("\n", " \n");

        // at the end remove additional multiple blanks (if there are any additional)
        out = out.replaceAll("\\p{Blank}+", " ");

        return out;
    }

    private String stemWord(String word){
        porterStemmer.setCurrent(word);
        porterStemmer.stem();
        return porterStemmer.getCurrent();
    }
}