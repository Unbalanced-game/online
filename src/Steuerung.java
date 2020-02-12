import java.awt.Color;
import java.awt.Desktop;
import java.io.File;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import javax.swing.JFrame;
import javax.swing.ScrollPaneConstants;
import javax.swing.ImageIcon;

public class Steuerung{
    Configuration config=new Configuration("config/general");
    Language lang=new Language();
    Audio audio=new Audio();
    Transfer tr;
    GameBoard boardInfo=new GameBoard();
    GuiBoard board;
    GameManager game;
    GuiDice dice;
    GuiCards cards;
    GuiCardsPlayed playedCards;
    GuiPlayers guiPlayers;
    GuiMenu menu;
    GuiLoading loading;
    GuiOptions opt;
    GuiStatus status;
    Popup pop=new Popup();
    NameGenerator nameGenerator=new NameGenerator(20);
    boolean isServer=false, connected=false, autoSize=true, autoJoin=true, triedToConnect=false;
    String username="NoName";
    int amountPopupsOpen=0,playerID;
    Color playerColor[]=new Color[]{new Color(51, 112, 255),new Color(235, 64, 52),new Color(104, 255, 23),new Color(255, 243, 23),new Color(189, 52, 235),new Color(52, 235, 153),new Color(0, 0, 0),new Color(255, 255, 255),new Color(255, 193, 23),new Color(235, 28, 104)};
    ImageIcon icon=new ImageIcon("images/icons/icon.png");

    public Steuerung(){
        menu=new GuiMenu(this);
    }

    public void createNewGame(String settings[]){
        try{
            if(Integer.parseInt(settings[1])>10 || Integer.parseInt(settings[1])<1)
                notification(lang.get("invalidAmountPlayers"));
            else try{
                    initColors(Integer.parseInt(settings[1]));
                    config.setOption("name", username);
                    config.setOption("board", settings[0]);
                    config.setOption("players", settings[1]);
                    config.setOption("deck", settings[2]);
                    if(!triedToConnect) tr=new Transfer("none",this);
                    boardInfo.setBoardFromFile("boards/"+settings[0]+".board");
                    game = new GameManager(this);
                    game.setPlayers(Integer.parseInt(settings[1]));
                    game.initCards(settings[2]);
                    isServer=true;playerID=0;
                    menu.dispose();
                    loading = new GuiLoading();
                }catch(Exception e){System.out.println("createNewGame() "+e);}
        }catch(Exception e){
            notification(lang.get("nan",settings[1]));}
    }

    public void joinGame(String mode){
        if(triedToConnect){
            tr.send.tryToConnect(mode);
            if(!mode.equals("auto")) sendToServer("gm--connect"+username);
        }else
            try{
                initColors(10);
                config.setOption("name", username);
                isServer=false;
                tr=new Transfer(mode,this);
                tr.send.tryToConnect(mode);
                if(!mode.equals("auto")) sendToServer("gm--connect"+username);
                triedToConnect=true;
            }catch(Exception e){System.out.println("joinGame() "+e);}
    }

