import java.net.URISyntaxException;

import javax.swing.SwingUtilities;
public class Application {

	//test
	//TODO save cookies 
	//TODO Probleme: Seitenladezeit (neuer Tab, erschafft error)
	public static void main(String[] args) throws URISyntaxException {
		
		Claw claw = new Claw(null); //TODO etwas einsetzen
		SwingUtilities.invokeLater(()-> new GUI(claw).run());
		
		
		
		
		
		
		
		//TODO bessere Lösung um file directory anzugeben
		/*ImageSaver iS = new ImageSaver("./testImg2.jpg");
		try {
			iS.saveImageTemporally(new URI("https://upload.wikimedia.org/wikipedia/commons/4/4c/Stpauli.jpg"));

			iS.writeToFile();
			
			
		} catch (NoImageSavedException e) {
			System.out.println("You fucked up noimg");
			e.printStackTrace();
		} catch(Exception e) {
			System.out.println("You fucked up I/O");
			e.printStackTrace();
		}*/
	}

}
