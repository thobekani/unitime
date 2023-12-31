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

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Element;
import org.hibernate.FlushMode;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.interfaces.ExternalUidLookup;
import org.unitime.timetable.interfaces.ExternalUidLookup.UserInfo;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseEvent;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.Event;
import org.unitime.timetable.model.EventContact;
import org.unitime.timetable.model.EventNote;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.Meeting;
import org.unitime.timetable.model.NonUniversityLocation;
import org.unitime.timetable.model.RelatedCourseInfo;
import org.unitime.timetable.model.Room;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.SpecialEvent;
import org.unitime.timetable.model.SponsoringOrganization;
import org.unitime.timetable.util.CalendarUtils;

/**
 * @author Stephanie Schluttenhofer, Tomas Muller
 *
 */
public class EventImport extends EventRelatedImports {
	private static final String specialEventElementName = "specialEvent";
	private static final String courseRelatedEventElementName = "courseRelatedEvent";
	private static final String meetingElementName = "meeting";
	private static final String noteElementName = "note";
	private static final String eventContactElementName = "eventContact";
	private static final String sponsoringOrgElementName = "sponsoringOrganization";
	private static final String courseElementName = "course";
	private String academicInitiative = null;
	
	private ExternalUidLookup iLookup = null;

	/**
	 * 
	 */
	public EventImport() {
        String className = ApplicationProperty.InstructorExternalIdLookupClass.value();
        if (className != null) {
	        try {
	        	iLookup = (ExternalUidLookup)Class.forName(className).getDeclaredConstructor().newInstance();
	        } catch (Exception e) {
	        	warn("Unable to instantiate external id lookup: " + e.getMessage(), e);
	        }
        }
	}

	/* (non-Javadoc)
	 * @see org.unitime.timetable.dataexchange.BaseImport#loadXml(org.dom4j.Element)
	 */
	@Override
	public void loadXml(Element rootElement) throws Exception {
		trimLeadingZerosFromExternalId = ApplicationProperty.DataExchangeTrimLeadingZerosFromExternalIds.isTrue();
		int loadedCount = 0;
		int recordCount = 0;
		try {
			String rootElementName = "events";
	        if (!rootElement.getName().equalsIgnoreCase(rootElementName)) {
	        	throw new Exception("Given XML file is not a Events load file.");
	        }
	        academicInitiative = getRequiredStringAttribute(rootElement, "academicInitiative", rootElementName);
	        dateFormat = getOptionalStringAttribute(rootElement, "dateFormat");
	        timeFormat = getOptionalStringAttribute(rootElement, "timeFormat");
	        String created = getOptionalStringAttribute(rootElement, "created");
	        if(timeFormat == null){
	        	timeFormat = "HHmm";
	        }
	        
	        beginTransaction();
	        
            String year   = rootElement.attributeValue("year");
            String term   = rootElement.attributeValue("term");
            String campus   = rootElement.attributeValue("campus", academicInitiative);
            if (campus != null && year != null && term != null) {
            	session = Session.getSessionUsingInitiativeYearTerm(campus, year, term);
            }
	
	        if (session == null){
	        	// Use the session for the academicInitiative that is effective for events now as the session to use for logging changes
	        	session = findDefaultSession(academicInitiative, new Date());
	        }
	        if (created != null) {
		        addNote("Loading Events XML file created on: " + created);
				ChangeLog.addChange(getHibSession(), getManager(), session, session, created, ChangeLog.Source.DATA_IMPORT_EVENTS, ChangeLog.Operation.CREATE, null, null);
				updateChangeList(true);
	        }
	        for ( Iterator<?> it = rootElement.elementIterator(); it.hasNext(); ) {
	            Element element = (Element) it.next();
	            Event event = null;
	            if(!element.getName().equalsIgnoreCase(specialEventElementName) && !element.getName().equalsIgnoreCase(courseRelatedEventElementName) ){
	            	throw new Exception("Expecting to find a '" + specialEventElementName + "' or a '" + courseRelatedEventElementName + "' at this level, instead found '" + element.getName() + "'.");
	            }
	            if (element.getName().equalsIgnoreCase(specialEventElementName)){            	
	            	event = elementSpecialEvent(element);
	            } else if (element.getName().equalsIgnoreCase(courseRelatedEventElementName)){            	
	            	event = elementCourseRelatedEvent(element);
	            }
	            
	            if (event != null){
	            	loadedCount++;
	            }
	            recordCount++;
	        	flushIfNeeded(true);
	        	updateChangeList(true);
	        }
	        commitTransaction();
		} catch (Exception e) {
			fatal("Exception: " + e.getMessage(), e);
			rollbackTransaction();
			throw e;
		}	
		addNote("Events Added: " + loadedCount + " of " + recordCount + " possible events.");
		updateChangeList(true);
		reportMissingLocations();
		mailLoadResults();
	}
	
	private Event elementSpecialEvent(Element specialEventElement) throws Exception {
		if (!specialEventElement.getName().equalsIgnoreCase(specialEventElementName)){
			addNote("Not Loading " + specialEventElement.getName() + " Error:  attempted to load as " + specialEventElementName);
			addNote("\t " + specialEventElement.getText());
			return(null);
        }			
		String eventName = null;
		try {
			eventName = getRequiredStringAttribute(specialEventElement, "eventName", specialEventElementName);					
		} catch (Exception e) {
			addNote("Not Loading " + specialEventElementName + " Error:  " + e.getMessage());
			addNote("\t " + specialEventElement.asXML());
			return(null);
		}
		Integer minCapacity = getOptionalIntegerAttribute(specialEventElement, "minCapacity");
		Integer maxCapacity = getOptionalIntegerAttribute(specialEventElement, "maxCapacity");
		String email = getOptionalStringAttribute(specialEventElement, "email");
		
		SpecialEvent event = new SpecialEvent();
		event.setEventName(eventName);
		event.setMinCapacity(minCapacity);
		event.setMaxCapacity(maxCapacity);
		event.setEmail(email);

		try {

			elementMeetings(specialEventElement, event);

			Element eventContactElement = specialEventElement.element(eventContactElementName);
			if (eventContactElement != null){
				event.setMainContact(elementEventContact(eventContactElement));
			} 

			elementAdditionalEventContacts(specialEventElement, event);

			Element sponsoringOrgElement = specialEventElement.element(sponsoringOrgElementName);
			if (sponsoringOrgElement != null){
				elementSponsoringOrganization(sponsoringOrgElement, event);
			}		

			elementNotes(specialEventElement, event);

		} catch (Exception e) {
			addNote("Not Loading " + specialEventElementName + " Error:  " + e.getMessage());
			addNote("\t " + specialEventElement.asXML());
			return(null);
		}
		getHibSession().persist(event);
		return(event);
	}	

	private Event elementCourseRelatedEvent(Element specialEventElement) throws Exception {
		if (!specialEventElement.getName().equalsIgnoreCase(courseRelatedEventElementName)){
			addNote("Not Loading " + specialEventElement.getName() + " Error:  attempted to load as " + courseRelatedEventElementName);
			addNote("\t " + specialEventElement.getText());
			return(null);
        }			
		String eventName = null;
		try {
			eventName = getRequiredStringAttribute(specialEventElement, "eventName", courseRelatedEventElementName);					
		} catch (Exception e) {
			addNote("Not Loading " + courseRelatedEventElementName + " Error:  " + e.getMessage());
			addNote("\t " + specialEventElement.asXML());
			return(null);
		}
		Integer minCapacity = getOptionalIntegerAttribute(specialEventElement, "minCapacity");
		Integer maxCapacity = getOptionalIntegerAttribute(specialEventElement, "maxCapacity");
		String email = getOptionalStringAttribute(specialEventElement, "email");
		Boolean requiredAttendance = getOptionalBooleanAttribute(specialEventElement, "attendanceRequired");
		if (requiredAttendance == null){
			requiredAttendance = Boolean.valueOf(false);
		}
		
		CourseEvent event = new CourseEvent();
		event.setEventName(eventName);
		event.setMinCapacity(minCapacity);
		event.setMaxCapacity(maxCapacity);
		event.setEmail(email);
		event.setReqAttendance(requiredAttendance);

		try {

			elementRelatedCourses(specialEventElement, event);

			elementMeetings(specialEventElement, event);

			Element eventContactElement = specialEventElement.element(eventContactElementName);
			if (eventContactElement != null){
				event.setMainContact(elementEventContact(eventContactElement));
			} 

			elementAdditionalEventContacts(specialEventElement, event);

			Element sponsoringOrgElement = specialEventElement.element(sponsoringOrgElementName);
			if (sponsoringOrgElement != null){
				elementSponsoringOrganization(sponsoringOrgElement, event);
			}		

			elementNotes(specialEventElement, event);

		} catch (Exception e) {
			addNote("Not Loading " + courseRelatedEventElementName + " Error:  " + e.getMessage());
			addNote("\t " + specialEventElement.asXML());
			return(null);
		}
		getHibSession().persist(event);
		return(event);
	}	

	private void elementMeetings(Element element, Event event) throws Exception {
		String meetingsElementName = "meetings";
        for ( Iterator<?> it = element.elementIterator(meetingsElementName); it.hasNext(); ) {
            Element meetingsElement = (Element) it.next();
            for( Iterator<?> meetingIt = meetingsElement.elementIterator(meetingElementName); meetingIt.hasNext(); ){
            	elementMeeting((Element)meetingIt.next(), event);
            }    
         }
        if (event.getMeetings() == null || event.getMeetings().size() == 0){
        	throw(new Exception(meetingsElementName + " element must contain at least one " + meetingElementName + " element"));
        }
	}
	
	private void elementMeeting(Element meetingElement, Event event) throws Exception {
		if (!meetingElement.getName().equalsIgnoreCase(meetingElementName)){
			throw(new Exception("Not Loading " + meetingElement.getName() + " Error:  attempted to load as " + meetingElementName));
		}			

		String meetingDateStr = getRequiredStringAttribute(meetingElement, "meetingDate", meetingElementName);
		String startTimeStr = getRequiredStringAttribute(meetingElement, "startTime", meetingElementName);
		String endTimeStr = getRequiredStringAttribute(meetingElement, "endTime", meetingElementName);
		
		Integer startOffset = getOptionalIntegerAttribute(meetingElement, "startOffset");
		Integer endOffset = getOptionalIntegerAttribute(meetingElement, "stopOffset");
		String buildingAbbv = getOptionalStringAttribute(meetingElement, "buildingAbbv");
		String roomNumber = getOptionalStringAttribute(meetingElement, "roomNumber");
		String location = getOptionalStringAttribute(meetingElement, "location");
		
		Boolean classCanOverride = getOptionalBooleanAttribute(meetingElement, "callCanOverride");
		String approvedDateStr = getOptionalStringAttribute(meetingElement, "approvedDateTime");
		
		if (classCanOverride == null){
			classCanOverride = Boolean.valueOf(true);
		}
		
		TimeObject timeObj = new TimeObject(startTimeStr, endTimeStr, null);
		
		Meeting meeting = timeObj.asMeeting();
		meeting.setStartOffset(startOffset);
		meeting.setStopOffset(endOffset);
		
		meeting.setLocationPermanentId(findMeetingLocationPermId(buildingAbbv, roomNumber, location));
		if (meeting.getLocationPermanentId() == null){
			throw(new Exception("Not Loading " + meetingElement.getName() + " Error:  meeting location not found - " + (buildingAbbv != null?buildingAbbv + (roomNumber != null?" " + roomNumber:""):(location != null?location:""))));			
		}

		meeting.setClassCanOverride(classCanOverride);
		
		meeting.setMeetingDate(CalendarUtils.getDate(meetingDateStr, dateFormat));
		if (meeting.getMeetingDate() == null){
			throw(new Exception("Not Loading " + meetingElement.getName() + " Error:  meeting date must not be null"));	
		}
		
		if (approvedDateStr != null){
			Date approvedDate = CalendarUtils.getDate(approvedDateStr, dateFormat + " " + timeFormat);
			if (approvedDate == null){
				approvedDate = CalendarUtils.getDate(approvedDateStr, dateFormat);
			}
			meeting.setStatus(Meeting.Status.APPROVED);
			meeting.setApprovalDate(approvedDate);
		}

		meeting.setEvent(event);
		event.addToMeetings(meeting);
	
	}
	private void elementRelatedCourses(Element element, CourseEvent event) throws Exception {
		String relatedCoursesElementName = "relatedCourses";
        for ( Iterator<?> it = element.elementIterator(relatedCoursesElementName); it.hasNext(); ) {
            Element relatedCoursesElement = (Element) it.next();
            for( Iterator<?> courseIt = relatedCoursesElement.elementIterator(courseElementName); courseIt.hasNext(); ){
            	elementCourse((Element)courseIt.next(), event);
            }    
         }
//        if (event.getMeetings() == null || event.getMeetings().size() == 0){
//        	throw(new Exception(relatedCoursesElementName + " element must contain at least one " + meetingElementName + " element"));
//        }
	}
	
