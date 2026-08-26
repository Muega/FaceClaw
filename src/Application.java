import java.io.File;
import java.net.URISyntaxException;

import javax.swing.SwingUtilities;
public class Application {

	//OpenCV file nötig fürs handlen des KI-Modells. Muss manuell geladen werden,
	//da der bin Ordner sich regelmäßig reinigt
	static {
	    try {
	        String path = new File("opencv_java4120.dll").getAbsolutePath();
	        System.load(path);
	        System.out.println("OpenCV file loaded: " + path);
	    } catch (UnsatisfiedLinkError e) {
	        System.err.println("Couldn't open OpenCV File!!!!!");
	        e.printStackTrace();
	    }
	}

	
	public static void main(String[] args) throws URISyntaxException {
		
		
		Claw claw = new Claw(); 
		SwingUtilities.invokeLater(()-> new GUI(claw).run());
		
	}

}
