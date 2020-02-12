import java.awt.Color;

public class GameManager{
    Steuerung dieSteuerung;
    private String cardInUseName[]=new String[40], validNumbers="", chooseCard="";
    private int maxPlayers=0, players=0, playerTurn, playerWalkLeft=0, rolled=0, cardsLeft;
    private int playedCardUserID[]=new int[40], timerValue=0, dummy1,dummy2,affectedID;
    private int normalTimerTime=5, copyCatUser=-1, diceSides=6, winTurn=0, winning=0;
    private int playerPos[][][], hndslln1=-1, hndslln2=-1, hndslln=0;
    private boolean playerWalking=false, cardInUse=false, letPlayerChoosePlayer=false;
    private boolean mayChooseHimself=false, letPlayerChooseHisCard=false,playerChooseNumber=false;
    private boolean pickCardWithoutRemove=false, copyCatCard=false, containsRedCard=false;
    private boolean forceRedCard=false, firstTurn=true, gameOver=false, userClicked=false;
    private boolean lockAffected=false, cardIgnoreUser=false;
    boolean mayNotCancleCardNow=false;
    private Player player[];
    private Card[] cards;
    private Summary sum;
    GuiStart waitingForPlayers;
    Ticker ticker=new Ticker(1000,this);
    String board[],sendBoard;

    public GameManager(Steuerung pDieSteuerung){
        dieSteuerung=pDieSteuerung;
    }

    public void setPlayers(int pPlayers){
        maxPlayers=pPlayers;
        player=new Player[maxPlayers];
        waitingForPlayers = new GuiStart(this,maxPlayers);
        sendBoard="setBoard--";
        board=dieSteuerung.boardInfo.getInput();
        for(int i=0;i<board.length;i++) sendBoard=sendBoard+board[i]+"#";
        addPlayer(dieSteuerung.tr.send.serverip,dieSteuerung.username);
        playerPos=new int[maxPlayers][maxPlayers][2];
        //for(int i=0;i<9;i++) addPlayer("10.10.10."+i,dieSteuerung.nameGenerator.generateName());
    }

    public void addPlayer(String pIp, String pName){
        if(!playerNameExist(pName)){
            diceSides=dieSteuerung.config.getOptionInt("diceSides");
            normalTimerTime=dieSteuerung.config.getOptionInt("timerTime");
            player[players] = new Player(diceSides);
            player[players].ip=pIp;
            player[players].id=players;
            player[players].name=pName;
            waitingForPlayers.addPlayer(pIp,getPlayerHTMLLabel(players));
            send(players,"connected"+players);
            dieSteuerung.tr.scheduleSendToClient(pIp,"connected"+players,1100);
            if(!dieSteuerung.isServer || players>0) dieSteuerung.audio.playSound("audio/join.wav");
            if(pName.equals("ersteramschlüssel"))send(players,"audio--audio/itschebinischte.wav");
            players++;
            dieSteuerung.tr.sendToClient(pIp,sendBoard);
        }
    }

    private boolean playerNameExist(String lookupname){
        for(int i=0;i<players;i++){
            if(player[i].name.equals(lookupname))return true;
        }
        return false;
    }

    public void gameStart(){
        sum = new Summary(players,this);
        for(int i=0;i<players;i++){
            player[i].posX=dieSteuerung.boardInfo.startX;
            player[i].posY=dieSteuerung.boardInfo.startY;
            broadcast("setPlayerPos--"+i+"#"+player[i].posX+"#"+player[i].posY);
        }
        playerTurn=dieSteuerung.board.randomInt(0, players-1);
        playerWalkLeft=1;
        if(player[playerTurn].walkBonus>0) playerWalkLeft++;
        broadcast("gameStart--"+players);
        for(int i=0;i<players;i++){
            broadcast("displayPlayer--<html>"+getPlayerHTMLLabel(i));
            broadcast("setPlayerText--"+i+"#Connected!");
            pickCard(i, dieSteuerung.config.getOptionInt("startCards"));
        }
        status(dieSteuerung.lang.get("playerTurn",getPlayerHTMLLabel(playerTurn)));
        savePlayerPositions(playerTurn);
    }

    private void send(int playerID, String message){
        dieSteuerung.sendToClient(player[playerID].ip, message);
    }

    private void broadcast(String message){
        for(int i=0;i<players;i++)
            dieSteuerung.sendToClient(player[i].ip, message);
    }

    private void broadcastInvert(String message){
        for(int i=players-1;i>=0;i--)
            dieSteuerung.sendToClient(player[i].ip, message);
    }

    private int getIpIndex(String pIp){
        for(int i=0;i<player.length;i++)
            if(player[i].ip.equals(pIp))return i;
        return 0;
    }

    public void recieveFromClient(String pIp, String message){
        log("server: "+message);System.out.println("server: "+message);
        if(message.contains("connect")){
            addPlayer(pIp,message.replace("connect",""));
        }else{
            int currentId=getIpIndex(pIp);
            if(message.equals("rollDice")){
                if(gameOver)return;
                if(playerPutCardAway()){
                    send(currentId,"notification--"+dieSteuerung.lang.get("playerPutCardAway"));
                }else if(currentId==playerTurn && playerWalkLeft>0 && !playerWalking && !cardInUse){
                    rolled=dieSteuerung.board.randomInt(1, player[currentId].nextRollAmount);
                    playerWalking=true;player[currentId].nextRollAmount=diceSides;
                    sum.diceRoll(currentId,rolled);
                    playerWalkDisplay(currentId,player[currentId].posX,player[currentId].posY,rolled+1,true);
                    if(hndslln>0&&playerTurn==hndslln1)playerWalkForward(hndslln2, rolled);
                    else if(hndslln>0&&playerTurn==hndslln2)playerWalkForward(hndslln1, rolled);
                    broadcast("rolled--"+currentId+"#"+rolled);
                    playerWalkLeft--;firstTurn=false;
                }else send(currentId,"notification--"+dieSteuerung.lang.get("mayNotRollDice"));
            }else if(message.equals("endTurn")){
                if(gameOver)return;
                if(playerPutCardAway()){
                    send(currentId,"notification--"+dieSteuerung.lang.get("playerPutCardAway"));
                }else if(currentId==playerTurn && playerWalkLeft==0 && !playerWalking && !cardInUse){
                    do{
                        savePlayerPositions(playerTurn);
                        playerTurn=(playerTurn+1)%players;
                        if(player[playerTurn].skipTurns<=0)player[playerTurn].skippingTurns=false;
                        player[playerTurn].removeSkipTurn();
                        if(hndslln>0 && playerTurn==hndslln2)hndslln--;
                    }while(player[playerTurn].skippingTurns);
                    if(player[winning].inGoal && winTurn==playerTurn)endGame(winning);
                    player[playerTurn].resetSkipTurn();
                    playerWalkLeft=1;
                    if(player[playerTurn].walkBonus>0)playerWalkLeft++;
                    player[playerTurn].removeRollTwice();
                    player[playerTurn].removeWalkDouble();
                    player[playerTurn].removeUseBridge();
                    send(playerTurn,"notification--"+dieSteuerung.lang.get("yourTurn"));
                    generateStatusText();
                    status(dieSteuerung.lang.get("playerTurn",getPlayerHTMLLabel(playerTurn)));
                }else if(currentId==playerTurn){
                    send(currentId,"notification--"+dieSteuerung.lang.get("mayNotEndTurn"));
                }else{
                    send(currentId,"notification--"+dieSteuerung.lang.get("notYourTurn"));
                }
            }else if(message.contains("tileClicked--")){
                if(gameOver)return;
                message=message.replace("tileClicked--", "");
                int x=Integer.parseInt(message.split("#")[0]), y=Integer.parseInt(message.split("#")[1]);
                ee(x,y,currentId);
                if(currentId==playerTurn && playerWalking){
                    if(dieSteuerung.board.l_highlight[x][y].isVisible()){
                        player[currentId].posX=x;
                        player[currentId].posY=y;
                        broadcast("setPlayerPos--"+currentId+"#"+player[currentId].posX+"#"+player[currentId].posY);
                        playerWalking=false;resetWalkDisplay();
                        walkOnTileEffect(currentId,x,y);
                    }
                }
                try{
                    if(currentId==playedCardUserID[dieSteuerung.playedCards.amountCards-1] && !playerWalking){
                        if(dieSteuerung.board.l_highlight[x][y].isVisible()){
                            player[currentId].posX=x;
                            player[currentId].posY=y;
                            broadcast("setPlayerPos--"+currentId+"#"+player[currentId].posX+"#"+player[currentId].posY);
                            playerWalking=false;resetWalkDisplay();
                            walkOnTileEffect(currentId,x,y);
                            returnToNormalGame();
                        }
                    }
                }catch(Exception e){}
                if(playerWalkLeft<=0) status(dieSteuerung.lang.get("statusEndTurn",getPlayerHTMLLabel(playerTurn)));
            }else if(message.contains("useCard--")){
                if(gameOver)return;
                useCard(currentId,message.replace("useCard--", ""));
            }else if(message.contains("clickUser--")){
                if(gameOver)return;
                userClicked(currentId,Integer.parseInt(message.replace("clickUser--", "")));
            }else if(message.contains("userInput--") && playerChooseNumber){
                if(gameOver)return;
                try{
                    userNumInput(currentId,Integer.parseInt(message.replace("userInput--", "")));
                }catch(Exception e){retryInput();}
            }
        }
    }

    private void resetWalkDisplay(){
        broadcast("resetHighlight");
    }

    private void playerWalkDisplay(int playerID, int x, int y, int amount, boolean reset){
        if(reset) resetWalkDisplay();
        if(player[playerID].walkDouble>0){
            player[playerID].walkDoubleRemoveable=true;
            playerWalkDisplayRec(playerID,x,y,((amount-1)*2)+1,player[playerID].useBridge>0);
        }else
            playerWalkDisplayRec(playerID,x,y,amount,player[playerID].useBridge>0);
        player[playerID].useBridgeRemoveable=true;
        for(int i=0;i<dieSteuerung.boardInfo.sizeX;i++){
            for(int j=0;j<dieSteuerung.boardInfo.sizeY;j++){
                if(dieSteuerung.board.l_highlight[i][j].isVisible())return;
            }
        }
        send(playerID,"notification--"+dieSteuerung.lang.get("unableToWalk"));
        playerWalking=false;
    }

    private void playerWalkDisplayRec(int playerID, int x, int y, int amount, boolean bridge){
        if(!dieSteuerung.boardInfo.tilesTypes[x][y].equals("br"))
            amount--;
        for(int i=0;i<dieSteuerung.boardInfo.possiblePaths[y][x].length();i++){
            if(amount>0){
                if(dieSteuerung.boardInfo.possiblePaths[y][x].charAt(i)=='u'){
                    playerWalkDisplayRec(playerID,x,y-1,amount,bridge);
                }else if(dieSteuerung.boardInfo.possiblePaths[y][x].charAt(i)=='d'){
                    playerWalkDisplayRec(playerID,x,y+1,amount,bridge);
                }else if(dieSteuerung.boardInfo.possiblePaths[y][x].charAt(i)=='l'){
                    playerWalkDisplayRec(playerID,x-1,y,amount,bridge);
                }else if(dieSteuerung.boardInfo.possiblePaths[y][x].charAt(i)=='r'){
                    playerWalkDisplayRec(playerID,x+1,y,amount,bridge);
                }
                if(bridge){
                    if(dieSteuerung.boardInfo.bridgeEntrance[y][x].equals("u")){
                        playerWalkDisplayRec(playerID,x,y-1,amount,bridge);
                    }else if(dieSteuerung.boardInfo.bridgeEntrance[y][x].equals("d")){
                        playerWalkDisplayRec(playerID,x,y+1,amount,bridge);
                    }else if(dieSteuerung.boardInfo.bridgeEntrance[y][x].equals("l")){
                        playerWalkDisplayRec(playerID,x-1,y,amount,bridge);
                    }else if(dieSteuerung.boardInfo.bridgeEntrance[y][x].equals("r")){
                        playerWalkDisplayRec(playerID,x+1,y,amount,bridge);
                    }
                }
            }
            if(amount<=0)
                broadcast("setHighlight--"+x+"#"+y+"#true");
        }
    }

    private void walkOnTileEffect(int userID, int x, int y){
        player[userID].inGoal=false;
        if(dieSteuerung.boardInfo.tilesTypes[x][y].equals("  "))
            send(userID,"notification--"+dieSteuerung.lang.get("invalidPosition"));
        //else if(dieSteuerung.boardInfo.tilesTypes[x][y].equals("st")); //Start
        else if(dieSteuerung.boardInfo.tilesTypes[x][y].equals("en")){
            player[userID].inGoal=true;winTurn=playerTurn;winning=userID;
        }
        //else if(dieSteuerung.boardInfo.tilesTypes[x][y].equals("no")); //normale Felder
        else if(dieSteuerung.boardInfo.tilesTypes[x][y].equals("ig")){
            player[userID].posX=dieSteuerung.boardInfo.prisonX;
            player[userID].posY=dieSteuerung.boardInfo.prisonY;
            broadcast("setPlayerPos--"+userID+"#"+player[userID].posX+"#"+player[userID].posY);
        }
        //else if(dieSteuerung.boardInfo.tilesTypes[x][y].equals("ge")); //Gefängnis
        //else if(dieSteuerung.boardInfo.tilesTypes[x][y].equals("br")); //Brücke
        else if(dieSteuerung.boardInfo.tilesTypes[x][y].equals("mk")){
            if(player[userID].cards>0)player[userID].putCardAway++;
        }else if(dieSteuerung.boardInfo.tilesTypes[x][y].equals("nw")){
            if(userID==playerTurn) playerWalkLeft++;
            else playerWalkForward(userID, dieSteuerung.board.randomInt(1,6));
        }else if(dieSteuerung.boardInfo.tilesTypes[x][y].contains("-"))
            if(!(Integer.parseInt(dieSteuerung.boardInfo.tilesTypes[x][y].replace("-", ""))==0))
                playerWalkBack(userID,Integer.parseInt(dieSteuerung.boardInfo.tilesTypes[x][y].replace("-", "")));
            else
                playerWalkBack(userID,10);
        else if(dieSteuerung.boardInfo.tilesTypes[x][y].charAt(1)=='k')
            pickCard(userID,Integer.parseInt(dieSteuerung.boardInfo.tilesTypes[x][y].replace("k","")));
        generateStatusText();syncCards();
    }

    private void playerWalkBack(int userID, int amount){
        for(int i=0;i<amount;i++){
            if(dieSteuerung.boardInfo.comeFrom[player[userID].posY][player[userID].posX].charAt(0)=='u'){
                player[userID].posY--;
            }else if(dieSteuerung.boardInfo.comeFrom[player[userID].posY][player[userID].posX].charAt(0)=='d'){
                player[userID].posY++;
            }else if(dieSteuerung.boardInfo.comeFrom[player[userID].posY][player[userID].posX].charAt(0)=='l'){
                player[userID].posX--;
            }else if(dieSteuerung.boardInfo.comeFrom[player[userID].posY][player[userID].posX].charAt(0)=='r'){
                player[userID].posX++;
            }
        }
        broadcast("setPlayerPos--"+userID+"#"+player[userID].posX+"#"+player[userID].posY);
        walkOnTileEffect(userID,player[userID].posX,player[userID].posY);
    }

    private void playerWalkForward(int userID, int amount){
        for(int i=0;i<amount;i++){
            if(dieSteuerung.boardInfo.possiblePaths[player[userID].posY][player[userID].posX].charAt(0)=='u'){
                player[userID].posY--;
            }else if(dieSteuerung.boardInfo.possiblePaths[player[userID].posY][player[userID].posX].charAt(0)=='d'){
                player[userID].posY++;
            }else if(dieSteuerung.boardInfo.possiblePaths[player[userID].posY][player[userID].posX].charAt(0)=='l'){
                player[userID].posX--;
            }else if(dieSteuerung.boardInfo.possiblePaths[player[userID].posY][player[userID].posX].charAt(0)=='r'){
                player[userID].posX++;
            }
        }
        broadcast("setPlayerPos--"+userID+"#"+player[userID].posX+"#"+player[userID].posY);
        walkOnTileEffect(userID,player[userID].posX,player[userID].posY);
    }

    private void pickCardFromArray(String cards[]){
        String message="";validNumbers="";
        for(int i=0;i<cards.length;i++){
            message=message+getCardByName(cards[i])+": "+cards[i]+"\n";
            validNumbers=validNumbers+String.valueOf(getCardByName(cards[i]));
        }
        if(validNumbers.length()==0)returnToNormalGame();
        else send(getCurrentPlayerID(),"input--"+dieSteuerung.lang.get("popChooseCard")+"\n"+message);
    }

    private void pickCard(int userID, int amount){
        int pickID;
        for(int i=0;i<amount;i++){
            if(cardsLeft>0){
                do{
                    pickID=dieSteuerung.board.randomInt(0, cards.length-1);
                }while(cards[pickID].ownerID>-1);
                player[userID].addCard(cards[pickID]);
                cards[pickID].ownerID=userID;
                send(userID, "addCard--"+cards[pickID].imageName);
                cardsLeft--;player[userID].cards++;
                checkRedCard();
            }
        }
    }

    private boolean isRedCard(String name){
        if(name.equals("Sprint")|| name.equals("Wait that's illegal") || name.equals("todo")) return true; return false;
    }

    private boolean isGreenCard(String name){
        if(name.equals("Windwall")|| name.equals("No U")||name.equals("Flash") || name.equals("todo")) return true; return false;
    }

    private boolean isReplayCard(String name){
        if(name.equals("Alternative Fakten")|| name.equals("Copy Cat")||name.equals("Differntly")||name.equals("Deflection")||name.equals("No U") || name.equals("todo")) return true; return false;
    }

    private void checkRedCard(){
        //if(firstTurn)return;
        for(int i=0;i<cards.length;i++){
            if(isRedCard(cards[i].name) && cards[i].ownerID>=0 && !containsRedCard){
                forceRedCard=true;
                useCard(cards[i].ownerID, cards[i].name);
                containsRedCard=true;
            }
        }
    }

    private int getCardByName(String name){
        for(int i=0;i<cards.length;i++){
            if(cards[i].name.equals(name))return i;
        }
        return 0;
    }

    private int getCardByImageName(String name){
        for(int i=0;i<cards.length;i++){
            if(cards[i].imageName.equals(name))return i;
        }
        return -1;
    }

    private void useCard(int userID, String name){
        int cardID=getCardByImageName(name);
        if(cardID==-1) cardID=getCardByName(name);
        if(player[userID].putCardAway>0){ //put card away
            setCardRemoveStuff(userID,cardID);
        }else if(playerWalking){
            send(userID,"notification--"+dieSteuerung.lang.get("mayNotPlayCard"));
        }else if(chooseCard.equals("choose") && userID==getCurrentPlayerID()){
            chooseCard=name;
            if(cardInUseName[dieSteuerung.playedCards.amountCards-1].equals("Der heilige Fisch")){
                cardFromUserToUser(getCurrentPlayerID(), affectedID, cardID);
                returnToNormalGame();
            }
        }else if((cardIgnoreUser || cards[cardID].ownerID==userID) && ((letPlayerChooseHisCard && pickCardWithoutRemove) || (copyCatCard && copyCatUser==userID))){
            letPlayerChooseHisCard=false;pickCardWithoutRemove=false;containsRedCard=false;cardIgnoreUser=false;
            if(cards[cardID].name.equals("Angel")){
                if(!copyCatCard) setCardClickedWithoutRemoveStuff(userID,cardID,dieSteuerung.playedCards.amountCards,getBeforePlayerID());
                else setCardClickedWithoutRemoveStuff(userID,cardID,dieSteuerung.playedCards.amountCards);
                letPlayerChoosePlayer=true;
                mayChooseHimself=false;
            }else if(cards[cardID].name.equals("Zündung!")){
                if(!copyCatCard) setCardClickedWithoutRemoveStuff(userID,cardID,dieSteuerung.playedCards.amountCards,getBeforePlayerID());
                else setCardClickedWithoutRemoveStuff(userID,cardID,dieSteuerung.playedCards.amountCards);
                letPlayerChoosePlayer=true;
                mayChooseHimself=false;
            }else if(cards[cardID].name.equals("Beleidigung")){
                if(!copyCatCard) setCardClickedWithoutRemoveStuff(userID,cardID,dieSteuerung.playedCards.amountCards,getBeforePlayerID());
                else setCardClickedWithoutRemoveStuff(userID,cardID,dieSteuerung.playedCards.amountCards);
                letPlayerChoosePlayer=true;
                mayChooseHimself=false;
            }else if(cards[cardID].name.equals("Black hole")){
                if(!copyCatCard) setCardClickedWithoutRemoveStuff(userID,cardID,dieSteuerung.playedCards.amountCards,getBeforePlayerID());
                else setCardClickedWithoutRemoveStuff(userID,cardID,dieSteuerung.playedCards.amountCards);
                timerValue=normalTimerTime;
                ticker.startTimer();
            }else if(cards[cardID].name.equals("Weißes Loch")){
                if(!copyCatCard) setCardClickedWithoutRemoveStuff(userID,cardID,dieSteuerung.playedCards.amountCards,getBeforePlayerID());
                else setCardClickedWithoutRemoveStuff(userID,cardID,dieSteuerung.playedCards.amountCards);
                timerValue=normalTimerTime;
                ticker.startTimer();
            }else if(cards[cardID].name.equals("Philipp")){
                if(!copyCatCard) setCardClickedWithoutRemoveStuff(userID,cardID,dieSteuerung.playedCards.amountCards,getBeforePlayerID());
                else setCardClickedWithoutRemoveStuff(userID,cardID,dieSteuerung.playedCards.amountCards);
                letPlayerChoosePlayer=true;
                mayChooseHimself=false;
            }else if(cards[cardID].name.equals("Copy Cat")){
                if(!copyCatCard) setCardClickedWithoutRemoveStuff(userID,cardID,dieSteuerung.playedCards.amountCards,getBeforePlayerID());
                else setCardClickedWithoutRemoveStuff(userID,cardID,dieSteuerung.playedCards.amountCards);
                timerValue=normalTimerTime;
                ticker.startTimer();
                copyCatUser=userID;
            }else if(cards[cardID].name.equals("Flash")){
                if(!copyCatCard) setCardClickedWithoutRemoveStuff(userID,cardID,dieSteuerung.playedCards.amountCards,getBeforePlayerID());
                else setCardClickedWithoutRemoveStuff(userID,cardID,dieSteuerung.playedCards.amountCards);
            }else if(cards[cardID].name.equals("Fischernetz")){
                if(!copyCatCard) setCardClickedWithoutRemoveStuff(userID,cardID,dieSteuerung.playedCards.amountCards,getBeforePlayerID());
                else setCardClickedWithoutRemoveStuff(userID,cardID,dieSteuerung.playedCards.amountCards);
            }else if(cards[cardID].name.equals("Cheat Code")){
                if(!copyCatCard) setCardClickedWithoutRemoveStuff(userID,cardID,dieSteuerung.playedCards.amountCards,getBeforePlayerID());
                else setCardClickedWithoutRemoveStuff(userID,cardID,dieSteuerung.playedCards.amountCards);
                timerValue=normalTimerTime;
                ticker.startTimer();
            }else if(cards[cardID].name.equals("Angelhaken")){
                if(!copyCatCard) setCardClickedWithoutRemoveStuff(userID,cardID,dieSteuerung.playedCards.amountCards,getBeforePlayerID());
                else setCardClickedWithoutRemoveStuff(userID,cardID,dieSteuerung.playedCards.amountCards);
                letPlayerChoosePlayer=true;
                mayChooseHimself=false;
            }else if(cards[cardID].name.equals("Double XP")){
                if(!copyCatCard) setCardClickedWithoutRemoveStuff(userID,cardID,dieSteuerung.playedCards.amountCards,getBeforePlayerID());
                else setCardClickedWithoutRemoveStuff(userID,cardID,dieSteuerung.playedCards.amountCards);
                timerValue=normalTimerTime;
                ticker.startTimer();
            }else if(cards[cardID].name.equals("Reparatur")){
                if(!copyCatCard) setCardClickedWithoutRemoveStuff(userID,cardID,dieSteuerung.playedCards.amountCards,getBeforePlayerID());
                else setCardClickedWithoutRemoveStuff(userID,cardID,dieSteuerung.playedCards.amountCards);
                timerValue=normalTimerTime;
                ticker.startTimer();
            }else if(cards[cardID].name.equals("Supply drop")){
                if(!copyCatCard) setCardClickedWithoutRemoveStuff(userID,cardID,dieSteuerung.playedCards.amountCards,getBeforePlayerID());
                else setCardClickedWithoutRemoveStuff(userID,cardID,dieSteuerung.playedCards.amountCards);
                timerValue=normalTimerTime;
                ticker.startTimer();
            }else if(cards[cardID].name.equals("Großer Fang")){
                if(!copyCatCard) setCardClickedWithoutRemoveStuff(userID,cardID,dieSteuerung.playedCards.amountCards,getBeforePlayerID());
                else setCardClickedWithoutRemoveStuff(userID,cardID,dieSteuerung.playedCards.amountCards);
                timerValue=normalTimerTime;
                ticker.startTimer();
            }else if(cards[cardID].name.equals("Der heilige Fisch")){
                if(!copyCatCard) setCardClickedWithoutRemoveStuff(userID,cardID,dieSteuerung.playedCards.amountCards,getBeforePlayerID());
                else setCardClickedWithoutRemoveStuff(userID,cardID,dieSteuerung.playedCards.amountCards);
                letPlayerChoosePlayer=true;
                mayChooseHimself=false;
            }else if(cards[cardID].name.equals("Alternative Fakten")){
                if(!copyCatCard) setCardClickedWithoutRemoveStuff(userID,cardID,dieSteuerung.playedCards.amountCards,getBeforePlayerID());
                else setCardClickedWithoutRemoveStuff(userID,cardID,dieSteuerung.playedCards.amountCards);
                letPlayerChooseHisCard=true;
                pickCardWithoutRemove=true;
                ticker.stop();
                timerValue=0;
            }else if(cards[cardID].name.equals("Differntly")){
                if(!copyCatCard) setCardClickedWithoutRemoveStuff(userID,cardID,dieSteuerung.playedCards.amountCards,getBeforePlayerID());
                else setCardClickedWithoutRemoveStuff(userID,cardID,dieSteuerung.playedCards.amountCards);
                setCardLabel(getCurrentCardNumber(),dieSteuerung.lang.get("labelAffected",getPlayerHTMLLabel(getBeforePlayerID())));
                timerValue=normalTimerTime;
                ticker.stop();
                ticker.startTimer();
            }else if(cards[cardID].name.equals("Deflection")){
                if(!copyCatCard) setCardClickedWithoutRemoveStuff(userID,cardID,dieSteuerung.playedCards.amountCards,getBeforePlayerID());
                else setCardClickedWithoutRemoveStuff(userID,cardID,dieSteuerung.playedCards.amountCards);
                timerValue=normalTimerTime;
                ticker.stop();
                ticker.startTimer();
            }else if(cards[cardID].name.equals("Jesus")){
                if(!copyCatCard) setCardClickedWithoutRemoveStuff(userID,cardID,dieSteuerung.playedCards.amountCards,getBeforePlayerID());
                else setCardClickedWithoutRemoveStuff(userID,cardID,dieSteuerung.playedCards.amountCards);
                timerValue=normalTimerTime;
                ticker.startTimer();
            }else if(cards[cardID].name.equals("Its Rewind Time")){
                if(!copyCatCard) setCardClickedWithoutRemoveStuff(userID,cardID,dieSteuerung.playedCards.amountCards,getBeforePlayerID());
                else setCardClickedWithoutRemoveStuff(userID,cardID,dieSteuerung.playedCards.amountCards);
                timerValue=normalTimerTime;
                ticker.startTimer();
            }else if(cards[cardID].name.equals("Sniper")){
                if(!copyCatCard) setCardClickedWithoutRemoveStuff(userID,cardID,dieSteuerung.playedCards.amountCards,getBeforePlayerID());
                else setCardClickedWithoutRemoveStuff(userID,cardID,dieSteuerung.playedCards.amountCards);
                letPlayerChoosePlayer=true;
                mayChooseHimself=false;
            }else if(cards[cardID].name.equals("Rasierklinge")){
                if(!copyCatCard) setCardClickedWithoutRemoveStuff(userID,cardID,dieSteuerung.playedCards.amountCards,getBeforePlayerID());
                else setCardClickedWithoutRemoveStuff(userID,cardID,dieSteuerung.playedCards.amountCards);
                letPlayerChoosePlayer=true;
                mayChooseHimself=false;
            }else if(cards[cardID].name.equals("Tornado")){
                if(!copyCatCard) setCardClickedWithoutRemoveStuff(userID,cardID,dieSteuerung.playedCards.amountCards,getBeforePlayerID());
                else setCardClickedWithoutRemoveStuff(userID,cardID,dieSteuerung.playedCards.amountCards);
                letPlayerChoosePlayer=true;
                mayChooseHimself=true;
            }else if(cards[cardID].name.equals("Homework Folder")){
                if(!copyCatCard) setCardClickedWithoutRemoveStuff(userID,cardID,dieSteuerung.playedCards.amountCards,getBeforePlayerID());
                else setCardClickedWithoutRemoveStuff(userID,cardID,dieSteuerung.playedCards.amountCards);
                letPlayerChoosePlayer=true;
                mayChooseHimself=false;
            }else if(cards[cardID].name.equals("Deflection")){
                setCardClickedStuff(userID,cardID,dieSteuerung.playedCards.amountCards);
                timerValue=normalTimerTime;
                ticker.stop();
                ticker.startTimer();
            }else if(cards[cardID].name.equals("No U")){
                setCardClickedStuff(userID,cardID,dieSteuerung.playedCards.amountCards);
                setCardLabel(getCurrentCardNumber(),dieSteuerung.lang.get("labelAffected",getPlayerHTMLLabel(getBeforePlayerID())));
                timerValue=normalTimerTime;
                ticker.stop();
                ticker.startTimer();
            }else if(cards[cardID].name.equals("Windwall")){
                setCardClickedStuff(userID,cardID,dieSteuerung.playedCards.amountCards);
                setCardLabel(getCurrentCardNumber(),dieSteuerung.lang.get("labelAffected",getPlayerHTMLLabel(getBeforePlayerID())));
                timerValue=normalTimerTime;
            }else if(cards[cardID].name.equals("Thunfisch")){
                if(!copyCatCard) setCardClickedWithoutRemoveStuff(userID,cardID,dieSteuerung.playedCards.amountCards,getBeforePlayerID());
                else setCardClickedWithoutRemoveStuff(userID,cardID,dieSteuerung.playedCards.amountCards);
                affectedID=userID;
                timerValue=normalTimerTime;
                ticker.startTimer();
            }else if(cards[cardID].name.equals("Verkehsamt")){
                if(!copyCatCard) setCardClickedWithoutRemoveStuff(userID,cardID,dieSteuerung.playedCards.amountCards,getBeforePlayerID());
                else setCardClickedWithoutRemoveStuff(userID,cardID,dieSteuerung.playedCards.amountCards);
                timerValue=normalTimerTime;
                ticker.startTimer();
            }else if(cards[cardID].name.equals("Well yes but actually no")){
                if(!copyCatCard) setCardClickedWithoutRemoveStuff(userID,cardID,dieSteuerung.playedCards.amountCards,getBeforePlayerID());
                else setCardClickedWithoutRemoveStuff(userID,cardID,dieSteuerung.playedCards.amountCards);
                letPlayerChoosePlayer=true;
                mayChooseHimself=true;
            }else if(cards[cardID].name.equals("Handschellen")){
                boolean cond=false;
                try{for(int i=0;i<players||cond;i++) if(!(i==userID)) cond=Math.abs(player[i].posX-player[userID].posX)<6 && Math.abs(player[i].posY-player[userID].posY)<6;}catch(Exception e){}
                if(cond){
                    if(!copyCatCard) setCardClickedWithoutRemoveStuff(userID,cardID,dieSteuerung.playedCards.amountCards,getBeforePlayerID());
                    else setCardClickedWithoutRemoveStuff(userID,cardID,dieSteuerung.playedCards.amountCards);
                    letPlayerChoosePlayer=true;
                    mayChooseHimself=false;
                }else send(userID,"notification--"+dieSteuerung.lang.get("mayNotPlayCard"));
            }else if(cards[cardID].name.equals("Missbrauch")){
                if(!copyCatCard) setCardClickedWithoutRemoveStuff(userID,cardID,dieSteuerung.playedCards.amountCards,getBeforePlayerID());
                else setCardClickedWithoutRemoveStuff(userID,cardID,dieSteuerung.playedCards.amountCards);
                letPlayerChoosePlayer=true;
                mayChooseHimself=true;
            }else send(userID,"notification--"+dieSteuerung.lang.get("mayNotPlayCard"));
            copyCatCard=false;

        }else if(cards[cardID].ownerID==userID && (!cardInUse || forceRedCard)){
            containsRedCard=false;
            if(cards[cardID].name.equals("Angel")){
                setCardClickedStuff(userID,cardID,dieSteuerung.playedCards.amountCards);
                letPlayerChoosePlayer=true;
                mayChooseHimself=false;
            }else if(cards[cardID].name.equals("Zündung!")){
                setCardClickedStuff(userID,cardID,dieSteuerung.playedCards.amountCards);
                letPlayerChoosePlayer=true;
                mayChooseHimself=false;
            }else if(cards[cardID].name.equals("Beleidigung")){
                setCardClickedStuff(userID,cardID,dieSteuerung.playedCards.amountCards);
                letPlayerChoosePlayer=true;
                mayChooseHimself=false;
            }else if(cards[cardID].name.equals("Black hole")){
                setCardClickedStuff(userID,cardID,dieSteuerung.playedCards.amountCards);
                timerValue=normalTimerTime;
                ticker.startTimer();
            }else if(cards[cardID].name.equals("Weißes Loch")){
                setCardClickedStuff(userID,cardID,dieSteuerung.playedCards.amountCards);
                timerValue=normalTimerTime;
                ticker.startTimer();
            }else if(cards[cardID].name.equals("Philipp")){
                setCardClickedStuff(userID,cardID,dieSteuerung.playedCards.amountCards);
                letPlayerChoosePlayer=true;
                mayChooseHimself=false;
            }else if(cards[cardID].name.equals("Copy Cat") && player[userID].cards>1){
                setCardClickedStuff(userID,cardID,dieSteuerung.playedCards.amountCards);
                timerValue=normalTimerTime;
                ticker.startTimer();
                copyCatUser=userID;
            }else if(cards[cardID].name.equals("Cheat Code")){
                setCardClickedStuff(userID,cardID,dieSteuerung.playedCards.amountCards);
                timerValue=normalTimerTime;
                ticker.startTimer();
            }else if(cards[cardID].name.equals("Angelhaken")){
                setCardClickedStuff(userID,cardID,dieSteuerung.playedCards.amountCards);
                letPlayerChoosePlayer=true;
                mayChooseHimself=false;
            }else if(cards[cardID].name.equals("Double XP")){
                setCardClickedStuff(userID,cardID,dieSteuerung.playedCards.amountCards);
                timerValue=normalTimerTime;
                ticker.startTimer();
            }else if(cards[cardID].name.equals("Reparatur")){
                setCardClickedStuff(userID,cardID,dieSteuerung.playedCards.amountCards);
                timerValue=normalTimerTime;
                ticker.startTimer();
            }else if(cards[cardID].name.equals("Supply drop")){
                setCardClickedStuff(userID,cardID,dieSteuerung.playedCards.amountCards);
                timerValue=normalTimerTime;
                ticker.startTimer();
            }else if(cards[cardID].name.equals("Der heilige Fisch")){
                setCardClickedStuff(userID,cardID,dieSteuerung.playedCards.amountCards);
                letPlayerChoosePlayer=true;
                mayChooseHimself=false;
            }else if(cards[cardID].name.equals("Jesus")){
                setCardClickedStuff(userID,cardID,dieSteuerung.playedCards.amountCards);
                timerValue=normalTimerTime;
                ticker.startTimer();
            }else if(cards[cardID].name.equals("Sprint")){
                setCardClickedStuff(userID,cardID,dieSteuerung.playedCards.amountCards);
                mayNotCancleCardNow=true;
                timerValue=normalTimerTime;
                ticker.startTimer();
            }else if(cards[cardID].name.equals("Großer Fang")){
                setCardClickedStuff(userID,cardID,dieSteuerung.playedCards.amountCards);
                timerValue=normalTimerTime;
                ticker.startTimer();
            }else if(cards[cardID].name.equals("Its Rewind Time")){
                setCardClickedStuff(userID,cardID,dieSteuerung.playedCards.amountCards);
                timerValue=normalTimerTime;
                ticker.startTimer();
            }else if(cards[cardID].name.equals("Sniper")){
                setCardClickedStuff(userID,cardID,dieSteuerung.playedCards.amountCards);
                letPlayerChoosePlayer=true;
                mayChooseHimself=true;
            }else if(cards[cardID].name.equals("Rasierklinge")){
                setCardClickedStuff(userID,cardID,dieSteuerung.playedCards.amountCards);
                letPlayerChoosePlayer=true;
                mayChooseHimself=true;
            }else if(cards[cardID].name.equals("Homework Folder")){
                setCardClickedStuff(userID,cardID,dieSteuerung.playedCards.amountCards);
                letPlayerChoosePlayer=true;
                mayChooseHimself=true;
            }else if(cards[cardID].name.equals("Tornado")){
                setCardClickedStuff(userID,cardID,dieSteuerung.playedCards.amountCards);
                letPlayerChoosePlayer=true;
                mayChooseHimself=true;
            }else if(cards[cardID].name.equals("Well yes but actually no")){
                setCardClickedStuff(userID,cardID,dieSteuerung.playedCards.amountCards);
                letPlayerChoosePlayer=true;
                mayChooseHimself=true;
            }else if(cards[cardID].name.equals("Wait that's illegal")){
                setCardClickedStuff(userID,cardID,dieSteuerung.playedCards.amountCards);
                affectedID=userID;
                timerValue=normalTimerTime;
                ticker.startTimer();
            }else if(cards[cardID].name.equals("Thunfisch")){
                setCardClickedStuff(userID,cardID,dieSteuerung.playedCards.amountCards);
                affectedID=userID;
                timerValue=normalTimerTime;
                ticker.startTimer();
            }else if(cards[cardID].name.equals("Verkehsamt")){
                setCardClickedStuff(userID,cardID,dieSteuerung.playedCards.amountCards);
                timerValue=normalTimerTime;
                ticker.startTimer();
            }else if(cards[cardID].name.equals("Handschellen")){
                boolean cond=false;
                try{for(int i=0;i<players||cond;i++) if(!(i==userID)) cond=Math.abs(player[i].posX-player[userID].posX)<6 && Math.abs(player[i].posY-player[userID].posY)<6;}catch(Exception e){}
                if(cond){
                    setCardClickedStuff(userID,cardID,dieSteuerung.playedCards.amountCards);
                    letPlayerChoosePlayer=true;
                    mayChooseHimself=false;hndslln2=userID;
                }else send(userID,"notification--"+dieSteuerung.lang.get("mayNotPlayCard"));
            }else if(cards[cardID].name.equals("Missbrauch")){
                setCardClickedStuff(userID,cardID,dieSteuerung.playedCards.amountCards);
                letPlayerChoosePlayer=true;
                mayChooseHimself=true;
            }else send(userID,"notification--"+dieSteuerung.lang.get("mayNotPlayCard"));

        }else if(cards[cardID].name.equals("Flash") && cardInUse && !letPlayerChoosePlayer){
            if(cardFlashable(cardInUseName[0])){
                setCardClickedStuff(userID,cardID,dieSteuerung.playedCards.amountCards);
                timerValue=normalTimerTime;
            }else send(userID,"notification--"+dieSteuerung.lang.get("mayNotPlayCard"));
        }else if(cards[cardID].name.equals("No U") && cardInUse && !letPlayerChoosePlayer){
            if(cardFlashable(cardInUseName[0])){
                setCardClickedStuff(userID,cardID,dieSteuerung.playedCards.amountCards);
                setCardLabel(getCurrentCardNumber(),dieSteuerung.lang.get("labelAffected",getPlayerHTMLLabel(getBeforePlayerID())));
                timerValue=normalTimerTime;
            }else send(userID,"notification--"+dieSteuerung.lang.get("mayNotPlayCard"));
        }else if(cards[cardID].name.equals("Fischernetz") && cardInUse && !letPlayerChoosePlayer){
            if(cardFlashable(cardInUseName[0])){
                setCardClickedStuff(userID,cardID,dieSteuerung.playedCards.amountCards);
                letPlayerChoosePlayer=true;
                mayChooseHimself=true;
                ticker.stop();
                timerValue=0;
            }else send(userID,"notification--"+dieSteuerung.lang.get("mayNotPlayCard"));
        }else if(cards[cardID].name.equals("Alternative Fakten") && cardInUse && !letPlayerChoosePlayer && player[userID].cards>1){
            setCardClickedStuff(userID,cardID,dieSteuerung.playedCards.amountCards);
            ticker.stop();
            timerValue=0;
            setCardLabel(getCurrentCardNumber(),dieSteuerung.lang.get("labelAffected",getPlayerHTMLLabel(getBeforePlayerID())));
        }else if(cards[cardID].name.equals("Differntly") && cardInUse && !letPlayerChoosePlayer && player[getCurrentPlayerID()].cards>0){
            setCardClickedStuff(userID,cardID,dieSteuerung.playedCards.amountCards);
            setCardLabel(getCurrentCardNumber(),dieSteuerung.lang.get("labelAffected",getPlayerHTMLLabel(getBeforePlayerID())));
            timerValue=normalTimerTime;
        }else if(cards[cardID].name.equals("Deflection") && cardInUse && !letPlayerChoosePlayer){
            setCardClickedStuff(userID,cardID,dieSteuerung.playedCards.amountCards);
            timerValue=normalTimerTime;
        }else if(cards[cardID].name.equals("Windwall") && cardInUse && !letPlayerChoosePlayer){
            setCardClickedStuff(userID,cardID,dieSteuerung.playedCards.amountCards);
            timerValue=normalTimerTime;
        }else if(cards[cardID].name.equals("jk") && cardInUse && !letPlayerChoosePlayer && isGreenCard(getCurrentCardName())){
            setCardClickedStuff(userID,cardID,dieSteuerung.playedCards.amountCards);
            timerValue=normalTimerTime;
        }else send(userID,"notification--"+dieSteuerung.lang.get("mayNotPlayCard"));
        forceRedCard=false;
        generateStatusText();
    }

    private boolean cardFlashable(String name){
        if(name.equals("Missbrauch")||name.equals("Verkehsamt")||name.equals("Well yes but actually no")||name.equals("Tornado")||name.equals("Wait that's illegal")||name.equals("No U")||name.equals("Homework Folder")||name.equals("Sniper")||name.equals("Rasierklinge")||name.equals("Fischernetz")||name.equals("Philipp")||name.equals("Angel")||name.equals("Black hole")||name.equals("Weißes Loch")||name.equals("Zündung!")||name.equals("Beleidigung")||name.equals("Angelhaken")||name.equals("Der heilige Fisch"))
            return true;
        return false;
    }

    private void setCardClickedStuff(int userID, int cardID, int cardNr, int playedUserID){
        sum.cardPlayed(cards[cardID].langName, playedUserID, false);
        cardInUse=true;
        playedCardUserID[cardNr]=playedUserID;
        cards[cardID].ownerID=-1;
        cardInUseName[cardNr]=cards[cardID].name;
        send(userID,"removeCard--"+cards[cardID].imageName);
        broadcast("addPlayedCard--"+cards[cardID].imageName);
        cardsLeft++;player[userID].cards--;
        player[userID].removeCard(cards[cardNr]);
        setCardLabel(getCurrentCardNumber(),dieSteuerung.lang.get("labelPlayedBy",getPlayerHTMLLabel(playedUserID)));
        status(dieSteuerung.lang.get("statusCardPlayedAs",getPlayerHTMLLabel(userID),cards[cardID].langName,getPlayerHTMLLabel(playedUserID)));
    }

    private void setCardClickedStuff(int userID, int cardID, int cardNr){
        sum.cardPlayed(cards[cardID].langName, userID, false);
        cardInUse=true;
        playedCardUserID[cardNr]=userID;
        cards[cardID].ownerID=-1;
        cardInUseName[cardNr]=cards[cardID].name;
        send(userID,"removeCard--"+cards[cardID].imageName);
        broadcast("addPlayedCard--"+cards[cardID].imageName);
        cardsLeft++;player[userID].cards--;
        player[userID].removeCard(cards[cardNr]);
        setCardLabel(getCurrentCardNumber(),dieSteuerung.lang.get("labelPlayedBy",getPlayerHTMLLabel(userID)));
        status(dieSteuerung.lang.get("statusCardPlayed",getPlayerHTMLLabel(userID),cards[cardID].langName));
    }

    private void setCardClickedWithoutRemoveStuff(int userID, int cardID, int cardNr){
        sum.cardPlayed(cards[cardID].langName, userID, false);
        cardInUse=true;
        playedCardUserID[cardNr]=userID;
        cardInUseName[cardNr]=cards[cardID].name;
        broadcast("addPlayedCard--"+cards[cardID].imageName);
        setCardLabel(getCurrentCardNumber(),dieSteuerung.lang.get("labelPlayedBy",getPlayerHTMLLabel(userID)));
        status(dieSteuerung.lang.get("statusCardPlayed",getPlayerHTMLLabel(userID),cards[cardID].langName));
    }

    private void setCardClickedWithoutRemoveStuff(int userID, int cardID, int cardNr, int playedUserID){
        sum.cardPlayed(cards[cardID].langName, playedUserID, false);
        cardInUse=true;
        playedCardUserID[cardNr]=playedUserID;
        cardInUseName[cardNr]=cards[cardID].name;
        broadcast("addPlayedCard--"+cards[cardID].imageName);
        setCardLabel(getCurrentCardNumber(),dieSteuerung.lang.get("labelPlayedBy",getPlayerHTMLLabel(playedUserID)));
        status(dieSteuerung.lang.get("statusCardPlayedAs",getPlayerHTMLLabel(userID),cards[cardID].langName,getPlayerHTMLLabel(playedUserID)));
    }

    private void setCardRemoveStuff(int userID, int cardID){
        cards[cardID].ownerID=-1;
        send(userID,"removeCard--"+cards[cardID].imageName);
        cardsLeft++;player[userID].cards--;
        player[userID].putCardAway--;
    }

    private void setCardLabel(int cardNr, String label){
        broadcast("setPlayedLabel--"+cardNr+"#"+label);
    }

    private void cardFromUserToUser(int userGiveID, int userGetID, int cardID){
        send(userGiveID,"removeCard--"+cards[cardID].imageName);
        send(userGetID, "addCard--"+cards[cardID].imageName);
        player[userGiveID].cards--;
        player[userGetID].cards++;
        player[userGiveID].removeCard(cards[cardID]);
        player[userGetID].addCard(cards[cardID]);
        cards[cardID].ownerID=userGetID;
        syncCards();
    }

    private void syncCards(){
        for(int i=0;i<players;i++){
            player[i].hasCards.clear();
            for(int j=0;j<cards.length;j++){
                if(cards[j].ownerID==i)player[i].addCard(cards[j]);
            }
        }
    }

    private void userClicked(int clickerID, int clickedID){
        if(lockAffected || (playedCardUserID[getCurrentCardNumber()]==clickerID && letPlayerChoosePlayer && timerValue<=0)){
            if((!lockAffected && !mayChooseHimself) && clickerID==clickedID){
                send(clickerID,"notification--"+dieSteuerung.lang.get("mayNotSelectYourself"));
            }else{
                if(!lockAffected)affectedID=clickedID;
                setCardLabel(getCurrentCardNumber(),dieSteuerung.lang.get("labelAffected",getPlayerHTMLLabel(affectedID)));
                timerValue=normalTimerTime;
                ticker.startTimer();
                letPlayerChoosePlayer=false;
                mayChooseHimself=false;
                if(getCurrentCardName().equals("Deflection")){
                    userClicked=true;
                }
            }
        }
    }

    private void userNumInput(int userID, int number){
        if(userID==getCurrentPlayerID()){
            if(getCurrentCardName().equals("Black hole")){
                if(number>3||number<1) retryInput();
                else{
                    for(int i=0;i<players;i++){
                        if(!(i==userID)){
                            if(getTilePlace(userID)<getTilePlace(i))
                                playerWalkBack(i, number);
                            else if(getTilePlace(userID)>getTilePlace(i))
                                playerWalkForward(i, number);
                        }
                    }
                    returnToNormalGame();
                }
            }else if(getCurrentCardName().equals("Weißes Loch")){
                if(number>3||number<1) retryInput();
                else{
                    for(int i=0;i<players;i++){
                        if(!(i==userID)){
                            if(getTilePlace(userID)>getTilePlace(i))
                                playerWalkBack(i, number);
                            else if(getTilePlace(userID)<=getTilePlace(i))
                                playerWalkForward(i, number);
                        }
                    }
                    returnToNormalGame();
                }
            }else if(getCurrentCardName().equals("Angelhaken")){
                if(validNumbers.contains(String.valueOf(number))){
                    cardFromUserToUser(affectedID,userID,number);
                    returnToNormalGame();
                }else retryInput();
            }else if(getCurrentCardName().equals("Der heilige Fisch")){
                if(validNumbers.contains(String.valueOf(number))){
                    cardFromUserToUser(affectedID,userID,number);
                }else retryInput();
            }else if(getCurrentCardName().equals("Differntly")){
                if(validNumbers.contains(String.valueOf(number))){
                    letPlayerChooseHisCard=true;
                    pickCardWithoutRemove=true;
                    useCard(getBeforePlayerID(), cards[number].imageName);
                }else retryInput();
            }else if(getCurrentCardName().equals("Rasierklinge")){
                if(validNumbers.contains(String.valueOf(number))){
                    player[affectedID].putCardAway++;
                    useCard(affectedID, cards[number].name);
                    returnToNormalGame();
                }else retryInput();
            }
        }else send(userID,dieSteuerung.lang.get("notYourTurn"));
    }

    public void returnToNormalGame(){
        if(mayNotCancleCardNow) return;
        pickCardWithoutRemove=false;
        letPlayerChooseHisCard=false;
        letPlayerChoosePlayer=false;
        mayChooseHimself=false;
        cardInUse=false;
        copyCatCard=false;
        containsRedCard=false;
        userClicked=false;
        lockAffected=false;
        cardIgnoreUser=false;
        for(int i=0;i<40;i++)
            cardInUseName[i]="";
        broadcast("removeAllPlayedCards");
        ticker.stop();
        generateStatusText();
        if(playerWalkLeft<=0) status(dieSteuerung.lang.get("statusEndTurn",getPlayerHTMLLabel(playerTurn)));
        else status(dieSteuerung.lang.get("playerTurn",getPlayerHTMLLabel(playerTurn)));
        checkRedCard();
    }

    private void generateStatusText(){
        for(int i=0;i<players;i++)
            broadcast("setPlayerText--"+i+"#"+generateStatusText(i));
    }

    private String generateStatusText(int playerID){
        String message="";
        message=message+dieSteuerung.lang.get("stausPlace",getPlayerPlace(playerID));
        if(player[playerID].cards>0) message=message+dieSteuerung.lang.get("stausCards",player[playerID].cards);
        if(hndslln>0 && (playerID==hndslln1||playerID==hndslln2)) message=message+dieSteuerung.lang.get("stausHndslln",hndslln);
        if(player[playerID].skipTurns>0) message=message+dieSteuerung.lang.get("stausSkipTurns",player[playerID].skipTurns);
        if(player[playerID].walkBonus>0) message=message+dieSteuerung.lang.get("stausWalkBonus",player[playerID].walkBonus);
        if(player[playerID].walkDouble>0) message=message+dieSteuerung.lang.get("stausWalkDouble",player[playerID].walkDouble);
        if(player[playerID].useBridge>0) message=message+dieSteuerung.lang.get("statusBridge",player[playerID].useBridge);
        if(message.length()==0)message=dieSteuerung.lang.get("stausNothing");
        return message;
    }

    private int getPlayerPlace(int playerID){
        int place=1;
        for(int i=0;i<players;i++){
            if(getTilePlace(i)>getTilePlace(playerID))place++;
        }
        return place;
    }

    public void initCards(String deck){
        String input[]=dieSteuerung.boardInfo.fm.readFile("cards/"+deck+".cards");
        String splitted[];
        cards=new Card[input.length];
        for(int i=0;i<input.length;i++){
            splitted=input[i].split("#");
            cards[i]=new Card(splitted[0],splitted[1],splitted[2]);
        }
        cardsLeft=input.length;
        for(int i=0;i<40;i++){
            playedCardUserID[i]=0;
            cardInUseName[i]="";
        }
    }

    public void tickerEvent(){
        timerValue--;
        if(gameOver){
            if(timerValue<=0 && timerValue>-10){
                if(dieSteuerung.pop.yesNo(dieSteuerung.lang.get("restartConfirm"))==0){
                    ticker.stop();timerValue=-12;
                    broadcastInvert("restart");dieSteuerung.restart();
                }else timerValue=20;
            }
        }else if(cardInUse && timerValue<=0 && timerValue>-10){
            ticker.stop();timerValue=-12;
            broadcast("audio--audio/timeover.wav");
            generateStatusText();
            mayNotCancleCardNow=false;
            try{
                if(cardInUseName[dieSteuerung.playedCards.amountCards-2].equals("Copy Cat"))
                    if(cardInUseName[dieSteuerung.playedCards.amountCards-3].equals("Alternative Fakten"))
                        playedCardUserID[dieSteuerung.playedCards.amountCards-1]=playedCardUserID[dieSteuerung.playedCards.amountCards-4];
            }catch(Exception e){}
            sum.cardPlayed(cardInUseName[dieSteuerung.playedCards.amountCards-1], -1, true);
            if(cardInUseName[dieSteuerung.playedCards.amountCards-1].equals("Angel")){
                player[affectedID].posX = player[getCurrentPlayerID()].posX;
                player[affectedID].posY = player[getCurrentPlayerID()].posY;
                broadcast("setPlayerPos--"+affectedID+"#"+player[affectedID].posX+"#"+player[affectedID].posY);
                walkOnTileEffect(affectedID, player[affectedID].posX, player[affectedID].posY);
                returnToNormalGame();
            }else if(cardInUseName[dieSteuerung.playedCards.amountCards-1].equals("Zündung!")){
                playerWalkBack(affectedID, 3);
                returnToNormalGame();
            }else if(cardInUseName[dieSteuerung.playedCards.amountCards-1].equals("Beleidigung")){
                player[affectedID].addSkipTurns(2);
                returnToNormalGame();
            }else if(cardInUseName[dieSteuerung.playedCards.amountCards-1].equals("Flash")){
                playerWalkDisplay(playedCardUserID[dieSteuerung.playedCards.amountCards-1],player[playedCardUserID[dieSteuerung.playedCards.amountCards-1]].posX, player[playedCardUserID[dieSteuerung.playedCards.amountCards-1]].posY, 3, true);
            }else if(cardInUseName[dieSteuerung.playedCards.amountCards-1].equals("Fischernetz")){
                player[affectedID].addSkipTurns(1);
            }else if(cardInUseName[dieSteuerung.playedCards.amountCards-1].equals("Black hole")){
                status(dieSteuerung.lang.get("statusEnterNumber",getPlayerHTMLLabel(playerTurn)));
                playerChooseNumber=true;
                send(getCurrentPlayerID(),"input--"+dieSteuerung.lang.get("popChooseNumBetween",1,3));
            }else if(cardInUseName[dieSteuerung.playedCards.amountCards-1].equals("Weißes Loch")){
                status(dieSteuerung.lang.get("statusEnterNumber",getPlayerHTMLLabel(playerTurn)));
                playerChooseNumber=true;
                send(getCurrentPlayerID(),"input--"+dieSteuerung.lang.get("popChooseNumBetween",1,3));
            }else if(cardInUseName[dieSteuerung.playedCards.amountCards-1].equals("Philipp")){
                dummy1 = player[getCurrentPlayerID()].posX;
                dummy2 = player[getCurrentPlayerID()].posY;
                player[getCurrentPlayerID()].posX = player[affectedID].posX;
                player[getCurrentPlayerID()].posY = player[affectedID].posY;
                player[affectedID].posX = dummy1;
                player[affectedID].posY = dummy2;
                broadcast("setPlayerPos--"+affectedID+"#"+player[affectedID].posX+"#"+player[affectedID].posY);
                broadcast("setPlayerPos--"+getCurrentPlayerID()+"#"+player[getCurrentPlayerID()].posX+"#"+player[getCurrentPlayerID()].posY);
                walkOnTileEffect(affectedID, player[affectedID].posX, player[affectedID].posY);
                walkOnTileEffect(getCurrentPlayerID(), player[getCurrentPlayerID()].posX, player[getCurrentPlayerID()].posY);
                returnToNormalGame();
            }else if(cardInUseName[dieSteuerung.playedCards.amountCards-1].equals("Copy Cat")){
                copyCatCard=true;
            }else if(cardInUseName[dieSteuerung.playedCards.amountCards-1].equals("Cheat Code")){
                for(int i=2;i<8;i++)
                    playerWalkDisplay(getCurrentPlayerID(), player[getCurrentPlayerID()].posX, player[getCurrentPlayerID()].posY, i,false);
            }else if(cardInUseName[dieSteuerung.playedCards.amountCards-1].equals("Thunfisch")){
                for(int i=2;i<5;i++)
                    playerWalkDisplay(getCurrentPlayerID(), player[getCurrentPlayerID()].posX, player[getCurrentPlayerID()].posY, i,false);
            }else if(cardInUseName[dieSteuerung.playedCards.amountCards-1].equals("Angelhaken")){
                syncCards();
                String[] pickCards=new String[player[affectedID].cards];
                for(int i=0;i<player[affectedID].cards;i++){
                    pickCards[i]=player[affectedID].hasCards.get(i).name;
                }
                playerChooseNumber=true;
                pickCardFromArray(pickCards);
            }else if(cardInUseName[dieSteuerung.playedCards.amountCards-1].equals("Double XP")){
                player[getCurrentPlayerID()].addRollTwice(1);
                playerWalkLeft++;
                returnToNormalGame();
            }else if(cardInUseName[dieSteuerung.playedCards.amountCards-1].equals("Reparatur")){
                player[getCurrentPlayerID()].addUseBridge(1);
                returnToNormalGame();
            }else if(cardInUseName[dieSteuerung.playedCards.amountCards-1].equals("Supply drop")){
                pickCard(getCurrentPlayerID(), 3);
                player[getCurrentPlayerID()].addSkipTurns(1);
                returnToNormalGame();
            }else if(cardInUseName[dieSteuerung.playedCards.amountCards-1].equals("Großer Fang")){
                pickCard(getCurrentPlayerID(), 2);
                returnToNormalGame();
            }else if(cardInUseName[dieSteuerung.playedCards.amountCards-1].equals("Der heilige Fisch")){
                syncCards();
                String[] pickCards=new String[player[affectedID].cards];
                for(int i=0;i<player[affectedID].cards;i++){
                    pickCards[i]=player[affectedID].hasCards.get(i).name;
                }
                playerChooseNumber=true;
                pickCardFromArray(pickCards);
                chooseCard="choose";
            }else if(cardInUseName[dieSteuerung.playedCards.amountCards-1].equals("Jesus")){
                player[getCurrentPlayerID()].addWalkDouble(1);
                player[getCurrentPlayerID()].addUseBridge(1);
                player[getCurrentPlayerID()].useBridgeRemoveable=false;
                returnToNormalGame();
            }else if(cardInUseName[dieSteuerung.playedCards.amountCards-1].equals("Sprint")){
                player[getCurrentPlayerID()].nextRollAmount=20;
                returnToNormalGame();
            }else if(cardInUseName[dieSteuerung.playedCards.amountCards-1].equals("Differntly")){
                letPlayerChooseHisCard=true;
                pickCardWithoutRemove=true;
                syncCards();
                String[] pickCards=new String[player[getBeforePlayerID()].cards];
                for(int i=0;i<player[getBeforePlayerID()].cards;i++){
                    pickCards[i]=player[getBeforePlayerID()].hasCards.get(i).name;
                }
                playerChooseNumber=true;
                pickCardFromArray(pickCards);
            }else if(cardInUseName[dieSteuerung.playedCards.amountCards-1].equals("Its Rewind Time")){
                int newPos[]=getPlayerPosition(playerTurn, getCurrentPlayerID());
                player[getCurrentPlayerID()].posX = newPos[0];
                player[getCurrentPlayerID()].posY = newPos[1];
                broadcast("setPlayerPos--"+getCurrentPlayerID()+"#"+player[getCurrentPlayerID()].posX+"#"+player[getCurrentPlayerID()].posY);
                walkOnTileEffect(getCurrentPlayerID(), player[getCurrentPlayerID()].posX, player[getCurrentPlayerID()].posY);
                returnToNormalGame();
            }else if(cardInUseName[dieSteuerung.playedCards.amountCards-1].equals("Sniper")){
                player[affectedID].addSkipTurns(1);
                if(player[affectedID].cards>0)player[affectedID].putCardAway++;
                playerWalkBack(affectedID, 1);
                returnToNormalGame();
            }else if(cardInUseName[dieSteuerung.playedCards.amountCards-1].equals("Fischernetz")){
                player[affectedID].addSkipTurns(1);
                returnToNormalGame();
            }else if(cardInUseName[dieSteuerung.playedCards.amountCards-1].equals("Rasierklinge")){
                syncCards();
                String[] pickCards=new String[player[affectedID].cards];
                for(int i=0;i<player[affectedID].cards;i++){
                    pickCards[i]=player[affectedID].hasCards.get(i).name;
                }
                playerChooseNumber=true;
                pickCardFromArray(pickCards);
            }else if(cardInUseName[dieSteuerung.playedCards.amountCards-1].equals("Deflection") && !userClicked){
                letPlayerChoosePlayer=true;mayChooseHimself=true;
            }else if(cardInUseName[dieSteuerung.playedCards.amountCards-1].equals("Deflection") && userClicked){
                lockAffected=true;pickCardWithoutRemove=true;letPlayerChooseHisCard=true;cardIgnoreUser=true;
                if(isReplayCard(getBeforeCardName()))
                    useCard(getBeforePlayerID(),getBefBefCardName());
                else useCard(getBeforePlayerID(),cardInUseName[dieSteuerung.playedCards.amountCards-2]);
                userClicked(getBeforePlayerID(), affectedID);
            }else if(cardInUseName[dieSteuerung.playedCards.amountCards-1].equals("No U")){
                lockAffected=true;pickCardWithoutRemove=true;letPlayerChooseHisCard=true;cardIgnoreUser=true;
                affectedID=getBeforePlayerID();
                useCard(getBeforePlayerID(),cardInUseName[dieSteuerung.playedCards.amountCards-2]);
                userClicked(getBeforePlayerID(), affectedID);
            }else if(cardInUseName[dieSteuerung.playedCards.amountCards-1].equals("Homework Folder")){
                player[affectedID].posX = dieSteuerung.boardInfo.prisonX;
                player[affectedID].posY = dieSteuerung.boardInfo.prisonY;
                broadcast("setPlayerPos--"+affectedID+"#"+player[affectedID].posX+"#"+player[affectedID].posY);
                walkOnTileEffect(affectedID, player[affectedID].posX, player[affectedID].posY);
                returnToNormalGame();
            }else if(cardInUseName[dieSteuerung.playedCards.amountCards-1].equals("Wait that's illegal")){
                player[affectedID].posX = dieSteuerung.boardInfo.prisonX;
                player[affectedID].posY = dieSteuerung.boardInfo.prisonY;
                broadcast("setPlayerPos--"+affectedID+"#"+player[affectedID].posX+"#"+player[affectedID].posY);
                walkOnTileEffect(affectedID, player[affectedID].posX, player[affectedID].posY);
                returnToNormalGame();
            }else if(cardInUseName[dieSteuerung.playedCards.amountCards-1].equals("Tornado")){
                syncCards();
                int tornadoCards=player[affectedID].cards;
                for(int i=0;i<cards.length;i++)
                    if(cards[i].ownerID==affectedID)
                        putCardAway(affectedID,i);
                pickCard(affectedID, tornadoCards);
                syncCards();
                returnToNormalGame();
            }else if(cardInUseName[dieSteuerung.playedCards.amountCards-1].equals("Well yes but actually no")){
                player[affectedID].addSkipTurns(3);
                pickCard(affectedID, 1);
                returnToNormalGame();
            }else if(cardInUseName[dieSteuerung.playedCards.amountCards-1].equals("Verkehsamt")){
                for(int i=0;i<players;i++){
                    if(dieSteuerung.boardInfo.isAltPath[player[i].posX][player[i].posY]){
                        player[i].posX = dieSteuerung.boardInfo.prisonX;
                        player[i].posY = dieSteuerung.boardInfo.prisonY;
                        broadcast("setPlayerPos--"+i+"#"+player[i].posX+"#"+player[i].posY);
                        walkOnTileEffect(i, player[i].posX, player[i].posY);
                    }
                }
                returnToNormalGame();
            }else if(cardInUseName[dieSteuerung.playedCards.amountCards-1].equals("Windwall")){
                returnToNormalGame();
            }else if(cardInUseName[dieSteuerung.playedCards.amountCards-1].equals("jk")){
                if(isReplayCard(getBefBefCardName())){
                    int cardID=getCardByImageName(getBefBefBefCardName());
                    if(cardID==-1) cardID=getCardByName(getBefBefBefCardName());
                    if(getBefBefBefPlayerID()==getCurrentPlayerID())
                        pickSpecificCard(getCurrentPlayerID(),cardID);
                    else send(getCurrentPlayerID(),"notification--"+dieSteuerung.lang.get("mayNotGetThatCard",getBefBefBefCardName()));
                }else{
                    int cardID=getCardByImageName(getBefBefCardName());
                    if(cardID==-1) cardID=getCardByName(getBefBefCardName());
                    if(getBefBefPlayerID()==getCurrentPlayerID())
                        pickSpecificCard(getCurrentPlayerID(),cardID);
                    else send(getCurrentPlayerID(),"notification--"+dieSteuerung.lang.get("mayNotGetThatCard",getBefBefCardName()));
                }
                returnToNormalGame();
            }else if(cardInUseName[dieSteuerung.playedCards.amountCards-1].equals("Handschellen")){
                if(Math.abs(player[affectedID].posX-player[hndslln2].posX)<6 && Math.abs(player[affectedID].posY-player[hndslln2].posY)<6){
                    hndslln1=affectedID;hndslln=4;
                    returnToNormalGame();
                }else{
                    letPlayerChoosePlayer=true;mayChooseHimself=false;
                    send(getCurrentPlayerID(),"notification--"+dieSteuerung.lang.get("mayNotSelectPlayer"));
                }
            }else if(cardInUseName[dieSteuerung.playedCards.amountCards-1].equals("Missbrauch")){
                playerWalkBack(affectedID, 3);
                player[affectedID].addSkipTurns(2);
                returnToNormalGame();
            }
        }else if(timerValue>0){
            try{broadcast("setPlayerText--"+playedCardUserID[dieSteuerung.playedCards.amountCards-1]+"#"+dieSteuerung.lang.get("stausTimeToReact")+timerValue);
                broadcast("audio--audio/tick.wav");}catch(Exception e){}
        }
    }

    private void pickSpecificCard(int playerID, int cardID){
        player[playerID].addCard(cards[cardID]);
        cards[cardID].ownerID=playerID;
        send(playerID, "addCard--"+cards[cardID].imageName);
        cardsLeft--;player[playerID].cards++;
    }

    private void putCardAway(int playerID, int cardID){
        player[playerID].removeCard(cards[cardID]);
        cards[cardID].ownerID=-1;
        send(playerID, "removeCard--"+cards[cardID].imageName);
        cardsLeft++;player[playerID].cards--;
    }

    private void endGame(int userID){
        broadcast("notification--<html>"+getPlayerHTMLLabel(userID)+dieSteuerung.lang.get("winGame"));
        sum.add(player[userID].name+dieSteuerung.lang.get("winGame"));
        broadcast("panel--"+sum.getStringToDisplay());
        gameOver=true;
        timerValue=10;
        ticker.startTimer();
    }

    private void retryInput(){
        send(getCurrentPlayerID(),"input--"+dieSteuerung.lang.get("popRetry"));
    }

    private int getCurrentPlayerID(){
        return playedCardUserID[dieSteuerung.playedCards.amountCards-1];
    }

    private int getBeforePlayerID(){
        try{return playedCardUserID[dieSteuerung.playedCards.amountCards-2];}catch(Exception e){return 0;}
    }

    private int getBefBefPlayerID(){
        try{return playedCardUserID[dieSteuerung.playedCards.amountCards-3];}catch(Exception e){return 0;}
    }

    private int getBefBefBefPlayerID(){
        try{return playedCardUserID[dieSteuerung.playedCards.amountCards-4];}catch(Exception e){return 0;}
    }

    private String getCurrentCardName(){
        try{
            return cardInUseName[dieSteuerung.playedCards.amountCards-1];
        }catch(Exception e){return "";}
    }

    private String getBeforeCardName(){
        try{return cardInUseName[dieSteuerung.playedCards.amountCards-2];}catch(Exception e){return "";}
    }

    private String getBefBefCardName(){
        try{return cardInUseName[dieSteuerung.playedCards.amountCards-3];}catch(Exception e){return "";}
    }

    private String getBefBefBefCardName(){
        try{return cardInUseName[dieSteuerung.playedCards.amountCards-4];}catch(Exception e){return "";}
    }

    private int getCurrentCardNumber(){
        try{
            return dieSteuerung.playedCards.amountCards-1;
        }catch(Exception e){return -1;}
    }

    private int getTilePlace(int playerID){
        return dieSteuerung.boardInfo.tilePlace[player[playerID].posX][player[playerID].posY];
    }

    private String getPlayerHTMLLabel(int i){
        return "<b style=\"color:rgb("+gPC(i).getRed()+", "+gPC(i).getGreen()+", "+gPC(i).getBlue()+");\">"+player[i].name+"</b>";
    }

    private Color gPC(int userID){ //get player color
        return dieSteuerung.playerColor[userID];
    }

    private boolean playerPutCardAway(){
        for(int i=0;i<players;i++) if(player[i].putCardAway>0)return true;
        return false;
    }

    public String getPlayerName(int playerID){
        return player[playerID].name;
    }

    public void displaySum(){
        sum.display();
    }

    public void log(String text){
        try{sum.log(text);}catch(Exception e){}
    }

    public void displayLog(){
        sum.displayLog();
    }

    private void status(String text){
        broadcast("setStatus--"+text);
    }

    private void savePlayerPositions(int currentPlayer){
        for(int i=0;i<players;i++){
            playerPos[currentPlayer][i][0]=player[i].posX;
            playerPos[currentPlayer][i][1]=player[i].posY;
        }
    }

    private int[] getPlayerPosition(int currentPlayer, int player){
        return new int[]{playerPos[currentPlayer][player][0], playerPos[currentPlayer][player][1]};
    }

    private void ee(int x, int y, int i){
        if(player[i].ee==true && x==4 && y==5 && !playerWalking &&!cardInUse) send(i, "ee");
        if(x==0 && (y==0 || y==1))player[i].ee=true; else player[i].ee=false;
    }
}