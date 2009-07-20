/**********************************************************************************
 * @author lovemorenalube
 **********************************************************************************/

package org.sakaiproject.evaluation.tool.producers;

import java.security.InvalidParameterException;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.model.EvalUser;
import org.sakaiproject.evaluation.model.EvalAssignGroup;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.tool.viewparams.EvalViewParameters;

import uk.org.ponder.rsf.components.UIBoundBoolean;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.decorators.DecoratorList;
import uk.org.ponder.rsf.components.decorators.UIIDStrategyDecorator;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

public class EvaluationAssignSelectProducer implements ViewComponentProducer, ViewParamsReporter{
	
	public static final String VIEW_ID = "evaluation_assign_select";

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
		String groupTitle = "", evalGroupId = "", selectType = "";  //Hold values passed via URL. selectType refers to what type of role is being show eg. Instructor or Assistant
		Long evalId;
		EvalViewParameters evalParameters;
		
		UIForm form = UIForm.make(tofill, "form");
		String actionBean = "setupEvalBean.";
		String actionBeanVariable = actionBean;
		//Deal with EvalGroups
		 if(viewparams != null && viewparams instanceof EvalViewParameters){
			evalParameters = (EvalViewParameters) viewparams;
			evalId = evalParameters.evaluationId;
			evalGroupId = evalParameters.evalGroupId;
			groupTitle = commonLogic.getDisplayTitle(evalGroupId);
			selectType = evalParameters.evalCategory;
			Set<String> users;
			if(EvalAssignGroup.SELECTION_TYPE_INSTRUCTOR.equals(selectType)){
				users = commonLogic.getUserIdsForEvalGroup(evalGroupId, EvalConstants.PERM_BE_EVALUATED);
				actionBeanVariable = actionBeanVariable+"deselectedInstructors";
			}else if(EvalAssignGroup.SELECTION_TYPE_ASSISTANT.equals(selectType)){
				users = commonLogic.getUserIdsForEvalGroup(evalGroupId, EvalConstants.PERM_ASSISTANT_ROLE);	
				actionBeanVariable = actionBeanVariable+"deselectedAssistants";
			}else{
				throw new InvalidParameterException("Cannot handle this selection type: "+selectType);
			}
			
			//Get users
			List<EvalUser> evalUsers = commonLogic.getEvalUsersByIds(users.toArray(new String[users.size()]));
			//Sort the users list by displayName
			Collections.sort(evalUsers, new EvalUser.SortNameComparator());
			
			for(EvalUser evalUser : evalUsers){
				UIBranchContainer row = UIBranchContainer.make(form, "item-row:");
				UIBoundBoolean bb = UIBoundBoolean.make(row, "row-select", Boolean.TRUE);
				bb.decorators = new DecoratorList( new UIIDStrategyDecorator(evalUser.userId) );
	            UIOutput.make(row, "row-number", evalUser.username); 
	            UIOutput.make(row, "row-name", evalUser.sortName);
	        }
			
		 /**
         * This is the evaluation we are working with on this page,
         * this should ONLY be read from, do not change any of these fields
         */
        EvalEvaluation evaluation = evaluationService.getEvaluationById(evalId);
        
        //do a check for the Header
		UIMessage.make(tofill, "title", (EvalAssignGroup.SELECTION_TYPE_INSTRUCTOR.equals(selectType) ? "assignselect.instructors.page.title" : "assignselect.tas.page.title"),
				new Object[] {groupTitle, evaluation.getTitle()});
		System.out.println(groupTitle);
		
		if(EvalAssignGroup.SELECTION_TYPE_INSTRUCTOR.equals(selectType)){
			UIMessage.make(form, "instruction", "assignselect.instructions.instructor");
		}else{
			UIMessage.make(form, "instruction", "assignselect.instructions.assistant");
		}
		UIMessage.make(form, "col-number", "assignselect.table.numbers");
		UIMessage.make(form, "col-name", "assignselect.table.names");
		 
		UIMessage.make(form, "save-item-action","assignselect.form.save" );
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
