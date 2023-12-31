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
import org.unitime.timetable.model.Department;

public class DepartmentDAO extends _RootDAO<Department,Long> {
	private static DepartmentDAO sInstance;

	public DepartmentDAO() {}

	public static DepartmentDAO getInstance() {
		if (sInstance == null) sInstance = new DepartmentDAO();
		return sInstance;
	}

	public Class<Department> getReferenceClass() {
		return Department.class;
	}

	@SuppressWarnings("unchecked")
	public List<Department> findBySession(org.hibernate.Session hibSession, Long sessionId) {
		return hibSession.createQuery("from Department x where x.session.uniqueId = :sessionId", Department.class).setParameter("sessionId", sessionId).list();
	}

	@SuppressWarnings("unchecked")
	public List<Department> findByStatusType(org.hibernate.Session hibSession, Long statusTypeId) {
		return hibSession.createQuery("from Department x where x.statusType.uniqueId = :statusTypeId", Department.class).setParameter("statusTypeId", statusTypeId).list();
	}

	@SuppressWarnings("unchecked")
	public List<Department> findBySolverGroup(org.hibernate.Session hibSession, Long solverGroupId) {
		return hibSession.createQuery("from Department x where x.solverGroup.uniqueId = :solverGroupId", Department.class).setParameter("solverGroupId", solverGroupId).list();
	}
}
