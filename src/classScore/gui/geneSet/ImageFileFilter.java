   /*
    * ImageFileFilter.java
    *
    * Created on June 19, 2004, 10:47 AM
    */

package classScore.gui.details;

import javax.swing.filechooser.FileFilter;
import java.io.File;
import baseCode.graphics.text.Util;

/**
 *
 * @author  Will Braynen
 */
public class ImageFileFilter extends FileFilter {

   
   public boolean accept( File f ) {
      
      if ( f.isDirectory() ) {
         return true;
      }
      
     return Util.hasImageExtension( f.getName() );

   } // end accept
   
   
   public String getDescription() {
      
      return "PNG or GIF images";
   }   
}
