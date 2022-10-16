package org.apache.maven.cli;

import org.codehaus.plexus.classworlds.ClassWorld;

public class DefaultCliRequest extends CliRequest{

	public DefaultCliRequest(String[] args, ClassWorld classWorld) {
		super(args, classWorld);
	}
	
	public void setWorkingDirectory(String directory) {
		this.workingDirectory = directory;
	}
}
