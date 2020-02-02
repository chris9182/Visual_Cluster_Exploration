package other.fileFilter;

import javax.swing.filechooser.FileNameExtensionFilter;

public class FileFilter {
	public static final FileNameExtensionFilter csvfilter = new FileNameExtensionFilter(
			"single Clustering Result (csv)", "csv");
	public static final FileNameExtensionFilter cwffilter = new FileNameExtensionFilter(
			"Clustering Workflow Files (cwf)", "cwf");
	public static final FileNameExtensionFilter crffilter = new FileNameExtensionFilter("Clustering Result Files (crf)",
			"crf");
}
