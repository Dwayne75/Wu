package org.fourthline.cling.support.model.container;

import org.fourthline.cling.support.model.DIDLObject.Class;

public class MusicGenre
  extends GenreContainer
{
  public static final DIDLObject.Class CLASS = new DIDLObject.Class("object.container.genre.musicGenre");
  
  public MusicGenre()
  {
    setClazz(CLASS);
  }
  
  public MusicGenre(Container other)
  {
    super(other);
  }
  
  public MusicGenre(String id, Container parent, String title, String creator, Integer childCount)
  {
    this(id, parent.getId(), title, creator, childCount);
  }
  
  public MusicGenre(String id, String parentID, String title, String creator, Integer childCount)
  {
    super(id, parentID, title, creator, childCount);
    setClazz(CLASS);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\model\container\MusicGenre.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */