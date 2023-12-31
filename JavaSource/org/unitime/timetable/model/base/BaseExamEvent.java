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

import org.unitime.timetable.model.Event;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.ExamEvent;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
public abstract class BaseExamEvent extends Event implements Serializable {
	private static final long serialVersionUID = 1L;

	private Exam iExam;

	public BaseExamEvent() {
	}

	public BaseExamEvent(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@ManyToOne(optional = false)
	@JoinColumn(name = "exam_id", nullable = false)
	public Exam getExam() { return iExam; }
	public void setExam(Exam exam) { iExam = exam; }

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof ExamEvent)) return false;
		if (getUniqueId() == null || ((ExamEvent)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((ExamEvent)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
	public String toString() {
		return "ExamEvent["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "ExamEvent[" +
			"\n	Email: " + getEmail() +
			"\n	EventName: " + getEventName() +
			"\n	Exam: " + getExam() +
			"\n	ExpirationDate: " + getExpirationDate() +
			"\n	MainContact: " + getMainContact() +
			"\n	MaxCapacity: " + getMaxCapacity() +
			"\n	MinCapacity: " + getMinCapacity() +
			"\n	SponsoringOrganization: " + getSponsoringOrganization() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
