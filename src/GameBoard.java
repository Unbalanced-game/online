public class GameBoard{
    FileManager fm=new FileManager();
    int sizeX,sizeY,startX,startY,endX,endY,prisonX,prisonY;
    String tilesTypes[][];
    String comeFrom[][];
    String boardInput[];
    String possiblePaths[][];
    String bridgeEntrance[][];
    int tilePlace[][];
    boolean isAltPath[][];

    public void setBoardFromFile(String filename){
        boardInput=fm.readFile(filename);
        sizeX=Integer.parseInt(boardInput[0]);
        sizeY=Integer.parseInt(boardInput[1]);

        tilesTypes=new String[sizeX][sizeY];
        comeFrom=new String[sizeY][sizeX];
        possiblePaths=new String[sizeY][sizeX];
        bridgeEntrance=new String[sizeY][sizeX];
        tilePlace=new int[sizeX][sizeY];
        isAltPath=new boolean[sizeX][sizeY];
        for(int j=0;j<sizeY;j++){
            for(int i=0;i<sizeX;i++){
                tilesTypes[i][j]=String.valueOf(boardInput[j+2].charAt(i*2))+String.valueOf(boardInput[j+2].charAt(i*2+1));
                if(tilesTypes[i][j].equals("st")){startX=i;startY=j;}
                else if(tilesTypes[i][j].equals("en")){endX=i;endY=j;}
                else if(tilesTypes[i][j].equals("ge")){prisonX=i;prisonY=j;}
            }
            comeFrom[j]=boardInput[j+sizeY+3].split(" ");
            possiblePaths[j]=boardInput[j+sizeY+sizeY+4].split(" ");
            for(int i=0;i<sizeX;i++){
                tilePlace[i][j]=Integer.parseInt(String.valueOf(boardInput[j+sizeY+sizeY+sizeY+5].charAt(i*2))+String.valueOf(boardInput[j+sizeY+sizeY+sizeY+5].charAt(i*2+1)).replace(" ",""));
            }
            for(int i=0;i<sizeX;i++){
                isAltPath[i][j]=(boardInput[j+sizeY+sizeY+sizeY+sizeY+6].charAt(i)=='y');
            }
            bridgeEntrance[j]=boardInput[j+sizeY+sizeY+sizeY+sizeY+sizeY+7].split(" ");

            /*for(int i=0;i<sizeX;i++){
                System.out.print(bridgeEntrance[j][i]+" ");
            }
            System.out.print("\n");*/
        }
    }

    public void setBoardFromArray(String[] boardInput){
        sizeX=Integer.parseInt(boardInput[0]);
        sizeY=Integer.parseInt(boardInput[1]);

        tilesTypes=new String[sizeX][sizeY];
        comeFrom=new String[sizeY][sizeX];
        possiblePaths=new String[sizeY][sizeX];
        bridgeEntrance=new String[sizeY][sizeX];
        tilePlace=new int[sizeX][sizeY];
        isAltPath=new boolean[sizeX][sizeY];
        for(int j=0;j<sizeY;j++){
            for(int i=0;i<sizeX;i++){
                tilesTypes[i][j]=String.valueOf(boardInput[j+2].charAt(i*2))+String.valueOf(boardInput[j+2].charAt(i*2+1));
                if(tilesTypes[i][j].equals("st")){startX=i;startY=j;}
                else if(tilesTypes[i][j].equals("en")){endX=i;endY=j;}
                else if(tilesTypes[i][j].equals("ge")){prisonX=i;prisonY=j;}
            }
            comeFrom[j]=boardInput[j+sizeY+3].split(" ");
            possiblePaths[j]=boardInput[j+sizeY+sizeY+4].split(" ");
            for(int i=0;i<sizeX;i++){
                isAltPath[i][j]=(boardInput[j+sizeY+sizeY+sizeY+sizeY+6].charAt(i)=='y');
            }
        }
    }

    public String[] getInput(){
        return boardInput;
    }

    /*public void walkAlong(){
    int currentX=startX,currentY=startY;
    while(!(currentX==endX && currentY==endY)){
    if(possiblePaths[currentY][currentX].charAt(0)=='d')currentY++;
    else if(possiblePaths[currentY][currentX].charAt(0)=='u')currentY--;
    else if(possiblePaths[currentY][currentX].charAt(0)=='r')currentX++;
    else if(possiblePaths[currentY][currentX].charAt(0)=='l')currentX--;
    System.out.println(currentX+" "+currentY);
    }
    }*/
}
