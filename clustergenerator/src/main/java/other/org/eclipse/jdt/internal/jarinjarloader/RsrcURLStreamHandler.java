package other.org.eclipse.jdt.internal.jarinjarloader;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

//from https://www.cs.umb.edu/~smimarog/diclens/
//modified
public class RsrcURLStreamHandler extends URLStreamHandler {
	private final ClassLoader classLoader;

	public RsrcURLStreamHandler(final ClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	@Override
	protected URLConnection openConnection(final URL u) throws IOException {
		return new RsrcURLConnection(u, this.classLoader);
	}

	@Override
	protected void parseURL(final URL url, final String spec, final int start, final int limit) {
		String file;
		if (spec.startsWith("rsrc:")) {
			file = spec.substring(5);
		} else if (url.getFile().equals("./")) {
			file = spec;
		} else if (url.getFile().endsWith("/")) {
			file = String.valueOf(url.getFile()) + spec;
		} else {
			file = spec;
		}
		this.setURL(url, "rsrc", "", -1, null, null, file, null, null);
	}
}
