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
package org.unitime.timetable.dataexchange;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.dom4j.Element;
import org.unitime.timetable.model.SavedHQL;
import org.unitime.timetable.model.SavedHQLParameter;

/**
 * @author Tomas Muller
 */
public class HQLImport extends BaseImport {

	@Override
	public void loadXml(Element root) throws Exception {
		try {
			beginTransaction();
			if (root.getName().equalsIgnoreCase("report")) {
				importReport(root);
			} else if (root.getName().equalsIgnoreCase("reports")) {
				for (Iterator i = root.elementIterator("report"); i.hasNext();)
					importReport((Element) i.next());
			} else {
				throw new Exception("Given XML file is not a HQL report file.");
			}
			commitTransaction();
		} catch (Exception e) {
			fatal("Exception: " + e.getMessage(), e);
			rollbackTransaction();
			throw e;
		}
	}

	protected void importReport(Element reportEl) {
		String name = reportEl.attributeValue("name");
		if (name == null) {
			error("Attribute name is not provided.");
			return;
		}
		SavedHQL report =  getHibSession().createQuery("from SavedHQL where name = :name", SavedHQL.class)
				.setParameter("name", name).setMaxResults(1).uniqueResult();
		if (report == null) {
			report = new SavedHQL();
			report.setName(name);
			report.setParameters(new HashSet<SavedHQLParameter>());
		}
		report.setType(0);
		Element flags = reportEl.element("flags");
		for (Iterator i = (flags == null ? reportEl : flags).elementIterator("flag"); i.hasNext();) {
			Element e = (Element) i.next();
			SavedHQL.Flag flag = SavedHQL.Flag.valueOf(e.getTextTrim());
			if (flag != null)
				report.set(flag);
		}
		Element queryEl = reportEl.element("query");
		if (queryEl != null)
			report.setQuery(queryEl.getText());
		else
			report.setQuery(null);
		Element descriptionEl = reportEl.element("description");
		if (descriptionEl != null)
			report.setDescription(descriptionEl.getText());
		else
			report.setDescription(null);
		
		Map<String, SavedHQLParameter> params = new HashMap<String, SavedHQLParameter>();
		for (SavedHQLParameter parameter: report.getParameters())
			params.put(parameter.getName(), parameter);
		
		for (Iterator i = reportEl.elementIterator("parameter"); i.hasNext(); ) {
			Element paramEl = (Element) i.next();
			String pName = paramEl.attributeValue("name");
			if (pName == null) continue;
			SavedHQLParameter parameter = params.remove(pName);
			if (parameter == null) {
				parameter = new SavedHQLParameter();
				parameter.setName(pName);
				parameter.setSavedHQL(report);
				report.getParameters().add(parameter);
			}
			parameter.setLabel(paramEl.attributeValue("label"));
			parameter.setType(paramEl.attributeValue("type"));
			parameter.setDefaultValue(paramEl.attributeValue("default"));
		}
		
		for (SavedHQLParameter parameter: params.values()) {
			getHibSession().remove(parameter);
			report.getParameters().remove(parameter);
		}
		
		if (report.getUniqueId() == null)
        	getHibSession().persist(report);
        else
        	getHibSession().merge(report);
	}

}
