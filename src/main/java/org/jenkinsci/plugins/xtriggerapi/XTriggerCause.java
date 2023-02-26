package org.jenkinsci.plugins.xtriggerapi;

import hudson.console.HyperlinkNote;
import hudson.model.Cause;
import hudson.model.Run;
import hudson.model.TaskListener;
import jenkins.model.Jenkins;
import jenkins.security.MasterToSlaveCallable;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Gregory Boissinot
 */
public class XTriggerCause extends Cause implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = -3551026356358107079L;

	private static Logger LOGGER = Logger.getLogger(XTriggerCause.class.getName());

    private String triggerName;

    private String causeFrom;

    private boolean logEnabled;

    protected XTriggerCause(String triggerName, String causeFrom) {
        this.triggerName = triggerName;
        this.causeFrom = causeFrom;
        this.logEnabled = false;
    }

    protected XTriggerCause(String triggerName, String causeFrom, boolean logEnabled) {
        this.triggerName = triggerName;
        this.causeFrom = causeFrom;
        this.logEnabled = logEnabled;
    }

    @Override
    public void onAddedTo(final Run build) {
        final XTriggerCauseAction causeAction = build.getAction(XTriggerCauseAction.class);
        if (causeAction != null) {
            try {
                Jenkins.get().getRootPath().act(new MasterToSlaveCallable<Void, XTriggerException>() {
                    @Override
                    public Void call() throws XTriggerException {
                        causeAction.setBuild(build);
                        File triggerLogFile = causeAction.getLogFile();
                        String logContent = causeAction.getLogMessage();
                        try {
                            FileUtils.writeStringToFile(triggerLogFile, logContent, Charset.defaultCharset());
                        } catch (IOException ioe) {
                            throw new XTriggerException(ioe);
                        }
                        return null;
                    }
                });
            } catch (IOException | InterruptedException | XTriggerException e) {
                LOGGER.log(Level.SEVERE, "Problem to attach cause object to build object.", e);
            }
        }
    }

    @Override
    public String getShortDescription() {
        if (causeFrom == null) {
            return "[" + triggerName + "]";
        } else if (!logEnabled) {
            return String.format("[%s] %s", triggerName, causeFrom);
        } else {
            return String.format("[%s] %s (%s)", triggerName, causeFrom, "<a href=\"triggerCauseAction\">log</a>");
        }
    }

    public void print(TaskListener listener) {
        if (causeFrom == null) {
            listener.getLogger().println("[" + triggerName + "]");
        } else {
            listener.getLogger().printf("[%s] %s (%s)%n", triggerName, causeFrom,
                    HyperlinkNote.encodeTo("triggerCauseAction", "log"));
        }
    }


    @SuppressWarnings("unused")
    public String getTriggerName() {
        return triggerName;
    }

}
