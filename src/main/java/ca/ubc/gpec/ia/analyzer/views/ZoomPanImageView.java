/*
 * Zoomable, pannable image view
 */
package ca.ubc.gpec.ia.analyzer.views;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.stage.Stage;

/**
 *
 * @author samuelc
 */
public class ZoomPanImageView extends ImageView {

    public static final int ARROW_MOVE_PIXEL_INCREMENT = 20;
    public static final int KEY_ZOOM_INCREMENT = 10;
    private double prevMouseX; // capture x coorindate of previous mouse position
    private double prevMouseY; // capture y coordinate of previous mouse position
    private int imageWidth; // image width
    private int imageHeight; // image height
    private ContextMenu menu; // menu
    final protected Menu helpMenu; // help menu item
    final protected Stage stage; // the stage that is showing this node

    public ZoomPanImageView(Image image, Stage stage) {
        super(image);

        imageWidth = (int) image.getWidth();
        imageHeight = (int) image.getHeight();
        this.setPreserveRatio(true); // always preserve ratio
        this.stage = stage;

        prevMouseX = 0; // initialize because pann with arrow key needs this ... just in case mouse press did not occur first
        prevMouseY = 0; // initialize because pann with arrow key needs this ... just in case mouse press did not occur first

        setViewport(new Rectangle2D(0, 0, imageWidth, imageHeight));
        //System.out.println("setting up initial viewport ... w/h:"+imageWidth+"/"+imageHeight+" stage w/h:"+stage.getWidth()+"/"+stage.getHeight());
        this.setFitWidth(ViewConstants.SCORING_PANE_WIDTH - ViewConstants.COUNTER_CARTOON_WIDTH); // fit prefer width over height

        stage.heightProperty().addListener(new ZoomPanImageViewChangeHeightListener(this));
        stage.widthProperty().addListener(new ZoomPanImageViewChangeWidthListener(this));

        // record x/y coord at mouse press - needed for mouse drag panning
        this.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e) {
                ZoomPanImageView imageView = ((ZoomPanImageView) e.getSource());
                imageView.requestFocus(); // get the focus
                prevMouseX = (int) e.getX();
                prevMouseY = (int) e.getY();
                // if menu is show, hide it so that it will not interfer with panning/zooming
                if (menu.isShowing()) {
                    menu.hide();
                }
            }
        });

        // zooming
        this.setOnScroll(new EventHandler<ScrollEvent>() {
            @Override
            public void handle(ScrollEvent e) {
                ZoomPanImageView imageView = ((ZoomPanImageView) e.getSource());
                imageView.zoom(e.getDeltaY(), imageView.getViewport());
            }
        });

        // panning with mouse
        this.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e) {
                ZoomPanImageView imageView = ((ZoomPanImageView) e.getSource());
                imageView.move(e.getX(), e.getY());
            }
        });

        // panning with arrow keys
        // zoom in out with +/- keys
        // NOTE: setOnKeyTyped() will NOT work with arrow keys
        //       use setOnKeyPressed instead!!!!
        this.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent ke) {
                ZoomPanImageView imageView = ((ZoomPanImageView) ke.getSource());
                switch (ke.getCode()) {
                    case LEFT:
                        imageView.move(prevMouseX + ARROW_MOVE_PIXEL_INCREMENT, prevMouseY);
                        break;
                    case RIGHT:
                        imageView.move(prevMouseX - ARROW_MOVE_PIXEL_INCREMENT, prevMouseY);
                        break;
                    case UP:
                        imageView.move(prevMouseX, prevMouseY + ARROW_MOVE_PIXEL_INCREMENT);
                        break;
                    case DOWN:
                        imageView.move(prevMouseX, prevMouseY - ARROW_MOVE_PIXEL_INCREMENT);
                        break;
                    case PLUS:
                    case ADD:
                    case EQUALS:
                        imageView.zoom(KEY_ZOOM_INCREMENT, imageView.getViewport());
                        break;
                    case MINUS:
                    case SUBTRACT:
                        imageView.zoom(-1 * KEY_ZOOM_INCREMENT, imageView.getViewport());
                        break;
                }
                ke.consume(); // consume this event so that nobody will do anything with it anymore!!
            }
        });

        // display menu
        menu = new ContextMenu();
        menu.setAutoHide(true); // auto-hide
        menu.getStyleClass().add(ViewConstants.DEFAULT_CSS_THEME_NAME);
        helpMenu = new Menu("Help");
        helpMenu.getStyleClass().add(ViewConstants.DEFAULT_CSS_THEME_NAME);
        helpMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
            }
        });
        menu.getItems().add(helpMenu);
        this.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e) {
                if (e.getButton() == MouseButton.SECONDARY) {
                    menu.show((ZoomPanImageView) e.getSource(), e.getScreenX(), e.getScreenY());
                }
            }
        });

        // setup help menu ...
        setupHelpMenu();

        // this following is needed, otherwise, keyevent will NOT WORK!!!
        this.setFocusTraversable(true);
    }

    /**
     * set up help menu
     */
    private void setupHelpMenu() {
        final String title = "How to zoom & pan ...";
        MenuItem navHelp = new MenuItem(title);
        navHelp.getStyleClass().add(ViewConstants.DEFAULT_CSS_THEME_NAME);
        navHelp.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                MyDialog.showNoticeDialog(
                        title,
                        "zoom-in: mouse scroll up or press the \"+\" key\n"
                        + "zoom-out: mouse scoll down or press the \"-\" key\n"
                        + "navigate (pan): mouse drag or press the arrow keys",
                        stage);
            }
        });
        helpMenu.getItems()
                .add(navHelp);
    }

    /**
     * refit image by width; reset viewport to try to view whole image; refit
     * image by width
     *
     * @param width
     */
    public void reFitImageByWidth(double width) {
        setViewport(new Rectangle2D(0, 0, imageWidth, imageHeight));
        this.setFitWidth(width);
    }

    /**
     * get actual image width
     *
     * @return
     */
    public int getImageWidth() {
        return imageWidth;
    }

    /**
     * get actual image height
     *
     * @return
     */
    public int getImageHeight() {
        return imageHeight;
    }

    /**
     * move in x/y increment
     *
     * @param x
     * @param y
     */
    public void move(double x, double y) {
        Rectangle2D prevViewport = getViewport();

        double nextX = Math.max(
                prevViewport.getMinX() < 0 ? prevViewport.getMinX() : 0,
                prevViewport.getMinX() + (prevViewport.getWidth() / stage.getWidth()) * (prevMouseX - x));
        if (nextX > 0 && (nextX + prevViewport.getWidth()) > imageWidth) {
            nextX = Math.min(nextX, prevViewport.getMinX()); // prevent image from going away from viewport
        }

        double nextY = Math.max(
                prevViewport.getMinY() < 0 ? prevViewport.getMinY() : 0,
                prevViewport.getMinY() + (prevViewport.getHeight() / stage.getHeight()) * (prevMouseY - y));
        if (nextY > 0 && (nextY + prevViewport.getHeight()) > imageHeight) {
            nextY = Math.min(nextY, prevViewport.getMinY()); // prevent image from going away from viewport
        }

        setViewport(new Rectangle2D(
                nextX,
                nextY,
                prevViewport.getWidth(),
                prevViewport.getHeight()));
        prevMouseX = x;
        prevMouseY = y;
    }

    /**
     * zoom in/out based on value of delta
     *
     * @param delta
     */
    public void zoom(double delta, Rectangle2D prevViewport) {
        double nextViewportWidth = prevViewport.getWidth();
        double nextViewportX = prevViewport.getMinX();

        double nextViewportHeight = prevViewport.getHeight();
        double nextViewportY = prevViewport.getMinY();

        // need to keep width/height ratio constant ... therefore CANNOT adjust 
        // both x and y with the same absolute value
        double deltaY = delta * prevViewport.getHeight() / prevViewport.getWidth();

        if ((!(nextViewportHeight > imageHeight && delta < 0)) && (!(nextViewportWidth > imageWidth && delta < 0))) {
            nextViewportHeight = nextViewportHeight - 2d * deltaY;
            nextViewportY = nextViewportY + deltaY;

            nextViewportWidth = nextViewportWidth - 2d * delta;
            nextViewportX = nextViewportX + delta;
        }

        setViewport(new Rectangle2D(
                nextViewportWidth > 0 ? nextViewportX : prevViewport.getMinX(),
                nextViewportHeight > 0 ? nextViewportY : prevViewport.getMinY(),
                nextViewportWidth > 0 ? nextViewportWidth : prevViewport.getWidth(),
                nextViewportHeight > 0 ? nextViewportHeight : prevViewport.getHeight()));
    }
}
