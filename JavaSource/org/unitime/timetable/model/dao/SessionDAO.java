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
import org.unitime.timetable.model.Session;

public class SessionDAO extends _RootDAO<Session,Long> {
	private static SessionDAO sInstance;

	public SessionDAO() {}

	public static SessionDAO getInstance() {
		if (sInstance == null) sInstance = new SessionDAO();
		return sInstance;
	}

	public Class<Session> getReferenceClass() {
		return Session.class;
	}

	@SuppressWarnings("unchecked")
	public List<Session> findByStatusType(org.hibernate.Session hibSession, Long statusTypeId) {
		return hibSession.createQuery("from Session x where x.statusType.uniqueId = :statusTypeId", Session.class).setParameter("statusTypeId", statusTypeId).list();
	}

	@SuppressWarnings("unchecked")
	public List<Session> findByDefaultDatePattern(org.hibernate.Session hibSession, Long defaultDatePatternId) {
		return hibSession.createQuery("from Session x where x.defaultDatePattern.uniqueId = :defaultDatePatternId", Session.class).setParameter("defaultDatePatternId", defaultDatePatternId).list();
	}

	@SuppressWarnings("unchecked")
	public List<Session> findByDefaultSectioningStatus(org.hibernate.Session hibSession, Long defaultSectioningStatusId) {
		return hibSession.createQuery("from Session x where x.defaultSectioningStatus.uniqueId = :defaultSectioningStatusId", Session.class).setParameter("defaultSectioningStatusId", defaultSectioningStatusId).list();
	}

	@SuppressWarnings("unchecked")
	public List<Session> findByDefaultClassDurationType(org.hibernate.Session hibSession, Long defaultClassDurationTypeId) {
		return hibSession.createQuery("from Session x where x.defaultClassDurationType.uniqueId = :defaultClassDurationTypeId", Session.class).setParameter("defaultClassDurationTypeId", defaultClassDurationTypeId).list();
	}

	@SuppressWarnings("unchecked")
	public List<Session> findByDefaultInstructionalMethod(org.hibernate.Session hibSession, Long defaultInstructionalMethodId) {
		return hibSession.createQuery("from Session x where x.defaultInstructionalMethod.uniqueId = :defaultInstructionalMethodId", Session.class).setParameter("defaultInstructionalMethodId", defaultInstructionalMethodId).list();
	}
}
