import controller.Controller;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AppController {
    public Button buttonChoosePathToIndex;
    public Button buttonReadIndexFromFile;
    public Button buttonWriteIndexToFile;
    public TextField textWords;
    public TextField textAuthor;
    public Button buttonSearch;
    public Pagination results = new Pagination(5, 0);

    private Controller controller = new Controller();
    private boolean indexLoaded = false;

    private List<String> searchResults = new ArrayList<>();
    private int maxItemsPerPage = 5;

    /**
     * Action after clicking buttonChoosePathToIndex.
     * @param actionEvent
     */
    public void choosePathToIndex(ActionEvent actionEvent) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File selectedDirectory = directoryChooser.showDialog(null);
        if(!(selectedDirectory == null)){
            controller.createIndex(selectedDirectory.getAbsolutePath());
            indexLoaded = true;
        }
    }

    /**
     * Action after clicking buttonReadIndexFromFile.
     * @param actionEvent
     */
    public void readIndexFromFile(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("SER files (*.ser)", "*.ser");
        fileChooser.getExtensionFilters().add(extFilter);
        File selectedFile = fileChooser.showOpenDialog(null);
        if(!(selectedFile == null)){
            boolean b = controller.readDictionariesFromFile(selectedFile.getAbsolutePath());

            if(!b) generateErrorAlert("Error during reading index file!");
            else indexLoaded = true;
        }
    }

    /**
     * Action after clicking buttonWriteIndexToFile.
     * @param actionEvent
     */
    public void writeIndexToFile(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("SER files (*.ser)", "*.ser");
        fileChooser.getExtensionFilters().add(extFilter);
        File file = fileChooser.showSaveDialog(null);

        if(!(file == null)){
            boolean b = controller.writeDictionariesToFile(file.getAbsolutePath());

            if(!b) generateErrorAlert("Error during writing index to file!");
            else indexLoaded = true;
        }
    }

    /**
     * Action after clicking buttonSearch.
     * @param actionEvent
     *
     * @todo search phrase
     */
    public void searchByWordsAndAuthor(ActionEvent actionEvent) {
        // when someone wants to search before loading/creating index
        if(!indexLoaded) generateErrorAlert("Index was neither created nor loaded!");

        // for now simplified version, only with one word
        String word = textWords.getText();
        String author = textAuthor.getText();

        if (author.equals("")){
            if (!(word.equals(""))){
                searchResults = controller.searchOneWord(word);
                updatePagination();
            }
        } else {
            if (!(word.equals(""))){
                searchResults = controller.searchOneWordAndFilterByAuthor(word, author);
                updatePagination();
            }
        }
    }

    /**
     * Method which creates pages in Pagination.
     * @param pageIndex
     * @return
     */
    private VBox createPage(int pageIndex){
        VBox box = new VBox(maxItemsPerPage);
        int position = pageIndex * maxItemsPerPage;

        for(int i = position; i < position + maxItemsPerPage; i++){
            if (i >= searchResults.size()) break;

            VBox element = new VBox();
            String path = searchResults.get(i);
            File file = new File(path);

            Hyperlink link = new Hyperlink(file.getName());
            link.setFont(Font.font(Font.getDefault().getFamily(), FontWeight.BOLD, 12));
            link.setOnAction(e -> {
                if (Desktop.isDesktopSupported()) {
                    new Thread(() -> {
                        try {
                            Desktop.getDesktop().open(file);
                        } catch (IOException w) {
                            w.printStackTrace();
                        }
                    }).start();
            }});

            Label text = new Label(path);
            text.setMaxWidth(550);
            text.setWrapText(true);

            element.getChildren().addAll(link, text);
            box.getChildren().add(element);
        }

        return box;
    }

    /**
     * Method which updates Pagination's properties.
     */
    private void updatePagination(){
        int x = (int) Math.ceil((double)searchResults.size() / (double)maxItemsPerPage);
        results.setPageCount(x);
        results.setCurrentPageIndex(0);

        results.setPageFactory(this::createPage);
    }

    /**
     * Method which generates error alert with information from text.
     * @param text
     */
    private void generateErrorAlert(String text){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(text);

        // this makes alerts work on KDE Linux (Kubuntu)
        alert.setResizable(true);
        alert.onShownProperty().addListener(e -> {
            Platform.runLater(() -> alert.setResizable(false));
        });

        alert.showAndWait();
    }

}
