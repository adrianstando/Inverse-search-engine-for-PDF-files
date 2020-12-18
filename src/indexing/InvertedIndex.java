package indexing;

import files.FileContent;
import org.tartarus.snowball.ext.PorterStemmer;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Collectors;
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

    private final List<String> commonWords = Arrays.stream(new String [] {"a", "able", "about", "all", "an", "and", "any", "are", "as", "at", "be", "been", "by",
            "can", "can't", "could", "couldn't", "do", "does", "doesn't", "don't", "down", "has", "hasn't", "have", "haven't", "he", "here", "his", "how",
            "I", "I'm", "if", "in", "is", "it", "its", "it's", "just", "like", "many", "much", "no", "not", "now", "of", "on", "one",
            "or", "she", "so", "than", "that", "the", "them", "then", "there", "these", "they", "this", "those", "to", "too", "up", "very", "was", "we", "were",
            "what", "when", "where", "which", "who", "will", "won't", "would", "you", "you'd", "you'll"}).map(String::toLowerCase).map(r -> r.replaceAll("\\p{Punct} | ’", "")).collect(Collectors.toList());

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
        out = out.replaceAll(" ", "  ");
        // usuwam wszystkie niepotrzebne znaki
        out = out.replaceAll("[^(\n | \\p{Blank} | \\p{IsAlphabetic} | \\p{Digit} | \\p{Punct}| ’)]", " ");
        // oraz znaki interpunkcyjne
        out = out.replaceAll("\\p{Punct}", "");
        out = out.replaceAll("’", "");
        // tam gdzie jest kilka pustych znakow obok siebie (ale bez znaku nowej linii)
        //text = text.replaceAll("\\s+"," ");
        out = out.replaceAll("[^(\n | \\p{IsAlphabetic} | \\p{Digit})]+", "");
        // i tam gdzie nowa linia, dodajmy spację, by rozdzielic slowa
        out = out.replaceAll("\n", " \n");
        out = out.replaceAll("\\p{Blank}+", " ");

        return out;
    }

    private String stemWord(String word){
        porterStemmer.setCurrent(word);
        porterStemmer.stem();
        return porterStemmer.getCurrent();
    }
}
