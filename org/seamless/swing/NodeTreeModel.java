package org.seamless.swing;

import java.util.List;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

public class NodeTreeModel
  implements TreeModel
{
  private Node rootNode;
  private Node selectedNode;
  
  public NodeTreeModel(Node rootNode)
  {
    this.rootNode = rootNode;
  }
  
  public Object getRoot()
  {
    return this.rootNode;
  }
  
  public boolean isLeaf(Object object)
  {
    Node node = (Node)object;
    boolean isLeaf = node.getChildren().size() == 0;
    return isLeaf;
  }
  
  public int getChildCount(Object parent)
  {
    Node node = (Node)parent;
    return node.getChildren().size();
  }
  
  public Object getChild(Object parent, int i)
  {
    Node node = (Node)parent;
    Object child = node.getChildren().get(i);
    return child;
  }
  
  public int getIndexOfChild(Object parent, Object child)
  {
    if ((parent == null) || (child == null)) {
      return -1;
    }
    Node node = (Node)parent;
    int index = node.getChildren().indexOf(child);
    return index;
  }
  
  public void valueForPathChanged(TreePath path, Object newvalue) {}
  
  public void addTreeModelListener(TreeModelListener l) {}
  
  public void removeTreeModelListener(TreeModelListener l) {}
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\seamless\swing\NodeTreeModel.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */