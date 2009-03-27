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

import java.security.InvalidParameterException;
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
import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.externals.ExternalHierarchyLogic;
import org.sakaiproject.evaluation.logic.model.EvalGroup;
import org.sakaiproject.evaluation.logic.model.EvalHierarchyNode;
import org.sakaiproject.evaluation.model.EvalAdhocGroup;
import org.sakaiproject.evaluation.model.EvalAssignGroup;
import org.sakaiproject.evaluation.model.EvalAssignUser;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.tool.SetupEvalBean;
import org.sakaiproject.evaluation.tool.renderers.HierarchyTreeNodeSelectRenderer;
import org.sakaiproject.evaluation.tool.viewparams.AdhocGroupParams;
import org.sakaiproject.evaluation.tool.viewparams.EvalViewParameters;
import org.sakaiproject.evaluation.utils.ComparatorsUtils;
import org.sakaiproject.evaluation.utils.EvalUtils;

import uk.org.ponder.htmlutil.HTMLUtil;
import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIELBinding;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UILink;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.UIOutputMany;
import uk.org.ponder.rsf.components.UISelect;
import uk.org.ponder.rsf.components.UISelectChoice;
import uk.org.ponder.rsf.components.UIVerbatim;
import uk.org.ponder.rsf.components.decorators.UIFreeAttributeDecorator;
import uk.org.ponder.rsf.components.decorators.UILabelTargetDecorator;
import uk.org.ponder.rsf.components.decorators.UIStyleDecorator;
import uk.org.ponder.rsf.components.decorators.UITooltipDecorator;
import uk.org.ponder.rsf.flow.ARIResult;
import uk.org.ponder.rsf.flow.ActionResultInterceptor;
import uk.org.ponder.rsf.renderer.message.MessageUtil;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;
import uk.org.ponder.rsf.viewstate.ViewStateHandler;

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

    private EvalSettings settings;
    public void setSettings(EvalSettings settings) {
        this.settings = settings;
    }

    private HierarchyTreeNodeSelectRenderer hierUtil;
    public void setHierarchyRenderUtil(HierarchyTreeNodeSelectRenderer util) {
        hierUtil = util;
    }

    private ExternalHierarchyLogic hierarchyLogic;
    public void setExternalHierarchyLogic(ExternalHierarchyLogic logic) {
        this.hierarchyLogic = logic;
    }

    private ViewStateHandler vsh;
    public void setViewStateHandler(ViewStateHandler vsh) {
        this.vsh = vsh;
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
   

    /**
     * Instance Variables for building up rendering information.
     */
    private StringBuilder initJS = new StringBuilder();

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
        
        UIInternalLink.make(tofill, "eval-settings-link",
                UIMessage.make("evalsettings.page.title"),
                new EvalViewParameters(EvaluationSettingsProducer.VIEW_ID, evalViewParams.evaluationId) );
        if (EvalConstants.EVALUATION_STATE_PARTIAL.equals(evaluation.getState())) {
            // creating a new eval
            UIMessage.make(tofill, "eval-start-text", "starteval.page.title");
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
         * which a checkbox for each one.  I'm not using the UIInitBlock at the moment
         * because it doesn't seem to take arrays for the javascript arguments (and keep 
         * them as javascript arrays).  So we are just putting the javascript initialization
         * here and running it at the bottom of the page.
         * 
         */
        Boolean showHierarchy = (Boolean) settings.get(EvalSettings.DISPLAY_HIERARCHY_OPTIONS);

        // NOTE: this is the one place where the perms should be used instead of user assignments (there are no assignments yet) -AZ

        // get the groups that this user is allowed to assign evals to
        List<EvalGroup> evalGroups = commonLogic.getEvalGroupsForUser(commonLogic.getCurrentUserId(), EvalConstants.PERM_ASSIGN_EVALUATION);

        // for backwards compatibility we will pull the list of groups the user is being evaluated in as well and merge it in
        List<EvalGroup> beEvalGroups = commonLogic.getEvalGroupsForUser(commonLogic.getCurrentUserId(), EvalConstants.PERM_BE_EVALUATED);
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
			
            
            
			                
            int count = 0;
            for (EvalGroup evalGroup : unassignedEvalGroups) {
                UIBranchContainer checkboxRow = UIBranchContainer.make(evalgroupArea, "groups:", count+"");
                if (count % 2 == 0) {
                    checkboxRow.decorate( new UIStyleDecorator("itemsListOddLine") ); // must match the existing CSS class
                }
                checkboxRow.decorate(new UIFreeAttributeDecorator("rel", count+"")); // table row counter for JS use in EVALSYS-618
                
                //keep deselected user info as a result of changes in EVALSYS-660
                Set<String> deselectedInsructorIds = new HashSet<String>();
                Set<String> deselectedAssistantIds = new HashSet<String>();
                if (! EvalConstants.EVALUATION_STATE_PARTIAL.equals(evaluation.getState())) {
                //Get saved selection settings for this eval
            	List<EvalAssignUser> deselectedUsers = evaluationService.getParticipantsForEval(evalViewParams.evaluationId, null, new String[]{evalGroup.evalGroupId}, null, EvalAssignUser.STATUS_REMOVED, null, null);
                //check for already deselected users that match this groupId
                for(EvalAssignUser deselectedUser:deselectedUsers){
                	if(evalGroup.evalGroupId.equals(deselectedUser.getEvalGroupId())){
                		if(EvalAssignUser.TYPE_EVALUATEE.equals(deselectedUser.getType())){
                			deselectedInsructorIds.add(deselectedUser.getUserId());
                		}else if(EvalAssignUser.TYPE_ASSISTANT.equals(deselectedUser.getType())){
                			deselectedAssistantIds.add(deselectedUser.getUserId());
                		}
                		
                	}
                	}
                }
                evalGroupsLabels.add(evalGroup.title);
                evalGroupsValues.add(evalGroup.evalGroupId);

                UISelectChoice choice = UISelectChoice.make(checkboxRow, "evalGroupId", evalGroupsSelectID, evalGroupsLabels.size()-1);
                form.parameters.add(new UIELBinding("selectedEvaluationUsersLocator."+evalGroup.evalGroupId.replaceAll("/site/", "")+".deselectedInstructors", deselectedInsructorIds!=null?deselectedInsructorIds.toArray(new String[deselectedInsructorIds.size()]):new String[]{}));
                form.parameters.add(new UIELBinding("selectedEvaluationUsersLocator."+evalGroup.evalGroupId.replaceAll("/site/", "")+".deselectedAssistants",deselectedAssistantIds!=null?deselectedAssistantIds.toArray(new String[deselectedAssistantIds.size()]):new String[]{}));
                


                // get title from the map since it is faster
                UIOutput title = UIOutput.make(checkboxRow, "groupTitle", evalGroup.title );
                UILabelTargetDecorator.targetLabel(title, choice); // make title a label for checkbox
                int totalUsers = commonLogic.countUserIdsForEvalGroup(evalGroup.evalGroupId, (EvalConstants.PERM_INSTRUCTOR_ROLE));
                if(totalUsers > 0){
                	int currentUsers = deselectedInsructorIds.size()>=0?(totalUsers-deselectedInsructorIds.size()):totalUsers;
                	UIInternalLink link = UIInternalLink.make(checkboxRow, "select-instructors", UIMessage.make("assignselect.instructors.select", 
                			new Object[] {currentUsers,totalUsers}), 
                			new EvalViewParameters(EvaluationAssignSelectProducer.VIEW_ID, evaluation.getId() ,evalGroup.evalGroupId, EvalAssignGroup.SELECTION_TYPE_INSTRUCTOR) );
                	link.decorate(new UIStyleDecorator("addItem total:"+totalUsers));
                	link.decorate(new UITooltipDecorator(messageLocator.getMessage("assignselect.instructors.page.title")));
                }
                totalUsers = commonLogic.countUserIdsForEvalGroup(evalGroup.evalGroupId, (EvalConstants.PERM_ASSISTANT_ROLE));
                if(totalUsers > 0){
                	int currentUsers = deselectedAssistantIds.size()>=0?(totalUsers-deselectedAssistantIds.size()):totalUsers;
                	UIInternalLink link = UIInternalLink.make(checkboxRow, "select-tas", UIMessage.make("assignselect.tas.select", 
                			new Object[] {currentUsers,totalUsers}) , 
                			new EvalViewParameters(EvaluationAssignSelectProducer.VIEW_ID, evaluation.getId() ,evalGroup.evalGroupId, EvalAssignGroup.SELECTION_TYPE_ASSISTANT) );
                    link.decorate(new UIStyleDecorator("addItem total:"+totalUsers));
                    link.decorate(new UITooltipDecorator(messageLocator.getMessage("assignselect.tas.page.title")));
                }
                
                
                count++;
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