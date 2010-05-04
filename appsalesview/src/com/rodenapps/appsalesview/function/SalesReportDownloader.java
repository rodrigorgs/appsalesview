package com.rodenapps.appsalesview.function;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.swing.JOptionPane;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.UnexpectedPage;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlImageInput;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlOption;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSelect;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;

public class SalesReportDownloader {
//	private static SalesReportDownloader instance = null;
	private WebClient webClient;
	private HtmlPage daySelectPage;
	private DateFormat salesDateFormat = new SimpleDateFormat("MM/dd/yyyy");
	
	public DateFormat getDateFormat() {
		return salesDateFormat;
	}

	public SalesReportDownloader() {
		webClient = new WebClient();
	}
	
//	public static SalesReportDownloader getInstance() {
//		if (instance == null)
//			instance = new SalesReportDownloader();
//		return instance;
//	}
	
	public String[] getAvailableDays(final String user, final String password) throws FailingHttpStatusCodeException, MalformedURLException, IOException {			
		HtmlPage loginPage = webClient.getPage("https://itts.apple.com/cgi-bin/WebObjects/Piano.woa");
		HtmlInput input = loginPage.getElementByName("theAccountName");
		input.setValueAttribute(user);
		input = loginPage.getElementByName("theAccountPW");
		input.setValueAttribute(password);
		HtmlImageInput button = loginPage.getElementByName("1.Continue");
		HtmlPage reportPage = button.click(0, 0);
		
		HtmlSelect select = reportPage.getHtmlElementById("selDateType");
		daySelectPage = (HtmlPage)select.setSelectedAttribute("Daily", true);
		
		select = daySelectPage.getHtmlElementById("dayorweekdropdown");
		List<HtmlOption> options = select.getOptions();
		
		String[] days = new String[options.size()];
		for (int i = 0; i < days.length; i++) {
			days[i] = options.get(i).getValueAttribute();
		}

		return days;
	}
	
	public String[] getDailyReport(String day) throws IOException {
		HtmlSelect select = daySelectPage.getHtmlElementById("dayorweekdropdown");
		select.setSelectedAttribute(day, true);
		HtmlSubmitInput submit = daySelectPage.getElementByName("download");
		UnexpectedPage report = submit.click();

		String contents = report.getWebResponse().getContentAsString("UTF-8");
		String filename = report.getWebResponse().getResponseHeaderValue("content-disposition").split("=")[1];
		if (filename.endsWith(".gz"))
			filename = filename.substring(0, filename.length() - 3);
		
		return new String[] {filename, contents};
	}
}
