package org.controlsfx.control.action;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Consumer;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.binding.When;
import javafx.beans.binding.When.StringConditionBuilder;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.MapChangeListener;
import javafx.collections.MapChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.css.Styleable;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.Separator;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.controlsfx.control.SegmentedButton;
import org.controlsfx.tools.Duplicatable;

public class ActionUtils
{
  public static enum ActionTextBehavior
  {
    SHOW,  HIDE;
    
    private ActionTextBehavior() {}
  }
  
  public static Button createButton(Action action, ActionTextBehavior textBehavior)
  {
    return (Button)configure(new Button(), action, textBehavior);
  }
  
  public static Button createButton(Action action)
  {
    return (Button)configure(new Button(), action, ActionTextBehavior.SHOW);
  }
  
  public static ButtonBase configureButton(Action action, ButtonBase button)
  {
    return configure(button, action, ActionTextBehavior.SHOW);
  }
  
  public static void unconfigureButton(ButtonBase button)
  {
    unconfigure(button);
  }
  
  public static MenuButton createMenuButton(Action action, ActionTextBehavior textBehavior)
  {
    return (MenuButton)configure(new MenuButton(), action, textBehavior);
  }
  
  public static MenuButton createMenuButton(Action action)
  {
    return (MenuButton)configure(new MenuButton(), action, ActionTextBehavior.SHOW);
  }
  
  public static Hyperlink createHyperlink(Action action)
  {
    return (Hyperlink)configure(new Hyperlink(), action, ActionTextBehavior.SHOW);
  }
  
  public static ToggleButton createToggleButton(Action action, ActionTextBehavior textBehavior)
  {
    return (ToggleButton)configure(new ToggleButton(), action, textBehavior);
  }
  
  public static ToggleButton createToggleButton(Action action)
  {
    return createToggleButton(action, ActionTextBehavior.SHOW);
  }
  
  public static SegmentedButton createSegmentedButton(ActionTextBehavior textBehavior, Collection<? extends Action> actions)
  {
    ObservableList<ToggleButton> buttons = FXCollections.observableArrayList();
    for (Action a : actions) {
      buttons.add(createToggleButton(a, textBehavior));
    }
    return new SegmentedButton(buttons);
  }
  
  public static SegmentedButton createSegmentedButton(Collection<? extends Action> actions)
  {
    return createSegmentedButton(ActionTextBehavior.SHOW, actions);
  }
  
  public static SegmentedButton createSegmentedButton(ActionTextBehavior textBehavior, Action... actions)
  {
    return createSegmentedButton(textBehavior, Arrays.asList(actions));
  }
  
  public static SegmentedButton createSegmentedButton(Action... actions)
  {
    return createSegmentedButton(ActionTextBehavior.SHOW, Arrays.asList(actions));
  }
  
  public static CheckBox createCheckBox(Action action)
  {
    return (CheckBox)configure(new CheckBox(), action, ActionTextBehavior.SHOW);
  }
  
  public static RadioButton createRadioButton(Action action)
  {
    return (RadioButton)configure(new RadioButton(), action, ActionTextBehavior.SHOW);
  }
  
  public static MenuItem createMenuItem(Action action)
  {
    MenuItem menuItem = action.getClass().isAnnotationPresent(ActionCheck.class) ? new CheckMenuItem() : new MenuItem();
    
    return configure(menuItem, action);
  }
  
  public static MenuItem configureMenuItem(Action action, MenuItem menuItem)
  {
    return configure(menuItem, action);
  }
  
  public static void unconfigureMenuItem(MenuItem menuItem)
  {
    unconfigure(menuItem);
  }
  
  public static Menu createMenu(Action action)
  {
    return (Menu)configure(new Menu(), action);
  }
  
  public static CheckMenuItem createCheckMenuItem(Action action)
  {
    return (CheckMenuItem)configure(new CheckMenuItem(), action);
  }
  
  public static RadioMenuItem createRadioMenuItem(Action action)
  {
    return (RadioMenuItem)configure(new RadioMenuItem((String)action.textProperty().get()), action);
  }
  
  public static Action ACTION_SEPARATOR = new Action(null, null)
  {
    public String toString()
    {
      return "Separator";
    }
  };
  public static Action ACTION_SPAN = new Action(null, null)
  {
    public String toString()
    {
      return "Span";
    }
  };
  
