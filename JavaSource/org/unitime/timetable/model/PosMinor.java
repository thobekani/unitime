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

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import org.unitime.timetable.model.base.BasePosMinor;
import org.unitime.timetable.model.dao.PosMinorDAO;

/**
 * @author Tomas Muller
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "pos_minor")
public class PosMinor extends BasePosMinor {
	private static final long serialVersionUID = 1L;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public PosMinor () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public PosMinor (java.lang.Long uniqueId) {
		super(uniqueId);
	}

/*[CONSTRUCTOR MARKER END]*/

    public static PosMinor findByCode(Long sessionId, String code) {
        return PosMinorDAO.getInstance().
        getSession().
        createQuery(
                "select a from PosMinor a where "+
                "a.session.uniqueId=:sessionId and "+
                "a.code=:code", PosMinor.class).
         setParameter("sessionId", sessionId.longValue()).
         setParameter("code", code).
         setCacheable(true).
         uniqueResult(); 
    }
    
    public static PosMinor findByCodeAcadAreaId(Long sessionId, String code, Long areaId) {
        if (areaId==null) return findByCode(sessionId, code);
        return PosMinorDAO.getInstance().
        getSession().
        createQuery(
                "select p from PosMinor p inner join p.academicAreas a where "+
                "p.session.uniqueId=:sessionId and "+
                "a.uniqueId=:areaId and p.code=:code", PosMinor.class).
         setParameter("sessionId", sessionId.longValue()).
         setParameter("areaId", areaId.longValue()).
         setParameter("code", code).
         setCacheable(true).
         uniqueResult(); 
    }

    public static PosMinor findByCodeAcadAreaAbbv(Long sessionId, String code, String areaAbbv) {
        if (areaAbbv==null || areaAbbv.trim().length()==0) return findByCode(sessionId, code);
        return PosMinorDAO.getInstance().
        getSession().
        createQuery(
                "select p from PosMinor p inner join p.academicAreas a where "+
                "p.session.uniqueId=:sessionId and "+
                "a.academicAreaAbbreviation=:areaAbbv and p.code=:code", PosMinor.class).
         setParameter("sessionId", sessionId.longValue()).
         setParameter("areaAbbv", areaAbbv).
         setParameter("code", code).
         setCacheable(true).
         uniqueResult(); 
    }
    
    public Object clone() {
    	PosMinor m = new PosMinor();
    	m.setExternalUniqueId(getExternalUniqueId());
    	m.setCode(getCode());
    	m.setName(getName());
    	return m;
    }

}
