package com.alberto.downloader.controller;

import com.alberto.downloader.util.R;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class AppController {

    public TextField tfUrl;
    public Button btDownload;
    public TabPane tpDownloads;

    //esto me servirá para ir guardando todas las descargas que se lancen.
    private Map<String, DownloadController> allDownloads;


    @FXML
    private ScrollPane scrollPane;
    public File defaultFile = new File("C:/Users/alber/IdeaProjects/ArtDownloader/ArtDownloader");
    public File file = defaultFile;

    private static final Logger logger = LogManager.getLogger(AppController.class);

    /*Este método me permite cambiar el directorio definido por defecto para almacenar las descargas*/
    @FXML
    private void changeDirectory (ActionEvent event) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setInitialDirectory(defaultFile);
        Stage stage = (Stage) scrollPane.getScene().getWindow();
        file = directoryChooser.showDialog(stage);
        if (file == null) {
            file = defaultFile;
        }
    }


    public AppController(){
        //inicializo la lista para ir guardando las descargas
        allDownloads = new HashMap<>();
    }

    @FXML
    public void launchDownload(ActionEvent event) {
        String urlText = tfUrl.getText();
        tfUrl.clear();
        tfUrl.requestFocus();

        launchDownload(urlText, file);
    }

    private void launchDownload(String url, File file) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(R.getUi("download.fxml"));
            DownloadController downloadController = new DownloadController(url, this.file);

            loader.setController(downloadController);
            VBox downloadBox = loader.load();

            String filename = url.substring(url.lastIndexOf("/") + 1);
            tpDownloads.getTabs().add(new Tab(filename, downloadBox));
            //Esto me permite añadir un botón de cierre a cada tapPane, sino lo configuro en el SceneBuilder
            //puedo elegir que se muestre en todas las pestañas, en ninguna o en la selecionada solo.
            //tpDownloads.setTabClosingPolicy(TabPane.TabClosingPolicy.ALL_TABS);

            //cada vez que lance una descarga, la voy a guardar en la lista, así los tengo controlados
            allDownloads.put(url, downloadController);
        } catch (IOException ioe) {
            System.out.println("Ha ocurrido un error inesperado.");
            ioe.printStackTrace();
        }
    }

    //Esto me permite parar todas la descargas.
    @FXML
    public void stopAllDownloads(ActionEvent event) {
        logger.info("Inicio parar todas las descargas");
        for (DownloadController downloadController : allDownloads.values())
            downloadController.stopAll();
        logger.info("Fin parar todas las descargas");
    }
    /*private void stopAllDownloads ()  {
        for (DownloadController downloadController : allDownloads.values())
            downloadController.stop();
    }*/

    //Con este método puedo ver en la pantalla de la aplicación el archivo de registro del LOG
    @FXML
    public void viewLog(ActionEvent event) throws IOException {
        Desktop desktop = Desktop.getDesktop();
        File log = new File("C:/Users/alber/IdeaProjects/ArtDownloader/ArtDownloader.log");
        desktop.open(log);
    }


}
