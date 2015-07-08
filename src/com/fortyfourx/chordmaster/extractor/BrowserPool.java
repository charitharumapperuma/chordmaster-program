package com.fortyfourx.chordmaster.extractor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;

public class BrowserPool {
	public static final long TIMEOUT = 60000; // 1 minutes
	public static final String FIREFOX_PROFILE_DIR = "C:/Users/Charith Arumapperuma/AppData/Roaming/Mozilla/Firefox/Profiles/285nppoq.default";

	private FirefoxProfile profile;

	private List<WebDriver> poolIdle;
	private List<WebDriver> poolBusy;
	
	private long lastActivityTime = 0;
	
	public BrowserPool(int size) {
		poolIdle = new ArrayList<WebDriver>();
		poolBusy = new ArrayList<WebDriver>();

		// Instead use browser's default profile
		profile = new FirefoxProfile(new File(BrowserPool.FIREFOX_PROFILE_DIR));
		
		System.out.println("creating " + size + " firefox browsers...");
		
		for (int i = 0; i < size; i++) {
			poolIdle.add(new FirefoxDriver(profile));
		}
		
		System.out.println(size + " browsers created succesfully...");

		this.lastActivityTime = System.currentTimeMillis();
	}

	public WebDriver pop() {
		WebDriver driver = null;
		if (!poolIdle.isEmpty()) {
			driver = poolIdle.remove(0);
			poolBusy.add(driver);
			
			// STATUS
			System.out.println("started using browser " + driver.hashCode() + "...");
		}

		this.lastActivityTime = System.currentTimeMillis();
		
		return driver;
	}

	public void push(WebDriver driver) {
		if (poolBusy.contains(driver)) {
			poolBusy.remove(driver);
			poolIdle.add(driver);
		}
		
		System.out.println("completed using browser " + driver.hashCode() + "...");

		this.lastActivityTime = System.currentTimeMillis();
	}

	public void close(WebDriver driver) {
		int index = poolIdle.indexOf(driver);
		if (index != -1) {
			poolIdle.get(index).close();
			poolIdle.remove(index);
		} 
		index = poolBusy.indexOf(driver);
		if (index != -1) {
			poolBusy.get(index).close();
			poolBusy.remove(index);
		}
	}
	
	public void closeAll() {
		System.out.println("closing all brosers...");
		
		for(WebDriver driver : poolIdle) {
			driver.close();
		}
		
		for(WebDriver driver : poolBusy) {
			driver.close();
		}
		
		System.out.println("all browsers are closed...");
	}
	
	public boolean isExpired() {
		
		if (this.poolBusy.isEmpty()){
			if ((System.currentTimeMillis() - this.lastActivityTime) > BrowserPool.TIMEOUT) {
				return true;
			}
		}
		return false;
	}
}

//Old way. In this way adblock is installed for every browser instance.
// causing browser to load slower and
// it loads adblock's startup page for every browser.
/*profile = new FirefoxProfile();
profile.setPreference("permissions.default.image", 2);
try {
	profile.addExtension(new File("./firefox/addons/adblock_plus-2.6.9-an+sm+tb+fx.xpi"));
} catch (IOException e) {
	// Do nothing. AdBlock is not critical to the expected task.
}*/