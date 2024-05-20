import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.net.InetAddress;
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
        }else if (e.getSource() == sendFileButton) {
            if (screenManager == null) {
                JOptionPane.showMessageDialog(this, "Server is not connected. Please connect first.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                int returnValue = fileChooser.showOpenDialog(this);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    String filePath = selectedFile.getAbsolutePath();

                    // Demande du fichier au client
                    byte[] fileData = screenManager.sendFile(filePath);
                    try (FileOutputStream fos = new FileOutputStream("received_" + selectedFile.getName())) {
                        fos.write(fileData);
                        JOptionPane.showMessageDialog(this, "File received and saved as " + "received_" + selectedFile.getName());
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(this, "Error saving file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                        ex.printStackTrace();
                    }
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error requesting file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }
    private void showSendFileButton() {
        sendFileButton = new JButton("Send File");
        sendFileButton.addActionListener(this);
        add(sendFileButton);

        // Redimensionner la fenêtre pour s'adapter au nouveau bouton
        setSize(300, 250);
        validate();
    }

    public static void main(String[] args) {
        new Serveur();
    }


}
