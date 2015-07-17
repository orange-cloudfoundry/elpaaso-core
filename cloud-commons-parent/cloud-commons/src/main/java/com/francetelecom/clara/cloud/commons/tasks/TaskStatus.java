/**
 * Copyright (C) 2015 Orange
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.francetelecom.clara.cloud.commons.tasks;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common cloud project Task component. A task is a long running activity,
 * internal to cloud projet and / or relating to external long running activity
 */
public class TaskStatus implements Serializable {

	private static final long serialVersionUID = -6895838149206097622L;

	private static Logger logger = LoggerFactory.getLogger(TaskStatus.class.getName());

	// TODO : use a generics to keep orig taskStatus ?
	private final long taskId;

	private TaskStatusEnum taskStatus = TaskStatusEnum.TRANSIENT;

	private final List<TaskStatus> subtasks = new ArrayList<TaskStatus>();

	private static final NumberFormat formatter = new DecimalFormat("000");

	private int maxPercent = 100;

	/** Start time of the task, -1 if not set (not started) */
	private long startTime = -1;

	/** End time of the task, -1 if not set (not ended) */
	private long endTime = -1;

	/** Suggested timeout in seconds. If 0 then no timeout is suggested. */
	private long suggestedTimeout = 0L;

	/** Indicates the last update date of this status */
	private long lastUpdate = -1;

	/** Title the task : should be set once, at the beginning of the task */
	private String title = "";

	/** Subtitle the task : describe what is currently done */
	private String subtitle = "";

	/**
	 * Indicates the percentage of the task: -1 mean that it is not known,
	 * otherwise it a value between 0 and 100
	 */
	private int percent = -1;

	/**
	 * Error message if any
	 */
	private String errorMessage = "";

