package com.baidu.ibase.valgrind;

import hudson.ExtensionPoint;
import hudson.model.TaskListener;

import java.io.Serializable;

public abstract class Rule implements Serializable, ExtensionPoint {
    public abstract void enforce(ValgrindReport report, TaskListener listener);

    private static final long serialVersionUID = 1L;
}
