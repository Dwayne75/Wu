package org.fourthline.cling.support.model;

import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import org.w3c.dom.Element;

public abstract class DIDLObject
{
  protected String id;
  protected String parentID;
  protected String title;
  protected String creator;
  protected DIDLObject() {}
  
  public static abstract class Property<V>
  {
    private V value;
    private final String descriptorName;
    private final List<Property<DIDLAttribute>> attributes = new ArrayList();
    
    protected Property()
    {
      this(null, null);
    }
    
    protected Property(String descriptorName)
    {
      this(null, descriptorName);
    }
    
    protected Property(V value, String descriptorName)
    {
      this.value = value;
      
      this.descriptorName = (descriptorName == null ? 
        getClass().getSimpleName().toLowerCase(Locale.ROOT).replace("didlobject$property$upnp$", "") : descriptorName);
    }
    
    protected Property(V value, String descriptorName, List<Property<DIDLAttribute>> attributes)
    {
      this.value = value;
      
      this.descriptorName = (descriptorName == null ? 
        getClass().getSimpleName().toLowerCase(Locale.ROOT).replace("didlobject$property$upnp$", "") : descriptorName);
      
      this.attributes.addAll(attributes);
    }
    
    public V getValue()
    {
      return (V)this.value;
    }
    
    public void setValue(V value)
    {
      this.value = value;
    }
    
    public String getDescriptorName()
    {
      return this.descriptorName;
    }
    
    public void setOnElement(Element element)
    {
      element.setTextContent(toString());
      for (Property<DIDLAttribute> attr : this.attributes) {
        element.setAttributeNS(
          ((DIDLAttribute)attr.getValue()).getNamespaceURI(), 
          ((DIDLAttribute)attr.getValue()).getPrefix() + ':' + attr.getDescriptorName(), 
          ((DIDLAttribute)attr.getValue()).getValue());
      }
    }
    
    public void addAttribute(Property<DIDLAttribute> attr)
    {
      this.attributes.add(attr);
    }
    
    public void removeAttribute(Property<DIDLAttribute> attr)
    {
      this.attributes.remove(attr);
    }
    
    public void removeAttribute(String descriptorName)
    {
      for (Property<DIDLAttribute> attr : this.attributes) {
        if (attr.getDescriptorName().equals(descriptorName))
        {
          removeAttribute(attr);
          break;
        }
      }
    }
    
    public Property<DIDLAttribute> getAttribute(String descriptorName)
    {
      for (Property<DIDLAttribute> attr : this.attributes) {
        if (attr.getDescriptorName().equals(descriptorName)) {
          return attr;
        }
      }
      return null;
    }
    
    public String toString()
    {
      return getValue() != null ? getValue().toString() : "";
    }
    
    public static abstract interface NAMESPACE {}
    
    public static class PropertyPersonWithRole
      extends DIDLObject.Property<PersonWithRole>
    {
      public PropertyPersonWithRole() {}
      
      public PropertyPersonWithRole(String descriptorName)
      {
        super();
      }
      
      public PropertyPersonWithRole(PersonWithRole value, String descriptorName)
      {
        super(descriptorName);
      }
      
      public void setOnElement(Element element)
      {
        if (getValue() != null) {
          ((PersonWithRole)getValue()).setOnElement(element);
        }
      }
    }
    
    public static class DC
    {
      public static abstract interface NAMESPACE
        extends DIDLObject.Property.NAMESPACE
      {
        public static final String URI = "http://purl.org/dc/elements/1.1/";
      }
      
      public static class DESCRIPTION
        extends DIDLObject.Property<String>
        implements DIDLObject.Property.DC.NAMESPACE
      {
        public DESCRIPTION() {}
        
        public DESCRIPTION(String value)
        {
          super(null);
        }
      }
      
      public static class PUBLISHER
        extends DIDLObject.Property<Person>
        implements DIDLObject.Property.DC.NAMESPACE
      {
        public PUBLISHER() {}
        
        public PUBLISHER(Person value)
        {
          super(null);
        }
      }
      
      public static class CONTRIBUTOR
        extends DIDLObject.Property<Person>
        implements DIDLObject.Property.DC.NAMESPACE
      {
        public CONTRIBUTOR() {}
        
        public CONTRIBUTOR(Person value)
        {
          super(null);
        }
      }
      
      public static class DATE
        extends DIDLObject.Property<String>
        implements DIDLObject.Property.DC.NAMESPACE
      {
        public DATE() {}
        
        public DATE(String value)
        {
          super(null);
        }
      }
      
      public static class LANGUAGE
        extends DIDLObject.Property<String>
        implements DIDLObject.Property.DC.NAMESPACE
      {
        public LANGUAGE() {}
        
        public LANGUAGE(String value)
        {
          super(null);
        }
      }
      
      public static class RELATION
        extends DIDLObject.Property<URI>
        implements DIDLObject.Property.DC.NAMESPACE
      {
        public RELATION() {}
        
        public RELATION(URI value)
        {
          super(null);
        }
      }
      
      public static class RIGHTS
        extends DIDLObject.Property<String>
        implements DIDLObject.Property.DC.NAMESPACE
      {
        public RIGHTS() {}
        
        public RIGHTS(String value)
        {
          super(null);
        }
      }
    }
    
    public static abstract class SEC
    {
      public static abstract interface NAMESPACE
        extends DIDLObject.Property.NAMESPACE
      {
        public static final String URI = "http://www.sec.co.kr/";
      }
      
      public static class CAPTIONINFOEX
        extends DIDLObject.Property<URI>
        implements DIDLObject.Property.SEC.NAMESPACE
      {
        public CAPTIONINFOEX()
        {
          this(null);
        }
        
        public CAPTIONINFOEX(URI value)
        {
          super("CaptionInfoEx");
        }
        
        public CAPTIONINFOEX(URI value, List<DIDLObject.Property<DIDLAttribute>> attributes)
        {
          super("CaptionInfoEx", attributes);
        }
      }
      
      public static class CAPTIONINFO
        extends DIDLObject.Property<URI>
        implements DIDLObject.Property.SEC.NAMESPACE
      {
        public CAPTIONINFO()
        {
          this(null);
        }
        
        public CAPTIONINFO(URI value)
        {
          super("CaptionInfo");
        }
        
        public CAPTIONINFO(URI value, List<DIDLObject.Property<DIDLAttribute>> attributes)
        {
          super("CaptionInfo", attributes);
        }
      }
      
      public static class TYPE
        extends DIDLObject.Property<DIDLAttribute>
        implements DIDLObject.Property.SEC.NAMESPACE
      {
        public TYPE()
        {
          this(null);
        }
        
        public TYPE(DIDLAttribute value)
        {
          super("type");
        }
      }
    }
    
    public static abstract class UPNP
    {
      public static abstract interface NAMESPACE
        extends DIDLObject.Property.NAMESPACE
      {
        public static final String URI = "urn:schemas-upnp-org:metadata-1-0/upnp/";
      }
      
      public static class ARTIST
        extends DIDLObject.Property.PropertyPersonWithRole
        implements DIDLObject.Property.UPNP.NAMESPACE
      {
        public ARTIST() {}
        
        public ARTIST(PersonWithRole value)
        {
          super(null);
        }
      }
      
      public static class ACTOR
        extends DIDLObject.Property.PropertyPersonWithRole
        implements DIDLObject.Property.UPNP.NAMESPACE
      {
        public ACTOR() {}
        
        public ACTOR(PersonWithRole value)
        {
          super(null);
        }
      }
      
      public static class AUTHOR
        extends DIDLObject.Property.PropertyPersonWithRole
        implements DIDLObject.Property.UPNP.NAMESPACE
      {
        public AUTHOR() {}
        
        public AUTHOR(PersonWithRole value)
        {
          super(null);
        }
      }
      
      public static class PRODUCER
        extends DIDLObject.Property<Person>
        implements DIDLObject.Property.UPNP.NAMESPACE
      {
        public PRODUCER() {}
        
        public PRODUCER(Person value)
        {
          super(null);
        }
      }
      
      public static class DIRECTOR
        extends DIDLObject.Property<Person>
        implements DIDLObject.Property.UPNP.NAMESPACE
      {
        public DIRECTOR() {}
        
        public DIRECTOR(Person value)
        {
          super(null);
        }
      }
      
      public static class GENRE
        extends DIDLObject.Property<String>
        implements DIDLObject.Property.UPNP.NAMESPACE
      {
        public GENRE() {}
        
        public GENRE(String value)
        {
          super(null);
        }
      }
      
      public static class ALBUM
        extends DIDLObject.Property<String>
        implements DIDLObject.Property.UPNP.NAMESPACE
      {
        public ALBUM() {}
        
        public ALBUM(String value)
        {
          super(null);
        }
      }
      
      public static class PLAYLIST
        extends DIDLObject.Property<String>
        implements DIDLObject.Property.UPNP.NAMESPACE
      {
        public PLAYLIST() {}
        
        public PLAYLIST(String value)
        {
          super(null);
        }
      }
      
      public static class REGION
        extends DIDLObject.Property<String>
        implements DIDLObject.Property.UPNP.NAMESPACE
      {
        public REGION() {}
        
        public REGION(String value)
        {
          super(null);
        }
      }
      
      public static class RATING
        extends DIDLObject.Property<String>
        implements DIDLObject.Property.UPNP.NAMESPACE
      {
        public RATING() {}
        
        public RATING(String value)
        {
          super(null);
        }
      }
      
      public static class TOC
        extends DIDLObject.Property<String>
        implements DIDLObject.Property.UPNP.NAMESPACE
      {
        public TOC() {}
        
        public TOC(String value)
        {
          super(null);
        }
      }
      
      public static class ALBUM_ART_URI
        extends DIDLObject.Property<URI>
        implements DIDLObject.Property.UPNP.NAMESPACE
      {
        public ALBUM_ART_URI()
        {
          this(null);
        }
        
        public ALBUM_ART_URI(URI value)
        {
          super("albumArtURI");
        }
        
        public ALBUM_ART_URI(URI value, List<DIDLObject.Property<DIDLAttribute>> attributes)
        {
          super("albumArtURI", attributes);
        }
      }
      
      public static class ARTIST_DISCO_URI
        extends DIDLObject.Property<URI>
        implements DIDLObject.Property.UPNP.NAMESPACE
      {
        public ARTIST_DISCO_URI()
        {
          this(null);
        }
        
        public ARTIST_DISCO_URI(URI value)
        {
          super("artistDiscographyURI");
        }
      }
      
      public static class LYRICS_URI
        extends DIDLObject.Property<URI>
        implements DIDLObject.Property.UPNP.NAMESPACE
      {
        public LYRICS_URI()
        {
          this(null);
        }
        
        public LYRICS_URI(URI value)
        {
          super("lyricsURI");
        }
      }
      
      public static class STORAGE_TOTAL
        extends DIDLObject.Property<Long>
        implements DIDLObject.Property.UPNP.NAMESPACE
      {
        public STORAGE_TOTAL()
        {
          this(null);
        }
        
        public STORAGE_TOTAL(Long value)
        {
          super("storageTotal");
        }
      }
      
      public static class STORAGE_USED
        extends DIDLObject.Property<Long>
        implements DIDLObject.Property.UPNP.NAMESPACE
      {
        public STORAGE_USED()
        {
          this(null);
        }
        
        public STORAGE_USED(Long value)
        {
          super("storageUsed");
        }
      }
      
      public static class STORAGE_FREE
        extends DIDLObject.Property<Long>
        implements DIDLObject.Property.UPNP.NAMESPACE
      {
        public STORAGE_FREE()
        {
          this(null);
        }
        
        public STORAGE_FREE(Long value)
        {
          super("storageFree");
        }
      }
      
      public static class STORAGE_MAX_PARTITION
        extends DIDLObject.Property<Long>
        implements DIDLObject.Property.UPNP.NAMESPACE
      {
        public STORAGE_MAX_PARTITION()
        {
          this(null);
        }
        
        public STORAGE_MAX_PARTITION(Long value)
        {
          super("storageMaxPartition");
        }
      }
      
      public static class STORAGE_MEDIUM
        extends DIDLObject.Property<StorageMedium>
        implements DIDLObject.Property.UPNP.NAMESPACE
      {
        public STORAGE_MEDIUM()
        {
          this(null);
        }
        
        public STORAGE_MEDIUM(StorageMedium value)
        {
          super("storageMedium");
        }
      }
      
      public static class LONG_DESCRIPTION
        extends DIDLObject.Property<String>
        implements DIDLObject.Property.UPNP.NAMESPACE
      {
        public LONG_DESCRIPTION()
        {
          this(null);
        }
        
        public LONG_DESCRIPTION(String value)
        {
          super("longDescription");
        }
      }
      
      public static class ICON
        extends DIDLObject.Property<URI>
        implements DIDLObject.Property.UPNP.NAMESPACE
      {
        public ICON()
        {
          this(null);
        }
        
        public ICON(URI value)
        {
          super("icon");
        }
      }
      
      public static class RADIO_CALL_SIGN
        extends DIDLObject.Property<String>
        implements DIDLObject.Property.UPNP.NAMESPACE
      {
        public RADIO_CALL_SIGN()
        {
          this(null);
        }
        
        public RADIO_CALL_SIGN(String value)
        {
          super("radioCallSign");
        }
      }
      
      public static class RADIO_STATION_ID
        extends DIDLObject.Property<String>
        implements DIDLObject.Property.UPNP.NAMESPACE
      {
        public RADIO_STATION_ID()
        {
          this(null);
        }
        
        public RADIO_STATION_ID(String value)
        {
          super("radioStationID");
        }
      }
      
      public static class RADIO_BAND
        extends DIDLObject.Property<String>
        implements DIDLObject.Property.UPNP.NAMESPACE
      {
        public RADIO_BAND()
        {
          this(null);
        }
        
        public RADIO_BAND(String value)
        {
          super("radioBand");
        }
      }
      
      public static class CHANNEL_NR
        extends DIDLObject.Property<Integer>
        implements DIDLObject.Property.UPNP.NAMESPACE
      {
        public CHANNEL_NR()
        {
          this(null);
        }
        
        public CHANNEL_NR(Integer value)
        {
          super("channelNr");
        }
      }
      
      public static class CHANNEL_NAME
        extends DIDLObject.Property<String>
        implements DIDLObject.Property.UPNP.NAMESPACE
      {
        public CHANNEL_NAME()
        {
          this(null);
        }
        
        public CHANNEL_NAME(String value)
        {
          super("channelName");
        }
      }
      
      public static class SCHEDULED_START_TIME
        extends DIDLObject.Property<String>
        implements DIDLObject.Property.UPNP.NAMESPACE
      {
        public SCHEDULED_START_TIME()
        {
          this(null);
        }
        
        public SCHEDULED_START_TIME(String value)
        {
          super("scheduledStartTime");
        }
      }
      
      public static class SCHEDULED_END_TIME
        extends DIDLObject.Property<String>
        implements DIDLObject.Property.UPNP.NAMESPACE
      {
        public SCHEDULED_END_TIME()
        {
          this(null);
        }
        
        public SCHEDULED_END_TIME(String value)
        {
          super("scheduledEndTime");
        }
      }
      
      public static class DVD_REGION_CODE
        extends DIDLObject.Property<Integer>
        implements DIDLObject.Property.UPNP.NAMESPACE
      {
        public DVD_REGION_CODE()
        {
          this(null);
        }
        
        public DVD_REGION_CODE(Integer value)
        {
          super("DVDRegionCode");
        }
      }
      
      public static class ORIGINAL_TRACK_NUMBER
        extends DIDLObject.Property<Integer>
        implements DIDLObject.Property.UPNP.NAMESPACE
      {
        public ORIGINAL_TRACK_NUMBER()
        {
          this(null);
        }
        
        public ORIGINAL_TRACK_NUMBER(Integer value)
        {
          super("originalTrackNumber");
        }
      }
      
      public static class USER_ANNOTATION
        extends DIDLObject.Property<String>
        implements DIDLObject.Property.UPNP.NAMESPACE
      {
        public USER_ANNOTATION()
        {
          this(null);
        }
        
        public USER_ANNOTATION(String value)
        {
          super("userAnnotation");
        }
      }
    }
    
    public static abstract class DLNA
    {
      public static class PROFILE_ID
        extends DIDLObject.Property<DIDLAttribute>
        implements DIDLObject.Property.DLNA.NAMESPACE
      {
        public PROFILE_ID()
        {
          this(null);
        }
        
        public PROFILE_ID(DIDLAttribute value)
        {
          super("profileID");
        }
      }
      
