/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
*/
package org.unitime.timetable.model.dao;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
import java.util.List;
import org.unitime.timetable.model.TaskExecution;

public class TaskExecutionDAO extends _RootDAO<TaskExecution,Long> {
	private static TaskExecutionDAO sInstance;

	public TaskExecutionDAO() {}

	public static TaskExecutionDAO getInstance() {
		if (sInstance == null) sInstance = new TaskExecutionDAO();
		return sInstance;
	}

	public Class<TaskExecution> getReferenceClass() {
		return TaskExecution.class;
	}

	@SuppressWarnings("unchecked")
	public List<TaskExecution> findByTask(org.hibernate.Session hibSession, Long taskId) {
		return hibSession.createQuery("from TaskExecution x where x.task.uniqueId = :taskId", TaskExecution.class).setParameter("taskId", taskId).list();
	}
}
