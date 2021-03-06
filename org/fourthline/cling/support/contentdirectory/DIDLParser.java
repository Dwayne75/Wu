package org.fourthline.cling.support.contentdirectory;

import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.fourthline.cling.model.XMLUtil;
import org.fourthline.cling.model.types.Datatype;
import org.fourthline.cling.model.types.Datatype.Builtin;
import org.fourthline.cling.model.types.InvalidValueException;
import org.fourthline.cling.support.model.DIDLAttribute;
import org.fourthline.cling.support.model.DIDLContent;
import org.fourthline.cling.support.model.DIDLObject;
import org.fourthline.cling.support.model.DIDLObject.Class;
import org.fourthline.cling.support.model.DIDLObject.Property;
import org.fourthline.cling.support.model.DIDLObject.Property.DC.CONTRIBUTOR;
import org.fourthline.cling.support.model.DIDLObject.Property.DC.DATE;
import org.fourthline.cling.support.model.DIDLObject.Property.DC.DESCRIPTION;
import org.fourthline.cling.support.model.DIDLObject.Property.DC.LANGUAGE;
import org.fourthline.cling.support.model.DIDLObject.Property.DC.NAMESPACE;
import org.fourthline.cling.support.model.DIDLObject.Property.DC.PUBLISHER;
import org.fourthline.cling.support.model.DIDLObject.Property.DC.RELATION;
import org.fourthline.cling.support.model.DIDLObject.Property.DC.RIGHTS;
import org.fourthline.cling.support.model.DIDLObject.Property.DLNA.PROFILE_ID;
import org.fourthline.cling.support.model.DIDLObject.Property.NAMESPACE;
import org.fourthline.cling.support.model.DIDLObject.Property.SEC.NAMESPACE;
import org.fourthline.cling.support.model.DIDLObject.Property.UPNP.ACTOR;
import org.fourthline.cling.support.model.DIDLObject.Property.UPNP.ALBUM;
import org.fourthline.cling.support.model.DIDLObject.Property.UPNP.ALBUM_ART_URI;
import org.fourthline.cling.support.model.DIDLObject.Property.UPNP.ARTIST;
import org.fourthline.cling.support.model.DIDLObject.Property.UPNP.ARTIST_DISCO_URI;
import org.fourthline.cling.support.model.DIDLObject.Property.UPNP.AUTHOR;
import org.fourthline.cling.support.model.DIDLObject.Property.UPNP.CHANNEL_NAME;
import org.fourthline.cling.support.model.DIDLObject.Property.UPNP.CHANNEL_NR;
import org.fourthline.cling.support.model.DIDLObject.Property.UPNP.DIRECTOR;
import org.fourthline.cling.support.model.DIDLObject.Property.UPNP.DVD_REGION_CODE;
import org.fourthline.cling.support.model.DIDLObject.Property.UPNP.GENRE;
import org.fourthline.cling.support.model.DIDLObject.Property.UPNP.ICON;
import org.fourthline.cling.support.model.DIDLObject.Property.UPNP.LONG_DESCRIPTION;
import org.fourthline.cling.support.model.DIDLObject.Property.UPNP.LYRICS_URI;
import org.fourthline.cling.support.model.DIDLObject.Property.UPNP.NAMESPACE;
import org.fourthline.cling.support.model.DIDLObject.Property.UPNP.ORIGINAL_TRACK_NUMBER;
import org.fourthline.cling.support.model.DIDLObject.Property.UPNP.PLAYLIST;
import org.fourthline.cling.support.model.DIDLObject.Property.UPNP.PRODUCER;
import org.fourthline.cling.support.model.DIDLObject.Property.UPNP.RADIO_BAND;
import org.fourthline.cling.support.model.DIDLObject.Property.UPNP.RADIO_CALL_SIGN;
import org.fourthline.cling.support.model.DIDLObject.Property.UPNP.RADIO_STATION_ID;
import org.fourthline.cling.support.model.DIDLObject.Property.UPNP.RATING;
import org.fourthline.cling.support.model.DIDLObject.Property.UPNP.REGION;
import org.fourthline.cling.support.model.DIDLObject.Property.UPNP.SCHEDULED_END_TIME;
import org.fourthline.cling.support.model.DIDLObject.Property.UPNP.SCHEDULED_START_TIME;
import org.fourthline.cling.support.model.DIDLObject.Property.UPNP.STORAGE_FREE;
import org.fourthline.cling.support.model.DIDLObject.Property.UPNP.STORAGE_MAX_PARTITION;
import org.fourthline.cling.support.model.DIDLObject.Property.UPNP.STORAGE_MEDIUM;
import org.fourthline.cling.support.model.DIDLObject.Property.UPNP.STORAGE_TOTAL;
import org.fourthline.cling.support.model.DIDLObject.Property.UPNP.STORAGE_USED;
import org.fourthline.cling.support.model.DIDLObject.Property.UPNP.TOC;
import org.fourthline.cling.support.model.DIDLObject.Property.UPNP.USER_ANNOTATION;
import org.fourthline.cling.support.model.DescMeta;
import org.fourthline.cling.support.model.Person;
import org.fourthline.cling.support.model.PersonWithRole;
import org.fourthline.cling.support.model.ProtocolInfo;
import org.fourthline.cling.support.model.Res;
import org.fourthline.cling.support.model.StorageMedium;
import org.fourthline.cling.support.model.WriteStatus;
import org.fourthline.cling.support.model.container.Container;
import org.fourthline.cling.support.model.item.Item;
import org.seamless.util.Exceptions;
import org.seamless.util.io.IO;
import org.seamless.xml.SAXParser;
import org.seamless.xml.SAXParser.Handler;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class DIDLParser
  extends SAXParser
{
  private static final Logger log = Logger.getLogger(DIDLParser.class.getName());
  public static final String UNKNOWN_TITLE = "Unknown Title";
  
  public DIDLContent parseResource(String resource)
    throws Exception
  {
    InputStream is = null;
    try
    {
      is = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource);
      return parse(IO.readLines(is));
    }
    finally
    {
      if (is != null) {
        is.close();
      }
    }
  }
  
  public DIDLContent parse(String xml)
    throws Exception
  {
    if ((xml == null) || (xml.length() == 0)) {
      throw new RuntimeException("Null or empty XML");
    }
    DIDLContent content = new DIDLContent();
    createRootHandler(content, this);
    
    log.fine("Parsing DIDL XML content");
    parse(new InputSource(new StringReader(xml)));
    return content;
  }
  
  protected RootHandler createRootHandler(DIDLContent instance, SAXParser parser)
  {
    return new RootHandler(instance, parser);
  }
  
  protected ContainerHandler createContainerHandler(Container instance, SAXParser.Handler parent)
  {
    return new ContainerHandler(instance, parent);
  }
  
  protected ItemHandler createItemHandler(Item instance, SAXParser.Handler parent)
  {
    return new ItemHandler(instance, parent);
  }
  
  protected ResHandler createResHandler(Res instance, SAXParser.Handler parent)
  {
    return new ResHandler(instance, parent);
  }
  
  protected DescMetaHandler createDescMetaHandler(DescMeta instance, SAXParser.Handler parent)
  {
    return new DescMetaHandler(instance, parent);
  }
  
  protected Container createContainer(Attributes attributes)
  {
    Container container = new Container();
    
    container.setId(attributes.getValue("id"));
    container.setParentID(attributes.getValue("parentID"));
    if (attributes.getValue("childCount") != null) {
      container.setChildCount(Integer.valueOf(attributes.getValue("childCount")));
    }
    try
    {
      Boolean value = (Boolean)Datatype.Builtin.BOOLEAN.getDatatype().valueOf(attributes
        .getValue("restricted"));
      if (value != null) {
        container.setRestricted(value.booleanValue());
      }
      value = (Boolean)Datatype.Builtin.BOOLEAN.getDatatype().valueOf(attributes
        .getValue("searchable"));
      if (value != null) {
        container.setSearchable(value.booleanValue());
      }
    }
    catch (Exception localException) {}
    return container;
  }
  
  protected Item createItem(Attributes attributes)
  {
    Item item = new Item();
    
    item.setId(attributes.getValue("id"));
    item.setParentID(attributes.getValue("parentID"));
    try
    {
      Boolean value = (Boolean)Datatype.Builtin.BOOLEAN.getDatatype().valueOf(attributes
        .getValue("restricted"));
      if (value != null) {
        item.setRestricted(value.booleanValue());
      }
    }
    catch (Exception localException) {}
    if (attributes.getValue("refID") != null) {
      item.setRefID(attributes.getValue("refID"));
    }
    return item;
  }
  
  protected Res createResource(Attributes attributes)
  {
    Res res = new Res();
    if (attributes.getValue("importUri") != null) {
      res.setImportUri(URI.create(attributes.getValue("importUri")));
    }
    try
    {
      res.setProtocolInfo(new ProtocolInfo(attributes
        .getValue("protocolInfo")));
    }
    catch (InvalidValueException ex)
    {
      log.warning("In DIDL content, invalid resource protocol info: " + Exceptions.unwrap(ex));
      return null;
    }
    if (attributes.getValue("size") != null) {
      res.setSize(toLongOrNull(attributes.getValue("size")));
    }
    if (attributes.getValue("duration") != null) {
      res.setDuration(attributes.getValue("duration"));
    }
    if (attributes.getValue("bitrate") != null) {
      res.setBitrate(toLongOrNull(attributes.getValue("bitrate")));
    }
    if (attributes.getValue("sampleFrequency") != null) {
      res.setSampleFrequency(toLongOrNull(attributes.getValue("sampleFrequency")));
    }
    if (attributes.getValue("bitsPerSample") != null) {
      res.setBitsPerSample(toLongOrNull(attributes.getValue("bitsPerSample")));
    }
    if (attributes.getValue("nrAudioChannels") != null) {
      res.setNrAudioChannels(toLongOrNull(attributes.getValue("nrAudioChannels")));
    }
    if (attributes.getValue("colorDepth") != null) {
      res.setColorDepth(toLongOrNull(attributes.getValue("colorDepth")));
    }
    if (attributes.getValue("protection") != null) {
      res.setProtection(attributes.getValue("protection"));
    }
    if (attributes.getValue("resolution") != null) {
      res.setResolution(attributes.getValue("resolution"));
    }
    return res;
  }
  
  private Long toLongOrNull(String value)
  {
    try
    {
      return Long.valueOf(value);
    }
    catch (NumberFormatException x) {}
    return null;
  }
  
  protected DescMeta createDescMeta(Attributes attributes)
  {
    DescMeta desc = new DescMeta();
    
    desc.setId(attributes.getValue("id"));
    if (attributes.getValue("type") != null) {
      desc.setType(attributes.getValue("type"));
    }
    if (attributes.getValue("nameSpace") != null) {
      desc.setNameSpace(URI.create(attributes.getValue("nameSpace")));
    }
    return desc;
  }
  
  public String generate(DIDLContent content)
    throws Exception
  {
    return generate(content, false);
  }
  
  public String generate(DIDLContent content, boolean nestedItems)
    throws Exception
  {
    return documentToString(buildDOM(content, nestedItems), true);
  }
  
  protected String documentToString(Document document, boolean omitProlog)
    throws Exception
  {
    TransformerFactory transFactory = TransformerFactory.newInstance();
    
    Transformer transformer = transFactory.newTransformer();
    if (omitProlog) {
      transformer.setOutputProperty("omit-xml-declaration", "yes");
    }
    StringWriter out = new StringWriter();
    transformer.transform(new DOMSource(document), new StreamResult(out));
    return out.toString();
  }
  
  protected Document buildDOM(DIDLContent content, boolean nestedItems)
    throws Exception
  {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(true);
    
    Document d = factory.newDocumentBuilder().newDocument();
    
    generateRoot(content, d, nestedItems);
    
    return d;
  }
  
  protected void generateRoot(DIDLContent content, Document descriptor, boolean nestedItems)
  {
    Element rootElement = descriptor.createElementNS("urn:schemas-upnp-org:metadata-1-0/DIDL-Lite/", "DIDL-Lite");
    descriptor.appendChild(rootElement);
    
    rootElement.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:upnp", "urn:schemas-upnp-org:metadata-1-0/upnp/");
    rootElement.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:dc", "http://purl.org/dc/elements/1.1/");
    rootElement.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:sec", "http://www.sec.co.kr/");
    for (Container container : content.getContainers()) {
      if (container != null) {
        generateContainer(container, descriptor, rootElement, nestedItems);
      }
    }
    for (Item item : content.getItems()) {
      if (item != null) {
        generateItem(item, descriptor, rootElement);
      }
    }
    for (DescMeta descMeta : content.getDescMetadata()) {
      if (descMeta != null) {
        generateDescMetadata(descMeta, descriptor, rootElement);
      }
    }
  }
  
  protected void generateContainer(Container container, Document descriptor, Element parent, boolean nestedItems)
  {
    if (container.getClazz() == null) {
      throw new RuntimeException("Missing 'upnp:class' element for container: " + container.getId());
    }
    Element containerElement = XMLUtil.appendNewElement(descriptor, parent, "container");
    if (container.getId() == null) {
      throw new NullPointerException("Missing id on container: " + container);
    }
    containerElement.setAttribute("id", container.getId());
    if (container.getParentID() == null) {
      throw new NullPointerException("Missing parent id on container: " + container);
    }
    containerElement.setAttribute("parentID", container.getParentID());
    if (container.getChildCount() != null) {
      containerElement.setAttribute("childCount", Integer.toString(container.getChildCount().intValue()));
    }
    containerElement.setAttribute("restricted", booleanToInt(container.isRestricted()));
    containerElement.setAttribute("searchable", booleanToInt(container.isSearchable()));
    
    String title = container.getTitle();
    if (title == null)
    {
      log.warning("Missing 'dc:title' element for container: " + container.getId());
      title = "Unknown Title";
    }
    XMLUtil.appendNewElementIfNotNull(descriptor, containerElement, "dc:title", title, "http://purl.org/dc/elements/1.1/");
    
    XMLUtil.appendNewElementIfNotNull(descriptor, containerElement, "dc:creator", container
    
      .getCreator(), "http://purl.org/dc/elements/1.1/");
    
    XMLUtil.appendNewElementIfNotNull(descriptor, containerElement, "upnp:writeStatus", container
    
      .getWriteStatus(), "urn:schemas-upnp-org:metadata-1-0/upnp/");
    
    appendClass(descriptor, containerElement, container.getClazz(), "upnp:class", false);
    for (DIDLObject.Class searchClass : container.getSearchClasses()) {
      appendClass(descriptor, containerElement, searchClass, "upnp:searchClass", true);
    }
    for (DIDLObject.Class createClass : container.getCreateClasses()) {
      appendClass(descriptor, containerElement, createClass, "upnp:createClass", true);
    }
    appendProperties(descriptor, containerElement, container, "upnp", DIDLObject.Property.UPNP.NAMESPACE.class, "urn:schemas-upnp-org:metadata-1-0/upnp/");
    appendProperties(descriptor, containerElement, container, "dc", DIDLObject.Property.DC.NAMESPACE.class, "http://purl.org/dc/elements/1.1/");
    if (nestedItems) {
      for (Item item : container.getItems()) {
        if (item != null) {
          generateItem(item, descriptor, containerElement);
        }
      }
    }
    for (Res resource : container.getResources()) {
      if (resource != null) {
        generateResource(resource, descriptor, containerElement);
      }
    }
    for (DescMeta descMeta : container.getDescMetadata()) {
      if (descMeta != null) {
        generateDescMetadata(descMeta, descriptor, containerElement);
      }
    }
  }
  
  protected void generateItem(Item item, Document descriptor, Element parent)
  {
    if (item.getClazz() == null) {
      throw new RuntimeException("Missing 'upnp:class' element for item: " + item.getId());
    }
    Element itemElement = XMLUtil.appendNewElement(descriptor, parent, "item");
    if (item.getId() == null) {
      throw new NullPointerException("Missing id on item: " + item);
    }
    itemElement.setAttribute("id", item.getId());
    if (item.getParentID() == null) {
      throw new NullPointerException("Missing parent id on item: " + item);
    }
    itemElement.setAttribute("parentID", item.getParentID());
    if (item.getRefID() != null) {
      itemElement.setAttribute("refID", item.getRefID());
    }
    itemElement.setAttribute("restricted", booleanToInt(item.isRestricted()));
    
    String title = item.getTitle();
    if (title == null)
    {
      log.warning("Missing 'dc:title' element for item: " + item.getId());
      title = "Unknown Title";
    }
    XMLUtil.appendNewElementIfNotNull(descriptor, itemElement, "dc:title", title, "http://purl.org/dc/elements/1.1/");
    
    XMLUtil.appendNewElementIfNotNull(descriptor, itemElement, "dc:creator", item
    
      .getCreator(), "http://purl.org/dc/elements/1.1/");
    
    XMLUtil.appendNewElementIfNotNull(descriptor, itemElement, "upnp:writeStatus", item
    
      .getWriteStatus(), "urn:schemas-upnp-org:metadata-1-0/upnp/");
    
    appendClass(descriptor, itemElement, item.getClazz(), "upnp:class", false);
    
    appendProperties(descriptor, itemElement, item, "upnp", DIDLObject.Property.UPNP.NAMESPACE.class, "urn:schemas-upnp-org:metadata-1-0/upnp/");
    appendProperties(descriptor, itemElement, item, "dc", DIDLObject.Property.DC.NAMESPACE.class, "http://purl.org/dc/elements/1.1/");
    appendProperties(descriptor, itemElement, item, "sec", DIDLObject.Property.SEC.NAMESPACE.class, "http://www.sec.co.kr/");
    for (Res resource : item.getResources()) {
      if (resource != null) {
        generateResource(resource, descriptor, itemElement);
      }
    }
    for (DescMeta descMeta : item.getDescMetadata()) {
      if (descMeta != null) {
        generateDescMetadata(descMeta, descriptor, itemElement);
      }
    }
  }
  
  protected void generateResource(Res resource, Document descriptor, Element parent)
  {
    if (resource.getValue() == null) {
      throw new RuntimeException("Missing resource URI value" + resource);
    }
    if (resource.getProtocolInfo() == null) {
      throw new RuntimeException("Missing resource protocol info: " + resource);
    }
    Element resourceElement = XMLUtil.appendNewElement(descriptor, parent, "res", resource.getValue());
    resourceElement.setAttribute("protocolInfo", resource.getProtocolInfo().toString());
    if (resource.getImportUri() != null) {
      resourceElement.setAttribute("importUri", resource.getImportUri().toString());
    }
    if (resource.getSize() != null) {
      resourceElement.setAttribute("size", resource.getSize().toString());
    }
    if (resource.getDuration() != null) {
      resourceElement.setAttribute("duration", resource.getDuration());
    }
    if (resource.getBitrate() != null) {
      resourceElement.setAttribute("bitrate", resource.getBitrate().toString());
    }
    if (resource.getSampleFrequency() != null) {
      resourceElement.setAttribute("sampleFrequency", resource.getSampleFrequency().toString());
    }
    if (resource.getBitsPerSample() != null) {
      resourceElement.setAttribute("bitsPerSample", resource.getBitsPerSample().toString());
    }
    if (resource.getNrAudioChannels() != null) {
      resourceElement.setAttribute("nrAudioChannels", resource.getNrAudioChannels().toString());
    }
    if (resource.getColorDepth() != null) {
      resourceElement.setAttribute("colorDepth", resource.getColorDepth().toString());
    }
    if (resource.getProtection() != null) {
      resourceElement.setAttribute("protection", resource.getProtection());
    }
    if (resource.getResolution() != null) {
      resourceElement.setAttribute("resolution", resource.getResolution());
    }
  }
  
  protected void generateDescMetadata(DescMeta descMeta, Document descriptor, Element parent)
  {
    if (descMeta.getId() == null) {
      throw new RuntimeException("Missing id of description metadata: " + descMeta);
    }
    if (descMeta.getNameSpace() == null) {
      throw new RuntimeException("Missing namespace of description metadata: " + descMeta);
    }
    Element descElement = XMLUtil.appendNewElement(descriptor, parent, "desc");
    descElement.setAttribute("id", descMeta.getId());
    descElement.setAttribute("nameSpace", descMeta.getNameSpace().toString());
    if (descMeta.getType() != null) {
      descElement.setAttribute("type", descMeta.getType());
    }
    populateDescMetadata(descElement, descMeta);
  }
  
  protected void populateDescMetadata(Element descElement, DescMeta descMeta)
  {
    if ((descMeta.getMetadata() instanceof Document))
    {
      Document doc = (Document)descMeta.getMetadata();
      
      NodeList nl = doc.getDocumentElement().getChildNodes();
      for (int i = 0; i < nl.getLength(); i++)
      {
        Node n = nl.item(i);
        if (n.getNodeType() == 1)
        {
          Node clone = descElement.getOwnerDocument().importNode(n, true);
          descElement.appendChild(clone);
        }
      }
    }
    else
    {
      log.warning("Unknown desc metadata content, please override populateDescMetadata(): " + descMeta.getMetadata());
    }
  }
  
  protected void appendProperties(Document descriptor, Element parent, DIDLObject object, String prefix, Class<? extends DIDLObject.Property.NAMESPACE> namespace, String namespaceURI)
  {
    for (DIDLObject.Property<Object> property : object.getPropertiesByNamespace(namespace))
    {
      Element el = descriptor.createElementNS(namespaceURI, prefix + ":" + property.getDescriptorName());
      parent.appendChild(el);
      property.setOnElement(el);
    }
  }
  
  protected void appendClass(Document descriptor, Element parent, DIDLObject.Class clazz, String element, boolean appendDerivation)
  {
    Element classElement = XMLUtil.appendNewElementIfNotNull(descriptor, parent, element, clazz
    
      .getValue(), "urn:schemas-upnp-org:metadata-1-0/upnp/");
    if ((clazz.getFriendlyName() != null) && (clazz.getFriendlyName().length() > 0)) {
      classElement.setAttribute("name", clazz.getFriendlyName());
    }
    if (appendDerivation) {
      classElement.setAttribute("includeDerived", Boolean.toString(clazz.isIncludeDerived()));
    }
  }
  
  protected String booleanToInt(boolean b)
  {
    return b ? "1" : "0";
  }
  
  public void debugXML(String s)
  {
    if (log.isLoggable(Level.FINE))
    {
      log.fine("-------------------------------------------------------------------------------------");
      log.fine("\n" + s);
      log.fine("-------------------------------------------------------------------------------------");
    }
  }
  
  public abstract class DIDLObjectHandler<I extends DIDLObject>
    extends SAXParser.Handler<I>
  {
    protected DIDLObjectHandler(SAXParser.Handler instance)
    {
      super(parent);
    }
    
    public void endElement(String uri, String localName, String qName)
      throws SAXException
    {
      super.endElement(uri, localName, qName);
      if ("http://purl.org/dc/elements/1.1/".equals(uri))
      {
        if ("title".equals(localName)) {
          ((DIDLObject)getInstance()).setTitle(getCharacters());
        } else if ("creator".equals(localName)) {
          ((DIDLObject)getInstance()).setCreator(getCharacters());
        } else if ("description".equals(localName)) {
          ((DIDLObject)getInstance()).addProperty(new DIDLObject.Property.DC.DESCRIPTION(getCharacters()));
        } else if ("publisher".equals(localName)) {
          ((DIDLObject)getInstance()).addProperty(new DIDLObject.Property.DC.PUBLISHER(new Person(getCharacters())));
        } else if ("contributor".equals(localName)) {
          ((DIDLObject)getInstance()).addProperty(new DIDLObject.Property.DC.CONTRIBUTOR(new Person(getCharacters())));
        } else if ("date".equals(localName)) {
          ((DIDLObject)getInstance()).addProperty(new DIDLObject.Property.DC.DATE(getCharacters()));
        } else if ("language".equals(localName)) {
          ((DIDLObject)getInstance()).addProperty(new DIDLObject.Property.DC.LANGUAGE(getCharacters()));
        } else if ("rights".equals(localName)) {
          ((DIDLObject)getInstance()).addProperty(new DIDLObject.Property.DC.RIGHTS(getCharacters()));
        } else if ("relation".equals(localName)) {
          ((DIDLObject)getInstance()).addProperty(new DIDLObject.Property.DC.RELATION(URI.create(getCharacters())));
        }
      }
      else if ("urn:schemas-upnp-org:metadata-1-0/upnp/".equals(uri)) {
        if ("writeStatus".equals(localName))
        {
          try
          {
            ((DIDLObject)getInstance()).setWriteStatus(
              WriteStatus.valueOf(getCharacters()));
          }
          catch (Exception ex)
          {
            DIDLParser.log.info("Ignoring invalid writeStatus value: " + getCharacters());
          }
        }
        else if ("class".equals(localName))
        {
          ((DIDLObject)getInstance()).setClazz(new DIDLObject.Class(
          
            getCharacters(), 
            getAttributes().getValue("name")));
        }
        else if ("artist".equals(localName))
        {
          ((DIDLObject)getInstance()).addProperty(new DIDLObject.Property.UPNP.ARTIST(new PersonWithRole(
          
            getCharacters(), getAttributes().getValue("role"))));
        }
        else if ("actor".equals(localName))
        {
          ((DIDLObject)getInstance()).addProperty(new DIDLObject.Property.UPNP.ACTOR(new PersonWithRole(
          
            getCharacters(), getAttributes().getValue("role"))));
        }
        else if ("author".equals(localName))
        {
          ((DIDLObject)getInstance()).addProperty(new DIDLObject.Property.UPNP.AUTHOR(new PersonWithRole(
          
            getCharacters(), getAttributes().getValue("role"))));
        }
        else if ("producer".equals(localName))
        {
          ((DIDLObject)getInstance()).addProperty(new DIDLObject.Property.UPNP.PRODUCER(new Person(
            getCharacters())));
        }
        else if ("director".equals(localName))
        {
          ((DIDLObject)getInstance()).addProperty(new DIDLObject.Property.UPNP.DIRECTOR(new Person(
            getCharacters())));
        }
        else if ("longDescription".equals(localName))
        {
          ((DIDLObject)getInstance()).addProperty(new DIDLObject.Property.UPNP.LONG_DESCRIPTION(
            getCharacters()));
        }
        else if ("storageUsed".equals(localName))
        {
          ((DIDLObject)getInstance()).addProperty(new DIDLObject.Property.UPNP.STORAGE_USED(
            Long.valueOf(getCharacters())));
        }
        else if ("storageTotal".equals(localName))
        {
          ((DIDLObject)getInstance()).addProperty(new DIDLObject.Property.UPNP.STORAGE_TOTAL(
            Long.valueOf(getCharacters())));
        }
        else if ("storageFree".equals(localName))
        {
          ((DIDLObject)getInstance()).addProperty(new DIDLObject.Property.UPNP.STORAGE_FREE(
            Long.valueOf(getCharacters())));
        }
        else if ("storageMaxPartition".equals(localName))
        {
          ((DIDLObject)getInstance()).addProperty(new DIDLObject.Property.UPNP.STORAGE_MAX_PARTITION(
            Long.valueOf(getCharacters())));
        }
        else if ("storageMedium".equals(localName))
        {
          ((DIDLObject)getInstance()).addProperty(new DIDLObject.Property.UPNP.STORAGE_MEDIUM(
            StorageMedium.valueOrVendorSpecificOf(getCharacters())));
        }
        else if ("genre".equals(localName))
        {
          ((DIDLObject)getInstance()).addProperty(new DIDLObject.Property.UPNP.GENRE(
            getCharacters()));
        }
        else if ("album".equals(localName))
        {
          ((DIDLObject)getInstance()).addProperty(new DIDLObject.Property.UPNP.ALBUM(
            getCharacters()));
        }
        else if ("playlist".equals(localName))
        {
          ((DIDLObject)getInstance()).addProperty(new DIDLObject.Property.UPNP.PLAYLIST(
            getCharacters()));
        }
        else if ("region".equals(localName))
        {
          ((DIDLObject)getInstance()).addProperty(new DIDLObject.Property.UPNP.REGION(
            getCharacters()));
        }
        else if ("rating".equals(localName))
        {
          ((DIDLObject)getInstance()).addProperty(new DIDLObject.Property.UPNP.RATING(
            getCharacters()));
        }
        else if ("toc".equals(localName))
        {
          ((DIDLObject)getInstance()).addProperty(new DIDLObject.Property.UPNP.TOC(
            getCharacters()));
        }
        else if ("albumArtURI".equals(localName))
        {
          DIDLObject.Property albumArtURI = new DIDLObject.Property.UPNP.ALBUM_ART_URI(URI.create(getCharacters()));
          
          Attributes albumArtURIAttributes = getAttributes();
          for (int i = 0; i < albumArtURIAttributes.getLength(); i++) {
            if ("profileID".equals(albumArtURIAttributes.getLocalName(i))) {
              albumArtURI.addAttribute(new DIDLObject.Property.DLNA.PROFILE_ID(new DIDLAttribute("urn:schemas-dlna-org:metadata-1-0/", "dlna", albumArtURIAttributes
              
                .getValue(i))));
            }
          }
          ((DIDLObject)getInstance()).addProperty(albumArtURI);
        }
        else if ("artistDiscographyURI".equals(localName))
        {
          ((DIDLObject)getInstance()).addProperty(new DIDLObject.Property.UPNP.ARTIST_DISCO_URI(
            URI.create(getCharacters())));
        }
        else if ("lyricsURI".equals(localName))
        {
          ((DIDLObject)getInstance()).addProperty(new DIDLObject.Property.UPNP.LYRICS_URI(
            URI.create(getCharacters())));
        }
        else if ("icon".equals(localName))
        {
          ((DIDLObject)getInstance()).addProperty(new DIDLObject.Property.UPNP.ICON(
            URI.create(getCharacters())));
        }
        else if ("radioCallSign".equals(localName))
        {
          ((DIDLObject)getInstance()).addProperty(new DIDLObject.Property.UPNP.RADIO_CALL_SIGN(
            getCharacters()));
        }
        else if ("radioStationID".equals(localName))
        {
          ((DIDLObject)getInstance()).addProperty(new DIDLObject.Property.UPNP.RADIO_STATION_ID(
            getCharacters()));
        }
        else if ("radioBand".equals(localName))
        {
          ((DIDLObject)getInstance()).addProperty(new DIDLObject.Property.UPNP.RADIO_BAND(
            getCharacters()));
        }
        else if ("channelNr".equals(localName))
        {
          ((DIDLObject)getInstance()).addProperty(new DIDLObject.Property.UPNP.CHANNEL_NR(
            Integer.valueOf(getCharacters())));
        }
        else if ("channelName".equals(localName))
        {
          ((DIDLObject)getInstance()).addProperty(new DIDLObject.Property.UPNP.CHANNEL_NAME(
            getCharacters()));
        }
        else if ("scheduledStartTime".equals(localName))
        {
          ((DIDLObject)getInstance()).addProperty(new DIDLObject.Property.UPNP.SCHEDULED_START_TIME(
            getCharacters()));
        }
        else if ("scheduledEndTime".equals(localName))
        {
          ((DIDLObject)getInstance()).addProperty(new DIDLObject.Property.UPNP.SCHEDULED_END_TIME(
            getCharacters()));
        }
        else if ("DVDRegionCode".equals(localName))
        {
          ((DIDLObject)getInstance()).addProperty(new DIDLObject.Property.UPNP.DVD_REGION_CODE(
            Integer.valueOf(getCharacters())));
        }
        else if ("originalTrackNumber".equals(localName))
        {
          ((DIDLObject)getInstance()).addProperty(new DIDLObject.Property.UPNP.ORIGINAL_TRACK_NUMBER(
            Integer.valueOf(getCharacters())));
        }
        else if ("userAnnotation".equals(localName))
        {
          ((DIDLObject)getInstance()).addProperty(new DIDLObject.Property.UPNP.USER_ANNOTATION(
            getCharacters()));
        }
      }
    }
  }
  
  public class RootHandler
    extends SAXParser.Handler<DIDLContent>
  {
    RootHandler(DIDLContent instance, SAXParser parser)
    {
      super(parser);
    }
    
    public void startElement(String uri, String localName, String qName, Attributes attributes)
      throws SAXException
    {
      super.startElement(uri, localName, qName, attributes);
      if (!"urn:schemas-upnp-org:metadata-1-0/DIDL-Lite/".equals(uri)) {
        return;
      }
      if (localName.equals("container"))
      {
        Container container = DIDLParser.this.createContainer(attributes);
        ((DIDLContent)getInstance()).addContainer(container);
        DIDLParser.this.createContainerHandler(container, this);
      }
      else if (localName.equals("item"))
      {
        Item item = DIDLParser.this.createItem(attributes);
        ((DIDLContent)getInstance()).addItem(item);
        DIDLParser.this.createItemHandler(item, this);
      }
      else if (localName.equals("desc"))
      {
        DescMeta desc = DIDLParser.this.createDescMeta(attributes);
        ((DIDLContent)getInstance()).addDescMetadata(desc);
        DIDLParser.this.createDescMetaHandler(desc, this);
      }
    }
    
    protected boolean isLastElement(String uri, String localName, String qName)
    {
      if (("urn:schemas-upnp-org:metadata-1-0/DIDL-Lite/".equals(uri)) && ("DIDL-Lite".equals(localName)))
      {
        ((DIDLContent)getInstance()).replaceGenericContainerAndItems();
        
        return true;
      }
      return false;
    }
  }
  
  public class ContainerHandler
    extends DIDLParser.DIDLObjectHandler<Container>
  {
    public ContainerHandler(Container instance, SAXParser.Handler parent)
    {
      super(instance, parent);
    }
    
    public void startElement(String uri, String localName, String qName, Attributes attributes)
      throws SAXException
    {
      super.startElement(uri, localName, qName, attributes);
      if (!"urn:schemas-upnp-org:metadata-1-0/DIDL-Lite/".equals(uri)) {
        return;
      }
      if (localName.equals("item"))
      {
        Item item = DIDLParser.this.createItem(attributes);
        ((Container)getInstance()).addItem(item);
        DIDLParser.this.createItemHandler(item, this);
      }
      else if (localName.equals("desc"))
      {
        DescMeta desc = DIDLParser.this.createDescMeta(attributes);
        ((Container)getInstance()).addDescMetadata(desc);
        DIDLParser.this.createDescMetaHandler(desc, this);
      }
      else if (localName.equals("res"))
      {
        Res res = DIDLParser.this.createResource(attributes);
        if (res != null)
        {
          ((Container)getInstance()).addResource(res);
          DIDLParser.this.createResHandler(res, this);
        }
      }
    }
    
    public void endElement(String uri, String localName, String qName)
      throws SAXException
    {
      super.endElement(uri, localName, qName);
      if ("urn:schemas-upnp-org:metadata-1-0/upnp/".equals(uri)) {
        if ("searchClass".equals(localName)) {
          ((Container)getInstance()).getSearchClasses().add(new DIDLObject.Class(
          
            getCharacters(), 
            getAttributes().getValue("name"), "true"
            .equals(getAttributes().getValue("includeDerived"))));
        } else if ("createClass".equals(localName)) {
          ((Container)getInstance()).getCreateClasses().add(new DIDLObject.Class(
          
            getCharacters(), 
            getAttributes().getValue("name"), "true"
            .equals(getAttributes().getValue("includeDerived"))));
        }
      }
    }
    
    protected boolean isLastElement(String uri, String localName, String qName)
    {
      if (("urn:schemas-upnp-org:metadata-1-0/DIDL-Lite/".equals(uri)) && ("container".equals(localName)))
      {
        if (((Container)getInstance()).getTitle() == null) {
          DIDLParser.log.warning("In DIDL content, missing 'dc:title' element for container: " + ((Container)getInstance()).getId());
        }
        if (((Container)getInstance()).getClazz() == null) {
          DIDLParser.log.warning("In DIDL content, missing 'upnp:class' element for container: " + ((Container)getInstance()).getId());
        }
        return true;
      }
      return false;
    }
  }
  
  public class ItemHandler
    extends DIDLParser.DIDLObjectHandler<Item>
  {
    public ItemHandler(Item instance, SAXParser.Handler parent)
    {
      super(instance, parent);
    }
    
    public void startElement(String uri, String localName, String qName, Attributes attributes)
      throws SAXException
    {
      super.startElement(uri, localName, qName, attributes);
      if (!"urn:schemas-upnp-org:metadata-1-0/DIDL-Lite/".equals(uri)) {
        return;
      }
      if (localName.equals("res"))
      {
        Res res = DIDLParser.this.createResource(attributes);
        if (res != null)
        {
          ((Item)getInstance()).addResource(res);
          DIDLParser.this.createResHandler(res, this);
        }
      }
      else if (localName.equals("desc"))
      {
        DescMeta desc = DIDLParser.this.createDescMeta(attributes);
        ((Item)getInstance()).addDescMetadata(desc);
        DIDLParser.this.createDescMetaHandler(desc, this);
      }
    }
    
    protected boolean isLastElement(String uri, String localName, String qName)
    {
      if (("urn:schemas-upnp-org:metadata-1-0/DIDL-Lite/".equals(uri)) && ("item".equals(localName)))
      {
        if (((Item)getInstance()).getTitle() == null) {
          DIDLParser.log.warning("In DIDL content, missing 'dc:title' element for item: " + ((Item)getInstance()).getId());
        }
        if (((Item)getInstance()).getClazz() == null) {
          DIDLParser.log.warning("In DIDL content, missing 'upnp:class' element for item: " + ((Item)getInstance()).getId());
        }
        return true;
      }
      return false;
    }
  }
  
  protected class ResHandler
    extends SAXParser.Handler<Res>
  {
    public ResHandler(Res instance, SAXParser.Handler parent)
    {
      super(parent);
    }
    
    public void endElement(String uri, String localName, String qName)
      throws SAXException
    {
      super.endElement(uri, localName, qName);
      ((Res)getInstance()).setValue(getCharacters());
    }
    
    protected boolean isLastElement(String uri, String localName, String qName)
    {
      return ("urn:schemas-upnp-org:metadata-1-0/DIDL-Lite/".equals(uri)) && ("res".equals(localName));
    }
  }
  
  public class DescMetaHandler
    extends SAXParser.Handler<DescMeta>
  {
    protected Element current;
    
    public DescMetaHandler(DescMeta instance, SAXParser.Handler parent)
    {
      super(parent);
      instance.setMetadata(instance.createMetadataDocument());
      this.current = ((Document)getInstance().getMetadata()).getDocumentElement();
    }
    
    public DescMeta<Document> getInstance()
    {
      return (DescMeta)super.getInstance();
    }
    
    public void startElement(String uri, String localName, String qName, Attributes attributes)
      throws SAXException
    {
      super.startElement(uri, localName, qName, attributes);
      
      Element newEl = ((Document)getInstance().getMetadata()).createElementNS(uri, qName);
      for (int i = 0; i < attributes.getLength(); i++) {
        newEl.setAttributeNS(attributes
          .getURI(i), attributes
          .getQName(i), attributes
          .getValue(i));
      }
      this.current.appendChild(newEl);
      this.current = newEl;
    }
    
    public void endElement(String uri, String localName, String qName)
      throws SAXException
    {
      super.endElement(uri, localName, qName);
      if (isLastElement(uri, localName, qName)) {
        return;
      }
      if ((getCharacters().length() > 0) && (!getCharacters().matches("[\\t\\n\\x0B\\f\\r\\s]+"))) {
        this.current.appendChild(((Document)getInstance().getMetadata()).createTextNode(getCharacters()));
      }
      this.current = ((Element)this.current.getParentNode());
      
      this.characters = new StringBuilder();
      this.attributes = null;
    }
    
    protected boolean isLastElement(String uri, String localName, String qName)
    {
      return ("urn:schemas-upnp-org:metadata-1-0/DIDL-Lite/".equals(uri)) && ("desc".equals(localName));
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\contentdirectory\DIDLParser.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */