package org.controlsfx.control;

import impl.org.controlsfx.skin.RatingSkin;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.control.Skin;

public class Rating
  extends ControlsFXControl
{
  public Rating()
  {
    this(5);
  }
  
  public Rating(int max)
  {
    this(max, -1);
  }
  
  public Rating(int max, int rating)
  {
    getStyleClass().setAll(new String[] { "rating" });
    
    setMax(max);
    setRating(rating == -1 ? (int)Math.floor(max / 2.0D) : rating);
  }
  
  protected Skin<?> createDefaultSkin()
  {
    return new RatingSkin(this);
  }
  
  public String getUserAgentStylesheet()
  {
    return getUserAgentStylesheet(Rating.class, "rating.css");
  }
  
  public final DoubleProperty ratingProperty()
  {
    return this.rating;
  }
  
  private DoubleProperty rating = new SimpleDoubleProperty(this, "rating", 3.0D);
  
  public final void setRating(double value)
  {
    ratingProperty().set(value);
  }
  
  public final double getRating()
  {
    return this.rating == null ? 3.0D : this.rating.get();
  }
  
  public final IntegerProperty maxProperty()
  {
    return this.max;
  }
  
  private IntegerProperty max = new SimpleIntegerProperty(this, "max", 5);
  private ObjectProperty<Orientation> orientation;
  
  public final void setMax(int value)
  {
    maxProperty().set(value);
  }
  
  public final int getMax()
  {
    return this.max == null ? 5 : this.max.get();
  }
  
  public final ObjectProperty<Orientation> orientationProperty()
  {
    if (this.orientation == null) {
      this.orientation = new SimpleObjectProperty(this, "orientation", Orientation.HORIZONTAL);
    }
    return this.orientation;
  }
  
  public final void setOrientation(Orientation value)
  {
    orientationProperty().set(value);
  }
  
  public final Orientation getOrientation()
  {
    return this.orientation == null ? Orientation.HORIZONTAL : (Orientation)this.orientation.get();
  }
  
  public final BooleanProperty partialRatingProperty()
  {
    return this.partialRating;
  }
  
  private BooleanProperty partialRating = new SimpleBooleanProperty(this, "partialRating", false);
  
  public final void setPartialRating(boolean value)
  {
    partialRatingProperty().set(value);
  }
  
  public final boolean isPartialRating()
  {
    return this.partialRating == null ? false : this.partialRating.get();
  }
  
  public final BooleanProperty updateOnHoverProperty()
  {
    return this.updateOnHover;
  }
  
  private BooleanProperty updateOnHover = new SimpleBooleanProperty(this, "updateOnHover", false);
  
  public final void setUpdateOnHover(boolean value)
  {
    updateOnHoverProperty().set(value);
  }
  
  public final boolean isUpdateOnHover()
  {
    return this.updateOnHover == null ? false : this.updateOnHover.get();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\controlsfx\control\Rating.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */