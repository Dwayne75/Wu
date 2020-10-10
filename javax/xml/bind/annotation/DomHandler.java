package javax.xml.bind.annotation;

import javax.xml.bind.ValidationEventHandler;
import javax.xml.transform.Result;
import javax.xml.transform.Source;

public abstract interface DomHandler<ElementT, ResultT extends Result>
{
  public abstract ResultT createUnmarshaller(ValidationEventHandler paramValidationEventHandler);
  
  public abstract ElementT getElement(ResultT paramResultT);
  
  public abstract Source marshal(ElementT paramElementT, ValidationEventHandler paramValidationEventHandler);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\javax\xml\bind\annotation\DomHandler.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */