package org.controlsfx.control;

import com.sun.javafx.event.EventHandlerManager;
import impl.org.controlsfx.skin.BreadCrumbBarSkin;
import impl.org.controlsfx.skin.BreadCrumbBarSkin.BreadCrumbButton;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventDispatchChain;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.control.Button;
import javafx.scene.control.Skin;
import javafx.scene.control.TreeItem;
import javafx.util.Callback;

public class BreadCrumbBar<T>
  extends ControlsFXControl
{
  private final EventHandlerManager eventHandlerManager = new EventHandlerManager(this);
  
  public static class BreadCrumbActionEvent<TE>
    extends Event
  {
    public static final EventType<BreadCrumbActionEvent> CRUMB_ACTION = new EventType("CRUMB_ACTION");
    private final TreeItem<TE> selectedCrumb;
    
    public BreadCrumbActionEvent(TreeItem<TE> selectedCrumb)
    {
      super();
      this.selectedCrumb = selectedCrumb;
    }
    
    public TreeItem<TE> getSelectedCrumb()
    {
      return this.selectedCrumb;
    }
  }
  
  public static <T> TreeItem<T> buildTreeModel(T... crumbs)
  {
    TreeItem<T> subRoot = null;
    for (T crumb : crumbs)
    {
      TreeItem<T> currentNode = new TreeItem(crumb);
      if (subRoot == null)
      {
        subRoot = currentNode;
      }
      else
      {
        subRoot.getChildren().add(currentNode);
        subRoot = currentNode;
      }
    }
    return subRoot;
  }
  
  private final Callback<TreeItem<T>, Button> defaultCrumbNodeFactory = new Callback()
  {
    public Button call(TreeItem<T> crumb)
    {
      return new BreadCrumbBarSkin.BreadCrumbButton(crumb.getValue() != null ? crumb.getValue().toString() : "");
    }
  };
  
  public BreadCrumbBar()
  {
    this(null);
  }
  
  public BreadCrumbBar(TreeItem<T> selectedCrumb)
  {
    getStyleClass().add("bread-crumb-bar");
    setSelectedCrumb(selectedCrumb);
    setCrumbFactory(this.defaultCrumbNodeFactory);
  }
  
  public EventDispatchChain buildEventDispatchChain(EventDispatchChain tail)
  {
    return tail.prepend(this.eventHandlerManager);
  }
  
  public final ObjectProperty<TreeItem<T>> selectedCrumbProperty()
  {
    return this.selectedCrumb;
  }
  
  private final ObjectProperty<TreeItem<T>> selectedCrumb = new SimpleObjectProperty(this, "selectedCrumb");
  
  public final TreeItem<T> getSelectedCrumb()
  {
    return (TreeItem)this.selectedCrumb.get();
  }
  
  public final void setSelectedCrumb(TreeItem<T> selectedCrumb)
  {
    this.selectedCrumb.set(selectedCrumb);
  }
  
  public final BooleanProperty autoNavigationEnabledProperty()
  {
    return this.autoNavigation;
  }
  
  private final BooleanProperty autoNavigation = new SimpleBooleanProperty(this, "autoNavigationEnabled", true);
  
  public final boolean isAutoNavigationEnabled()
  {
    return this.autoNavigation.get();
  }
  
  public final void setAutoNavigationEnabled(boolean enabled)
  {
    this.autoNavigation.set(enabled);
  }
  
  public final ObjectProperty<Callback<TreeItem<T>, Button>> crumbFactoryProperty()
  {
    return this.crumbFactory;
  }
  
  private final ObjectProperty<Callback<TreeItem<T>, Button>> crumbFactory = new SimpleObjectProperty(this, "crumbFactory");
  
  public final void setCrumbFactory(Callback<TreeItem<T>, Button> value)
  {
    if (value == null) {
      value = this.defaultCrumbNodeFactory;
    }
    crumbFactoryProperty().set(value);
  }
  
  public final Callback<TreeItem<T>, Button> getCrumbFactory()
  {
    return (Callback)this.crumbFactory.get();
  }
  
  public final ObjectProperty<EventHandler<BreadCrumbActionEvent<T>>> onCrumbActionProperty()
  {
    return this.onCrumbAction;
  }
  
  public final void setOnCrumbAction(EventHandler<BreadCrumbActionEvent<T>> value)
  {
    onCrumbActionProperty().set(value);
  }
  
  public final EventHandler<BreadCrumbActionEvent<T>> getOnCrumbAction()
  {
    return (EventHandler)onCrumbActionProperty().get();
  }
  
  private ObjectProperty<EventHandler<BreadCrumbActionEvent<T>>> onCrumbAction = new ObjectPropertyBase()
  {
    protected void invalidated()
    {
      BreadCrumbBar.this.eventHandlerManager.setEventHandler(BreadCrumbBar.BreadCrumbActionEvent.CRUMB_ACTION, (EventHandler)get());
    }
    
    public Object getBean()
    {
      return BreadCrumbBar.this;
    }
    
    public String getName()
    {
      return "onCrumbAction";
    }
  };
  private static final String DEFAULT_STYLE_CLASS = "bread-crumb-bar";
  
  protected Skin<?> createDefaultSkin()
  {
    return new BreadCrumbBarSkin(this);
  }
  
  public String getUserAgentStylesheet()
  {
    return getUserAgentStylesheet(BreadCrumbBar.class, "breadcrumbbar.css");
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\controlsfx\control\BreadCrumbBar.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */