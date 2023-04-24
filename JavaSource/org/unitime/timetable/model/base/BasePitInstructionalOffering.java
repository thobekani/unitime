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

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToMany;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Formula;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.PitCourseOffering;
import org.unitime.timetable.model.PitInstrOfferingConfig;
import org.unitime.timetable.model.PitInstructionalOffering;
import org.unitime.timetable.model.PitOfferingCoordinator;
import org.unitime.timetable.model.PointInTimeData;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
public abstract class BasePitInstructionalOffering implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private Integer iInstrOfferingPermId;
	private Integer iDemand;
	private Integer iLimit;
	private Long iUniqueIdRolledForwardFrom;
	private String iExternalUniqueId;
	private Integer iEnrollment;

	private PointInTimeData iPointInTimeData;
	private InstructionalOffering iInstructionalOffering;
	private Set<PitCourseOffering> iPitCourseOfferings;
	private Set<PitInstrOfferingConfig> iPitInstrOfferingConfigs;
	private Set<PitOfferingCoordinator> iPitOfferingCoordinators;

	public BasePitInstructionalOffering() {
	}

	public BasePitInstructionalOffering(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@Id
	@GenericGenerator(name = "pit_instr_offering_id", strategy = "org.unitime.commons.hibernate.id.UniqueIdGenerator", parameters = {
		@Parameter(name = "sequence", value = "point_in_time_seq")
	})
	@GeneratedValue(generator = "pit_instr_offering_id")
	@Column(name="uniqueid")
	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	@Column(name = "instr_offering_perm_id", nullable = false, length = 10)
	public Integer getInstrOfferingPermId() { return iInstrOfferingPermId; }
	public void setInstrOfferingPermId(Integer instrOfferingPermId) { iInstrOfferingPermId = instrOfferingPermId; }

	@Column(name = "demand", nullable = true, length = 4)
	public Integer getDemand() { return iDemand; }
	public void setDemand(Integer demand) { iDemand = demand; }

	@Column(name = "offr_limit", nullable = true, length = 10)
	public Integer getLimit() { return iLimit; }
	public void setLimit(Integer limit) { iLimit = limit; }

	@Column(name = "uid_rolled_fwd_from", nullable = true, length = 20)
	public Long getUniqueIdRolledForwardFrom() { return iUniqueIdRolledForwardFrom; }
	public void setUniqueIdRolledForwardFrom(Long uniqueIdRolledForwardFrom) { iUniqueIdRolledForwardFrom = uniqueIdRolledForwardFrom; }

	@Column(name = "external_uid", nullable = true, length = 40)
	public String getExternalUniqueId() { return iExternalUniqueId; }
	public void setExternalUniqueId(String externalUniqueId) { iExternalUniqueId = externalUniqueId; }

	@Formula("(select count(distinct e.pit_student_id) from %SCHEMA%.pit_student_class_enrl e inner join %SCHEMA%.pit_course_offering co on co.uniqueid = e.pit_course_offering_id where co.pit_instr_offr_id = uniqueid)")
	public Integer getEnrollment() { return iEnrollment; }
	public void setEnrollment(Integer enrollment) { iEnrollment = enrollment; }

	@ManyToOne(optional = false)
	@JoinColumn(name = "point_in_time_data_id", nullable = false)
	public PointInTimeData getPointInTimeData() { return iPointInTimeData; }
	public void setPointInTimeData(PointInTimeData pointInTimeData) { iPointInTimeData = pointInTimeData; }

	@ManyToOne(optional = true)
	@JoinColumn(name = "instr_offering_id", nullable = true)
	public InstructionalOffering getInstructionalOffering() { return iInstructionalOffering; }
	public void setInstructionalOffering(InstructionalOffering instructionalOffering) { iInstructionalOffering = instructionalOffering; }

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "pitInstructionalOffering", cascade = {CascadeType.ALL}, orphanRemoval = true)
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, include = "non-lazy")
	public Set<PitCourseOffering> getPitCourseOfferings() { return iPitCourseOfferings; }
	public void setPitCourseOfferings(Set<PitCourseOffering> pitCourseOfferings) { iPitCourseOfferings = pitCourseOfferings; }
	public void addTopitCourseOfferings(PitCourseOffering pitCourseOffering) {
		if (iPitCourseOfferings == null) iPitCourseOfferings = new HashSet<PitCourseOffering>();
		iPitCourseOfferings.add(pitCourseOffering);
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "pitInstructionalOffering", cascade = {CascadeType.ALL}, orphanRemoval = true)
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, include = "non-lazy")
	public Set<PitInstrOfferingConfig> getPitInstrOfferingConfigs() { return iPitInstrOfferingConfigs; }
	public void setPitInstrOfferingConfigs(Set<PitInstrOfferingConfig> pitInstrOfferingConfigs) { iPitInstrOfferingConfigs = pitInstrOfferingConfigs; }
	public void addTopitInstrOfferingConfigs(PitInstrOfferingConfig pitInstrOfferingConfig) {
		if (iPitInstrOfferingConfigs == null) iPitInstrOfferingConfigs = new HashSet<PitInstrOfferingConfig>();
		iPitInstrOfferingConfigs.add(pitInstrOfferingConfig);
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "pitInstructionalOffering")
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, include = "non-lazy")
	public Set<PitOfferingCoordinator> getPitOfferingCoordinators() { return iPitOfferingCoordinators; }
	public void setPitOfferingCoordinators(Set<PitOfferingCoordinator> pitOfferingCoordinators) { iPitOfferingCoordinators = pitOfferingCoordinators; }
	public void addTopitOfferingCoordinators(PitOfferingCoordinator pitOfferingCoordinator) {
		if (iPitOfferingCoordinators == null) iPitOfferingCoordinators = new HashSet<PitOfferingCoordinator>();
		iPitOfferingCoordinators.add(pitOfferingCoordinator);
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof PitInstructionalOffering)) return false;
		if (getUniqueId() == null || ((PitInstructionalOffering)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((PitInstructionalOffering)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
	public String toString() {
		return "PitInstructionalOffering["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "PitInstructionalOffering[" +
			"\n	Demand: " + getDemand() +
			"\n	ExternalUniqueId: " + getExternalUniqueId() +
			"\n	InstrOfferingPermId: " + getInstrOfferingPermId() +
			"\n	InstructionalOffering: " + getInstructionalOffering() +
			"\n	Limit: " + getLimit() +
			"\n	PointInTimeData: " + getPointInTimeData() +
			"\n	UniqueId: " + getUniqueId() +
			"\n	UniqueIdRolledForwardFrom: " + getUniqueIdRolledForwardFrom() +
			"]";
	}
}
