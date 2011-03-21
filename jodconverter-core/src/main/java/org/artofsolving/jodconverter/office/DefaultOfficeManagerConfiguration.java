//
// JODConverter - Java OpenDocument Converter
// Copyright 2011 Art of Solving Ltd
// Copyright 2004-2011 Mirko Nasato
//
// JODConverter is free software: you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public License
// as published by the Free Software Foundation, either version 3 of
// the License, or (at your option) any later version.
//
// JODConverter is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General
// Public License along with JODConverter.  If not, see
// <http://www.gnu.org/licenses/>.
//
package org.artofsolving.jodconverter.office;

import java.io.File;

import org.artofsolving.jodconverter.process.SigarProcessManager;

import com.google.common.base.Preconditions;

public class DefaultOfficeManagerConfiguration {

    private File officeHome = OfficeUtils.getDefaultOfficeHome();
    private OfficeConnectionProtocol connectionProtocol = OfficeConnectionProtocol.SOCKET;
    private int[] portNumbers = new int[] { 2002 };
    private String[] pipeNames = new String[] { "office" };
    private String[] runAsArgs = null;
    private File templateProfileDir = null;
    private long taskQueueTimeout = 30000L;  // 30 seconds
    private long taskExecutionTimeout = 120000L;  // 2 minutes
    private int maxTasksPerProcess = 200;

    public DefaultOfficeManagerConfiguration setOfficeHome(String officeHome) throws NullPointerException, IllegalArgumentException {
        Preconditions.checkNotNull("officeHome", officeHome);
        return setOfficeHome(new File(officeHome));
    }

    public DefaultOfficeManagerConfiguration setOfficeHome(File officeHome) throws NullPointerException, IllegalArgumentException  {
        Preconditions.checkNotNull("officeHome", officeHome);
        checkArgument("officeHome", officeHome.isDirectory(), "must exist and be a directory");
        this.officeHome = officeHome;
        return this;
    }

    public DefaultOfficeManagerConfiguration setConnectionProtocol(OfficeConnectionProtocol connectionProtocol) throws NullPointerException {
        Preconditions.checkNotNull("connectionProtocol", connectionProtocol);
        this.connectionProtocol = connectionProtocol;
        return this;
    }

    public DefaultOfficeManagerConfiguration setPortNumber(int portNumber) {
        this.portNumbers = new int[] { portNumber };
        return this;
    }

    public DefaultOfficeManagerConfiguration setPortNumbers(int... portNumbers) throws NullPointerException, IllegalArgumentException {
        Preconditions.checkNotNull("portNumbers", portNumbers);
        checkArgument("portNumbers", portNumbers.length > 0, "must not be empty");
        this.portNumbers = portNumbers;
        return this;
    }

    public DefaultOfficeManagerConfiguration setPipeName(String pipeName) throws NullPointerException {
        Preconditions.checkNotNull("pipeName", pipeName);
        this.pipeNames = new String[] { pipeName };
        return this;
    }

    public DefaultOfficeManagerConfiguration setPipeNames(String... pipeNames) throws NullPointerException, IllegalArgumentException {
        Preconditions.checkNotNull("pipeNames", pipeNames);
        checkArgument("pipeNames", pipeNames.length > 0, "must not be empty");
        this.pipeNames = pipeNames;
        return this;
    }

    public DefaultOfficeManagerConfiguration setRunAsArgs(String... runAsArgs) {
		this.runAsArgs = runAsArgs;
		return this;
	}

    public DefaultOfficeManagerConfiguration setTemplateProfileDir(File templateProfileDir) throws IllegalArgumentException {
        if (templateProfileDir != null) {
            checkArgument("templateProfileDir", templateProfileDir.isDirectory(), "must exist and be a directory");
        }
        this.templateProfileDir = templateProfileDir;
        return this;
    }

    public DefaultOfficeManagerConfiguration setTaskQueueTimeout(long taskQueueTimeout) {
        this.taskQueueTimeout = taskQueueTimeout;
        return this;
    }

    public DefaultOfficeManagerConfiguration setTaskExecutionTimeout(long taskExecutionTimeout) {
        this.taskExecutionTimeout = taskExecutionTimeout;
        return this;
    }

    public DefaultOfficeManagerConfiguration setMaxTasksPerProcess(int maxTasksPerProcess) {
        this.maxTasksPerProcess = maxTasksPerProcess;
        return this;
    }

   public OfficeManager buildOfficeManager() throws IllegalStateException {
        if (!officeHome.isDirectory()) {
            throw new IllegalStateException("officeHome doesn't exist or is not a directory: " + officeHome);
        } else if (!OfficeUtils.getOfficeExecutable(officeHome).isFile()) {
            throw new IllegalStateException("invalid officeHome: it doesn't contain soffice.bin: " + officeHome);
        }
        if (templateProfileDir != null && !isValidProfileDir(templateProfileDir)) {
            throw new IllegalStateException("invalid templateProfileDir: " + templateProfileDir);
        }
        
        int numInstances = connectionProtocol == OfficeConnectionProtocol.PIPE ? pipeNames.length : portNumbers.length;
        UnoUrl[] unoUrls = new UnoUrl[numInstances];
        for (int i = 0; i < numInstances; i++) {
            unoUrls[i] = (connectionProtocol == OfficeConnectionProtocol.PIPE) ? UnoUrl.pipe(pipeNames[i]) : UnoUrl.socket(portNumbers[i]);
        }
        return new ProcessPoolOfficeManager(officeHome, unoUrls, runAsArgs, templateProfileDir, taskQueueTimeout, taskExecutionTimeout, maxTasksPerProcess, new SigarProcessManager());
    }

        private void checkArgument(String argName, boolean condition, String message) throws IllegalArgumentException {
        if (!condition) {
            throw new IllegalArgumentException(argName + " " + message);
        }
    }

    private boolean isValidProfileDir(File profileDir) {
        File setupXcu = new File(profileDir, "user/registry/data/org/openoffice/Setup.xcu");
        return setupXcu.exists();
    }

}
