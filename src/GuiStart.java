
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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class GuiStart extends JFrame{
    private JMenuBar menuBar;
    private JButton b_start;
    private JLabel lIps;
    private int players,currentPlayers=0;
    private GameManager gameManager;

    public GuiStart(GameManager gm, int pPlayers){
        gameManager=gm;
        players=pPlayers;
        this.setTitle("Waiting for players! | UNBALANCED");
        this.setSize(406,361);

        JPanel contentPane = new JPanel(null);
        contentPane.setPreferredSize(new Dimension(406,361));
        contentPane.setBackground(new Color(255,255,255));
        setIconImage(gm.dieSteuerung.icon.getImage());

        b_start = new JButton();
        b_start.setBounds(258,30,116,40);
        b_start.setBackground(new Color(214,217,223));
        b_start.setForeground(new Color(0,0,0));
        b_start.setEnabled(true);
        b_start.setFont(new Font("SansSerif",0,12));
        b_start.setText("Start game!");
        b_start.setVisible(true);
        b_start.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    start();
                }
            });

        lIps = new JLabel();
        lIps.setBounds(24,22,205,307);
        lIps.setBackground(new Color(214,217,223));
        lIps.setForeground(new Color(0,0,0));
        lIps.setEnabled(true);
        lIps.setFont(new Font("sansserif",0,12));
        lIps.setText("<html>");
        lIps.setVisible(true);

        contentPane.add(b_start);
        contentPane.add(lIps);

        this.add(contentPane);
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.pack();
        this.setVisible(false);
        
        addWindowListener(new java.awt.event.WindowAdapter(){
            @Override
            public void windowClosing(WindowEvent e){
                gameManager.dieSteuerung.restart();
            }
        });
    }


    private void start(){
        if(players<=currentPlayers){
            gameManager.gameStart();dispose();
        }else gameManager.dieSteuerung.notification(gameManager.dieSteuerung.lang.get("notEnoughPlayers",currentPlayers,players));
    }
    
    public void addPlayer(String ip, String name){
        currentPlayers++;
        lIps.setText((lIps.getText()+"<br>"+ip+":  "+name));
    }

}