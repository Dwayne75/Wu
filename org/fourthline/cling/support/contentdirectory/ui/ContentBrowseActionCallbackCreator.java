package org.fourthline.cling.support.contentdirectory.ui;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import org.fourthline.cling.controlpoint.ActionCallback;
import org.fourthline.cling.model.meta.Service;

public abstract interface ContentBrowseActionCallbackCreator
{
  public abstract ActionCallback createContentBrowseActionCallback(Service paramService, DefaultTreeModel paramDefaultTreeModel, DefaultMutableTreeNode paramDefaultMutableTreeNode);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\contentdirectory\ui\ContentBrowseActionCallbackCreator.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */