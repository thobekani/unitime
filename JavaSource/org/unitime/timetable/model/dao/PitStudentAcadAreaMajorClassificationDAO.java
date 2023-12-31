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
import org.unitime.timetable.model.PitStudentAcadAreaMajorClassification;

public class PitStudentAcadAreaMajorClassificationDAO extends _RootDAO<PitStudentAcadAreaMajorClassification,Long> {
	private static PitStudentAcadAreaMajorClassificationDAO sInstance;

	public PitStudentAcadAreaMajorClassificationDAO() {}

	public static PitStudentAcadAreaMajorClassificationDAO getInstance() {
		if (sInstance == null) sInstance = new PitStudentAcadAreaMajorClassificationDAO();
		return sInstance;
	}

	public Class<PitStudentAcadAreaMajorClassification> getReferenceClass() {
		return PitStudentAcadAreaMajorClassification.class;
	}

	@SuppressWarnings("unchecked")
	public List<PitStudentAcadAreaMajorClassification> findByPitStudent(org.hibernate.Session hibSession, Long pitStudentId) {
		return hibSession.createQuery("from PitStudentAcadAreaMajorClassification x where x.pitStudent.uniqueId = :pitStudentId", PitStudentAcadAreaMajorClassification.class).setParameter("pitStudentId", pitStudentId).list();
	}

	@SuppressWarnings("unchecked")
	public List<PitStudentAcadAreaMajorClassification> findByAcademicArea(org.hibernate.Session hibSession, Long academicAreaId) {
		return hibSession.createQuery("from PitStudentAcadAreaMajorClassification x where x.academicArea.uniqueId = :academicAreaId", PitStudentAcadAreaMajorClassification.class).setParameter("academicAreaId", academicAreaId).list();
	}

	@SuppressWarnings("unchecked")
	public List<PitStudentAcadAreaMajorClassification> findByAcademicClassification(org.hibernate.Session hibSession, Long academicClassificationId) {
		return hibSession.createQuery("from PitStudentAcadAreaMajorClassification x where x.academicClassification.uniqueId = :academicClassificationId", PitStudentAcadAreaMajorClassification.class).setParameter("academicClassificationId", academicClassificationId).list();
	}

	@SuppressWarnings("unchecked")
	public List<PitStudentAcadAreaMajorClassification> findByMajor(org.hibernate.Session hibSession, Long majorId) {
		return hibSession.createQuery("from PitStudentAcadAreaMajorClassification x where x.major.uniqueId = :majorId", PitStudentAcadAreaMajorClassification.class).setParameter("majorId", majorId).list();
	}

	@SuppressWarnings("unchecked")
	public List<PitStudentAcadAreaMajorClassification> findByConcentration(org.hibernate.Session hibSession, Long concentrationId) {
		return hibSession.createQuery("from PitStudentAcadAreaMajorClassification x where x.concentration.uniqueId = :concentrationId", PitStudentAcadAreaMajorClassification.class).setParameter("concentrationId", concentrationId).list();
	}
}
