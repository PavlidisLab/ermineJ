package classScore.gui.details;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.HeadlessException;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Institution:: Columbia University</p>
 * @author not attributable
 * @version 1.0
 */

public class JSaveOptions extends JComponent {

   String[] options1 = {
                       "Include row and column labels",
                       "Do not include labels",
                       "Include row labels only",
                       "Include column labels only"
   };
   JComboBox jComboBox1 = new JComboBox(options1);

   String[] options2 = {
                       "Normalize to variance 1, mean 0",
                       "Do not normalize"
   };
   JComboBox jComboBox2 = new JComboBox(options2);

   GridLayout gridLayout1 = new GridLayout(12, 2);
   JLabel jLabel4 = new JLabel();
   JLabel jLabel5 = new JLabel();

   public JSaveOptions() throws HeadlessException {
      try {
         jbInit();
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   private void jbInit() throws Exception {
      setLayout(gridLayout1);
      this.setMaximumSize(new Dimension(264, 63));
      this.setMinimumSize(new Dimension(264, 63));
      jLabel4.setHorizontalAlignment(SwingConstants.CENTER);
      jLabel4.setText("Save Options:");
      jLabel5.setText("");
      gridLayout1.setColumns(2);
      gridLayout1.setHgap(5);
      gridLayout1.setRows(9);
      gridLayout1.setVgap(5);
      jComboBox1.setAlignmentY((float) 0.5);
      this.add(jLabel5, null);
      this.add(jLabel4, null);
      this.add(jComboBox1, null);
      this.add(jComboBox2, null);
   }

}
