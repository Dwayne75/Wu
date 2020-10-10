package org.fourthline.cling.support.shared;

import java.awt.Dimension;
import java.awt.Frame;
import java.util.logging.Logger;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import org.fourthline.cling.model.ModelUtil;
import org.seamless.swing.Application;
import org.seamless.xml.DOM;
import org.seamless.xml.DOMParser;
import org.w3c.dom.Document;

public class TextExpandDialog
  extends JDialog
{
  private static Logger log = Logger.getLogger(TextExpandDialog.class.getName());
  
  public TextExpandDialog(Frame frame, String text)
  {
    super(frame);
    setResizable(true);
    
    JTextArea textArea = new JTextArea();
    JScrollPane textPane = new JScrollPane(textArea);
    textPane.setPreferredSize(new Dimension(500, 400));
    add(textPane);
    String pretty;
    if ((text.startsWith("<")) && (text.endsWith(">")))
    {
      String pretty;
      try
      {
        pretty = new DOMParser()
        {
          protected DOM createDOM(Document document)
          {
            return null;
          }
        }
        
          .print(text, 2, false);
      }
      catch (Exception ex)
      {
        String pretty;
        log.severe("Error pretty printing XML: " + ex.toString());
        pretty = text;
      }
    }
    else
    {
      String pretty;
      if (text.startsWith("http-get")) {
        pretty = ModelUtil.commaToNewline(text);
      } else {
        pretty = text;
      }
    }
    textArea.setEditable(false);
    textArea.setText(pretty);
    
    pack();
    Application.center(this, getOwner());
    setVisible(true);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\shared\TextExpandDialog.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */