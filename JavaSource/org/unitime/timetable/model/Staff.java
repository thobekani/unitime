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


import java.util.List;

import org.hibernate.query.Query;
import org.unitime.timetable.model.base.BaseStaff;
import org.unitime.timetable.model.dao.StaffDAO;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.NameFormat;
import org.unitime.timetable.util.NameInterface;


/**
 * @author Tomas Muller
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "staff")
public class Staff extends BaseStaff implements Comparable, NameInterface {
	private static final long serialVersionUID = 1L;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public Staff () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public Staff (java.lang.Long uniqueId) {
		super(uniqueId);
	}

/*[CONSTRUCTOR MARKER END]*/
	
	/**
	 * 
	 * @param deptCode
	 * @return
	 */
	public static List<Staff> getStaffByDept(String deptCode, Long acadSessionId) throws Exception {	
		if (deptCode == null) return null;
		Query<Staff> q = StaffDAO.getInstance().getSession().createQuery(
				"select distinct s from Staff s where s.dept=:deptCode and " +
				"(s.campus is null or s.campus=(select x.academicInitiative from Session x where x.uniqueId = :sessionId)) and " +
				"(select di.externalUniqueId from DepartmentalInstructor di " +
				"where di.department.deptCode=:deptCode and di.department.session.uniqueId=:sessionId and di.externalUniqueId = s.externalUniqueId ) is null",
				Staff.class);
		q.setParameter("deptCode", deptCode);
		q.setParameter("sessionId", acadSessionId);
		q.setCacheable(true);
		return q.list();
	}
	

	/**
	 * Search staff list for instructors with matching names
	 * @param fname First Name 
	 * @param lname Last Name
	 * @return
	 */
	public static List<Staff> findMatchingName(String fname, String lname) {
		if ((fname==null || fname.trim().length()==0) && (lname==null || lname.trim().length()==0)) return null;
		return StaffDAO.getInstance().getSession()
				.createQuery("from Staff where firstName like :fname and lastName like :lname", Staff.class)
				.setParameter("fname", fname == null ? "%" : fname + "%")
				.setParameter("lname", lname == null ? "%" : lname + "%")
				.list();
	}
	
	/**
	 * 
	 * @param o
	 * @return
	 */
	public int compareTo(Object o) {
		if (o==null || !(o instanceof Staff)) return -1;
		Staff i = (Staff)o;
		if (getPositionType()==null) {
			if (i.getPositionType()!=null) return 1;
		} else {
			if (i.getPositionType()==null) return -1;
			int cmp = getPositionType().getSortOrder().compareTo(i.getPositionType().getSortOrder());
			if (cmp!=0) return cmp;
		}
		int cmp = nameLastNameFirst().compareToIgnoreCase(i.nameLastNameFirst());
		if (cmp!=0) return cmp;
		return (getUniqueId() == null ? Long.valueOf(-1) : getUniqueId()).compareTo(i.getUniqueId() == null ? -1 : i.getUniqueId());
	}
	
	/**
	 * 
	 * @return
	 */
	public String nameLastNameFirst() {
		return ((getLastName() == null ? "" : getLastName().trim()) + ", "
				+ (getFirstName() == null ? "" : getFirstName().trim()) + " " + (getMiddleName() == null ? ""
				: getMiddleName().trim()));
	}
	
	public String getDeptName(Long sessionId) {
		if (getDept()==null || getDept().length()==0) return "N/A";
		Department d = Department.findByDeptCode(getDept(), sessionId);
		if (d != null && (getCampus() == null || getCampus().equals(d.getSession().getAcademicInitiative())))
			return "<span title='"+d.getHtmlTitle()+"'>"+d.getShortLabel()+"</span>";
		else
			return getDept() + (getCampus() == null ? "" : " (" + getCampus() + ")");
	}
	
	@Transient
	public String getName() {
		return Constants.toInitialCase(
				(getLastName()==null?"":getLastName().trim())+", "+
				(getFirstName()==null?"":getFirstName().trim())+
				(getMiddleName()==null?"":" "+getMiddleName().trim()));
	}
	
	public String getName(String instructorNameFormat) {
		return NameFormat.fromReference(instructorNameFormat).format(this);
	}

}
