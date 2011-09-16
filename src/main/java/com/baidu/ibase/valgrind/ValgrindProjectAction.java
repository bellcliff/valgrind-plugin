package com.baidu.ibase.valgrind;

import hudson.model.Action;
import hudson.model.Result;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;

import java.io.IOException;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

public class ValgrindProjectAction implements Action {
	public final AbstractProject<?, ?> project;

	public ValgrindProjectAction(AbstractProject<?, ?> project) {
		this.project = project;
	}

	public String getIconFileName() {
		return "graph.gif";
	}

	public String getDisplayName() {
		return "valgrind";
	}

	public String getUrlName() {
		return "valgrind";
	}

	/**
	 * Gets the most recent {@link EmmaBuildAction} object.
	 */
	public ValgrindBuildAction getLastResult() {
		for (AbstractBuild<?, ?> b = project.getLastBuild(); b != null; b = b
				.getPreviousBuild()) {
			//show valgrind result even if build fail.
//			if (b.getResult() == Result.FAILURE)
//				continue;
			ValgrindBuildAction r = b.getAction(ValgrindBuildAction.class);
			if (r != null)
				return r;
		}
		return null;
	}

	public void doGraph(StaplerRequest req, StaplerResponse rsp)
			throws IOException {
		if (getLastResult() != null)
			getLastResult().doGraph(req, rsp);
	}
}
