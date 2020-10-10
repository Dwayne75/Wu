package impl.org.controlsfx.spreadsheet;

import java.net.URL;
import javafx.collections.ObservableList;
import javafx.scene.control.Skin;
import javafx.scene.control.TableView;
import org.controlsfx.control.spreadsheet.SpreadsheetCell;
import org.controlsfx.control.spreadsheet.SpreadsheetView;

public class SpreadsheetGridView
  extends TableView<ObservableList<SpreadsheetCell>>
{
  private final SpreadsheetHandle handle;
  private String stylesheet;
  
  public SpreadsheetGridView(SpreadsheetHandle handle)
  {
    this.handle = handle;
  }
  
  public String getUserAgentStylesheet()
  {
    if (this.stylesheet == null) {
      this.stylesheet = SpreadsheetView.class.getResource("spreadsheet.css").toExternalForm();
    }
    return this.stylesheet;
  }
  
  protected Skin<?> createDefaultSkin()
  {
    return new GridViewSkin(this.handle);
  }
  
  public GridViewSkin getGridViewSkin()
  {
    return this.handle.getCellsViewSkin();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\impl\org\controlsfx\spreadsheet\SpreadsheetGridView.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */