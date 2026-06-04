import javax.swing.*;

public class UISwing {
    public static void main(String[] args) {

        String name = JOptionPane.showInputDialog("Enter your name:");
        JOptionPane.showMessageDialog(null, "Hello, " + name + "! Welcome to Swing!");
        JFrame frame = new JFrame("My Swing Application");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);



    }
}
