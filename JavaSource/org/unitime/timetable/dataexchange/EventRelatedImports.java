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

package org.unitime.timetable.dataexchange;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import org.unitime.commons.Email;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.model.Meeting;
import org.unitime.timetable.model.NonUniversityLocation;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.util.CalendarUtils;
import org.unitime.timetable.util.Constants;

/**
 * @author Stephanie Schluttenhofer, Tomas Muller
 *
 */
public abstract class EventRelatedImports extends BaseImport {

	protected String timeFormat = null;
	protected Vector<String> changeList = new Vector<String>();
	protected TreeSet<String> missingLocations = new TreeSet<String>();
	protected Vector<String> notes = new Vector<String>();
	protected String dateFormat = null;
	protected boolean trimLeadingZerosFromExternalId = false;
	protected Session session = null;


	protected abstract String getEmailSubject();
	/**
	 * 
	 */
	public EventRelatedImports() {
	}

	protected void addNote(String note){
		notes.add(note);
	}
	
	protected void clearNotes(){
		notes = new Vector<String>();
	}
	
	protected void updateChangeList(boolean changed){
		if(changed && notes != null) {
			changeList.addAll(notes);
			String note = null;
			for(Iterator<String> it = notes.iterator(); it.hasNext(); ){
				note = (String) it.next();
				info(note);
			}
		}
		clearNotes();
	}
	
	protected void addMissingLocation(String location){
		missingLocations.add(location);
	}
	
	protected void reportMissingLocations(){
		if (!missingLocations.isEmpty()) {
			changeList.add("\nMissing Locations\n");
			info("\nMissing Locations\n");
			for(Iterator<String> it = missingLocations.iterator(); it.hasNext();){
				String location = (String) it.next();
				changeList.add("\t" + location);
				info("\t" + location);
			}
		}
	}
	
	protected void mailLoadResults(){
    	try {
    		Email email = Email.createEmail();
    		email.setSubject("UniTime (Data Import): " + getEmailSubject());
           	
           	String mail = "";
           	for (Iterator<String> it = changeList.iterator(); it.hasNext(); ){
           		mail += (String) it.next() + "\n";
           	}
           	email.setText(mail);
           	
        	email.addRecipient(getManager().getEmailAddress(), getManager().getName());
           	
        	if (ApplicationProperty.EmailNotificationDataExchange.isTrue())
        		email.addNotifyCC();
           	
           	email.send();
		} catch (Exception e) {
			sLog.error(e.getMessage(), e);
		}
	}

	protected Session findSession(String academicInitiative, String academicYear, String academicTerm){ 		
		return getHibSession().
		createQuery("from Session as s where s.academicInitiative = :academicInititive and s.academicYear = :academicYear  and s.academicTerm = :academicTerm", Session.class).
		setParameter("academicInititive", academicInitiative).
		setParameter("academicYear", academicYear).
		setParameter("academicTerm", academicTerm).
		setCacheable(true).
		uniqueResult();
	}
	
	protected List<NonUniversityLocation> findNonUniversityLocationsWithIdOrName(String id, String name){
		if (id != null) {
			return getHibSession().
			createQuery("select distinct l from NonUniversityLocation as l where l.externalUniqueId=:id and l.session.uniqueId=:sessionId", NonUniversityLocation.class).
			setParameter("sessionId", session.getUniqueId()).
			setParameter("id", id).
			setCacheable(true).
			list();
		}
		if (name != null) {
			return getHibSession().
			createQuery("select distinct l from NonUniversityLocation as l where l.name=:name and l.session.uniqueId=:sessionId", NonUniversityLocation.class).
			setParameter("sessionId", session.getUniqueId()).
			setParameter("name", name).
			setCacheable(true).
			list();
		}
		return(new ArrayList());
	}
	
	protected List<NonUniversityLocation> findNonUniversityLocationsWithName(String name){
		if (name != null) {
			return getHibSession().
			createQuery("select distinct l from NonUniversityLocation as l where l.name=:name and l.session.uniqueId=:sessionId", NonUniversityLocation.class).
			setParameter("sessionId", session.getUniqueId()).
			setParameter("name", name).
			setCacheable(true).
			list();
		}
		return(new ArrayList());
	}

	
	protected class TimeObject {
		private Integer startPeriod;
		private Integer endPeriod;
		private Set<Integer> days;
		private String patternName = null;
		
		TimeObject(String startTime, String endTime, String daysOfWeek) throws Exception{
			this(startTime, endTime, daysOfWeek, null);
		}
		
		TimeObject(String startTime, String endTime, String daysOfWeek, String name) throws Exception{
		
			startPeriod = str2Slot(startTime);
			endPeriod = str2Slot(endTime);
			patternName = name;
			if (endPeriod == 0){
				// if the end period is midnight then the meeting ends at the end of the day i.e. last slot
				endPeriod = Constants.SLOTS_PER_DAY;
			}
			if (startPeriod >= endPeriod){
				throw new Exception("Invalid time '"+startTime+"' must be before ("+endTime+").");
			}
			if (daysOfWeek == null || daysOfWeek.length() == 0){
				return;
			}
			setDaysOfWeek(daysOfWeek);	
		}
		
		private void setDaysOfWeek(String daysOfWeek){
			days = new TreeSet<Integer>();
			String tmpDays = daysOfWeek;
			if(tmpDays.contains("Th")){
				days.add(Calendar.THURSDAY);
				tmpDays = tmpDays.replace("Th", "..");
			}
			if(tmpDays.contains("R")){
				days.add(Calendar.THURSDAY);
				tmpDays = tmpDays.replace("R", "..");
			}
			if (tmpDays.contains("Su")){
				days.add(Calendar.SUNDAY);
				tmpDays = tmpDays.replace("Su", "..");
			}
			if (tmpDays.contains("U")){
				days.add(Calendar.SUNDAY);
				tmpDays = tmpDays.replace("U", "..");
			}
			if (tmpDays.contains("M")){
				days.add(Calendar.MONDAY);
				tmpDays = tmpDays.replace("M", ".");
			}
			if (tmpDays.contains("T")){
				days.add(Calendar.TUESDAY);
				tmpDays = tmpDays.replace("T", ".");
			}
			if (tmpDays.contains("W")){
				days.add(Calendar.WEDNESDAY);
				tmpDays = tmpDays.replace("W", ".");
			}
			if (tmpDays.contains("F")){
				days.add(Calendar.FRIDAY);
				tmpDays = tmpDays.replace("F", ".");
			}
			if (tmpDays.contains("S")){
				days.add(Calendar.SATURDAY);
				tmpDays = tmpDays.replace("S", ".");
			}						
		}
		
		public int getDayCode() {
			if (days == null || days.isEmpty()) return 0;
			int dayCode = 0;
			if (days.contains(Calendar.MONDAY))
				dayCode += Constants.DAY_CODES[Constants.DAY_MON];
			if (days.contains(Calendar.TUESDAY))
				dayCode += Constants.DAY_CODES[Constants.DAY_TUE];
			if (days.contains(Calendar.WEDNESDAY))
				dayCode += Constants.DAY_CODES[Constants.DAY_WED];
			if (days.contains(Calendar.THURSDAY))
				dayCode += Constants.DAY_CODES[Constants.DAY_THU];
			if (days.contains(Calendar.FRIDAY))
				dayCode += Constants.DAY_CODES[Constants.DAY_FRI];
			if (days.contains(Calendar.SATURDAY))
				dayCode += Constants.DAY_CODES[Constants.DAY_SAT];
			if (days.contains(Calendar.SUNDAY))
				dayCode += Constants.DAY_CODES[Constants.DAY_SUN];
			return dayCode;
		}
		
		public Integer getStartPeriod() {
			return startPeriod;
		}
		public void setStartPeriod(Integer startPeriod) {
			this.startPeriod = startPeriod;
		}
		public Integer getEndPeriod() {
			return endPeriod;
		}
		public void setEndPeriod(Integer endPeriod) {
			this.endPeriod = endPeriod;
		}
		public Set<Integer> getDays() {
			return days;
		}
		public void setDays(Set<Integer> days) {
			this.days = days;
		}
		
		public Meeting asMeeting(){
			Meeting meeting = new Meeting();
			
			meeting.setClassCanOverride(Boolean.valueOf(true));
			meeting.setStartOffset(Integer.valueOf(0));
			meeting.setStartPeriod(this.getStartPeriod());
			meeting.setStopOffset(Integer.valueOf(0));
			meeting.setStopPeriod(this.getEndPeriod());
            meeting.setStatus(Meeting.Status.PENDING);

			return(meeting);
		}
		public Integer str2Slot(String timeString) throws Exception {
			
			int slot = -1;
			try {
				Date date = CalendarUtils.getDate(timeString, timeFormat);
				SimpleDateFormat df = new SimpleDateFormat("HHmm");
				int time = Integer.parseInt(df.format(date));
				int hour = time/100;
				int min = time%100;
				if (hour>=24)
					throw new Exception("Invalid time '"+timeString+"' -- hour ("+hour+") must be between 0 and 23.");
				if (min>=60)
					throw new Exception("Invalid time '"+timeString+"' -- minute ("+min+") must be between 0 and 59.");
				
				if ((min%Constants.SLOT_LENGTH_MIN)!=0){
					min = min - (min%Constants.SLOT_LENGTH_MIN);
					//throw new Exception("Invalid time '"+timeString+"' -- minute ("+min+") must be divisible by "+Constants.SLOT_LENGTH_MIN+".");
				}
				slot = (hour*60+min - Constants.FIRST_SLOT_TIME_MIN)/Constants.SLOT_LENGTH_MIN;
			} catch (NumberFormatException ex) {
				throw new Exception("Invalid time '"+timeString+"' -- not a number.");
			}
			if (slot<0)
				throw new Exception("Invalid time '"+timeString+"', did not meet format: " + timeFormat);
			return(slot);
		}
		
		public String getPatternName() { return patternName; }
		public void setPatternName(String patternName) { this.patternName = patternName; }
	}
}
