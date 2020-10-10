package com.sun.tools.jxc;

import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.declaration.TypeDeclaration;
import com.sun.tools.jxc.gen.config.Classes;
import com.sun.tools.jxc.gen.config.Config;
import com.sun.tools.jxc.gen.config.Schema;
import com.sun.tools.xjc.SchemaCache;
import com.sun.tools.xjc.api.Reference;
import com.sun.tools.xjc.util.ForkContentHandler;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;
import javax.xml.validation.ValidatorHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

public final class ConfigReader
{
  private final Set<Reference> classesToBeIncluded = new HashSet();
  private final SchemaOutputResolver schemaOutputResolver;
  private final AnnotationProcessorEnvironment env;
  
  public ConfigReader(AnnotationProcessorEnvironment env, Collection<? extends TypeDeclaration> classes, File xmlFile, ErrorHandler errorHandler)
    throws SAXException, IOException
  {
    this.env = env;
    Config config = parseAndGetConfig(xmlFile, errorHandler);
    checkAllClasses(config, classes);
    String path = xmlFile.getAbsolutePath();
    String xmlPath = path.substring(0, path.lastIndexOf(File.separatorChar));
    this.schemaOutputResolver = createSchemaOutputResolver(config, xmlPath);
  }
  
  public Collection<Reference> getClassesToBeIncluded()
  {
    return this.classesToBeIncluded;
  }
  
  private void checkAllClasses(Config config, Collection<? extends TypeDeclaration> rootClasses)
  {
    List<Pattern> includeRegexList = config.getClasses().getIncludes();
    List<Pattern> excludeRegexList = config.getClasses().getExcludes();
    for (Iterator i$ = rootClasses.iterator(); i$.hasNext();)
    {
      typeDecl = (TypeDeclaration)i$.next();
      
      qualifiedName = typeDecl.getQualifiedName();
      
      Iterator i$ = excludeRegexList.iterator();
      for (;;)
      {
        if (!i$.hasNext()) {
          break label108;
        }
        Pattern pattern = (Pattern)i$.next();
        boolean match = checkPatternMatch(qualifiedName, pattern);
        if (match) {
          break;
        }
      }
      for (Pattern pattern : includeRegexList)
      {
        boolean match = checkPatternMatch(qualifiedName, pattern);
        if (match)
        {
          this.classesToBeIncluded.add(new Reference(typeDecl, this.env));
          break;
        }
      }
    }
    TypeDeclaration typeDecl;
    String qualifiedName;
    label108:
  }
  
  public SchemaOutputResolver getSchemaOutputResolver()
  {
    return this.schemaOutputResolver;
  }
  
  private SchemaOutputResolver createSchemaOutputResolver(Config config, String xmlpath)
  {
    File baseDir = new File(xmlpath, config.getBaseDir().getPath());
    SchemaOutputResolverImpl schemaOutputResolver = new SchemaOutputResolverImpl(baseDir);
    for (Schema schema : config.getSchema())
    {
      String namespace = schema.getNamespace();
      File location = schema.getLocation();
      schemaOutputResolver.addSchemaInfo(namespace, location);
    }
    return schemaOutputResolver;
  }
  
  private boolean checkPatternMatch(String qualifiedName, Pattern pattern)
  {
    Matcher matcher = pattern.matcher(qualifiedName);
    return matcher.matches();
  }
  
  private static SchemaCache configSchema = new SchemaCache(Config.class.getResource("config.xsd"));
  
  private Config parseAndGetConfig(File xmlFile, ErrorHandler errorHandler)
    throws SAXException, IOException
  {
    XMLReader reader;
    try
    {
      SAXParserFactory factory = SAXParserFactory.newInstance();
      factory.setNamespaceAware(true);
      reader = factory.newSAXParser().getXMLReader();
    }
    catch (ParserConfigurationException e)
    {
      throw new Error(e);
    }
    NGCCRuntimeEx runtime = new NGCCRuntimeEx(errorHandler);
    
    ValidatorHandler validator = configSchema.newValidator();
    validator.setErrorHandler(errorHandler);
    
    reader.setContentHandler(new ForkContentHandler(validator, runtime));
    
    reader.setErrorHandler(errorHandler);
    Config config = new Config(runtime);
    runtime.setRootHandler(config);
    reader.parse(new InputSource(xmlFile.toURL().toExternalForm()));
    runtime.reset();
    
    return config;
  }
  
  private static final class SchemaOutputResolverImpl
    extends SchemaOutputResolver
  {
    private final File baseDir;
    private final Map<String, File> schemas = new HashMap();
    
    public Result createOutput(String namespaceUri, String suggestedFileName)
    {
      if (this.schemas.containsKey(namespaceUri))
      {
        File loc = (File)this.schemas.get(namespaceUri);
        if (loc == null) {
          return null;
        }
        loc.getParentFile().mkdirs();
        
        return new StreamResult(loc);
      }
      File schemaFile = new File(this.baseDir, suggestedFileName);
      
      return new StreamResult(schemaFile);
    }
    
    public SchemaOutputResolverImpl(File baseDir)
    {
      assert (baseDir != null);
      this.baseDir = baseDir;
    }
    
    public void addSchemaInfo(String namespaceUri, File location)
    {
      if (namespaceUri == null) {
        namespaceUri = "";
      }
      this.schemas.put(namespaceUri, location);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\jxc\ConfigReader.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */