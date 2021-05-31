/*
 * CSP_GUIView.java
 */

package csp_gui;

import csp.ByRef;
import csp.Chromosome;
import csp.CspProcess;
import csp.ExternalData;
import csp.MyException;
import csp.UserInput;
import java.awt.Color;
import org.jdesktop.application.Action;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.TaskMonitor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;
import java.util.StringTokenizer;
import javax.swing.Timer;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.text.DefaultCaret;
import org.jdesktop.application.Application;
//import user_code.NqueenII;
import user_code.TimeTableData;
//import user_code.GraphColoring;
//import user_code.MapColoring;
//import user_code.Nqueen;
//import user_code.NqueenII;
//import user_code.NqueenII;
//import user_code.TimeTableData;

/**
 * The application's main frame.
 */
public class CSP_GUIView extends FrameView {

//    public ByRef msgFromAlgo;
    public long threadID;
    class StartProcessThread extends Thread{

        @Override
        public void run() {     
            PrintWriter resultToFile;
            PrintWriter chromes;
            ByRef nextPrefSuggestion = new ByRef("????");
  
            threadID  = this.getId();
            
            progressBar.setMinimum(0);
            progressBar.setMaximum ( 100);

            try {
                Chromosome.totalAppEvals = 0;
                Chromosome.totalRefEvals = 0;
                intialRAstatus = new ByRef(RAplay);
                cspProcess_.start(progressBar, intialRAstatus, chkSaveChromes.isSelected(), nextPrefSuggestion, ICHEAthread, txaDebug);
                resultToFile = new PrintWriter(new FileWriter(new File(".").getCanonicalPath() + "/output.txt")); 
                chromes = new PrintWriter(new FileWriter(new File(".").getCanonicalPath() + "/chromosomes.txt")); 
                resultToFile.println("\n\n--------------------------------------------------");
//                resultToFile.println("Final Solution [" +cspProcess_.getSolution().size() +"]" );
//                resultToFile.println(cspProcess_.getSolution());
//                if(chkSaveChromes.isSelected())
//                    chromes.print(cspProcess_.printChromeValues()); //?? what is the purpose???
//                else
//                    chromes.print("");
                chromes.close();
                resultToFile.close(); 
                lblPrefSuggestion.setText("(Suggestion: "+(String)nextPrefSuggestion.getVal()+")");
            } catch (MyException me) {
                me.showMessageBox();
            } catch (IOException ioe){
                System.err.println(ioe.getLocalizedMessage());
                ioe.printStackTrace();
            }
        }

        public StartProcessThread() {
            super.start();
        }                
    }
    
    private volatile StartProcessThread ICHEAthread;
    private volatile Thread starterThread;
    private DefaultCaret caret;
    
    //private UserInput userInput_;
    private CspProcess cspProcess_;
    private Properties properties_;
    private int solutionBy;
    private int extendedSolutionSpaceCounter;
    private Boolean RAplay = false;
    private ByRef intialRAstatus;
    
    private void chageEnabilityAllTextBoxes(boolean  enable){
        JTextField temp;
        for (Object j : this.mainControlPanel.getComponents()) { 
            if( j instanceof  JTextField){
                temp =(JTextField)j ;
                temp.setText(null);
                temp.setEnabled(enable);
            }
        }
        this.txtCurPref.setText("0");
        this.txtPrevPref.setText("-1");
        this.txtExtendedSScounter.setText("0");
    }

    private void chageEnabilityAllButtons(boolean  enable){
        JButton temp;
        for (Object j : this.mainControlPanel.getComponents()) {            
            if( j instanceof  JButton){
                temp =(JButton)j ;
                temp.setEnabled(enable);
            }
        }
    }
    