	private void elementCourse(Element courseElement, CourseEvent courseEvent) throws Exception {
		if (!courseElement.getName().equalsIgnoreCase(courseElementName)){
			throw(new Exception("Not Loading " + courseElement.getName() + " Error:  attempted to load as " + courseElementName));
		}			

		String relatedExternalIdStr = getRequiredStringAttribute(courseElement, "relatedExternalId", courseElementName);
		String relationshipType = getRequiredStringAttribute(courseElement, "relationshipType", courseElementName);
		String term = courseElement.attributeValue("term", session.getAcademicTerm());
		String year = courseElement.attributeValue("year", session.getAcademicYear());
		
		String courseExternalIdStr = getOptionalStringAttribute(courseElement, "courseExternalId");
		
		CourseOffering courseOffering = null;
		if (courseExternalIdStr != null){
			courseOffering = findCourseOfferingFromExternalId(courseExternalIdStr, year, term, academicInitiative);
		}
		
		RelatedCourseInfo rci = new RelatedCourseInfo();
		if (relationshipType.equalsIgnoreCase("class")){
			Class_ c = findClassFromExternalId(relatedExternalIdStr, year, term, academicInitiative);
			if (c == null){
				throw(new Exception("Not Loading " + courseElement.getName() + " Error: unable to find a class with an externalId of " + relatedExternalIdStr));			
			}			
			rci.setOwner(c);
		    if (courseOffering == null){
		    	rci.setCourse(c.getSchedulingSubpart().getControllingCourseOffering());
		    } else {
		    	rci.setCourse(courseOffering);
		    }
		} else if (relationshipType.equalsIgnoreCase("instructionalOffering")){
			InstructionalOffering io = findInstructionalOfferingFromExternalId(relatedExternalIdStr, year, term, academicInitiative);
			if (io == null){
				throw(new Exception("Not Loading " + courseElement.getName() + " Error: unable to find a instructional offering with an externalId of " + relatedExternalIdStr));			
			}			
			rci.setOwner(io);
		    if (courseOffering == null){
		    	rci.setCourse(io.getControllingCourseOffering());
		    } else {
		    	rci.setCourse(courseOffering);
		    }		
		} else if (relationshipType.equalsIgnoreCase("courseOffering")){
			CourseOffering co = findCourseOfferingFromExternalId(relatedExternalIdStr, year, term, academicInitiative);
			if (co == null){
				throw(new Exception("Not Loading " + courseElement.getName() + " Error: unable to find a course offering with an externalId of " + relatedExternalIdStr));			
			}			
			rci.setOwner(co);
		    rci.setCourse(co);
		}
		rci.setEvent(courseEvent);
		courseEvent.addToRelatedCourses(rci);
	}

