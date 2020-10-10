package org.seamless.swing;

import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JLabel;
import javax.swing.JSeparator;

public class Form
{
  public GridBagConstraints lastConstraints = null;
  public GridBagConstraints middleConstraints = null;
  public GridBagConstraints labelConstraints = null;
  public GridBagConstraints separatorConstraints = null;
  
  public Form(int padding)
  {
    this.lastConstraints = new GridBagConstraints();
    
    this.lastConstraints.fill = 2;
    
    this.lastConstraints.anchor = 18;
    
    this.lastConstraints.weightx = 1.0D;
    
    this.lastConstraints.gridwidth = 0;
    
    this.lastConstraints.insets = new Insets(padding, padding, padding, padding);
    
    this.middleConstraints = ((GridBagConstraints)this.lastConstraints.clone());
    
    this.middleConstraints.gridwidth = -1;
    
    this.labelConstraints = ((GridBagConstraints)this.lastConstraints.clone());
    
    this.labelConstraints.weightx = 0.0D;
    this.labelConstraints.gridwidth = 1;
    
    this.separatorConstraints = new GridBagConstraints();
    this.separatorConstraints.fill = 2;
    this.separatorConstraints.gridwidth = 0;
  }
  
  public void addLastField(Component c, Container parent)
  {
    GridBagLayout gbl = (GridBagLayout)parent.getLayout();
    gbl.setConstraints(c, this.lastConstraints);
    parent.add(c);
  }
  
  public void addLabel(Component c, Container parent)
  {
    GridBagLayout gbl = (GridBagLayout)parent.getLayout();
    gbl.setConstraints(c, this.labelConstraints);
    parent.add(c);
  }
  
  public JLabel addLabel(String s, Container parent)
  {
    JLabel c = new JLabel(s);
    addLabel(c, parent);
    return c;
  }
  
  public void addMiddleField(Component c, Container parent)
  {
    GridBagLayout gbl = (GridBagLayout)parent.getLayout();
    gbl.setConstraints(c, this.middleConstraints);
    parent.add(c);
  }
  
  public void addLabelAndLastField(String s, Container c, Container parent)
  {
    addLabel(s, parent);
    addLastField(c, parent);
  }
  
  public void addLabelAndLastField(String s, String value, Container parent)
  {
    addLabel(s, parent);
    addLastField(new JLabel(value), parent);
  }
  
  public void addSeparator(Container parent)
  {
    JSeparator separator = new JSeparator();
    GridBagLayout gbl = (GridBagLayout)parent.getLayout();
    gbl.setConstraints(separator, this.separatorConstraints);
    parent.add(separator);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\seamless\swing\Form.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */