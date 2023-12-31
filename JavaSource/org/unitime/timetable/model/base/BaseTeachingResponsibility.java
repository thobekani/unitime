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

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;

import java.io.Serializable;

import org.unitime.timetable.model.RefTableEntry;
import org.unitime.timetable.model.TeachingResponsibility;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
public abstract class BaseTeachingResponsibility extends RefTableEntry implements Serializable {
	private static final long serialVersionUID = 1L;

	private Boolean iCoordinator;
	private Boolean iInstructor;
	private String iAbbreviation;
	private Integer iOptions;


	public BaseTeachingResponsibility() {
	}

	public BaseTeachingResponsibility(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@Column(name = "coordinator", nullable = false)
	public Boolean isCoordinator() { return iCoordinator; }
	@Transient
	public Boolean getCoordinator() { return iCoordinator; }
	public void setCoordinator(Boolean coordinator) { iCoordinator = coordinator; }

	@Column(name = "instructor", nullable = false)
	public Boolean isInstructor() { return iInstructor; }
	@Transient
	public Boolean getInstructor() { return iInstructor; }
	public void setInstructor(Boolean instructor) { iInstructor = instructor; }

	@Column(name = "abbreviation", nullable = true, length = 40)
	public String getAbbreviation() { return iAbbreviation; }
	public void setAbbreviation(String abbreviation) { iAbbreviation = abbreviation; }

	@Column(name = "options", nullable = false)
	public Integer getOptions() { return iOptions; }
	public void setOptions(Integer options) { iOptions = options; }

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof TeachingResponsibility)) return false;
		if (getUniqueId() == null || ((TeachingResponsibility)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((TeachingResponsibility)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
	public String toString() {
		return "TeachingResponsibility["+getUniqueId()+" "+getLabel()+"]";
	}

	public String toDebugString() {
		return "TeachingResponsibility[" +
			"\n	Abbreviation: " + getAbbreviation() +
			"\n	Coordinator: " + getCoordinator() +
			"\n	Instructor: " + getInstructor() +
			"\n	Label: " + getLabel() +
			"\n	Options: " + getOptions() +
			"\n	Reference: " + getReference() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
