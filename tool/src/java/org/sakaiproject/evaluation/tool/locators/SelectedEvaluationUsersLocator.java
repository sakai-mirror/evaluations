package org.sakaiproject.evaluation.tool.locators;

import java.util.HashMap;
import java.util.Map;

import uk.org.ponder.beanutil.BeanLocator;
/**
 * 
* This is not a true locator of a persistant object but exists to resolve
* Multiple potential mappings between Instructors and TA's to be evaluated and evaluation groups 
*
* @author dhorwitz
*
*/
public class SelectedEvaluationUsersLocator implements BeanLocator {
	
	private Map<String, EvalautionUserSelection> localStore = new HashMap<String, EvalautionUserSelection>();
	
	public Object locateBean(String name) {
		if (localStore.containsKey(name)) {
			return localStore.get(name); 
		} else {
			//these should always exist
			EvalautionUserSelection ret = new EvalautionUserSelection();
			localStore.put(name, ret);
			return ret;
		}
		
	}

	public String[] getDeselectedInstructors(String groupId) {
		if (localStore.containsKey(groupId)) {
			EvalautionUserSelection thisSelection = localStore.get(groupId);
			return thisSelection.deselectedInstructors;
		} 
		return null;
	}
	

	public String[] getDeselectedAssistants(String groupId) {
		if (localStore.containsKey(groupId)) {
			EvalautionUserSelection thisSelection = localStore.get(groupId);
			return thisSelection.deselectedAssistants;
		} 
		return null;
	}
	
	private class EvalautionUserSelection {
		
		private String id;
		public String[] deselectedInstructors;
		public String[] deselectedAssistants;
		
		public String getId() {
			return id;
		}
		public void setId(String id) {
			this.id = id;
		}
		
		
		
	}
	
}
