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
import org.unitime.timetable.model.InstrOfferingConfig;

public class InstrOfferingConfigDAO extends _RootDAO<InstrOfferingConfig,Long> {
	private static InstrOfferingConfigDAO sInstance;

	public InstrOfferingConfigDAO() {}

	public static InstrOfferingConfigDAO getInstance() {
		if (sInstance == null) sInstance = new InstrOfferingConfigDAO();
		return sInstance;
	}

	public Class<InstrOfferingConfig> getReferenceClass() {
		return InstrOfferingConfig.class;
	}

	@SuppressWarnings("unchecked")
	public List<InstrOfferingConfig> findByInstructionalOffering(org.hibernate.Session hibSession, Long instructionalOfferingId) {
		return hibSession.createQuery("from InstrOfferingConfig x where x.instructionalOffering.uniqueId = :instructionalOfferingId", InstrOfferingConfig.class).setParameter("instructionalOfferingId", instructionalOfferingId).list();
	}

	@SuppressWarnings("unchecked")
	public List<InstrOfferingConfig> findByClassDurationType(org.hibernate.Session hibSession, Long classDurationTypeId) {
		return hibSession.createQuery("from InstrOfferingConfig x where x.classDurationType.uniqueId = :classDurationTypeId", InstrOfferingConfig.class).setParameter("classDurationTypeId", classDurationTypeId).list();
	}

	@SuppressWarnings("unchecked")
	public List<InstrOfferingConfig> findByInstructionalMethod(org.hibernate.Session hibSession, Long instructionalMethodId) {
		return hibSession.createQuery("from InstrOfferingConfig x where x.instructionalMethod.uniqueId = :instructionalMethodId", InstrOfferingConfig.class).setParameter("instructionalMethodId", instructionalMethodId).list();
	}
}
