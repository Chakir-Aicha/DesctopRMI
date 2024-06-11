import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetAddress;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
/**
 * La classe Serveur représente l'interface graphique côté serveur pour configurer et démarrer le serveur.
 * Elle étend JFrame et implémente ActionListener pour gérer les événements de l'interface.
 *
 * La fenêtre affiche un champ de texte pour saisir un mot de passe, l'adresse IP privée du serveur,
 * et un bouton "Connect".
 *
 * Lorsque le bouton "Connect" est cliqué, la méthode actionPerformed() est appelée.
 * Dans cette méthode, une instance de ScreenManagerImpl est créée avec le mot de passe saisi.
 * Ensuite, un objet Registry RMI est créé et l'objet ScreenManagerImpl est enregistré auprès de celui-ci.
 *
 * La classe Serveur est responsable de l'interface utilisateur côté serveur et de la configuration initiale
 * du serveur. Elle crée l'objet ScreenManager qui gère les connexions clientes et les interactions avec l'écran.
 */
public class Serveur extends JFrame implements ActionListener {
    private JTextField passwordField;
    private JLabel privateIPLabel;
    private JButton connectButton;
    private ScreenManager screenManager;
    public Serveur() {
        setTitle("Server");
        setSize(300, 150);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        JPanel passwordPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        passwordPanel.add(new JLabel("Enter Password:"));
        passwordField = new JTextField(20);
        passwordPanel.add(passwordField);
        add(passwordPanel);
        // Ajout un label pour l'adresse IP
        add(Box.createRigidArea(new Dimension(0, 7)));
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            privateIPLabel = new JLabel("Private IP: " + localHost.getHostAddress());
            privateIPLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            add(privateIPLabel);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Ajouter un espace rigide pour séparer les composants
        add(Box.createRigidArea(new Dimension(0, 10)));

        connectButton = new JButton("Connect");
        connectButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        connectButton.addActionListener(this);
        add(connectButton);

        setVisible(true);
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
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }
}