package ca.ubc.gpec.ia.analyzer.gui;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.io.Opener;

import javax.swing.JFileChooser;
import javax.swing.JPanel;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import java.awt.GridBagConstraints;
import javax.swing.JComboBox;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JTextField;

import ca.ubc.gpec.ia.analyzer.settings.StainAnalyzerSettings;
import ca.ubc.gpec.ia.analyzer.deconvolution.ColourDeconvolutionConstants;

public class ChannelsPanel extends JPanel {
	private JLabel channel1Label;
	private JLabel channel2Label;
	private JLabel channel3Label;
	private JTextField channel1Modx;
	private JTextField channel1Mody;
	private JTextField channel1Modz;
	private JTextField channel2Modx;
	private JTextField channel2Mody;
	private JTextField channel2Modz;
	private JTextField channel3Modx;
	private JTextField channel3Mody;
	private JTextField channel3Modz;
	private StainAnalyzerSettings settings;
	private JComboBox stainSelectionComboBox;
	private String previousSelectedStain;
	private String selectedStain;
	
	public JTextField getChannel1_modx() {
		return channel1Modx;
	}

	public JTextField getChannel1_mody() {
		return channel1Mody;
	}

	public JTextField getChannel1_modz() {
		return channel1Modz;
	}

	public JTextField getChannel2_modx() {
		return channel2Modx;
	}

	public JTextField getChannel2_mody() {
		return channel2Mody;
	}

	public JTextField getChannel2_modz() {
		return channel2Modz;
	}

	public JTextField getChannel3_modx() {
		return channel3Modx;
	}

	public JTextField getChannel3_mody() {
		return channel3Mody;
	}

	public JTextField getChannel3_modz() {
		return channel3Modz;
	}

	public JComboBox getStainSelectionComboBox() {
		return stainSelectionComboBox;
	}

