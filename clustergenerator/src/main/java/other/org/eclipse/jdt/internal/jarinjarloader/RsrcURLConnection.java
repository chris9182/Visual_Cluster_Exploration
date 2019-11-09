package other.org.eclipse.jdt.internal.jarinjarloader;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;

//from https://www.cs.umb.edu/~smimarog/diclens/
//modified
public class RsrcURLConnection extends URLConnection {
	private final ClassLoader classLoader;

	public RsrcURLConnection(final URL url, final ClassLoader classLoader) {
		super(url);
		this.classLoader = classLoader;
	}

	@Override
	public void connect() throws IOException {
	}

	@Override
	public InputStream getInputStream() throws IOException {
		final String file = URLDecoder.decode(super.url.getFile(), "UTF-8");
		final InputStream result = this.classLoader.getResourceAsStream(file);
		if (result == null) {
			throw new MalformedURLException("Could not open InputStream for URL '" + super.url + "'");
		}
		return result;
	}
}
