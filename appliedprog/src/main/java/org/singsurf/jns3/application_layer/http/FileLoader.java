package org.singsurf.jns3.application_layer.http;

import java.io.InputStream;

/**
 * Interface for loading files requested by the HTTP server.
 */
public interface FileLoader {
	/**
	 * Load a file from the given path.
	 * @param path The path to the file to load.
	 * @return An InputStream for reading the file, or null if the file does not exist.
	 */
	InputStream loadFile(String path);
}
