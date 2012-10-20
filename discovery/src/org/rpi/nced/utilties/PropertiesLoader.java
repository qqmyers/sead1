package org.rpi.nced.utilties;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesLoader {

	static Properties _properties = null;

	/**
	 * @return the _properties
	 */
	public static Properties getProperties() {
		return _properties;
	}

	public void loadProperties() throws IOException {
		InputStream inputStream = this.getClass().getClassLoader()
				.getResourceAsStream("nced.properties");

		_properties = new Properties();

		// load the inputStream using the Properties
		_properties.load(inputStream);

		inputStream.close();
	}

}
