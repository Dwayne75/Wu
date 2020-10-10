package org.controlsfx.tools;

import com.sun.javafx.webkit.Accessor;
import com.sun.webkit.WebPage;
import java.net.URL;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.concurrent.Worker.State;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.SnapshotResult;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.util.Callback;

class SVGLoader
{
  public static void loadSVGImage(URL svgImage, double prefWidth, double prefHeight, Callback<ImageView, Void> callback)
  {
    loadSVGImage(svgImage, prefWidth, prefHeight, callback, null);
  }
  
  public static void loadSVGImage(URL svgImage, WritableImage outputImage)
  {
    if (outputImage == null) {
      throw new NullPointerException("outputImage can not be null");
    }
    double w = outputImage.getWidth();
    double h = outputImage.getHeight();
    loadSVGImage(svgImage, w, h, null, outputImage);
  }
  
  public static void loadSVGImage(URL svgImage, double prefWidth, final double prefHeight, Callback<ImageView, Void> callback, final WritableImage outputImage)
  {
    final WebView view = new WebView();
    WebEngine eng = view.getEngine();
    
    WebPage webPage = Accessor.getPageFor(eng);
    webPage.setBackgroundColor(webPage.getMainFrame(), 65280);
    webPage.setOpaque(webPage.getMainFrame(), false);
    
    Scene scene = new Scene(view);
    final Stage stage = new Stage();
    stage.setScene(scene);
    stage.setWidth(0.0D);
    stage.setHeight(0.0D);
    stage.setOpacity(0.0D);
    stage.show();
    
    String content = "<html><body style=\"margin-top: 0px; margin-bottom: 30px; margin-left: 0px; margin-right: 0px; padding: 0;\"><img id=\"svgImage\" style=\"display: block;float: top;\" width=\"" + prefWidth + "\" height=\"" + prefHeight + "\" src=\"" + svgImage.toExternalForm() + "\" /></body></head>";
    
    eng.loadContent(content);
    
    eng.getLoadWorker().stateProperty().addListener(new ChangeListener()
    {
      public void changed(ObservableValue<? extends Worker.State> o, Worker.State oldValue, Worker.State newValue)
      {
        if (newValue == Worker.State.SUCCEEDED)
        {
          double svgWidth = this.val$prefWidth >= 0.0D ? this.val$prefWidth : SVGLoader.getSvgWidth(prefHeight);
          double svgHeight = view >= 0.0D ? this.val$prefWidth : SVGLoader.getSvgHeight(prefHeight);
          
          SnapshotParameters params = new SnapshotParameters();
          params.setFill(Color.TRANSPARENT);
          params.setViewport(new Rectangle2D(0.0D, 0.0D, svgWidth, svgHeight));
          
          stage.snapshot(new Callback()
          {
            public Void call(SnapshotResult param)
            {
              WritableImage snapshot = param.getImage();
              ImageView image = new ImageView(snapshot);
              if (SVGLoader.1.this.val$callback != null) {
                SVGLoader.1.this.val$callback.call(image);
              }
              SVGLoader.1.this.val$stage.hide();
              return null;
            }
          }, params, this.val$outputImage);
        }
      }
    });
  }
  
  private static double getSvgWidth(WebEngine webEngine)
  {
    Object result = getSvgDomProperty(webEngine, "offsetWidth");
    if ((result instanceof Integer)) {
      return ((Integer)result).intValue();
    }
    return -1.0D;
  }
  
  private static double getSvgHeight(WebEngine webEngine)
  {
    Object result = getSvgDomProperty(webEngine, "offsetHeight");
    if ((result instanceof Integer)) {
      return ((Integer)result).intValue();
    }
    return -1.0D;
  }
  
  private static Object getSvgDomProperty(WebEngine webEngine, String property)
  {
    return webEngine.executeScript("document.getElementById('svgImage')." + property);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\controlsfx\tools\SVGLoader.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */