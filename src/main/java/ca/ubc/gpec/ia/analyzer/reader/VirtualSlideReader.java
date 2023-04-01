/*
 * Reader virtual slide, generic class
 */
package ca.ubc.gpec.ia.analyzer.reader;

import ca.ubc.gpec.fieldselector.model.FieldOfView;
import ca.ubc.gpec.ia.analyzer.reader.aperioAnnotation.exception.RegionGeneratorMaxTriesExceeded;
import ca.ubc.gpec.ia.analyzer.reader.aperioAnnotation.model.Annotation;
import ca.ubc.gpec.ia.analyzer.reader.aperioAnnotation.model.Annotations;
import ca.ubc.gpec.ia.analyzer.reader.aperioAnnotation.model.Region;
import ca.ubc.gpec.ia.analyzer.reader.aperioAnnotation.exception.RegionNotPreprocessedException;
import ca.ubc.gpec.ia.analyzer.reader.aperioAnnotation.exception.RegionNotSupportedException;
import ca.ubc.gpec.ia.analyzer.views.ViewConstants;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.io.FileSaver;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Random;
import java.util.TreeSet;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import loci.formats.FormatException;
import loci.formats.IFormatReader;
import loci.formats.ImageReader;
import loci.formats.MetadataTools;
import loci.formats.gui.BufferedImageReader;
import loci.formats.in.*;

/**
 *
 * @author samuelc
 */
public class VirtualSlideReader {
    
    public static boolean DEBUG = true;
    
    public static final String EXPORT_DIRECTORY_NAME = ".extractedSVSRegions";
    
    private File virtualSlideFile; // file in vendor specific format
    private File annotationFile; // annotation file
    private File exportDirectory; // directory (folder) to hold any exported files
    private ArrayList<File> analyzableFiles; // analyzable file(s) e.g.
    // if originalFile is a jpeg, analyzableFile would be the originalFile
    // if originalFile is a svs, analyzableFile(s) would be region(s) specified
    // by the corresponding annotation file.
    private IFormatReader ir; // pointer to the image reader
    private Annotations annotations; // annotation objects

    /**
     * constructor
     *
     * @param file
     * @param readAndExtractAnnotation
     */
    public VirtualSlideReader(File file, boolean readAndExtractAnnotation) throws VirtualSlideReaderException {
        virtualSlideFile = file;
        analyzableFiles = new ArrayList<>();
        // read file ...
        ir = new ImageReader();
        // tell the reader where to store the metadata from the dataset
        ir.setMetadataStore(MetadataTools.createOMEXMLMetadata());
        // initialize annotation to null
        annotations = null;
        try {
            ir.setId(virtualSlideFile.getPath());
            ir.getFormat();

            // check for common file types ...
            BMPReader br = new BMPReader();
            JPEGReader jr = new JPEGReader();
            //TiffReader tr = new TiffReader();
            GIFReader gr = new GIFReader();
            if (br.isThisType(virtualSlideFile.getPath())
                    | jr.isThisType(virtualSlideFile.getPath())
                    // | tr.isThisType(originalFile.getPath())
                    | gr.isThisType(virtualSlideFile.getPath())) {
                // common file type that ImageJ can handle ... set analyzable file to original file
                if (DEBUG) {
                    System.out.println("common type file selected: " + virtualSlideFile.getName());
                }
                analyzableFiles.add(virtualSlideFile);
            } else {
                // files that ImageJ cannot handle ... vendor specific file format ...

                //////////////////////////////////////////////////
                // APERIO SVS, Hamamatsu .ndpi, Leica .scn - (2015-03-19 bioformat does not really work on scn quite yet - maybe try later) 
                // double check to make sure input file is svs
                SVSReader sr = new SVSReader();
                NDPIReader nr = new NDPIReader();
                //LeicaSCNReader lr = new LeicaSCNReader();
                if (sr.isThisType(virtualSlideFile.getPath())
                        || nr.isThisType(virtualSlideFile.getPath()) //|| lr.isThisType(virtualSlideFile.getPath())
                        ) {
                    if (readAndExtractAnnotation) {
                        // expect to see xml annotation file since it is currently not possible 
                        // to export the whole aperio file (too big)
                        // annotation file name ...
                        // [some name].svs => [some name].xml
                        String annotationFilename = virtualSlideFile.getPath().substring(0, virtualSlideFile.getPath().length() - 3) + "xml";
                        if (DEBUG) {
                            System.out.println("looking for annotation file: " + annotationFilename);
                        }
                        annotationFile = new File(annotationFilename);
                        if (!annotationFile.canRead()) {
                            throw new VirtualSlideReaderException("expected but failed to find/read annotation file: " + annotationFilename);
                        }
                        extractAndExportAnnotationSelection(ir, annotationFile, true, true);
                    }
                } else {
                    ///////////////////////////
                    // unknown file type
                    throw new VirtualSlideReaderException("unknown file format: " + virtualSlideFile.getName());
                }
            }
        } catch (FormatException | IOException ex) {
            throw new VirtualSlideReaderException(ex.toString());
        }
    }

