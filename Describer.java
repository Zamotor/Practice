package com.example.language;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public abstract class Describer {

	boolean isEnterMethod = false;
	boolean isOnStatement = false;
	boolean isRegularText = false;
	boolean isMatch = false;
	
	//append a specified element behind blank
	public static final String NEXT = "|";
	//uncertain element/elements
	public static final String BESIDE = "-";
	//represents the front element is nullable
	public static final String NULLABLE  = "+";
	public static final String UPPER = "upper";
	public static final String BELOW = "below";
	
	public HashMap<String, String> describes = new HashMap<>();
	public HashMap<String, String> relations = new HashMap<>(); 
	public HashMap<String, String> pairSymbols = new HashMap<>();
	public HashMap<String, String> matchFeatures = new HashMap<>();
	public List<String> targetLines = new ArrayList<>();
	
	ThreadPoolExecutor executor;
	ArrayBlockingQueue<Runnable> blockingQueue;
	
	public boolean invalid() {
		return false;
	}
	
	public void loadData(List<String> data) {
		targetLines = data;
	}
	
	public abstract boolean isCollection(String name);
	
	public abstract String[] getCollectionType(String name);
	
	public void addDescribe(String attribute, String describe) {
		describes.put(attribute, describe);
	}
	
	public String getDescribe(String attribute) {
		return describes.get(attribute);
	}
	
	public String getRelation(String attribute) {
		return relations.get(attribute);
	}
	
	public void setRelationMapping(String branch, String root) {
		relations.put(branch, root);
	}
	
	public void addPairSymbol(String first, String second) {
		pairSymbols.put(first, second);
	}
	
	public boolean containSymbol(String symbol) {
		for (String s : pairSymbols.keySet()) {
			if (s.equals(symbol)) {
				return true;
			}
		}
		return false;
	}
	
	public String extractFeature(String attribute) {
		String[] factors = extractFactor(attribute);
		String[] matchFactors = new String[factors.length];
		int[] matchPosition = new int[factors.length];
		int counter = 0;
		for (int i = 0; i < factors.length; i++) {
			if (factors[i].charAt(factors[i].length() - 1) != '+') {
				//matchFactors[counter++] = factors[i];
				matchPosition[counter++] = i;
			} else if (factors[i].charAt(factors[i].length() - 2) == '\\') {
				//matchFactors[counter++] = factors[i];
				matchPosition[counter++] = i;
			} 
		}
		if (matchFactors[0].isEmpty()) {
			if (matchPosition[0] == 0) {
				throw new IllegalArgumentException();
			}
		}

		int length = 0;
		for (int i = 0; i < matchFactors.length; i++) {
			if (matchPosition[i] != 0) {
				length++;
			} /*else {
				break;
			}*/
		}

		int[] hitCounter = new int[length];
		System.arraycopy(matchFactors, 0, matchFactors, 0, length);
		Iterator<String> iter = describes.keySet().iterator();
		
		// a|b
		// a-b
		// a-b-c
		//
		// method modifier+|type|name|(-)|{-}
		// class modifier+|type|name|{-}
		// statement modifier+|type+|name+name+|(-)-; ream; ream-; (-);
		String[] compareFactors;
		while (iter.hasNext()) {
			String describe = describes.get(iter.next());
			if (describe.equals(describes.get(attribute))) {
				continue;
			}
			compareFactors = extractFactor(describe);
			compareFactors = getPureFactor(compareFactors);
			counter = 0;
			for (; counter < hitCounter.length;) {
				for (int i = 0; i < compareFactors.length; i++) {
					if (compareFactors.equals(factors[matchPosition[counter]])) {
						hitCounter[counter] = hitCounter[counter] + 1;
					}
				}
			}
		}
		String feature = factors[matchPosition[getMiniNumIndex(hitCounter)]];
		matchFeatures.put(attribute, feature);
		return feature;
	}
	
	private int getMiniNumIndex(int[] nums) {
		int num = nums[0];
		int index = 0;
		for (int i = 0; i < nums.length; i++) {
			if (num > nums[i]) {
				num = nums[i];
				index = i;
			}
		}
		return index;
	}
	
	public String[] extractFactor(String describe) {
		String[] factors;
		int counter = 0;
		char[] array = describe.toCharArray();
		int[] index = new int[20];
		for (int i = 0; i < array.length; i++) {
			if ((array[i] == '|') && (array[i - 1] != '\\')) {
				index[counter++] = i;
			} else if ((array[i] == '-') && (array[i - 1] != '\\')) {
				if (!containSymbol(describe.substring(counter, i))) {
					index[counter++] = i;
				}
			}
		}
		factors = new String[counter];
		if (counter > 0) {
			int frontIndex = 0;
			for (int i = 0; i < counter; i++) {
				factors[i] = describe.substring(frontIndex, index[i]);
				frontIndex = index[i] + 1;
			}
		} else {
			factors[counter] = describe;
		}
		return factors;
	}
	
	private String[] getPureFactor(String[] factors) {
		for (int i = 0; i < factors.length; i++) {
			if (isNullable(factors[i])) {
				factors[i] = factors[i].substring(0, factors[i].length() - 1);
				break;
			} 
		}
		return factors;
	}
	
	public void analyze() {
		blockingQueue = new ArrayBlockingQueue<>(20);
		executor = new ThreadPoolExecutor(4, 8, 2, TimeUnit.SECONDS, blockingQueue);
	}
	
	public void findAttribute(String attribute) {
		executor.execute(new AttributeSearchRunnable(attribute));
	}
	
	public void validate(String attribute, int lineNum) {
		String describe = describes.get(attribute);
		// int index = describe.indexOf(matchFeatures.get(attribute));
		String line = targetLines.get(lineNum);
		String[] factors = extractFactor(describe);
		String[] pureFactors = getPureFactor(factors);
		char[] contentArray = line.toCharArray();
		
		int fromIndex = 0;
		int endIndex = 0;
		
		int position = 0;//line.indexOf(factors[index]) - 1;
		String matchFactor;
		int counter = 0;
		char[] factorArray = new char[20];
		for (int i = 0; i < factors.length; i++) {
			matchFactor = getFrontPair(pureFactors[i]);
			if (matchFactor.equals(pureFactors[i])) {
				endIndex = line.indexOf(pureFactors[i], endIndex);
				if ((endIndex != -1) && isNullable(factors[i])) {
					String sub = line.substring(endIndex + pureFactors[i].length());
					fromIndex = endIndex;
					matchFactor = getFrontPair(pureFactors[i + 1]);
					if ((sub.indexOf(matchFactor, endIndex) == -1) && !sub.contains(pureFactors[i])) {
						sub = targetLines.get(++lineNum);
						while (sub.indexOf(matchFactor, 0) != -1) {
							sub = targetLines.get(++lineNum);
							if ( sub.contains(pureFactors[i])) {
								isMatch = false;
								return;
							}
						}
					} else {
						if (sub.contains(pureFactors[i])) {
							isMatch = false;
							return;
						}
						
					}
				} else {
					isMatch = false;
				}

			} else {

			}
		}
		/*if (position != 0) {
			for (int i = index - 1; i > 0; i--) {
				if (true) {
					for (int j = position; j > 0; j--) {
						if (contentArray[j] != ' ') {
							counter++;
						} else {
							if (counter > 0) {
								System.arraycopy(contentArray, (j + counter), factorArray, 0, counter);
								matchFactor = String.valueOf(factorArray, 0, counter);
								if (matchType(factors[index - 1], matchFactor)) {
									counter = 0;
									position = position - counter;
									index++;
									break;
								} else {
									isMatch = false;
									return;
								}
							}
						}
					}
				}

			}

		}*/

	}
	
	private void getNextMatch() {
		
	}
	
	private void pairMatch() {
		
	}
	
	private boolean isNullable(String factor) {
		if ((factor.charAt(factor.length() - 1) == '+') &&
				(factor.charAt(factor.length() - 2) != '\\')) {
			return true;
		} 
		return false;
	}
	
	public boolean isMatch(String factor, String content) {
		return isMatch;
	}
	
	public abstract boolean matchType(String factor, String content);
	
	private String getFrontPair(String factor) {
		if (factor.length() > 2) {
			if ((factor.length() % 2) == 1) {
				int position = (factor.length() / 2);
				if ((factor.charAt(position) == '-') && (factor.charAt(position - 1) != '\\')) {
					factor = factor.substring(0, position);
				}
			}
		}
		return factor;
	}
	
	class AttributeSearchRunnable implements Runnable {

		private String attr;

		public AttributeSearchRunnable(String attr) {
			this.attr = attr;
		}

		@Override
		public void run() {
			String matchFactor = extractFeature(attr);
			matchFactor = getFrontPair(matchFactor);
			List<String> lineNum = new ArrayList<>();
			for (int i = 0; i < targetLines.size(); i++) {
				if (targetLines.get(i).contains(matchFactor)) {
					lineNum.add(String.valueOf(i));
				}
			}
		}

	}
	
	class ValidateRunnable implements Runnable {
		
		@Override
		public void run() {
			
		}
	}

	
	
}
