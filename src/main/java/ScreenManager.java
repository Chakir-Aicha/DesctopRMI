import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ScreenManager extends Remote {
        public boolean checkPassword(String inputPassword) throws RemoteException;
        public byte[] sendScreen() throws RemoteException;
        public double getWidth() throws RemoteException;
        public double getHeight() throws RemoteException;
        void mousereceive(int[] mouseCoordinates) throws RemoteException;
        void moveMouse(int x, int y) throws RemoteException;
        void clickMouse(int x,int y) throws RemoteException;
        void pressKey(int keyCode) throws RemoteException;
        void releaseKey(int keyCode) throws RemoteException;
        void keyTyped(int keyChar) throws RemoteException;
        void mousePressed(int x, int y,int button) throws RemoteException;
        void mouseReleased(int x, int y, int button) throws RemoteException;
        void mouseEntered() throws RemoteException;
        void mouseExited() throws RemoteException;
        void mouseDragged(int x, int y) throws RemoteException;
        byte[] sendFile( String filePath) throws RemoteException;
}
