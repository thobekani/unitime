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
import org.unitime.timetable.model.ExternalRoomFeature;

public class ExternalRoomFeatureDAO extends _RootDAO<ExternalRoomFeature,Long> {
	private static ExternalRoomFeatureDAO sInstance;

	public ExternalRoomFeatureDAO() {}

	public static ExternalRoomFeatureDAO getInstance() {
		if (sInstance == null) sInstance = new ExternalRoomFeatureDAO();
		return sInstance;
	}

	public Class<ExternalRoomFeature> getReferenceClass() {
		return ExternalRoomFeature.class;
	}

	@SuppressWarnings("unchecked")
	public List<ExternalRoomFeature> findByRoom(org.hibernate.Session hibSession, Long roomId) {
		return hibSession.createQuery("from ExternalRoomFeature x where x.room.uniqueId = :roomId", ExternalRoomFeature.class).setParameter("roomId", roomId).list();
	}
}
