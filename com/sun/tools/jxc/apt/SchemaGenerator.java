package com.sun.tools.jxc.apt;

import com.sun.mirror.apt.AnnotationProcessor;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.apt.AnnotationProcessorFactory;
import com.sun.mirror.apt.Filer;
import com.sun.mirror.apt.Filer.Location;
import com.sun.mirror.apt.Messager;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;
import com.sun.mirror.declaration.ClassDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;
import com.sun.tools.xjc.api.J2SJAXBModel;
import com.sun.tools.xjc.api.JavaCompiler;
import com.sun.tools.xjc.api.Reference;
import com.sun.tools.xjc.api.XJC;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;

public class SchemaGenerator
  implements AnnotationProcessorFactory
{
  private final Map<String, File> schemaLocations = new HashMap();
  private File episodeFile;
  
  public SchemaGenerator() {}
  
  public SchemaGenerator(Map<String, File> m)
  {
    this.schemaLocations.putAll(m);
  }
  
  public void setEpisodeFile(File episodeFile)
  {
    this.episodeFile = episodeFile;
  }
  
  public Collection<String> supportedOptions()
  {
    return Collections.emptyList();
  }
  
  public Collection<String> supportedAnnotationTypes()
  {
    return Arrays.asList(new String[] { "*" });
  }
  
  public AnnotationProcessor getProcessorFor(Set<AnnotationTypeDeclaration> atds, final AnnotationProcessorEnvironment env)
  {
    new AnnotationProcessor()
    {
      final ErrorReceiverImpl errorListener = new ErrorReceiverImpl(env);
      
      public void process()
      {
        List<Reference> decls = new ArrayList();
        for (TypeDeclaration d : env.getTypeDeclarations()) {
          if ((d instanceof ClassDeclaration)) {
            decls.add(new Reference(d, env));
          }
        }
        J2SJAXBModel model = XJC.createJavaCompiler().bind(decls, Collections.emptyMap(), null, env);
        if (model == null) {
          return;
        }
        try
        {
          model.generateSchema(new SchemaOutputResolver()
          {
            public Result createOutput(String namespaceUri, String suggestedFileName)
              throws IOException
            {
              OutputStream out;
              File file;
              OutputStream out;
              if (SchemaGenerator.this.schemaLocations.containsKey(namespaceUri))
              {
                File file = (File)SchemaGenerator.this.schemaLocations.get(namespaceUri);
                if (file == null) {
                  return null;
                }
                out = new FileOutputStream(file);
              }
              else
              {
                file = new File(suggestedFileName);
                out = SchemaGenerator.1.this.val$env.getFiler().createBinaryFile(Filer.Location.CLASS_TREE, "", file);
                file = file.getAbsoluteFile();
              }
              StreamResult ss = new StreamResult(out);
              SchemaGenerator.1.this.val$env.getMessager().printNotice("Writing " + file);
              ss.setSystemId(file.toURL().toExternalForm());
              return ss;
            }
          }, this.errorListener);
          if (SchemaGenerator.this.episodeFile != null)
          {
            env.getMessager().printNotice("Writing " + SchemaGenerator.this.episodeFile);
            model.generateEpisodeFile(new StreamResult(SchemaGenerator.this.episodeFile));
          }
        }
        catch (IOException e)
        {
          this.errorListener.error(e.getMessage(), e);
        }
      }
    };
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\jxc\apt\SchemaGenerator.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */