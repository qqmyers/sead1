package org.sead.acr.common.utilities;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesLoader {

	static Properties _properties = null;
	static String _propfile = "nced.properties";

	/**
	 * @return the _properties
	 */
	public static Properties getProperties() {
		return getProperties(null);
	}
	
	public static Properties getProperties(String thePropfile) {
		if (_properties == null) {
			try {
				if(thePropfile!= null) {
					_propfile=thePropfile;
				}
				new PropertiesLoader().loadProperties();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return _properties;
	}

	public void loadProperties() throws IOException {
		InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(_propfile);
		_properties = new Properties();

		// load the inputStream using the Properties
		_properties.load(inputStream);

		inputStream.close();
	}

}
