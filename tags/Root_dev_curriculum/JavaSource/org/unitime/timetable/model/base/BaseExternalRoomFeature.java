/*
 * UniTime 3.1 (University Timetabling Application)
 * Copyright (C) 2008, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package org.unitime.timetable.model.base;

import java.io.Serializable;


/**
 * This is an object that contains data related to the EXTERNAL_ROOM_FEATURE table.
 * Do not modify this class because it will be overwritten if the configuration file
 * related to this class is modified.
 *
 * @hibernate.class
 *  table="EXTERNAL_ROOM_FEATURE"
 */

public abstract class BaseExternalRoomFeature  implements Serializable {

	public static String REF = "ExternalRoomFeature";
	public static String PROP_NAME = "name";
	public static String PROP_VALUE = "value";


	// constructors
	public BaseExternalRoomFeature () {
		initialize();
	}

	/**
	 * Constructor for primary key
	 */
	public BaseExternalRoomFeature (java.lang.Long uniqueId) {
		this.setUniqueId(uniqueId);
		initialize();
	}

	/**
	 * Constructor for required fields
	 */
	public BaseExternalRoomFeature (
		java.lang.Long uniqueId,
		org.unitime.timetable.model.ExternalRoom room,
		java.lang.String name,
		java.lang.String value) {

		this.setUniqueId(uniqueId);
		this.setRoom(room);
		this.setName(name);
		this.setValue(value);
		initialize();
	}

	protected void initialize () {}



	private int hashCode = Integer.MIN_VALUE;

	// primary key
	private java.lang.Long uniqueId;

	// fields
	private java.lang.String name;
	private java.lang.String value;

	// many to one
	private org.unitime.timetable.model.ExternalRoom room;



	/**
	 * Return the unique identifier of this class
     * @hibernate.id
     *  generator-class="sequence"
     *  column="UNIQUEID"
     */
	public java.lang.Long getUniqueId () {
		return uniqueId;
	}

	/**
	 * Set the unique identifier of this class
	 * @param uniqueId the new ID
	 */
	public void setUniqueId (java.lang.Long uniqueId) {
		this.uniqueId = uniqueId;
		this.hashCode = Integer.MIN_VALUE;
	}




	/**
	 * Return the value associated with the column: NAME
	 */
	public java.lang.String getName () {
		return name;
	}

	/**
	 * Set the value related to the column: NAME
	 * @param name the NAME value
	 */
	public void setName (java.lang.String name) {
		this.name = name;
	}



	/**
	 * Return the value associated with the column: VALUE
	 */
	public java.lang.String getValue () {
		return value;
	}

	/**
	 * Set the value related to the column: VALUE
	 * @param value the VALUE value
	 */
	public void setValue (java.lang.String value) {
		this.value = value;
	}



	/**
	 * Return the value associated with the column: EXTERNAL_ROOM_ID
	 */
	public org.unitime.timetable.model.ExternalRoom getRoom () {
		return room;
	}

	/**
	 * Set the value related to the column: EXTERNAL_ROOM_ID
	 * @param room the EXTERNAL_ROOM_ID value
	 */
	public void setRoom (org.unitime.timetable.model.ExternalRoom room) {
		this.room = room;
	}





	public boolean equals (Object obj) {
		if (null == obj) return false;
		if (!(obj instanceof org.unitime.timetable.model.ExternalRoomFeature)) return false;
		else {
			org.unitime.timetable.model.ExternalRoomFeature externalRoomFeature = (org.unitime.timetable.model.ExternalRoomFeature) obj;
			if (null == this.getUniqueId() || null == externalRoomFeature.getUniqueId()) return false;
			else return (this.getUniqueId().equals(externalRoomFeature.getUniqueId()));
		}
	}

	public int hashCode () {
		if (Integer.MIN_VALUE == this.hashCode) {
			if (null == this.getUniqueId()) return super.hashCode();
			else {
				String hashStr = this.getClass().getName() + ":" + this.getUniqueId().hashCode();
				this.hashCode = hashStr.hashCode();
			}
		}
		return this.hashCode;
	}


	public String toString () {
		return super.toString();
	}


}
