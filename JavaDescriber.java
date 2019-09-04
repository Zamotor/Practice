package com.example.language;

import static com.example.language.Attribute.METHOD;
import static com.example.language.Attribute.STATEMENT;
import static com.example.language.JavaAttribute.MODIFIERS;
import static com.example.language.JavaAttribute.TYPE;
import static com.example.language.JavaAttribute.CLASS;
import static com.example.language.JavaAttribute.TYPECOLLECTION;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.example.asynchronized.AsynchronizedTask;
import com.sun.prism.impl.Disposer.Target;
import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils.Collections;

import static com.example.language.JavaAttribute.NAME;
import static com.example.language.Attribute.SLOT;

public class JavaDescriber extends Describer {

	public static void main(String[] args) {
		File file = new File("./Component.java");
		JavaDescriber describer = new JavaDescriber(file);
		describer.init();
		long start = System.currentTimeMillis();
		if (describer.loadData()) {

			describer.analyze();
			// describer.getStringContent();
			// int index = describer.getJClass();
			// index = index + 1;
			// System.out.println("error");
			// describer.getJMethod(index);
			/*
			 * describer.getJClass(); //describer.getJMethod();
			 * describer.getJComment(); //describer.getStringContent();
			 */
			long end = System.currentTimeMillis();

			System.out.println((end - start) + "ms");
		}
		/*
		 * try { Thread.sleep(1000); long start = System.currentTimeMillis();
		 * describer.getJClass(); //describer.getJMethod();
		 * describer.getJComment(); //describer.getStringContent(); long end =
		 * System.currentTimeMillis();
		 * 
		 * System.out.println((end - start)); } catch (InterruptedException e) {
		 * e.printStackTrace(); }
		 */

		System.out.println("done!");
	}

	private static final String METHOD_DESCRIBE = "modifier|keyword|keyword|type|name|keyword";
	private static final String CLASS_DESCRIBE = "modifier|keyword|type|name|keyword|type|keyword";

	private File srcFile;
	private List<CharPosition> blockStartPos = new ArrayList<>();
	private List<CharPosition> blockCommentBoundsPos = new ArrayList<>();
	private List<CharPosition> stringContentBoundsPos = new ArrayList<>();
	private List<CharPosition> lineCommentPos = new ArrayList<>();
	private ArrayList<String> srcFileLines = new ArrayList<>();
	private ArrayList<String> pureSrcFileLines = new ArrayList<>();
	private HashMap<String, String> matchSymbols = new HashMap<>();
	private List<String> symbols = new ArrayList<>();
	private int pairEnterCounter = 0;
	private List<CharPosition> methodList = new ArrayList<>();
	private List<CharPosition> classList = new ArrayList<>();
	private CharPosition classPointer = new CharPosition(0, 0);

	private static final String UP_MODE = "UP";
	private static final String DOWN_MODE = "down";
	private int quoteCount = 0;
	ArrayBlockingQueue<Runnable> blockingQueue = new ArrayBlockingQueue<>(20);
	ThreadPoolExecutor executor = new ThreadPoolExecutor(5, 10, 2, TimeUnit.SECONDS, blockingQueue,
			new ThreadPoolExecutor.DiscardPolicy());

	public JavaDescriber(File file) {
		srcFile = file;

	}

	public void init() {
		matchSymbols.put(METHOD, "{");
		matchSymbols.put(CLASS, "{");
		matchSymbols.put(STATEMENT, ";");
		pairSymbols.put("(", ")");
		pairSymbols.put("<", ">");
		pairSymbols.put("{", "}");
		pairSymbols.put("\"", "\"");
		pairSymbols.put("/*", "*/");
		symbols.add("()");
		symbols.add("{}");
		symbols.add("\"\"");
		symbols.add("/**/");
		symbols.add("<>");
		executor.allowsCoreThreadTimeOut();
	}

	public void analyze() {
		// if (loadData()) {
		// parseMethod();
		String result = "OK";
		FutureTask<String> findAttribute = new FutureTask<>(new TaskFind(), result);
		FutureTask<String> taskParseMethod = null;
		FutureTask<String> taskParseComment = null;
		FutureTask<String> taskParseString = null;
		executor.execute(findAttribute);
		try {
			if (result.equals(findAttribute.get())) {
				taskParseComment = new FutureTask<>(new TaskParseComment(), result);
				taskParseString = new FutureTask<>(new TaskParseString(), result);
				System.out.println("parsing...");
				executor.execute(taskParseComment);
				if (result.equals(taskParseComment.get())) {
					System.out.println("comment parsing completed");
					executor.execute(taskParseString);
					if (result.equals(taskParseString.get())) {
						blockCommentBoundsPos = removeBoundsElem(blockCommentBoundsPos, stringContentBoundsPos);
						removeLineElem(blockCommentBoundsPos, lineCommentPos);
						blockStartPos = removeBoundsElem(blockStartPos, stringContentBoundsPos);
						blockStartPos = removeBoundsElem(blockStartPos, blockCommentBoundsPos);
						removeLineElem(blockStartPos, lineCommentPos);
						System.out.println("string parsing completed");
						CharPosition pos = getJClass();
						taskParseMethod = new FutureTask<>(new TaskParseMethod(pos, methodList), result);
						executor.execute(taskParseMethod);

						if (result.equals(taskParseMethod.get())) {
							System.out.println("method parsing completed");
							System.out.println("done");
							//getStatement(247, 869);
							getClassStatement(methodList, classPointer.getRow(), pos);
							if (result.equals(taskParseMethod.get())) {
								executor.shutdown();
							}
						}
					}
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		//executor.shutdown();
		if (executor.isShutdown()) {
			System.out.println("shutdown...");
		}
	}

	public boolean loadData() {
		// Thread task = new LoadFileThread();
		// task.start();
		String result = "OK";
		FutureTask<String> loadTask = new FutureTask<>(new TaskLoadFile(), result);
		executor.execute(loadTask);
		System.out.println("loading...");
		try {
			if (loadTask.get().equals(result)) {
				if (!srcFileLines.isEmpty()) {
					System.out.println("completed");
					return true;
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return false;
	}

	public CharPosition getJClass() {
		CharPosition startPos = null;
		CharPosition endPos;
		CharPosition pos = null;
		int index = 0;
		for (int i = 0; i < blockStartPos.size(); i++) {
			startPos = blockStartPos.get(i);
			startPos = parseClass(startPos);
			if (startPos == null) {
				// System.out.println("format error " + i);
				if (i == 1) {
					// System.out.println();
				}
			} else {
				classList.add(startPos);
				pos = new CharPosition(startPos.getRow(), startPos.getColumn());
				String content = pureSrcFileLines.get(blockStartPos.get(i).getRow());
				content = content.substring(0, blockStartPos.get(i).getColumn());
				System.out.println("found class " + content);
				index = i + 1;
				pos.setRow(index);
				endPos = rangePairSymbolPosition(pureSrcFileLines, "{", startPos);
				index = endPos.getRow();
				for (int j = i; j < blockStartPos.size(); j++) {
					if (index < blockStartPos.get(j).getRow()) {
						index = j - 1;
						break;
					}
					if (index == blockStartPos.get(j).getRow()) {
						if (endPos.getMarkPosition().getColumn() > blockStartPos.get(j).getColumn()) {
							index = j;
							break;
						}
					}
					if (j == (blockStartPos.size() - 1)) {
						index = j;
						break;
					}
				}
				endPos.setRow(index);
				pos.markPosition(endPos);
				break;
			}
		}
		return pos;
	}

	List<String[]> classInfo = new ArrayList<>();

	public CharPosition parseClass(CharPosition srcPos) {
		CharPosition pos = new CharPosition(srcPos.getRow(), srcPos.getColumn());
		String[] factors = split(CLASS_DESCRIBE, '|');
		int counter = factors.length;
		String[] words = new String[counter];
		
		String impl = getImplOrThrows(pos);
		if (impl != null) {
			String key = impl.split(" ")[0];
			if (!key.equals("implements")) {
				return null;
			}
			words[--counter] = impl;
		} else {
			words[--counter] = "";
		}
		
		while (counter != 0) {
			words[--counter] = getPreviousWord(pos);
			if (!keyWordMatch(factors[counter], words[counter])) {
				String a = new String(words[counter]);
				while (!keyWordMatch(factors[counter], a)) {
					words[counter] = "";
					counter--;
				}
				words[counter] = a;
			}
		}
		formatAttribute(words, factors);	
		if (!words[2].isEmpty() && words[2].equals("class"))
		return pos;
		
		return null;
	}
	
	private String getImplOrThrows(CharPosition srcPos) {
		CharPosition pos = new CharPosition(srcPos.getRow(), srcPos.getColumn());
		String word = getPreviousWord(pos);
		int row = pos.getRow();
		int column = pos.getColumn();
		pos.setColumn(pos.getColumn() + 1);
		pos = getFirstSymPos(pos, ',', UP_MODE);
		StringBuffer buf = new StringBuffer();
		buf.append(word);

		if (pos != null) {
			while (pos != null) {
				buf.insert(0, ",");
				word = getPreviousWord(pos);
				buf.insert(0, word);
				row = pos.getRow();
				column = pos.getColumn();
				pos.setColumn(pos.getColumn() - 1);
				pos = getFirstSymPos(pos, ',', UP_MODE);
			}
		}
		pos = new CharPosition(row, column);
		word = getPreviousWord(pos);
		if (word != null) {
			if (word.equals("implements") || word.equals("throws")) {
				buf.insert(0, word + " ");
				srcPos.setRow(pos.getRow());
				srcPos.setColumn(pos.getColumn());
				return buf.toString();
			}
		}
		return null;
	}
	
	public void getClassStatement(List<CharPosition> methodPos, int i, CharPosition innerBlockBounds) {
		CharPosition classPos = blockStartPos.get(innerBlockBounds.getRow() - 1);
		int calCounter = methodPos.size() + classList.size() - i + 2;
		int c = calCounter;
		int mCounter = 0;
		CharPosition startPos = classPos;
		CharPosition endPos = null;
		while (calCounter != 0) {
			if (mCounter < methodPos.size()) {
				endPos = methodPos.get(mCounter);
				//System.out.println("calCounter: " + c + "mCount: " + methodPos.size());
				//System.out.println("calCounter: " + calCounter);
			} else {
				if (i != classList.size()) {
					endPos = classList.get(i);
					i++;
				}
			}
			if (i != classList.size()) {
				if (endPos.getRow() > classList.get(i).getRow()) {
					endPos = classList.get(i);
					i++;
					if (i == (classList.size() - 1)) {
						mCounter--;
					}
				} else if (endPos.getRow() == classList.get(i).getRow()) {
					if (endPos.getColumn() > classList.get(i).getColumn()) {
						endPos = classList.get(i);
						i++;
						if (i == (classList.size() - 1)) {
							mCounter--;
						}
					}
				} else {
					mCounter++;
				}
			} else {
				mCounter++;
			}

			startPos.markPosition(endPos);
			getStatement(startPos);
			calCounter--;
			startPos = endPos.getMarkPosition();
			if (startPos.getRow() == 7860) {
				System.out.println();
			}
		}
	}

	public void getJComment() {

		parseComment();

		System.out.println("block comment count " + blockCommentBoundsPos.size());
		System.out.println("line comment count " + lineCommentPos.size());
		System.out.println("filter...");
		lineCommentPos = removeBoundsElem(lineCommentPos, blockCommentBoundsPos);
		removeLineElem(blockCommentBoundsPos, lineCommentPos);
		System.out.println("block comment count " + blockCommentBoundsPos.size());
		System.out.println("line comment count " + lineCommentPos.size());
	}

	/* */
	public void getStringContent() {
		// for (int i = 0; i < stringContentBoundsPos.size(); i++) {
		// startPos = stringContentBoundsPos.get(i);
		// commentEnd =
		parseString();
		System.out.println("String count " + stringContentBoundsPos.size());
		// if (commentEnd == null) {
		// System.out.println("format error, missing '\"' ");
		// }
		// }
	}
	
	
	public void getJMethod(CharPosition srcPos, List<CharPosition> methodPos) {
		int start = srcPos.getRow();
		int end = srcPos.getMarkPosition().getRow();
		getJMethod(start, end, methodPos);
	}

	public void getJMethod(int start, int end, List<CharPosition> methodPos) {

		classPointer.setRow(classList.size());
		int counter = 0;
		CharPosition blockEnd = new CharPosition(0, 0);
		CharPosition pos;
		List<CharPosition> newList = new ArrayList<>();
		int position;
		for (int i = start; i <= end; i++) {
			pos = blockStartPos.get(i);
			blockEnd = parseMethod(pos);
			if (i == 907) {
				//System.out.println(i);
				//i = 99;
			}
			//System.out.println(i);
			position = blockEnd.getRow();
			if (blockEnd.getColumn() != -1) {
				methodPos.add(pos);
				//System.out.println("totalSize " + blockStartPos.size() + " "
				// + (i + 1) + " parse:");
				pos.markPosition(blockEnd);
				counter++;
				//System.out.println(srcFileLines.get(pos.getRow()));
			} else {
				classList.add(pos);
				// blockEnd = null;
				 //System.out.println("not a method" + " row " + pos.getRow() +
				 //" " + i + ":");
				 //System.out.println(srcFileLines.get(pos.getRow()));
				 //pos = null;
			}
			for (i = i + 1; i <= end; i++) {
				pos = blockStartPos.get(i);
				if (pos.getRow() > position) {
					i--;
					if (i == 907) {
						//System.out.println();
					}
					if (i == blockStartPos.size()) {
						break;
					}
					break;
				}
			}

		}
		
		//methodPos = newList;
		System.out.println("method count " + counter);
		System.out.println("filter...");
		methodPos = removeBoundsElem(methodPos, stringContentBoundsPos);
		System.out.println("method count " + methodPos.size());
		/*for (int i = start; i < end; i++) {
			pos = blockStartPos.get(i).getMarkPosition();
			if (pos != null) {
				newBlockPos.add(pos);
			}
		}*/
		//blockStartPos = newBlockPos;
		//methodPos = removeBoundsElem(blockStartPos, stringContentBoundsPos);

	}

	public CharPosition parseMethod(CharPosition srcPos) {
		CharPosition rightBlockEnd = null;
		CharPosition pos = new CharPosition(srcPos.getRow(), srcPos.getColumn());
		String thr = getImplOrThrows(pos);
		
		String[] factors = split(METHOD_DESCRIBE, '|');
		int count = factors.length;
		String[] words = new String[count];
		if (thr != null) {
			String key = thr.split(" ")[0];
			if (!key.equals("throws")) {
				
			}
			words[--count] = thr;
		} else {
			words[--count] = "";
		}

		CharPosition rightBracketPos = getFirstSymPos(pos, ')', UP_MODE);
		rightBlockEnd = rangePairSymbolPosition(pureSrcFileLines, "{", srcPos);
		srcPos.markPosition(rightBlockEnd);
		
		if (rightBracketPos != null) {
			
			CharPosition leftBracketPos;
			leftBracketPos = rangePairSymbolPosition(pureSrcFileLines, ")", rightBracketPos);
			pos = leftBracketPos;
			if (leftBracketPos != null) {
				while (count != 0) {
					words[--count] = getPreviousWord(pos);
					if (words[count] == null) {
						while (count != -1) {
							words[count] = "";
							count--;
						}
						break;
					}
					if (!keyWordMatch(factors[count], words[count])) {
						String a = new String(words[count]);
						while (!keyWordMatch(factors[count], a)) {
							words[count] = "";
							count--;
							if (count == 0) {
								break;
							}
						}
						words[count] = a;
					}
				}
				//String[] words = getWord(count, leftBracketPos, line);// content.split("
																		// ");
				//String[] elems = new String[count];
				//filterWords(words, factors, count);
				formatAttribute(words, factors);
				String name = words[words.length - 2];
				if (!name.isEmpty()) {
					if (name.charAt(0) >= 'a') {
						// methodList.add(words);
					} else {
						// rightBlockEnd.setColumn(-1);
					}

				} else {
					rightBlockEnd.setColumn(-1);
					// srcPos.setColumn(-1);
				}
			}
		} else {
			rightBlockEnd.setColumn(-1);
			// srcPos.setColumn(-1);
		}
		srcPos.markPosition(rightBlockEnd);
		return rightBlockEnd;
	}

	public void formatAttribute(String[] content, String[] factors) {
		int counter = 0;
		for (int i = 0; i < factors.length; i++) {
			String[] types = getTypeCollection(factors[i]);
			for (int j = i; j < content.length; j++) {
				for (String type : types) {
					counter++;
					if (factors[i].equals(JavaAttribute.NAME)) {
						if (!content[j].isEmpty()) {
							content[i] = content[j];
							if (i != j) {
								content[j] = "";
							}
							j = content.length;
							break;
						}
					}
					if (content[j].equals(type)) {
						content[i] = content[j];
						if (i != j) {
							content[j] = "";
						}
						j = content.length;
						break;
					} else {
						if (counter == types.length) {
							content[i] = "";
						}
					}
				}
			}
		}
	}

	private String[] split(String content, char sep) {
		String[] a = null;
		char[] array = content.toCharArray();
		int[] index = new int[array.length];
		int counter = 0;
		for (int i = 0; i < array.length; i++) {
			if (array[i] == sep) {
				index[counter++] = i;
			}
		}
		a = new String[counter + 1];
		a[0] = content.substring(0, index[0]);
		for (int i = 1; i <= counter; i++) {
			if (i != counter) {
				a[i] = content.substring(index[i - 1] + 1, index[i]);
			} else {
				a[i] = content.substring(index[i - 1] + 1, content.length());
			}
		}
		return a;
	}

	private CharPosition getNearSymPos(CharPosition srcPos, String mode) {

		CharPosition pos = new CharPosition(srcPos.getRow(), srcPos.getColumn());
		if (mode.equals(UP_MODE)) {
			pos.setColumn(srcPos.getColumn() - 1);
		} else if (mode.equals(DOWN_MODE)) {
			pos.setColumn(srcPos.getColumn() + 1);
		}
		pos = getLineSymPos(pos, mode);

		while ((pos != null) && (pos.getColumn() == -1)) {
			if (mode.equals(UP_MODE)) {
				pos.rowReduce();
				pos.setColumn(pureSrcFileLines.get(pos.getRow()).length() - 1);
				pos = getLineSymPos(pos, UP_MODE);
			} else if (mode.equals(DOWN_MODE)) {
				pos.rowGrow();
				pos.setColumn(0);
				pos = getLineSymPos(pos, DOWN_MODE);
			}
			if (pos != null) {
				if ((pos.getRow() == 0) || (pos.getRow() == pureSrcFileLines.size() - 1)) {
					break;
				}
			}
		}
		return pos;
	}

	private CharPosition getFirstSymPos(CharPosition srcPos, char c, String mode) {
		CharPosition pos = getNearSymPos(srcPos, mode);
		char ch = pureSrcFileLines.get(pos.getRow()).charAt(pos.getColumn());
		if (c == ch) {
			return pos;
		}
		return null;
	}

	public CharPosition getLineSymPos(CharPosition srcPos, String mode) {
		CharPosition pos = new CharPosition(srcPos.getRow(), -1);;
		char[] contentArray = pureSrcFileLines.get(srcPos.getRow()).toCharArray();
		if (mode.equals(UP_MODE)) {
			if (srcPos.getColumn() == 0) {
				return pos = new CharPosition(srcPos.getRow(), -1);
			}
			for (int i = srcPos.getColumn(); i >= 0; i--) {
				if (contentArray[i] != ' ') {
					return pos = new CharPosition(srcPos.getRow(), i);

				} else {
					if (i == 0) {
						return pos = new CharPosition(srcPos.getRow(), -1);
					}
				}
			}
		} else if (mode.equals(DOWN_MODE)) {
			for (int i = srcPos.getColumn(); i < contentArray.length; i++) {
				if (contentArray[i] != ' ') {
					return pos = new CharPosition(srcPos.getRow(), i);
				} else {
					if (i == (contentArray.length - 1)) {
						return pos = new CharPosition(srcPos.getRow(), -1);
					}
				}
			}
		}
		return pos;
	}

	private void filterWords(String[] words, String[] factors, int count) {
		int counter = count;
		String word;
		for (int i = words.length - 1; (i >= 0) && (counter != 0); i--) {
			word = words[i];
			char c;
			if (counter != 0) {
				if (!grammarMatch(factors[counter - 1], word)) {
					if (counter != words.length) {
						if (word.equals("synchronized")) {
							counter--;
							continue;
						}
					}
					char[] wordArray = word.toCharArray();
					for (int j = word.length() - 1; j >= 0; j--) {
						c = wordArray[j];
						if (!isValidChar(c)) {
							word = String.valueOf(wordArray, j + 1, word.length() - j - 1);
							if (grammarMatch(factors[counter - 1], word)) {
								words[counter - 1] = word;
								for (; (i >= 0) && (counter != 0); i--) {
									words[counter - 1] = "";
								}
								return;
							} else {
								if (word.isEmpty()) {
									for (; (i >= 0) && (counter != 0); i--) {
										words[counter - 1] = "";
										counter--;
									}
								}
								return;
							}
						} else {
							if (j == 0) {
								counter--;
								while (counter != 0) {
									if (!grammarMatch(factors[counter - 1], word)) {
										counter--;
									} else {
										break;
									}
								}
								if (counter >= 1) {
									break;
								} else {
									while (i >= 0) {
										words[i] = "";
										i--;
									}
								}
							}
						}
					}
				}
			}
			counter--;
		}
	}

	private String[] getWord(int counter, CharPosition srcPos, String content) {
		String[] words = new String[counter];
		char c;
		char[] conArray = content.toCharArray();
		int end = 0;
		int start = 0;
		CharPosition pos = new CharPosition(srcPos.getRow(), srcPos.getColumn());
		pos.setColumn(pos.getColumn() - 1);
		while (counter != 0) {
			for (int i = pos.getColumn(); (i >= 0) && (counter != 0); i--) {
				i = getNotBlank(conArray, i, 0);
				if (i < 0) {
					break;
				}
				c = conArray[i];
				if (c == '>') {
					end = i + 1;
					pos.setColumn(i);
					pos = rangePairSymbolPosition(pureSrcFileLines, ">", pos);
					start = pos.getColumn();
					String word = String.valueOf(conArray, start, (end - start));
					i = getNotBlank(conArray, i, 0);
					while (i < 0) {
						conArray = fetchContent(pos, 0);
						i = getNotBlank(conArray, i, 0);
						// pos.setColumn(i);
					}
					end = i + 1;
					for (; i >= 0; i--) {
						c = conArray[i];
						if ((c == ' ')) {
							start = i + 1;
							word = word + String.valueOf(conArray, start, (end - start));
							words[counter - 1] = word;
							break;
						} else if (i == 0) {
							start = 0;
							word = word + String.valueOf(conArray, start, (end - start));
							words[counter - 1] = word;
						}
					}

				} else {
					end = i + 1;
					for (i = i - 1; (i >= 0) && (counter != 0); i--) {
						c = conArray[i];
						if ((c == ' ')) {
							start = i + 1;
							String word = String.valueOf(conArray, start, (end - start));
							words[counter - 1] = word;
							break;
						} else if (i == 0) {
							start = 0;
							String word = String.valueOf(conArray, start, (end - start));
							words[counter - 1] = word;
						}
					}
				}
				counter--;
				if (counter == 0) {
					pos.setColumn(i);
					break;
				}
			}
			conArray = fetchContent(pos, 0);
			pos.setColumn(conArray.length - 1);
		}
		return words;
	}

	private int getNotBlank(char[] conArray, int srcPos, int direct) {
		// to left
		if (direct == 0) {
			for (int i = srcPos; i >= 0; i--) {
				if (conArray[i] != ' ') {
					return i;
				}
			} // to right
		} else if (direct == 1) {
			for (int i = srcPos; i < conArray.length; i++) {
				if (conArray[i] != ' ') {
					return i;
				}
			}
		}

		return -1;
	}

	private char[] fetchContent(CharPosition pos, int direct) {
		if (direct == 0) {// up
			pos.rowReduce();
		} else if (direct == 1) {// down
			pos.rowGrow();
		}
		String content = pureSrcFileLines.get(pos.getRow());
		return content.toCharArray();
	}

	public void getMethodName() {

	}

	public void getType() {

	}

	public void getModifier() {

	}

	public boolean isValidChar(char c) {
		if (c == '$' || c == '_' || c == '[' || c == ']') {
			return true;
		}
		if ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9')) {
			return true;
		}
		return false;
	}

	private CharPosition rangePairSymbolPosition(List<String> fileLines, String symbol, CharPosition srcPos) {
		CharPosition pos;
		int position;// String s = " ", String s = " " ";
		/*
		 * if (symbol.equals(">")) { System.out.println(); }
		 */
		if (pairSymbols.get(symbol) == null) {
			pos = new CharPosition(srcPos.getRow(), srcPos.getColumn() - symbol.length());
			position = linePairSymbolMatch(fileLines, symbol, pos);
			while (position == -1) {
				if (pos.getRow() == 0) {
					//pos = null;
					break;
				}
				pos.rowReduce();
				pos.setColumn(fileLines.get(pos.getRow()).length() - 1);
				position = linePairSymbolMatch(fileLines, symbol, pos);
			}
		} else {
			pos = new CharPosition(srcPos.getRow(), srcPos.getColumn() + symbol.length());
			position = linePairSymbolMatch(fileLines, symbol, pos);
			if (position == -1) {
				pos.setColumn(0);
			}
			while (position == -1) {
				if (pos.getRow() == (fileLines.size() - 1)) {
					//pos = null;
					break;
				}
				pos.rowGrow();
				// pos.setColumn(fileLines.get(pos.getRow()).length());
				position = linePairSymbolMatch(fileLines, symbol, pos);
			}
		}
		if (pos != null) {
			pos.setColumn(position);
		}
		return pos;
	}

	private boolean isInStringRange(CharPosition srcPos) {
		return isInStringRange(srcPos, 0, 0, 0);
	}

	private boolean isInStringRange(CharPosition srcPos, int stringStart, int blockStart, int lineStart) {
		CharPosition pos;
		if ((stringContentBoundsPos != null) && (stringContentBoundsPos.get(0).getMarkPosition() != null)) {
			for (int i = stringStart; i < stringContentBoundsPos.size(); i++) {
				pos = stringContentBoundsPos.get(i);
				if (srcPos.getRow() == pos.getRow()) {
					if (inColumnRange(srcPos, pos)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	private boolean inRowRange(CharPosition srcPos, CharPosition rangePos) {
		/*
		 * if ((srcPos.getRow() > rangePos.getRow()) && (srcPos.getRow() <
		 * rangePos.getMarkPosition().getRow())) { return true; } else if
		 * (srcPos.getRow() == rangePos.getRow()) { if (srcPos.getRow() ==
		 * rangePos.getMarkPosition().getRow()) { return inColumnRange(srcPos,
		 * rangePos); } else { if (srcPos.getColumn() > rangePos.getColumn()) {
		 * return true; } } } else if (srcPos.getRow() ==
		 * rangePos.getMarkPosition().getRow()){ if (srcPos.getColumn() <
		 * rangePos.getMarkPosition().getColumn()) { return true; } }
		 */
		return false;
	}

	private boolean inColumnRange(CharPosition srcPos, CharPosition rangePos) {
		if ((srcPos.getColumn() > rangePos.getColumn())
				&& (srcPos.getColumn() < rangePos.getMarkPosition().getColumn())) {
			return true;
		}
		return false;
	}

	private int linePairSymbolMatch(List<String> fileLines, String symbol, CharPosition srcPos) {
		CharPosition pos = new CharPosition(srcPos.getRow(), srcPos.getColumn());
		String content = fileLines.get(srcPos.getRow());
		char c;
		char[] strArray;
		String halfPair = getHalfPair(symbol);
		if (symbol.equals("/*")) {
			halfPair = "/*";
		}
		int symLen = symbol.length();
		strArray = content.toCharArray();
		if (pairSymbols.get(symbol) == null) {
			for (int i = srcPos.getColumn(); i >= 0; i--) {
				c = strArray[i];
				if (symbol.contains(String.valueOf(c))) {
					pos.setColumn(i);
					if (!isInStringRange(pos)) {
						if (symLen > 1) {
							/*
							 * if ((c == '/') && (strArray[--i] == '*')) {
							 * pairEnterCounter++; } else if ((c == '*') &&
							 * (strArray[--i] == '/')){ if (pairEnterCounter >
							 * 0) { pairEnterCounter--; } else if
							 * (pairEnterCounter == 0) { return i; } }
							 */
						} else {
							// if (symbol.equals(halfPair)) {
							pairEnterCounter++;
							// }
						}
					}
				} else {

					if (halfPair.contains(String.valueOf(c))) {
						pos.setColumn(i);
						if (!isInStringRange(pos)) {
							if (pairEnterCounter > 0) {
								pairEnterCounter--;
							} else if (pairEnterCounter == 0) {
								return i;
							}
						}
					}

				}
			}
		} else {
			for (int i = srcPos.getColumn(); i < strArray.length; i++) {
				c = strArray[i];
				if (symbol.contains(String.valueOf(c))) {
					pos.setColumn(i);
					if (!isInStringRange(pos)) {
						if (symLen > 1) {
							if (i != (strArray.length - 1)) {
								if ((c == '*') && (strArray[++i] == '/')) {
									return i;
								}
							}
						} else {
							pairEnterCounter++;
						}
					}
				} else {

					if (halfPair.contains(String.valueOf(c))) {
						pos.setColumn(i);
						if (!isInStringRange(pos)) {
							if (pairEnterCounter > 0) {
								pairEnterCounter--;
							} else if (pairEnterCounter == 0) {
								return i;
							}
						}
					}

				}
			}
		}
		return -1;
	}

	public boolean grammarMatch(String attr, String content) {
		return keyWordMatch(attr, content);
	}

	public boolean keyWordMatch(String attr, String content) {
		String[] types = getTypeCollection(attr);
		boolean isOtherBlock = false;

		if (isCollection(attr)) {
			if (types[0].equals(JavaAttribute.NAME)) {
				for (String s : JavaAttribute.CONTROLBLOCKS) {
					if (content.equals(s)) {
						isOtherBlock = true;
					}
				}
				return (!isOtherBlock);
			}

			for (String s : types) {
				if (content.equals(s)) {
					return true;
				} else {
					/*if (attr.equals(JavaAttribute.TYPE)) {
						return true;
					}*/
					if (attr.equals(JavaAttribute.TYPE)) {
						if (!content.isEmpty()) {
							//System.out.println(content);
							if ((content.charAt(0) >= 'A') && (content.charAt(0) <= 'Z')) {
								return true;
							}
						}
					}
				}
			}
		}
		return false;
	}

	public String[] getTypeCollection(String attr) {
		String[] result;
		switch (attr) {
		case JavaAttribute.MODIFIER:
			result = JavaAttribute.MODIFIERS;
			break;
		case JavaAttribute.TYPE:
			result = JavaAttribute.TYPES;
			break;
		case JavaAttribute.NAME:
			result = JavaAttribute.NAMES;
			break;
		case JavaAttribute.KEYWORD:
			result = JavaAttribute.KEYWORDS;
			break;
		default:
			result = null;
		}
		return result;
	}

	public boolean isCollection(String attr) {
		for (String s : JavaAttribute.TYPECOLLECTION) {
			if (attr.equals(s)) {
				return true;
			}
		}
		return false;
	}

	public String getHalfPair(String symbol) {
		for (String s : symbols) {
			int index = s.indexOf(symbol);
			if (index > 0) {
				return s.substring(0, index);
			} else if (index == 0) {
				return s.substring(symbol.length(), s.length());
			}
		}
		return null;
	}

	private void getInnerClass(CharPosition srcPos) {
		List<CharPosition> methodPos = new ArrayList<>();
		String result = "ok";
		FutureTask<String> task = new FutureTask<>(new TaskParseMethod(srcPos, methodPos), result);
		try {
			if (result.equals(task.get())) {
				executor.execute(task);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
	}

	private List<CharPosition> getStatement(CharPosition srcPos) {
		// List<String> lines = new ArrayList<>();
		// List<CharPosition> linePos = lines;
		CharPosition pos;
		List<CharPosition> statementPos = getStatementEnds(srcPos);
		for (int i = 0; i < statementPos.size(); i++) {
			pos = statementPos.get(i);
			if (i == 1) {
				System.out.println();
			}
			//System.out.println(i);
			getStatementStart(pos);
		}

		parseStatement(statementPos);
		return statementPos;
	}

	private List<CharPosition> getStatementEnds(CharPosition srcPos) {
		List<CharPosition> statementPos = new ArrayList<>();
		String line;
		int position = 0;
		CharPosition pos;
		String symbol = ";";
		for (int i = srcPos.getRow(); i <= srcPos.getMarkPosition().getRow(); i++) {
			// pos = linePos.get(i);
			// position = pos.getColumn();
			line = pureSrcFileLines.get(i);
			/*
			 * if (position == 0) { line =
			 * pureSrcFileLines.get(pos.getRow()).substring(0 ,
			 * (pos.getMarkPosition().getColumn() + 1)); }
			 */
			position = line.indexOf(symbol);
			if (position > -1) {
				pos = new CharPosition(i, position);
				pos.markPosition(new CharPosition(i, position));
				if (i == srcPos.getRow()) {
					if (position < srcPos.getColumn()) {
						continue;
					}
				} else {
					if (i == srcPos.getMarkPosition().getRow()) {
						if (position > srcPos.getMarkPosition().getColumn()) {
							break;
						}
					}
				}
				statementPos.add(pos);
				for (; (position > -1) && (position < line.length() - 1);) {
					position = line.indexOf(symbol, (position + symbol.length()));
					if (position > -1) {
						pos = new CharPosition(i, position);
						pos.markPosition(new CharPosition(i, position));
						if (i == srcPos.getMarkPosition().getRow()) {
							if (position > srcPos.getMarkPosition().getColumn()) {
								break;
							}
						}
						statementPos.add(pos);
					}
				}
			}
		}
		return statementPos;
	}

	private void parseStatement(List<CharPosition> statementPos) {
		List<String[]> varList = new ArrayList<>();
		List<String> invokeList = new ArrayList<>();
		CharPosition pos;
		for (int i = 0; i < statementPos.size(); i++) {
			pos = statementPos.get(i);
			System.out.print(i + " ");
			if (i == 43) {
				System.out.println();
			}
			for (String[] var : getVariable(pos)) {
				varList.add(var);
				System.out.print("variable defined: ");
				for (String s : var)
					if (!s.isEmpty()) {
						System.out.print(s + " ");
					}
			}
			System.out.print("   ");
			for (String invoke : getInvoke(pos)) {
				invokeList.add(invoke);
				System.out.print("invoke method: ");
				if (!invoke.isEmpty()) {
					System.out.print(invoke + "()");
				}
			}
			System.out.println();
		}
	}

	public void getStatementStart(CharPosition statementPos) {
		CharPosition srcPos = statementPos;
		String content = pureSrcFileLines.get(srcPos.getRow());

		char[] strArray = content.toCharArray();

		char c;
		CharPosition pos = new CharPosition(srcPos.getRow(), srcPos.getColumn());
		int position;/* = getNotBlank(strArray, (pos.getColumn() - 1), 0);
		for (int i = pos.getRow(); (i >= 0) && (position == -1); i--) {
			strArray = fetchContent(pos, 0);
			position = getNotBlank(strArray, (strArray.length - 1), 0);
		}*/
		//pos.setColumn(pos.getColumn()  1);
		pos = getNearSymPos(pos, UP_MODE);
		position = pos.getColumn();
		c = pureSrcFileLines.get(pos.getRow()).charAt(position);//strArray[position];
		if ((c == '}') || (c == ')')) {
			pos.setColumn(position);
			//while (true) {
				System.out.println("row " + pos.getRow());
				pos = rangePairSymbolPosition(pureSrcFileLines, String.valueOf(c), pos);
				if (pos.getRow() == 7860)
					System.out.println("row " + pos.getRow() + ", column " + pos.getColumn());
				if (isInStringRange(pos)) {
					//break;
				}
				CharPosition p = pos;//new CharPosition(pos.getRow(), pos.getColumn());
				pos = getNearSymPos(pos, UP_MODE);
				if (c == '}') {
					c = '=';
					if (c != pureSrcFileLines.get(pos.getRow()).charAt(pos.getColumn())) {
						// srcPos.setRow(pos.getRow());
						// srcPos.setColumn(i + 1);
						System.out.println();
						return;
					}
				}
				pos = p;
			//}
		}
		pos.setColumn(pos.getColumn() - 1);
		strArray = pureSrcFileLines.get(pos.getRow()).toCharArray();
		while (true) {
			for (int i = pos.getColumn(); ((i >= 0) && (strArray.length != 0)); i--) {
				i = getNotBlank(strArray, i, 0);
				if (i < 0) {
					break;
				}
				c = strArray[i];
				if ((c == '}') || (c == ')' || (c == '{') || (c == ';'))) {
					pos.setColumn(i);
					if (!isInStringRange(pos)) {
						srcPos.setRow(pos.getRow());
						srcPos.setColumn(i + 1);
						pos.setColumn(-1);
						break;
					}
				}
			}
			if (pos.getColumn() != -1) {
				strArray = fetchContent(pos, 0);
				pos.setColumn(strArray.length - 1);
				if (strArray.length == 0) {
					System.out.println();
					pos.setColumn(0);
				}
			} else {
				break;
			}
		}
		pos.setColumn(srcPos.getColumn());
		position = getNotBlank(strArray, srcPos.getColumn(), 1);
		while (position == -1) {
			strArray = fetchContent(srcPos, 1);
			if (srcPos.getRow() > (pureSrcFileLines.size() - 1)) {
				// return;
			}
			position = getNotBlank(strArray, 0, 1);

		}
		srcPos.setColumn(position);
	}

	private List<String> getInvoke(CharPosition srcPos) {
		CharPosition pos = new CharPosition(srcPos.getRow(), srcPos.getColumn());
		List<String> invokeList = new ArrayList<>();
		CharPosition endPos;
		while (true) {
			StringBuffer buf = new StringBuffer();
			pos = getClosestSymbolPos(pureSrcFileLines, "(", pos, 1);
			endPos = rangePairSymbolPosition(pureSrcFileLines, "(", pos);
			if (pos.getRow() > srcPos.getMarkPosition().getRow()) {
				break;
			} else if (pos.getRow() == srcPos.getMarkPosition().getRow()) {
				if (pos.getColumn() > srcPos.getMarkPosition().getColumn()) {
					break;
				}
			}
			
			String word = getPreviousWord(pos);
			buf.append(word);
			pos.setColumn(pos.getColumn() + 1);
			pos = getFirstSymPos(pos, '.', UP_MODE);

			while (pos != null) {
				buf.insert(0, '.');
				word = getPreviousWord(pos);
				buf.insert(0, word);
				pos.setColumn(pos.getColumn() + 1);
				pos = getFirstSymPos(pos, '.', UP_MODE);
			}
			invokeList.add(buf.toString());
			if (pos == null) {
				break;
			}
			pos = endPos;
		}
		return invokeList;
	}

	private List<String[]> getVariable(CharPosition srcPos) {
		CharPosition pos;
		List<String[]> varList = new ArrayList<>();
		String[] factors = { "modifier", "keyword", "keyword", "type", "name" };
		char[] strArray = pureSrcFileLines.get(srcPos.getRow()).toCharArray();
		int position = getNotBlank(strArray, srcPos.getColumn(), 1);
		while (position == -1) {
			strArray = fetchContent(srcPos, 1);
			if (srcPos.getRow() > (pureSrcFileLines.size() - 1)) {
				// return;
			}
			position = getNotBlank(strArray, srcPos.getColumn(), 1);

		}
		srcPos.setColumn(position);
		pos = srcPos;
		int counter = 0;
		char c;
		String[] words = new String[factors.length];
		strArray = pureSrcFileLines.get(pos.getRow()).toCharArray();
		pos.setColumn(pos.getColumn() - 1);
		while ((counter != 5)) {
			
			words[counter] = getNextWord(pos);
			if (pos.getRow() > srcPos.getMarkPosition().getRow()) {
				break;
			} else if (pos.getRow() == srcPos.getMarkPosition().getRow()) {
				if (pos.getColumn() > srcPos.getMarkPosition().getColumn()) {
					break;
				}
			}
			if (!keyWordMatch(factors[counter], words[counter])) {
				String a = new String(words[counter]);
				/*words[counter] = "";
				counter++;*/
				while (!keyWordMatch(factors[counter], a)) {
					words[counter] = "";
					counter++;
				}
				words[counter] = a;
			}
			if (counter == 3) {
				int row = pos.getRow();
				int column = pos.getColumn();
				pos.setColumn(pos.getColumn() - 1);
				pos = getNearSymPos(pos, DOWN_MODE);
				c = pureSrcFileLines.get(pos.getRow()).charAt(pos.getColumn());
				if (c == '.') {
					varList.clear();
					return varList;
				}
				pos.setRow(row);
				pos.setColumn(column);
			}
			if ((counter == 4) && (!words[counter - 1].isEmpty())) {
				varList.add(words);
				if (!words[counter - 2].isEmpty()) {
					while (true) {
						String[] newWords = new String[words.length];
						for (int i = 0; i <= (counter - 2); i++) {
							newWords[i] = new String(words[i]);
						}

						pos = getFirstSymPos(pos, ',', DOWN_MODE);
						if (pos != null) {

							if (pos.getRow() > srcPos.getMarkPosition().getRow()) {
								break;
							} else if (pos.getRow() == srcPos.getMarkPosition().getRow()) {
								if (pos.getColumn() > srcPos.getMarkPosition().getColumn()) {
									break;
								}
							}
							pos.setColumn(pos.getColumn() + 1);
							newWords[counter - 1] = getWord(pos, 1);
							varList.add(newWords);

						} else {
							break;
						}
					}
				}
			}
			counter++;
		}
		/*if (words[counter - 2].isEmpty()) {
			System.out.println(counter);
			varList.clear();
		}*/
		return varList;
	}
	
	private void getImportStatement() {
		List<HashMap<CharPosition, String>> importStatement = new ArrayList<>();
		
	}
	
	private void getPackageStatement() {
		
	}

	private String getWord(CharPosition srcPos, int direct) {
		char c;
		boolean isWord = false;
		char[] strArray = pureSrcFileLines.get(srcPos.getRow()).toCharArray();
		String word = null;
		int end = 0;
		int counter = 0;
		if (direct == 0) {
			for (int i = srcPos.getColumn(); i >= 0; i--) {
				c = strArray[i];
				if (c != ' ') {
					//end = i;
					counter++;
					isWord = true;
				} else {
					if (isWord) {
						isWord = false;
						word = String.valueOf(strArray, i, counter);
						srcPos.setColumn(i);
						counter = 0;
						return word;
					}
				}
				if ((i == 0) && (word == null)) {
					//strArray = fetchContent(pos, )
				}
			}
		}
		return null;
	}
	
	private String getPreviousWord(CharPosition srcPos) {
		char c;
		boolean isWord = false;
		char[] strArray;
		CharPosition pos;
		String word = null;
		int wordCounter = 0;

		pos = getNearSymPos(srcPos, UP_MODE);
		strArray = pureSrcFileLines.get(pos.getRow()).toCharArray();
		for (int i = pos.getColumn(); i >= 0; i--) {
			c = strArray[i];
			if (isValidChar(c)) {
				wordCounter++;
				if (!isWord) {
					isWord = true;
				}
			} else {
				if (c == '>') {
					isWord = true;
				}
				if (isWord) {
					isWord = false;

					String key;
					isWord = false;
					StringBuffer buf = new StringBuffer();
					if (c == '>') {
						pos.setColumn(i);
						key = getTagContent(pos, String.valueOf(c));
						word = getPreviousWord(pos);
						buf.append(word);
						buf.append(key);
						wordCounter = 0;
					} else {
						word = String.valueOf(strArray, (i + 1), wordCounter);
						buf.append(word);
						pos.setColumn(i);
						wordCounter = 0;
					}
					word = buf.toString();
					break;
				}
			}
			
			if ((i == 0) && (isWord)) {
				word = String.valueOf(strArray, i, wordCounter);
				wordCounter = 0;
				pos.setColumn(i);
				break;
			}
		}
		srcPos.setRow(pos.getRow());
		srcPos.setColumn(pos.getColumn());
		return word;
	}
	
	private String getNextWord(CharPosition srcPos) {
		char c;
		boolean isWord = false;
		char[] strArray;
		String word = null;
		CharPosition pos;
		int wordCounter = 0;
		pos = getNearSymPos(srcPos, DOWN_MODE);
		strArray = pureSrcFileLines.get(pos.getRow()).toCharArray();
		for (int i = pos.getColumn(); i < strArray.length; i++) {
			c = strArray[i];
			if (isValidChar(c)) {
				wordCounter++;
				if (!isWord) {
					isWord = true;
				}
			} else {
				if (c == '<') {
					isWord = true;
				}
				if (isWord) {
					String key;
					isWord = false;
					StringBuffer buf = new StringBuffer();
					if (c == '<') {
						pos.setColumn(i);
						word = String.valueOf(strArray, (i - wordCounter), wordCounter);
						key = getTagContent(pos, String.valueOf(c));
						buf.append(word);
						buf.append(key);
						wordCounter = 0;
					} else {
						word = String.valueOf(strArray, (i - wordCounter), wordCounter);
						buf.append(word);
						pos.setColumn(i);
						int row = pos.getRow();
						pos = getNearSymPos(pos, DOWN_MODE);
						c = pureSrcFileLines.get(pos.getRow()).charAt(pos.getColumn());
						if (c == '<') {
							key = getTagContent(pos, String.valueOf(c));
							buf.append(key);
						} else {
							pos.setRow(row);
							pos.setColumn(i);
						}
						wordCounter = 0;
					}
					word = buf.toString();
					break;
				}
			}
			
			if ((i == (strArray.length - 1)) && (isWord)) {
				word = String.valueOf(strArray, (i - wordCounter + 1), wordCounter);
				wordCounter = 0;
				pos.setColumn(i);
				break;
			}
		}
		srcPos.setRow(pos.getRow());
		srcPos.setColumn(pos.getColumn());
		return word;
	}
	
	private String getTagContent(CharPosition srcPos, String sym) {
		String content = null;
		CharPosition startPos;
		CharPosition pos;
		CharPosition endPos;
		int row = srcPos.getRow();
		int column = srcPos.getColumn();
		pos = rangePairSymbolPosition(pureSrcFileLines, sym, srcPos);
		srcPos.setRow(pos.getRow());
		srcPos.setColumn(pos.getColumn());
		if (pos.getRow() < row) {
			startPos = pos;
			endPos = new CharPosition(row, column);
		} else if (pos.getRow() > row){
			startPos = new CharPosition(row, column);
			endPos = pos;
		} else {
			if (column < pos.getColumn()) {
				startPos = new CharPosition(row, column);
				endPos = pos;
			} else {
				startPos = pos;
				endPos = new CharPosition(row, column);
			}
		}
		StringBuffer buf = new StringBuffer();
		String key;
		if (pos.getRow() == row) {
			key = pureSrcFileLines.get(startPos.getRow())
					.substring(startPos.getColumn(), (endPos.getColumn() + 1));
			buf.append(key);
		} else {
			key = pureSrcFileLines.get(startPos.getRow())
					.substring(startPos.getColumn(), (endPos.getColumn() + 1));
			buf.append(key);
			for (int d = startPos.getRow() + 1; d <= endPos.getRow(); d++) {
				String temp = pureSrcFileLines.get(d);
				if (d == endPos.getRow()) {
					temp = pureSrcFileLines.get(endPos.getRow()).substring(0,
							(endPos.getColumn() + 1));
				}
				buf.append(temp);
			}
			
		}
		content = buf.toString();
		return content;
	}

	public void parseAnnotation() {

	}

	public void parseString() {
		String line;
		int column;
		char c;
		CharPosition pos;
		// System.out.println(stringContentBoundsPos.size() + " left");
		for (int i = 0; i < stringContentBoundsPos.size(); i++) {
			pos = stringContentBoundsPos.get(i);
			line = pureSrcFileLines.get(pos.getRow());
			column = pos.getColumn();
			// System.out.println(i + ": " +
			// srcFileLines.get(stringContentBoundsPos.get(i).getRow()));
			if (column > 0) {
				c = line.charAt(column - 1);
				if (c == '\\') {
					// stringContentBoundsPos.remove(i);
					// System.out.println("row " + pos.getRow() + ", " +
					// pos.getColumn() + " removed " + i);
					if (i == 247) {
						// System.out.println();
					}
					stringContentBoundsPos.remove(i);
					i--;
					// quoteCount++;
				}
			}
		}
		// System.out.println(stringContentBoundsPos.size() + " left");

		// System.out.println(stringContentBoundsPos.size() + " left");
		stringContentBoundsPos = removeBoundsElem(stringContentBoundsPos, blockCommentBoundsPos);
		// System.out.println(stringContentBoundsPos.size() + " left");
		// System.out.println("removed " + quoteCount);
		removeLineElem(stringContentBoundsPos, lineCommentPos);
		for (int i = 0; i < stringContentBoundsPos.size(); i = i + 2) {
			pos = stringContentBoundsPos.get(i);
			pos.markPosition(stringContentBoundsPos.get((i + 1)));
			stringContentBoundsPos.remove(i + 1);
			i--;
		}
		// System.out.println("com");
		// CharPosition endPos = rangePairSymbolPosition("\"", srcPos);
		// srcPos.markPosition(endPos);
		// return endPos;
	}

	public void parseComment() {
		CharPosition srcPos;
		CharPosition endPos;
		for (int i = 0; i < blockCommentBoundsPos.size(); i++) {
			srcPos = blockCommentBoundsPos.get(i);
			endPos = rangePairSymbolPosition(srcFileLines, "/*", srcPos);
			if (endPos == null) {
				System.out.println("format error, missing '*/' ");
			} else {
				srcPos.markPosition(endPos);
			}
		}

		for (int i = 0; i < lineCommentPos.size(); i++) {
			srcPos = lineCommentPos.get(i);
			srcPos.markPosition(srcPos);
		}
	}
	
	public void findAttribute(String attribute, List<CharPosition> list) {
		findAttribute(attribute, list, srcFileLines);
	}

	public void findAttribute(String attribute, List<CharPosition> list, List<String> lines) {
		CharPosition pos;
		String symbol = attribute;// pairSymbols.get(attribute);
		String line;
		int position = 0;
		for (int i = 0; i < lines.size(); i++) {

			/*
			 * if (attribute.equals("\"") && (i == 218)) { System.out.println();
			 * }
			 */
			line = lines.get(i);
			position = line.indexOf(symbol, position);
			if (position > -1) {
				pos = new CharPosition(i, position);
				list.add(pos);
				for (; (position > -1) && (position < line.length() - 1);) {
					position = line.indexOf(symbol, (position + symbol.length()));
					if (position > -1) {
						pos = new CharPosition(i, position);
						list.add(pos);
					}
				}
			}
		}
	}

	private CharPosition getClosestSymbolPos(List<String> lines, String symbol, CharPosition srcPos, int direct) {
		CharPosition pos = new CharPosition(srcPos.getRow(), srcPos.getColumn());
		int position = 0;
		int counter = 0;
		char[] symArray = symbol.toCharArray();
		char[] strArray = lines.get(pos.getRow()).toCharArray();
		if (direct == 0) {
			counter = symArray.length - 1;
			while (true) {
				position = pos.getColumn() - 1;
				for (int i = position; i >= 0; i--) {
					if (strArray[i] == symArray[counter]) {
						counter--;
					} else {
						if (counter < symArray.length) {
							i = i + symArray.length - 1 - counter - 1;
							counter = symArray.length;
							continue;
						}
					}
					if (counter == -1) {
						pos.setColumn(i);
						break;
					}
				}
				if (counter != -1) {
					strArray = fetchContent(pos, 0);
					pos.setColumn(strArray.length);
				} else {
					break;
				}
			}
		} else if (direct == 1) {
			counter = 0;
			while (true) {
				position = pos.getColumn() + 1;
				for (int i = position; i < strArray.length; i++) {
					if (i == 75) {
						System.out.println();
					}
					if (strArray[i] == symArray[counter]) {
						counter++;
					} else {
						if (counter > 0) {
							i = i - counter + 1;
							counter = 0;
							continue;
						}
					}
					if (counter == symArray.length) {
						pos.setColumn(i);
						break;
					}
				}
				if (counter != symArray.length) {
					strArray = fetchContent(pos, 1);
					pos.setColumn(0);
				} else if (counter == symArray.length) {
					break;
				}
			}
		}
		return pos;
	}

	private void removeStringContent() {
		// List<String> pureSrcFileLines = new ArrayList<>();
		// java.util.Collections.copy(pureSrcFileLines, srcFileLines);
		// for (int i = 0; i < srcFileLines.size(); i++) {
		pureSrcFileLines.addAll(srcFileLines);
		// }
		String line;
		for (int i = 0; i < pureSrcFileLines.size(); i++) {
			line = pureSrcFileLines.get(i);
			// if (i )
		}
		CharPosition pos;
		char[] strArray;
		for (int i = 0; i < blockCommentBoundsPos.size(); i++) {
			pos = blockCommentBoundsPos.get(i);
			if (pos.getRow() < pos.getMarkPosition().getRow()) {
				line = pureSrcFileLines.get(pos.getRow());
				strArray = line.toCharArray();
				replaceToBlank(strArray, pos.getColumn(), (strArray.length - 1));
				pureSrcFileLines.set(pos.getRow(), String.valueOf(strArray));
				for (int j = pos.getRow() + 1; j <= (pos.getMarkPosition().getRow() - 1); j++) {
					line = pureSrcFileLines.get(j);
					strArray = line.toCharArray();
					replaceToBlank(strArray, 0, (strArray.length - 1));
					;
					pureSrcFileLines.set(j, String.valueOf(strArray));
				}
				line = pureSrcFileLines.get(pos.getMarkPosition().getRow());
				strArray = line.toCharArray();
				replaceToBlank(strArray, 0, pos.getMarkPosition().getColumn());
				pureSrcFileLines.set(pos.getMarkPosition().getRow(), String.valueOf(strArray));
			} else if (pos.getRow() == pos.getMarkPosition().getRow()) {
				line = pureSrcFileLines.get(pos.getRow());
				strArray = line.toCharArray();
				replaceToBlank(strArray, pos.getColumn(), pos.getMarkPosition().getColumn());
				pureSrcFileLines.set(pos.getRow(), String.valueOf(strArray));
			}
		}

		for (int i = 0; i < lineCommentPos.size(); i++) {
			pos = lineCommentPos.get(i);
			line = pureSrcFileLines.get(pos.getRow());
			strArray = line.toCharArray();
			replaceToBlank(strArray, pos.getColumn(), (strArray.length - 1));
			pureSrcFileLines.set(pos.getRow(), String.valueOf(strArray));
		}
		File file = new File("./Component3.java");
		if (!file.exists()) {
			try {
				file.createNewFile();
				System.out.println("file created");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		FileWriter writer = null;
		BufferedWriter bw = null;
		try {
			writer = new FileWriter(file);
			bw = new BufferedWriter(writer);

			for (int i = 0; i < pureSrcFileLines.size(); i++) {
				bw.write(pureSrcFileLines.get(i) + "\r\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				bw.close();
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

	}

	private void replaceToBlank(char[] data, int start, int end) {
		for (int i = start; i <= end; i++) {
			data[i] = ' ';
		}
	}

	private List<CharPosition> removeBoundsElem(List<CharPosition> list, List<CharPosition> refList) {
		CharPosition cPos;
		CharPosition sPos;
		int index = 0;
		List<CharPosition> newList = new ArrayList<>();
		/*
		 * if (list.get(0) == null) { System.out.println("empty"); }
		 */
		for (int i = 0; i < list.size(); i++) {
			cPos = list.get(i);
			// System.out.println(i);
			/*
			 * if (cPos == null) { System.out.println("empty"); }
			 */
			for (int j = index; j < refList.size(); j++) {
				sPos = refList.get(j);
				/*
				 * if (cPos == null) { System.out.println("empty"); }
				 */

				/*
				 * if (sPos == null) { System.out.println("empty"); } if
				 * (sPos.getMarkPosition() == null) { System.out.println(i);
				 * System.out.println("empty"); }
				 */
				if ((cPos.getRow() > sPos.getRow()) && (cPos.getRow() < sPos.getMarkPosition().getRow())) {
					// stringContentBoundsPos.remove(j);

					if (cPos != null) {
						// System.out.println("row " + cPos.getRow() + ", " +
						// cPos.getColumn() + " removed caused in range");
						// list.remove(j);

						// System.out.println(i + " removed " + "row at " +
						// cPos.getRow());
						/*
						 * System.out.println("cPos row " + cPos.getRow() +
						 * " column " + cPos.getColumn() + " mark row " +
						 * cPos.getMarkPosition().getRow() + " column " +
						 * cPos.getMarkPosition().getColumn());
						 * System.out.println("sPos row " + sPos.getRow() +
						 * " column " + sPos.getColumn() + " mark row " +
						 * sPos.getMarkPosition().getRow() + " column " +
						 * sPos.getMarkPosition().getColumn());
						 */
						cPos = null;
						quoteCount++;
						break;
					}
				} else {
					if ((sPos.getRow() == cPos.getRow())) {
						if (sPos.getMarkPosition().getRow() == cPos.getRow()) {
							if ((cPos.getColumn() > sPos.getColumn())
									&& (cPos.getColumn() < sPos.getMarkPosition().getColumn())) {
								if (cPos != null) {
									// System.out.println("row " + cPos.getRow()
									// + ", " + cPos.getColumn() + " removed
									// caused in range");
									// list.remove(j);
									// System.out.println(i + " removed " + "row
									// at " + cPos.getRow());
									/*
									 * System.out.println("cPos row " +
									 * cPos.getRow() + " column " +
									 * cPos.getColumn() + " mark row " +
									 * cPos.getMarkPosition().getRow() +
									 * " column " +
									 * cPos.getMarkPosition().getColumn());
									 * System.out.println("sPos row " +
									 * sPos.getRow() + " column " +
									 * sPos.getColumn() + " mark row " +
									 * sPos.getMarkPosition().getRow() +
									 * " column " +
									 * sPos.getMarkPosition().getColumn());
									 */
									cPos = null;
									quoteCount++;
									break;
								}
							}
						} else {
							if ((cPos.getColumn() > sPos.getColumn())
									|| (cPos.getColumn() < sPos.getMarkPosition().getColumn())) {
								if (cPos != null) {
									// System.out.println("row " + cPos.getRow()
									// + ", " + cPos.getColumn() + " removed
									// caused in range");
									// list.remove(j);
									// System.out.println(i + " removed " + "row
									// at " + cPos.getRow());
									/*
									 * System.out.println("cPos row " +
									 * cPos.getRow() + " column " +
									 * cPos.getColumn() + " mark row " +
									 * cPos.getMarkPosition().getRow() +
									 * " column " +
									 * cPos.getMarkPosition().getColumn());
									 * System.out.println("sPos row " +
									 * sPos.getRow() + " column " +
									 * sPos.getColumn() + " mark row " +
									 * sPos.getMarkPosition().getRow() +
									 * " column " +
									 * sPos.getMarkPosition().getColumn());
									 */
									cPos = null;
									quoteCount++;
									break;
								}
							}
						}
					}
					index = j;
					if (cPos.getRow() < sPos.getRow()) {
						break;
					}
					/*
					 * if (cPos.getRow() > sPos.getMarkPosition().getRow()) {
					 * index = j; continue; }
					 */
				}
			}
			if (cPos != null) {
				newList.add(cPos);
			}
		}
		return newList;
	}

	private void removeLineElem(List<CharPosition> list, List<CharPosition> targetList) {
		CharPosition targetPos;
		CharPosition pos;
		int index = 0;
		for (int i = 0; i < list.size(); i++) {
			pos = list.get(i);
			for (int j = index; j < targetList.size(); j++) {
				targetPos = targetList.get(j);
				if (pos.getRow() == targetPos.getRow()) {
					if (pos.getColumn() > targetPos.getColumn()) {
						list.remove(j);
						System.out.println("row " + pos.getRow() + ", column " + pos.getColumn() + " removed " + i);
						index = j;
						i--;
						break;
					}
				}
				index = j;
				if (pos.getRow() < targetPos.getRow()) {
					break;
				}
			}
		}
	}

	class TaskFind implements Runnable {

		@Override
		public void run() {
			String result1 = "result1";
			String result2 = "result2";
			String result3 = "result3";
			String result4 = "result4";
			FutureTask<String> t1 = new FutureTask<>(new TaskFindAttribute("/*", blockCommentBoundsPos), result1);
			FutureTask<String> t2 = new FutureTask<>(new TaskFindAttribute("//", lineCommentPos), result2);
			FutureTask<String> t3 = new FutureTask<>(new TaskFindAttribute("\"", stringContentBoundsPos), result3);
			FutureTask<String> t4 = new FutureTask<>(new TaskFindAttribute("{", blockStartPos), result4);
			executor.execute(t1);
			executor.execute(t2);
			executor.execute(t3);
			executor.execute(t4);
			try {
				System.out.println("fetching...");
				t4.get();
				t3.get();
				t2.get();
				t1.get();
				System.out.println("blockCommentBoundsPos size " + blockCommentBoundsPos.size());
				System.out.println("lineCommentPos size " + lineCommentPos.size());
				System.out.println("stringContentBoundsPos size " + stringContentBoundsPos.size());
				System.out.println("blockStartPos size " + blockStartPos.size());
				System.out.println("completed");
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		}

	}
	
	class TaskParseStatement implements Runnable {

		private CharPosition srcPos;
		
		public TaskParseStatement(CharPosition srcPos) {
			this.srcPos = srcPos;
		}
		
		@Override
		public void run() {
			getStatement(srcPos);
		}
		
	}

	class TaskParseMethod implements Runnable {
		
		private int start;
		private int end;
		private List<CharPosition> methodPos;

		public TaskParseMethod(CharPosition srcPos, List<CharPosition> methodPos) {
			start = srcPos.getRow();
			end = srcPos.getMarkPosition().getRow();
			this.methodPos = methodPos;
		}
		
		@Override
		public void run() {
			System.out.println("search method...");
			getJMethod((start + 1), end, methodPos);
			
		}

	}

	class TaskParseComment implements Runnable {

		@Override
		public void run() {
			getJComment();
			removeStringContent();
		}

	}

	class TaskParseString implements Runnable {

		@Override
		public void run() {
			getStringContent();
			// parseString();
		}

	}

	class TaskFindAttribute implements Runnable {
		String attr;
		List<CharPosition> list;
		List<String> lines;
		
		public TaskFindAttribute(String attr, List<CharPosition> list) {
			this(attr, list, srcFileLines);
		}

		public TaskFindAttribute(String attr, List<CharPosition> list, List<String> lines) {
			this.attr = attr;
			this.list = list;
			this.lines = lines;
		}

		@Override
		public void run() {
			findAttribute(attr, list, lines);
		}
	}

	class TaskLoadFile implements Runnable {

		@Override
		public void run() {
			BufferedReader br;
			FileReader reader;
			try {
				reader = new FileReader(srcFile);
				br = new BufferedReader(reader);

				String line = "";
				while (line != null) {
					line = br.readLine();
					srcFileLines.add(line);
				}
				srcFileLines.remove(srcFileLines.size() - 1);
				srcFileLines.trimToSize();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static class CharPosition {

		int column;
		int row;

		CharPosition markPosition;

		public CharPosition() {

		}

		public CharPosition(int row, int column) {
			this.column = column;
			this.row = row;
		}

		public void rowGrow() {
			row++;
		}

		public void rowReduce() {
			row--;
		}

		public int getBeforeRow() {
			return (row - 1);
		}

		public int getNextRow() {
			return (row + 1);
		}

		public int getColumn() {
			return column;
		}

		public void setColumn(int column) {
			this.column = column;
		}

		public int getBeforeRow(int count) {
			return (row - count);
		}

		public int getNextRow(int count) {
			return (row + count);
		}

		public int getRow() {
			return row;
		}

		public void setRow(int row) {
			this.row = row;
		}

		public void markPosition(CharPosition markPosition) {
			this.markPosition = new CharPosition(markPosition.getRow(), markPosition.getColumn());
		}

		public CharPosition getMarkPosition() {
			return markPosition;
		}
	}

	@Override
	public String[] getCollectionType(String name) {
		return null;
	}

	@Override
	public boolean matchType(String factor, String content) {
		return false;
	}

}
