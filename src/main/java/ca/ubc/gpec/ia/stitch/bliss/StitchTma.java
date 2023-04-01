/*
 * Stitch_BLISS.java
 *
 * Created on June 21, 2005, 3:17 PM
 *
 * Jun 20, 2005pa
 * - Combine dialog boxes for stitched folder name, number of rows & columns
 * - Add ability to batch stitch images from more than one folders
 *
 * Jun 21, 2005
 * - Add ability to automatically determine number of tiles for each core
 *
 * Jun 22, 2005
 * - Add ability to stitch images from different Z planes
 *
 * Jul 5, 2005
 * - Add ability to stitch images scanned in "rectangle" format
 */
package ca.ubc.gpec.ia.stitch.bliss;

import ij.*;
import ij.io.*;
import ij.process.*;
import ij.gui.*;
import ij.plugin.*;

import java.io.*;
import java.util.StringTokenizer;
import java.util.regex.*;
import java.util.ArrayList;
import java.text.DecimalFormat;
import javax.swing.*;


/**
 *
 * @author samuelc
 */
public class StitchTma {

    private String csv_file = "DB_Input.csv";
    private String final_scan_file = "FinalScan.ini";
    private String current_dir;                     // Current directory
    private String stitched_parent;                 // Parent folder of the stitched images
    private String stitched_suffix = "_stitched";   // Suffix of folder(s) for the stitched images
    private String coreFilenameFormat;				// core filename format
    private File[] folders;                         // Folders to be stitched
    private boolean auto_tiles;                     // Whether to auto determine number of tiles
    private int tile_cols;                          // Number of tiles horizontally
    private int tile_rows;                          // Number of tiles vertically
    private boolean finish_msg = true;              // Whether to display a message when finished stitching
    private String imgFormat;                       // Format of result image
    static final int TMA_FORMAT = 1;                // scanned format of the image
    static final int RECTANGLE_FORMAT = 2;
    private String stanford_format_option_sector_num;
    private String stanford_format_option_tma_num;
    private String stanford_format_option_slice_num;
    private String stanford_format_option_bliss_abs_code;
    static final String OPTION_CORE_FILENAME_FORMAT_GPEC = "GPEC";
    static final String OPTION_CORE_FILENAME_FORMAT_STANFORD = "Stanford TMAD";
    static final String OPTION_FIND_TILES_DIM_AUTO = "Automatically";
    static final String OPTION_FIND_FILES_DIM_MANUAL = "Manually";
    static final String FILE_FORMAT_JPG = "jpg";
    static final String FILE_FORMAT_BMP = "bmp";
    static final String FILE_FORMAT_TIF = "tif";

    public void run(String arg) throws SettingsException, StitchException {
        getSettings(arg);
        stitchFolders();
    }

    /**
     * return true if settings ok, false, settings incompletely specified
     *
     * @param title
     * @return
     */
    public void getSettings(String title) throws SettingsException {
        GenericDialog gd = new GenericDialog("Stitched filename format:");
        // Get user preference on core file name format
        String coreFilenameFormatChoices[] = new String[2];
        coreFilenameFormatChoices[0] = OPTION_CORE_FILENAME_FORMAT_GPEC;
        coreFilenameFormatChoices[1] = OPTION_CORE_FILENAME_FORMAT_STANFORD;
        gd.addChoice("Please specify stitched image filename format:", coreFilenameFormatChoices, OPTION_CORE_FILENAME_FORMAT_GPEC);
        gd.showDialog();
        if (gd.wasCanceled()) {	// Exit the plugin
            throw new SettingsException("stitched image filename format not specified");
        }
        this.coreFilenameFormat = gd.getNextChoice().toString(); // getting user choice
        if (this.coreFilenameFormat.equals(OPTION_CORE_FILENAME_FORMAT_STANFORD)) {

            // ask more details for Stanford TMAD format ...
            gd = new GenericDialog("Stanford TMAD core image format settings:");

            // sector number - assume only one sector per directory !!!
            gd.addStringField("Please specify sector number (assume only one sector per image folder):", "", 3);
            gd.addStringField("Please specify TMA block number:", "", 3);
            gd.addStringField("Please specify TMA slice number:", "", 3);
            gd.addStringField("Please specify BLISS antibody code:", "", 6);
            gd.showDialog();
            if (gd.wasCanceled()) {	// Exit the plugin
                throw new SettingsException("Stanford TMAD info not specified");
            }

            stanford_format_option_sector_num = gd.getNextString();
            stanford_format_option_tma_num = gd.getNextString();
            stanford_format_option_slice_num = gd.getNextString();
            stanford_format_option_bliss_abs_code = gd.getNextString();
        }

        //////////////////////////////////////////
        // ask user for location of files ...
        // Ask user to specify the location of the image folder
        JFileChooser fc = new JFileChooser();
        if (this.coreFilenameFormat.equals(OPTION_CORE_FILENAME_FORMAT_STANFORD)) {
            fc.setMultiSelectionEnabled(false);	// disable multiple select
        } else {
            fc.setMultiSelectionEnabled(true);	// enable multiple select
        }
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);	// Only allow user to choose directories
        if (title.length() < 1) {
            title = "Please select folder(s) to be stitched";
        }
        fc.setDialogTitle(title);
        int returnVal = fc.showOpenDialog(IJ.getInstance());
        if (returnVal != JFileChooser.APPROVE_OPTION) { // Exit the plugin
            throw new SettingsException("input image folder not specified");
        }
        if (this.coreFilenameFormat.equals(OPTION_CORE_FILENAME_FORMAT_STANFORD)) {
            this.folders = new File[1];
            this.folders[0] = fc.getSelectedFile();
        } else {
            this.folders = fc.getSelectedFiles(); // possible multiple files
        }

        // Obtain and create the stitched path
        gd = new GenericDialog("Stitch settings:");
        this.current_dir = fc.getCurrentDirectory().getPath();
        gd.addStringField("Please specify the parent folder for the stitched files: ", this.current_dir, 20);
        gd.addStringField("Please specify the suffix of the folder(s) for the stitched files: ", this.stitched_suffix, 20);

        // Get number of tiles per row and column
        String choices[] = new String[2];
        choices[0] = OPTION_FIND_TILES_DIM_AUTO;
        choices[1] = OPTION_FIND_FILES_DIM_MANUAL;
        gd.addChoice("Specify number of tiles per image:", choices, OPTION_FIND_TILES_DIM_AUTO);

        // Get format of result image
        String formatChoices[] = new String[3];
        formatChoices[0] = FILE_FORMAT_BMP;
        //formatChoices[1] = "gif";
        formatChoices[1] = FILE_FORMAT_JPG;
        //formatChoices[3] = "png";
        formatChoices[2] = FILE_FORMAT_TIF;
        gd.addChoice("Specify format of result image(s):", formatChoices, FILE_FORMAT_JPG);

        gd.showDialog();
        if (gd.wasCanceled()) {	// Exit the plugin
            throw new SettingsException("stitch settings not specified");
        }

        // Obtain setting values
        this.stitched_parent = gd.getNextString();
        this.stitched_suffix = gd.getNextString();
        String tile_choice = gd.getNextChoice();
        this.imgFormat = gd.getNextChoice();



        if (tile_choice.equals(OPTION_FIND_TILES_DIM_AUTO)) {
            this.auto_tiles = true;
        } else { // Ask user to input number of tiles
            this.auto_tiles = false;
            gd = new GenericDialog("Stitch settings:");
            gd.addStringField("Please specify the number of tiles horizontally:", "3", 2);
            gd.addStringField("Please specify the number of tiles vertically: ", "3", 2);
            gd.showDialog();
            if (gd.wasCanceled()) {	// Exit the plugin
                throw new SettingsException("tile dimension not specified");
            }

            // Obtain setting values
            this.tile_cols = Integer.parseInt(gd.getNextString());
            this.tile_rows = Integer.parseInt(gd.getNextString());
        }

        // if we are here, settings are completely specified.
        // ok to continue to stitch
    }

    public void stitchFolders() throws StitchException {
        for (int i = 0; i < this.folders.length; i++) {
            stitchFolder(this.folders[i]);
        }

        if (this.finish_msg == true) {
            IJ.showMessage("Done stitching images in '" + this.current_dir + "'.");
        }
    }

    public String[] getStitchedFolders() { // Get folders containing the stitched image
        String[] dirs = new String[this.folders.length];
        for (int i = 0; i < this.folders.length; i++) {
            dirs[i] = this.stitched_parent + System.getProperty("file.separator") + folders[i].getName() + this.stitched_suffix;
        }
        return dirs;
    }

    public String getStitchedParent() { // Returns the parent folder of stitched folders
        return this.stitched_parent;
    }

    public void setFinishMsg(boolean b) { // Set whether to display message when finished
        this.finish_msg = b;
    }

    private void stitchFolder(File folder) throws StitchException {
        String path_name = folder.getName();              		// The name of the source directory
        String path = this.current_dir + System.getProperty("file.separator") + path_name;	// Source path
        String source_csv_file = path + System.getProperty("file.separator") + this.csv_file;      	// Fully qualified path to CSV file
        String stitched_path = this.stitched_parent + System.getProperty("file.separator") + path_name + this.stitched_suffix;          // The output path for stitched images

        File f = new File(stitched_path);
        f.mkdir();

        // Copy the DB_Input.csv file
        /*
         * File source_file = new File(source_csv_file); File target_file = new
         * File(stitched_path + System.getProperty("file.separator") +
         * this.csv_file); try { copy(source_file, target_file); }
         * catch(IOException e) { IJ.showMessage("Cannot copy DB_Input.csv
         * file.");
        }
         */

        // See if auto determine tiles.
        if (this.auto_tiles == true) {
            determineTiles(path + System.getProperty("file.separator") + this.final_scan_file);
        }

        // Get the Z planes
        ArrayList zplanes = getZPlanes(path);
        if (zplanes.size() > 1 & this.coreFilenameFormat.equals(OPTION_CORE_FILENAME_FORMAT_STANFORD)) {
            // currently does not support multiple z-plane for stanford filename format
            throw new StitchException("Currently does not support multiple z-plane for Stanford filename format.");
        }

        int tile_index = 0;             // Index of tiles
        int zplanes_tile_index = 0;         // Tracks the tile index for current set of Z planes

        // See if the DB_input.csv file exists. If it does, then is TMA format. Otherwise is rectangle format.
        f = new File(source_csv_file);
        if (f.exists()) { // TMA format
            //Read the CSV file
            try {
                FileReader fr = new FileReader(source_csv_file);
                BufferedReader bIn = new BufferedReader(fr);

                //read first line from file
                String line;
                int line_index = 0;
                int core_img_index = 1;  // Index of core images
                int img_count;  // Number of core images in the subfolder
                //String core_img_index_format = "000"; // The string format of the image index

                while ((line = bIn.readLine()) != null) {
                    if (line_index == 0) { // First line - tells you number of cores
                        img_count = Integer.parseInt(line);
                    } else {
                        // Get the row and column
                        StringTokenizer st = new StringTokenizer(line);
                        int row = Integer.parseInt(st.nextToken(","));
                        int col = Integer.parseInt(st.nextToken(","));

                        zplanes_tile_index = createImage(TMA_FORMAT, path_name, path, stitched_path, zplanes, tile_index, zplanes_tile_index, row, col, core_img_index);
                        // Increment the tile index since done with current set of Z planes
                        tile_index += (zplanes_tile_index / line_index);

                        // Increment core image index
                        core_img_index++;
                    }
                    line_index++;
                }
            } catch (IOException e) {
                IJ.showMessage("File not found.");
            }
        } else { // Rectangle format
            zplanes_tile_index = createImage(RECTANGLE_FORMAT, path_name, path, stitched_path, zplanes, tile_index, zplanes_tile_index, 0, 0, 0);
        }
    }

    // Stitch images scanned in tissue microarray format
    private int createImage(int format, String path_name, String path, String stitched_path, ArrayList zplanes, int tile_index, int zplanes_tile_index, int row, int col, int core_img_index) {
        String core_img_index_format = "000"; // The string format of the image index

        int tiles_per_core = this.tile_cols * this.tile_rows;

        int tile_width = 752; //Integer.parseInt(IJ.getString("Please specify the width of each tile: ", "752"));
        int tile_height = 480; //Integer.parseInt(IJ.getString("Please specify the height of each tile: ", "480"));

        // Figure out the image height and width
        int img_width = tile_width * this.tile_cols;
        int img_height = tile_height * this.tile_rows;

        for (int z_index = 0; z_index < zplanes.size(); z_index++) { // Traverse thru all Z planes
            // Reset the tile index for current set of Z planes
            zplanes_tile_index = tile_index;

            // Create new image
            String stitched_filename = "";
            if (format == TMA_FORMAT) {
                DecimalFormat df = new DecimalFormat(core_img_index_format);
                if (this.coreFilenameFormat.equals(OPTION_CORE_FILENAME_FORMAT_GPEC)) {
                    stitched_filename = path_name + "_" + df.format(core_img_index) + "_r" + row + "c" + col + zplanes.get(z_index) + "." + imgFormat;
                } else {
                    // Stanford TMAD format
                    stitched_filename =
                            stanford_format_option_sector_num + "_"
                            + stanford_format_option_tma_num + "_"
                            + col + "_"
                            + row + "_"
                            + stanford_format_option_slice_num + "_"
                            + stanford_format_option_bliss_abs_code + "_"
                            + core_img_index
                            + "." + imgFormat;
                }
            } else if (format == RECTANGLE_FORMAT) {
                if (zplanes.get(z_index).toString().length() > 0) {
                    stitched_filename = path_name + "_" + zplanes.get(z_index) + "." + imgFormat;
                } else {
                    stitched_filename = path_name + "." + imgFormat;
                }
            }
            ImagePlus stitched = NewImage.createRGBImage(stitched_filename, img_width, img_height, 1, NewImage.FILL_WHITE);
            ImageProcessor stitched_proc = stitched.getProcessor();

            String source_img_file;
            File sf;
            for (int row_index = 1; row_index <= tile_rows; row_index++) {
                if ((row_index % 2) == 0) { // Even rows
                    for (int col_index = tile_cols; col_index >= 1; col_index--) {
                        // Figure out the source file and see if it exist
                        source_img_file = path + System.getProperty("file.separator") + "Da" + Integer.toString(zplanes_tile_index) + zplanes.get(z_index) + ".jpg";
                        sf = new File(source_img_file);
                        if (!sf.exists()) {
                            return -1;
                        }
                        stitched_proc = stitch(stitched_proc, source_img_file, tile_width, tile_height, col_index, row_index, zplanes_tile_index);

                        // Increment core image index
                        zplanes_tile_index++;
                    }
                } else { // Odd rows
                    for (int col_index = 1; col_index <= tile_cols; col_index++) {
                        // Figure out the source file and see if it exist
                        source_img_file = path + System.getProperty("file.separator") + "Da" + Integer.toString(zplanes_tile_index) + zplanes.get(z_index) + ".jpg";
                        sf = new File(source_img_file);
                        if (!sf.exists()) {
                            return -1;
                        }
                        stitched_proc = stitch(stitched_proc, source_img_file, tile_width, tile_height, col_index, row_index, zplanes_tile_index);

                        // Increment tile index
                        zplanes_tile_index++;
                    }
                }
            }

            // Save the image
            String target_img_file = stitched_path + System.getProperty("file.separator") + stitched_filename;
            IJ.showStatus("Generating '" + stitched_filename + "'...");
            FileSaver fs = new FileSaver(stitched);
            if (imgFormat.equals("bmp")) {
                fs.saveAsBmp(target_img_file);
            } //else if (imgFormat.equals("gif")) {fs.saveAsGif(target_img_file);}
            else if (imgFormat.equals("jpg")) {
                fs.saveAsJpeg(target_img_file);
            } //else if (imgFormat.equals("png")) {fs.saveAsPng(target_img_file);}
            else if (imgFormat.equals("tif")) {
                fs.saveAsTiff(target_img_file);
            }
        } // done for loop for Z planes

        return zplanes_tile_index;
    }

    private void determineTiles(String file) { // Determine the number of tiles from the FinalScan.ini file
        //Read the file
        try {
            FileReader fr = new FileReader(file);
            BufferedReader bIn = new BufferedReader(fr);

            //read first line from file
            String line;
            Pattern tn = Pattern.compile("\\[Da(\\d+)\\]");         // Look for [Da123]
            Pattern x = Pattern.compile("^x=(\\-?\\d+)");            // Look for x=-1234
            Pattern y = Pattern.compile("^y=(\\-?\\d+)");            // Look for y=-3456
            Pattern s = Pattern.compile("^s=(\\d+)");               // Look for s=1
            String curr_tn = "";
            String curr_x = "";
            String curr_y = "";
            String prev_tn = "";
            String prev_x = "";
            String prev_y = "";
            int con_tiles = 0; // Number of consecutive tiles traversed
            int con_y = 0;     // Number of consecutive tiles with same y-coordinate traversed

            while ((line = bIn.readLine()) != null) {
                Matcher m_tn = tn.matcher(line);
                Matcher m_x = x.matcher(line);
                Matcher m_y = y.matcher(line);
                Matcher m_s = s.matcher(line);
                if (m_tn.matches()) {
                    curr_tn = m_tn.group(1);
                    con_tiles++;
                } else if (m_x.matches()) {
                    curr_x = m_x.group(1);
                } else if (m_y.matches()) {
                    curr_y = m_y.group(1);
                } else if (m_s.matches()) { // Done with the current tile
                    if ((!curr_tn.equals("0")) && (!prev_x.equals(curr_x)) && (!prev_y.equals(curr_y))) { // Done with a core
                        this.tile_cols = con_y + 1;
                        this.tile_rows = con_tiles / (con_y + 1);
                        break;
                    }

                    if (curr_y.equals(prev_y)) {
                        con_y++;
                    } else {
                        con_y = 0;
                    }

                    prev_x = curr_x;
                    prev_y = curr_y;
                }
            }

            // All the way to the end of file and still in the same "core"
            this.tile_cols = con_y + 1;
            this.tile_rows = con_tiles / (con_y + 1);
        } catch (IOException e) {
            IJ.showMessage("File not found.");
        }
    }

    private ArrayList getZPlanes(String path) {
        ArrayList zplanes = new ArrayList();
        File f = new File(path);
        String[] files = f.list();
        Pattern p = Pattern.compile("Da\\d+(_?\\w*)\\.jpg");         // Look for Da2_u2.jpg
        for (int i = 0; i < files.length; i++) {
            Matcher m = p.matcher(files[i]);
            if (m.matches()) {
                String match = m.group(1);
                if (zplanes.contains(match)) { // Found all z planes
                    break;
                } else { // A new zplane
                    zplanes.add(match);
                }
            }
        }

        return zplanes;
    }

    private ImageProcessor stitch(ImageProcessor stitched_proc, String source_file, int tile_width, int tile_height, int col_index, int row_index, int tile_index) {
        // Open the individual tiled file
        IJ.showStatus("Stitching '" + source_file + "'...");

        //try {
        ImagePlus ip = new ImagePlus(source_file);
        ImageProcessor iproc = ip.getProcessor();

        // Copy
        int start_x = (col_index - 1) * tile_width;
        int start_y = (row_index - 1) * tile_height;

        stitched_proc.copyBits(iproc, start_x, start_y, Blitter.COPY);
        //}
        //catch(IllegalArgumentException e) {
        // Image file not found - skip
        //}

        return stitched_proc;
    }

    private void copy(File source, File dest) throws IOException {  // From Chris Smith, MindIQ Corporation
        final int BUFFER_SIZE = 32768;
        InputStream in = new FileInputStream(source);
        try {
            OutputStream out = new FileOutputStream(dest);
            try {
                byte[] buffer = new byte[BUFFER_SIZE];
                int len;

                while ((len = in.read(buffer)) > 0) {
                    out.write(buffer, 0, len);
                }
            } finally {
                out.close();
            }
        } finally {
            in.close();
        }
    }
}
