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
package org.unitime.commons.hibernate.stats;

import org.hibernate.SessionFactory;
import org.hibernate.stat.CacheRegionStatistics;
import org.hibernate.stat.CollectionStatistics;
import org.hibernate.stat.EntityStatistics;
import org.hibernate.stat.QueryStatistics;
import org.hibernate.stat.Statistics;
import org.unitime.commons.Debug;
import org.unitime.commons.web.htmlgen.Table;
import org.unitime.commons.web.htmlgen.TableCell;
import org.unitime.commons.web.htmlgen.TableHeaderCell;
import org.unitime.commons.web.htmlgen.TableRow;
import org.unitime.timetable.model.dao._RootDAO;


/**
 * Displays hibernate statistics in html format
 * @author Heston Fernandes
 */
public class StatsProvider {
    
    public static String getStatsHtml(boolean summaryOnly) {
        return new StatsProvider().getStatsHtml(new _RootDAO().getSession().getSessionFactory(), summaryOnly);
    }

    /**
     * Format statistics in HTML
     * @param sessionFactory Hibernate Session Factory
     * @param summaryOnly true - Display only summary info
     * @return HTML String
     */
    public String getStatsHtml(
            SessionFactory sessionFactory, 
            boolean summaryOnly ) {
        
        StringBuffer hibStats = new StringBuffer();
        
        
        try {
            // Get statistics
            Statistics stats = sessionFactory.getStatistics();

            // Checks statistics enabled
            if(!stats.isStatisticsEnabled()) {
                return "<font color='red'><b>Hibernate statistics is not enabled.</b></font>";
            }
            
            
            // Row Color for even numbered rows
            String evenRowColor = "#FAFAFA";
            
            // Generate HTML table
            Table table = new Table();
            table.setWidth("100%");
            table.setStyleClass("unitime-Table");

            // Links
            StringBuffer links = new StringBuffer("");
            
            links.append("<A class=\"l7\" href=\"#Entity\">Entity</A>");
            if(!summaryOnly)
                links.append(" - <A class=\"l7\" href=\"#EntityDetail\">Detail</A>");
            
            links.append(" | <A class=\"l7\" href=\"#Collection\">Collection</A>");
            if(!summaryOnly)
                links.append(" - <A class=\"l7\" href=\"#CollectionDetail\">Detail</A>");
            
            links.append(" | <A class=\"l7\" href=\"#SecondLevelCache\">Second Level Cache</A>");
            if(!summaryOnly)
                links.append(" - <A class=\"l7\" href=\"#SecondLevelCacheDetail\">Detail</A>");

            links.append(" | <A class=\"l7\" href=\"#Query\">Query</A>");
            if(!summaryOnly)
                links.append(" - <A class=\"l7\" href=\"#QueryDetail\">Detail</A>");
            
        	TableRow row = new TableRow();
            row.addContent(cell(links.toString(), 1, 2, true, "center", "middle"));
        	table.addContent(row);
            
        	// Link to top
        	TableRow linkToTop = new TableRow();
        	linkToTop.addContent(cell("<A class=\"l7\" href=\"#BackToTop\">Back to Top</A>", 1, 2, true, "right", "middle"));
        	
        	
        	hibStats.append(table.toHtml());
        	table = new Table();
            table.setStyleClass("unitime-Table");
            
        	
            // ---------------------- Overall Stats ------------------------
        	row = new TableRow();
        	TableHeaderCell xc = headerCell("<A id=\"BackToTop\"></A>Metric", 1, 2);
            xc.setStyleClass("WelcomeRowHead");
            row.addContent(xc);
            xc.setStyle("min-width:400px;");
        	table.addContent(row);

        	row = new TableRow();
            row.addContent(cell(" &nbsp; Start Time", 1, 1, true));
            row.addContent(cell(stats.getStart().toString(), 1, 1, false));
            table.addContent(row);

            row = new TableRow();
            row.addContent(cell(" &nbsp; Connect Count", 1, 1, true));
            row.addContent(cell(stats.getConnectCount()+"", 1, 1, false));
            table.addContent(row);

            row = new TableRow();
            row.addContent(cell(" &nbsp; Flush Count", 1, 1, true));
            row.addContent(cell(stats.getFlushCount()+"", 1, 1, false));
            table.addContent(row);

            row = new TableRow();
            row.addContent(cell(" &nbsp; Session Open Count", 1, 1, true));
            row.addContent(cell(stats.getSessionOpenCount()+"", 1, 1, false));
            table.addContent(row);

            row = new TableRow();
            row.addContent(cell(" &nbsp; Session Close Count", 1, 1, true));
            row.addContent(cell(stats.getSessionCloseCount()+"", 1, 1, false));
            table.addContent(row);

            row = new TableRow();
            row.addContent(cell(" &nbsp; Transaction Count", 1, 1, true));
            row.addContent(cell(stats.getTransactionCount()+"", 1, 1, false));
            table.addContent(row);

            row = new TableRow();
            row.addContent(cell(" &nbsp; Successful Transaction Count", 1, 1, true));
            row.addContent(cell(stats.getSuccessfulTransactionCount()+"", 1, 1, false));
            table.addContent(row);

            row = new TableRow();
            row.addContent(cell(" &nbsp; Prepare Statement Count", 1, 1, true));
            row.addContent(cell(stats.getPrepareStatementCount()+"", 1, 1, false));
            table.addContent(row);

            row = new TableRow();
            row.addContent(cell(" &nbsp; Close Statement Count", 1, 1, true));
            row.addContent(cell(stats.getCloseStatementCount()+"", 1, 1, false));
            table.addContent(row);

            row = new TableRow();
            row.addContent(cell(" &nbsp; Optimistic Failure Count", 1, 1, true));
            row.addContent(cell(stats.getOptimisticFailureCount()+"", 1, 1, false));
            table.addContent(row);

            row = new TableRow();
            row.addContent(cell("&nbsp;", 1, 2, false));
            table.addContent(row);
            
            hibStats.append(table.toHtml());
        	table = new Table();
            table.setStyleClass("unitime-Table");

 
            // ---------------------- Entity Stats ------------------------
            row = new TableRow();
            xc = headerCell("<A id=\"Entity\"></A>Entity", 1, 2);
            xc.setStyleClass("WelcomeRowHead");
            xc.setStyle("min-width:400px;");
            row.addContent(xc);
            table.addContent(row);

            row = new TableRow();
            row.addContent(cell(" &nbsp; Fetch Count", 1, 1, true));
            row.addContent(cell(stats.getEntityFetchCount()+"", 1, 1, false));
            table.addContent(row);

            row = new TableRow();
            row.addContent(cell(" &nbsp; Load Count", 1, 1, true));
            row.addContent(cell(stats.getEntityLoadCount()+"", 1, 1, false));
            table.addContent(row);

            row = new TableRow();
            row.addContent(cell(" &nbsp; Insert Count", 1, 1, true));
            row.addContent(cell(stats.getEntityInsertCount()+"", 1, 1, false));
            table.addContent(row);

            row = new TableRow();
            row.addContent(cell(" &nbsp; Update Count", 1, 1, true));
            row.addContent(cell(stats.getEntityUpdateCount()+"", 1, 1, false));
            table.addContent(row);

            row = new TableRow();
            row.addContent(cell(" &nbsp; Delete Count", 1, 1, true));
            row.addContent(cell(stats.getEntityDeleteCount()+"", 1, 1, false));
            table.addContent(row);

        	table.addContent(linkToTop);

        	row = new TableRow();
            row.addContent(cell("&nbsp;", 1, 2, false));
            table.addContent(row);
        	
        	hibStats.append(table.toHtml());
        	table = new Table();
            table.setStyleClass("unitime-Table");
            

            // ---------------------- Detailed Entity Stats ------------------------
            
            if(!summaryOnly) {
	            row = new TableRow();
	            TableHeaderCell hc = headerCell("<A id=\"EntityDetail\"></A>Entity Statistics Detail", 1, 2);
	            hc.setStyleClass("WelcomeRowHead");
	            row.addContent(hc);
	            table.addContent(row);
	
	            String[] cEntityNames = stats.getEntityNames();
	            
	            if(cEntityNames==null || cEntityNames.length==0) {
	                row = new TableRow();
	                row.addContent(cell("No entity names found", 1, 2, false));
	                table.addContent(row);
	            }
	            else {
	                Table subTable = new Table();
	                subTable.setStyleClass("unitime-Table");
	
	            	row = new TableRow();
	            	row.addContent(headerCell(" &nbsp; ", 1, 1));
	            	row.addContent(headerCell(" Fetches ", 1, 1));
	            	row.addContent(headerCell(" Loads ", 1, 1));
	            	row.addContent(headerCell(" Inserts ", 1, 1));
	            	row.addContent(headerCell(" Updates ", 1, 1));
	            	row.addContent(headerCell(" Deletes ", 1, 1));
	            	subTable.addContent(row);
	                
	                for (int i=0; i<cEntityNames.length; i++) {
	                    String entityName = cEntityNames[i];
	                    EntityStatistics eStats = stats.getEntityStatistics(entityName);
	                    
	                    row = new TableRow();
	                    if(i%2==0)
	                        row.setBgColor(evenRowColor);
	                    row.addContent(cell(entityName + " &nbsp;", 1, 1, true));
	                    row.addContent(cell(eStats.getFetchCount()+"", 1, 1, false));
	                    row.addContent(cell(eStats.getLoadCount()+"", 1, 1, false));
	                    row.addContent(cell(eStats.getInsertCount()+"", 1, 1, false));
	                    row.addContent(cell(eStats.getUpdateCount()+"", 1, 1, false));
	                    row.addContent(cell(eStats.getDeleteCount()+"", 1, 1, false));
	                    subTable.addContent(row);
	                }
	                
	                row = new TableRow();
	                row.addContent(cell(subTable.toHtml(), 1, 2, true));
	                table.addContent(row);
	            }
	
	        	table.addContent(linkToTop);

	        	row = new TableRow();
	            row.addContent(cell("&nbsp;", 1, 2, false));
	            table.addContent(row);

	        	hibStats.append(table.toHtml());
	        	table = new Table();
	            table.setStyleClass("unitime-Table");
            }
           
            
            // ---------------------- Collection Stats ------------------------
            row = new TableRow();
            xc = headerCell("<A id=\"Collection\"></A>Collection", 1, 2);
            xc.setStyleClass("WelcomeRowHead");
            xc.setStyle("min-width:400px;");
            row.addContent(xc);
            table.addContent(row);

            row = new TableRow();
            row.addContent(cell(" &nbsp; Fetch Count", 1, 1, true));
            row.addContent(cell(stats.getCollectionFetchCount()+"", 1, 1, false));
            table.addContent(row);

            row = new TableRow();
            row.addContent(cell(" &nbsp; Load Count", 1, 1, true));
            row.addContent(cell(stats.getCollectionLoadCount()+"", 1, 1, false));
            table.addContent(row);

            row = new TableRow();
            row.addContent(cell(" &nbsp; Update Count", 1, 1, true));
            row.addContent(cell(stats.getCollectionUpdateCount()+"", 1, 1, false));
            table.addContent(row);

            row = new TableRow();
            row.addContent(cell(" &nbsp; Remove Count", 1, 1, true));
            row.addContent(cell(stats.getCollectionRemoveCount()+"", 1, 1, false));
            table.addContent(row);

            row = new TableRow();
            row.addContent(cell(" &nbsp; Recreate Count", 1, 1, true));
            row.addContent(cell(stats.getCollectionRecreateCount()+"", 1, 1, false));
            table.addContent(row);

        	table.addContent(linkToTop);

        	row = new TableRow();
            row.addContent(cell("&nbsp;", 1, 2, false));
            table.addContent(row);

        	hibStats.append(table.toHtml());
        	table = new Table();
            table.setStyleClass("unitime-Table");


            // ---------------------- Detailed Collection Stats ------------------------
            if(!summaryOnly) {

	            row = new TableRow();
	            TableHeaderCell hc = headerCell("<A id=\"CollectionDetail\"></A>Collection Statistics Detail", 1, 2);
	            hc.setStyleClass("WelcomeRowHead");
	            row.addContent(hc);
	            table.addContent(row);
	
	            String[] cRoleNames = stats.getCollectionRoleNames();
	            
	            if(cRoleNames==null || cRoleNames.length==0) {
	                row = new TableRow();
	                row.addContent(cell("No collection roles found", 1, 2, false));
	                table.addContent(row);
	            }
	            else {
	                Table subTable = new Table();
	                subTable.setStyleClass("unitime-Table");
	
	            	row = new TableRow();
	            	row.addContent(headerCell(" &nbsp; ", 1, 1));
	            	row.addContent(headerCell(" Fetches ", 1, 1));
	            	row.addContent(headerCell(" Loads ", 1, 1));
	            	row.addContent(headerCell(" Updates ", 1, 1));
	            	row.addContent(headerCell(" Removes ", 1, 1));
	            	row.addContent(headerCell(" Recreates ", 1, 1));
	            	subTable.addContent(row);
	
	                for (int i=0; i<cRoleNames.length; i++) {
	                    String roleName = cRoleNames[i];
	                    CollectionStatistics cStats = stats.getCollectionStatistics(roleName);
	                    
	                    row = new TableRow();
	                    if(i%2==0)
	                        row.setBgColor(evenRowColor);
	                    row.addContent(cell(roleName + " &nbsp;", 1, 1, true));
	                    row.addContent(cell(cStats.getFetchCount()+"", 1, 1, false));
	                    row.addContent(cell(cStats.getLoadCount()+"", 1, 1, false));
	                    row.addContent(cell(cStats.getUpdateCount()+"", 1, 1, false));
	                    row.addContent(cell(cStats.getRemoveCount()+"", 1, 1, false));
	                    row.addContent(cell(cStats.getRecreateCount()+"", 1, 1, false));
	                    subTable.addContent(row);
	                }
	
	                row = new TableRow();
	                row.addContent(cell(subTable.toHtml(), 1, 2, true));
	                table.addContent(row);
	            }
	
	            row = new TableRow();
	            row.addContent(cell("<hr>", 1, 2, false));
	            table.addContent(row);

	        	table.addContent(linkToTop);
	        	hibStats.append(table.toHtml());
	        	table = new Table();
	            table.setStyleClass("unitime-Table");
            }
            
            
            // ---------------------- Second Level Cache Stats ------------------------
            row = new TableRow();
            xc = headerCell("<A id=\"SecondLevelCache\"></A>Second Level Cache", 1, 2);
            xc.setStyleClass("WelcomeRowHead");
            xc.setStyle("min-width:400px;");
            row.addContent(xc);
            table.addContent(row);

            row = new TableRow();
            row.addContent(cell(" &nbsp; Hit Count", 1, 1, true));
            row.addContent(cell(stats.getSecondLevelCacheHitCount()+"", 1, 1, false));
            table.addContent(row);

            row = new TableRow();
            row.addContent(cell(" &nbsp; Miss Count", 1, 1, true));
            row.addContent(cell(stats.getSecondLevelCacheMissCount()+"", 1, 1, false));
            table.addContent(row);

            row = new TableRow();
            row.addContent(cell(" &nbsp; Put Count", 1, 1, true));
            row.addContent(cell(stats.getSecondLevelCachePutCount()+"", 1, 1, false));
            table.addContent(row);

        	table.addContent(linkToTop);

        	row = new TableRow();
            row.addContent(cell("&nbsp;", 1, 2, false));
            table.addContent(row);

        	hibStats.append(table.toHtml());
        	table = new Table();
            table.setStyleClass("unitime-Table");

            
            // ---------------------- Detailed Second Level Cache Stats ------------------------
            if(!summaryOnly) {

	            row = new TableRow();
	            TableHeaderCell hc = headerCell("<A id=\"SecondLevelCacheDetail\"></A>Second Level Cache Statistics Detail", 1, 2);
	            hc.setStyleClass("WelcomeRowHead");
	            row.addContent(hc);
	            table.addContent(row);
	
	            String[] cRegionNames = stats.getSecondLevelCacheRegionNames();
	            
	            if(cRegionNames==null || cRegionNames.length==0) {
	                row = new TableRow();
	                row.addContent(cell("No region names found", 1, 2, false));
	                table.addContent(row);
	            }
	            else {
	                Table subTable = new Table();
	                subTable.setStyleClass("unitime-Table");
	
	            	row = new TableRow();
	            	row.addContent(headerCell(" &nbsp; ", 1, 1));
	            	row.addContent(headerCell(" Entities ", 1, 1));
	            	row.addContent(headerCell(" Hits ", 1, 1));
	            	row.addContent(headerCell(" Misses ", 1, 1));
	            	row.addContent(headerCell(" Puts ", 1, 1));
	            	
	            	for (int i = 1; i < row.getContents().size(); i++)
	            		((TableHeaderCell)row.getContents().get(i)).setAlign("right");
	            	subTable.addContent(row);
	            	
	            	long elementsInMem = 0, putCnt = 0, missCnt = 0, hitCnt = 0;
	
	                for (int i=0; i<cRegionNames.length; i++) {
	                    String cRegionName = cRegionNames[i];
	                    CacheRegionStatistics sStats = null;
	                    try {
	                    	sStats = stats.getDomainDataRegionStatistics(cRegionName);
	                    } catch (IllegalArgumentException e) {
	                    	Debug.warning(e.getMessage());
	                    	continue;
	                    }
	                    
	                    row = new TableRow();
	                    if(i%2==0)
	                        row.setBgColor(evenRowColor);
	                    row.addContent(cell(cRegionName + " &nbsp;", 1, 1, true));
	                    row.addContent(cell(sStats.getElementCountInMemory()+"", 1, 1, false));
	                    row.addContent(cell(sStats.getHitCount()+"", 1, 1, false));
	                    row.addContent(cell(sStats.getMissCount()+"", 1, 1, false));
	                    row.addContent(cell(sStats.getPutCount()+"", 1, 1, false));
	                    elementsInMem += sStats.getElementCountInMemory();
	                    putCnt += sStats.getPutCount();
	                    missCnt += sStats.getMissCount();
	                    hitCnt += sStats.getHitCount();
	                    subTable.addContent(row);
	                }
	                
	            	row = new TableRow();
	            	row.addContent(headerCell("Total &nbsp;", 1, 1));
	            	row.addContent(headerCell(""+elementsInMem, 1, 1));
	            	row.addContent(headerCell(""+hitCnt, 1, 1));
	            	row.addContent(headerCell(""+missCnt, 1, 1));
	            	row.addContent(headerCell(""+putCnt, 1, 1));
	            	for (Object x: row.getContents())
	            		((TableHeaderCell)x).setStyleClass(null);
	            	subTable.addContent(row);
	                
	
	                row = new TableRow();
	                row.addContent(cell(subTable.toHtml(), 1, 2, true));
	                table.addContent(row);
	            }
	
	        	table.addContent(linkToTop);

	        	row = new TableRow();
	            row.addContent(cell("&nbsp;", 1, 2, false));
	            table.addContent(row);

	        	hibStats.append(table.toHtml());
	        	table = new Table();
	            table.setStyleClass("unitime-Table");

            }

            
            // ---------------------- Query Stats ------------------------
           row = new TableRow();
            xc = headerCell("<A id=\"Query\"></A>Query", 1, 2);
            xc.setStyleClass("WelcomeRowHead");
            xc.setStyle("min-width:400px;");
            row.addContent(xc);
            table.addContent(row);

            row = new TableRow();
            row.addContent(cell(" &nbsp; Execution Count", 1, 1, true));
            row.addContent(cell(stats.getQueryExecutionCount()+"", 1, 1, false));
            table.addContent(row);

            row = new TableRow();
            row.addContent(cell(" &nbsp; Execution Max Time", 1, 1, true));
            row.addContent(cell(stats.getQueryExecutionMaxTime()+" ms", 1, 1, false));
            table.addContent(row);

            row = new TableRow();
            row.addContent(cell(" &nbsp; Cache Hit Count", 1, 1, true));
            row.addContent(cell(stats.getQueryCacheHitCount()+" ms", 1, 1, false));
            table.addContent(row);

            row = new TableRow();
            row.addContent(cell(" &nbsp; Cache Miss Count", 1, 1, true));
            row.addContent(cell(stats.getQueryCacheMissCount()+" ms", 1, 1, false));
            table.addContent(row);

            row = new TableRow();
            row.addContent(cell(" &nbsp; Cache Put Count", 1, 1, true));
            row.addContent(cell(stats.getQueryCachePutCount()+"", 1, 1, false));
            table.addContent(row);

        	table.addContent(linkToTop);

        	row = new TableRow();
            row.addContent(cell("&nbsp;", 1, 2, false));
            table.addContent(row);

        	hibStats.append(table.toHtml());
        	table = new Table();
            table.setStyleClass("unitime-Table");

            // ---------------------- Detailed Query Stats ------------------------
            if(!summaryOnly) {
            	row = new TableRow();
            	TableHeaderCell hc = headerCell("<A id='QueryDetail'></a>Query Statistics Detail", 1, 2);
            	hc.setStyleClass("WelcomeRowHead");
	           	row.addContent(hc);
	           	table.addContent(row);
	
	           	String[] cQueryStrings = stats.getQueries();
	            
	            if(cQueryStrings==null || cQueryStrings.length==0) {
	                row = new TableRow();
	                row.addContent(cell("No query strings found", 1, 2, false));
	                table.addContent(row);
	            }
	            else {
	                Table subTable = new Table();
	                subTable.setStyleClass("unitime-Table");
	
	            	row = new TableRow();
	            	row.addContent(headerCell(" &nbsp; ", 1, 1));
	            	row.addContent(headerCell(" Execs ", 1, 1));
	            	row.addContent(headerCell(" Rows ", 1, 1));
	            	row.addContent(headerCell(" Max Time ", 1, 1));
	            	row.addContent(headerCell(" Min Time ", 1, 1));
	            	row.addContent(headerCell(" Avg Time ", 1, 1));
	            	row.addContent(headerCell(" Cache Hits ", 1, 1));
	            	row.addContent(headerCell(" Cache Misses ", 1, 1));
	            	row.addContent(headerCell(" Cache Puts ", 1, 1));
	            	subTable.addContent(row);
	
	            	for (int i=0; i<cQueryStrings.length; i++) {
	                    String cQueryString = cQueryStrings[i];
	                    QueryStatistics qStats = stats.getQueryStatistics(cQueryString);
	                    
	                    row = new TableRow();
	                    if(i%2==0)
	                        row.setBgColor(evenRowColor);
	                    TableCell c = cell(cQueryString + " &nbsp;", 1, 1, false);
	                    c.setStyle("white-space:normal;");
	                    row.addContent(c);
	                    row.addContent(cell(qStats.getExecutionCount()+"", 1, 1, false));
	                    row.addContent(cell(qStats.getExecutionRowCount()+"", 1, 1, false));
	                    row.addContent(cell(qStats.getExecutionMaxTime()+" ms", 1, 1, false));
	                    if (qStats.getExecutionMinTime() > qStats.getExecutionMaxTime()) {
	                    	row.addContent(cell("", 1, 1, false));
	                    } else {
	                    	row.addContent(cell(qStats.getExecutionMinTime()+" ms", 1, 1, false));
	                    }
	                    row.addContent(cell(qStats.getExecutionAvgTime()+" ms", 1, 1, false));
	                    row.addContent(cell(qStats.getCacheHitCount()+"", 1, 1, false));
	                    row.addContent(cell(qStats.getCacheMissCount()+"", 1, 1, false));
	                    row.addContent(cell(qStats.getCachePutCount()+"", 1, 1, false));
	                    subTable.addContent(row);
	                }
	            	
	                row = new TableRow();
	                row.addContent(cell(subTable.toHtml(), 1, 2, true));
	                table.addContent(row);
	            }
	
	        	table.addContent(linkToTop);

	        	row = new TableRow();
	            row.addContent(cell("&nbsp;", 1, 2, false));
	            table.addContent(row);

	            hibStats.append(table.toHtml());
            }
        }
        catch (Exception e) {
            hibStats.append("Exception occured: " + e.getMessage());
            e.printStackTrace();
        }
        
        return hibStats.toString();
    }
    
    /**
     * Generate header cell
     * @param content Content of cell
     * @param rowSpan Row Span
     * @param colSpan Column Span
     * @return TableHeaderCell Object
     */
    private TableHeaderCell headerCell(String content, int rowSpan, int colSpan){
    	TableHeaderCell cell = new TableHeaderCell();
    	cell.setStyleClass("unitime-TableHeader");
    	cell.setRowSpan(rowSpan);
    	cell.setColSpan(colSpan);
    	cell.setNoWrap(true);
    	cell.setAlign(content.matches(" ?[0-9]+ ?[a-z]*") ? "right" : "left");
    	cell.setValign("top");
    	cell.addContent(content);
    	if ("Value".equals(content)) {
    		cell.setStyle("min-width: 100px;");
    		cell.setAlign("right");
    	}
    	return(cell);
     }
        
    /**
     * Generate table cell (align=left and valign=top)
     * @param content Content of cell
     * @param rowSpan Row Span
     * @param colSpan Column Span
     * @param noWrap noWrap attribute (true / false)
     * @return TableCell Object
     */
    private TableCell cell(String content, int rowSpan, int colSpan, boolean noWrap){
    	TableCell cell = cell(content, rowSpan, colSpan, noWrap,
    			content.matches(" ?[0-9]+ ?[a-z]*") ? "right" : "left", "top");
    	return(cell);
    }
    
    /**
     * Generate table cell
     * @param content Content of cell
     * @param rowSpan Row Span
     * @param colSpan Column Span
     * @param noWrap noWrap attribute (true / false)
     * @param align left / right / center
     * @param valign top / bottom / middle
     * @return TableCell Object
     */
    private TableCell cell(String content, int rowSpan, int colSpan, boolean noWrap, String align, String valign){
    	TableCell cell = new TableCell();
    	cell.setRowSpan(rowSpan);
    	cell.setColSpan(colSpan);
    	cell.setNoWrap(noWrap);
    	cell.setAlign(align);
    	cell.setValign(valign);
    	cell.addContent(content);
    	return(cell);
    }
    
}
