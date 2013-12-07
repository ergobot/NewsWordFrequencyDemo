package com.demo.newswordfrequency;

import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.InvalidFormatException;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;

import de.l3s.boilerpipe.BoilerpipeProcessingException;
import de.l3s.boilerpipe.extractors.ArticleExtractor;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class Driver {

	
	// Key: url (from urls object)
	// Value: Map
	static ConcurrentHashMap<URL,Map<String,Long>> urlWordCounts = new ConcurrentHashMap<URL,Map<String,Long>>();
	
	// Inner Map
	// Key: word (from the tokenized article)
	// Value: count (word count)
	
	// total word count
	static ConcurrentMap<String,Long> totalWordCountUnsorted = new ConcurrentHashMap<String,Long>();
	
	
	public static void main(String[] args) throws InvalidFormatException, IOException, BoilerpipeProcessingException {
		
		// For timing
		long startTime = System.currentTimeMillis();
		
		WebDriver browser = new FirefoxDriver();
		
		browser.get("http://news.google.com/");
		List<WebElement> elements = browser.findElements(By.className("article"));
		ArrayList<URL> urls = new ArrayList<URL>();
		System.out.println("\n***************");
		System.out.println("Getting all urls");
		for(WebElement element : elements)
		{
			urls.add(new URL(element.getAttribute("href")));
			//System.out.println(element.getAttribute("href"));
		}
		browser.quit();
		System.out.println("Size: " + urls.size());
		System.out.println("Completed getting all urls");
		System.out.println("***************\n");
		
		for(URL url : urls) //uncomment for all urls
		{
			System.out.println("\n*****************");
			System.out.println("Extracting article from: " + url);
			try{
			String articleText = ArticleExtractor.INSTANCE.getText(url);
			System.out.println(articleText);
			System.out.println("Completed extracting article");
			System.out.println("*****************\n");
			
			System.out.println("\n*****************");
			System.out.println("Start tokenizing");
			ConcurrentHashMap<String,Long> wordCounts = Tokenize(articleText);
			System.out.println("Tokenizing finished");
			System.out.println("*****************\n");
			
			
			System.out.println("\n*****************");
			System.out.println("Sorting the word counts");
			Map<String,Long> sortedWordCounts = sortByComparator(wordCounts);
			urlWordCounts.put(url, wordCounts);
			System.out.println("Finished sorting word counts");
			System.out.println("*****************\n");
			
			// Print out the sorted word counts
			printWordCounts(sortedWordCounts);
			}
			catch(Exception ex)
			{
				System.out.println("Something went wrong, going to the next link.");
			}
			
		}
		
		/*
		 * For fun, we're going to add up all of the word counts from
		 * each url into a single word count
		 */
		
		// Add all of the wordCounts together
	
		Map<String,Long> totalWordCountSorted = sortByComparator(totalWordCountUnsorted);
		System.out.println("\n**************************");
		System.out.println("Total word count");
		printWordCounts(totalWordCountSorted);
		System.out.println("Finished printing total word count");
		System.out.println("**************************\n");
		
		System.out.println("\n***************");
		System.out.println("Finished");
		// print out the stats
		long duration = System.currentTimeMillis() - startTime;
		System.out.println("Total time (sec): " + duration/1000);
		System.out.println("***************\n");
	}
	
	public static void printWordCounts(Map<String,Long> wordCounts){
		
		for(Map.Entry<String,Long> entry: wordCounts.entrySet())
		{
			String key = entry.getKey();
			Long value = entry.getValue();
			
			System.out.println(key + "\t\t\t\t:\t" + value.toString());
			
		}
	}
	
	public static ConcurrentHashMap<String,Long> Tokenize(String rawInput) throws InvalidFormatException, IOException {
		InputStream is = Driver.class.getResourceAsStream("en-token.bin");
 
		TokenizerModel model = new TokenizerModel(is);
 
		Tokenizer tokenizer = new TokenizerME(model);
 
		String tokens[] = tokenizer.tokenize(rawInput);
		
		ConcurrentHashMap<String,Long> wordCount = new ConcurrentHashMap<String,Long>();
		for (String word : tokens){
//			System.out.println(a);
			// performance/autoboxing
			long count = (Long) (wordCount.containsKey(word) ? wordCount.get(word) : 0);
			wordCount.put(word, count+1);
			
			long totalCount = (Long) (totalWordCountUnsorted.containsKey(word) ? totalWordCountUnsorted.get(word) : 0);
			totalWordCountUnsorted.put(word, totalCount+1);
		}
		is.close();
		return wordCount;
	}

	private static Map sortByComparator(Map unsortMap) {
		 
		List list = new LinkedList(unsortMap.entrySet());
 
		// sort list based on comparator
		Collections.sort(list, new Comparator() {
			public int compare(Object o1, Object o2) {
				return ((Comparable) ((Map.Entry) (o1)).getValue())
                                       .compareTo(((Map.Entry) (o2)).getValue());
			}
		});
 
		// put sorted list into map again
                //LinkedHashMap make sure order in which keys were inserted
		Map sortedMap = new LinkedHashMap();
		for (Iterator it = list.iterator(); it.hasNext();) {
			Map.Entry entry = (Map.Entry) it.next();
			sortedMap.put(entry.getKey(), entry.getValue());
		}
		return sortedMap;
	}
	
}
