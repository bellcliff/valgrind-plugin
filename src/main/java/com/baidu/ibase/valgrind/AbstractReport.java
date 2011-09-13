package com.baidu.ibase.valgrind;

import hudson.model.AbstractBuild;
import hudson.model.ModelObject;

import java.io.IOException;

public abstract class AbstractReport<PARENT extends AggregatedReport<?, PARENT, ?>, SELF extends ValgrindObject<SELF>>
		extends ValgrindObject<SELF> implements ModelObject {

	private String name;

	private PARENT parent;

	public void addCoverage(ValgrindElement me) throws IOException {
		me.addTo(this);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDisplayName() {
		return name;
	}

	/**
	 * Called at the last stage of the tree construction, to set the back
	 * pointer.
	 */
	protected void setParent(PARENT p) {
		this.parent = p;
	}

	/**
	 * Gets the back pointer to the parent coverage object.
	 */
	public PARENT getParent() {
		return parent;
	}

	@Override
	public SELF getPreviousResult() {
		PARENT p = parent;
		while (true) {
			p = p.getPreviousResult();
			if (p == null)
				return null;
			SELF prev = (SELF) p.getChildren().get(name);
			if (prev != null)
				return prev;
		}
	}

	@Override
	public AbstractBuild<?, ?> getBuild() {
		return parent.getBuild();
	}
}
