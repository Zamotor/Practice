package com.example.language;

public interface JavaAttribute extends Attribute {

	public static final String SRC_SUBFFIX = ".java";

	public static final String PACKAGE = "package";
	
	public static final String IMPORT = "import";
	
	public static final String CLASS = "class";
	
	public static final String MODIFIER = "modifier";
	
	public static final String TYPE = "type";
	
	public static final String NAME = "name";
	
	public static final String KEYWORD = "keyword";
	
	public static final String STATIC = "static";
	
	public static final String FINAL = "final";
	
	public static final String ANNOTATION = "annotation";
	
	public static final String BLOCK = "block";
	
	public static final String[] TYPECOLLECTION = {
			MODIFIER, TYPE, NAME, KEYWORD
	};
	
	public static final String[] MODIFIERS = {
			"private", "protected", "public", "package"
	};
	
	public static final String[] TYPES = {
			"boolean", "int", "float", "double", "long", "short", "byte", "char",
			"void", "class", "interface"
	};
	
	public static final String[] NAMES = {"name"};
	
	public static final String[] CONTROLBLOCKS = {"if", "while", "for", "synchronized", "catch"};
	
	public static final String[] KEYWORDS = {"static", "final", "extends", "implements", "transient"
			, "volatile", "throws"};
}
