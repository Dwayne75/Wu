package org.controlsfx.control.table;

import com.sun.javafx.scene.control.skin.TableHeaderRow;
import com.sun.javafx.scene.control.skin.TableViewSkin;
import com.sun.javafx.scene.control.skin.TableViewSkinBase;
import java.lang.reflect.Field;
import java.util.function.Consumer;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeTableView;

final class TableViewUtils
{
  public static void modifyTableMenu(TableView<?> tableView, Consumer<ContextMenu> consumer)
  {
    modifyTableMenu(tableView, consumer);
  }
  
  public static void modifyTableMenu(TreeTableView<?> treeTableView, Consumer<ContextMenu> consumer)
  {
    modifyTableMenu(treeTableView, consumer);
  }
  
  private static void modifyTableMenu(Control control, final Consumer<ContextMenu> consumer)
  {
    if (control.getScene() == null)
    {
      control.sceneProperty().addListener(new InvalidationListener()
      {
        public void invalidated(Observable o)
        {
          this.val$control.sceneProperty().removeListener(this);
          TableViewUtils.modifyTableMenu(this.val$control, consumer);
        }
      });
      return;
    }
    Skin<?> skin = control.getSkin();
    if (skin == null)
    {
      control.skinProperty().addListener(new InvalidationListener()
      {
        public void invalidated(Observable o)
        {
          this.val$control.skinProperty().removeListener(this);
          TableViewUtils.modifyTableMenu(this.val$control, consumer);
        }
      });
      return;
    }
    doModify(skin, consumer);
  }
  
  private static void doModify(Skin<?> skin, Consumer<ContextMenu> consumer)
  {
    if (!(skin instanceof TableViewSkinBase)) {
      return;
    }
    TableViewSkin<?> tableSkin = (TableViewSkin)skin;
    TableHeaderRow headerRow = getHeaderRow(tableSkin);
    if (headerRow == null) {
      return;
    }
    ContextMenu contextMenu = getContextMenu(headerRow);
    consumer.accept(contextMenu);
  }
  
  private static TableHeaderRow getHeaderRow(TableViewSkin<?> tableSkin)
  {
    ObservableList<Node> children = tableSkin.getChildren();
    int i = 0;
    for (int max = children.size(); i < max; i++)
    {
      Node child = (Node)children.get(i);
      if ((child instanceof TableHeaderRow)) {
        return (TableHeaderRow)child;
      }
    }
    return null;
  }
  
  private static ContextMenu getContextMenu(TableHeaderRow headerRow)
  {
    try
    {
      Field privateContextMenuField = TableHeaderRow.class.getDeclaredField("columnPopupMenu");
      privateContextMenuField.setAccessible(true);
      return (ContextMenu)privateContextMenuField.get(headerRow);
    }
    catch (IllegalArgumentException ex)
    {
      ex.printStackTrace();
    }
    catch (IllegalAccessException ex)
    {
      ex.printStackTrace();
    }
    catch (NoSuchFieldException ex)
    {
      ex.printStackTrace();
    }
    catch (SecurityException ex)
    {
      ex.printStackTrace();
    }
    return null;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\controlsfx\control\table\TableViewUtils.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */