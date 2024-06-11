import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.awt.event.*;
import java.rmi.RemoteException;
import java.util.Random;
/**
 * La classe Client représente l'interface graphique côté client pour se connecter à un serveur distant.
 * Elle étend JFrame et implémente ActionListener pour gérer les événements de l'interface.
 *
 * La fenêtre affiche un mot de passe généré aléatoirement et un bouton "Connect".
 * Lorsque le bouton est cliqué, la méthode actionPerformed() est appelée.
 *
 * Dans actionPerformed(), le client tente de se connecter au serveur en utilisant le mot de passe généré.
 * Si le mot de passe est correct, une nouvelle fenêtre ImageWindow est ouverte pour afficher l'écran distant.
 * Sinon, un message d'erreur est affiché.
 *
 * La classe contient également deux méthodes utilitaires :
 *
 * - generateRandomPassword() génère un mot de passe aléatoire de 6 chiffres.
 * - sendFile() ouvre une boîte de dialogue pour sélectionner un fichier à envoyer au serveur.
 *   Le fichier sélectionné est lu et envoyé au serveur via la méthode sendFile() de l'objet ScreenManager.
 */

public class Client extends JFrame implements ActionListener {
    private JButton connectButton;
    private JLabel passwordLabel;
    private ScreenManager screen;
    public Client() {
        setTitle("Connect to Server");
        setSize(300, 150);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        setLayout(new GridLayout(2, 1));
        // Label pour le mot de passe généré
        passwordLabel = new JLabel("Generated Password: " + generateRandomPassword(), SwingConstants.CENTER);
        add(passwordLabel);
        // Panneau pour le bouton de connexion
        JPanel bottomPanel = new JPanel(new FlowLayout());
        connectButton = new JButton("Connect");
        connectButton.addActionListener(this);
        bottomPanel.add(connectButton);
        add(bottomPanel);
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == connectButton) {
            try {
                String password = passwordLabel.getText().split(": ")[1];
                System.out.println("Generated Password: " + password);
                Registry registry = LocateRegistry.getRegistry("192.168.137.188", 1099);
                //Registry registry = LocateRegistry.getRegistry("localhost", 1099);
                screen = (ScreenManager) registry.lookup("ScreenManager");

                if (screen.checkPassword(password)) {
                    EventQueue.invokeLater(() -> {
                        try {
                            new ImageWindow(screen);
                        } catch (RemoteException ex) {
                            ex.printStackTrace();
                        }
                        dispose();
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
    // Fonction pour générer un mot de passe aléatoire
    private String generateRandomPassword() {
        Random random = new Random();
        StringBuilder password = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            password.append(random.nextInt(10));
        }
        return password.toString();
    }
    // Fonction pour envoyer un fichier
    private void sendFile() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
                byte[] fileData = Files.readAllBytes(file.toPath());
                screen.sendFile(file.getName(), fileData);
                JOptionPane.showMessageDialog(this, "File sent successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "File transfer failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    /**
     * La classe ImageWindow est une fenêtre graphique qui affiche l'écran d'un ordinateur distant.
     * Elle étend JFrame et implémente les interfaces KeyListener, MouseListener et MouseMotionListener
     * pour gérer les événements de clavier et de souris.
     *
     * Cette classe est liée à un objet ScreenManager qui gère la communication avec l'ordinateur distant.
     * Elle affiche l'écran distant dans un JLabel, en mettant à jour l'image toutes les 100 ms.
     *
     * La fenêtre dispose également d'un menu avec deux options :
     * - "Send File" qui permet d'envoyer un fichier à l'ordinateur distant.
     * - "Receive File" qui permet de recevoir un fichier de l'ordinateur distant.
     *
     * Les événements de clavier et de souris sont transmis à l'ordinateur distant via l'objet ScreenManager.
     * Les coordonnées de la souris sont adaptées à la résolution de l'écran distant.
     *
     * La fenêtre s'adapte également au redimensionnement, en mettant à jour les dimensions locales
     * et en recalculant les échelles pour les coordonnées de la souris.
     */
    public class ImageWindow extends JFrame implements KeyListener, MouseListener,MouseMotionListener {
        private ScreenManager screenManager;
        private JLabel imageLabel;
        private double localWidth, localHeight, remoteWidth, remoteHeight;
        private JMenuBar menuBar;
        private JMenu fileMenu;
        private JMenuItem sendFileMenuItem;
        private JMenuItem receiveFileMenuItem;

        public ImageWindow(ScreenManager screenManager) throws RemoteException {
            this.screenManager = screenManager;
            setTitle("Remote Screen Viewer");
            setSize(800, 600);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            imageLabel = new JLabel();
            add(new JScrollPane(imageLabel));
            imageLabel.addMouseListener(this);
            imageLabel.addMouseMotionListener(this);
            addKeyListener(this);
            setFocusable(true);
            setFocusTraversalKeysEnabled(false);
            initializeMenu();
            setVisible(true);
            // Écouter les événements de redimensionnement de la fenêtre
            addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent e) {
                    localWidth = imageLabel.getWidth();
                    localHeight = imageLabel.getHeight();
                }
            });
            // Mettre à jour l'image toutes les 100 ms
            new Timer(1000 / 10, e -> updateImage()).start();
            localWidth = getWidth();
            localHeight = getHeight();
            remoteWidth = screenManager.getWidth();
            remoteHeight = screenManager.getHeight();
            setVisible(true);
        }
        private Point getRemoteCoordinates(int localX, int localY) {
            localWidth = imageLabel.getWidth();
            localHeight = imageLabel.getHeight();
            double scaleX = remoteWidth / localWidth;
            double scaleY = remoteHeight / localHeight;
            int remoteX = (int) (localX * scaleX);
            int remoteY = (int) (localY * scaleY);
            return new Point(remoteX, remoteY);
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
        @Override
        public void keyTyped(KeyEvent e) {
            try {
                screenManager.keyTyped(e.getKeyCode());
            } catch (RemoteException ex) {
                ex.printStackTrace();
            }
        }
        @Override
        public void keyPressed(KeyEvent e) {
            try {
                screenManager.pressKey(e.getKeyCode());
            } catch (RemoteException ex) {
                ex.printStackTrace();
            }
        }
        @Override
        public void keyReleased(KeyEvent e) {
            try {
                screenManager.releaseKey(e.getKeyCode());
            } catch (RemoteException ex) {
                ex.printStackTrace();
            }
        }
        @Override
        public void mouseDragged(MouseEvent e) {
            try {
                screenManager.mouseDragged(e.getX(), e.getY());
            } catch (RemoteException ex) {
                ex.printStackTrace();
            }
        }
        @Override
        public void mouseMoved(MouseEvent e) {
            try {
                Point p = getRemoteCoordinates(e.getX(), e.getY());
                screenManager.moveMouse(p.x, p.y);
            } catch (RemoteException ex) {
                ex.printStackTrace();
            }
        }
        @Override
        public void mouseClicked(MouseEvent e) {
            try {
                Point p = getRemoteCoordinates(e.getX(), e.getY());
                screenManager.clickMouse(p.x,p.y);
            } catch (RemoteException ex) {
                ex.printStackTrace();
            }
        }
        @Override
        public void mousePressed(MouseEvent e) {
            try {
                int button = e.getButton();
                Point p = getRemoteCoordinates(e.getX(), e.getY());
                screenManager.mousePressed(p.x,p.y,button);
            } catch (RemoteException ex) {
                ex.printStackTrace();
            }
        }
        @Override
        public void mouseReleased(MouseEvent e) {
            try {
                int button = e.getButton();
                Point p = getRemoteCoordinates(e.getX(), e.getY());
                screenManager.mouseReleased(p.x,p.y,button);
            } catch (RemoteException ex) {
                ex.printStackTrace();
            }
        }
        @Override
        public void mouseEntered(MouseEvent e) {
        }
        @Override
        public void mouseExited(MouseEvent e) {
        }
        private void initializeMenu() {
            menuBar = new JMenuBar();
            fileMenu = new JMenu("File");
            sendFileMenuItem = new JMenuItem("Send File");
            sendFileMenuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    sendFile();
                }
            });
            fileMenu.add(sendFileMenuItem);
            receiveFileMenuItem = new JMenuItem("Receive File");
            receiveFileMenuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String downloadFileName = "src/serverFile.txt";
                    try {
                        byte[] fileData = screen.receivefile(downloadFileName);
                        Files.write(Paths.get( downloadFileName), fileData);
                        System.out.println("File downloaded successfully!");
                    } catch (RemoteException ea) {
                        System.err.println("Download error: " + ea.getMessage());
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            });
            fileMenu.add(receiveFileMenuItem);
            menuBar.add(fileMenu);
            setJMenuBar(menuBar);
        }
    }
}
