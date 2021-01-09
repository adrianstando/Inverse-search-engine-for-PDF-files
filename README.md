# Inverse search engine for PDF files

## App description

The app builds [Inverted index](https://en.wikipedia.org/wiki/Inverted_index) from all `*.pdf` files stored in chosen directory and its subdirectories so as to enable fast word (and phrase) searches.
Additionally, you can filter results by author (extracted from pdf file metadata).

The app was created mainly for pdf files in English, but you can use it for any other language.

## Run application

The final files are located in `out` folder.

To run application on Linux, type in the following command:

```
java -jar Inverse-search-engine-for-PDF-files.jar
```

To run application on Windows, just open `Inverse-search-engine-for-PDF-files.exe` file.

On Windows, so as to enable correct scalling, you should do the following:

1. Right click on `Inverse-search-engine-for-PDF-files.exe` -> `Properties` -> `Comptability` -> `Override high DPI scalling behaviour`

2. In `Scaling performed by:` choose `System`.

## User manual

1. When you run app, you can see the following GUI.

![GUI](/images/GUI.jpg)

2. Firstly, you need to build index. Click button `Choose path to index` to choose starting path. 
The app uses by default [PorterStemmer](https://opennlp.apache.org/docs/1.7.2/apidocs/opennlp-tools/opennlp/tools/stemmer/PorterStemmer.html) library for [word stemming](https://en.wikipedia.org/wiki/Stemming). You can disable this option by selecting appropriate box (you have to do it before choosing starting path).

![Building index](/images/building_index.jpg)

3. Once index is built, you can search words and phrases. To do it, type in them into text field next to **Words**. If you want to choose files created only by  one particular author, type in its name next to **Author**, and click **SEARCH** button.

![Searching](/images/searching.jpg)

4. You will see results below sorted by the number of occurences. The file name is displayed in blue and it's path in black below. Note that text in blue is hyperlink and if you click it, the corresponding file opens.

![Results](/images/results.jpg)

5. You can also write created index to file by clicking **Write index to file** button. Next time you want to search something in the same path, you can just simply load created index from file by clicking **Read index from file**.

![Saving and loading index](/images/saving_and_loading_index.jpg)

## Project purpose

It is the final project created during Java course at Data Science studies at MiNI, Warsaw University of Technology.
