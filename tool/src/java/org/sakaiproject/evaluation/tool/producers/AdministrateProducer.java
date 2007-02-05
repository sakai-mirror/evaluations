/******************************************************************************
 * AdministrateProducer.java - created by aaronz@vt.edu
 * 
 * Copyright (c) 2007 Virginia Polytechnic Institute and State University
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 * Contributors:
 * Aaron Zeckoski (aaronz@vt.edu)
 * Kapil Ahuja (kahuja@vt.edu)
 * Antranig Basman (antranig@caret.cam.ac.uk)
 *****************************************************************************/

package org.sakaiproject.evaluation.tool.producers;

import java.util.ArrayList;
import java.util.List;

import org.sakaiproject.evaluation.logic.EvalExternalLogic;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.model.constant.EvalConstants;
import org.sakaiproject.evaluation.tool.EvaluationConstant;

import uk.org.ponder.beanutil.PathUtil;
import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.rsf.components.ELReference;
import uk.org.ponder.rsf.components.UIBoundBoolean;
import uk.org.ponder.rsf.components.UIBoundList;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.UISelect;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;

/**
 * Handles administration of the evaluation system
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 * @author Kapil Ahuja (kahuja@vt.edu)
 * @author Antranig Basman (antranig@caret.cam.ac.uk)
 */
public class AdministrateProducer implements ViewComponentProducer, NavigationCaseReporter {

	/**
	 * This is used for navigation within the system.
	 */
	public static final String VIEW_ID = "administrate"; //$NON-NLS-1$

	/**
	 * Used to return the view id of this producer. The view id is used for 
	 * navigation within the system.
	 * @return view id of this producer
	 */
	public String getViewID() {
		return VIEW_ID;
	}

	// Spring injection 
	private EvalExternalLogic external;
	public void setExternal(EvalExternalLogic external) {
		this.external = external;
	}

	// Spring injection 
	private MessageLocator messageLocator;
	public void setMessageLocator(MessageLocator messageLocator) {
		this.messageLocator = messageLocator;
	}
	
	// Used to prepare the path for WritableBeanLocator
    private String ADMIN_WBL = "settingsBean";

	/* 
	 * (non-Javadoc)
	 * @see uk.org.ponder.rsf.view.ComponentProducer#fillComponents(uk.org.ponder.rsf.components.UIContainer, 
	 * 																uk.org.ponder.rsf.viewstate.ViewParameters, 
	 * 																uk.org.ponder.rsf.view.ComponentChecker)
	 */
	public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {
		String currentUserId = external.getCurrentUserId();
		boolean userAdmin = external.isUserAdmin(currentUserId);

		if (! userAdmin) {
			// Security check and denial
			throw new SecurityException("Non-admin users may not access this page");
		}

		/*
		 * top links here
		 */
		UIOutput.make(tofill, "administrate-title", messageLocator.getMessage("administrate.page.title") ); //$NON-NLS-1$ //$NON-NLS-2$
		if (userAdmin) {
			UIInternalLink.make(tofill, "control-panel-toplink", messageLocator.getMessage("controlpanel.page.title"), //$NON-NLS-1$ //$NON-NLS-2$
					new SimpleViewParameters(ControlPanelProducer.VIEW_ID));
		}
		UIInternalLink.make(tofill, "summary-toplink", messageLocator.getMessage("summary.page.title"), new SimpleViewParameters(SummaryProducer.VIEW_ID)); //$NON-NLS-1$ //$NON-NLS-2$
		UIInternalLink.make(tofill, "control-scales-toplink", messageLocator.getMessage("administrate.top.control.scales"),  //$NON-NLS-1$ //$NON-NLS-2$
				new SimpleViewParameters(ScaleControlProducer.VIEW_ID));			
	
		//System Settings
		UIForm form = UIForm.make(tofill, "basic-form"); //$NON-NLS-1$		
		UIOutput.make(form, "system-settings-header", messageLocator.getMessage("administrate.system.settings.header")); //$NON-NLS-1$ //$NON-NLS-2$
		UIOutput.make(form, "system-settings-instructions", messageLocator.getMessage("administrate.system.settings.instructions")); //$NON-NLS-1$ //$NON-NLS-2$

		//Instructor Settings
		UIOutput.make(form, "instructor-settings-header", messageLocator.getMessage("administrate.instructor.settings.header")); //$NON-NLS-1$ //$NON-NLS-2$
        makeBoolean(form, "instructors-eval-create", EvalSettings.INSTRUCTOR_ALLOWED_CREATE_EVALUATIONS); //$NON-NLS-1$ 
		UIOutput.make(form, "instructors-eval-create-note", messageLocator.getMessage("administrate.instructors.eval.create.note")); //$NON-NLS-1$ //$NON-NLS-2$
		
        //Select for whether instructors can view results or not
        String[] administrateConfigurableList = 
        {
            messageLocator.getMessage("administrate.true.label"),
            messageLocator.getMessage("administrate.false.label"),
            messageLocator.getMessage("administrate.configurable.label")
        };		
        makeSelect(form, "instructors-view-results", //$NON-NLS-1$ 
        		administrateConfigurableList, 
        		administrateConfigurableList, 
        		EvalSettings.INSTRUCTOR_ALLOWED_VIEW_RESULTS);
	    UIOutput.make(form, "instructors-view-results-note", messageLocator.getMessage("administrate.instructors.view.results.note")); //$NON-NLS-1$ //$NON-NLS-2$
		
		makeBoolean(form, "instructors-email-students", EvalSettings.INSTRUCTOR_ALLOWED_EMAIL_STUDENTS); //$NON-NLS-1$ 
		UIOutput.make(form, "instructors-email-students-note", messageLocator.getMessage("administrate.instructors.email.students.note")); //$NON-NLS-1$ //$NON-NLS-2$
		
		/*
		 * (non-Javadoc)
		 * Select for whether instructors must use evaluations from above.
		 * 
		 * Note: The values should be irrespective of i18n as they are stored in database. 
		 * Here the 4th values that is "messageLocator.getMessage("administrate.configurable.label")"
		 * is actually converted to a NULL inside SettingsWBL.java so does not matter to be 
		 * language specific here.
		 */
        String[] hierarchyOptionValues = 
        {
            EvalConstants.INSTRUCTOR_OPT_IN,
            EvalConstants.INSTRUCTOR_OPT_OUT,
            EvalConstants.INSTRUCTOR_REQUIRED,
            messageLocator.getMessage("administrate.configurable.label")
        };
        String[] hierarchyOptionLabels = 
        {
            messageLocator.getMessage("evalsettings.instructors.label.opt.in"),
            messageLocator.getMessage("evalsettings.instructors.label.opt.out"),
            messageLocator.getMessage("evalsettings.instructors.label.required"),
            messageLocator.getMessage("administrate.configurable.label")
        };
        makeSelect(form, "instructors-hierarchy", //$NON-NLS-1$ 
        		hierarchyOptionValues, 
        		hierarchyOptionLabels, 
        		EvalSettings.INSTRUCTOR_MUST_USE_EVALS_FROM_ABOVE);
		UIOutput.make(form, "instructors-hierarchy-note", messageLocator.getMessage("administrate.instructors.hierarchy.note")); //$NON-NLS-1$ //$NON-NLS-2$
		
		//Select for number of questions intructors can add
        makeSelect(form, "instructors-num-questions", //$NON-NLS-1$ 
        		EvaluationConstant.PULLDOWN_INTEGER_VALUES, 
        		EvaluationConstant.PULLDOWN_INTEGER_VALUES, 
        		EvalSettings.INSTRUCTOR_ADD_ITEMS_NUMBER);
		UIOutput.make(form, "instructors-num-questions-note", messageLocator.getMessage("administrate.instructors.num.questions.note")); //$NON-NLS-1$ //$NON-NLS-2$
		
		// Student Settings
		UIOutput.make(form, "student-settings-header", messageLocator.getMessage("administrate.student.settings.header")); //$NON-NLS-1$ //$NON-NLS-2$

		//Select for whether students can leave questions unanswered or not
		makeSelect(form, "students-unanswered",	//$NON-NLS-1$ 
				administrateConfigurableList, 
				administrateConfigurableList, 
				EvalSettings.STUDENT_ALLOWED_LEAVE_UNANSWERED); 
		UIOutput.make(form, "students-unanswered-note", messageLocator.getMessage("administrate.students.unanswered.note")); //$NON-NLS-1$ //$NON-NLS-2$
		
		//Select for whether student can modify responses upto due date
		makeSelect(form, "students-modify-responses", //$NON-NLS-1$ 
				administrateConfigurableList, 
				administrateConfigurableList, 
				EvalSettings.STUDENT_MODIFY_RESPONSES); 
		UIOutput.make(form, "students-modify-responses-note", messageLocator.getMessage("administrate.students.modify.responses.note")); //$NON-NLS-1$ //$NON-NLS-2$
		
		//Select for whether students can view results
		makeSelect(form, "students-view-results", //$NON-NLS-1$
				administrateConfigurableList, 
				administrateConfigurableList, 
				EvalSettings.STUDENT_VIEW_RESULTS);
		UIOutput.make(form, "students-view-results-note", messageLocator.getMessage("administrate.students.view.results.note")); //$NON-NLS-1$ //$NON-NLS-2$
		
		// Administrator Settings
		UIOutput.make(form, "administrator-settings-header", messageLocator.getMessage("administrate.admin.settings.header"));		 //$NON-NLS-1$ //$NON-NLS-2$

		//Select for number of questions admins can add
		makeSelect(form, "admin-hierarchy-num-questions", //$NON-NLS-1$
				EvaluationConstant.PULLDOWN_INTEGER_VALUES,
				EvaluationConstant.PULLDOWN_INTEGER_VALUES,
				EvalSettings.ADMIN_ADD_ITEMS_NUMBER); 
		UIOutput.make(form, "admin-hierarchy-num-questions-note", messageLocator.getMessage("administrate.admin.hierarchy.num.questions.note")); //$NON-NLS-1$ //$NON-NLS-2$

		makeBoolean(form, "admin-view-instructor-added-results", EvalSettings.ADMIN_VIEW_INSTRUCTOR_ADDED_RESULTS); //$NON-NLS-1$ 
		UIOutput.make(form, "admin-view-instructor-added-results-note", messageLocator.getMessage("administrate.admin.view.instructor.added.results.note"));		 //$NON-NLS-1$ //$NON-NLS-2$
		makeBoolean(form, "admin-view-below-results", EvalSettings.ADMIN_VIEW_BELOW_RESULTS); //$NON-NLS-1$ 
		UIOutput.make(form, "admin-view-below-results-note", messageLocator.getMessage("administrate.admin.view.below.results.note"));		 //$NON-NLS-1$ //$NON-NLS-2$
		
		UIOutput.make(form, "general-settings-header", messageLocator.getMessage("administrate.general.settings.header"));		 //$NON-NLS-1$ //$NON-NLS-2$
		makeInput(form, "general-helpdesk-email", EvalSettings.FROM_EMAIL_ADDRESS);
		UIOutput.make(form, "general-helpdesk-email-note", messageLocator.getMessage("administrate.general.helpdesk.email.note")); //$NON-NLS-1$ //$NON-NLS-2$
		
		// Select for number of responses before results could be viewed
		makeSelect(form, "general-responses-before-view",  
				EvaluationConstant.PULLDOWN_INTEGER_VALUES,
				EvaluationConstant.PULLDOWN_INTEGER_VALUES,
				EvalSettings.RESPONSES_REQUIRED_TO_VIEW_RESULTS);
		UIOutput.make(form, "general-responses-before-view-note", messageLocator.getMessage("administrate.general.responses.before.view.note"));		 //$NON-NLS-1$ //$NON-NLS-2$
		
		makeBoolean(form, "general-na-allowed", EvalSettings.NOT_AVAILABLE_ALLOWED); //$NON-NLS-1$ 
		UIOutput.make(form, "general-na-allowed-note", messageLocator.getMessage("administrate.general.na.allowed.note"));	 //$NON-NLS-1$ //$NON-NLS-2$
		
		// Select for maximum number of questions in a block
		makeSelect(form, "general-max-questions-block",	//$NON-NLS-1$
				EvaluationConstant.PULLDOWN_INTEGER_VALUES,
				EvaluationConstant.PULLDOWN_INTEGER_VALUES,
				EvalSettings.ITEMS_ALLOWED_IN_QUESTION_BLOCK);
		UIOutput.make(form, "general-max-questions-block-note", messageLocator.getMessage("administrate.general.max.questions.block.note"));		 //$NON-NLS-1$ //$NON-NLS-2$
		
		// Select for template sharing and visibility settings
		String[] sharingValues = 
        {
            EvalConstants.SHARING_OWNER,
            EvalConstants.SHARING_PRIVATE,
            EvalConstants.SHARING_PUBLIC
        };
        String[] sharingLabels = 
        {
            messageLocator.getMessage("administrate.sharing.owner"),
            messageLocator.getMessage("modifytemplatetitledesc.sharing.private"),
            messageLocator.getMessage("modifytemplatetitledesc.sharing.public")
        };
        makeSelect(form, "general-template-sharing",  //$NON-NLS-1$ 
        		sharingValues, 
        		sharingLabels, 
        		EvalSettings.TEMPLATE_SHARING_AND_VISIBILITY);
		UIOutput.make(form, "general-template-sharing-note", messageLocator.getMessage("administrate.general.template.sharing.note"));		 //$NON-NLS-1$ //$NON-NLS-2$
		
		//Select for whether question category would be course or instructor
		makeSelect(form, "general-default-question", //$NON-NLS-1$ 
				administrateConfigurableList, 
				administrateConfigurableList, 
				EvalSettings.ITEM_USE_COURSE_CATEGORY_ONLY);
		UIOutput.make(form, "general-default-question-category", messageLocator.getMessage("administrate.general.default.question.category.note"));	 //$NON-NLS-1$ //$NON-NLS-2$
		
		makeBoolean(form, "general-use-stop-date", EvalSettings.EVAL_USE_STOP_DATE); //$NON-NLS-1$ 
		UIOutput.make(form, "general-use-stop-date-note", messageLocator.getMessage("administrate.general.use.stop.date.note"));		 //$NON-NLS-1$ //$NON-NLS-2$
		makeBoolean(form, "general-expert-templates", EvalSettings.USE_EXPERT_TEMPLATES); //$NON-NLS-1$ 
		UIOutput.make(form, "general-expert-templates-note", messageLocator.getMessage("administrate.general.expert.templates.note"));	 //$NON-NLS-1$ //$NON-NLS-2$
		makeBoolean(form, "general-expert-questions", EvalSettings.USE_EXPERT_ITEMS); 
		UIOutput.make(form, "general-expert-questions-note", messageLocator.getMessage("administrate.general.expert.questions.note"));		 //$NON-NLS-1$ //$NON-NLS-2$
		makeBoolean(form, "general-same-view-date",  EvalSettings.EVAL_USE_SAME_VIEW_DATES); //$NON-NLS-1$ 
		UIOutput.make(form, "general-same-view-date-note", messageLocator.getMessage("administrate.general.same.view.date.note"));	 //$NON-NLS-1$ //$NON-NLS-2$
		makeBoolean(form, "general-require-comments-block",  EvalSettings.REQUIRE_COMMENTS_BLOCK); //$NON-NLS-1$ 
		UIOutput.make(form, "general-require-comments-block-note", messageLocator.getMessage("administrate.general.require.comments.block.note"));	 //$NON-NLS-1$ //$NON-NLS-2$

		//Number of days old can an eval be and still be recently closed
		makeSelect(form, "general-eval-closed-still-recent", //$NON-NLS-1$
				EvaluationConstant.PULLDOWN_INTEGER_VALUES,
				EvaluationConstant.PULLDOWN_INTEGER_VALUES,
				EvalSettings.EVAL_RECENTLY_CLOSED_DAYS); 
		UIOutput.make(form, "general-eval-closed-still-recent-note", messageLocator.getMessage("administrate.general.eval.closed.still.recent.note")); //$NON-NLS-1$ //$NON-NLS-2$
		
		// Save settings button
        // NB no action now required
		UICommand.make(form, "saveSettings", messageLocator.getMessage("administrate.save.settings.button"), null); //$NON-NLS-1$ //$NON-NLS-2$	
	}

