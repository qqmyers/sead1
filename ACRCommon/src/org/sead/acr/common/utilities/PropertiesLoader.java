package org.sead.acr.common.utilities;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import sun.util.logging.resources.logging;

public class PropertiesLoader {

	static Properties _properties = null;
	static String _propfile = "commons.properties";

	/**
	 * @return the _properties
	 */
	public static Properties getProperties() {
		return getProperties(null);
	}

	public static Properties getProperties(String thePropfile) {
		if (_properties == null) {
			try {
				if (thePropfile != null) {
					_propfile = thePropfile;
				}
				new PropertiesLoader().loadProperties();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return _properties;
	}

	public void loadProperties() throws IOException {
		InputStream inputStream = this.getClass().getClassLoader()
				.getResourceAsStream(_propfile);
		_properties = new Properties();

		// load the inputStream using the Properties
		if (inputStream != null) {
			_properties.load(inputStream);

			inputStream.close();
		} else {
			throw new IOException("Properties file can't be read");
		}
	}

}
