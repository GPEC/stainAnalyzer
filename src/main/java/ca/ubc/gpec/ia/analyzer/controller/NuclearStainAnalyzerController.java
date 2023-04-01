/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.ubc.gpec.ia.analyzer.controller;

import ca.ubc.gpec.ia.analyzer.model.IAO;
import ca.ubc.gpec.ia.analyzer.settings.StainAnalyzerSettings;
import ca.ubc.gpec.ia.analyzer.transformation.ImageTransformationException;
import ca.ubc.gpec.ia.analyzer.views.ViewConstants;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import static javafx.application.Application.launch;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 *
 * @author samuelc
 */
public class NuclearStainAnalyzerController extends StainAnalyzerController {

    public NuclearStainAnalyzerController() {
        super(null);
    }

    /**
     * constructor
     */
    public NuclearStainAnalyzerController(NuclearStainAnalyzer nuclearStainAnalyzer) {
        super(nuclearStainAnalyzer);
    }

    /**
     * instantiate an instance of StainAnalyzer, apply settings and analyze
     * input ioa
     *
     * @param iao
     * @throws ImageTransformationException
     * @throws MalformedURLException
     */
    public IAO analyzeIao(IAO iao) throws ImageTransformationException, MalformedURLException, URISyntaxException {
        // create an instance of Staing analyzer
        NuclearStainAnalyzer nuclearStainAnalyzer = new NuclearStainAnalyzer();

        // set setting ... already done when call setConfigJPanel(ConfigJPanel configJPanel)

        // analyze images
        return nuclearStainAnalyzer.analyzeIao(iao);

    }
    
            /**
     * display main page
     * @param stage
     * @throws IOException 
     */
    public void start(Stage stage) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource(ViewConstants.VIEWS_ROOT + "index.fxml"));

        stage.setTitle("GPEC StainAnalyzer");
        stage.setScene(new Scene(root, 300, 275));
        stage.show();
    }

    /**
     * The main() method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be
     * launched through deployment artifacts, e.g., in IDEs with limited FX
     * support. NetBeans ignores main().
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}