    /**
     * check to see if file is Aperio SVS
     *
     * @return
     */
    public boolean isAperioSvs() {
        SVSReader sr = new SVSReader();
        return sr.isThisType(virtualSlideFile.getPath());
    }

    /**
     * check to see if file is Hamamatsu ndpi format
     *
     * @return
     */
    public boolean isHamamatsuNdpi() {
        NDPIReader nr = new NDPIReader();
        return nr.isThisType(virtualSlideFile.getPath());
    }

    /**
     * check to see if file is Leica scn format
     *
     * @return
     */
    public boolean isLeicaScn() {
        LeicaSCNReader lr = new LeicaSCNReader();
        return lr.isThisType(virtualSlideFile.getPath());
    }

    /**
     * return the # of images from the last image layer corresponding to (and
     * including the) slide view image
     *
     * @return
     */
    private int getNumImagesFromLastToSlideView() {
        return 1;
    }

    /**
     * return the # of images from the last image layer corresponding to (and
     * including the) label image
     *
     * @return
     */
    private int getNumImagesFromLastToLabel() {
        return 2;
    }

    /**
     * return the # of images from the last image layer corresponding to (and
     * including the) thumbnail image
     *
     * @return
     */
    private int getNumImagesFromLastToThumbnail() {
        return 3;
    }

    /**
     * return the # of images from the last image layer corresponding to (and
     * including the)low res image
     *
     * @return
     */
    private int getNumImagesFromLastToLowRes() {
        return 4;
    }

    /**
     * return original file (in vendor specific format)
     *
     * @return
     */
    public File getVirtualSlideFile() {
        return virtualSlideFile;
    }

    /**
     * get width of image
     *
     * @return
     */
    public int getWidth() {
        return ir.getSizeX();
    }

    /**
     * get height of image
     *
     * @return
     */
    public int getHeight() {
        return ir.getSizeY();
    }

    /**
     * get label image ... ASSUME label is AWAYS in the last series!!!
     *
     * @return
     * @throws VirtualSlideReaderException
     * @throws FormatException
     * @throws IOException
     */
    public BufferedImage getLabel() throws VirtualSlideReaderException, FormatException, IOException {
        // make sure its aperio ...
        SVSReader sr = new SVSReader();
        NDPIReader nr = new NDPIReader();
        LeicaSCNReader lr = new LeicaSCNReader();
        if (!(sr.isThisType(virtualSlideFile.getPath())
                || nr.isThisType(virtualSlideFile.getPath())
                || lr.isThisType(virtualSlideFile.getPath()))) {
            throw new VirtualSlideReaderException("input file NOT .svs .ndpi or .scn (currently only support .svs/.ndpi/.scn): " + virtualSlideFile.getName());
        }
        ir.setSeries(ir.getSeriesCount() - getNumImagesFromLastToLabel());
        BufferedImageReader bir = new BufferedImageReader(ir);
        BufferedImage result = bir.openImage(0);
        ir.setSeries(0);
        return result;
    }

    /**
     * get label image width
     *
     * @return
     * @throws VirtualSlideReaderException
     * @throws FormatException
     * @throws IOException
     */
    public int getLabelWidth() throws VirtualSlideReaderException, FormatException, IOException {
        return getLabel().getWidth();
    }

    /**
     * get label image height
     *
     * @return
     * @throws VirtualSlideReaderException
     * @throws FormatException
     * @throws IOException
     */
    public int getLabelHeight() throws VirtualSlideReaderException, FormatException, IOException {
        return getLabel().getHeight();
    }

    /**
     * get thumbnail image .. assume FIRST series only
     *
     * @return
     * @throws VirtualSlideReaderException
     * @throws FormatException
     * @throws IOException
     */
    public BufferedImage getThumbImage() throws VirtualSlideReaderException, FormatException, IOException {
        BufferedImageReader bir;
        BufferedImage result = null;
        SVSReader sr = new SVSReader();
        NDPIReader nr = new NDPIReader();
        LeicaSCNReader lr = new LeicaSCNReader();
        if (sr.isThisType(virtualSlideFile.getPath())
                || nr.isThisType(virtualSlideFile.getPath())
                || lr.isThisType(virtualSlideFile.getPath())) {
            ir.setSeries(ir.getSeriesCount() - getNumImagesFromLastToThumbnail());
            bir = new BufferedImageReader(ir);
            result = bir.openImage(0);
        } else {
            // get a generic(?) thumbnail provided by BufferedImageReader
            bir = new BufferedImageReader(ir);
            result = bir.openThumbImage(0);
        }
        ir.setSeries(0);
        return result;
    }

    /**
     * get width of thumbnail image
     *
     * @return
     * @throws VirtualSlideReaderException
     * @throws FormatException
     * @throws IOException
     */
    public int getThumbImageWidth() throws VirtualSlideReaderException, FormatException, IOException {
        return getThumbImage().getWidth();
    }

    /**
     * get height of thumbnail image
     *
     * @return
     * @throws VirtualSlideReaderException
     * @throws FormatException
     * @throws IOException
     */
    public int getThumbImageHeight() throws VirtualSlideReaderException, FormatException, IOException {
        return getThumbImage().getHeight();
    }

    /**
     * get scale from Thumbnail to original image
     *
     * @return
     * @throws VirtualSlideReaderException
     * @throws FormatException
     * @throws IOException
     */
    public float getThumbImageScale() throws VirtualSlideReaderException, FormatException, IOException {
        return (((float) getWidth()) / ((float) getThumbImageWidth()) + ((float) getHeight()) / ((float) getThumbImageHeight())) / 2f;
    }

    /**
     * get slide view image
     *
     * @return
     * @throws VirtualSlideReaderException
     * @throws FormatException
     * @throws IOException
     */
    public BufferedImage getSlideView() throws VirtualSlideReaderException, FormatException, IOException {
        // make sure its aperio ...
        SVSReader sr = new SVSReader();
        NDPIReader nr = new NDPIReader();
        LeicaSCNReader lr = new LeicaSCNReader();
        if (nr.isThisType(virtualSlideFile.getPath())) {
            // it seems that there's no "slide view" for ndpi ... return the label view instead
            return getLabel();
        } else if (!sr.isThisType(virtualSlideFile.getPath()) && !lr.isThisType(virtualSlideFile.getPath())) {
            throw new VirtualSlideReaderException("input file NOT .svs .ndpi or .scn (currently only support .svs/.ndpi/.scn): " + virtualSlideFile.getName());
        }
        ir.setSeries(ir.getSeriesCount() - getNumImagesFromLastToSlideView());
        BufferedImageReader bir = new BufferedImageReader(ir);
        BufferedImage result = bir.openImage(0);
        ir.setSeries(0);
        return result;
    }

    /**
     * get slide view image width
     *
     * @return
     * @throws VirtualSlideReaderException
     * @throws FormatException
     * @throws IOException
     */
    public int getSlideViewWidth() throws VirtualSlideReaderException, FormatException, IOException {
        return getSlideView().getWidth();
    }

    /**
     * get slide view image height
     *
     * @return
     * @throws VirtualSlideReaderException
     * @throws FormatException
     * @throws IOException
     */
    public int getSlideViewHeight() throws VirtualSlideReaderException, FormatException, IOException {
        return getSlideView().getHeight();
    }

    /**
     * get low resolution view image
     *
     * @return
     * @throws VirtualSlideReaderException
     * @throws FormatException
     * @throws IOException
     */
    public BufferedImage getLowResView() throws VirtualSlideReaderException, FormatException, IOException {
        // make sure its aperio ...
        SVSReader sr = new SVSReader();
        NDPIReader nr = new NDPIReader();
        LeicaSCNReader lr = new LeicaSCNReader();
        if (!(sr.isThisType(virtualSlideFile.getPath()) || nr.isThisType(virtualSlideFile.getPath()) || lr.isThisType(virtualSlideFile.getPath()))) {
            throw new VirtualSlideReaderException("input file NOT .svs .ndpi or .scn (currently only support .svs/.ndpi/.scn): " + virtualSlideFile.getName());
        }
        ir.setSeries(ir.getSeriesCount() - getNumImagesFromLastToLowRes());
        BufferedImageReader bir = new BufferedImageReader(ir);
        BufferedImage result = bir.openImage(0);
        ir.setSeries(0);
        return result;
    }

    /**
     * get width of low res image
     *
     * @return
     * @throws VirtualSlideReaderException
     * @throws FormatException
     * @throws IOException
     */
    public int getLowResViewWidth() throws VirtualSlideReaderException, FormatException, IOException {
        return getLowResView().getWidth();
    }

    /**
     * get height of low res image
     *
     * @return
     * @throws VirtualSlideReaderException
     * @throws FormatException
     * @throws IOException
     */
    public int getLowResViewHeight() throws VirtualSlideReaderException, FormatException, IOException {
        return getLowResView().getHeight();
    }

    /**
     * get scale from low res view to original image
     *
     * @return
     * @throws VirtualSlideReaderException
     * @throws FormatException
     * @throws IOException
     */
    public float getLowResViewScale() throws VirtualSlideReaderException, FormatException, IOException {
        return (((float) getWidth()) / ((float) getLowResViewWidth()) + ((float) getHeight()) / ((float) getLowResViewHeight())) / 2f;
    }

    /**
     * get buffered image with ROI with specified type - support drawing of
     * single annotation, colored in red
     *
     * @param type
     * @return
     * @throws VirtualSlideReaderException
     * @throws FormatException
     * @throws IOException
     * @throws RegionNotSupportedException
     * @throws RegionNotPreprocessedException
     */
    public BufferedImage getThumbImageWithRoi(String type) throws VirtualSlideReaderException, FormatException, IOException, RegionNotSupportedException, RegionNotPreprocessedException {
        Hashtable<String, Color> typeTable = new Hashtable<>();
        typeTable.put(type, Color.red);
        return getThumbImageWithRoi(typeTable);
    }

    /**
     * get buffered image with ROI with specified type - support drawing of
     * multiple annotation(s), with different colors for different annotation
     *
     * @param type of annotation/consolidation(s) ... Hashtable<String,Color>
     * @return
     * @throws VirtualSlideReaderException
     * @throws FormatException
     * @throws IOException
     * @throws RegionNotSupportedException
     * @throws RegionNotPreprocessedException
     */
    public BufferedImage getThumbImageWithRoi(Hashtable<String, Color> typeTable) throws VirtualSlideReaderException, FormatException, IOException, RegionNotSupportedException, RegionNotPreprocessedException {
        //BufferedImageReader bir = new BufferedImageReader(ir);
        BufferedImage result = getThumbImage(); // bir.openThumbImage(0);
        if (annotations == null) {
            // no annotations (ROI) to draw
            return result;
        } else {
            // get scaling factor    
            //float xScale = ((float) ir.getThumbSizeX()) / ((float) ir.getSizeX());
            //float yScale = ((float) ir.getThumbSizeY()) / ((float) ir.getSizeY());
            float xScale = ((float) getThumbImageWidth()) / ((float) ir.getSizeX());
            float yScale = ((float) getThumbImageHeight()) / ((float) ir.getSizeY());
            
            for (String type : typeTable.keySet()) {
                Annotation annotation;
                boolean drawRegionNumber = false;
                if (type.equals(Annotation.ANNOTATION_TYPE_CONSOLIDATED_SELECTION)) {
                    annotation = annotations.getConsolidatedAnnotation();
                } else if (type.equals(Annotation.ANNOTATION_TYPE_GENERATED_VIRTUAL_TMA_CORES)) {
                    annotation = annotations.getRandomVirtualTmaCoresAnnotation();
                    drawRegionNumber = true; // for virtual tma cores drawn on thumbnail, want to draw the core number as well
                } else {
                    // default get original annotation
                    annotation = annotations.getOriginalAnnotation();
                }
                int regionNumber = 1;
                for (Region region : annotation.getRegions().getRegions()) {
                    Roi roi = region.getRoi(xScale, yScale, true);
                    roi.setStrokeColor(typeTable.get(type));
                    roi.setStrokeWidth(5);
                    roi.drawOverlay(result.getGraphics());
                    if (drawRegionNumber) {
                        Rectangle roiBound = roi.getBounds();
                        Graphics2D g2 = result.createGraphics();
                        g2.setFont(new Font(ViewConstants.DEFAULT_FONT_NAME, Font.BOLD, ViewConstants.DEFAULT_FONT_SIZE));
                        g2.setColor(typeTable.get(type));
                        g2.drawString("" + regionNumber, (int) Math.round(roiBound.getCenterX()), (int) Math.round(roiBound.getCenterY()));
                    }
                    regionNumber++;
                }
            }
            return result;
        }
    }

    /**
     * return the original annotation file
     *
     * @return
     */
    public File getAnnotationFile() {
        return annotationFile;
    }

    /**
     * read and set annotation file
     *
     * @param annotationFile
     * @throws VirtualSlideReaderException
     */
    public void setAnnotationFile(File annotationFile) throws VirtualSlideReaderException, JAXBException, FileNotFoundException {
        this.annotationFile = annotationFile;
        if (!annotationFile.canRead()) {
            throw new VirtualSlideReaderException("failed to read annotation file: " + annotationFile.getPath());
        }
        // parse annotation
        JAXBContext context = JAXBContext.newInstance(Annotations.class);
        Unmarshaller um = context.createUnmarshaller();
        annotations = (Annotations) um.unmarshal(new FileReader(annotationFile));
    }

    /**
     * read and set annotation file
     *
     * @param annotationFilename
     * @throws VirtualSlideReaderException
     */
    public void setAnnotationFile(String annotationFilename) throws VirtualSlideReaderException, JAXBException, FileNotFoundException {
        setAnnotationFile(new File(annotationFilename));
    }

    /**
     * get the annotations object
     *
     * WARNING!!! annotations object may be constantly being changed. Therefore,
     * if one wants the original UNCHANGED annotations, use
     * getOriginalAnnotations()
     *
     * @return
     */
    public Annotations getAnnotations() throws VirtualSlideReaderException {
        if (isAperioSvs() || isHamamatsuNdpi()) {
            if (annotations != null) {
                return annotations;
            } else {
                throw new VirtualSlideReaderException("missing annotation file");
            }
        } else {
            throw new VirtualSlideReaderException("getNumAnnotationLayers(): unsupported file format: " + virtualSlideFile.getName());
        }
    }

