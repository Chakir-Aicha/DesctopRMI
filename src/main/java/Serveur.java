import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Files;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Serveur extends JFrame implements ActionListener {
    private JTextField passwordField;
    private JLabel privateIPLabel;
    private JLabel publicIPLabel;
    private JButton connectButton;
    private JButton sendFileButton;
    private ScreenManager screenManager;
    public Serveur() {
        // Configuration de la fenêtre
        setTitle("Server Configuration");
        setSize(300, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        // Ajout du champ de mot de passe

        add(new JLabel("Enter Password:"));
        passwordField = new JTextField(20);
        add(passwordField);

        // Ajout des labels pour les adresses IP
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            privateIPLabel = new JLabel("Private IP: " + localHost.getHostAddress());
            add(privateIPLabel);

            publicIPLabel = new JLabel("Public IP: " + getPublicIP());
            add(publicIPLabel);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Ajout du bouton de connexion
        connectButton = new JButton("Connect");
        connectButton.addActionListener(this);
        add(connectButton);
        // Afficher la fenêtre
        setVisible(true);
    }

    private String getPublicIP() throws Exception {
        // Exemple de méthode pour récupérer l'adresse IP publique (simplifiée)
        return InetAddress.getLocalHost().getHostAddress(); // Simplifié pour l'exemple
    }
    @Override
    public void actionPerformed(ActionEvent e) {
        String password = passwordField.getText();
        System.out.println("Generated Password: " + password);
        try {
            screenManager = new ScreenManagerImpl(password);
        } catch (RemoteException ex) {
            ex.printStackTrace();
        }
        if (e.getSource() == connectButton) {
            try {

                Registry registry = LocateRegistry.createRegistry(1099);
                registry.rebind("ScreenManager", screenManager);
                JOptionPane.showMessageDialog(this, "Server is connected and ready!");
                showSendFileButton();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }

    }

    public static void main(String[] args) {
        new Serveur();
    }


}
