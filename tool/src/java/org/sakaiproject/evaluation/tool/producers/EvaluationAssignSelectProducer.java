/**********************************************************************************
 * @author lovemorenalube
 **********************************************************************************/

package org.sakaiproject.evaluation.tool.producers;

import java.security.InvalidParameterException;
import java.util.Iterator;
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
		// TODO Auto-generated method stub		
		String groupTitle = "", evalGroupId = "", selectType = "";
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
				users = commonLogic.getUserIdsForEvalGroup(evalGroupId, EvalConstants.PERM_INSTRUCTOR_ROLE);
				actionBeanVariable = actionBeanVariable+"deselectedInstructors";
			}else if(EvalAssignGroup.SELECTION_TYPE_ASSISTANT.equals(selectType)){
				users = commonLogic.getUserIdsForEvalGroup(evalGroupId, EvalConstants.PERM_ASSISTANT_ROLE);	
				actionBeanVariable = actionBeanVariable+"deselectedAssistants";
			}else{
				throw new InvalidParameterException("Cannot handle this selection type: "+selectType);
			}
			for(Iterator<String> selector = users.iterator(); selector.hasNext();){
				String userId = selector.next();
				EvalUser user = commonLogic.getEvalUserById(userId);
				UIBranchContainer row = UIBranchContainer.make(form, "item-row:");
				UIBoundBoolean bb = UIBoundBoolean.make(row, "row-select", Boolean.TRUE);
				bb.decorators = new DecoratorList( new UIIDStrategyDecorator(user.userId) );
	            UIOutput.make(row, "row-number", user.username); 
	            UIOutput.make(row, "row-name", user.displayName); 
	            }
			
		 /**
         * This is the evaluation we are working with on this page,
         * this should ONLY be read from, do not change any of these fields
         */
        EvalEvaluation evaluation = evaluationService.getEvaluationById(evalId);
        
        
		
		//do a check for the Header
		UIMessage.make(tofill, "title", (EvalAssignGroup.SELECTION_TYPE_INSTRUCTOR.equals(selectType)? "assignselect.instructors.page.title" : "assignselect.tas.page.title"));
		
		
		UIMessage.make(form, "group-title", "assignselect.page.group");
		UIOutput.make(form, "group-name", groupTitle);
		UIMessage.make(form, "eval-title", "assignselect.page.evaluation");
		UIOutput.make(form, "eval-name", evaluation.getTitle());
		UIMessage.make(form, "instruction", "assignselect.instructions");
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
