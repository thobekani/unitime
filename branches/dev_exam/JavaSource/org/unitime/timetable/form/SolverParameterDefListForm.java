/*
 * UniTime 3.0 (University Course Timetabling & Student Sectioning Application)
 * Copyright (C) 2007, UniTime.org, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package org.unitime.timetable.form;

import java.util.ArrayList;
import java.util.Collection;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

/** 
 * @author Tomas Muller
 */
public class SolverParameterDefListForm extends ActionForm {

	// --------------------------------------------------------- Instance Variables

	/** solverParamDefs property */
	private Collection solverParamDefs;

	// --------------------------------------------------------- Methods

	/** 
	 * Method reset
	 * @param mapping
	 * @param request
	 */
	public void reset(ActionMapping mapping, HttpServletRequest request) {
		solverParamDefs = new ArrayList();
	}

	/** 
	 * Returns the solverParamDefs.
	 * @return Collection
	 */
	public Collection getSolverParamDefs() {
		return solverParamDefs;
	}

	/** 
	 * Set the solverParamDefs.
	 * @param solverParamDefs The solverParamDefs to set
	 */
	public void setSolverParamDefs(Collection solverParamDefs) {
		this.solverParamDefs = solverParamDefs;
	}

}
