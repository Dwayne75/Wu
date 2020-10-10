package impl.org.controlsfx.skin;

import com.sun.javafx.scene.control.behavior.BehaviorBase;
import com.sun.javafx.scene.control.skin.BehaviorSkinBase;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.controlsfx.control.HyperlinkLabel;

public class HyperlinkLabelSkin
  extends BehaviorSkinBase<HyperlinkLabel, BehaviorBase<HyperlinkLabel>>
{
  private static final String HYPERLINK_START = "[";
  private static final String HYPERLINK_END = "]";
  private final TextFlow textFlow;
  private final EventHandler<ActionEvent> eventHandler = new EventHandler()
  {
    public void handle(ActionEvent event)
    {
      EventHandler<ActionEvent> onActionHandler = ((HyperlinkLabel)HyperlinkLabelSkin.this.getSkinnable()).getOnAction();
      if (onActionHandler != null) {
        onActionHandler.handle(event);
      }
    }
  };
  
  public HyperlinkLabelSkin(HyperlinkLabel control)
  {
    super(control, new BehaviorBase(control, Collections.emptyList()));
    
    this.textFlow = new TextFlow();
    getChildren().add(this.textFlow);
    updateText();
    
    registerChangeListener(control.textProperty(), "TEXT");
  }
  
  protected void handleControlPropertyChanged(String p)
  {
    super.handleControlPropertyChanged(p);
    if (p == "TEXT") {
      updateText();
    }
  }
  
  private void updateText()
  {
    String text = ((HyperlinkLabel)getSkinnable()).getText();
    if ((text == null) || (text.isEmpty()))
    {
      this.textFlow.getChildren().clear();
      return;
    }
    List<Node> nodes = new ArrayList();
    
    int start = 0;
    int textLength = text.length();
    while ((start != -1) && (start < textLength))
    {
      int startPos = text.indexOf("[", start);
      int endPos = text.indexOf("]", startPos);
      if (((startPos == -1) || (endPos == -1)) && 
        (textLength > start))
      {
        Label label = new Label(text.substring(start));
        nodes.add(label);
        break;
      }
      Text label = new Text(text.substring(start, startPos));
      nodes.add(label);
      
      Hyperlink hyperlink = new Hyperlink(text.substring(startPos + 1, endPos));
      hyperlink.setPadding(new Insets(0.0D, 0.0D, 0.0D, 0.0D));
      hyperlink.setOnAction(this.eventHandler);
      nodes.add(hyperlink);
      
      start = endPos + 1;
    }
    this.textFlow.getChildren().setAll(nodes);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\impl\org\controlsfx\skin\HyperlinkLabelSkin.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */