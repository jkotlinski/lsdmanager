import javax.swing.filechooser.*;
import java.io.File;

public class LsdSngFilter extends FileFilter {
	private String getExtension(File f) {
		String ext = null;
		String s = f.getName();
		int i = s.lastIndexOf('.');

		if (i > 0 &&  i < s.length() - 1) {
			ext = s.substring(i+1).toLowerCase();
		}
		return ext;
	}

	public boolean accept(File f) {
		if (f.isDirectory()) {
			return true;
		}

		String extension = getExtension(f);
		return extension != null ? extension.equals("lsdsng") : false;
	}

	public String getDescription() {
		return "LSDj compressed song file";
	}
}
