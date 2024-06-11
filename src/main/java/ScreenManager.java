import java.rmi.Remote;
import java.rmi.RemoteException;
/**
 * L'interface ScreenManager définit les méthodes pour contrôler et interagir avec un écran distant
 * à travers une connexion RMI (Remote Method Invocation).
 *
 * Les principales méthodes définies dans cette interface sont :
 *
 * - checkPassword(String) : Vérifie si le mot de passe fourni est correct.
 * - sendScreen() : Capture l'écran distant et retourne un tableau de bytes représentant l'image capturée.
 * - getWidth(), getHeight() : Retourne la largeur et la hauteur de l'écran distant.
 * - moveMouse(int, int) : Déplace le curseur de la souris sur l'écran distant aux coordonnées spécifiées.
 * - clickMouse(int, int) : Simule un clic de souris sur l'écran distant aux coordonnées spécifiées.
 * - pressKey(int), releaseKey(int), keyTyped(int) : Simule des événements clavier sur l'écran distant.
 * - mousePressed(int, int, int), mouseReleased(int, int, int) : Simule des événements de souris pressée/relâchée sur l'écran distant.
 * - mouseEntered(), mouseExited() : Gère les événements de souris entrant/sortant de l'écran distant.
 * - mouseDragged(int, int) : Déplace le curseur de la souris tout en maintenant le bouton de la souris enfoncé sur l'écran distant.
 * - sendFile(String, byte[]) : Envoie un fichier à l'ordinateur distant.
 * - receivefile(String) : Reçoit un fichier de l'ordinateur distant.
 *
 * Toutes les méthodes de cette interface peuvent lever une RemoteException, car elles sont destinées
 * à être appelées à distance via RMI.
 *
 * Cette interface étend l'interface Remote, ce qui est requis pour les interfaces RMI.
 */

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
        void sendFile( String fileName , byte[] fileData) throws RemoteException;
        byte[] receivefile(String fileName) throws RemoteException;
}
