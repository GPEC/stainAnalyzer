/*
 * Image Analysis Object (IAO)
 */
package ca.ubc.gpec.ia.analyzer.model;

import ca.ubc.gpec.ia.analyzer.transformation.ImageNotFoundImageTransformationException;
import ca.ubc.gpec.ia.analyzer.transformation.ImageTransformationException;
import ca.ubc.gpec.ia.analyzer.transformation.NullImageTransformation;
import ca.ubc.gpec.ia.analyzer.util.MiscUtil;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.TreeSet;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlRootElement;
import org.apache.commons.io.FilenameUtils;

/**
 *
 * @author samuelc
 */
@XmlRootElement(namespace = "ca.ubc.gpec.imagej.analyzer.model")
public class IAO implements Comparable {

    public static final boolean DEBUG = true;
    public static final String EXPORT_IAO_IMAGE_DESCRIPTOR_SEPARATOR = "___";
    private ImageTransformation imageTransformation; // final (finished analyses) image will have imageTransformation = null;
    private TreeSet<IAO> iaos; // analyses objects
    private TreeSet<ImageDescriptor> imageDescriptors; // analyzed/original result image(s)

    /**
     * for Comparable interface
     *
     * @param other
     * @return
     */
    @Override
    public int compareTo(Object other) {
        IAO otherIAO = (IAO) other;
        // currently only look at FIRST image
        // also, ASSUME that there is AT LEAST ONE image in any IAO object!!!
        int compareByFirstImage = imageDescriptors.first().compareTo(otherIAO.getImageDescriptors().first());
        return compareByFirstImage != 0 ? compareByFirstImage : imageTransformation.compareTo(otherIAO.getImageTransformation());
    }

    /**
     * make a value copy of this IAO ... WARNING!!! right after clone (i.e.
     * before any transformation done), compareTo with original IAO will return
     * 0 (i.e. equal)
     *
     * @return
     */
    @Override
    public IAO clone() {
        IAO clone = new IAO();
        // copy iaos ...
        for (IAO iao : iaos) {
            clone.getIaos().add(iao.clone());
        }
        // copy imageDescriptors ...
        for (ImageDescriptor imageDescriptor : imageDescriptors) {
            clone.getImageDescriptors().add(imageDescriptor.clone());
        }
        // copy imageTransformation
        clone.setImageTransformation(imageTransformation.clone());
        return clone;
    }

    /**
     * setter - for JAXB use
     *
     * @param imageTransformation
     */
    public void setImageTransformation(ImageTransformation imageTransformation) {
        this.imageTransformation = imageTransformation;
    }

    /**
     * getter - for JAXB use
     *
     * @return
     */
    public ImageTransformation getImageTransformation() {
        return imageTransformation;
    }

    /**
     * setter
     *
     * @param analysis
     */
    public void setIaos(TreeSet<IAO> iaos) {
        this.iaos = iaos;
    }

    /**
     * add an IAO to this IAO - if the new iao already exist in iaos, there will
     * be no side effect since iaos is a Set.
     *
     * @param iao
     */
    public void addIao(IAO iao) {
        iaos.add(iao);
    }

    /**
     * get the analysis that is done to the image attached to this IAO
     *
     * @return
     */
    public TreeSet<IAO> getIaos() {
        return iaos;
    }

    /**
     * setter
     *
     * @param imageDescriptors
     */
    public void setImageDescriptors(TreeSet<ImageDescriptor> imageDescriptors) {
        this.imageDescriptors = imageDescriptors;
    }

    /**
     * add an imageDescriptor to imageDescriptors - if imageDescriptor already
     * exist in imageDescriptors, there will be no side-effects since
     * imageDescriptors is a Set.
     *
     * @param imageDescriptor
     */
    public void addImageDescriptor(ImageDescriptor imageDescriptor) {
        imageDescriptors.add(imageDescriptor);
    }

    /**
     * getter
     *
     * @return
     */
    public TreeSet<ImageDescriptor> getImageDescriptors() {
        return imageDescriptors;
    }

    /**
     * constructor
     */
    public IAO() {
        imageTransformation = new NullImageTransformation();
        iaos = new TreeSet<IAO>();
        imageDescriptors = new TreeSet<ImageDescriptor>();
    }

    /**
     * generate the export xml file name - the problem is if there's > 1
     * original file
     *
     * @return
     */
    private String generateIAOXmlFilename() throws IAOExportException, MalformedURLException {
        String filename = null;
        if (imageDescriptors.isEmpty()) {
            throw new IAOExportException("no image attached to this IAO");
        } else if (imageDescriptors.size() == 1) {
            String urlText = imageDescriptors.first().getUrl();
            URL url = new URL(urlText);
            filename = url.getFile();
        } else {
            // there's more than one file
            Iterator<ImageDescriptor> itr = imageDescriptors.iterator();
            filename = FilenameUtils.removeExtension((new URL(itr.next().getUrl())).getFile());
            while (itr.hasNext()) {
                // find file name
                filename = filename + EXPORT_IAO_IMAGE_DESCRIPTOR_SEPARATOR + FilenameUtils.removeExtension(FilenameUtils.getName((new URL(itr.next().getUrl())).getFile()));
            }
        }
        return addIAOXmlExtension(MiscUtil.revertUrlSpecialCharacterEncoding(filename));
    }

    /**
     * export IAO to xml file - this method will ONLY work if the url
     * corresponds to a local file - the xml file will be [filename]_iao.xml
     * e.g. /tmp/abc.jpg => /tmp/abc_iao.xml
     */
    public void exportToFile() throws IAOExportException, MalformedURLException, JAXBException, IOException {
        URL url = new URL(imageDescriptors.first().getUrl());
        if (!url.getProtocol().equals("file")) {
            throw new IAOExportException("local filename not specified");
        }
        exportToFile(generateIAOXmlFilename());
    }

    private String addIAOXmlExtension(String input) {
        //return input+".xml";
        return FilenameUtils.removeExtension(input) + "_iao.xml";
    }

    /**
     * export IAO to xml file - specifying file name to export to
     *
     * @param filename
     * @throws JAXBException
     * @throws IOException
     */
    public void exportToFile(String filename) throws JAXBException, IOException {
        // create JAXB context and instantiate marshaller
        JAXBContext context = JAXBContext.newInstance(IAO.class);
        Marshaller m = context.createMarshaller();
        Writer w = new FileWriter(filename);
        if (DEBUG) {
            System.out.println("writing file " + filename + " ...");
        }
        m.marshal(this, w);
        w.flush();
        w.close();
    }

    /**
     * set and apply the image transformation - this will apply the
     * transformation to ALL images attached to this IAO - return the result IAO
     * i.e. the IAO containing the result image(s)
     *
     * @param imageTransformation
     * @return
     * @throws TransformationException
     * @throws MalformedURLException
     */
    public IAO setAndApplyImageTransformation(ImageTransformation imageTransformation) throws ImageTransformationException, MalformedURLException {
        setImageTransformation(imageTransformation);
        return imageTransformation.apply(this);
    }

    /**
     * set and apply the image transformation to the specified image
     *
     * @param parentIao
     * @param imageTransformation
     * @param id
     * @return
     * @throws TransformationException
     * @throws MalformedURLException
     */
    public IAO setAndApplyImageTransformation(IAO parentIao, ImageTransformation imageTransformation, TreeSet<ImageDescriptor> ids) throws ImageTransformationException, MalformedURLException {
        IAO childIao = new IAO();
        for (ImageDescriptor id : ids) {
            // check to see if id is attached directly to this IAO
            if (imageDescriptors.contains(id)) {
                // attached directly ... just attached id to the newly created IAO
                childIao.addImageDescriptor(id);
                imageDescriptors.remove(id);
            } else {
                // not attached directly ... search for the image among IAO's attached directly to
                // this IAO
                boolean imageFound = false;
                for (IAO checkIao : iaos) {
                    if (checkIao.imageDescriptors.contains(id)) {
                        childIao.addImageDescriptor(id);
                        imageFound = true;
                        break;
                        // DO NOT remove id from exist checkIao.imageDescriptors
                        // since this image is involved in MORE THAN ONE transformaiton
                    }
                }
                if (!imageFound) { // specified image not found
                    throw new ImageNotFoundImageTransformationException("setAndApplyImageTransformation: specified image not found: " + id);
                }
            }
        }
        parentIao.addIao(childIao);
        // check if this IAO do not have any more image atached to it, remove it from parent.
        // this node will become orphan and hopefully it will be garbage collected
        if (imageDescriptors.isEmpty()) {
            parentIao.getIaos().remove(this);
        }
        return childIao.setAndApplyImageTransformation(imageTransformation);
    }

    /**
     * convenient method with only one ImageDescriptor as input
     *
     * @param parentIao
     * @param imageTransformation
     * @param id
     * @return
     * @throws ImageTransformationException
     * @throws MalformedURLException
     */
    public IAO setAndApplyImageTransformation(IAO parentIao, ImageTransformation imageTransformation, ImageDescriptor id) throws ImageTransformationException, MalformedURLException {
        TreeSet<ImageDescriptor> ids = new TreeSet<ImageDescriptor>();
        ids.add(id);
        return setAndApplyImageTransformation(parentIao, imageTransformation, ids);
    }

    /**
     * main method for process testing
     *
     * @param args
     */
    public static void main(String[] args) {
        System.out.println("process test IAO ...");

        System.out.println(0xff);
        System.out.println(0xff & 1);
        System.out.println(0xff & 2);
        System.out.println(0xff & 3);
        System.out.println(0xff & 4);

        IAO iao = new IAO();
        iao.addImageDescriptor(new ImageDescriptor("file://abc.jpeg"));

        IAO childIao1 = new IAO();
        childIao1.addImageDescriptor(new ImageDescriptor("file://abc-R.jpeg"));
        childIao1.addImageDescriptor(new ImageDescriptor("file://abc-G.jpeg"));
        childIao1.setImageTransformation(new NullImageTransformation());

        IAO childIao2 = new IAO();
        childIao2.addImageDescriptor(new ImageDescriptor("file://abc-B.jpeg"));
        childIao2.setImageTransformation(new NullImageTransformation());

        IAO grandChildIao = new IAO();
        grandChildIao.addImageDescriptor(new ImageDescriptor("file://abc-R-G.jpeg"));
        grandChildIao.addImageDescriptor(new ImageDescriptor("file://abc-G.jpeg"));

        childIao1.addIao(grandChildIao);

        iao.addIao(childIao1);
        iao.addIao(childIao2);
        iao.setImageTransformation(new NullImageTransformation());

        try {
            // create JAXB context and instantiate marshaller
            JAXBContext context = JAXBContext.newInstance(IAO.class);
            Marshaller m = context.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            m.marshal(iao, System.out);

            String ouputFilename = "/home/samuelc/Documents/VMware Shared/NetBeansProjects_OUTPUT/imagej/test_iao.xml";
            Writer w = new FileWriter(ouputFilename);
            m.marshal(iao, w);

            // get variables from our xml file, created before
            System.out.println();
            System.out.println("Output from our XML File: ");
            Unmarshaller um = context.createUnmarshaller();
            IAO iao2 = (IAO) um.unmarshal(new FileReader(ouputFilename));
            System.out.println("1st level transformation: " + iao2.getImageTransformation().getName());
            Iterator<IAO> iaoItr = iao2.getIaos().iterator();
            while (iaoItr.hasNext()) {
                IAO nextIao = iaoItr.next();
                System.out.println("2nd level transformation: " + nextIao.getImageTransformation().getName());
            }


        } catch (JAXBException jaxbe) {
            System.err.println(jaxbe);
        } catch (IOException jaxbe) {

            System.err.println(jaxbe);
        }



    }
}