  public static ToolBar createToolBar(Collection<? extends Action> actions, ActionTextBehavior textBehavior)
  {
    return updateToolBar(new ToolBar(), actions, textBehavior);
  }
  
  public static ToolBar updateToolBar(ToolBar toolbar, Collection<? extends Action> actions, ActionTextBehavior textBehavior)
  {
    toolbar.getItems().clear();
    for (Action action : actions) {
      if ((action instanceof ActionGroup))
      {
        MenuButton menu = createMenuButton(action, textBehavior);
        menu.setFocusTraversable(false);
        menu.getItems().addAll(toMenuItems(((ActionGroup)action).getActions()));
        toolbar.getItems().add(menu);
      }
      else if (action == ACTION_SEPARATOR)
      {
        toolbar.getItems().add(new Separator());
      }
      else if (action == ACTION_SPAN)
      {
        Pane span = new Pane();
        HBox.setHgrow(span, Priority.ALWAYS);
        VBox.setVgrow(span, Priority.ALWAYS);
        toolbar.getItems().add(span);
      }
      else if (action != null)
      {
        ButtonBase button;
        ButtonBase button;
        if (action.getClass().getAnnotation(ActionCheck.class) != null) {
          button = createToggleButton(action, textBehavior);
        } else {
          button = createButton(action, textBehavior);
        }
        button.setFocusTraversable(false);
        toolbar.getItems().add(button);
      }
    }
    return toolbar;
  }
  
  public static MenuBar createMenuBar(Collection<? extends Action> actions)
  {
    return updateMenuBar(new MenuBar(), actions);
  }
  
  public static MenuBar updateMenuBar(MenuBar menuBar, Collection<? extends Action> actions)
  {
    menuBar.getMenus().clear();
    for (Action action : actions) {
      if ((action != ACTION_SEPARATOR) && (action != ACTION_SPAN))
      {
        Menu menu = createMenu(action);
        if ((action instanceof ActionGroup)) {
          menu.getItems().addAll(toMenuItems(((ActionGroup)action).getActions()));
        } else if (action != null) {}
        menuBar.getMenus().add(menu);
      }
    }
    return menuBar;
  }
  
  public static ButtonBar createButtonBar(Collection<? extends Action> actions)
  {
    return updateButtonBar(new ButtonBar(), actions);
  }
  
  public static ButtonBar updateButtonBar(ButtonBar buttonBar, Collection<? extends Action> actions)
  {
    buttonBar.getButtons().clear();
    for (Action action : actions) {
      if (!(action instanceof ActionGroup)) {
        if ((action != ACTION_SPAN) && (action != ACTION_SEPARATOR) && (action != null)) {
          buttonBar.getButtons().add(createButton(action, ActionTextBehavior.SHOW));
        }
      }
    }
    return buttonBar;
  }
  
  public static ContextMenu createContextMenu(Collection<? extends Action> actions)
  {
    return updateContextMenu(new ContextMenu(), actions);
  }
  
  public static ContextMenu updateContextMenu(ContextMenu menu, Collection<? extends Action> actions)
  {
    menu.getItems().clear();
    menu.getItems().addAll(toMenuItems(actions));
    return menu;
  }
  
  private static Collection<MenuItem> toMenuItems(Collection<? extends Action> actions)
  {
    Collection<MenuItem> items = new ArrayList();
    for (Action action : actions) {
      if ((action instanceof ActionGroup))
      {
        Menu menu = createMenu(action);
        menu.getItems().addAll(toMenuItems(((ActionGroup)action).getActions()));
        items.add(menu);
      }
      else if (action == ACTION_SEPARATOR)
      {
        items.add(new SeparatorMenuItem());
      }
      else if ((action != null) && (action != ACTION_SPAN))
      {
        items.add(createMenuItem(action));
      }
    }
    return items;
  }
  
  private static Node copyNode(Node node)
  {
    if ((node instanceof ImageView)) {
      return new ImageView(((ImageView)node).getImage());
    }
    if ((node instanceof Duplicatable)) {
      return (Node)((Duplicatable)node).duplicate();
    }
    return null;
  }
  
  private static void bindStyle(Styleable styleable, Action action)
  {
    styleable.getStyleClass().addAll(action.getStyleClass());
    action.getStyleClass().addListener(new ListChangeListener()
    {
      public void onChanged(ListChangeListener.Change<? extends String> c)
      {
        while (c.next())
        {
          if (c.wasRemoved()) {
            this.val$styleable.getStyleClass().removeAll(c.getRemoved());
          }
          if (c.wasAdded()) {
            this.val$styleable.getStyleClass().addAll(c.getAddedSubList());
          }
        }
      }
    });
  }
  
  private static <T extends ButtonBase> T configure(T btn, Action action, ActionTextBehavior textBehavior)
  {
    if (action == null) {
      throw new NullPointerException("Action can not be null");
    }
    bindStyle(btn, action);
    if (textBehavior == ActionTextBehavior.SHOW) {
      btn.textProperty().bind(action.textProperty());
    }
    btn.disableProperty().bind(action.disabledProperty());
    
    btn.graphicProperty().bind(new ObjectBinding()
    {
      protected Node computeValue()
      {
        return ActionUtils.copyNode((Node)this.val$action.graphicProperty().get());
      }
      
      public void removeListener(InvalidationListener listener)
      {
        super.removeListener(listener);
        unbind(new Observable[] { this.val$action.graphicProperty() });
      }
    });
    btn.getProperties().putAll(action.getProperties());
    action.getProperties().addListener(new ButtonPropertiesMapChangeListener(btn, action, null));
    
    btn.tooltipProperty().bind(new ObjectBinding()
    {
      private Tooltip tooltip;
      private StringBinding textBinding;
      
      protected Tooltip computeValue()
      {
        String longText = this.textBinding.get();
        return (longText == null) || (this.textBinding.get().isEmpty()) ? null : this.tooltip;
      }
      
      public void removeListener(InvalidationListener listener)
      {
        super.removeListener(listener);
        unbind(new Observable[] { this.val$action.longTextProperty() });
        this.tooltip.textProperty().unbind();
      }
    });
    if ((btn instanceof ToggleButton)) {
      ((ToggleButton)btn).selectedProperty().bindBidirectional(action.selectedProperty());
    }
    btn.setOnAction(action);
    
    return btn;
  }
  
  private static void unconfigure(ButtonBase btn)
  {
    if ((btn == null) || (!(btn.getOnAction() instanceof Action))) {
      return;
    }
    Action action = (Action)btn.getOnAction();
    
    btn.styleProperty().unbind();
    btn.textProperty().unbind();
    btn.disableProperty().unbind();
    btn.graphicProperty().unbind();
    
    action.getProperties().removeListener(new ButtonPropertiesMapChangeListener(btn, action, null));
    
    btn.tooltipProperty().unbind();
    if ((btn instanceof ToggleButton)) {
      ((ToggleButton)btn).selectedProperty().unbindBidirectional(action.selectedProperty());
    }
    btn.setOnAction(null);
  }
  
  private static <T extends MenuItem> T configure(T menuItem, Action action)
  {
    if (action == null) {
      throw new NullPointerException("Action can not be null");
    }
    bindStyle(menuItem, action);
    
    menuItem.textProperty().bind(action.textProperty());
    menuItem.disableProperty().bind(action.disabledProperty());
    menuItem.acceleratorProperty().bind(action.acceleratorProperty());
    
    menuItem.graphicProperty().bind(new ObjectBinding()
    {
      protected Node computeValue()
      {
        return ActionUtils.copyNode((Node)this.val$action.graphicProperty().get());
      }
      
      public void removeListener(InvalidationListener listener)
      {
        super.removeListener(listener);
        unbind(new Observable[] { this.val$action.graphicProperty() });
      }
    });
    menuItem.getProperties().putAll(action.getProperties());
    action.getProperties().addListener(new MenuItemPropertiesMapChangeListener(menuItem, action, null));
    if ((menuItem instanceof RadioMenuItem)) {
      ((RadioMenuItem)menuItem).selectedProperty().bindBidirectional(action.selectedProperty());
    } else if ((menuItem instanceof CheckMenuItem)) {
      ((CheckMenuItem)menuItem).selectedProperty().bindBidirectional(action.selectedProperty());
    }
    menuItem.setOnAction(action);
    
    return menuItem;
  }
  
  private static void unconfigure(MenuItem menuItem)
  {
    if ((menuItem == null) || (!(menuItem.getOnAction() instanceof Action))) {
      return;
    }
    Action action = (Action)menuItem.getOnAction();
    
    menuItem.styleProperty().unbind();
    menuItem.textProperty().unbind();
    menuItem.disableProperty().unbind();
    menuItem.acceleratorProperty().unbind();
    menuItem.graphicProperty().unbind();
    
    action.getProperties().removeListener(new MenuItemPropertiesMapChangeListener(menuItem, action, null));
    if ((menuItem instanceof RadioMenuItem)) {
      ((RadioMenuItem)menuItem).selectedProperty().unbindBidirectional(action.selectedProperty());
    } else if ((menuItem instanceof CheckMenuItem)) {
      ((CheckMenuItem)menuItem).selectedProperty().unbindBidirectional(action.selectedProperty());
    }
    menuItem.setOnAction(null);
  }
  
  private static class ButtonPropertiesMapChangeListener<T extends ButtonBase>
    implements MapChangeListener<Object, Object>
  {
    private final WeakReference<T> btnWeakReference;
    private final Action action;
    
    private ButtonPropertiesMapChangeListener(T btn, Action action)
    {
      this.btnWeakReference = new WeakReference(btn);
      this.action = action;
    }
    
    public void onChanged(MapChangeListener.Change<?, ?> change)
    {
      T btn = (ButtonBase)this.btnWeakReference.get();
      if (btn == null)
      {
        this.action.getProperties().removeListener(this);
      }
      else
      {
        btn.getProperties().clear();
        btn.getProperties().putAll(this.action.getProperties());
      }
    }
    
    public boolean equals(Object otherObject)
    {
      if (this == otherObject) {
        return true;
      }
      if ((otherObject == null) || (getClass() != otherObject.getClass())) {
        return false;
      }
      ButtonPropertiesMapChangeListener<?> otherListener = (ButtonPropertiesMapChangeListener)otherObject;
      
      T btn = (ButtonBase)this.btnWeakReference.get();
      ButtonBase otherBtn = (ButtonBase)otherListener.btnWeakReference.get();
      if (btn != null ? !btn.equals(otherBtn) : otherBtn != null) {
        return false;
      }
      return this.action.equals(otherListener.action);
    }
    
    public int hashCode()
    {
      T btn = (ButtonBase)this.btnWeakReference.get();
      int result = btn != null ? btn.hashCode() : 0;
      result = 31 * result + this.action.hashCode();
      return result;
    }
  }
  
  private static class MenuItemPropertiesMapChangeListener<T extends MenuItem>
    implements MapChangeListener<Object, Object>
  {
    private final WeakReference<T> menuItemWeakReference;
    private final Action action;
    
    private MenuItemPropertiesMapChangeListener(T menuItem, Action action)
    {
      this.menuItemWeakReference = new WeakReference(menuItem);
      this.action = action;
    }
    
    public void onChanged(MapChangeListener.Change<?, ?> change)
    {
      T menuItem = (MenuItem)this.menuItemWeakReference.get();
      if (menuItem == null)
      {
        this.action.getProperties().removeListener(this);
      }
      else
      {
        menuItem.getProperties().clear();
        menuItem.getProperties().putAll(this.action.getProperties());
      }
    }
    
    public boolean equals(Object otherObject)
    {
      if (this == otherObject) {
        return true;
      }
      if ((otherObject == null) || (getClass() != otherObject.getClass())) {
        return false;
      }
      MenuItemPropertiesMapChangeListener<?> otherListener = (MenuItemPropertiesMapChangeListener)otherObject;
      
      T menuItem = (MenuItem)this.menuItemWeakReference.get();
      MenuItem otherMenuItem = (MenuItem)otherListener.menuItemWeakReference.get();
      return (otherMenuItem == null) && (this.action.equals(otherListener.action)) ? true : menuItem != null ? menuItem.equals(otherMenuItem) : false;
    }
    
    public int hashCode()
    {
      T menuItem = (MenuItem)this.menuItemWeakReference.get();
      int result = menuItem != null ? menuItem.hashCode() : 0;
      result = 31 * result + this.action.hashCode();
      return result;
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\controlsfx\control\action\ActionUtils.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */