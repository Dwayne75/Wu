package com.sun.activation.viewers;

import java.awt.Button;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import javax.activation.CommandObject;
import javax.activation.DataHandler;

public class TextEditor
  extends Panel
  implements CommandObject, ActionListener
{
  private TextArea text_area = null;
  private GridBagLayout panel_gb = null;
  private Panel button_panel = null;
  private Button save_button = null;
  private File text_file = null;
  private String text_buffer = null;
  private InputStream data_ins = null;
  private FileInputStream fis = null;
  private DataHandler _dh = null;
  private boolean DEBUG = false;
  
  public TextEditor()
  {
    this.panel_gb = new GridBagLayout();
    setLayout(this.panel_gb);
    
    this.button_panel = new Panel();
    
    this.button_panel.setLayout(new FlowLayout());
    this.save_button = new Button("SAVE");
    this.button_panel.add(this.save_button);
    addGridComponent(this, this.button_panel, this.panel_gb, 0, 0, 1, 1, 1, 0);
    
    this.text_area = new TextArea("This is text", 24, 80, 1);
    
    this.text_area.setEditable(true);
    
    addGridComponent(this, this.text_area, this.panel_gb, 0, 1, 1, 2, 1, 1);
    
    this.save_button.addActionListener(this);
  }
  
  private void addGridComponent(Container cont, Component comp, GridBagLayout mygb, int gridx, int gridy, int gridw, int gridh, int weightx, int weighty)
  {
    GridBagConstraints c = new GridBagConstraints();
    c.gridx = gridx;
    c.gridy = gridy;
    c.gridwidth = gridw;
    c.gridheight = gridh;
    c.fill = 1;
    c.weighty = weighty;
    c.weightx = weightx;
    c.anchor = 10;
    mygb.setConstraints(comp, c);
    cont.add(comp);
  }
  
  public void setCommandContext(String verb, DataHandler dh)
    throws IOException
  {
    this._dh = dh;
    setInputStream(this._dh.getInputStream());
  }
  
  public void setInputStream(InputStream ins)
    throws IOException
  {
    byte[] data = new byte['Ѐ'];
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    int bytes_read = 0;
    while ((bytes_read = ins.read(data)) > 0) {
      baos.write(data, 0, bytes_read);
    }
    ins.close();
    
    this.text_buffer = baos.toString();
    
    this.text_area.setText(this.text_buffer);
  }
  
  private void performSaveOperation()
  {
    OutputStream fos = null;
    try
    {
      fos = this._dh.getOutputStream();
    }
    catch (Exception e) {}
    String buffer = this.text_area.getText();
    if (fos == null)
    {
      System.out.println("Invalid outputstream in TextEditor!");
      System.out.println("not saving!");
      return;
    }
    try
    {
      fos.write(buffer.getBytes());
      fos.flush();
      fos.close();
    }
    catch (IOException e)
    {
      System.out.println("TextEditor Save Operation failed with: " + e);
    }
  }
  
  public void addNotify()
  {
    super.addNotify();
    invalidate();
  }
  
  public Dimension getPreferredSize()
  {
    return this.text_area.getMinimumSize(24, 80);
  }
  
  public void actionPerformed(ActionEvent evt)
  {
    if (evt.getSource() == this.save_button) {
      performSaveOperation();
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\activation\viewers\TextEditor.class
 * Java compiler version: 4 (48.0)
 * JD-Core Version:       0.7.1
 */