    /**
     * get ORIGINAL annotations
     *
     * assuming original annotation is ALWAYS the FIRST annotation
     *
     * @return
     * @throws JAXBException
     * @throws FileNotFoundException
     */
    public Annotations getOriginalAnnotations() throws JAXBException, FileNotFoundException {
        Annotations originalAnnotations = new Annotations();
        // need to preserve the following:  
        // micronsPerPixel, randomSeed, trialCentre, slideId,
        // specimenReceivedDate, rescore, pathologistName
        originalAnnotations.setMicronsPerPixel(annotations.getMicronsPerPixel());
        originalAnnotations.setRandomSeed(annotations.getRandomSeed());
        originalAnnotations.setTrialCentre(annotations.getTrialCentre());
        originalAnnotations.setSlideId(annotations.getSlideId());
        originalAnnotations.setSpecimenReceivedDate(annotations.getSpecimenReceivedDate());
        originalAnnotations.setRescore(annotations.getRescore());
        originalAnnotations.setPathologistName(annotations.getPathologistName());
        // end of preserve fields
        TreeSet<Annotation> newAnnotationTreeSet = new TreeSet<>();
        newAnnotationTreeSet.add(annotations.getOriginalAnnotation());
        originalAnnotations.setAnnotations(newAnnotationTreeSet);
        return originalAnnotations;
    }

    /**
     * set annotation back to original annotation
     */
    public void resetAnnotationBackToOriginal() throws JAXBException, FileNotFoundException {
        annotations = getOriginalAnnotations();
    }

    /**
     * generate random seed NOTE: default random seed = x-axis of first vertex
     * of the first region of the first annotation of the ORIGINAL annotation
     */
    public int getRandomSeed() {
        return annotations.getRandomSeed();
    }

    /**
     * return the number of annotation layers - applicable only to Aperio
     * annotation - EXCLUDES all annotations layers that are generated by this
     * application e.g. consolidated, random virtual TMA core
     *
     * @return
     */
    public int getNumOriginalAnnotationLayers() throws VirtualSlideReaderException {
        return getAnnotations().getNumOriginalAnnotationLayers();
    }

    /**
     * get micron per pixel on the annotation
     *
     * @return
     * @throws VirtualSlideReaderException
     */
    public float getMicronsPerPixel() throws VirtualSlideReaderException {
        // getAnnotations() checks for annotation file existance and virtual slide file type
        return getAnnotations().getMicronsPerPixel();
    }

    /**
     * preprocess annotation selections i.e. regions and random virtual tma
     * cores if selections do not exist ... do nothing
     *
     * @throws VirtualSlideReaderException
     */
    public void preprocessAnnotationSelections() throws VirtualSlideReaderException, RegionNotSupportedException {
        // NO NEED TO preprocess regions from original annotation

        // preprocess consolided regions
        extractAnnotationSelections(false, false);
        // preprocess random virtual tma cores
        if (getAnnotations().randomVirtualTmaCoresExist()) {
            for (Region r : getAnnotations().getRandomVirtualTmaCoresAnnotation().getRegions().getRegions()) {
                r.preprocess(ir.getSizeX(), ir.getSizeY());
            }
        }
    }

    /**
     * consolidate annotation selections i.e. regions
     *
     * @throws VirtualSlideReaderException
     */
    public void consolidateAnnotationSelections() throws VirtualSlideReaderException {
        extractAnnotationSelections(true, false);
    }

    /**
     * extract annotations selections
     *
     * @throws VirtualSlideReaderException
     */
    public void extractAnnotationSelections(boolean consolidate, boolean export) throws VirtualSlideReaderException {
        extractAndExportAnnotationSelection(ir, annotationFile, consolidate, export);
    }

    /**
     * extract selection regions found in annotation file consolidate
     * annotation!!!
     *
     * @param ir
     * @param annotationFile
     * @param consolidate
     * @param export
     * @throws VirtualSlideReaderException
     */
    private void extractAndExportAnnotationSelection(IFormatReader ir, File annotationFile, boolean consolidate, boolean export) throws VirtualSlideReaderException {
        // need to make folder for selection
        String exportDirectoryName = annotationFile.getParentFile().getPath()
                + System.getProperty("file.separator")
                + EXPORT_DIRECTORY_NAME;
        exportDirectory = new File(exportDirectoryName);
        if (export) {
            if (!exportDirectory.exists()) {
                // create directory
                if (!exportDirectory.mkdir()) {
                    throw new VirtualSlideReaderException("failed to create directory to store SVS exracted regions (" + exportDirectoryName + ")");
                }
            }
        }
        try {
            // make sure annotations exist
            if (annotations == null) {
                throw new VirtualSlideReaderException("missing annotation file");
            }
            float imageWidth = ir.getSizeX();
            float imageHeight = ir.getSizeY();
            Annotation annotation;
            int annotationCount = 1;
            if (consolidate) {
                // try consolidate region
                annotations.consolidateRegions();
            }
            // try to use consolidated annotation first ... if not found use original annotation
            if (annotations.consolidatedAnnotationExists()) {
                annotation = annotations.getConsolidatedAnnotation();
            } else {
                annotation = annotations.getOriginalAnnotation();
            }
            Iterator<Region> regionItr = annotation.getRegions().getRegions().iterator();
            int regionCount = 1;
            while (regionItr.hasNext()) {
                Region region = regionItr.next();
                region.preprocess(imageWidth, imageHeight);
                /////////////////////////////////
                /// export annotation to file ///
                if (export) {
                    // output file name
                    String outputFilename = exportDirectoryName
                            + System.getProperty("file.separator")
                            + virtualSlideFile.getName()
                            + "_a" + annotationCount + "r" + regionCount + ".tiff";

                    // imp for ROI
                    BufferedImageReader bir = new BufferedImageReader(ir);
                    ImagePlus imp = new ImagePlus(
                            "",
                            bir.openImage(
                                    0,
                                    region.getEnclosingRectangleX(), // x 
                                    region.getEnclosingRectangleY(), // y
                                    region.getEnclosingRectangleWidth(), // width
                                    region.getEnclosingRectangleHeight() // height
                            ));

                    // try do a ROI on the extract image ...
                    //PolygonRoi roi = new PolygonRoi(new FloatPolygon(new float[]{10f, 200f, 150f, 110f}, new float[]{10f, 200f, 5f, 60f}), Roi.FREEROI);
                    Roi roi = region.getRoi();
                    
                    imp.getProcessor().setColor(Color.WHITE);
                    imp.getProcessor().fillOutside(roi);
                    //imp.show();

                    FileSaver fs = new FileSaver(imp);
                    fs.saveAsTiff(outputFilename);
                    analyzableFiles.add(new File(outputFilename)); // add analyzable file
                }
                regionCount++;
            }
        } catch (IOException | RegionNotPreprocessedException | RegionNotSupportedException | FormatException ex) {
            throw new VirtualSlideReaderException(ex.toString());
        }
    }

    /**
     * get analyzable files
     *
     * @return
     */
    public ArrayList<File> getAnalyzableFiles() {
        return analyzableFiles;
    }