    public CSP_GUIView(SingleFrameApplication app) {
        super(app);
        String version;
        initComponents();
        this.rdoSatisfaction.setSelected(true);
        solutionBy = Chromosome.BY_SATISFACTIONS;
        extendedSolutionSpaceCounter = 0;
        //this.btnTest.setText("Fill with Test values");
        chageEnabilityAllTextBoxes(false);
        properties_= new Properties();
        try {
            properties_.load(new FileInputStream("src/csp_gui/resources/CSP_GUIApp.properties"));
            version = properties_.getProperty("Application.version");
        } catch (IOException e) {
            version = "xxx";
        }  

        this.lblVersion.setText("Ver TEST: " + version);
        starterThread = Thread.currentThread();
        
//        Dimension d = this.getFrame().getSize();
//        this.getFrame().setExtendedState(Frame.MAXIMIZED_BOTH); 
//        this.getFrame().setMinimumSize(d);
//        this.getFrame().setMaximumSize(d);
        
        caret = (DefaultCaret)txaDebug.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        
        
        //this.getRootPane().setDefaultButton(btnData);
        
//        try {
////            LookAndFeel k = new  NimbusLookAndFeel();
////            UIManager.setLookAndFeel(k);
////
//UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
//SwingUtilities.updateComponentTreeUI(mainPanel);
//SwingUtilities.updateComponentTreeUI(menuBar);
//SwingUtilities.updateComponentTreeUI(statusPanel);
//SwingUtilities.updateComponentTreeUI(fchDataFile);
//
////
//        } catch (Exception ex){//UnsupportedLookAndFeelException ex) {
//            Logger.getLogger(CSP_GUIView.class.getName()).log(Level.SEVERE, null, ex);
//        }

//        for (Component j : this.mainControlPanel.getComponents()) {            
//            SwingUtilities.updateComponentTreeUI(j);
//        }
        // <editor-fold defaultstate="collapsed" desc="Default Code">

        // status bar initialization - message timeout, idle icon and busy animation, etc
        ResourceMap resourceMap = getResourceMap();
        int messageTimeout = resourceMap.getInteger("StatusBar.messageTimeout");
        messageTimer = new Timer(messageTimeout, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                statusMessageLabel.setText("");
            }
        });
        messageTimer.setRepeats(false);
        int busyAnimationRate = resourceMap.getInteger("StatusBar.busyAnimationRate");
        for (int i = 0; i < busyIcons.length; i++) {
            busyIcons[i] = resourceMap.getIcon("StatusBar.busyIcons[" + i + "]");
        }
        busyIconTimer = new Timer(busyAnimationRate, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                busyIconIndex = (busyIconIndex + 1) % busyIcons.length;
                statusAnimationLabel.setIcon(busyIcons[busyIconIndex]);
            }
        });
        idleIcon = resourceMap.getIcon("StatusBar.idleIcon");
        statusAnimationLabel.setIcon(idleIcon);
        //progressBar.setVisible(false);

        // connecting action tasks to status bar via TaskMonitor
        TaskMonitor taskMonitor = new TaskMonitor(getApplication().getContext());
        taskMonitor.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                String propertyName = evt.getPropertyName();
                if ("started".equals(propertyName)) {
                    if (!busyIconTimer.isRunning()) {
                        statusAnimationLabel.setIcon(busyIcons[0]);
                        busyIconIndex = 0;
                        busyIconTimer.start();
                    }
                    //progressBar.setVisible(true);
                    //progressBar.setIndeterminate(true);
                } else if ("done".equals(propertyName)) {
                    busyIconTimer.stop();
                    statusAnimationLabel.setIcon(idleIcon);
                    //progressBar.setVisible(false);
                    //progressBar.setValue(0);
                } else if ("message".equals(propertyName)) {
                    String text = (String)(evt.getNewValue());
                    statusMessageLabel.setText((text == null) ? "" : text);
                    messageTimer.restart();
                } else if ("progress".equals(propertyName)) {
                    int value = (Integer)(evt.getNewValue());
                    //progressBar.setVisible(true);
                    //progressBar.setIndeterminate(false);
                    //progressBar.setValue(value);
                }
            }
        });

        // </editor-fold>
    }
    

    @Action
    public void showAboutBox() {
        if (aboutBox == null) {
            JFrame mainFrame = CSP_GUIApp.getApplication().getMainFrame();
            aboutBox = new CSP_GUIAboutBox(mainFrame);
            aboutBox.setLocationRelativeTo(mainFrame);
        }
        CSP_GUIApp.getApplication().show(aboutBox);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        tabbedPane = new javax.swing.JTabbedPane();
        mainControlPanel = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        txtConstraints = new javax.swing.JTextField();
        txtDecisionVar = new javax.swing.JTextField();
        txtMaxValues = new javax.swing.JTextField();
        txtMinValues = new javax.swing.JTextField();
        chkOnlyCSP = new javax.swing.JCheckBox();
        cboMutationStrategy = new javax.swing.JComboBox();
        txtRearrangeCSP = new javax.swing.JTextField();
        btnCSPrestructure = new javax.swing.JButton();
        txtOjbective = new javax.swing.JTextField();
        btnRAcontrol = new javax.swing.JButton();
        btnTest = new javax.swing.JButton();
        btnStuckInLocal = new javax.swing.JButton();
        chkMinVal = new javax.swing.JCheckBox();
        jSeparator2 = new javax.swing.JSeparator();
        chkMaxVal = new javax.swing.JCheckBox();
        rdoDiscrete = new javax.swing.JRadioButton();
        lblVersion = new javax.swing.JLabel();
        btnData = new javax.swing.JButton();
        rdoContinuous = new javax.swing.JRadioButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        chkMatlabDraw = new javax.swing.JCheckBox();
        bthDownSS = new javax.swing.JButton();
        txtPrevPref = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        txtHardViosTolerance = new javax.swing.JTextField();
        lblPrefSuggestion = new javax.swing.JLabel();
        txtExtendedSScounter = new javax.swing.JTextField();
        txtCurPref = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        chkSaveChromes = new javax.swing.JCheckBox();
        txtPop = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        PanelSolveBy = new javax.swing.JPanel();
        rdoSatisfaction = new javax.swing.JRadioButton();
        rdoViolation = new javax.swing.JRadioButton();
        rdoRo = new javax.swing.JRadioButton();
        rdoFitness = new javax.swing.JRadioButton();
        jLabel10 = new javax.swing.JLabel();
        btnUpSS = new javax.swing.JButton();
        jLabel8 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        btnStart = new javax.swing.JButton();
        txtGen = new javax.swing.JTextField();
        btnPrintSol = new javax.swing.JButton();
        cboResolveLocalOpt = new javax.swing.JComboBox();
        RApanel = new javax.swing.JPanel();
        txtRAcommonerAge = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        txtRAdegInfluence = new javax.swing.JTextField();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        txtRAguruPopSize = new javax.swing.JTextField();
        jLabel16 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        chkGuruKarma = new javax.swing.JCheckBox();
        jLabel25 = new javax.swing.JLabel();
        txtRAfullInfluence = new javax.swing.JTextField();
        jLabel26 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jLabel18 = new javax.swing.JLabel();
        txtMaxTransGens = new javax.swing.JTextField();
        jLabel19 = new javax.swing.JLabel();
        txtAcceptedRatioInc = new javax.swing.JTextField();
        jLabel20 = new javax.swing.JLabel();
        txtSameBestGen = new javax.swing.JTextField();
        jLabel21 = new javax.swing.JLabel();
        txtTabuGens = new javax.swing.JTextField();
        jLabel22 = new javax.swing.JLabel();
        jLabel23 = new javax.swing.JLabel();
        txtRhoCOP = new javax.swing.JTextField();
        txtRhoCSP = new javax.swing.JTextField();
        jLabel24 = new javax.swing.JLabel();
        txtLocalBestComp = new javax.swing.JTextField();
        jLabel27 = new javax.swing.JLabel();
        txtNoProgressLimit = new javax.swing.JTextField();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        txaDebug = new javax.swing.JTextArea();
        dbgRAcommonerValidity = new javax.swing.JCheckBox();
        dbgCOPfitness = new javax.swing.JCheckBox();
        dbgCategorySelection = new javax.swing.JCheckBox();
        menuBar = new javax.swing.JMenuBar();
        javax.swing.JMenu fileMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem exitMenuItem = new javax.swing.JMenuItem();
        javax.swing.JMenu helpMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem aboutMenuItem = new javax.swing.JMenuItem();
        statusPanel = new javax.swing.JPanel();
        statusMessageLabel = new javax.swing.JLabel();
        statusAnimationLabel = new javax.swing.JLabel();
        progressBar = new javax.swing.JProgressBar();
        fchDataFile = new javax.swing.JFileChooser();
        bgrpDataType = new javax.swing.ButtonGroup();
        bgrpDataType.add(this.rdoContinuous);
        bgrpDataType.add(this.rdoDiscrete);
        bgrpSolveBy = new javax.swing.ButtonGroup();
        bgrpSolveBy.add(this.rdoRo);
        bgrpSolveBy.add(this.rdoSatisfaction);
        bgrpSolveBy.add(this.rdoViolation);
        bgrpSolveBy.add(this.rdoFitness);

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(csp_gui.CSP_GUIApp.class).getContext().getResourceMap(CSP_GUIView.class);
        mainPanel.setToolTipText(resourceMap.getString("mainPanel.toolTipText")); // NOI18N
        mainPanel.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        mainPanel.setName("mainPanel"); // NOI18N

        //try{
            //UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
            //SwingUtilities.updateComponentTreeUI(tabbedPane);
            //}catch (Exception e){
            //e.printStackTrace();
            //}
        tabbedPane.setToolTipText(resourceMap.getString("tabbedPane.toolTipText")); // NOI18N
        tabbedPane.setName("tabbedPane"); // NOI18N

        //try{
            //UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
            //SwingUtilities.updateComponentTreeUI(mainControlPanel);
            //}catch (Exception e){
            //e.printStackTrace();
            //}
        mainControlPanel.setName("mainControlPanel"); // NOI18N

        jLabel5.setBackground(resourceMap.getColor("jLabel5.background")); // NOI18N
        jLabel5.setText(resourceMap.getString("jLabel5.text")); // NOI18N
        jLabel5.setName("jLabel5"); // NOI18N

        jLabel4.setBackground(resourceMap.getColor("jLabel4.background")); // NOI18N
        jLabel4.setText(resourceMap.getString("jLabel4.text")); // NOI18N
        jLabel4.setName("jLabel4"); // NOI18N

        txtConstraints.setText(resourceMap.getString("txtConstraints.text")); // NOI18N
        txtConstraints.setName("txtConstraints"); // NOI18N
        txtConstraints.setNextFocusableComponent(chkMinVal);

        txtDecisionVar.setText(resourceMap.getString("txtDecisionVar.text")); // NOI18N
        txtDecisionVar.setName("txtDecisionVar"); // NOI18N
        txtDecisionVar.setNextFocusableComponent(txtMinValues);

        txtMaxValues.setText(resourceMap.getString("txtMaxValues.text")); // NOI18N
        txtMaxValues.setName("txtMaxValues"); // NOI18N
        txtMaxValues.setNextFocusableComponent(txtConstraints);

        txtMinValues.setText(resourceMap.getString("txtMinValues.text")); // NOI18N
        txtMinValues.setName("txtMinValues"); // NOI18N
        txtMinValues.setNextFocusableComponent(txtMaxValues);

        chkOnlyCSP.setSelected(true);
        chkOnlyCSP.setText(resourceMap.getString("chkOnlyCSP.text")); // NOI18N
        chkOnlyCSP.setToolTipText(resourceMap.getString("chkOnlyCSP.toolTipText")); // NOI18N
        chkOnlyCSP.setName("chkOnlyCSP"); // NOI18N

        cboMutationStrategy.setBackground(resourceMap.getColor("cboMutationStrategy.background")); // NOI18N
        cboMutationStrategy.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Mut (0) - id 0", "Mut (1) - id 1", "Mut (2) - id 2", "Mut (3) - currently not implemented", "Mut (-1) - No mutation strategy." }));
        cboMutationStrategy.setToolTipText(resourceMap.getString("cboMutationStrategy.toolTipText")); // NOI18N
        cboMutationStrategy.setName("cboMutationStrategy"); // NOI18N
        cboMutationStrategy.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cboMutationStrategyActionPerformed(evt);
            }
        });

        txtRearrangeCSP.setBackground(resourceMap.getColor("txtRearrangeCSP.background")); // NOI18N
        txtRearrangeCSP.setText(resourceMap.getString("txtRearrangeCSP.text")); // NOI18N
        txtRearrangeCSP.setName("txtRearrangeCSP"); // NOI18N

        btnCSPrestructure.setBackground(resourceMap.getColor("btnCSPrestructure.background")); // NOI18N
        btnCSPrestructure.setIcon(resourceMap.getIcon("btnCSPrestructure.icon")); // NOI18N
        btnCSPrestructure.setText(resourceMap.getString("btnCSPrestructure.text")); // NOI18N
        btnCSPrestructure.setToolTipText(resourceMap.getString("btnCSPrestructure.toolTipText")); // NOI18N
        btnCSPrestructure.setName("btnCSPrestructure"); // NOI18N
        btnCSPrestructure.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCSPrestructureActionPerformed(evt);
            }
        });

        txtOjbective.setText(resourceMap.getString("txtOjbective.text")); // NOI18N
        txtOjbective.setName("txtOjbective"); // NOI18N
        txtOjbective.setNextFocusableComponent(txtDecisionVar);

        btnRAcontrol.setBackground(resourceMap.getColor("btnRAcontrol.background")); // NOI18N
        btnRAcontrol.setFont(resourceMap.getFont("btnRAcontrol.font")); // NOI18N
        btnRAcontrol.setForeground(resourceMap.getColor("btnRAcontrol.foreground")); // NOI18N
        btnRAcontrol.setText(resourceMap.getString("btnRAcontrol.text")); // NOI18N
        btnRAcontrol.setToolTipText(resourceMap.getString("btnRAcontrol.toolTipText")); // NOI18N
        btnRAcontrol.setName("btnRAcontrol"); // NOI18N
        btnRAcontrol.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRAcontrolActionPerformed(evt);
            }
        });

        btnTest.setText(resourceMap.getString("btnTest.text")); // NOI18N
        btnTest.setName("btnTest"); // NOI18N
        btnTest.setNextFocusableComponent(btnData);
        btnTest.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTestActionPerformed(evt);
            }
        });

        btnStuckInLocal.setBackground(resourceMap.getColor("btnStuckInLocal.background")); // NOI18N
        btnStuckInLocal.setText(resourceMap.getString("btnStuckInLocal.text")); // NOI18N
        btnStuckInLocal.setToolTipText(resourceMap.getString("btnStuckInLocal.toolTipText")); // NOI18N
        btnStuckInLocal.setName("btnStuckInLocal"); // NOI18N
        btnStuckInLocal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnStuckInLocalActionPerformed(evt);
            }
        });

        chkMinVal.setText(resourceMap.getString("chkMinVal.text")); // NOI18N
        chkMinVal.setName("chkMinVal"); // NOI18N
        chkMinVal.setNextFocusableComponent(chkMaxVal);
        chkMinVal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkMinValActionPerformed(evt);
            }
        });

        jSeparator2.setOrientation(javax.swing.SwingConstants.VERTICAL);
        jSeparator2.setName("jSeparator2"); // NOI18N

        chkMaxVal.setText(resourceMap.getString("chkMaxVal.text")); // NOI18N
        chkMaxVal.setName("chkMaxVal"); // NOI18N
        chkMaxVal.setNextFocusableComponent(txtPop);
        chkMaxVal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkMaxValActionPerformed(evt);
            }
        });

        rdoDiscrete.setText(resourceMap.getString("rdoDiscrete.text")); // NOI18N
        rdoDiscrete.setName("rdoDiscrete"); // NOI18N
        rdoDiscrete.setNextFocusableComponent(rdoContinuous);
        rdoDiscrete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rdoDiscreteActionPerformed(evt);
            }
        });

        lblVersion.setFont(resourceMap.getFont("lblVersion.font")); // NOI18N
        lblVersion.setText(resourceMap.getString("lblVersion.text")); // NOI18N
        lblVersion.setName("lblVersion"); // NOI18N

        btnData.setText(resourceMap.getString("btnData.text")); // NOI18N
        btnData.setName("btnData"); // NOI18N
        btnData.setNextFocusableComponent(txtOjbective);
        btnData.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDataActionPerformed(evt);
            }
        });

        rdoContinuous.setText(resourceMap.getString("rdoContinuous.text")); // NOI18N
        rdoContinuous.setName("rdoContinuous"); // NOI18N
        rdoContinuous.setNextFocusableComponent(btnTest);
        rdoContinuous.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rdoContinuousActionPerformed(evt);
            }
        });

        jLabel1.setBackground(resourceMap.getColor("jLabel1.background")); // NOI18N
        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        jLabel3.setBackground(resourceMap.getColor("jLabel3.background")); // NOI18N
        jLabel3.setText(resourceMap.getString("jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N

        chkMatlabDraw.setText(resourceMap.getString("chkMatlabDraw.text")); // NOI18N
        chkMatlabDraw.setName("chkMatlabDraw"); // NOI18N
        chkMatlabDraw.setNextFocusableComponent(btnStart);
        chkMatlabDraw.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkMatlabDrawActionPerformed(evt);
            }
        });

        bthDownSS.setBackground(resourceMap.getColor("bthDownSS.background")); // NOI18N
        bthDownSS.setIcon(new javax.swing.ImageIcon(getClass().getResource("/csp_gui/resources/down.GIF"))); // NOI18N
        bthDownSS.setText(resourceMap.getString("bthDownSS.text")); // NOI18N
        bthDownSS.setToolTipText(resourceMap.getString("bthDownSS.toolTipText")); // NOI18N
        bthDownSS.setEnabled(false);
        bthDownSS.setName("bthDownSS"); // NOI18N
        bthDownSS.setSelected(true);
        bthDownSS.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bthDownSSActionPerformed(evt);
            }
        });

        txtPrevPref.setText(resourceMap.getString("txtPrevPref.text")); // NOI18N
        txtPrevPref.setName("txtPrevPref"); // NOI18N

        jLabel7.setText(resourceMap.getString("jLabel7.text")); // NOI18N
        jLabel7.setName("jLabel7"); // NOI18N

        txtHardViosTolerance.setBackground(resourceMap.getColor("txtHardViosTolerance.background")); // NOI18N
        txtHardViosTolerance.setText(resourceMap.getString("txtHardViosTolerance.text")); // NOI18N
        txtHardViosTolerance.setToolTipText(resourceMap.getString("txtHardViosTolerance.toolTipText")); // NOI18N
        txtHardViosTolerance.setName("txtHardViosTolerance"); // NOI18N
        txtHardViosTolerance.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtHardViosToleranceFocusLost(evt);
            }
        });

        lblPrefSuggestion.setText(resourceMap.getString("lblPrefSuggestion.text")); // NOI18N
        lblPrefSuggestion.setName("lblPrefSuggestion"); // NOI18N

        txtExtendedSScounter.setBackground(resourceMap.getColor("txtExtendedSScounter.background")); // NOI18N
        txtExtendedSScounter.setEditable(false);
        txtExtendedSScounter.setText(resourceMap.getString("txtExtendedSScounter.text")); // NOI18N
        txtExtendedSScounter.setEnabled(false);
        txtExtendedSScounter.setName("txtExtendedSScounter"); // NOI18N

        txtCurPref.setText(resourceMap.getString("txtCurPref.text")); // NOI18N
        txtCurPref.setName("txtCurPref"); // NOI18N

        jLabel11.setText(resourceMap.getString("jLabel11.text")); // NOI18N
        jLabel11.setName("jLabel11"); // NOI18N

        jSeparator1.setName("jSeparator1"); // NOI18N

        chkSaveChromes.setSelected(true);
        chkSaveChromes.setText(resourceMap.getString("chkSaveChromes.text")); // NOI18N
        chkSaveChromes.setName("chkSaveChromes"); // NOI18N
        chkSaveChromes.setNextFocusableComponent(rdoSatisfaction);

        txtPop.setText(resourceMap.getString("txtPop.text")); // NOI18N
        txtPop.setName("txtPop"); // NOI18N
        txtPop.setNextFocusableComponent(txtGen);
        txtPop.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                txtPopFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtPopFocusLost(evt);
            }
        });

        jLabel9.setText(resourceMap.getString("jLabel9.text")); // NOI18N
        jLabel9.setName("jLabel9"); // NOI18N

        PanelSolveBy.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(-16777216,true), 1, true));
        PanelSolveBy.setName("PanelSolveBy"); // NOI18N

        rdoSatisfaction.setSelected(true);
        rdoSatisfaction.setText(resourceMap.getString("rdoSatisfaction.text")); // NOI18N
        rdoSatisfaction.setName("rdoSatisfaction"); // NOI18N
        rdoSatisfaction.setNextFocusableComponent(rdoViolation);
        rdoSatisfaction.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rdoSatisfactionActionPerformed(evt);
            }
        });

        rdoViolation.setText(resourceMap.getString("rdoViolation.text")); // NOI18N
        rdoViolation.setName("rdoViolation"); // NOI18N
        rdoViolation.setNextFocusableComponent(rdoRo);
        rdoViolation.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rdoViolationActionPerformed(evt);
            }
        });

        rdoRo.setText(resourceMap.getString("rdoRo.text")); // NOI18N
        rdoRo.setName("rdoRo"); // NOI18N
        rdoRo.setNextFocusableComponent(rdoFitness);
        rdoRo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rdoRoActionPerformed(evt);
            }
        });

        rdoFitness.setText(resourceMap.getString("rdoFitness.text")); // NOI18N
        rdoFitness.setName("rdoFitness"); // NOI18N
        rdoFitness.setNextFocusableComponent(chkMatlabDraw);
        rdoFitness.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rdoFitnessActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout PanelSolveByLayout = new javax.swing.GroupLayout(PanelSolveBy);
        PanelSolveBy.setLayout(PanelSolveByLayout);
        PanelSolveByLayout.setHorizontalGroup(
            PanelSolveByLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PanelSolveByLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(PanelSolveByLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(rdoSatisfaction)
                    .addComponent(rdoViolation)
                    .addComponent(rdoRo)
                    .addComponent(rdoFitness))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        PanelSolveByLayout.setVerticalGroup(
            PanelSolveByLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PanelSolveByLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(rdoSatisfaction)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(rdoViolation)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(rdoRo)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(rdoFitness)
                .addContainerGap())
        );

        PanelSolveByLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {rdoFitness, rdoRo, rdoSatisfaction, rdoViolation});

        jLabel10.setText(resourceMap.getString("jLabel10.text")); // NOI18N
        jLabel10.setName("jLabel10"); // NOI18N

        btnUpSS.setBackground(resourceMap.getColor("btnUpSS.background")); // NOI18N
        btnUpSS.setIcon(resourceMap.getIcon("btnUpSS.icon")); // NOI18N
        btnUpSS.setText(resourceMap.getString("btnUpSS.text")); // NOI18N
        btnUpSS.setToolTipText(resourceMap.getString("btnUpSS.toolTipText")); // NOI18N
        btnUpSS.setName("btnUpSS"); // NOI18N
        btnUpSS.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUpSSActionPerformed(evt);
            }
        });

        jLabel8.setText(resourceMap.getString("jLabel8.text")); // NOI18N
        jLabel8.setName("jLabel8"); // NOI18N

        jLabel6.setText(resourceMap.getString("jLabel6.text")); // NOI18N
        jLabel6.setName("jLabel6"); // NOI18N

        btnStart.setText(resourceMap.getString("btnStart.text")); // NOI18N
        btnStart.setEnabled(false);
        btnStart.setName("btnStart"); // NOI18N
        btnStart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnStartActionPerformed(evt);
            }
        });

        txtGen.setText(resourceMap.getString("txtGen.text")); // NOI18N
        txtGen.setName("txtGen"); // NOI18N
        txtGen.setNextFocusableComponent(chkSaveChromes);
        txtGen.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                txtGenFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtGenFocusLost(evt);
            }
        });

        btnPrintSol.setBackground(resourceMap.getColor("btnPrintSol.background")); // NOI18N
        btnPrintSol.setText(resourceMap.getString("btnPrintSol.text")); // NOI18N
        btnPrintSol.setToolTipText(resourceMap.getString("btnPrintSol.toolTipText")); // NOI18N
        btnPrintSol.setName("btnPrintSol"); // NOI18N
        btnPrintSol.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPrintSolActionPerformed(evt);
            }
        });

        cboResolveLocalOpt.setBackground(resourceMap.getColor("cboResolveLocalOpt.background")); // NOI18N
        cboResolveLocalOpt.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Resolve Level 0", "Resolve Level 1", "Resolve Level 2", "Resolve Level 3", "Resolve Level 4", "Resolve Level 5" }));
        cboResolveLocalOpt.setName("cboResolveLocalOpt"); // NOI18N
        cboResolveLocalOpt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cboResolveLocalOptActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout mainControlPanelLayout = new javax.swing.GroupLayout(mainControlPanel);
        mainControlPanel.setLayout(mainControlPanelLayout);
        mainControlPanelLayout.setHorizontalGroup(
            mainControlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainControlPanelLayout.createSequentialGroup()
                .addGroup(mainControlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(mainControlPanelLayout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addGroup(mainControlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel8)
                            .addComponent(jLabel9))
                        .addGap(66, 66, 66)
                        .addGroup(mainControlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtPrevPref, javax.swing.GroupLayout.PREFERRED_SIZE, 69, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtCurPref, javax.swing.GroupLayout.PREFERRED_SIZE, 69, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(10, 10, 10)
                        .addComponent(lblPrefSuggestion))
                    .addGroup(mainControlPanelLayout.createSequentialGroup()
                        .addGap(49, 49, 49)
                        .addComponent(lblVersion, javax.swing.GroupLayout.PREFERRED_SIZE, 118, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(6, 6, 6)
                        .addComponent(rdoDiscrete))
                    .addGroup(mainControlPanelLayout.createSequentialGroup()
                        .addGap(173, 173, 173)
                        .addComponent(rdoContinuous, javax.swing.GroupLayout.PREFERRED_SIZE, 197, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(mainControlPanelLayout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addGroup(mainControlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(mainControlPanelLayout.createSequentialGroup()
                                .addComponent(jLabel6)
                                .addGap(22, 22, 22)
                                .addComponent(txtPop, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(mainControlPanelLayout.createSequentialGroup()
                                .addComponent(jLabel7)
                                .addGap(32, 32, 32)
                                .addComponent(txtGen, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(mainControlPanelLayout.createSequentialGroup()
                                .addGap(80, 80, 80)
                                .addComponent(chkSaveChromes))
                            .addGroup(mainControlPanelLayout.createSequentialGroup()
                                .addGap(56, 56, 56)
                                .addComponent(btnStart, javax.swing.GroupLayout.PREFERRED_SIZE, 215, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(18, 18, 18)
                        .addComponent(PanelSolveBy, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(mainControlPanelLayout.createSequentialGroup()
                        .addGap(462, 462, 462)
                        .addComponent(chkMaxVal, javax.swing.GroupLayout.PREFERRED_SIZE, 156, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(mainControlPanelLayout.createSequentialGroup()
                        .addGap(462, 462, 462)
                        .addGroup(mainControlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(mainControlPanelLayout.createSequentialGroup()
                                .addGap(22, 22, 22)
                                .addComponent(chkMatlabDraw, javax.swing.GroupLayout.PREFERRED_SIZE, 139, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(chkMinVal, javax.swing.GroupLayout.PREFERRED_SIZE, 156, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(mainControlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addGroup(mainControlPanelLayout.createSequentialGroup()
                            .addGap(28, 28, 28)
                            .addGroup(mainControlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jLabel1)
                                .addComponent(jLabel2)
                                .addComponent(jLabel3)
                                .addComponent(jLabel4)
                                .addComponent(jLabel5))
                            .addGap(4, 4, 4)
                            .addGroup(mainControlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(txtConstraints, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(txtMaxValues, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(txtMinValues, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(txtDecisionVar, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(txtOjbective, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mainControlPanelLayout.createSequentialGroup()
                            .addContainerGap(489, Short.MAX_VALUE)
                            .addComponent(btnTest, javax.swing.GroupLayout.PREFERRED_SIZE, 139, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mainControlPanelLayout.createSequentialGroup()
                            .addContainerGap(489, Short.MAX_VALUE)
                            .addComponent(btnData, javax.swing.GroupLayout.PREFERRED_SIZE, 139, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mainControlPanelLayout.createSequentialGroup()
                            .addGap(36, 36, 36)
                            .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 592, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(mainControlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnStuckInLocal, javax.swing.GroupLayout.DEFAULT_SIZE, 156, Short.MAX_VALUE)
                    .addGroup(mainControlPanelLayout.createSequentialGroup()
                        .addComponent(btnCSPrestructure, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(6, 6, 6)
                        .addGroup(mainControlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(chkOnlyCSP, javax.swing.GroupLayout.DEFAULT_SIZE, 89, Short.MAX_VALUE)
                            .addComponent(txtRearrangeCSP, javax.swing.GroupLayout.DEFAULT_SIZE, 89, Short.MAX_VALUE)))
                    .addComponent(jLabel11)
                    .addComponent(jLabel10)
                    .addGroup(mainControlPanelLayout.createSequentialGroup()
                        .addComponent(btnUpSS, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 55, Short.MAX_VALUE)
                        .addComponent(bthDownSS, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(cboMutationStrategy, 0, 0, Short.MAX_VALUE)
                    .addComponent(btnPrintSol, javax.swing.GroupLayout.DEFAULT_SIZE, 156, Short.MAX_VALUE)
                    .addComponent(cboResolveLocalOpt, 0, 156, Short.MAX_VALUE)
                    .addComponent(btnRAcontrol, javax.swing.GroupLayout.DEFAULT_SIZE, 156, Short.MAX_VALUE)
                    .addComponent(txtHardViosTolerance, javax.swing.GroupLayout.DEFAULT_SIZE, 156, Short.MAX_VALUE)
                    .addComponent(txtExtendedSScounter, javax.swing.GroupLayout.DEFAULT_SIZE, 156, Short.MAX_VALUE))
                .addContainerGap())
        );
        mainControlPanelLayout.setVerticalGroup(
            mainControlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainControlPanelLayout.createSequentialGroup()
                .addGap(11, 11, 11)
                .addGroup(mainControlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSeparator2, javax.swing.GroupLayout.DEFAULT_SIZE, 432, Short.MAX_VALUE)
                    .addGroup(mainControlPanelLayout.createSequentialGroup()
                        .addGroup(mainControlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(mainControlPanelLayout.createSequentialGroup()
                                .addGroup(mainControlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(lblVersion)
                                    .addGroup(mainControlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(rdoDiscrete)
                                        .addComponent(btnTest)))
                                .addGroup(mainControlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(rdoContinuous)
                                    .addComponent(btnData))
                                .addGap(6, 6, 6)
                                .addGroup(mainControlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(txtOjbective, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(chkMatlabDraw))
                                .addGap(3, 3, 3)
                                .addGroup(mainControlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(mainControlPanelLayout.createSequentialGroup()
                                        .addGap(3, 3, 3)
                                        .addComponent(jLabel2))
                                    .addComponent(txtDecisionVar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(2, 2, 2)
                                .addGroup(mainControlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(mainControlPanelLayout.createSequentialGroup()
                                        .addGap(4, 4, 4)
                                        .addComponent(jLabel3))
                                    .addGroup(mainControlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(chkMinVal)
                                        .addComponent(txtMinValues, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGap(3, 3, 3)
                                .addGroup(mainControlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(mainControlPanelLayout.createSequentialGroup()
                                        .addGap(4, 4, 4)
                                        .addComponent(jLabel4))
                                    .addGroup(mainControlPanelLayout.createSequentialGroup()
                                        .addGap(1, 1, 1)
                                        .addComponent(txtMaxValues, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(chkMaxVal))
                                .addGap(4, 4, 4)
                                .addGroup(mainControlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(mainControlPanelLayout.createSequentialGroup()
                                        .addGap(3, 3, 3)
                                        .addComponent(jLabel5))
                                    .addComponent(txtConstraints, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(11, 11, 11)
                                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(11, 11, 11)
                                .addGroup(mainControlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(mainControlPanelLayout.createSequentialGroup()
                                        .addGroup(mainControlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(mainControlPanelLayout.createSequentialGroup()
                                                .addGap(3, 3, 3)
                                                .addComponent(jLabel6))
                                            .addComponent(txtPop, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGap(6, 6, 6)
                                        .addGroup(mainControlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(mainControlPanelLayout.createSequentialGroup()
                                                .addGap(3, 3, 3)
                                                .addComponent(jLabel7))
                                            .addComponent(txtGen, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGap(7, 7, 7)
                                        .addComponent(chkSaveChromes)
                                        .addGap(7, 7, 7)
                                        .addComponent(btnStart))
                                    .addComponent(PanelSolveBy, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(11, 11, 11)
                                .addGroup(mainControlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(mainControlPanelLayout.createSequentialGroup()
                                        .addGap(3, 3, 3)
                                        .addComponent(jLabel9))
                                    .addComponent(txtPrevPref, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(2, 2, 2)
                                .addGroup(mainControlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(mainControlPanelLayout.createSequentialGroup()
                                        .addGap(3, 3, 3)
                                        .addComponent(jLabel8))
                                    .addComponent(txtCurPref, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(mainControlPanelLayout.createSequentialGroup()
                                        .addGap(3, 3, 3)
                                        .addComponent(lblPrefSuggestion))))
                            .addGroup(mainControlPanelLayout.createSequentialGroup()
                                .addComponent(btnRAcontrol)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(mainControlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(btnCSPrestructure)
                                    .addGroup(mainControlPanelLayout.createSequentialGroup()
                                        .addComponent(txtRearrangeCSP, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(chkOnlyCSP)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnStuckInLocal, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cboResolveLocalOpt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(31, 31, 31)
                                .addComponent(cboMutationStrategy, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnPrintSol)
                                .addGap(11, 11, 11)
                                .addComponent(jLabel11)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtHardViosTolerance, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(28, 28, 28)
                                .addComponent(jLabel10)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtExtendedSScounter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(6, 6, 6)
                                .addGroup(mainControlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(bthDownSS)
                                    .addComponent(btnUpSS))))
                        .addGap(14, 14, 14))))
        );

        tabbedPane.addTab(resourceMap.getString("mainControlPanel.TabConstraints.tabTitle"), mainControlPanel); // NOI18N

        RApanel.setName("RApanel"); // NOI18N

        txtRAcommonerAge.setText(resourceMap.getString("txtRAcommonerAge.text")); // NOI18N
        txtRAcommonerAge.setName("txtRAcommonerAge"); // NOI18N
        txtRAcommonerAge.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtRAcommonerAgeFocusLost(evt);
            }
        });

        jLabel12.setText(resourceMap.getString("jLabel12.text")); // NOI18N
        jLabel12.setName("jLabel12"); // NOI18N

        jLabel13.setText(resourceMap.getString("jLabel13.text")); // NOI18N
        jLabel13.setName("jLabel13"); // NOI18N

        txtRAdegInfluence.setText(resourceMap.getString("txtRAdegInfluence.text")); // NOI18N
        txtRAdegInfluence.setToolTipText(resourceMap.getString("txtRAdegInfluence.toolTipText")); // NOI18N
        txtRAdegInfluence.setName("txtRAdegInfluence"); // NOI18N
        txtRAdegInfluence.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtRAdegInfluenceFocusLost(evt);
            }
        });

        jLabel14.setText(resourceMap.getString("jLabel14.text")); // NOI18N
        jLabel14.setName("jLabel14"); // NOI18N

        jLabel15.setText(resourceMap.getString("jLabel15.text")); // NOI18N
        jLabel15.setName("jLabel15"); // NOI18N

        txtRAguruPopSize.setText(resourceMap.getString("txtRAguruPopSize.text")); // NOI18N
        txtRAguruPopSize.setName("txtRAguruPopSize"); // NOI18N
        txtRAguruPopSize.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtRAguruPopSizeFocusLost(evt);
            }
        });

        jLabel16.setText(resourceMap.getString("jLabel16.text")); // NOI18N
        jLabel16.setName("jLabel16"); // NOI18N

        jLabel17.setText(resourceMap.getString("jLabel17.text")); // NOI18N
        jLabel17.setName("jLabel17"); // NOI18N

        chkGuruKarma.setText(resourceMap.getString("chkGuruKarma.text")); // NOI18N
        chkGuruKarma.setName("chkGuruKarma"); // NOI18N
        chkGuruKarma.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkGuruKarmaActionPerformed(evt);
            }
        });

        jLabel25.setText(resourceMap.getString("jLabel25.text")); // NOI18N
        jLabel25.setName("jLabel25"); // NOI18N

        txtRAfullInfluence.setText(resourceMap.getString("txtRAfullInfluence.text")); // NOI18N
        txtRAfullInfluence.setName("txtRAfullInfluence"); // NOI18N
        txtRAfullInfluence.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtRAfullInfluenceFocusLost(evt);
            }
        });

        jLabel26.setText(resourceMap.getString("jLabel26.text")); // NOI18N
        jLabel26.setName("jLabel26"); // NOI18N

        javax.swing.GroupLayout RApanelLayout = new javax.swing.GroupLayout(RApanel);
        RApanel.setLayout(RApanelLayout);
        RApanelLayout.setHorizontalGroup(
            RApanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(RApanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(RApanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel12)
                    .addComponent(jLabel14)
                    .addComponent(jLabel16)
                    .addComponent(jLabel25))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(RApanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(txtRAfullInfluence)
                    .addComponent(txtRAguruPopSize)
                    .addComponent(txtRAdegInfluence)
                    .addComponent(txtRAcommonerAge, javax.swing.GroupLayout.DEFAULT_SIZE, 79, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(RApanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel26, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel17, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel15, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addComponent(chkGuruKarma)
                .addContainerGap(214, Short.MAX_VALUE))
        );
        RApanelLayout.setVerticalGroup(
            RApanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(RApanelLayout.createSequentialGroup()
                .addGap(34, 34, 34)
                .addGroup(RApanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel12)
                    .addComponent(txtRAcommonerAge, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel13))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(RApanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel14)
                    .addComponent(txtRAdegInfluence, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel15))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(RApanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtRAguruPopSize, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel16)
                    .addComponent(jLabel17)
                    .addComponent(chkGuruKarma))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(RApanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel25)
                    .addComponent(txtRAfullInfluence, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel26))
                .addContainerGap(301, Short.MAX_VALUE))
        );

        tabbedPane.addTab(resourceMap.getString("RApanel.TabConstraints.tabTitle"), RApanel); // NOI18N

        jPanel1.setName("jPanel1"); // NOI18N

        jLabel18.setText(resourceMap.getString("jLabel18.text")); // NOI18N
        jLabel18.setName("jLabel18"); // NOI18N

        txtMaxTransGens.setText(resourceMap.getString("txtMaxTransGens.text")); // NOI18N
        txtMaxTransGens.setName("txtMaxTransGens"); // NOI18N
        txtMaxTransGens.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtMaxTransGensFocusLost(evt);
            }
        });

        jLabel19.setText(resourceMap.getString("jLabel19.text")); // NOI18N
        jLabel19.setName("jLabel19"); // NOI18N

        txtAcceptedRatioInc.setText(resourceMap.getString("txtAcceptedRatioInc.text")); // NOI18N
        txtAcceptedRatioInc.setName("txtAcceptedRatioInc"); // NOI18N
        txtAcceptedRatioInc.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtAcceptedRatioIncFocusLost(evt);
            }
        });

        jLabel20.setText(resourceMap.getString("jLabel20.text")); // NOI18N
        jLabel20.setToolTipText(resourceMap.getString("jLabel20.toolTipText")); // NOI18N
        jLabel20.setName("jLabel20"); // NOI18N

        txtSameBestGen.setText(resourceMap.getString("txtSameBestGen.text")); // NOI18N
        txtSameBestGen.setName("txtSameBestGen"); // NOI18N
        txtSameBestGen.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtSameBestGenFocusLost(evt);
            }
        });

        jLabel21.setText(resourceMap.getString("jLabel21.text")); // NOI18N
        jLabel21.setName("jLabel21"); // NOI18N

        txtTabuGens.setText(resourceMap.getString("txtTabuGens.text")); // NOI18N
        txtTabuGens.setName("txtTabuGens"); // NOI18N
        txtTabuGens.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtTabuGensFocusLost(evt);
            }
        });

        jLabel22.setText(resourceMap.getString("jLabel22.text")); // NOI18N
        jLabel22.setName("jLabel22"); // NOI18N

        jLabel23.setText(resourceMap.getString("jLabel23.text")); // NOI18N
        jLabel23.setName("jLabel23"); // NOI18N

        txtRhoCOP.setText(resourceMap.getString("txtRhoCOP.text")); // NOI18N
        txtRhoCOP.setToolTipText(resourceMap.getString("txtRhoCOP.toolTipText")); // NOI18N
        txtRhoCOP.setName("txtRhoCOP"); // NOI18N
        txtRhoCOP.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtRhoCOPFocusLost(evt);
            }
        });

        txtRhoCSP.setText(resourceMap.getString("txtRhoCSP.text")); // NOI18N
        txtRhoCSP.setToolTipText(resourceMap.getString("txtRhoCSP.toolTipText")); // NOI18N
        txtRhoCSP.setName("txtRhoCSP"); // NOI18N
        txtRhoCSP.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtRhoCSPFocusLost(evt);
            }
        });

        jLabel24.setText(resourceMap.getString("jLabel24.text")); // NOI18N
        jLabel24.setName("jLabel24"); // NOI18N

        txtLocalBestComp.setText(resourceMap.getString("txtLocalBestComp.text")); // NOI18N
        txtLocalBestComp.setName("txtLocalBestComp"); // NOI18N
        txtLocalBestComp.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtLocalBestCompFocusLost(evt);
            }
        });

        jLabel27.setText(resourceMap.getString("jLabel27.text")); // NOI18N
        jLabel27.setName("jLabel27"); // NOI18N

        txtNoProgressLimit.setText(resourceMap.getString("txtNoProgressLimit.text")); // NOI18N
        txtNoProgressLimit.setName("txtNoProgressLimit"); // NOI18N
        txtNoProgressLimit.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtNoProgressLimitFocusLost(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel27)
                            .addComponent(jLabel24)
                            .addComponent(jLabel18))
                        .addGap(35, 35, 35)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtNoProgressLimit, javax.swing.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE)
                            .addComponent(txtLocalBestComp, javax.swing.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE)
                            .addComponent(txtRhoCSP, javax.swing.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE)
                            .addComponent(txtRhoCOP, javax.swing.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE)
                            .addComponent(txtTabuGens, javax.swing.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE)
                            .addComponent(txtMaxTransGens, javax.swing.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE)
                            .addComponent(txtAcceptedRatioInc, javax.swing.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE)
                            .addComponent(txtSameBestGen, javax.swing.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE))
                        .addGap(433, 433, 433))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel19)
                        .addContainerGap(680, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel20)
                        .addContainerGap(549, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel21)
                        .addContainerGap(702, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel22, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(734, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel23, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(736, Short.MAX_VALUE))))
        );

        jPanel1Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {txtAcceptedRatioInc, txtMaxTransGens, txtSameBestGen, txtTabuGens});

        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(31, 31, 31)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel18, javax.swing.GroupLayout.DEFAULT_SIZE, 20, Short.MAX_VALUE)
                    .addComponent(txtMaxTransGens, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel19, javax.swing.GroupLayout.DEFAULT_SIZE, 20, Short.MAX_VALUE)
                    .addComponent(txtAcceptedRatioInc, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel20, javax.swing.GroupLayout.DEFAULT_SIZE, 20, Short.MAX_VALUE)
                    .addComponent(txtSameBestGen, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel21)
                    .addComponent(txtTabuGens, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel22, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtRhoCOP, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel23, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtRhoCSP, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(txtLocalBestComp, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel24))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel27)
                    .addComponent(txtNoProgressLimit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(210, 210, 210))
        );

        jPanel1Layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {txtAcceptedRatioInc, txtMaxTransGens, txtSameBestGen, txtTabuGens});

        tabbedPane.addTab(resourceMap.getString("jPanel1.TabConstraints.tabTitle"), jPanel1); // NOI18N

        jPanel2.setName("jPanel2"); // NOI18N

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        txaDebug.setColumns(20);
        txaDebug.setEditable(false);
        txaDebug.setRows(5);
        txaDebug.setToolTipText(resourceMap.getString("txaDebug.toolTipText")); // NOI18N
        txaDebug.setWrapStyleWord(true);
        txaDebug.setName("txaDebug"); // NOI18N
        txaDebug.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                txaDebugFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                txaDebugFocusLost(evt);
            }
        });
        jScrollPane1.setViewportView(txaDebug);

        dbgRAcommonerValidity.setText(resourceMap.getString("dbgRAcommonerValidity.text")); // NOI18N
        dbgRAcommonerValidity.setName("dbgRAcommonerValidity"); // NOI18N
        dbgRAcommonerValidity.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dbgRAcommonerValidityActionPerformed(evt);
            }
        });

        dbgCOPfitness.setText(resourceMap.getString("dbgCOPfitness.text")); // NOI18N
        dbgCOPfitness.setName("dbgCOPfitness"); // NOI18N
        dbgCOPfitness.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dbgCOPfitnessActionPerformed(evt);
            }
        });

        dbgCategorySelection.setText(resourceMap.getString("dbgCategorySelection.text")); // NOI18N
        dbgCategorySelection.setName("dbgCategorySelection"); // NOI18N
        dbgCategorySelection.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dbgCategorySelectionActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(dbgRAcommonerValidity)
                    .addComponent(dbgCOPfitness)
                    .addComponent(dbgCategorySelection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 28, Short.MAX_VALUE)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 569, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(dbgRAcommonerValidity)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(dbgCOPfitness)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(dbgCategorySelection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 421, Short.MAX_VALUE))
                .addContainerGap())
        );

        tabbedPane.addTab(resourceMap.getString("jPanel2.TabConstraints.tabTitle"), jPanel2); // NOI18N

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addComponent(tabbedPane, javax.swing.GroupLayout.PREFERRED_SIZE, 829, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(21, Short.MAX_VALUE))
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addComponent(tabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 471, Short.MAX_VALUE)
                .addGap(4, 4, 4))
        );

        menuBar.setName("menuBar"); // NOI18N

        fileMenu.setText(resourceMap.getString("fileMenu.text")); // NOI18N
        fileMenu.setName("fileMenu"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(csp_gui.CSP_GUIApp.class).getContext().getActionMap(CSP_GUIView.class, this);
        exitMenuItem.setAction(actionMap.get("quit")); // NOI18N
        exitMenuItem.setName("exitMenuItem"); // NOI18N
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        helpMenu.setText(resourceMap.getString("helpMenu.text")); // NOI18N
        helpMenu.setName("helpMenu"); // NOI18N

        aboutMenuItem.setAction(actionMap.get("showAboutBox")); // NOI18N
        aboutMenuItem.setName("aboutMenuItem"); // NOI18N
        helpMenu.add(aboutMenuItem);

        menuBar.add(helpMenu);

        statusPanel.setName("statusPanel"); // NOI18N
        statusPanel.setPreferredSize(new java.awt.Dimension(850, 40));
        statusPanel.setRequestFocusEnabled(false);
        statusPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        statusMessageLabel.setName("statusMessageLabel"); // NOI18N
        statusPanel.add(statusMessageLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(214, 11, -1, -1));

        statusAnimationLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        statusAnimationLabel.setName("statusAnimationLabel"); // NOI18N
        statusPanel.add(statusAnimationLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(850, 11, -1, -1));

        progressBar.setName("progressBar"); // NOI18N
        statusPanel.add(progressBar, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 17, 820, -1));

        fchDataFile.setName("fchDataFile"); // NOI18N

        setComponent(mainPanel);
        setMenuBar(menuBar);
        setStatusBar(statusPanel);
    }// </editor-fold>//GEN-END:initComponents

    private void rdoDiscreteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rdoDiscreteActionPerformed
        chageEnabilityAllTextBoxes(true); // TODO add your handling code here:
}//GEN-LAST:event_rdoDiscreteActionPerformed

    private void rdoContinuousActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rdoContinuousActionPerformed
        chageEnabilityAllTextBoxes(true);
}//GEN-LAST:event_rdoContinuousActionPerformed

    private void btnTestActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTestActionPerformed
        // TODO add your handling code here:
        // TODO add your handling code here:
      
        //G1
        // total vars = 13
        // total constraints = 9+13=22
        //0 <= xi <= 1, i = 0-8, 
        //0 <=xi <= 100, i = 9,10,11
        //0 <= x12 <= 1.
//        this.txtConstraints.setText("22");
//        this.txtDecisionVar.setText("13");
//        this.txtMaxValues.setText("1,1,1,1,1,1,1,1,1,100,100,100,1");
//        this.txtMinValues.setText("0,0,0,0,0,0,0,0,0,0,0,0,0");
//        this.txtOjbective.setText("1");
//        this.txtGen.setText("1000");
//        this.txtPop.setText("100");
        
        
        //G2
        // total vars (n) = 20 (prefered)
        // total constraints = 20+2
        //0 <= xi <= 10, i = 0-n, 
//        this.txtConstraints.setText("22");
//        this.txtDecisionVar.setText("20");
//        this.txtMaxValues.setText("10");
//        this.txtMinValues.setText("0");
//        this.txtOjbective.setText("1");
//        this.txtGen.setText("1000");
//        this.txtPop.setText("100");
//        this.chkMinVal.setSelected(true);
//        this.chkMaxVal.setSelected(true);
//        this.chkMinValActionPerformed(evt);
//        this.chkMaxValActionPerformed(evt);
        
        //G3
        // total vars (n) = 10 (prefered)
        // total constraints = 1+10
        //0 <= xi <=1, i = 0-n, 
//        this.txtConstraints.setText("11");
//        this.txtDecisionVar.setText("10");
//        this.txtMaxValues.setText("1");
//        this.txtMinValues.setText("0");
//        this.txtOjbective.setText("1");
//        this.txtGen.setText("1000");
//        this.txtPop.setText("100");
//        this.chkMinVal.setSelected(true);
//        this.chkMaxVal.setSelected(true);
//        this.chkMinValActionPerformed(evt);
//        this.chkMaxValActionPerformed(evt);
        
        //G4
        // total vars (n) = 5
        // total constraints = 3+5
        //78 <= x0 <= 102
        //33 <= x1 <= 45
        //27 <= xi <= 45 ,i = 2, 3, 4    
//        this.txtConstraints.setText("8");
//        this.txtDecisionVar.setText("5");
//        this.txtMaxValues.setText("102,45,45,45,45");
//        this.txtMinValues.setText("78,33,27,27,27");
//        this.txtOjbective.setText("1");
//        this.txtGen.setText("1000");
//        this.txtPop.setText("100");
        
        //G5         
        // total vars (n) = 4
        // total constraints = 5+4
        // 0 <= xi <= 1200, i = 0, 1
        // -0.55<=xi<=0.55, i = 2, 3
//        this.txtConstraints.setText("9");
//        this.txtDecisionVar.setText("4");
//        this.txtMaxValues.setText("1200,1200,0.55,0.55");
//        this.txtMinValues.setText("0,0,-0.55,-0.55");
//        this.txtOjbective.setText("1");
//        this.txtGen.setText("100000");
//        this.txtPop.setText("25");
        
        //G6
        // total vars (n) = 2 
        // total constraints = 2+2
        // 13 <= x0 <= 100,
        // 0 <= x1 <= 100,
//        this.txtConstraints.setText("4");
//        this.txtDecisionVar.setText("2");
//        this.txtMaxValues.setText("100,100");
//        this.txtMinValues.setText("13,0");
//        this.txtOjbective.setText("1");
//        this.txtGen.setText("1000");
//        this.txtPop.setText("100");
        
        //G7
        // total vars (n) = 10
        // total constraints = 8+10
        // -10.0 <=xi <= 10.0, i = 0..9
