package other.dm.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;

//from https://www.cs.umb.edu/~smimarog/diclens/
//modified
public class EasyFileReader implements Iterable<String> {
	private BufferedReader reader;

	public String readALine() {
		String line = null;
		try {
			line = this.reader.readLine();
		} catch (final IOException e) {
			throw new IllegalStateException(e);
		}
		return line;
	}

	public EasyFileReader(final String fileName) {
		final File file = new File(fileName);
		try {
			final Reader in = new FileReader(file);
			this.reader = new BufferedReader(in);
		} catch (final FileNotFoundException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public Iterator<String> iterator() {
		return new EasyFileIterator(this);
	}
}
