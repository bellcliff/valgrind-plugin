package com.baidu.ibase.valgrind;

import java.io.Serializable;

public class Ratio implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	int bytes = 0;
	int blocks = 0;
	boolean initialized = false;
	
	@Override
	public String toString() {
		return bytes + "bytes in " + blocks + "blocks";
	}
	
    public boolean isInitialized() {
    	return initialized;
    }
    
    public void addValue(int bytes, int blocks) {
        this.bytes = bytes;
        this.blocks = blocks;
		initialized = true;
    }
}
