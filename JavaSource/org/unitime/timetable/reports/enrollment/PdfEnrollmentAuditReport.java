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
package org.unitime.timetable.reports.enrollment;

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.List;
import java.util.TreeSet;

import org.hibernate.query.Query;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.ExaminationMessages;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.dao.StudentClassEnrollmentDAO;
import org.unitime.timetable.reports.AbstractReport;

import com.lowagie.text.DocumentException;

/**
 * @author Stephanie Schluttenhofer
 *
 */
public abstract class PdfEnrollmentAuditReport extends AbstractReport {
	protected static ExaminationMessages MSG = Localization.create(ExaminationMessages.class);
    public static Hashtable<String,Class> sRegisteredReports = new Hashtable<String, Class>();
    public static String sAllRegisteredReports = "";
	protected static int studentIdLength = 10;
	protected static int studentNameLength = 23;
	protected static int offeringNameLength = 45;
	protected static int classNameLength = 14;
	protected static int itypeLength = 7;
	protected static int multipleClassesLength = 30;


    static {
        sRegisteredReports.put("struct", EnrollmentsViolatingCourseStructureAuditReport.class);
        sRegisteredReports.put("missing", MissingCourseEnrollmentsAuditReport.class);
        sRegisteredReports.put("many-subp", MultipleCourseEnrollmentsAuditReport.class);
        sRegisteredReports.put("many-conf", MultipleConfigEnrollmentsAuditReport.class);
        for (String report : sRegisteredReports.keySet())
            sAllRegisteredReports += (sAllRegisteredReports.length()>0?",":"") + report;
    }

    private Session iSession = null;
    private boolean iShowId;
    private boolean iShowName;
    private TreeSet<SubjectArea> iSubjectAreas;

	/**
	 * @param mode
	 * @param file
	 * @param title
	 * @param title2
	 * @param subject
	 * @param session
	 * @throws IOException
	 * @throws DocumentException
	 */
	public PdfEnrollmentAuditReport(int mode, File file, String title,
			String title2, String subject, String session) throws IOException,
			DocumentException {
		super(mode, file, title, title2, subject, session);
	}

    public PdfEnrollmentAuditReport(int mode, String title, File file, Session session, TreeSet<SubjectArea> subjectAreas, String subTitle) throws DocumentException, IOException {
        super(mode, file, title, subTitle, title + " -- " + session.getLabel(), session.getLabel());
        this.iSession = session;
        this.iSubjectAreas = subjectAreas;
    }

    public PdfEnrollmentAuditReport(int mode, String title, File file, Session session) throws DocumentException, IOException {
    	super(mode, file, title, "", title + " -- " + session.getLabel(), session.getLabel());
        iSession = session;
        this.iSubjectAreas = null;
    }

   public abstract void printReport() throws DocumentException;

/**
 * @return the iSession
 */
public Session getSession() {
	return iSession;
}

/**
 * @param iSession the iSession to set
 */
public void setSession(Session session) {
	this.iSession = session;
}

/**
 * @return the showId
 */
public boolean isShowId() {
	return iShowId;
}

/**
 * @param showId the showId to set
 */
public void setShowId(boolean showId) {
	this.iShowId = showId;
}

/**
 * @return the showName
 */
public boolean isShowName() {
	return iShowName;
}

/**
 * @param showName the showName to set
 */
public void setShowName(boolean showName) {
	this.iShowName = showName;
}

/**
 * @return the subjectAreas
 */
public TreeSet<SubjectArea> getSubjectAreas() {
	return iSubjectAreas;
}

/**
 * @param subjectAreas the subjectAreas to set
 */
public void setSubjectAreas(TreeSet<SubjectArea> subjectAreas) {
	this.iSubjectAreas = subjectAreas;
} 

protected abstract String createQueryString(TreeSet<SubjectArea> subjectAreas);

protected List getAuditResults(TreeSet<SubjectArea> subjectAreas){

	String query = createQueryString(subjectAreas);
	@SuppressWarnings("deprecation")
	Query q = StudentClassEnrollmentDAO.getInstance().getSession().createQuery(query);
	q.setParameter("sessId", getSession().getUniqueId().longValue());
	return q.list();
}

protected Line buildBaseAuditLine(EnrollmentAuditResult result) {
	return new Line(
			(isShowId() ? lpad(result.getStudentId(), ' ', studentIdLength) : new Cell()),
			(isShowName() ? rpad(result.getStudentName(), ' ', studentNameLength) : new Cell()),
			rpad(result.getOffering(), ' ', offeringNameLength)
			);
}

protected Line[] getBaseHeader(){
	return new Line[] {
			new Line(
					(isShowId() ? rpad("", ' ', studentIdLength) : new Cell()),
					(isShowName() ? rpad("", ' ', studentNameLength) : new Cell()),
					rpad("", ' ', offeringNameLength)
				),
			new Line(
					(isShowId() ? rpad(MSG.lrStudentID(), ' ', studentIdLength) : new Cell()),
					(isShowName() ? rpad(MSG.lrStudentName(), ' ', studentNameLength) : new Cell()),
					rpad(MSG.lrOffering(), ' ', offeringNameLength)
				),
			new Line(
					(isShowId() ? rpad("", '-', studentIdLength) : new Cell()),
					(isShowName() ? rpad("", '-', studentNameLength) : new Cell()),
					rpad("", '-', offeringNameLength)
				)
	};
}

protected abstract class EnrollmentAuditResult  {
	private String studentId;
	private String studentLastName;
	private String studentFirstName;
	private String studentMiddleName;
	private String subjectArea;
	private String courseNbr;
	private String title;


	public EnrollmentAuditResult(Object[] result) {
		super();
		if (result[0] != null) this.studentId = result[0].toString().trim();
		if (result[1] != null) this.studentLastName = result[1].toString();
		if (result[2] != null) this.studentFirstName = result[2].toString();
		if (result[3] != null) this.studentMiddleName = result[3].toString();
		if (result[4] != null) this.subjectArea = result[4].toString();
		if (result[5] != null) this.courseNbr = result[5].toString();
		if (result[6] != null) this.title = result[6].toString();
	}
	
	public String getStudentName(){
		StringBuilder sb = new StringBuilder();
		if (studentLastName != null && studentLastName.length() > 0) {
			sb.append(studentLastName);
			if (studentFirstName != null && studentFirstName.length() > 0) {
				sb.append(", ")
				  .append(studentFirstName.charAt(0));
				if (studentMiddleName != null && studentMiddleName.length() > 0){
					sb.append(" ")
					  .append(studentMiddleName.charAt(0));
				}
			}
		}
		String name = sb.toString();
		if (name.length() > studentNameLength){
			name = name.substring(0, studentNameLength);
		}
		return(name);
	}

	/**
	 * @return the studentId
	 */
	public String getStudentId() {
		return studentId;
	}
	
	public String getOffering(){
		StringBuilder sb = new StringBuilder();
		sb.append(subjectArea)
		  .append(" ")
		  .append(courseNbr)
		  .append(" - ")
		  .append(title);
		String title = sb.toString();
		if (title.length() > offeringNameLength){
			title = title.substring(0, offeringNameLength);
		}
		return(title);
	}
	
	protected String createClassString(String itypeStr, String nbrStr, String suffixStr){
		StringBuilder sb = new StringBuilder();
		sb.append(itypeStr);
		if (nbrStr.length() != 0){
			sb.append(" ")
			  .append(nbrStr);
			if (!suffixStr.trim().equals("-")){
				sb.append("(")
				  .append(suffixStr)
				  .append(")");
			}
		}
		return(sb.toString());
	}
	
}


}
