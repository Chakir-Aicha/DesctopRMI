import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.awt.event.*;
import java.rmi.RemoteException;
public class Client extends JFrame implements ActionListener {
    private JTextField ipField;
    private JTextField passwordField;
    private JButton connectButton;
    JMenu fileMenu;
    private JMenuBar menuBar;
    private JMenuItem sendFileMenuItem;
    private JMenuItem receiveFileMenuItem;
    private ScreenManager screen; // Référence à l'interface Screen

    public Client() {
        setTitle("Connect to Server");
        setSize(300, 150);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        JPanel panel = new JPanel();
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
                String password = passwordField.getText();
                Registry registry = LocateRegistry.getRegistry("192.168.137.1", 1099);
                screen = (ScreenManager) registry.lookup("ScreenManager");

                if (screen.checkPassword(password)) {
                    menuBar = new JMenuBar();
                    fileMenu = new JMenu("File");
                    sendFileMenuItem = new JMenuItem("Send File");
                    receiveFileMenuItem = new JMenuItem("Receive File");
                    sendFileMenuItem.addActionListener(this);
                    receiveFileMenuItem.addActionListener(this);
                    fileMenu.add(sendFileMenuItem);
                    fileMenu.add(receiveFileMenuItem);
                    menuBar.add(fileMenu);
                    setJMenuBar(menuBar);
                    // Si la connexion est réussie, ouvrez la fenêtre d'affichage de l'image
                    EventQueue.invokeLater(() -> {
                        try {
                            new ImageWindow(screen);
                        } catch (RemoteException ex) {
                            ex.printStackTrace();
                        }
                        dispose(); // Ferme la fenêtre de connexion
                    });
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid Password", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Connection Failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        } else if (e.getSource() == sendFileMenuItem) {
            sendFile();
        } else if (e.getSource() == receiveFileMenuItem) {
            receiveFile();
        }
    }
    // Ajoutez ces méthodes dans la classe Client
    private void sendFile() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
                byte[] fileData = Files.readAllBytes(file.toPath());
                screen.sendFile(file.getName(), fileData);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void receiveFile() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
                byte[] fileData = screen.receivefile(file.getName());
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(fileData);
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public static void main(String[] args) {
        new Client();
    }

    public class ImageWindow extends JFrame implements KeyListener, MouseListener,MouseMotionListener {
        private ScreenManager screenManager;
        private JLabel imageLabel;
        private double localWidth, localHeight, remoteWidth, remoteHeight;

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
            // Écouteur pour les événements de redimensionnement
            addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent e) {
                    localWidth = imageLabel.getWidth();
                    localHeight = imageLabel.getHeight();
                }
            });
            // Lancer un thread pour mettre à jour périodiquement l'image
            new Timer(1000 / 10, e -> updateImage()).start(); // Mettre à jour l'image 10 fois par seconde
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
                System.out.println("Server: key typed");
                screenManager.keyTyped(e.getKeyCode());
            } catch (RemoteException ex) {
                ex.printStackTrace();
            }
        }

        @Override
        public void keyPressed(KeyEvent e) {
            try {
                System.out.println("Server: key pressed");
                screenManager.pressKey(e.getKeyCode());
            } catch (RemoteException ex) {
                ex.printStackTrace();
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
            try {
                System.out.println("Server: key released");
                screenManager.releaseKey(e.getKeyCode());
            } catch (RemoteException ex) {
                ex.printStackTrace();
            }
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            try {
                System.out.println("Server: Mouse dragged");
                screenManager.mouseDragged(e.getX(), e.getY());
            } catch (RemoteException ex) {
                ex.printStackTrace();
            }
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            try {
                System.out.println("Server: Mouse mouved");
                Point p = getRemoteCoordinates(e.getX(), e.getY());
                screenManager.moveMouse(p.x, p.y);
            } catch (RemoteException ex) {
                ex.printStackTrace();
            }
        }
        @Override
        public void mouseClicked(MouseEvent e) {
            try {
                System.out.println("mouse clicked");
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
                System.out.println("mouse pressed");
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
                System.out.println("mouse released");
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
    }

}
