/*
 * describe the image transformation
 */
package ca.ubc.gpec.ia.analyzer.model;

import ca.ubc.gpec.ia.analyzer.transformation.DataNotFoundImageTransformationException;
import ca.ubc.gpec.ia.analyzer.transformation.ImageTransformationException;
import ca.ubc.gpec.ia.analyzer.transformation.ParameterNotFoundImageTransformationException;
import java.net.MalformedURLException;
import java.util.TreeSet;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author samuelc
 */
@XmlRootElement(name = "imageTransformation")
public class ImageTransformation implements Comparable {

    public static final String IMAGE_TRANSFORMATION_NOT_SET = "not set";
    protected String name; // java class name
    protected TreeSet<ImageTransformationParameter> parameters;
    protected TreeSet<ImageTransformationData> data;

    /**
     * for Comparable interface
     *
     * @param other
     * @return
     */
    @Override
    public int compareTo(Object other) {
        return name.compareTo(((ImageTransformation) other).getName());
    }

    @Override
    public ImageTransformation clone() {
        ImageTransformation clone = new ImageTransformation();
        clone.setName(name);
        for (ImageTransformationParameter parameter : parameters) {
            clone.getParameters().add(parameter);
        }
        for (ImageTransformationData datum : data) {
            clone.getData().add(datum);
        }
        return clone;
    }
    
    /**
     * setter
     *
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * getter
     *
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * setter
     *
     * @param parameters
     */
    public void setParameters(TreeSet<ImageTransformationParameter> parameters) {
        this.parameters = parameters;
    }

    /**
     * getter
     *
     * @return
     */
    public TreeSet<ImageTransformationParameter> getParameters() {
        return parameters;
    }

    /**
     * setter
     * @param data 
     */
    public void setData(TreeSet<ImageTransformationData> data) {
        this.data = data;
    }
    
    /**
     * getter
     * @return 
     */
    public TreeSet<ImageTransformationData> getData() {
        return data;
    }
    
    /**
     * constructor
     *
     * @param name
     */
    public ImageTransformation() {
        this.name = null;
        parameters = new TreeSet<ImageTransformationParameter>();
        data = new TreeSet<ImageTransformationData>();
    }

    /**
     *
     * @param paramName
     * @return
     * @throws TransformationParameterNotFoundException
     */
    public String getParameterValue(String paramName) throws ParameterNotFoundImageTransformationException {
        for (ImageTransformationParameter param : parameters) {
            if (param.getName().equals(paramName)) {
                return param.getValue();
            }
        }
        throw new ParameterNotFoundImageTransformationException("parameter (" + paramName + ") not found.");
    }

    public Object getDataValue(String dataName) throws DataNotFoundImageTransformationException {
        for (ImageTransformationData d : data) {
            if (d.getName().equals(dataName)) {
                return d.getValue();
            }
        }
        throw new DataNotFoundImageTransformationException("data (" + dataName + ") not found.");
    }

    /**
     * apply this image transformation to IAO
     *
     * @param iao
     * @return the IAO containing the result images. This IAO is linked to the
     * input IAO
     */
    public IAO apply(IAO iao) throws ImageTransformationException, MalformedURLException{
        return null; // based class do nothing
    }
}
