import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
/**
 * La classe ScreenManagerImpl implémente l'interface ScreenManager et fournit des fonctionnalités
 * pour contrôler l'écran à distance, envoyer/recevoir des fichiers, et gérer les événements de souris
 * et de clavier. Cette classe utilise la classe Robot de Java pour interagir avec l'écran local.
 *
 * Lors de l'instanciation, un mot de passe doit être fourni et un objet Robot est créé pour
 * contrôler l'écran.
 *
 * Les principales méthodes de cette classe sont :
 *
 * - checkPassword(String) : Vérifie si le mot de passe fourni correspond au mot de passe de cette instance.
 * - sendScreen() : Capture l'écran et retourne un tableau de bytes représentant l'image capturée.
 * - moveMouse(int, int) : Déplace le curseur de la souris aux coordonnées spécifiées.
 * - clickMouse(int, int) : Simule un clic gauche de la souris aux coordonnées spécifiées.
 * - sendFile(String, byte[]) : envoyer le fichier
 * - receivefile(String) : récevoirr le fichier
 * - pressKey(int), releaseKey(int), keyTyped(int) : Simule des événements clavier.
 * - mousePressed(int, int, int), mouseReleased(int, int, int) : Simule des événements de souris pressée/relâchée.
 * - mouseDragged(int, int) : Déplace le curseur de la souris tout en maintenant le bouton de la souris enfoncé.
 * - getWidth(), getHeight() : Retourne la largeur et la hauteur de l'écran.
 * - mousereceive(int[]) : Déplace le curseur de la souris aux coordonnées spécifiées dans un tableau.
 *
 * Cette classe étend UnicastRemoteObject pour permettre l'accès à distance aux méthodes via RMI.
 */
public class ScreenManagerImpl extends UnicastRemoteObject implements ScreenManager{
    String password;
    Robot robot =null;
    protected ScreenManagerImpl(String password) throws RemoteException {
        super();
        this.password=password;
        try {
            robot=new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }
    //comparer le mot de passe avec celui générer par le client
    @Override
    public boolean checkPassword(String checkpassword) throws RemoteException {
        if(password.equals(checkpassword))
            return true;
        return false;
    }
    //capturer l'écran du post distant
    @Override
    public byte[] sendScreen() throws RemoteException {
        try {
            // Déterminer la taille de l'écran
            Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
            // Capturer l'écran
            BufferedImage image = robot.createScreenCapture(screenRect);

            // Convertir BufferedImage en tableau de bytes
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(image, "jpg", outputStream);
            return outputStream.toByteArray();
        } catch (Exception e) {
            System.err.println("Erreur lors de la capture d'écran: " + e.getMessage());
            throw new RemoteException("Erreur lors de la capture d'écran", e);
        }
    }
    @Override
    public void moveMouse(int x, int y) throws RemoteException {
        robot.mouseMove(x, y);
    }
    @Override
    public void clickMouse(int x, int y) throws RemoteException {
        robot.mouseMove(x, y);
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
    }
    //envoyer le fichier
    @Override
    public void sendFile(String fileName, byte[] fileData) throws RemoteException {
        try {
            File file = new File(fileName);
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(fileData);
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RemoteException("Error writing file: " + e.getMessage());
        }
    }
    //récevoir le fichier
    @Override
    public byte[] receivefile(String fileName) throws RemoteException {
        try {
            File file = new File(fileName);
            if (!file.exists()) {
                throw new RemoteException("File not found: " + fileName);
            }
            byte[] fileData = new byte[(int) file.length()];
            try (FileInputStream fis = new FileInputStream(file)) {
                fis.read(fileData);
            }
            return fileData;
        } catch (IOException e) {
            e.printStackTrace();
            throw new RemoteException("Error reading file: " + e.getMessage());
        }
    }
    @Override
    public void pressKey(int keyCode) throws RemoteException {
        robot.keyPress(keyCode);
    }
    @Override
    public void releaseKey(int keyCode) throws RemoteException {
        robot.keyRelease(keyCode);
    }
    @Override
    public void keyTyped(int keyChar) throws RemoteException {
        robot.keyPress(keyChar);
        robot.keyRelease(keyChar);
    }
    @Override
    public void mousePressed(int x, int y, int button) throws RemoteException {
        int mask = 0;
        switch (button) {
            case 1:
                mask = InputEvent.BUTTON1_DOWN_MASK;
                break;
            case 2:
                mask = InputEvent.BUTTON2_DOWN_MASK;
                break;
            case 3:
                mask = InputEvent.BUTTON3_DOWN_MASK;
                break;
        }
        robot.mouseMove(x,y);
        robot.mousePress(mask);
    }
    @Override
    public void mouseReleased(int x, int y, int button) throws RemoteException {
        int mask = 0;
        switch (button) {
            case 1:
                mask = InputEvent.BUTTON1_DOWN_MASK;
                break;
            case 2:
                mask = InputEvent.BUTTON2_DOWN_MASK;
                break;
            case 3:
                mask = InputEvent.BUTTON3_DOWN_MASK;
                break;
        }
        robot.mouseMove(x,y);
        robot.mouseRelease(mask);
    }
    @Override
    public void mouseEntered() throws RemoteException {

    }
    @Override
    public void mouseExited() throws RemoteException {

    }
    @Override
    public void mouseDragged(int x, int y) throws RemoteException {
        robot.mouseMove(x, y);
    }
    @Override
    public double getWidth() throws RemoteException {
        return Toolkit.getDefaultToolkit().getScreenSize().getWidth();
    }
    @Override
    public double getHeight() throws RemoteException {
        return Toolkit.getDefaultToolkit().getScreenSize().getHeight();
    }
    @Override
    public void mousereceive(int[] coordinates) throws RemoteException {
        this.robot.mouseMove(coordinates[0],coordinates[1]);
    }
}