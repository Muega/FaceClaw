import java.nio.file.Paths;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileSystemView;

public class GUI {

	private SwingWorker<Void, String> currentWorker;
	
	Claw claw;
	boolean clawStarted = false;
	
	JFrame frame;
	JPanel layout;
	//TODO Frame Icon
	
	private final int width = 400;
	private final int height = 500;
	
	JLabel searchLabel;
	JTextField searchTextfield;
	JButton submitSearch;
	JLabel pathLabel;
	JButton openFileChooser;
	
	JFileChooser fileChooser;
	
	private JTextArea logArea;
	private JScrollPane scroll;
	
	JButton musicToggle;
	boolean musicOn;
	
	String dirPath = "";
	String searchtext = "";
	GUI(Claw claw){
		this.claw = claw;
	}
	
	public void run() {
		frame = new JFrame("Claw");
		frame.setVisible(true);
		frame.setSize(width, height);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		layout = new JPanel();
		layout.setLayout(new BoxLayout(layout, BoxLayout.Y_AXIS));
		frame.add(layout);
		
		searchLabel = new JLabel("Search for images:");
		searchTextfield = new JTextField();
		submitSearch = new JButton("search");
		dirPath = Paths.get("").toAbsolutePath().toString();
		pathLabel = new JLabel("Directory: " + dirPath);
		openFileChooser = new JButton("change");
		logArea = new JTextArea(10,40);
		scroll = new JScrollPane(logArea);
		
		openFileChooser.addActionListener(e ->{
			 fileChooser = new JFileChooser(dirPath);

            // set the selection mode to directories only
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

            // invoke the showsSaveDialog function to show the save dialog
            int r = fileChooser.showSaveDialog(null);

            if (r == JFileChooser.APPROVE_OPTION) {
                // set the label to the path of the selected directory
            	dirPath = fileChooser.getSelectedFile().getAbsolutePath() + "\\";
                pathLabel.setText("Directory: " + dirPath);
            }
		});
		
		submitSearch.addActionListener(e -> {
			
			if (currentWorker != null && !currentWorker.isDone()) {
		        JOptionPane.showMessageDialog(frame, "Suche läuft noch – bitte warten oder abbrechen.");
		        return;
		    }
			
			searchtext = searchTextfield.getText();
			
			if (searchtext.isEmpty()) return;
			
			 submitSearch.setEnabled(false);
			
			 currentWorker = new SwingWorker<Void, String>() {
				 
				 @Override protected Void doInBackground() throws Exception {
					 claw.start();
					 startSearch(searchtext, dirPath);
					 return null;
				 }
				 @Override protected void process(List<String> chunks) {
			            chunks.forEach(s -> logArea.append(s + "\n"));
			     }
				 @Override protected void done() {
			            submitSearch.setEnabled(true);
			            try { get(); } catch (Exception ex) {
			                JOptionPane.showMessageDialog(frame, "Fehler: " + ex.getMessage());
			            }
			     }
				 
			 };
			 
			 //Close Window + Driver
			 frame.addWindowListener(new java.awt.event.WindowAdapter() {
				    @Override
				    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
				        if (clawStarted) {
				        	claw.quit();
				            System.exit(0);
				        }else {
				        	System.exit(0);
				        }
				    }
				});
			 
			 currentWorker.execute();

		});
		
		musicToggle = new JButton("Off");
		musicOn = false;
		musicToggle.addActionListener(action -> toggleMusic());
		
		layout.add(searchLabel);
		layout.add(searchTextfield);
		layout.add(submitSearch);
		layout.add(pathLabel);
		layout.add(openFileChooser);
		layout.add(scroll);
		layout.add(musicToggle);
	}
	
	private void toggleMusic() {
		musicOn = !musicOn;
		if(musicOn) {
			musicToggle.setText("On");
		}else {
			 musicToggle.setText("Off");
		}
	}
	
	private void startSearch(String search, String dirPath) {
		if(!clawStarted) {
			claw.startSearch(search, dirPath);
			clawStarted = true; 
		}
		//TODO LAzy machen
		claw.startSearch(search, dirPath);
		
	}
}
