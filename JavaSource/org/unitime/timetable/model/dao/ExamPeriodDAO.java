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
import org.unitime.timetable.model.ExamPeriod;

public class ExamPeriodDAO extends _RootDAO<ExamPeriod,Long> {
	private static ExamPeriodDAO sInstance;

	public ExamPeriodDAO() {}

	public static ExamPeriodDAO getInstance() {
		if (sInstance == null) sInstance = new ExamPeriodDAO();
		return sInstance;
	}

	public Class<ExamPeriod> getReferenceClass() {
		return ExamPeriod.class;
	}

	@SuppressWarnings("unchecked")
	public List<ExamPeriod> findBySession(org.hibernate.Session hibSession, Long sessionId) {
		return hibSession.createQuery("from ExamPeriod x where x.session.uniqueId = :sessionId", ExamPeriod.class).setParameter("sessionId", sessionId).list();
	}

	@SuppressWarnings("unchecked")
	public List<ExamPeriod> findByExamType(org.hibernate.Session hibSession, Long examTypeId) {
		return hibSession.createQuery("from ExamPeriod x where x.examType.uniqueId = :examTypeId", ExamPeriod.class).setParameter("examTypeId", examTypeId).list();
	}

	@SuppressWarnings("unchecked")
	public List<ExamPeriod> findByPrefLevel(org.hibernate.Session hibSession, Long prefLevelId) {
		return hibSession.createQuery("from ExamPeriod x where x.prefLevel.uniqueId = :prefLevelId", ExamPeriod.class).setParameter("prefLevelId", prefLevelId).list();
	}
}
