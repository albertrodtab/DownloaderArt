package com.alberto.downloader.controller;

import com.alberto.downloader.task.DownloadTask;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;

public class DownloadController implements Initializable {

    public TextField tfUrl;
    public TextField delay;
    public Label lbStatus;
    public Label lbDelay;
    public ProgressBar pbProgress;
    private String urlText;
    private DownloadTask downloadTask;
    private File defaultFile;
    private File file;
    private AppController controler;
    private Timeline timeline = new Timeline();
    private ExecutorService exec;

    private static final Logger logger = LogManager.getLogger(DownloadController.class);

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        tfUrl.setText(urlText);
    }

    public DownloadController(String urlText, File defaultFile, ExecutorService executor) {
        //recojo el texto del enlace y lo muestro en la caja de texto de la pantalla de descarga.
        logger.info("Creado: " + urlText);
        this.urlText = urlText;
        this.defaultFile = defaultFile;
        this.exec = executor;

    }

        public void start(){
        try {
            downloadTask = new DownloadTask(urlText, file);

            pbProgress.progressProperty().unbind();

            pbProgress.progressProperty().bind(downloadTask.progressProperty());

            downloadTask.stateProperty().addListener((observableValue, oldState, newState) -> {
                System.out.println(observableValue.toString());
                if (newState == Worker.State.SUCCEEDED) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setContentText("La descarga ha terminado");
                    alert.show();
                }// Comentar esta parte de codigo para no limitar el tamaño de la descarga.
                  else if (newState == Worker.State.FAILED){
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setContentText("El tamaño de la descarga es superior al límite de 10 MB.");
                    alert.show();
                  }
            });

            downloadTask.messageProperty()
                    .addListener((observableValue, oldValue, newValue) -> lbStatus.setText(newValue));
            // con esto cada vez que invoco el método start lanzo un hilo de manera manual.
            //new Thread(downloadTask).start();

            //Si llamo al execute él se encargará de lanzar la tarea y controlar que se ejecuten de manera
            //concurrente tantas como yo haya definido al declarar el execute.
            exec.execute(downloadTask);



        }catch (MalformedURLException murle) {
            murle.printStackTrace();
            logger.error("URL mal formada", murle.fillInStackTrace());
        } catch (IOException e) {
            System.out.println("Ha ocurrido un error inesperado.");
            e.printStackTrace();
        }

    }

    @FXML
    public void start(ActionEvent event) {
        //Esto me permite elegir el directorio donde deseo guardar la descarga, si el usuario
        //no lo cambia se descargará en el directorio establecido por defecto en la aplicación
        logger.info("Inicio proceso descarga, escoge donde guardarla");
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(defaultFile);
        file = fileChooser.showSaveDialog(tfUrl.getScene().getWindow());
        if (file == null)
            return;
        logger.info("Fin de proceso de escoger donde guardar la descarga");
        int delayTime = delay();
        logger.info("Retraso de la descarga");

        if (delayTime == 0) {
            start();
        } else {
            timeline = new Timeline (
                    new KeyFrame(
                            Duration.seconds(delayTime), actionEvent -> start()
                    )
            );
            timeline.play();
            logger.info("Fin retraso de la descarga");
        }
    }

    //con esté método puedo hacer que el inicio de una descarga se retrase por el tiempo que yo elija.
    public int delay() {
        if (delay.getText().equals("")) {
            return 0;
        } else {
            try {
                if (Integer.parseInt(delay.getText()) > 0) {
                    return Integer.parseInt(delay.getText());
                } else {
                    return 0;
                }
            } catch (NumberFormatException nfe) {
                return 0;
            }
        }
    }

    //Este método me permite parar una única descarga, la que el usuario decida
    @FXML
    public void stop(ActionEvent event) {
        stop();
    }

    public void stop() {
        if (downloadTask != null) {
            if (timeline.getStatus() == Animation.Status.RUNNING) {
                timeline.stop();
            }
            pbProgress.progressProperty().unbind();
            pbProgress.setProgress(0);
            logger.info("Cancelado: " + urlText);
            downloadTask.cancel();
        }
    }


    public void stopAll() {
        if (downloadTask != null) {
            if (timeline.getStatus() == Animation.Status.RUNNING) {
                timeline.stop();
                logger.info("Cancelado: " + urlText);
                downloadTask.cancel();
            }
            pbProgress.progressProperty().unbind();
            pbProgress.setProgress(0);
            logger.info("Cancelado: " + urlText);
            downloadTask.cancel();
        }
    }

    //Con este método elimino una descarga.
    @FXML
    public void delete(ActionEvent event) {
        delete();
    }

    public void delete() {
        if (file != null) {
            logger.info("Eliminado: " + file);
            file.delete();
        }
    }

    public String getUrlText() {
        return urlText;
    }


    //PRUEBAS QUE ESTOY REALIZANDO PARA PODER CERRAR LA APLICACIÓN
    /*Con esto puedo cerrar toda la pantalla*/
    /*@FXML
    private void btnCerrar_Click(ActionEvent e) {
        cerrarVentana(e);
    }

    public static void cerrarVentana(ActionEvent e) {
        Node source = (Node) e.getSource();     //Me devuelve el elemento al que hice click
        Stage stage = (Stage) source.getScene().getWindow();//Me devuelve la ventana donde se encuentra el elemento
        stage.close();                          //Me cierra la ventana
    }*/
}
