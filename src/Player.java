import java.util.*;

public class Player{
    int walkBonus=0,id,skipTurns=0,cards=0,putCardAway=0,walkDouble=0,useBridge=0,nextRollAmount=6;
    String name,ip;
    int posX,posY;
    ArrayList<Card> hasCards = new ArrayList<>();
    boolean skippingTurns=false, walkDoubleRemoveable=false, useBridgeRemoveable=false,ee=false;
    boolean inGoal=false;
    
    public Player(int diceSides){
        nextRollAmount=diceSides;
    }
    
    public void addCard(Card card){
        hasCards.add(card);
    }
    
    public void removeCard(Card card){
        hasCards.remove(card);
    }
    
    public void addSkipTurns(int amount){
        skipTurns=skipTurns+amount;
        skippingTurns=true;
    }
    
    public void removeSkipTurn(){
        if(skipTurns>-1)skipTurns--;
    }
    
    public void addRollTwice(int amount){
        walkBonus = walkBonus + amount;
    }
    
    public void removeRollTwice(){
        if(walkBonus>0)walkBonus--;
    }
    
    public void addWalkDouble(int amount){
        walkDouble = walkDouble + amount;
    }
    
    public void removeWalkDouble(){
        if(walkDouble>0 && walkDoubleRemoveable)walkDouble--;
        walkDoubleRemoveable=false;
    }
    
    public void addUseBridge(int amount){
        useBridge = useBridge + amount;
    }
    
    public void removeUseBridge(){
        if(useBridge>0 && useBridgeRemoveable)useBridge--;
        useBridgeRemoveable=false;
    }
    
    public void resetSkipTurn(){
        skipTurns=0;
    }
}
