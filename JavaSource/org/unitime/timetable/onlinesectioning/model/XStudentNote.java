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
package org.unitime.timetable.onlinesectioning.model;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.Date;

import org.unitime.timetable.model.StudentNote;

/**
 * @author Tomas Muller
 */
public class XStudentNote implements Serializable, Comparable<XStudentNote>, Externalizable {
	private static final long serialVersionUID = 1L;
	private Long iId;
	private String iNote;
	private String iUserId;
	private Date iTimeStamp;
	
	public XStudentNote(StudentNote note) {
		iId = note.getUniqueId();
		iNote = note.getTextNote();
		iUserId = note.getUserId();
		iTimeStamp = note.getTimeStamp();
	}
	
	public XStudentNote(ObjectInput in) throws IOException, ClassNotFoundException {
    	readExternal(in);
    }
	
	public Long getNoteId() { return iId; }
	public String getNote() { return iNote; }
	public boolean hasNote() { return iNote != null && !iNote.isEmpty(); }
	public String getUserId() { return iUserId; }
	public Date getTimeStamp() { return iTimeStamp; }
	
    @Override
    public int hashCode() {
        return (int) (getNoteId() ^ (getNoteId() >>> 32));
    }

	@Override
	public int compareTo(XStudentNote note) {
		int cmp = getTimeStamp().compareTo(note.getTimeStamp());
		if (cmp != 0) return cmp;
		return getNoteId().compareTo(note.getNoteId());
	}
	
	@Override
    public String toString() {
        return getNote();
    }

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		long id = in.readLong();
		iId = (id < 0 ? null : Long.valueOf(id));
		iNote = (String)in.readObject();
		iUserId = (String)in.readObject();
		long ts = in.readLong();
		iTimeStamp = (ts == 0l ? null : new Date(ts));
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeLong(iId == null ? -1l : iId);
		out.writeObject(iNote);
		out.writeObject(iUserId);
		out.writeLong(iTimeStamp == null ? 0l : iTimeStamp.getTime());
	}
}
