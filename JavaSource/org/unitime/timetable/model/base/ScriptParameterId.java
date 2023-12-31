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

import java.io.Serializable;

import org.unitime.timetable.model.Script;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public class ScriptParameterId implements Serializable {
	private static final long serialVersionUID = 1L;

	private Script iScript;
	private String iName;

	public ScriptParameterId() {}

	public ScriptParameterId(Script script, String name) {
		iScript = script;
		iName = name;
	}

	public Script getScript() { return iScript; }
	public void setScript(Script script) { iScript = script; }

	public String getName() { return iName; }
	public void setName(String name) { iName = name; }


	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof ScriptParameterId)) return false;
		ScriptParameterId scriptParameter = (ScriptParameterId)o;
		if (getScript() == null || scriptParameter.getScript() == null || !getScript().equals(scriptParameter.getScript())) return false;
		if (getName() == null || scriptParameter.getName() == null || !getName().equals(scriptParameter.getName())) return false;
		return true;
	}

	@Override
	public int hashCode() {
		if (getScript() == null || getName() == null) return super.hashCode();
		return getScript().hashCode() ^ getName().hashCode();
	}

}
