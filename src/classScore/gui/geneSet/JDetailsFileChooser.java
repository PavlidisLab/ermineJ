/*
 * JDetailsFileChooser.java
 *
 * Created on June 19, 2004, 1:47 PM
 */

package classScore.gui.geneSet;

// for JDetailsFileChooser
import javax.swing.JFileChooser;
import java.awt.Component;
import java.awt.HeadlessException;

// for JDetailsFileChooserOptions (the file chooser accessory)
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.HeadlessException;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

/**
 *
 * @author  Will Braynen
 */
public class JDetailsFileChooser extends JFileChooser {
   
   
   // fields
   JDetailsFileChooserOptions m_options;
   
   
   /** Creates a new instance of JDetailsFileChooser */
   public JDetailsFileChooser() {

      super();
      
      // Create a file filter for the file chooser
      ImageFileFilter imageFileFilter = new ImageFileFilter();
      super.setFileFilter( imageFileFilter );
      super.setAcceptAllFileFilterUsed( false );

      // Create other save options (e.g. include row and column labels in image)
      m_options = new JDetailsFileChooserOptions();
      super.setAccessory( m_options );
   }
   
   
   public boolean normalized() {
      return m_options.normalized();
   }
   
   
   public boolean includeLabels() {
      return m_options.includeLabels();
   }
} // end class JDetailsFileChooser


/**
 * The accessory component for the file chooser.<p>
 *
 * Lets the user change save options and might also in the future include
 * a preview of what the matrix image will look like with the chosen options.
 */
class JDetailsFileChooserOptions extends JComponent {

   String[] m_includeLabelsOptions = {
                       "Include row and column labels",
                       "Do not include labels"
   };
   JComboBox m_includeLabelsComboBox = new JComboBox( m_includeLabelsOptions );

   String[] m_normalizeOptions = {
                       "Normalize to variance 1, mean 0",
                       "Do not normalize"
   };
   JComboBox m_normalizeComboBox = new JComboBox( m_normalizeOptions );

   GridLayout gridLayout1 = new GridLayout(12, 2);
   JLabel m_titleLabel = new JLabel();
   JLabel m_spacerLabel = new JLabel();

   public JDetailsFileChooserOptions() throws HeadlessException {
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
      m_titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
      m_titleLabel.setText("Save Options:");
      m_spacerLabel.setText("");
      gridLayout1.setColumns(2);
      gridLayout1.setHgap(5);
      gridLayout1.setRows(9);
      gridLayout1.setVgap(5);
      this.add(m_spacerLabel, null);
      this.add(m_titleLabel, null);
      this.add(m_includeLabelsComboBox, null);
      this.add(m_normalizeComboBox, null);
   }

   public boolean normalized() {
      return ( 0 == m_normalizeComboBox.getSelectedIndex() );
   }
   
   public boolean includeLabels() {
      return ( 0 == m_includeLabelsComboBox.getSelectedIndex() );
   }
} // end JDetailsFileChooserOptions
