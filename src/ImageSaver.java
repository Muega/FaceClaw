import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;

import javax.imageio.ImageIO;


public class ImageSaver {

	
	
	private BufferedImage img;
	private File fileDir;
	
	
	public DownloadLog log;
	
	private Path targetDirectory;
	private String dir;
	private String sourceURL;
	
	private int fileCount;
	
	private int imgHeight;
	private int imgWidth;
	private double fileSize;
	
	String fileTitle;
	
	
	
	public ImageSaver(String targetDirectory, String searchTitle){
		
		this.dir = targetDirectory;
		this.fileTitle = searchTitle;
		img = null;
		this.fileCount = 0;
		log = new DownloadLog();
	}
	
	public void saveImageTemporally(URI uri){
	
		try {
			img = ImageIO.read(new URL(uri.toString()));
			imgHeight = img.getHeight();
			imgWidth = img.getWidth();
			sourceURL = uri.toString();
			System.out.println("height: "+ imgHeight + " Width: " + imgWidth);
			System.out.println("Image: '"+ sourceURL +"' saved temporally");
		} catch (IOException e) {
			System.out.println("Error, retrieving Img from: " + uri.toString());
			e.printStackTrace();
		}		
	}
	
	public void writeToFile() throws NoImageSavedException{
		
		fileDir = new File(createFileName());
		targetDirectory = Paths.get(fileDir.getAbsolutePath());
		System.out.println(fileDir.getAbsolutePath());
		
		if (img == null) {
			throw new NoImageSavedException("No Image was saved temporarilly via method 'saveImageTemporally(URL src)'");
			
		}
		
		try {
			ImageIO.write(img, "png", fileDir);
			fileSize = (double) fileDir.length() / (1024 * 1024);
			
			System.out.println("image written to: "+ targetDirectory + " FileSize: " + fileSize);
			log.add(new DownloadEntry(sourceURL, targetDirectory, fileSize, imgHeight, imgWidth, true, Instant.now()));
		} catch (IOException e) {
			System.out.println("Error. saving img to: " + fileDir.getPath());
			e.printStackTrace();
			log.add(new DownloadEntry(sourceURL, targetDirectory, 0, imgHeight, imgWidth, false, Instant.now()));
		}
	}
	
	private String createFileName() {
		String newDir;
		do {
			fileCount++;
			newDir = dir +fileTitle+ "_"+ fileCount + ".png";
			System.out.println(dir);
			System.out.println(newDir);
			
		}while(new File(newDir).exists());
		return newDir;
		
	}
	
	
	
}
