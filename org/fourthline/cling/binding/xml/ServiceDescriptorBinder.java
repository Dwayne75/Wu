package org.fourthline.cling.binding.xml;

import org.fourthline.cling.model.ValidationException;
import org.fourthline.cling.model.meta.Service;
import org.w3c.dom.Document;

public abstract interface ServiceDescriptorBinder
{
  public abstract <T extends Service> T describe(T paramT, String paramString)
    throws DescriptorBindingException, ValidationException;
  
  public abstract <T extends Service> T describe(T paramT, Document paramDocument)
    throws DescriptorBindingException, ValidationException;
  
  public abstract String generate(Service paramService)
    throws DescriptorBindingException;
  
  public abstract Document buildDOM(Service paramService)
    throws DescriptorBindingException;
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\binding\xml\ServiceDescriptorBinder.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */