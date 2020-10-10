package org.fourthline.cling.support.contentdirectory;

import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;
import org.fourthline.cling.binding.annotations.UpnpAction;
import org.fourthline.cling.binding.annotations.UpnpInputArgument;
import org.fourthline.cling.binding.annotations.UpnpService;
import org.fourthline.cling.binding.annotations.UpnpServiceId;
import org.fourthline.cling.binding.annotations.UpnpServiceType;
import org.fourthline.cling.binding.annotations.UpnpStateVariable;
import org.fourthline.cling.binding.annotations.UpnpStateVariables;
import org.fourthline.cling.model.types.ErrorCode;
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
import org.fourthline.cling.model.types.csv.CSV;
import org.fourthline.cling.model.types.csv.CSVString;
import org.fourthline.cling.support.model.BrowseFlag;
import org.fourthline.cling.support.model.BrowseResult;
import org.fourthline.cling.support.model.DIDLContent;
import org.fourthline.cling.support.model.SortCriterion;

@UpnpService(serviceId=@UpnpServiceId("ContentDirectory"), serviceType=@UpnpServiceType(value="ContentDirectory", version=1))
@UpnpStateVariables({@UpnpStateVariable(name="A_ARG_TYPE_ObjectID", sendEvents=false, datatype="string"), @UpnpStateVariable(name="A_ARG_TYPE_Result", sendEvents=false, datatype="string"), @UpnpStateVariable(name="A_ARG_TYPE_BrowseFlag", sendEvents=false, datatype="string", allowedValuesEnum=BrowseFlag.class), @UpnpStateVariable(name="A_ARG_TYPE_Filter", sendEvents=false, datatype="string"), @UpnpStateVariable(name="A_ARG_TYPE_SortCriteria", sendEvents=false, datatype="string"), @UpnpStateVariable(name="A_ARG_TYPE_Index", sendEvents=false, datatype="ui4"), @UpnpStateVariable(name="A_ARG_TYPE_Count", sendEvents=false, datatype="ui4"), @UpnpStateVariable(name="A_ARG_TYPE_UpdateID", sendEvents=false, datatype="ui4"), @UpnpStateVariable(name="A_ARG_TYPE_URI", sendEvents=false, datatype="uri"), @UpnpStateVariable(name="A_ARG_TYPE_SearchCriteria", sendEvents=false, datatype="string")})
public abstract class AbstractContentDirectoryService
{
  public static final String CAPS_WILDCARD = "*";
  @UpnpStateVariable(sendEvents=false)
  private final CSV<String> searchCapabilities;
  @UpnpStateVariable(sendEvents=false)
  private final CSV<String> sortCapabilities;
  @UpnpStateVariable(sendEvents=true, defaultValue="0", eventMaximumRateMilliseconds=200)
  private UnsignedIntegerFourBytes systemUpdateID = new UnsignedIntegerFourBytes(0L);
  protected final PropertyChangeSupport propertyChangeSupport;
  
  protected AbstractContentDirectoryService()
  {
    this(new ArrayList(), new ArrayList(), null);
  }
  
  protected AbstractContentDirectoryService(List<String> searchCapabilities, List<String> sortCapabilities)
  {
    this(searchCapabilities, sortCapabilities, null);
  }
  
  protected AbstractContentDirectoryService(List<String> searchCapabilities, List<String> sortCapabilities, PropertyChangeSupport propertyChangeSupport)
  {
    this.propertyChangeSupport = (propertyChangeSupport != null ? propertyChangeSupport : new PropertyChangeSupport(this));
    this.searchCapabilities = new CSVString();
    this.searchCapabilities.addAll(searchCapabilities);
    this.sortCapabilities = new CSVString();
    this.sortCapabilities.addAll(sortCapabilities);
  }
  
  @UpnpAction(out={@org.fourthline.cling.binding.annotations.UpnpOutputArgument(name="SearchCaps")})
  public CSV<String> getSearchCapabilities()
  {
    return this.searchCapabilities;
  }
  
  @UpnpAction(out={@org.fourthline.cling.binding.annotations.UpnpOutputArgument(name="SortCaps")})
  public CSV<String> getSortCapabilities()
  {
    return this.sortCapabilities;
  }
  
  @UpnpAction(out={@org.fourthline.cling.binding.annotations.UpnpOutputArgument(name="Id")})
  public synchronized UnsignedIntegerFourBytes getSystemUpdateID()
  {
    return this.systemUpdateID;
  }
  
  public PropertyChangeSupport getPropertyChangeSupport()
  {
    return this.propertyChangeSupport;
  }
  
  protected synchronized void changeSystemUpdateID()
  {
    Long oldUpdateID = getSystemUpdateID().getValue();
    this.systemUpdateID.increment(true);
    getPropertyChangeSupport().firePropertyChange("SystemUpdateID", oldUpdateID, 
    
      getSystemUpdateID().getValue());
  }
  
