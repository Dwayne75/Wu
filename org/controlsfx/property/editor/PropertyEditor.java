package org.controlsfx.property.editor;

import javafx.scene.Node;

public abstract interface PropertyEditor<T>
{
  public abstract Node getEditor();
  
  public abstract T getValue();
  
  public abstract void setValue(T paramT);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\controlsfx\property\editor\PropertyEditor.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */