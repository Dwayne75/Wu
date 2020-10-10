package com.wurmonline.server.tools;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.JToggleButton;

public class ItemModeller
  extends JFrame
  implements KeyListener, WindowListener
{
  private static final long serialVersionUID = 1008389608509176516L;
  private JPanel southHackPanel = new JPanel();
  private JLabel ipAdressLabel = new JLabel();
  private JProgressBar hackProgressBar = new JProgressBar();
  private FlowLayout flowLayout1 = new FlowLayout();
  private JPanel textAreaPanel = new JPanel();
  private JTextField ipAdressTextField = new JTextField();
  private BorderLayout borderLayout1 = new BorderLayout();
  private JComboBox<?> portComboBox = new JComboBox();
  private JToggleButton hackButton = new JToggleButton();
  private JComboBox<?> hackComboBox = new JComboBox();
  private JTextField inputTextField = new JTextField();
  private JPanel ipAdressPanel = new JPanel();
  private JButton pingButton = new JButton();
  private JButton scanButton = new JButton();
  private TextArea messageTextArea = new TextArea();
  private TextArea codeTextArea = new TextArea();
  
  public ItemModeller()
  {
    super("Wurm Item Modeller");
    addMessage("Welcome to wurm item modeller.");
    try
    {
      jbInit();
      setBounds(0, 0, 1000, 700);
      setVisible(true);
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    addWindowListener(this);
  }
  
  private void jbInit()
    throws Exception
  {
    this.ipAdressLabel.setText("Create new:");
    this.ipAdressLabel.setVerticalAlignment(1);
    this.ipAdressLabel.setVerticalTextPosition(1);
    this.southHackPanel.setLayout(this.flowLayout1);
    this.ipAdressTextField.setMinimumSize(new Dimension(70, 21));
    this.ipAdressTextField.setPreferredSize(new Dimension(170, 21));
    this.ipAdressTextField.setText("");
    this.ipAdressTextField.addActionListener(new ItemModeller.1(this));
    
    this.textAreaPanel.setLayout(this.borderLayout1);
    this.hackButton.setText("Load");
    this.hackButton.addActionListener(new ItemModeller.2(this));
    
    this.hackProgressBar.setMaximum(100);
    this.hackProgressBar.setMinimum(0);
    this.textAreaPanel.setMinimumSize(new Dimension(200, 500));
    this.textAreaPanel.setPreferredSize(new Dimension(250, 500));
    this.textAreaPanel.setToolTipText("");
    this.inputTextField.setText("");
    this.inputTextField.setHorizontalAlignment(0);
    this.inputTextField.addActionListener(new ItemModeller.3(this));
    
    this.pingButton.setText("Save");
    this.pingButton.addActionListener(new ItemModeller.4(this));
    
    this.scanButton.setToolTipText("");
    this.scanButton.setText("Load");
    this.scanButton.addActionListener(new ItemModeller.5(this));
    
    this.hackComboBox.addActionListener(new ItemModeller.6(this));
    
    this.portComboBox.addActionListener(new ItemModeller.7(this));
    
    this.textAreaPanel.add(this.inputTextField, "South");
    this.textAreaPanel.add(this.messageTextArea, "Center");
    getContentPane().add(this.ipAdressPanel, "North");
    this.ipAdressPanel.add(this.ipAdressLabel, null);
    this.ipAdressPanel.add(this.ipAdressTextField, null);
    this.ipAdressPanel.add(this.pingButton, null);
    this.ipAdressPanel.add(this.scanButton, null);
    getContentPane().add(this.southHackPanel, "South");
    String[] data = { "" };
    
    this.portComboBox = new JComboBox(data);
    this.southHackPanel.add(this.hackComboBox, null);
    this.southHackPanel.add(this.portComboBox, null);
    this.southHackPanel.add(this.hackButton, null);
    this.southHackPanel.add(this.hackProgressBar, null);
    getContentPane().add(this.codeTextArea, "Center");
    getContentPane().add(this.textAreaPanel, "East");
    addMessage("Read all about it here.");
  }
  
  void hackButton_actionPerformed(ActionEvent e) {}
  
  void inputTextField_actionPerformed(ActionEvent e)
  {
    this.inputTextField.setText("");
  }
  
  void ipAdressTextField_actionPerformed(ActionEvent e) {}
  
  void pingButton_actionPerformed(ActionEvent e)
  {
    this.ipAdressTextField.setBackground(Color.white);
  }
  
  void scanButton_actionPerformed(ActionEvent e) {}
  
  void remoteFileSystem_actionPerformed(ActionEvent e) {}
  
  void localFileSystem_actionPerformed(ActionEvent e)
  {
    addMessage("Doing something with the local window.");
  }
  
  void hackComboBox_actionPerformed(ActionEvent e) {}
  
  void portComboBox_actionPerformed(ActionEvent e) {}
  
  public void windowDeactivated(WindowEvent e) {}
  
  public void windowActivated(WindowEvent e) {}
  
  public void windowDeiconified(WindowEvent e) {}
  
  public void windowIconified(WindowEvent e) {}
  
  public void windowClosed(WindowEvent e) {}
  
  public void windowClosing(WindowEvent e)
  {
    shutDown();
  }
  
  public void windowOpened(WindowEvent e) {}
  
  public void addMessage(String message)
  {
    if (message.endsWith("\n")) {
      this.messageTextArea.append(message);
    } else {
      this.messageTextArea.append(message + "\n");
    }
  }
  
  private void shutDown()
  {
    System.exit(0);
  }
  
  public void keyReleased(KeyEvent e) {}
  
  public void keyTyped(KeyEvent e) {}
  
  public synchronized void keyPressed(KeyEvent e)
  {
    if (e.getKeyCode() == 27) {
      shutDown();
    }
  }
  
  public static void main(String[] args) {}
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\tools\ItemModeller.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */