package org.fourthline.cling.support.contentdirectory.ui;

import javax.swing.JTree;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;
import org.fourthline.cling.controlpoint.ActionCallback;
import org.fourthline.cling.controlpoint.ControlPoint;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.support.contentdirectory.callback.Browse.Status;
import org.fourthline.cling.support.model.container.Container;

public abstract class ContentTree
  extends JTree
  implements ContentBrowseActionCallbackCreator
{
  protected Container rootContainer;
  protected DefaultMutableTreeNode rootNode;
  
  protected ContentTree() {}
  
  public ContentTree(ControlPoint controlPoint, Service service)
  {
    init(controlPoint, service);
  }
  
  public void init(ControlPoint controlPoint, Service service)
  {
    this.rootContainer = createRootContainer(service);
    this.rootNode = new DefaultMutableTreeNode(this.rootContainer)
    {
      public boolean isLeaf()
      {
        return false;
      }
    };
    DefaultTreeModel treeModel = new DefaultTreeModel(this.rootNode);
    setModel(treeModel);
    
    getSelectionModel().setSelectionMode(1);
    addTreeWillExpandListener(createContainerTreeExpandListener(controlPoint, service, treeModel));
    setCellRenderer(createContainerTreeCellRenderer());
    
    controlPoint.execute(createContentBrowseActionCallback(service, treeModel, getRootNode()));
  }
  
  public Container getRootContainer()
  {
    return this.rootContainer;
  }
  
  public DefaultMutableTreeNode getRootNode()
  {
    return this.rootNode;
  }
  
  public DefaultMutableTreeNode getSelectedNode()
  {
    return (DefaultMutableTreeNode)getLastSelectedPathComponent();
  }
  
  protected Container createRootContainer(Service service)
  {
    Container rootContainer = new Container();
    rootContainer.setId("0");
    rootContainer.setTitle("Content Directory on " + service.getDevice().getDisplayString());
    return rootContainer;
  }
  
  protected TreeWillExpandListener createContainerTreeExpandListener(ControlPoint controlPoint, Service service, DefaultTreeModel treeModel)
  {
    return new ContentTreeExpandListener(controlPoint, service, treeModel, this);
  }
  
  protected DefaultTreeCellRenderer createContainerTreeCellRenderer()
  {
    return new ContentTreeCellRenderer();
  }
  
  public ActionCallback createContentBrowseActionCallback(Service service, DefaultTreeModel treeModel, DefaultMutableTreeNode treeNode)
  {
    new ContentBrowseActionCallback(service, treeModel, treeNode)
    {
      public void updateStatusUI(Browse.Status status, DefaultMutableTreeNode treeNode, DefaultTreeModel treeModel)
      {
        ContentTree.this.updateStatus(status, treeNode, treeModel);
      }
      
      public void failureUI(String failureMessage)
      {
        ContentTree.this.failure(failureMessage);
      }
    };
  }
  
  public void updateStatus(Browse.Status status, DefaultMutableTreeNode treeNode, DefaultTreeModel treeModel)
  {
    switch (status)
    {
    case LOADING: 
    case NO_CONTENT: 
      treeNode.removeAllChildren();
      int index = treeNode.getChildCount() <= 0 ? 0 : treeNode.getChildCount();
      treeModel.insertNodeInto(new DefaultMutableTreeNode(status.getDefaultMessage()), treeNode, index);
      treeModel.nodeStructureChanged(treeNode);
    }
  }
  
  public abstract void failure(String paramString);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\contentdirectory\ui\ContentTree.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */