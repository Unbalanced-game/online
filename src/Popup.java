import javax.swing.JOptionPane;

public class Popup{
    public void message(String title, String message){
        JOptionPane.showMessageDialog(null,message,title, JOptionPane.INFORMATION_MESSAGE);
    }

    public void error(String title, String message){
        JOptionPane.showMessageDialog(null,message,title, JOptionPane.ERROR_MESSAGE);
    }

    public String dropDown(String title, String message, String[] options){
        return (String) JOptionPane.showInputDialog(null, message,title, JOptionPane.QUESTION_MESSAGE, null,options,options[0]);
    }

    public String textInput(String message){
        return JOptionPane.showInputDialog(message);
    }
    
    public int yesNo(String message){
        return JOptionPane.showConfirmDialog(null, message);
    }
}