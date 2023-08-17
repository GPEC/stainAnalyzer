# stainAnalyzer

This software assists pathologist score Ki67 IHC on whole section.

Please see Nielsen et al. Histopathology 2023 for a detail description of how this software was used in the LUMINA clinical trial.

StainAnayzer is built using the JavaFX platform.  It relies on two external libraries for image processing: OME's Bio-Formats (https://www.openmicroscopy.org/) and ImageJ (https://imagej.net/)

This GitHub repository contains the source code of StainAnalyzer.

A packaged Windows installer is available in the "build" folder.

# User Guide

Prerequisite: the Ki67 whole section slide must be scanned by a scanner that produces Aperio svs file.

To score Ki67 using the stainAnalyzer:

1. Annotate tumor area using Aperio ImageScope (https://www.leicabiosystems.com/en-ca/digital-pathology/manage/aperio-imagescope/)

    - The tumor area can be specified using the "Pen Tool" (free hand), "Rectangle Tool" or "Eclipse Tool".  When using the "Pen Tool", please be sure that the region closes off (i.e. the line front and end join together.  If the front and end does not join, stainAnalyzer will not be able to identify the selected region.)
    - Multiple tumor areas can be selected.  However, the tumor area must be large enough to allow virtual core (1-mm x 1-mm square) to be selected.
    - Please save the annotation (xml file).  Please use the default name i.e. [name of image file].xml.  For example, the default annotation file name for "test.svs" is "test.xml"

2. Execute stainAnalyzer and please follow the instructions presented.

# misc notes

## Compile tips

Use Maven, keep all javafx module version to be the same


## Executing on Intellij (or other IDE) 

### need the following run configuration
--module-path
"C:\Program Files\JavaFX\javafx-sdk-17.0.2\lib"
--add-modules=javafx.controls,javafx.swing,javafx.graphics,javafx.fxml,javafx.media,javafx.web
--add-reads
javafx.graphics=ALL-UNNAMED
--add-opens
javafx.controls/com.sun.javafx.charts=ALL-UNNAMED
--add-opens
javafx.graphics/com.sun.javafx.iio=ALL-UNNAMED
--add-opens
javafx.graphics/com.sun.javafx.iio.common=ALL-UNNAMED
--add-opens
javafx.graphics/com.sun.javafx.css=ALL-UNNAMED
--add-opens
javafx.base/com.sun.javafx.runtime=ALL-UNNAMED

