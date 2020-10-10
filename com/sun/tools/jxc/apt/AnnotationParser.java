package com.sun.tools.jxc.apt;

import com.sun.mirror.apt.AnnotationProcessor;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;
import com.sun.tools.jxc.ConfigReader;
import com.sun.tools.xjc.ErrorReceiver;
import com.sun.tools.xjc.api.J2SJAXBModel;
import com.sun.tools.xjc.api.JavaCompiler;
import com.sun.tools.xjc.api.Reference;
import com.sun.tools.xjc.api.XJC;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;
import javax.xml.bind.SchemaOutputResolver;
import org.xml.sax.SAXException;

final class AnnotationParser
  implements AnnotationProcessor
{
  private final AnnotationProcessorEnvironment env;
  private ErrorReceiver errorListener;
  
  public AnnotationProcessorEnvironment getEnv()
  {
    return this.env;
  }
  
  AnnotationParser(Set<AnnotationTypeDeclaration> atds, AnnotationProcessorEnvironment env)
  {
    this.env = env;
    this.errorListener = new ErrorReceiverImpl(env.getMessager(), env.getOptions().containsKey("-Ajaxb.debug"));
  }
  
  public void process()
  {
    for (Map.Entry<String, String> me : this.env.getOptions().entrySet())
    {
      String key = (String)me.getKey();
      if (key.startsWith("-Ajaxb.config="))
      {
        String value = key.substring("-Ajaxb.config".length() + 1);
        
        StringTokenizer st = new StringTokenizer(value, File.pathSeparator);
        if (!st.hasMoreTokens()) {
          this.errorListener.error(null, Messages.OPERAND_MISSING.format(new Object[] { "-Ajaxb.config" }));
        } else {
          while (st.hasMoreTokens())
          {
            File configFile = new File(st.nextToken());
            if (!configFile.exists()) {
              this.errorListener.error(null, Messages.NON_EXISTENT_FILE.format(new Object[0]));
            } else {
              try
              {
                ConfigReader configReader = new ConfigReader(this.env, this.env.getTypeDeclarations(), configFile, this.errorListener);
                
                Collection<Reference> classesToBeIncluded = configReader.getClassesToBeIncluded();
                J2SJAXBModel model = XJC.createJavaCompiler().bind(classesToBeIncluded, Collections.emptyMap(), null, this.env);
                
                SchemaOutputResolver schemaOutputResolver = configReader.getSchemaOutputResolver();
                
                model.generateSchema(schemaOutputResolver, this.errorListener);
              }
              catch (IOException e)
              {
                this.errorListener.error(e.getMessage(), e);
              }
              catch (SAXException e) {}
            }
          }
        }
      }
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\jxc\apt\AnnotationParser.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */