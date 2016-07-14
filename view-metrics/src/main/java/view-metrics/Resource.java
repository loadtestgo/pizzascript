package webshared;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Class for reading resources
 *
 * You can always do load directly from the class loader as well:
 *      webshared.Resource.class.getClassLoader().getResource();
 */
public class Resource {
    public static String getDevPath() {
        File f = new File(System.getProperty("user.dir"));
        return f.getParent() + "/web-shared/src/main/resources/";
    }

    public static URL getResource(boolean isDev, String filePath) {
        if (isDev) {
            File file = new File(getDevPath(), filePath);
            if (file.exists()) {
                URL url = getUrlForFile(file);
                if (url != null) {
                    return url;
                }
            }
        }

        return Resource.class.getClassLoader().getResource(filePath);
    }

    static private URL getUrlForFile(File file) {
        try {
            return file.toURI().toURL();
        } catch(MalformedURLException malformedURLException) {
        }
        return null;
    }
}
