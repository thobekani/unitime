/*
 * UniTime 3.3 (University Timetabling Application)
 * Copyright (C) 2011, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
*/
package org.unitime.timetable.gwt.resources;

import com.google.gwt.i18n.client.Messages;

public interface GwtMessages extends Messages {
	
	@DefaultMessage("{0} Help")
	String pageHelp(String pageTitle);

	@DefaultMessage("Version {0} built on {1}")
	String pageVersion(String version, String buildDate);
	
	@DefaultMessage("&copy; 2008 - 2012 UniTime LLC,<br>distributed under GNU General Public License.")
	String pageCopyright();
	
	@DefaultMessage("Login is required to access this page.")
	String authenticationRequired();
	
	@DefaultMessage("Your timetabling session has expired. Please log in again.")
	String authenticationExpired();

	@DefaultMessage("Insufficient user privileges.")
	String authenticationInsufficient();
	
	@DefaultMessage("No academic session selected.")
	String authenticationNoSession();

	@DefaultMessage("Export in iCalendar format.")
	String exportICalendar();
	
	@DefaultMessage("Select All")
	String opSelectAll();

	@DefaultMessage("Clear Selection")
	String opClearSelection();
	
	@DefaultMessage("&#10007; Remove")
	String opDeleteSelectedMeetings();
	
	@DefaultMessage("&#10008 Remove All")
	String opDeleteNewMeetings();
	
	@DefaultMessage("Setup Times ...")
	String opChangeOffsets();
	
	@DefaultMessage("<b><i>+</i></b> Add Meetings ...")
	String opAddMeetings();

	@DefaultMessage("Sort by {0}")
	String opSortBy(String column);
	
	@DefaultMessage("&#9744; {0}")
	String opShow(String column);

	@DefaultMessage("&#9746; {0}")
	String opHide(String column);
	
	@DefaultMessage("&#10003; Approve ...")
	String opApproveSelectedMeetings();

	@DefaultMessage("&#10004; Approve All ...")
	String opApproveAllMeetings();

	@DefaultMessage("&#10007; Reject ...")
	String opRejectSelectedMeetings();

	@DefaultMessage("&#10008; Reject All ...")
	String opRejectAllMeetings();

	@DefaultMessage("<i>?</i> Inquire ...")
	String opInquireSelectedMeetings();

	@DefaultMessage("<b><i>?</i></b> Inquire ...")
	String opInquireAllMeetings();

	@DefaultMessage("Date")
	String colDate();

	@DefaultMessage("Published Time")
	String colPublishedTime();

	@DefaultMessage("Allocated Time")
	String colAllocatedTime();

	@DefaultMessage("Setup")
	String colSetupTimeShort();

	@DefaultMessage("Setup Time")
	String colSetupTime();

	@DefaultMessage("Teardown")
	String colTeardownTimeShort();

	@DefaultMessage("Teardown Time")
	String colTeardownTime();

	@DefaultMessage("Location")
	String colLocation();
	
	@DefaultMessage("Capacity")
	String colCapacity();

	@DefaultMessage("Approved")
	String colApproval();
	
	@DefaultMessage("Name")
	String colName();
	
	@DefaultMessage("Section")
	String colSection();
	
	@DefaultMessage("Type")
	String colType();
	
	@DefaultMessage("Instructor / Sponsor")
	String colSponsorOrInstructor();

	@DefaultMessage("Main Contact")
	String colMainContact();
	
	@DefaultMessage("Attend/Limit")
	String colLimit();

	@DefaultMessage("Enrollment")
	String colEnrollment();

	@DefaultMessage("Conflicts with {0}")
	String conflictWith(String event);
	
	@DefaultMessage("not approved")
	String approvalNotApproved();

	@DefaultMessage("not approved")
	String approvalNotApprovedPast();

	@DefaultMessage("new meeting")
	String approvalNewMeeting();
	
	@DefaultMessage("midnight")
	String timeMidnitgh();

	@DefaultMessage("noon")
	String timeNoon();
	
	@DefaultMessage("all day")
	String timeAllDay();
	
	@DefaultMessage("Setup / Teardown Times")
	String dlgChangeOffsets();
	
	@DefaultMessage("Setup Time:")
	String propSetupTime();

	@DefaultMessage("Teardown Time:")
	String propTeardownTime();
	
	@DefaultMessage("Academic Session:")
	String propAcademicSession();
	
	@DefaultMessage("Event Filter:")
	String propEventFilter();
	
	@DefaultMessage("Room Filter:")
	String propRoomFilter();
	
	@DefaultMessage("Resource Type:")
	String propResourceType();
	
	@DefaultMessage("Resource:")
	String propResource();
	
	@DefaultMessage("<u>O</u>k")
	String buttonOk();

	@DefaultMessage("<u>C</u>ancel")
	String buttonCancel();
	
	@DefaultMessage("<u>S</u>earch")
	String buttonSearch();
	
	@DefaultMessage("<u>L</u>ookup")
	String buttonLookup();
	
	@DefaultMessage("<u>A</u>dd Event")
	String buttonAddEvent();
	
	@DefaultMessage("<u>P</u>rint")
	String buttonPrint();
	
	@DefaultMessage("E<u>x</u>port")
	String buttonExportICal();
	
	@DefaultMessage("Filter")
	String sectFilter();
	
	@DefaultMessage("{0} timetable for {1}")
	String sectTimetable(String resource, String session);

	@DefaultMessage("{0} events for {1}")
	String sectEvents(String resource, String session);
	
	@DefaultMessage("Loading {0}...")
	String waitLoading(String name);
	
	@DefaultMessage("Loading {0} timetable for {1} ...")
	String waitLoadingTimetable(String name, String session);
	
	@DefaultMessage("Failed to load {0}: {1}")
	String failedLoad(String name, String reason);
	
	@DefaultMessage("No events found for {0} in {1}.")
	String failedNoEvents(String name, String session);
	
	@DefaultMessage("There are more than {0} meetings matching the filter. Only {0} meetings are loaded.")
	String warnTooManyMeetings(int maximum);
	
	@DefaultMessage("Room")
	String resourceRoom();
	
	@DefaultMessage("All Rooms")
	String allRooms();
	
	@DefaultMessage("<u>T</u>imetable")
	String tabGrid();
	
	@DefaultMessage("<u>L</u>ist of Events")
	String tabTable();
}