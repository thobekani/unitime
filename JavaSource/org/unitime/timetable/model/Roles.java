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


import java.util.Set;
import java.util.TreeSet;

import org.unitime.timetable.model.base.BaseRoles;
import org.unitime.timetable.model.dao.RolesDAO;
import org.unitime.timetable.security.rights.HasRights;
import org.unitime.timetable.security.rights.Right;


/**
 * @author Tomas Muller
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "roles")
public class Roles extends BaseRoles implements HasRights, Comparable<Roles> {

/**
	 *
	 */
	private static final long serialVersionUID = 3256722879445154100L;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public Roles () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public Roles (java.lang.Long roleId) {
		super(roleId);
	}

/*[CONSTRUCTOR MARKER END]*/

	public static final String ROLE_STUDENT = "Student";
	public static final String ROLE_INSTRUCTOR = "Instructor";
	public static final String ROLE_NONE = "No Role";
	public static final String ROLE_ANONYMOUS = "Anonymous";
	
    public static String USER_ROLES_ATTR_NAME = "userRoles";
    public static String ROLES_ATTR_NAME = "rolesList";
    
    public static Roles getRole(String roleRef, org.hibernate.Session hibSession) {
    	return hibSession.createQuery(
    			"from Roles where reference = :reference", Roles.class)
    			.setParameter("reference", roleRef).setCacheable(true).uniqueResult();
    }

    @Override
	public boolean hasRight(Right right) {
    	return getRights().contains(right.name());
    }
    
	@Transient
    public Long getUniqueId() { return getRoleId(); }
    
	@Transient
    public boolean isUsed() {
    	return (RolesDAO.getInstance().getSession().createQuery(
    			"select count(m) from ManagerRole m where m.role.roleId = :roleId", Number.class)
    			.setParameter("roleId", getRoleId()).uniqueResult()).intValue() > 0;
    }
    
    public static Set<Roles> findAll(boolean managerOnly) {
    	return findAll(managerOnly, RolesDAO.getInstance().getSession());
    }
    
    public static Set<Roles> findAll(boolean managerOnly, org.hibernate.Session hibSession) {
    	if (managerOnly)
    		return new TreeSet<Roles>(hibSession
    				.createQuery("from Roles where manager = true", Roles.class)
    				.setCacheable(true)
    				.list());
    	return new TreeSet<Roles>(hibSession
				.createQuery("from Roles", Roles.class)
				.setCacheable(true)
				.list());
    }

    public static Set<Roles> findAllInstructorRoles() {
    	return findAllInstructorRoles(RolesDAO.getInstance().getSession());
    }
    
    public static Set<Roles> findAllInstructorRoles(org.hibernate.Session hibSession) {
    	return new TreeSet<Roles>(hibSession
				.createQuery("from Roles where instructor = true", Roles.class)
				.setCacheable(true)
				.list());
    }

    @Override
	public int compareTo(Roles o) {
		if (isManager() != o.isManager())
			return (isManager() ? 1 : -1);
		if (getRights().size() != o.getRights().size())
			return (getRights().size() < o.getRights().size() ? -1 : 1); 
		return getAbbv().compareToIgnoreCase(o.getAbbv());
	}
}
