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
package org.unitime.timetable.model;


import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;

import java.util.List;

import org.unitime.timetable.model.base.BaseHistory;
import org.unitime.timetable.model.dao.HistoryDAO;


/**
 * @author Tomas Muller
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "history")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name="subclass", discriminatorType = DiscriminatorType.STRING)
public class History extends BaseHistory {
	private static final long serialVersionUID = 1L;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public History () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public History (java.lang.Long uniqueId) {
		super(uniqueId);
	}

/*[CONSTRUCTOR MARKER END]*/

	/**
	 * Retrieves all history data for the academic session
	 * @param sessionId academic session
	 * @param aClass history class
	 * @return List of aClassHistory objects
	 */
	public static List<History> getHistoryList(Long sessionId, Class aClass) {
		return HistoryDAO.getInstance().getSession()
				.createQuery("from History where sessionId = :sessionId", History.class)
				.setParameter("sessionId", sessionId)
				.list();
	}
	

}
