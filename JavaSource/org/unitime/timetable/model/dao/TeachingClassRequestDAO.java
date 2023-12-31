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
import org.unitime.timetable.model.TeachingClassRequest;

public class TeachingClassRequestDAO extends _RootDAO<TeachingClassRequest,Long> {
	private static TeachingClassRequestDAO sInstance;

	public TeachingClassRequestDAO() {}

	public static TeachingClassRequestDAO getInstance() {
		if (sInstance == null) sInstance = new TeachingClassRequestDAO();
		return sInstance;
	}

	public Class<TeachingClassRequest> getReferenceClass() {
		return TeachingClassRequest.class;
	}

	@SuppressWarnings("unchecked")
	public List<TeachingClassRequest> findByTeachingRequest(org.hibernate.Session hibSession, Long teachingRequestId) {
		return hibSession.createQuery("from TeachingClassRequest x where x.teachingRequest.uniqueId = :teachingRequestId", TeachingClassRequest.class).setParameter("teachingRequestId", teachingRequestId).list();
	}

	@SuppressWarnings("unchecked")
	public List<TeachingClassRequest> findByTeachingClass(org.hibernate.Session hibSession, Long teachingClassId) {
		return hibSession.createQuery("from TeachingClassRequest x where x.teachingClass.uniqueId = :teachingClassId", TeachingClassRequest.class).setParameter("teachingClassId", teachingClassId).list();
	}
}
