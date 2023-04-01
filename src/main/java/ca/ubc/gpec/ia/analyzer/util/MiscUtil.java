/*
 * some misc utility functions ...
 */
package ca.ubc.gpec.ia.analyzer.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 *
 * @author samuelc
 */
public class MiscUtil {

    /**
     * change URL character back to "regular" character ... e.g. "%20" to " "
     * (space)
     *
     * @param input
     * @return
     */
    public static String revertUrlSpecialCharacterEncoding(String input) {
        String result = input.replaceAll("%20", " ");
        return result;
    }

    /**
     * path to url e.g. "C:/abc/abc.jpg" => "file:///C:/abc/abc.jpg" note: the
     * file "input" refers to does not need to exist
     *
     * @param input
     * @return
     */
    public static String pathToUrl(String input) throws MalformedURLException {
        return revertUrlSpecialCharacterEncoding(((new File(input)).toURI().toURL()).toExternalForm().replace("file:/", "file:///"));
    }

    /**
     * from input stream, read all text and return the content as String
     *
     * @param is
     * @return
     */
    public static String readTextFileContent(InputStream is) throws IOException {
        String result = "";
        BufferedReader r = new BufferedReader(new InputStreamReader(is));
        String nextLine = r.readLine();
        while (nextLine != null) {
            result = result + nextLine;
            nextLine = r.readLine();
        }
        return result;
    }

    /*
     * Calculate checksum of a File using MD5 algorithm
     * Reference: http://javarevisited.blogspot.ca/2013/06/how-to-generate-md5-checksum-for-files.html
     */
    public static String md5Sum(String path) throws IOException, NoSuchAlgorithmException {
        return md5Sum(new File(path));
    }

    /*
     * Calculate checksum of a File using MD5 algorithm
     * Reference: http://javarevisited.blogspot.ca/2013/06/how-to-generate-md5-checksum-for-files.html
     */
    public static String md5Sum(File file) throws IOException, NoSuchAlgorithmException {
        String checksum = null;
        FileInputStream fis = new FileInputStream(file);
        MessageDigest md = MessageDigest.getInstance("MD5");

        //Using MessageDigest update() method to provide input
        byte[] buffer = new byte[8192];
        int numOfBytesRead;
        while ((numOfBytesRead = fis.read(buffer)) > 0) {
            md.update(buffer, 0, numOfBytesRead);
        }
        byte[] hash = md.digest();
        checksum = new BigInteger(1, hash).toString(16); //don't use this, truncates leading zero

        return checksum;
    }

}
