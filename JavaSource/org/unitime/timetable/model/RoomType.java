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

import java.util.List;
import java.util.TreeSet;

import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.model.base.BaseRoomType;
import org.unitime.timetable.model.dao.RoomTypeDAO;
import org.unitime.timetable.model.dao.RoomTypeOptionDAO;


/**
 * @author Tomas Muller
 */
@Entity
@Table(name = "room_type")
public class RoomType extends BaseRoomType implements Comparable<RoomType> {
	private static final long serialVersionUID = 1L;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public RoomType () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public RoomType (Long uniqueId) {
		super(uniqueId);
	}

/*[CONSTRUCTOR MARKER END]*/
	
	public static TreeSet<RoomType> findAll() {
	    return new TreeSet<RoomType>(RoomTypeDAO.getInstance().findAll());
	}

	public static TreeSet<RoomType> findAll(Long sessionId) {
		return new TreeSet<RoomType>(RoomTypeDAO.getInstance().getSession().createQuery(
				"select distinct t from Location l inner join l.roomType t where l.session.uniqueId = :sessionId", RoomType.class)
				.setParameter("sessionId", sessionId).setCacheable(true).list());
	}

	public static TreeSet<RoomType> findAll(boolean isRoom) {
        return new TreeSet<RoomType>(
        		RoomTypeDAO.getInstance().getSession()
				.createQuery("from RoomType where room = :isRoom", RoomType.class)
				.setParameter("isRoom", isRoom)
				.setCacheable(true)
				.list());
    }

    public static RoomType findByReference(String ref) {
    	return RoomTypeDAO.getInstance().getSession()
				.createQuery("from RoomType where reference = :ref", RoomType.class)
				.setParameter("ref", ref)
				.setCacheable(true)
				.uniqueResult();
    }
    
    public int compareTo(RoomType t) {
        int cmp = getOrd().compareTo(t.getOrd());
        if (cmp!=0) return cmp;
        cmp = getLabel().compareTo(t.getLabel());
        if (cmp!=0) return cmp;
        return (getUniqueId() == null ? Long.valueOf(-1) : getUniqueId()).compareTo(t.getUniqueId() == null ? -1 : t.getUniqueId());
    }
    
    public RoomTypeOption getOption(Department department) {
    	if (department == null) {
    		RoomTypeOption opt = new RoomTypeOption(this, department);
    		opt.setStatus(RoomTypeOption.getDefaultStatus());
        	opt.setBreakTime(ApplicationProperty.RoomDefaultBreakTime.intValue(getReference()));
    		return opt;
    	}
        RoomTypeOption opt = RoomTypeOptionDAO.getInstance().getSession().createQuery(
    			"from RoomTypeOption where department.uniqueId = :departmentId and roomType.uniqueId = :roomTypeId", RoomTypeOption.class)
    			.setParameter("departmentId", department.getUniqueId())
    			.setParameter("roomTypeId", getUniqueId())
    			.setCacheable(true)
    			.uniqueResult();
        if (opt==null) opt = new RoomTypeOption(this, department);
        if (opt.getStatus() == null) opt.setStatus(RoomTypeOption.getDefaultStatus());
        if (opt.getBreakTime() == null)
        	opt.setBreakTime(ApplicationProperty.RoomDefaultBreakTime.intValue(getReference()));
        return opt;
    }
    
    @Deprecated
    public boolean canScheduleEvents(Long sessionId) {
    	boolean ret = false;
    	for (RoomTypeOption option: RoomTypeDAO.getInstance().getSession().createQuery(
                "select distinct o from " + (isRoom() ? "Room" : "NonUniversityLocation") + " r, RoomTypeOption o " +
                "where r.roomType.uniqueId = :roomTypeId and r.session.uniqueId = :sessionId and "+
                "r.eventDepartment.allowEvents = true and r.eventDepartment = o.department and r.roomType = o.roomType", RoomTypeOption.class)
                .setParameter("roomTypeId", getUniqueId())
                .setParameter("sessionId", sessionId)
    			.setCacheable(true).list()) {
    		if (option.canScheduleEvents()) { ret = true; break; }
    	}
    	return ret;

    }
    
    public int countRooms() {
        return RoomTypeDAO.getInstance().getSession().createQuery(
                "select count(distinct r.permanentId) from "+(isRoom()?"Room":"NonUniversityLocation")+" r where r.roomType.uniqueId=:roomTypeId", Number.class
        ).setParameter("roomTypeId", getUniqueId()).setCacheable(true).uniqueResult().intValue();
    }

    public int countRooms(Long sessionId) {
        return RoomTypeDAO.getInstance().getSession().createQuery(
                "select count(r) from "+(isRoom()?"Room":"NonUniversityLocation")+" r where r.roomType.uniqueId=:roomTypeId and r.session.uniqueId=:sessionId", Number.class
        ).setParameter("roomTypeId", getUniqueId()).setParameter("sessionId", sessionId).setCacheable(true).uniqueResult().intValue();
    }

    public int countManagableRooms() {
        return RoomTypeDAO.getInstance().getSession().createQuery(
                "select count(distinct r.permanentId) from "+(isRoom()?"Room":"NonUniversityLocation")+" r " +
                "where r.roomType.uniqueId=:roomTypeId and "+
                "r.eventDepartment.allowEvents = true", Number.class
        ).setParameter("roomTypeId", getUniqueId()).setCacheable(true).uniqueResult().intValue();
    }
    
    public List<Location> getManagableRooms(Long sessionId) {
        return RoomTypeDAO.getInstance().getSession().createQuery(
                "select r from "+(isRoom()?"Room":"NonUniversityLocation")+" r " +
                "where r.roomType.uniqueId=:roomTypeId and r.session.uniqueId=:sessionId and "+
                "r.eventDepartment.allowEvents = true", Location.class
        ).setParameter("roomTypeId", getUniqueId()).setParameter("sessionId", sessionId).setCacheable(true).list();
    }


    public int countManagableRooms(Long sessionId) {
        return RoomTypeDAO.getInstance().getSession().createQuery(
                "select count(r) from "+(isRoom()?"Room":"NonUniversityLocation")+" r " +
                "where r.roomType.uniqueId=:roomTypeId and r.session.uniqueId=:sessionId and "+
                "r.eventDepartment.allowEvents = true", Number.class
        ).setParameter("roomTypeId", getUniqueId()).setParameter("sessionId", sessionId).setCacheable(true).uniqueResult().intValue();
    }

    public int countManagableRoomsOfBuilding(Long buildingId) {
        return RoomTypeDAO.getInstance().getSession().createQuery(
                "select count(r) from Room r " +
                "where r.roomType.uniqueId=:roomTypeId and r.building.uniqueId=:buildingId and "+
                "r.eventDepartment.allowEvents = true", Number.class
        ).setParameter("roomTypeId", getUniqueId()).setParameter("buildingId", buildingId).setCacheable(true).uniqueResult().intValue();
    }
}
