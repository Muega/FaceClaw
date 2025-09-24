import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Scanner;
import java.util.Set;
import java.io.File;
import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.io.FileReader;
import java.io.BufferedReader;
import java.util.StringTokenizer;


import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.Keys;

//test
public class Claw {

	ImageSaver is;
	WebDriver driver;
	WebDriverWait driverWait;
	
	Actions actions;
	
	//Debugger ScriptExecutor
	JavascriptExecutor js;
	int idx;
	
	String objectCheck;
	int currentLoop = 0;
	int maxLoops = 10;
	
	ArrayList<String> tabs;
	String searchTab;
	
	File cookieFile;
	
	
	WebElement googleSearchBar;
	String searchtext;
	
	WebElement imageGrid;
	ArrayList<WebElement> imageContainerList;
	
	WebElement imageContainerChild;
	
	
	WebElement currentImageIngres;
	String currentIngressURL;
	
	WebElement imageElement;
	String imageURL;
	
	Scanner sc = new Scanner(System.in);
	
	
	
	public void run() {
		
		startDriver();
		startDriverWait();
		startActions();
		startDebuggerJSExecutor();

		driver.get("https://www.google.de/imghp?hl=de&ogbl");
		
		driverWait = new WebDriverWait(driver, Duration.ofSeconds(30));

		// Schritt 1: DOM + Ressourcen geladen
		driverWait.until(d -> ((JavascriptExecutor) d).executeScript("return document.readyState").equals("complete"));

		// Schritt 2: Business-spezifische Elemente sichtbar
		driverWait.until(ExpectedConditions.presenceOfElementLocated(By.className("gLFyf")));

		
		
		// create file named Cookies to store Login Information		
	    cookieFile = new File("Cookies.data");							
	    try		
	    {	  
	        
	    	if (cookieFile.exists()) {
	    		
	    		File file = new File("Cookies.data");							
	            FileReader fileReader = new FileReader(file);							
	            BufferedReader Buffreader = new BufferedReader(fileReader);							
	            String strline;			
	            
	            while((strline=Buffreader.readLine())!=null){									
		            StringTokenizer token = new StringTokenizer(strline,";");									
		            
		            while(token.hasMoreTokens()){					
			            String name = token.nextToken();					
			            String value = token.nextToken();					
			            String domain = token.nextToken();					
			            String path = token.nextToken();					
			            Date expiry = null;					
			            		
			            String val;			
			            if(!(val=token.nextToken()).equals("null")){	//TODO cookie expiring handling
			            	DateFormat df = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.US);
			            	expiry = df.parse(val);					
			            }		
			            Boolean isSecure = new Boolean(token.nextToken()).								
			            booleanValue();		
			            Cookie ck = new Cookie(name,value,domain,path,expiry,isSecure);			
			            System.out.println(ck);
			            driver.manage().addCookie(ck); // This will add the stored cookie to your current session					
		            }
	            }
	    	}
	    }
	    		
	    	
//	    		cookieFile.delete();
//	    		System.out.println("CookieFile deleted");
//	    		cookieFile.createNewFile();			
//	            FileWriter fileWrite = new FileWriter(cookieFile);							
//	            BufferedWriter Bwrite = new BufferedWriter(fileWrite);							
//	            // loop for getting the cookie information 		
//	            	
//	            // loop for getting the cookie information 		
//	            for(Cookie ck : driver.manage().getCookies())							
//	            {			
//	                Bwrite.write((ck.getName()+";"+ck.getValue()+";"+ck.getDomain()+";"+ck.getPath()+";"+ck.getExpiry()+";"+ck.isSecure()));																									
//	                Bwrite.newLine();             
//	            }			
//	            Bwrite.close();			
//	            fileWrite.close();	
//	    	}
//	            
//	    }
	    catch(Exception ex)					
	    {		
	        ex.printStackTrace();			
	    }	
	    
		
		
		
		
		
		
		
		//Sammelt das Bilder Ergebnisgrid
		imageGrid = driver.findElement(By.xpath("//*[@id=\"rso\"]/div/div/div[1]/div/div"));
		
        //TODO scrollen (funktioniert noch nicht)
		//((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'})", imageGrid);
        
		//Liste aus Gridelementen
		imageContainerList = (ArrayList<WebElement>) imageGrid.findElements(By.xpath("./child::*")); //Casting has good performance. see https://stackoverflow.com/questions/16320014/java-optimization-nitpick-is-it-faster-to-cast-something-and-let-it-throw-excep/28858680#28858680
		System.out.println(imageContainerList.size()+" Einträge im Elemente Grid"); //Infoausgabe: Elemente im Suchergebnis Grid
		
