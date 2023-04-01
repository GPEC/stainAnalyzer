/*
 * show dialog message at appropriate time/occasion indicated by "threshold"
 */
package ca.ubc.gpec.ia.analyzer.views;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.media.AudioClip;
import javax.sound.sampled.Clip;
import javafx.stage.Stage;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 *
 * @author samuelc
 */
public class ReminderDialogMessage extends ReminderAction {

    public static final String NOTIFICATION_SOUND_FILENAME = "DingLing.wav";
    //private AudioClip reminderSound;
    private Clip reminderSound;

    private String message;
    private Stage stage;

    /**
     * constructor
     *
     * @param threshold
     * @param message
     * @param stage
     */
    public ReminderDialogMessage(int threshold, String message, Stage stage) {
        super(threshold);
        this.message = message;
        //this.reminderSound = new AudioClip(this.getClass().getResource(ViewConstants.VIEWS_ROOT + "sounds" + ViewConstants.RESOURCE_SEPARATOR + NOTIFICATION_SOUND_FILENAME).toString());
        try {
            this.reminderSound = AudioSystem.getClip();
            reminderSound.open(AudioSystem.getAudioInputStream(this.getClass().getResource(ViewConstants.RESOURCE_SEPARATOR + "sounds" + ViewConstants.RESOURCE_SEPARATOR + NOTIFICATION_SOUND_FILENAME)));
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException ex) {
            // do nothing
        }
        this.stage = stage;
    }

    /**
     * do action if count equals threshold
     *
     * @param count
     */
    @Override
    public void doAction(int count) {
        if (count == getThreshold()) { // only do if count equals threshold
            //reminderSound.play();
            reminderSound.setFramePosition(0);
            reminderSound.start();
            MyDialog.showNoticeDialog(message, stage);
        }
    }
}
