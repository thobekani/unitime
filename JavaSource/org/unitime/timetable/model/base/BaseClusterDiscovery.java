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
package org.unitime.timetable.model.base;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.MappedSuperclass;

import java.io.Serializable;
import java.util.Date;

import org.unitime.timetable.model.ClusterDiscovery;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
@IdClass(ClusterDiscoveryId.class)
public abstract class BaseClusterDiscovery implements Serializable {
	private static final long serialVersionUID = 1L;

	private String iOwnAddress;
	private String iClusterName;
	private byte[] iPingData;
	private Date iTimeStamp;


	public BaseClusterDiscovery() {
	}


	@Id
	@Column(name="own_address", length = 200)
	public String getOwnAddress() { return iOwnAddress; }
	public void setOwnAddress(String ownAddress) { iOwnAddress = ownAddress; }

	@Id
	@Column(name="cluster_name", length = 200)
	public String getClusterName() { return iClusterName; }
	public void setClusterName(String clusterName) { iClusterName = clusterName; }

	@Column(name = "ping_data", nullable = true, length = 5000)
	public byte[] getPingData() { return iPingData; }
	public void setPingData(byte[] pingData) { iPingData = pingData; }

	@Column(name = "time_stamp", nullable = false)
	public Date getTimeStamp() { return iTimeStamp; }
	public void setTimeStamp(Date timeStamp) { iTimeStamp = timeStamp; }

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof ClusterDiscovery)) return false;
		ClusterDiscovery clusterDiscovery = (ClusterDiscovery)o;
		if (getOwnAddress() == null || clusterDiscovery.getOwnAddress() == null || !getOwnAddress().equals(clusterDiscovery.getOwnAddress())) return false;
		if (getClusterName() == null || clusterDiscovery.getClusterName() == null || !getClusterName().equals(clusterDiscovery.getClusterName())) return false;
		return true;
	}

	@Override
	public int hashCode() {
		if (getOwnAddress() == null || getClusterName() == null) return super.hashCode();
		return getOwnAddress().hashCode() ^ getClusterName().hashCode();
	}

	public String toString() {
		return "ClusterDiscovery[" + getOwnAddress() + ", " + getClusterName() + "]";
	}

	public String toDebugString() {
		return "ClusterDiscovery[" +
			"\n	ClusterName: " + getClusterName() +
			"\n	OwnAddress: " + getOwnAddress() +
			"\n	PingData: " + getPingData() +
			"\n	TimeStamp: " + getTimeStamp() +
			"]";
	}
}
