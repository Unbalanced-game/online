
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

public class GuiCards extends JFrame {
    Steuerung dieSteuerung;
    private JMenuBar menuBar;
    private JLabel l_card[];
    private JLabel l_highlight[];
    private float size=3;
    public int amountCards=0;
    private ArrayList<String> cards = new ArrayList<>();
    private ArrayList<String> highlightCards = new ArrayList<>();
    private JPanel contentPane = new JPanel(null);
    private HashMap<String, ImageIcon> cardImages = new HashMap<String, ImageIcon>();

    public GuiCards(Steuerung pDieSteuerung){
        dieSteuerung=pDieSteuerung;
        setUndecorated(true);

        if(dieSteuerung.autoSize){
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice defaultScreen = ge.getDefaultScreenDevice();
            Rectangle rect = defaultScreen.getDefaultConfiguration().getBounds();
            size=(float)rect.getWidth()/640f;
        }

        this.setTitle("Cards | UNBALANCED");
        this.setSize((int)(62.8*size*amountCards+size+size*amountCards),10000);

        contentPane.setPreferredSize(new Dimension((int)(62.8*size*1+size+size*1),(int)(94.5*size+size*4)));
        contentPane.setBackground(new Color(1.0f,1.0f,1.0f,1.0f));
        setBackground(new Color(1.0f,1.0f,1.0f,1.0f));
        setIconImage(dieSteuerung.icon.getImage());

        /*l_highlight=new JLabel[40];BufferedImage highlightImage=null;
        try{
        highlightImage = ImageIO.read(new File("images/cards/highlight.png"));
        }catch(Exception e){}
        for(int i=0;i<40;i++){
        l_highlight[i] = new JLabel();
        l_highlight[i].setBounds((int)((i*(62.8*size)+size*2)),(int)(size*2),(int)(62.8*size),(int)(94.5*size));
        l_highlight[i].setBackground(new Color(214,217,223));
        l_highlight[i].setForeground(new Color(0,0,0));
        l_highlight[i].setEnabled(true);
        l_highlight[i].setFont(new Font("sansserif",0,12));
        l_highlight[i].setText("");
        l_highlight[i].setVisible(false);
        add(l_highlight[i]);
        contentPane.add(l_highlight[i]);
        l_highlight[i].setIcon(getScaledImage(new ImageIcon(highlightImage),(int)(62.8*size),(int)(94.5*size)));
        }*/

        l_card=new JLabel[40];
        for(int i=0;i<40;i++){
            l_card[i] = new JLabel();
            l_card[i].setBounds((int)(i*(62.8*size)+size*2),(int)(size*2),(int)(62.8*size),(int)(94.5*size));
            l_card[i].setBackground(new Color(214,217,223));
            l_card[i].setForeground(new Color(0,0,0));
            l_card[i].setEnabled(true);
            l_card[i].setFont(new Font("sansserif",0,12));
            l_card[i].setVisible(true);final int ii=i;
            l_card[i].addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        clicked(ii);
                    }

                });
            add(l_card[i]);
            contentPane.add(l_card[i]);
        }
        l_card[0].setText(dieSteuerung.lang.get("cardsDefault"));

        this.add(contentPane);
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.pack();
        this.setVisible(false);

        FrameDragListener frameDragListener = new FrameDragListener(this,getY());
        addMouseListener(frameDragListener);
        addMouseMotionListener(frameDragListener);

        setLocation(130, getY());
    }

    public void addCard(String name){
        cards.add(name);amountCards++;refreshCards();
    }

    public void removeCard(String name){
        if(cards.remove(name)){
            amountCards--;refreshCards();
        }
    }

    private void refreshCards(){
        for(int i=0;i<amountCards;i++){
            if(cardImages.containsKey(cards.get(i))){
                try{
                    l_card[i].setIcon(cardImages.get(cards.get(i)));
                }catch(Exception e){}
            }else{
                try{
                    BufferedImage image = ImageIO.read(new File("images/cards/"+cards.get(i)+".png"));
                    cardImages.put(cards.get(i),getScaledImage(new ImageIcon(image),(int)(62.8*size),(int)(94.5*size)));
                    l_card[i].setIcon(cardImages.get(cards.get(i)));
                }catch(Exception e){}
            }
            /*if(highlightCards.contains(cards.get(i)))
            l_highlight[i].setVisible(true);*/
        }

        if(amountCards<=0)
            try{
                BufferedImage image = ImageIO.read(new File("images/cards/empty.png"));
                l_card[0].setIcon(getScaledImage(new ImageIcon(image),(int)(62.8*size),(int)(94.5*size)));
            }catch(Exception e){}
        try{
            BufferedImage image = ImageIO.read(new File("images/cards/empty.png"));
            l_card[amountCards].setIcon(getScaledImage(new ImageIcon(image),(int)(62.8*size),(int)(94.5*size)));
        }catch(Exception e){}

        if(amountCards<=0)
            setSize((int)(62.8*size+size+size),(int)(94.5*size+size*4));
        else
            setSize((int)(62.8*size*amountCards+size+size*amountCards),(int)(94.5*size+size*4));
    }

    public void setHighlight(String cards){
        String[] highlightcards=cards.split("#");
        highlightCards.clear();
        for(int i=0;i<highlightcards.length;i++){
            highlightCards.add(highlightcards[i]);
        }
        refreshCards();
    }

    private void clicked(int id){
        try{
            dieSteuerung.sendToServer("gm--useCard--"+cards.get(id));
        }catch(Exception e){}
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