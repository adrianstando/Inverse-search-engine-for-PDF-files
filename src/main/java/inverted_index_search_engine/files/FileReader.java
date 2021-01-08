package main.java.inverted_index_search_engine.files;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.util.PDFTextStripper;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;

/**
 * Class enables reading the content from *.pdf files, which were put in files queue.
 */

public class FileReader implements Runnable {
    private final String poison = "THIS_IS_THE_END.non_existing_extension";
    private int intPutExtraPoison = 0;

    private BlockingQueue<File> files;
    private BlockingQueue<FileContent> filesContent;

    private boolean running = true;

    public FileReader(BlockingQueue<File> files, BlockingQueue<FileContent> filesContent){
        this.files = files;
        this.filesContent = filesContent;
    }

    /**
     * This method reads content from next file from queue files and return FileContent object.
     *
     * @return FileContent object
     * @throws IOException
     * @throws InterruptedException
     */
    public FileContent getNextFile() throws IOException, InterruptedException {
        File file = files.take();

        if (file.getPath().equals(poison)){
            return new FileContent(file, "", "");
        } else {

            PDDocument document;
            try {
                document = PDDocument.load(file);
                if (document.isEncrypted()){
                    // if PDF file is encrypted
                    document.close();
                    return new FileContent(file, "", "");
                }
            } catch (IOException e) {
                // if any problem with loading PDF file occurs
                return new FileContent(file, "", "");
            }

            PDDocumentInformation info = document.getDocumentInformation();
            String author = info.getAuthor();
            if (author == null) author = "";

            PDFTextStripper pdfTextStripper = new PDFTextStripper();
            String text;
            try {
                text = pdfTextStripper.getText(document);
            } catch (IOException e) {
                text = "";
            }

            if (text == null) text = "";

            document.close();

            return new FileContent(file, text, author);
        }
    }

    /**
     * If there are more indexing threads than reading threads, program can put some extra poison pills. User can set
     * the number of this extra poison pills by this function.
     *
     * @param putExtraPoison
     */
    public void putExtraPoison(int putExtraPoison) {
        this.intPutExtraPoison = putExtraPoison;
    }


    /**
     * Program reads from files in its own thread. When content is extracted, it is put in filesContent queue.
     */
    @Override
    public void run() {
        while(running){
            FileContent fileContent;
            try {
                fileContent = getNextFile();
                filesContent.put(fileContent);

                if(fileContent.getFile().getPath().equals(poison)){
                    stop();
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }

        while (intPutExtraPoison > 0){
            try {
                filesContent.put(new FileContent(new File(poison), "", ""));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            intPutExtraPoison--;
        }
    }

    /**
     * Function stops thread.
     */
    public void stop(){
        running = false;
    }

}
