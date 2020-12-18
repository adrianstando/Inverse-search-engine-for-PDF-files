package searching;

import files.FileContent;
import indexing.Author;
import indexing.AuthorDictionary;
import indexing.Word;
import indexing.WordsDictionary;

import java.util.*;
import java.util.stream.Collectors;

public class Search {
    private WordsDictionary wordsDictionary;
    private AuthorDictionary authorDictionary;

    private HashMap<String, Word> wordsHashMap;
    private HashMap<String, Author> authorHashMap;

    public Search(AuthorDictionary authorDictionary, WordsDictionary wordsDictionary){
        this.authorDictionary = authorDictionary;
        this.wordsDictionary = wordsDictionary;
        updateDictionaries();
    }

    public void updateDictionaries(){
        this.wordsHashMap = wordsDictionary.getDictionary();
        this.authorHashMap = authorDictionary.getDictionary();
    }

    public List<String> searchAuthor(String author){
        return new ArrayList<>(authorHashMap.get(author).getFilesConnectedWith());
    }

    public List<String> searchOneWord(String word){
        HashMap<String, List<Integer>> map = wordsHashMap.get(word).getAllFilesWithPositions();

        ArrayList<String> out = map.entrySet().stream()
                .sorted((a, b) -> -(a.getValue().size() - b.getValue().size()) )
                .map(Map.Entry::getKey)
                .collect(Collectors.toCollection(ArrayList::new));

        return out;
    }

    public List<String> searchPhrase(List<String> words){
        // getting Word objects of required words
        Set<Word> setOfWordObjects =  wordsHashMap.entrySet().stream()
                .parallel().unordered()
                .filter(words::contains)
                .map(Map.Entry::getValue)
                .collect(Collectors.toSet());

        // map Word object to WordInFile object
        LinkedList<WordInFile> list = new LinkedList<>();
        for(Word word : setOfWordObjects){
            word.getAllFilesWithPositions().entrySet().stream()
                    .map(r ->
                            new WordInFile(word.getWord(),
                                    r.getKey(),
                                    r.getValue().stream().collect(Collectors.groupingBy(e -> e, Collectors.counting())).entrySet()))
                    .forEach(list::add);
        }

        // map path vs list of WordInFile objects
        Map<String, List<WordInFile>> groupedFiles = list.stream()
                .collect(Collectors.groupingBy(WordInFile::getPath));

        // map path to how many phrases in file
        Map <String, Integer> numberOfPhrasesInFile = new HashMap<>();
        for(Map.Entry<String, List<WordInFile>> x : groupedFiles.entrySet()){
            numberOfPhrasesInFile.put(x.getKey(), howManyCommon(x.getValue()));
        }

        //return sorted List of file paths

        return numberOfPhrasesInFile.entrySet().stream()
                .sorted((a, b) -> -(a.getValue() - b.getValue()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toCollection(ArrayList::new));

        /*setOfWordObjects.stream()
                .collect(Collectors.groupingBy())*/

        //return null;
    }

    private int howManyCommon(List<WordInFile> list){
        int k = 0;
        for(Line line : list.get(0).getPositions()){
            int lineNumber = line.getLine();
            int howMany = line.getNumber();


            for(WordInFile wordFile : list){
                boolean czyZnalezionyWPliku = false;
                for(Line lineInside : wordFile.getPositions()){
                    if (lineInside.getLine() == lineNumber){
                        czyZnalezionyWPliku = true;
                        howMany = Math.min(howMany, lineInside.getNumber());
                    }
                    if(czyZnalezionyWPliku) break;
                }


                if(!czyZnalezionyWPliku) break;
            }

          k += howMany;

        }

        return 0;
    }

    public List<String> searchOneWordAndFilterByAuthor(String word, String author){
        List<String> wordList = searchOneWord(word);
        List<String> authorList = searchAuthor(author);

        ArrayList<String> out = wordList.stream()
                .filter(authorList::contains)
                .collect(Collectors.toCollection(ArrayList::new));

        return out;
    }

    public List<String> searchPhraseAndFilterByAuthor(List<String> words, String author){
        List<String> wordList = searchPhrase(words);
        List<String> authorList = searchAuthor(author);

        ArrayList<String> out = wordList.stream()
                .filter(authorList::contains)
                .collect(Collectors.toCollection(ArrayList::new));

        return out;
    }


    class WordInFile{
        private String word;
        private List<Line> positions;
        private String path;

        public WordInFile(String word, String path, List<Line> positions){
            this.word = word;
            this.positions = positions;
            this.path = path;
        }

        public WordInFile(String word, String path, Set<Map.Entry<Integer, Long>> positionsXXX){
            this.word = word;
            this.path = path;

            this.positions = new LinkedList<>();

            for(Map.Entry<Integer, Long> e : positionsXXX){
                Line line = new Line(e.getKey(), e.getValue().intValue());
                this.positions.add(line);
            }
        }

        public String getWord() {
            return word;
        }

        public void setWord(String word) {
            this.word = word;
        }

        public List<Line> getPositions() {
            return positions;
        }

        public void setPositions(List<Line> positions) {
            this.positions = positions;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }
    }

    class Line{
        private int line;
        private int number;

        public Line(int line, int number) {
            this.line = line;
            this.number = number;
        }

        public int getLine() {
            return line;
        }

        public void setLine(int line) {
            this.line = line;
        }

        public int getNumber() {
            return number;
        }

        public void setNumber(int number) {
            this.number = number;
        }
    }

}
