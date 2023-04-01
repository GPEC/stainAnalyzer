// reference: http://myjavafx.blogspot.ca/2012/01/javafx-calendar-control.html
package eu.schudt.javafx.controls.calendar;

import ca.ubc.gpec.ia.analyzer.views.ViewConstants;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Popup;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;

/**
 * @author Christian Schudt
 */
public class DatePicker extends TextField { // extends HBox

    public static final String RESOURCE_SEPARATOR = "/";
    public static final String CSS_FILENAME = RESOURCE_SEPARATOR
            + "eu" + RESOURCE_SEPARATOR
            + "schudt" + RESOURCE_SEPARATOR
            + "javafx" + RESOURCE_SEPARATOR
            + "controls" + RESOURCE_SEPARATOR
            + "calendar" + RESOURCE_SEPARATOR
            + "calendarstyle.css";
    private static final String CSS_DATE_PICKER_VALID = "datepicker-valid";
    private static final String CSS_DATE_PICKER_INVALID = "datepicker-invalid";
    private Timer timer;
    //private TextField textField; // EXTENDS TextField!!!
    //private StringProperty promptText = new SimpleStringProperty();
    private CalendarView calendarView;
    private BooleanProperty invalid = new SimpleBooleanProperty();
    private ObjectProperty<Locale> locale = new SimpleObjectProperty<>();
    private ObjectProperty<Date> selectedDate = new SimpleObjectProperty<>();
    private ObjectProperty<DateFormat> dateFormat = new SimpleObjectProperty<>();
    private Popup popup;
    private boolean textSetProgrammatically;
    private EventHandler whatToDoWhenPopupCloses;

    /**
     * Initializes the date picker with the default locale.
     */
    public DatePicker() {
        this(Locale.getDefault());
    }

