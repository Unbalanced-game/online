
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

public class GuiMenu extends JFrame{
    Steuerung dieSteuerung;
    private JMenuBar menuBar;
    private JButton b_client;
    private JButton b_server;
    private JButton b_options;
    private JComboBox cb_map;
    private JComboBox cb_deck;
    private JLabel l_deck;
    private JLabel l_map;
    private JLabel l_players;
    private JTextField tf_name;
    private JTextField tf_players;
    private JButton b_randomName;
    private JRadioButton rb_auto;
    private JRadioButton rb_IP;
    private JRadioButton rb_ID;
    private String mode;

    public GuiMenu(Steuerung dieSteuerung){
        this.dieSteuerung=dieSteuerung;
        this.setTitle("Main Menu | UNBALANCED");
        this.setSize(500,400);

        JPanel contentPane = new JPanel(null);
        contentPane.setPreferredSize(new Dimension(500,400));
        contentPane.setBackground(new Color(255,255,255));
        setIconImage(dieSteuerung.icon.getImage());

        b_client = new JButton();
        b_client.setBounds(253,98,138,39);
        b_client.setBackground(new Color(214,217,223));
        b_client.setForeground(new Color(0,0,0));
        b_client.setEnabled(true);
        b_client.setFont(new Font("sansserif",0,12));
        b_client.setText(dieSteuerung.lang.get("startAsClient"));
        b_client.setVisible(true);
        b_client.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    startAsClient();
                }
            });

        b_server = new JButton();
        b_server.setBounds(102,98,138,39);
        b_server.setBackground(new Color(214,217,223));
        b_server.setForeground(new Color(0,0,0));
        b_server.setEnabled(true);
        b_server.setFont(new Font("sansserif",0,12));
        b_server.setText(dieSteuerung.lang.get("startAsServer"));
        b_server.setVisible(true);
        b_server.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    startAsServer();
                }
            });

        b_options = new JButton();
        b_options.setBounds(190,360,100,20);
        b_options.setBackground(new Color(214,217,223));
        b_options.setForeground(new Color(0,0,0));
        b_options.setEnabled(true);
        b_options.setFont(new Font("sansserif",0,12));
        b_options.setText(dieSteuerung.lang.get("buttonOptions"));
        b_options.setVisible(true);
        b_options.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent evt){
                    try{dieSteuerung.opt.dispose();}catch(Exception e){}
                    dieSteuerung.opt=new GuiOptions(dieSteuerung);
                }
            });

        String files[]=dieSteuerung.nameGenerator.fm.getFilesWithEnding("boards/", ".board");
        for(int i=0;i<files.length;i++)
            files[i]=String.valueOf(files[i].charAt(0)).toUpperCase()+files[i].replace(".board","").substring(1,files[i].replace(".board","").length());
        cb_map = new JComboBox(new DefaultComboBoxModel(files));
        cb_map.setBounds(102,239,138,35);
        cb_map.setBackground(new Color(214,217,223));
        cb_map.setForeground(new Color(0,0,0));
        cb_map.setEnabled(true);
        cb_map.setFont(new Font("sansserif",0,12));
        cb_map.setVisible(true);
        cb_map.setSelectedItem(dieSteuerung.config.getOptionString("board"));

        files=dieSteuerung.nameGenerator.fm.getFilesWithEnding("cards/", ".cards");
        for(int i=0;i<files.length;i++)
            files[i]=String.valueOf(files[i].charAt(0)).toUpperCase()+files[i].replace(".cards","").substring(1,files[i].replace(".cards","").length());
        cb_deck = new JComboBox(new DefaultComboBoxModel(files));
        cb_deck.setBounds(102,303,138,35);
        cb_deck.setBackground(new Color(214,217,223));
        cb_deck.setForeground(new Color(0,0,0));
        cb_deck.setEnabled(true);
        cb_deck.setFont(new Font("sansserif",0,12));
        cb_deck.setVisible(true);
        cb_deck.setSelectedItem(dieSteuerung.config.getOptionString("deck"));

        l_deck = new JLabel();
        l_deck.setBounds(107,282,138,15);
        l_deck.setBackground(new Color(214,217,223));
        l_deck.setForeground(new Color(0,0,0));
        l_deck.setEnabled(true);
        l_deck.setFont(new Font("sansserif",0,12));
        l_deck.setText(dieSteuerung.lang.get("deckSelection"));
        l_deck.setVisible(true);

        l_map = new JLabel();
        l_map.setBounds(107,217,138,15);
        l_map.setBackground(new Color(214,217,223));
        l_map.setForeground(new Color(0,0,0));
        l_map.setEnabled(true);
        l_map.setFont(new Font("sansserif",0,12));
        l_map.setText(dieSteuerung.lang.get("boardSelection"));
        l_map.setVisible(true);

        l_players = new JLabel();
        l_players.setBounds(107,150,138,15);
        l_players.setBackground(new Color(214,217,223));
        l_players.setForeground(new Color(0,0,0));
        l_players.setEnabled(true);
        l_players.setFont(new Font("sansserif",0,12));
        l_players.setText(dieSteuerung.lang.get("amountPlayers"));
        l_players.setVisible(true);

        tf_name = new JTextField();
        tf_name.setBounds(177,39,134,39);
        tf_name.setBackground(new Color(255,255,255));
        tf_name.setForeground(new Color(0,0,0));
        tf_name.setEnabled(true);
        tf_name.setFont(new Font("sansserif",0,12));
        tf_name.setText(dieSteuerung.config.getOptionString("name"));
        tf_name.setVisible(true);

        b_randomName = new JButton();
        b_randomName.setBounds(324,42,88,32);
        b_randomName.setBackground(new Color(214,217,223));
        b_randomName.setForeground(new Color(0,0,0));
        b_randomName.setEnabled(true);
        b_randomName.setFont(new Font("sansserif",0,12));
        b_randomName.setText(dieSteuerung.lang.get("randomName"));
        b_randomName.setVisible(true);
        b_randomName.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    tf_name.setText(dieSteuerung.nameGenerator.generateName());
                    dieSteuerung.config.setOption("name", tf_name.getText());
                }
            });

        tf_players = new JTextField();
        tf_players.setBounds(102,175,90,35);
        tf_players.setBackground(new Color(255,255,255));
        tf_players.setForeground(new Color(0,0,0));
        tf_players.setEnabled(true);
        tf_players.setFont(new Font("sansserif",0,12));
        tf_players.setText(dieSteuerung.config.getOptionString("players"));
        tf_players.setVisible(true);

        mode=dieSteuerung.config.getOptionString("mode");
        rb_auto = new JRadioButton();
        rb_auto.setBounds(260,140,250,35);
        rb_auto.setBackground(new Color(255,255,255));
        rb_auto.setForeground(new Color(0,0,0));
        rb_auto.setEnabled(true);
        rb_auto.setFont(new Font("sansserif",0,12));
        rb_auto.setText(dieSteuerung.lang.get("connectModeAuto"));
        rb_auto.setVisible(true);
        if(mode.equals("auto")) rb_auto.setSelected(true);
        rb_auto.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    connectMode("auto");
                }
            });

        rb_IP = new JRadioButton();
        rb_IP.setBounds(260,170,250,35);
        rb_IP.setBackground(new Color(255,255,255));
        rb_IP.setForeground(new Color(0,0,0));
        rb_IP.setEnabled(true);
        rb_IP.setFont(new Font("sansserif",0,12));
        rb_IP.setText(dieSteuerung.lang.get("connectModeIP"));
        rb_IP.setVisible(true);
        if(mode.equals("ip")) rb_IP.setSelected(true);
        rb_IP.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    connectMode("ip");
                }
            });

        rb_ID = new JRadioButton();
        rb_ID.setBounds(260,200,250,35);
        rb_ID.setBackground(new Color(255,255,255));
        rb_ID.setForeground(new Color(0,0,0));
        rb_ID.setEnabled(true);
        rb_ID.setFont(new Font("sansserif",0,12));
        rb_ID.setText(dieSteuerung.lang.get("connectModeID"));
        rb_ID.setVisible(true);
        if(mode.equals("id")) rb_ID.setSelected(true);
        rb_ID.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    connectMode("id");
                }
            });

        contentPane.add(b_client);
        contentPane.add(b_server);
        contentPane.add(b_randomName);
        contentPane.add(b_options);
        contentPane.add(cb_map);
        contentPane.add(cb_deck);
        contentPane.add(l_deck);
        contentPane.add(l_map);
        contentPane.add(l_players);
        contentPane.add(tf_name);
        contentPane.add(tf_players);
        contentPane.add(rb_auto);
        contentPane.add(rb_IP);
        contentPane.add(rb_ID);

        this.add(contentPane);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.pack();
        this.setVisible(true);
    }

    private void connectMode(String mode){
        this.mode = mode;
        rb_auto.setSelected(false);
        rb_IP.setSelected(false);
        rb_ID.setSelected(false);
        if(mode.equals("auto")) rb_auto.setSelected(true);
        if(mode.equals("ip")) rb_IP.setSelected(true);
        if(mode.equals("id")) rb_ID.setSelected(true);
        dieSteuerung.config.setOption("mode", mode);
    }

    private void startAsClient(){
        setInputEnabled(false);
        dieSteuerung.config.setOption("deck", String.valueOf(cb_deck.getSelectedItem()));
        dieSteuerung.config.setOption("board", String.valueOf(cb_map.getSelectedItem()));
        dieSteuerung.username = tf_name.getText();
        dieSteuerung.joinGame(mode);
        setInputEnabled(true);
    }

    private void startAsServer(){
        setInputEnabled(false);
        dieSteuerung.config.setOption("deck", String.valueOf(cb_deck.getSelectedItem()));
        dieSteuerung.config.setOption("board", String.valueOf(cb_map.getSelectedItem()));
        dieSteuerung.username = tf_name.getText();
        dieSteuerung.createNewGame(new String[]{String.valueOf(cb_map.getSelectedItem()),tf_players.getText(),String.valueOf(cb_deck.getSelectedItem())});
        setInputEnabled(true);
    }

    public void setInputEnabled(boolean enabled){
        b_client.setEnabled(enabled);
        b_server.setEnabled(enabled);
        b_randomName.setEnabled(enabled);
        cb_map.setEnabled(enabled);
        cb_deck.setEnabled(enabled);
        tf_name.setEnabled(enabled);
        tf_players.setEnabled(enabled);
    }
}