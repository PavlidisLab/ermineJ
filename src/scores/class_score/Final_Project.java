package scores.class_score;
/******************************************************************************
  Author :Shahmil Merchant
  Version :1.0
  Created :09/02/02
  Revision History: none
  Description:Final test file
                                                                                                                                                            
*******************************************************************************/


import scores.class_score.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.ButtonGroup;
import javax.swing.JMenuBar;
import javax.swing.KeyStroke;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;

/*****************************************************************************************/
public class Final_Project {
/*****************************************************************************************/



/*****************************************************************************************/
 public static void main(String[] args) {
/*****************************************************************************************/
	try {
        UIManager.setLookAndFeel(
            UIManager.getCrossPlatformLookAndFeelClassName());
	} catch (Exception e){ }
        JFrame test = new JFrame();
		
	test.addWindowListener(new WindowAdapter() {
		public void windowClosing(WindowEvent e) {
		    System.exit(0);
		}
	    });
	 test.getContentPane().add(new Class_Frame(), 
                                   BorderLayout.CENTER);
        test.setTitle("Class_Score");
	//	test.pack();
	test.setSize(465,600);
	test.setVisible(true);
	}

}
