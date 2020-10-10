package org.fourthline.cling.support.contentdirectory.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import org.fourthline.cling.model.action.ActionException;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.types.ErrorCode;
import org.fourthline.cling.support.contentdirectory.callback.Browse;
import org.fourthline.cling.support.contentdirectory.callback.Browse.Status;
import org.fourthline.cling.support.model.BrowseFlag;
import org.fourthline.cling.support.model.DIDLContent;
import org.fourthline.cling.support.model.SortCriterion;
import org.fourthline.cling.support.model.container.Container;
import org.fourthline.cling.support.model.item.Item;

public abstract class ContentBrowseActionCallback
  extends Browse
{
  private static Logger log = Logger.getLogger(ContentBrowseActionCallback.class.getName());
  protected final DefaultTreeModel treeModel;
  protected final DefaultMutableTreeNode treeNode;
  
  public ContentBrowseActionCallback(Service service, DefaultTreeModel treeModel, DefaultMutableTreeNode treeNode)
  {
    super(service, ((Container)treeNode.getUserObject()).getId(), BrowseFlag.DIRECT_CHILDREN, "*", 0L, null, new SortCriterion[] { new SortCriterion(true, "dc:title") });
    this.treeModel = treeModel;
    this.treeNode = treeNode;
  }
  
  public ContentBrowseActionCallback(Service service, DefaultTreeModel treeModel, DefaultMutableTreeNode treeNode, String filter, long firstResult, long maxResults, SortCriterion... orderBy)
  {
    super(service, ((Container)treeNode.getUserObject()).getId(), BrowseFlag.DIRECT_CHILDREN, filter, firstResult, Long.valueOf(maxResults), orderBy);
    this.treeModel = treeModel;
    this.treeNode = treeNode;
  }
  
  public DefaultTreeModel getTreeModel()
  {
    return this.treeModel;
  }
  
  public DefaultMutableTreeNode getTreeNode()
  {
    return this.treeNode;
  }
  
  public void received(ActionInvocation actionInvocation, DIDLContent didl)
  {
    log.fine("Received browse action DIDL descriptor, creating tree nodes");
    final List<DefaultMutableTreeNode> childNodes = new ArrayList();
    try
    {
      for (Container childContainer : didl.getContainers())
      {
        DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(childContainer)
        {
          public boolean isLeaf()
          {
            return false;
          }
        };
        childNodes.add(childNode);
      }
      for (Item childItem : didl.getItems())
      {
        DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(childItem)
        {
          public boolean isLeaf()
          {
            return true;
          }
        };
        childNodes.add(childNode);
      }
    }
    catch (Exception ex)
    {
      log.fine("Creating DIDL tree nodes failed: " + ex);
      actionInvocation.setFailure(new ActionException(ErrorCode.ACTION_FAILED, "Can't create tree child nodes: " + ex, ex));
      
      failure(actionInvocation, null);
    }
    SwingUtilities.invokeLater(new Runnable()
    {
      public void run()
      {
        ContentBrowseActionCallback.this.updateTreeModel(childNodes);
      }
    });
  }
  
  public void updateStatus(final Browse.Status status)
  {
    SwingUtilities.invokeLater(new Runnable()
    {
      public void run()
      {
        ContentBrowseActionCallback.this.updateStatusUI(status, ContentBrowseActionCallback.this.treeNode, ContentBrowseActionCallback.this.treeModel);
      }
    });
  }
  
  public void failure(ActionInvocation invocation, UpnpResponse operation, final String defaultMsg)
  {
    SwingUtilities.invokeLater(new Runnable()
    {
      public void run()
      {
        ContentBrowseActionCallback.this.failureUI(defaultMsg);
      }
    });
  }
  
  protected void updateTreeModel(List<DefaultMutableTreeNode> childNodes)
  {
    log.fine("Adding nodes to tree: " + childNodes.size());
    
    removeChildren();
    for (DefaultMutableTreeNode childNode : childNodes) {
      insertChild(childNode);
    }
  }
  
  protected void removeChildren()
  {
    this.treeNode.removeAllChildren();
    this.treeModel.nodeStructureChanged(this.treeNode);
  }
  
  protected void insertChild(MutableTreeNode childNode)
  {
    int index = this.treeNode.getChildCount() <= 0 ? 0 : this.treeNode.getChildCount();
    this.treeModel.insertNodeInto(childNode, this.treeNode, index);
  }
  
  public abstract void updateStatusUI(Browse.Status paramStatus, DefaultMutableTreeNode paramDefaultMutableTreeNode, DefaultTreeModel paramDefaultTreeModel);
  
  public abstract void failureUI(String paramString);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\contentdirectory\ui\ContentBrowseActionCallback.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */