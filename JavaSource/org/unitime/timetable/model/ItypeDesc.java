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

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;


import java.util.TreeSet;

import org.hibernate.Session;
import org.unitime.timetable.model.base.BaseItypeDesc;
import org.unitime.timetable.model.dao.ItypeDescDAO;


/**
 * @author Tomas Muller
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "itype_desc")
public class ItypeDesc extends BaseItypeDesc implements Comparable<Object> {
	private static final long serialVersionUID = 1L;

    public static String[] sBasicTypes = new String[] {"Extended","Basic"}; 

/*[CONSTRUCTOR MARKER BEGIN]*/
	public ItypeDesc () {
		super();
	}

/*[CONSTRUCTOR MARKER END]*/

    /** Request attribute name for available itypes **/
    public static String ITYPE_ATTR_NAME = "itypesList";
    
    /**
     * @return Returns the itypes.
     */
    @SuppressWarnings("unchecked")
	public static TreeSet<ItypeDesc> findAll(boolean basic) {
        return new TreeSet<ItypeDesc>(
                ItypeDescDAO.getInstance().
                getSession().
                createQuery("select i from ItypeDesc i"+(basic?" where i.basic = true":""), ItypeDesc.class).
                setCacheable(true).
                list());
    }

	@Transient
    public String getBasicType() {
    	return sBasicTypes[getBasic() ? 1 : 0];
    }
    
    public int compareTo(Object o) {
        if (o==null || !(o instanceof ItypeDesc)) return -1;
        return getItype().compareTo(((ItypeDesc)o).getItype());
    }
    
    public static ItypeDesc findForReference(String reference, Session hibSession) {
    	return((ItypeDesc) hibSession
    			.createQuery("from ItypeDesc i where i.sis_ref = :ref", ItypeDesc.class)
    			.setParameter("ref", reference)
    			.setCacheable(true)
    			.uniqueResult());
    }
    
}
