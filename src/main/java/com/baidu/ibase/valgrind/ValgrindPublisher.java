package com.baidu.ibase.valgrind;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;

import java.io.File;
import java.io.IOException;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.StaplerRequest;

public class ValgrindPublisher extends Recorder {

	/**
	 * Relative path to the Emma XML file inside the workspace.
	 */
	public String includes;

	/**
	 * Rule to be enforced. Can be null.
	 * 
	 * TODO: define a configuration mechanism.
	 */
	public Rule rule;

	public ValgrindHealthReportThresholds headlthThresholds = new ValgrindHealthReportThresholds();

	/**
	 * look for reports based in the configured parameter includes.
	 * <ul>
	 * 'includes' is
	 * <li>- absolute path
	 * <li>- relative path to workspace
	 * <li>- an Ant-style pattern
	 * <li>- a list of files and folders separated by the characters ;:,
	 */
	protected static FilePath[] locateReport(FilePath workspace, String includes)
			throws IOException, InterruptedException {
		// support absolute path
		FilePath dir = new FilePath(new File(includes));
		if (dir.exists())
			return dir.list("*.xml");
		if (new FilePath(workspace, includes).exists())
			return dir.list("*.xml");
		// search in workspace
		return workspace.list(includes);
	}

	protected static void saveReport(FilePath folder, FilePath[] files)
			throws IOException, InterruptedException {
		folder.mkdirs();
		for (int i = 0; i < files.length; i++) {
			FilePath src = files[i];
			FilePath dst = folder.child(src.getName());
			src.copyTo(dst);
		}
	}

	@Override
	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher,
			BuildListener listener) throws InterruptedException, IOException {

		FilePath[] reports = locateReport(build.getWorkspace(), includes);
		if (reports.length == 0) {
			build.setResult(Result.FAILURE);
			return true;
		}
		saveReport(getValgrindReportPath(build), reports);

		final ValgrindBuildAction action = new ValgrindBuildAction(build, rule,
				ValgrindReport.parse(reports), headlthThresholds);
		build.getActions().add(action);
		
		return true;
	}

	public Action getProjectAction(AbstractProject<?, ?> project) {
		return new ValgrindProjectAction(project);
	}

	public BuildStepMonitor getRequiredMonitorService() {
		return BuildStepMonitor.BUILD;
	}

	static FilePath getValgrindReportPath(AbstractBuild<?, ?> build) {
		return new FilePath(new File(build.getRootDir(), "valgrind"));
	}

	@Override
	public BuildStepDescriptor<Publisher> getDescriptor() {
		return DESCRIPTOR;
	}

	@Extension
	public static final BuildStepDescriptor<Publisher> DESCRIPTOR = new DescriptorImpl();

	public static class DescriptorImpl extends BuildStepDescriptor<Publisher> {
		public DescriptorImpl() {
			super(ValgrindPublisher.class);
		}

		@Override
		public boolean isApplicable(
				@SuppressWarnings("rawtypes") Class<? extends AbstractProject> jobType) {
			return true;
		}

		@Override
		public Publisher newInstance(StaplerRequest req, JSONObject formData)
				throws hudson.model.Descriptor.FormException {
			ValgrindPublisher publisher = new ValgrindPublisher();
			req.bindParameters(publisher, "valgrind.");
			return publisher;
		}

		@Override
		public String getDisplayName() {
			return "Valgrind";
		}
	}
}