	private EventContact elementEventContact(Element eventContactElement) throws Exception {
		if (!eventContactElement.getName().equalsIgnoreCase(eventContactElementName)){
			throw(new Exception("Not Loading " + eventContactElement.getName() + " Error:  attempted to load as " + eventContactElementName));
		}			
		
		String externalId = getRequiredStringAttribute(eventContactElement, "externalId", eventContactElementName);		
		String firstName = getOptionalStringAttribute(eventContactElement, "firstName");
		String middleName = getOptionalStringAttribute(eventContactElement, "middleName");
		String lastName  = getRequiredStringAttribute(eventContactElement, "lastName", eventContactElementName);
		String acadTitle = getOptionalStringAttribute(eventContactElement, "acadTitle");
		String email = getOptionalStringAttribute(eventContactElement, "email");
		String phone = getOptionalStringAttribute(eventContactElement, "phone");
		try {
		   Long numId = Long.valueOf(externalId);
		   if (numId != null && numId.longValue() > 0){
			   externalId = numId.toString();
		   }
		} catch (Exception e) {
		  //  leave the externalId alone
		}
		EventContact ec = EventContact.findByExternalUniqueId(externalId);
		if (ec == null) {
			UserInfo user = null;
			try {
				if (iLookup != null)
					user = iLookup.doLookup(externalId);
			} catch (Exception e) {
				warn("Failed to lookup " + externalId + ": " + e.getMessage(), e);
			}
			if (user != null) {
				if (email == null)
					email = user.getEmail();
				if (firstName == null)
					firstName = user.getFirstName();
				if (middleName == null)
					middleName = user.getMiddleName();
				if (lastName == null)
					lastName = user.getLastName();
				if (phone == null)
					phone = user.getLastName();
				if (acadTitle == null)
					acadTitle = user.getAcademicTitle();
			}
			ec = new EventContact();
			ec.setFirstName(firstName);
			ec.setMiddleName(middleName);
			ec.setLastName(lastName);
			ec.setAcademicTitle(acadTitle);
			ec.setEmailAddress(email);
			ec.setPhone(phone);
			ec.setExternalUniqueId(externalId);
			getHibSession().persist(ec);
			flush(true);
		}
		return(ec);
		
	}
	
	/*
	 *  The additional event contacts relationship to an event is not currently supported in the user interface so the
	 *  ability to load addition contacts through the xml interface is currently disabled.
	 */
	
	private void elementAdditionalEventContacts(Element element, Event event) throws Exception {
		String additionalContactsElementName = "additionalEventContacts";
		for ( Iterator<?> it = element.elementIterator(additionalContactsElementName); it.hasNext(); ) {
			Element additionalContactsElement = (Element) it.next();
	        EventContact ec = null;
	        for( Iterator<?> eventContactIt = additionalContactsElement.elementIterator(eventContactElementName); eventContactIt.hasNext(); ){
	        	ec = elementEventContact((Element)eventContactIt.next());
	        	if (ec != null){
	        		event.getAdditionalContacts().add(ec);
	        	}
	        }    
	     }
	}

	private void elementSponsoringOrganization(Element sponsoringOrgElement, Event event) throws Exception {
		if (!sponsoringOrgElement.getName().equalsIgnoreCase(sponsoringOrgElementName)){
			throw(new Exception("Not Loading " + sponsoringOrgElement.getName() + " Error:  attempted to load as " + sponsoringOrgElementName));
		}			
		String name = getRequiredStringAttribute(sponsoringOrgElement, "name", sponsoringOrgElementName);
		
		if (name != null){
			SponsoringOrganization sponsoringOrg = findSponsoringOrg(name);
			if (sponsoringOrg != null){
				event.setSponsoringOrganization(sponsoringOrg);
			} else {
				throw(new Exception(sponsoringOrgElementName + " element matching org not found:  " + name));
			}
		}
	}
	
