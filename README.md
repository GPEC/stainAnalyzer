# stainAnalyzer

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

