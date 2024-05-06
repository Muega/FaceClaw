import java.time.Duration;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
//test
public class TestClaw {

	
	WebDriver driver;
	
	
	public void run() {
		startDriver();
		driver.get("https://www.google.de");
//		new WebDriverWait(driver, Duration.ofSeconds(20));
		
//		closeDriver();
	}
	
	private void startDriver() {
		 driver = new ChromeDriver();
	}
	
	private void closeDriver() {
		driver.close();
	}
}
