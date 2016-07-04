package com.paulmunly.speedreader;

import com.paulmunly.speedreader.SpeedReaderResource.Word;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * This creates the Swing-based GUI for the SpeedReader application
 * TODO: Determine if a curses or similar library is available for Java
 * TODO: Port to Android?
 * @author Paul Munly <paul@paulmunly.com>
 */
public class SpeedReaderGUI extends JFrame implements KeyListener {
    
    private static final long serialVersionUID = -6814961319886508942L;
    private SpeedReaderResource SRR;
    private JMenuBar theMenuBar;
    private JMenu fileMenu;
    private JMenu bookmarksMenu;
    private JMenu helpMenu;
    private JMenuItem fileOpenItem;
    private JMenuItem fileCloseItem;
    private JMenuItem fileQuitItem;
    private ImageIcon playIcon;
    private ImageIcon pauseIcon;
    private ImageIcon backIcon;
    private ImageIcon forwardIcon;
    private ImageIcon bookmarkIcon;
    private Dimension buttonDimension;
    private JLabel lblWord;
    private JLabel lblStatus;
    private JTextArea taWPM;
    private JSpinner spinWPM;
    private JButton btnPlayPause;
    private JButton btnBack;
    private JButton btnForward;
    private JButton btnBookmark;
    private JPanel thePanel;
    private JPanel southPanel;
    private JPanel westPanel;
    private JPanel centerPanel;
    private JPanel eastPanel;
    private JProgressBar progressBar;
    private Thread wordThread;
    private GridBagLayout gridBag;
    private int WPM;
    private int BACK_MNEMONIC;
    private int FORWARD_MNEMONIC;
    private int PLAY_PAUSE_MNEMONIC;
    private int BOOKMARK_MNEMONIC;
    private static final int DEFAULTFRAMEWIDTH = 800;
    private static final int DEFAULTFRAMEHEIGHT = 600;
    private int FRAMEWIDTH;
    private int FRAMEHEIGHT;
    private boolean USERCHANGEDWINDOWSIZE = false;
    private boolean FULLSCREEN;
    
    /**
     * Creates a new SpeedReaderGUI without an input.  This will eventually open an 'introduction' file
     * explaining how to use the program. After the user has loaded their first file it will subsequently
     * automatically load the user's last opened file.
     */
    public SpeedReaderGUI() {
        this.readConfig();
        this.initComponents();
    }
    
    /**
     * Loads the GUI, using srr as the SpeedReaderResource.
     * @param srr The SpeedReaderResource the user elected to read (or the last one read)
     */
    public SpeedReaderGUI(SpeedReaderResource srr) {
        this.SRR = srr;
        this.readConfig();
        this.initComponents();
        if(this.spinWPM.hasFocus())
            this.getContentPane().requestFocusInWindow();
    }
    
    /**
     * Loads the GUI, using srr as the SpeedReaderResource and wpm to set the initial Words Per Minute.
     * @param srr The SpeedReaderResource the user elected to read (or the last one read)
     * @param wpm the desired Words Per Minute
     */
    public SpeedReaderGUI(SpeedReaderResource srr, int wpm) {
        this.SRR = srr;
        this.WPM = wpm;
        this.readConfig();
        this.initComponents();
        if(this.spinWPM.hasFocus())
            this.getContentPane().requestFocusInWindow();

    }
    
    private void initComponents() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            /*
            for(UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
            */
            addKeyListener(this);
            
            int red = UIManager.getColor("Button.background").getRed();
            int green = UIManager.getColor("Button.background").getGreen();
            int blue = UIManager.getColor("Button.background").getBlue();
            int alpha = UIManager.getColor("Button.background").getAlpha();
            Color bgcolor = new Color(red, green, blue, alpha);
                        
            this.setTitle("Speed Reader version " + SpeedReader.getVersion());
            this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            
            this.theMenuBar = new JMenuBar();
            this.fileMenu = createMenu("File", 'f');
            this.fileOpenItem = new JMenuItem("Open File...", 'o');
            this.fileCloseItem = new JMenuItem("Close File...", 'c');
            this.fileQuitItem = new JMenuItem("Exit SpeedReader", 'x');
            this.fileMenu.add(this.fileOpenItem);
            this.fileMenu.add(this.fileCloseItem);
            this.fileMenu.add(this.fileQuitItem);
            
            this.bookmarksMenu = createMenu("Bookmarks", 'b');
            
            this.helpMenu = createMenu("Help", 'h');
            
            this.theMenuBar.add(this.fileMenu);
            this.theMenuBar.add(this.bookmarksMenu);
            this.theMenuBar.add(Box.createHorizontalGlue());
            this.theMenuBar.add(this.helpMenu);
            
            this.setJMenuBar(this.theMenuBar);
            
            this.backIcon = new ImageIcon("media/back.png");
            this.forwardIcon = new ImageIcon("media/forward.png");
            this.playIcon = new ImageIcon("media/play.png");
            this.pauseIcon = new ImageIcon("media/pause.png");
            this.bookmarkIcon = new ImageIcon("media/bookmark.png");
            this.buttonDimension = new Dimension(64, 64);
            this.gridBag = new GridBagLayout();
            GridBagConstraints gbc = new GridBagConstraints();
            this.thePanel = new JPanel(this.gridBag);
            this.thePanel.setBackground(Color.WHITE);
            
            this.lblWord = new JLabel();
            if(SpeedReader.inDebug())
                this.lblWord.setBorder(new javax.swing.border.EtchedBorder());
            this.lblWord.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 64));
            this.lblWord.setHorizontalAlignment(SwingConstants.CENTER);
            this.lblWord.setBackground(Color.WHITE);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.weightx = 1.0;
            gbc.weighty = 1.0;
            gbc.gridwidth = 3;
            this.thePanel.add(this.lblWord, gbc);
            
            this.southPanel = new JPanel(new GridLayout(1, 3));
            
            this.westPanel = new JPanel();
            if(SpeedReader.inDebug())
                this.westPanel.setBorder(new javax.swing.border.EtchedBorder());
            this.taWPM = new JTextArea();
            this.taWPM.setText("Words\nPer\nMinute: ");
            this.taWPM.setEditable(false);
            this.taWPM.setFocusable(false);
            this.taWPM.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
            this.taWPM.setBackground(bgcolor);
            this.westPanel.add(this.taWPM);
            this.spinWPM = new JSpinner(new SpinnerNumberModel(getWPM(), 0, 2500, 50));
            this.spinWPM.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 54));
            this.spinWPM.setPreferredSize(new Dimension(150, 80));
            this.spinWPM.setRequestFocusEnabled(false);
            this.westPanel.add(this.spinWPM);

            this.centerPanel = new JPanel(new GridLayout(1,4));
            if(SpeedReader.inDebug())
                this.centerPanel.setBorder(new javax.swing.border.EtchedBorder());
            this.btnBack = createButton("btnBack", this.backIcon, this.buttonDimension, "Pause and Go Back one word.", this.BACK_MNEMONIC);
            this.centerPanel.add(this.btnBack);
            this.btnPlayPause = createButton("btnPlayPause", this.playIcon, this.buttonDimension, "Start and Stop playback", this.PLAY_PAUSE_MNEMONIC);
            this.centerPanel.add(this.btnPlayPause);
            this.btnForward = createButton("btnForward", this.forwardIcon, this.buttonDimension, "Pause and Go Forward one word.", this.FORWARD_MNEMONIC);
            this.centerPanel.add(this.btnForward);                
            this.btnBookmark = createButton("btnBookmark", this.bookmarkIcon, this.buttonDimension, "Bookmark your Position and Pause Playback", this.BOOKMARK_MNEMONIC);
            this.centerPanel.add(this.btnBookmark);
            
            this.eastPanel = new JPanel();
            BoxLayout eastBox = new BoxLayout(this.eastPanel, BoxLayout.X_AXIS);
            if(SpeedReader.inDebug())
                this.eastPanel.setBorder(new javax.swing.border.EtchedBorder());
            this.eastPanel.setLayout(eastBox);
            this.lblStatus = new JLabel("Status Messages Will Go Here");
            this.eastPanel.add(this.lblStatus);
            
            this.southPanel.add(this.westPanel);
            this.southPanel.add(this.centerPanel);
            this.southPanel.add(this.eastPanel);
            
            gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.gridx = 0;
            gbc.gridy = 2;
            gbc.weightx = 1.0;
            gbc.weighty = 0.0;
            gbc.gridwidth = 3;
            this.thePanel.add(this.southPanel, gbc);

            this.progressBar = new JProgressBar();
            gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.gridx = 0;
            gbc.gridy = 1;
            gbc.weightx = 1.0;
            gbc.weighty = 0.0;
            gbc.gridwidth = 3;
            this.thePanel.add(this.progressBar, gbc);
            this.progressBar.setMaximum((int)this.SRR.getResourceLength());
            this.progressBar.setValue(0);
            
            
            
            this.add(this.thePanel);
            this.setBounds(100, 100, SpeedReaderGUI.DEFAULTFRAMEWIDTH, SpeedReaderGUI.DEFAULTFRAMEHEIGHT);
            this.setMinimumSize(new Dimension(SpeedReaderGUI.DEFAULTFRAMEWIDTH, SpeedReaderGUI.DEFAULTFRAMEHEIGHT));
            
            this.btnPlayPause.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    btnPlayPauseClicked(e);
                }
            });

            this.btnBack.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    btnBackClicked(e);
                }
            });
            
            this.btnForward.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    btnForwardClicked(e);
                }
            });

            this.btnBookmark.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    btnBookmarkClicked(e);
                }
            });

            this.spinWPM.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    setWPM();
                }
            });
            
            this.fileQuitItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    fileQuitItemActionPerformed(e);
                }
            });
            
            this.fileOpenItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    fileOpenItemActionPerformed(e);
                }
            });
            
            this.fileCloseItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    fileCloseItemActionPerformed(e);
                }
            });
            
            //Here we are adding code to determine when our window is resized
            this.getContentPane().addHierarchyBoundsListener(new HierarchyBoundsListener() {
                @Override
                public void ancestorMoved(HierarchyEvent he) {
                    // Do Nothing -- the user (hopefully) knows where they put the GUI
                }
                
                @Override
                public void ancestorResized(HierarchyEvent he) {
                    updateWindowsize(he);
                }
            });
            
            this.wordThread = new Thread(new WordLoop(this.SRR, this.SRR.getDelay(this.WPM)));
            this.pack();
            this.setFullScreen(this.FULLSCREEN);
            this.requestFocusInWindow();
        
        } catch (UnsupportedLookAndFeelException | ClassNotFoundException | 
        		 InstantiationException | IllegalAccessException e) {
            //TODO - Handle the exception
            e.printStackTrace();
        }
    }
    
    /*
     * TODO: Make this recognize when a user changes the size vs. the system doing so.
     * Currently with USERCHANGEDWINDOWSIZE = true; uncommented, we change the window's size whenever it's changed (system or otherwise)
     */
    protected void updateWindowsize(HierarchyEvent he) {
        Rectangle r = he.getChanged().getBounds();
        
        if(SpeedReader.inDebug())
            System.out.println("DEBUG: Window Size Changed to: " + r.width + "x" + r.height + ".");
        
        //this.USERCHANGEDWINDOWSIZE = true;
        this.FRAMEHEIGHT = r.height;
        this.FRAMEWIDTH = r.width;
    }
    
    protected void fileCloseItemActionPerformed(ActionEvent e) {
        
    }
    
    protected void fileOpenItemActionPerformed(ActionEvent e) {
        
    }
    
    protected void fileQuitItemActionPerformed(ActionEvent e) {
        if(this.wordThread.isAlive() && !this.wordThread.isInterrupted()) {
            this.interruptThread();
            this.btnPlayPauseSwapIcon();
        }
        JOptionPane exitPane = new javax.swing.JOptionPane("Really Exit Speed Reader?", JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION);
        JDialog dialog = exitPane.createDialog(this, "Exit Speed Reader");
        dialog.setVisible(true);
        Object selectedValue = exitPane.getValue();
        if(selectedValue.equals(JOptionPane.YES_OPTION)) {
            this.SRR.closeResource();
            System.exit(0);
        }
    }
    
    @Override
    public void keyTyped(KeyEvent e) {}     //Ignore
    
    @Override
    public void keyPressed(KeyEvent e) {}     //Ignore
    
    @Override
    public void keyReleased(KeyEvent e) {
        if(e.getKeyCode() == this.PLAY_PAUSE_MNEMONIC) {
            this.btnPlayPauseSwapIcon();
            this.restartThread();
            this.requestFocusInWindow();
        } else if(e.getKeyCode() == this.BACK_MNEMONIC) {
            if(SpeedReader.inDebug())
                updateStatus("DEBUG: <Back> Released");
            
            this.btnBackClicked(null);
        } else if(e.getKeyCode() == this.FORWARD_MNEMONIC) {
                if(SpeedReader.inDebug())
                    updateStatus("DEBUG: <Forward> Released");
                
                this.btnForwardClicked(null);
        } else if(e.getKeyCode() == this.BOOKMARK_MNEMONIC) {
            if(SpeedReader.inDebug())
                updateStatus("DEBUG: <Bookmark> Released");
            
            this.btnBookmarkClicked(null);
        } else if(e.getKeyCode() == KeyEvent.VK_F3) {
            setFullScreen(!this.FULLSCREEN);
            this.requestFocusInWindow();
        } else {
            if(SpeedReader.inDebug())
                updateStatus("DEBUG: Key Typed = " + e.getKeyChar());
        }
    }
    
    private void btnBackClicked(ActionEvent e) {
        this.btnPlayPause.setIcon(this.playIcon);
        showPreviousWord();
        requestFocusInWindow();
        if(!Thread.interrupted()) {
            interruptThread();
        }
    }
    
    private void btnForwardClicked(ActionEvent e) {
        this.btnPlayPause.setIcon(this.playIcon);
        showNextWord();
        requestFocusInWindow();
        if(!Thread.interrupted())
            interruptThread();
    }
    
    private void btnPlayPauseClicked(ActionEvent e) {
        if(SpeedReader.inDebug())
            updateStatus("DEBUG: btnPlayPauseClicked");
        
        btnPlayPauseSwapIcon();
        restartThread();
        requestFocusInWindow();
    }
    
    /**
     * Executes an ActionEvent when btnBookmark is clicked
     * @param e the ActionEvent associated with the action
     */
    private void btnBookmarkClicked(ActionEvent e) {
        if(!Thread.interrupted())
            interruptThread();
        this.btnPlayPause.setIcon(this.playIcon);
        if(this.SRR.setBookmark()) {
            Word word = this.SRR.getCurrentWord();
            String pos = "Line: " + word.getLine() + " Position: " + word.getPosition()
                    + " ..." + word.getPreviousWord().getText()
                    + " " + word.getText()
                    + " " + word.getNextWord().getText() + "...";
            JMenuItem jmi = createBookmarkJMenuItem(word, pos);
            this.bookmarksMenu.add(jmi);
            updateStatus("New Bookmark created!");
        } else {
            updateStatus("Bookmark already exists!");
        }
        requestFocusInWindow();
    }
    
    private JMenuItem createBookmarkJMenuItem(final Word word, String text) {
        JMenuItem jmi = new JMenuItem(text);
        jmi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    bookmarkMenuItemActionPerformed(e, word);
                } catch (NullPointerException npe) {
                    updateStatus("Error");
                    return;
                }
            }
        });
        return jmi;
    }
    
    private void bookmarkMenuItemActionPerformed(ActionEvent e, Word word) {
        this.SRR.setCurrentWord(word);
        setText(word.getText());
    }
    
    private void showPreviousWord() {
        boolean backward = false;
        SpeedReaderResource.Word prevWord = this.SRR.getPreviousWord();
        updateProgress(prevWord.getText(), backward);
        setText(prevWord.getText());
    }
    
    private void showNextWord() {
        boolean forward = true;
        SpeedReaderResource.Word nextWord = this.SRR.getNextWord();
        updateProgress(nextWord.getText(), forward);
        setText(nextWord.getText());
    }
       
    private void setText(String text) {
        this.lblWord.setText(text);
    }
    
    private void btnPlayPauseSwapIcon() {
        ImageIcon showing = (ImageIcon)this.btnPlayPause.getIcon();
        if(showing.equals(this.playIcon))
            this.btnPlayPause.setIcon(this.pauseIcon);
        else
            this.btnPlayPause.setIcon(this.playIcon);
    }
    
    private void updateStatus(String update) {
        this.lblStatus.setText(update);
    }
    
    private void updateProgress(String text, boolean forward) {
        int update = this.progressBar.getValue();
        if(forward)
            update += text.length()+1;      //+1 to account for space character we're delineating against
        else
            update -= text.length()+1;      //+1 to account for space character we're delineating against
        
        this.progressBar.setValue(update);
    }
    
    private JButton createButton(String btnName, ImageIcon icon,
                                 Dimension dim, String tooltip, int mnemonic) {
        JButton button = new JButton();
        button.setIcon(icon);
        button.setName(btnName);
        button.setMaximumSize(dim);
        button.setPreferredSize(dim);
        button.setToolTipText(tooltip);
        button.setMnemonic(mnemonic);
        return button;
    }
    
    private JMenu createMenu(String menuName, char mnemonic) {
        JMenu menu = new JMenu(menuName);
        menu.setMnemonic(mnemonic);
        return menu;
    }
    
    private int getWPM() {
        return this.WPM;
    }
    
    private void setWPM() {
        Object wpm = this.spinWPM.getValue();
        if(wpm instanceof Integer) {
            this.WPM = ((Integer) wpm).intValue();
            if(SpeedReader.inDebug())
                System.out.println("DEBUG: WPM Updated to: " + ((Integer)wpm).intValue());
        } else {
            updateStatus("Numbers Only Please!");
        }
        requestFocusInWindow();
    }
    
    private void restartThread() {
        if(!this.wordThread.isAlive()) {
            this.wordThread = new Thread(new WordLoop(this.SRR, this.SRR.getDelay(this.WPM)));
            this.wordThread.start();
        } else if(this.wordThread.isAlive()) {
            interruptThread();
        }
    }
    
    //This will eventually read a configuration file and adjust settings accordingly
    private void readConfig() {
        this.BACK_MNEMONIC = java.awt.event.KeyEvent.VK_B;
        this.FORWARD_MNEMONIC = java.awt.event.KeyEvent.VK_M;
        this.PLAY_PAUSE_MNEMONIC = java.awt.event.KeyEvent.VK_N;
        this.BOOKMARK_MNEMONIC = java.awt.event.KeyEvent.VK_Z;
        this.FRAMEWIDTH = 800;
        this.FRAMEHEIGHT = 600;
        this.FULLSCREEN = false;
    }
    
    private void interruptThread() {
        this.wordThread.interrupt();
        try {
            this.wordThread.join(0,1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    private Rectangle getDefaultWindowSize() {
        return new Rectangle(SpeedReaderGUI.DEFAULTFRAMEWIDTH,
                             SpeedReaderGUI.DEFAULTFRAMEHEIGHT);
    }
    
    private Rectangle getUserWindowSize() {
        return new Rectangle(this.FRAMEWIDTH, this.FRAMEHEIGHT);
    }
    
    /**
     * Sets the GUI to fullscreen or exits fullscreen. Tries first for exclusive fullscreen
     * then falls-back to a maximized window.  If exiting fullscreen, returns to the default window size
     * TODO: Make this return to the last windowed size when exiting fullscreen
     * @param fullscreen whether to set fullscreen
     */
    private void setFullScreen(boolean fullscreen) {
        if(SpeedReader.inDebug())
            System.out.println("DEBUG: Window is currently "
                              +(this.FULLSCREEN ? "fullscreen." : "windowed.")+ " Setting to "
                              +(fullscreen ? "fullscreen." : "windowed."));
        if(!Thread.interrupted())
            interruptThread();
        if(this.btnPlayPause.getIcon().equals(this.pauseIcon)) 
            this.btnPlayPauseSwapIcon();
        
        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        
        if(fullscreen && gd.isFullScreenSupported()) {
            try {
                this.dispose();
                this.setUndecorated(true);              
                gd.setFullScreenWindow(this);
                this.setVisible(true);
                requestFocusInWindow();
            //Unable to set exclusive fullscreen (TODO: Why??), un-set fullscreen and maximize instead
            } finally {
                gd.setFullScreenWindow(null);
                this.setExtendedState(Frame.MAXIMIZED_BOTH);
                fullscreen = true;
                requestFocusInWindow();
            }
            if(SpeedReader.inDebug()) {
                String success = (this.getExtendedState() == Frame.MAXIMIZED_BOTH ? "Successful." : "Unsuccessful.");
                System.out.println("DEBUG: Just tried setting Fullscreen. " + success);
            }
        } else {    //This may die(?) if gd.isFullScreenSupported() returns false and we set to null
            gd.setFullScreenWindow(null);
            this.dispose();
            this.setUndecorated(false);
            this.setResizable(true);
            this.setExtendedState(Frame.NORMAL);
            if(this.USERCHANGEDWINDOWSIZE)
                setBounds(getUserWindowSize());
            else
                setBounds(getDefaultWindowSize());
            this.setVisible(true);
            requestFocusInWindow();
            fullscreen = false;
        }
        if(SpeedReader.inDebug())
            System.out.println("DEBUG: Is Fullscreen Supported? " + gd.isFullScreenSupported());
        
        this.FULLSCREEN = fullscreen;
    }
    
    
    /**
     * The WordLoop class is a Threaded class which pulls the next words from our SpeedReaderResource
     * @author Paul Munly <paul@paulmunly.com>
     */
    private class WordLoop implements Runnable {
       
        private SpeedReaderResource SRR;
        private int DELAY;
        private int PAUSELENGTH;
        
        public WordLoop(SpeedReaderResource srr, int delay, int pauselength) {
            this.SRR = srr;
            this.DELAY = delay;
            this.PAUSELENGTH = pauselength;
        }
        
        public WordLoop(SpeedReaderResource srr, int delay) {
            this.SRR = srr;
            this.DELAY = delay;
            this.PAUSELENGTH = delay * 2;
        }
        
        public void run() {
            if(SpeedReader.inDebug())
                System.out.println("DEBUG: Thread Name: " + Thread.currentThread().getName()
                                   + " Current Number Active Threads: " + Thread.currentThread().getThreadGroup().activeCount());
            while(!Thread.currentThread().isInterrupted()) {
                try {
                    SpeedReaderResource.Word word = this.SRR.getNextWord();
                    //TODO: See if SRR.NOTREADY is ever set -- is this still a useful check?
                    if(!word.textMatches(this.SRR.NOTREADY)) {
                        setText(word.getText());
                        updateProgress(word.getText(), true);
                    } else { //We've reached the end of the file
                        updateStatus("End of File Reached!");
                        btnPlayPauseSwapIcon();
                        return;
                    }
                    if(!word.isPausePhrase())
                        Thread.sleep(this.DELAY);
                    else
                        Thread.sleep(this.PAUSELENGTH);
                } catch (InterruptedException e) {
                    return;
                }
                this.DELAY = this.SRR.getDelay(getWPM());
            }
        }
    }
}