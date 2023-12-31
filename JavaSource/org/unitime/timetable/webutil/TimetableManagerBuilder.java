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
package org.unitime.timetable.webutil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.unitime.commons.web.WebTable.WebTableLine;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.defaults.CommonValues;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.ManagerRole;
import org.unitime.timetable.model.SolverGroup;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.comparators.RolesComparator;
import org.unitime.timetable.model.dao.TimetableManagerDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.NameFormat;


/**
 * Build list of Managers for the currently selected academic session
 * 
 * @author Heston Fernandes, Tomas Muller, Stephanie Schluttenhofer
 */
public class TimetableManagerBuilder {
	protected static final CourseMessages MSG = Localization.create(CourseMessages.class);
	
    public PdfWebTable getManagersTable(SessionContext context, String format, boolean showAll) {
        int cols = 7;
		org.hibernate.Session hibSession = null;
		boolean html = "html".equalsIgnoreCase(format);
		boolean pdf = "pdf".equalsIgnoreCase(format);
        
        boolean dispLastChanges = CommonValues.Yes.eq(UserProperty.DisplayLastChanges.get(context.getUser()));
        if (dispLastChanges) cols++;

	    Long currentAcadSession = context.getUser().getCurrentAcademicSessionId();
	    
		// Create new table
        PdfWebTable webTable = new PdfWebTable( cols,
			    (html? "" : MSG.sectManagerList(currentAcadSession == null ? "" : context.getUser().getCurrentAuthority().getQualifiers("Session").get(0).getQualifierLabel())),
			    "timetableManagerList.action?order=%%",
                (dispLastChanges?
                        new String[] {
                        		MSG.columnRoles(),
                        		MSG.columnExternalId(),
                        		MSG.columnManagerName(),
                        		MSG.columnEmailAddress(),
                        		MSG.columnDepartment(),
                        		MSG.columnSubjectArea(),
                        		MSG.columnSolverGroup(),
                        		MSG.columnLastChange()}:
                        new String[] {
                        		MSG.columnRoles(),
                        		MSG.columnExternalId(),
                        		MSG.columnManagerName(),
                        		MSG.columnEmailAddress(),
                        		MSG.columnDepartment(),
                        		MSG.columnSubjectArea(),
                        		MSG.columnSolverGroup()}),
			    new String[] {"left", "left", "left", "left", "left", "left", "left", "left"},
			    new boolean[] {true, true, true, true, true, true, true, false} );
        webTable.enableHR("#9CB0CE");
        webTable.setRowStyle("white-space: nowrap");
        	
	    TimetableManagerDAO empDao = TimetableManagerDAO.getInstance();
		hibSession = empDao.getSession();

		List<TimetableManager> empList = hibSession.createQuery("from TimetableManager order by lastName, firstName", TimetableManager.class).list();
		Iterator iterEmp = empList.iterator();
		
		NameFormat nameFormat = NameFormat.fromReference(context.getUser().getProperty(UserProperty.NameFormat));

		while(iterEmp.hasNext()) {
		    TimetableManager manager = (TimetableManager) iterEmp.next();

		    String puid = manager.getExternalUniqueId();
		    String email = manager.getEmailAddress()!=null ? manager.getEmailAddress() : " ";
		    String fullName = nameFormat.format(manager);
		    String subjectList = "";
		    String roleStr = "";
		    String deptStr = "";
		    Set depts = manager.getDepartments();
		    Set mgrRolesSet = manager.getManagerRoles();
		    
		    String onClick = (context.hasPermission(manager, Right.TimetableManagerEdit) ? "onClick=\"document.location='timetableManagerEdit.action?op=Edit&id=" + manager.getUniqueId() + "';\"" : null);

		    // Determine role type
		    String roleOrd = "";
	        ArrayList mgrRoles = new ArrayList(mgrRolesSet);
	        Collections.sort(mgrRoles, new RolesComparator());
	        
	        boolean sessionIndependent = false;
		    for (Iterator i=mgrRoles.iterator(); i.hasNext(); ) {
		        ManagerRole mgrRole = (ManagerRole) i.next();
                String roleRef = mgrRole.getRole().getAbbv(); 
		        String title = roleRef;
		        boolean receivesEmail = (mgrRole.isReceiveEmails() == null?false:mgrRole.isReceiveEmails().booleanValue());
                if (roleStr.length()>0) roleStr+=","+(html?"<br>":"\n");
                if (mgrRoles.size()>1 && mgrRole.isPrimary().booleanValue()) {
                    roleStr += (html?"<span title='"+roleRef+" - " + MSG.flagPrimaryRole() + (receivesEmail?"":", " + MSG.explNoEmailForThisRole())+"' style='font-weight:bold;'>"+roleRef + (receivesEmail?"":"*") +"</span>": pdf ? "@@BOLD "+roleRef + (receivesEmail?"":"*")+"@@END_BOLD " : roleRef + (receivesEmail?"":"*"));
                } else {
                    roleStr += (html?(!receivesEmail?"<span title='"+roleRef + (receivesEmail?"":", " + MSG.explNoEmailForThisRole())+"' style='font-weight:normal;'>"+roleRef + (receivesEmail?"":"*") +"</span>": roleRef):roleRef + (receivesEmail?"":"*"));
                }
		        roleOrd += title;
		        if (mgrRole.getRole().hasRight(Right.SessionIndependent) || (mgrRole.getRole().hasRight(Right.SessionIndependentIfNoSessionGiven) && manager.getDepartments().isEmpty()))
		        	sessionIndependent = true;
		    }
		    
		    // Departments
		    boolean departmental = false;
		    for (Iterator di=depts.iterator(); di.hasNext(); ) {
		        Department dept = (Department) di.next();
		        
		        if (!dept.getSession().getUniqueId().equals(currentAcadSession)) continue;
		        departmental = true;
		        
	            if (deptStr.trim().length()>0) deptStr += ", "+(html?"<br>":"\n");
	            deptStr += 
                    (html?
                            "<span title='"+dept.getHtmlTitle()+"'>"+(dept.isExternalManager()?"<b>":"")+
                            dept.getDeptCode()+(dept.getAbbreviation()==null?"":": "+dept.getAbbreviation().trim())+
                            (dept.isExternalManager()?"</b>":"")+"</span>"
                         :
                             (dept.isExternalManager() && pdf?"@@BOLD ":"")+
                             dept.getDeptCode()+(dept.getAbbreviation()==null?"":": "+dept.getAbbreviation().trim())+
                             (dept.isExternalManager() && pdf?"@@END_BOLD ":"")
                     );
		        
		        // Construct SubjectArea List
			    Set saList = dept.getSubjectAreas();
			    if (saList!=null && saList.size()>0) {
			        for (Iterator si = saList.iterator(); si.hasNext(); ) {
			            SubjectArea sa = (SubjectArea) si.next();
                        if (subjectList.length()>0) subjectList+=","+(html?"<br>":"\n");
                        subjectList += (html?"<span title='"+sa.getTitle()+"'>"+sa.getSubjectAreaAbbreviation().trim()+"</span>":sa.getSubjectAreaAbbreviation().trim());
			        }
			    }
		    }
		    
		    if (!showAll && !sessionIndependent && !departmental) continue;
		    
		    if (html && deptStr.trim().length()==0)
		        deptStr = "&nbsp;";
		    if (html && subjectList.trim().length()==0)
		        subjectList = "&nbsp;";
		    
		    String solverGroupStr = "";
		    for (Iterator i=manager.getSolverGroups().iterator();i.hasNext();) {
		    	SolverGroup sg = (SolverGroup)i.next();
		    	if (!sg.getSession().getUniqueId().equals(currentAcadSession)) continue;
		    	if (solverGroupStr.length()>0) solverGroupStr += ","+(html?"<br>":"\n");
		    	solverGroupStr += (html?"<span title='"+sg.getName()+"'>"+sg.getAbbv()+"</span>":sg.getAbbv());
		    }
		    if (html && solverGroupStr.length()==0) solverGroupStr = "&nbsp;";
            
            String lastChangeStr = null;
            Long lastChangeCmp = null;
            if (dispLastChanges) {
                List changes = null;
                if (currentAcadSession!=null) changes = ChangeLog.findLastNChanges(currentAcadSession, manager.getUniqueId(), null, null, 1);
                ChangeLog lastChange = (changes==null || changes.isEmpty()?null:(ChangeLog)changes.get(0));
                if (html)
                    lastChangeStr = (lastChange==null?"&nbsp;":"<span title='"+lastChange.getLabel()+"'>"+MSG.formatLastChange(lastChange.getSourceTitle(), lastChange.getOperationTitle(), ChangeLog.sDFdate.format(lastChange.getTimeStamp()))+"</span>");
                else
                    lastChangeStr = (lastChange==null?"":MSG.formatLastChange(lastChange.getSourceTitle(), lastChange.getOperationTitle(), ChangeLog.sDFdate.format(lastChange.getTimeStamp())));
                lastChangeCmp = Long.valueOf(lastChange==null?0:lastChange.getTimeStamp().getTime());
            }
		    
		    // Add to web table
		    WebTableLine line = webTable.addLine(
	        	onClick,
	        	new String[] { roleStr, puid, fullName, email, deptStr, subjectList, solverGroupStr, lastChangeStr},
	        	new Comparable[] {roleOrd, puid, fullName, email, deptStr, subjectList, solverGroupStr, lastChangeCmp} );
		    line.setUniqueId(manager.getUniqueId().toString());
		}
        
        return webTable;
    }
}
