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


import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Transient;

import java.util.Date;

import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.gwt.shared.ReservationInterface.OverrideType;
import org.unitime.timetable.model.base.BaseOverrideReservation;

@Entity
@DiscriminatorValue("4")
public class OverrideReservation extends BaseOverrideReservation {
	private static final long serialVersionUID = 1L;

	public OverrideReservation() {
		super();
	}
	
	@Transient
	public OverrideType getOverrideType() {
		return getType() == null ? null : OverrideType.values()[getType()];
	}
	
	public void setOverrideType(OverrideType type) {
		setType(type == null ? null : type.ordinal());
	}
	
	@Override
	@Transient
	public boolean isExpired() {
		OverrideType type = getOverrideType();
		return (type == null || type.isCanHaveExpirationDate() ? super.isExpired() : type.isExpired());
	}
	
	@Override
	@Transient
	public Date getStartDate() {
		OverrideType type = getOverrideType();
		return (type == null || type.isCanHaveExpirationDate() ? super.getStartDate() : null);
	}
	
	@Override
	@Transient
	public Date getExpirationDate() {
		OverrideType type = getOverrideType();
		return (type == null || type.isCanHaveExpirationDate() ? super.getExpirationDate() : null);		
	}
	
	@Override
	@Transient
	public int getPriority() {
		return ApplicationProperty.ReservationPriorityOverride.intValue();
	}

	@Override
	@Transient
	public boolean isCanAssignOverLimit() {
		OverrideType type = getOverrideType();
		return type != null && type.isAllowOverLimit();
	}

	@Override
	@Transient
	public boolean isMustBeUsed() {
		OverrideType type = getOverrideType();
		return type != null && type.isMustBeUsed();
	}

	@Override
	@Transient
	public boolean isAllowOverlap() {
		OverrideType type = getOverrideType();
		return type != null && type.isAllowTimeConflict();
	}
	
	@Override
	@Transient
	public boolean isAlwaysExpired() {
		OverrideType type = getOverrideType();
		return type != null && !type.isCanHaveExpirationDate();
	}
}
