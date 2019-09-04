package com.example.main;

import com.example.ui.MainActivity;

public class Application {

	private static MainActivity mainActivity;
	
	public static void main(String[] args) {
		
		mainActivity = new MainActivity();
		mainActivity.onCreate();
		
	} 

}
