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
package com.francetelecom.clara.cloud.paas.activation.v1.backend;

import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import com.francetelecom.clara.cloud.commons.tasks.PollTaskStateInterface;
import com.francetelecom.clara.cloud.commons.tasks.TaskStatus;
import com.francetelecom.clara.cloud.commons.tasks.TaskStatusEnum;

public class MyOtherBackend implements PollTaskStateInterface<TaskStatus> {

	private static final Logger log = LoggerFactory.getLogger(MyOtherBackend.class);

	public TaskStatus work(String input) {
		// return ticket id to check afterwards if stuff has been done
		log.debug("work(" + input + ") method is called on thread : "
				+ Thread.currentThread().getName());
		TaskStatus taskStatus = new TaskStatus(Integer.valueOf(input));
		taskStatus.setTaskStatus(TaskStatusEnum.STARTED);
		return taskStatus;
	}

	@Override
	@Transactional
	public TaskStatus giveCurrentTaskStatus(TaskStatus t) {
		log.debug("giveCurrentTaskStatus(" + t.getTaskId()
				+ ") method is called on thread : "
				+ Thread.currentThread().getName());
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		TaskStatus taskStatus = new TaskStatus(t.getTaskId());
		taskStatus
				.setTaskStatus(new Random().nextInt(2) == 0 ? TaskStatusEnum.FINISHED_OK
						: TaskStatusEnum.STARTED);
		return taskStatus;
	}

}
