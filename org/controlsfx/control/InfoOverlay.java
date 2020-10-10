package org.controlsfx.control;

import impl.org.controlsfx.skin.InfoOverlaySkin;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Skin;
import javafx.scene.image.ImageView;

public class InfoOverlay
  extends ControlsFXControl
{
  public InfoOverlay()
  {
    this((Node)null, null);
  }
  
  public InfoOverlay(String imageUrl, String text)
  {
    this(new ImageView(imageUrl), text);
  }
  
  public InfoOverlay(Node content, String text)
  {
    getStyleClass().setAll(new String[] { "info-overlay" });
    
    setContent(content);
    setText(text);
  }
  
  protected Skin<?> createDefaultSkin()
  {
    return new InfoOverlaySkin(this);
  }
  
  private ObjectProperty<Node> content = new SimpleObjectProperty(this, "content");
  
  public final ObjectProperty<Node> contentProperty()
  {
    return this.content;
  }
  
  public final void setContent(Node content)
  {
    contentProperty().set(content);
  }
  
  public final Node getContent()
  {
    return (Node)contentProperty().get();
  }
  
  private StringProperty text = new SimpleStringProperty(this, "text");
  
  public final StringProperty textProperty()
  {
    return this.text;
  }
  
  public final String getText()
  {
    return (String)textProperty().get();
  }
  
  public final void setText(String text)
  {
    textProperty().set(text);
  }
  
  private BooleanProperty showOnHover = new SimpleBooleanProperty(this, "showOnHover", true);
  private static final String DEFAULT_STYLE_CLASS = "info-overlay";
  
  public final BooleanProperty showOnHoverProperty()
  {
    return this.showOnHover;
  }
  
  public final boolean isShowOnHover()
  {
    return showOnHoverProperty().get();
  }
  
  public final void setShowOnHover(boolean value)
  {
    showOnHoverProperty().set(value);
  }
  
  public String getUserAgentStylesheet()
  {
    return getUserAgentStylesheet(InfoOverlay.class, "info-overlay.css");
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\controlsfx\control\InfoOverlay.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */