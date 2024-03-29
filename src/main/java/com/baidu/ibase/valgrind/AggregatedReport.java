package com.baidu.ibase.valgrind;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

public abstract class AggregatedReport<PARENT extends AggregatedReport<?, PARENT, ?>, SELF extends AggregatedReport<PARENT, SELF, CHILD>, CHILD extends AbstractReport<SELF, CHILD>>
		extends AbstractReport<PARENT, SELF> {

	private final Map<String, CHILD> children = new HashMap<String, CHILD>();

	public void add(CHILD child) {
		children.put(child.getName(), child);
		setRatios(child.getRatios());
	}	

	public Map<String, CHILD> getChildren() {
		return children;
	}

	protected void setParent(PARENT p) {
		super.setParent(p);
		for (CHILD c : children.values())
			c.setParent((SELF) this);
	}

	public CHILD getDynamic(String token, StaplerRequest req,
			StaplerResponse rsp) throws IOException {
		return getChildren().get(token);
	}

	@Override
	public void setFailed() {
		super.setFailed();

		if (getParent() != null)
			getParent().setFailed();
	}

	public boolean hasChildren() {
		return getChildren().size() > 0;
	}

}
