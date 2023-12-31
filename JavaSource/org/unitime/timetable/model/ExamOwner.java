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

import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import org.unitime.commons.hibernate.util.HibernateUtil;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.model.base.BaseExamOwner;
import org.unitime.timetable.model.comparators.ClassComparator;
import org.unitime.timetable.model.comparators.InstrOfferingConfigComparator;
import org.unitime.timetable.model.dao.Class_DAO;
import org.unitime.timetable.model.dao.CourseOfferingDAO;
import org.unitime.timetable.model.dao.ExamOwnerDAO;
import org.unitime.timetable.model.dao.InstrOfferingConfigDAO;
import org.unitime.timetable.model.dao.InstructionalOfferingDAO;

/**
 * @author Tomas Muller
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "exam_owner")
public class ExamOwner extends BaseExamOwner implements Comparable<ExamOwner> {
	private static final long serialVersionUID = 1L;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public ExamOwner () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public ExamOwner (java.lang.Long uniqueId) {
		super(uniqueId);
	}

/*[CONSTRUCTOR MARKER END]*/

	
	public static final int sOwnerTypeClass = 3;
	public static final int sOwnerTypeConfig = 2;
	public static final int sOwnerTypeCourse = 1;
	public static final int sOwnerTypeOffering = 0;
	public static String[] sOwnerTypes = new String[] {"Offering", "Course", "Config", "Class"};
	
	public static ExamOwner findByOwnerIdType(Long ownerId, Integer ownerType) {
	    return ExamOwnerDAO.getInstance().
	        getSession().
	        createQuery("select o from ExamOwner o where o.ownerId=:ownerId and o.ownerType=:ownerType", ExamOwner.class).
	        setParameter("ownerId", ownerId).
	        setParameter("ownerType", ownerType).
	        setCacheable(true).uniqueResult();
	}
	
	
	private Object iOwnerObject = null;
	@Transient
	public Object getOwnerObject() {
	    if (iOwnerObject!=null) return iOwnerObject;
	    switch (getOwnerType()) {
	        case sOwnerTypeClass : 
	            iOwnerObject = new Class_DAO().get(getOwnerId());
	            return iOwnerObject;
	        case sOwnerTypeConfig : 
	            iOwnerObject = InstrOfferingConfigDAO.getInstance().get(getOwnerId());
	            return iOwnerObject;
	        case sOwnerTypeCourse : 
	            iOwnerObject = CourseOfferingDAO.getInstance().get(getOwnerId());
	            return iOwnerObject;
	        case sOwnerTypeOffering : 
	            iOwnerObject = InstructionalOfferingDAO.getInstance().get(getOwnerId());
	            return iOwnerObject;
	        default : throw new RuntimeException("Unknown owner type "+getOwnerType());
	    }
	}
	
    public void setOwner(Class_ clazz) {
        setOwnerId(clazz.getUniqueId());
        setOwnerType(sOwnerTypeClass);
        setCourse(clazz.getSchedulingSubpart().getInstrOfferingConfig().getControllingCourseOffering());
    }

    public void setOwner(InstrOfferingConfig config) {
        setOwnerId(config.getUniqueId());
        setOwnerType(sOwnerTypeConfig);
        setCourse(config.getControllingCourseOffering());
    }

    public void setOwner(CourseOffering course) {
        setOwnerId(course.getUniqueId());
        setOwnerType(sOwnerTypeCourse);
        setCourse(course);
    }

    public void setOwner(InstructionalOffering offering) {
        setOwnerId(offering.getUniqueId());
        setOwnerType(sOwnerTypeOffering);
        setCourse(offering.getControllingCourseOffering());
    }
    
    public CourseOffering computeCourse() {
        Object owner = getOwnerObject();
        switch (getOwnerType()) {
            case sOwnerTypeClass : 
                return ((Class_)owner).getSchedulingSubpart().getControllingCourseOffering();
            case sOwnerTypeConfig : 
                return ((InstrOfferingConfig)owner).getControllingCourseOffering();
            case sOwnerTypeCourse : 
                return (CourseOffering)owner;
            case sOwnerTypeOffering : 
                return ((InstructionalOffering)owner).getControllingCourseOffering();
            default : throw new RuntimeException("Unknown owner type "+getOwnerType());
        }
    }
    
    public int compareTo(ExamOwner owner) {
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
            case sOwnerTypeClass : return new ClassComparator(ClassComparator.COMPARE_BY_HIERARCHY).compare((Class_)getOwnerObject(), (Class_)owner.getOwnerObject());
            case sOwnerTypeConfig : return new InstrOfferingConfigComparator(null).compare(getOwnerObject(), owner.getOwnerObject());
        }
           
        return getOwnerId().compareTo(owner.getOwnerId());
    }
    
	@Transient
    public List<Student> getStudents() {
        switch (getOwnerType()) {
        case sOwnerTypeClass : 
            return ExamOwnerDAO.getInstance().getSession().createQuery(
                    "select distinct e.student from " +
                    "StudentClassEnrollment e inner join e.clazz c  " +
                    "where c.uniqueId = :examOwnerId", Student.class)
                    .setParameter("examOwnerId", getOwnerId())
                    .setCacheable(true)
                    .list();
        case sOwnerTypeConfig : 
            return ExamOwnerDAO.getInstance().getSession().createQuery(
                    "select distinct e.student from " +
                    "StudentClassEnrollment e inner join e.clazz c  " +
                    "where c.schedulingSubpart.instrOfferingConfig.uniqueId = :examOwnerId", Student.class)
                    .setParameter("examOwnerId", getOwnerId())
                    .setCacheable(true)
                    .list();
        case sOwnerTypeCourse : 
            return ExamOwnerDAO.getInstance().getSession().createQuery(
                    "select distinct e.student from " +
                    "StudentClassEnrollment e inner join e.courseOffering co  " +
                    "where co.uniqueId = :examOwnerId", Student.class)
                    .setParameter("examOwnerId", getOwnerId())
                    .setCacheable(true)
                    .list();
        case sOwnerTypeOffering : 
            return ExamOwnerDAO.getInstance().getSession().createQuery(
                    "select distinct e.student from " +
                    "StudentClassEnrollment e inner join e.courseOffering co  " +
                    "where co.instructionalOffering.uniqueId = :examOwnerId", Student.class)
                    .setParameter("examOwnerId", getOwnerId())
                    .setCacheable(true)
                    .list();
        default : throw new RuntimeException("Unknown owner type "+getOwnerType());
        }
    }
    
	@Transient
    public Collection<StudentClassEnrollment> getStudentClassEnrollments() {
        switch (getOwnerType()) {
        case sOwnerTypeClass : 
            return ExamOwnerDAO.getInstance().getSession().createQuery(
            		"select distinct e from StudentClassEnrollment e, StudentClassEnrollment f where f.clazz.uniqueId = :classId" +
        			" and e.courseOffering.instructionalOffering = f.courseOffering.instructionalOffering and e.student = f.student", StudentClassEnrollment.class)
                    .setParameter("classId", getOwnerId())
                    .setCacheable(true)
                    .list();
        case sOwnerTypeConfig : 
            return ExamOwnerDAO.getInstance().getSession().createQuery(
            		"select distinct e from StudentClassEnrollment e, StudentClassEnrollment f where f.clazz.schedulingSubpart.instrOfferingConfig.uniqueId = :configId" +
            		" and e.courseOffering.instructionalOffering = f.courseOffering.instructionalOffering and e.student = f.student", StudentClassEnrollment.class)
                    .setParameter("configId", getOwnerId())
                    .setCacheable(true)
                    .list();
        case sOwnerTypeCourse : 
            return ExamOwnerDAO.getInstance().getSession().createQuery(
                    "select e from StudentClassEnrollment e where e.courseOffering.uniqueId = :courseId", StudentClassEnrollment.class)
                    .setParameter("courseId", getOwnerId())
                    .setCacheable(true)
                    .list();
        case sOwnerTypeOffering : 
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
        case sOwnerTypeClass : 
            return ExamOwnerDAO.getInstance().getSession().createQuery(
                    "select distinct e.student.uniqueId from " +
                    "StudentClassEnrollment e inner join e.clazz c  " +
                    "where c.uniqueId = :examOwnerId", Long.class)
                    .setParameter("examOwnerId", getOwnerId())
                    .setCacheable(true)
                    .list();
        case sOwnerTypeConfig : 
            return ExamOwnerDAO.getInstance().getSession().createQuery(
                    "select distinct e.student.uniqueId from " +
                    "StudentClassEnrollment e inner join e.clazz c  " +
                    "where c.schedulingSubpart.instrOfferingConfig.uniqueId = :examOwnerId", Long.class)
                    .setParameter("examOwnerId", getOwnerId())
                    .setCacheable(true)
                    .list();
        case sOwnerTypeCourse : 
            return ExamOwnerDAO.getInstance().getSession().createQuery(
                    "select distinct e.student.uniqueId from " +
                    "StudentClassEnrollment e inner join e.courseOffering co  " +
                    "where co.uniqueId = :examOwnerId", Long.class)
                    .setParameter("examOwnerId", getOwnerId())
                    .setCacheable(true)
                    .list();
        case sOwnerTypeOffering : 
            return ExamOwnerDAO.getInstance().getSession().createQuery(
                    "select distinct e.student.uniqueId from " +
                    "StudentClassEnrollment e inner join e.courseOffering co  " +
                    "where co.instructionalOffering.uniqueId = :examOwnerId", Long.class)
                    .setParameter("examOwnerId", getOwnerId())
                    .setCacheable(true)
                    .list();
        default : throw new RuntimeException("Unknown owner type "+getOwnerType());
        }
    }
    
    public List<Long> getStudentIds(CourseOffering co) {
        switch (getOwnerType()) {
        case sOwnerTypeClass : 
            return ExamOwnerDAO.getInstance().getSession().createQuery(
                    "select distinct e.student.uniqueId from " +
                    "StudentClassEnrollment e inner join e.clazz c  " +
                    "where c.uniqueId = :examOwnerId and e.courseOffering.uniqueId=:courseOfferingId", Long.class)
                    .setParameter("examOwnerId", getOwnerId())
                    .setParameter("courseOfferingId", co.getUniqueId())
                    .setCacheable(true)
                    .list();
        case sOwnerTypeConfig : 
            return ExamOwnerDAO.getInstance().getSession().createQuery(
                    "select distinct e.student.uniqueId from " +
                    "StudentClassEnrollment e inner join e.clazz c  " +
                    "where c.schedulingSubpart.instrOfferingConfig.uniqueId = :examOwnerId and e.courseOffering.uniqueId=:courseOfferingId", Long.class)
                    .setParameter("examOwnerId", getOwnerId())
                    .setParameter("courseOfferingId", co.getUniqueId())
                    .setCacheable(true)
                    .list();
        case sOwnerTypeCourse : 
            return ExamOwnerDAO.getInstance().getSession().createQuery(
                    "select distinct e.student.uniqueId from " +
                    "StudentClassEnrollment e inner join e.courseOffering co  " +
                    "where co.uniqueId = :examOwnerId and e.courseOffering.uniqueId=:courseOfferingId", Long.class)
                    .setParameter("examOwnerId", getOwnerId())
                    .setParameter("courseOfferingId", co.getUniqueId())
                    .setCacheable(true)
                    .list();
        case sOwnerTypeOffering : 
            return ExamOwnerDAO.getInstance().getSession().createQuery(
                    "select distinct e.student.uniqueId from " +
                    "StudentClassEnrollment e inner join e.courseOffering co  " +
                    "where co.instructionalOffering.uniqueId = :examOwnerId and e.courseOffering.uniqueId=:courseOfferingId", Long.class)
                    .setParameter("examOwnerId", getOwnerId())
                    .setParameter("courseOfferingId", co.getUniqueId())
                    .setCacheable(true)
                    .list();
        default : throw new RuntimeException("Unknown owner type "+getOwnerType());
        }
    }
    
    protected void computeStudentExams(Hashtable<Long, Set<Exam>> studentExams) {
        switch (getOwnerType()) {
        case sOwnerTypeClass :
            for (Object[] o: ExamOwnerDAO.getInstance().getSession().createQuery(
                "select e.student.uniqueId, o.exam from ExamOwner o, StudentClassEnrollment f, StudentClassEnrollment e inner join e.clazz c " +
                "where c.uniqueId = :examOwnerId and e.student=f.student and " +
                "o.ownerType=:ownerType and o.ownerId=f.clazz.uniqueId and o.exam.examType.uniqueId=:examTypeId", Object[].class)
                .setParameter("ownerType", ExamOwner.sOwnerTypeClass)
                .setParameter("examTypeId", getExam().getExamType().getUniqueId())
                .setParameter("examOwnerId", getOwnerId())
                .setCacheable(true).list()) {
                Long studentId = (Long)o[0];
                Exam exam = (Exam)o[1];
                Set<Exam> exams  = studentExams.get(studentId);
                if (exams==null) { exams = new HashSet(); studentExams.put(studentId, exams); }
                exams.add(exam);
            }
            for (Object[] o: ExamOwnerDAO.getInstance().getSession().createQuery(
                    "select e.student.uniqueId, o.exam from ExamOwner o, StudentClassEnrollment f, StudentClassEnrollment e inner join e.clazz c " +
                    "where c.uniqueId = :examOwnerId and e.student=f.student and " +
                    "o.ownerType=:ownerType and o.ownerId=f.clazz.schedulingSubpart.instrOfferingConfig.uniqueId and o.exam.examType.uniqueId=:examTypeId", Object[].class)
                    .setParameter("ownerType", ExamOwner.sOwnerTypeConfig)
                    .setParameter("examTypeId", getExam().getExamType().getUniqueId())
                    .setParameter("examOwnerId", getOwnerId())
                    .setCacheable(true).list()) {
                Long studentId = (Long)o[0];
                Exam exam = (Exam)o[1];
                Set<Exam> exams  = studentExams.get(studentId);
                if (exams==null) { exams = new HashSet(); studentExams.put(studentId, exams); }
                exams.add(exam);
            }
            for (Object[] o: ExamOwnerDAO.getInstance().getSession().createQuery(
                    "select e.student.uniqueId, o.exam from ExamOwner o, StudentClassEnrollment f, StudentClassEnrollment e inner join e.clazz c " +
                    "where c.uniqueId = :examOwnerId and e.student=f.student and " +
                    "o.ownerType=:ownerType and o.ownerId=f.courseOffering.uniqueId and o.exam.examType.uniqueId=:examTypeId", Object[].class)
                    .setParameter("ownerType", ExamOwner.sOwnerTypeCourse)
                    .setParameter("examTypeId", getExam().getExamType().getUniqueId())
                    .setParameter("examOwnerId", getOwnerId())
                    .setCacheable(true).list()) {
                Long studentId = (Long)o[0];
                Exam exam = (Exam)o[1];
                Set<Exam> exams  = studentExams.get(studentId);
                if (exams==null) { exams = new HashSet(); studentExams.put(studentId, exams); }
                exams.add(exam);
            }
            for (Object[] o: ExamOwnerDAO.getInstance().getSession().createQuery(
                    "select e.student.uniqueId, o.exam from ExamOwner o, StudentClassEnrollment f, StudentClassEnrollment e inner join e.clazz c " +
                    "where c.uniqueId = :examOwnerId and e.student=f.student and " +
                    "o.ownerType=:ownerType and o.ownerId=f.courseOffering.instructionalOffering.uniqueId and o.exam.examType.uniqueId=:examTypeId", Object[].class)
                    .setParameter("ownerType", ExamOwner.sOwnerTypeOffering)
                    .setParameter("examTypeId", getExam().getExamType().getUniqueId())
                    .setParameter("examOwnerId", getOwnerId())
                    .setCacheable(true).list()) {
                Long studentId = (Long)o[0];
                Exam exam = (Exam)o[1];
                Set<Exam> exams  = studentExams.get(studentId);
                if (exams==null) { exams = new HashSet(); studentExams.put(studentId, exams); }
                exams.add(exam);
            }
            break;
        case sOwnerTypeConfig :
            for (Object[] o: ExamOwnerDAO.getInstance().getSession().createQuery(
                    "select e.student.uniqueId, o.exam from ExamOwner o, StudentClassEnrollment f, StudentClassEnrollment e inner join e.clazz c " +
                    "where c.schedulingSubpart.instrOfferingConfig.uniqueId = :examOwnerId and e.student=f.student and " +
                    "o.ownerType=:ownerType and o.ownerId=f.clazz.uniqueId and o.exam.examType.uniqueId=:examTypeId", Object[].class)
                    .setParameter("ownerType", ExamOwner.sOwnerTypeClass)
                    .setParameter("examTypeId", getExam().getExamType().getUniqueId())
                    .setParameter("examOwnerId", getOwnerId())
                    .setCacheable(true).list()) {
                Long studentId = (Long)o[0];
                Exam exam = (Exam)o[1];
                Set<Exam> exams  = studentExams.get(studentId);
                if (exams==null) { exams = new HashSet(); studentExams.put(studentId, exams); }
                exams.add(exam);
            }
            for (Object[] o: ExamOwnerDAO.getInstance().getSession().createQuery(
                    "select e.student.uniqueId, o.exam from ExamOwner o, StudentClassEnrollment f, StudentClassEnrollment e inner join e.clazz c " +
                    "where c.schedulingSubpart.instrOfferingConfig.uniqueId = :examOwnerId and e.student=f.student and " +
                    "o.ownerType=:ownerType and o.ownerId=f.clazz.schedulingSubpart.instrOfferingConfig.uniqueId and o.exam.examType.uniqueId=:examTypeId", Object[].class)
                    .setParameter("ownerType", ExamOwner.sOwnerTypeConfig)
                    .setParameter("examTypeId", getExam().getExamType().getUniqueId())
                    .setParameter("examOwnerId", getOwnerId())
                    .setCacheable(true).list()) {
                Long studentId = (Long)o[0];
                Exam exam = (Exam)o[1];
                Set<Exam> exams  = studentExams.get(studentId);
                if (exams==null) { exams = new HashSet(); studentExams.put(studentId, exams); }
                exams.add(exam);
            }
            for (Object[] o: ExamOwnerDAO.getInstance().getSession().createQuery(
                    "select e.student.uniqueId, o.exam from ExamOwner o, StudentClassEnrollment f, StudentClassEnrollment e inner join e.clazz c " +
                    "where c.schedulingSubpart.instrOfferingConfig.uniqueId = :examOwnerId and e.student=f.student and " +
                    "o.ownerType=:ownerType and o.ownerId=f.courseOffering.uniqueId and o.exam.examType.uniqueId=:examTypeId", Object[].class)
                    .setParameter("ownerType", ExamOwner.sOwnerTypeCourse)
                    .setParameter("examTypeId", getExam().getExamType().getUniqueId())
                    .setParameter("examOwnerId", getOwnerId())
                    .setCacheable(true).list()) {
                Long studentId = (Long)o[0];
                Exam exam = (Exam)o[1];
                Set<Exam> exams  = studentExams.get(studentId);
                if (exams==null) { exams = new HashSet(); studentExams.put(studentId, exams); }
                exams.add(exam);
            }
            for (Object[] o: ExamOwnerDAO.getInstance().getSession().createQuery(
                    "select e.student.uniqueId, o.exam from ExamOwner o, StudentClassEnrollment f, StudentClassEnrollment e inner join e.clazz c " +
                    "where c.schedulingSubpart.instrOfferingConfig.uniqueId = :examOwnerId and e.student=f.student and " +
                    "o.ownerType=:ownerType and o.ownerId=f.courseOffering.instructionalOffering.uniqueId and o.exam.examType.uniqueId=:examTypeId", Object[].class)
                    .setParameter("ownerType", ExamOwner.sOwnerTypeOffering)
                    .setParameter("examTypeId", getExam().getExamType().getUniqueId())
                    .setParameter("examOwnerId", getOwnerId())
                    .setCacheable(true).list()) {
                Long studentId = (Long)o[0];
                Exam exam = (Exam)o[1];
                Set<Exam> exams  = studentExams.get(studentId);
                if (exams==null) { exams = new HashSet(); studentExams.put(studentId, exams); }
                exams.add(exam);
            }
            break;
        case sOwnerTypeCourse :
            for (Object[] o: ExamOwnerDAO.getInstance().getSession().createQuery(
                    "select e.student.uniqueId, o.exam from ExamOwner o, StudentClassEnrollment f, StudentClassEnrollment e inner join e.courseOffering co " +
                    "where co.uniqueId = :examOwnerId and e.student=f.student and " +
                    "o.ownerType=:ownerType and o.ownerId=f.clazz.uniqueId and o.exam.examType.uniqueId=:examTypeId", Object[].class)
                    .setParameter("ownerType", ExamOwner.sOwnerTypeClass)
                    .setParameter("examTypeId", getExam().getExamType().getUniqueId())
                    .setParameter("examOwnerId", getOwnerId())
                    .setCacheable(true).list()) {
                Long studentId = (Long)o[0];
                Exam exam = (Exam)o[1];
                Set<Exam> exams  = studentExams.get(studentId);
                if (exams==null) { exams = new HashSet(); studentExams.put(studentId, exams); }
                exams.add(exam);
            }
            for (Object[] o: ExamOwnerDAO.getInstance().getSession().createQuery(
                    "select e.student.uniqueId, o.exam from ExamOwner o, StudentClassEnrollment f, StudentClassEnrollment e inner join e.courseOffering co " +
                    "where co.uniqueId = :examOwnerId and e.student=f.student and " +
                    "o.ownerType=:ownerType and o.ownerId=f.clazz.schedulingSubpart.instrOfferingConfig.uniqueId and o.exam.examType.uniqueId=:examTypeId", Object[].class)
                    .setParameter("ownerType", ExamOwner.sOwnerTypeConfig)
                    .setParameter("examTypeId", getExam().getExamType().getUniqueId())
                    .setParameter("examOwnerId", getOwnerId())
                    .setCacheable(true).list()) {
                Long studentId = (Long)o[0];
                Exam exam = (Exam)o[1];
                Set<Exam> exams  = studentExams.get(studentId);
                if (exams==null) { exams = new HashSet(); studentExams.put(studentId, exams); }
                exams.add(exam);
            }
            for (Object[] o: ExamOwnerDAO.getInstance().getSession().createQuery(
                    "select e.student.uniqueId, o.exam from ExamOwner o, StudentClassEnrollment f, StudentClassEnrollment e inner join e.courseOffering co " +
                    "where co.uniqueId = :examOwnerId and e.student=f.student and  " +
                    "o.ownerType=:ownerType and o.ownerId=f.courseOffering.uniqueId and o.exam.examType.uniqueId=:examTypeId", Object[].class)
                    .setParameter("ownerType", ExamOwner.sOwnerTypeCourse)
                    .setParameter("examTypeId", getExam().getExamType().getUniqueId())
                    .setParameter("examOwnerId", getOwnerId())
                    .setCacheable(true).list()) {
                Long studentId = (Long)o[0];
                Exam exam = (Exam)o[1];
                Set<Exam> exams  = studentExams.get(studentId);
                if (exams==null) { exams = new HashSet(); studentExams.put(studentId, exams); }
                exams.add(exam);
            }
            for (Object[] o: ExamOwnerDAO.getInstance().getSession().createQuery(
                    "select e.student.uniqueId, o.exam from ExamOwner o, StudentClassEnrollment f, StudentClassEnrollment e inner join e.courseOffering co " +
                    "where co.uniqueId = :examOwnerId and e.student=f.student and " +
                    "o.ownerType=:ownerType and o.ownerId=f.courseOffering.instructionalOffering.uniqueId and o.exam.examType.uniqueId=:examTypeId", Object[].class)
                    .setParameter("ownerType", ExamOwner.sOwnerTypeOffering)
                    .setParameter("examTypeId", getExam().getExamType().getUniqueId())
                    .setParameter("examOwnerId", getOwnerId())
                    .setCacheable(true).list()) {
                Long studentId = (Long)o[0];
                Exam exam = (Exam)o[1];
                Set<Exam> exams  = studentExams.get(studentId);
                if (exams==null) { exams = new HashSet(); studentExams.put(studentId, exams); }
                exams.add(exam);
            }
            break;
        case sOwnerTypeOffering :
            for (Object[] o: ExamOwnerDAO.getInstance().getSession().createQuery(
                    "select e.student.uniqueId, o.exam from ExamOwner o, StudentClassEnrollment f, StudentClassEnrollment e inner join e.courseOffering co " +
                    "where co.instructionalOffering.uniqueId = :examOwnerId and e.student=f.student and " +
                    "o.ownerType=:ownerType and o.ownerId=f.clazz.uniqueId and o.exam.examType.uniqueId=:examTypeId", Object[].class)
                    .setParameter("ownerType", ExamOwner.sOwnerTypeClass)
                    .setParameter("examTypeId", getExam().getExamType().getUniqueId())
                    .setParameter("examOwnerId", getOwnerId())
                    .setCacheable(true).list()) {
                Long studentId = (Long)o[0];
                Exam exam = (Exam)o[1];
                Set<Exam> exams  = studentExams.get(studentId);
                if (exams==null) { exams = new HashSet(); studentExams.put(studentId, exams); }
                exams.add(exam);
            }
            for (Object[] o: ExamOwnerDAO.getInstance().getSession().createQuery(
                    "select e.student.uniqueId, o.exam from ExamOwner o, StudentClassEnrollment f, StudentClassEnrollment e inner join e.courseOffering co " +
                    "where co.instructionalOffering.uniqueId = :examOwnerId and e.student=f.student and " +
                    "o.ownerType=:ownerType and o.ownerId=f.clazz.schedulingSubpart.instrOfferingConfig.uniqueId and o.exam.examType.uniqueId=:examTypeId", Object[].class)
                    .setParameter("ownerType", ExamOwner.sOwnerTypeConfig)
                    .setParameter("examTypeId", getExam().getExamType().getUniqueId())
                    .setParameter("examOwnerId", getOwnerId())
                    .setCacheable(true).list()) {
                Long studentId = (Long)o[0];
                Exam exam = (Exam)o[1];
                Set<Exam> exams  = studentExams.get(studentId);
                if (exams==null) { exams = new HashSet(); studentExams.put(studentId, exams); }
                exams.add(exam);
            }
            for (Object[] o: ExamOwnerDAO.getInstance().getSession().createQuery(
                    "select e.student.uniqueId, o.exam from ExamOwner o, StudentClassEnrollment f, StudentClassEnrollment e inner join e.courseOffering co " +
                    "where co.instructionalOffering.uniqueId = :examOwnerId and e.student=f.student and " +
                    "o.ownerType=:ownerType and o.ownerId=f.courseOffering.uniqueId and o.exam.examType.uniqueId=:examTypeId", Object[].class)
                    .setParameter("ownerType", ExamOwner.sOwnerTypeCourse)
                    .setParameter("examTypeId", getExam().getExamType().getUniqueId())
                    .setParameter("examOwnerId", getOwnerId())
                    .setCacheable(true).list()) {
                Long studentId = (Long)o[0];
                Exam exam = (Exam)o[1];
                Set<Exam> exams  = studentExams.get(studentId);
                if (exams==null) { exams = new HashSet(); studentExams.put(studentId, exams); }
                exams.add(exam);
            }
            for (Object[] o: ExamOwnerDAO.getInstance().getSession().createQuery(
                    "select e.student.uniqueId, o.exam from ExamOwner o, StudentClassEnrollment f, StudentClassEnrollment e inner join e.courseOffering co " +
                    "where co.instructionalOffering.uniqueId = :examOwnerId and e.student=f.student and " +
                    "o.ownerType=:ownerType and o.ownerId=f.courseOffering.instructionalOffering.uniqueId and o.exam.examType.uniqueId=:examTypeId", Object[].class)
                    .setParameter("ownerType", ExamOwner.sOwnerTypeOffering)
                    .setParameter("examTypeId", getExam().getExamType().getUniqueId())
                    .setParameter("examOwnerId", getOwnerId())
                    .setCacheable(true).list()) {
                Long studentId = (Long)o[0];
                Exam exam = (Exam)o[1];
                Set<Exam> exams  = studentExams.get(studentId);
                if (exams==null) { exams = new HashSet(); studentExams.put(studentId, exams); }
                exams.add(exam);
            }
            break;
        }
    }
    
    protected void computeStudentAssignments(Hashtable<Assignment, Set<Long>> studentAssignments) {
        switch (getOwnerType()) {
        case sOwnerTypeClass :
            for (Object[] o: ExamOwnerDAO.getInstance().getSession().createQuery(
                "select e.student.uniqueId, a from Assignment a, StudentClassEnrollment f, StudentClassEnrollment e inner join e.clazz c " +
                "where c.uniqueId = :examOwnerId and " +
                "e.student=f.student and f.clazz = a.clazz and a.solution.commited = true", Object[].class)
                .setParameter("examOwnerId", getOwnerId())
                .setCacheable(true).list()) {
                Long studentId = (Long)o[0];
                Assignment assignment = (Assignment)o[1];
                Set<Long> students  = studentAssignments.get(assignment);
                if (students==null) { students = new HashSet(); studentAssignments.put(assignment, students); }
                students.add(studentId);
            }
            break;
        case sOwnerTypeConfig :
            for (Object[] o: ExamOwnerDAO.getInstance().getSession().createQuery(
                    "select e.student.uniqueId, a from Assignment a, StudentClassEnrollment f, StudentClassEnrollment e inner join e.clazz c " +
                    "where c.schedulingSubpart.instrOfferingConfig.uniqueId = :examOwnerId and " +
                    "e.student=f.student and f.clazz = a.clazz and a.solution.commited = true", Object[].class)
                    .setParameter("examOwnerId", getOwnerId())
                    .setCacheable(true).list()) {
                Long studentId = (Long)o[0];
                Assignment assignment = (Assignment)o[1];
                Set<Long> students  = studentAssignments.get(assignment);
                if (students==null) { students = new HashSet(); studentAssignments.put(assignment, students); }
                students.add(studentId);
            }
            break;
        case sOwnerTypeCourse :
            for (Object[] o: ExamOwnerDAO.getInstance().getSession().createQuery(
                    "select e.student.uniqueId, a from Assignment a, StudentClassEnrollment f, StudentClassEnrollment e inner join e.courseOffering co " +
                    "where co.uniqueId = :examOwnerId and " +
                    "e.student=f.student and f.clazz = a.clazz and a.solution.commited = true", Object[].class)
                    .setParameter("examOwnerId", getOwnerId())
                    .setCacheable(true).list()) {
                Long studentId = (Long)o[0];
                Assignment assignment = (Assignment)o[1];
                Set<Long> students  = studentAssignments.get(assignment);
                if (students==null) { students = new HashSet(); studentAssignments.put(assignment, students); }
                students.add(studentId);
            }
            break;
        case sOwnerTypeOffering :
            for (Object[] o: ExamOwnerDAO.getInstance().getSession().createQuery(
                    "select e.student.uniqueId, a from Assignment a, StudentClassEnrollment f, StudentClassEnrollment e inner join e.courseOffering co " +
                    "where co.instructionalOffering.uniqueId = :examOwnerId and " +
                    "e.student=f.student and f.clazz = a.clazz and a.solution.commited = true", Object[].class)
                    .setParameter("examOwnerId", getOwnerId())
                    .setCacheable(true).list()) {
                Long studentId = (Long)o[0];
                Assignment assignment = (Assignment)o[1];
                Set<Long> students  = studentAssignments.get(assignment);
                if (students==null) { students = new HashSet(); studentAssignments.put(assignment, students); }
                students.add(studentId);
            }
            break;
        }
    }
    
    protected void computeOverlappingStudentMeetings(Hashtable<Meeting, Set<Long>> studentMeetings, Long periodId) {
        switch (getOwnerType()) {
        case sOwnerTypeClass :
            for (Object[] o: ExamOwnerDAO.getInstance().getSession().createQuery(
                "select e.student.uniqueId, m from ClassEvent ce inner join ce.meetings m inner join ce.clazz.studentEnrollments f, StudentClassEnrollment e inner join e.clazz c, ExamPeriod p " +
                "where c.uniqueId = :examOwnerId and e.student=f.student and "+
                "p.uniqueId=:periodId and p.startSlot - :travelTime < m.stopPeriod and m.startPeriod < p.startSlot + p.length + :travelTime and "+
                HibernateUtil.addDate("p.session.examBeginDate","p.dateOffset")+" = m.meetingDate", Object[].class)
                .setParameter("examOwnerId", getOwnerId())
                .setParameter("travelTime", ApplicationProperty.ExaminationTravelTimeClass.intValue())
                .setParameter("periodId", periodId)
                .setCacheable(true).list()) {
                Long studentId = (Long)o[0];
                Meeting meeting = (Meeting)o[1];
                Set<Long> students  = studentMeetings.get(meeting);
                if (students==null) { students = new HashSet(); studentMeetings.put(meeting, students); }
                students.add(studentId);
            }
            break;
        case sOwnerTypeConfig :
            for (Object[] o: ExamOwnerDAO.getInstance().getSession().createQuery(
                    "select e.student.uniqueId, m from ClassEvent ce inner join ce.meetings m inner join ce.clazz.studentEnrollments f, StudentClassEnrollment e inner join e.clazz c, ExamPeriod p " +
                    "where c.schedulingSubpart.instrOfferingConfig.uniqueId = :examOwnerId and e.student=f.student and "+
                    "p.uniqueId=:periodId and p.startSlot - :travelTime < m.stopPeriod and m.startPeriod < p.startSlot + p.length + :travelTime and "+
                    HibernateUtil.addDate("p.session.examBeginDate","p.dateOffset")+" = m.meetingDate", Object[].class)
                    .setParameter("examOwnerId", getOwnerId())
                    .setParameter("travelTime", ApplicationProperty.ExaminationTravelTimeClass.intValue())
                    .setParameter("periodId", periodId)
                    .setCacheable(true).list()) {
                Long studentId = (Long)o[0];
                Meeting meeting = (Meeting)o[1];
                Set<Long> students  = studentMeetings.get(meeting);
                if (students==null) { students = new HashSet(); studentMeetings.put(meeting, students); }
                students.add(studentId);
            }
            break;
        case sOwnerTypeCourse :
            for (Object[] o: ExamOwnerDAO.getInstance().getSession().createQuery(
                    "select e.student.uniqueId, m from ClassEvent ce inner join ce.meetings m inner join ce.clazz.studentEnrollments f, StudentClassEnrollment e inner join e.courseOffering co, ExamPeriod p " +
                    "where co.uniqueId = :examOwnerId and e.student=f.student and "+
                    "p.uniqueId=:periodId and p.startSlot - :travelTime < m.stopPeriod and m.startPeriod < p.startSlot + p.length + :travelTime and "+
                    HibernateUtil.addDate("p.session.examBeginDate","p.dateOffset")+" = m.meetingDate", Object[].class)
                    .setParameter("examOwnerId", getOwnerId())
                    .setParameter("travelTime", ApplicationProperty.ExaminationTravelTimeClass.intValue())
                    .setParameter("periodId", periodId)
                    .setCacheable(true).list()) {
                Long studentId = (Long)o[0];
                Meeting meeting = (Meeting)o[1];
                Set<Long> students  = studentMeetings.get(meeting);
                if (students==null) { students = new HashSet(); studentMeetings.put(meeting, students); }
                students.add(studentId);
            }
            break;
        case sOwnerTypeOffering :
            for (Object[] o: ExamOwnerDAO.getInstance().getSession().createQuery(
                    "select e.student.uniqueId, m from ClassEvent ce inner join ce.meetings m inner join ce.clazz.studentEnrollments f, StudentClassEnrollment e inner join e.courseOffering co, ExamPeriod p " +
                    "where co.instructionalOffering.uniqueId = :examOwnerId and e.student=f.student and "+
                    "p.uniqueId=:periodId and p.startSlot - :travelTime < m.stopPeriod and m.startPeriod < p.startSlot + p.length + :travelTime and "+
                    HibernateUtil.addDate("p.session.examBeginDate","p.dateOffset")+" = m.meetingDate", Object[].class)
                    .setParameter("examOwnerId", getOwnerId())
                    .setParameter("travelTime", ApplicationProperty.ExaminationTravelTimeClass.intValue())
                    .setParameter("periodId", periodId)
                    .setCacheable(true).list()) {
                Long studentId = (Long)o[0];
                Meeting meeting = (Meeting)o[1];
                Set<Long> students  = studentMeetings.get(meeting);
                if (students==null) { students = new HashSet(); studentMeetings.put(meeting, students); }
                students.add(studentId);
            }
            break;
        }
    }
    
    public int countStudents() {
        switch (getOwnerType()) {
        case sOwnerTypeClass : 
            return ExamOwnerDAO.getInstance().getSession().createQuery(
                    "select count(distinct e.student) from " +
                    "StudentClassEnrollment e inner join e.clazz c  " +
                    "where c.uniqueId = :examOwnerId", Number.class)
                    .setParameter("examOwnerId", getOwnerId())
                    .setCacheable(true)
                    .uniqueResult().intValue();
        case sOwnerTypeConfig : 
            return ExamOwnerDAO.getInstance().getSession().createQuery(
                    "select count(distinct e.student) from " +
                    "StudentClassEnrollment e inner join e.clazz c  " +
                    "where c.schedulingSubpart.instrOfferingConfig.uniqueId = :examOwnerId", Number.class)
                    .setParameter("examOwnerId", getOwnerId())
                    .setCacheable(true)
                    .uniqueResult().intValue();
        case sOwnerTypeCourse : 
            return ExamOwnerDAO.getInstance().getSession().createQuery(
                    "select count(distinct e.student) from " +
                    "StudentClassEnrollment e inner join e.courseOffering co  " +
                    "where co.uniqueId = :examOwnerId", Number.class)
                    .setParameter("examOwnerId", getOwnerId())
                    .setCacheable(true)
                    .uniqueResult().intValue();
        case sOwnerTypeOffering : 
            return ExamOwnerDAO.getInstance().getSession().createQuery(
                    "select count(distinct e.student) from " +
                    "StudentClassEnrollment e inner join e.courseOffering co  " +
                    "where co.instructionalOffering.uniqueId = :examOwnerId", Number.class)
                    .setParameter("examOwnerId", getOwnerId())
                    .setCacheable(true)
                    .uniqueResult().intValue();
        default : throw new RuntimeException("Unknown owner type "+getOwnerType());
        }
    }
    
    public int countStudents(CourseOffering co) {
        switch (getOwnerType()) {
        case sOwnerTypeClass : 
            return ExamOwnerDAO.getInstance().getSession().createQuery(
                    "select count(distinct e.student) from " +
                    "StudentClassEnrollment e inner join e.clazz c  " +
                    "where c.uniqueId = :examOwnerId and e.courseOffering.uniqueId=:courseOfferingId", Number.class)
                    .setParameter("examOwnerId", getOwnerId())
                    .setParameter("courseOfferingId", co.getUniqueId())
                    .setCacheable(true)
                    .uniqueResult().intValue();
        case sOwnerTypeConfig : 
            return ExamOwnerDAO.getInstance().getSession().createQuery(
                    "select count(distinct e.student) from " +
                    "StudentClassEnrollment e inner join e.clazz c  " +
                    "where c.schedulingSubpart.instrOfferingConfig.uniqueId = :examOwnerId and e.courseOffering.uniqueId=:courseOfferingId", Number.class)
                    .setParameter("examOwnerId", getOwnerId())
                    .setParameter("courseOfferingId", co.getUniqueId())
                    .setCacheable(true)
                    .uniqueResult().intValue();
        case sOwnerTypeCourse : 
            return ExamOwnerDAO.getInstance().getSession().createQuery(
                    "select count(distinct e.student) from " +
                    "StudentClassEnrollment e inner join e.courseOffering co  " +
                    "where co.uniqueId = :examOwnerId and e.courseOffering.uniqueId=:courseOfferingId", Number.class)
                    .setParameter("examOwnerId", getOwnerId())
                    .setParameter("courseOfferingId", co.getUniqueId())
                    .setCacheable(true)
                    .uniqueResult().intValue();
        case sOwnerTypeOffering : 
            return ExamOwnerDAO.getInstance().getSession().createQuery(
                    "select count(distinct e.student) from " +
                    "StudentClassEnrollment e inner join e.courseOffering co  " +
                    "where co.instructionalOffering.uniqueId = :examOwnerId and e.courseOffering.uniqueId=:courseOfferingId", Number.class)
                    .setParameter("examOwnerId", getOwnerId())
                    .setParameter("courseOfferingId", co.getUniqueId())
                    .setCacheable(true)
                    .uniqueResult().intValue();
        default : throw new RuntimeException("Unknown owner type "+getOwnerType());
        }
    }
    
	@Transient
    public int getLimit() {
        Object owner = getOwnerObject();
        switch (getOwnerType()) {
            case sOwnerTypeClass : 
                return ((Class_)owner).getClassLimit();
            case sOwnerTypeConfig : 
                return ((InstrOfferingConfig)owner).getLimit();
            case sOwnerTypeCourse : 
                CourseOffering course = ((CourseOffering)owner);
                if (course.getReservation() != null)
                	return course.getReservation();
                return (course.getInstructionalOffering().getLimit() == null ? 0 : course.getInstructionalOffering().getLimit());
            case sOwnerTypeOffering : 
                return (((InstructionalOffering)owner).getLimit()==null?0:((InstructionalOffering)owner).getLimit());
            default : throw new RuntimeException("Unknown owner type "+getOwnerType());
        }
    }
    
	@Transient
    public int getSize() {
        boolean considerLimit = ApplicationProperty.ExaminationSizeUseLimitInsteadOfEnrollment.isTrue(getExam().getExamType().getReference(), getExam().getExamType().getType() != ExamType.sExamTypeFinal);
        return (considerLimit?Math.max(countStudents(), getLimit()):countStudents());
    }
    
    public int getSize(CourseOffering co) {
    	boolean considerLimit = ApplicationProperty.ExaminationSizeUseLimitInsteadOfEnrollment.isTrue(getExam().getExamType().getReference(), getExam().getExamType().getType() != ExamType.sExamTypeFinal);
        return (considerLimit?Math.max(countStudents(), getLimit()):countStudents(co));
    }

	@Transient
    public String getLabel() {
        return genName(ApplicationProperties.getProperty("tmtbl.exam.name."+ExamOwner.sOwnerTypes[getOwnerType()]));
    }

	@Transient
    public String getSubject() {
        return getCourse().getSubjectAreaAbbv();
    }
    
	@Transient
    public String getCourseNbr() {
        return getCourse().getCourseNbr();
    }
    
	@Transient
    public String getItype() {
        switch (getOwnerType()) {
            case sOwnerTypeClass : 
                if (ApplicationProperty.ExaminationReportsExternalId.isTrue()) {
                    String ext = ((Class_)getOwnerObject()).getExternalId(getCourse());
                    return (ext==null?"":ext);
                } else
                    return ((Class_)getOwnerObject()).getSchedulingSubpart().getItypeDesc();
            case sOwnerTypeConfig : 
                return "["+((InstrOfferingConfig)getOwnerObject()).getName()+"]";
            case sOwnerTypeCourse : 
            case sOwnerTypeOffering : 
                return "";
            default : throw new RuntimeException("Unknown owner type "+getOwnerType());
        }
    }
    
	@Transient
    public String getSection() {
        switch (getOwnerType()) {
            case sOwnerTypeClass :
                Class_ clazz = (Class_)getOwnerObject();
                return (ApplicationProperty.ExaminationReportsClassSufix.isTrue() && clazz.getClassSuffix(getCourse())!=null?clazz.getClassSuffix(getCourse()):clazz.getSectionNumberString());
            case sOwnerTypeConfig : 
                if (ApplicationProperty.ExaminationReportsShowInstructionalType.isFalse())
                    return "["+((InstrOfferingConfig)getOwnerObject()).getName()+"]";
            case sOwnerTypeCourse : 
            case sOwnerTypeOffering : 
                return "";
            default : throw new RuntimeException("Unknown owner type "+getOwnerType());
        }
    }
    
    public String genName(String pattern) {
        String name = pattern;
        int idx = -1;
        while (name.indexOf('%',idx+1)>=0) {
            idx = name.indexOf('%',idx);
            char code = name.charAt(idx+1);
            String name4code = genName(code);
            name = name.substring(0,idx)+(name4code==null?"":name4code)+name.substring(idx+2);
        }
        return name;
    }
    
    /**
     * Basic codes
     * s ... subject area
     * c ... course number
     * i ... itype abbv
     * n ... section number
     * x ... configuration name
     * Additional codes
     * d ... department abbv
     * a ... class suffix (div-sec number)
     * y ... itype suffix (a, b etc.)
     * e ... class external id
     * f ... course external id
     * o ... offering external id
     * t ... exam type suffix (tmtbl.exam.name.type.Final and tmtbl.exam.name.type.Midterm)
     * I ... itype code
     * p ... itype parent abbv
     * P ... itype parent code
     * m ... instructional method reference
     * M ... instructional method label
     * _ ... space
     * T ... course title
     */
	protected String genName(char code) {
        switch (code) {
        case '_' : return " ";
        case 's' : return getCourse().getSubjectArea().getSubjectAreaAbbreviation();
        case 'c' : return getCourse().getCourseNbr();
        case 'T' : return getCourse().getTitle();
        case 'i' :
            switch (getOwnerType()) {
            case sOwnerTypeClass : return ((Class_)getOwnerObject()).getSchedulingSubpart().getItypeDesc().trim();
            default : return "";
            }
        case 'n' :
            switch (getOwnerType()) {
            case sOwnerTypeClass : return ((Class_)getOwnerObject()).getSectionNumberString();
            default : return "";
            }
        case 'x' :
            switch (getOwnerType()) {
            case sOwnerTypeClass : return ((Class_)getOwnerObject()).getSchedulingSubpart().getInstrOfferingConfig().getName();
            case sOwnerTypeConfig : return ((InstrOfferingConfig)getOwnerObject()).getName();
            default : return "";
            }
        case 'D' :
            return getCourse().getDepartment().getDeptCode();
        case 'd' :
            Department d = getCourse().getDepartment();
            return (d.getAbbreviation()==null || d.getAbbreviation().length()==0?d.getDeptCode():d.getAbbreviation());
        case 'a' :
            switch (getOwnerType()) {
            case sOwnerTypeClass : return ((Class_)getOwnerObject()).getClassSuffix(getCourse());
            default : return "";
            }
        case 'y' :
            switch (getOwnerType()) {
            case sOwnerTypeClass : return ((Class_)getOwnerObject()).getSchedulingSubpart().getSchedulingSubpartSuffix();
            default : return "";
            }
        case 'e' :
            switch (getOwnerType()) {
            case sOwnerTypeClass : return ((Class_)getOwnerObject()).getExternalId(getCourse());
            default : return "";
            }
        case 'f' :
            return getCourse().getExternalUniqueId();
        case 'o' :
            return getCourse().getInstructionalOffering().getExternalUniqueId();
        case 't' :
            return ApplicationProperties.getProperty("tmtbl.exam.name.type."+getExam().getExamType().getReference());
        case 'I' :
            switch (getOwnerType()) {
            case sOwnerTypeClass : return ((Class_)getOwnerObject()).getSchedulingSubpart().getItype().getItype().toString();
            default : return "";
            }
        case 'p' :
            switch (getOwnerType()) {
            case sOwnerTypeClass :
                ItypeDesc itype = ((Class_)getOwnerObject()).getSchedulingSubpart().getItype();
                while (itype.getParent()!=null) itype = itype.getParent();
                return itype.getAbbv();
            default : return "";
            }
        case 'P' :
            switch (getOwnerType()) {
            case sOwnerTypeClass : 
                ItypeDesc itype = ((Class_)getOwnerObject()).getSchedulingSubpart().getItype();
                while (itype.getParent()!=null) itype = itype.getParent();
                return itype.getItype().toString();
            default : return "";
            }
        case 'm':
        	switch (getOwnerType()) {
            case sOwnerTypeConfig :
            	InstructionalMethod im = ((InstrOfferingConfig)getOwnerObject()).getInstructionalMethod();
            	if (im != null) return im.getReference();
            	return "";
            case sOwnerTypeClass:
            	im = ((Class_)getOwnerObject()).getSchedulingSubpart().getInstrOfferingConfig().getInstructionalMethod();
            	if (im != null) return im.getReference();
            	return "";
            default:
            	if (getCourse().getInstructionalOffering().getInstrOfferingConfigs().size() == 1) {
            		im = getCourse().getInstructionalOffering().getInstrOfferingConfigs().iterator().next().getInstructionalMethod();
            		if (im != null) return im.getReference();	
            	}
            	return "";
        	}
        case 'M':
        	switch (getOwnerType()) {
            case sOwnerTypeConfig :
            	InstructionalMethod im = ((InstrOfferingConfig)getOwnerObject()).getInstructionalMethod();
            	if (im != null) return im.getLabel();
            	return "";
            case sOwnerTypeClass:
            	im = ((Class_)getOwnerObject()).getSchedulingSubpart().getInstrOfferingConfig().getInstructionalMethod();
            	if (im != null) return im.getLabel();
            	return "";
            default:
            	if (getCourse().getInstructionalOffering().getInstrOfferingConfigs().size() == 1) {
            		im = getCourse().getInstructionalOffering().getInstrOfferingConfigs().iterator().next().getInstructionalMethod();
            		if (im != null) return im.getLabel();	
            	}
            	return "";
        	}
        }
        return "";
    }
}
