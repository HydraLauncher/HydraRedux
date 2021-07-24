package org.gethydra.redux;

import javafx.application.Application;
import javafx.stage.Stage;

import java.io.InputStream;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class Main extends Application
{
    private static Logger log;

    public void start(Stage primaryStage) throws Exception
    {
        try
        {
            new HydraRedux().init(primaryStage);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(0);
        }
    }

    public static void main(String[] args)
    {
        try
        {
            InputStream stream = Main.class.getResourceAsStream("/logger.properties");
            LogManager.getLogManager().readConfiguration(stream);
            log = Logger.getLogger("HydraRedux");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        log.info("Started");

        try
        {
            launch(args);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(0);
        }
    }
}
