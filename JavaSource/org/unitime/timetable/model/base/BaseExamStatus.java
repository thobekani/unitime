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
package org.unitime.timetable.model.base;

import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.unitime.timetable.model.DepartmentStatusType;
import org.unitime.timetable.model.ExamStatus;
import org.unitime.timetable.model.ExamType;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.TimetableManager;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
@IdClass(ExamStatusId.class)
public abstract class BaseExamStatus implements Serializable {
	private static final long serialVersionUID = 1L;

	private Session iSession;
	private ExamType iType;

	private DepartmentStatusType iStatus;
	private Set<TimetableManager> iManagers;

	public BaseExamStatus() {
	}


	@Id
	@ManyToOne(optional = false)
	@JoinColumn(name = "session_id")
	public Session getSession() { return iSession; }
	public void setSession(Session session) { iSession = session; }

	@Id
	@ManyToOne(optional = false)
	@JoinColumn(name = "type_id")
	public ExamType getType() { return iType; }
	public void setType(ExamType type) { iType = type; }

	@ManyToOne(optional = true)
	@JoinColumn(name = "status_id", nullable = true)
	public DepartmentStatusType getStatus() { return iStatus; }
	public void setStatus(DepartmentStatusType status) { iStatus = status; }

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "exam_managers",
		joinColumns = {
			@JoinColumn(name = "session_id"),
			@JoinColumn(name = "type_id")
		},
		inverseJoinColumns = { @JoinColumn(name = "manager_id") })
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	public Set<TimetableManager> getManagers() { return iManagers; }
	public void setManagers(Set<TimetableManager> managers) { iManagers = managers; }
	public void addToManagers(TimetableManager timetableManager) {
		if (iManagers == null) iManagers = new HashSet<TimetableManager>();
		iManagers.add(timetableManager);
	}
	@Deprecated
	public void addTomanagers(TimetableManager timetableManager) {
		addToManagers(timetableManager);
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof ExamStatus)) return false;
		ExamStatus examStatus = (ExamStatus)o;
		if (getSession() == null || examStatus.getSession() == null || !getSession().equals(examStatus.getSession())) return false;
		if (getType() == null || examStatus.getType() == null || !getType().equals(examStatus.getType())) return false;
		return true;
	}

	@Override
	public int hashCode() {
		if (getSession() == null || getType() == null) return super.hashCode();
		return getSession().hashCode() ^ getType().hashCode();
	}

	public String toString() {
		return "ExamStatus[" + getSession() + ", " + getType() + "]";
	}

	public String toDebugString() {
		return "ExamStatus[" +
			"\n	Session: " + getSession() +
			"\n	Status: " + getStatus() +
			"\n	Type: " + getType() +
			"]";
	}
}
