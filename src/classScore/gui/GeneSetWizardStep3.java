package classScore.gui;

import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.text.*;
import baseCode.gui.*;
import classScore.*;
import classScore.data.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: </p>
 * @author not attributable
 * @version $Id$
 */

public class GeneSetWizardStep3 extends WizardStep
{
   GeneSetWizard wiz;
   Settings settings;
   GeneAnnotations geneData;
   NewGeneSet newGeneSet;
   boolean makenew;

   JLabel classIDFinal;
   JLabel classDescL;
   JTextField classIDTF;
   JTextArea classDescTA;
   JTable finalTable;

   public GeneSetWizardStep3( GeneSetWizard wiz, Settings settings,
                            GeneAnnotations geneData, NewGeneSet newGeneSet,
                            boolean makenew ) {
      super( wiz );
      this.wiz = wiz;
      this.settings = settings;
      this.geneData = geneData;
      this.newGeneSet = newGeneSet;
      this.makenew = makenew;
      AbstractTableModel finalTableModel = newGeneSet.toTableModel(true);
      finalTable.setModel(finalTableModel);
   }

   //Component initialization
   protected void jbInit() {
      BorderLayout borderLayout4 = new BorderLayout();
      this.setLayout(borderLayout4);
      JPanel step3Panel = new JPanel();
      BorderLayout borderLayout1 = new BorderLayout();
      step3Panel.setLayout(borderLayout1);

      JPanel ncIDPanel = new JPanel();
      ncIDPanel.setPreferredSize(new Dimension(128, 51));
      JLabel classIDL = new JLabel("New Class ID: ");
      classIDTF = new JTextField();
      classIDTF.setPreferredSize(new Dimension(100, 19));
      classIDTF.setBorder(BorderFactory.createLoweredBevelBorder());
      classIDTF.setToolTipText("New Class ID");
      ncIDPanel.add(classIDL);
      ncIDPanel.add(classIDTF);

      JPanel ncInfo1Panel = new JPanel();
      ncInfo1Panel.setPreferredSize(new Dimension(165, 240));
      JPanel ncDescPanel = new JPanel();
      ncDescPanel.setPreferredSize(new Dimension(165, 180));
      classDescL = new JLabel("New Class ID: ");
      classDescL.setRequestFocusEnabled(true);
      classDescL.setText("New Class Description: ");
      classDescTA = new JTextArea();
      classDescTA.setToolTipText("New Class ID");
      classDescTA.getDocument().addDocumentListener(new GeneSetWizardStep3_ClassDescListener(this));
      classDescTA.setLineWrap(true);
      JScrollPane classDTAScroll = new JScrollPane(classDescTA);
      classDTAScroll.setBorder(BorderFactory.createLoweredBevelBorder());
      classDTAScroll.setPreferredSize(new Dimension(160, 140));
      ncDescPanel.add(classDescL);
      ncDescPanel.add(classDTAScroll, null);
      ncInfo1Panel.add(ncIDPanel, null);
      ncInfo1Panel.add(ncDescPanel, null);

      JPanel ncInfo2Panel = new JPanel();
      BorderLayout borderLayout2 = new BorderLayout();
      ncInfo2Panel.setLayout(borderLayout2);
      ncInfo2Panel.setPreferredSize(new Dimension(220, 240));
      classIDFinal = new JLabel("New Class ID: ");
      classIDFinal.setText("No Class Name");
      classIDFinal.setRequestFocusEnabled(true);
      DefaultCellEditor classIDEditor = new DefaultCellEditor(classIDTF);
      classIDEditor.addCellEditorListener(new GeneSetWizardStep3_classIDEditorAdaptor(this));
      finalTable = new JTable();
      finalTable.getTableHeader().setReorderingAllowed( false );
      JScrollPane finalScrollPane = new JScrollPane(finalTable);
      finalScrollPane.setPreferredSize(new Dimension(200, 200));
      ncInfo2Panel.add(classIDFinal, BorderLayout.NORTH);
      ncInfo2Panel.add(finalScrollPane, BorderLayout.CENTER);

      step3Panel.add(ncInfo1Panel, BorderLayout.WEST);
      step3Panel.add(ncInfo2Panel, BorderLayout.CENTER);

      this.add( step3Panel, BorderLayout.CENTER );
   }

   public boolean isReady() {
      return true;
   }

   void classIDEditor_actionPerformed(ChangeEvent e) {
      String classID = (String) ((DefaultCellEditor) e.getSource()).
                       getCellEditorValue();
      if (geneData.classExists(classID) && makenew) {
         GuiUtil.error("A class by the ID " + classID + " already exists.");
      } else {
         newGeneSet.setId(classID);
         classIDFinal.setText(classID);
      }
   }

   void classDescListener_actionPerformed(DocumentEvent e) {
      Document doc = (Document) e.getDocument();
      int length = doc.getLength();
      try {newGeneSet.setDesc(doc.getText(0, length));
      } catch (BadLocationException be) {be.printStackTrace();
      }
   }


   public void update() {
      classIDTF.setText(newGeneSet.getId());
      classDescTA.setText(newGeneSet.getDesc());
      if (newGeneSet.getId().compareTo("") != 0) {
         classIDFinal.setText(newGeneSet.getId());
      }
   }

}

class GeneSetWizardStep3_classIDEditorAdaptor implements CellEditorListener {
   GeneSetWizardStep3 adaptee;
   GeneSetWizardStep3_classIDEditorAdaptor(GeneSetWizardStep3 adaptee) {this.adaptee = adaptee;
   }

   public void editingStopped(ChangeEvent e) {
      adaptee.classIDEditor_actionPerformed(e);
   }

   public void editingCanceled(ChangeEvent e) {
      editingCanceled(e);
   }
}

class GeneSetWizardStep3_ClassDescListener implements DocumentListener {
   GeneSetWizardStep3 adaptee;
   GeneSetWizardStep3_ClassDescListener(GeneSetWizardStep3 adaptee) {this.adaptee = adaptee;
   }

   public void insertUpdate(DocumentEvent e) {adaptee.
           classDescListener_actionPerformed(e);
   }

   public void removeUpdate(DocumentEvent e) {adaptee.
           classDescListener_actionPerformed(e);
   }

   public void changedUpdate(DocumentEvent e) {}
}
