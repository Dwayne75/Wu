package com.wurmonline.server.webinterface;

import com.wurmonline.server.players.Ban;
import com.wurmonline.shared.exceptions.WurmServerException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

public abstract interface WebInterface
  extends Remote
{
  public static final int DEFAULT_RMI_PORT = 7220;
  public static final int DEFAULT_REGISTRATION_PORT = 7221;
  
  public abstract int getPower(String paramString, long paramLong)
    throws RemoteException;
  
  public abstract boolean isRunning(String paramString)
    throws RemoteException;
  
  public abstract int getPlayerCount(String paramString)
    throws RemoteException;
  
  public abstract int getPremiumPlayerCount(String paramString)
    throws RemoteException;
  
  public abstract String getTestMessage(String paramString)
    throws RemoteException;
  
  public abstract void broadcastMessage(String paramString1, String paramString2)
    throws RemoteException;
  
  public abstract long getAccountStatusForPlayer(String paramString1, String paramString2)
    throws RemoteException;
  
  public abstract long chargeMoney(String paramString1, String paramString2, long paramLong)
    throws RemoteException;
  
  public abstract String getServerStatus(String paramString)
    throws RemoteException;
  
  public abstract Map<String, Integer> getBattleRanks(String paramString, int paramInt)
    throws RemoteException;
  
  public abstract Map<String, Long> getFriends(String paramString, long paramLong)
    throws RemoteException;
  
  public abstract Map<String, String> getInventory(String paramString, long paramLong)
    throws RemoteException;
  
  public abstract Map<Long, Long> getBodyItems(String paramString, long paramLong)
    throws RemoteException;
  
  public abstract Map<String, Float> getSkills(String paramString, long paramLong)
    throws RemoteException;
  
  public abstract Map<String, ?> getPlayerSummary(String paramString, long paramLong)
    throws RemoteException;
  
  public abstract long getLocalCreationTime(String paramString)
    throws RemoteException;
  
  public abstract String ban(String paramString1, String paramString2, String paramString3, int paramInt)
    throws RemoteException;
  
  public abstract String pardonban(String paramString1, String paramString2)
    throws RemoteException;
  
  public abstract String addBannedIp(String paramString1, String paramString2, String paramString3, int paramInt)
    throws RemoteException;
  
  public abstract Ban[] getPlayersBanned(String paramString)
    throws RemoteException;
  
  public abstract Ban[] getIpsBanned(String paramString)
    throws RemoteException;
  
  public abstract String removeBannedIp(String paramString1, String paramString2)
    throws RemoteException;
  
  public abstract Map<Integer, String> getKingdoms(String paramString)
    throws RemoteException;
  
  public abstract Map<Long, String> getPlayersForKingdom(String paramString, int paramInt)
    throws RemoteException;
  
  public abstract long getPlayerId(String paramString1, String paramString2)
    throws RemoteException;
  
  public abstract Map<String, ?> createPlayer(String paramString1, String paramString2, String paramString3, String paramString4, String paramString5, String paramString6, byte paramByte1, byte paramByte2, long paramLong, byte paramByte3)
    throws RemoteException;
  
  public abstract Map<String, String> createPlayerPhaseOne(String paramString1, String paramString2, String paramString3)
    throws RemoteException;
  
  public abstract Map<String, ?> createPlayerPhaseTwo(String paramString1, String paramString2, String paramString3, String paramString4, String paramString5, String paramString6, byte paramByte1, byte paramByte2, long paramLong, byte paramByte3, String paramString7)
    throws RemoteException;
  
  public abstract Map<String, ?> createPlayerPhaseTwo(String paramString1, String paramString2, String paramString3, String paramString4, String paramString5, String paramString6, byte paramByte1, byte paramByte2, long paramLong, byte paramByte3, String paramString7, int paramInt)
    throws RemoteException;
  
  public abstract Map<String, ?> createPlayerPhaseTwo(String paramString1, String paramString2, String paramString3, String paramString4, String paramString5, String paramString6, byte paramByte1, byte paramByte2, long paramLong, byte paramByte3, String paramString7, int paramInt, boolean paramBoolean)
    throws RemoteException;
  
  public abstract byte[] createAndReturnPlayer(String paramString1, String paramString2, String paramString3, String paramString4, String paramString5, String paramString6, byte paramByte1, byte paramByte2, long paramLong, byte paramByte3, boolean paramBoolean1, boolean paramBoolean2, boolean paramBoolean3)
    throws RemoteException;
  
  public abstract Map<String, String> addMoneyToBank(String paramString1, String paramString2, long paramLong, String paramString3)
    throws RemoteException;
  
  public abstract long getMoney(String paramString1, long paramLong, String paramString2)
    throws RemoteException;
  
  public abstract Map<String, String> reversePayment(String paramString1, long paramLong, int paramInt1, int paramInt2, String paramString2, String paramString3, String paramString4)
    throws RemoteException;
  
  public abstract Map<String, String> addMoneyToBank(String paramString1, String paramString2, long paramLong, String paramString3, boolean paramBoolean)
    throws RemoteException;
  
  public abstract Map<String, String> addMoneyToBank(String paramString1, String paramString2, long paramLong1, long paramLong2, String paramString3, boolean paramBoolean)
    throws RemoteException;
  
  public abstract Map<String, String> addPlayingTime(String paramString1, String paramString2, int paramInt1, int paramInt2, String paramString3, boolean paramBoolean)
    throws RemoteException;
  
  public abstract Map<String, String> addPlayingTime(String paramString1, String paramString2, int paramInt1, int paramInt2, String paramString3)
    throws RemoteException;
  
  public abstract Map<Integer, String> getDeeds(String paramString)
    throws RemoteException;
  
  public abstract Map<String, ?> getDeedSummary(String paramString, int paramInt)
    throws RemoteException;
  
  public abstract Map<String, Long> getPlayersForDeed(String paramString, int paramInt)
    throws RemoteException;
  
  public abstract Map<String, Integer> getAlliesForDeed(String paramString, int paramInt)
    throws RemoteException;
  
  public abstract String[] getHistoryForDeed(String paramString, int paramInt1, int paramInt2)
    throws RemoteException;
  
  public abstract String[] getAreaHistory(String paramString, int paramInt)
    throws RemoteException;
  
  public abstract Map<String, ?> getItemSummary(String paramString, long paramLong)
    throws RemoteException;
  
  public abstract Map<String, String> getPlayerIPAddresses(String paramString)
    throws RemoteException;
  
  public abstract Map<String, String> getNameBans(String paramString)
    throws RemoteException;
  
  public abstract Map<String, String> getIPBans(String paramString)
    throws RemoteException;
  
  public abstract Map<String, String> getWarnings(String paramString)
    throws RemoteException;
  
  public abstract String getWurmTime(String paramString)
    throws RemoteException;
  
  public abstract String getUptime(String paramString)
    throws RemoteException;
  
  public abstract String getNews(String paramString)
    throws RemoteException;
  
  public abstract String getGameInfo(String paramString)
    throws RemoteException;
  
  public abstract Map<String, String> getKingdomInfluence(String paramString)
    throws RemoteException;
  
  public abstract Map<String, ?> getMerchantSummary(String paramString, long paramLong)
    throws RemoteException;
  
  public abstract Map<String, ?> getBankAccount(String paramString, long paramLong)
    throws RemoteException;
  
  public abstract Map<String, ?> authenticateUser(String paramString1, String paramString2, String paramString3, String paramString4, Map paramMap)
    throws RemoteException;
  
  public abstract Map<String, ?> authenticateUser(String paramString1, String paramString2, String paramString3, String paramString4)
    throws RemoteException;
  
  public abstract Map<String, String> changePassword(String paramString1, String paramString2, String paramString3, String paramString4)
    throws RemoteException;
  
  public abstract Map<String, String> changePassword(String paramString1, String paramString2, String paramString3, String paramString4, String paramString5)
    throws RemoteException;
  
  public abstract boolean changePassword(String paramString1, long paramLong, String paramString2)
    throws RemoteException;
  
  public abstract Map<String, String> changeEmail(String paramString1, String paramString2, String paramString3, String paramString4)
    throws RemoteException;
  
  public abstract String getChallengePhrase(String paramString1, String paramString2)
    throws RemoteException;
  
  public abstract String[] getPlayerNamesForEmail(String paramString1, String paramString2)
    throws RemoteException;
  
  public abstract String getEmailAddress(String paramString1, String paramString2)
    throws RemoteException;
  
  public abstract Map<String, String> requestPasswordReset(String paramString1, String paramString2, String paramString3)
    throws RemoteException;
  
  public abstract Map<Integer, String> getAllServers(String paramString)
    throws RemoteException;
  
  public abstract Map<Integer, String> getAllServerInternalAddresses(String paramString)
    throws RemoteException;
  
  public abstract boolean sendMail(String paramString1, String paramString2, String paramString3, String paramString4, String paramString5)
    throws RemoteException;
  
  public abstract Map<String, String> getPendingAccounts(String paramString)
    throws RemoteException;
  
  public abstract void shutDown(String paramString1, String paramString2, String paramString3, String paramString4, int paramInt)
    throws RemoteException;
  
  public abstract Map<String, Byte> getReferrers(String paramString, long paramLong)
    throws RemoteException;
  
  public abstract String addReferrer(String paramString1, String paramString2, long paramLong)
    throws RemoteException;
  
  public abstract String acceptReferrer(String paramString1, long paramLong, String paramString2, boolean paramBoolean)
    throws RemoteException;
  
  public abstract Map<String, Double> getSkillStats(String paramString, int paramInt)
    throws RemoteException;
  
  public abstract Map<Integer, String> getSkills(String paramString)
    throws RemoteException;
  
  public abstract Map<String, ?> getStructureSummary(String paramString, long paramLong)
    throws RemoteException;
  
  public abstract long getStructureIdFromWrit(String paramString, long paramLong)
    throws RemoteException;
  
  public abstract Map<String, ?> getTileSummary(String paramString, int paramInt1, int paramInt2, boolean paramBoolean)
    throws RemoteException;
  
  public abstract String getReimbursementInfo(String paramString1, String paramString2)
    throws RemoteException;
  
  public abstract boolean withDraw(String paramString1, String paramString2, String paramString3, String paramString4, int paramInt1, int paramInt2, boolean paramBoolean, int paramInt3)
    throws RemoteException;
  
  public abstract boolean transferPlayer(String paramString1, String paramString2, int paramInt1, int paramInt2, boolean paramBoolean, int paramInt3, byte[] paramArrayOfByte)
    throws RemoteException;
  
  public abstract boolean setCurrentServer(String paramString1, String paramString2, int paramInt)
    throws RemoteException;
  
  public abstract boolean addDraggedItem(String paramString, long paramLong1, byte[] paramArrayOfByte, long paramLong2, int paramInt1, int paramInt2)
    throws RemoteException;
  
  public abstract String rename(String paramString1, String paramString2, String paramString3, String paramString4, int paramInt)
    throws RemoteException;
  
  public abstract String changePassword(String paramString1, String paramString2, String paramString3, String paramString4, int paramInt)
    throws RemoteException;
  
  public abstract String changeEmail(String paramString1, String paramString2, String paramString3, String paramString4, String paramString5, int paramInt, String paramString6, String paramString7)
    throws RemoteException;
  
  public abstract String addReimb(String paramString1, String paramString2, String paramString3, int paramInt1, int paramInt2, int paramInt3, boolean paramBoolean)
    throws RemoteException;
  
  public abstract long[] getCurrentServerAndWurmid(String paramString1, String paramString2, long paramLong)
    throws RemoteException;
  
  public abstract Map<Long, byte[]> getPlayerStates(String paramString, long[] paramArrayOfLong)
    throws RemoteException, WurmServerException;
  
  public abstract void manageFeature(String paramString, int paramInt1, int paramInt2, boolean paramBoolean1, boolean paramBoolean2, boolean paramBoolean3)
    throws RemoteException;
  
  public abstract void startShutdown(String paramString1, String paramString2, int paramInt, String paramString3)
    throws RemoteException;
  
  public abstract String sendMail(String paramString, byte[] paramArrayOfByte1, byte[] paramArrayOfByte2, long paramLong1, long paramLong2, int paramInt)
    throws RemoteException;
  
  public abstract String setPlayerPremiumTime(String paramString1, long paramLong1, long paramLong2, int paramInt1, int paramInt2, String paramString2)
    throws RemoteException;
  
  public abstract String setPlayerMoney(String paramString1, long paramLong1, long paramLong2, long paramLong3, String paramString2)
    throws RemoteException;
  
  public abstract Map<String, String> doesPlayerExist(String paramString1, String paramString2)
    throws RemoteException;
  
  public abstract void setWeather(String paramString, float paramFloat1, float paramFloat2, float paramFloat3)
    throws RemoteException;
  
  public abstract String sendVehicle(String paramString, byte[] paramArrayOfByte1, byte[] paramArrayOfByte2, long paramLong1, long paramLong2, int paramInt1, int paramInt2, int paramInt3, int paramInt4, float paramFloat)
    throws RemoteException;
  
  public abstract void requestDemigod(String paramString1, byte paramByte, String paramString2)
    throws RemoteException;
  
  public abstract String ascend(String paramString1, int paramInt, String paramString2, long paramLong, byte paramByte1, byte paramByte2, byte paramByte3, float paramFloat1, float paramFloat2, float paramFloat3, float paramFloat4, float paramFloat5, float paramFloat6, float paramFloat7)
    throws RemoteException;
  
  public abstract boolean requestDeityMove(String paramString1, int paramInt1, int paramInt2, String paramString2)
    throws RemoteException;
  
  public abstract void setKingdomInfo(String paramString1, int paramInt, byte paramByte1, byte paramByte2, String paramString2, String paramString3, String paramString4, String paramString5, String paramString6, String paramString7, boolean paramBoolean)
    throws RemoteException;
  
  public abstract boolean kingdomExists(String paramString, int paramInt, byte paramByte, boolean paramBoolean)
    throws RemoteException;
  
  public abstract void genericWebCommand(String paramString, short paramShort, long paramLong, byte[] paramArrayOfByte)
    throws RemoteException;
  
  public abstract int[] getPremTimeSilvers(String paramString, long paramLong)
    throws RemoteException;
  
  public abstract void awardPlayer(String paramString1, long paramLong, String paramString2, int paramInt1, int paramInt2)
    throws RemoteException;
  
  public abstract boolean isFeatureEnabled(String paramString, int paramInt)
    throws RemoteException;
  
  public abstract boolean setPlayerFlag(String paramString, long paramLong, int paramInt, boolean paramBoolean)
    throws RemoteException;
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\webinterface\WebInterface.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */