package files;

import java.io.File;
import java.util.concurrent.ArrayBlockingQueue;
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
     * This method searches recursively for files with PDF extension.
     *
     * @param directory
     * @throws InterruptedException
     */
    private void findAndAddAllPDFFiles(File directory) throws InterruptedException {
        File[] listOfFiles = directory.listFiles();

        if (listOfFiles==null) return;

        for(File fileInDirectory : listOfFiles){
            if(fileInDirectory.isFile() && isPDF(fileInDirectory)){
                files.put(fileInDirectory);
            } else if (fileInDirectory.isDirectory()){
                findAndAddAllPDFFiles(fileInDirectory);
            }
        }
    }

    /**
     * Method checks if the File object has .pdf extension.
     *
     * @param file
     * @return isItPDF
     */

    private boolean isPDF(File file){
        String fileName = file.getName();
        boolean isItPDF = fileName.matches(".*\\.pdf") || fileName.matches(".*\\.PDF");

        return isItPDF;
    }

    /**
     * Function searches *.pdf files in its own thread.
     */
    @Override
    public void run() {
        try {
            findAndAddAllPDFFiles(new File(pathZero));
        } catch (InterruptedException e) {
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

    /*public static void main(String[] args){
        BlockingQueue<File> files = new ArrayBlockingQueue<>(10);

        String currentDirectory = System.getProperty("user.dir");
        System.out.println("The current working directory is " + currentDirectory);

        FileFinder fileFinder = new FileFinder("./dysk", 1, files);

        Thread thread = new Thread(fileFinder);
        thread.start();

        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println(files);
    }*/
}
