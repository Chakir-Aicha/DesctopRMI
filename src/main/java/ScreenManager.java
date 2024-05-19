import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ScreenManager extends Remote {
        public boolean checkPassword(String inputPassword) throws RemoteException;
        public byte[] sendScreen() throws RemoteException;
        public double getWidth() throws RemoteException;
        public double getHeight() throws RemoteException;
        void moveMouse(int x, int y) throws RemoteException;
        void clickMouse(int button) throws RemoteException;
        byte[] sendFile( String filePath) throws RemoteException;
}
