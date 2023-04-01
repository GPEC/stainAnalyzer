/*
 * The keycounter cartoon image with ability to update count 
 */
package ca.ubc.gpec.ia.analyzer.views;

import java.io.IOException;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.media.AudioClip;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 *
 * @author samuelc
 */
public class CounterCartoon extends StackPane {

    public static final String CELL_COUNTER_IMAGE_FILENAME = "BCC2PICM_rotated_fit.jpeg";
    public static final String NEGATIVE_SOUND_FILENAME = "beep_09.wav";
    public static final String POSITIVE_SOUND_FILENAME = "Click01.wav";
    public static final String UNDO_SOUND_FILENAME = "Click02.wav";
    private Label positiveCountLabel;
    private Label negativeCountLabel;
    //private AudioClip negativeSound;
    //private AudioClip positiveSound;
    //private AudioClip undoSound;
    private Clip negativeCountSound;
    private Clip positiveCountSound;
    private Clip undoCountSound;

    /**
     * constructor
     */
    public CounterCartoon(int positiveCount, int negativeCount) {
        super();
        ImageView counterImageView = new ImageView(new Image(
                this.getClass().getResource(ViewConstants.RESOURCE_SEPARATOR + "images" + ViewConstants.RESOURCE_SEPARATOR + CELL_COUNTER_IMAGE_FILENAME).toExternalForm()));
        positiveCountLabel = new Label("" + positiveCount);
        negativeCountLabel = new Label("" + negativeCount);

        positiveCountLabel.setTranslateY(-35); // hard code position!!!
        positiveCountLabel.setTranslateX(43); // hard code position!!!

        negativeCountLabel.setTranslateY(-35); // hard code position!!!
        negativeCountLabel.setTranslateX(-8); // hard code position!!!
        try {
            // get sound files ...
            //negativeSound = new AudioClip(this.getClass().getResource(ViewConstants.VIEWS_ROOT + "sounds" + ViewConstants.RESOURCE_SEPARATOR + NEGATIVE_SOUND_FILENAME).toString());
            //positiveSound = new AudioClip(this.getClass().getResource(ViewConstants.VIEWS_ROOT + "sounds" + ViewConstants.RESOURCE_SEPARATOR + POSITIVE_SOUND_FILENAME).toString());
            //undoSound = new AudioClip(this.getClass().getResource(ViewConstants.VIEWS_ROOT + "sounds" + ViewConstants.RESOURCE_SEPARATOR + UNDO_SOUND_FILENAME).toString());
            negativeCountSound = AudioSystem.getClip();
            positiveCountSound = AudioSystem.getClip();
            undoCountSound = AudioSystem.getClip();
            negativeCountSound.open(AudioSystem.getAudioInputStream(this.getClass().getResource(ViewConstants.RESOURCE_SEPARATOR + "sounds" + ViewConstants.RESOURCE_SEPARATOR + NEGATIVE_SOUND_FILENAME)));
            positiveCountSound.open(AudioSystem.getAudioInputStream(this.getClass().getResource(ViewConstants.RESOURCE_SEPARATOR + "sounds" + ViewConstants.RESOURCE_SEPARATOR + POSITIVE_SOUND_FILENAME)));
            undoCountSound.open(AudioSystem.getAudioInputStream(this.getClass().getResource(ViewConstants.RESOURCE_SEPARATOR + "sounds" + ViewConstants.RESOURCE_SEPARATOR + UNDO_SOUND_FILENAME)));
            this.getChildren().addAll(counterImageView, positiveCountLabel, negativeCountLabel);
        } catch (UnsupportedAudioFileException | LineUnavailableException | IOException ex) {
            // do nothing
        }
    }

    /**
     * refresh counter
     *
     * @param positiveCount
     * @param negativeCount
     */
    public void refresh(int positiveCount, int negativeCount) {
        positiveCountLabel.setText("" + positiveCount);
        negativeCountLabel.setText("" + negativeCount);
    }

    /**
     * play negative sound
     */
    public void playNegativeSound() {
        negativeCountSound.setFramePosition(0);
        negativeCountSound.start();
        //negativeSound.play();
    }

    /**
     * play positive sound
     */
    public void playPositiveSound() {
        positiveCountSound.setFramePosition(0);
        positiveCountSound.start();
        //positiveSound.play();
    }

    /**
     * play undo sound
     */
    public void playUndoSound() {
        undoCountSound.setFramePosition(0);
        undoCountSound.start();
        //undoSound.play();
    }

}
