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
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;

import java.io.Serializable;

import org.unitime.timetable.model.InstructorCourseRequirement;
import org.unitime.timetable.model.InstructorCourseRequirementNote;
import org.unitime.timetable.model.InstructorCourseRequirementType;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
@IdClass(InstructorCourseRequirementNoteId.class)
public abstract class BaseInstructorCourseRequirementNote implements Serializable {
	private static final long serialVersionUID = 1L;

	private InstructorCourseRequirement iRequirement;
	private InstructorCourseRequirementType iType;
	private String iNote;


	public BaseInstructorCourseRequirementNote() {
	}


	@Id
	@ManyToOne(optional = false)
	@JoinColumn(name = "requirement_id")
	public InstructorCourseRequirement getRequirement() { return iRequirement; }
	public void setRequirement(InstructorCourseRequirement requirement) { iRequirement = requirement; }

	@Id
	@ManyToOne(optional = false)
	@JoinColumn(name = "type_id")
	public InstructorCourseRequirementType getType() { return iType; }
	public void setType(InstructorCourseRequirementType type) { iType = type; }

	@Column(name = "note", nullable = true, length = 2048)
	public String getNote() { return iNote; }
	public void setNote(String note) { iNote = note; }

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof InstructorCourseRequirementNote)) return false;
		InstructorCourseRequirementNote instructorCourseRequirementNote = (InstructorCourseRequirementNote)o;
		if (getRequirement() == null || instructorCourseRequirementNote.getRequirement() == null || !getRequirement().equals(instructorCourseRequirementNote.getRequirement())) return false;
		if (getType() == null || instructorCourseRequirementNote.getType() == null || !getType().equals(instructorCourseRequirementNote.getType())) return false;
		return true;
	}

	@Override
	public int hashCode() {
		if (getRequirement() == null || getType() == null) return super.hashCode();
		return getRequirement().hashCode() ^ getType().hashCode();
	}

	public String toString() {
		return "InstructorCourseRequirementNote[" + getRequirement() + ", " + getType() + "]";
	}

	public String toDebugString() {
		return "InstructorCourseRequirementNote[" +
			"\n	Note: " + getNote() +
			"\n	Requirement: " + getRequirement() +
			"\n	Type: " + getType() +
			"]";
	}
}
