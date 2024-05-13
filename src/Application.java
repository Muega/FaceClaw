import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.beust.jcommander.converters.PathConverter;

public class Application {

	//test
	//TODO save cookies 
	public static void main(String[] args) throws URISyntaxException {
		
		
		
		
		TestClaw test = new TestClaw();
		
		test.run();
		
		
		//TODO bessere Lösung um file directory anzugeben
		ImageSaver iS = new ImageSaver("./testImg2.jpg");
		try {
			iS.saveImageTemporally(new URI("https://upload.wikimedia.org/wikipedia/commons/4/4c/Stpauli.jpg"));

			iS.writeToFile();
		} catch (NoImageSavedException e) {
			System.out.println("You fucked up noimg");
			e.printStackTrace();
		} catch(Exception e) {
			System.out.println("You fucked up I/O");
			e.printStackTrace();
		}
	}

}
