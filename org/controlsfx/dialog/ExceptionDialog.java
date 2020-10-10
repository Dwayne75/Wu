package org.controlsfx.dialog;

import impl.org.controlsfx.i18n.Localization;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import javafx.collections.ObservableList;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

public class ExceptionDialog
  extends Dialog<ButtonType>
{
  public ExceptionDialog(Throwable exception)
  {
    DialogPane dialogPane = getDialogPane();
    
    setTitle(Localization.getString("exception.dlg.title"));
    dialogPane.setHeaderText(Localization.getString("exception.dlg.header"));
    dialogPane.getStyleClass().add("exception-dialog");
    dialogPane.getStylesheets().add(ProgressDialog.class.getResource("dialogs.css").toExternalForm());
    dialogPane.getButtonTypes().addAll(new ButtonType[] { ButtonType.OK });
    
    String contentText = getContentText();
    dialogPane.setContent(new Label((contentText != null) && (!contentText.isEmpty()) ? contentText : exception
      .getMessage()));
    
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    exception.printStackTrace(pw);
    String exceptionText = sw.toString();
    
    Label label = new Label(Localization.localize(Localization.getString("exception.dlg.label")));
    
    TextArea textArea = new TextArea(exceptionText);
    textArea.setEditable(false);
    textArea.setWrapText(true);
    
    textArea.setMaxWidth(Double.MAX_VALUE);
    textArea.setMaxHeight(Double.MAX_VALUE);
    GridPane.setVgrow(textArea, Priority.ALWAYS);
    GridPane.setHgrow(textArea, Priority.ALWAYS);
    
    GridPane root = new GridPane();
    root.setMaxWidth(Double.MAX_VALUE);
    root.add(label, 0, 0);
    root.add(textArea, 0, 1);
    
    dialogPane.setExpandableContent(root);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\controlsfx\dialog\ExceptionDialog.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */