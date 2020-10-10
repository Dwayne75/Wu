package com.wurmonline.server.meshgen;

import com.wurmonline.mesh.MeshIO;
import java.awt.BorderLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.text.DefaultCaret;

public final class MeshGenGui
  extends JFrame
  implements ActionListener
{
  private static final long serialVersionUID = -1462641916710560981L;
  private static final Logger logger = Logger.getLogger(MeshGenGui.class.getName());
  JLabel imageLabel;
  JButton generateGroundButton;
  JButton normaliseButton;
  JButton flowButton;
  JButton texturizeButton;
  JButton saveButton;
  JButton saveImageButton;
  JButton loadButton;
  JButton addIslandsButton;
  JToggleButton layerToggle;
  long seed = 0L;
  MeshGen meshGen;
  JPanel panel;
  MeshIO topLayerMeshIO;
  MeshIO rockLayerMeshIO;
  boolean loaded = false;
  final String baseDir = "worldmachine" + File.separator + "NewEle2015";
  final String baseFile = File.separator + "output.r32";
  private JProgressBar progressBar;
  private JTextArea taskOutput;
  private MeshGenGui.Task task;
  private JFrame frame;
  
  public MeshGenGui()
  {
    super("Wurm MeshGen GUI");
    if (logger.isLoggable(Level.FINE)) {
      logger.fine("Starting Wurm MeshGen GUI");
    }
    this.panel = new JPanel();
    this.panel.setLayout(new BorderLayout());
    
    this.imageLabel = new JLabel();
    
    this.progressBar = new JProgressBar(0, 100);
    this.progressBar.setValue(0);
    this.progressBar.setStringPainted(true);
    
    this.flowButton = new JButton("Load base map");
    this.flowButton.addActionListener(this);
    this.flowButton.setToolTipText("Load the output.r32 base map file");
    
    this.texturizeButton = new JButton("Texturize");
    this.texturizeButton.addActionListener(this);
    
    this.saveButton = new JButton("Save");
    this.saveButton.addActionListener(this);
    this.saveButton.setToolTipText("Save the top_layer.map and rock_layer.map");
    
    this.loadButton = new JButton("Load");
    this.loadButton.addActionListener(this);
    this.loadButton.setToolTipText("Load the top_layer.map and rock_layer.map");
    
    this.saveImageButton = new JButton("Save Image");
    this.saveImageButton.addActionListener(this);
    this.saveImageButton.setToolTipText("Save the coloured image of top_layer.map to map.png");
    
    this.layerToggle = new JToggleButton("Layer", false);
    this.layerToggle.addActionListener(this);
    this.layerToggle.setToolTipText("Selected shows the rock layer, unselected shows the surface layer");
    
    this.addIslandsButton = new JButton("Add Islands");
    this.addIslandsButton.addActionListener(this);
    this.addIslandsButton.setToolTipText("Add some islands to the top_layer.map and rock_layer.map");
    
    JPanel buttonPanel = new JPanel();
    buttonPanel.add(this.progressBar);
    buttonPanel.add(this.layerToggle);
    
    buttonPanel.add(this.flowButton);
    buttonPanel.add(this.texturizeButton);
    buttonPanel.add(this.addIslandsButton);
    buttonPanel.add(this.saveButton);
    buttonPanel.add(this.loadButton);
    buttonPanel.add(this.saveImageButton);
    
    this.panel.add(new JScrollPane(this.imageLabel), "Center");
    this.panel.add(buttonPanel, "South");
    
    setContentPane(this.panel);
    setSize(1200, 800);
    setDefaultCloseOperation(3);
    enableButtons(false);
    
    this.frame = new JFrame("Please wait...");
    this.frame.setDefaultCloseOperation(3);
    this.frame.setLayout(new BorderLayout());
    
    this.taskOutput = new JTextArea(20, 30);
    this.taskOutput.setMargin(new Insets(5, 5, 5, 5));
    this.taskOutput.setEditable(false);
    
    DefaultCaret caret = (DefaultCaret)this.taskOutput.getCaret();
    caret.setUpdatePolicy(2);
    
    JScrollPane scroll = new JScrollPane(this.taskOutput, 22, 31);
    
    JPanel mpanel = new JPanel();
    mpanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
    mpanel.add(scroll, "Center");
    this.frame.setContentPane(mpanel);
    this.frame.pack();
  }
  
  public void actionPerformed(ActionEvent e)
  {
    try
    {
      Thread.sleep(500L);
    }
    catch (InterruptedException e2)
    {
      e2.printStackTrace();
    }
    try
    {
      if (e.getSource() == this.flowButton)
      {
        if (!this.loaded)
        {
          this.loaded = true;
          try
          {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new MeshGenGui.1(this));
            
            chooser.setCurrentDirectory(new File(this.baseDir));
            int returnVal = chooser.showOpenDialog(this.panel);
            
            File worldMachineOutput = returnVal == 0 ? chooser.getSelectedFile() : new File(this.baseDir + this.baseFile);
            
            long baseFileSize = worldMachineOutput.length();
            
            double mapDimension = Math.sqrt(baseFileSize) / 2.0D;
            logger.info("Math.sqrt(fis.getChannel().size())/2 " + mapDimension);
            if (logger.isLoggable(Level.FINE)) {
              logger.fine("Opening " + worldMachineOutput.getName() + ", length: " + baseFileSize + " Bytes");
            }
            this.task = new MeshGenGui.2(this, "Loading file.", worldMachineOutput, mapDimension);
            
            logger.log(Level.INFO, "Created task. Now setting prio and starting.");
            this.task.execute();
          }
          catch (Exception ex)
          {
            logger.log(Level.WARNING, "Problem loading Base Map", ex);
          }
        }
        else
        {
          this.task = new MeshGenGui.3(this, "Showing Image.");
          
          this.task.execute();
        }
      }
      else if (e.getSource() == this.texturizeButton)
      {
        if (this.meshGen != null)
        {
          this.task = new MeshGenGui.4(this, "Adding textures.");
          
          this.task.execute();
        }
      }
      else if (e.getSource() == this.addIslandsButton)
      {
        if (this.meshGen != null) {
          if (this.topLayerMeshIO != null)
          {
            this.task = new MeshGenGui.5(this, "Adding islands.");
            
            this.task.execute();
          }
          else
          {
            logger.info("Failed to add Islands. Save map first.");
          }
        }
      }
      else if (e.getSource() == this.loadButton)
      {
        try
        {
          String mapDirectory = selectMapDir();
          logger.info("Opening Mesh " + mapDirectory + File.separatorChar + "top_layer.map");
          MeshIO meshIO = MeshIO.open(mapDirectory + File.separatorChar + "top_layer.map");
          logger.info("Opening Mesh " + mapDirectory + File.separatorChar + "rock_layer.map");
          MeshIO meshIO2 = MeshIO.open(mapDirectory + File.separatorChar + "rock_layer.map");
          if (meshIO.getSize() != meshIO2.getSize()) {
            logger.warning("top layer and rock layer are not the same size");
          }
          this.task = new MeshGenGui.6(this, "Loading maps.", meshIO, meshIO2);
          
          this.task.execute();
        }
        catch (IOException ioe)
        {
          logger.log(Level.WARNING, "Problem loading Map", ioe);
        }
      }
      else if (e.getSource() == this.generateGroundButton)
      {
        if (this.meshGen != null)
        {
          this.task = new MeshGenGui.7(this, "Generating ground.");
          
          this.task.execute();
        }
      }
      else if (e.getSource() == this.saveButton)
      {
        this.task = new MeshGenGui.8(this, "Saving maps.");
        
        this.task.execute();
      }
      else if (e.getSource() == this.saveImageButton)
      {
        if (this.meshGen != null)
        {
          this.task = new MeshGenGui.9(this, "Saving png.");
          
          this.task.execute();
        }
      }
      else if (e.getSource() == this.layerToggle)
      {
        if (this.meshGen != null)
        {
          this.task = new MeshGenGui.10(this, "Toggling layer.");
          
          this.task.execute();
        }
      }
    }
    catch (RuntimeException re)
    {
      logger.log(Level.SEVERE, "Error while handling ActionClass ", re);
      throw re;
    }
  }
  
  private void enableButtons(boolean running)
  {
    if (running)
    {
      this.progressBar.setVisible(true);
      this.layerToggle.setEnabled(false);
      this.flowButton.setEnabled(false);
      this.texturizeButton.setEnabled(false);
      this.addIslandsButton.setEnabled(false);
      this.saveButton.setEnabled(false);
      this.loadButton.setEnabled(false);
      this.saveImageButton.setEnabled(false);
    }
    else
    {
      boolean sf = this.meshGen != null;
      this.progressBar.setVisible(false);
      this.layerToggle.setEnabled(sf);
      this.flowButton.setEnabled(true);
      this.texturizeButton.setEnabled(sf);
      this.addIslandsButton.setEnabled(sf);
      this.saveButton.setEnabled(sf);
      this.loadButton.setEnabled(true);
      this.saveImageButton.setEnabled(sf);
    }
  }
  
  private String selectMapDir()
  {
    for (;;)
    {
      JFileChooser chooser = new JFileChooser();
      chooser.setFileFilter(new MeshGenGui.11(this));
      
      chooser.setCurrentDirectory(new File("."));
      chooser.setAcceptAllFileFilterUsed(false);
      
      chooser.setFileFilter(new MeshGenGui.12(this));
      
      chooser.setCurrentDirectory(new File("."));
      chooser.setFileSelectionMode(1);
      chooser.setDialogTitle("Select the directory containing the map files");
      chooser.setApproveButtonText("Use this dir");
      chooser.setApproveButtonToolTipText("<html>The selected directory will be used by the Mesh Generator GUI<br> to load the top_layer.map and rock_layer.map files</html");
      
      int returnVal = chooser.showOpenDialog(this.panel);
      if (returnVal == 0)
      {
        File file = chooser.getSelectedFile();
        if (file.isFile())
        {
          if (logger.isLoggable(Level.FINE)) {
            logger.fine("Using the directory containing the chosen file: " + file);
          }
          file = file.getParentFile();
        }
        if (!file.exists()) {
          file.mkdir();
        }
        if (file.listFiles().length != 0)
        {
          int option = JOptionPane.showConfirmDialog(this.panel, "<html>Use \"" + file.toString() + "\"?", "Confirm directory", 0);
          if (option == 0) {
            return file.toString();
          }
        }
        else
        {
          int option = JOptionPane.showConfirmDialog(this.panel, "<html>Use \"" + file.toString() + "\"?<br><br><b>Warning: The directory is empty.</b><br>This should contain the maps", "Confirm directory", 0);
          if (option == 0) {
            return file.toString();
          }
        }
      }
      else
      {
        return null;
      }
    }
  }
  
  public static void main(String[] args)
  {
    new MeshGenGui().setVisible(true);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\meshgen\MeshGenGui.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */