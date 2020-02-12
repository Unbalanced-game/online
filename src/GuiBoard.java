
import javax.swing.UIManager.LookAndFeelInfo;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.awt.Color;
import javax.imageio.ImageIO;
import javax.swing.border.Border;
import javax.swing.*;
import java.io.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowEvent;
import java.util.HashMap;

public class GuiBoard extends JFrame{
    Steuerung dieSteuerung;
    JPanel contentPane = new JPanel(null);
    private JMenuBar menuBar;
    private JLabel l_tiles[][];
    private JLabel l_paths[][];
    public JLabel l_highlight[][];
    private JLabel l_player[];
    private HashMap<String, ImageIcon> tiles = new HashMap<String, ImageIcon>();
    GameBoard boardInfo;
    int size=60;

    public GuiBoard(GameBoard pBoardInfo, Steuerung pDieSteuerung){
        dieSteuerung=pDieSteuerung;
        boardInfo=pBoardInfo;
        //setUndecorated(true);

        if(dieSteuerung.autoSize){
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice defaultScreen = ge.getDefaultScreenDevice();
            Rectangle rect = defaultScreen.getDefaultConfiguration().getBounds();
            size=(int)rect.getWidth()/32;
        }
        int sizeX, sizeY;
        sizeX=boardInfo.sizeX;sizeY=boardInfo.sizeY;
        this.setTitle("Board | UNBALANCED");
        this.setSize(size+((size+1)*sizeX),size+((size+1)*sizeY));
        setIconImage(dieSteuerung.icon.getImage());

        contentPane.setPreferredSize(new Dimension(size+((size+1)*sizeX),size+((size+1)*sizeY)));
        contentPane.setBackground(new Color(255,255,255));
        
        l_player=new JLabel[10];
        for(int i=0;i<10;i++){
            l_player[i]=new JLabel();
            l_player[i].setVisible(true);
            contentPane.add(l_player[i]);
            add(l_player[i]);
            try{
                BufferedImage empty = ImageIO.read(new File("images/icons/empty.png"));

                Graphics2D g = (Graphics2D) empty.getGraphics();
                g.setColor(dieSteuerung.playerColor[i]);
                g.setStroke(new BasicStroke(50.0f));
                g.drawOval(25, 25, empty.getWidth()-50, empty.getHeight()-50);

                l_player[i].setIcon(getScaledImage(new ImageIcon(empty),size/2,size/2));
            }catch(Exception e){}
        }

        BufferedImage tileHighlight=null;
        try{
            tileHighlight = ImageIO.read(new File("images/tiles/highlight.png"));
        }catch(Exception e){}
        ImageIcon tile=getScaledImage(new ImageIcon(tileHighlight),size,size);
        l_highlight=new JLabel[sizeX][sizeY];
        for(int i=0;i<sizeX;i++){
            for(int j=0;j<sizeY;j++){
                l_highlight[i][j] = new JLabel();
                l_highlight[i][j].setBounds((size/2)+((size+1)*i),(size/2)+((size+1)*j),size,size);
                l_highlight[i][j].setVisible(false);
                contentPane.add(l_highlight[i][j]);
                add(l_highlight[i][j]);
                l_highlight[i][j].setIcon(tile);
            }
        }

        l_tiles=new JLabel[sizeX][sizeY];
        for(int i=0;i<sizeX;i++){
            for(int j=0;j<sizeY;j++){
                l_tiles[i][j] = new JLabel();
                l_tiles[i][j].setBounds((size/2)+((size+1)*i),(size/2)+((size+1)*j),size,size);
                l_tiles[i][j].setVisible(true);final int ii=i;final int jj=j;
                l_tiles[i][j].addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            clickedField(ii,jj);
                        }

                    });
                setIconLabel(i,j,boardInfo.tilesTypes[i][j]);
                contentPane.add(l_tiles[i][j]);
                add(l_tiles[i][j]);
            }
        }

        l_paths=new JLabel[sizeX][sizeY];
        for(int i=0;i<sizeX;i++){
            for(int j=0;j<sizeY;j++){
                for(int k=0;k<boardInfo.comeFrom[j][i].length();k++){
                    if(!boardInfo.tilesTypes[i][j].equals("br")){
                        l_paths[i][j] = new JLabel();
                        l_paths[i][j].setBounds((size/2)+((size+1)*i),(size/2)+((size+1)*j),size,size);
                        l_paths[i][j].setVisible(true);
                        setIconPath(i,j,boardInfo.comeFrom[j][i],k,false);
                        contentPane.add(l_paths[i][j]);
                        add(l_paths[i][j]);
                    }
                }
            }
        }
        for(int i=0;i<sizeX;i++){
            for(int j=0;j<sizeY;j++){
                for(int k=0;k<boardInfo.possiblePaths[j][i].length();k++){
                    if(!boardInfo.tilesTypes[i][j].equals("br")){
                        l_paths[i][j] = new JLabel();
                        l_paths[i][j].setBounds((size/2)+((size+1)*i),(size/2)+((size+1)*j),size,size);
                        l_paths[i][j].setVisible(true);
                        setIconPath(i,j,boardInfo.possiblePaths[j][i],k,true);
                        contentPane.add(l_paths[i][j]);
                        add(l_paths[i][j]);
                    }
                }
            }
        }

        this.add(contentPane);
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.pack();
        this.setVisible(false);

        setLocation(getX()-size*2,getY()+(int)(size*2.3));
        this.setResizable(false);

        addWindowListener(new java.awt.event.WindowAdapter(){
                @Override
                public void windowClosing(WindowEvent e){
                    if(dieSteuerung.pop.yesNo(dieSteuerung.lang.get("exitConfirm"))==0)
                    dieSteuerung.restart();
                    System.exit(101);
                }
            });
    }

    private void clickedField(int x, int y){
        dieSteuerung.sendToServer("gm--tileClicked--"+x+"#"+y);
    }

    private void setIconPath(int x, int y, String icon, int i, boolean comeFrom){
        if(icon.equals("  ")) l_paths[x][y].setVisible(false);
        try{
            BufferedImage tile = ImageIO.read(new File("images/tiles/path"+icon.charAt(i)+".png"));
            if(boardInfo.isAltPath[x][y])
                tile = ImageIO.read(new File("images/tiles/pathalt"+icon.charAt(i)+".png"));
            else{
                if(comeFrom){
                    if(icon.charAt(i)=='u' && boardInfo.isAltPath[x][y-1])
                        tile = ImageIO.read(new File("images/tiles/pathalt"+icon.charAt(i)+".png"));
                    else if(icon.charAt(i)=='d' && boardInfo.isAltPath[x][y+1])
                        tile = ImageIO.read(new File("images/tiles/pathalt"+icon.charAt(i)+".png"));
                    else if(icon.charAt(i)=='l' && boardInfo.isAltPath[x-1][y])
                        tile = ImageIO.read(new File("images/tiles/pathalt"+icon.charAt(i)+".png"));
                    else if(icon.charAt(i)=='r' && boardInfo.isAltPath[x+1][y])
                        tile = ImageIO.read(new File("images/tiles/pathalt"+icon.charAt(i)+".png"));
                }else{
                    if(icon.charAt(i)=='u' && boardInfo.isAltPath[x][y-1])
                        tile = ImageIO.read(new File("images/tiles/pathalt"+icon.charAt(i)+".png"));
                    else if(icon.charAt(i)=='d' && boardInfo.isAltPath[x][y+1])
                        tile = ImageIO.read(new File("images/tiles/pathalt"+icon.charAt(i)+".png"));
                    else if(icon.charAt(i)=='l' && boardInfo.isAltPath[x-1][y])
                        tile = ImageIO.read(new File("images/tiles/pathalt"+icon.charAt(i)+".png"));
                    else if(icon.charAt(i)=='r' && boardInfo.isAltPath[x+1][y])
                        tile = ImageIO.read(new File("images/tiles/pathalt"+icon.charAt(i)+".png"));
                }
            }
            l_paths[x][y].setIcon(getScaledImage(new ImageIcon(tile),size,size));
        }catch(Exception e){}
    }

    private void setIconLabeld(int x, int y, String icon){
        if(icon.equals("  ")) l_tiles[x][y].setVisible(false);
        else{
            try{
                BufferedImage tile = ImageIO.read(new File("images/tiles/"+toIcon(icon)));
                l_tiles[x][y].setIcon(getScaledImage(new ImageIcon(tile),size,size));
            }catch(Exception e){}
        }
    }

    private void setIconLabel(int x, int y, String icon){
        if(icon.equals("  ")) l_tiles[x][y].setVisible(false);
        else if(tiles.containsKey(icon)){
            try{
                l_tiles[x][y].setIcon(tiles.get(icon));
            }catch(Exception e){}
        }else{
            try{
                BufferedImage tile = ImageIO.read(new File("images/tiles/"+toIcon(icon)));
                tiles.put(icon, getScaledImage(new ImageIcon(tile),size,size));
                l_tiles[x][y].setIcon(tiles.get(icon));
            }catch(Exception e){}
        }
    }

    private String toIcon(String t){
        if(t.equals("  "))return "empty.png";
        else if(t.equals("st"))return "start.png";
        else if(t.equals("en"))return "goal.png";
        else if(t.equals("no"))return "normal.png";
        else if(t.equals("ge"))return "prison.png";
        else if(t.equals("ig"))return "toprison.png";
        else if(t.equals("br"))return "bridge.png";
        else if(t.equals("mk"))return "minuskarte.png";
        else if(t.equals("nw"))return "reroll.png";
        else if(t.contains("-"))
            return ("-"+t.replace("-", "").replace("0","10")+".png");
        else if(t.contains("k"))
            return (t+".png");
        return "";
    }

    //https://stackoverflow.com/questions/6714045/how-to-resize-jlabel-imageicon; Thanks to trolologuy!
    private ImageIcon getScaledImage(ImageIcon srcImg, int w, int h){
        Image image = srcImg.getImage();
        Image newimg = image.getScaledInstance(w, h,  java.awt.Image.SCALE_SMOOTH);
        return new ImageIcon(newimg);
    }

    public void setPlayerPosition(int player, int x, int y){
        l_player[player].setBounds((size/2)+((size+1)*x)+randomInt(0,size/2),(size/2)+((size+1)*y)+randomInt(0,size/2),size/2,size/2);
    }

    int randomInt(int min, int max){
        return min + (int)(Math.random() * ((max - min) + 1));
    }

    public void setHighlight(int x,int y,boolean enabled){
        l_highlight[x][y].setVisible(enabled);
    }

    public void ee(){
        BufferedImage tile=null;
        try{
            tile = ImageIO.read(new File("images/tiles/ee.png"));
        }catch(Exception e){}
        ImageIcon tileIcon=getScaledImage(new ImageIcon(tile),size,size);
        for(int i=0;i<boardInfo.sizeX;i++){
            for(int j=0;j<boardInfo.sizeY;j++){
                l_highlight[i][j].setIcon(tileIcon);
                l_highlight[i][j].setVisible(true);
            }
        }
    }
}