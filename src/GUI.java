import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.List;

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
import javax.swing.JCheckBox;
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

public class GUI {

	//Atributes
	//***********************************************
	private SwingWorker<Void, String> currentWorker;
	
	private Claw claw;
	private boolean clawStarted = false;
	
	private FileReader fileReader;
	private BufferedReader buffReader;
	private File settingsFile;
	private boolean settingsExisted;
	String readLine;
	String[] settingsArr;
	int readInt;
	Path readPath;
	File readPathFile;
	
	private JFrame frame;
	private ImageIcon clawImg;
	
	private JLabel searchLabel;
	private JTextField searchTextfield;
	private JCheckBox faceDetectCheck;
	
	private JLabel maxImages;
	private JSpinner maxImagesSpinner;
	private JButton submitSearch;
	
	private JLabel pathLabel;
	private JButton openFileChooser;
	
	private JFileChooser fileChooser;
	
	private JTextArea logArea;
	private JScrollPane scroll;
	
	private ImageIcon soundImg;
	private ImageIcon noSoundImg;
	
	private JButton musicToggle;
	private boolean musicOn;
	
	private File audioFile;
	private AudioInputStream audioIn;
	private Clip clip;
	private FloatControl gainControl;
	
	private String dirPath = "";
	private String searchtext = "";
	
	private JProgressBar progressBar;
	protected GUI(Claw claw){
		this.claw = claw;
	}
	
