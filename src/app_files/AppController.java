package app_files;

import controller.Controller;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

public class AppController {
    public Button buttonChoosePathToIndex;
    public Button buttonReadIndexFromFile;
    public Button buttonWriteIndexToFile;
    public TextField textWords;
    public TextField textAuthor;
    public Button buttonSearch;
    public Pagination results = new Pagination(5, 0);
    public CheckBox disableEnglishStemmerButton;
    public Label currentIndexTextField;
    public Label currentIndexPathField;
    public Label currentIndexEnglishStemmerField;

    private Controller controller = new Controller(true);
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
            controller.setEnableStemmer(!disableEnglishStemmerButton.isSelected());

            generateWaitingAlertAndDoTask("Building index",
                    "The index is being built from path: \n" + selectedDirectory.getAbsolutePath(),
                    "Error!",
                    () -> {
                        // controller after indexing sets current path
                        controller.createIndex(selectedDirectory.getAbsolutePath());
                        indexLoaded = true;
                        return true;
                    },
                    () -> {
                        updateEnglishStemmerField();
                        updatePathField();
                    });


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
            generateWaitingAlertAndDoTask("Reading index",
                    "The index is being read from path: \n" + selectedFile.getAbsolutePath(),
                    "Error during reading index from file!",
                    () -> {
                        boolean b = controller.readDictionariesFromFile(selectedFile.getAbsolutePath());

                        if (!b){
                            return false;
                        } else {
                            indexLoaded = true;
                            return true;
                        }
                    },
                    () -> {
                        updateEnglishStemmerField();
                        updatePathField();
                    });
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
            generateWaitingAlertAndDoTask("Writing index to file",
                    "The index is being written to path: \n" + file.getAbsolutePath(),
                    "Error during writing index to file!",
                    () -> {
                        boolean b = controller.writeDictionariesToFile(file.getAbsolutePath());

                        if (!b){
                            return false;
                        } else {
                            indexLoaded = true;
                            return true;
                        }
                    },
                    () -> {} );
        }
    }

    /**
     * Action after clicking buttonSearch.
     * @param actionEvent
     */
    public void searchByWordsAndAuthor(ActionEvent actionEvent) {
        // when someone wants to search before loading/creating index
        if(!indexLoaded) generateErrorAlert("Index was neither created nor loaded!");

        String input = textWords.getText();
        String author = textAuthor.getText();

        searchResults = controller.search(input, author);
        updatePagination();
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
        results.setPageCount(Math.max(x, 1));
        results.setCurrentPageIndex(0);
        results.setMaxPageIndicatorCount(Math.min(10, Math.max(x, 1)));

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

        // bug in KDE Linux - this must be added
        // this makes alerts work on KDE Linux (Kubuntu)
        alert.setResizable(true);
        alert.onShownProperty().addListener(e -> {
            Platform.runLater(() -> alert.setResizable(false));
        });

        alert.showAndWait();
    }

    /**
     * Method which generates waiting alert with given title and text. Alert hides after function func ends.
     * @param title
     * @param text
     * @param func
     */
    private void generateWaitingAlertAndDoTask(String title, String text, String textOnError,
                                               BooleanSupplier func, Runnable funcAtSuccess){
        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.setTitle(title);

        Text textAlert = new Text(text);
        textAlert.setWrappingWidth(500);
        alert.getDialogPane().setContent(textAlert);

        ProgressIndicator progressIndicator = new ProgressIndicator();
        alert.setGraphic(progressIndicator);

        // bug in KDE Linux - this must be added
        // this makes alerts work on KDE Linux (Kubuntu)
        alert.setResizable(true);
        alert.onShownProperty().addListener(e -> {
            Platform.runLater(() -> alert.setResizable(false));
        });

        Task<Boolean> task = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                return func.getAsBoolean();
            }
        };

        // when task ends, the alert disappears
        task.setOnSucceeded(e ->{
            alert.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL);
            alert.close();
            funcAtSuccess.run();
        } );
        task.setOnFailed(e -> {
            alert.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL);
            alert.close();
            generateErrorAlert(textOnError);
        });


        // run alert and task
        alert.show();
        Thread taskThread = new Thread(task);
        taskThread.start();
    }

    /**
     * Method updates current path field.
     */
    private void updatePathField(){
        String path = controller.getCurrentPath();
        currentIndexPathField.setText("Path: " + path);
    }

    /**
     * Method updates current English Stemmer state field.
     */
    private void updateEnglishStemmerField(){
        if(controller.isEnableStemmer()){
            currentIndexEnglishStemmerField.setText("English Stemmer: ENABLED");
        } else {
            currentIndexEnglishStemmerField.setText("English Stemmer: DISABLED");
        }
    }

}
