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

import java.io.File;
import java.io.FileWriter;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.tiles.annotation.TilesDefinition;
import org.apache.struts2.tiles.annotation.TilesPutAttribute;
import org.cpsolver.coursett.model.TimeLocation;
import org.springframework.security.core.context.SecurityContextHolder;
import org.unitime.commons.Debug;
import org.unitime.commons.MultiComparable;
import org.unitime.commons.web.WebTable;
import org.unitime.commons.web.WebTable.WebTableLine;
import org.unitime.commons.web.WebTable.WebTableTweakStyle;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.localization.messages.ExaminationMessages;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.events.QueryEncoderBackend;
import org.unitime.timetable.form.PersonalizedExamReportForm;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.interfaces.ExternalUidTranslation;
import org.unitime.timetable.interfaces.ExternalUidTranslation.Source;
import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.DepartmentStatusType;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.ExamOwner;
import org.unitime.timetable.model.ExamType;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.Meeting;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.StudentClassEnrollment;
import org.unitime.timetable.model.dao.DepartmentalInstructorDAO;
import org.unitime.timetable.model.dao.ExamDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.model.dao.StudentDAO;
import org.unitime.timetable.reports.exam.InstructorExamReport;
import org.unitime.timetable.reports.exam.StudentExamReport;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.exam.ui.ExamAssignment;
import org.unitime.timetable.solver.exam.ui.ExamAssignmentInfo;
import org.unitime.timetable.solver.exam.ui.ExamInfo;
import org.unitime.timetable.solver.exam.ui.ExamAssignmentInfo.BackToBackConflict;
import org.unitime.timetable.solver.exam.ui.ExamAssignmentInfo.DirectConflict;
import org.unitime.timetable.solver.exam.ui.ExamAssignmentInfo.MoreThanTwoADayConflict;
import org.unitime.timetable.solver.exam.ui.ExamInfo.ExamSectionInfo;
import org.unitime.timetable.util.AccessDeniedException;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.ExportUtils;
import org.unitime.timetable.util.Formats;
import org.unitime.timetable.webutil.PdfWebTable;

/**
 * @author Tomas Muller
 */
@Action(value = "personalSchedule", results = {
		@Result(name = "show", type = "tiles", location = "personalSchedule.tiles"),
		@Result(name = "main", type = "redirect", location = "/main.action"),
		@Result(name = "back", type = "redirect", location = "/login.action"),
		@Result(name = "exams", type = "redirect", location = "/exams.action"),
		@Result(name = "classes", type = "redirect", location = "/classes.action")
	})
@TilesDefinition(name = "personalSchedule.tiles", extend = "baseLayout", putAttributes =  {
		@TilesPutAttribute(name = "title", value = "Personal Schedule"),
		@TilesPutAttribute(name = "body", value = "/exam/personalizedReport.jsp"),
		@TilesPutAttribute(name = "checkRole", value = "false")
	})
public class PersonalizedExamReportAction extends UniTimeAction<PersonalizedExamReportForm> {
	private static final long serialVersionUID = -4629112485625863953L;
	protected final static CourseMessages MSG = Localization.create(CourseMessages.class);
	protected final static ExaminationMessages XMSG = Localization.create(ExaminationMessages.class);
	protected static GwtConstants CONSTANTS = Localization.create(GwtConstants.class);

    public static ExternalUidTranslation sTranslation;
    private static Log sLog = LogFactory.getLog(PersonalizedExamReportAction.class);
    
    static {
        if (ApplicationProperty.ExternalUserIdTranslation.value()!=null) {
            try {
                sTranslation = (ExternalUidTranslation)Class.forName(ApplicationProperty.ExternalUserIdTranslation.value()).getConstructor().newInstance();
            } catch (Exception e) { Debug.error("Unable to instantiate external uid translation class, "+e.getMessage()); }
        }
    }
    
    public static String translate(String uid, Source target) {
        if (sTranslation==null || uid==null || target.equals(Source.User)) return uid;
        return sTranslation.translate(uid, Source.User, target);
    }
    
    public static boolean hasPersonalReport(String userId) {
    	if (userId == null) return false;
        //if (user.getRole()!=null) return false;
        HashSet<Session> sessions = new HashSet();
        DepartmentalInstructor instructor = null;
        for (DepartmentalInstructor s: DepartmentalInstructorDAO.getInstance().getSession().
                createQuery("select i from DepartmentalInstructor i where i.externalUniqueId=:externalId", DepartmentalInstructor.class).
                setParameter("externalId", userId).
                setCacheable(true).list()) {
            if (!canDisplay(s.getDepartment().getSession())) continue;
            sessions.add(s.getDepartment().getSession());
            if (instructor==null || instructor.getDepartment().getSession().compareTo(s.getDepartment().getSession())<0) instructor = s;
        }
        
        Student student = null;
        for (Student s: StudentDAO.getInstance().getSession().
                createQuery("select s from Student s where s.externalUniqueId=:externalId", Student.class).
                setParameter("externalId", userId).
                setCacheable(true).list()) {
            if (!canDisplay(s.getSession())) continue;
            sessions.add(s.getSession());
            if (student==null || student.getSession().compareTo(s.getSession())<0) student = s;
        }
        
        if (instructor==null && student==null) return false;

        return true;
    }
    
    public String execute() throws Exception {
    	if (form == null) {
    		form = new PersonalizedExamReportForm();
    		form.reset();
    	}
    	
    	if (form.getOp() != null) op = form.getOp();
        
        String back = (String)request.getSession().getAttribute("loginPage");
        if (back == null) back = "back";
        
        try {
        	sessionContext.checkPermission(Right.PersonalSchedule);
        } catch (AccessDeniedException e) {
        	request.setAttribute("message", e.getMessage());
        	return back;
        }
        
        if (request.getParameter("q") != null) {
        	String[] params = QueryEncoderBackend.decode(request.getParameter("q")).split(":");
        	if (params != null && params.length == 2) {
        		form.setUid(params[0]);
        		form.setSessionId(Long.valueOf(params[1]));
        	}
        }
        
        String externalId = sessionContext.getUser().getExternalUserId();
        String userName = sessionContext.getUser().getName();
        
        form.setAdmin(sessionContext.hasPermission(Right.PersonalScheduleLookup));
        form.setLogout(!"back".equals(back));
        
        if (sessionContext.hasPermission(Right.PersonalScheduleLookup) && form.getUid() != null && !form.getUid().isEmpty()) {
            externalId = form.getUid();
            userName =
                    (form.getLname()==null || form.getLname().length()==0?"":" "+Constants.toInitialCase(form.getLname()))+
                    (form.getFname()==null || form.getFname().length()==0?"":" "+form.getFname().substring(0,1).toUpperCase())+
                    (form.getMname()==null || form.getMname().length()==0?"":" "+form.getMname().substring(0,1).toUpperCase());
        }

        if (externalId==null || externalId.length()==0) {
            request.setAttribute("message", "No user id provided.");
            return back;
        }
        
        if (MSG.buttonLogOut().equals(form.getOp())) {
        	SecurityContextHolder.getContext().setAuthentication(null);
            return back;
        }
        
        if ("classes".equals(back)) {
            if (form.getSessionId() == null) {
                form.setSessionId((Long)request.getSession().getAttribute("Classes.session"));
            } else {
                request.getSession().setAttribute("Classes.session", form.getSessionId());
            }
        } else if ("exams".equals(back)) {
            if (form.getSessionId() == null) {
            	form.setSessionId((Long)request.getSession().getAttribute("Exams.session"));
            } else {
                request.getSession().setAttribute("Exams.session", form.getSessionId());
            }
        }
        
        HashSet<Session> sessions = new HashSet();
        DepartmentalInstructor instructor = null;
        for (DepartmentalInstructor s: DepartmentalInstructorDAO.getInstance().getSession().
                createQuery("select i from DepartmentalInstructor i where i.externalUniqueId=:externalId", DepartmentalInstructor.class).
                setParameter("externalId", translate(externalId,Source.Staff)).
                setCacheable(true).list()) {
            if (!canDisplay(s.getDepartment().getSession())) continue;
            sessions.add(s.getDepartment().getSession());
            if (form.getSessionId() == null) {
                if (instructor==null || instructor.getDepartment().getSession().compareTo(s.getDepartment().getSession())<0) instructor = s;
            } else if (form.getSessionId().equals(s.getDepartment().getSession().getUniqueId())) {
                instructor = s;
            }
        }
        
        Student student = null;
        for (Student s: StudentDAO.getInstance().getSession().
                createQuery("select s from Student s where s.externalUniqueId=:externalId", Student.class).
                setParameter("externalId", translate(externalId,Source.Student)).
                setCacheable(true).list()) {
            if (!canDisplay(s.getSession())) continue;
            sessions.add(s.getSession());
            if (form.getSessionId() == null) {
                if (student==null || student.getSession().compareTo(s.getSession())<0) student = s;
            } else if (form.getSessionId().equals(s.getSession().getUniqueId()))
                student = s;
        }
        
        if (instructor==null && student==null) {
        	if (form.getAdmin()) {
        		back = "back";
        		form.setLogout(false);
        	} else {
                if ("classes".equals(back))
                    request.setAttribute("message", MSG.infoNoClassesFound());
                else if ("exams".equals(back))
                    request.setAttribute("message", MSG.infoNoExaminationsFound());
                else
                    request.setAttribute("message", MSG.infoNoScheduleFound());
                sLog.info("No matching instructor or student found for "+userName+" ("+translate(externalId,Source.Student)+"), forwarding back ("+back+").");
                return back;
        	}
        }
        
        form.setCanExport(false);
        
        if (instructor!=null && student!=null && !instructor.getDepartment().getSession().equals(student.getSession())) {
            if (instructor.getDepartment().getSession().compareTo(student.getSession())<0)
                instructor = null;
            else
                student = null;
        }
        long t0 = System.currentTimeMillis();
        if (instructor!=null) {
            sLog.info("Requesting schedule for "+instructor.getName(DepartmentalInstructor.sNameFormatShort)+" (instructor)");
        } else if (student!=null) {
            sLog.info("Requesting schedule for "+student.getName(DepartmentalInstructor.sNameFormatShort)+" (student)");
        }
        
        HashSet<ExamOwner> studentExams = new HashSet<ExamOwner>();
        if (student!=null) {
        	/*
            for (Iterator i=student.getClassEnrollments().iterator();i.hasNext();) {
                StudentClassEnrollment sce = (StudentClassEnrollment)i.next();
                studentExams.addAll(Exam.findAllRelated("Class_", sce.getClazz().getUniqueId()));
            }
            */
            studentExams.addAll(
            		ExamDAO.getInstance().getSession().createQuery(
                            "select distinct o from Student s inner join s.classEnrollments ce, ExamOwner o inner join o.course co "+
                            "inner join co.instructionalOffering io "+
                            "inner join io.instrOfferingConfigs ioc " +
                            "inner join ioc.schedulingSubparts ss "+
                            "inner join ss.classes c where "+
                            "s.uniqueId=:studentId and c=ce.clazz and ("+
                            "(o.ownerType="+ExamOwner.sOwnerTypeCourse+" and o.ownerId=co.uniqueId) or "+
                            "(o.ownerType="+ExamOwner.sOwnerTypeOffering+" and o.ownerId=io.uniqueId) or "+
                            "(o.ownerType="+ExamOwner.sOwnerTypeConfig+" and o.ownerId=ioc.uniqueId) or "+
                            "(o.ownerType="+ExamOwner.sOwnerTypeClass+" and o.ownerId=c.uniqueId) "+
                            ")", ExamOwner.class).
                            setParameter("studentId", student.getUniqueId()).setCacheable(true).list());
            for (Iterator<ExamOwner> i=studentExams.iterator();i.hasNext();) {
            	Exam exam = i.next().getExam();
            	DepartmentStatusType type = exam.effectiveStatusType();
            	if (type == null || !type.can(exam.getExamType().getType() == ExamType.sExamTypeFinal ? DepartmentStatusType.Status.ReportExamsFinal : DepartmentStatusType.Status.ReportExamsMidterm))
            		i.remove();
            }
        }
        
        HashSet<Exam> instructorExams = new HashSet<Exam>();
        if (instructor!=null) {
        	instructorExams.addAll(instructor.getAllExams());
        	for (Iterator<Exam> i=instructorExams.iterator();i.hasNext();) {
        		Exam exam = i.next();
        		DepartmentStatusType type = exam.effectiveStatusType();
        		if (type == null || !type.can(exam.getExamType().getType() == ExamType.sExamTypeFinal ? DepartmentStatusType.Status.ReportExamsFinal : DepartmentStatusType.Status.ReportExamsMidterm))
        			i.remove();
        	}
        }
        
        WebTable.setOrder(sessionContext,"exams.o0",request.getParameter("o0"),1);
        WebTable.setOrder(sessionContext,"exams.o1",request.getParameter("o1"),1);
        WebTable.setOrder(sessionContext,"exams.o2",request.getParameter("o2"),1);
        WebTable.setOrder(sessionContext,"exams.o3",request.getParameter("o3"),1);
        WebTable.setOrder(sessionContext,"exams.o4",request.getParameter("o4"),1);
        WebTable.setOrder(sessionContext,"exams.o5",request.getParameter("o5"),1);
        WebTable.setOrder(sessionContext,"exams.o6",request.getParameter("o6"),1);
        WebTable.setOrder(sessionContext,"exams.o7",request.getParameter("o7"),1);
        
        boolean hasClasses = false;
        if (student!=null && student.getSession().canNoRoleReportClass() && !student.getClassEnrollments().isEmpty()) {
            PdfWebTable table =  getStudentClassSchedule(true, student);
            if (!table.getLines().isEmpty()) {
                request.setAttribute("clsschd", table.printTable(WebTable.getOrder(sessionContext,"exams.o6")));
                hasClasses = true;
                form.setCanExport(true);
            }
        }
        if (instructor!=null && instructor.getDepartment().getSession().canNoRoleReportClass()) {
            PdfWebTable table = getInstructorClassSchedule(true, instructor);
            if (!table.getLines().isEmpty()) {
                request.setAttribute("iclsschd", table.printTable(Math.abs(WebTable.getOrder(sessionContext,"exams.o7"))));
                hasClasses = true;
                form.setCanExport(true);
            }
        }
        
        if (instructor!=null && sessions.size()>1) {
            PdfWebTable table = getSessions(true, sessions, instructor.getName(DepartmentalInstructor.sNameFormatLastFist), instructor.getDepartment().getSession().getUniqueId());
            request.setAttribute("sessions", table.printTable(WebTable.getOrder(sessionContext,"exams.o0")));
        } else if (student!=null && sessions.size()>1) {
            PdfWebTable table = getSessions(true, sessions, student.getName(DepartmentalInstructor.sNameFormatLastFist), student.getSession().getUniqueId());
            request.setAttribute("sessions", table.printTable(WebTable.getOrder(sessionContext,"exams.o0")));
        }
        
        if (!hasClasses && instructorExams.isEmpty() && studentExams.isEmpty()) {
            if ("classes".equals(back))
                form.setMessage(MSG.infoNoClassesFoundForSession((instructor!=null?instructor.getDepartment().getSession():student.getSession()).getLabel()));
            else if ("exams".equals(back))
                form.setMessage(MSG.infoNoExaminationsFoundForSession((instructor!=null?instructor.getDepartment().getSession():student.getSession()).getLabel()));
            else if (student!=null || instructor!=null)
                form.setMessage(MSG.infoNoClassesOrExamsFoundForSession((instructor!=null?instructor.getDepartment().getSession():student.getSession()).getLabel()));
            else if (sessionContext.hasPermission(Right.PersonalScheduleLookup)) 
                form.setMessage(MSG.infoNoClassesOrExamsFoundForSession(SessionDAO.getInstance().get(sessionContext.getUser().getCurrentAcademicSessionId()).getLabel()));
            else
                form.setMessage(MSG.infoNoClassesOrExamsFoundForUser(userName));
            sLog.info(MSG.infoNoClassesOrExamsFoundForUser((instructor!=null?instructor.getName(DepartmentalInstructor.sNameFormatShort):student!=null?student.getName(DepartmentalInstructor.sNameFormatShort):userName)));
        }
        
        boolean useCache = ApplicationProperty.ExaminationCacheConflicts.isTrue();
        
        if (MSG.actionExportPdf().equals(form.getOp())) {
            sLog.info("  Generating PDF for "+(instructor!=null?instructor.getName(DepartmentalInstructor.sNameFormatShort):student.getName(DepartmentalInstructor.sNameFormatShort)));
            OutputStream out = ExportUtils.getPdfOutputStream(response, "schedule");
                
            if (!instructorExams.isEmpty()) {
                TreeSet<ExamAssignmentInfo> exams = new TreeSet<ExamAssignmentInfo>();
                for (Exam exam : instructorExams) {
                    if (exam.getAssignedPeriod()==null) continue;
                    exams.add(new ExamAssignmentInfo(exam, useCache));
                }

                InstructorExamReport ir = new InstructorExamReport(
                        0, out, instructor.getDepartment().getSession(),
                        null, null, exams);
                ir.setM2d(true); ir.setDirect(true);
                ir.setClassSchedule(instructor.getDepartment().getSession().canNoRoleReportClass());
                ir.printHeader();
                ir.printReport(ExamInfo.createInstructorInfo(instructor), exams);
                ir.lastPage();
                ir.close();
            } else if (!studentExams.isEmpty()) {
                TreeSet<ExamAssignmentInfo> exams = new TreeSet<ExamAssignmentInfo>();
                TreeSet<ExamSectionInfo> sections = new TreeSet<ExamSectionInfo>();
                for (ExamOwner examOwner : studentExams) {
                    if (examOwner.getExam().getAssignedPeriod()==null) continue;
                    ExamAssignmentInfo x = new ExamAssignmentInfo(examOwner, student, studentExams);
                    exams.add(x);
                    sections.addAll(x.getSectionsIncludeCrosslistedDummies());
                }
                
                StudentExamReport sr = new StudentExamReport(
                        0, out, student.getSession(),
                        null, null, exams);
                sr.setM2d(true); sr.setBtb(true); sr.setDirect(true);
                sr.setClassSchedule(student.getSession().canNoRoleReportClass());
                sr.printHeader();
                sr.printReport(student, sections);
                sr.lastPage();
                sr.close();
            } else if (hasClasses) {
                if (instructor!=null) {
                    InstructorExamReport ir = new InstructorExamReport(
                            0, out, instructor.getDepartment().getSession(),
                            null, null, new TreeSet<ExamAssignmentInfo>());
                    ir.setM2d(true); ir.setDirect(true);
                    ir.setClassSchedule(instructor.getDepartment().getSession().canNoRoleReportClass());
                    ir.printHeader();
                    ir.printReport(ExamInfo.createInstructorInfo(instructor), new TreeSet<ExamAssignmentInfo>());
                    ir.lastPage();
                    ir.close();
                } else if (student!=null) {
                    StudentExamReport sr = new StudentExamReport(
                            0, out, student.getSession(),
                            null, null, new TreeSet<ExamAssignmentInfo>());
                    sr.setM2d(true); sr.setBtb(true); sr.setDirect(true);
                    sr.setClassSchedule(student.getSession().canNoRoleReportClass());
                    sr.printHeader();
                    sr.printReport(student, new TreeSet<ExamSectionInfo>());
                    sr.lastPage();
                    sr.close();
                }
            }
            
            out.flush(); out.close();
            return null;
        }
        
        if (MSG.actionExportIcal().equals(form.getOp())) {
        	Long sid = (instructor != null ? instructor.getDepartment().getSession().getUniqueId() : student.getSession().getUniqueId());
        	response.sendRedirect( response.encodeURL("export?q=" + QueryEncoderBackend.encode("output=events.ics&type=person&ext=" + externalId + (sid == null ? "" : "&sid=" + sid))));
        	return null;
        }
        
        if (!studentExams.isEmpty()) {
            form.setCanExport(true);
            TreeSet<ExamAssignmentInfo> exams = new TreeSet<ExamAssignmentInfo>();
            for (ExamOwner examOwner : studentExams) exams.add(new ExamAssignmentInfo(examOwner, student, studentExams));
            
            PdfWebTable table = getStudentExamSchedule(true, exams, student);
            request.setAttribute("schedule", table.printTable(WebTable.getOrder(sessionContext,"exams.o1")));
            
            table = getStudentConflits(true, exams, student);
            if (!table.getLines().isEmpty())
                request.setAttribute("conf", table.printTable(WebTable.getOrder(sessionContext,"exams.o3")));
        }
        
        if (!instructorExams.isEmpty()) {
            form.setCanExport(true);
            TreeSet<ExamAssignmentInfo> exams = new TreeSet<ExamAssignmentInfo>();
            for (Exam exam : instructorExams) exams.add(new ExamAssignmentInfo(exam, useCache));
            
            PdfWebTable table = getInstructorExamSchedule(true, exams, instructor);
            request.setAttribute("ischedule", table.printTable(WebTable.getOrder(sessionContext,"exams.o2")));
            
            table = getInstructorConflits(true, exams, instructor);
            if (!table.getLines().isEmpty())
                request.setAttribute("iconf", table.printTable(WebTable.getOrder(sessionContext,"exams.o4")));

            table = getStudentConflits(true, exams, instructor);
            if (!table.getLines().isEmpty())
                request.setAttribute("sconf", table.printTable(WebTable.getOrder(sessionContext,"exams.o5")));
        }
        long t1 = System.currentTimeMillis();
        sLog.info("Request processed in "+new DecimalFormat("0.00").format(((double)(t1-t0))/1000.0)+" s for "+(instructor!=null?instructor.getName(DepartmentalInstructor.sNameFormatShort):student!=null?student.getName(DepartmentalInstructor.sNameFormatShort):userName));
        return "show";
    }
    
    public static boolean canDisplay(Session session) {
        if (session.getStatusType()==null) return false;
        if (session.canNoRoleReportExamFinal()) return true;
        if (session.canNoRoleReportExamMidterm()) return true;
        if (session.canNoRoleReportClass()) return true;
        return false;
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
    
    protected long getMeetingComparable(ExamSectionInfo section) {
        if (section.getOwner().getOwnerObject() instanceof Class_) {
            Class_ clazz = (Class_)section.getOwner().getOwnerObject();
            Assignment assignment = clazz.getCommittedAssignment();
            TreeSet meetings = (clazz.getEvent()==null?null:new TreeSet(clazz.getEvent().getMeetings()));
            if (meetings!=null && !meetings.isEmpty()) {
                return ((Meeting)meetings.first()).getMeetingDate().getTime();
            } else if (assignment!=null) {
                return assignment.getTimeLocation().getStartSlot();
            }
        }
        return -1;
    }

    protected String getMeetingTime(ExamSectionInfo section) {
        String meetingTime = "";
        if (section.getOwner().getOwnerObject() instanceof Class_) {
            Formats.Format<Date> dpf = Formats.getDateFormat(Formats.Pattern.DATE_EVENT_SHORT);
            Class_ clazz = (Class_)section.getOwner().getOwnerObject();
            Assignment assignment = clazz.getCommittedAssignment();
            TreeSet meetings = (clazz.getEvent()==null?null:new TreeSet(clazz.getEvent().getMeetings()));
            if (meetings!=null && !meetings.isEmpty()) {
                Date first = ((Meeting)meetings.first()).getMeetingDate();
                Date last = ((Meeting)meetings.last()).getMeetingDate();
                meetingTime += dpf.format(first)+" - "+dpf.format(last);
            } else if (assignment!=null && assignment.getDatePattern()!=null) {
                DatePattern dp = assignment.getDatePattern();
                if (dp!=null && !dp.isDefault()) {
                    if (dp.isAlternate())
                        meetingTime += dp.getName();
                    else {
                        meetingTime += dpf.format(dp.getStartDate())+" - "+dpf.format(dp.getEndDate());
                    }
                }
            }
            if (meetings!=null && !meetings.isEmpty()) {
                int dayCode = getDaysCode(meetings);
                String days = "";
                for (int i=0;i<Constants.DAY_CODES.length;i++)
                    if ((dayCode & Constants.DAY_CODES[i])!=0) days += CONSTANTS.shortDays()[i];
                meetingTime += " "+days;
                Meeting first = (Meeting)meetings.first();
                meetingTime += " "+first.startTime()+" - "+first.stopTime();
            } else if (assignment!=null) {
                TimeLocation t = assignment.getTimeLocation();
                meetingTime += " "+t.getDayHeader()+" "+t.getStartTimeHeader(CONSTANTS.useAmPm())+" - "+t.getEndTimeHeader(CONSTANTS.useAmPm());
            }
        }
        return meetingTime;
    }
    
    public PdfWebTable getSessions(boolean html, HashSet<Session> sessions, String name, Long sessionId) {
        PdfWebTable table = new PdfWebTable( 5,
        		MSG.sectAvailableAcademicSessionsForUser(name),
                "personalSchedule.action?o0=%%" + (sessionId == null ? "" : "&sessionId=" + sessionId),
                new String[] {
                	MSG.columnTerm(),
                	MSG.columnYear(),
                	MSG.columnCampus()},
                new String[] {"left", "left", "left"},
                new boolean[] {true, true, true} );
        table.setRowStyle("white-space:nowrap");
        for (Session session : sessions) {
            String bgColor = null;
            if (sessionId.equals(session.getUniqueId())) bgColor = "rgb(168,187,225)";
            table.addLine(
            		"onClick=\"document.getElementById('personalSchedule_form_sessionId').value='" + session.getUniqueId() +"'; document.getElementById('personalSchedule').submit();\"",
                    new String[] {
                        session.getAcademicTerm(),
                        session.getAcademicYear(),
                        session.getAcademicInitiative()
                    },
                    new Comparable[] {
                        new MultiComparable(session.getSessionBeginDateTime(),session.getAcademicInitiative()),
                        new MultiComparable(session.getSessionBeginDateTime(),session.getAcademicInitiative()),
                        new MultiComparable(session.getAcademicInitiative(),session.getSessionBeginDateTime())}).setBgColor(bgColor);
        }
        return table;
    }
    
    public PdfWebTable getStudentExamSchedule(boolean html, TreeSet<ExamAssignmentInfo> exams, Student student) {
        PdfWebTable table = new PdfWebTable( 5,
        		MSG.sectExaminationScheduleForStudent(student.getSession().getLabel(), student.getName(DepartmentalInstructor.sNameFormatLastFist)),
                "personalSchedule.action?o1=%%&q=" + QueryEncoderBackend.encode(student.getExternalUniqueId()+ ":" + student.getSession().getUniqueId()),
                new String[] {
                    XMSG.colOwner(),
                    MSG.columnMeetingTime(),
                    MSG.columnExamDate(),
                    MSG.columnExamTime(),
                    MSG.columnExamRoom()},
                new String[] {"left", "left", "left", "left", "left"},
                new boolean[] {true, true, true, true, true} );
        table.setRowStyle("white-space:nowrap");
        String noRoom = ApplicationProperty.ExaminationsNoRoomText.value();
        for (ExamAssignmentInfo exam : exams) {
            if (exam.getPeriod()==null) continue;
            for (ExamSectionInfo section : exam.getSectionsIncludeCrosslistedDummies()) {
                if (!section.getStudentIds().contains(student.getUniqueId())) continue;
                String sectionName = section.getName();
                table.addLine(
                        new String[] {
                        		sectionName,
                                getMeetingTime(section),
                                exam.getDate(false),
                                exam.getTime(false),
                                (exam.getNrRooms()==0 ? noRoom : html ? exam.getRoomsNameWithHint(false, ", ") : exam.getRoomsName(false, ", "))
                        },
                        new Comparable[] {
                            new MultiComparable(-exam.getExamType().getType(), sectionName, exam),
                            new MultiComparable(-exam.getExamType().getType(), getMeetingComparable(section), sectionName, exam),
                            new MultiComparable(-exam.getExamType().getType(), exam.getPeriodOrd(), sectionName, exam),
                            new MultiComparable(-exam.getExamType().getType(), exam.getPeriod()==null?-1:exam.getPeriod().getStartSlot(), sectionName, exam),
                            new MultiComparable(-exam.getExamType().getType(), exam.getRoomsName(":"), sectionName, exam)
                        });
            }
        }
        table.setWebTableTweakStyle(new WebTableTweakStyle() {
			public String getStyleHtml(WebTableLine current, WebTableLine next, int order) {
				if (next!=null && ((MultiComparable)current.getOrderBy()[0]).getContent()[0].compareTo(((MultiComparable)next.getOrderBy()[0]).getContent()[0])!=0)
					return "border-bottom: rgb(81,81,81) 1px dashed";
				return null;
			}
		});
        return table;
    }
    
    public PdfWebTable getStudentConflits(boolean html, TreeSet<ExamAssignmentInfo> exams, Student student) {
        String nl = (html?"<br>":"\n");
        boolean showBackToBack = ApplicationProperty.ExaminationReportsStudentBackToBacks.isTrue();
        PdfWebTable table = new PdfWebTable( 6,
        		showBackToBack ? 
        				MSG.sectExaminationConflictsOrBackToBacksForStudent(student.getSession().getLabel(), student.getName(DepartmentalInstructor.sNameFormatLastFist)) :
        				MSG.sectExaminationConflictsForStudent(student.getSession().getLabel(), student.getName(DepartmentalInstructor.sNameFormatLastFist)),
                "personalSchedule.action?o3=%%&q=" + QueryEncoderBackend.encode(student.getExternalUniqueId()+ ":" + student.getSession().getUniqueId()),
                new String[] {
                    MSG.columnExamType(),
                    XMSG.colOwner(),
                    MSG.columnExamDate(),
                    MSG.columnExamTime(),
                    MSG.columnExamRoom(),
                    MSG.columnBackToBackDistance()},
                    new String[] {"left","left","left","left","left","left"},
                    new boolean[] {true,  true, true, true, true, true} );
        table.setRowStyle("white-space:nowrap");
        String noRoom = ApplicationProperty.ExaminationsNoRoomText.value();
        for (ExamAssignmentInfo exam : exams) {
            for (DirectConflict conflict : exam.getDirectConflicts()) {
                if (conflict.getOtherExam()!=null && exam.compareTo(conflict.getOtherExam())>=0 && exams.contains(conflict.getOtherExam())) continue;
                if (!conflict.getStudents().contains(student.getUniqueId())) continue;
                String classes = "", date = "", time = "", room = "";
                boolean firstSection = true;
                for (ExamSectionInfo section : exam.getSectionsIncludeCrosslistedDummies()) {
                    if (!section.getStudentIds().contains(student.getUniqueId())) continue;
                    if (classes.length()>0) {
                        classes += nl; date += nl; time += nl; room += nl;
                    }
                    classes += section.getName();
                    if (firstSection) {
                        date += exam.getDate(false);
                        time += exam.getTime(false);
                        room += (exam.getNrRooms()==0 ? noRoom : html ? exam.getRoomsNameWithHint(false, ", ") : exam.getRoomsName(false,", "));
                    }
                    firstSection = false;
                }
                firstSection = true;
                if (conflict.getOtherExam()!=null) {
                    for (ExamSectionInfo section : conflict.getOtherExam().getSectionsIncludeCrosslistedDummies()) {
                        if (!section.getStudentIds().contains(student.getUniqueId())) continue;
                        if (classes.length()>0) {
                            classes += nl; date += nl; time += nl; room += nl;
                        }
                        classes += section.getName();
                        if (firstSection) {
                            room += (conflict.getOtherExam().getNrRooms()==0?noRoom:conflict.getOtherExam().getRoomsName(false,", "));
                        }
                        firstSection = false;
                    }
                } else if (conflict.getOtherEventId()!=null) {
                    classes += nl; date += nl; time += nl; room += nl;
                    classes += conflict.getOtherEventName();
                    room += conflict.getOtherEventRoom();
                    //date += conflict.getOtherEventDate();
                    time += conflict.getOtherEventTime(); 
                }
                table.addLine(
                        new String[] {
                            (html?"<font color='"+PreferenceLevel.prolog2color("P")+"'>":"")+(conflict.getOtherEventId()!=null?conflict.isOtherClass()?XMSG.typeClass():XMSG.typeEvent():XMSG.conflictDirect())+(html?"</font>":""),
                            classes,
                            date,
                            time,
                            room,
                            ""
                        }, new Comparable[] {
                            new MultiComparable(-exam.getExamType().getType(), 0, exam, 0),
                            new MultiComparable(-exam.getExamType().getType(), exam, exam, 0),
                            new MultiComparable(-exam.getExamType().getType(), exam.getPeriodOrd(), exam, 0),
                            new MultiComparable(-exam.getExamType().getType(), exam.getPeriod().getStartSlot(), exam, 0),
                            new MultiComparable(-exam.getExamType().getType(), exam.getRoomsName(":"), exam, 0),
                            new MultiComparable(-exam.getExamType().getType(), -1.0, exam, 0)
                        });
            }
            if (showBackToBack)
                for (BackToBackConflict conflict : exam.getBackToBackConflicts()) {
                    if (exam.compareTo(conflict.getOtherExam())>=0 && exams.contains(conflict.getOtherExam())) continue;
                    if (!conflict.getStudents().contains(student.getUniqueId())) continue;
                    String classes = "", date = "", time = "", room = "";
                    boolean firstSection = true;
                    for (ExamSectionInfo section : exam.getSectionsIncludeCrosslistedDummies()) {
                        if (!section.getStudentIds().contains(student.getUniqueId())) continue;
                        if (classes.length()>0) {
                            classes += nl; date += nl; time += nl; room += nl;
                        }
                        classes += section.getName();
                        if (firstSection) {
                            date += exam.getDate(false);
                            time += exam.getTime(false);
                            room += (exam.getNrRooms()==0?noRoom:exam.getRoomsName(false,", "));;
                        }
                        firstSection = false;
                    }
                    firstSection = true;
                    for (ExamSectionInfo section : conflict.getOtherExam().getSectionsIncludeCrosslistedDummies()) {
                        if (!section.getStudentIds().contains(student.getUniqueId())) continue;
                        if (classes.length()>0) {
                            classes += nl; date += nl; time += nl; room += nl;
                        }
                        classes += section.getName();
                        if (firstSection) {
                            time += conflict.getOtherExam().getTime(false);
                            room += (conflict.getOtherExam().getNrRooms()==0?noRoom:conflict.getOtherExam().getRoomsName(false,", "));
                        }
                        firstSection = false;
                    }
                    table.addLine(
                            new String[] {
                                (html?"<font color='"+PreferenceLevel.prolog2color("1")+"'>":"")+XMSG.conflictBackToBack()+(html?"</font>":""),
                                classes,
                                date,
                                time,
                                room,
                                MSG.backToBackDistanceInMeters((int)(conflict.getDistance()))
                            }, new Comparable[] {
                                new MultiComparable(-exam.getExamType().getType(), 2, exam, 0),
                                new MultiComparable(-exam.getExamType().getType(), exam, exam, 0),
                                new MultiComparable(-exam.getExamType().getType(), exam.getPeriodOrd(), exam, 0),
                                new MultiComparable(-exam.getExamType().getType(), exam.getPeriod().getStartSlot(), exam, 0),
                                new MultiComparable(-exam.getExamType().getType(), exam.getRoomsName(":"), exam, 0),
                                new MultiComparable(-exam.getExamType().getType(), conflict.getDistance(), exam, 0)
                            });
                }
            conflicts: for (MoreThanTwoADayConflict conflict : exam.getMoreThanTwoADaysConflicts()) {
                for (ExamAssignment other : conflict.getOtherExams())
                    if (exam.compareTo(other)>=0 && exams.contains(other)) continue conflicts;
                if (!conflict.getStudents().contains(student.getUniqueId())) continue;
                String classes = "", date = "", time = "", room = "";
                boolean firstSection = true;
                for (ExamSectionInfo section : exam.getSectionsIncludeCrosslistedDummies()) {
                    if (!section.getStudentIds().contains(student.getUniqueId())) continue;
                    if (classes.length()>0) {
                        classes += nl; date += nl; time += nl; room += nl;
                    }
                    classes += section.getName();
                    if (firstSection) {
                        date += exam.getDate(false);
                        time += exam.getTime(false);
                        room += (exam.getNrRooms()==0?noRoom:exam.getRoomsName(false,", "));
                    }
                    firstSection = false;
                }
                for (ExamAssignment other : conflict.getOtherExams()) {
                    firstSection = true;
                    for (ExamSectionInfo section : other.getSectionsIncludeCrosslistedDummies()) {
                        if (!section.getStudentIds().contains(student.getUniqueId())) continue;
                        if (classes.length()>0) {
                            classes += nl; date += nl; time += nl; room += nl;
                        }
                        classes += section.getName();
                        if (firstSection) {
                            time += other.getTime(false);
                            room += (other.getNrRooms()==0?noRoom:other.getRoomsName(false,", "));
                        }
                        firstSection = false;
                    }
                }
                table.addLine(
                        (sessionContext.hasPermission(exam, Right.ExaminationDetail) ? "onClick=\"document.location='examDetail.action?examId="+exam.getExamId()+"';\"" : ""),
                        new String[] {
                            (html?"<font color='"+PreferenceLevel.prolog2color("2")+"'>":"")+(html?XMSG.conflictMoreThanTwoADay().replace(">","&gt;"):XMSG.conflictMoreThanTwoADay())+(html?"</font>":""),
                            classes,
                            date,
                            time,
                            room,
                            ""
                        }, new Comparable[] {
                            new MultiComparable(-exam.getExamType().getType(), 1, exam, 0),
                            new MultiComparable(-exam.getExamType().getType(), exam, exam, 0),
                            new MultiComparable(-exam.getExamType().getType(), exam.getPeriodOrd(), exam, 0),
                            new MultiComparable(-exam.getExamType().getType(), exam.getPeriod().getStartSlot(), exam, 0),
                            new MultiComparable(-exam.getExamType().getType(), exam.getRoomsName(":"), exam, 0),
                            new MultiComparable(-exam.getExamType().getType(), -1.0, exam, 0)
                        },
                        exam.getExamId().toString());
            }
        }
        table.setWebTableTweakStyle(new WebTableTweakStyle() {
			public String getStyleHtml(WebTableLine current, WebTableLine next, int order) {
				if (next!=null && ((MultiComparable)current.getOrderBy()[0]).getContent()[0].compareTo(((MultiComparable)next.getOrderBy()[0]).getContent()[0])!=0)
					return "border-bottom: rgb(81,81,81) 1px dashed";
				return null;
			}
		});
        return table;
    }

    
    public PdfWebTable getInstructorExamSchedule(boolean html, TreeSet<ExamAssignmentInfo> exams, DepartmentalInstructor instructor) {
        String nl = (html?"<br>":"\n");
        PdfWebTable table = new PdfWebTable( 8,
        		MSG.sectExaminationScheduleForInstructor(
        				instructor.getDepartment().getSession().getLabel(),
        				instructor.getName(DepartmentalInstructor.sNameFormatLastFist)),
                "personalSchedule.action?o2=%%&q=" + QueryEncoderBackend.encode(instructor.getExternalUniqueId()+ ":" + instructor.getDepartment().getSession().getUniqueId()),
                new String[] {
                	XMSG.colOwner(),
                    MSG.columnEnrollment(),
                    MSG.columnExamSeatingType().replace("<br>", nl),
                    MSG.columnMeetingTimes(),
                    MSG.columnExamDate(),
                    MSG.columnExamTime(),
                    MSG.columnExamRoom(),
                    MSG.columnExamRoomCapacity()},
                new String[] {"left", "right", "center", "left", "left", "left", "left", "right"},
                new boolean[] {true, true, true, true, true, true, true, true} );
        table.setRowStyle("white-space:nowrap");
        String noRoom = ApplicationProperty.ExaminationsNoRoomText.value();
        for (ExamAssignmentInfo exam : exams) {
            if (exam.getPeriod()==null) continue;
            for (ExamSectionInfo section : exam.getSectionsIncludeCrosslistedDummies()) {
                table.addLine(
                        new String[] {
                                section.getName(),
                                String.valueOf(section.getNrStudents()),
                                Exam.getSeatingTypeLabel(exam.getSeatingType()),
                                getMeetingTime(section),
                                exam.getDate(false),
                                exam.getTime(false),
                                (exam.getNrRooms()==0?noRoom:exam.getRoomsName(false,", ")),
                                exam.getRoomsCapacity(false, ", ")
                        },
                        new Comparable[] {
                            new MultiComparable(-exam.getExamType().getType(), section.getName(), exam),
                            new MultiComparable(-exam.getExamType().getType(), -exam.getNrStudents(), section.getName(), exam),
                            new MultiComparable(-exam.getExamType().getType(), exam.getSeatingType(), section.getName(), exam),
                            new MultiComparable(-exam.getExamType().getType(), getMeetingComparable(section), section.getName(), exam),
                            new MultiComparable(-exam.getExamType().getType(), exam.getPeriodOrd(), section.getName(), exam),
                            new MultiComparable(-exam.getExamType().getType(), exam.getPeriod()==null?-1:exam.getPeriod().getStartSlot(), section.getName(), exam),
                            new MultiComparable(-exam.getExamType().getType(), exam.getRoomsName(":"), section.getName(), exam),
                            new MultiComparable(-exam.getExamType().getType(), -exam.getRoomsCapacity(), section.getName(), exam),
                        });
            }
        }
        table.setWebTableTweakStyle(new WebTableTweakStyle() {
			public String getStyleHtml(WebTableLine current, WebTableLine next, int order) {
				if (next!=null && ((MultiComparable)current.getOrderBy()[0]).getContent()[0].compareTo(((MultiComparable)next.getOrderBy()[0]).getContent()[0])!=0)
					return "border-bottom: rgb(81,81,81) 1px dashed";
				return null;
			}
		});
        return table;
    }
    
    public PdfWebTable getInstructorConflits(boolean html, TreeSet<ExamAssignmentInfo> exams, DepartmentalInstructor instructor) {
        String nl = (html?"<br>":"\n");
        boolean showBackToBack = ApplicationProperty.ExaminationReportsInstructorBackToBacks.isTrue();
        PdfWebTable table = new PdfWebTable( 8,
        		showBackToBack ? MSG.sectExaminationConflictsOrBackToBacksForInstructor(instructor.getDepartment().getSession().getLabel(), instructor.getName(DepartmentalInstructor.sNameFormatLastFist)):
        			MSG.sectExaminationConflictsForInstructor(instructor.getDepartment().getSession().getLabel(), instructor.getName(DepartmentalInstructor.sNameFormatLastFist)),
                "personalSchedule.action?o4=%%&q=" + QueryEncoderBackend.encode(instructor.getExternalUniqueId()+ ":"+instructor.getDepartment().getSession().getUniqueId()),
                new String[] {
                    MSG.columnExamType(),
                    XMSG.colOwner(),
                    MSG.columnEnrollment(),
                    MSG.columnExamSeatingType().replace("<br>", nl),
                    MSG.columnExamDate(),
                    MSG.columnExamTime(),
                    MSG.columnExamRoom(),
                    MSG.columnBackToBackDistance()},
                    new String[] {"left","left","right","center","left","left","left","left"},
                    new boolean[] {true,  true, true, true, true, true, true, true} );
        table.setRowStyle("white-space:nowrap");
        String noRoom = ApplicationProperty.ExaminationsNoRoomText.value();
        for (ExamAssignmentInfo exam : exams) {
            for (DirectConflict conflict : exam.getInstructorDirectConflicts()) {
                if (conflict.getOtherExam()!=null && exam.compareTo(conflict.getOtherExam())>=0 && exams.contains(conflict.getOtherExam())) continue;
                String classes = "", enrollment = "", seating = "", date = "", time = "", room = "";
                boolean firstSection = true;
                for (ExamSectionInfo section : exam.getSectionsIncludeCrosslistedDummies()) {
                    if (classes.length()>0) {
                        classes += nl; enrollment += nl; seating += nl; date += nl; time += nl; room += nl;
                    }
                    classes += section.getName();
                    enrollment += String.valueOf(section.getNrStudents());
                    if (firstSection) {
                        seating += Exam.getSeatingTypeLabel(exam.getSeatingType());
                        date += exam.getDate(false);
                        time += exam.getTime(false);
                        room += (exam.getNrRooms()==0?noRoom:exam.getRoomsName(false,", "));
                    }
                    firstSection = false;
                }
                firstSection = true;
                if (conflict.getOtherExam()!=null) {
                    for (ExamSectionInfo section : conflict.getOtherExam().getSectionsIncludeCrosslistedDummies()) {
                        if (classes.length()>0) {
                            classes += nl; enrollment += nl; seating += nl; date += nl; time += nl; room += nl;
                        }
                        classes += section.getName();
                        enrollment += String.valueOf(section.getNrStudents());
                        if (firstSection) {
                            seating += Exam.getSeatingTypeLabel(conflict.getOtherExam().getSeatingType());
                            room += (conflict.getOtherExam().getNrRooms()==0?conflict.getOtherExam():exam.getRoomsName(false,", "));
                        }
                        firstSection = false;
                    }
                } else if (conflict.getOtherEventId()!=null) {
                    classes += nl; enrollment += nl; seating += nl; date += nl; time += nl; room += nl;
                    classes += conflict.getOtherEventName();
                    enrollment += conflict.getOtherEventSize();
                    seating += conflict.isOtherClass()?XMSG.typeClass():XMSG.typeEvent();
                    room += conflict.getOtherEventRoom();
                    //date += conflict.getOtherEventDate();
                    time += conflict.getOtherEventTime(); 
                }
                table.addLine(
                        new String[] {
                            (html?"<font color='"+PreferenceLevel.prolog2color("P")+"'>":"")+(conflict.getOtherEventId()!=null?conflict.isOtherClass()?XMSG.typeClass():XMSG.typeEvent():XMSG.conflictDirect())+(html?"</font>":""),
                            classes,
                            enrollment,
                            seating,
                            date,
                            time,
                            room,
                            ""
                        }, new Comparable[] {
                            new MultiComparable(-exam.getExamType().getType(), 0, exam, 0),
                            new MultiComparable(-exam.getExamType().getType(), exam, exam, 0),
                            new MultiComparable(-exam.getExamType().getType(), -exam.getNrStudents()-(conflict.getOtherExam()==null?0:conflict.getOtherExam().getNrStudents()), exam, 0),
                            new MultiComparable(-exam.getExamType().getType(), exam.getExamTypeLabel(), exam, 0),
                            new MultiComparable(-exam.getExamType().getType(), exam.getPeriodOrd(), exam, 0),
                            new MultiComparable(-exam.getExamType().getType(), exam.getPeriod().getStartSlot(), exam, 0),
                            new MultiComparable(-exam.getExamType().getType(), exam.getRoomsName(":"), exam, 0),
                            new MultiComparable(-exam.getExamType().getType(), -1.0, exam, 0)
                        });
            }
            if (showBackToBack)
                for (BackToBackConflict conflict : exam.getInstructorBackToBackConflicts()) {
                    if (exam.compareTo(conflict.getOtherExam())>=0 && exams.contains(conflict.getOtherExam())) continue;
                    String classes = "", enrollment = "", seating = "", date = "", time = "", room = "";
                    boolean firstSection = true;
                    for (ExamSectionInfo section : exam.getSectionsIncludeCrosslistedDummies()) {
                        if (classes.length()>0) {
                            classes += nl; enrollment += nl; seating += nl; date += nl; time += nl; room += nl;
                        }
                        classes += section.getName();
                        enrollment += String.valueOf(section.getNrStudents());
                        if (firstSection) {
                            seating += Exam.getSeatingTypeLabel(exam.getSeatingType());
                            date += exam.getDate(false);
                            time += exam.getTime(false);
                            room += (exam.getNrRooms()==0?noRoom:exam.getRoomsName(false,", "));
                        }
                        firstSection = false;
                    }
                    firstSection = true;
                    for (ExamSectionInfo section : conflict.getOtherExam().getSectionsIncludeCrosslistedDummies()) {
                        if (classes.length()>0) {
                            classes += nl; enrollment += nl; seating += nl; date += nl; time += nl; room += nl;
                        }
                        classes += section.getName();
                        enrollment += String.valueOf(section.getNrStudents());
                        if (firstSection) {
                            seating += Exam.getSeatingTypeLabel(conflict.getOtherExam().getSeatingType());
                            time += conflict.getOtherExam().getTime(false);
                            room += (conflict.getOtherExam().getNrRooms()==0?conflict.getOtherExam():exam.getRoomsName(false,", "));
                        }
                        firstSection = false;
                    }
                    table.addLine(
                            new String[] {
                                (html?"<font color='"+PreferenceLevel.prolog2color("1")+"'>":"")+XMSG.conflictBackToBack()+(html?"</font>":""),
                                classes,
                                enrollment,
                                seating,
                                date,
                                time,
                                room,
                                MSG.backToBackDistanceInMeters((int)(conflict.getDistance()))
                            }, new Comparable[] {
                                new MultiComparable(-exam.getExamType().getType(), 2, exam, 0),
                                new MultiComparable(-exam.getExamType().getType(), exam, exam, 0),
                                new MultiComparable(-exam.getExamType().getType(), -exam.getNrStudents()-(conflict.getOtherExam()==null?0:conflict.getOtherExam().getNrStudents()), exam, 0),
                                new MultiComparable(-exam.getExamType().getType(), exam.getExamTypeLabel(), exam, 0),
                                new MultiComparable(-exam.getExamType().getType(), exam.getPeriodOrd(), exam, 0),
                                new MultiComparable(-exam.getExamType().getType(), exam.getPeriod().getStartSlot(), exam, 0),
                                new MultiComparable(-exam.getExamType().getType(), exam.getRoomsName(":"), exam, 0),
                                new MultiComparable(-exam.getExamType().getType(), conflict.getDistance(), exam, 0)
                            });
                }
            conflicts: for (MoreThanTwoADayConflict conflict : exam.getInstructorMoreThanTwoADaysConflicts()) {
                for (ExamAssignment other : conflict.getOtherExams())
                    if (exam.compareTo(other)>=0 && exams.contains(other)) continue conflicts;
                String classes = "", enrollment = "", seating = "", date = "", time = "", room = "";
                int nrStudents = exam.getNrStudents();
                boolean firstSection = true;
                for (ExamSectionInfo section : exam.getSectionsIncludeCrosslistedDummies()) {
                    if (classes.length()>0) {
                        classes += nl; enrollment += nl; seating += nl; date += nl; time += nl; room += nl;
                    }
                    classes += section.getName();
                    enrollment += String.valueOf(section.getNrStudents());
                    if (firstSection) {
                        seating += Exam.getSeatingTypeLabel(exam.getSeatingType());
                        date += exam.getDate(false);
                        time += exam.getTime(false);
                        room += (exam.getNrRooms()==0?noRoom:exam.getRoomsName(false,", "));
                    }
                    firstSection = false;
                }
                for (ExamAssignment other : conflict.getOtherExams()) {
                    firstSection = true;
                    nrStudents += other.getNrStudents();
                    for (ExamSectionInfo section : other.getSectionsIncludeCrosslistedDummies()) {
                        if (classes.length()>0) {
                            classes += nl; enrollment += nl; seating += nl; date += nl; time += nl; room += nl;
                        }
                        classes += section.getName();
                        enrollment += String.valueOf(section.getNrStudents());
                        if (firstSection) {
                            seating += Exam.getSeatingTypeLabel(exam.getSeatingType());
                            time += other.getTime(false);
                            room += (other.getNrRooms()==0?noRoom:other.getRoomsName(false,", "));
                        }
                        firstSection = false;
                    }
                }
                table.addLine(
                		(sessionContext.hasPermission(exam, Right.ExaminationDetail) ? "onClick=\"document.location='examDetail.action?examId="+exam.getExamId()+"';\"" : ""),
                        new String[] {
                            (html?"<font color='"+PreferenceLevel.prolog2color("2")+"'>":"")+(html?XMSG.conflictMoreThanTwoADay().replace(">","&gt;"):XMSG.conflictMoreThanTwoADay())+(html?"</font>":""),
                            classes,
                            enrollment,
                            seating,
                            date,
                            time,
                            room,
                            ""
                        }, new Comparable[] {
                            new MultiComparable(-exam.getExamType().getType(), 1, exam, 0),
                            new MultiComparable(-exam.getExamType().getType(), exam, exam, 0),
                            new MultiComparable(-exam.getExamType().getType(), -nrStudents, exam, 0),
                            new MultiComparable(-exam.getExamType().getType(), exam.getExamTypeLabel(), exam, 0),
                            new MultiComparable(-exam.getExamType().getType(), exam.getPeriodOrd(), exam, 0),
                            new MultiComparable(-exam.getExamType().getType(), exam.getPeriod().getStartSlot(), exam, 0),
                            new MultiComparable(-exam.getExamType().getType(), exam.getRoomsName(":"), exam, 0),
                            new MultiComparable(-exam.getExamType().getType(), -1.0, exam, 0)
                        },
                        exam.getExamId().toString());
            }
        }
        table.setWebTableTweakStyle(new WebTableTweakStyle() {
			public String getStyleHtml(WebTableLine current, WebTableLine next, int order) {
				if (next!=null && ((MultiComparable)current.getOrderBy()[0]).getContent()[0].compareTo(((MultiComparable)next.getOrderBy()[0]).getContent()[0])!=0)
					return "border-bottom: rgb(81,81,81) 1px dashed";
				return null;
			}
		});
        return table;
    }
    
    public PdfWebTable getStudentConflits(boolean html, TreeSet<ExamAssignmentInfo> exams, DepartmentalInstructor instructor) {
        String nl = (html?"<br>":"\n");
        PdfWebTable table = new PdfWebTable( 8,
        		MSG.sectExaminationConflictsForStudent(instructor.getDepartment().getSession().getLabel(), instructor.getName(DepartmentalInstructor.sNameFormatLastFist)),
                "personalSchedule.action?o5=%%&q=" + QueryEncoderBackend.encode(instructor.getExternalUniqueId()+ ":"+instructor.getDepartment().getSession().getUniqueId()),
                new String[] {
                    MSG.columnStudentName(),
                    MSG.columnExamType(),
                    XMSG.colOwner(),
                    MSG.columnEnrollment(),
                    MSG.columnExamSeatingType().replace("<br>", nl),
                    MSG.columnExamDate(),
                    MSG.columnExamTime(),
                    MSG.columnExamRoom()},
                    new String[] {"left","left","left","right","center","left","left","left"},
                    new boolean[] {true, true,  true, true, true, true, true, true} );
        table.setRowStyle("white-space:nowrap");
        String noRoom = ApplicationProperty.ExaminationsNoRoomText.value();
        table.setBlankWhenSame(true);
        for (ExamAssignmentInfo exam : exams) {
            for (DirectConflict conflict : exam.getDirectConflicts()) {
                if (conflict.getOtherExam()!=null && exam.compareTo(conflict.getOtherExam())>=0 && exams.contains(conflict.getOtherExam())) continue;
                for (Long studentId : conflict.getStudents()) {
                    Student student = StudentDAO.getInstance().get(studentId);
                    String id = student.getExternalUniqueId();
                    String name = student.getName(DepartmentalInstructor.sNameFormatLastFist);
                    String classes = "", enrollment = "", seating = "", date = "", time = "", room = "";
                    boolean firstSection = true;
                    for (ExamSectionInfo section : exam.getSectionsIncludeCrosslistedDummies()) {
                        if (!section.getStudentIds().contains(studentId)) continue;
                        if (classes.length()>0) {
                            classes += nl; enrollment += nl; seating += nl; date += nl; time += nl; room += nl;
                        }
                        classes += section.getName();
                        enrollment += String.valueOf(section.getNrStudents());
                        if (firstSection) {
                            seating += Exam.getSeatingTypeLabel(exam.getSeatingType());
                            date += exam.getDate(false);
                            time += exam.getTime(false);
                            room += (exam.getNrRooms()==0?noRoom:exam.getRoomsName(false,", "));
                        }
                        firstSection = false;
                    }
                    firstSection = true;
                    if (conflict.getOtherExam()!=null) {
                        for (ExamSectionInfo section : conflict.getOtherExam().getSectionsIncludeCrosslistedDummies()) {
                            if (!section.getStudentIds().contains(studentId)) continue;
                            if (classes.length()>0) {
                                classes += nl; enrollment += nl; seating += nl; date += nl; time += nl; room += nl;
                            }
                            classes += section.getName();
                            enrollment += String.valueOf(section.getNrStudents());
                            if (firstSection) {
                                seating += Exam.getSeatingTypeLabel(exam.getSeatingType());
                                room += (conflict.getOtherExam().getNrRooms()==0?noRoom:conflict.getOtherExam().getRoomsName(false,", "));
                            }
                            firstSection = false;
                        }
                    } else if (conflict.getOtherEventId()!=null) {
                        classes += nl; enrollment += nl; seating += nl; date += nl; time += nl; room += nl;
                        classes += conflict.getOtherEventName();
                        enrollment += conflict.getOtherEventSize();
                        seating += conflict.isOtherClass()?XMSG.typeClass():XMSG.typeEvent();
                        room += conflict.getOtherEventRoom();
                        //date += conflict.getOtherEventDate();
                        time += conflict.getOtherEventTime(); 
                    }
                    table.addLine(
                            new String[] {
                                name,
                                (html?"<font color='"+PreferenceLevel.prolog2color("P")+"'>":"")+XMSG.conflictDirect()+(html?"</font>":""),
                                classes,
                                enrollment,
                                seating,
                                date,
                                time,
                                room
                            }, new Comparable[] {
                                new MultiComparable(-exam.getExamType().getType(), name,id, exam, 0),
                                new MultiComparable(-exam.getExamType().getType(), 0, exam, 0),
                                new MultiComparable(-exam.getExamType().getType(), exam, exam, 0),
                                new MultiComparable(-exam.getExamType().getType(), -exam.getNrStudents()-(conflict.getOtherExam()==null?0:conflict.getOtherExam().getNrStudents()), exam, 0),
                                new MultiComparable(-exam.getExamType().getType(), exam.getExamTypeLabel(), exam, 0),
                                new MultiComparable(-exam.getExamType().getType(), exam.getPeriodOrd(), exam, 0),
                                new MultiComparable(-exam.getExamType().getType(), (exam.getPeriod() == null ? -1 : exam.getPeriod().getStartSlot()), exam, 0),
                                new MultiComparable(-exam.getExamType().getType(), exam.getRoomsName(":"), exam, 0)
                            });
                }
            }
            /*
            for (BackToBackConflict conflict : exam.getBackToBackConflicts()) {
                if (exam.compareTo(conflict.getOtherExam())>=0 && exams.contains(conflict.getOtherExam())) continue;
                for (Long studentId : conflict.getStudents()) {
                    Student student = StudentDAO.getInstance().get(studentId);
                    String id = student.getExternalUniqueId();
                    String name = student.getName(DepartmentalInstructor.sNameFormatLastFist);
                    String classes = "", enrollment = "", seating = "", date = "", time = "", room = "", distance = "", blank="";
                    boolean firstSection = true;
                    for (ExamSectionInfo section : exam.getSectionsIncludeCrosslistedDummies()) {
                        if (!section.getStudentIds().contains(studentId)) continue;
                        if (classes.length()>0) {
                            blank+=nl; classes += nl; enrollment += nl; seating += nl; date += nl; time += nl; room += nl; distance += nl;
                        }
                        classes += section.getName();
                        enrollment += String.valueOf(section.getNrStudents());
                        if (firstSection) {
                            seating += Exam.getSeatingTypeLabel(exam.getSeatingType());
                            date += exam.getDate(false);
                            time += exam.getTime(false);
                            room += (exam.getNrRooms()==0?noRoom:exam.getRoomsName(false,", "));
                        }
                        firstSection = false;
                    }
                    firstSection = true;
                    for (ExamSectionInfo section : conflict.getOtherExam().getSectionsIncludeCrosslistedDummies()) {
                        if (!section.getStudentIds().contains(studentId)) continue;
                        if (classes.length()>0) {
                            blank+=nl; classes += nl; enrollment += nl; seating += nl; date += nl; time += nl; room += nl; distance += nl;
                        }
                        classes += section.getName();
                        enrollment += String.valueOf(section.getNrStudents());
                        if (firstSection) {
                            seating += Exam.getSeatingTypeLabel(exam.getSeatingType());
                            time += conflict.getOtherExam().getTime(false);
                            room += (conflict.getOtherExam().getNrRooms()==0?noRoom:conflict.getOtherExam().getRoomsName(false,", "));
                        }
                        firstSection = false;
                    }
                    table.addLine(
                            new String[] {
                                name,
                                (html?"<font color='"+PreferenceLevel.prolog2color("1")+"'>":"")+XMSG.conflictBackToBack()+(html?"</font>":""),
                                classes,
                                enrollment,
                                seating,
                                date,
                                time,
                                room
                            }, new Comparable[] {
                                new MultiComparable(-exam.getExamType(), name, id, exam, 0),
                                new MultiComparable(-exam.getExamType(), 2, exam, 0),
                                new MultiComparable(-exam.getExamType(), exam, exam, 0),
                                new MultiComparable(-exam.getExamType(), -exam.getNrStudents()-conflict.getOtherExam().getNrStudents(), exam, 0),
                                new MultiComparable(-exam.getExamType(), exam.getExamType(), exam, 0),
                                new MultiComparable(-exam.getExamType(), exam.getPeriodOrd(), exam, 0),
                                new MultiComparable(-exam.getExamType(), exam.getPeriod().getStartSlot(), exam, 0),
                                new MultiComparable(-exam.getExamType(), exam.getRoomsName(":"), exam, 0)
                            });
                }
            }
            */
            conflicts: for (MoreThanTwoADayConflict conflict : exam.getMoreThanTwoADaysConflicts()) {
                for (ExamAssignment other : conflict.getOtherExams())
                    if (exam.compareTo(other)>=0 && exams.contains(other)) continue conflicts;
                for (Long studentId : conflict.getStudents()) {
                    Student student = StudentDAO.getInstance().get(studentId);
                    String id = student.getExternalUniqueId();
                    String name = student.getName(DepartmentalInstructor.sNameFormatLastFist);
                    String classes = "", enrollment = "", seating = "", date = "", time = "", room = "";
                    int nrStudents = exam.getNrStudents();
                    boolean firstSection = true;
                    for (ExamSectionInfo section : exam.getSectionsIncludeCrosslistedDummies()) {
                        if (!section.getStudentIds().contains(studentId)) continue;
                        if (classes.length()>0) {
                            classes += nl; enrollment += nl; seating += nl; date += nl; time += nl; room += nl;
                        }
                        classes += section.getName();
                        enrollment += String.valueOf(section.getNrStudents());
                        if (firstSection) {
                            seating += Exam.getSeatingTypeLabel(exam.getSeatingType());
                            date += exam.getDate(false);
                            time += exam.getTime(false);
                            room += (exam.getNrRooms()==0?noRoom:exam.getRoomsName(false,", "));
                        }
                        firstSection = false;
                    }
                    for (ExamAssignment other : conflict.getOtherExams()) {
                        firstSection = true;
                        nrStudents += other.getNrStudents();
                        for (ExamSectionInfo section : other.getSectionsIncludeCrosslistedDummies()) {
                            if (!section.getStudentIds().contains(studentId)) continue;
                            if (classes.length()>0) {
                                classes += nl; enrollment += nl; seating += nl; date += nl; time += nl; room += nl;
                            }
                            classes += section.getName();
                            enrollment += String.valueOf(section.getNrStudents());
                            if (firstSection) {
                                seating += Exam.getSeatingTypeLabel(exam.getSeatingType());
                                time += other.getTime(false);
                                room += (other.getNrRooms()==0?noRoom:other.getRoomsName(false,", "));
                            }
                            firstSection = false;
                        }
                    }
                    table.addLine(
                            new String[] {
                                name,
                                (html?"<font color='"+PreferenceLevel.prolog2color("2")+"'>":"")+(html?XMSG.conflictMoreThanTwoADay().replace(">","&gt;"):XMSG.conflictMoreThanTwoADay())+(html?"</font>":""),
                                classes,
                                enrollment,
                                seating,
                                date,
                                time,
                                room
                            }, new Comparable[] {
                                new MultiComparable(-exam.getExamType().getType(), name, id, exam, 0),
                                new MultiComparable(-exam.getExamType().getType(), 1, exam, 0),
                                new MultiComparable(-exam.getExamType().getType(), exam, exam, 0),
                                new MultiComparable(-exam.getExamType().getType(), -nrStudents, exam, 0),
                                new MultiComparable(-exam.getExamType().getType(), exam.getExamTypeLabel(), exam, 0),
                                new MultiComparable(-exam.getExamType().getType(), exam.getPeriodOrd(), exam, 0),
                                new MultiComparable(-exam.getExamType().getType(), exam.getPeriod().getStartSlot(), exam, 0),
                                new MultiComparable(-exam.getExamType().getType(), exam.getRoomsName(":"), exam, 0),
                            });
                }
            }
        }
        table.setWebTableTweakStyle(new WebTableTweakStyle() {
			public String getStyleHtml(WebTableLine current, WebTableLine next, int order) {
				if (next!=null && ((MultiComparable)current.getOrderBy()[0]).getContent()[0].compareTo(((MultiComparable)next.getOrderBy()[0]).getContent()[0])!=0)
					return "border-bottom: rgb(81,81,81) 1px solid";
				return null;
			}
		});
        return table;       
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
    
    public PdfWebTable getStudentClassSchedule(boolean html, Student student) {
        String nl = (html?"<br>":"\n");
        PdfWebTable table = new PdfWebTable( 6,
        		MSG.sectClassScheduleForStudent(student.getSession().getLabel(), student.getName(DepartmentalInstructor.sNameFormatLastFist)),
                "personalSchedule.action?o6=%%&q=" + QueryEncoderBackend.encode(student.getExternalUniqueId()+ ":"+student.getSession().getUniqueId()),
                new String[] {
                    MSG.columnCourse(),
                    MSG.columnInstructionType().replace("\n", nl),
                    MSG.columnSection(),
                    MSG.columnAssignedTime(),
                    MSG.columnAssignedRoom(),
                    MSG.columnInstructor()
                    },
                new String[] {"left", "left", "left", "left", "left", "left"},
                new boolean[] {true, true, true, true, true, true} );
        table.setRowStyle("white-space:nowrap");
        table.setBlankWhenSame(true);
        boolean suffix = ApplicationProperty.ExaminationReportsClassSufix.isTrue();
        for (Iterator i=student.getClassEnrollments().iterator();i.hasNext();) {
            StudentClassEnrollment sce = (StudentClassEnrollment)i.next();
            String course = sce.getCourseOffering().getCourseName();
            String itype =  sce.getClazz().getSchedulingSubpart().getItypeDesc();
            int itypeCmp = sce.getClazz().getSchedulingSubpart().getItype().getItype();
            String section = (suffix && sce.getClazz().getClassSuffix()!=null?sce.getClazz().getClassSuffix():sce.getClazz().getSectionNumberString());
            String time = getMeetingTime(sce.getClazz());
            long timeCmp = getMeetingComparable(sce.getClazz());
            String room = getMeetingRooms(html, sce.getClazz());
            String instr = getMeetingInstructor(sce.getClazz());
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
        return table;        
    }
    
    public PdfWebTable getInstructorClassSchedule(boolean html, DepartmentalInstructor instructor) {
        String nl = (html?"<br>":"\n");
        PdfWebTable table = new PdfWebTable( 6,
        		MSG.sectClassScheduleForInstructor(instructor.getDepartment().getSession().getLabel(), instructor.getName(DepartmentalInstructor.sNameFormatLastFist)),
                "personalSchedule.action?o7=%%&q=" + QueryEncoderBackend.encode(instructor.getExternalUniqueId()+ ":"+instructor.getDepartment().getSession().getUniqueId()),
                new String[] {
                    MSG.columnCourse(),
                    MSG.columnInstructionType().replace("\n", nl),
                    MSG.columnSection(),
                    MSG.columnAssignedTime(),
                    MSG.columnAssignedRoom(),
                    MSG.columnShare()
                    },
                new String[] {"left", "left", "left", "left", "left", "left"},
                new boolean[] {true, true, true, true, true, true} );
        Set allClasses = new HashSet();
        for (Iterator i=DepartmentalInstructor.getAllForInstructor(instructor, instructor.getDepartment().getSession().getUniqueId()).iterator();i.hasNext();) {
            DepartmentalInstructor di = (DepartmentalInstructor)i.next();
            allClasses.addAll(di.getClasses());
        }
        table.setRowStyle("white-space:nowrap");
        table.setBlankWhenSame(true);
        boolean suffix = ApplicationProperty.ExaminationReportsClassSufix.isTrue();
        for (Iterator i=allClasses.iterator();i.hasNext();) {
            ClassInstructor ci = (ClassInstructor)i.next();
            String course = ci.getClassInstructing().getSchedulingSubpart().getControllingCourseOffering().getCourseName();
            String itype =  ci.getClassInstructing().getSchedulingSubpart().getItypeDesc();
            int itypeCmp = ci.getClassInstructing().getSchedulingSubpart().getItype().getItype();
            String section = (suffix && ci.getClassInstructing().getClassSuffix()!=null?ci.getClassInstructing().getClassSuffix():ci.getClassInstructing().getSectionNumberString());
            String time = getMeetingTime(ci.getClassInstructing());
            long timeCmp = getMeetingComparable(ci.getClassInstructing());
            String room = getMeetingRooms(html, ci.getClassInstructing());
            String share = ci.getPercentShare()+"%";
            if (html && ci.isLead()) share = "<b>"+share+"</b>";
            table.addLine(
                    new String[] {
                            course,
                            itype,
                            section,
                            time,
                            room,
                            share
                        },
                        new Comparable[] {
                            new MultiComparable(course, itypeCmp, section, timeCmp, room, ""),
                            new MultiComparable(itypeCmp,  course,section, timeCmp, room, ""),
                            new MultiComparable(course, section, itypeCmp, timeCmp, room, ""),
                            new MultiComparable(timeCmp, room, course, itypeCmp, section, ""),
                            new MultiComparable(room, timeCmp, course, itypeCmp, section, ""),
                            new MultiComparable(-ci.getPercentShare(), course, itypeCmp, section, timeCmp, room, "")
                        });
            if (ci.getClassInstructing().getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering().getCourseOfferings().size()>1) {
            	String clist = "";
            	for (Iterator j=ci.getClassInstructing().getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering().getCourseOfferings().iterator();j.hasNext();) {
            		CourseOffering co = (CourseOffering)j.next();
            		if (co.isIsControl()) continue;
            		if (clist.length()>0) clist += ", ";
            		String xcourse = co.getCourseName();
            		String xsection = (suffix && ci.getClassInstructing().getClassSuffix(co)!=null?ci.getClassInstructing().getClassSuffix(co):ci.getClassInstructing().getSectionNumberString());
                	table.addLine(
                            new String[] {
                                    "&nbsp;&nbsp;&nbsp;w / "+xcourse,
                                    "",
                                    (section.equals(xsection)?"":xsection),
                                    "",
                                    "",
                                    ""
                                },
                                new Comparable[] {
                                    new MultiComparable(course, itypeCmp, section, timeCmp, room, xcourse),
                                    new MultiComparable(itypeCmp,  course,section, timeCmp, room, xcourse),
                                    new MultiComparable(course, section, itypeCmp, timeCmp, room, xcourse),
                                    new MultiComparable(timeCmp, room, course, itypeCmp, section, xcourse),
                                    new MultiComparable(room, timeCmp, course, itypeCmp, section, xcourse),
                                    new MultiComparable(-ci.getPercentShare(), course, itypeCmp, section, timeCmp, room, xcourse)
                                });
            	}
            }
        }
        return table;        
    }
    
    public void printStudentSchedule(File file, Student student, HashSet<ExamOwner> exams) throws Exception {
        PrintWriter out = null;
        try {
            SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
            df.setTimeZone(TimeZone.getTimeZone("UTC"));
            SimpleDateFormat tf = new SimpleDateFormat("HHmmss");
            tf.setTimeZone(TimeZone.getTimeZone("UTC"));
            out = new PrintWriter(new FileWriter(file));
            out.println("BEGIN:VCALENDAR");
            out.println("VERSION:2.0");
            out.println("CALSCALE:GREGORIAN");
            out.println("METHOD:PUBLISH");
            out.println("X-WR-CALNAME:"+student.getName(DepartmentalInstructor.sNameFormatLastFist));
            out.println("X-WR-TIMEZONE:"+TimeZone.getDefault().getID());
            out.println("PRODID:-//UniTime "+Constants.getVersion()+"/UniTime Personal Schedule//NONSGML v1.0//EN");
            if (student.getSession().canNoRoleReportClass()) {
                for (Iterator i=student.getClassEnrollments().iterator();i.hasNext();) {
                    StudentClassEnrollment sce = (StudentClassEnrollment)i.next();
                    if (sce.getClazz().getEvent()!=null) {
                        for (Iterator k=sce.getClazz().getEvent().getMeetings().iterator();k.hasNext();) {
                            Meeting meeting = (Meeting)k.next();
                            out.println("BEGIN:VEVENT");
                            out.println("UID:m"+meeting.getUniqueId());
                            out.println("DTSTART:"+df.format(meeting.getStartTime())+"T"+tf.format(meeting.getStartTime())+"Z");
                            out.println("DTEND:"+df.format(meeting.getStopTime())+"T"+tf.format(meeting.getStopTime())+"Z");
                            out.println("SUMMARY:"+sce.getClazz().getClassLabel(sce.getCourseOffering())+" ("+meeting.getEvent().getEventTypeLabel()+")");
                            if (meeting.getLocation()!=null)
                                out.println("LOCATION:"+meeting.getLocation().getLabel());
                            out.println("END:VEVENT");
                        }
                        
                    }
                }
            }
            for (ExamOwner examOwner: exams) {
            	Exam exam = examOwner.getExam();
                if (exam.getAssignedPeriod()==null) continue;
                for (ExamSectionInfo section: new ExamAssignment(exam).getSectionsIncludeCrosslistedDummies()) {
                    if (section.getStudentIds().contains(student.getUniqueId())) {
                        out.println("BEGIN:VEVENT");
                        out.println("UID:x"+exam.getUniqueId());
                        out.println("DTSTART:"+df.format(exam.getAssignedPeriod().getStartTime())+"T"+tf.format(exam.getAssignedPeriod().getStartTime())+"Z");
                        Calendar endTime = Calendar.getInstance(); endTime.setTime(exam.getAssignedPeriod().getStartTime());
                        endTime.add(Calendar.MINUTE, exam.getLength());
                        out.println("DTEND:"+df.format(endTime.getTime())+"T"+tf.format(endTime.getTime())+"Z");
                        out.println("SUMMARY:"+section.getName()+" ("+exam.getExamType().getLabel()+" Exam)");
                        //out.println("DESCRIPTION:"+exam.getExamName()+" ("+Exam.sExamTypes[exam.getExamType()]+" Exam)");
                        if (!exam.getAssignedRooms().isEmpty()) {
                            String rooms = "";
                            for (Iterator i=new TreeSet(exam.getAssignedRooms()).iterator();i.hasNext();) {
                                Location location = (Location)i.next();
                                if (rooms.length()>0) rooms+=", ";
                                rooms+=location.getLabel();
                            }
                            out.println("LOCATION:"+rooms);
                        }
                        out.println("END:VEVENT");
                    }
                }
            }
            out.println("END:VCALENDAR");
        } finally {
            if (out!=null) { out.flush(); out.close(); }
        }
    }
    
    public void printInstructorSchedule(File file, DepartmentalInstructor instructor, HashSet<Exam> exams) throws Exception {
        PrintWriter out = null;
        try {
            SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
            df.setTimeZone(TimeZone.getTimeZone("UTC"));
            SimpleDateFormat tf = new SimpleDateFormat("HHmmss");
            tf.setTimeZone(TimeZone.getTimeZone("UTC"));
            out = new PrintWriter(new FileWriter(file));
            out.println("BEGIN:VCALENDAR");
            out.println("VERSION:2.0");
            out.println("CALSCALE:GREGORIAN");
            out.println("METHOD:PUBLISH");
            out.println("X-WR-CALNAME:"+instructor.getName(DepartmentalInstructor.sNameFormatLastFist));
            out.println("X-WR-TIMEZONE:"+TimeZone.getDefault().getID());
            out.println("PRODID:-//UniTime "+Constants.getVersion()+"/UniTime Personal Schedule//NONSGML v1.0//EN");
            if (instructor.getDepartment().getSession().canNoRoleReportClass()) {
                for (Iterator i=DepartmentalInstructor.getAllForInstructor(instructor, instructor.getDepartment().getSession().getUniqueId()).iterator();i.hasNext();) {
                    DepartmentalInstructor di = (DepartmentalInstructor)i.next();
                    for (Iterator j=di.getClasses().iterator();j.hasNext();) {
                        ClassInstructor ci = (ClassInstructor)j.next();
                        if (ci.getClassInstructing().getEvent()!=null) {
                            for (Iterator k=ci.getClassInstructing().getEvent().getMeetings().iterator();k.hasNext();) {
                                Meeting meeting = (Meeting)k.next();
                                out.println("BEGIN:VEVENT");
                                out.println("UID:m"+meeting.getUniqueId());
                                out.println("DTSTART:"+df.format(meeting.getStartTime())+"T"+tf.format(meeting.getStartTime())+"Z");
                                out.println("DTEND:"+df.format(meeting.getStopTime())+"T"+tf.format(meeting.getStopTime())+"Z");
                                out.println("SUMMARY:"+meeting.getEvent().getEventName()+" ("+meeting.getEvent().getEventTypeLabel()+")");
                                if (meeting.getLocation()!=null)
                                    out.println("LOCATION:"+meeting.getLocation().getLabel());
                                out.println("END:VEVENT");
                            }
                        }
                    }
                }
            }
            for (Exam exam: exams) {
                if (exam.getAssignedPeriod()==null) continue;
                out.println("BEGIN:VEVENT");
                out.println("UID:x"+exam.getUniqueId());
                out.println("DTSTART:"+df.format(exam.getAssignedPeriod().getStartTime())+"T"+tf.format(exam.getAssignedPeriod().getStartTime())+"Z");
                Calendar endTime = Calendar.getInstance(); endTime.setTime(exam.getAssignedPeriod().getStartTime());
                endTime.add(Calendar.MINUTE, exam.getLength());
                out.println("DTEND:"+df.format(endTime.getTime())+"T"+tf.format(endTime.getTime())+"Z");
                out.println("SUMMARY:"+exam.getLabel()+" ("+exam.getExamType().getLabel()+" Exam)");
                //out.println("DESCRIPTION:"+exam.getExamName()+" ("+Exam.sExamTypes[exam.getExamType()]+" Exam)");
                if (!exam.getAssignedRooms().isEmpty()) {
                    String rooms = "";
                    for (Iterator i=new TreeSet(exam.getAssignedRooms()).iterator();i.hasNext();) {
                        Location location = (Location)i.next();
                        if (rooms.length()>0) rooms+=", ";
                        rooms+=location.getLabel();
                    }
                    out.println("LOCATION:"+rooms);
                }
                out.println("END:VEVENT");                
            }
            out.println("END:VCALENDAR");
        } finally {
            if (out!=null) { out.flush(); out.close(); }
        }
    }
}
