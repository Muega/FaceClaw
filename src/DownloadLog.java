import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DownloadLog {
	
    private final List<DownloadEntry> history = new ArrayList<>();
    private final Map<Path, DownloadEntry> byPath = new LinkedHashMap<>();
    private final Map<String, DownloadEntry> byUrl  = new LinkedHashMap<>();
    
    //publics
    public boolean alreadyDownloaded(Path path) {
    	
        return byPath.containsKey(normalize(path));
    }
    
    public boolean alreadyDownloadedUrl(String url) {
    	
        return byUrl.containsKey(normalizeUrl(url));
    }

    public void add(DownloadEntry entry) {
    	
        Path path = normalize(entry.path());
        history.add(entry);
        byPath.put(path, entry);
        byUrl.put(normalizeUrl(entry.url()), entry);
    }

    //privates
    private static Path normalize(Path p) {
    	
        return p.toAbsolutePath().normalize();
    }
    private static String normalizeUrl(String u) {
    	
        return u.trim();
    }

    public List<DownloadEntry> history() {
    	
    	return List.copyOf(history); 
    }
}