	/** 
	 * do some init stuff e.g. add action listener, populate stain selection choices
	 */
	private void initialize(StainAnalyzerSettings settings) {
		this.settings = settings;
		
		// available stains
		String [] stains = ColourDeconvolutionConstants.getInstance().AVAILABLE_STAIN_OPTIONS;
		for (String stain : stains) {
			stainSelectionComboBox.addItem(stain);
		}
		
		// get user selection
		selectedStain = (String)settings.getSettingValue(StainAnalyzerSettings.SETTING_KEY_STAIN);
		previousSelectedStain = selectedStain;
		
		stainSelectionComboBox.setSelectedItem(selectedStain);
		
		// add action listener
	    stainSelectionComboBox.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		updateTextFields();
        	}
        });
	    
	    updateTextFields();
	}
	
	private void updateTextFields() {
		
		// get selected stain
		selectedStain = (String)stainSelectionComboBox.getSelectedItem();

		ColourDeconvolutionConstants CDC = ColourDeconvolutionConstants.getInstance();
		
		if (selectedStain.equals(CDC.STAIN_OPTION_FROM_ROI)) {
			// check to see if channel matrix from ROI is available
			// if not, need to ask user to quit and run channel matrix
			// from ROI first
			
			double[][] channelMatrixFromROI = CDC.getChannelMatrixFromROI();
			
			if (channelMatrixFromROI==null) {
				IJ.showMessage("Channel matrix from ROI not available.\nPlease use Settings Editor to generate channel matrix from ROI first");
				selectedStain = previousSelectedStain;
				stainSelectionComboBox.setSelectedItem(previousSelectedStain);
			} else {			
				// update textfields
				channel1Modx.setText(""+channelMatrixFromROI[0][0]);
				channel1Mody.setText(""+channelMatrixFromROI[0][1]);
				channel1Modz.setText(""+channelMatrixFromROI[0][2]);
			
				channel2Modx.setText(""+channelMatrixFromROI[1][0]);
				channel2Mody.setText(""+channelMatrixFromROI[1][1]);
				channel2Modz.setText(""+channelMatrixFromROI[1][2]);
			
				channel3Modx.setText(""+channelMatrixFromROI[2][0]);
				channel3Mody.setText(""+channelMatrixFromROI[2][1]);
				channel3Modz.setText(""+channelMatrixFromROI[2][2]);

				// set values to be user values
				IJ.showMessage("Setting USER VALUES to values from ROI");
				selectedStain = CDC.STAIN_OPTION_USER_VALUES;
				stainSelectionComboBox.setSelectedItem(selectedStain);
			}
		} else {
			// get selected stain info
			double[][] channelMatrix = CDC.getChannelMatrix(selectedStain, settings);
				
			// update textfields
			channel1Modx.setText(""+channelMatrix[0][0]);
			channel1Mody.setText(""+channelMatrix[0][1]);
			channel1Modz.setText(""+channelMatrix[0][2]);
		
			channel2Modx.setText(""+channelMatrix[1][0]);
			channel2Mody.setText(""+channelMatrix[1][1]);
			channel2Modz.setText(""+channelMatrix[1][2]);
		
			channel3Modx.setText(""+channelMatrix[2][0]);
			channel3Mody.setText(""+channelMatrix[2][1]);
			channel3Modz.setText(""+channelMatrix[2][2]);
		}
		
		// get selected stain info
		String[] channelNames = CDC.getChannelNames(selectedStain);
		// update channel labels
		channel1Label.setText(channelNames[0]);
		channel2Label.setText(channelNames[1]);
		channel3Label.setText(channelNames[2]);
		
		// if selected stain is user, enable text field, otherwise disable them
		boolean enableTextFields = false;
		if (selectedStain.equals(CDC.STAIN_OPTION_USER_VALUES)) {
			enableTextFields = true;
		} 
		channel1Modx.setEnabled(enableTextFields);
		channel1Mody.setEnabled(enableTextFields);
		channel1Modz.setEnabled(enableTextFields);
		
		channel2Modx.setEnabled(enableTextFields);
		channel2Mody.setEnabled(enableTextFields);
		channel2Modz.setEnabled(enableTextFields);
		
		channel3Modx.setEnabled(enableTextFields);
		channel3Mody.setEnabled(enableTextFields);
		channel3Modz.setEnabled(enableTextFields);

		
		previousSelectedStain = selectedStain; // update previousSelectedStain
	}
		
	/**
	 * Create the panel. Windows builder generate !!! better not touch it here !!!
	 */
	public ChannelsPanel(StainAnalyzerSettings settings) {		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 22, 0, 23, 277, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 0.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 1.0, 0.0, 1.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		JLabel lblChannelsForStain = new JLabel("Channels for stain separations:");
		GridBagConstraints gbc_lblChannelsForStain = new GridBagConstraints();
		gbc_lblChannelsForStain.insets = new Insets(0, 0, 5, 5);
		gbc_lblChannelsForStain.gridx = 0;
		gbc_lblChannelsForStain.gridy = 0;
		add(lblChannelsForStain, gbc_lblChannelsForStain);
		
		stainSelectionComboBox = new JComboBox();
		GridBagConstraints gbc_comboBox = new GridBagConstraints();
		gbc_comboBox.insets = new Insets(0, 0, 5, 0);
		gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBox.gridx = 2;
		gbc_comboBox.gridy = 0;
		add(stainSelectionComboBox, gbc_comboBox);
		
		JPanel panel_1 = new JPanel();
		GridBagConstraints gbc_panel_1 = new GridBagConstraints();
		gbc_panel_1.gridwidth = 3;
		gbc_panel_1.insets = new Insets(0, 0, 5, 0);
		gbc_panel_1.fill = GridBagConstraints.BOTH;
		gbc_panel_1.gridx = 0;
		gbc_panel_1.gridy = 2;
		add(panel_1, gbc_panel_1);
		GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWidths = new int[]{0, 0};
		gbl_panel_1.rowHeights = new int[]{0, 0};
		gbl_panel_1.columnWeights = new double[]{0.0, Double.MIN_VALUE};
		gbl_panel_1.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		panel_1.setLayout(gbl_panel_1);
		
		JLabel lblCustomValuesFor = new JLabel("Custom values for channel vectors (if \"User values\" is selected):");
		GridBagConstraints gbc_lblCustomValuesFor = new GridBagConstraints();
		gbc_lblCustomValuesFor.gridx = 0;
		gbc_lblCustomValuesFor.gridy = 0;
		panel_1.add(lblCustomValuesFor, gbc_lblCustomValuesFor);
		
		JPanel panel = new JPanel();
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.anchor = GridBagConstraints.NORTH;
		gbc_panel.gridwidth = 3;
		gbc_panel.fill = GridBagConstraints.HORIZONTAL;
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 4;
		add(panel, gbc_panel);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		gbl_panel.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		gbl_panel.columnWeights = new double[]{1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);
		
		JPanel panel_2 = new JPanel();
		GridBagConstraints gbc_panel_2 = new GridBagConstraints();
		gbc_panel_2.gridwidth = 6;
		gbc_panel_2.gridheight = 11;
		gbc_panel_2.insets = new Insets(0, 0, 5, 5);
		gbc_panel_2.fill = GridBagConstraints.BOTH;
		gbc_panel_2.gridx = 0;
		gbc_panel_2.gridy = 0;
		panel.add(panel_2, gbc_panel_2);
		
		channel1Label = new JLabel("Channel 1:");
		GridBagConstraints gbc_lblChannel = new GridBagConstraints();
		gbc_lblChannel.anchor = GridBagConstraints.WEST;
		gbc_lblChannel.insets = new Insets(0, 0, 5, 5);
		gbc_lblChannel.gridx = 6;
		gbc_lblChannel.gridy = 0;
		panel.add(channel1Label, gbc_lblChannel);
		
		JLabel lblModx = new JLabel("MODx[0]=");
		GridBagConstraints gbc_lblModx = new GridBagConstraints();
		gbc_lblModx.insets = new Insets(0, 0, 5, 5);
		gbc_lblModx.anchor = GridBagConstraints.EAST;
		gbc_lblModx.gridx = 8;
		gbc_lblModx.gridy = 0;
		panel.add(lblModx, gbc_lblModx);
		
		channel1Modx = new JTextField();
		GridBagConstraints gbc_textField = new GridBagConstraints();
		gbc_textField.anchor = GridBagConstraints.WEST;
		gbc_textField.insets = new Insets(0, 0, 5, 0);
		gbc_textField.gridx = 9;
		gbc_textField.gridy = 0;
		panel.add(channel1Modx, gbc_textField);
		channel1Modx.setColumns(20);
		
		JLabel lblMody = new JLabel("MODy[0]=");
		GridBagConstraints gbc_lblMody = new GridBagConstraints();
		gbc_lblMody.anchor = GridBagConstraints.EAST;
		gbc_lblMody.insets = new Insets(0, 0, 5, 5);
		gbc_lblMody.gridx = 8;
		gbc_lblMody.gridy = 1;
		panel.add(lblMody, gbc_lblMody);
		
		channel1Mody = new JTextField();
		GridBagConstraints gbc_textField_1 = new GridBagConstraints();
		gbc_textField_1.anchor = GridBagConstraints.WEST;
		gbc_textField_1.insets = new Insets(0, 0, 5, 0);
		gbc_textField_1.gridx = 9;
		gbc_textField_1.gridy = 1;
		panel.add(channel1Mody, gbc_textField_1);
		channel1Mody.setColumns(20);
		
		JLabel lblModz = new JLabel("MODz[0]=");
		GridBagConstraints gbc_lblModz = new GridBagConstraints();
		gbc_lblModz.anchor = GridBagConstraints.EAST;
		gbc_lblModz.insets = new Insets(0, 0, 5, 5);
		gbc_lblModz.gridx = 8;
		gbc_lblModz.gridy = 2;
		panel.add(lblModz, gbc_lblModz);
		
		channel1Modz = new JTextField();
		GridBagConstraints gbc_textField_2 = new GridBagConstraints();
		gbc_textField_2.insets = new Insets(0, 0, 5, 0);
		gbc_textField_2.anchor = GridBagConstraints.WEST;
		gbc_textField_2.gridx = 9;
		gbc_textField_2.gridy = 2;
		panel.add(channel1Modz, gbc_textField_2);
		channel1Modz.setColumns(20);
		
		channel2Label = new JLabel("Channel 2:");
		GridBagConstraints gbc_lblChannel_1 = new GridBagConstraints();
		gbc_lblChannel_1.anchor = GridBagConstraints.WEST;
		gbc_lblChannel_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblChannel_1.gridx = 6;
		gbc_lblChannel_1.gridy = 4;
		panel.add(channel2Label, gbc_lblChannel_1);
		
		JLabel lblModx_1 = new JLabel("MODx[1]=");
		GridBagConstraints gbc_lblModx_1 = new GridBagConstraints();
		gbc_lblModx_1.anchor = GridBagConstraints.EAST;
		gbc_lblModx_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblModx_1.gridx = 8;
		gbc_lblModx_1.gridy = 4;
		panel.add(lblModx_1, gbc_lblModx_1);
		
		channel2Modx = new JTextField();
		GridBagConstraints gbc_textField_3 = new GridBagConstraints();
		gbc_textField_3.anchor = GridBagConstraints.WEST;
		gbc_textField_3.insets = new Insets(0, 0, 5, 0);
		gbc_textField_3.gridx = 9;
		gbc_textField_3.gridy = 4;
		panel.add(channel2Modx, gbc_textField_3);
		channel2Modx.setColumns(20);
		
		JLabel lblMody_1 = new JLabel("MODy[1]=");
		GridBagConstraints gbc_lblMody_1 = new GridBagConstraints();
		gbc_lblMody_1.anchor = GridBagConstraints.EAST;
		gbc_lblMody_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblMody_1.gridx = 8;
		gbc_lblMody_1.gridy = 5;
		panel.add(lblMody_1, gbc_lblMody_1);
		
		channel2Mody = new JTextField();
		GridBagConstraints gbc_textField_4 = new GridBagConstraints();
		gbc_textField_4.anchor = GridBagConstraints.WEST;
		gbc_textField_4.insets = new Insets(0, 0, 5, 0);
		gbc_textField_4.gridx = 9;
		gbc_textField_4.gridy = 5;
		panel.add(channel2Mody, gbc_textField_4);
		channel2Mody.setColumns(20);
		
		JLabel lblModz_1 = new JLabel("MODz[1]=");
		GridBagConstraints gbc_lblModz_1 = new GridBagConstraints();
		gbc_lblModz_1.anchor = GridBagConstraints.EAST;
		gbc_lblModz_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblModz_1.gridx = 8;
		gbc_lblModz_1.gridy = 6;
		panel.add(lblModz_1, gbc_lblModz_1);
		
		channel2Modz = new JTextField();
		GridBagConstraints gbc_textField_5 = new GridBagConstraints();
		gbc_textField_5.insets = new Insets(0, 0, 5, 0);
		gbc_textField_5.anchor = GridBagConstraints.WEST;
		gbc_textField_5.gridx = 9;
		gbc_textField_5.gridy = 6;
		panel.add(channel2Modz, gbc_textField_5);
		channel2Modz.setColumns(20);
		
		channel3Label = new JLabel("Channel 3:");
		GridBagConstraints gbc_lblChannel_2 = new GridBagConstraints();
		gbc_lblChannel_2.anchor = GridBagConstraints.WEST;
		gbc_lblChannel_2.insets = new Insets(0, 0, 5, 5);
		gbc_lblChannel_2.gridx = 6;
		gbc_lblChannel_2.gridy = 8;
		panel.add(channel3Label, gbc_lblChannel_2);
		
		JLabel lblModx_2 = new JLabel("MODx[2]=");
		GridBagConstraints gbc_lblModx_2 = new GridBagConstraints();
		gbc_lblModx_2.anchor = GridBagConstraints.EAST;
		gbc_lblModx_2.insets = new Insets(0, 0, 5, 5);
		gbc_lblModx_2.gridx = 8;
		gbc_lblModx_2.gridy = 8;
		panel.add(lblModx_2, gbc_lblModx_2);
		
		channel3Modx = new JTextField();
		GridBagConstraints gbc_textField_6 = new GridBagConstraints();
		gbc_textField_6.anchor = GridBagConstraints.WEST;
		gbc_textField_6.insets = new Insets(0, 0, 5, 0);
		gbc_textField_6.gridx = 9;
		gbc_textField_6.gridy = 8;
		panel.add(channel3Modx, gbc_textField_6);
		channel3Modx.setColumns(20);
		
		JLabel lblMody_2 = new JLabel("MODy[2]=");
		GridBagConstraints gbc_lblMody_2 = new GridBagConstraints();
		gbc_lblMody_2.anchor = GridBagConstraints.EAST;
		gbc_lblMody_2.insets = new Insets(0, 0, 5, 5);
		gbc_lblMody_2.gridx = 8;
		gbc_lblMody_2.gridy = 9;
		panel.add(lblMody_2, gbc_lblMody_2);
		
		channel3Mody = new JTextField();
		GridBagConstraints gbc_textField_7 = new GridBagConstraints();
		gbc_textField_7.anchor = GridBagConstraints.WEST;
		gbc_textField_7.insets = new Insets(0, 0, 5, 0);
		gbc_textField_7.gridx = 9;
		gbc_textField_7.gridy = 9;
		panel.add(channel3Mody, gbc_textField_7);
		channel3Mody.setColumns(20);
		
		JLabel lblModz_2 = new JLabel("MODz[2]=");
		GridBagConstraints gbc_lblModz_2 = new GridBagConstraints();
		gbc_lblModz_2.anchor = GridBagConstraints.EAST;
		gbc_lblModz_2.insets = new Insets(0, 0, 0, 5);
		gbc_lblModz_2.gridx = 8;
		gbc_lblModz_2.gridy = 10;
		panel.add(lblModz_2, gbc_lblModz_2);
		
		channel3Modz = new JTextField();
		GridBagConstraints gbc_textField_8 = new GridBagConstraints();
		gbc_textField_8.anchor = GridBagConstraints.WEST;
		gbc_textField_8.gridx = 9;
		gbc_textField_8.gridy = 10;
		panel.add(channel3Modz, gbc_textField_8);
		channel3Modz.setColumns(20);
	
	
		// initialize the components ...
		initialize(settings);
		
	}

}
