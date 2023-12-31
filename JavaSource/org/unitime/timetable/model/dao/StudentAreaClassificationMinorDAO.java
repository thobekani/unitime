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
import org.unitime.timetable.model.StudentAreaClassificationMinor;

public class StudentAreaClassificationMinorDAO extends _RootDAO<StudentAreaClassificationMinor,Long> {
	private static StudentAreaClassificationMinorDAO sInstance;

	public StudentAreaClassificationMinorDAO() {}

	public static StudentAreaClassificationMinorDAO getInstance() {
		if (sInstance == null) sInstance = new StudentAreaClassificationMinorDAO();
		return sInstance;
	}

	public Class<StudentAreaClassificationMinor> getReferenceClass() {
		return StudentAreaClassificationMinor.class;
	}

	@SuppressWarnings("unchecked")
	public List<StudentAreaClassificationMinor> findByStudent(org.hibernate.Session hibSession, Long studentId) {
		return hibSession.createQuery("from StudentAreaClassificationMinor x where x.student.uniqueId = :studentId", StudentAreaClassificationMinor.class).setParameter("studentId", studentId).list();
	}

	@SuppressWarnings("unchecked")
	public List<StudentAreaClassificationMinor> findByAcademicArea(org.hibernate.Session hibSession, Long academicAreaId) {
		return hibSession.createQuery("from StudentAreaClassificationMinor x where x.academicArea.uniqueId = :academicAreaId", StudentAreaClassificationMinor.class).setParameter("academicAreaId", academicAreaId).list();
	}

	@SuppressWarnings("unchecked")
	public List<StudentAreaClassificationMinor> findByAcademicClassification(org.hibernate.Session hibSession, Long academicClassificationId) {
		return hibSession.createQuery("from StudentAreaClassificationMinor x where x.academicClassification.uniqueId = :academicClassificationId", StudentAreaClassificationMinor.class).setParameter("academicClassificationId", academicClassificationId).list();
	}

	@SuppressWarnings("unchecked")
	public List<StudentAreaClassificationMinor> findByMinor(org.hibernate.Session hibSession, Long minorId) {
		return hibSession.createQuery("from StudentAreaClassificationMinor x where x.minor.uniqueId = :minorId", StudentAreaClassificationMinor.class).setParameter("minorId", minorId).list();
	}
}