	/**
	 * Empty task status constructor, for new TaskStatus
	 */
	public TaskStatus() {
		this(-1);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (endTime ^ (endTime >>> 32));
		result = prime * result + ((errorMessage == null) ? 0 : errorMessage.hashCode());
		result = prime * result + (int) (lastUpdate ^ (lastUpdate >>> 32));
		result = prime * result + maxPercent;
		result = prime * result + percent;
		result = prime * result + (int) (startTime ^ (startTime >>> 32));
		result = prime * result + ((subtitle == null) ? 0 : subtitle.hashCode());
		result = prime * result + (int) (suggestedTimeout ^ (suggestedTimeout >>> 32));
		result = prime * result + (int) (taskId ^ (taskId >>> 32));
		result = prime * result + ((taskStatus == null) ? 0 : taskStatus.hashCode());
		result = prime * result + ((title == null) ? 0 : title.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TaskStatus other = (TaskStatus) obj;
		if (endTime != other.endTime)
			return false;
		if (errorMessage == null) {
			if (other.errorMessage != null)
				return false;
		} else if (!errorMessage.equals(other.errorMessage))
			return false;
		if (lastUpdate != other.lastUpdate)
			return false;
		if (maxPercent != other.maxPercent)
			return false;
		if (percent != other.percent)
			return false;
		if (startTime != other.startTime)
			return false;
		if (subtitle == null) {
			if (other.subtitle != null)
				return false;
		} else if (!subtitle.equals(other.subtitle))
			return false;
		if (suggestedTimeout != other.suggestedTimeout)
			return false;
		if (taskId != other.taskId)
			return false;
		if (taskStatus != other.taskStatus)
			return false;
		if (title == null) {
			if (other.title != null)
				return false;
		} else if (!title.equals(other.title))
			return false;
		return true;
	}

	/**
	 * Basic task status constructor
	 * 
	 * @param taskId
	 *            ID of the task status
	 */
	public TaskStatus(long taskId) {
		this.taskId = taskId;
		this.startTime = System.currentTimeMillis();
		this.lastUpdate = System.currentTimeMillis();
	}

	/**
	 * Copy constructor
	 * 
	 * @param status
	 *            Status to copy
	 */
	public TaskStatus(TaskStatus status) {
		this.taskId = status.taskId;
		this.startTime = status.startTime;
		this.endTime = status.endTime;
		this.taskStatus = status.taskStatus;
		this.title = status.title;
		this.subtitle = status.subtitle;
		this.percent = status.percent;
		this.errorMessage = status.errorMessage;
		this.lastUpdate = System.currentTimeMillis();
		this.maxPercent = status.maxPercent;
		this.suggestedTimeout = status.suggestedTimeout;
		for (TaskStatus subtask : status.subtasks) {
			this.addSubtask(new TaskStatus(subtask));
		}
	}

    /**
     * Enables copy constructor to instanciate subclasses of TaskStatus as subtasks
     */
    public interface SubTaskFactory {
        TaskStatus createSubTask(TaskStatus subtask);
    }

	/**
	 * Copy constructor for subclassed substasks
	 *
	 * @param status
	 *            Status to copy
	 */
	public TaskStatus(TaskStatus status, SubTaskFactory subTaskFactory) {
		this.taskId = status.taskId;
		this.startTime = status.startTime;
		this.endTime = status.endTime;
		this.taskStatus = status.taskStatus;
		this.title = status.title;
		this.subtitle = status.subtitle;
		this.percent = status.percent;
		this.errorMessage = status.errorMessage;
		this.lastUpdate = System.currentTimeMillis();
		this.maxPercent = status.maxPercent;
		this.suggestedTimeout = status.suggestedTimeout;
		for (TaskStatus subtask : status.subtasks) {
			this.addSubtask(subTaskFactory.createSubTask(subtask));
		}
	}

	/**
	 * Update status depending on sub tasks
	 */
	public void update() {
		if (subtasks.size() > 0) {
			// If there are subtask, then update fields depending on them
			int totalPercent = 0;
			int totalMaxPercent = 0;
			long endTime = 0;
			boolean allFinishedOk = true, oneStarted = false, oneFailed = false;
			for (TaskStatus subtask : subtasks) {
				totalMaxPercent += subtask.getMaxPercent();
				if (subtask.getPercent() > 0) {
					totalPercent += (subtask.getPercent() * subtask.getMaxPercent()) / 100;
				}
				if (subtask.getEndTime() > getEndTime()) {
					endTime = subtask.getEndTime();
				}
				allFinishedOk = allFinishedOk && subtask.getTaskStatus() == TaskStatusEnum.FINISHED_OK;
				if (subtask.getTaskStatus() != TaskStatusEnum.TRANSIENT) {
					oneStarted = true;
				}
				if (subtask.getTaskStatus() == TaskStatusEnum.STARTED) {
					setSubtitle(subtask.getSubtitle());
				} else if (subtask.getTaskStatus() == TaskStatusEnum.FINISHED_FAILED) {
					oneFailed = true;
					setTaskStatus(TaskStatusEnum.FINISHED_FAILED);
					setErrorMessage(subtask.getErrorMessage());
					break;
				}
			}
			if (getTaskStatus() != TaskStatusEnum.FINISHED_FAILED) {
				setPercent((totalPercent * 100) / totalMaxPercent);
			}
			if (getTaskStatus() != TaskStatusEnum.FINISHED_FAILED && allFinishedOk) {
				setTaskStatus(TaskStatusEnum.FINISHED_OK);
				setPercent(100);
				setSubtitle("");
				setEndTime(endTime);
			} else if (getTaskStatus() != TaskStatusEnum.FINISHED_FAILED && oneStarted && !oneFailed) {
				setTaskStatus(TaskStatusEnum.STARTED);
			}
		}
	}

	public static void displayTaskStatus(TaskStatus status, int offset) {

		StringBuffer buf = new StringBuffer(offset);
		for (int i = 0; i < offset; i++) {
			buf.append(" ");
		}
		if (status.getPercent() < 0) {
			buf.append(" -  ]");
		} else {
			buf.append(formatter.format(status.getPercent()));
			buf.append("%]");
		}
		switch (status.getTaskStatus()) {
		case FINISHED_FAILED:
			buf.append(" (*KO) ");
			break;
		case FINISHED_OK:
			buf.append(" (+OK) ");
			break;
		case STARTED:
			buf.append(" (RUN) ");
			break;
		case TRANSIENT:
			buf.append(" (---) ");
			break;
		}
		buf.append(status.getTitle());
		if (status.getSubtitle() != null && status.getSubtitle().length() > 0) {
			buf.append(" : ");
			buf.append(status.getSubtitle());
		}
		if (status.getTaskStatus() == TaskStatusEnum.FINISHED_FAILED) {
			buf.append(" ** ");
			buf.append(status.getErrorMessage());
		}
		logger.info(buf.toString());
		for (TaskStatus subtask : status.listSubtasks()) {
			displayTaskStatus(subtask, offset + 3);
		}
	}

	public TaskStatusEnum getTaskStatus() {
		return taskStatus;
	}

	public void setTaskStatus(TaskStatusEnum taskStatus) {
		this.taskStatus = taskStatus;
		this.lastUpdate = System.currentTimeMillis();
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
		this.lastUpdate = System.currentTimeMillis();
	}

	public long getEndTime() {
		return endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
		this.lastUpdate = System.currentTimeMillis();
	}

	public long getSuggestedTimeout() {
		return suggestedTimeout;
	}

	public void setSuggestedTimeout(long suggestedTimeout) {
		this.suggestedTimeout = suggestedTimeout;
		this.lastUpdate = System.currentTimeMillis();
	}

	public long getTaskId() {
		return taskId;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
		this.lastUpdate = System.currentTimeMillis();
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	/**
	 * Returns complete if finished OK or failed
	 * 
	 * @return true if finished OK or failed
	 */
	public boolean isComplete() {
		return (hasFailed() || hasSucceed());
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
		this.lastUpdate = System.currentTimeMillis();
	}

	public String getSubtitle() {
		return subtitle;
	}

	public void setSubtitle(String subtitle) {
		this.subtitle = subtitle;
		this.lastUpdate = System.currentTimeMillis();
	}

	public int getPercent() {
		return percent;
	}

	public void setPercent(int percent) {
		this.percent = percent;
		this.lastUpdate = System.currentTimeMillis();
	}

	public long getLastUpdate() {
		return lastUpdate;
	}

	public void addSubtask(TaskStatus status) {
		subtasks.add(status);
		update();
	}

	public int getMaxPercent() {
		return maxPercent;
	}

	public void setMaxPercent(int maxPercent) {
		this.maxPercent = maxPercent;
	}

	public List<TaskStatus> listSubtasks() {
		return Collections.unmodifiableList(subtasks);
	}

	@Override
	public String toString() {
		return "TaskStatus [taskId=" + taskId + ", taskStatus=" + taskStatus + ", startTime=" + startTime + ", endTime=" + endTime + ", lastUpdate="
				+ lastUpdate + ", title=" + title + ", subtitle=" + subtitle + ", percent=" + percent + ", errorMessage=" + errorMessage + "]";
	}

    //~ status change helpers
    /**
     * help to change a task status as 'STARTED'
     * change the title, startTime (to NOW), an status
     * @param newTitle the new title to set
     */
    public void setAsStarted(String newTitle) {
        setTitle(newTitle);
        setStartTime(System.currentTimeMillis());
        setTaskStatus(TaskStatusEnum.STARTED);
    }
    /**
     * help to change a task status as 'FINISHED_OK'
     * endTime (to NOW), an status
     */
    public void setAsFinishedOk() {
        setPercent(100);
        setEndTime(System.currentTimeMillis());
        setTaskStatus(TaskStatusEnum.FINISHED_OK);
    }
    /**
     * help to change a task status as 'FINISHED_FAILED'
     * setErrorMessage, endTime (to NOW), an status
     * @param errorMessage error message to set on the taskStatus
     */
    public void setAsFinishedFailed(String errorMessage) {
        setEndTime(System.currentTimeMillis());
        setTaskStatus(TaskStatusEnum.FINISHED_FAILED);
        setErrorMessage(errorMessage);
    }

    public void progress(int percent, String subtitle) {
        setPercent(percent);
        setSubtitle(subtitle);
        logger.debug(subtitle);
    }

	/**
	 * @return true if task has succeed
	 */
	public boolean hasSucceed() {
		return TaskStatusEnum.FINISHED_OK.equals(taskStatus);
	}
	
	/**
	 * @return true if task has failed
	 */
	public boolean hasFailed() {
		return TaskStatusEnum.FINISHED_FAILED.equals(taskStatus);
	}

	/**
	 * @return true if task has started
	 */
	public boolean isStarted() {
		return TaskStatusEnum.STARTED.equals(taskStatus);
	}
}