		//Iteration um alle Bilder der Elemente auszuwählen
		for(WebElement imageContainer: imageContainerList ) {
			
			//Hovern zum generieren der lazy Daten
			actions.moveToElement(imageContainer).perform();

			
			//testet Element Tag zum debuggen
			objectCheck = imageContainer.getTagName();
			System.out.println(objectCheck + imageContainer.getAttribute("jscontroller"));
			if(!objectCheck.equals("div")) {
				System.out.println("Continuing because Element is not a div: "+ objectCheck);
				continue;
			}

			//TODO Max Loop length rausnehmen
			if(currentLoop <= maxLoops ) {
				currentLoop++;
			}else {
				System.out.println("Loops reached: "+ maxLoops);
				break;
			}
			driver.switchTo().window(tabs.get(0));
			
			//Debugger highlighted Element
			js.executeScript(
			        "arguments[0].style.outline='3px solid magenta';" +
			        "arguments[0].setAttribute('data-debug-idx', arguments[1]);", 
			        imageContainer, idx++
			    );
			/*(2)
			imageContainerChild = imageContainer.findElement(
				    By.cssSelector(":scope > div > div:nth-of-type(2)")
					);
			System.out.println(imageContainerChild.getTagName());

			currentImageIngres = driverWait.until(
									ExpectedConditions.presenceOfNestedElementLocatedBy(imageContainerChild, By.cssSelector(":scope a[href]"))
								);*/
			currentImageIngres = driverWait.until(
					ExpectedConditions.presenceOfNestedElementLocatedBy(imageContainer, By.xpath("./div[2]/h3/a"))
				);
			
			/*(3) currentImageIngres = imageContainer.findElement(
			            By.xpath("./div[2]/h3/a")
			        );*/
			
			//(1) imageContainer.findElement(By.cssSelector("a"));
			currentIngressURL =  currentImageIngres.getAttribute("href");
			
			System.out.println("Abgerufene Url des Elements:  "+currentIngressURL);
			
			//openNewTab(currentIngressURL);
			searchTab = openInNewTabAndSwitch(driver, currentIngressURL, Duration.ofSeconds(10));
			
			//driver.switchTo().window(tabs.get(1));
			imageElement = driverWait.until(
					ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id=\"imp\"]/div[1]/div[1]/div[2]/div/div[2]/c-wiz/div/div[2]/div/a/img[1]"))
				);
			
			//imageElement = driver.findElement(By.xpath("//*[@id=\"imp\"]/div[1]/div[1]/div[2]/div/div[2]/c-wiz/div/div[2]/div/a/img[1]"));
			imageURL = imageElement.getAttribute("src");
			
			is = new ImageSaver("./", searchtext);
			
			try {
				is.saveImageTemporally(new URI(imageURL));

				is.writeToFile();
				
				
			} catch (NoImageSavedException e) {
				System.out.println("You fucked up noimg");
				e.printStackTrace();
			} catch(Exception e) {
				System.out.println("You fucked up I/O");
				e.printStackTrace();
			}
			
			
			driver.close();
			driver.switchTo().window(searchTab);
		}
	
	
        //TODO alle Fehler catchen
	
	}
	
	
	
	
	private void startDriver() {
		 driver = new ChromeDriver();
		 tabs = new ArrayList<>(driver.getWindowHandles());
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
	
	
	private void closeDriver() {
		driver.close();
	}
	
	private void hoverOverElement(WebElement elm) {
		actions.moveToElement(elm).perform();
	}
	
	public void setSearchText(String searchText) {
		this.searchtext = searchText;
	}
	public void startSearch(String search) {
		//Scanner an Google Searchbar
	    driver.get("https://www.google.de/imghp?hl=de&ogbl");
		
		googleSearchBar = driver.findElement(By.className("gLFyf"));
		
		//searchtext = sc.nextLine();
		setSearchText(search);
		googleSearchBar.click();
		
		actions
        .sendKeys(this.searchtext)
        .perform();
		

		actions
		.sendKeys(Keys.RETURN)
		.perform();
	}
	
	/*
	private void openNewTab(String url) {
	((JavascriptExecutor) driver).executeScript("window.open(arguments[0], '_blank');", url);
    driver.switchTo().window(tabs.get(tabs.size() - 1));
	}*/
	
	public static String openInNewTabAndSwitch(WebDriver driver, String url, Duration timeout) {
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

}
