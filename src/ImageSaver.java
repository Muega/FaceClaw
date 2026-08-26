import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;

import javax.imageio.ImageIO;


public class ImageSaver {

	
	
	private BufferedImage img;
	private File fileDir;
	
	
	public DownloadLog log;
	
	private Path targetDirectory;
	private String dir;
	private String sourceURL;
	
	private boolean faceDetectionEnabled;
	private boolean hasFace;
	
	
	private int fileCount;
	
	private int imgHeight;
	private int imgWidth;
	private double fileSize;
	
	String fileTitle;
	
	final int CONNECT_TIMEOUT_MS = 5000;   // 5s Verbindungsaufbau
    final int READ_TIMEOUT_MS    = 15000;  // 15s pro Lese Block
	
    FaceOcclusionDetector faceDetector = new FaceOcclusionDetector("./models/face_detection_yunet_2023mar.onnx");

	
	public ImageSaver(String targetDirectory, String searchTitle, DownloadLog log){
		
		this.dir = targetDirectory;
		this.fileTitle = searchTitle;
		img = null;
		this.fileCount = 0;
		this.log = log;
		
		this.hasFace = false;
	}
	
	public String saveImageTemporally(URI uri){
		
		hasFace = false;
		
		try {
			URL url = uri.toURL();
	        URLConnection connection = url.openConnection();
	        connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
	        connection.setReadTimeout(READ_TIMEOUT_MS);  
			
	        try (InputStream in = connection.getInputStream()) {
	            img = ImageIO.read(in);
	        }

	        if (img == null) {
	        	System.out.println("Error, decoding Img from: " + uri.toString() + " Img == null");
	            return "!!!!!!!!!!!!!!!!!!!!!!!!!!+\"\n"+"Error, decoding Img from: " + uri.toString()+"\n!!!!!!!!!!!!!!!!!!!!!!!!!!\n";
	        }
	        
	        
	        if(faceDetectionEnabled) {
	        	 if (!faceDetector.hasFace(img)) {
	  	            System.out.println("Kein Gesicht → wird uebersprungen: " + uri);
	  	            return "No face detected";
	  	        }else {
	  	        	hasFace = true;
	  	        }
	  	        
	        }
	      
			imgHeight = img.getHeight();
			imgWidth = img.getWidth();
			sourceURL = uri.toString();
			System.out.println("height: "+ imgHeight + " Width: " + imgWidth);
			System.out.println("Image: '"+ sourceURL +"' saved temporally");
			return "Image loaded";
		} catch (IOException e) {
			System.out.println("Error, retrieving Img from: " + uri.toString());
			e.printStackTrace();
			return "!!!!!!!!!!!!!!!!!!!!!!!!!!"+"\n"+"Error, retrieving Img from: " + uri.toString()+"\n!!!!!!!!!!!!!!!!!!!!!!!!!!\n";
		} catch(Exception e) {
			e.printStackTrace();
			return "!!!!!!!!!!!!!!!!!!!!!!!!!!"+"\n"+"Error, retrieving Img from: " + uri.toString()+"\n!!!!!!!!!!!!!!!!!!!!!!!!!!\n";
		}
	}
	
	public String writeToFile() throws NoImageSavedException{
		
		 if(faceDetectionEnabled) {
        	 if (!hasFace) {
  	            System.out.println("Bild Uebersprungen\\n");
  	            return "Skipped, because of missing face detection";
  	        }
  	        
        }
		 
		normalizeTitle();
		fileDir = new File(createFileName());
		targetDirectory = Paths.get(fileDir.getAbsolutePath());
		System.out.println(fileDir.getAbsolutePath());
		
		
		
		try {
			if (img == null) {
				throw new NoImageSavedException("No Image was saved temporarilly via method 'saveImageTemporally(URL src)'");
				
			}
			
			ImageIO.write(img, "png", fileDir);
			fileSize = (double) fileDir.length() / (1024 * 1024);
			
			System.out.println("image written to: "+ targetDirectory + " FileSize: " + fileSize);
			log.add(new DownloadEntry(sourceURL, targetDirectory, fileSize, imgHeight, imgWidth, faceDetectionEnabled, true, LocalDateTime.now()));
			return "image written to: "+ targetDirectory + " FileSize: " + fileSize +"\n";
		} catch (IOException e) {
			System.out.println("Error. saving img to: " + fileDir.getPath());
			e.printStackTrace();
			log.add(new DownloadEntry(sourceURL, targetDirectory, 0, imgHeight, imgWidth, faceDetectionEnabled , false, LocalDateTime.now()));
			return "!!!!!!!!!!!!!!!!!!!!!!!!!!\nError. saving img to: " + fileDir.getPath() + "\n!!!!!!!!!!!!!!!!!!!!!!!!!!\n";
		} catch (NoImageSavedException e) {
			return "!!!!!!!!!!!!!!!!!!!!!!!!!!\nError. saving img to: " + fileDir.getPath() + "\n!!!!!!!!!!!!!!!!!!!!!!!!!!\n";
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
	
	private void normalizeTitle() {
		fileTitle = fileTitle.replaceAll("\\s+", "_");
		
	}
	
	public void setFaceDetectionEnabled(boolean enabled) {
		this.faceDetectionEnabled = enabled; 
		}
	
	
}
