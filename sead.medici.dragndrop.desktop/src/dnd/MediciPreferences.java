/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dnd;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Ram
 */
public class MediciPreferences {

    private String _serverName = "";

    private MediciPreferences() {
    }
    private static MediciPreferences _instance = null;

    /**
     * @return the _instance
     */
    public static MediciPreferences getInstance() {

        if (_instance == null) {
            _instance = new MediciPreferences();
        }
        return _instance;
    }
    private String _propertyFilePath = "";

    Properties getProperties() {
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream("MediciPreferences.properties"));
            _serverName = properties.getProperty("server");
            _serverName = _serverName.endsWith("/") ? _serverName : _serverName + "/";
            String appDataKeyWord = properties.getProperty("appdatakeyword");
            File appdatapath = new File(System.getenv(appDataKeyWord));
            String seadBoxFolderName = properties.getProperty("seadboxfoldername");
            File seadFolder = new File(appdatapath, seadBoxFolderName);

            if (!seadFolder.exists()) {
                seadFolder.mkdir();
            }
            String preferencesFileName = properties.getProperty("preferencesfilename");



            File preferencesFile = new File(seadFolder, preferencesFileName);

            _propertyFilePath = preferencesFile.getPath();

            if (!preferencesFile.exists()) {
                preferencesFile.createNewFile();
            }
            properties.clear();
            properties.load(new FileInputStream(getPropertyFilePath()));

        } catch (IOException ex) {
            Logger.getLogger(LoginForm.class.getName()).log(Level.SEVERE, null, ex);
        }
        return properties;
    }

    /**
     * @return the _serverName
     */
    public String getServerName() {
        return _serverName;
    }

    void storeProperties(Properties _properties) {
        try {
            _properties.store(new FileOutputStream(_propertyFilePath), null);
        } catch (IOException ex) {
            Logger.getLogger(MediciPreferences.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * @return the propertyFilePath
     */
    public String getPropertyFilePath() {
        return _propertyFilePath;
    }
}
