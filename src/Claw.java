import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

import javax.swing.JOptionPane;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class Claw {

	//Attributes
	//******************************************************
	
	//Driver & Tools
	ImageSaver is;
	WebDriver driver;
	WebDriverWait driverWait;
	
	Actions actions;
	
	//Debugger ScriptExecutor
	JavascriptExecutor js;
	int idx;
	
	private volatile boolean stop;
	private volatile boolean faceDetectionEnabled;
	
	//Variables
	String objectCheck;
	int currentLoop = 0;
	int maxLoops = 10;
	int percent = 0;
	
	ArrayList<String> tabs;
	String searchTab;
	
	boolean cookieExpired;
	//Files
	File cookieFile;
	
	
	String dirPath;
	
	//WebElements
	WebElement googleSearchBar;
	String searchtext;
	
	WebElement imageGrid;
	ArrayList<WebElement> imageContainerList;
	
	WebElement imageContainerChild;
	
	
	WebElement currentImageIngres;
	String currentIngressURL;
	
	WebElement imageElement;
	String imageURL;
	
	DownloadLog dlLog;
	DateTimeFormatter realFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	DateTimeFormatter pathFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
	LocalDateTime timestamp;
	
	
	
	//GUI
	 private Consumer<String> log;
	 private IntConsumer progressCb;
	 
	
	//Start Method starts Driver and prepares for Crawling with Method startSearch()
	//*****************************************************************************
	public void start() {
		
		
		if(driver!= null) return;
		startDriver();
		startDriverWait();
		startActions();
		startDebuggerJSExecutor();
		
		log("Services started");

		
		
		driver.get("https://www.google.de/imghp?hl=de&ogbl");
		
		waitUntilReady();
		checkCookies();
		
		
		
        //TODO alle Fehler catchen edit: zumindest die meisten wurden gecatcht haha
	
	}
	
	
	//private start() methods
	
	private void startDriver() {
		 driver = new ChromeDriver();
		 tabs = new ArrayList<>(driver.getWindowHandles());
		 driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(60));
		 log("Driver started");
	}
	
	private void startDriverWait() {
		driverWait = new WebDriverWait(driver, Duration.ofSeconds(15));
	}
	
	private void startActions() {
		actions = new Actions(driver);
	}
	
	private void startDebuggerJSExecutor() {
		js = (JavascriptExecutor) driver;
		idx = 0;
	}
	
	private void waitUntilReady() {
		 log("Waiting for page to be loaded...");

		driverWait.until(d -> ((JavascriptExecutor) d).executeScript("return document.readyState").equals("complete"));

		driverWait.until(ExpectedConditions.presenceOfElementLocated(By.className("gLFyf")));   
		
	}
	
	private void checkCookies() {
	    cookieExpired = false;
	    log("Trying to retrieve cookies...");

	    File cookieFile = new File("./ressources/cookies.data");
	    if (!cookieFile.exists()) {
	        log("No cookie file found.");
	        return;
	    }

	    try (BufferedReader br = new BufferedReader(new FileReader(cookieFile))) {
	        String line;
	        Date now = new Date();
	        DateFormat df = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.GERMAN);

	        while ((line = br.readLine()) != null) {
	            line = line.trim();
	            if (line.isEmpty()) continue;

	            
	            String[] parts = line.split(";", 6);
	            if (parts.length < 6) {
	                System.err.println("Invalid cookie line: " + line);
	                continue;
	            }

	            String name = parts[0];
	            String value = parts[1];
	            String domain = parts[2];
	            String path = parts[3];
	            String expiryStr = parts[4];
	            String secureStr = parts[5];

	            Date expiry = null;
	            if (!"null".equalsIgnoreCase(expiryStr)) {
	                try {
	                    expiry = df.parse(expiryStr);
	                } catch (ParseException e) {
	                    System.err.println("Could not parse expiry date for cookie " + name);
	                    continue;
	                }
	            }

	            if (expiry != null && expiry.before(now)) {
	                cookieExpired = true;
	                continue;
	            }

	            boolean isSecure = Boolean.parseBoolean(secureStr);

	            org.openqa.selenium.Cookie ck = new org.openqa.selenium.Cookie(
	                    name, value, domain, path, expiry, isSecure
	            );

	            driver.manage().addCookie(ck);
	        }

	        log("Cookies loaded successfully.");

	    } catch (IOException e) {
	        e.printStackTrace();
	        log("Error loading cookies: " + e.getMessage());
	    }
	}
	    		
	    	
	
	private void hoverOverElement(WebElement elm) {
		actions.moveToElement(elm).perform();
	}
	
	public void setSearchText(String searchText) {
		this.searchtext = searchText;
	}
	
	//startSearch activates the Crawling Process - also invokes processResult() (the second part of Crawling)
	//*****************************************************************************************************
	public void startSearch(String search, String dirPath, int maxImages) {
		
		stop = false;
		
		this.dirPath = dirPath;
		this.maxLoops = maxImages;
	    driver.get("https://www.google.de/imghp?hl=de&ogbl");
		
		googleSearchBar = driverWait.until(ExpectedConditions.visibilityOfElementLocated(By.className("gLFyf")));
		
		
		setSearchText(search);
		
		try {
			googleSearchBar.click();
		} catch (org.openqa.selenium.ElementClickInterceptedException ex) {
		    handleCookies(driver);
		    saveCookies();
		    googleSearchBar.click();
		}
		
		googleSearchBar.click();
		
		actions
        .sendKeys(this.searchtext)
        .perform();
		

		actions
		.sendKeys(Keys.RETURN)
		.perform();
		log("Searching for: " + search + " | " + maxImages + " result(s) maximum");
		
		processResult(log);
		
		
	}
	private void processResult(Consumer<String> log) {
		
		dlLog = new DownloadLog();
		
		//Captcha Abfrage (1)
		boolean captchaPresent = false;
		try {
			
		    WebElement captchaForm = driver.findElement(By.cssSelector("#captcha-form"));
		    captchaPresent = true;
		} catch (org.openqa.selenium.NoSuchElementException ignored) {}

		if (captchaPresent) {
		    
		    
		    JOptionPane.showMessageDialog(null,
		        "Captcha detected. Please solve Captcha to show you are a human. Afterwards click 'Ok'");

		    
		    WebDriverWait longWait = new WebDriverWait(driver, Duration.ofMinutes(10)); 
		    longWait.until(d -> {
		        try {
		            return d.findElements(By.cssSelector("#captcha-form")).isEmpty();
		        } catch (Exception e) { return false; }
		    });
		}
		
		//Sammelt das Bilder Ergebnisgrid
		imageGrid = driver.findElement(By.xpath("//*[@id=\"rso\"]/div/div/div[1]/div/div"));
		
		//Liste aus Gridelementen
		imageContainerList = (ArrayList<WebElement>) imageGrid.findElements(By.xpath("./child::*")); 
		System.out.println(imageContainerList.size()+" Einträge im Elemente Grid"); 
		
		//Iteration um alle Bilder der Elemente auszuwählen
		//for(WebElement imageContainer: imageContainerList ) {
		int max = maxLoops;
		int i = -1; //Grid Element Index, wird immer erhöht
		int p = 0; //Nur Bilder Index, wird nur beim erfolgreichem Durchlauf erhöht
		//filtert leere divs etc
		while(p < max) {
			i++;
			
			System.out.println("i: "+i);
			System.out.println("p: "+p);
		    try {
		        
		    	//Check if done
		        List<WebElement> kids = imageGrid.findElements(By.xpath("./child::*"));
		        System.out.println("kids size: "+ kids.size());
		        if (p+1 >= kids.size()) {
		            loadUntilCountInGrid(imageGrid, p + 1);               
		            kids = imageGrid.findElements(By.xpath("./child::*")); 
		            if (p +1 >= kids.size()) {
		            	
		                System.out.println("Loops reached: "+ p+1);
		                if(p+1 < maxLoops) {
		                	log("Could just find "+ (p+1)+ " elements to crawl of "+maxLoops+" requested.... ");
		                }
						log("Maximum results reached!");
		                break;
		            }
		        }
		        
		        WebElement imageContainer = kids.get(i);
				
				//testet Element Tag um Bild WebElement zu identifizieren
				objectCheck = imageContainer.getTagName();
				System.out.println(objectCheck + imageContainer.getAttribute("jscontroller"));
				if(!objectCheck.equals("div")) {
					System.out.println("Continuing because Element is not a div: "+ objectCheck);
					//max++; //If object
					continue;
				}
	
				
				
				//Hovern zum generieren der lazy Daten
				actions.moveToElement(imageContainer).perform();
	
				
				
				driver.switchTo().window(tabs.get(0));
				
				//Debugger highlighted Element
				js.executeScript(
				        "arguments[0].style.outline='3px solid magenta';" +
				        "arguments[0].setAttribute('data-debug-idx', arguments[1]);", 
				        imageContainer, idx++
				    );
		
				
				//Weiter im DOM nach img src path graben
				try {
					currentImageIngres = driverWait.until(
							ExpectedConditions.presenceOfNestedElementLocatedBy(imageContainer, By.xpath("./div[2]/h3/a"))
						);
				}catch(org.openqa.selenium.NoSuchElementException e) {
					
					System.err.println("NoSuchElementException no WebElement <a> could be found in Div");
					log("Skipping element because no Image could be found");
					continue;
				}catch(org.openqa.selenium.TimeoutException e) {
					System.err.println("Timeout Exception no WebElement <a> could be found in Div");
					log("Skipping element because no Image could be found in time");
					continue;
				}
				
				
				log("\n---------------------------------------------------");
				log("Crawling: "+(p+1) + "/" + maxLoops);
				log("---------------------------------------------------");
				
				currentIngressURL =  currentImageIngres.getAttribute("href");
				
				System.out.println("Abgerufene Url des Elements:  "+currentIngressURL);
				log("Url loading: " + currentIngressURL);
				
			
				if(currentIngressURL == null) {
					log("Skipping because URL was not retrieved");
					System.out.println("URL was null, skipping loop");
					continue;
				}
				//Switch tabs to image to download directly from normal resolution img file
				searchTab = openInNewTabAndSwitch(driver, currentIngressURL, Duration.ofSeconds(10));
				
				System.out.println("Wait for page to be loaded");
				driverWait.until(d -> ((JavascriptExecutor) d).executeScript("return document.readyState").equals("complete"));
				
				//Captcha Abfrage (2)
				captchaPresent = false;
				try {
					
				    WebElement captchaForm = driver.findElement(By.cssSelector("#captcha-form"));
				    captchaPresent = true;
				} catch (org.openqa.selenium.NoSuchElementException ignored) {}

				if (captchaPresent) {
				    
				    
				    JOptionPane.showMessageDialog(null,
				        "Captcha detected. Please solve Captcha to show you are a human. Afterwards click 'Ok'");

				    
				    WebDriverWait longWait = new WebDriverWait(driver, Duration.ofMinutes(10)); 
				    longWait.until(d -> {
				        try {
				            return d.findElements(By.cssSelector("#captcha-form")).isEmpty();
				        } catch (Exception e) { return false; }
				    });
				}
				
				
				System.out.println("Wait until WebElement c-wiz/div is found");
				
				try {
					hoverOverElement(driverWait.until(
							ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id=\"imp\"]/div[1]/div[1]/div[2]/div/div[2]/c-wiz/div"))
						));
				}catch(org.openqa.selenium.NoSuchElementException e) {
					
					System.err.println("NoSuchElement Exception no WebElement c-wiz/div could be found in Div and couldn't be hovered");
					log("Skipping element because the Element couldn't be selected");
					continue;
				}catch(org.openqa.selenium.TimeoutException e) {
					System.err.println("Timeout Exception no WebElement <a> could be found in Div");
					log("Skipping element because no Image could be found in time");
					continue;
				}
				
				System.out.println("Wait until the child Element imageElement is found");
				
				try {
					imageElement = driverWait.until(
							ExpectedConditions.presenceOfElementLocated(By.cssSelector("#imp c-wiz > div > div > div:nth-of-type(2) > div > a > img:nth-of-type(1)"))
							);
				}catch(org.openqa.selenium.NoSuchElementException e) {
					
					System.err.println("NoSuchElement Exception no WebElement img could be found in Div");
					log("Skipping element because no Image could be found");
					continue;
				}catch(org.openqa.selenium.TimeoutException e) {
					System.err.println("Timeout Exception no WebElement <a> could be found in Div");
					log("Skipping element because no Image could be found in time");
					continue;
				}
				
				imageURL = imageElement.getAttribute("src");
				log("Imagesource: " + imageURL);
				
				
				//Image Saver (1) saves images temporarily and (2) writes images into directory
				is = new ImageSaver(dirPath, searchtext, dlLog);
				is.setFaceDetectionEnabled(faceDetectionEnabled);

				
				try {
					System.out.println(dirPath);
					log(is.saveImageTemporally(new URI(imageURL)));
	
					log(is.writeToFile());
					
					
				} catch (NoImageSavedException e) {
					System.out.println("You fucked up noimg");
					e.printStackTrace();
				} catch(Exception e) {
					System.out.println("You fucked up I/O");
					e.printStackTrace();
				}
				
				//Kill all other tabs
				//calulate progress for GUI
				//Increase second Iterator
				driver.switchTo().window(searchTab);
				closeAllOtherTabs(driver);
				percent = (int)(((double)(p+1)/ (double) maxLoops)*100);
				System.out.println("percent: "+percent);
				if(percent == 100) {percent = 99;}
				progress(percent);
				
				/*
				if(i+1 >= maxLoops) {
					//Greift wenn weniger maxLoops als Elemente im Grid. (Teilweise redundant)
					System.out.println("Loops reached: "+ maxLoops);
					log("Maximum results reached!");
					currentLoop = 0;
					break;
				}*/
				p++;
		    }catch (org.openqa.selenium.StaleElementReferenceException sere) {
		        i--;
		    }catch (org.openqa.selenium.ElementNotInteractableException e) { //In case of no further elements
		    	if(p+1 < maxLoops) {
                	log("Could just find "+ (p+1)+ " elements to crawl of "+maxLoops+" requested.... ");
                }
				log("Maximum results reached!");
                break;
		    }
				
		}
		System.out.println("Loops reached: "+ maxLoops);
		log("Maximum results reached!");
	}
	
	//private crawling methods
	private static String openInNewTabAndSwitch(WebDriver driver, String url, Duration timeout) {
	    Set<String> before = driver.getWindowHandles();
	    String original = driver.getWindowHandle();
	    ((JavascriptExecutor) driver).executeScript("window.open(arguments[0], '_blank');", url);

	    WebDriverWait wait = new WebDriverWait(driver, timeout);
	    wait.until(d -> d.getWindowHandles().size() > before.size());

	    Set<String> after = driver.getWindowHandles();
	    after.removeAll(before);
	    String handle = after.iterator().next();
	    driver.switchTo().window(handle);
	    return original;
	}
	
	private void closeAllOtherTabs(WebDriver driver) {
		log("Closing unnecessary tabs");
	    String current = driver.getWindowHandle();
	    for (String handle : driver.getWindowHandles()) {
	        if (!handle.equals(current)) {
	            driver.switchTo().window(handle);
	            driver.close();
	        }
	    }
	    driver.switchTo().window(current);
	}
	
	
	private void loadUntilCountInGrid(WebElement imageGrid, int targetCount) {
	    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
	    JavascriptExecutor js = (JavascriptExecutor) driver;

	    //(1) get gridCount
	    //(2) compare to last count
	    //(3) if still Elements scroll to next, else scroll to bottom
	    //(4) wait for more loading
	    //(5) if no change return
	    
	    int last = -1, noChange = 0;
	    while (true) {
	        List<WebElement> kids = imageGrid.findElements(By.xpath("./child::*"));
	        int count = kids.size();
	        if (count >= targetCount) return;

	        if (count > last) { last = count; noChange = 0; }
	        else { noChange++; }

	        
	        if (!kids.isEmpty()) {
	            WebElement tail = kids.get(kids.size() - 1);
	            try {
	                js.executeScript("arguments[0].scrollIntoView({block:'end', inline:'nearest'});", tail);
	            } catch (Exception ignore) {}
	        } else {
	            // falls noch keine Kinder da sind, einmal ans Seitenende scrollen
	            js.executeScript("window.scrollTo(0, document.body.scrollHeight)");
	        }

	        
	        try {
	            wait.until(d -> imageGrid.findElements(By.xpath("./child::*")).size() > count);
	        } catch (Exception ignore) {}

	        if (noChange >= 6) return;
	    }
	}
	
	public void saveDownloadLog() {
		List<DownloadEntry> dlHistory =  dlLog.history();
		String dlLogFileName = dirPath +  searchtext.replaceAll("\\s+", "_");
		timestamp = LocalDateTime.now();
		dlLogFileName += "_" + timestamp.format(pathFormatter) + ".txt";
		File dlLogFile = new File(dlLogFileName);
		
		if(!dlLogFile.exists()) {try {
			dlLogFile.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}}
		
		log("Saving download log...");
		 try (FileWriter fw = new FileWriter(dlLogFile, false); 
				  PrintWriter pw = new PrintWriter(fw)) {

			 pw.println("###DownloadLog###");
			 pw.println("List from " + timestamp.format(realFormatter));
			 pw.println("Images searched for text: "+searchtext+"\n");
			 
			 int listcounter = 1;
			 for (DownloadEntry entry: dlHistory) {
				 	
				 	pw.println("##############################################");
				 	pw.println("Entry " + listcounter + " of " + dlHistory.size());
				 	pw.println("##############################################\n");
				 	if(!entry.success()) {
				 		pw.println("!!!! DOWNLOAD NOT SUCCESFULL!!!\n");
				 	}
				 	pw.println("URL: " + entry.url());   
			        pw.println("Saved to: "+ entry.path());
			        pw.println("File size (Mb):  "+ entry.sizeMB());
			        pw.println("Image height: "+ entry.imgHeight());
			        pw.println("Image width: "+ entry.imgWidth());
			        pw.print("Face check enabled: ");  if (entry.faceCheckEnabled()) { pw.println("Yes");}else { pw.println("No");}
			        pw.println("Downloaded: "+ entry.timestamp().format(realFormatter) +"\n");	
			        
			        listcounter++;
					
			 }
			 pw.flush();
			 pw.close();
			       
			 log("DownloadLog saved to: "+ dirPath);
		 } catch (IOException e) {
			        e.printStackTrace();
			        log("Error saving Download log!");
		 }
		
	}
	
	private void handleCookies(WebDriver driver) {
	    WebDriverWait wait = new WebDriverWait(driver, java.time.Duration.ofSeconds(5));

	    
	    java.util.List<WebElement> buttons =
	            driver.findElements(By.cssSelector("#W0wltc button, #W0wltc div, #W0wltc > div"));
	    if (!buttons.isEmpty()) {
	        WebElement button = buttons.get(0);
	        try {
	            button.click();
	            return;
	        } catch (Exception e) {
	            ((org.openqa.selenium.JavascriptExecutor) driver)
	                    .executeScript("arguments[0].click();", button);
	            return;
	        }
	    }
	    
	    
	    try {
	        wait.until(d ->
	                d.findElements(By.cssSelector("#W0wltc, div[aria-modal='true']")).isEmpty()
	        );
	    } catch (Exception ignored) {}
	}
	
	private void saveCookies() {
		if(!cookieExpired) {
			return;
		}
	    File cookieFile = new File("./ressources/cookies.data");

	    try {

	        //Existierende Datei überschreiben(!) (keine Anhänge!!!)
	        FileWriter fileWriter = new FileWriter(cookieFile, false);
	        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

	        DateFormat df = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.GERMAN);

	        //Alle aktuellen Cookies aus dem WebDriver holen
	        Set<org.openqa.selenium.Cookie> cookies = driver.manage().getCookies();
	        for (org.openqa.selenium.Cookie ck : cookies) {
	            // Ablaufdatum als String oder "null"
	            String expiryStr = (ck.getExpiry() != null)
	                    ? df.format(ck.getExpiry())
	                    : "null";

	            //Schreibe Cookie in Datei
	            bufferedWriter.write(
	                ck.getName() + ";" +
	                ck.getValue() + ";" +
	                ck.getDomain() + ";" +
	                ck.getPath() + ";" +
	                expiryStr + ";" +
	                ck.isSecure()
	            );
	            bufferedWriter.newLine();
	        }

	        bufferedWriter.flush();
	        bufferedWriter.close();

	        log("Cookies saved successfully (" + cookies.size() + " total)");
	        System.out.println("Cookies saved successfully (" + cookies.size() + " total)");

	    } catch (IOException e) {
	        log("Error saving cookies: " + e.getMessage());
	        e.printStackTrace();
	    }
	}
	
	//FaceDetection Methods
	public void setFaceDetectionEnabled(boolean enabled) {
	    this.faceDetectionEnabled = enabled;
	}
	public boolean isFaceDetectionEnabled() {
	    return faceDetectionEnabled;
	}
	
	
	//Progress and log Callbacks for GUI
	public void setLogger(Consumer<String> log) {
        this.log = (log != null) ? log : s -> {};
    }

	private void log(String s) {                
        try { log.accept(s); } catch (Exception ignored) {}
    }

    public void setProgressCallback(IntConsumer cb) { this.progressCb = cb != null ? cb : p -> {}; }

    private void progress(int p) { progressCb.accept(Math.max(0, Math.min(100, p))); }
    
    
    //Quit
    public void quit() {
		log("Closing Driver...");
		try {
				if (driver != null) driver.quit(); 
			} catch (Exception ignored) {}
        driver = null;
	}
    
	 public void cancel() {
		 stop = true; 
	}
}
