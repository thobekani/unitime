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
import org.unitime.timetable.model.DepartmentalInstructor;

public class DepartmentalInstructorDAO extends _RootDAO<DepartmentalInstructor,Long> {
	private static DepartmentalInstructorDAO sInstance;

	public DepartmentalInstructorDAO() {}

	public static DepartmentalInstructorDAO getInstance() {
		if (sInstance == null) sInstance = new DepartmentalInstructorDAO();
		return sInstance;
	}

	public Class<DepartmentalInstructor> getReferenceClass() {
		return DepartmentalInstructor.class;
	}

	@SuppressWarnings("unchecked")
	public List<DepartmentalInstructor> findByPositionType(org.hibernate.Session hibSession, Long positionTypeId) {
		return hibSession.createQuery("from DepartmentalInstructor x where x.positionType.uniqueId = :positionTypeId", DepartmentalInstructor.class).setParameter("positionTypeId", positionTypeId).list();
	}

	@SuppressWarnings("unchecked")
	public List<DepartmentalInstructor> findByDepartment(org.hibernate.Session hibSession, Long departmentId) {
		return hibSession.createQuery("from DepartmentalInstructor x where x.department.uniqueId = :departmentId", DepartmentalInstructor.class).setParameter("departmentId", departmentId).list();
	}

	@SuppressWarnings("unchecked")
	public List<DepartmentalInstructor> findByRole(org.hibernate.Session hibSession, Long roleId) {
		return hibSession.createQuery("from DepartmentalInstructor x where x.role.roleId = :roleId", DepartmentalInstructor.class).setParameter("roleId", roleId).list();
	}

	@SuppressWarnings("unchecked")
	public List<DepartmentalInstructor> findByTeachingPreference(org.hibernate.Session hibSession, Long teachingPreferenceId) {
		return hibSession.createQuery("from DepartmentalInstructor x where x.teachingPreference.uniqueId = :teachingPreferenceId", DepartmentalInstructor.class).setParameter("teachingPreferenceId", teachingPreferenceId).list();
	}
}