    /**
     * Initializes the date picker with the given locale.
     *
     * @param locale The locale.
     */
    public DatePicker(Locale locale) {
        super(); // textField = new TextField();
        calendarView = new CalendarView(locale);

        this.locale.set(locale);

        calendarView.setEffect(new DropShadow());

        // Use the same locale.
        calendarView.localeProperty().bind(localeProperty());

        // Bind the current date of the calendar view with the selected date, so that the calendar shows up with the same month as in the text field.
        calendarView.currentDateProperty().bind(selectedDateProperty());

        // When the user selects a date in the calendar view, hide it.
        calendarView.selectedDateProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                selectedDate.set(calendarView.selectedDateProperty().get());
                hidePopup();
            }
        });

        calendarView.todayButtonTextProperty().set("Today"); // default text label for "Today" button at bottom of datepicker
        calendarView.setShowWeeks(false); // default to NOT show weeks

        // Change the CSS styles, when this control becomes invalid.
        invalid.addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                if (invalid.get()) {
                    getStyleClass().add(CSS_DATE_PICKER_INVALID);
                    getStyleClass().remove(CSS_DATE_PICKER_VALID);
                } else {
                    getStyleClass().remove(CSS_DATE_PICKER_INVALID);
                    getStyleClass().add(CSS_DATE_PICKER_VALID);
                }
            }
        });

        // When the text field no longer has the focus, try to parse the date.
        addEventHandler(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (!focusedProperty().get()) {
                    if (!getText().equals("")) {
                        tryParse(true);
                    }
                } else {
                    showPopup();
                }
            }
        });

        // Listen to user input.
        // Sam Leung: disable user manual input date!!! ... force user to use the date picker
        textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String s1) {
                // Only evaluate the input, it it wasn't set programmatically.
                if (textSetProgrammatically) {
                    return;
                } else {
                    // if user tries to type manually, set date to null and clear textField!!!
                    selectedDate.set(null);
                    updateTextField();
                }
            }
        });

        selectedDateProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                updateTextField();
                invalid.set(false);
            }
        });

        localeProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                updateTextField();
            }
        });

        addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                showPopup(); // force user to use the date picker instead of typing manually
            }
        });

        //getChildren().add(textField); // NO LONGER EXTENDS HBOX

        // initialize handler for any things to do when popup closes
        // initially set to null i.e. do nothing
        whatToDoWhenPopupCloses = null;
    }

    private void hidePopup() {
        if (popup != null) {
            popup.hide();
        }
    }

    /**
     * Tries to parse the text field for a valid date.
     *
     * @param setDateToNullOnException True, if the date should be set to null,
     * when a {@link ParseException} occurs. This is the case, when the text
     * field loses focus.
     */
    private void tryParse(boolean setDateToNullOnException) {
        if (timer != null) {
            timer.cancel();
        }
        try {
            // Double parse the date here, since e.g. 01.01.1 is parsed as year 1, and then formatted as 01.01.01 and then parsed as year 2001.
            // This might lead to an undesired date.
            DateFormat dateFormat = getActualDateFormat();
            Date parsedDate = dateFormat.parse(getText());
            parsedDate = dateFormat.parse(dateFormat.format(parsedDate));
            if (selectedDate.get() == null || selectedDate.get() != null && parsedDate.getTime() != selectedDate.get().getTime()) {
                selectedDate.set(parsedDate);
            }
            invalid.set(false);
            updateTextField();
        } catch (ParseException e) {
            invalid.set(true);
            if (setDateToNullOnException) {
                selectedDate.set(null);
            }
        }

    }

    /**
     * Updates the text field.
     */
    private void updateTextField() {
        // Mark the we update the text field (and not the user), so that it can be ignored, by textField.textProperty()
        textSetProgrammatically = true;
        if (selectedDateProperty().get() != null) {
            String date = getActualDateFormat().format(selectedDateProperty().get());
            if (!getText().equals(date)) {
                setText(date);
            }
        } else {
            setText("");
        }
        textSetProgrammatically = false;
    }

    /**
     * Gets the actual date format. If {@link #dateFormatProperty()} is set,
     * take it, otherwise get a default format for the current locale.
     *
     * @return The date format.
     */
    private DateFormat getActualDateFormat() {
        if (dateFormat.get() != null) {
            return dateFormat.get();
        }

        //DateFormat format = DateFormat.getDateInstance(DateFormat.MEDIUM, locale.get());
        DateFormat format = ViewConstants.SIMPLE_DATE_FORMAT;
        format.setCalendar(calendarView.getCalendar());
        format.setLenient(false);

        return format;
    }

    /**
     * Use this to set further properties of the calendar.
     *
     * @return The calendar view.
     */
    public CalendarView getCalendarView() {
        return calendarView;
    }

    /**
     * States whether the user input is invalid (is no valid date).
     *
     * @return The property.
     */
    public ReadOnlyBooleanProperty invalidProperty() {
        return invalid;
    }

    /**
     * The locale.
     *
     * @return The property.
     */
    public final ObjectProperty<Locale> localeProperty() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale.set(locale);
    }

    public Locale getLocale() {
        return locale.get();
    }

    /**
     * The selected date.
     *
     * @return The property.
     */
    public final ObjectProperty<Date> selectedDateProperty() {
        return selectedDate;
    }

    public void setSelectedDate(Date date) {
        this.selectedDate.set(date);
    }

    public Date getSelectedDate() {
        return selectedDate.get();
    }

    /**
     * Gets the date format.
     *
     * @return The date format.
     */
    public ObjectProperty<DateFormat> dateFormatProperty() {
        return dateFormat;
    }

    public void setDateFormat(DateFormat dateFormat) {
        this.dateFormat.set(dateFormat);
    }

    public DateFormat getDateFormat() {
        return dateFormat.get();
    }

    /**
     * set event handler to do things when popup closes
     */
    public void setWhatToDoWhenPopupCloses(EventHandler eh) {
        whatToDoWhenPopupCloses = eh;
    }

    /**
     * Shows the pop up.
     */
    private void showPopup() {

        if (popup == null) {
            popup = new Popup();
            popup.setAutoHide(true);
            popup.setHideOnEscape(true);
            popup.setAutoFix(true);
            popup.getContent().add(calendarView);
            if (whatToDoWhenPopupCloses != null) {
                popup.setOnHidden(whatToDoWhenPopupCloses);
            }
            calendarView.getStyleClass().add(ViewConstants.DEFAULT_CSS_THEME_NAME);
        }

        Bounds calendarBounds = calendarView.getBoundsInLocal();
        Bounds bounds = localToScene(getBoundsInLocal());

        double posX = calendarBounds.getMinX() + bounds.getMinX() + getScene().getX() + getScene().getWindow().getX();
        double posY = calendarBounds.getMinY() + bounds.getHeight() + bounds.getMinY() + getScene().getY() + getScene().getWindow().getY();

        popup.show(this, posX, posY);
    }
}
