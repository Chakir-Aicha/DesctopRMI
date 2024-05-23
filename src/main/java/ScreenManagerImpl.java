import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;

public class ScreenManagerImpl extends UnicastRemoteObject implements ScreenManager{
    Double width, height;
    String password;
    Robot robot =null;
    private Map<String, byte[]> fileStore = new HashMap<>();
    private static final String STORAGE_DIR = "server_files";

    protected ScreenManagerImpl(String password) throws RemoteException {
        super();
        this.password=password;
        Path storagePath = Paths.get(STORAGE_DIR);
        if (!Files.exists(storagePath)) {
            try {
                Files.createDirectory(storagePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            robot=new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
        }
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
        System.out.println("Server: Mouse entered");
    }

    @Override
    public void clickMouse(int x, int y) throws RemoteException {
        System.out.println("Server: Clicking mouse at (" + x + ", " + y + ")");
        robot.mouseMove(x, y);
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
    }
    @Override
    public void sendFile(String fileName, byte[] fileData) throws RemoteException {
        Path filePath = Paths.get(STORAGE_DIR, fileName);
        try (FileOutputStream fos = new FileOutputStream(filePath.toFile())) {
            fos.write(fileData);
            System.out.println("File saved: " + filePath.toAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public byte[] receivefile(String fileName) throws RemoteException {
        Path filePath = Paths.get(STORAGE_DIR, fileName);
        try {
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    @Override
    public void pressKey(int keyCode) throws RemoteException {
        robot.keyPress(keyCode);
        System.out.println("Server: Mouse entered");
    }

    @Override
    public void releaseKey(int keyCode) throws RemoteException {
        robot.keyRelease(keyCode);
        System.out.println("Server: Mouse entered");
    }

    @Override
    public void keyTyped(int keyChar) throws RemoteException {
        robot.keyPress(keyChar);
        robot.keyRelease(keyChar);
        System.out.println("Server: Mouse entered");
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
        System.out.println("Server: Mouse pressed at (" + x + ", " + y + ") with button " + button);
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
        System.out.println("Server: Mouse dragged");
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