    public void recieve(String ip, String message){
        if(message.contains("gm--") && isServer) game.recieveFromClient(ip,message.replace("gm--",""));
        else{
            if(isServer)game.log("client: "+message);System.out.println("client: "+message);
            if(message.contains("connected") && !connected){
                connected=true;
                menu.dispose();
                if(!isServer){
                    notification(lang.get("connected"));
                    playerID=Integer.parseInt(message.replace("connected",""));
                    loading = new GuiLoading();
                    tr.send.serverip=ip;
                }
            }else if(message.contains("setBoard--")){
                if(!isServer)boardInfo.setBoardFromArray(message.replace("setBoard--","").split("#"));
                board = new GuiBoard(boardInfo, this);
                dice=new GuiDice(this);
                cards=new GuiCards(this);
                playedCards=new GuiCardsPlayed(this);
                status=new GuiStatus();
                if(isServer)try{
                        loading.dispose();
                        game.waitingForPlayers.setVisible(true);
                        notification(lang.get("successfullySetupServer"));
                    }catch(Exception e){restart();}
            }else if(message.contains("setPlayerPos--")){
                String setPos[]=message.replace("setPlayerPos--", "").split("#");
                board.setPlayerPosition(Integer.parseInt(setPos[0]), Integer.parseInt(setPos[1]), Integer.parseInt(setPos[2]));
            }else if(message.contains("gameStart--")){
                guiPlayers=new GuiPlayers(this,Integer.parseInt(message.replace("gameStart--","")));
                audio.playSound("audio/start.wav");
                try{loading.dispose();}catch(Exception e){}
                board.setVisible(true);
                cards.setVisible(true);
                playedCards.setVisible(true);
                guiPlayers.setVisible(true);
                dice.setVisible(true);
                status.setVisible(true);
            }else if(message.contains("rolled--")){
                if(Integer.parseInt(message.replace("rolled--","").split("#")[0])==playerID)
                    dice.setDice(message.replace("rolled--","").split("#")[1]);
            }else if(message.contains("notification--")){
                notification(message.replace("notification--",""));
            }else if(message.contains("setHighlight--")){
                message=message.replace("setHighlight--","");
                if(message.split("#")[2].equals("false"))board.setHighlight(Integer.parseInt(message.split("#")[0]),Integer.parseInt(message.split("#")[1]),false);
                else if(message.split("#")[2].equals("true"))board.setHighlight(Integer.parseInt(message.split("#")[0]),Integer.parseInt(message.split("#")[1]),true);
            }else if(message.contains("resetHighlight")){
                for(int i=0;i<boardInfo.sizeX;i++){
                    for(int j=0;j<boardInfo.sizeY;j++){
                        board.setHighlight(i,j,false);
                    }
                }
            }else if(message.contains("addCard--")){
                cards.addCard(message.replace("addCard--",""));
            }else if(message.contains("removeCard--")){
                cards.removeCard(message.replace("removeCard--",""));
            }else if(message.contains("setCardHighlight--")){
                cards.setHighlight(message.replace("setCardHighlight--",""));
            }else if(message.contains("displayPlayer--")){
                guiPlayers.addPlayer(message.replace("displayPlayer--",""));
            }else if(message.contains("setPlayerText--")){
                guiPlayers.setPlayerText(Integer.parseInt(message.replace("setPlayerText--","").split("#")[0]),message.replace("setPlayerText--","").split("#")[1]);
            }else if(message.contains("addPlayedCard--")){
                playedCards.addCard(message.replace("addPlayedCard--",""));
            }else if(message.contains("removeAllPlayedCards")){
                playedCards.removeAllCards();
            }else if(message.contains("audio--")){
                audio.playSound(message.replace("audio--",""));
            }else if(message.contains("input--")){
                sendToServer("gm--userInput--"+pop.textInput(message.replace("input--","")));
            }else if(message.contains("setPlayedLabel--")){
                playedCards.setLabel(Integer.parseInt(message.replace("setPlayedLabel--","").split("#")[0]),message.replace("setPlayedLabel--","").split("#")[1]);
            }else if(message.equals("ee")){
                audio.playSound("audio/ee.wav");
                board.ee();
            }else if(message.contains("panel--")){
                if(message.contains("Events:")) audio.playSound("audio/win.wav");
                newPanel(message.replace("panel--",""));
            }else if(message.equals("restart")){
                restart();
            }else if(message.contains("setStatus--")){
                status.set(message.replace("setStatus--",""));
            }
        }
    }

    public void sendToClient(String ip, String message){
        if(isServer && ip.equals(tr.send.serverip)) recieve(tr.send.serverip,message);
        else tr.sendToClient(ip,message);
    }

    public void sendToServer(String message){
        if(isServer) recieve(tr.send.serverip,message);
        else tr.sendToServer(message);
    }

    private void initColors(int amount){
        Configuration colors=new Configuration("config/colors");
        playerColor=new Color[amount];
        for(int i=1;i<=amount;i++){
            playerColor[i-1]=Color.decode(colors.getOptionString("c"+i));
        }
    }

    public void notification(String message){
        new GuiPopup(message,this,true);
    }

    public void restart(){
        try{
            File file = new File("unbalanced.jar");
            Desktop desktop = Desktop.getDesktop();
            if(file.exists()) desktop.open(file);
        }catch(Exception e){}
        System.exit(100);
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

    public static void main(String args[]){
        new Steuerung();
    }
}