      public static abstract interface NAMESPACE
        extends DIDLObject.Property.NAMESPACE
      {
        public static final String URI = "urn:schemas-dlna-org:metadata-1-0/";
      }
    }
  }
  
  public static class Class
  {
    protected String value;
    protected String friendlyName;
    protected boolean includeDerived;
    
    public Class() {}
    
    public Class(String value)
    {
      this.value = value;
    }
    
    public Class(String value, String friendlyName)
    {
      this.value = value;
      this.friendlyName = friendlyName;
    }
    
    public Class(String value, String friendlyName, boolean includeDerived)
    {
      this.value = value;
      this.friendlyName = friendlyName;
      this.includeDerived = includeDerived;
    }
    
    public String getValue()
    {
      return this.value;
    }
    
    public void setValue(String value)
    {
      this.value = value;
    }
    
    public String getFriendlyName()
    {
      return this.friendlyName;
    }
    
    public void setFriendlyName(String friendlyName)
    {
      this.friendlyName = friendlyName;
    }
    
    public boolean isIncludeDerived()
    {
      return this.includeDerived;
    }
    
    public void setIncludeDerived(boolean includeDerived)
    {
      this.includeDerived = includeDerived;
    }
    
    public boolean equals(DIDLObject instance)
    {
      return getValue().equals(instance.getClazz().getValue());
    }
  }
  
  protected boolean restricted = true;
  protected WriteStatus writeStatus;
  protected Class clazz;
  protected List<Res> resources = new ArrayList();
  protected List<Property> properties = new ArrayList();
  protected List<DescMeta> descMetadata = new ArrayList();
  
  protected DIDLObject(DIDLObject other)
  {
    this(other.getId(), other
      .getParentID(), other
      .getTitle(), other
      .getCreator(), other
      .isRestricted(), other
      .getWriteStatus(), other
      .getClazz(), other
      .getResources(), other
      .getProperties(), other
      .getDescMetadata());
  }
  
  protected DIDLObject(String id, String parentID, String title, String creator, boolean restricted, WriteStatus writeStatus, Class clazz, List<Res> resources, List<Property> properties, List<DescMeta> descMetadata)
  {
    this.id = id;
    this.parentID = parentID;
    this.title = title;
    this.creator = creator;
    this.restricted = restricted;
    this.writeStatus = writeStatus;
    this.clazz = clazz;
    this.resources = resources;
    this.properties = properties;
    this.descMetadata = descMetadata;
  }
  
  public String getId()
  {
    return this.id;
  }
  
  public DIDLObject setId(String id)
  {
    this.id = id;
    return this;
  }
  
  public String getParentID()
  {
    return this.parentID;
  }
  
  public DIDLObject setParentID(String parentID)
  {
    this.parentID = parentID;
    return this;
  }
  
  public String getTitle()
  {
    return this.title;
  }
  
  public DIDLObject setTitle(String title)
  {
    this.title = title;
    return this;
  }
  
  public String getCreator()
  {
    return this.creator;
  }
  
  public DIDLObject setCreator(String creator)
  {
    this.creator = creator;
    return this;
  }
  
  public boolean isRestricted()
  {
    return this.restricted;
  }
  
  public DIDLObject setRestricted(boolean restricted)
  {
    this.restricted = restricted;
    return this;
  }
  
  public WriteStatus getWriteStatus()
  {
    return this.writeStatus;
  }
  
  public DIDLObject setWriteStatus(WriteStatus writeStatus)
  {
    this.writeStatus = writeStatus;
    return this;
  }
  
  public Res getFirstResource()
  {
    return getResources().size() > 0 ? (Res)getResources().get(0) : null;
  }
  
  public List<Res> getResources()
  {
    return this.resources;
  }
  
  public DIDLObject setResources(List<Res> resources)
  {
    this.resources = resources;
    return this;
  }
  
  public DIDLObject addResource(Res resource)
  {
    getResources().add(resource);
    return this;
  }
  
  public Class getClazz()
  {
    return this.clazz;
  }
  
  public DIDLObject setClazz(Class clazz)
  {
    this.clazz = clazz;
    return this;
  }
  
  public List<Property> getProperties()
  {
    return this.properties;
  }
  
  public DIDLObject setProperties(List<Property> properties)
  {
    this.properties = properties;
    return this;
  }
  
  public DIDLObject addProperty(Property property)
  {
    if (property == null) {
      return this;
    }
    getProperties().add(property);
    return this;
  }
  
  public DIDLObject replaceFirstProperty(Property property)
  {
    if (property == null) {
      return this;
    }
    Iterator<Property> it = getProperties().iterator();
    while (it.hasNext())
    {
      Property p = (Property)it.next();
      if (p.getClass().isAssignableFrom(property.getClass())) {
        it.remove();
      }
    }
    addProperty(property);
    return this;
  }
  
  public DIDLObject replaceProperties(Class<? extends Property> propertyClass, Property[] properties)
  {
    if (properties.length == 0) {
      return this;
    }
    removeProperties(propertyClass);
    return addProperties(properties);
  }
  
  public DIDLObject addProperties(Property[] properties)
  {
    if (properties == null) {
      return this;
    }
    for (Property property : properties) {
      addProperty(property);
    }
    return this;
  }
  
  public DIDLObject removeProperties(Class<? extends Property> propertyClass)
  {
    Iterator<Property> it = getProperties().iterator();
    while (it.hasNext())
    {
      Property property = (Property)it.next();
      if (propertyClass.isInstance(property)) {
        it.remove();
      }
    }
    return this;
  }
  
  public boolean hasProperty(Class<? extends Property> propertyClass)
  {
    for (Property property : getProperties()) {
      if (propertyClass.isInstance(property)) {
        return true;
      }
    }
    return false;
  }
  
  public <V> Property<V> getFirstProperty(Class<? extends Property<V>> propertyClass)
  {
    for (Property property : getProperties()) {
      if (propertyClass.isInstance(property)) {
        return property;
      }
    }
    return null;
  }
  
  public <V> Property<V> getLastProperty(Class<? extends Property<V>> propertyClass)
  {
    Property found = null;
    for (Property property : getProperties()) {
      if (propertyClass.isInstance(property)) {
        found = property;
      }
    }
    return found;
  }
  
  public <V> Property<V>[] getProperties(Class<? extends Property<V>> propertyClass)
  {
    List<Property<V>> list = new ArrayList();
    for (Property property : getProperties()) {
      if (propertyClass.isInstance(property)) {
        list.add(property);
      }
    }
    return (Property[])list.toArray(new Property[list.size()]);
  }
  
  public <V> Property<V>[] getPropertiesByNamespace(Class<? extends DIDLObject.Property.NAMESPACE> namespace)
  {
    List<Property<V>> list = new ArrayList();
    for (Property property : getProperties()) {
      if (namespace.isInstance(property)) {
        list.add(property);
      }
    }
    return (Property[])list.toArray(new Property[list.size()]);
  }
  
  public <V> V getFirstPropertyValue(Class<? extends Property<V>> propertyClass)
  {
    Property<V> prop = getFirstProperty(propertyClass);
    return prop == null ? null : prop.getValue();
  }
  
  public <V> List<V> getPropertyValues(Class<? extends Property<V>> propertyClass)
  {
    List<V> list = new ArrayList();
    for (Property property : getProperties(propertyClass)) {
      list.add(property.getValue());
    }
    return list;
  }
  
  public List<DescMeta> getDescMetadata()
  {
    return this.descMetadata;
  }
  
  public void setDescMetadata(List<DescMeta> descMetadata)
  {
    this.descMetadata = descMetadata;
  }
  
  public DIDLObject addDescMetadata(DescMeta descMetadata)
  {
    getDescMetadata().add(descMetadata);
    return this;
  }
  
  public boolean equals(Object o)
  {
    if (this == o) {
      return true;
    }
    if ((o == null) || (getClass() != o.getClass())) {
      return false;
    }
    DIDLObject that = (DIDLObject)o;
    if (!this.id.equals(that.id)) {
      return false;
    }
    return true;
  }
  
  public int hashCode()
  {
    return this.id.hashCode();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\model\DIDLObject.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */