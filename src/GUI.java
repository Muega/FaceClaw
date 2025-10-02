import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.List;
import java.util.function.IntConsumer;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingWorker;
import javax.swing.plaf.ProgressBarUI;

public class GUI {

	private SwingWorker<Void, String> currentWorker;
	
	Claw claw;
	boolean clawStarted = false;
	
	JFrame frame;
	JPanel layout;
	ImageIcon clawImg;
	
	JLabel searchLabel;
	JTextField searchTextfield;
	
	JPanel maxImagesContainer;
	JLabel maxImages;
	JSpinner maxImagesSpinner;
	JButton submitSearch;
	
	JPanel selectPathContainer;
	JLabel pathLabel;
	JButton openFileChooser;
	
	JFileChooser fileChooser;
	
	private JTextArea logArea;
	private JScrollPane scroll;
	
	ImageIcon soundImg;
	ImageIcon noSoundImg;
	
	JButton musicToggle;
	boolean musicOn;
	
	File audioFile;
	AudioInputStream audioIn;
	Clip clip;
	FloatControl gainControl;
	
	String dirPath = "";
	String searchtext = "";
	
	JProgressBar progressBar;
	GUI(Claw claw){
		this.claw = claw;
	}
	
	public void run() {
		
		//GUI Design
		frame = new JFrame("Claw");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		clawImg = new ImageIcon("./ressources/claw.png");
		frame.setIconImage(clawImg.getImage());

		// Außen: BorderLayout
		JPanel root = new JPanel(new java.awt.BorderLayout(8,8));
		root.setBorder(javax.swing.BorderFactory.createEmptyBorder(8,8,8,8));
		frame.setContentPane(root);

		// === TOP-Bereich: Eingaben + Controls ===
		JPanel top = new JPanel();
		top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
		root.add(top, java.awt.BorderLayout.NORTH);

		// Zeile 1: Label + Textfield
		JPanel row1 = new JPanel();
		row1.setLayout(new BoxLayout(row1, BoxLayout.X_AXIS));
		searchLabel = new JLabel("Search for images:");
		searchTextfield = new JTextField();

		// Textfield darf breiter werden:
		searchTextfield.setMaximumSize(
		    new java.awt.Dimension(Integer.MAX_VALUE, searchTextfield.getPreferredSize().height)
		);

		row1.add(searchLabel);
		row1.add(Box.createHorizontalStrut(8));
		row1.add(searchTextfield);
		top.add(row1);
		top.add(Box.createVerticalStrut(8));

		// Zeile 2: Max Images + Spinner + Submit
		JPanel row2 = new JPanel();
		row2.setLayout(new BoxLayout(row2, BoxLayout.X_AXIS));
		maxImages = new JLabel("Maximum amount of images:");
		maxImagesSpinner = new JSpinner(new SpinnerNumberModel(10, 1, 9999, 1));
		submitSearch = new JButton("search");

		row2.add(maxImages);
		row2.add(Box.createHorizontalStrut(8));
		row2.add(maxImagesSpinner);
		row2.add(Box.createHorizontalGlue());   // schiebt Submit nach rechts
		row2.add(submitSearch);
		top.add(row2);
		top.add(Box.createVerticalStrut(8));

		// Zeile 3: Pfadwahl
		JPanel row3 = new JPanel();
		row3.setLayout(new BoxLayout(row3, BoxLayout.X_AXIS));
		dirPath = Paths.get("").toAbsolutePath().toString();
		pathLabel = new JLabel("Directory: " + dirPath);
		openFileChooser = new JButton("change");

		// Label darf expandieren, Button bleibt kompakt
		pathLabel.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, pathLabel.getPreferredSize().height));
		row3.add(pathLabel);
		row3.add(Box.createHorizontalStrut(8));
		row3.add(openFileChooser);
		top.add(row3);

		// === CENTER: Log wächst mit ===
		logArea = new JTextArea(10, 40);
		logArea.setEditable(false);
		scroll = new JScrollPane(logArea);
		root.add(scroll, java.awt.BorderLayout.CENTER);

		// Auto-Scroll: immer ans Ende springen, wenn Text kommt
		javax.swing.text.DefaultCaret caret = (javax.swing.text.DefaultCaret) logArea.getCaret();
		caret.setUpdatePolicy(javax.swing.text.DefaultCaret.ALWAYS_UPDATE);

		//South
		JPanel south = new JPanel(new BorderLayout(8, 0));
		
		soundImg = new ImageIcon("./ressources/Sound.png");
		noSoundImg = new ImageIcon("./ressources/noSound.png");
		musicToggle = new JButton(soundImg);
		
		progressBar = new JProgressBar(0, 100);
		// Bar selbst dünn halten
		int barH = 12;
		progressBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, barH));
		progressBar.setPreferredSize(new Dimension(100, barH));
		progressBar.setMinimumSize(new Dimension(10, barH));
		progressBar.setStringPainted(true);        // Prozent anzeigen
		progressBar.setValue(0);
		progressBar.setIndeterminate(false);  
		
		// Wrapper verhindert vertikales Strecken der Bar
		JPanel pbWrapper = new JPanel();
		pbWrapper.setLayout(new BoxLayout(pbWrapper, BoxLayout.Y_AXIS));
		pbWrapper.add(Box.createVerticalGlue());
		pbWrapper.add(progressBar);
		pbWrapper.add(Box.createVerticalGlue());

		south.add(pbWrapper, BorderLayout.CENTER);
		south.add(musicToggle, BorderLayout.EAST);

		root.add(south, BorderLayout.SOUTH);
		
		frame.pack();
		frame.setMinimumSize(frame.getSize());       
		frame.setLocationRelativeTo(null);           
		frame.setVisible(true);
		
		
		//Audio
		audioFile = new File("./ressources/audio.wav");
		try {
			audioIn = AudioSystem.getAudioInputStream(audioFile);
			clip = AudioSystem.getClip();
			clip.open(audioIn);
			gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
			gainControl.setValue(-30.0f);
			clip.loop(Clip.LOOP_CONTINUOUSLY);
			clip.start();
			
			
		} catch (UnsupportedAudioFileException e1) {
			e1.printStackTrace();
		} catch (IOException e2) {
			e2.printStackTrace();
		} catch (LineUnavailableException e3) {
			e3.printStackTrace();
		}
		
		
		musicOn = true;
		musicToggle.addActionListener(action -> toggleMusic());
		
		//File Path Selector
		openFileChooser.addActionListener(e ->{
			 fileChooser = new JFileChooser(dirPath);

            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

            int r = fileChooser.showSaveDialog(null);

            if (r == JFileChooser.APPROVE_OPTION) {
            	dirPath = fileChooser.getSelectedFile().getAbsolutePath() + "\\";
                pathLabel.setText("Directory: " + dirPath);
            }
		});
		
		//Submit Logic
		submitSearch.addActionListener(e -> {
			
			if (currentWorker != null && !currentWorker.isDone()) {
		        JOptionPane.showMessageDialog(frame, "Suche läuft noch – bitte warten oder abbrechen.");
		        return;
		    }
			
			try {
				maxImagesSpinner.commitEdit();
			} catch (ParseException e1) {
				e1.printStackTrace();
			}
			searchtext = searchTextfield.getText();
			
			if (searchtext.isEmpty()) return;
			
			 submitSearch.setEnabled(false);
			 
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
			 
			 
			 
			//Worker Tasks
			 currentWorker = new SwingWorker<Void, String>() {
				 
				 @Override protected Void doInBackground() throws Exception {
					 claw.setLogger(this::publish);
				     claw.setProgressCallback(this::setProgress);

					 
					 claw.start();
					 GUI.this.startSearch(searchtext, dirPath, (Integer) maxImagesSpinner.getValue());
					 return null;
				 }
				 @Override protected void process(List<String> chunks) {
			            chunks.forEach(s -> logArea.append(s + "\n"));
			     }
				 @Override protected void done() {
			            submitSearch.setEnabled(true);
			            setProgress(100);
			            try { get(); } catch (Exception ex) {
			            	logArea.append("Fehler: " + ex.getMessage() + "\n");
			                JOptionPane.showMessageDialog(frame, "Fehler: " + ex.getMessage());
			            }
			            
			     }
				 
			 };
			 
			 currentWorker.addPropertyChangeListener(evt -> {
			    if ("progress".equals(evt.getPropertyName())) {
			        progressBar.setIndeterminate(false);
			        progressBar.setValue((Integer) evt.getNewValue());
			    }
			 });
			 currentWorker.execute();

		});
		
		
