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

public class GuiStatus extends JFrame {
    private JLabel l_text;
    private boolean cond=false;
    private int length=40;

    public GuiStatus(){

        this.setTitle("Status | UNBALANCED");
        this.setSize(403,96);
        setUndecorated(true);
        
        FrameDragListener frameDragListener = new FrameDragListener(this);
        addMouseListener(frameDragListener);
        addMouseMotionListener(frameDragListener);

        JPanel contentPane = new JPanel(null);
        contentPane.setPreferredSize(new Dimension(403,96));
        contentPane.setBackground(new Color(255,255,255));
        setIconImage(new ImageIcon("images/icons/icon.png").getImage());

        l_text = new JLabel();
        l_text.setBounds(15,5,373,82);
        l_text.setBackground(new Color(214,217,223));
        l_text.setForeground(new Color(0,0,0));
        l_text.setEnabled(true);
        l_text.setFont(new Font("SansSerif",0,19));
        l_text.setText("Game start!");
        l_text.setVisible(true);

        contentPane.add(l_text);

        this.add(contentPane);
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.pack();
        this.setVisible(false);
        
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice defaultScreen = ge.getDefaultScreenDevice();
        Rectangle rect = defaultScreen.getDefaultConfiguration().getBounds();
        setLocation(130, (int)rect.getMaxY()-100);
    }

    public void set(String text){
        l_text.setText("<html>"+text+"</html>");
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
}