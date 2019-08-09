package clusterproject.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;

public class HackedObjectInputStream extends ObjectInputStream {

	public HackedObjectInputStream(InputStream in) throws IOException {
		super(in);
	}

	@Override
	protected ObjectStreamClass readClassDescriptor() throws IOException, ClassNotFoundException {
		// XXX currently disabled may be used in the future for compability
		if (true)
			return super.readClassDescriptor();
		ObjectStreamClass resultClassDescriptor = super.readClassDescriptor();

		if (resultClassDescriptor.getName().contains("clusterproject.clustergenerator"))
			resultClassDescriptor = ObjectStreamClass.lookup(Class.forName(
					resultClassDescriptor.getName().replaceAll("clusterproject.clustergenerator", "clusterproject")));

		return resultClassDescriptor;
	}
}
