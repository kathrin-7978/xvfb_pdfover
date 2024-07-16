package at.asit.pdfover.gui.tests;

import java.io.File;

import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;

public class FileExistsCondition extends DefaultCondition {
	
	private final File file;
	
	public FileExistsCondition(File file) {
		this.file = file;
	}
	
	@Override
	public boolean test() throws Exception {
		return file.exists();
	}

	@Override
	public String getFailureMessage() {
		return String.format("Could not create output file %s", file.getName());
	}

}
