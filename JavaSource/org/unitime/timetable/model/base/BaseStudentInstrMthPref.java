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

import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;

import java.io.Serializable;

import org.unitime.timetable.model.InstructionalMethod;
import org.unitime.timetable.model.StudentInstrMthPref;
import org.unitime.timetable.model.StudentSectioningPref;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
public abstract class BaseStudentInstrMthPref extends StudentSectioningPref implements Serializable {
	private static final long serialVersionUID = 1L;

	private InstructionalMethod iInstructionalMethod;

	public BaseStudentInstrMthPref() {
	}

	public BaseStudentInstrMthPref(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@ManyToOne(optional = false)
	@JoinColumn(name = "instr_mthd_id", nullable = false)
	public InstructionalMethod getInstructionalMethod() { return iInstructionalMethod; }
	public void setInstructionalMethod(InstructionalMethod instructionalMethod) { iInstructionalMethod = instructionalMethod; }

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof StudentInstrMthPref)) return false;
		if (getUniqueId() == null || ((StudentInstrMthPref)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((StudentInstrMthPref)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
	public String toString() {
		return "StudentInstrMthPref["+getUniqueId()+" "+getLabel()+"]";
	}

	public String toDebugString() {
		return "StudentInstrMthPref[" +
			"\n	CourseRequest: " + getCourseRequest() +
			"\n	InstructionalMethod: " + getInstructionalMethod() +
			"\n	Label: " + getLabel() +
			"\n	Required: " + getRequired() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
