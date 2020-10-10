package com.wurmonline.server.questions;

import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.ServerEntry;
import com.wurmonline.server.Servers;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.behaviours.Seat;
import com.wurmonline.server.behaviours.Vehicle;
import com.wurmonline.server.behaviours.Vehicles;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.players.Player;
import java.util.Properties;

public class SetDestinationQuestion
  extends Question
  implements TimeConstants
{
  private static final int WIDTH = 350;
  private static final int HEIGHT = 250;
  private static final boolean RESIZEABLE = true;
  private static final boolean CLOSEABLE = true;
  private static final int[] RGB = { 200, 200, 200 };
  private static final String key = "dest";
  private static final String title = "Plot a course";
  private static final String question = "Plot a course for your boat:";
  private static final int CLEAR = 65536;
  private static final String cyan = "66,200,200";
  private static final String orange = "255,156,66";
  private static final String red = "255,66,66";
  private static final String white = "255,255,255";
  private Vehicle vehicle;
  
  public SetDestinationQuestion(Creature aResponder, Item aTarget)
  {
    super(aResponder, "Plot a course", "Plot a course for your boat:", 130, aTarget.getWurmId());
    if (aTarget.isBoat()) {
      this.vehicle = Vehicles.getVehicle(aTarget);
    }
  }
  
  public void answer(Properties answers)
  {
    if ((!getResponder().isVehicleCommander()) || (getResponder().getVehicle() == -10L))
    {
      getResponder().getCommunicator().sendNormalServerMessage("You must be embarked as the commander of a boat to plot a course. Try dragging the boat inland before embarking again.");
      return;
    }
    String val = answers.getProperty("dest");
    if (val != null)
    {
      int serverId = Integer.parseInt(val);
      if (serverId == 65536)
      {
        if (this.vehicle.hasDestinationSet())
        {
          this.vehicle.clearDestination();
          getResponder().getCommunicator().sendNormalServerMessage("This boat no longer has a course plotted.");
          alertPassengers();
          return;
        }
        getResponder().getCommunicator().sendNormalServerMessage("You decide not to plot a course.");
        return;
      }
      if ((this.vehicle.hasDestinationSet()) && (serverId == this.vehicle.getDestinationServer().getId()))
      {
        getResponder().getCommunicator().sendNormalServerMessage("You decide to keep your course set to " + this.vehicle
          .getDestinationServer().getName() + ".");
        return;
      }
      ServerEntry entry = Servers.getServerWithId(serverId);
      if (entry != null)
      {
        if (Servers.isAvailableDestination(getResponder(), entry))
        {
          this.vehicle.setDestination(entry);
          getResponder().getCommunicator()
            .sendNormalServerMessage("You plot a course to " + entry.getName() + ".");
          this.vehicle.checkPassengerPermissions(getResponder());
          alertPassengers();
          if ((!entry.EPIC) || (Server.getInstance().isPS()))
          {
            this.vehicle.alertPassengersOfKingdom(entry, true);
            if (((entry.PVPSERVER) && (!Servers.localServer.PVPSERVER)) || (entry.isChaosServer())) {
              this.vehicle.alertAllPassengersOfEnemies(entry);
            }
          }
        }
        else
        {
          getResponder().getCommunicator().sendNormalServerMessage("The waters between here and " + entry.getName() + " are too rough to navigate.");
        }
      }
      else {
        getResponder().getCommunicator().sendNormalServerMessage("You decide to not plot a course.");
      }
    }
    else
    {
      getResponder().getCommunicator().sendNormalServerMessage("You decide to not plot a course.");
    }
  }
  
  public void sendQuestion()
  {
    if (this.vehicle == null) {
      return;
    }
    StringBuilder buf = new StringBuilder(getBmlHeader());
    ServerEntry[] servers = Servers.getDestinations(getResponder());
    long cooldown = this.vehicle.getPlotCourseCooldowns();
    boolean isPvPBlocking = this.vehicle.isPvPBlocking();
    if (Servers.localServer.PVPSERVER)
    {
      String restriction = this.vehicle.checkCourseRestrictions();
      if (restriction != "")
      {
        buf.append("label{type='bold'; color='255,156,66'; text='Course Restrictions'};");
        buf.append("text{text='" + restriction + "'};");
        buf.append("text{text=''};");
        buf.append(createAnswerButton2());
        getResponder().getCommunicator().sendBml(350, 250 + servers.length * 20, true, true, buf.toString(), RGB[0], RGB[1], RGB[2], "Plot a course"); return;
      }
    }
    String color;
    String name;
    if (this.vehicle.hasDestinationSet())
    {
      color = (this.vehicle.getDestinationServer().PVPSERVER) || (this.vehicle.getDestinationServer().isChaosServer()) ? "255,66,66" : "66,200,200";
      
      name = (this.vehicle.getDestinationServer().PVPSERVER) || (this.vehicle.getDestinationServer().isChaosServer()) ? this.vehicle.getDestinationServer().getName() + " [PvP]" : this.vehicle.getDestinationServer().getName();
      buf.append("harray{label{type='bold'; color='255,255,255'; text='Current destination: '};");
      buf.append("label{color='" + color + "'; text='" + name + "'}}");
    }
    buf.append("text{text=''};");
    if ((servers.length == 0) || ((servers.length == 1) && (servers[0] == Servers.localServer)))
    {
      buf.append("text{text='There are no available destinations.'};");
    }
    else
    {
      color = servers;name = color.length;
      for (String str1 = 0; str1 < name; str1++)
      {
        ServerEntry lServer = color[str1];
        if (lServer != Servers.localServer) {
          if ((!lServer.LOGINSERVER) || (Server.getInstance().isPS()))
          {
            boolean selected = false;
            if ((this.vehicle.hasDestinationSet()) && (this.vehicle.getDestinationServer() == lServer)) {
              selected = true;
            }
            if ((lServer.PVPSERVER) || (lServer.isChaosServer()))
            {
              if (isPvPBlocking) {
                buf.append("label{color='255,66,66' text='" + lServer.getName() + " [PvP] (PvP travel blocked)'};");
              } else if (cooldown > 0L) {
                buf.append("label{color='255,66,66' text='" + lServer.getName() + " [PvP] (Available in " + 
                  Server.getTimeFor(cooldown) + ")'};");
              } else {
                buf.append(createRadioWithLabel("dest", String.valueOf(lServer.getId()), lServer.getName() + " [PvP]", "255,66,66", selected));
              }
            }
            else {
              buf.append(createRadioWithLabel("dest", String.valueOf(lServer.getId()), lServer.getName(), "66,200,200", selected));
            }
          }
        }
      }
      if (this.vehicle.hasDestinationSet()) {
        buf.append(createRadioWithLabel("dest", String.valueOf(65536), "Clear destination", "255,255,255", false));
      } else {
        buf.append(createRadioWithLabel("dest", String.valueOf(65536), "No destination", "255,255,255", true));
      }
      buf.append("text{text=''};");
      buf.append("text{text='Plotting a course will send you to that server when you sail across any border of " + Servers.localServer
        .getName() + ".'};");
      buf.append("text{text=''};");
      buf.append("text{text='You will appear on the opposite side of the selected server. For example, if you cross the northern border, you will appear on the southern side of the server you have selected.'};");
      if (isPvPBlocking)
      {
        buf.append("text{text=''};");
        buf.append("text{text='You or a passenger has PvP travel blocked. This option can be toggled in the Profile.'};");
      }
    }
    buf.append("text{text=''};");
    buf.append(createAnswerButton2());
    getResponder().getCommunicator().sendBml(350, 250 + servers.length * 20, true, true, buf.toString(), RGB[0], RGB[1], RGB[2], "Plot a course");
  }
  
  private String createRadioWithLabel(String group, String id, String message, String color, boolean selected)
  {
    String toReturn = "harray{radio{group='" + group + "'; id='" + id + "'; selected='" + selected + "'}";
    toReturn = toReturn + "label{color='" + color + "'; text='" + message + "'}}";
    return toReturn;
  }
  
  private void alertPassengers()
  {
    if (this.vehicle.seats != null) {
      for (Seat lSeat : this.vehicle.seats) {
        if ((lSeat.isOccupied()) && (lSeat != this.vehicle.getPilotSeat())) {
          try
          {
            Player passenger = Players.getInstance().getPlayer(lSeat.getOccupant());
            if (!this.vehicle.hasDestinationSet())
            {
              passenger.getCommunicator().sendNormalServerMessage(getResponder().getName() + " has cleared the plotted course.");
            }
            else
            {
              ServerEntry entry = this.vehicle.getDestinationServer();
              String msg = getResponder().getName() + " has plotted a course to " + entry.getName();
              if (!Servers.mayEnterServer(passenger, entry)) {
                msg = msg + ", but you will not be able to travel with " + getResponder().getHimHerItString();
              }
              passenger.getCommunicator().sendAlertServerMessage(msg + ".");
            }
          }
          catch (NoSuchPlayerException localNoSuchPlayerException) {}
        }
      }
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\questions\SetDestinationQuestion.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */