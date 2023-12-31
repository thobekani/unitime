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
import org.unitime.timetable.model.TeachingRequest;

public class TeachingRequestDAO extends _RootDAO<TeachingRequest,Long> {
	private static TeachingRequestDAO sInstance;

	public TeachingRequestDAO() {}

	public static TeachingRequestDAO getInstance() {
		if (sInstance == null) sInstance = new TeachingRequestDAO();
		return sInstance;
	}

	public Class<TeachingRequest> getReferenceClass() {
		return TeachingRequest.class;
	}

	@SuppressWarnings("unchecked")
	public List<TeachingRequest> findByOffering(org.hibernate.Session hibSession, Long offeringId) {
		return hibSession.createQuery("from TeachingRequest x where x.offering.uniqueId = :offeringId", TeachingRequest.class).setParameter("offeringId", offeringId).list();
	}

	@SuppressWarnings("unchecked")
	public List<TeachingRequest> findBySameCoursePreference(org.hibernate.Session hibSession, Long sameCoursePreferenceId) {
		return hibSession.createQuery("from TeachingRequest x where x.sameCoursePreference.uniqueId = :sameCoursePreferenceId", TeachingRequest.class).setParameter("sameCoursePreferenceId", sameCoursePreferenceId).list();
	}

	@SuppressWarnings("unchecked")
	public List<TeachingRequest> findBySameCommonPart(org.hibernate.Session hibSession, Long sameCommonPartId) {
		return hibSession.createQuery("from TeachingRequest x where x.sameCommonPart.uniqueId = :sameCommonPartId", TeachingRequest.class).setParameter("sameCommonPartId", sameCommonPartId).list();
	}

	@SuppressWarnings("unchecked")
	public List<TeachingRequest> findByResponsibility(org.hibernate.Session hibSession, Long responsibilityId) {
		return hibSession.createQuery("from TeachingRequest x where x.responsibility.uniqueId = :responsibilityId", TeachingRequest.class).setParameter("responsibilityId", responsibilityId).list();
	}
}
