package com.wurmonline.server;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public abstract class XMLSerializer
{
  private static Logger logger = Logger.getLogger(XMLSerializer.class.getName());
  private static final String baseDirectory = "wurmDB";
  private static final String subDirectory = "test";
  private static final String subDirectoryDirectory = "base";
  private String fileName = "xmlTest.xml";
  private static final String dotXML = ".xml";
  private final Object[] emptyObjectArray = new Object[0];
  
  public LinkedList<Field> getSaveFields()
  {
    LinkedList<Field> result = new LinkedList();
    for (Class<?> c = getClass(); c != null; c = c.getSuperclass())
    {
      Field[] fields = c.getDeclaredFields();
      for (Field classField : fields) {
        if (classField.getAnnotation(XMLSerializer.Saved.class) != null) {
          result.add(classField);
        }
      }
    }
    return result;
  }
  
  public Map<String, Field> getSaveFieldsMap()
  {
    ConcurrentHashMap<String, Field> result = new ConcurrentHashMap();
    for (Class<?> c = getClass(); c != null; c = c.getSuperclass())
    {
      Field[] fields = c.getDeclaredFields();
      for (Field classField : fields) {
        if (classField.getAnnotation(XMLSerializer.Saved.class) != null) {
          result.put(classField.getName(), classField);
        }
      }
    }
    return result;
  }
  
  public final boolean saveXML()
  {
    LinkedList<Field> result = getSaveFields();
    boolean saved = saveToDisk(result);
    return saved;
  }
  
  private final boolean saveToDisk(LinkedList<Field> result)
  {
    long start = System.nanoTime();
    try
    {
      Document document = createFieldsXmlDocument(result);
      transformerFactory = TransformerFactory.newInstance();
      Transformer transformer = transformerFactory.newTransformer();
      DOMSource source = new DOMSource(document);
      File dir = getRootDir();
      dir.mkdirs();
      File file = new File(dir + File.separator + this.fileName);
      logger.info("Dumping fields to absolute path: " + file.getAbsolutePath());
      file.createNewFile();
      if (file != null)
      {
        StreamResult sresult = new StreamResult(new FileOutputStream(file));
        transformer.transform(source, sresult);
      }
    }
    catch (Exception ex)
    {
      TransformerFactory transformerFactory;
      long end;
      logger.log(Level.WARNING, ex.getMessage(), ex);
      long end;
      return 0;
    }
    finally
    {
      long end = System.nanoTime();
      logger.info("Dumping fields to XML took " + (float)(end - start) / 1000000.0F + " ms");
    }
    return true;
  }
  
  public final File getRootDir()
  {
    return new File("wurmDB" + File.separator + "test" + File.separator + "base" + File.separator);
  }
  
  public abstract Object createInstanceAndCallLoadXML(File paramFile);
  
  public final void loadXML(File file)
  {
    try
    {
      DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
      Document doc = dBuilder.parse(file);
      
      doc.getDocumentElement().normalize();
      
      System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
      
      NodeList nList = doc.getElementsByTagName("FIELD");
      
      System.out.println("----------------------------");
      Map<String, Field> fieldsMap = getSaveFieldsMap();
      for (int temp = 0; temp < nList.getLength(); temp++)
      {
        Node nNode = nList.item(temp);
        
        System.out.println("\nCurrent Element :" + nNode.getNodeName());
        if (nNode.getNodeType() == 1)
        {
          Element eElement = (Element)nNode;
          String fieldName = eElement.getElementsByTagName("NAME").item(0).getTextContent();
          
          String value = eElement.getElementsByTagName("VALUE").item(0).getTextContent();
          System.out.println("NAME : " + eElement.getElementsByTagName("NAME").item(0).getTextContent());
          
          System.out.println("VALUE : " + eElement.getElementsByTagName("VALUE").item(0).getTextContent());
          
          Field f = (Field)fieldsMap.get(fieldName);
          try
          {
            if (f != null)
            {
              f.setAccessible(true);
              if ((f.getType() == Boolean.class) || (f.getType() == Boolean.TYPE)) {
                f.set(this, Boolean.valueOf(Boolean.parseBoolean(value)));
              }
              if ((f.getType() == Byte.class) || (f.getType() == Byte.TYPE)) {
                f.set(this, Byte.valueOf(Byte.parseByte(value)));
              } else if ((f.getType() == Short.class) || (f.getType() == Short.TYPE)) {
                f.set(this, Short.valueOf(Short.parseShort(value)));
              } else if ((f.getType() == Integer.class) || (f.getType() == Integer.TYPE)) {
                f.set(this, Integer.valueOf(Integer.parseInt(value)));
              } else if ((f.getType() == Float.class) || (f.getType() == Float.TYPE)) {
                f.set(this, Float.valueOf(Float.parseFloat(value)));
              } else if ((f.getType() == Long.class) || (f.getType() == Long.TYPE)) {
                f.set(this, Long.valueOf(Long.parseLong(value)));
              } else if (f.getType() == String.class) {
                f.set(this, value);
              }
            }
            else
            {
              logger.log(Level.INFO, "Field " + fieldName + " is missing from Xml and will not be set");
            }
          }
          catch (Exception ex)
          {
            logger.log(Level.WARNING, fieldName + ":" + ex.getMessage());
          }
        }
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }
  
  public Object[] loadAllXMLData()
  {
    Set loadedObjects = new HashSet();
    File baseDir = new File("wurmDB" + File.separator + "test");
    for (File dir : baseDir.listFiles()) {
      if (dir.isDirectory()) {
        for (File f : dir.listFiles()) {
          if (f.isDirectory()) {
            for (File toLoad : f.listFiles()) {
              if (!toLoad.isDirectory())
              {
                if (toLoad.getName().endsWith(".xml")) {
                  loadedObjects.add(createInstanceAndCallLoadXML(toLoad));
                }
              }
              else {
                logger.log(Level.INFO, "Not loading " + toLoad + " since it is a directory.");
              }
            }
          } else if (f.getName().endsWith(".xml")) {
            loadedObjects.add(createInstanceAndCallLoadXML(f));
          }
        }
      }
    }
    if (!loadedObjects.isEmpty()) {
      return loadedObjects.toArray(new Object[loadedObjects.size()]);
    }
    return this.emptyObjectArray;
  }
  
  Document createFieldsXmlDocument(LinkedList<Field> fields)
    throws ParserConfigurationException
  {
    String root = "fields";
    DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
    Document document = documentBuilder.newDocument();
    
    Element rootElement = document.createElement("fields");
    document.appendChild(rootElement);
    for (Field field : fields)
    {
      field.setAccessible(true);
      Element node = document.createElement("FIELD");
      rootElement.appendChild(node);
      try
      {
        createNode("NAME", field.getName(), document, node);
        
        createNode("VALUE", field.get(this).toString(), document, node);
      }
      catch (IllegalAccessException iae)
      {
        logger.log(Level.WARNING, "Failed to write " + field.getName());
      }
    }
    return document;
  }
  
  public static void createNode(String element, String data, Document document, Element rootElement)
  {
    Element em = document.createElement(element);
    em.appendChild(document.createTextNode(data));
    rootElement.appendChild(em);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\XMLSerializer.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */