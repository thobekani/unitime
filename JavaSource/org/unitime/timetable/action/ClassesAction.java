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
package org.unitime.timetable.action;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.tiles.annotation.TilesDefinition;
import org.apache.struts2.tiles.annotation.TilesPutAttribute;
import org.cpsolver.coursett.model.TimeLocation;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.unitime.commons.MultiComparable;
import org.unitime.commons.web.WebTable;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.form.ClassesForm;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.Meeting;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.dao.Class_DAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.UserAuthority;
import org.unitime.timetable.security.UserContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.spring.SpringApplicationContextHolder;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.Formats;
import org.unitime.timetable.util.LoginManager;
import org.unitime.timetable.webutil.PdfWebTable;

/** 
 * @author Tomas Muller, Stephanie Schluttenhofer
 */
@Action(value = "classes", results = {
		@Result(name = "show", type = "tiles", location = "classes.tiles"),
		@Result(name = "personal", type = "redirect", location = "/personalSchedule.action")
	})
@TilesDefinition(name = "classes.tiles", extend = "baseLayout", putAttributes =  {
		@TilesPutAttribute(name = "title", value = "Class Schedule"),
		@TilesPutAttribute(name = "body", value = "/user/classes.jsp"),
		@TilesPutAttribute(name = "checkLogin", value = "false"),
		@TilesPutAttribute(name = "checkRole", value = "false")
	})
public class ClassesAction extends UniTimeAction<ClassesForm> {
	private static final long serialVersionUID = -9496906356490277L;
	protected static GwtConstants CONSTANTS = Localization.create(GwtConstants.class);
	protected final static CourseMessages MSG = Localization.create(CourseMessages.class);
	
	private String year, term, campus, subject, select, course;
	
	public String getYear() { return year; }
	public void setYear(String year) { this.year = year; }
	public String getTerm() { return term; }
	public void setTerm(String term) { this.term = term; }
	public String getCampus() { return campus; }
	public void setCampus(String campus) { this.campus = campus; }
	public String getSubject() { return  subject; }
	public void setSubject(String subject) { this.subject = subject; }
	public String getSelect() { return select; }
	public void setSelect(String select) { this.select = select; }
	public String getCourse() { return course; }
	public void setCourse(String course) { this.course = course; }
	
	public String execute() throws Exception {
		if (form == null) {
	    	form = new ClassesForm();
	    	form.reset();
	    }
		
    	if (form.getOp() != null) op = form.getOp();
        
        if (subject != null || subject != null) {
            form.load(request.getSession());
            if (subject != null) {
                form.setSubjectArea(subject);
            } else {
                if (form.canDisplayAllSubjectsAtOnce()){
            		form.setSubjectArea("--ALL--");
            	}
            }
            if (year!=null && term!=null && campus!=null) {
                Session session = Session.getSessionUsingInitiativeYearTerm(campus, year, term);
                if (session!=null) form.setSession(session.getUniqueId());
            } 
            if (course != null) {
                form.setCourseNumber(course);
            } else {
                form.setCourseNumber(null);
            }
            op = MSG.buttonApply();
        }
        
        if ("Change".equals(op)) {
        	form.save(request.getSession());
        }

        if (MSG.buttonLogIn().equals(op)) {
        	if (form.getUsername()!=null && form.getUsername().length()>0 && form.getPassword()!=null && form.getPassword().length()>0) {
            	try {
            		Authentication authRequest = new UsernamePasswordAuthenticationToken(form.getUsername(), form.getPassword());
            		Authentication authResult = getAuthenticationManager().authenticate(authRequest);
            		SecurityContextHolder.getContext().setAuthentication(authResult);
            		UserContext user = (UserContext)authResult.getPrincipal();
            		if (user.getCurrentAuthority() == null || !user.getCurrentAuthority().hasRight(Right.PersonalSchedule))
            			for (UserAuthority auth: user.getAuthorities()) {
            				if (auth.getAcademicSession() != null && auth.getAcademicSession().getQualifierId().equals(form.getSession()) && auth.hasRight(Right.PersonalSchedule)) {
            					user.setCurrentAuthority(auth); break;
            				}
            			}
            		request.getSession().setAttribute("loginPage", "classes");
            		LoginManager.loginSuceeded(authResult.getName());
            		if (user.getCurrentAuthority() == null) {
            			response.sendRedirect("selectPrimaryRole.action");
            			return null;
            		}
            		return "personal";
            	} catch (Exception e) {
            		form.setMessage("Authentication failed: " + e.getMessage());
            		LoginManager.addFailedLoginAttempt(form.getUsername(), new Date());
            	}
            }
        	op = MSG.buttonApply();
        }

        if (MSG.buttonApply().equals(op)) {
            form.save(request.getSession());
        }
        form.load(request.getSession());
        
        WebTable.setOrder(sessionContext,"classes.order",request.getParameter("ord"),1);
        
        if (form.getSession()!=null && form.getSubjectArea()!=null && form.getSubjectArea().length()>0) {
            org.unitime.timetable.model.Session session = SessionDAO.getInstance().get(form.getSession());
            if (session.getStatusType().canNoRoleReportClass()) {
                List classes = null;
                SubjectArea sa = null;
                if ("--ALL--".equals(form.getSubjectArea())) 
                    classes = Class_.findAll(form.getSession());
                else {
                    sa = SubjectArea.findByAbbv(form.getSession(), form.getSubjectArea());
                    if (sa!=null) {
                    	if (ApplicationProperty.CourseOfferingTitleSearch.isTrue() && form.getCourseNumber() != null && form.getCourseNumber().length() > 2) {
                    		classes = Class_DAO.getInstance().getSession().createQuery(
                                    "select distinct c from Class_ c inner join c.schedulingSubpart.instrOfferingConfig.instructionalOffering io inner join io.courseOfferings co where " +
                                    "c.schedulingSubpart.instrOfferingConfig.instructionalOffering.session.uniqueId=:sessionId and "+
                                    "co.subjectArea.uniqueId=:subjectAreaId and (co.courseNbr like :courseNbr or lower(co.title) like ('%' || lower(:courseNbr) || '%'))",
                                    Class_.class).
                            setParameter("sessionId", form.getSession()).
                            setParameter("subjectAreaId", sa.getUniqueId()).
                            setParameter("courseNbr", form.getCourseNumber().replaceAll("\\*", "%")).
                            setCacheable(true).list();
                		} else if (form.getCourseNumber()!=null && form.getCourseNumber().length()>0) {
                            classes = Class_DAO.getInstance().getSession().createQuery(
                                    "select distinct c from Class_ c inner join c.schedulingSubpart.instrOfferingConfig.instructionalOffering io inner join io.courseOfferings co where " +
                                    "c.schedulingSubpart.instrOfferingConfig.instructionalOffering.session.uniqueId=:sessionId and "+
                                    "co.subjectArea.uniqueId=:subjectAreaId and co.courseNbr like :courseNbr",
                                    Class_.class).
                            setParameter("sessionId", form.getSession()).
                            setParameter("subjectAreaId", sa.getUniqueId()).
                            setParameter("courseNbr", form.getCourseNumber().replaceAll("\\*", "%")).
                            setCacheable(true).list();
                        } else {
                            classes = Class_DAO.getInstance().getSession().createQuery(
                                    "select distinct c from Class_ c inner join c.schedulingSubpart.instrOfferingConfig.instructionalOffering io inner join io.courseOfferings co where " +
                                    "c.schedulingSubpart.instrOfferingConfig.instructionalOffering.session.uniqueId=:sessionId and "+
                                    "co.subjectArea.uniqueId=:subjectAreaId",
                                    Class_.class).
                            setParameter("sessionId", form.getSession()).
                            setParameter("subjectAreaId", sa.getUniqueId()).
                            setCacheable(true).list();
                        }
                    }
                }
                if (classes!=null && !classes.isEmpty()) {
                    int ord = WebTable.getOrder(sessionContext,"classes.order");
                    PdfWebTable table = getTable(true, form, classes, ord);
                    if (table!=null)
                        form.setTable(table.printTable(ord), table.getNrColumns(), table.getLines().size());
                }
            }
        }
        
        return "show";
	}
	
    public int getDaysCode(Set meetings) {
        int daysCode = 0;
        for (Iterator i=meetings.iterator();i.hasNext();) {
            Meeting meeting = (Meeting)i.next();
            Calendar date = Calendar.getInstance(Locale.US);
            date.setTime(meeting.getMeetingDate());
            switch (date.get(Calendar.DAY_OF_WEEK)) {
            case Calendar.MONDAY : daysCode |= Constants.DAY_CODES[Constants.DAY_MON]; break;
            case Calendar.TUESDAY : daysCode |= Constants.DAY_CODES[Constants.DAY_TUE]; break;
            case Calendar.WEDNESDAY : daysCode |= Constants.DAY_CODES[Constants.DAY_WED]; break;
            case Calendar.THURSDAY : daysCode |= Constants.DAY_CODES[Constants.DAY_THU]; break;
            case Calendar.FRIDAY : daysCode |= Constants.DAY_CODES[Constants.DAY_FRI]; break;
            case Calendar.SATURDAY : daysCode |= Constants.DAY_CODES[Constants.DAY_SAT]; break;
            case Calendar.SUNDAY : daysCode |= Constants.DAY_CODES[Constants.DAY_SUN]; break;
            }
        }
        return daysCode;
    }
    
    protected String getMeetingTime(Class_ clazz) {
        String meetingTime = "";
        Formats.Format<Date> dpf = Formats.getDateFormat(Formats.Pattern.DATE_EVENT_SHORT);
        Assignment assignment = clazz.getCommittedAssignment();
        TreeSet meetings = (clazz.getEvent()==null?null:new TreeSet(clazz.getEvent().getMeetings()));
        DatePattern dp = (assignment==null?null:assignment.getDatePattern());
        if (meetings!=null && !meetings.isEmpty()) {
            int dayCode = getDaysCode(meetings);
            String days = "";
            for (int i=0;i<Constants.DAY_CODES.length;i++)
                if ((dayCode & Constants.DAY_CODES[i])!=0) days += CONSTANTS.shortDays()[i];
            meetingTime += days;
            Meeting first = (Meeting)meetings.first();
            meetingTime += " "+first.startTime()+" - "+first.stopTime();
        } else if (assignment!=null) {
            TimeLocation t = assignment.getTimeLocation();
            meetingTime += t.getDayHeader()+" "+t.getStartTimeHeader(CONSTANTS.useAmPm())+" - "+t.getEndTimeHeader(CONSTANTS.useAmPm());
        } else {
            meetingTime += MSG.arrHrs();
        }
        if (meetings!=null && !meetings.isEmpty()) {
            if (dp==null || !dp.isDefault()) {
                Date first = ((Meeting)meetings.first()).getMeetingDate();
                Date last = ((Meeting)meetings.last()).getMeetingDate();
                if (dp!=null && dp.isAlternate()) 
                    meetingTime += " ("+dpf.format(first)+" - "+dpf.format(last)+" "+dp.getName()+")";
                else
                    meetingTime += " ("+dpf.format(first)+" - "+dpf.format(last)+")";
            }
        } else if (dp!=null && !dp.isDefault()) {
            if (dp.isAlternate()) 
                meetingTime += " ("+dpf.format(dp.getStartDate())+" - "+dpf.format(dp.getEndDate())+" "+dp.getName()+")";
            else
                meetingTime += " ("+dpf.format(dp.getStartDate())+" - "+dpf.format(dp.getEndDate())+")";
        }
        return meetingTime;
    }
    
    protected String getMeetingRooms(boolean html, Class_ clazz) {
        String meetingRooms = "";
        Assignment assignment = clazz.getCommittedAssignment();
        TreeSet<Meeting> meetings = (clazz.getEvent()==null?null:new TreeSet(clazz.getEvent().getMeetings()));
        TreeSet<Location> locations = new TreeSet<Location>();
        if (meetings!=null && !meetings.isEmpty()) {
            for (Meeting meeting : meetings)
                if (meeting.getLocation()!=null) locations.add(meeting.getLocation());
        } else if (assignment!=null && assignment.getDatePattern()!=null) {
            for (Iterator i=assignment.getRooms().iterator();i.hasNext();) {
                locations.add((Location)i.next());
            }
        }
        for (Location location: locations) {
            if (meetingRooms.length()>0) meetingRooms+=", ";
            meetingRooms+=(html ? location.getLabelWithHint() : location.getLabel());
        }
        return meetingRooms;
    }
    
    protected long getMeetingComparable(Class_ clazz) {
        Assignment assignment = clazz.getCommittedAssignment();
        TreeSet meetings = (clazz.getEvent()==null?null:new TreeSet(clazz.getEvent().getMeetings()));
        if (meetings!=null && !meetings.isEmpty()) {
            return ((Meeting)meetings.first()).getMeetingDate().getTime();
        } else if (assignment!=null) {
            return assignment.getTimeLocation().getStartSlot();
        }
        return -1;
    }
    
    protected String getMeetingInstructor(Class_ clazz) {
        String meetingInstructor = "";
        if (!clazz.isDisplayInstructor()) return meetingInstructor;
        for (Iterator i=new TreeSet(clazz.getClassInstructors()).iterator();i.hasNext();) {
            ClassInstructor ci = (ClassInstructor)i.next();
            if (meetingInstructor.length()>0) meetingInstructor+=", ";
            meetingInstructor += ci.getInstructor().getName(DepartmentalInstructor.sNameFormatLastInitial);
        }
        return meetingInstructor;
    }
    
    public boolean match(ClassesForm form, CourseOffering co) {
        if ("--ALL--".equals(form.getSubjectArea())) return true;
        if (!co.getSubjectArea().getSubjectAreaAbbreviation().equals(form.getSubjectArea())) return false;
        if (ApplicationProperty.CourseOfferingTitleSearch.isTrue() && form.getCourseNumber() != null && form.getCourseNumber().length() > 2) {
        	return co.getCourseNbr().matches(form.getCourseNumber().replaceAll("\\*", ".*")) || (co.getTitle() != null && co.getTitle().toLowerCase().contains(form.getCourseNumber().toLowerCase()));
        } else if (form.getCourseNumber()!=null && form.getCourseNumber().length()>0) {
            return co.getCourseNbr().matches(form.getCourseNumber().replaceAll("\\*", ".*"));
        }
        return true;
    }
    
	private PdfWebTable getTable(boolean html, ClassesForm form, Collection<Class_> classes, int ord) {
	    String nl = (html?"<br>":"\n");
        PdfWebTable table = new PdfWebTable( 6,
                form.getSessionLabel()+" classes"+("--ALL--".equals(form.getSubjectArea())?"":" ("+form.getSubjectArea()+(form.getCourseNumber()!=null && form.getCourseNumber().length()>0?" "+form.getCourseNumber():"")+")"), 
                "classes.action?ord=%%",
                new String[] {
                	MSG.columnCourse(),
                	MSG.columnInstructionType().replace("\n", nl),
                	MSG.columnSection(),
                	MSG.columnAssignedTime(),
                	MSG.columnAssignedRoom(),
                    MSG.columnInstructor()},
                    new String[] {"left", "left", "left", "left", "left", "left"},
                    new boolean[] {true, true, true, true, true, true} );
        table.setRowStyle("white-space:nowrap");
        table.setBlankWhenSame(true);
        boolean suffix = ApplicationProperty.ExaminationReportsClassSufix.isTrue();
        for (Class_ clazz: classes) {
            for (CourseOffering co : (Collection<CourseOffering>)clazz.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering().getCourseOfferings()) {
                if (!match(form, co)) continue;
                String course = co.getCourseName();
                String itype =  clazz.getSchedulingSubpart().getItypeDesc();
                int itypeCmp = clazz.getSchedulingSubpart().getItype().getItype();
                String section = (suffix && clazz.getClassSuffix(co)!=null?clazz.getClassSuffix(co):clazz.getSectionNumberString());
                String time = getMeetingTime(clazz);
                long timeCmp = getMeetingComparable(clazz);
                String room = getMeetingRooms(html, clazz);
                String instr = getMeetingInstructor(clazz);
                table.addLine(
                        new String[] {
                                course,
                                itype,
                                section,
                                time,
                                room,
                                instr
                            },
                            new Comparable[] {
                                new MultiComparable(course, itypeCmp, section, timeCmp, room, instr),
                                new MultiComparable(itypeCmp, course, section, timeCmp, room, instr),
                                new MultiComparable(course, section, itypeCmp, timeCmp, room, instr),
                                new MultiComparable(timeCmp, room, course, itypeCmp, section, instr),
                                new MultiComparable(room, timeCmp, course, itypeCmp, section, instr),
                                new MultiComparable(instr, course, itypeCmp, section, timeCmp, room)
                            });                
            }
        }
        return table;	    
	}

	protected AuthenticationManager getAuthenticationManager() {
		return (AuthenticationManager)SpringApplicationContextHolder.getBean("authenticationManager");
	}
}

