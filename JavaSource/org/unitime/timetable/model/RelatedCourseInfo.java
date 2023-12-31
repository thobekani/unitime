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
 
package org.unitime.timetable.model;


import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.util.List;

import org.unitime.timetable.model.base.BaseRelatedCourseInfo;
import org.unitime.timetable.model.comparators.ClassComparator;
import org.unitime.timetable.model.comparators.InstrOfferingConfigComparator;
import org.unitime.timetable.model.dao.Class_DAO;
import org.unitime.timetable.model.dao.CourseOfferingDAO;
import org.unitime.timetable.model.dao.ExamOwnerDAO;
import org.unitime.timetable.model.dao.RelatedCourseInfoDAO;
import org.unitime.timetable.model.dao.InstrOfferingConfigDAO;
import org.unitime.timetable.model.dao.InstructionalOfferingDAO;

/**
 * @author Stephanie Schluttenhofer, Tomas Muller
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "related_course_info")
public class RelatedCourseInfo extends BaseRelatedCourseInfo implements Comparable<RelatedCourseInfo> {
	private static final long serialVersionUID = 1L;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public RelatedCourseInfo () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public RelatedCourseInfo (java.lang.Long uniqueId) {
		super(uniqueId);
	}

/*[CONSTRUCTOR MARKER END]*/
	public static List<RelatedCourseInfo> findByOwnerIdType(org.hibernate.Session hibSession, Long ownerId, Integer ownerType) {
	    return (hibSession.
	        createQuery("select o from RelatedCourseInfo o where o.ownerId=:ownerId and o.ownerType=:ownerType", RelatedCourseInfo.class).
	        setParameter("ownerId", ownerId).
	        setParameter("ownerType", ownerType).
	        setCacheable(true).list());
	}
	
	public static List findByOwnerIdType(Long ownerId, Integer ownerType) {
	    return (findByOwnerIdType(RelatedCourseInfoDAO.getInstance().getSession(), ownerId, ownerType));
	}
	
	
	private Object iOwnerObject = null;
	@Transient
	public Object getOwnerObject() {
	    if (iOwnerObject!=null) return iOwnerObject;
	    switch (getOwnerType()) {
	        case ExamOwner.sOwnerTypeClass : 
	            iOwnerObject = new Class_DAO().get(getOwnerId());
	            return iOwnerObject;
	        case ExamOwner.sOwnerTypeConfig : 
	            iOwnerObject = InstrOfferingConfigDAO.getInstance().get(getOwnerId());
	            return iOwnerObject;
	        case ExamOwner.sOwnerTypeCourse : 
	            iOwnerObject = CourseOfferingDAO.getInstance().get(getOwnerId());
	            return iOwnerObject;
	        case ExamOwner.sOwnerTypeOffering : 
	            iOwnerObject = InstructionalOfferingDAO.getInstance().get(getOwnerId());
	            return iOwnerObject;
	        default : throw new RuntimeException("Unknown owner type "+getOwnerType());
	    }
	}
	
    public void setOwner(Class_ clazz) {
        setOwnerId(clazz.getUniqueId());
        setOwnerType(ExamOwner.sOwnerTypeClass);
        setCourse(clazz.getSchedulingSubpart().getInstrOfferingConfig().getControllingCourseOffering());
    }

    public void setOwner(InstrOfferingConfig config) {
        setOwnerId(config.getUniqueId());
        setOwnerType(ExamOwner.sOwnerTypeConfig);
        setCourse(config.getControllingCourseOffering());
    }

    public void setOwner(CourseOffering course) {
        setOwnerId(course.getUniqueId());
        setOwnerType(ExamOwner.sOwnerTypeCourse);
        setCourse(course);
    }

    public void setOwner(InstructionalOffering offering) {
        setOwnerId(offering.getUniqueId());
        setOwnerType(ExamOwner.sOwnerTypeOffering);
        setCourse(offering.getControllingCourseOffering());
    }
    
    public CourseOffering computeCourse() {
        Object owner = getOwnerObject();
        switch (getOwnerType()) {
            case ExamOwner.sOwnerTypeClass : 
                return ((Class_)owner).getSchedulingSubpart().getControllingCourseOffering();
            case ExamOwner.sOwnerTypeConfig : 
                return ((InstrOfferingConfig)owner).getControllingCourseOffering();
            case ExamOwner.sOwnerTypeCourse : 
                return (CourseOffering)owner;
            case ExamOwner.sOwnerTypeOffering : 
                return ((InstructionalOffering)owner).getControllingCourseOffering();
            default : throw new RuntimeException("Unknown owner type "+getOwnerType());
        }
    }
    
    public int compareTo(RelatedCourseInfo owner) {
        CourseOffering c1 = getCourse();
        CourseOffering c2 = owner.getCourse();
        int cmp = 0;
        
        cmp = c1.getSubjectAreaAbbv().compareTo(c2.getSubjectAreaAbbv());
        if (cmp!=0) return cmp;
        
        cmp = c1.getCourseNbr().compareTo(c2.getCourseNbr());
        if (cmp!=0) return cmp;
        
        cmp = getOwnerType().compareTo(owner.getOwnerType());
        if (cmp!=0) return cmp;
        
        switch (getOwnerType()) {
            case ExamOwner.sOwnerTypeClass : return new ClassComparator(ClassComparator.COMPARE_BY_HIERARCHY).compare((Class_)getOwnerObject(), (Class_)owner.getOwnerObject());
            case ExamOwner.sOwnerTypeConfig : return new InstrOfferingConfigComparator(null).compare(getOwnerObject(), owner.getOwnerObject());
        }
           
        return getOwnerId().compareTo(owner.getOwnerId());
    }
    
	@Transient
    public List<Student> getStudents() {
        switch (getOwnerType()) {
        case ExamOwner.sOwnerTypeClass : 
            return RelatedCourseInfoDAO.getInstance().getSession().createQuery(
                    "select distinct e.student from " +
                    "StudentClassEnrollment e inner join e.clazz c  " +
                    "where c.uniqueId = :eventOwnerId", Student.class)
                    .setParameter("eventOwnerId", getOwnerId())
                    .setCacheable(true)
                    .list();
        case ExamOwner.sOwnerTypeConfig : 
            return RelatedCourseInfoDAO.getInstance().getSession().createQuery(
                    "select distinct e.student from " +
                    "StudentClassEnrollment e inner join e.clazz c  " +
                    "where c.schedulingSubpart.instrOfferingConfig.uniqueId = :eventOwnerId", Student.class)
                    .setParameter("eventOwnerId", getOwnerId())
                    .setCacheable(true)
                    .list();
        case ExamOwner.sOwnerTypeCourse : 
            return RelatedCourseInfoDAO.getInstance().getSession().createQuery(
                    "select distinct e.student from " +
                    "StudentClassEnrollment e inner join e.courseOffering co  " +
                    "where co.uniqueId = :eventOwnerId", Student.class)
                    .setParameter("eventOwnerId", getOwnerId())
                    .setCacheable(true)
                    .list();
        case ExamOwner.sOwnerTypeOffering : 
            return RelatedCourseInfoDAO.getInstance().getSession().createQuery(
                    "select distinct e.student from " +
                    "StudentClassEnrollment e inner join e.courseOffering co  " +
                    "where co.instructionalOffering.uniqueId = :eventOwnerId", Student.class)
                    .setParameter("eventOwnerId", getOwnerId())
                    .setCacheable(true)
                    .list();
        default : throw new RuntimeException("Unknown owner type "+getOwnerType());
        }
    }
    
	@Transient
    public List<StudentClassEnrollment> getStudentClassEnrollments() {
        switch (getOwnerType()) {
        case ExamOwner.sOwnerTypeClass : 
            return ExamOwnerDAO.getInstance().getSession().createQuery(
            		"select distinct e from StudentClassEnrollment e, StudentClassEnrollment f where f.clazz.uniqueId = :classId" +
        			" and e.courseOffering.instructionalOffering = f.courseOffering.instructionalOffering and e.student = f.student", StudentClassEnrollment.class)
                    .setParameter("classId", getOwnerId())
                    .setCacheable(true)
                    .list();
        case ExamOwner.sOwnerTypeConfig : 
            return ExamOwnerDAO.getInstance().getSession().createQuery(
            		"select distinct e from StudentClassEnrollment e, StudentClassEnrollment f where f.clazz.schedulingSubpart.instrOfferingConfig.uniqueId = :configId" +
            		" and e.courseOffering.instructionalOffering = f.courseOffering.instructionalOffering and e.student = f.student", StudentClassEnrollment.class)
                    .setParameter("configId", getOwnerId())
                    .setCacheable(true)
                    .list();
        case ExamOwner.sOwnerTypeCourse : 
            return ExamOwnerDAO.getInstance().getSession().createQuery(
                    "select e from StudentClassEnrollment e where e.courseOffering.uniqueId = :courseId", StudentClassEnrollment.class)
                    .setParameter("courseId", getOwnerId())
                    .setCacheable(true)
                    .list();
        case ExamOwner.sOwnerTypeOffering : 
            return ExamOwnerDAO.getInstance().getSession().createQuery(
                    "select e from StudentClassEnrollment e where e.courseOffering.instructionalOffering.uniqueId = :offeringId", StudentClassEnrollment.class)
                    .setParameter("offeringId", getOwnerId())
                    .setCacheable(true)
                    .list();
        default : throw new RuntimeException("Unknown owner type "+getOwnerType());
        }
    }
    
	@Transient
    public List<Long> getStudentIds() {
        switch (getOwnerType()) {
        case ExamOwner.sOwnerTypeClass : 
            return RelatedCourseInfoDAO.getInstance().getSession().createQuery(
                    "select distinct e.student.uniqueId from " +
                    "StudentClassEnrollment e inner join e.clazz c  " +
                    "where c.uniqueId = :eventOwnerId", Long.class)
                    .setParameter("eventOwnerId", getOwnerId())
                    .setCacheable(true)
                    .list();
        case ExamOwner.sOwnerTypeConfig : 
            return RelatedCourseInfoDAO.getInstance().getSession().createQuery(
                    "select distinct e.student.uniqueId from " +
                    "StudentClassEnrollment e inner join e.clazz c  " +
                    "where c.schedulingSubpart.instrOfferingConfig.uniqueId = :eventOwnerId", Long.class)
                    .setParameter("eventOwnerId", getOwnerId())
                    .setCacheable(true)
                    .list();
        case ExamOwner.sOwnerTypeCourse : 
            return RelatedCourseInfoDAO.getInstance().getSession().createQuery(
                    "select distinct e.student.uniqueId from " +
                    "StudentClassEnrollment e inner join e.courseOffering co  " +
                    "where co.uniqueId = :eventOwnerId", Long.class)
                    .setParameter("eventOwnerId", getOwnerId())
                    .setCacheable(true)
                    .list();
        case ExamOwner.sOwnerTypeOffering : 
            return RelatedCourseInfoDAO.getInstance().getSession().createQuery(
                    "select distinct e.student.uniqueId from " +
                    "StudentClassEnrollment e inner join e.courseOffering co  " +
                    "where co.instructionalOffering.uniqueId = :eventOwnerId", Long.class)
                    .setParameter("eventOwnerId", getOwnerId())
                    .setCacheable(true)
                    .list();
        default : throw new RuntimeException("Unknown owner type "+getOwnerType());
        }
    }
    
	@Transient
    public List<DepartmentalInstructor> getInstructors() {
        switch (getOwnerType()) {
        case ExamOwner.sOwnerTypeClass : 
            return RelatedCourseInfoDAO.getInstance().getSession().createQuery(
                    "select i from " +
                    "Class_ c inner join c.classInstructors ci inner join ci.instructor i " +
                    "where c.uniqueId = :eventOwnerId and ci.lead=true", DepartmentalInstructor.class)
                    .setParameter("eventOwnerId", getOwnerId())
                    .setCacheable(true)
                    .list();
        case ExamOwner.sOwnerTypeConfig : 
            return RelatedCourseInfoDAO.getInstance().getSession().createQuery(
                    "select distinct i from " +
                    "Class_ c inner join c.classInstructors ci inner join ci.instructor i " +
                    "where c.schedulingSubpart.instrOfferingConfig.uniqueId = :eventOwnerId", DepartmentalInstructor.class)
                    .setParameter("eventOwnerId", getOwnerId())
                    .setCacheable(true)
                    .list();
        case ExamOwner.sOwnerTypeCourse : 
            return RelatedCourseInfoDAO.getInstance().getSession().createQuery(
                    "select distinct i from " +
                    "Class_ c inner join c.classInstructors ci inner join ci.instructor i inner join c.schedulingSubpart.instrOfferingConfig.instructionalOffering.courseOfferings co " +
                    "where co.uniqueId = :eventOwnerId", DepartmentalInstructor.class)
                    .setParameter("eventOwnerId", getOwnerId())
                    .setCacheable(true)
                    .list();
        case ExamOwner.sOwnerTypeOffering : 
            return RelatedCourseInfoDAO.getInstance().getSession().createQuery(
                    "select distinct i from " +
                    "Class_ c inner join c.classInstructors ci inner join ci.instructor i " +
                    "where c.schedulingSubpart.instrOfferingConfig.instructionalOffering.uniqueId = :eventOwnerId", DepartmentalInstructor.class)
                    .setParameter("eventOwnerId", getOwnerId())
                    .setCacheable(true)
                    .list();
        default : throw new RuntimeException("Unknown owner type "+getOwnerType());
        }
    }

    public int countStudents() {
        switch (getOwnerType()) {
        case ExamOwner.sOwnerTypeClass : 
            return RelatedCourseInfoDAO.getInstance().getSession().createQuery(
                    "select count(distinct e.student) from " +
                    "StudentClassEnrollment e inner join e.clazz c  " +
                    "where c.uniqueId = :eventOwnerId", Number.class)
                    .setParameter("eventOwnerId", getOwnerId())
                    .setCacheable(true)
                    .uniqueResult().intValue();
        case ExamOwner.sOwnerTypeConfig : 
            return RelatedCourseInfoDAO.getInstance().getSession().createQuery(
                    "select count(distinct e.student) from " +
                    "StudentClassEnrollment e inner join e.clazz c  " +
                    "where c.schedulingSubpart.instrOfferingConfig.uniqueId = :eventOwnerId", Number.class)
                    .setParameter("eventOwnerId", getOwnerId())
                    .setCacheable(true)
                    .uniqueResult().intValue();
        case ExamOwner.sOwnerTypeCourse : 
            return RelatedCourseInfoDAO.getInstance().getSession().createQuery(
                    "select count(distinct e.student) from " +
                    "StudentClassEnrollment e inner join e.courseOffering co  " +
                    "where co.uniqueId = :eventOwnerId", Number.class)
                    .setParameter("eventOwnerId", getOwnerId())
                    .setCacheable(true)
                    .uniqueResult().intValue();
        case ExamOwner.sOwnerTypeOffering : 
            return RelatedCourseInfoDAO.getInstance().getSession().createQuery(
                    "select count(distinct e.student) from " +
                    "StudentClassEnrollment e inner join e.courseOffering co  " +
                    "where co.instructionalOffering.uniqueId = :eventOwnerId", Number.class)
                    .setParameter("eventOwnerId", getOwnerId())
                    .setCacheable(true)
                    .uniqueResult().intValue();
        default : throw new RuntimeException("Unknown owner type "+getOwnerType());
        }
    }
    
	@Transient
    public int getLimit() {
        Object owner = getOwnerObject();
        switch (getOwnerType()) {
            case ExamOwner.sOwnerTypeClass : 
                return ((Class_)owner).getClassLimit();
            case ExamOwner.sOwnerTypeConfig : 
                return ((InstrOfferingConfig)owner).getLimit();
            case ExamOwner.sOwnerTypeCourse : 
                CourseOffering course = ((CourseOffering)owner);
                if (course.getReservation() != null)
                	return course.getReservation();
                return (course.getInstructionalOffering().getLimit() == null ? 0 : course.getInstructionalOffering().getLimit());
            case ExamOwner.sOwnerTypeOffering : 
                return (((InstructionalOffering)owner).getLimit()==null?0:((InstructionalOffering)owner).getLimit());
            default : throw new RuntimeException("Unknown owner type "+getOwnerType());
        }
    }
    
	@Transient
    public String getLabel() {
        Object owner = getOwnerObject();
        switch (getOwnerType()) {
            case ExamOwner.sOwnerTypeClass : 
                return ((Class_)owner).getClassLabel(getCourse());
            case ExamOwner.sOwnerTypeConfig : 
                return getCourse().getCourseName() + " [" + ((InstrOfferingConfig)owner).getName() + "]";
            case ExamOwner.sOwnerTypeCourse : 
                return ((CourseOffering)owner).getCourseName();
            case ExamOwner.sOwnerTypeOffering : 
                return ((InstructionalOffering)owner).getCourseName();
            default : throw new RuntimeException("Unknown owner type "+getOwnerType());
        }
    }


}
