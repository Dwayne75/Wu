package org.fourthline.cling.support.model.container;

import java.net.URI;
import java.util.List;
import org.fourthline.cling.support.model.DIDLObject.Class;
import org.fourthline.cling.support.model.DIDLObject.Property.UPNP.ARTIST_DISCO_URI;
import org.fourthline.cling.support.model.DIDLObject.Property.UPNP.GENRE;

public class MusicArtist
  extends PersonContainer
{
  public static final DIDLObject.Class CLASS = new DIDLObject.Class("object.container.person.musicArtist");
  
  public MusicArtist()
  {
    setClazz(CLASS);
  }
  
  public MusicArtist(Container other)
  {
    super(other);
  }
  
  public MusicArtist(String id, Container parent, String title, String creator, Integer childCount)
  {
    this(id, parent.getId(), title, creator, childCount);
  }
  
  public MusicArtist(String id, String parentID, String title, String creator, Integer childCount)
  {
    super(id, parentID, title, creator, childCount);
    setClazz(CLASS);
  }
  
  public String getFirstGenre()
  {
    return (String)getFirstPropertyValue(DIDLObject.Property.UPNP.GENRE.class);
  }
  
  public String[] getGenres()
  {
    List<String> list = getPropertyValues(DIDLObject.Property.UPNP.GENRE.class);
    return (String[])list.toArray(new String[list.size()]);
  }
  
  public MusicArtist setGenres(String[] genres)
  {
    removeProperties(DIDLObject.Property.UPNP.GENRE.class);
    for (String genre : genres) {
      addProperty(new DIDLObject.Property.UPNP.GENRE(genre));
    }
    return this;
  }
  
  public URI getArtistDiscographyURI()
  {
    return (URI)getFirstPropertyValue(DIDLObject.Property.UPNP.ARTIST_DISCO_URI.class);
  }
  
  public MusicArtist setArtistDiscographyURI(URI uri)
  {
    replaceFirstProperty(new DIDLObject.Property.UPNP.ARTIST_DISCO_URI(uri));
    return this;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\model\container\MusicArtist.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */