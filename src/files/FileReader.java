package files;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.util.PDFTextStripper;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;

public class FileReader implements Runnable {
    private final String poison = "THIS_IS_THE_END.non_existing_extension";

    private BlockingQueue<File> files;
    private BlockingQueue<FileContent> filesContent;

    private boolean running = true;

    public FileReader(BlockingQueue<File> files, BlockingQueue<FileContent> filesContent){
        this.files = files;
        this.filesContent = filesContent;
    }

    public FileContent getNextFile() throws IOException, InterruptedException {
        File file = files.take();

        if (file.getPath().equals(poison)){
            return new FileContent(file, "", "");
        } else {
            PDDocument document = PDDocument.load(file);
            PDDocumentInformation info = document.getDocumentInformation();
            PDFTextStripper pdfTextStripper = new PDFTextStripper();

            String author = info.getAuthor();
            String text = pdfTextStripper.getText(document);

            document.close();

            return new FileContent(file, text, author);
        }
    }

    private int intPutExtraPoison;
    public void PutExtraPoison(int putExtraPoison) {
        this.intPutExtraPoison = putExtraPoison;
    }

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

    public void stop(){
        running = false;
    }

    public static void main(String[] args){
        File file = new File("non.existing.file");
        System.out.println(file.getPath());
    }
}
