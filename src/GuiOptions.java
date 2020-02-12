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

public class GuiOptions extends JFrame{
    private JButton b_confirm, b_lang;
    private JLabel l_label[];
    private JTextField tf_input[];
    Steuerung dieSteuerung;
    int options=3;

    public GuiOptions(Steuerung dieSteuerung){
        this.dieSteuerung = dieSteuerung;
        this.setTitle("Options | UNBALANCED");
        this.setSize(453,211);

        JPanel contentPane = new JPanel(null);
        contentPane.setPreferredSize(new Dimension(453,211));
        contentPane.setBackground(new Color(255,255,255));

        b_confirm = new JButton();
        b_confirm.setBounds(329,28,90,35);
        b_confirm.setBackground(new Color(214,217,223));
        b_confirm.setForeground(new Color(0,0,0));
        b_confirm.setEnabled(true);
        b_confirm.setFont(new Font("sansserif",0,12));
        b_confirm.setText(dieSteuerung.lang.get("optConfirm"));
        b_confirm.setVisible(true);
        b_confirm.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent evt){
                    confirm();dispose();
                }
            });
        contentPane.add(b_confirm);

        b_lang = new JButton();
        b_lang.setBounds(329,75,90,35);
        b_lang.setBackground(new Color(214,217,223));
        b_lang.setForeground(new Color(0,0,0));
        b_lang.setEnabled(true);
        b_lang.setFont(new Font("sansserif",0,12));
        b_lang.setText(dieSteuerung.lang.get("optLang"));
        b_lang.setVisible(true);
        b_lang.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent evt){
                    dieSteuerung.lang.selectLanguagePopup(true);
                    dieSteuerung.restart();
                }
            });
        contentPane.add(b_lang);
        
        l_label=new JLabel[options];
        for(int i=0;i<options;i++){
            l_label[i] = new JLabel();
            l_label[i].setBounds(130,28+i*40,200,35);
            l_label[i].setBackground(new Color(214,217,223));
            l_label[i].setForeground(new Color(0,0,0));
            l_label[i].setEnabled(true);
            l_label[i].setFont(new Font("sansserif",0,12));
            l_label[i].setText(dieSteuerung.lang.get("optLabel"+i));
            l_label[i].setVisible(true);
            contentPane.add(l_label[i]);
        }

        tf_input=new JTextField[options];
        for(int i=0;i<options;i++){
            tf_input[i] = new JTextField();
            tf_input[i].setBounds(25,28+i*40,90,35);
            tf_input[i].setBackground(new Color(255,255,255));
            tf_input[i].setForeground(new Color(0,0,0));
            tf_input[i].setEnabled(true);
            tf_input[i].setFont(new Font("sansserif",0,12));
            tf_input[i].setText("");
            tf_input[i].setVisible(true);
            contentPane.add(tf_input[i]);
        }
        tf_input[0].setText(dieSteuerung.config.getOptionString("timerTime"));
        tf_input[1].setText(dieSteuerung.config.getOptionString("diceSides"));
        tf_input[2].setText(dieSteuerung.config.getOptionString("startCards"));

        this.add(contentPane);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.pack();
        this.setVisible(true);
    }

    private void confirm(){
        dieSteuerung.config.setOption("timerTime", tf_input[0].getText());
        dieSteuerung.config.setOption("diceSides", tf_input[1].getText());
        dieSteuerung.config.setOption("startCards", tf_input[2].getText());
    }
}