import javax.swing.filechooser.*;
import java.io.File;

public class SAVFilter extends FileFilter {

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
    /**@todo: implement this javax.swing.filechooser.FileFilter abstract method*/
        if (f.isDirectory()) {
            return true;
        }

        String extension = getExtension(f);
        if (extension != null) {
            if (extension.equals("sav")) {
                    return true;
            } else {
                return false;
            }
        }
        return false;
  }
  public String getDescription() {
    /**@todo: implement this javax.swing.filechooser.FileFilter abstract method*/
    return "LSDj .SAV File";
  }
}
