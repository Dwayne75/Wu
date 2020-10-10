package org.fourthline.cling.support.contentdirectory.ui;

import java.awt.Component;
import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import org.fourthline.cling.support.model.DIDLObject.Class;
import org.fourthline.cling.support.model.container.Container;
import org.fourthline.cling.support.model.item.Item;

public class ContentTreeCellRenderer
  extends DefaultTreeCellRenderer
{
  public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus)
  {
    super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
    
    DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
    if ((node.getUserObject() instanceof Container))
    {
      Container container = (Container)node.getUserObject();
      setText(container.getTitle());
      setIcon(expanded ? getContainerOpenIcon() : getContainerClosedIcon());
    }
    else if ((node.getUserObject() instanceof Item))
    {
      Item item = (Item)node.getUserObject();
      setText(item.getTitle());
      
      DIDLObject.Class upnpClass = item.getClazz();
      setIcon(getItemIcon(item, upnpClass != null ? upnpClass.getValue() : null));
    }
    else if ((node.getUserObject() instanceof String))
    {
      setIcon(getInfoIcon());
    }
    onCreate();
    return this;
  }
  
  protected void onCreate() {}
  
  protected Icon getContainerOpenIcon()
  {
    return null;
  }
  
  protected Icon getContainerClosedIcon()
  {
    return null;
  }
  
  protected Icon getItemIcon(Item item, String upnpClass)
  {
    return null;
  }
  
  protected Icon getInfoIcon()
  {
    return null;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\contentdirectory\ui\ContentTreeCellRenderer.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */