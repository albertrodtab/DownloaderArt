package com.alberto.downloader.task;

import javafx.concurrent.Task;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.math.RoundingMode;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.Instant;

public class DownloadTask extends Task<Integer> {

    private URL url;
    private File file;
    boolean suspender; //Suspende un hilo cuando es true

    private static final Logger logger = LogManager.getLogger(DownloadTask.class);

    public DownloadTask(String urlText, File file) throws MalformedURLException {
        this.url = new URL(urlText);
        this.file = file;
    }

   @Override
    protected Integer call() throws Exception {
        logger.info("Descarga " + url.toString() + " iniciada");
        updateMessage("Conectando con el servidor . . .");

        URLConnection urlConnection = url.openConnection();
        double fileSize = urlConnection.getContentLength();

        double megaSize = fileSize / 1048576;
        if (megaSize > 10){
            logger.trace("Máximo tamaño de fichero alcanzado");
            throw new Exception("Max. size");
        }
        BufferedInputStream in = new BufferedInputStream(url.openStream());
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        byte dataBuffer[] = new byte[1024];
        int bytesRead;
        int totalRead = 0;
        double downloadProgress = 0;
        Instant start = Instant.now();
        Instant current;
        float elapsedTime;

        while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
            downloadProgress = ((double) totalRead / fileSize);

            current = Instant.now();
            elapsedTime = Duration.between(start, current).toSeconds();
            updateProgress(downloadProgress, 1);

            //esto era lo que tenía yo para calcular el porcentaje
            /*DecimalFormat df = new DecimalFormat("##.##");
            df.setRoundingMode(RoundingMode.DOWN);*/

            updateMessage(Math.round(downloadProgress * 100) + " %\t\t\t\t" + Math.round(elapsedTime)+" sec.");

            //comentar esta linea para que la descarga se produzca a la velocidad normal, esto ralentiza la velocidad de descarga.
            Thread.sleep(1);

            fileOutputStream.write(dataBuffer, 0, bytesRead);
            totalRead += bytesRead;

            if (isCancelled()) {
                logger.info("Descarga " + url.toString() + " cancelada");
                updateMessage("");
                fileOutputStream.close();
                return null;
            }
        }

        updateProgress(1, 1);
        updateMessage("100 %");
        fileOutputStream.close();
        logger.info("Descarga " + url.toString() + " finalizada");
        return null;
    }

}
