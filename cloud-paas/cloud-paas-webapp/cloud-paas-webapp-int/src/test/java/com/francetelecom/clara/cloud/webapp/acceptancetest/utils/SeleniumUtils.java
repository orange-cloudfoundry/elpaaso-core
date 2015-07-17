/**
 * Copyright (C) 2015 Orange
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.francetelecom.clara.cloud.webapp.acceptancetest.utils;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * http://www.brimllc.com/2011/01/extending-selenium-2-0-webdriver-to-support-ajax/
 */
public class SeleniumUtils {

	/***
	 * Gets an element by name or id
	 * 
	 * @param driver
	 * @param nameOrID
	 * @return
	 */
	public static WebElement getByNameOrID(WebDriver driver, String nameOrID) {
		WebElement element = null;
		// try getting the element by name
		try {
			element = driver.findElement(By.name(nameOrID));
		} catch (Exception ex) {

		}
		if (null == element) {
			try {
				element = driver.findElement(By.id(nameOrID));
			} catch (Exception ex) {

			}
		}
		return element;
	}

	/***
	 * Gets select option by xpath
	 * 
	 * @param driver
	 * @param nameOrID
	 * @param optionXPath
	 * @return
	 */
	public static WebElement getSelectOptionByXPath(WebDriver driver, String nameOrID, String optionXPath) {
		WebElement selectElement = getByNameOrID(driver, nameOrID);
		WebElement options = selectElement.findElement(By.tagName("option"));
		return selectElement.findElement(By.xpath(optionXPath));
	}

	/**
	 * Gets select option by option value
	 * 
	 * @param driver
	 * @param nameOrID
	 * @param optionValue
	 * @return
	 */
	public static WebElement getSelectOptionByValue(WebDriver driver, String nameOrID, String optionValue) {
		String optionXPath = "//option[@value='" + optionValue + "']";
		return getSelectOptionByXPath(driver, nameOrID, optionXPath);
	}

	public static WebElement getSelectOptionByValue(WebDriver driver, WebElement selectElement, String optionValue) {
		return selectElement.findElement(By.xpath("//option[@value='" + optionValue + "']"));
	}

	public static boolean assertNull(WebElement element) {
		try {
			// call any method on the element
			element.isEnabled();
		} catch (Exception ex) {
			return true;
		}
		return false;
	}

	public static boolean assertNotNull(WebElement element) {
		try {
			// call any method on the element
			element.isEnabled();
		} catch (Exception ex) {
			return false;
		}
		return true;
	}

	/***
	 * Gets input type by type
	 * 
	 * @param driver
	 * @param type
	 * @return
	 */
	public static WebElement getInputItemByType(WebDriver driver, String type) {
		String xpath = "//input[@type='" + type + "']";
		return driver.findElement(By.xpath(xpath));
	}

	public static WebElement getRadioWithIndex(WebDriver driver, String nameOrId, String index) {
		// String xpath =
		// "//input[@id='"+nameOrId+"' and @type='radio']["+index.trim()+"]";
		String xpath = "//input[@id='" + nameOrId + "' and @type='radio'][1]";
		WebElement radio = null;
		try {
			radio = driver.findElement(By.xpath(xpath));
		} catch (Exception ex) {

		}
		// try with name now
		// xpath =
		// "//input[@name='"+nameOrId+"' and @type='radio']["+index.trim()+"]";
		xpath = "//input[@name='" + nameOrId + "' and @type='radio']/[2]";
		try {
			radio = driver.findElement(By.xpath(xpath));
		} catch (Exception ex) {

		}
		return radio;
	}

	/***
	 * Gets input type by value
	 * 
	 * @param driver
	 * @param type
	 * @return
	 */
	public static WebElement getInputItemByValue(WebDriver driver, String value) {
		String xpath = "//input[@value='" + value + "']";
		return driver.findElement(By.xpath(xpath));
	}

	/***
	 * Gets input type by type and value
	 * 
	 * @param driver
	 * @param type
	 * @param value
	 * @return
	 */
	public static WebElement getInputItemByTypeAndValue(WebDriver driver, String type, String value) {
		String xpath = "//input[@type='" + type + "' and @value='" + value + "']";
		return driver.findElement(By.xpath(xpath));
	}

	public static WebElement getInputItemByTypeAndName(WebDriver driver, String type, String name) {
		String xpath = "//input[@type='" + type + "' and @name='" + name + "']";
		return driver.findElement(By.xpath(xpath));
	}

	/***
	 * Gets image by alt text
	 * 
	 * @param driver
	 * @param type
	 * @return
	 */
	public static WebElement getImageByAltText(WebDriver driver, String altText) {
		String xpath = "//img[@alt='" + altText + "']";
		return driver.findElement(By.xpath(xpath));
	}

	public static WebElement getLinkByNameOrID(WebDriver driver, String nameOrID) {
		String xpath = "//a[@id='" + nameOrID + "']";
		WebElement link = null;
		try {
			link = getElementByXPath(driver, xpath);
		} catch (Exception ex) {

		}
		if (null != link) {
			return link;
		}
		xpath = "//a[@name='" + nameOrID + "']";
		try {
			link = getElementByXPath(driver, xpath);
		} catch (Exception ex) {

		}
		return link;
	}

	/***
	 * Gets element by xpath
	 * 
	 * @param driver
	 * @param xPath
	 * @return
	 */
	public static WebElement getElementByXPath(WebDriver driver, String xPath) {
		return driver.findElement(By.xpath(xPath));
	}

	private static void waitForASecond() {
		try {
			Thread.sleep(1000);
		} catch (Exception ex) {
			// do nothing
		}
	}

	/***
	 * Waits the till element is displayed
	 * 
	 * @param driver
	 * @param elementNameOrID
	 */
	public static WebElement waitForElement(WebDriver driver, String elementNameOrID, long timeout) {
		long start = System.currentTimeMillis();
		WebElement webElement = null;
		while (null == webElement) {
			webElement = getByNameOrID(driver, elementNameOrID);
			if (null != webElement)
				break;
			Assert.assertTrue("Timeout (>"+timeout+"ms) while waiting for web element "+elementNameOrID+" in page '"+driver.getTitle()+"'", System.currentTimeMillis() - start < timeout);
			waitForASecond();
		}
		return webElement;
	}

	public static WebElement waitForSelectElement(WebDriver driver, String elementNameOrID, String optionValue, long timeout) {
		long start = System.currentTimeMillis();
		WebElement webElement = null;
		while (null == webElement) {
			webElement = getSelectOptionByValue(driver, elementNameOrID, optionValue);
			if (null != webElement)
				break;
			Assert.assertTrue("Timeout (>"+timeout+"ms) while waiting for select web element "+elementNameOrID+" in page '"+driver.getTitle()+"'", System.currentTimeMillis() - start < timeout);
			waitForASecond();
		}
		return webElement;
	}

	public static WebElement waitForEnabled(WebDriver driver, String elementNameOrID, long timeout) {
		long start = System.currentTimeMillis();
		WebElement webElement = null;
		while (null == webElement || !webElement.isEnabled()) {
			webElement = getByNameOrID(driver, elementNameOrID);
			if (null != webElement && webElement.isEnabled())
				break;
			Assert.assertTrue("Timeout (>"+timeout+"ms) while waiting for web element "+elementNameOrID+" to be enabled in page '"+driver.getTitle()+"'", System.currentTimeMillis() - start < timeout);
			waitForASecond();
		}
		return webElement;
	}

	public static WebElement waitForEnabled(WebElement element, long timeout) {
		long start = System.currentTimeMillis();
		while (!element.isEnabled()) {
			Assert.assertTrue("Timeout (>"+timeout+"ms) while waiting for web element "+element.toString()+" to be enabled", System.currentTimeMillis() - start < timeout);
			waitForASecond();
		}
		return element;
	}

	public static WebElement waitForInputElementByTypeAndValue(WebDriver driver, String type, String value, long timeout) {
		long start = System.currentTimeMillis();
		WebElement webElement = null;

		while (null == webElement) {
			try {
				webElement = getInputItemByTypeAndValue(driver, type, value);
			} catch (Exception ex) {

			}
			if (null != webElement)
				break;
			Assert.assertTrue("Timeout (>"+timeout+"ms) while waiting for input element type "+type+" to have value "+value+" in page '"+driver.getTitle()+"'", System.currentTimeMillis() - start < timeout);
			waitForASecond();
		}
		return webElement;
	}

	/***
	 * Waits till the title is displayed
	 * 
	 * @param driver
	 * @param title
	 */
	public static void waitForTitle(WebDriver driver, String title, long timeout) {
		long start = System.currentTimeMillis();
		String pageTitle = null;
		while (StringUtils.isBlank(pageTitle) || !StringUtils.equals(pageTitle, title)) {
			try {
				pageTitle = driver.getTitle();
			} catch (Exception ex) {

			}
			if (StringUtils.isNotBlank(pageTitle) && StringUtils.equals(pageTitle, title))
				break;
			Assert.assertTrue("Timeout (>"+timeout+"ms) while waiting for title to be set to '"+title+"' in page '"+driver.getTitle()+"'", System.currentTimeMillis() - start < timeout);
			waitForASecond();
		}
	}

	public static WebElement waitForElementSelected(WebElement webElement, long timeout) {
		long start = System.currentTimeMillis();
		if (null == webElement) {
			return webElement;
		}
		while (!webElement.isSelected()) {
			Assert.assertTrue("Timeout (>"+timeout+"ms) while waiting for web element "+webElement.toString()+" to be selected", System.currentTimeMillis() - start < timeout);
			waitForASecond();
		}
		return webElement;
	}
}
