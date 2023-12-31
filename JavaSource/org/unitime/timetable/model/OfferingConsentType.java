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


import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import java.util.List;

import org.unitime.timetable.model.base.BaseOfferingConsentType;
import org.unitime.timetable.model.dao.OfferingConsentTypeDAO;


/**
 * @author Tomas Muller
 */
@Entity
@Table(name = "offr_consent_type")
public class OfferingConsentType extends BaseOfferingConsentType {
	private static final long serialVersionUID = 1L;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public OfferingConsentType () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public OfferingConsentType (Long uniqueId) {
		super(uniqueId);
	}

/*[CONSTRUCTOR MARKER END]*/

    /** Request attribute name for available consent typess **/
    public static String CONSENT_TYPE_ATTR_NAME = "consentTypeList";  
    
	/**
	 * Retrieves all consent types in the database
	 * ordered by column label
	 * @return List of ConsentType objects
	 */
	@Transient
    public static List<OfferingConsentType> getConsentTypeList() {
		return OfferingConsentTypeDAO.getInstance().getSession().createQuery(
				"from OfferingConsentType order by label", OfferingConsentType.class)
				.setCacheable(true).list();
    }
    
	public static OfferingConsentType getOfferingConsentTypeForReference(String referenceString) {
		if (referenceString == null || referenceString.isEmpty()) return null;
		return OfferingConsentTypeDAO.getInstance().getSession().createQuery(
				"from OfferingConsentType where reference = :reference", OfferingConsentType.class)
				.setParameter("reference", referenceString).setMaxResults(1).setCacheable(true).uniqueResult();
	}
	
}