    /**
     * generate random virtual TMA cores
     *
     * NOTE: support only ONE set of random virtual TMA cores
     *
     * @param diameterInMicrometer
     * @param randomNumberGenerator
     * @param numOfCores
     * @throws VirtualSlideReaderException
     * @throws RegionNotPreprocessedException
     * @throws RegionNotSupportedException
     * @throws RegionGeneratorMaxTriesExceeded
     */
    public void generateRandomVirtualTmaCores(int diameterInMicrometer, Random randomNumberGenerator, int numOfCores) throws VirtualSlideReaderException, RegionNotPreprocessedException, RegionNotSupportedException, RegionGeneratorMaxTriesExceeded {
        annotations.generateRandomVirtualTmaCores(diameterInMicrometer, randomNumberGenerator, numOfCores);
    }

    /**
     * generate random virtual TMA cores
     *
     * NOTE: support only ONE set of random virtual TMA cores
     *
     * use default random number generator
     *
     * @param diameterInMicrometer
     * @param numOfCores
     * @throws VirtualSlideReaderException
     * @throws RegionNotPreprocessedException
     * @throws RegionNotSupportedException
     * @throws RegionGeneratorMaxTriesExceeded
     */
    public void generateRandomVirtualTmaCores(int diameterInMicrometer, int numOfCores) throws VirtualSlideReaderException, RegionNotPreprocessedException, RegionNotSupportedException, RegionGeneratorMaxTriesExceeded {
        annotations.generateRandomVirtualTmaCores(diameterInMicrometer, numOfCores);
    }

    /**
     * generate additional random virtual TMA cores and add it to the SAME
     * regions containing the existing random virtual TMA cores
     *
     * NOTE: random virtual tma cores must exist first!!!
     *
     * @param diameterInMicrometer
     * @param randomNumberGenerator
     * @param overlapCircleOk
     * @throws RegionNotPreprocessedException
     * @throws RegionNotSupportedException
     * @throws RegionGeneratorMaxTriesExceeded
     */
    public void generateAdditionalRandomVirtualTmaCore(
            int diameterInMicrometer,
            Random randomNumberGenerator,
            boolean overlapCircleOk) throws RegionNotPreprocessedException, RegionNotSupportedException, RegionGeneratorMaxTriesExceeded {
        annotations.generateAdditionalRandomVirtualTmaCore(diameterInMicrometer, randomNumberGenerator, overlapCircleOk);
    }

    /**
     * generate additional random virtual TMA cores and add it to the SAME
     * regions containing the existing random virtual TMA cores
     *
     * NOTE: random virtual tma cores must exist first!!!
     *
     * use default random number generator
     *
     * @param diameterInMicrometer
     * @param overlapCircleOk
     * @throws RegionNotPreprocessedException
     * @throws RegionNotSupportedException
     * @throws RegionGeneratorMaxTriesExceeded
     */
    public void generateAdditionalRandomVirtualTmaCore(
            int diameterInMicrometer,
            boolean overlapCircleOk) throws RegionNotPreprocessedException, RegionNotSupportedException, RegionGeneratorMaxTriesExceeded {
        annotations.generateAdditionalRandomVirtualTmaCore(diameterInMicrometer, overlapCircleOk);
    }

    /**
     * generate manually selected virtual tma cores based on the input field of fields
     * @param fovs 
     */
    public void generateManualSelectedVirtualTmaCore(ArrayList<FieldOfView> fovs) throws VirtualSlideReaderException, RegionNotSupportedException, RegionNotPreprocessedException {
        annotations.generateManuallySelectedVirtualTmaCores(fovs);
    }
    
    /**
     * get the region as BufferedImage
     *
     * @param region
     * @return
     * @throws FormatException
     * @throws RegionNotPreprocessedException
     * @throws IOException
     * @throws RegionNotSupportedException
     */
    public BufferedImage getRegionAsBufferedImage(Region region) throws FormatException, RegionNotPreprocessedException, IOException, RegionNotSupportedException {
        return region.getBufferedImage(ir);
    }

    /**
     * export annotations to file
     *
     * @param filename
     * @throws JAXBException
     */
    public void exportAnnotationsToFile(String filename) throws JAXBException {
        annotations.exportToFile(filename);
    }
}
