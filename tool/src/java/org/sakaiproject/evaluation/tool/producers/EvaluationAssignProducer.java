/**
 * EvaluationAssignProducer.java - evaluation - Sep 18, 2006 11:35:56 AM - azeckoski
 * $URL$
 * $Id$
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.evaluation.tool.producers;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.externals.ExternalHierarchyLogic;
import org.sakaiproject.evaluation.logic.model.EvalGroup;
import org.sakaiproject.evaluation.logic.model.EvalHierarchyNode;
import org.sakaiproject.evaluation.model.EvalAssignGroup;
import org.sakaiproject.evaluation.model.EvalAssignUser;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.tool.viewparams.EvalViewParameters;
import org.sakaiproject.evaluation.utils.ComparatorsUtils;

import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIELBinding;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.UIOutputMany;
import uk.org.ponder.rsf.components.UISelect;
import uk.org.ponder.rsf.components.UISelectChoice;
import uk.org.ponder.rsf.components.decorators.UIDecorator;
import uk.org.ponder.rsf.components.decorators.UIDisabledDecorator;
import uk.org.ponder.rsf.components.decorators.UIFreeAttributeDecorator;
import uk.org.ponder.rsf.components.decorators.UILabelTargetDecorator;
import uk.org.ponder.rsf.components.decorators.UIStyleDecorator;
import uk.org.ponder.rsf.components.decorators.UITooltipDecorator;
import uk.org.ponder.rsf.flow.ARIResult;
import uk.org.ponder.rsf.flow.ActionResultInterceptor;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

/**
 * View for assigning an evaluation to groups and hierarchy nodes. 
 * 
 * This Producer has instance variables for tracking state of the view, and
 * should never be reused or used as a singleton.
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 * @author Steve Githens (sgithens@caret.cam.ac.uk)
 */
@SuppressWarnings("deprecation")
public class EvaluationAssignProducer implements ViewComponentProducer, ViewParamsReporter, ActionResultInterceptor {
	
	private static Log log = LogFactory.getLog(EvaluationAssignProducer.class);

    public static final String VIEW_ID = "evaluation_assign";
    public String getViewID() {
        return VIEW_ID;
    }

    private EvalEvaluationService evaluationService;
    public void setEvaluationService(EvalEvaluationService evaluationService) {
        this.evaluationService = evaluationService;
    }

    private ExternalHierarchyLogic hierarchyLogic;
    public void setExternalHierarchyLogic(ExternalHierarchyLogic logic) {
        this.hierarchyLogic = logic;
    }

    private EvalCommonLogic commonLogic;
    public void setCommonLogic(EvalCommonLogic bean) {
        this.commonLogic = bean;
    }
    
    private Locale locale;
    public void setLocale(Locale locale) {
        this.locale = locale;
    }
    
    public MessageLocator messageLocator;
    public void setMessageLocator(MessageLocator messageLocator) {
        this.messageLocator = messageLocator;
    }
   
