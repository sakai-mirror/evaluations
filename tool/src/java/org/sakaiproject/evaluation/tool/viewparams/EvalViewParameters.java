/******************************************************************************
 * EvalViewParameters.java - created by aaronz on 31 May 2007
 * 
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *****************************************************************************/

package org.sakaiproject.evaluation.tool.viewparams;

import java.util.HashMap;
import java.util.Map;

import uk.org.ponder.rsf.viewstate.SimpleViewParameters;

/**
 * Allows for passing of information needed for previewing templates or evaluations,
 * also for taking evaluations page, also used for controlling evaluations
 * (rewrite of original)
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class EvalViewParameters extends SimpleViewParameters {

   public Long evaluationId;
   public Long templateId; 
   public String evalGroupId;
   public Long responseId;
   public String evalCategory;

   /**
    * if this is true then all the other values are ignored and this is assigned to the
    * default "no group" assignGroup, if false or null then it gets ignored
    */
   public Boolean noGroups;
   /**
    * Holds the selected group ids to assign to this evaluation
    */
   //public Map<String, Boolean> selectedGroupIDsMap = new HashMap<String, Boolean>();
   public String[] selectedGroupIDs = new String[] {};
   
   /**
    * Holds the selected node ids to assign to this evaluation
    */
   //public Map<String, Boolean> selectedHierarchyNodeIDsMap = new HashMap<String, Boolean>();
   public String[] selectedHierarchyNodeIDs = new String[] {};


   public EvalViewParameters() { }

   public EvalViewParameters(String viewID, Long evaluationId) {
      this.viewID = viewID;
      this.evaluationId = evaluationId;
   }

   public EvalViewParameters(String viewID, Long evaluationId, String evalGroupId) {
      this.viewID = viewID;
      this.evaluationId = evaluationId;
      this.evalGroupId = evalGroupId;
   }

   public EvalViewParameters(String viewID, Long evaluationId, Long templateId) {
      this.viewID = viewID;
      this.evaluationId = evaluationId;	
      this.templateId = templateId;
   }

   public EvalViewParameters(String viewID, Long evaluationId, Long templateId, String evalGroupId) {
      this.viewID = viewID;
      this.evaluationId = evaluationId;	
      this.templateId = templateId;
      this.evalGroupId = evalGroupId;
   }

   public EvalViewParameters(String viewID, Long evaluationId, String evalGroupId, String evalCategory) {
      this.viewID = viewID;
      this.evaluationId = evaluationId;
      this.evalGroupId = evalGroupId;
      this.evalCategory = evalCategory;
   }

   public EvalViewParameters(String viewID, Long evaluationId, Long responseId, String evalGroupId, String evalCategory) {
      this.viewID = viewID;
      this.evaluationId = evaluationId;
      this.evalGroupId = evalGroupId;
      this.responseId = responseId;
      this.evalCategory = evalCategory;
   }

}
