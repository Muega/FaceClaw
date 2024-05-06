import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;

import javax.imageio.ImageIO;

//Test
public class ImageSaver {

	
	
	private BufferedImage img;
	private File fileDir;
	private String dir;
	
	public ImageSaver(String targetDirectory){
		this.dir = targetDirectory;
		this.fileDir = new File(this.dir);
		img = null;
	}
	
	public void saveImageTemporally(URI uri){
	
		try {
			img = ImageIO.read(new URL(uri.toString()));
			System.out.println("Image: '"+ uri.toString()+"' saved temporally");
		} catch (IOException e) {
			System.out.println("Error, retrieving Img from: " + uri.toString());
			e.printStackTrace();
		}		
	}
	
	public void writeToFile() throws NoImageSavedException{
		
		if (img == null) {
			throw new NoImageSavedException("No Image was saved temporarilly via method 'saveImageTemporally(URL src)'");
		}
		
		try {
			ImageIO.write(img, "png", fileDir);
			System.out.println("image written to: "+ fileDir.getPath());
		} catch (IOException e) {
			System.out.println("Error. saving img to: " + fileDir.getPath());
			e.printStackTrace();
		}
	}
	
	
}
