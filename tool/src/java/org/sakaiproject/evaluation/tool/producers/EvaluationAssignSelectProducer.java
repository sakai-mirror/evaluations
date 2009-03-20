/**********************************************************************************
 *
 * Copyright (c) 2005, 2006 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.evaluation.tool.producers;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.model.EvalUser;
import org.sakaiproject.evaluation.model.EvalAdhocGroup;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.tool.viewparams.AdhocGroupParams;
import org.sakaiproject.evaluation.tool.viewparams.EvalViewParameters;
import org.sakaiproject.evaluation.tool.viewparams.ItemViewParameters;

import uk.org.ponder.rsf.components.ParameterList;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIELBinding;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.UIParameter;
import uk.org.ponder.rsf.components.UISelect;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

public class EvaluationAssignSelectProducer implements ViewComponentProducer, ViewParamsReporter{

	public static final String VIEW_ID = "evaluation_assign_select";
	private static Log log = LogFactory.getLog(EvaluationAssignSelectProducer.class);

	public String getViewID() {
		return VIEW_ID;
	}
	private EvalCommonLogic commonLogic;
    public void setCommonLogic(EvalCommonLogic bean) {
        this.commonLogic = bean;
    }
    private EvalEvaluationService evaluationService;
    public void setEvaluationService(EvalEvaluationService evaluationService) {
        this.evaluationService = evaluationService;
    }

	public void fillComponents(UIContainer tofill, ViewParameters viewparams,
			ComponentChecker checker) {
		// TODO Auto-generated method stub		
		String groupTitle = "", evalGroupId = "", currentUserId = commonLogic.getCurrentUserId(), selectType = "";
		Long groupId, evalId;
		AdhocGroupParams adhocGroupParams;
		EvalViewParameters evalParameters;
		
		UIForm form = UIForm.make(tofill, "form");
		
		//Deal with AdhocGroups  ******Detect type of AdhocGroupParams here and  deal with that type else check if it's EvalViewParameters type and deal with it. Is this needed??
		/*if(viewparams != null && viewparams instanceof AdhocGroupParams){
			adhocGroupParams = (AdhocGroupParams) viewparams;
			groupId = adhocGroupParams.adhocGroupId;
			selectType = adhocGroupParams.selectType;
			evalId = adhocGroupParams.evalId;
			EvalAdhocGroup evalAdhocGroup = commonLogic.getAdhocGroupById(groupId);
			List<EvalAdhocGroup> myAdhocGroups = commonLogic.getAdhocGroupsForOwner(currentUserId);
			if(myAdhocGroups.size() > 0){
				for (EvalAdhocGroup adhocGroup: myAdhocGroups) {
					if(adhocGroup.getId().equals(groupId)){
						groupTitle = adhocGroup.getTitle();
					}
				}
				if(groupTitle == ""){
					throw new IllegalArgumentException("Cannot find group with id: "+ groupId +" in this users profile.");
				}
			}else{
				throw new IllegalArgumentException("This user: "+currentUserId+" does not have any groups.");
			}
			log.info("Got and adhocGroup with ID: " + adhocGroupParams.adhocGroupId + " for type: " + adhocGroupParams.selectType);
			
			String[] participants = evalAdhocGroup.getParticipantIds();
	         if (participants.length > 0) {
	            List<EvalUser> evalUsers = commonLogic.getEvalUsersByIds(participants);
	            for (EvalUser evalUser : evalUsers) {
	               UIBranchContainer row = UIBranchContainer.make(form, "item-row:");
	               UISelect.make(row, "row-select", new String[] {}, new String[] {}, "#{}");
	               if (EvalConstants.USER_TYPE_INTERNAL.equals(evalUser.type)) {
	                  UIMessage.make(row, "row-number", "modifyadhocgroup.adhocuser.label");
	               } else {
	                  UIOutput.make(row, "row-number", evalUser.username);
	               }
	               UIOutput.make(row, "row-name", evalUser.displayName);
	               }
	         }
		}else */
		//Deal with EvalGroups
		 if(viewparams != null && viewparams instanceof EvalViewParameters){
			evalParameters = (EvalViewParameters) viewparams;
			evalId = evalParameters.evaluationId;
			evalGroupId = evalParameters.evalGroupId;
			groupTitle = commonLogic.getDisplayTitle(evalGroupId);
			selectType = evalParameters.evalCategory;
			
			Set<String[]> users = commonLogic.getUsersByRole(evalGroupId, ("instructor".equals(selectType)? EvalConstants.PERM_INSTRUCTOR_ROLE : EvalConstants.PERM_ASSISTANT_ROLE));
			Iterator<String[]> i = users.iterator();
			while(i.hasNext()){
				String[] r = i.next();
				EvalUser user = commonLogic.getEvalUserById(r[0]);
				UIBranchContainer row = UIBranchContainer.make(form, "item-row:");
	            UISelect.make(row, "row-select", new String[] {}, new String[] {}, "#{}");
	            UIOutput.make(row, "row-number", user.username);
	            UIOutput.make(row, "row-name", user.displayName); 
	            }
		

		 /**
         * This is the evaluation we are working with on this page,
         * this should ONLY be read from, do not change any of these fields
         */
        EvalEvaluation evaluation = evaluationService.getEvaluationById(evalId);
        
        
		
		//do a check for the Header
		UIMessage.make(tofill, "title", ("instructor".equals(selectType)? "assignselect.instructors.page.title" : "assignselect.tas.page.title"));
		
		
		UIMessage.make(form, "group-title", "assignselect.page.group");
		UIOutput.make(form, "group-name", groupTitle);
		UIMessage.make(form, "eval-title", "assignselect.page.evaluation");
		UIOutput.make(form, "eval-name", evaluation.getTitle());
		UIMessage.make(form, "instruction", "assignselect.instructions");
		UIMessage.make(form, "col-number", "assignselect.table.numbers");
		UIMessage.make(form, "col-name", "assignselect.table.names");
		
		
		 
		
		form.parameters = new ParameterList();
		form.parameters.add(new UIELBinding("templateBBean.saveAssignSelection.evalGroupId", evalGroupId));
		form.parameters.add(new UIELBinding("templateBBean.saveAssignSelection.selectType", selectType));
         
		UICommand.make(form, "save-item-action", UIMessage.make("assignselect.form.save"), "#{templateBBean.saveAssignSelection}");
		UIMessage.make(form, "cancel-button", "general.cancel.button");
		
		 }else{
			throw new IllegalArgumentException("Params passed are not proper.");
		}
	}

	public ViewParameters getViewParameters() {
		// TODO Auto-generated method stub
		return new EvalViewParameters();
	}

}
