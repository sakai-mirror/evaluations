/******************************************************************************
 * ExternalEvalGroups.java - created by aaronz@vt.edu
 * 
 * Copyright (c) 2007 Virginia Polytechnic Institute and State University
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 * Contributors:
 * Aaron Zeckoski (aaronz@vt.edu) - primary
 * 
 *****************************************************************************/

package org.sakaiproject.evaluation.logic.externals;

import java.util.List;
import java.util.Set;

import org.sakaiproject.evaluation.logic.model.EvalGroup;

/**
 * This interface provides methods to get EvalGroups (user collections) information
 * into the evaluation system and permissions related to those EvalGroups
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public interface ExternalEvalGroups {

	// EvalGroups

	/**
	 * @return the current group context for the current session
	 * (represents the current group location of the user in the system)
	 */
	public String getCurrentEvalGroup();

	/**
	 * Construct an {@link EvalGroup} object based on the unique string id
	 * 
	 * @param evalGroupId the internal unique ID for an evalGroup
	 * @return an {@link EvalGroup} object or null if not found
	 */
	public EvalGroup makeEvalGroupObject(String evalGroupId);

	/**
	 * Get the title associated with an evalGroup
	 * 
	 * @param evalGroupId the internal unique ID for an evalGroup
	 * @return the displayable title or warning text if it cannot be found
	 */
	public String getDisplayTitle(String evalGroupId);


	// ENROLLMENTS

	/**
	 * Get a list of all user ids that have a specific permission in a context
	 * 
	 * @param evalGroupId the internal unique ID for an evalGroup
	 * @param permission a permission string constant
	 * @return a Set of Strings which represent the user Ids of all users in the site with that permission
	 */
	public Set getUserIdsForEvalGroup(String evalGroupId, String permission);

	/**
	 * Get a count of all user ids that have a specific permission in a context
	 * 
	 * @param evalGroupId the internal unique ID for an evalGroup
	 * @param permission a permission string constant
	 * @return a count of the users
	 */
	public int countUserIdsForEvalGroup(String evalGroupId, String permission);

	/**
	 * Get a list of all eval groups that a user has a specific permission in
	 * (use {@link #countEvalGroupsForUser(String, String)} if you just need the number)
	 * 
	 * @param userId the internal user id (not username)
	 * @param permission a permission string constant
	 * @return a List of {@link EvalGroup} objects
	 */
	public List getEvalGroupsForUser(String userId, String permission);

	/**
	 * Get a count of all contexts that a user has a specific permission in
	 * 
	 * @param userId the internal user id (not username)
	 * @param permission a permission string constant
	 * @return the count of the eval groups that the user has a permission in
	 */
	public int countEvalGroupsForUser(String userId, String permission);

}