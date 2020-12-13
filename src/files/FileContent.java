package files;

import java.io.File;

/**
 * Class represents the content of the *.pdf file.
 */

public class FileContent {
    private File file;
    private String content;
    private String author;

    public FileContent(File file, String content, String author){
        this.file = file;
        this.content = content;
        this.author = author;
    }

    public File getFile() {
        return file;
    }

    public String getContent() {
        return content;
    }

    public String getAuthor() {
        return author;
    }

    @Override
    public String toString() {
        String text = "Path: " + file.getPath()
                + "Author: " + author
               // + "\'" + content + "\'";
                ;
        return text;
    }
}
