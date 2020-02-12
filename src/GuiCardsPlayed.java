
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
import javax.swing.border.Border;
import javax.swing.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.*;
import java.util.*;

public class GuiCardsPlayed extends JFrame {
    Steuerung dieSteuerung;
    private JMenuBar menuBar;
    private JLabel l_card[];
    private JLabel l_desc[];
    private float size=3;
    public int amountCards=0;
    private ArrayList<String> cards = new ArrayList<>();
    private ArrayList<String> highlightCards = new ArrayList<>();
    private JPanel contentPane = new JPanel(null);
    private HashMap<String, ImageIcon> cardImages = new HashMap<String, ImageIcon>();
    private ImageIcon empty = null;

    public GuiCardsPlayed(Steuerung pDieSteuerung){
        dieSteuerung=pDieSteuerung;
        setUndecorated(true);

        if(dieSteuerung.autoSize){
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice defaultScreen = ge.getDefaultScreenDevice();
            Rectangle rect = defaultScreen.getDefaultConfiguration().getBounds();
            size=(float)rect.getWidth()/640f;
        }

        this.setTitle("Cards played | UNBALANCED");
        this.setSize((int)(62.8*size*amountCards+size+size*amountCards),10000);

        contentPane.setPreferredSize(new Dimension((int)(62.8*size*1+size+size*1),(int)(107*size+size*5)));
        contentPane.setBackground(new Color(1.0f,1.0f,1.0f,1.0f));
        setBackground(new Color(1.0f,1.0f,1.0f,1.0f));
        setIconImage(dieSteuerung.icon.getImage());

        l_card=new JLabel[40];
        for(int i=0;i<40;i++){
            l_card[i] = new JLabel();
            l_card[i].setBounds((int)((i*(62.8*size)+size*2)),(int)(size*2),(int)(62.8*size),(int)(94.5*size));
            l_card[i].setBackground(new Color(214,217,223));
            l_card[i].setForeground(new Color(0,0,0));
            l_card[i].setEnabled(true);
            l_card[i].setFont(new Font("sansserif",0,12));
            l_card[i].setVisible(true);
            add(l_card[i]);
            contentPane.add(l_card[i]);
        }
        l_card[0].setText(dieSteuerung.lang.get("playedCardsDefault"));

        l_desc=new JLabel[40];
        for(int i=0;i<40;i++){
            l_desc[i] = new JLabel("", SwingConstants.CENTER);
            l_desc[i].setBounds((int)((i*(62.8*size)+size*2)),(int)(size*2+90*size+size*5),(int)(62.8*size),(int)(15*size));
            l_desc[i].setBackground(new Color(214,217,223));
            l_desc[i].setForeground(new Color(0,0,0));
            l_desc[i].setEnabled(true);
            l_desc[i].setFont(new Font("sansserif",0,12));
            l_desc[i].setVisible(true);
            l_desc[i].setText("<html>");
            add(l_desc[i]);
            contentPane.add(l_desc[i]);
        }

        this.add(contentPane);
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.pack();
        this.setVisible(false);

        FrameDragListener frameDragListener = new FrameDragListener(this,getY());
        addMouseListener(frameDragListener);
        addMouseMotionListener(frameDragListener);

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice defaultScreen = ge.getDefaultScreenDevice();
        Rectangle rect = defaultScreen.getDefaultConfiguration().getBounds();
        setLocation((int)rect.getMaxX()-750, getY());

        try{
            BufferedImage emptyb = ImageIO.read(new File("images/cards/empty.png"));
            empty = getScaledImage(new ImageIcon(emptyb),(int)(62.8*size),(int)(94.5*size));
        }catch(Exception e){}
    }

    public void addCard(String name){
        cards.add(name);
        if(cardImages.containsKey(name)){
            try{
                l_card[amountCards].setIcon(cardImages.get(name));
            }catch(Exception e){}
        }else{
            try{
                BufferedImage image = ImageIO.read(new File("images/cards/"+name+".png"));
                cardImages.put(name,getScaledImage(new ImageIcon(image),(int)(62.8*size),(int)(94.5*size)));
                l_card[amountCards].setIcon(cardImages.get(name));
            }catch(Exception e){}
        }
        /*try{
        BufferedImage image = ImageIO.read(new File("images/cards/"+cards.get(amountCards)+".png"));
        l_card[amountCards].setIcon(getScaledImage(new ImageIcon(image),(int)(62.8*size),(int)(94.5*size)));
        }catch(Exception e){}*/
        l_card[amountCards].setVisible(true);
        amountCards++;
        setSize((int)(62.8*size*amountCards+size+size*amountCards),(int)((107*size+size*5)));
    }

    public void removeAllCards(){
        amountCards=0;cards.clear();
        setSize((int)(62.8*size+size+size),(int)(94.5*size+size*4));
        for(int i=0;i<40;i++){
            l_card[i].setVisible(false);
            l_desc[i].setText("<html>");
        }
        try{
            l_card[0].setIcon(empty);
        }catch(Exception e){}
    }

    public void setLabel(int cardID, String message){
        if(l_desc[cardID].getText().equals("<html>")){
            l_desc[cardID].setText(l_desc[cardID].getText()+message);
        }else{
            l_desc[cardID].setText(l_desc[cardID].getText()+"<br>"+message);
        }
    }

    //https://stackoverflow.com/questions/6714045/how-to-resize-jlabel-imageicon; Thanks to trolologuy!
    private ImageIcon getScaledImage(ImageIcon srcImg, int w, int h){
        Image image = srcImg.getImage();
        Image newimg = image.getScaledInstance(w, h,  java.awt.Image.SCALE_SMOOTH);
        return new ImageIcon(newimg);
    }

    public static class FrameDragListener extends MouseAdapter{
        private final JFrame frame;
        private Point mouseDownCompCoords = null;
        int ypos;
        public FrameDragListener(JFrame frame, int pypos) {
            ypos=pypos;
            this.frame = frame;
        }

        public void mouseReleased(MouseEvent e) {
            mouseDownCompCoords = null;
        }

        public void mousePressed(MouseEvent e) {
            mouseDownCompCoords = e.getPoint();
        }

        public void mouseDragged(MouseEvent e) {
            Point currCoords = e.getLocationOnScreen();
            frame.setLocation(currCoords.x - mouseDownCompCoords.x, ypos);
        }
    }
}