//        this.txtConstraints.setText("18");
//        this.txtDecisionVar.setText("10");
//        this.txtMaxValues.setText("10");
//        this.txtMinValues.setText("-10");
//        this.txtOjbective.setText("1");
//        this.txtGen.setText("1000");
//        this.txtPop.setText("100");
//        this.chkMinVal.setSelected(true);
//        this.chkMaxVal.setSelected(true);
//        this.chkMinValActionPerformed(evt);
//        this.chkMaxValActionPerformed(evt);
        
        //G8
        // total vars (n) = 2 
        // total constraints = 2+2
        // 0 <= x0 <= 10,
        // 0 <= x1 <= 10,
//        this.txtConstraints.setText("4");
//        this.txtDecisionVar.setText("2");
//        this.txtMaxValues.setText("10, 10");
//        this.txtMinValues.setText("0, 0");
//        this.txtOjbective.setText("1");
//        this.txtGen.setText("1000");
//        this.txtPop.setText("100");
        
        //G9
        // total vars (n) = 7
        // total constraints = 4 + 7
        // -10<=xi<=10, i = 0..6
//        this.txtConstraints.setText("11");
//        this.txtDecisionVar.setText("7");
//        this.txtMaxValues.setText("10,10,10,10,10,10,10");
//        this.txtMinValues.setText("-10,-10,-10,-10,-10,-10,-10");
//        this.txtOjbective.setText("1");
//        this.txtGen.setText("1000");
//        this.txtPop.setText("100");
//        
        //G10
        // total vars (n) = 8
        // total constraints = 6+8
        // 100<=xi<=10,000 i = 1
        // 1000<=xi<=10,000 i = 2,3
        // 10<=xi<=1000 i = 4..8
//        this.txtConstraints.setText("14");
//        this.txtDecisionVar.setText("8");
//        this.txtMaxValues.setText("10000, 10000, 10000, 1000, 1000, 1000, 1000, 1000");
//        this.txtMinValues.setText("100, 1000, 1000, 10, 10, 10, 10, 10");
//        this.txtOjbective.setText("1");
//        this.txtGen.setText("1000");
//        this.txtPop.setText("100");
        
        //G11
        // total vars (n) = 2
        // total constraints = 1+2
        // -1<=xi<=1 i = 0,1
//        this.txtConstraints.setText("3");
//        this.txtDecisionVar.setText("2");
//        this.txtMaxValues.setText("1, 1");
//        this.txtMinValues.setText("-1, -1");
//        this.txtOjbective.setText("1");
//        this.txtGen.setText("1000");
//        this.txtPop.setText("100");
        
        //G12
        // total vars (n) = 3
        // total constraints = 3+729 
        // 0<=xi<=10 ii = 0,1,2
//        this.txtConstraints.setText("732");
//        this.txtDecisionVar.setText("3");
//        this.txtMaxValues.setText("10,10,10");
//        this.txtMinValues.setText("0,0,0");
//        this.txtOjbective.setText("1");
//        this.txtGen.setText("1000");
//        this.txtPop.setText("100");
        
        //alkyl
//        this.txtConstraints.setText("21");
//        this.txtDecisionVar.setText("14");
//        this.txtMaxValues.setText("2,1.6,1.2,5,2,0.93,0.95,12,4,1.62,1.01010101010101,1.01010101010101,1.11111111111111,1.01010101010101");
//        this.txtMinValues.setText("0,0,0,0,0,0.85,0.9,3.0,1.2,1.45,0.99,0.99,0.9,0.99");
//        this.txtOjbective.setText("1");
//        this.txtGen.setText("100000");
//        this.txtPop.setText("25");

        //hs109
        // total vars (n) = 9
        // total constraints = 11+9 = 20
        // 0<= x[ii] <= inf; ii = 0,1 
        // -.55 <= x[ii] <= .55; ii = 2,3
	// 196 <= x[ii] <= 252; ii = 4, 5, 6,
        // -400 <= x[ii] <= 800; ii = 7, 8
//        this.txtConstraints.setText("20"); //9+11
//        this.txtDecisionVar.setText("9");
//        this.txtMaxValues.setText("1.0e8,1.0e8, 0.55, 0.55, 252,252,252, 800, 800");
//        this.txtMinValues.setText("0,0, -0.55,-0.55, 196,196,196, -400,-400");
//        this.txtOjbective.setText("1");
//        this.txtGen.setText("1000");
//        this.txtPop.setText("25");
        
        // broyden
//        this.txtConstraints.setText("20");
//        this.txtDecisionVar.setText("10");
//        this.txtMaxValues.setText("1.0E8");
//        this.txtMinValues.setText("-1.0E8");
//        this.txtOjbective.setText("1");
//        this.txtGen.setText("1000");
//        this.txtPop.setText("25");
//        this.chkMinVal.setSelected(true);
//        this.chkMaxVal.setSelected(true);
//        this.chkMinValActionPerformed(evt);
//        this.chkMaxValActionPerformed(evt);
     
        
        this.txtGen.setText("1000");
        this.txtPop.setText("100");
        this.txtHardViosTolerance.setText("0");
        this.txtRearrangeCSP.setText("1");
        
        //G24_4
        // total vars (n) = 2
        // total constraints = 2+2
        // 0 <= x0 <= 3, 
        // 0<=x1<=4, 
//        this.txtConstraints.setText("4");
//        this.txtDecisionVar.setText("2");
//        this.txtMaxValues.setText("3,4");
//        this.txtMinValues.setText("0,0");
//        this.txtOjbective.setText("1");
//        this.txtGen.setText("1001");
//        this.txtPop.setText("100");
        
        //h77
        // total vars (n) = 5
        // total constraints = 3+5
        // 0 <= xi <= 4, ii = 0..4
//        this.txtConstraints.setText("8");
//        this.txtDecisionVar.setText("5");
//        this.txtMaxValues.setText("4,4,4,4,4");
//        this.txtMinValues.setText("0,0,0,0,0");
//        this.txtOjbective.setText("1");
//        this.txtGen.setText("1001");
//        this.txtPop.setText("100");
        
        //chem
        // total vars (n) = 5
        // total constraints = 5+5		
        // 0<= x[i] <= 1.0E8; i = 0..4
//        this.txtConstraints.setText("10");
//        this.txtDecisionVar.setText("5");
//        this.txtMaxValues.setText("1.0E8,1.0E8,1.0E8,1.0E8,1.0E8");
//        this.txtMinValues.setText("0,0,0,0,0");
//        this.txtOjbective.setText("1");
//        this.txtGen.setText("5000");
//        this.txtPop.setText("100");        
}//GEN-LAST:event_btnTestActionPerformed

    private void chkMinValActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkMinValActionPerformed
        // TODO add your handling code here:
        StringTokenizer min_str = new StringTokenizer(this.txtMinValues.getText().replaceAll(" ", ""),",");

        String str;
        String sameVal;

        if (this.chkMinVal.isSelected()){
            try {
                sameVal = min_str.nextElement().toString();
                str = sameVal;
                for (int i = 0; i < Integer.parseInt(this.txtDecisionVar.getText())-1; i++) {
                    str = str+","+sameVal;
                }
                this.txtMinValues.setText(str);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
}//GEN-LAST:event_chkMinValActionPerformed

    private void chkMaxValActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkMaxValActionPerformed
        // TODO add your handling code here:
        StringTokenizer min_str = new StringTokenizer(this.txtMaxValues.getText().replaceAll(" ", ""),",");

        String str;
        String sameVal;

        if (this.chkMaxVal.isSelected()){
            try {
                sameVal = min_str.nextElement().toString();
                str = sameVal;
                for (int i = 0; i < Integer.parseInt(this.txtDecisionVar.getText())-1; i++) {
                    str = str+","+sameVal;
                }
                this.txtMaxValues.setText(str);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
}//GEN-LAST:event_chkMaxValActionPerformed

        private Object castString(Class t, String data){
        Object obj;

        if (t.getName().contains("Double") || t.getName().contains("double")){
            obj = Double.parseDouble(data);
        }
        else if(t.getName().contains("Integer") || t.getName().contains("int")){
            obj = Integer.parseInt(data);
        }
        else{
            obj = null;
        }
        return obj;
    }
        
    private void btnStartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnStartActionPerformed

        UserInput userInput;
        Class c;

        try {
            if (this.rdoContinuous.isSelected()) {
                c = Double.class;
            } else if(this.rdoDiscrete.isSelected()){
                c = Integer.class;
            } else {
                throw new MyException("Select an option for type of data", "Incomplete Form", JOptionPane.WARNING_MESSAGE);
            }

            userInput = new UserInput(c, chkSaveChromes.isSelected());            
            userInput.totalConstraints = Integer.parseInt(this.txtConstraints.getText());
            userInput.totalDecisionVars = Integer.parseInt(this.txtDecisionVar.getText());
            userInput.totalObjectives = Integer.parseInt(this.txtOjbective.getText());
            userInput.solutionBy = this.solutionBy;
            StringTokenizer min_str = new StringTokenizer(this.txtMinValues.getText().replaceAll(" ", ""),",");
            StringTokenizer max_str = new StringTokenizer(this.txtMaxValues.getText().replaceAll(" ", ""),",");

            if(min_str.countTokens() != max_str.countTokens()){
                throw new Exception();
            }
            int sz; // it is a MUST
            sz = min_str.countTokens();

            for (int i = 0; i < sz; i++) {
                userInput.minVals.add(Double.valueOf(min_str.nextElement().toString()));
                userInput.maxVals.add(Double.valueOf(max_str.nextElement().toString()));
            }

            userInput.population = Integer.parseInt(this.txtPop.getText());
            userInput.generation = Integer.parseInt(this.txtGen.getText());

            //            int sz;
            //            String str = "";
            //            Object obj;
            //            sz = max_str.countTokens();
            //            for (int i = 0; i < sz; i++) {
            //                str = max_str.nextElement().toString();
            //                obj = castString(c, str);
            //                userInput_.maxVals.add(c.cast(obj));
            //            }

            userInput.validateData();
            System.out.println(userInput);

            cspProcess_ = new CspProcess(userInput);
            cspProcess_.bMatlabDraw = this.chkMatlabDraw.isSelected();

            new StartProcessThread();
            

        } catch (MyException e){
            e.showMessageBox();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Check your data!", "Incorrect Data", JOptionPane.ERROR_MESSAGE);
        } 
    }//GEN-LAST:event_btnStartActionPerformed

    private void btnDataActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDataActionPerformed
        class MyCustomFilter extends javax.swing.filechooser.FileFilter {
            @Override
            public boolean accept(File file) {
                // Allow only directories, or files with ".txt" extension
                return file.isDirectory() || file.getAbsolutePath().endsWith(".txt") 
                        || file.getAbsolutePath().endsWith(".dat")
                        || file.getAbsolutePath().endsWith(".col");
            }
            
            @Override
            public String getDescription() {
                // This description will be displayed in the dialog,
                // hard-coded = ugly, should be done via I18N
                return "Text documents (*.txt; *.dat; *.col)";
            }
        } 

        File f;
        try{
            f = new File(new File(".").getCanonicalPath());
            this.fchDataFile = new javax.swing.JFileChooser();
            this.fchDataFile.setFileFilter(new MyCustomFilter()); 
            this.fchDataFile.setCurrentDirectory(f);
        } catch (IOException e) {
            e.printStackTrace();
            Application.getInstance().exit();
        }
        
        
        
        int returnVal = this.fchDataFile.showOpenDialog(null);
        
        if(returnVal == JFileChooser.CANCEL_OPTION){
            return;
        }
        
        ExternalData edata;
        File file = null;
        //Scanner readFile = null;
        String errmsg = "File not found";
        Class c;
        
        try {
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                file = fchDataFile.getSelectedFile();
                System.out.println(file.getCanonicalPath().toString());
                //readFile = new Scanner(file);
            }else{
                //readFile = null;
                errmsg = "No File Selected";
                throw new FileNotFoundException(errmsg);
            }

        } catch (FileNotFoundException fnfe) {
            System.err.println(fnfe.getLocalizedMessage());
            MyException e = new MyException(errmsg, "Incorrect File",JOptionPane.INFORMATION_MESSAGE);
            e.showMessageBox();
            Application.getInstance().exit();
        } catch (IOException ioe){
            System.err.println(ioe.getLocalizedMessage());
            Application.getInstance().exit();
        }
 
        try{
            if (this.rdoContinuous.isSelected()) {
                c = Double.class;
            } else if(this.rdoDiscrete.isSelected()){
                c = Integer.class;
            } else {
                throw new MyException("Select an option for type of data", "Incomplete Form", JOptionPane.WARNING_MESSAGE);
            }
            
            edata = new TimeTableData(file.getAbsolutePath(), file.getName(), Integer.parseInt(this.txtPop.getText()), 
                    Integer.parseInt(this.txtGen.getText()), Integer.parseInt(txtCurPref.getText()), 
                    Integer.parseInt(txtPrevPref.getText()), chkSaveChromes.isSelected(), this.solutionBy, c,
                    Integer.parseInt(txtHardViosTolerance.getText()));
//            edata = null;

            System.out.println(edata.getUserInput());

            this.chageEnabilityAllButtons(false);            
            cspProcess_ = new CspProcess(edata);
            cspProcess_.bMatlabDraw = this.chkMatlabDraw.isSelected();
            ICHEAthread = new StartProcessThread();

            //<<ICHEA controls
            this.txtMaxTransGens.setText(String.valueOf(cspProcess_.maxTransitionGen));
            this.txtAcceptedRatioInc.setText(String.valueOf(cspProcess_.startAcceptedConstRatio));
            this.txtSameBestGen.setText(String.valueOf(cspProcess_.SAME_BEST_GENERATIONS));
            this.txtTabuGens.setText(String.valueOf(cspProcess_.maxTabuGens));
            this.txtRhoCOP.setText(String.valueOf(cspProcess_.rhoCOP));
            this.txtRhoCSP.setText(String.valueOf(cspProcess_.rhoCSP));
            this.txtLocalBestComp.setText(String.valueOf(cspProcess_.externalData_.localBestComparision));
            this.txtNoProgressLimit.setText(String.valueOf(CspProcess.NO_PROGRESS_LIMIT));
            //>>
            
            //<< RA controls
            this.txtRAcommonerAge.setText(String.valueOf(cspProcess_.RAmaxCommonerAge));
            this.txtRAdegInfluence.setText(String.valueOf(cspProcess_.RAdegreeOfInfluence));
            this.txtRAguruPopSize.setText(String.valueOf(cspProcess_.getRAguruSize()));
            //>>
            
            
            this.chageEnabilityAllButtons(true);
            System.out.println();
            //this.RAplay = (Boolean)this.intialRAstatus.getVal();
            btnRAcontrol.setText(getRAstatus());
            
            
        }catch(MyException me){
            me.showMessageBox();
        } catch (UnsupportedOperationException uoe){
            System.err.println(uoe.getLocalizedMessage());
        } catch (Exception e) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException ie) {
            }

            JOptionPane.showMessageDialog(null, "Check your data!", "Incorrect Data", JOptionPane.ERROR_MESSAGE);
            System.err.println(e.getLocalizedMessage());
            e.printStackTrace();
            Application.getInstance().exit();
            //e.printStackTrace();
        }
        // edata.readData();
    }//GEN-LAST:event_btnDataActionPerformed

    private void txtPopFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtPopFocusLost
        // TODO add your handling code here:
        try{
            if(Integer.valueOf(this.txtPop.getText()) == null){
                JOptionPane.showMessageDialog(null, "Enter Population", "Incorrect Data", JOptionPane.WARNING_MESSAGE);
            }else{
                this.btnData.setEnabled(true);
            }

            if(cspProcess_ != null){
                if(ICHEAthread.isAlive()){
                    resizePopulation(); 
                }
            }
             
        } catch (NumberFormatException e){
            JOptionPane.showMessageDialog(null, "Enter integer value for population", "Incorrect Data", JOptionPane.WARNING_MESSAGE);
            //this.txtPop.requestFocusInWindow();
            
        }
    }//GEN-LAST:event_txtPopFocusLost

    
    
    private void txtPopFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtPopFocusGained
        // TODO add your handling code here:
        this.txtPop.selectAll();
    }//GEN-LAST:event_txtPopFocusGained

    private void txtGenFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtGenFocusLost
        // TODO add your handling code here:
        this.btnData.setEnabled(true);
        if(cspProcess_ != null){
            cspProcess_.userInput_.generation = Integer.parseInt(this.txtGen.getText());
        }
    }//GEN-LAST:event_txtGenFocusLost

    private void rdoSatisfactionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rdoSatisfactionActionPerformed
        // TODO add your handling code here:
        this.solutionBy = Chromosome.BY_SATISFACTIONS;
    }//GEN-LAST:event_rdoSatisfactionActionPerformed

    private void rdoViolationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rdoViolationActionPerformed
        // TODO add your handling code here:
        this.solutionBy = Chromosome.BY_VIOLATIONS;
    }//GEN-LAST:event_rdoViolationActionPerformed

    private void rdoRoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rdoRoActionPerformed
        // TODO add your handling code here:
        this.solutionBy = Chromosome.BY_RHO;
    }//GEN-LAST:event_rdoRoActionPerformed

    private void rdoFitnessActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rdoFitnessActionPerformed
        // TODO add your handling code here:
        this.solutionBy = Chromosome.BY_FITNESS;
    }//GEN-LAST:event_rdoFitnessActionPerformed

    private void txtGenFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtGenFocusGained
        // TODO add your handling code here:
        this.txtGen.selectAll();
    }//GEN-LAST:event_txtGenFocusGained

    private void updateSolutionSpace(int val){        
        try {
            ICHEAthread.interrupt(); 
            System.out.println("*ICHEA thread interrupted from GUI...");
//            synchronized(starterThread){ // can remove these 4 lines of code...
//                System.out.println("*Now GUI is about to wait... state: "+starterThread.getState());
//                starterThread.wait();
//                System.out.println("*waiting finished... state: "+starterThread.getState());
//            }

            while(ICHEAthread.getState() != Thread.State.WAITING);
            
            System.out.println("*ICHEAthread current state: " + ICHEAthread.getState());
            if(cspProcess_ != null && ICHEAthread.getState() == Thread.State.WAITING){                       
                cspProcess_.changeSolutionSpaceSize(val);
                System.out.println("*Solution space changed by (" + val + ")");
            }
            synchronized(ICHEAthread){
                System.out.println("*b4 notifying ICHEA thread");
                ICHEAthread.notify();
                System.out.println("*after notifying ICHEA thread");
            }                       
        } catch (Exception ex) {
            ex.printStackTrace();
            Application.getInstance().exit();
        }    
        System.out.println("*GUI job done...");
    }
    
    
    
    private void updateHardConstVioTolerance(int val){        
        try {
            ICHEAthread.interrupt(); 
            System.out.println("*ICHEA thread interrupted from GUI...");

            while(ICHEAthread.getState() != Thread.State.WAITING);
            
            System.out.println("*ICHEAthread current state: " + ICHEAthread.getState());
            if(cspProcess_ != null && ICHEAthread.getState() == Thread.State.WAITING){                       
                cspProcess_.changeHardConstraintViolationTolerance(val);
                System.out.println("*Tolerance value for hard constraint violation changed by (" + val +")");
            }
            synchronized(ICHEAthread){
                System.out.println("*b4 notifying ICHEA thread");
                ICHEAthread.notify();
                System.out.println("*after notifying ICHEA thread");
            }                       
        } catch (Exception ex) {
            ex.printStackTrace();
            Application.getInstance().exit();
        }    
        System.out.println("*GUI job done...");
    }
    
    
    private void btnUpSSActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpSSActionPerformed
        // TODO add your handling code here:
        this.extendedSolutionSpaceCounter++;
        this.txtExtendedSScounter.setText(String.valueOf(extendedSolutionSpaceCounter));        
        updateSolutionSpace(1);
        
    }//GEN-LAST:event_btnUpSSActionPerformed

    private void bthDownSSActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bthDownSSActionPerformed
        // TODO add your handling code here:
        this.extendedSolutionSpaceCounter--;
//        if(this.extendedSolutionSpaceCounter<0){
//            this.extendedSolutionSpaceCounter = 0;
//        }
        this.txtExtendedSScounter.setText(String.valueOf(extendedSolutionSpaceCounter));
        updateSolutionSpace(-1);       
    }//GEN-LAST:event_bthDownSSActionPerformed

    private void txtHardViosToleranceFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtHardViosToleranceFocusLost
        // TODO add your handling code here:
//        if(cspProcess_ != null){
//            cspProcess_.userInput_.hardConstViosTolerance = Integer.parseInt(this.txtHardViosTolerance.getText());
//        }
        
        if(ICHEAthread != null){
            if(ICHEAthread.isAlive())
                updateHardConstVioTolerance(Integer.parseInt(this.txtHardViosTolerance.getText()));
        }
            
    }//GEN-LAST:event_txtHardViosToleranceFocusLost

    private void btnCSPrestructureActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCSPrestructureActionPerformed
        // TODO add your handling code here:
        if(ICHEAthread != null){
            if(ICHEAthread.isAlive())
                CSPrestructure(Integer.parseInt(this.txtRearrangeCSP.getText()));
        }
    }//GEN-LAST:event_btnCSPrestructureActionPerformed

    private void cboMutationStrategyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cboMutationStrategyActionPerformed
        // TODO add your handling code here:
        if(cspProcess_ != null){
//            cspProcess_.mutationID = this.cboMutationStrategy.getSelectedIndex();  
            JOptionPane.showMessageDialog(null, "cspProcess_.mutationID", "Deprecated..", JOptionPane.PLAIN_MESSAGE);
        }
    }//GEN-LAST:event_cboMutationStrategyActionPerformed

    private void btnRAcontrolActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRAcontrolActionPerformed
        // TODO add your handling code here:
//        Color initFc = btnRAcontrol.getBackground();

//        this.btnRAcontrol.setBackground(new Color(0, 204, 102));
        if(cspProcess_ != null){
            if(ICHEAthread.isAlive()){
                this.RAplay = !this.RAplay;
                controlRA(); 
            }
        }
//        this.btnRAcontrol.setForeground(initFc);
    }//GEN-LAST:event_btnRAcontrolActionPerformed

    private void btnStuckInLocalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnStuckInLocalActionPerformed
        // TODO add your handling code here:
        if(ICHEAthread != null){
            if(ICHEAthread.isAlive())
                ResolveLocalOptimal(this.cboResolveLocalOpt.getSelectedIndex());
        }
        
    }//GEN-LAST:event_btnStuckInLocalActionPerformed

    private void btnPrintSolActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPrintSolActionPerformed
        // TODO add your handling code here:
        if(cspProcess_ != null){
            cspProcess_.forcePrintSol = true;
        }
    }//GEN-LAST:event_btnPrintSolActionPerformed

    private void txtRAcommonerAgeFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtRAcommonerAgeFocusLost
        // TODO add your handling code here:
        try{
            int age;
            if(this.txtRAcommonerAge.getText().isEmpty()){
                return; // do nothing
            }else{
                age = Integer.valueOf(this.txtRAcommonerAge.getText());
            }

            if(cspProcess_ != null){
                if(ICHEAthread.isAlive()){
                    cspProcess_.RAmaxCommonerAge = age; 
                    System.out.println("RA commoners age changed to: " + age);
                }
            }
             
        } catch (NumberFormatException e){           
            JOptionPane.showMessageDialog(null, "Enter integer value for Age("+this.txtRAcommonerAge.getText()+")", "Incorrect Data", JOptionPane.WARNING_MESSAGE);
            //this.txtPop.requestFocusInWindow();
            
        }
    }//GEN-LAST:event_txtRAcommonerAgeFocusLost

    private void txtRAdegInfluenceFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtRAdegInfluenceFocusLost
        // TODO add your handling code here:
        try{
            int inf;
            if(this.txtRAdegInfluence.getText().isEmpty()){
                return; // do nothing
            }else{
                inf = Integer.valueOf(this.txtRAdegInfluence.getText());
            }

            if(cspProcess_ != null){
                if(ICHEAthread.isAlive()){
                    cspProcess_.RAdegreeOfInfluence = inf; 
                    System.out.println("Degree of Influence changed to: " + inf);
                }
            }
             
        } catch (NumberFormatException e){
            JOptionPane.showMessageDialog(null, "Enter integer value for Degree of Influence", "Incorrect Data", JOptionPane.WARNING_MESSAGE);
            //this.txtPop.requestFocusInWindow();
            
        }
    }//GEN-LAST:event_txtRAdegInfluenceFocusLost

    private void txtRAguruPopSizeFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtRAguruPopSizeFocusLost
        // TODO add your handling code here:
         try {
            int pop;

            if(this.txtRAguruPopSize.getText().isEmpty()){
                return; // do nothing
            }else{
                pop = Integer.valueOf(this.txtRAguruPopSize.getText());
            }
            
            ICHEAthread.interrupt(); 
            while(ICHEAthread.getState() != Thread.State.WAITING);
            if(cspProcess_ != null && ICHEAthread.getState() == Thread.State.WAITING){                       
                cspProcess_.setRAguruSize(pop);
                System.out.println("*RA guru population size changed to: " + pop);
            }
            synchronized(ICHEAthread){
                ICHEAthread.notify();
            }                       
        } catch (NumberFormatException e){
            JOptionPane.showMessageDialog(null, "Enter integer value for RA Guru Population.", "Incorrect Data", JOptionPane.WARNING_MESSAGE);         
        }catch (Exception ex) {
            ex.printStackTrace();
//            Application.getInstance().exit();
        }      
    }//GEN-LAST:event_txtRAguruPopSizeFocusLost

    private void txtMaxTransGensFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtMaxTransGensFocusLost
        // TODO add your handling code here:
        try{
            int gens;

            if(this.txtMaxTransGens.getText().isEmpty()){
                return; // do nothing
            }else{
                gens = Integer.valueOf(this.txtMaxTransGens.getText());
            }

            if(cspProcess_ != null){
                if(ICHEAthread.isAlive()){
                    cspProcess_.maxTransitionGen = gens; 
                    System.out.println("Max Transition Generations changed to: " + gens);
                }
            }
             
        } catch (NumberFormatException e){
            JOptionPane.showMessageDialog(null, "Enter integer value for Max Transition Generations.", "Incorrect Data", JOptionPane.WARNING_MESSAGE);
            //this.txtPop.requestFocusInWindow();
            
        }
        
    }//GEN-LAST:event_txtMaxTransGensFocusLost

    private void txtAcceptedRatioIncFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtAcceptedRatioIncFocusLost
        // TODO add your handling code here:
        try{
            double ratio;

            if(this.txtAcceptedRatioInc.getText().isEmpty()){
                return; // do nothing
            }else{
                ratio = Double.valueOf(this.txtAcceptedRatioInc.getText());
            }

            if(cspProcess_ != null){
                if(ICHEAthread.isAlive()){
                    cspProcess_.startAcceptedConstRatio = ratio; 
                    System.out.println("Accepted Ratio Increments changed to: " + ratio);
                }
            }
             
        } catch (NumberFormatException e){
            JOptionPane.showMessageDialog(null, "Enter double value for Accepted Ratio Increments.", "Incorrect Data", JOptionPane.WARNING_MESSAGE);
            //this.txtPop.requestFocusInWindow();
            
        }
    }//GEN-LAST:event_txtAcceptedRatioIncFocusLost

    private void txtSameBestGenFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtSameBestGenFocusLost
        // TODO add your handling code here:
                try{
            int sbg;

            if(this.txtSameBestGen.getText().isEmpty()){
                return; // do nothing
            }else{
                sbg = Integer.valueOf(this.txtSameBestGen.getText());
            }

            if(cspProcess_ != null){
                if(ICHEAthread.isAlive()){
                    cspProcess_.SAME_BEST_GENERATIONS = sbg; 
                    System.out.println("Same Best Generation changed to: " + sbg);
                }
            }
             
        } catch (NumberFormatException e){
            JOptionPane.showMessageDialog(null, "Enter int value for Same Best Generation.", "Incorrect Data", JOptionPane.WARNING_MESSAGE);     
        }
    }//GEN-LAST:event_txtSameBestGenFocusLost

    private void chkGuruKarmaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkGuruKarmaActionPerformed
        // TODO add your handling code here
        if(!this.chkGuruKarma.isSelected()){
            return;
        }
        try {
            int pop;

            if(this.txtRAguruPopSize.getText().isEmpty()){
                return; // do nothing
            }else{
                pop = Integer.valueOf(this.txtRAguruPopSize.getText());
            }
            
            ICHEAthread.interrupt(); 
            while(ICHEAthread.getState() != Thread.State.WAITING);
            if(cspProcess_ != null && ICHEAthread.getState() == Thread.State.WAITING){                       
                cspProcess_.RAcommunitySize = 0;
                System.out.println("*RA guru population size changed to: " + pop);
            }
            synchronized(ICHEAthread){
                ICHEAthread.notify();
            }                       
        }catch (Exception ex) {
            ex.printStackTrace();
//            Application.getInstance().exit();
        }        
    }//GEN-LAST:event_chkGuruKarmaActionPerformed

    private void txtTabuGensFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtTabuGensFocusLost
        // TODO add your handling code here:
        try{
            int tabuGens;

            if(this.txtTabuGens.getText().isEmpty()){
                return; // do nothing
            }else{
                tabuGens = Integer.valueOf(this.txtTabuGens.getText());
            }

            if(cspProcess_ != null){
                if(ICHEAthread.isAlive()){
                    cspProcess_.maxTabuGens = tabuGens; 
                    System.out.println("Tabu Gens changed to: " + tabuGens);
                }
            }
             
        } catch (NumberFormatException e){
            JOptionPane.showMessageDialog(null, "Enter integer value for Tabu Gens.", "Incorrect Data", JOptionPane.WARNING_MESSAGE);
            //this.txtPop.requestFocusInWindow();
            
        }
    }//GEN-LAST:event_txtTabuGensFocusLost

    private void txtRhoCOPFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtRhoCOPFocusLost
        // TODO add your handling code here:
        try{
            double rho;

            if(this.txtRhoCOP.getText().isEmpty()){
                return; // do nothing
            }else{
                rho = Double.valueOf(this.txtRhoCOP.getText());
            }

            if(cspProcess_ != null){
                if(ICHEAthread.isAlive()){
                    cspProcess_.rhoCOP = rho; 
                    System.out.println("Rho for COP changed to: " + rho);
                }
            }
             
        } catch (NumberFormatException e){
            JOptionPane.showMessageDialog(null, "Enter double value for Rho value.", "Incorrect Data", JOptionPane.WARNING_MESSAGE);
            //this.txtPop.requestFocusInWindow();
            
        }
    }//GEN-LAST:event_txtRhoCOPFocusLost

    private void txtRhoCSPFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtRhoCSPFocusLost
        // TODO add your handling code here:
        try{
            double rho;

            if(this.txtRhoCSP.getText().isEmpty()){
                return; // do nothing
            }else{
                rho = Double.valueOf(this.txtRhoCSP.getText());
            }

            if(cspProcess_ != null){
                if(ICHEAthread.isAlive()){
                    cspProcess_.rhoCSP = rho; 
                    System.out.println("Rho for COP changed to: " + rho);
                }
            }
             
        } catch (NumberFormatException e){
            JOptionPane.showMessageDialog(null, "Enter double value for Rho value.", "Incorrect Data", JOptionPane.WARNING_MESSAGE);           
        }
    }//GEN-LAST:event_txtRhoCSPFocusLost

    private void txtLocalBestCompFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtLocalBestCompFocusLost
        // TODO add your handling code here:
        try{
            double percent;

            if(this.txtLocalBestComp.getText().isEmpty()){
                return; // do nothing
            }else{
                percent = Double.valueOf(this.txtLocalBestComp.getText());
            }

            if(cspProcess_ != null){
                if(ICHEAthread.isAlive()){
                    cspProcess_.externalData_.localBestComparision = percent; 
                    System.out.println("local best comparision changed to: " + percent);
                }
            }
             
        } catch (NumberFormatException e){
            JOptionPane.showMessageDialog(null, "Enter double value for local best comparision.", "Incorrect Data", JOptionPane.WARNING_MESSAGE);           
        }
    }//GEN-LAST:event_txtLocalBestCompFocusLost

    private void dbgRAcommonerValidityActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dbgRAcommonerValidityActionPerformed
        // TODO add your handling code here:      
        if(cspProcess_ != null){
            if(ICHEAthread.isAlive()){
                if(this.dbgRAcommonerValidity.isSelected()){
                    txaDebug.append("Ready to print RAcommoner Validity...\n\n");
                    CspProcess.Debug.RAcommonerValidity = true;
                }else{
                    CspProcess.Debug.RAcommonerValidity = false;
                }
            }
        }
    }//GEN-LAST:event_dbgRAcommonerValidityActionPerformed

    private void dbgCOPfitnessActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dbgCOPfitnessActionPerformed
        // TODO add your handling code here:
        if(cspProcess_ != null){
            if(ICHEAthread.isAlive()){
                if(this.dbgCOPfitness.isSelected()){
                    txaDebug.append("Ready to print COP fitness (NOT YET READY)...\n\n");
                    CspProcess.Debug.COPfitDisp = true;
                }else{
                    CspProcess.Debug.COPfitDisp = false;
                }
            }
        }
    }//GEN-LAST:event_dbgCOPfitnessActionPerformed

    private void dbgCategorySelectionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dbgCategorySelectionActionPerformed
        // TODO add your handling code here:
        if(cspProcess_ != null){
            if(ICHEAthread.isAlive()){
                if(this.dbgCategorySelection.isSelected()){
                    txaDebug.append("Ready to print indices for selection through categorization [NOT YET READY]...\n\n");
                    CspProcess.Debug.categoryList = true;
                }else{
                    CspProcess.Debug.categoryList = false;
                }                                      
            }
        }
    }//GEN-LAST:event_dbgCategorySelectionActionPerformed

    private void txaDebugFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txaDebugFocusGained
        // TODO add your handling code here:
        caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
    }//GEN-LAST:event_txaDebugFocusGained

    private void txaDebugFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txaDebugFocusLost
        // TODO add your handling code here:
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        txaDebug.setText(txaDebug.getText());//necessary otherwise auto scroll does NOT work
    }//GEN-LAST:event_txaDebugFocusLost

    private void txtRAfullInfluenceFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtRAfullInfluenceFocusLost
        // TODO add your handling code here:
        try{
            double fullInf;
            if(this.txtRAfullInfluence.getText().isEmpty()){
                return; // do nothing
            }else{
                fullInf = Double.valueOf(this.txtRAfullInfluence.getText());
            }

            if(cspProcess_ != null){
                if(ICHEAthread.isAlive()){
                    cspProcess_.RAfullInfluencePer = fullInf; 
                    System.out.println("Full Percent Influence changed to: " + fullInf);
                }
            }
             
        } catch (NumberFormatException e){
            JOptionPane.showMessageDialog(null, "Enter double value for the full Influence", "Incorrect Data", JOptionPane.WARNING_MESSAGE);
        } 
    }//GEN-LAST:event_txtRAfullInfluenceFocusLost

    private void cboResolveLocalOptActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cboResolveLocalOptActionPerformed
        // TODO add your handling code here:
        JOptionPane.showMessageDialog(null, "<html>Resolve level ("+ cboResolveLocalOpt.getSelectedIndex()+") noted.<BR>Now click <B>&lt;&lt;Attemp>></B> button to start resolving local opt solution.",
                "Local Optimal Situation",JOptionPane.INFORMATION_MESSAGE);
    }//GEN-LAST:event_cboResolveLocalOptActionPerformed

    private void txtNoProgressLimitFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtNoProgressLimitFocusLost
        // TODO add your handling code here:
        try{
            int noProgressLimit;
            if(this.txtNoProgressLimit.getText().isEmpty()){
                return; // do nothing
            }else{
                noProgressLimit = Integer.valueOf(this.txtNoProgressLimit.getText());
            }

            if(cspProcess_ != null){
                if(ICHEAthread.isAlive()){
                    CspProcess.NO_PROGRESS_LIMIT = noProgressLimit; 
                    System.out.println("No progress limit changed to: " + noProgressLimit);
                }
            }
             
        } catch (NumberFormatException e){
            JOptionPane.showMessageDialog(null, "Enter integer value for the no progress limit", "Incorrect Data", JOptionPane.WARNING_MESSAGE);
        } 
    }//GEN-LAST:event_txtNoProgressLimitFocusLost

private void chkMatlabDrawActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkMatlabDrawActionPerformed
// TODO add your handling code here:
    try{
        if(cspProcess_ != null){
            if(ICHEAthread.isAlive()){
                cspProcess_.bMatlabDraw = this.chkMatlabDraw.isSelected(); 
                if(cspProcess_.bMatlabDraw)
                    System.out.println("\nMatlab Draw (Re)Started!");
                else
                    System.out.println("\nMatlab Draw Stopped!");
            }
        }

    } catch (NumberFormatException e){
        JOptionPane.showMessageDialog(null, "Enter integer value for the no progress limit", "Incorrect Data", JOptionPane.WARNING_MESSAGE);
    } 
    
    
}//GEN-LAST:event_chkMatlabDrawActionPerformed

    public void printDebug(String info){
        this.txaDebug.setText(info);
    }
    
    private String getRAstatus(){
        String status;
        if(this.RAplay){
            status = "* RA Running >";
        }else{
            status = "* RA Paused ||";
        }
        return status;
    }
    /**
     * 
     * @param play true - Restart RA, false - Pause RA 
     */
    private void controlRA(){
        this.btnRAcontrol.setText(getRAstatus());
            
        try {
            ICHEAthread.interrupt(); 
            System.out.println("*ICHEA thread interrupted from GUI...");

            while(ICHEAthread.getState() != Thread.State.WAITING);
            
            if(cspProcess_ != null && ICHEAthread.getState() == Thread.State.WAITING){                       
                cspProcess_.changeRAcontrol(this.RAplay);
                System.out.println(getRAstatus());
            }
            synchronized(ICHEAthread){
                ICHEAthread.notify();
            }                       
        } catch (Exception ex) {
            ex.printStackTrace();
            Application.getInstance().exit();
        }    
    }
    
    private void ResolveLocalOptimal(final int level){        
        try {
            ICHEAthread.interrupt(); 
            System.out.println("\n* ICHEA thread interrupted from GUI...");

            while(ICHEAthread.getState() != Thread.State.WAITING);
            if(cspProcess_ != null && ICHEAthread.getState() == Thread.State.WAITING){                       
                cspProcess_.resolveLocalOpt(level);
                System.out.println("* Attempt to resolve local optima ...");
            }
            synchronized(ICHEAthread){                
                ICHEAthread.notify();                
            }                       
        } catch (Exception ex) {
            ex.printStackTrace();
            Application.getInstance().exit();
        }    
    }
    
    private void resizePopulation(){        
        try {
            ICHEAthread.interrupt(); 
            System.out.println("\n* ICHEA thread interrupted from GUI...");
                       
            while(ICHEAthread.getState() != Thread.State.WAITING);
            if(cspProcess_ != null && ICHEAthread.getState() == Thread.State.WAITING){                       
                cspProcess_.changePopulationSize(Integer.parseInt(txtPop.getText())); //txtPop's number format is already checked in txtPop_lostFocus Event
                System.out.println("*Population size changed by ("+txtPop.getText()+")");
            }
            synchronized(ICHEAthread){                
                ICHEAthread.notify();                
            }                       
        } catch (Exception ex) {
            ex.printStackTrace();
            Application.getInstance().exit();
        }    
    }
    
    private void CSPrestructure(int val){        
        try {
            ICHEAthread.interrupt(); 
            System.out.println("*ICHEA thread interrupted from GUI...");

            while(ICHEAthread.getState() != Thread.State.WAITING);
            
            System.out.println("*ICHEAthread current state: " + ICHEAthread.getState());
            if(cspProcess_ != null && ICHEAthread.getState() == Thread.State.WAITING){                       
                cspProcess_.changeCSPspace(val,this.chkOnlyCSP.isSelected());
                System.out.println("*Tolerance value for hard constraint violation changed...");
            }
            synchronized(ICHEAthread){
                System.out.println("*b4 notifying ICHEA thread");
                ICHEAthread.notify();
                System.out.println("*after notifying ICHEA thread");
            }                       
        } catch (Exception ex) {
            ex.printStackTrace();
            Application.getInstance().exit();
        }    
        System.out.println("*GUI job done...");
    }
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel PanelSolveBy;
    private javax.swing.JPanel RApanel;
    private javax.swing.ButtonGroup bgrpDataType;
    private javax.swing.ButtonGroup bgrpSolveBy;
    private javax.swing.JButton bthDownSS;
    private javax.swing.JButton btnCSPrestructure;
    private javax.swing.JButton btnData;
    private javax.swing.JButton btnPrintSol;
    private javax.swing.JButton btnRAcontrol;
    private javax.swing.JButton btnStart;
    private javax.swing.JButton btnStuckInLocal;
    private javax.swing.JButton btnTest;
    private javax.swing.JButton btnUpSS;
    private javax.swing.JComboBox cboMutationStrategy;
    private javax.swing.JComboBox cboResolveLocalOpt;
    private javax.swing.JCheckBox chkGuruKarma;
    private javax.swing.JCheckBox chkMatlabDraw;
    private javax.swing.JCheckBox chkMaxVal;
    private javax.swing.JCheckBox chkMinVal;
    private javax.swing.JCheckBox chkOnlyCSP;
    private javax.swing.JCheckBox chkSaveChromes;
    private javax.swing.JCheckBox dbgCOPfitness;
    private javax.swing.JCheckBox dbgCategorySelection;
    private javax.swing.JCheckBox dbgRAcommonerValidity;
    private javax.swing.JFileChooser fchDataFile;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JLabel lblPrefSuggestion;
    private javax.swing.JLabel lblVersion;
    private javax.swing.JPanel mainControlPanel;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JRadioButton rdoContinuous;
    private javax.swing.JRadioButton rdoDiscrete;
    private javax.swing.JRadioButton rdoFitness;
    private javax.swing.JRadioButton rdoRo;
    private javax.swing.JRadioButton rdoSatisfaction;
    private javax.swing.JRadioButton rdoViolation;
    private javax.swing.JLabel statusAnimationLabel;
    private javax.swing.JLabel statusMessageLabel;
    private javax.swing.JPanel statusPanel;
    private javax.swing.JTabbedPane tabbedPane;
    private javax.swing.JTextArea txaDebug;
    private javax.swing.JTextField txtAcceptedRatioInc;
    private javax.swing.JTextField txtConstraints;
    private javax.swing.JTextField txtCurPref;
    private javax.swing.JTextField txtDecisionVar;
    private javax.swing.JTextField txtExtendedSScounter;
    private javax.swing.JTextField txtGen;
    private javax.swing.JTextField txtHardViosTolerance;
    private javax.swing.JTextField txtLocalBestComp;
    private javax.swing.JTextField txtMaxTransGens;
    private javax.swing.JTextField txtMaxValues;
    private javax.swing.JTextField txtMinValues;
    private javax.swing.JTextField txtNoProgressLimit;
    private javax.swing.JTextField txtOjbective;
    private javax.swing.JTextField txtPop;
    private javax.swing.JTextField txtPrevPref;
    private javax.swing.JTextField txtRAcommonerAge;
    private javax.swing.JTextField txtRAdegInfluence;
    private javax.swing.JTextField txtRAfullInfluence;
    private javax.swing.JTextField txtRAguruPopSize;
    private javax.swing.JTextField txtRearrangeCSP;
    private javax.swing.JTextField txtRhoCOP;
    private javax.swing.JTextField txtRhoCSP;
    private javax.swing.JTextField txtSameBestGen;
    private javax.swing.JTextField txtTabuGens;
    // End of variables declaration//GEN-END:variables

    private final Timer messageTimer;
    private final Timer busyIconTimer;
    private final Icon idleIcon;
    private final Icon[] busyIcons = new Icon[15];
    private int busyIconIndex = 0;

    private JDialog aboutBox;
}
