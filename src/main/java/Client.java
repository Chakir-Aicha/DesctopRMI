import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.awt.event.*;
import java.rmi.RemoteException;
public class Client extends JFrame implements ActionListener {
    private JTextField ipField;
    private JTextField passwordField;
    private JButton connectButton;
    private ScreenManager screen; // Référence à l'interface Screen

    public Client() {
        setTitle("Connect to Server");
        setSize(300, 150);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        JPanel panel = new JPanel();
        panel.add(new JLabel("Server IP:"));
        ipField = new JTextField(15);
        panel.add(ipField);
        panel.add(new JLabel("Password:"));
        passwordField = new JTextField(15);
        panel.add(passwordField);
        connectButton = new JButton("Connect");
        connectButton.addActionListener(this);
        panel.add(connectButton);
        add(panel);

        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == connectButton) {
            try {
                String serverIP = ipField.getText();
                String password = passwordField.getText();
                Registry registry = LocateRegistry.getRegistry(serverIP, 1099);
                screen = (ScreenManager) registry.lookup("ScreenManager");

                if (screen.checkPassword(password)) {
                    // Si la connexion est réussie, ouvrez la fenêtre d'affichage de l'image
                    EventQueue.invokeLater(() -> {
                        new ImageWindow(screen);
                        dispose(); // Ferme la fenêtre de connexion
                    });
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid Password", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Connection Failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        new Client();
    }

    public class ImageWindow extends JFrame {
        private ScreenManager screenManager;
        private JLabel imageLabel;

        public ImageWindow(ScreenManager screenManager) {
            this.screenManager = screenManager;
            setTitle("Remote Screen Viewer");
            setSize(800, 600);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            imageLabel = new JLabel();
            add(new JScrollPane(imageLabel));

            // Ajouter des écouteurs d'événements pour la souris et le clavier
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    try {
                        int button = e.getButton();
                        screenManager.clickMouse(button);
                    } catch (RemoteException ex) {
                        ex.printStackTrace();
                    }
                }
            });

            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    try {
                        screenManager.moveMouse(e.getX(), e.getY());
                    } catch (RemoteException ex) {
                        ex.printStackTrace();
                    }
                }
            });

            // Lancer un thread pour mettre à jour périodiquement l'image
            new Timer(1000 / 10, e -> updateImage()).start(); // Mettre à jour l'image 10 fois par seconde

            setVisible(true);
        }

        private void updateImage() {
            try {
                byte[] imageBytes = screenManager.sendScreen();
                BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));
                if (image != null) {
                    imageLabel.setIcon(new ImageIcon(image));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Vous pouvez également ajouter d'autres méthodes pour gérer les événements du clavier si nécessaire
    }

}