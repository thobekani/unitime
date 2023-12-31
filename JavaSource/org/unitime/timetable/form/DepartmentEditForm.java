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
package org.unitime.timetable.form;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import org.unitime.commons.Debug;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.action.UniTimeAction;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentStatusType;
import org.unitime.timetable.model.ExternalDepartmentStatusType;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao.DepartmentDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.util.ReferenceList;


/**
 * @author Tomas Muller, Stephanie Schluttenhofer
 */
public class DepartmentEditForm implements UniTimeForm {
	private static final long serialVersionUID = -6614766002463228171L;
	protected static final GwtMessages MSG = Localization.create(GwtMessages.class);
	
	public Long iId = null;
	public Long iSessionId = null;
	public String iName = null;
	public String iDeptCode = null;
	public String iStatusType = null;
	public String iOp = null;
	public String iAbbv = null;
	public String iExternalId = null;
	public int iDistPrefPriority = 0;
	public boolean iIsExternal = false;
	public String iExtAbbv = null;
	public String iExtName = null;
    public boolean iAllowReqTime = false;
    public boolean iAllowReqRoom = false;
    public boolean iAllowReqDist = false;
    public boolean iAllowEvents = false;
    public boolean iInheritInstructorPreferences = false;
    public boolean iAllowStudentScheduling = false;
    private List<Long> iDependentDepartments;
    private List<String> iDependentStatuses;
    private boolean iFullyEditable = false;
    
    public DepartmentEditForm() {
    	reset();
    }
    
	public void validate(UniTimeAction action) {
		
        if (iName==null || iName.trim().equalsIgnoreCase("")) {
        	action.addFieldError("form.name", MSG.errorNameIsRequired());
        }
        if (iName!=null && iName.trim().length() > 100) {
        	action.addFieldError("form.name", MSG.errorTooLong(MSG.colName()));
        }
        
        if (iAbbv==null || iAbbv.trim().equalsIgnoreCase("")) {
        	action.addFieldError("form.abbv", MSG.errorAbbreviationIsEmpty());
        }
        
        if (iAbbv!=null && iAbbv.trim().length() > 20) {
        	action.addFieldError("form.abbv", MSG.errorTooLong(MSG.colAbbreviation()));
        }

        if (iDeptCode==null || iDeptCode.trim().equalsIgnoreCase("")) {
        	action.addFieldError("form.deptCode", MSG.errorDeptCodeIsEmpty());
        }
        
        if (iDeptCode!=null && iDeptCode.trim().length() > 50) {
        	action.addFieldError("form.deptCode", MSG.errorTooLong(MSG.colCode()));
        }

        if (iIsExternal && (iExtName==null || iExtName.trim().length()==0)) {
        	action.addFieldError("form.extName", MSG.errorRequired(MSG.fieldExternalManagerName()));
        }
        
        if (!iIsExternal && iExtName!=null && iExtName.trim().length() > 0){
        	action.addFieldError("form.extName", MSG.errorExternalManagerNameUse());
        }
        
        if (iIsExternal && (iExtName!=null && iExtName.trim().length() > 30)) {
        	action.addFieldError("form.extName", MSG.errorTooLong(MSG.fieldExternalManagerName()));
        }

        if (iIsExternal && (iExtAbbv==null || iExtAbbv.trim().length()==0)) {
        	action.addFieldError("form.extAbbv", MSG.errorRequired(MSG.fieldExternalManagerAbbreviation()));
        }
        
        if (!iIsExternal && iExtAbbv!=null && iExtAbbv.trim().length() > 0){
        	action.addFieldError("form.extAbbv", MSG.errorExternalManagerAbbreviationUse());
        }
        
        if (iIsExternal && (iExtAbbv!=null && iExtAbbv.trim().length() > 10)) {
        	action.addFieldError("form.extAbbv", MSG.errorTooLong(MSG.fieldExternalManagerAbbreviation()));
        }

        try {
			Department dept = Department.findByDeptCode(iDeptCode, iSessionId);
			if (dept!=null && !dept.getUniqueId().equals(iId)) {
				action.addFieldError("form.deptCode", MSG.errorDeptCodeMustBeUnique());
			}
			
		} catch (Exception e) {
			Debug.error(e);
			action.addFieldError("form.deptCode", e.getMessage());
		}
	}

	@Override
	public void reset() {
		iId = null; iSessionId = null; iName = null; iDeptCode = null; iStatusType = null; iAbbv=null; iDistPrefPriority = 0;
		iIsExternal = false; iExtName = null; iExtAbbv = null;
        iAllowReqTime = false; iAllowReqRoom = false; iAllowReqDist = false; iAllowEvents = false;
        iInheritInstructorPreferences = false; iAllowStudentScheduling = false;
        iDependentDepartments = new ArrayList<Long>();
        iDependentStatuses = new ArrayList<String>();
        iFullyEditable = false;
	}

	public Long getId() { return iId; }
	public void setId(Long id) { iId = id; }

	public Long getSessionId() { return iSessionId; }
	public void setSessionId(Long sessionId) { iSessionId = sessionId; }

	public String getExternalId() {
		return iExternalId;
	}

	public void setExternalId(String externalId) {
		iExternalId = externalId;
	}

	public String getName() { return iName; }
	public void setName(String name) { iName = name; }
	public String getAbbv() { return iAbbv; }
	public void setAbbv(String abbv) { iAbbv = abbv; }
	public int getDistPrefPriority() { return iDistPrefPriority; }
	public void setDistPrefPriority(int distPrefPriority) { iDistPrefPriority = distPrefPriority; }
	public String getDeptCode() { return iDeptCode; }
	public void setDeptCode(String deptCode) { iDeptCode = deptCode; }
	public String getStatusType() { return iStatusType; }
	public void setStatusType(String statusType) { iStatusType = statusType; }
	public String getOp() { return iOp; }
	public void setOp(String op) { iOp = op; }

    public boolean getIsExternal() { return iIsExternal; }
	public void setIsExternal(boolean isExternal) { iIsExternal = isExternal; }
    public boolean getAllowReqTime() { return iAllowReqTime; }
    public void setAllowReqTime(boolean allowReqTime) { iAllowReqTime = allowReqTime; }
    public boolean getAllowReqRoom() { return iAllowReqRoom; }
    public void setAllowReqRoom(boolean allowReqRoom) { iAllowReqRoom = allowReqRoom; }
    public boolean getAllowReqDist() { return iAllowReqDist; }
    public void setAllowReqDist(boolean allowReqDist) { iAllowReqDist = allowReqDist; }
    public boolean getAllowEvents() { return iAllowEvents; }
    public void setAllowEvents(boolean allowEvents) { iAllowEvents = allowEvents; }
    public boolean getInheritInstructorPreferences() { return iInheritInstructorPreferences; }
    public void setInheritInstructorPreferences(boolean inheritInstructorPreferences) { iInheritInstructorPreferences = inheritInstructorPreferences; }
	public String getExtAbbv() { return iExtAbbv; }
	public void setExtAbbv(String extAbbv) { iExtAbbv = extAbbv; }
	public String getExtName() { return iExtName; }
	public void setExtName(String extName) { iExtName = extName; }
    public boolean getAllowStudentScheduling() { return iAllowStudentScheduling; }
    public void setAllowStudentScheduling(boolean allowStudentScheduling) { iAllowStudentScheduling = allowStudentScheduling; }
	
	public ReferenceList getStatusOptions() { 
		ReferenceList ref = new ReferenceList();
		ref.addAll(DepartmentStatusType.findAllForDepartment());
		return ref;
	}
	
    public List<Long> getDependentDepartments() { return iDependentDepartments; }
    public Long getDependentDepartments(int key) { return iDependentDepartments.get(key); }
    public void setDependentDepartments(int key, Long value) { iDependentDepartments.set(key, value); }
    public void setDependentDepartments(List<Long> departments) { iDependentDepartments = departments; }
    public List<String> getDependentStatuses() { return iDependentStatuses; }
    public String getDependentStatuses(int key) { return iDependentStatuses.get(key); }
    public void setDependentStatuses(int key, String value) { iDependentStatuses.set(key, value); }
    public void setDependentStatuses(List<String> statuses) { iDependentStatuses = statuses; }
	
	public void load(Department department) {
		setId(department.getUniqueId());
		setSessionId(department.getSessionId());
		setName(department.getName());
		setAbbv(department.getAbbreviation());
		setDistPrefPriority(department.getDistributionPrefPriority()==null?0:department.getDistributionPrefPriority().intValue());
		setDeptCode(department.getDeptCode());
		setStatusType(department.getStatusType()==null?null:department.getStatusType().getReference());
		setExternalId(department.getExternalUniqueId());
		setIsExternal(department.isExternalManager().booleanValue());
		setExtAbbv(department.getExternalMgrAbbv());
		setExtName(department.getExternalMgrLabel());
        setAllowReqRoom(department.isAllowReqRoom()!=null && department.isAllowReqRoom().booleanValue());
        setAllowReqTime(department.isAllowReqTime()!=null && department.isAllowReqTime().booleanValue());
        setAllowReqDist(department.isAllowReqDistribution()!=null && department.isAllowReqDistribution().booleanValue());
        setAllowEvents(department.isAllowEvents());
        setAllowStudentScheduling(department.isAllowStudentScheduling());
        setInheritInstructorPreferences(department.isInheritInstructorPreferences());
        iDependentDepartments.clear(); iDependentStatuses.clear();
        if (department.isExternalManager() && department.getExternalStatusTypes() != null) {
        	if (!department.getExternalStatusTypes().isEmpty()) {
            	TreeSet<ExternalDepartmentStatusType> set = new TreeSet<ExternalDepartmentStatusType>(new Comparator<ExternalDepartmentStatusType>() {
    				@Override
    				public int compare(ExternalDepartmentStatusType e1, ExternalDepartmentStatusType e2) {
    					return e1.getDepartment().compareTo(e2.getDepartment());
    				}
    			});
            	set.addAll(department.getExternalStatusTypes());
            	for (ExternalDepartmentStatusType e: set) {
            		iDependentDepartments.add(e.getDepartment().getUniqueId());
            		iDependentStatuses.add(e.getStatusType().getReference());
            	}
        	}
            addBlankDependentDepartment();
            addBlankDependentDepartment();
        }
	}
	
	public void addBlankDependentDepartment() {
        iDependentDepartments.add(-1l);
        iDependentStatuses.add("");
	}
	
	public void deleteDependentDepartment(int idx) {
        iDependentDepartments.remove(idx);
        iDependentStatuses.remove(idx);
	}
	
	public void deleteAllDependentDepartments() {
        iDependentDepartments.clear();
        iDependentStatuses.clear();
        addBlankDependentDepartment();
        addBlankDependentDepartment();
	}

	public void save(SessionContext context) throws Exception {
		DepartmentDAO dao = DepartmentDAO.getInstance();
		org.hibernate.Session session = dao.getSession();
		Department department;
		Session acadSession = null;
		
		if (getId() == null || getId() < 0) {
			department = new Department();
			acadSession = SessionDAO.getInstance().get(context.getUser().getCurrentAcademicSessionId()); 
			department.setSession(acadSession);
			department.setDistributionPrefPriority(0);
			acadSession.addToDepartments(department);
			department.setExternalStatusTypes(new HashSet<ExternalDepartmentStatusType>());
		}
		else {
			department = dao.get(getId(), session);
		}
		if (department!=null) {
			if (isFullyEditable()) {
				department.setStatusType(getStatusType()==null || getStatusType().length()==0 ? null : DepartmentStatusType.findByRef(getStatusType()));
				department.setName(getName());
				department.setDeptCode(getDeptCode());
				department.setAbbreviation(getAbbv());
				department.setExternalUniqueId(getExternalId());
				department.setDistributionPrefPriority(getDistPrefPriority());
				department.setExternalManager(Boolean.valueOf(getIsExternal()));
				department.setExternalMgrLabel(getExtName());
				department.setExternalMgrAbbv(getExtAbbv());
	            department.setAllowReqRoom(Boolean.valueOf(getAllowReqRoom()));
	            department.setAllowReqTime(Boolean.valueOf(getAllowReqTime()));
	            department.setAllowReqDistribution(Boolean.valueOf(getAllowReqDist()));
	            department.setAllowEvents(getAllowEvents());
	            department.setAllowStudentScheduling(getAllowStudentScheduling());
	            department.setInheritInstructorPreferences(getInheritInstructorPreferences());
			}
            
            List<ExternalDepartmentStatusType> statuses = new ArrayList<ExternalDepartmentStatusType>(department.getExternalStatusTypes());
            if (department.isExternalManager()) {
            	for (int i = 0; i < Math.min(iDependentDepartments.size(), iDependentStatuses.size()); i++) {
            		Long deptId = iDependentDepartments.get(i);
            		String status = (String)iDependentStatuses.get(i);
            		if (deptId >= 0 && !status.isEmpty()) {
            			ExternalDepartmentStatusType t = null;
            			for (Iterator<ExternalDepartmentStatusType> j = statuses.iterator(); j.hasNext(); ) {
            				ExternalDepartmentStatusType x = j.next();
            				if (deptId.equals(x.getDepartment().getUniqueId())) {
            					j.remove(); t = x; break;
            				}
            			}
            			if (t == null) {
            				t = new ExternalDepartmentStatusType();
            				t.setExternalDepartment(department);
            				t.setDepartment(DepartmentDAO.getInstance().get(deptId));
                			department.getExternalStatusTypes().add(t);
            			}
            			t.setStatusType(DepartmentStatusType.findByRef(status));
            		}
            	}
            }
            for (ExternalDepartmentStatusType t: statuses) {
            	department.getExternalStatusTypes().remove(t);
            	session.remove(t);
            }
            
            if (department.getUniqueId() == null)
            	session.persist(department);
			else
				session.merge(department);
            ChangeLog.addChange(
                    session, 
                    context, 
                    department, 
                    ChangeLog.Source.DEPARTMENT_EDIT, 
                    (getId() == null?ChangeLog.Operation.CREATE:ChangeLog.Operation.UPDATE), 
                    null, 
                    department);
            setId(department.getUniqueId());

			session.flush();
			if( acadSession != null){
				session.refresh(acadSession);
			}
		}
	}
	
	public boolean isFullyEditable() { return iFullyEditable; }
	public void setFullyEditable(boolean fullyEditable) { iFullyEditable = fullyEditable; }
}