	/* 
	 * (non-Javadoc)
	 * @see uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter#reportNavigationCases()
	 */
	public List reportNavigationCases() {
		List i = new ArrayList();
		return i;
	}
	
	/*
	 * (non-Javadoc)
	 * This method is used to render checkboxes.
	 */
    private void makeBoolean(UIContainer parent, String ID, String adminkey) {
      // Must use "composePath" here since admin keys currently contain periods
      UIBoundBoolean.make(parent, ID, adminkey == null? null : PathUtil.composePath(ADMIN_WBL, adminkey)); 
    }
    
	/*
	 * (non-Javadoc)
	 * This is a common method used to render dropdowns (select boxes).
	 */
    private void makeSelect(UIContainer parent, String ID, String[] values, String[] labels, String adminkey) {
      UISelect selection = UISelect.make(parent, ID); 
      selection.selection = new UIInput();
      if (adminkey != null) {
        selection.selection.valuebinding = new ELReference(PathUtil.composePath(ADMIN_WBL, adminkey));
      }
      UIBoundList selectvalues = new UIBoundList();
      selectvalues.setValue(values);
      selection.optionlist = selectvalues;
      UIBoundList selectlabels = new UIBoundList();
      selectlabels.setValue(labels);
      selection.optionnames = selectlabels;    
    }
    
	/*
	 * (non-Javadoc)
	 * This is a common method used to render text boxes.
	 */
    private void makeInput(UIContainer parent, String ID, String adminkey) {
      UIInput.make(parent, ID, PathUtil.composePath(ADMIN_WBL, adminkey));
    }
}
