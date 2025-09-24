import java.awt.event.ActionEvent;
import java.util.function.BiConsumer;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class GUI {

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
	
	JButton musicToggle;
	boolean musicOn;
	
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
		submitSearch.addActionListener((ActionEvent e) -> {searchtext = searchTextfield.getText(); startSearch(searchtext); 
		});
		
		musicToggle = new JButton("Off");
		musicOn = false;
		musicToggle.addActionListener(action -> toggleMusic());
		
		layout.add(searchLabel);
		layout.add(searchTextfield);
		layout.add(submitSearch);
		layout.add(submitSearch);
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
	
	private void startSearch(String search) {
		if(!clawStarted) {
			claw.run();
			clawStarted = true;
		}
		//TODO LAzy machen
		claw.startSearch(search);
		
	}
}