	private void elementNotes(Element element, Event event) throws Exception {
		String notesElementName = "notes";
        for ( Iterator<?> it = element.elementIterator(notesElementName); it.hasNext(); ) {
            Element notesElement = (Element) it.next();
            for( Iterator<?> noteIt = notesElement.elementIterator(noteElementName); noteIt.hasNext(); ){
            	elementNote((Element)noteIt.next(), event);
            }    
         }
 	}

	private void elementNote(Element noteElement, Event event) throws Exception {
		if (!noteElement.getName().equalsIgnoreCase(noteElementName)){
			throw(new Exception("Not Loading " + noteElement.getName() + " Error:  attempted to load as " + noteElementName));
		}			

		String noteText = getRequiredStringAttribute(noteElement, "noteText", noteElementName);
		String noteTypeStr = getRequiredStringAttribute(noteElement, "noteType", noteElementName);
		String noteTimestampStr = getOptionalStringAttribute(noteElement, "timestamp");
		String userName = getOptionalStringAttribute(noteElement, "userName");
		String userId = getOptionalStringAttribute(noteElement, "userId");
		
		
		EventNote note = new EventNote();
		note.setTextNote(noteText);
		if(noteTypeStr.equalsIgnoreCase("create")){
			note.setNoteType(EventNote.sEventNoteTypeCreateEvent);
		} else if (noteTypeStr.equalsIgnoreCase("update")){
			note.setNoteType(EventNote.sEventNoteTypeAddMeetings);
		} else if (noteTypeStr.equalsIgnoreCase("approve")){
			note.setNoteType(EventNote.sEventNoteTypeApproval);
		} else if (noteTypeStr.equalsIgnoreCase("reject")){
			note.setNoteType(EventNote.sEventNoteTypeRejection);
		} else if (noteTypeStr.equalsIgnoreCase("delete")){
			note.setNoteType(EventNote.sEventNoteTypeDeletion);
		} else if (noteTypeStr.equalsIgnoreCase("edit")){
			note.setNoteType(EventNote.sEventNoteTypeEditEvent);
		}
		
		if (noteTimestampStr == null){
			note.setTimeStamp(new Date());
		} else {
			note.setTimeStamp(CalendarUtils.getDate(noteTimestampStr, dateFormat + " " + timeFormat));
		}
		
		note.setUser(userName);
		note.setUserId(userId);
		
		note.setEvent(event);
		event.addToNotes(note);
	}
	
	@Override
	protected String getEmailSubject() {
		return("Event Import Results");
	}
	
	private Session findDefaultSession(String academicInitiative, Date aDate){
		return getHibSession().
		createQuery("from Session as s where s.academicInitiative = :academicInititive and s.eventBeginDate <= :aDate  and s.eventEndDate >= :aDate", Session.class).
		setParameter("academicInititive", academicInitiative).
		setParameter("aDate", aDate).
		setCacheable(true).
		uniqueResult();
	}

	private Room findRoom(String buildingAbbv, String roomNumber){
		if (buildingAbbv == null || roomNumber == null) return null;
		Room room = null;
		if (session != null) {
			room = getHibSession().createQuery(
					"from Room r where r.roomNumber=:roomNbr and r.building.abbreviation = :building and r.session.uniqueId = :sessionId", Room.class)
					.setParameter("sessionId", session.getUniqueId())
					.setParameter("building", buildingAbbv)
					.setParameter("roomNbr", roomNumber)
					.setCacheable(true)
					.setMaxResults(1)
					.setCacheable(true)
					.uniqueResult();
		}
		if (room == null) {
			List<Room> rooms =  getHibSession().
			createQuery("select distinct r from Room as r where r.roomNumber=:roomNbr and r.building.abbreviation = :building", Room.class).
			setParameter("building", buildingAbbv).
			setParameter("roomNbr", roomNumber).
			setCacheable(true).
			setHibernateFlushMode(FlushMode.MANUAL).
			list();
			if (rooms != null && rooms.size() > 0){
				room = rooms.iterator().next();
			}
		}
		return(room);
	}

	private SponsoringOrganization findSponsoringOrg(String name){
		SponsoringOrganization sponsoringOrg = null;
		if (sponsoringOrg == null) {
			sponsoringOrg = getHibSession().
			createQuery("select distinct so from SponsoringOrganization as so where so.name = :name", SponsoringOrganization.class).
			setParameter("name", name).
			setCacheable(true).
			setHibernateFlushMode(FlushMode.MANUAL).
			uniqueResult();
		}
		return(sponsoringOrg);
	}

	private NonUniversityLocation findLocation(String name){
		List locations = findNonUniversityLocationsWithName(name);
		if (locations == null || locations.size() > 0){
			return((NonUniversityLocation) locations.iterator().next());
		} else {
			return(null);
		}
	}
	
	private Long findMeetingLocationPermId(String buildingAbbv, String roomNumber, String location){
		Room room = findRoom(buildingAbbv, roomNumber);
		if (room != null){
			return(room.getPermanentId());
		}
		NonUniversityLocation nonUnivLocation = findLocation(location);
		if (nonUnivLocation != null){
			return(nonUnivLocation.getPermanentId());
		}
		return(null);
	}
	
	private Class_ findClassFromExternalId(String externalId, String year, String term, String academicInitiative){
		if (externalId == null || year == null || term == null || academicInitiative == null){
			return(null);
		}

		return getHibSession().
		createQuery("select c from Class_ as c inner join c.schedulingSubpart.instrOfferingConfig.instructionalOffering.session as s where s.academicInitiative = :academicInititive and s.academicYear = :aYear and s.academicTerm = :aTerm and c.externalUniqueId = :anExternalId", Class_.class).
		setParameter("academicInititive", academicInitiative).
		setParameter("aYear", year).
		setParameter("aTerm", term).
		setParameter("anExternalId", externalId).
		setCacheable(true).
		setMaxResults(1).
		uniqueResult();

	}
	
	private InstructionalOffering findInstructionalOfferingFromExternalId(String externalId, String year, String term, String academicInitiative){
		if (externalId == null || year == null || term == null || academicInitiative == null){
			return(null);
		}

		return getHibSession().
		createQuery("select io from InstructionalOffering as io inner join io.session as s where s.academicInitiative = :academicInititive and s.academicYear = :aYear and s.academicTerm = :aTerm and io.externalUniqueId = :anExternalId", InstructionalOffering.class).
		setParameter("academicInititive", academicInitiative).
		setParameter("aYear", year).
		setParameter("aTerm", term).
		setParameter("anExternalId", externalId).
		setCacheable(true).
		setMaxResults(1).
		uniqueResult();

	}
	
	private CourseOffering findCourseOfferingFromExternalId(String externalId, String year, String term, String academicInitiative){
		if (externalId == null || year == null || term == null || academicInitiative == null){
			return(null);
		}

		return getHibSession().
		createQuery("select co from CourseOffering as co inner join co.instructionalOffering.session as s where s.academicInitiative = :academicInititive and s.academicYear = :aYear and s.academicTerm = :aTerm and co.externalUniqueId = :anExternalId", CourseOffering.class).
		setParameter("academicInititive", academicInitiative).
		setParameter("aYear", year).
		setParameter("aTerm", term).
		setParameter("anExternalId", externalId).
		setCacheable(true).
		setMaxResults(1).
		uniqueResult();

	}

	
	
}
