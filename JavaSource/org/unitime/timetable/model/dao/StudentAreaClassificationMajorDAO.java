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
import org.unitime.timetable.model.StudentAreaClassificationMajor;

public class StudentAreaClassificationMajorDAO extends _RootDAO<StudentAreaClassificationMajor,Long> {
	private static StudentAreaClassificationMajorDAO sInstance;

	public StudentAreaClassificationMajorDAO() {}

	public static StudentAreaClassificationMajorDAO getInstance() {
		if (sInstance == null) sInstance = new StudentAreaClassificationMajorDAO();
		return sInstance;
	}

	public Class<StudentAreaClassificationMajor> getReferenceClass() {
		return StudentAreaClassificationMajor.class;
	}

	@SuppressWarnings("unchecked")
	public List<StudentAreaClassificationMajor> findByStudent(org.hibernate.Session hibSession, Long studentId) {
		return hibSession.createQuery("from StudentAreaClassificationMajor x where x.student.uniqueId = :studentId", StudentAreaClassificationMajor.class).setParameter("studentId", studentId).list();
	}

	@SuppressWarnings("unchecked")
	public List<StudentAreaClassificationMajor> findByAcademicArea(org.hibernate.Session hibSession, Long academicAreaId) {
		return hibSession.createQuery("from StudentAreaClassificationMajor x where x.academicArea.uniqueId = :academicAreaId", StudentAreaClassificationMajor.class).setParameter("academicAreaId", academicAreaId).list();
	}

	@SuppressWarnings("unchecked")
	public List<StudentAreaClassificationMajor> findByAcademicClassification(org.hibernate.Session hibSession, Long academicClassificationId) {
		return hibSession.createQuery("from StudentAreaClassificationMajor x where x.academicClassification.uniqueId = :academicClassificationId", StudentAreaClassificationMajor.class).setParameter("academicClassificationId", academicClassificationId).list();
	}

	@SuppressWarnings("unchecked")
	public List<StudentAreaClassificationMajor> findByMajor(org.hibernate.Session hibSession, Long majorId) {
		return hibSession.createQuery("from StudentAreaClassificationMajor x where x.major.uniqueId = :majorId", StudentAreaClassificationMajor.class).setParameter("majorId", majorId).list();
	}

	@SuppressWarnings("unchecked")
	public List<StudentAreaClassificationMajor> findByConcentration(org.hibernate.Session hibSession, Long concentrationId) {
		return hibSession.createQuery("from StudentAreaClassificationMajor x where x.concentration.uniqueId = :concentrationId", StudentAreaClassificationMajor.class).setParameter("concentrationId", concentrationId).list();
	}

	@SuppressWarnings("unchecked")
	public List<StudentAreaClassificationMajor> findByDegree(org.hibernate.Session hibSession, Long degreeId) {
		return hibSession.createQuery("from StudentAreaClassificationMajor x where x.degree.uniqueId = :degreeId", StudentAreaClassificationMajor.class).setParameter("degreeId", degreeId).list();
	}

	@SuppressWarnings("unchecked")
	public List<StudentAreaClassificationMajor> findByProgram(org.hibernate.Session hibSession, Long programId) {
		return hibSession.createQuery("from StudentAreaClassificationMajor x where x.program.uniqueId = :programId", StudentAreaClassificationMajor.class).setParameter("programId", programId).list();
	}

	@SuppressWarnings("unchecked")
	public List<StudentAreaClassificationMajor> findByCampus(org.hibernate.Session hibSession, Long campusId) {
		return hibSession.createQuery("from StudentAreaClassificationMajor x where x.campus.uniqueId = :campusId", StudentAreaClassificationMajor.class).setParameter("campusId", campusId).list();
	}
}