    public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {

        // local variables used in the render logic
        String currentUserId = commonLogic.getCurrentUserId();
        DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM, locale);
        DateFormat dtf = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, locale);

        // render top links
        renderTopLinks(tofill, currentUserId);

        EvalViewParameters evalViewParams = (EvalViewParameters) viewparams;
        if (evalViewParams.evaluationId == null) {
            throw new IllegalArgumentException("Cannot access this view unless the evaluationId is set");
        }

        /**
         * This is the evaluation we are working with on this page,
         * this should ONLY be read from, do not change any of these fields
         */
        EvalEvaluation evaluation = evaluationService.getEvaluationById(evalViewParams.evaluationId);
        String actionBean = "setupEvalBean.";
        Boolean newEval = false;
        
        UIInternalLink.make(tofill, "eval-settings-link",
                UIMessage.make("evalsettings.page.title"),
                new EvalViewParameters(EvaluationSettingsProducer.VIEW_ID, evalViewParams.evaluationId) );
        if (EvalConstants.EVALUATION_STATE_PARTIAL.equals(evaluation.getState())) {
            // creating a new eval
            UIMessage.make(tofill, "eval-start-text", "starteval.page.title");
            newEval = true;
        }

        UIMessage.make(tofill, "assign-eval-edit-page-title", "assigneval.assign.page.title", new Object[] {evaluation.getTitle()});
        UIMessage.make(tofill, "assign-eval-instructions", "assigneval.assign.instructions", new Object[] {evaluation.getTitle()});
        

        UIMessage.make(tofill, "evalAssignInstructions", "evaluationassignconfirm.eval.assign.instructions",
                new Object[] {df.format(evaluation.getStartDate())});

        // display info about the evaluation (dates and what not)
        UIOutput.make(tofill, "startDate", dtf.format(evaluation.getStartDate()) );

        if (evaluation.getDueDate() != null) {
            UIBranchContainer branch = UIBranchContainer.make(tofill, "showDueDate:");
            UIOutput.make(branch, "dueDate", dtf.format(evaluation.getDueDate()) );
        }

        if (evaluation.getStopDate() != null) {
            UIBranchContainer branch = UIBranchContainer.make(tofill, "showStopDate:");
            UIOutput.make(branch, "stopDate", dtf.format(evaluation.getStopDate()) );
        }

        if (evaluation.getViewDate() != null) {
            UIBranchContainer branch = UIBranchContainer.make(tofill, "showViewDate:");
            UIOutput.make(branch, "viewDate", dtf.format(evaluation.getViewDate()) );
        }
       
        /* 
         * About this form.
         * 
         * This is a GET form that has 2 UISelects, one for Hierarchy Nodes, and
         * one for Eval Groups (which includes adhoc groups).  They are interspered
         * and mixed together. In order to do this easily we pass in empty String
         * arrays for the option values and labels in the UISelects. This is partially
         * because rendering each individual checkbox requires and integer indicating
         * it's position, and this view is too complicated to generate these arrays
         * ahead of time.  So we generate the String Arrays on the fly, using the list.size()-1
         * at each point to get this index.  Then at the very end we update the UISelect's
         * with the appropriate optionlist and optionnames. This actually works 
         * really good and the wizard feels much smoother than it did with the 
         * old session bean.
         * 
         * Also see the comments on HierarchyTreeNodeSelectRenderer. 
         * 
         */
        UIForm form = UIForm.make(tofill, "eval-assign-form");

        // Things for building the UISelect of Eval Group Checkboxes
        List<String> evalGroupsLabels = new ArrayList<String>();
        List<String> evalGroupsValues = new ArrayList<String>();
        UISelect evalGroupsSelect = UISelect.makeMultiple(form, "evalGroupSelectHolder", 
                new String[] {}, new String[] {}, actionBean + "selectedGroupIDs", new String[] {});
        String evalGroupsSelectID = evalGroupsSelect.getFullID();

        /*
         * About the 4 collapsable areas.
         * 
         * What's happening here is that we have 4 areas: hierarchy, groups, 
         * new adhoc groups, and existing adhoc groups that can be hidden and selected
         * which a checkbox for each one.
         * 
         */
       
        // NOTE: this is the one place where the perms should be used instead of user assignments (there are no assignments yet) -AZ
        
        // get the current eval group id (ie: reference site id) that the user is in now
        String currentEvalGroupId = commonLogic.getCurrentEvalGroup();
        // get the current eval group id (ie: site id) that the user is in now
        String currentSiteId = EntityReference.getIdFromRef(currentEvalGroupId);

        // get the groups that this user is allowed to assign evals to
        //TODO: Filter site types. EVALSYS-762
        List<EvalGroup> evalGroups = commonLogic.getFilteredEvalGroupsForUser(commonLogic.getCurrentUserId(), EvalConstants.PERM_ASSIGN_EVALUATION, currentSiteId);  
        
        // for backwards compatibility we will pull the list of groups the user is being evaluated in as well and merge it in
        List<EvalGroup> beEvalGroups = commonLogic.getFilteredEvalGroupsForUser(commonLogic.getCurrentUserId(), EvalConstants.PERM_BE_EVALUATED, currentSiteId);  
        for (EvalGroup evalGroup : beEvalGroups) {
            if (! evalGroups.contains(evalGroup)) {
                evalGroups.add(evalGroup);
            }
        }

        if (evalGroups.size() > 0) {
            Map<String, EvalGroup> groupsMap = new HashMap<String, EvalGroup>();
            for (int i=0; i < evalGroups.size(); i++) {
                EvalGroup c = (EvalGroup) evalGroups.get(i);
                groupsMap.put(c.evalGroupId, c);
            }
            
            /*
             * Area 2. display checkboxes for selecting the non-hierarchy groups
             */
            UIBranchContainer evalgroupArea = UIBranchContainer.make(form, "evalgroups-area:");

            UIOutput.make(evalgroupArea, "evalgroups-assignment-area");
            
            String[] nonAssignedEvalGroupIDs = getEvalGroupIDsNotAssignedInHierarchy(evalGroups).toArray(new String[] {});
            List<EvalGroup> unassignedEvalGroups = new ArrayList<EvalGroup>();
            for (int i = 0; i < nonAssignedEvalGroupIDs.length; i++) {
                unassignedEvalGroups.add(groupsMap.get(nonAssignedEvalGroupIDs[i]));
            }
            // sort the list by title 
            Collections.sort(unassignedEvalGroups, new ComparatorsUtils.GroupComparatorByTitle());
            
            //Move current site to top of this list EVALSYS-762.
            EvalGroup currentGroup = null;
            int count2 = 0, found = 0;
            for ( EvalGroup group : unassignedEvalGroups ){
            	if ( group.evalGroupId.equals(currentEvalGroupId)){
            		currentGroup = group;
            		found = count2; // Save current group's list index so later we can remove it
            	}
            	count2 ++;
            }
            unassignedEvalGroups.remove(found);		
            unassignedEvalGroups.add(0, currentGroup);
            
			
            List<String> assignGroupsIds = new ArrayList<String>();
            String groupSelectionOTP = "assignGroupSelectionSettings.";
            if(! newEval){
            	Map<Long, List<EvalAssignGroup>> selectedGroupsMap = evaluationService.getAssignGroupsForEvals(new Long[] {evalViewParams.evaluationId}, true, null);
            	List<EvalAssignGroup> assignGroups = selectedGroupsMap.get(evalViewParams.evaluationId);
            	for(EvalAssignGroup assGroup: assignGroups){
            		assignGroupsIds.add(assGroup.getEvalGroupId());
            		
            		//Add group selection settings to form to support EVALSYS-778
            		Map<String, String> selectionOptions = assGroup.getSelectionOptions();
                    form.parameters.add(new UIELBinding(groupSelectionOTP + assGroup.getEvalGroupId().replaceAll("/site/", "") + ".instructor", selectionOptions.get(EvalAssignGroup.SELECTION_TYPE_INSTRUCTOR)));
                    form.parameters.add(new UIELBinding(groupSelectionOTP + assGroup.getEvalGroupId().replaceAll("/site/", "") + ".assistant", selectionOptions.get(EvalAssignGroup.SELECTION_TYPE_ASSISTANT)));
            	}
            }
                           
            int count = 0;
            for (EvalGroup evalGroup : unassignedEvalGroups) {
            	if(evalGroup != null){
            		
            	String evalGroupId = evalGroup.evalGroupId;
            	
            	int numEvaluatorsInSite = commonLogic.countUserIdsForEvalGroup(evalGroupId, EvalConstants.PERM_TAKE_EVALUATION);
            	boolean hasEvaluators = numEvaluatorsInSite > 0;
            	            	
                UIBranchContainer checkboxRow = UIBranchContainer.make(evalgroupArea, "groups:", count+"");
                if (count % 2 == 0) {
                    checkboxRow.decorate( new UIStyleDecorator("itemsListOddLine") ); // must match the existing CSS class
                }
                checkboxRow.decorate(new UIFreeAttributeDecorator("rel", count+"")); // table row counter for JS use in EVALSYS-618
                
                //keep deselected user info as a result of changes in EVALSYS-660
                Set<String> deselectedInsructorIds = new HashSet<String>();
                Set<String> deselectedAssistantIds = new HashSet<String>();
                
                
                if (! newEval) {
                //Get saved selection settings for this eval
            	List<EvalAssignUser> deselectedInsructors = evaluationService.getParticipantsForEval(evalViewParams.evaluationId, null, new String[]{evalGroupId}, EvalAssignUser.TYPE_EVALUATEE, EvalAssignUser.STATUS_REMOVED, null, null);
                List<EvalAssignUser> deselectedAssistants = evaluationService.getParticipantsForEval(evalViewParams.evaluationId, null, new String[]{evalGroupId}, EvalAssignUser.TYPE_ASSISTANT, EvalAssignUser.STATUS_REMOVED, null, null);
               
            	//check for already deselected users that match this groupId
                for(EvalAssignUser deselectedUser:deselectedInsructors){
                	deselectedInsructorIds.add(deselectedUser.getUserId());
                }
                for(EvalAssignUser deselectedUser:deselectedAssistants){
                	deselectedAssistantIds.add(deselectedUser.getUserId());
                }
               
                //Assign attribute to row to help JS set checkbox selection to true
                if(assignGroupsIds.contains(evalGroupId)){
                	checkboxRow.decorate(new UIStyleDecorator("selectedGroup"));
                }
                 
                }else{
                	//add blank selection options for this group for use by evalAssign.js
                	form.parameters.add(new UIELBinding(groupSelectionOTP + evalGroupId.replaceAll("/site/", "") + ".instructor", ""));
                    form.parameters.add(new UIELBinding(groupSelectionOTP + evalGroupId.replaceAll("/site/", "") + ".assistant", ""));	
                }
                
                
                evalGroupsLabels.add(evalGroup.title);
                evalGroupsValues.add(evalGroupId);

                String evalUsersLocator = "selectedEvaluationUsersLocator.";
                
                UISelectChoice choice = UISelectChoice.make(checkboxRow, "evalGroupId", evalGroupsSelectID, evalGroupsLabels.size()-1);
                
                if (! hasEvaluators){
                	choice.decorate( new UIDisabledDecorator() );
                }
                form.parameters.add(new UIELBinding(evalUsersLocator + evalGroupId.replaceAll("/site/", "")+".deselectedInstructors", deselectedInsructorIds!=null?deselectedInsructorIds.toArray(new String[deselectedInsructorIds.size()]):new String[]{}));
                form.parameters.add(new UIELBinding(evalUsersLocator + evalGroupId.replaceAll("/site/", "")+".deselectedAssistants", deselectedAssistantIds!=null?deselectedAssistantIds.toArray(new String[deselectedAssistantIds.size()]):new String[]{}));
                
                //add ordering bindings
                form.parameters.add(new UIELBinding(evalUsersLocator + evalGroupId.replaceAll("/site/", "") + ".orderingInstructors", new String[]{} ));
                form.parameters.add(new UIELBinding(evalUsersLocator + evalGroupId.replaceAll("/site/", "") + ".orderingAssistants", new String[]{} ));
                
                // get title from the map since it is faster
	            UIOutput title = UIOutput.make(checkboxRow, "groupTitle", evalGroup.title );
	                
                if( hasEvaluators ){
	                int totalUsers = commonLogic.countUserIdsForEvalGroup(evalGroupId, EvalConstants.PERM_BE_EVALUATED);
	                if(totalUsers > 0){
	                	int currentUsers = deselectedInsructorIds.size() >= 0 ? ( totalUsers-deselectedInsructorIds.size() ) : totalUsers;
	                	UIInternalLink link = UIInternalLink.make(checkboxRow, "select-instructors", UIMessage.make("assignselect.instructors.select", 
	                			new Object[] {currentUsers,totalUsers}), 
	                			new EvalViewParameters(EvaluationAssignSelectProducer.VIEW_ID, evaluation.getId() ,evalGroupId, EvalAssignGroup.SELECTION_TYPE_INSTRUCTOR) );
	                	link.decorate(new UIStyleDecorator("addItem total:"+totalUsers));
	                	link.decorate(new UITooltipDecorator(messageLocator.getMessage("assignselect.instructors.page.title")));
	                }
	                totalUsers = commonLogic.countUserIdsForEvalGroup(evalGroup.evalGroupId, EvalConstants.PERM_ASSISTANT_ROLE);
	                if(totalUsers > 0){
	                	int currentUsers = deselectedAssistantIds.size() >= 0 ? ( totalUsers-deselectedAssistantIds.size() ) : totalUsers;
	                	UIInternalLink link = UIInternalLink.make(checkboxRow, "select-tas", UIMessage.make("assignselect.tas.select", 
	                			new Object[] {currentUsers,totalUsers}) , 
	                			new EvalViewParameters(EvaluationAssignSelectProducer.VIEW_ID, evaluation.getId() ,evalGroup.evalGroupId, EvalAssignGroup.SELECTION_TYPE_ASSISTANT) );
	                    link.decorate(new UIStyleDecorator("addItem total:"+totalUsers));
	                    link.decorate(new UITooltipDecorator(messageLocator.getMessage("assignselect.tas.page.title")));
	                }
                }else{
                	title.decorate( new UIStyleDecorator("instruction") );
                	UIMessage.make(checkboxRow, "select-no", "assigneval.cannot.assign");
                }
                UILabelTargetDecorator.targetLabel(title, choice); // make title a label for checkbox
	                
                
                count++;
            }
            }
        } else {
            // TODO tell user there are no groups to assign to
        }

        evalGroupsSelect.optionlist = UIOutputMany.make(evalGroupsValues.toArray(new String[] {}));
        evalGroupsSelect.optionnames = UIOutputMany.make(evalGroupsLabels.toArray(new String[] {}));
        
        form.parameters.add( new UIELBinding(actionBean + "evaluationId", evalViewParams.evaluationId) );
        
        UIMessage.make(form, "back-button", "general.back.button");
        UICommand.make(form, "confirmAssignCourses", UIMessage.make("evaluationassignconfirm.done.button"),actionBean + "completeConfirmAction" );
        
    }

    /**
     * I think this is getting all the groupIds that are not currently assigned to nodes in the hierarchy
     * 
     * @param evalGroups the list of eval groups to check in
     * @return the set of evalGroupsIds from the input list of evalGroups which are not assigned to hierarchy nodes
     */
    protected Set<String> getEvalGroupIDsNotAssignedInHierarchy(List<EvalGroup> evalGroups) {
        // TODO - we probably need a method to simply get all assigned groupIds in the hierarchy to make this a bit faster

        // 1. All the Evaluation Group IDs in a set
        Set<String> evalGroupIDs = new HashSet<String>();
        for (EvalGroup evalGroup: evalGroups) {
            evalGroupIDs.add(evalGroup.evalGroupId);
        }

        // 2. All the Evaluation Group IDs that are assigned to Hierarchy Nodes
        EvalHierarchyNode rootNode = hierarchyLogic.getRootLevelNode();
        String[] rootNodeChildren = rootNode.childNodeIds.toArray(new String[] {});
        if (rootNodeChildren.length > 0) {
            Map<String,Set<String>> assignedGroups = hierarchyLogic.getEvalGroupsForNodes(rootNodeChildren);

            Set<String> hierAssignedGroupIDs = new HashSet<String>();
            for (String key: assignedGroups.keySet()) {
                hierAssignedGroupIDs.addAll(assignedGroups.get(key));
            }
            // 3. Remove all EvalGroup IDs that have been assigned to 
            evalGroupIDs.removeAll(hierAssignedGroupIDs);
        }


        return evalGroupIDs;
    }


    /* (non-Javadoc)
     * @see uk.org.ponder.rsf.flow.ActionResultInterceptor#interceptActionResult(uk.org.ponder.rsf.flow.ARIResult, uk.org.ponder.rsf.viewstate.ViewParameters, java.lang.Object)
     */
    public void interceptActionResult(ARIResult result, ViewParameters incoming, Object actionReturn) {
        // handles the navigation cases and passing along data from view to view
        EvalViewParameters evp = (EvalViewParameters) incoming;
        Long evalId = evp.evaluationId;
        if ("evalSettings".equals(actionReturn)) {
            result.resultingView = new EvalViewParameters(EvaluationSettingsProducer.VIEW_ID, evalId);
        } else if ("evalAssign".equals(actionReturn)) {
            result.resultingView = new EvalViewParameters(EvaluationAssignProducer.VIEW_ID, evalId);
        } else if ("evalConfirm".equals(actionReturn)) {
            result.resultingView = new EvalViewParameters(EvaluationAssignConfirmProducer.VIEW_ID, evalId);
        } else if ("controlEvals".equals(actionReturn)) {
            result.resultingView = new SimpleViewParameters(ControlEvaluationsProducer.VIEW_ID);
        }
    }

    /* (non-Javadoc)
     * @see uk.org.ponder.rsf.viewstate.ViewParamsReporter#getViewParameters()
     */
    public ViewParameters getViewParameters() {
        return new EvalViewParameters();
    }

    /**
     * Renders the usual action breadcrumbs at the top.
     * 
     * @param tofill
     * @param currentUserId
     */
    private void renderTopLinks(UIContainer tofill, String currentUserId) {
        boolean userAdmin = commonLogic.isUserAdmin(currentUserId);
        boolean beginEvaluation = evaluationService.canBeginEvaluation(currentUserId);

        /*
         * top links here
         */
        UIInternalLink.make(tofill, "summary-link", 
                UIMessage.make("summary.page.title"), 
                new SimpleViewParameters(SummaryProducer.VIEW_ID));

        if (userAdmin) {
            UIInternalLink.make(tofill, "administrate-link", 
                    UIMessage.make("administrate.page.title"),
                    new SimpleViewParameters(AdministrateProducer.VIEW_ID));
            UIInternalLink.make(tofill, "control-scales-link",
                    UIMessage.make("controlscales.page.title"),
                    new SimpleViewParameters(ControlScalesProducer.VIEW_ID));
        }

        if (beginEvaluation) {
            UIInternalLink.make(tofill, "control-evaluations-link",
                    UIMessage.make("controlevaluations.page.title"),
                    new SimpleViewParameters(ControlEvaluationsProducer.VIEW_ID));
        } else {
            throw new SecurityException("User attempted to access " + 
                    VIEW_ID + " when they are not allowed");
        }
    }

}
