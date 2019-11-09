package other.dm.common;

import java.util.Iterator;

//from https://www.cs.umb.edu/~smimarog/diclens/
//modified
public class EasyFileIterator implements Iterator<String> {
	private final EasyFileReader reader;
	private String nextLine;

	public EasyFileIterator(final EasyFileReader reader) {
		this.reader = reader;
		this.nextLine = reader.readALine();
	}

	@Override
	public boolean hasNext() {
		return this.nextLine != null;
	}

	@Override
	public String next() {
		final String currentLine = this.nextLine;
		if (currentLine != null) {
			this.nextLine = this.reader.readALine();
		}
		return currentLine;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
}
