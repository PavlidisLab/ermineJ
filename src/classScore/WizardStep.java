package classScore;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.AbstractTableModel;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

abstract class WizardStep extends JPanel{
   public WizardStep(Wizard wiz) {
      super();
      try {
         jbInit();
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   //Component initialization
   abstract void jbInit() throws Exception;

   private boolean testfile(String filename) {
      if (filename != null && filename.length() > 0) {
         File f = new File(filename);
         if (f.exists()) {
            return true;
         } else {
            JOptionPane.showMessageDialog(null,
                                          "File " + filename +
                                          " doesn't exist.  ");
         }
         return false;
      } else {
         JOptionPane.showMessageDialog(null, "A required file field is blank.");
         return false;
      }
   }

}

