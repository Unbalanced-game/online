
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

public class GuiPopup extends JFrame {
    private JLabel l_message;
    int currentOpacity=100;
    String filename;
    Steuerung dieSteuerung;
    boolean incHeight;

    public GuiPopup(String text, Steuerung pDieSteuerung, boolean pIncHeight){
        incHeight=pIncHeight;
        dieSteuerung=pDieSteuerung;
        Toolkit.getDefaultToolkit().beep();
        setAlwaysOnTop(true);
        setFocusableWindowState(false);
        this.setTitle("Display");
        this.setSize(266,53);

        JPanel contentPane = new JPanel(null);
        contentPane.setPreferredSize(new Dimension(266,53));
        contentPane.setBackground(new Color(255,255,255));
        setUndecorated(true);

        l_message = new JLabel();
        l_message.setBounds(8,4,253,44);
        l_message.setBackground(new Color(214,217,223));
        l_message.setForeground(new Color(0,0,0));
        l_message.setEnabled(true);
        l_message.setFont(new Font("sansserif",0,12));
        l_message.setText(text);
        l_message.setVisible(true);
        l_message.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    close();
                }
            });

        contentPane.add(l_message);

        this.add(contentPane);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.pack();

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice defaultScreen = ge.getDefaultScreenDevice();
        Rectangle rect = defaultScreen.getDefaultConfiguration().getBounds();
        int x = (int) rect.getMaxX() - getWidth()-8;
        int y = (int) rect.getMaxY() - getHeight()-8;
        if(dieSteuerung.amountPopupsOpen<0)dieSteuerung.amountPopupsOpen=0;
        y=y-(dieSteuerung.amountPopupsOpen*60);
        if(y<=50){y=(int) rect.getMaxY() - getHeight()-8;x=x-275;}
        setLocation(x, y);

        if(incHeight)dieSteuerung.amountPopupsOpen++;
        this.setVisible(true);
        Thread opacity = new Thread() {
                public void run() {
                    try{Thread.sleep(3000);}catch(Exception e){}
                    for(currentOpacity=currentOpacity;currentOpacity>0;currentOpacity--){
                        try{Thread.sleep(40);}catch(Exception e){}
                        setOpacity(currentOpacity*0.01f);
                    }
                    if(incHeight)dieSteuerung.amountPopupsOpen--;
                    dispose();
                }  
            };
        opacity.start();
        dieSteuerung.audio.playSound("audio/notification.wav");
    }

    private void close(){
        for(currentOpacity=currentOpacity;currentOpacity>0;currentOpacity--){
            try{Thread.sleep(5);}catch(Exception e){}
            setOpacity(currentOpacity*0.01f);
        }
        if(incHeight)dieSteuerung.amountPopupsOpen--;
        dispose();
    }
}