//		layout.add(searchLabel);
//		layout.add(searchTextfield);
//		
//		maxImagesContainer.add(Box.createHorizontalStrut(10));
//		maxImagesContainer.add(maxImages);
//		maxImagesContainer.add(Box.createHorizontalStrut(60));
//		maxImagesContainer.add(maxImagesSpinner);
//		layout.add(maxImagesContainer);
//		
//		layout.add(submitSearch);
//		
//		selectPathContainer.add(Box.createHorizontalStrut(10));
//		selectPathContainer.add(pathLabel);
//		selectPathContainer.add(Box.createHorizontalStrut(10));
//		selectPathContainer.add(openFileChooser);
//		layout.add(selectPathContainer);
//		layout.add(scroll);
//		layout.add(musicToggle);
//		musicToggle.setEnabled(true);
	}
	
	//private methods
	private void toggleMusic() {
		
		if(musicOn) {
			musicToggle.setIcon(noSoundImg);;
			clip.stop();
		}else {
			 musicToggle.setIcon(soundImg);
			 clip.start();
			 clip.loop(Clip.LOOP_CONTINUOUSLY);
		}
		musicOn = !musicOn;
	}
	
	private void startSearch(String search, String dirPath, int maxImages) {
		if(!clawStarted) {
			claw.startSearch(search, dirPath, maxImages);
			clawStarted = true; 
		}else {
			claw.startSearch(search, dirPath, maxImages);
		}
	
		
	}
}
