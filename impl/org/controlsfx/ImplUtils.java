package impl.org.controlsfx;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.Pane;

public class ImplUtils
{
  public static void injectAsRootPane(Scene scene, Parent injectedParent, boolean useReflection)
  {
    Parent originalParent = scene.getRoot();
    scene.setRoot(injectedParent);
    if (originalParent != null)
    {
      getChildren(injectedParent, useReflection).add(0, originalParent);
      
      injectedParent.getProperties().putAll(originalParent.getProperties());
    }
  }
  
  public static void injectPane(Parent parent, Parent injectedParent, boolean useReflection)
  {
    if (parent == null) {
      throw new IllegalArgumentException("parent can not be null");
    }
    List<Node> ownerParentChildren = getChildren(parent.getParent(), useReflection);
    
    int ownerPos = ownerParentChildren.indexOf(parent);
    ownerParentChildren.remove(ownerPos);
    ownerParentChildren.add(ownerPos, injectedParent);
    
    getChildren(injectedParent, useReflection).add(0, parent);
    
    injectedParent.getProperties().putAll(parent.getProperties());
  }
  
  public static void stripRootPane(Scene scene, Parent originalParent, boolean useReflection)
  {
    Parent oldParent = scene.getRoot();
    getChildren(oldParent, useReflection).remove(originalParent);
    originalParent.getStyleClass().remove("root");
    scene.setRoot(originalParent);
  }
  
  public static List<Node> getChildren(Node n, boolean useReflection)
  {
    return (n instanceof Parent) ? getChildren((Parent)n, useReflection) : Collections.emptyList();
  }
  
  public static List<Node> getChildren(Parent p, boolean useReflection)
  {
    ObservableList<Node> children = null;
    if ((p instanceof Pane))
    {
      children = ((Pane)p).getChildren();
    }
    else if ((p instanceof Group))
    {
      children = ((Group)p).getChildren();
    }
    else if ((p instanceof Control))
    {
      Control c = (Control)p;
      Skin<?> s = c.getSkin();
      children = (s instanceof SkinBase) ? ((SkinBase)s).getChildren() : getChildrenReflectively(p);
    }
    else if (useReflection)
    {
      children = getChildrenReflectively(p);
    }
    if (children == null) {
      throw new RuntimeException("Unable to get children for Parent of type " + p.getClass() + ". useReflection is set to " + useReflection);
    }
    return children == null ? FXCollections.emptyObservableList() : children;
  }
  
  public static ObservableList<Node> getChildrenReflectively(Parent p)
  {
    ObservableList<Node> children = null;
    try
    {
      Method getChildrenMethod = Parent.class.getDeclaredMethod("getChildren", new Class[0]);
      if (getChildrenMethod != null)
      {
        if (!getChildrenMethod.isAccessible()) {
          getChildrenMethod.setAccessible(true);
        }
        children = (ObservableList)getChildrenMethod.invoke(p, new Object[0]);
      }
    }
    catch (ReflectiveOperationException|IllegalArgumentException e)
    {
      throw new RuntimeException("Unable to get children for Parent of type " + p.getClass(), e);
    }
    return children;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\impl\org\controlsfx\ImplUtils.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */