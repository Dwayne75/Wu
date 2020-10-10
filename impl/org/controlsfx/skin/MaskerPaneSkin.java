package impl.org.controlsfx.skin;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.controlsfx.control.MaskerPane;

public class MaskerPaneSkin
  extends SkinBase<MaskerPane>
{
  public MaskerPaneSkin(MaskerPane maskerPane)
  {
    super(maskerPane);
    getChildren().add(createMasker(maskerPane));
  }
  
  private StackPane createMasker(MaskerPane maskerPane)
  {
    VBox vBox = new VBox();
    vBox.setAlignment(Pos.CENTER);
    vBox.setSpacing(10.0D);
    vBox.getStyleClass().add("masker-center");
    
    vBox.getChildren().add(createLabel());
    vBox.getChildren().add(createProgressIndicator());
    
    HBox hBox = new HBox();
    hBox.setAlignment(Pos.CENTER);
    hBox.getChildren().addAll(new Node[] { vBox });
    
    StackPane glass = new StackPane();
    glass.setAlignment(Pos.CENTER);
    glass.getStyleClass().add("masker-glass");
    glass.getChildren().add(hBox);
    
    return glass;
  }
  
  private Label createLabel()
  {
    Label text = new Label();
    text.textProperty().bind(((MaskerPane)getSkinnable()).textProperty());
    text.getStyleClass().add("masker-text");
    return text;
  }
  
  private Label createProgressIndicator()
  {
    Label graphic = new Label();
    graphic.setGraphic(((MaskerPane)getSkinnable()).getProgressNode());
    graphic.visibleProperty().bind(((MaskerPane)getSkinnable()).progressVisibleProperty());
    graphic.getStyleClass().add("masker-graphic");
    return graphic;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\impl\org\controlsfx\skin\MaskerPaneSkin.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */