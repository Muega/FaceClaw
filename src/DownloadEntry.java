import java.nio.file.Path;
import java.time.LocalDateTime;

public record DownloadEntry(
		 	String url,
		    Path path,
		    double sizeMB,
		    int imgHeight,
		    int imgWidth,
		    boolean faceCheckEnabled,
		    boolean success,
		    LocalDateTime timestamp) {

}
