package com.baidu.ibase.valgrind.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class IOHelper {
	public static StringBuilder read(InputStream in) throws IOException{
		StringBuilder sb = new StringBuilder();
		// set charset here
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
		while(bufferedReader.ready()){
			sb.append(bufferedReader.readLine());
			sb.append(System.getProperty("line.separator"));
		}
		return sb;
	}
}
