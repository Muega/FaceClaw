import java.nio.file.Path;
import java.time.Instant;

public record DownloadEntry(
		 	String url,
		    Path path,
		    double sizeMB,
		    int imgHeight,
		    int imgWidth,
		    boolean success,
		    Instant timestamp) {

}
