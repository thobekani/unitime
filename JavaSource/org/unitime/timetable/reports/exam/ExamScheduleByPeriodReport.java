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
package org.unitime.timetable.reports.exam;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.unitime.timetable.model.ExamPeriod;
import org.unitime.timetable.model.ExamType;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.solver.exam.ui.ExamAssignmentInfo;
import org.unitime.timetable.solver.exam.ui.ExamRoomInfo;
import org.unitime.timetable.solver.exam.ui.ExamInfo.ExamSectionInfo;

import com.lowagie.text.DocumentException;

/**
 * @author Tomas Muller
 */
public class ExamScheduleByPeriodReport extends PdfLegacyExamReport {
	private static Log sLog = LogFactory.getLog(ExamScheduleByPeriodReport.class);
    
    public ExamScheduleByPeriodReport(int mode, File file, Session session, ExamType examType, Collection<SubjectArea> subjectAreas, Collection<ExamAssignmentInfo> exams) throws IOException, DocumentException {
        super(mode, file, MSG.legacyReportScheduleByPeriod(), session, examType, subjectAreas, exams);
    }

    
    public void printReport() throws DocumentException {
    	setHeaderLine(
    			new Line(
    					rpad(MSG.lrDateAndTime(), 30),
    	    			rpad(MSG.lrSubject(), 7),
    	    			rpad(MSG.lrCourse(), 8),
    	    			(iItype ? rpad(iExternal ? MSG.lrExtnID() : MSG.lrType(), 6) : NULL),
    	    			rpad(MSG.lrSection(), 10),
    	    			rpad(MSG.lrMeetingTimes(), 35),
    	    			lpad(MSG.lrEnrl(), 5),
    	    			(iDispRooms ? rpad(MSG.lrRoom(), 11) : NULL),
    	    			(iDispRooms ? lpad(MSG.lrCap(), 5) : NULL),
    	    			(iDispRooms ? lpad(MSG.lrExCap(), 6) : NULL)
    			), new Line(
    					lpad("", '-', 30),
    	    			lpad("", '-', 7),
    	    			lpad("", '-', 8),
    	    			(iItype ? lpad("", '-', 6) : NULL),
    	    			lpad("", '-', 10),
    	    			lpad("", '-', 35),
    	    			lpad("", '-', 5),
    	    			(iDispRooms ? lpad("", '-', 11) : NULL),
    	    			(iDispRooms ? lpad("", '-', 5) : NULL),
    	    			(iDispRooms ? lpad("", '-', 6) : NULL)
    			));
        printHeader();
        sLog.debug(MSG.statusSortingExams());
        TreeSet<ExamAssignmentInfo> exams = new TreeSet();
        for (ExamAssignmentInfo exam : getExams()) {
            if (exam.getPeriod()==null || !hasSubjectArea(exam)) continue;
            exams.add(exam);
        }
        sLog.debug(MSG.statusPrintingReport());
        for (Iterator p=ExamPeriod.findAll(getSession().getUniqueId(), getExamType()).iterator();p.hasNext();) {
            ExamPeriod period = (ExamPeriod)p.next();
            iPeriodPrinted = false;
            setPageName(formatPeriod(period));
            setCont(formatPeriod(period));
            for (Iterator<ExamAssignmentInfo> i = exams.iterator(); i.hasNext();) {
                ExamAssignmentInfo exam = i.next();
                if (!period.equals(exam.getPeriod())) continue;
                if (iPeriodPrinted) {
                    if (!iNewPage) println(new Line());
                }
                ExamSectionInfo lastSection = null;
                for (Iterator<ExamSectionInfo> j = exam.getSectionsIncludeCrosslistedDummies().iterator(); j.hasNext();) {
                    ExamSectionInfo  section = j.next();
                    if (!hasSubjectArea(section)) continue;
                    iSubjectPrinted = iCoursePrinted = iStudentPrinted = false;
                    if (lastSection!=null) {
                        if (section.getSubject().equals(lastSection.getSubject())) {
                            iSubjectPrinted = true;
                            if (section.getCourseNbr().equals(lastSection.getCourseNbr())) {
                                iCoursePrinted = true;
                                if (section.getItype().equals(lastSection.getItype())) {
                                    iStudentPrinted = true;
                                }
                            }
                        }
                    } 
                    lastSection = section;

                    if (!iDispRooms) {
                        println(
                            rpad(iPeriodPrinted && isSkipRepeating()?"":formatPeriod(period),30),
                            rpad(iSubjectPrinted && isSkipRepeating()?"":section.getSubject(),7),
                            rpad(iCoursePrinted && isSkipRepeating()?"":section.getCourseNbr(), 8),
                            (iItype?rpad(iStudentPrinted && isSkipRepeating()?"":section.getItype(), 6): NULL),
                            formatSection10(section.getSection()),
                            rpad(getMeetingTime(section),35),
                            lpad(String.valueOf(section.getNrStudents()),5)
                            );
                        iPeriodPrinted = !iNewPage;
                    } else {
                        if (section.getExamAssignment().getRooms()==null || section.getExamAssignment().getRooms().isEmpty()) {
                            println(
                                    rpad(iPeriodPrinted && isSkipRepeating()?"":formatPeriod(period),30),
                                    rpad(iSubjectPrinted && isSkipRepeating()?"":section.getSubject(),7),
                                    rpad(iCoursePrinted && isSkipRepeating()?"":section.getCourseNbr(), 8),
                                    (iItype?rpad(iStudentPrinted && isSkipRepeating()?"":section.getItype(), 6):NULL),
                                    formatSection10(section.getSection()),
                                    rpad(getMeetingTime(section),35),
                                    lpad(String.valueOf(section.getNrStudents()),5),
                                    new Cell(iNoRoom).withColSpan(3)
                                    );
                            iPeriodPrinted = !iNewPage;
                        } else {
                            if (getLineNumber()+section.getExamAssignment().getRooms().size()>getNrLinesPerPage() && getNrLinesPerPage() > 0) newPage();
                            boolean firstRoom = true;
                            for (ExamRoomInfo room : section.getExamAssignment().getRooms()) {
                                println(
                                        rpad(!firstRoom || iPeriodPrinted && isSkipRepeating()?"":formatPeriod(period),30),
                                        rpad(!firstRoom || iSubjectPrinted && isSkipRepeating()?"":section.getSubject(),7),
                                        rpad(!firstRoom || iCoursePrinted && isSkipRepeating()?"":section.getCourseNbr(), 8),
                                        (iItype?rpad(!firstRoom || iStudentPrinted && isSkipRepeating()?"":section.getItype(), 6):NULL),
                                        formatSection10(!firstRoom && isSkipRepeating()?"":section.getSection()),
                                        rpad(!firstRoom?new Cell(""):getMeetingTime(section),35),
                                        lpad(!firstRoom && isSkipRepeating()?"":String.valueOf(section.getNrStudents()),5),
                                        formatRoom(room),
                                        lpad(""+room.getCapacity(),5),
                                        lpad(""+room.getExamCapacity(),6)
                                        );
                                firstRoom = false;
                            }
                            iPeriodPrinted = !iNewPage;
                        }
                    }
                    
                }
            }
            setCont(null);
            if (iPeriodPrinted && p.hasNext()) {
                newPage();
            }
        }
        if (iPeriodPrinted) lastPage();
    }
}
