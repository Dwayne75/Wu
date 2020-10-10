package org.fourthline.cling.support.contentdirectory.ui;

import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreePath;
import org.fourthline.cling.controlpoint.ActionCallback;
import org.fourthline.cling.controlpoint.ControlPoint;
import org.fourthline.cling.model.meta.Service;

public class ContentTreeExpandListener
  implements TreeWillExpandListener
{
  protected final ControlPoint controlPoint;
  protected final Service service;
  protected final DefaultTreeModel treeModel;
  protected final ContentBrowseActionCallbackCreator actionCreator;
  
  public ContentTreeExpandListener(ControlPoint controlPoint, Service service, DefaultTreeModel treeModel, ContentBrowseActionCallbackCreator actionCreator)
  {
    this.controlPoint = controlPoint;
    this.service = service;
    this.treeModel = treeModel;
    this.actionCreator = actionCreator;
  }
  
  public void treeWillExpand(TreeExpansionEvent e)
    throws ExpandVetoException
  {
    DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode)e.getPath().getLastPathComponent();
    
    treeNode.removeAllChildren();
    this.treeModel.nodeStructureChanged(treeNode);
    
    ActionCallback callback = this.actionCreator.createContentBrowseActionCallback(this.service, this.treeModel, treeNode);
    
    this.controlPoint.execute(callback);
  }
  
  public void treeWillCollapse(TreeExpansionEvent e)
    throws ExpandVetoException
  {}
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\contentdirectory\ui\ContentTreeExpandListener.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */