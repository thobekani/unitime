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

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.OneToMany;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.JoinFormula;
import org.unitime.timetable.model.DepartmentStatusType;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.ExamConflict;
import org.unitime.timetable.model.ExamOwner;
import org.unitime.timetable.model.ExamPeriod;
import org.unitime.timetable.model.ExamType;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.PreferenceGroup;
import org.unitime.timetable.model.Session;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
public abstract class BaseExam extends PreferenceGroup implements Serializable {
	private static final long serialVersionUID = 1L;

	private String iName;
	private String iNote;
	private Integer iLength;
	private Integer iExamSize;
	private Integer iPrintOffset;
	private Integer iMaxNbrRooms;
	private Integer iSeatingType;
	private String iAssignedPreference;
	private Integer iAvgPeriod;
	private Long iUniqueIdRolledForwardFrom;

	private Session iSession;
	private ExamPeriod iAssignedPeriod;
	private ExamType iExamType;
	private DepartmentStatusType iStatusType;
	private Set<ExamOwner> iOwners;
	private Set<Location> iAssignedRooms;
	private Set<DepartmentalInstructor> iInstructors;
	private Set<ExamConflict> iConflicts;

	public BaseExam() {
	}

	public BaseExam(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@Column(name = "name", nullable = true, length = 100)
	public String getName() { return iName; }
	public void setName(String name) { iName = name; }

	@Column(name = "note", nullable = true, length = 1000)
	public String getNote() { return iNote; }
	public void setNote(String note) { iNote = note; }

	@Column(name = "length", nullable = false, length = 10)
	public Integer getLength() { return iLength; }
	public void setLength(Integer length) { iLength = length; }

	@Column(name = "exam_size", nullable = true, length = 10)
	public Integer getExamSize() { return iExamSize; }
	public void setExamSize(Integer examSize) { iExamSize = examSize; }

	@Column(name = "print_offset", nullable = true, length = 10)
	public Integer getPrintOffset() { return iPrintOffset; }
	public void setPrintOffset(Integer printOffset) { iPrintOffset = printOffset; }

	@Column(name = "max_nbr_rooms", nullable = false, length = 10)
	public Integer getMaxNbrRooms() { return iMaxNbrRooms; }
	public void setMaxNbrRooms(Integer maxNbrRooms) { iMaxNbrRooms = maxNbrRooms; }

	@Column(name = "seating_type", nullable = false, length = 10)
	public Integer getSeatingType() { return iSeatingType; }
	public void setSeatingType(Integer seatingType) { iSeatingType = seatingType; }

	@Column(name = "assigned_pref", nullable = true, length = 100)
	public String getAssignedPreference() { return iAssignedPreference; }
	public void setAssignedPreference(String assignedPreference) { iAssignedPreference = assignedPreference; }

	@Column(name = "avg_period", nullable = true, length = 10)
	public Integer getAvgPeriod() { return iAvgPeriod; }
	public void setAvgPeriod(Integer avgPeriod) { iAvgPeriod = avgPeriod; }

	@Column(name = "uid_rolled_fwd_from", nullable = true, length = 20)
	public Long getUniqueIdRolledForwardFrom() { return iUniqueIdRolledForwardFrom; }
	public void setUniqueIdRolledForwardFrom(Long uniqueIdRolledForwardFrom) { iUniqueIdRolledForwardFrom = uniqueIdRolledForwardFrom; }

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "session_id", nullable = false)
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	public Session getSession() { return iSession; }
	public void setSession(Session session) { iSession = session; }

	@ManyToOne(optional = true)
	@JoinColumn(name = "assigned_period", nullable = true)
	public ExamPeriod getAssignedPeriod() { return iAssignedPeriod; }
	public void setAssignedPeriod(ExamPeriod assignedPeriod) { iAssignedPeriod = assignedPeriod; }

	@ManyToOne(optional = false)
	@JoinColumn(name = "exam_type_id", nullable = false)
	public ExamType getExamType() { return iExamType; }
	public void setExamType(ExamType examType) { iExamType = examType; }

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinFormula(" ( select s.status_id from %SCHEMA%.exam_status s where s.session_id = session_id and s.type_id = exam_type_id ) ")
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	public DepartmentStatusType getStatusType() { return iStatusType; }
	public void setStatusType(DepartmentStatusType statusType) { iStatusType = statusType; }

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "exam", cascade = {CascadeType.ALL}, orphanRemoval = true)
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	public Set<ExamOwner> getOwners() { return iOwners; }
	public void setOwners(Set<ExamOwner> owners) { iOwners = owners; }
	public void addToOwners(ExamOwner examOwner) {
		if (iOwners == null) iOwners = new HashSet<ExamOwner>();
		iOwners.add(examOwner);
	}
	@Deprecated
	public void addToowners(ExamOwner examOwner) {
		addToOwners(examOwner);
	}

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "exam_room_assignment",
		joinColumns = { @JoinColumn(name = "exam_id") },
		inverseJoinColumns = { @JoinColumn(name = "location_id") })
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	public Set<Location> getAssignedRooms() { return iAssignedRooms; }
	public void setAssignedRooms(Set<Location> assignedRooms) { iAssignedRooms = assignedRooms; }
	public void addToAssignedRooms(Location location) {
		if (iAssignedRooms == null) iAssignedRooms = new HashSet<Location>();
		iAssignedRooms.add(location);
	}
	@Deprecated
	public void addToassignedRooms(Location location) {
		addToAssignedRooms(location);
	}

	@ManyToMany(fetch = FetchType.LAZY, mappedBy = "exams")
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	public Set<DepartmentalInstructor> getInstructors() { return iInstructors; }
	public void setInstructors(Set<DepartmentalInstructor> instructors) { iInstructors = instructors; }
	public void addToInstructors(DepartmentalInstructor departmentalInstructor) {
		if (iInstructors == null) iInstructors = new HashSet<DepartmentalInstructor>();
		iInstructors.add(departmentalInstructor);
	}
	@Deprecated
	public void addToinstructors(DepartmentalInstructor departmentalInstructor) {
		addToInstructors(departmentalInstructor);
	}

	@ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.ALL})
	@JoinTable(name = "xconflict_exam",
		joinColumns = { @JoinColumn(name = "exam_id") },
		inverseJoinColumns = { @JoinColumn(name = "conflict_id") })
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	@Cascade(value = org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
	public Set<ExamConflict> getConflicts() { return iConflicts; }
	public void setConflicts(Set<ExamConflict> conflicts) { iConflicts = conflicts; }
	public void addToConflicts(ExamConflict examConflict) {
		if (iConflicts == null) iConflicts = new HashSet<ExamConflict>();
		iConflicts.add(examConflict);
	}
	@Deprecated
	public void addToconflicts(ExamConflict examConflict) {
		addToConflicts(examConflict);
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof Exam)) return false;
		if (getUniqueId() == null || ((Exam)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((Exam)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
	public String toString() {
		return "Exam["+getUniqueId()+" "+getName()+"]";
	}

	public String toDebugString() {
		return "Exam[" +
			"\n	AssignedPeriod: " + getAssignedPeriod() +
			"\n	AssignedPreference: " + getAssignedPreference() +
			"\n	AvgPeriod: " + getAvgPeriod() +
			"\n	ExamSize: " + getExamSize() +
			"\n	ExamType: " + getExamType() +
			"\n	Length: " + getLength() +
			"\n	MaxNbrRooms: " + getMaxNbrRooms() +
			"\n	Name: " + getName() +
			"\n	Note: " + getNote() +
			"\n	PrintOffset: " + getPrintOffset() +
			"\n	SeatingType: " + getSeatingType() +
			"\n	Session: " + getSession() +
			"\n	UniqueId: " + getUniqueId() +
			"\n	UniqueIdRolledForwardFrom: " + getUniqueIdRolledForwardFrom() +
			"]";
	}
}
