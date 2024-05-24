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
import java.nio.file.Paths;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.awt.event.*;
import java.rmi.RemoteException;
import java.util.Random;

public class Client extends JFrame implements ActionListener {
    private JButton connectButton;
    private JLabel passwordLabel;
    JMenu fileMenu;
    private JMenuBar menuBar;
    private JMenuItem sendFileMenuItem;
    private JMenuItem receiveFileMenuItem;
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
                            String downloadFileName = "serverFile.txt";
                            try {
                                byte[] fileData = screen.receivefile(downloadFileName);
                                Files.write(Paths.get("downloaded_" + downloadFileName), fileData);
                                System.out.println("File downloaded successfully!");
                            } catch (RemoteException ea) {
                                System.err.println("Download error: " + ea.getMessage());
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }

                            // Envoyer un fichier
                            String uploadFileName = "localFile.txt";
                            try {
                                byte[] uploadData = Files.readAllBytes(Paths.get(uploadFileName));
                                screen.sendFile("uploaded_" + uploadFileName, uploadData);
                                System.out.println("succes");
                            } catch (RemoteException ae) {
                                System.err.println("Upload error: " + ae.getMessage());
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
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
    private String generateRandomPassword() {
        Random random = new Random();
        StringBuilder password = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            password.append(random.nextInt(10));
        }
        return password.toString();
    }
    private void setUpMenu() {
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
        validate();
    }
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
                try (FileOutputStream fos = new FileOutputStream(file)) {
                    fos.write(fileData);
                }
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
            setUpMenu();
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
