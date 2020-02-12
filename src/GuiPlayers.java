
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
import java.util.*;

public class GuiPlayers extends JFrame{
    Steuerung dieSteuerung;
    private JMenuBar menuBar;
    private JLabel l_player[],l_playerDesc[];
    private ArrayList<String> players = new ArrayList<>();
    private int size=3, amountCards=0;
    private JPanel contentPane = new JPanel(null);

    public GuiPlayers(Steuerung pDieSteuerung, int amountPlayers){
        dieSteuerung=pDieSteuerung;
        setUndecorated(true);

        this.setTitle("Players | UNBALANCED");
        this.setSize((int)62.8*size*amountCards+size+size*amountCards,10000);

        contentPane.setPreferredSize(new Dimension(300,amountPlayers*50+20));
        contentPane.setBackground(new Color(1.0f,1.0f,1.0f));
        setIconImage(dieSteuerung.icon.getImage());

        l_player=new JLabel[amountPlayers];
        for(int i=0;i<amountPlayers;i++){
            l_player[i] = new JLabel();
            l_player[i].setBounds(10,10+(50*i),280,20);
            l_player[i].setBackground(new Color(214,217,223));
            l_player[i].setForeground(new Color(0,0,0));
            l_player[i].setEnabled(true);
            l_player[i].setFont(new Font("sansserif",0,12));
            l_player[i].setText("");
            l_player[i].setVisible(true);final int ii=i;
            l_player[i].addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        clicked(ii);
                    }

                });
            add(l_player[i]);
            contentPane.add(l_player[i]);
        }

        l_playerDesc=new JLabel[amountPlayers];
        for(int i=0;i<amountPlayers;i++){
            l_playerDesc[i] = new JLabel();
            l_playerDesc[i].setBounds(10,30+(50*i),280,20);
            l_playerDesc[i].setBackground(new Color(214,217,223));
            l_playerDesc[i].setForeground(new Color(0,0,0));
            l_playerDesc[i].setEnabled(true);
            l_playerDesc[i].setFont(new Font("sansserif",0,12));
            l_playerDesc[i].setText(dieSteuerung.lang.get("playersNoDesc"));
            l_playerDesc[i].setVisible(true);final int ii=i;
            l_playerDesc[i].addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        clicked(ii);
                    }

                });
            add(l_playerDesc[i]);
            contentPane.add(l_playerDesc[i]);
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
        setLocation((int)rect.getMaxX()-300, getY());
    }

    public void addPlayer(String name){
        players.add(name);
        for(int i=0;i<players.size();i++) l_player[i].setText(players.get(i));
    }

    public void setPlayerText(int playerID, String text){
        l_playerDesc[playerID].setText(text);
    }

    private void clicked(int id){
        dieSteuerung.sendToServer("gm--clickUser--"+id);
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