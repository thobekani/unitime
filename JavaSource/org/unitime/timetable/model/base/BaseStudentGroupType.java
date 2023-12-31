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
import org.unitime.timetable.model.StudentGroupType;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
public abstract class BaseStudentGroupType extends RefTableEntry implements Serializable {
	private static final long serialVersionUID = 1L;

	private Boolean iKeepTogether;
	private Short iAllowDisabled;
	private Boolean iAdvisorsCanSet;


	public BaseStudentGroupType() {
	}

	public BaseStudentGroupType(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@Column(name = "together", nullable = false)
	public Boolean isKeepTogether() { return iKeepTogether; }
	@Transient
	public Boolean getKeepTogether() { return iKeepTogether; }
	public void setKeepTogether(Boolean keepTogether) { iKeepTogether = keepTogether; }

	@Column(name = "allow_disabled", nullable = false)
	public Short getAllowDisabled() { return iAllowDisabled; }
	public void setAllowDisabled(Short allowDisabled) { iAllowDisabled = allowDisabled; }

	@Column(name = "advisor", nullable = false)
	public Boolean isAdvisorsCanSet() { return iAdvisorsCanSet; }
	@Transient
	public Boolean getAdvisorsCanSet() { return iAdvisorsCanSet; }
	public void setAdvisorsCanSet(Boolean advisorsCanSet) { iAdvisorsCanSet = advisorsCanSet; }

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof StudentGroupType)) return false;
		if (getUniqueId() == null || ((StudentGroupType)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((StudentGroupType)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
	public String toString() {
		return "StudentGroupType["+getUniqueId()+" "+getLabel()+"]";
	}

	public String toDebugString() {
		return "StudentGroupType[" +
			"\n	AdvisorsCanSet: " + getAdvisorsCanSet() +
			"\n	AllowDisabled: " + getAllowDisabled() +
			"\n	KeepTogether: " + getKeepTogether() +
			"\n	Label: " + getLabel() +
			"\n	Reference: " + getReference() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
