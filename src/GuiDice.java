
import javax.swing.UIManager.LookAndFeelInfo;
import java.awt.*;
import java.awt.event.*;
import javax.swing.border.Border;
import javax.swing.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.*;

public class GuiDice extends JFrame implements KeyListener{
    private JMenuBar menuBar;
    private JLabel l_image,l_endTurn;
    Audio audio=new Audio();
    Steuerung dieSteuerung;

    public GuiDice(Steuerung pDieSteuerung){
        dieSteuerung=pDieSteuerung;
        setUndecorated(true);

        FrameDragListener frameDragListener = new FrameDragListener(this);
        addMouseListener(frameDragListener);
        addMouseMotionListener(frameDragListener);

        this.setTitle("Dice | UNBALANCED");
        this.setSize(125,190);

        JPanel contentPane = new JPanel(null);
        contentPane.setPreferredSize(new Dimension(125,190));
        contentPane.setBackground(new Color(255,255,255));
        setIconImage(dieSteuerung.icon.getImage());

        l_image = new JLabel();
        l_image.setBounds(24,29,75,75);
        l_image.setBackground(new Color(214,217,223));
        l_image.setForeground(new Color(0,0,0));
        l_image.setEnabled(true);
        l_image.setFont(new Font("sansserif",0,12));
        l_image.setText("");
        l_image.setVisible(true);
        l_image.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    roll();
                }

            });
        contentPane.add(l_image);

        l_endTurn = new JLabel();
        l_endTurn.setBounds(24,110,75,75);
        l_endTurn.setBackground(new Color(214,217,223));
        l_endTurn.setForeground(new Color(0,0,0));
        l_endTurn.setEnabled(true);
        l_endTurn.setFont(new Font("sansserif",0,12));
        l_endTurn.setText("");
        l_endTurn.setVisible(true);
        l_endTurn.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    dieSteuerung.sendToServer("gm--endTurn");
                }

            });
        contentPane.add(l_endTurn);

        this.add(contentPane);
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.pack();
        this.setVisible(false);
        setLocation(dieSteuerung.board.getX()+dieSteuerung.board.getWidth()+20, dieSteuerung.board.getY()+(dieSteuerung.board.getHeight()/2)-80);

        try{
            BufferedImage tile = ImageIO.read(new File("images/dice/1.png"));
            l_image.setIcon(getScaledImage(new ImageIcon(tile),75,75));
        }catch(Exception e){}
        try{
            BufferedImage tile = ImageIO.read(new File("images/icons/endturn.png"));
            l_endTurn.setIcon(getScaledImage(new ImageIcon(tile),75,75));
        }catch(Exception e){}

        addKeyListener(new KeyAdapter() {
                public void keyTyped(KeyEvent evt) {
                    keyPressed2(evt);
                }
            });
    }

    public void roll(){
        dieSteuerung.sendToServer("gm--rollDice");
    }

    public void setDice(String filename){
        audio.playSound("audio/diceroll.wav");
        if(Integer.parseInt(filename)>9)filename="bigger";
        try{
            BufferedImage tile = ImageIO.read(new File("images/dice/"+filename+".png"));
            l_image.setIcon(getScaledImage(new ImageIcon(tile),75,75));
        }catch(Exception e){}
    }

    //https://stackoverflow.com/questions/6714045/how-to-resize-jlabel-imageicon; Thanks to trolologuy!
    private ImageIcon getScaledImage(ImageIcon srcImg, int w, int h){
        Image image = srcImg.getImage();
        Image newimg = image.getScaledInstance(w, h,  java.awt.Image.SCALE_SMOOTH);
        return new ImageIcon(newimg);
    }
    
    private void keyPressed2(KeyEvent evt){
        if(evt.getKeyChar()=='s'){
            dieSteuerung.game.displaySum();
        }else if(evt.getKeyChar()=='c'){
            dieSteuerung.game.mayNotCancleCardNow=false;
            dieSteuerung.game.returnToNormalGame();
        }else if(evt.getKeyChar()=='l'){
            dieSteuerung.game.displayLog();
        }
    }

    public static class FrameDragListener extends MouseAdapter{
        private final JFrame frame;
        private Point mouseDownCompCoords = null;

        public FrameDragListener(JFrame frame) {
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
            frame.setLocation(currCoords.x - mouseDownCompCoords.x, currCoords.y - mouseDownCompCoords.y);
        }
    }
    
    public void keyTyped(KeyEvent e){
    }
    
    public void keyPressed(KeyEvent e){
    }
    
    public void keyReleased(KeyEvent e){
    }
}