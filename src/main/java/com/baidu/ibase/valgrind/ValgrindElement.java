package com.baidu.ibase.valgrind;

import java.io.IOException;

public class ValgrindElement {
	private String type;
	private int bytes;
	private int blocks;

    // set by attributes
    public void setType(String type) {
        this.type = type;
    }

    // set by attributes
    public void setValue(int bytes, int blocks) {
        this.bytes= bytes;
        this.blocks = blocks;
    }
    
    void addTo(AbstractReport<?,?> report) throws IOException {

    	Ratio r = null;
    	if(type.equals("definety, %")) {
    		r = report.definity;
        } else if(type.equals("indirect, %")) {
    		r = report.indirect;
        } else if(type.equals("reachable, %")) {
    		r = report.reachable;
        } else if(type.equals("possible, %")) {
    		r = report.possible;
        } else {
            throw new IllegalArgumentException("Invalid type: "+type);
        }
    	r.addValue(bytes, blocks);

    }
}
