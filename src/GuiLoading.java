
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
import javax.imageio.ImageIO;
import java.io.*;
import java.util.*;

public class GuiLoading extends JFrame {
    private JMenuBar menuBar;
    private JLabel l_animation;
    private int ee=0;

    public GuiLoading(){
        this.setTitle("Loading | UNBALANCED");
        this.setSize(100,100);
        setUndecorated(true);

        JPanel contentPane = new JPanel(null);
        contentPane.setPreferredSize(new Dimension(100,100));
        contentPane.setBackground(new Color(255,255,255));
        setIconImage(new ImageIcon("images/icons/icon.png").getImage());

        l_animation = new JLabel();
        l_animation.setBounds(0,0,100,100);
        l_animation.setBackground(new Color(214,217,223));
        l_animation.setForeground(new Color(0,0,0));
        l_animation.setEnabled(true);
        l_animation.setFont(new Font("sansserif",0,12));
        l_animation.setText("");
        l_animation.setVisible(true);
        l_animation.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    clicked();
                }

            });

        contentPane.add(l_animation);

        this.add(contentPane);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.pack();
        this.setVisible(true);

        try{
            ImageIcon ii = new ImageIcon(this.getClass().getResource("images/icons/loading.gif"));
            l_animation.setIcon(ii);
        }catch(Exception e){}
    }

    private void clicked(){
        ee++;
        if(ee==10){ee();Audio.playSound("audio/ee.wav");}
        else if(ee<10) Audio.playSound("audio/ee"+(((int)(ee/2))+1)+".wav");
    }

    private void ee(){
        Desktop desktop = Desktop.getDesktop();
        File file = new File("images/YanWittmann.png");
        try{if(file.exists()) desktop.open(file);}catch(Exception e){}
    }

}