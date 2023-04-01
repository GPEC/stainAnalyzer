package ca.ubc.gpec.ia.analyzer.gui;

/**
 * show info for this plugin
 */

import javax.swing.JPanel;

import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import javax.swing.JEditorPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLEditorKit;

import java.awt.GridBagConstraints;
import java.io.IOException;

public class AboutPanel extends JPanel {

	private static final String GPEC_DESCRIPTION = "<p>The Genetic Pathology Evaluation Centre (<a href='http://www.gpec.ubc.ca'>GPEC</a>) was developed in late 2001, and is a collaborative research venture of the Pathology Department at VGH and UBC, the Vancouver Prostate Centre at Vancouver General Hospital, and the British Columbia Cancer Agency (BCCA).</p>";
	private static final String PLUGIN_DESCRIPTION = "<p>The Subcellular Stain Analyser is an <a href='http://rsbweb.nih.gov/ij/'>ImageJ</a> plugin application designed for automated image analyses of histological images.</p>";
	private static final String AUTHOR_DESCRIPTION = "<p>The project was started in February 2005 at the Genetic Pathology Evaluation Centre, University of British Columbia. Authors:<ul><li>Andy Chan<li>Dmitry Turbin<li>Samuel Leung<li>Dustin Thomson</ul></p>";
	private static final String SPECIAL_THANKS = "<p><i>Special thanks</i> to ... <ul><li>Wayne Rasband - for the wonderful ImageJ program<li>Gabriel Landini - for his plugin Colour Deconvolutor<li>Zafir Anjum - for table methods programming</ul></p>";		
			
	JEditorPane content;
	
	/**
	 * Create the panel.
	 */
	public AboutPanel() {
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		content = new JEditorPane();
		GridBagConstraints gbc_editorPane = new GridBagConstraints();
		gbc_editorPane.fill = GridBagConstraints.BOTH;
		gbc_editorPane.gridx = 0;
		gbc_editorPane.gridy = 0;
		add(content, gbc_editorPane);

		content.setContentType("text/html");
		content.setPreferredSize(new Dimension(520,540));
		
		HTMLEditorKit ekit = new HTMLEditorKit();

		content.setEditorKit(ekit);
		addContent();
		content.setEditable(false);
		
		// add hyperlink listener
		content.addHyperlinkListener(new HyperlinkListener() {
			public void hyperlinkUpdate(HyperlinkEvent evt) {
		        if (evt.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
		        	try {
		        		Desktop.getDesktop().browse(evt.getURL().toURI());
		        	} catch (Exception e) {
		        		// just ignore this silent :)
		        	}
		        }
		    }
		});
	}

	/**
	 * add html to contnet
	 */
	private void addContent() {
		content.setText(
			"<h2>About GPEC ...</h2>" +	
			GPEC_DESCRIPTION + 
			"<h2>About this software ...</h2>" +				
			PLUGIN_DESCRIPTION +
			"<h2>About authors ...</h2>" +	
			AUTHOR_DESCRIPTION + 
			SPECIAL_THANKS
		);
	}
}