  @UpnpAction(out={@org.fourthline.cling.binding.annotations.UpnpOutputArgument(name="Result", stateVariable="A_ARG_TYPE_Result", getterName="getResult"), @org.fourthline.cling.binding.annotations.UpnpOutputArgument(name="NumberReturned", stateVariable="A_ARG_TYPE_Count", getterName="getCount"), @org.fourthline.cling.binding.annotations.UpnpOutputArgument(name="TotalMatches", stateVariable="A_ARG_TYPE_Count", getterName="getTotalMatches"), @org.fourthline.cling.binding.annotations.UpnpOutputArgument(name="UpdateID", stateVariable="A_ARG_TYPE_UpdateID", getterName="getContainerUpdateID")})
  public BrowseResult browse(@UpnpInputArgument(name="ObjectID", aliases={"ContainerID"}) String objectId, @UpnpInputArgument(name="BrowseFlag") String browseFlag, @UpnpInputArgument(name="Filter") String filter, @UpnpInputArgument(name="StartingIndex", stateVariable="A_ARG_TYPE_Index") UnsignedIntegerFourBytes firstResult, @UpnpInputArgument(name="RequestedCount", stateVariable="A_ARG_TYPE_Count") UnsignedIntegerFourBytes maxResults, @UpnpInputArgument(name="SortCriteria") String orderBy)
    throws ContentDirectoryException
  {
    try
    {
      orderByCriteria = SortCriterion.valueOf(orderBy);
    }
    catch (Exception ex)
    {
      SortCriterion[] orderByCriteria;
      throw new ContentDirectoryException(ContentDirectoryErrorCode.UNSUPPORTED_SORT_CRITERIA, ex.toString());
    }
    try
    {
      SortCriterion[] orderByCriteria;
      return browse(objectId, 
      
        BrowseFlag.valueOrNullOf(browseFlag), filter, firstResult
        
        .getValue().longValue(), maxResults.getValue().longValue(), orderByCriteria);
    }
    catch (ContentDirectoryException ex)
    {
      throw ex;
    }
    catch (Exception ex)
    {
      throw new ContentDirectoryException(ErrorCode.ACTION_FAILED, ex.toString());
    }
  }
  
  public abstract BrowseResult browse(String paramString1, BrowseFlag paramBrowseFlag, String paramString2, long paramLong1, long paramLong2, SortCriterion[] paramArrayOfSortCriterion)
    throws ContentDirectoryException;
  
  @UpnpAction(out={@org.fourthline.cling.binding.annotations.UpnpOutputArgument(name="Result", stateVariable="A_ARG_TYPE_Result", getterName="getResult"), @org.fourthline.cling.binding.annotations.UpnpOutputArgument(name="NumberReturned", stateVariable="A_ARG_TYPE_Count", getterName="getCount"), @org.fourthline.cling.binding.annotations.UpnpOutputArgument(name="TotalMatches", stateVariable="A_ARG_TYPE_Count", getterName="getTotalMatches"), @org.fourthline.cling.binding.annotations.UpnpOutputArgument(name="UpdateID", stateVariable="A_ARG_TYPE_UpdateID", getterName="getContainerUpdateID")})
  public BrowseResult search(@UpnpInputArgument(name="ContainerID", stateVariable="A_ARG_TYPE_ObjectID") String containerId, @UpnpInputArgument(name="SearchCriteria") String searchCriteria, @UpnpInputArgument(name="Filter") String filter, @UpnpInputArgument(name="StartingIndex", stateVariable="A_ARG_TYPE_Index") UnsignedIntegerFourBytes firstResult, @UpnpInputArgument(name="RequestedCount", stateVariable="A_ARG_TYPE_Count") UnsignedIntegerFourBytes maxResults, @UpnpInputArgument(name="SortCriteria") String orderBy)
    throws ContentDirectoryException
  {
    try
    {
      orderByCriteria = SortCriterion.valueOf(orderBy);
    }
    catch (Exception ex)
    {
      SortCriterion[] orderByCriteria;
      throw new ContentDirectoryException(ContentDirectoryErrorCode.UNSUPPORTED_SORT_CRITERIA, ex.toString());
    }
    try
    {
      SortCriterion[] orderByCriteria;
      return search(containerId, searchCriteria, filter, firstResult
      
        .getValue().longValue(), maxResults.getValue().longValue(), orderByCriteria);
    }
    catch (ContentDirectoryException ex)
    {
      throw ex;
    }
    catch (Exception ex)
    {
      throw new ContentDirectoryException(ErrorCode.ACTION_FAILED, ex.toString());
    }
  }
  
  public BrowseResult search(String containerId, String searchCriteria, String filter, long firstResult, long maxResults, SortCriterion[] orderBy)
    throws ContentDirectoryException
  {
    try
    {
      return new BrowseResult(new DIDLParser().generate(new DIDLContent()), 0L, 0L);
    }
    catch (Exception ex)
    {
      throw new ContentDirectoryException(ErrorCode.ACTION_FAILED, ex.toString());
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\contentdirectory\AbstractContentDirectoryService.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */