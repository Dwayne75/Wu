package org.controlsfx.control;

import impl.org.controlsfx.skin.MaskerPaneSkin;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Skin;

public class MaskerPane
  extends ControlsFXControl
{
  public MaskerPane()
  {
    getStyleClass().add("masker-pane");
  }
  
  private final DoubleProperty progress = new SimpleDoubleProperty(this, "progress", -1.0D);
  
  public final DoubleProperty progressProperty()
  {
    return this.progress;
  }
  
  public final double getProgress()
  {
    return this.progress.get();
  }
  
  public final void setProgress(double progress)
  {
    this.progress.set(progress);
  }
  
  private final ObjectProperty<Node> progressNode = new SimpleObjectProperty()
  {
    public String getName()
    {
      return "progressNode";
    }
    
    public Object getBean()
    {
      return MaskerPane.this;
    }
  };
  
  public final ObjectProperty<Node> progressNodeProperty()
  {
    return this.progressNode;
  }
  
  public final Node getProgressNode()
  {
    return (Node)this.progressNode.get();
  }
  
  public final void setProgressNode(Node progressNode)
  {
    this.progressNode.set(progressNode);
  }
  
  private final BooleanProperty progressVisible = new SimpleBooleanProperty(this, "progressVisible", true);
  
  public final BooleanProperty progressVisibleProperty()
  {
    return this.progressVisible;
  }
  
  public final boolean getProgressVisible()
  {
    return this.progressVisible.get();
  }
  
  public final void setProgressVisible(boolean progressVisible)
  {
    this.progressVisible.set(progressVisible);
  }
  
  private final StringProperty text = new SimpleStringProperty(this, "text", "Please Wait...");
  
  public final StringProperty textProperty()
  {
    return this.text;
  }
  
  public final String getText()
  {
    return (String)this.text.get();
  }
  
  public final void setText(String text)
  {
    this.text.set(text);
  }
  
  protected Skin<?> createDefaultSkin()
  {
    return new MaskerPaneSkin(this);
  }
  
  public String getUserAgentStylesheet()
  {
    return getUserAgentStylesheet(MaskerPane.class, "maskerpane.css");
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\controlsfx\control\MaskerPane.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */