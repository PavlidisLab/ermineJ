package scores.class_score;
/******************************************************************************
  Author :Shahmil Merchant
  Version :1.0
  Created :09/02/02
  Revision History: none
  Description:Front end GUI for class scores
                                                                                                                                                            
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
import java.awt.Toolkit;
//import java.sql.Time;


/*****************************************************************************************/
/*****************************************************************************************/
public class Class_Frame extends JPanel {

    //for pvals
    public final static int ONE_SECOND = 1000;
    private javax.swing.Timer timer;
    private JProgressBar progress;
    //for correls
    private javax.swing.Timer timer_correl;
    private JProgressBar progress_correl;
    private File startpath;

    //variables used for different UI components
    //Combobox for file names
    JComboBox fileNameField1;
    JComboBox fileNameField2;
    JComboBox fileNameField3;
    JComboBox fileNameField4;
   
    //similar variables for class_pvals panel
    JComboBox fileNameField11;
    JComboBox fileNameField22;
    JComboBox fileNameField33;
    JComboBox fileNameField44;
    JComboBox fileNameField55;

    //help browsing for class_correls
    JButton browseButton1;
    JButton browseButton2;
    JButton browseButton3;
    JButton browseButton4;

    //for class_pvals
    JButton browseButton11;
    JButton browseButton22;
    JButton browseButton33;
    JButton browseButton44;
    JButton browseButton55;
    //for weights for pvals
    JCheckBox dologbox;
    String weight_boolean = "true";

    String dolog_boolean = "true"; // do we take the log of the gene scores?
    int pvalcolumn = 1; // which column in the file has the gene scores?

    //correls
    JButton commitButton;
    JButton cancelButton;
    //pvals
    JButton commitButton1;
    JButton cancelButton1;

    //correls
    JLabel fileNameLabel1;
    JLabel fileNameLabel2;
    JLabel fileNameLabel3;
    JLabel fileNameLabel4;

    //pvals
    JLabel fileNameLabel11;
    JLabel fileNameLabel22;
    JLabel fileNameLabel33;
    JLabel fileNameLabel44;
    JLabel fileNameLabel55;

    //correls
    Vector fileNames;

    //pvals
    Vector fileNames1;

    //correls
    static String fileName1; 
    static String fileName2;
    static String fileName3; 
    static String fileName4;

    //pvals
    static String fileName11;// = "C:\Documents and Settings\Edward\Desktop\ermineJ\java_proj\data\one-way-anova-parsed.txt";
    static String fileName22;// = "C:\Documents and Settings\Edward\Desktop\ermineJ\java_proj\data\AffyGO.txt";          
    static String fileName33;// = "C:\Documents and Settings\Edward\Desktop\ermineJ\java_proj\Results\out1.txt";                 
    static String fileName44;// = "C:\Documents and Settings\Edward\Desktop\ermineJ\java_proj\data\goNames.txt";
    static String fileName55;// = "C:\Documents and Settings\Edward\Desktop\ermineJ\java_proj\data\HG-U95Av2.ug.txt";
    static String method_name = "MEAN_METHOD";
    static String groupMethod = "MEAN_PVAL";
    
    //various parameters for class_correls
    JTextField text_numField;
    JTextField text_maxField;
    JTextField text_minField;
    JTextField text_histoField;

    static int numField=0;
    static int maxField=0;
    static int minField=0;
    static int quantileField=0;
    static double histoField=0;

    //for class_pvals
    JTextField text_numField1;
    JTextField text_maxField1;
    JTextField text_minField1;
    JTextField text_quantileField1;
    JTextField text_histoField1;
    JTextField text_pVal1;

    static int numField1=0;
    static int maxField1=0;
    static int minField1=0;
    static int quantileField1=0;
    static double histoField1=0;
    static double pVal1=0;



    /**
       constructor for creating initial tabbed panel calls corresponding components to display
    */
    public Class_Frame() {

	JTabbedPane tabbedPane= new JTabbedPane();
	startpath = new File(System.getProperty("user.dir"));

	Component panel1 = make_Pval_Panel();
        tabbedPane.addTab("Gene score-based", null, panel1, "Calculates class scores using gene scores");
        tabbedPane.setSelectedIndex(0);
	
	/* todo: fix up the correlation score code */
	//        Component panel2 = make_Correl_Panel();
	//        tabbedPane.addTab("Gene Correlation-based",null,panel2, "Calculates class scores using gene correlations");

	//Add the tabbed pane to this panel.
        setLayout(new GridLayout(1, 1)); 
        add(tabbedPane);
    }



    /*****************************************************************************************/    
    //for correl panel
    /*****************************************************************************************/    
    protected Component make_Correl_Panel() {
	JPanel panel = new JPanel(false);
	fileNameField1 = new JComboBox();
	fileNameField2 = new JComboBox();
	fileNameField3 = new JComboBox();
	fileNameField4 = new JComboBox();
	fileNameField1.setEditable(true);
	fileNameField2.setEditable(true);
	fileNameField3.setEditable(true);
	fileNameField4.setEditable(true);
	fileNameField1.setPreferredSize(new Dimension(300, 20));
	fileNameField2.setPreferredSize(new Dimension(300, 20));
	fileNameField3.setPreferredSize(new Dimension(300, 20));
	fileNameField4.setPreferredSize(new Dimension(300, 20));
	browseButton1 = new JButton("Browse...");
	browseButton2 = new JButton("Browse...");
	browseButton3 = new JButton("Browse...");
	browseButton4 = new JButton("Browse...");
	commitButton = new JButton("Run");
	cancelButton = new JButton("Quit");
	try {
	    browseButton1.addActionListener(new BrowseListener(fileNameField1));
	    browseButton2.addActionListener(new BrowseListener(fileNameField2));
	    browseButton3.addActionListener(new BrowseListener(fileNameField3));
	    browseButton4.addActionListener(new BrowseListener(fileNameField4));	 
	    cancelButton.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
			cancel();
		    }
		});
	    commitButton.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
			commit_correl();
		    }
		});
	    
	} catch (Exception e) {
	    e.printStackTrace();
	}
	
	setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
	fileNameLabel1 = new JLabel("Data File",JLabel.LEFT);
	Box fileNameLabel1Box = new Box(BoxLayout.X_AXIS);
	fileNameLabel1Box.add(fileNameLabel1);
	fileNameLabel1Box.add(Box.createHorizontalGlue());
	Box fileName1Box = new Box(BoxLayout.X_AXIS);
	fileName1Box.add(fileNameField1);
	fileName1Box.add(Box.createHorizontalStrut(10));
	fileName1Box.add(browseButton1);
	fileName1Box.add(Box.createHorizontalGlue());
	
     
	fileNameLabel2 = new JLabel("Affy GO File",JLabel.LEFT);
	Box fileNameLabel2Box = new Box(BoxLayout.X_AXIS);
	fileNameLabel2Box.add(fileNameLabel2);
	fileNameLabel2Box.add(Box.createHorizontalGlue());
	Box fileName2Box = new Box(BoxLayout.X_AXIS);
	fileName2Box.add(fileNameField2);
	fileName2Box.add(Box.createHorizontalStrut(10));
	fileName2Box.add(browseButton2);
	fileName2Box.add(Box.createHorizontalGlue());
     
	fileNameLabel3 = new JLabel("Destination File",JLabel.LEFT);
	Box fileNameLabel3Box = new Box(BoxLayout.X_AXIS);
	fileNameLabel3Box.add(fileNameLabel3);
	fileNameLabel3Box.add(Box.createHorizontalGlue());
	Box fileName3Box = new Box(BoxLayout.X_AXIS);
	fileName3Box.add(fileNameField3);
	fileName3Box.add(Box.createHorizontalStrut(10));
	fileName3Box.add(browseButton3);
	fileName3Box.add(Box.createHorizontalGlue());
     
	fileNameLabel4 = new JLabel("Go Biological names File",JLabel.LEFT);
	Box fileNameLabel4Box = new Box(BoxLayout.X_AXIS);
	fileNameLabel4Box.add(fileNameLabel4);
	fileNameLabel4Box.add(Box.createHorizontalGlue());
	Box fileName4Box = new Box(BoxLayout.X_AXIS);
	fileName4Box.add(fileNameField4);
	fileName4Box.add(Box.createHorizontalStrut(10));
	fileName4Box.add(browseButton4);
	fileName4Box.add(Box.createHorizontalGlue());


	panel.add(fileNameLabel1Box);
	panel.add(fileName1Box);
	panel.add(fileNameLabel2Box);
	panel.add(fileName2Box);
	panel.add(fileNameLabel3Box);
	panel.add(fileName3Box);
	panel.add(fileNameLabel4Box);
	panel.add(fileName4Box);
	panel.add(Box.createVerticalStrut(20));
    
	JLabel num = new JLabel("Number of Iterations");
	text_numField = new JTextField("10000",5);

	panel.add(num);
	panel.add(text_numField);
    
	JLabel class_max = new JLabel("Max class size");
	text_maxField = new JTextField("100",5);
    

	panel.add(class_max);
	panel.add(text_maxField);
	panel.add(Box.createHorizontalStrut(40));     
	panel.add(Box.createVerticalStrut(20)); 

	JLabel class_min = new JLabel("Min class size");
	text_minField = new JTextField("4",5);
    

	panel.add(class_min);
	panel.add(text_minField);
     
	JLabel histogram = new JLabel("Histogram Range");
	text_histoField = new JTextField("5.0",5);
    
	panel.add(histogram);
	panel.add(text_histoField);
	panel.add(Box.createHorizontalStrut(40));
	panel.add(Box.createVerticalStrut(40));
     
	Box commitBox = new Box(BoxLayout.X_AXIS);
	commitBox.add(Box.createHorizontalGlue());
	commitBox.add(commitButton);
	commitBox.add(Box.createHorizontalStrut(40));
	commitBox.add(cancelButton);
	commitBox.add(Box.createHorizontalGlue());
	panel.add(commitBox,BorderLayout.SOUTH);
	panel.add(Box.createVerticalGlue());
	timer_correl = new javax.swing.Timer(ONE_SECOND, new TimerListener_correl());

     return panel;
    }
  

    /*****************************************************************************************/
    /*****************************************************************************************/
    protected Component make_Pval_Panel() {
	
	JPanel panel = new JPanel(false);
	
	Vector sf = comboReader(getCanonical("scoreFile.pref"));
	if (sf != null) 
	    fileNameField11 = new JComboBox(sf); // gene scores
	else
	    fileNameField11 = new JComboBox(); // gene scores
	
	Vector gf = comboReader(getCanonical("goFile.pref"));
	if (gf != null) 
	    fileNameField22 = new JComboBox(gf); // go names
	else
	    fileNameField22 = new JComboBox();
	
	Vector of = comboReader(getCanonical("outputFile.pref"));
	if (of != null) 
	fileNameField33 = new JComboBox(of); // output
	else
	    fileNameField33 = new JComboBox();
	
	Vector nf = comboReader(getCanonical("nameFile.pref"));
	if (nf != null) 
	    fileNameField44 = new JComboBox(nf);
	else
	    fileNameField44 = new JComboBox(); 

	Vector grf = comboReader(getCanonical("groupFile.pref"));
	if (grf != null) 
	    fileNameField55 = new JComboBox(grf);
	else
	    fileNameField55 = new JComboBox();
	
	//    fileNameField22 = new JComboBox(comboReader(getCanonical("goFile"))); // probe to go map
	//   fileNameField33 = new JComboBox(comboReader(getCanonical("outputFile"))); // output file
	//   fileNameField44 = new JComboBox(comboReader(getCanonical("nameFile"))); // biological names for go
	//   fileNameField55 = new JComboBox(comboReader(getCanonical("groupFile"))); // probe to ug map
	
	fileNameField44.addItem("../data/goNames.txt");
	
	fileNameField11.setEditable(true);
	fileNameField22.setEditable(true);
	fileNameField33.setEditable(true);
	fileNameField44.setEditable(true);
	fileNameField55.setEditable(true);
	fileNameField11.setPreferredSize(new Dimension(300, 20));
	fileNameField22.setPreferredSize(new Dimension(300, 20));
	fileNameField33.setPreferredSize(new Dimension(300, 20));
	fileNameField44.setPreferredSize(new Dimension(300, 20));
	fileNameField55.setPreferredSize(new Dimension(300, 20));
	browseButton11 = new JButton("Browse...");
	browseButton22 = new JButton("Browse...");
	browseButton33 = new JButton("Browse...");
	browseButton44 = new JButton("Browse...");
	browseButton55 = new JButton("Browse...");
	commitButton1 = new JButton("Run");
	cancelButton1 = new JButton("Quit");

	//listeners for browse button
	try {
	browseButton11.addActionListener(new BrowseListener(fileNameField11));
	browseButton22.addActionListener(new BrowseListener(fileNameField22));
	browseButton33.addActionListener(new BrowseListener(fileNameField33));
	browseButton44.addActionListener(new BrowseListener(fileNameField44));
	browseButton55.addActionListener(new BrowseListener(fileNameField55));
	
	cancelButton1.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    //close window
		    cancel();
		}
	    });

	commitButton1.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    //extract data from fields and use class_pvals
		    
		    commit();
		}
	    });
	
	} catch (Exception e) {
	    e.printStackTrace();
	}
	
	//set layouts for file fields
	setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
	fileNameLabel11 = new JLabel("Gene P value File",JLabel.LEFT);
	Box fileNameLabel11Box = new Box(BoxLayout.X_AXIS);
	fileNameLabel11Box.add(fileNameLabel11);
	fileNameLabel11Box.add(Box.createHorizontalGlue());
	Box fileName11Box = new Box(BoxLayout.X_AXIS);
	fileName11Box.add(fileNameField11);
	fileName11Box.add(Box.createHorizontalStrut(20));
	fileName11Box.add(browseButton11);
	fileName11Box.add(Box.createHorizontalGlue());
	
	fileNameLabel22 = new JLabel("Probe to Functional class Mapping File",JLabel.LEFT);
	Box fileNameLabel22Box = new Box(BoxLayout.X_AXIS);
	fileNameLabel22Box.add(fileNameLabel22);
	fileNameLabel22Box.add(Box.createHorizontalGlue());
	Box fileName22Box = new Box(BoxLayout.X_AXIS);
	fileName22Box.add(fileNameField22);
	fileName22Box.add(Box.createHorizontalStrut(10));
	fileName22Box.add(browseButton22);
	fileName22Box.add(Box.createHorizontalGlue());
	
	fileNameLabel33 = new JLabel("Output File",JLabel.LEFT);
	Box fileNameLabel33Box = new Box(BoxLayout.X_AXIS);
	fileNameLabel33Box.add(fileNameLabel33);
	fileNameLabel33Box.add(Box.createHorizontalGlue());
	Box fileName33Box = new Box(BoxLayout.X_AXIS);
	fileName33Box.add(fileNameField33);
	fileName33Box.add(Box.createHorizontalStrut(10));
	fileName33Box.add(browseButton33);
	fileName33Box.add(Box.createHorizontalGlue());
	
	fileNameLabel44 = new JLabel("GO Biological Names File",JLabel.LEFT);
	Box fileNameLabel44Box = new Box(BoxLayout.X_AXIS);
	fileNameLabel44Box.add(fileNameLabel44);
	fileNameLabel44Box.add(Box.createHorizontalGlue());
	Box fileName44Box = new Box(BoxLayout.X_AXIS);
	fileName44Box.add(fileNameField44);
	fileName44Box.add(Box.createHorizontalStrut(10));
	fileName44Box.add(browseButton44);
	fileName44Box.add(Box.createHorizontalGlue());
	
	fileNameLabel55 = new JLabel("Probe Group Mapping File",JLabel.LEFT);
	Box fileNameLabel55Box = new Box(BoxLayout.X_AXIS);
	fileNameLabel55Box.add(fileNameLabel55);
	fileNameLabel55Box.add(Box.createHorizontalGlue());
	Box fileName55Box = new Box(BoxLayout.X_AXIS);
	fileName55Box.add(fileNameField55);
	fileName55Box.add(Box.createHorizontalStrut(10));
	fileName55Box.add(browseButton55);
	fileName55Box.add(Box.createHorizontalGlue());
	
	panel.add(fileNameLabel11Box);
	panel.add(fileName11Box);
	panel.add(fileNameLabel22Box);
	panel.add(fileName22Box);
	panel.add(fileNameLabel33Box);
	panel.add(fileName33Box);
	panel.add(fileNameLabel44Box);
	panel.add(fileName44Box);
	panel.add(fileNameLabel55Box);
	panel.add(fileName55Box);
	panel.add(Box.createVerticalStrut(20));
	
	
	//set radio button options 
	JRadioButton meanButton = new JRadioButton("Use Mean method");
	meanButton.setToolTipText("Use the mean of the class's gene scores.");
	meanButton.setActionCommand("MEAN_METHOD");
	meanButton.setSelected(true);
	
	JRadioButton quantileButton = new JRadioButton("Use Quantile method");
	quantileButton.setToolTipText("Use a quantile of the class's gene scores.");
	quantileButton.setActionCommand("QUANTILE_METHOD");
	
	JRadioButton meanAboveButton = new JRadioButton("Use Mean Above Quantile method");
	meanAboveButton.setToolTipText("Use the mean above quantile of the class's gene scores.");
	meanAboveButton.setActionCommand("MEAN_ABOVE_QUANTILE_METHOD");
        
	ButtonGroup group = new ButtonGroup();
	group.add(meanButton);
	group.add(quantileButton);
	group.add(meanAboveButton);
	
	JRadioButton meanGroupButton = new JRadioButton("Use Mean group pvalues");
	meanGroupButton.setToolTipText("Use the mean of the group's pvalues.");
	meanGroupButton.setActionCommand("MEAN_PVAL");
	meanGroupButton.setSelected(true);
     
	JRadioButton bestGroupButton = new JRadioButton("Use Best group pvalues");
	bestGroupButton.setToolTipText("Use the mean of the group's pvalues.");
	bestGroupButton.setActionCommand("BEST_PVAL");
    
	JRadioButton noWeightButton = new JRadioButton("Use Single gene pvalues");
	noWeightButton.setToolTipText("Treat replicates as separate genes.");
	noWeightButton.setActionCommand("NO_WEIGHT");
	
	ButtonGroup group2 = new ButtonGroup();
	group2.add(meanGroupButton);
	group2.add(bestGroupButton);
	group2.add(noWeightButton);
	
	
	JPanel radioPanel2 = new JPanel();
	radioPanel2.setLayout(new BoxLayout(radioPanel2,BoxLayout.Y_AXIS));
	radioPanel2.add(Box.createHorizontalStrut(40));
	radioPanel2.add(meanGroupButton);
	radioPanel2.add(Box.createHorizontalGlue());
	radioPanel2.add(bestGroupButton);
	radioPanel2.add(Box.createHorizontalGlue());
	radioPanel2.add(noWeightButton);
	radioPanel2.add(Box.createHorizontalGlue());
	panel.add(radioPanel2);
	
	JPanel radioPanel = new JPanel();
	radioPanel.setLayout(new BoxLayout(radioPanel,BoxLayout.Y_AXIS));
	radioPanel.add(meanButton);
	radioPanel.add(Box.createHorizontalGlue());
	radioPanel.add(quantileButton);
	radioPanel.add(Box.createHorizontalGlue());
	radioPanel.add(meanAboveButton);
	radioPanel.add(Box.createHorizontalGlue());
	panel.add(radioPanel);
	
	RadioListener myListener = new RadioListener();
	meanButton.addActionListener(myListener);
	quantileButton.addActionListener(myListener);
	meanAboveButton.addActionListener(myListener);
    
	RadioListener2 myListener2 = new RadioListener2();
	meanGroupButton.addActionListener(myListener2);
	bestGroupButton.addActionListener(myListener2);
	noWeightButton.addActionListener(myListener2);
	
        
	//various other parameters
	
	JLabel num1 = new JLabel("Iterations");
	num1.setToolTipText("Applies to mean and quantile methods: more trials take longer.");
	text_numField1 = new JTextField("10000",10);
	panel.add(num1);
	panel.add(text_numField1);
	panel.add(Box.createHorizontalStrut(10));
	
	JLabel quantile1 = new JLabel("Quantile");
	quantile1.setToolTipText("Applies to quantile method only. Use 50 for the median");
	text_quantileField1 = new JTextField("50",10);
	panel.add(quantile1);
	panel.add(text_quantileField1);
	panel.add(Box.createHorizontalStrut(10));
	
	JLabel pval1 = new JLabel("P-value cutoff");
	pval1.setToolTipText("The pvalue cutoff to use for the hypergeometric distribution class evaluation");
	text_pVal1 = new JTextField("0.00001", 10);
	panel.add(pval1);
	panel.add(text_pVal1);
	panel.add(Box.createVerticalStrut(40));

	JLabel class_max1 = new JLabel("Max class size");
	class_max1.setToolTipText("Classes with more members will not be considered");
	text_maxField1 = new JTextField("100",10);
	panel.add(class_max1);
	panel.add(text_maxField1);
	panel.add(Box.createHorizontalStrut(10));
	
	JLabel class_min1 = new JLabel("Min class size");
	class_min1.setToolTipText("Classes with fewer members will not be considered");
	text_minField1 = new JTextField("4",5);
	panel.add(class_min1);
	panel.add(text_minField1);
	panel.add(Box.createHorizontalStrut(40));
	panel.add(Box.createVerticalStrut(40)); 
	
	dologbox = new JCheckBox("Use -log of input values");
	dologbox.setToolTipText("Leave this checked if you are using p values. Otherwise you might want to uncheck it.");
	dologbox.setSelected(true);
	dologListener checkListener = new dologListener();
	dologbox.addItemListener(checkListener);
	panel.add(dologbox);
	panel.add(Box.createVerticalStrut(20));

    
	Box commitBox1 = new Box(BoxLayout.X_AXIS);
	commitBox1.add(Box.createHorizontalGlue());
	commitBox1.add(commitButton1);
	commitBox1.add(Box.createHorizontalStrut(40));
	commitBox1.add(Box.createVerticalStrut(80));
	commitBox1.add(cancelButton1);
	commitBox1.add(Box.createHorizontalGlue());
	panel.add(commitBox1,BorderLayout.SOUTH);
	panel.add(Box.createVerticalGlue());
    
	timer = new javax.swing.Timer(ONE_SECOND, new TimerListener());
	return panel; 
    }




    /*****************************************************************************************/
    /*****************************************************************************************/
    public class TimerListener implements ActionListener {
	
        public void actionPerformed(ActionEvent evt) {
	    //SwingUtilities.invokeLater(new Update());
	    try {
		SwingUtilities.invokeLater(new Updater());
	    } catch (Exception e){

	    }
	}
    }
 
   
    /*****************************************************************************************/
    /*****************************************************************************************/
    public class Updater implements Runnable {
	public void run() {
	    progress.setIndeterminate(true);
	}
    }
    
    /*****************************************************************************************/
    /*****************************************************************************************/
    public class TimerListener_correl implements ActionListener {
	
        public void actionPerformed(ActionEvent evt) {
	    //SwingUtilities.invokeLater(new Update());
	    try {
		SwingUtilities.invokeLater(new Updater_correl());
	    } catch (Exception e){
		
	    }
	}
    }
 
   
    /*****************************************************************************************/
    /*****************************************************************************************/
    public class Updater_correl implements Runnable {
	public void run() {
	    progress_correl.setIndeterminate(true);
	}
    }

    
    /*****************************************************************************************/
    /*****************************************************************************************/
    public class Update implements Runnable {
	public void run() {
	    /*    if (progressMonitor.isCanceled()) {
		  progressMonitor.close();
		  }
		  progressMonitor.setProgress(counter);
		  progressMonitor.setNote("Operation is "+counter+ "%complete");
		  counter+=2;
	    */
	    
	}
    }
    
    
    /****************************************************************************************/
    private class BrowseListener implements ActionListener{
	/*****************************************************************************************/
	JComboBox target;

	public BrowseListener(JComboBox target) {
	    this.target = target;
	}

	public void actionPerformed(ActionEvent e) {
	    browse(target); // target will be a combo box  always ??
	}
    }



    /*****************************************************************************************/  
    protected boolean isURL(String filename) {
	/*****************************************************************************************/
	try {
	    URL url = new URL(filename);
	} catch (MalformedURLException e) {
	    return false;
	}
	return true;
    }



    /*****************************************************************************************/
    /*****************************************************************************************/
    protected String getCanonical(String in) {
	if (in == null || in.length() == 0)
	    return in;
	File outFile = new File(in);
	try {
	    return outFile.getCanonicalPath();
	} catch (Exception e) {
	    e.printStackTrace();
	    return null;
	}
    }


    /*****************************************************************************************/
    //collects field variables - just the filenames.
    /*****************************************************************************************/
    protected boolean collectParams_pval() {
	
	fileNames1 = new Vector();
    	fileName11 = getString(fileNameField11);
	fileName22 = getString(fileNameField22);
	fileName33 = getString(fileNameField33);
	fileName44 = getString(fileNameField44);
	fileName55 = getString(fileNameField55);
	
	//    if(	fileName11.equals(""))
	//	fileName11 = "C:/Documents and Settings/Edward/Desktop/ermineJ/java_proj/data/age.welch.pvals.highexpression.forerminej.txt"; 
	//fileName11 = "C:/Documents and Settings/Edward/Desktop/ermineJ/java_proj/data/pp50.txt"; 
	//    if(	fileName22.equals(""))
	//	fileName22 = "C:/Documents and Settings/Edward/Desktop/ermineJ/java_proj/data/MG-U74Av2.go.txt";                              
	//fileName33 = "C:/Documents and Settings/Edward/Desktop/ermineJ/java_proj/Results/" + getString(fileNameField33);                               
	//    if(	fileName44.equals(""))
	//fileName44 = "C:/Documents and Settings/Edward/Desktop/ermineJ/java_proj/data/goNames.txt";   
	//if(	fileName55.equals(""))
	//fileName55 = "C:/Documents and Settings/Edward/Desktop/ermineJ/java_proj/data/MG-U74Av2.ug.txt";                                  
	
	/* todo: why is this code necessary ??? */
	/*	if (!isURL(fileName11))
	    fileName11 = getCanonical(fileName11);
	else
	    ;
 
	 if (!isURL(fileName22))
	     fileName22 = getCanonical(fileName22);
	else
	     ;

	if  (!isURL(fileName33))
	     fileName33 = getCanonical(fileName33);
	else 
	    ; 
 
	 if (!isURL(fileName44))
	     fileName44 = getCanonical(fileName44);
	else
	      ;
 
	if (!isURL(fileName55))
	     fileName55 = getCanonical(fileName55);
	else
	     ;
	*/
	
	if (testfile(fileName11))
	    fileNames1.addElement(fileName11);
	else
	    return false;


	if (testfile(fileName22)) 
	    fileNames1.addElement(fileName22);
	else return false;

	if (fileName33 != null && fileName33.length() > 0) // okay if
							   // this
							   // file
							   // doesn't
							   // exist,
							   // we will
							   // make
							   // it. should
							   // test
							   // that the
							   // path is
							   // valid --
							   // todo
	    fileNames1.addElement(fileName33);
	else
	    return false;

	if (testfile(fileName44)) 
	    fileNames1.addElement(fileName44);
	else
	    return false;

	if (testfile(fileName55))
	    fileNames1.addElement(fileName55);
	else
	    return false;


	return true;

    }
    


    private boolean testfile(String filename) {
	if (filename != null && filename.length() > 0) {
	    File  f = new File(filename);
	    if ( f.exists() )
		return true;
	    else
		JOptionPane.showMessageDialog(null, "File " + filename + " doesn't exist.  ");
	    return false;
	} else {
	    JOptionPane.showMessageDialog(null, "A required file field is blank");
	    return false;
	}
    }



    /*****************************************************************************************/
    /*****************************************************************************************/
    protected void collectParams_correl() {
	fileNames = new Vector();

	fileName1 = getString(fileNameField1);
	fileName2 = getString(fileNameField2);
	fileName3 = getString(fileNameField3);
	fileName4 = getString(fileNameField4);

	if (!isURL(fileName1))
	    fileName1 = getCanonical(fileName1);
	if (!isURL(fileName2))
	    fileName2 = getCanonical(fileName2);
	if (!isURL(fileName3))
	    fileName3 = getCanonical(fileName3);
	if (!isURL(fileName4))
	    fileName4 = getCanonical(fileName4);



	if (fileName1 != null &&
	    fileName1.length() > 0)
	    fileNames.addElement(fileName1);

	    if (fileName2 != null &&
		fileName2.length() > 0)
		fileNames.addElement(fileName2);
	    if (fileName3 != null &&
		fileName3.length() > 0)
		fileNames.addElement(fileName3);
	    if (fileName4 != null &&
		fileName4.length() > 0)
		fileNames.addElement(fileName4);


    }





    
    /*****************************************************************************************/
    /*****************************************************************************************/
    public void cancel() {
	System.exit(0);
    }


    /*****************************************************************************************/
    public void commit() {
	/*****************************************************************************************/
	//progressMonitor = new ProgressMonitor(this,"Monitoring Progress","Initialising....", 0,100);
	//progressMonitor.setProgress(0);

	if(!collectParams_pval())
	    return; // something is wrong.

	Vector files1 = (Vector) fileNames1.clone();
	if(files1.size() < 5) { 
	    JOptionPane.showMessageDialog(null, "Enter all 5 file names.");
	    return;
	}
	

	int filesFound1 = 0;
	Vector foundFiles1 = new Vector();
	Vector missedFiles1 = new Vector();
	boolean containsURLs1 = false;
	for(int i=0; i < files1.size(); i++) {
	    String fileName = (String) files1.elementAt(i);
	    try {
		URL url = new URL(fileName);
		InputStream is = url.openStream();
		is.close();
		containsURLs1= true;
		foundFiles1.addElement(fileName);
	    } catch (MalformedURLException e) {
		File file = new File(fileName);
		if (file.exists())
		    foundFiles1.addElement(fileName );
		else
		    missedFiles1.addElement(fileName );
	    } catch (IOException e) {
		missedFiles1.add(fileName );
	    }
	}


	if (foundFiles1.size() > 0) {
	    String message = "Are you sure these are the files\n";
	    for(int i=0; i < foundFiles1.size(); i++) {
		message += "   "+((String) foundFiles1.elementAt(i))
		    + "\n";
	    }
	    message += "Weight:" + weight_boolean + "\n";
	    message += "Method:" + method_name + "\n";
	    message += "Take -log of input values:" + dolog_boolean + "\n";
	    message += "Use these files?";
	    int response = JOptionPane.showConfirmDialog(null,
							 message,
							 "Use files?",
							 JOptionPane.YES_NO_OPTION);
	    if (response != JOptionPane.YES_OPTION)
		return;
	}
	

	String numFieldS1;
	numFieldS1 =text_numField1.getText();
	if (numFieldS1 !=null){
	    numField1=Integer.parseInt(numFieldS1);
	}else {
	    JOptionPane.showMessageDialog(null,
					  "Number of iterations is empty. " +
					  "Please change number.");
	    return;
	}
	
	String maxFieldS1;
	maxFieldS1 = text_maxField1.getText();
	if (maxFieldS1 !=null && Integer.parseInt(maxFieldS1) >= 3){
	    maxField1=Integer.parseInt(maxFieldS1);
	} else {
	    JOptionPane.showMessageDialog(null,
					  "Max size is invalid (must be 3 or more). " +
					  "Please change Max Size.");
	    return;
	}
	    
	String minFieldS1;
	minFieldS1 = text_minField1.getText();
	if (minFieldS1 !=null &&  Integer.parseInt(minFieldS1) > 2 ){
	    minField1=Integer.parseInt(minFieldS1);
	} else {
	    JOptionPane.showMessageDialog(null,
					  "Min size is invald (must be 2 or more). " +
					  "Please change Min Size.");
	    return;
	}
	
	if (minField1 > maxField1) {
	    JOptionPane.showMessageDialog(null,
					  "Values of min class > max. " +
					  "Please change class Sizes.");
	    return;
	}
	

	String quantileFieldS1;
	quantileFieldS1 = text_quantileField1.getText();
	if (method_name == "QUANTILE_METHOD") {
	    if (quantileFieldS1 !=null){
		quantileField1=Integer.parseInt(quantileFieldS1);
	    } else  {
		JOptionPane.showMessageDialog(null,
					      "Quantile is empty. " +
					      "Please change quantile.");
		return;
	    }
	}
	
	String pValS1;
	pValS1 = text_pVal1.getText();
	if (pValS1 !=null) {
	    pVal1=Double.parseDouble(pValS1);
	} else {
	    JOptionPane.showMessageDialog(null,
					  "P-value is empty. " +
					  "Please change range.");
	    return; 
	} 
	

	new Thread() {
	    public void run() {
		progress = new JProgressBar();
		JFrame frame = new JFrame("Progress");
		//frame.setContentPane(progress);
		frame.getContentPane().add(progress);
		frame.setSize(50,50);
		frame.setVisible(true);
		progress.setMinimum(0);
		progress.setValue(0);
		progress.setIndeterminate(true);
		timer.start();
		timeCounter.start();
		class_pvals test = new class_pvals(fileName11,fileName22,fileName44,fileName33,fileName55,method_name,groupMethod,maxField1,minField1,numField1,quantileField1,pVal1, weight_boolean, pvalcolumn, dolog_boolean);
		test.class_pval_generator();
		timeCounter.stop();
		timer.stop();
		frame.setVisible(false);
		comboWriter(getCanonical("scoreFile.pref"), fileName11); // gene scores            
		comboWriter(getCanonical("goFile.pref"), fileName22); // probe to go map           
		comboWriter(getCanonical("outputFile.pref"), fileName33); // output file           
		comboWriter(getCanonical("nameFile.pref"), fileName44); // biological names for go 
		comboWriter(getCanonical("groupFile.pref"), fileName55); // probe to ug map 
	    }
        }.start();
    }




    /*****************************************************************************************/
    /*****************************************************************************************/
    public void commit_correl() {

	collectParams_correl();
	
	Vector files = (Vector) fileNames.clone();
	
	if(files.size() < 4) {
	    JOptionPane.showMessageDialog(null,
					  "Enter all 4  "+
					  "file names.");
	    return;
	}	    
	
	int filesFound = 0;
	Vector foundFiles = new Vector();
	Vector missedFiles = new Vector();
	boolean containsURLs = false;
	for(int i=0; i < files.size(); i++) {
	    String fileName = (String) files.elementAt(i);
	    try {
		URL url = new URL(fileName);
		InputStream is = url.openStream();
		is.close();
		containsURLs = true;
		foundFiles.addElement(fileName);
	    } catch (MalformedURLException e) {
		File file = new File(fileName);
		if (file.exists())
		    foundFiles.addElement(fileName);
		else
		    missedFiles.addElement(fileName);
	    } catch (IOException e) {
		missedFiles.add(fileName);
	    }
	}


	    if (foundFiles.size() > 0) {
		String message = "Are you sure these are the files:\n";
		for(int i=0; i < foundFiles.size(); i++) {
		    message += "   "+((String) foundFiles.elementAt(i))
			+ "\n";
		}
		message += "Use these files?";
		int response = JOptionPane.
		    showConfirmDialog(null,
				      message,
				      "Proceed?",
				      JOptionPane.YES_NO_OPTION);
		if (response != JOptionPane.YES_OPTION)
		    return;
	    }


	    String numFieldS;
	    numFieldS =text_numField.getText();
	    if (numFieldS !=null){
		numField=Integer.parseInt(numFieldS);
	    } else {
	    	JOptionPane.showMessageDialog(null,
					      "Number of iterations is empty. "+
					      "Please change number.");	
		return;
	    }

	    String maxFieldS;
	    maxFieldS = text_maxField.getText();
	    if (maxFieldS !=null){
		maxField=Integer.parseInt(maxFieldS);
	    } else {
		JOptionPane.showMessageDialog(null,
					      "Max size is empty. "+
					      "Please change Max Size.");
		return;
	    }

	    String minFieldS;
	    minFieldS = text_minField.getText();
	    if (minFieldS !=null){
		minField=Integer.parseInt(minFieldS);
	    } else {
		JOptionPane.showMessageDialog(null,
					      "Min size is empty. "+
					      "Please change Min Size.");
		return;
	    }

	    if (minField > maxField) {
		JOptionPane.showMessageDialog(null,
					      "Values of min class > max. "+
					      "Please change class Size.");
	    return;
	    }




	    	    String histoFieldS;
	    	    histoFieldS = text_histoField.getText();
	    	    if (histoFieldS !=null){
	    		histoField=Double.parseDouble(histoFieldS);
	    	    } else {
	    	    	JOptionPane.showMessageDialog(null,
	    					      "Histogram range is empty.  "+
	    					      "Please change range.");
	    	    return; 
	    	    }
 
	  
	    new Thread() {
		public void run() {
		    progress_correl = new JProgressBar();
		    JFrame frame_correl = new JFrame("Progress");
		    //frame.setContentPane(progress);
		    frame_correl.getContentPane().add(progress_correl);
		    frame_correl.setSize(50,50);
		    frame_correl.setVisible(true);
		    progress_correl.setMinimum(0);
		    progress_correl.setValue(0);
		    progress_correl.setIndeterminate(true);
		    timer_correl.start();
		    class_correls test = new class_correls(fileName1,fileName2,fileName4,fileName3,maxField,minField,numField,histoField);
		    test.class_correl_generator();
		    timer_correl.stop();
		    frame_correl.setVisible(false);
		}
	    }.start();

	    
	    
	    
    }


    /*****************************************************************************************/
    /*****************************************************************************************/
    private class RadioListener implements ActionListener { 
        public void actionPerformed(ActionEvent e) {
	    method_name=e.getActionCommand();
	}
    }
    
    /*****************************************************************************************/
    /*****************************************************************************************/
    private class RadioListener2 implements ActionListener { 
        public void actionPerformed(ActionEvent e) {
	    groupMethod=e.getActionCommand();
	    if(groupMethod.equals("NO_WEIGHT"))
	        weight_boolean="false";
	    else
	        weight_boolean="true";
	}
    }    

    /*****************************************************************************************/
    /*****************************************************************************************/
    private class dologListener implements ItemListener { 
        public void itemStateChanged(ItemEvent e) {
	    int state = e.getStateChange();
	    if (state == e.SELECTED)
	        dolog_boolean="true";
	    else
	        dolog_boolean="false";
	}
    }    

    /*****************************************************************************************/    
    //private class CheckBoxListener implements ItemListener {
    /*****************************************************************************************/
    /*public void itemStateChanged(ItemEvent e) {
      
    int state =e.getStateChange();
    if (state == e.SELECTED) {
    weight_boolean="true";
    } else if (state == e.DESELECTED)
    {
    weight_boolean="false";
    }   
    }
    }*/
    


    /*****************************************************************************************/
    /*****************************************************************************************/
    public String getString(JComboBox comp) {
	
	String selectedPath = (String) comp.getSelectedItem();
	if (selectedPath == null ||
	    !selectedPath.equals(comp.getEditor().getItem())) {
	    selectedPath = (String) comp.getEditor().getItem();
	}
	return selectedPath;
    }

 
    /*****************************************************************************************/
    /*****************************************************************************************/
    public void browse(JTextField target) {
	JFileChooser chooser = new JFileChooser();
	chooser.setCurrentDirectory(new File(System.getProperty("user.dir")));

	if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
	    target.setText(chooser.getSelectedFile().toString());
    }

    

    /*****************************************************************************************/
    /*****************************************************************************************/
    public void browse(JComboBox target) {
	String selectedPath = getString(target);
	File currentFile = new File(selectedPath);
	//String startPath;
	//	if (selectedPath.length() > 0 &&
	//	    currentFile.exists())
	//   startPath = currentFile.getPath();
 	//else
	//    startPath = System.getProperty("user.dir");

	JFileChooser chooser = new JFileChooser(startpath);

	int returnVal = chooser.showOpenDialog(this);

	if (returnVal == JFileChooser.APPROVE_OPTION) {
	    target.configureEditor(target.getEditor(),
				   chooser.getSelectedFile().toString());
	    if (chooser.getSelectedFile().exists()) {
		startpath = chooser.getSelectedFile();
	    }
	}
    }
    

    public Vector comboReader(String inFilename){
	File file = new File(inFilename);

	System.err.println("Reading " + inFilename);

	if (file.exists() && file.isFile() && file.canRead() ) {

	    Vector fileList = new Vector();
	    try {
		FileInputStream fis = new FileInputStream(inFilename);
		BufferedInputStream bis = new BufferedInputStream(fis);
		BufferedReader dis = new BufferedReader(new InputStreamReader(bis));
		while(dis.ready())
		    {
			String line = dis.readLine();
			fileList.add(line);
			System.err.println(line);
		    }
		dis.close();
	    } catch (IOException e) { 
		// catch possible io errors from readLine()
		System.out.println("IOException error!");
		e.printStackTrace();
	    }
	    return fileList;
	} else {
	    return null;
	}
    }
    
    public void comboWriter(String outFilename, String names){
        try {
	    BufferedWriter out = new BufferedWriter(new FileWriter(outFilename, false));
	    System.err.println(names);
	    out.write(names+"\n");
	    out.close();
        } catch (IOException e) { 
	    System.out.println("Combo writer error!");
	    e.printStackTrace();
	}
    }




    /*****************************************************************************************/
    /*****************************************************************************************/
    public static void main(String[] args) {
	try {
	    UIManager.setLookAndFeel(
				     UIManager.getCrossPlatformLookAndFeelClassName());
	} catch (Exception e){ }
        JFrame test = new JFrame();
	test.setResizable(false);
	
	test.addWindowListener(new WindowAdapter() {
		public void windowClosing(WindowEvent e) {
		    System.exit(0);
		}
	    });
	test.getContentPane().add(new Class_Frame(), 
				  BorderLayout.CENTER);
	test.setTitle("Functional Class Scoring");
	//test.pack();
	test.setSize(450,400);
	test.setVisible(true);
    }

}
