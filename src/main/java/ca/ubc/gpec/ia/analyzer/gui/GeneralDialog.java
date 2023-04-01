/**
 * GeneralDialog.java
 *
 * Created on August 17, 2005 by Andy Chan Adjusted for Java 6.0 in February
 * 2010 by Dmitry Turbin
 *
 * Extends the JDialog to allow easy usage.
 *
 * Acknowledgments: Some source code of this class comes from the
 * SpringUtilities class of the Sun Java tutorial "How to Use SpringLayout".
 * Please refer to
 * http://java.sun.com/docs/books/tutorial/uiswing/layout/example-1dot4/SpringUtilities.java
 */
package ca.ubc.gpec.ia.analyzer.gui;

// ImageJ libraries
import ij.gui.*;

// Import Java libraries
import java.util.Hashtable;

// Libraries for dialog
import javax.swing.border.Border;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;

public class GeneralDialog extends JDialog implements ActionListener,
        ChangeListener {
    // Attributes

    private int numRows = 0; // Number of rows of components
    private JButton okButton, cancelButton, helpButton; // Buttons to be
    // displayed in the
    // dialog
    private JPanel panel; // The JPanel object that holds the content of the
    // dialog
    private JTabbedPane tabbedPane; // The JTabbedPane object allowing tab
    // contents
    private JPanel buttonsPanel; // The JPanel object that holds the dialog
    // buttons
    private String okButtonLabel = "OK";
    private String cancelButtonLabel = "Cancel";
    private Hashtable comboBoxes = new Hashtable(); // A hashtable that holds
    // all comboboxes
    private Hashtable textFields = new Hashtable(); // A hashtable that holds
    // all textfields
    private Hashtable checkBoxes = new Hashtable(); // A hashtable that holds
    // all checkboxes
    private Hashtable buttons = new Hashtable(); // A hashtable that holds all
    // buttons
    private Hashtable tables = new Hashtable(); // A hashtable that holds all
    // Jtables
    private Hashtable labels = new Hashtable(); // A hashtable that holds all
    // labels
    private Hashtable sliders = new Hashtable(); // A hashtable that holds all
    // sliders
    private Hashtable radioButtons = new Hashtable(); // A hashtable that holds
    // all radio buttons
    private Hashtable buttonGroups = new Hashtable(); // A hashtable that holds
    // all button groups
    private boolean canceled = false; // Whether the "Cancel" button was pressed
    private Object helpMsg; // An object that contains the help message

    /**
     * Constructor allowing specification of the dialog title
     *
     * @param title The title of the dialog
     */
    public GeneralDialog(String title) {
        super(new JFrame(), title, true);
        panel = new JPanel(new BorderLayout());
    }

    /**
     * Constructor allowing specification of the dialog title and
     * modal/non-modal
     *
     * @param title The title of the dialog
     * @param modal Whether the dialog is modal or not
     */
    public GeneralDialog(String title, boolean modal) {
        super(new JFrame(), title, modal);
        panel = new JPanel(new BorderLayout());
    }

    /**
     * Shows the dialog to the user
     */
    public void showDialog() {
        /**
         * // Set look and feel to system look try {
         * UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
         * } catch(Exception e) { //System.out.println("Error setting native
         * look and feel: " + e); }
         *
         */
        // // Layout the tab panels
        if (tabbedPane != null) {
            panel.add(tabbedPane, BorderLayout.CENTER);
        }

        // Create the buttons in a panel
        if (buttonsPanel == null) {
            buttonsPanel = createButtonsPanel();
        }
        panel.add(buttonsPanel, BorderLayout.SOUTH);

        // Set properties of the dialog
        setResizable(false);
        setDefaultLookAndFeelDecorated(true);
        setDefaultCloseOperation(GeneralDialog.DISPOSE_ON_CLOSE);
        setContentPane(panel);
        pack();
        GUI.center(this);

        // Display the dialog
        setVisible(true);
    }

    /**
     * Allow user to add a tab to the dialog
     *
     * @param title The title of the tab
     * @param panel The JPanel object to be contained in this tab
     */
    public void addTab(String title, Component panel) {
        if (tabbedPane == null) {
            tabbedPane = new JTabbedPane();
        }

        if (panel.getClass().getName().equals("javax.swing.JPanel")) {
            // if (panel.getClass().getName() == "javax.swing.JPanel") {
            JPanel jp = (JPanel) panel;
            Border padding = BorderFactory.createEmptyBorder(5, 5, 5, 5);
            jp.setBorder(padding);
            tabbedPane.addTab(title, jp);
        } else {
            tabbedPane.addTab(title, panel);
        }
    }

    /**
     * Allow user to add a button to the given JPanel
     *
     * @param panel The JPanel where the button is to be added
     * @param name The name of the button to be reference in code
     * @param label The label of the button
     * @param addNumRows Whether to treat the button as a new row of components
     * of the JPanel
     */
    public void addButton(JPanel panel, String name, String label,
            boolean addNumRows) {
        addButton(panel, name, label);
        if (addNumRows) {
            numRows++;
        }
    }

    /**
     * Allow user to add a button to the given JPanel
     *
     * @param panel The JPanel where the button is to be added
     * @param name The name of the button to be reference in code
     * @param label The label of the button
     */
    public void addButton(JPanel panel, String name, String label) {
        JButton button = new JButton(label);
        button.addActionListener(this);
        panel.add(button);
        buttons.put(name, button);
    }

    /**
     * Allow user to add a button to the given JPanel
     *
     * @param panel The JPanel where the button is to be added
     * @param name The name of the button to be reference in code
     * @param label The label of the button
     * @param addNumRows Whether to treat the button as a new row of components
     * of the JPanel
     */
    public void addColourButton(JPanel panel, String name, Color c,
            boolean addNumRows) {
        addColourButton(panel, name, c);
        if (addNumRows) {
            numRows++;
        }
    }

    /**
     * Allow user to add a button to the given JPanel
     *
     * @param panel The JPanel where the button is to be added
     * @param name The name of the button to be reference in code
     * @param label The label of the button
     */
    public void addColourButton(JPanel panel, String name, Color c) {
        JButton button = new JButton();
        button.setForeground(c);
        button.setBackground(c);
        button.setOpaque(true);
        button.addActionListener(this);
        panel.add(button);
        buttons.put(name, button);
    }

    /**
     * Allow user to add a checkbox to the given JPanel
     *
     * @param panel The JPanel where the checkbox is to be added
     * @param name The name of the checkbox to be reference in code
     * @param label The label of the checkbox
     * @param checked Whether the checkbox is checked or not
     * @param addNumRows Whether to treat the checkbox as a new row of
     * components of the JPanel
     */
    public void addCheckBox(JPanel panel, String name, String label,
            boolean checked, boolean addNumRows) {
        addCheckBox(panel, name, label, checked);
        if (addNumRows) {
            numRows++;
        }
    }

    /**
     * Allow user to add a checkbox to the given JPanel
     *
     * @param panel The JPanel where the checkbox is to be added
     * @param name The name of the checkbox to be reference in code
     * @param label The label of the checkbox
     * @param checked Whether the checkbox is checked or not
     */
    public void addCheckBox(JPanel panel, String name, String label,
            boolean checked) {
        JCheckBox checkBox = new JCheckBox(label, checked);
        panel.add(checkBox);
        checkBoxes.put(name, checkBox);
    }

    /**
     * Allow user to add a checkbox to the given JPanel
     *
     * @param panel The JPanel where the combobox is to be added
     * @param name The name of the combobox to be reference in code
     * @param label The label of the combobox
     * @param values An array of values to be added to the checkbox
     * @param selectedItem The item to be selected by default
     */
    public JComboBox addComboBox(JPanel panel, String name, String label,
            String[] values, Object selectedItem) {
        addComboBox(panel, name, label, values);
        JComboBox combo = (JComboBox) comboBoxes.get(name);
        combo.setSelectedItem(selectedItem);
        return combo;
    }

    /**
     * Allow user to add a checkbox to the given JPanel
     *
     * @param panel The JPanel where the combobox is to be added
     * @param name The name of the combobox to be reference in code
     * @param label The label of the combobox
     * @param values An array of values to be added to the checkbox
     */
    public JComboBox addComboBox(JPanel panel, String name, String label,
            String[] values) {
        // Create label for the combo box
        JLabel l = new JLabel(label, JLabel.TRAILING);
        panel.add(l);

        JComboBox combo = new JComboBox();
        // Populate the combobox list
        for (int i = 0; i < values.length; i++) {
            combo.addItem(values[i]);
        }
        l.setLabelFor(combo);
        panel.add(combo);
        comboBoxes.put(name, combo);
        numRows++;
        return combo;
    }

    /**
     * Allow user to add a label to the given JPanel
     *
     * @param panel The JPanel where the label is to be added
     * @param name The name of the label to be reference in code
     * @param text The text to be added to the label
     * @param horizontalAlignment The horizontal alignment of the label
     * @param fg The foreground colour of the label
     * @param addNumRows Whether to treat the label as a new row of components
     * of the JPanel
     */
    public void addLabel(JPanel panel, String name, String text,
            int horizontalAlignment, boolean addNumRows) {
        JLabel l = new JLabel(text, horizontalAlignment);
        panel.add(l);
        labels.put(name, l);

        if (addNumRows) {
            numRows++;
        }
    }

    /**
     * Allow user to add a label to the given JPanel
     *
     * @param panel The JPanel where the label is to be added
     * @param name The name of the label to be reference in code
     * @param fg The foreground colour of the label
     * @param addNumRows Whether to treat the label as a new row of components
     * of the JPanel
     */
    public void addColourLabel(JPanel panel, String name, Color c, int size,
            boolean addNumRows) {
        String s = "";
        for (int i = 0; i < size; i++) {
            s = s + " ";
        }

        JLabel l = new JLabel(s);
        l.setBackground(c);
        l.setOpaque(true);
        panel.add(l);
        labels.put(name, l);

        if (addNumRows) {
            numRows++;
        }
    }

    /**
     * Allow user to add a textfield to the given JPanel
     *
     * @param panel The JPanel where the textfield is to be added
     * @param name The name of the textfield to be reference in code
     * @param label The label of the textfield
     * @param text The text to be added to the textfield
     * @param columns Specify the number of columns (width) of the textfield
     * @param addNumRows Whether to treat the checkbox as a new row of
     * components of the JPanel
     */
    public void addTextField(JPanel panel, String name, String label,
            String text, int columns, boolean addNumRows) {
        // Create label for the text field
        JLabel l = new JLabel(label, JLabel.TRAILING);
        panel.add(l);

        JTextField textField = new JTextField(text, columns);
        l.setLabelFor(textField);
        panel.add(textField);
        textFields.put(name, textField);

        if (addNumRows) {
            numRows++;
        }
    }

    /**
     * Allow user to add a textfield to the given JPanel
     *
     * @param panel The JPanel where the textfield is to be added
     * @param name The name of the textfield to be reference in code
     * @param text The text to be added to the textfield
     * @param columns Specify the number of columns (width) of the textfield
     * @param addNumRows Whether to treat the checkbox as a new row of
     * components of the JPanel
     */
    public void addTextField(JPanel panel, String name, String text,
            int columns, boolean addNumRows) {
        JTextField textField = new JTextField(text, columns);
        panel.add(textField);
        textFields.put(name, textField);

        if (addNumRows) {
            numRows++;
        }
    }

    /**
     * Allow user to add a textfield to the given JPanel
     *
     * @param panel The JPanel where the textfield is to be added
     * @param name The name of the textfield to be reference in code
     * @param label The label of the textfield
     * @param text The text to be added to the textfield
     * @param columns Specify the number of columns (width) of the textfield
     */
    public void addTextField(JPanel panel, String name, String label,
            String text, int columns) {
        // Create label for the text field
        JLabel l = new JLabel(label, JLabel.TRAILING);
        panel.add(l);

        JTextField textField = new JTextField(text, columns);
        l.setLabelFor(textField);
        panel.add(textField);
        textFields.put(name, textField);
        numRows++;
    }

    /**
     * Adds a GeneralTable object to the given panel along with a JTableHeader
     *
     * @param panel The JPanel where the textfield is to be added
     * @param name The name of the table to be reference in code
     * @param table The GeneralTable object to be added
     */
    public void addTableWithHeader(JPanel panel, String name, GeneralTable table) {
        panel.add(table.getTableHeader());
        addTable(panel, name, table);
    }

    /**
     * Adds a GeneralTable object to the given panel without header
     *
     * @param panel The JPanel where the textfield is to be added
     * @param name The name of the table to be reference in code
     * @param table The GeneralTable object to be added
     */
    public void addTable(JPanel panel, String name, GeneralTable table) {
        panel.add(table);
        tables.put(name, table);
    }

    /**
     * Allow user to add a slider to the given JPanel
     *
     * @param panel The JPanel where the slider is to be added
     * @param name The name of the slider to be reference in code
     * @param label The label of the slider
     * @param addNumRows Whether to treat the slider as a new row of components
     * of the JPanel
     */
    public void addSlider(JPanel panel, String name, int orientation, int min,
            int max, int value, boolean addNumRows) {
        addSlider(panel, name, orientation, min, max, value);
        if (addNumRows) {
            numRows++;
        }
    }

    /**
     * Allow user to add a slider to the given JPanel
     *
     * @param panel The JPanel where the slider is to be added
     * @param name The name of the slider to be reference in code
     * @param label The label of the slider
     */
    public void addSlider(JPanel panel, String name, int orientation, int min,
            int max, int value) {
        JSlider s = new JSlider(orientation, min, max, value);
        s.addChangeListener(this);
        panel.add(s);
        sliders.put(name, s);
    }

    /**
     * Allow user to add a radiobutton to the given JPanel
     *
     * @param panel The JPanel where the radiobutton is to be added
     * @param name The name of the radiobutton to be reference in code
     * @param label The label of the radiobutton
     * @param addNumRows Whether to treat the radiobutton as a new row of
     * components of the JPanel
     */
    public void addRadioButton(JPanel panel, String name, String label,
            String group, boolean selected, boolean addNumRows) {
        addRadioButton(panel, name, label, group, selected);
        if (addNumRows) {
            numRows++;
        }
    }

    /**
     * Allow user to add a radiobutton to the given JPanel
     *
     * @param panel The JPanel where the radiobutton is to be added
     * @param name The name of the radiobutton to be reference in code
     * @param label The label of the radiobutton
     */
    public void addRadioButton(JPanel panel, String name, String label,
            String group, boolean selected) {
        JRadioButton rb = new JRadioButton(label);
        rb.setSelected(selected);
        rb.addActionListener(this);
        panel.add(rb);
        radioButtons.put(name, rb);

        ButtonGroup bg;
        if (buttonGroups.containsKey(group)) { // Get existing button group
            bg = (ButtonGroup) buttonGroups.get(group);
        } else { // Create button group if not already exist
            bg = new ButtonGroup();
            buttonGroups.put(group, bg);
        }
        // Add button to button group
        bg.add(rb);
    }

    /**
     * Adds a help message object to the dialog
     *
     * @param helpMsg The help message object to be added. It can be any
     * JComponent.
     */
    public void addHelpMsg(Object helpMsg) {
        this.helpMsg = helpMsg;
    }

    /**
     * Reset the number of rows of components
     */
    public void resetNumRows() {
        numRows = 0;
    }

    /**
     * Get the number of rows of components
     */
    public int getNumRows() {
        return numRows;
    }

    /**
     * Whether the Cancel button was pressed
     */
    public boolean wasCanceled() {
        return canceled;
    }

    /**
     * Obtain the item selected in the given combobox
     *
     * @param name The name of the combobox
     * @return A string that represents the value of the selected item
     */
    public String getComboBoxValue(String name) {
        String value = null;
        if (comboBoxes.containsKey(name)) {
            JComboBox cb = (JComboBox) comboBoxes.get(name);
            value = cb.getSelectedItem().toString();
        }

        return value;
    }

    /**
     * Obtain the value of a given checkbox
     *
     * @param name The name of the checkbox
     * @return A boolean indicating whether the checkbox was checked or not
     */
    public boolean getCheckBoxValue(String name) {
        boolean value = false;
        if (checkBoxes.containsKey(name)) {
            JCheckBox cb = (JCheckBox) checkBoxes.get(name);
            value = cb.isSelected();
        }

        return value;
    }

    /**
     * Obtain the text of the given text field
     *
     * @param name The name of the textfield
     * @return A string that represents the text of the textfield
     */
    public String getTextFieldValue(String name) {
        String value = null;
        if (textFields.containsKey(name)) {
            JTextField tf = (JTextField) textFields.get(name);
            value = tf.getText();
        }

        return value;
    }

    /**
     * Obtain the value of the JComponent object in the cell of the given table
     *
     * @param name The name of the table
     * @param row The row number of the cell
     * @param col The column number of the cell
     * @return An object that represents the value of the JComponent
     */
    public Object getTableValue(String name, int row, int col) {
        Object value = null;
        if (tables.containsKey(name)) {
            GeneralTable table = (GeneralTable) tables.get(name);
            value = table.getValue(row, col);
        }

        return value;
    }

    /**
     * Creates a JPanel that contains the buttons of the dialog
     *
     * @return A JPanel object that contains the buttons.
     */
    private JPanel createButtonsPanel() {
        JPanel panel1 = new JPanel(new FlowLayout());

        // OK button
        okButton = new JButton(okButtonLabel);
        okButton.addActionListener(this);
        panel1.add(okButton);

        // Cancel button
        cancelButton = new JButton(cancelButtonLabel);
        cancelButton.addActionListener(this);
        panel1.add(cancelButton);

        if (helpMsg != null) {
            helpButton = new JButton("Help");
            helpButton.addActionListener(this);
            panel1.add(helpButton);
        }

        return panel1;
    }

    /**
     * Event listener to decide what action to perform after a button is
     * clicked.
     *
     * @param An ActionEvent object.
     */
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source == cancelButton) {
            canceled = true;
        }
        if (source == cancelButton || source == okButton) {
            closeDialog();
        } else if (source == helpButton) {
            JOptionPane.showMessageDialog(this, helpMsg,
                    "Haralick Texture Help", JOptionPane.PLAIN_MESSAGE);
        }
    }

    /**
     * Listen to the slider.
     */
    public void stateChanged(ChangeEvent e) {
    }

    // The following section of code comes directly from the SpringUtilities
    // class of the Sun Java tutorial
    // "How to Use SpringLayout".
    /**
     * Closes the dialog.
     */
    protected void closeDialog() {
        setVisible(false);
    }

    /* Used by makeCompactGrid. */
    private static SpringLayout.Constraints getConstraintsForCell(int row,
            int col, Container parent, int cols) {
        SpringLayout layout = (SpringLayout) parent.getLayout();
        Component c = parent.getComponent(row * cols + col);
        return layout.getConstraints(c);
    }

    public static void makeCompactGrid(Container parent, int rows, int cols,
            int initialX, int initialY, int xPad, int yPad) {
        SpringLayout layout;
        try {
            layout = (SpringLayout) parent.getLayout();
        } catch (ClassCastException exc) {
            System.err
                    .println("The first argument to makeCompactGrid must use SpringLayout.");
            return;
        }

        // Align all cells in each column and make them the same width.
        Spring x = Spring.constant(initialX);
        for (int c = 0; c < cols; c++) {
            Spring width = Spring.constant(0);
            for (int r = 0; r < rows; r++) {
                width = Spring.max(width,
                        getConstraintsForCell(r, c, parent, cols).getWidth());
            }
            for (int r = 0; r < rows; r++) {
                SpringLayout.Constraints constraints = getConstraintsForCell(r,
                        c, parent, cols);
                constraints.setX(x);
                constraints.setWidth(width);
            }
            x = Spring.sum(x, Spring.sum(width, Spring.constant(xPad)));
        }

        // Align all cells in each row and make them the same height.
        Spring y = Spring.constant(initialY);
        for (int r = 0; r < rows; r++) {
            Spring height = Spring.constant(0);
            for (int c = 0; c < cols; c++) {
                height = Spring.max(height,
                        getConstraintsForCell(r, c, parent, cols).getHeight());
            }
            for (int c = 0; c < cols; c++) {
                SpringLayout.Constraints constraints = getConstraintsForCell(r,
                        c, parent, cols);
                constraints.setY(y);
                constraints.setHeight(height);
            }
            y = Spring.sum(y, Spring.sum(height, Spring.constant(yPad)));
        }

        // Set the parent's size.
        SpringLayout.Constraints pCons = layout.getConstraints(parent);
        pCons.setConstraint(SpringLayout.SOUTH, y);
        pCons.setConstraint(SpringLayout.EAST, x);
    }

    // Accessor methods
    protected JButton okButton() {
        return okButton;
    }

    protected JButton helpButton() {
        return helpButton;
    }

    protected JButton cancelButton() {
        return cancelButton;
    }

    protected boolean canceled() {
        return canceled;
    }

    protected void canceled(boolean c) {
        canceled = c;
    }

    protected Object helpMsg() {
        return helpMsg;
    }

    protected Hashtable buttons() {
        return buttons;
    }

    protected Hashtable textFields() {
        return textFields;
    }

    public void addTextField(String name, JTextField tf) {
        textFields.put(name, tf);
    }

    protected Hashtable sliders() {
        return sliders;
    }

    protected Hashtable labels() {
        return labels;
    }

    protected Hashtable radioButtons() {
        return radioButtons;
    }

    public void setTextField(String name, String text) {
        if (textFields.containsKey(name)) {
            JTextField t = (JTextField) textFields.get(name);
            t.setText(text);
        }
    }

    public void setColourLabel(String name, Color c) {
        if (labels.containsKey(name)) {
            JLabel l = (JLabel) labels.get(name);
            l.setBackground(c);
        }
    }

    public void setLabel(String name, String text) {
        if (labels.containsKey(name)) {
            JLabel l = (JLabel) labels.get(name);
            l.setText(text);
        }
    }

    public void setButtonColour(String name, Color c) {
        if (buttons.containsKey(name)) {
            JButton b = (JButton) buttons.get(name);
            b.setForeground(c);
            b.setBackground(c);
            b.setOpaque(true);
        }
    }

    public void setSliderProperties(String name, int majorTickSpacing,
            int minorTickSpacing, boolean setPaintTicks, boolean setPaintLabels) {
        if (sliders.containsKey(name)) {
            JSlider s = (JSlider) sliders.get(name);
            s.setMajorTickSpacing(majorTickSpacing);
            s.setMinorTickSpacing(minorTickSpacing);
            s.setPaintTicks(setPaintTicks);
            s.setPaintLabels(setPaintLabels);
        }
    }

    public void setOKButtonLabel(String label) {
        okButtonLabel = label;
    }

    public void setCancelButtonLabel(String label) {
        cancelButtonLabel = label;
    }

    public void setButtonsPanel(JPanel bp) {
        buttonsPanel = bp;
    }
}
