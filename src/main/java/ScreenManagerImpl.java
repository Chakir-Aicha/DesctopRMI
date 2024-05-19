import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class ScreenManagerImpl extends UnicastRemoteObject implements ScreenManager{
    Double width, height;
    String password;
    Robot robot =null;

    protected ScreenManagerImpl(String password) throws RemoteException {
        super();
        this.password=password;
    }

    @Override
    public boolean checkPassword(String checkpassword) throws RemoteException {
        if(password.equals(checkpassword))
            return true;
        return false;
    }

    @Override
    public byte[] sendScreen() throws RemoteException {
        try {
            // Créer un objet Robot pour capturer l'écran
            robot = new Robot();
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
    public void clickMouse(int button) throws RemoteException {
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
        robot.mousePress(mask);
        robot.mouseRelease(mask);
    }

    public byte[] sendFile(String filePath) throws RemoteException {
        try (FileInputStream fis = new FileInputStream(filePath)) {
            byte[] fileData = new byte[(int) new File(filePath).length()];
            fis.read(fileData);
            return fileData;
        } catch (Exception e) {
            System.err.println("Erreur lors de la demande du fichier: " + e.getMessage());
            throw new RemoteException("Erreur lors de la demande du fichier", e);
        }
    }

    @Override
    public double getWidth() throws RemoteException {
        return this.width;
    }

    @Override
    public double getHeight() throws RemoteException {
        return this.height;
    }
}