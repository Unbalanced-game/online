import java.util.*;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import javax.swing.JFrame;
import javax.swing.ScrollPaneConstants;
import javax.swing.ImageIcon;
public class Summary{
    ArrayList<String> events = new ArrayList<>();
    ArrayList<String> log = new ArrayList<>();
    int cardsPlayed[], rolled[], rolledTimes[];
    int players;
    GameManager gm;

    public Summary(int players, GameManager gm){
        this.players = players;
        this.gm = gm;
        cardsPlayed = new int[players];
        rolled = new int[players];
        rolledTimes = new int[players];
    }

    public void add(String text){
        events.add(text);
    }

    public void remove(String text){
        events.remove(text);
    }

    public void cardPlayed(String cardName, int playerID, boolean finalCard){
        if(finalCard){
            add(gm.dieSteuerung.lang.get("sumCardPlayed",cardName));
        }else{
            add(gm.dieSteuerung.lang.get("sumCardAdded",gm.getPlayerName(playerID),cardName));
            cardsPlayed[playerID] = cardsPlayed[playerID] + 1;
        }
    }

    public void diceRoll(int playerID, int amount){
        add(gm.dieSteuerung.lang.get("sumRolled",gm.getPlayerName(playerID),amount));
        rolled[playerID] += amount;
        rolledTimes[playerID]++;
    }

    public void printAll(){
        for(int i=0;i<events.size();i++)
            System.out.println(events.get(i));
    }

    public void display(){
        String eventString="\nEvents:\n";
        for(int i=0;i<events.size();i++)
            eventString = eventString+events.get(i)+"\n";
        eventString = eventString+"\n - - - - - - - - - - - - - - - - - -\n\n";
        for(int i=0;i<players;i++){
            eventString = eventString + gm.dieSteuerung.lang.get("sumPlayer",gm.getPlayerName(i))+"\n";
            eventString = eventString + gm.dieSteuerung.lang.get("sumPlayerRolled",rolled[i],getAverageRoll(i),rolledTimes[i])+"\n";
            eventString = eventString + gm.dieSteuerung.lang.get("sumPlayerCardsPlayed",cardsPlayed[i])+"\n\n";
        }
        newPanel(eventString);
    }
    
    private String getAverageRoll(int playerID){
        double i1=rolled[playerID], i2=rolledTimes[playerID], i3 = i1/i2;
        return String.valueOf(i3).substring(0,3);
    }
    
    public String getStringToDisplay(){
        String eventString="\nEvents:\n";
        for(int i=0;i<events.size();i++)
            eventString = eventString+events.get(i)+"\n";
        eventString = eventString+"\n - - - - - - - - - - - - - - - - - -\n\n";
        for(int i=0;i<players;i++){
            eventString = eventString + gm.dieSteuerung.lang.get("sumPlayer",gm.getPlayerName(i))+"\n";
            eventString = eventString + gm.dieSteuerung.lang.get("sumPlayerRolled",rolled[i],getAverageRoll(i),rolledTimes[i])+"\n";
            eventString = eventString + gm.dieSteuerung.lang.get("sumPlayerCardsPlayed",cardsPlayed[i])+"\n\n";
        }
        return eventString;
    }

    public void log(String text){
        log.add(text);
    }

    public void displayLog(){
        String slog="";
        for(int i=0;i<log.size();i++)
            slog = slog+log.get(i)+"\n";
        newPanel(slog);
    }

    private void newPanel(String text){
        JPanel middlePanel=new JPanel();
        JTextArea display=new JTextArea (16,58);
        display.setText(text);
        display.setEditable(false);
        JScrollPane scroll=new JScrollPane(display);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        middlePanel.add(scroll);
        JFrame frame=new JFrame();
        frame.add(middlePanel);
        frame.setIconImage(new ImageIcon("images/icons/icon.png").getImage());
        frame.setTitle("Summary | UNBALANCED");
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}