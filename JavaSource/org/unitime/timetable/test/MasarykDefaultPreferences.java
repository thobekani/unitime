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
package org.unitime.timetable.test;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.unitime.commons.hibernate.util.HibernateUtil;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.BuildingPref;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.DistributionObject;
import org.unitime.timetable.model.DistributionPref;
import org.unitime.timetable.model.DistributionType;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.Meeting;
import org.unitime.timetable.model.NonUniversityLocation;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.Room;
import org.unitime.timetable.model.RoomGroup;
import org.unitime.timetable.model.RoomGroupPref;
import org.unitime.timetable.model.RoomPref;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.TimePattern;
import org.unitime.timetable.model.TimePatternDays;
import org.unitime.timetable.model.TimePatternModel;
import org.unitime.timetable.model.TimePatternTime;
import org.unitime.timetable.model.TimePref;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.util.Constants;

/**
 * @author Tomas Muller
 */
public class MasarykDefaultPreferences {
    protected static Log sLog = LogFactory.getLog(MasarykDefaultPreferences.class);
    
    private static String ident(CourseOffering co, Class_ c, org.hibernate.Session hibSession) {
    	if (co.getExternalUniqueId() == null) return null;
    	String ext = co.getExternalUniqueId();
    	if (c.getClassSuffix() != null && !c.getClassSuffix().isEmpty()) 
    		ext += "/" + c.getClassSuffix();
    	else if (c.getExternalUniqueId() != null && !c.getExternalUniqueId().isEmpty() && c.getExternalUniqueId().indexOf('/') >= 0)
			ext += c.getExternalUniqueId().substring(c.getExternalUniqueId().indexOf('/'));
		return ext;
    }
    
	public static void main(String[] args) {
        try {
        	// select count(p) from Preference p where p.owner not in (select g from PreferenceGroup g)
            HibernateUtil.configureHibernate(ApplicationProperties.getProperties());

            org.hibernate.Session hibSession = new _RootDAO().getSession();
            
            Session session = Session.getSessionUsingInitiativeYearTerm(
                    ApplicationProperties.getProperty("initiative", "FF"),
                    ApplicationProperties.getProperty("year","2011"),
                    ApplicationProperties.getProperty("term","Jaro")
                    );
            
            boolean incremental = "true".equalsIgnoreCase(ApplicationProperties.getProperty("incremental","false"));
            boolean addMeetWith = "true".equalsIgnoreCase(ApplicationProperties.getProperty("meetwith","false"));
            boolean useScheduleNote = "true".equalsIgnoreCase(ApplicationProperties.getProperty("note","true"));
            
            if (session==null) {
                sLog.error("Academic session not found, use properties initiative, year, and term to set academic session.");
                System.exit(0);
            } else {
                sLog.info("Session: "+session);
            }
            
            Session previous = Session.getSessionUsingInitiativeYearTerm(
            		session.getAcademicInitiative(), String.valueOf(Integer.parseInt(session.getAcademicYear())-1), session.getAcademicTerm());
            Hashtable<String, Float> oldRatios = new Hashtable<String, Float>();
            if (previous != null) {
                for (SubjectArea sa: previous.getSubjectAreas()) {
                	for (CourseOffering co: sa.getCourseOfferings()) {
                		for (InstrOfferingConfig cfg: co.getInstructionalOffering().getInstrOfferingConfigs()) {
                			for (SchedulingSubpart ss: cfg.getSchedulingSubparts()) {
                				for (Class_ c: ss.getClasses()) {
                					if (c.getRoomRatio() == null || c.getRoomRatio() <= 0f || c.getRoomRatio() >= 1f) continue;
                					String id = ident(co, c, hibSession);
                					Float ratio = oldRatios.get(id);
                					if (ratio == null || ratio > c.getRoomRatio()) {
	                					oldRatios.put(id, c.getRoomRatio());
                						sLog.info(id + " <- " + c.getRoomRatio());
                					}
                				}
                			}
                		}
                	}
                }
            }
            
            MakeAssignmentsForClassEvents makePattern = new MakeAssignmentsForClassEvents(session, hibSession);
            
            /*
            for (ExactTimeMins x: ExactTimeMinsDAO.getInstance().findAll(hibSession)) {
            	x.setNrSlots(x.getMinsPerMtgMax() / 5);
            	x.setBreakTime(5);
            	hibSession.saveOrUpdate(x);
            }
            */
            
            RoomGroup poc = null, mult = null, bez = null;
            for (RoomGroup rg: RoomGroup.getAllGlobalRoomGroups(session)) {
            	if (rg.getAbbv().equals("POČ"))
            		poc = rg;
            	else if (rg.getAbbv().equals("MULT"))
            		mult = rg;
            	else if (rg.getAbbv().equals("BĚŽ"))
            		bez = rg;
            }

            if (!incremental) {
            	hibSession.createMutationQuery(
            			"delete DistributionPref where owner in (from Department d where d.session.uniqueId = :sessionId)")
            			.setParameter("sessionId", session.getUniqueId()).executeUpdate();
            	hibSession.createMutationQuery(
            			"delete DistributionPref where owner in (from Session s where s.uniqueId = :sessionId)")
            			.setParameter("sessionId", session.getUniqueId()).executeUpdate();
            }
            
            Hashtable<String, Set<Class_>> meetWith = new Hashtable<String, Set<Class_>>();
            
			DistributionType sameDaysType = hibSession.createQuery(
			"select d from DistributionType d where d.reference = :type", DistributionType.class).setParameter("type", "SAME_DAYS").uniqueResult();

            
			TimePattern tp2h = hibSession.createQuery(
					"select p from TimePattern as p where p.session.uniqueId=:sessionId and p.name=:name", TimePattern.class).
					setParameter("sessionId", session.getUniqueId()).
					setParameter("name", "2h").uniqueResult();
			
            for (SchedulingSubpart ss: hibSession.createQuery(
            		"select distinct s from SchedulingSubpart s inner join s.instrOfferingConfig.instructionalOffering.courseOfferings co where " +
            		"co.subjectArea.department.session.uniqueId = :sessionId", SchedulingSubpart.class)
            		.setParameter("sessionId", session.getUniqueId()).list()) {
            	
            	boolean hasPreferences = false;
            	if (!ss.getPreferences().isEmpty()) hasPreferences = true;
            	for (Class_ c: ss.getClasses()) {
            		if (c.effectiveDatePattern().getName().startsWith("import") || c.getNbrRooms() == 0) {
            			hasPreferences = false; break;
            		}
            		if (c.getPreferences().size() > c.getPreferences(TimePref.class).size()) hasPreferences = true;
            		else for (Iterator i = c.getPreferences(TimePref.class).iterator(); !hasPreferences && i.hasNext(); ) {
            			TimePref t = (TimePref)i.next();
            			TimePatternModel m = t.getTimePatternModel();
            			if (!m.isExactTime() && !m.isDefault()) {
            				hasPreferences = true;
            			}
            		}
            	}
            	if (hasPreferences && incremental) {
            		continue;
            	}
        		sLog.info("Setting " + ss.getSchedulingSubpartLabel() + " ...");

        		if (ss.getInstrOfferingConfig().isUnlimitedEnrollment()) {
        			ss.getInstrOfferingConfig().setUnlimitedEnrollment(false);
        			ss.getInstrOfferingConfig().setLimit(0);
            		hibSession.merge(ss);
        		}
        		
        		if (ss.getChildSubparts().isEmpty() && ss.getParentSubpart() != null) {
        			boolean sameDay = false;
        			boolean hasConstraint = false;
        			boolean friday = false;
        			for (Class_ c: ss.getClasses()) {
        				int dayCode = 0;
        				while (c != null) {
        					for (DistributionObject d: c.getDistributionObjects()) {
        						if (d.getDistributionPref().getDistributionType().equals(sameDaysType)) {
        							hasConstraint = true;
        						}
        					}
        					Assignment a = c.getCommittedAssignment();
        					if (a != null) {
        						if ((dayCode & a.getDays()) != 0) { sameDay = true; }
        						dayCode |= a.getDays();
        					}
        					c = c.getParentClass();
        				}
        				friday = (dayCode & Constants.DAY_CODES[Constants.DAY_FRI]) != 0;
        			}
        			if (!hasConstraint && !friday && !sameDay) {
                    	DistributionPref dp = new DistributionPref();
                    	dp.setDistributionType(sameDaysType);
        				dp.setPrefLevel(PreferenceLevel.getPreferenceLevel(sameDay ? PreferenceLevel.sStronglyDiscouraged : PreferenceLevel.sProhibited));
        				dp.setDistributionObjects(new HashSet<DistributionObject>());
        				dp.setStructure(DistributionPref.Structure.AllClasses);
        				dp.setOwner(ss.getManagingDept());
        				SchedulingSubpart x = ss;
        				int index = 1;
        				while (x != null) {
            				DistributionObject o = new DistributionObject();
            				o.setDistributionPref(dp);
            				o.setPrefGroup(x);
            				o.setSequenceNumber(index++);
            				dp.getDistributionObjects().add(o);
        					x = x.getParentSubpart();
        				}
        				hibSession.persist(dp);
        			}
        		}
        		
            	for (Class_ c: ss.getClasses()) {
            		Meeting m = c.getEvent().getMeetings().iterator().next();
            		int minPerMeeting = 5 + 5 * (m.getStopPeriod() - m.getStartPeriod());
            		if ((minPerMeeting + 5) % 50 == 0) minPerMeeting += 5;
            		if ((minPerMeeting - 5) % 50 == 0) minPerMeeting -= 5;
            		if (ss.getMinutesPerWk() != minPerMeeting) {
                		System.out.println(c.getClassLabel(hibSession) + " has " + ss.getMinutesPerWk() + " minutes per meeting (should have " + minPerMeeting + ").");
                		if (c.getSectionNumber() == 1) {
                			ss.setMinutesPerWk(minPerMeeting);
                			hibSession.merge(ss);
                		}
            		}
            	}

            	for (Class_ c: ss.getClasses()) {
            		Assignment a = c.getCommittedAssignment();
            		if (a == null) continue;
            		
            		if (c.effectiveDatePattern().getName().startsWith("import")) {
            			c.setDatePattern(makePattern.getDatePattern(c.getEvent()));
            		}
            		
            		for (Location location: a.getRooms()) {
            			if (!(location instanceof Room)) continue;
                		String code = location.getUniqueId() + ":" + a.getDatePattern().getUniqueId() + ":" + a.getTimePattern().getUniqueId() + ":" + a.getDays() + ":" + a.getStartSlot();
                		Set<Class_> classes = meetWith.get(code);
                		if (classes == null) {
                			classes = new HashSet<Class_>();
                			meetWith.put(code, classes);
                		}
                		classes.add(c);
            		}
            		
            		// Reset room ratio
            		c.setRoomRatio(1f);
            		// Reset preferences
            		c.getPreferences().clear();
            		// Strongly preferred room
            		TimePattern pattern = null; 
            		patterns: for (TimePattern p: TimePattern.findApplicable(session.getUniqueId(), false, false, false, c.getSchedulingSubpart().getMinutesPerWk(), c.effectiveDatePattern(), c.getSchedulingSubpart().getInstrOfferingConfig().getDurationModel(), null)) {
            			for (TimePatternDays d: p.getDays())
            				if (a.getDays().equals(d.getDayCode()))
                				for (TimePatternTime t: p.getTimes()) {
                					if (t.getStartSlot().equals(a.getStartSlot())) {
                						pattern = p;
                						break patterns;
                					}
                				}
            		}
            		boolean extConv = false;
            		if (pattern == null) {
            			// Exact time
            			pattern = TimePattern.findExactTime(session.getUniqueId());
            			TimePatternModel m = pattern.getTimePatternModel();
            	    	m.setExactDays(a.getDays());
            	    	m.setExactStartSlot(a.getStartSlot());
                		TimePref tp = new TimePref();
                		tp.setPrefLevel(PreferenceLevel.getPreferenceLevel(PreferenceLevel.sRequired));
                		tp.setTimePatternModel(m);
                		tp.setOwner(c);
                		c.getPreferences().add(tp);
            		} else {
                		if (pattern.getName().equals("2h ext")) {
                			pattern = tp2h;
                			extConv = true;
                		}
            			TimePatternModel m = pattern.getTimePatternModel();
            			for (int d = 0; d < m.getNrDays(); d++)
            				for (int t = 0; t < m.getNrTimes(); t++) {
            					if (a.getTimeLocation().getStartSlot() == m.getStartSlot(t) &&
            						a.getTimeLocation().getDayCode() == m.getDayCode(d)) {
            						for (int tt = Math.max(0, t - 1); tt < Math.min(m.getNrTimes(), t + 2); tt++)
            							for (int dd = 0; dd < m.getNrDays(); dd++)
                    						m.setPreference(dd, tt, PreferenceLevel.sPreferred);
            						m.setPreference(d, t, PreferenceLevel.sStronglyPreferred);
            						if (d == m.getNrDays() - 1) {
            							for (int dd = 0; dd < m.getNrDays() - 1; dd++)
            	            				for (int tt = 0; tt < m.getNrTimes(); tt++)
            	            					m.setPreference(dd, tt, PreferenceLevel.sProhibited);
            						} else {
        	            				for (int tt = 0; tt < m.getNrTimes(); tt++)
        	            					m.setPreference(m.getNrDays() - 1, tt, PreferenceLevel.sProhibited);
            						}
            					} else if (extConv && t > 0 && a.getTimeLocation().getDayCode() == m.getDayCode(d) &&
            							a.getTimeLocation().getStartSlot() > m.getStartSlot(t-1) &&
            							a.getTimeLocation().getStartSlot() < m.getStartSlot(t)) {
        							for (int dd = 0; dd < m.getNrDays(); dd++) {
                						m.setPreference(dd, t-1, PreferenceLevel.sPreferred);
                						m.setPreference(dd, t, PreferenceLevel.sPreferred);
        							}
            						m.setPreference(d, t-1, PreferenceLevel.sStronglyPreferred);
            						m.setPreference(d, t, PreferenceLevel.sStronglyPreferred);
            					}
            				}
                		TimePref tp = new TimePref();
                		tp.setPrefLevel(PreferenceLevel.getPreferenceLevel(PreferenceLevel.sRequired));
                		tp.setTimePatternModel(m);
                		tp.setOwner(c);
                		c.getPreferences().add(tp);
            		}

            		boolean rgPref = true;
            		for (Location l: a.getRooms()) {
            			if (l instanceof NonUniversityLocation) {
            				RoomPref rp = new RoomPref();
            				rp.setOwner(c);
            				rp.setPrefLevel(PreferenceLevel.getPreferenceLevel(PreferenceLevel.sRequired));
            				rp.setRoom(l);
            				c.getPreferences().add(rp);
            				rgPref = false;
            			} else {
            				RoomPref rp = new RoomPref();
            				rp.setOwner(c);
            				if (l.getRoomGroups().isEmpty()) {
                				rp.setPrefLevel(PreferenceLevel.getPreferenceLevel(PreferenceLevel.sRequired));
                				rgPref = false;
            				} else if (l.getCapacity() == 0) {
                				rp.setPrefLevel(PreferenceLevel.getPreferenceLevel(PreferenceLevel.sStronglyPreferred));
            				} else {            					
                				rp.setPrefLevel(PreferenceLevel.getPreferenceLevel(PreferenceLevel.sPreferred));
            				}
            				rp.setRoom(l);
            				c.getPreferences().add(rp);
            				BuildingPref bp = new BuildingPref();
            				bp.setOwner(c);
            				bp.setPrefLevel(PreferenceLevel.getPreferenceLevel(PreferenceLevel.sStronglyPreferred));
            				bp.setBuilding(((Room)l).getBuilding());
            				c.getPreferences().add(bp);
            				
            				Float lastLikeRoomRatio = null;
            				String id = ident(ss.getControllingCourseOffering(), c, hibSession);
            				if (id != null)
            					lastLikeRoomRatio = oldRatios.get(id);
            				
            				if (lastLikeRoomRatio != null)
            					sLog.info(id + " had room ration " + lastLikeRoomRatio + " last year.");
            				if (l.getCapacity() > 0 && l.getCapacity() < c.getClassLimit()) {
            					c.setRoomRatio((float) Math.floor(100 * l.getCapacity() / c.getClassLimit()) / 100f);
            					if (lastLikeRoomRatio != null && lastLikeRoomRatio < c.getRoomRatio()) {
            						sLog.info("Setting room ratio for " + c.getClassLabel(hibSession) + " <- " + lastLikeRoomRatio + " (was " + c.getRoomRatio() + ")");
            						c.setRoomRatio(lastLikeRoomRatio);
            					}
            				} else if (l.getCapacity() == 0) {
            					c.setRoomRatio(0f);
            				} else {
            					if (lastLikeRoomRatio != null) {
            						sLog.info("Setting room ratio for " + c.getClassLabel(hibSession) + " <- " + lastLikeRoomRatio);
            						c.setRoomRatio(lastLikeRoomRatio);
            					}
            				}
            			}
            			if (!useScheduleNote && rgPref) {
                			for (RoomGroup rg: l.getRoomGroups()) {
                				if (rg.isGlobal() && rg.getAbbv().equals("MULT")) {
                					RoomGroupPref gp = new RoomGroupPref();
                					gp.setOwner(c);
                					gp.setPrefLevel(PreferenceLevel.getPreferenceLevel(PreferenceLevel.sStronglyPreferred));
                					gp.setRoomGroup(rg);
                    				c.getPreferences().add(gp);
                					RoomGroupPref gp2 = new RoomGroupPref();
                					gp2.setOwner(c);
                					gp2.setPrefLevel(PreferenceLevel.getPreferenceLevel(PreferenceLevel.sPreferred));
                					gp2.setRoomGroup(poc);
                    				c.getPreferences().add(gp2);
                				} else if (rg.isGlobal() && rg.getAbbv().equals("POČ")) {
                					RoomGroupPref gp = new RoomGroupPref();
                					gp.setPrefLevel(PreferenceLevel.getPreferenceLevel(PreferenceLevel.sRequired));
                					gp.setOwner(c);
                					gp.setRoomGroup(rg);
                    				c.getPreferences().add(gp);
                				} else if (rg.isGlobal() && rg.getAbbv().equals("BĚŽ")) {
                					RoomGroupPref gp = new RoomGroupPref();
                					gp.setPrefLevel(PreferenceLevel.getPreferenceLevel(PreferenceLevel.sStronglyPreferred));
                					gp.setOwner(c);
                					gp.setRoomGroup(rg);
                    				c.getPreferences().add(gp);
                					RoomGroupPref gp2 = new RoomGroupPref();
                					gp2.setOwner(c);
                					gp2.setPrefLevel(PreferenceLevel.getPreferenceLevel(PreferenceLevel.sStronglyDiscouraged));
                					gp2.setRoomGroup(poc);
                    				c.getPreferences().add(gp2);
                				}
                			}
            			}
            		}
            		
            		// Room preferences
            		if (useScheduleNote && rgPref) {
                		if ("MM".equals(c.getSchedulePrintNote())) {
        					RoomGroupPref gp = new RoomGroupPref();
        					gp.setOwner(c);
        					gp.setPrefLevel(PreferenceLevel.getPreferenceLevel(PreferenceLevel.sStronglyPreferred));
        					gp.setRoomGroup(mult);
            				c.getPreferences().add(gp);
        					RoomGroupPref gp2 = new RoomGroupPref();
        					gp2.setOwner(c);
        					gp2.setPrefLevel(PreferenceLevel.getPreferenceLevel(PreferenceLevel.sPreferred));
        					gp2.setRoomGroup(poc);
            				c.getPreferences().add(gp2);
        					RoomGroupPref gp3 = new RoomGroupPref();
        					gp3.setOwner(c);
        					gp3.setPrefLevel(PreferenceLevel.getPreferenceLevel(PreferenceLevel.sDiscouraged));
        					gp3.setRoomGroup(bez);
            				c.getPreferences().add(gp3);
                		} else if ("PC".equals(c.getSchedulePrintNote())) {
        					RoomGroupPref gp = new RoomGroupPref();
        					gp.setPrefLevel(PreferenceLevel.getPreferenceLevel(PreferenceLevel.sRequired));
        					gp.setOwner(c);
        					gp.setRoomGroup(poc);
            				c.getPreferences().add(gp);
                		} else {
        					RoomGroupPref gp = new RoomGroupPref();
        					gp.setPrefLevel(PreferenceLevel.getPreferenceLevel(PreferenceLevel.sStronglyPreferred));
        					gp.setOwner(c);
        					gp.setRoomGroup(bez);
            				c.getPreferences().add(gp);
        					RoomGroupPref gp2 = new RoomGroupPref();
        					gp2.setOwner(c);
        					gp2.setPrefLevel(PreferenceLevel.getPreferenceLevel(PreferenceLevel.sStronglyDiscouraged));
        					gp2.setRoomGroup(poc);
            				c.getPreferences().add(gp2);
                		}
            		}

            		c.setNbrRooms(Math.max(1, a.getRooms().size()));
            		
            		hibSession.merge(c);
            	}
        		hibSession.flush();
            }
            hibSession.flush();
            
            if (addMeetWith) {
    			DistributionType meetWithType = hibSession.createQuery(
				"select d from DistributionType d where d.reference = :type", DistributionType.class).setParameter("type", "MEET_WITH").uniqueResult();

            for (Set<Class_> classes: meetWith.values()) {
            	if (classes.size() <= 1) continue;
            	sLog.info("Adding meet with between: " + classes);
            	DistributionPref dp = new DistributionPref();
            	dp.setDistributionType(meetWithType);
				dp.setPrefLevel(PreferenceLevel.getPreferenceLevel(PreferenceLevel.sRequired));
				dp.setDistributionObjects(new HashSet<DistributionObject>());
				dp.setStructure(DistributionPref.Structure.AllClasses);
				int index = 1;
				int total = 0;
            	for (Class_ c: classes) {
            		if (index == 1)
            			dp.setOwner(c.getManagingDept());
    				DistributionObject o = new DistributionObject();
    				o.setDistributionPref(dp);
    				o.setPrefGroup(c);
    				o.setSequenceNumber(index++);
    				dp.getDistributionObjects().add(o);
    				total += c.getClassLimit();
            	}
        		for (Location l: classes.iterator().next().getCommittedAssignment().getRooms()) {
        			if (l instanceof NonUniversityLocation) continue;
    				if (l.getCapacity() > 0 && l.getCapacity() < total) {
    					double roomRatio = Math.floor(100.0 * l.getCapacity() / total) / 100.0;
    					for (Class_ c: classes) {
    						double roomLimit = Math.floor(roomRatio * c.getClassLimit());
    						c.setRoomRatio((float) Math.floor(100f * roomLimit / c.getClassLimit()) / 100f);
    						hibSession.merge(c);
    					}
    				}
        		}
				hibSession.persist(dp);
            }
            
            hibSession.flush();
            }
            
            sLog.info("All done.");
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
}