	protected void run() {
		
		//Initializing Settings Tools
				settingsFile = new File("./ressources/settings.data");
				
				
					if(!settingsFile.exists()) {
						try {
							settingsFile.createNewFile();
						} catch (IOException e) {
							e.printStackTrace();
						}
						settingsExisted = false;
					}else {
						settingsExisted = true;
					}
				
		
		//GUI Design
		//**************************************************************
		frame = new JFrame("Claw");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		clawImg = new ImageIcon("./ressources/claw.png");
		frame.setIconImage(clawImg.getImage());

		//BorderLayout
		JPanel root = new JPanel(new java.awt.BorderLayout(8,8));
		root.setBorder(javax.swing.BorderFactory.createEmptyBorder(8,8,8,8));
		frame.setContentPane(root);

		//Top: Eingaben + Controls
		JPanel top = new JPanel();
		top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
		root.add(top, java.awt.BorderLayout.NORTH);

		//row1: Label + Textfield
		JPanel row1 = new JPanel();
		row1.setLayout(new BoxLayout(row1, BoxLayout.X_AXIS));
		searchLabel = new JLabel("Search for images:");
		searchTextfield = new JTextField();

		//Textfield wider
		searchTextfield.setMaximumSize(
		    new java.awt.Dimension(Integer.MAX_VALUE, searchTextfield.getPreferredSize().height)
		);

		row1.add(searchLabel);
		row1.add(Box.createHorizontalStrut(8));
		row1.add(searchTextfield);
		top.add(row1);
		top.add(Box.createVerticalStrut(8));

		//row2: Max Images + Spinner + Submit + FaceDetectCheck
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
		
		faceDetectCheck = new JCheckBox("Face detection");
		faceDetectCheck.setSelected(true); 

		row2.add(Box.createHorizontalStrut(12));
		row2.add(faceDetectCheck);
		row2.add(Box.createHorizontalGlue());
		row2.add(submitSearch);

		//row3: path + filechooser
		JPanel row3 = new JPanel();
		row3.setLayout(new BoxLayout(row3, BoxLayout.X_AXIS));
		dirPath = Paths.get("").toAbsolutePath().toString();
		pathLabel = new JLabel("Directory: " + dirPath);
		openFileChooser = new JButton("change");

		//Label flex, Button static
		pathLabel.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, pathLabel.getPreferredSize().height));
		row3.add(pathLabel);
		row3.add(Box.createHorizontalStrut(8));
		row3.add(openFileChooser);
		top.add(row3);

		//Center: Log flex
		logArea = new JTextArea(10, 40);
		logArea.setEditable(false);
		scroll = new JScrollPane(logArea);
		root.add(scroll, java.awt.BorderLayout.CENTER);

		//Autoscroll logArea
		javax.swing.text.DefaultCaret caret = (javax.swing.text.DefaultCaret) logArea.getCaret();
		caret.setUpdatePolicy(javax.swing.text.DefaultCaret.ALWAYS_UPDATE);

		//South: music + progressbar
		JPanel south = new JPanel(new BorderLayout(8, 0));
		
		soundImg = new ImageIcon("./ressources/Sound.png");
		noSoundImg = new ImageIcon("./ressources/noSound.png");
		musicToggle = new JButton(soundImg);
		
		progressBar = new JProgressBar(0, 100);
		//Bar auto thin
		int barH = 12;
		progressBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, barH));
		progressBar.setPreferredSize(new Dimension(100, barH));
		progressBar.setMinimumSize(new Dimension(10, barH));
		progressBar.setStringPainted(true);       
		progressBar.setValue(0);
		progressBar.setIndeterminate(false);  
		
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
		
		//Listeners
		//*******************************************************
		musicOn = true;
		musicToggle.addActionListener(action -> toggleMusic());
		
		//Read Settings
		readSettings();
		
		//File Path Selector
		openFileChooser.addActionListener(e ->{
			 fileChooser = new JFileChooser(dirPath);

            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

            int r = fileChooser.showSaveDialog(null);

            if (r == JFileChooser.APPROVE_OPTION) {
            	dirPath = fileChooser.getSelectedFile().getAbsolutePath();
                pathLabel.setText("Directory: " + dirPath);
            }
		});
		
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
			
			//save Settings
			saveSettings();
			
			searchtext = searchTextfield.getText();
			
			if (searchtext.isEmpty()) return;
			
			 submitSearch.setEnabled(false);	
			 submitSearch.setEnabled(false);
			 faceDetectCheck.setEnabled(false);
			 
			 
			//Worker Tasks
			 //*********************************
			 currentWorker = new SwingWorker<Void, String>() {
				 
				 @Override protected Void doInBackground() throws Exception {
					 claw.setLogger(this::publish);
				     claw.setProgressCallback(this::setProgress);
				     claw.setFaceDetectionEnabled(faceDetectCheck.isSelected());
					 
					 claw.start();
					 GUI.this.startSearch(searchtext, dirPath + "\\", (Integer) maxImagesSpinner.getValue());
					 return null;
				 }
				 @Override protected void process(List<String> chunks) {
			            chunks.forEach(s -> logArea.append(s + "\n"));
			     }
				 @Override protected void done() {
						 try {
							 claw.saveDownloadLog();
						 }catch(java.lang.NullPointerException e) {
							 logArea.append("No Dowloadlist created. No entrys found.\n");
						 }
					 	
			            submitSearch.setEnabled(true);
			            submitSearch.setEnabled(true);
			            faceDetectCheck.setEnabled(true);
			            setProgress(100);
			            try { get(); } catch (Exception ex) {
			            	logArea.append("*************************************************\nFehler: " + ex.getMessage() + "\n Error led to stop of process\n*************************************************\n");
			                //JOptionPane.showMessageDialog(frame, "Fehler: " + ex.getMessage());
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
	
	private void readSettings() {
		
		try{
			fileReader = new FileReader(settingsFile);
			buffReader = new BufferedReader(fileReader);
		
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} 
		
		//Settings Read
		//1.) maxImages for JSpinner
		//2.) Path
		
		//load settings
		if(settingsExisted) {
			
			
			readLine = "";
			settingsArr = new String[2];
			int i = 0;
			try {
				while((readLine = buffReader.readLine()) != null) {
					settingsArr[i] = readLine;
					i++;
					if(i>1) {
						break;
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			//maxImages Check
			try {
				if(settingsArr[0] == null) {
					throw new NullPointerException("First line of data File is null");
				}
				readInt = Integer.parseInt(settingsArr[0]);
				if(readInt >= 1 && readInt <= 9999) {
					maxImagesSpinner.setValue(readInt);
				}
			}catch(NumberFormatException e) {
				logArea.append("Save data corrupted. Invalid Number loaded\n");
				e.printStackTrace();
			}catch (NullPointerException e) {
				logArea.append("No save data (1) loaded\n");
			}
			
			//Path Check
			try {
				if(settingsArr[1] == null) {
					throw new NullPointerException("Second line of data File is null");
				}
				readPathFile = new File(settingsArr[1]);
				if(readPathFile.exists()) {
					dirPath = readPathFile.getAbsolutePath();
					pathLabel.setText("Directory: " + dirPath);
					
				}else {
					logArea.append("Save data (2) invalid\n");
				}
			}catch(NullPointerException e) {
				logArea.append("No save data (2) loaded\n");
			}
			
			try {
			    if (settingsArr[2] != null) {
			        boolean on = Boolean.parseBoolean(settingsArr[2].trim());
			        faceDetectCheck.setSelected(on);
			    }
			} catch (Exception ignored) {
			    //Falls alte Datei nur 2 Zeilen hat
			}
			
		}else {
			logArea.append("No save file found\nNew save file created\n");
		}
		
		
		try {
			buffReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void saveSettings() {

		 try (FileWriter fw = new FileWriter(settingsFile, false); 
			  PrintWriter pw = new PrintWriter(fw)) {

		        pw.println(maxImagesSpinner.getValue().toString());
		        pw.println(dirPath);   
		        pw.println(faceDetectCheck.isSelected());
		        pw.flush();
				pw.close();
		    } catch (IOException e) {
		        e.printStackTrace();
		        logArea.append("Fehler beim Speichern der Einstellungen\n");
		    }
		
		
	}
}
