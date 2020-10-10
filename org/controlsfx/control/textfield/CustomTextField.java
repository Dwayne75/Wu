package org.controlsfx.control.textfield;

import impl.org.controlsfx.skin.CustomTextFieldSkin;
import java.net.URL;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Skin;
import javafx.scene.control.TextField;

public class CustomTextField
  extends TextField
{
  public CustomTextField()
  {
    getStyleClass().add("custom-text-field");
  }
  
  private ObjectProperty<Node> left = new SimpleObjectProperty(this, "left");
  
  public final ObjectProperty<Node> leftProperty()
  {
    return this.left;
  }
  
  public final Node getLeft()
  {
    return (Node)this.left.get();
  }
  
  public final void setLeft(Node value)
  {
    this.left.set(value);
  }
  
  private ObjectProperty<Node> right = new SimpleObjectProperty(this, "right");
  
  public final ObjectProperty<Node> rightProperty()
  {
    return this.right;
  }
  
  public final Node getRight()
  {
    return (Node)this.right.get();
  }
  
  public final void setRight(Node value)
  {
    this.right.set(value);
  }
  
  protected Skin<?> createDefaultSkin()
  {
    new CustomTextFieldSkin(this)
    {
      public ObjectProperty<Node> leftProperty()
      {
        return CustomTextField.this.leftProperty();
      }
      
      public ObjectProperty<Node> rightProperty()
      {
        return CustomTextField.this.rightProperty();
      }
    };
  }
  
  public String getUserAgentStylesheet()
  {
    return CustomTextField.class.getResource("customtextfield.css").toExternalForm();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\controlsfx\control\textfield\CustomTextField.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */