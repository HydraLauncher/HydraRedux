package org.gethydra.redux;

import javafx.application.Application;
import javafx.stage.Stage;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.ByteBuddyAgent;
import org.gethydra.redux.backend.ZipUtil;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
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
            ex.printStackTrace(System.err);
            System.exit(0);
        }
    }

    public static void main(String[] args)
    {
        try
        {
//            ByteBuddyAgent.install(); //TODO: this should probably have a backup or failsafe in case it doesnt work

            InputStream stream = Main.class.getResourceAsStream("/logger.properties");
            LogManager.getLogManager().readConfiguration(stream);
            log = Logger.getLogger("HydraRedux");
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            JOptionPane.showMessageDialog(null, ex.getMessage());
        }

        log.info("Started");

        System.getenv().forEach((key, value) -> log.info("Diagnostic (" + key + "): " + value));

        try
        {
            launch(args);
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            JOptionPane.showMessageDialog(null, ex.getMessage());
            System.exit(0);
        }
    }


}
