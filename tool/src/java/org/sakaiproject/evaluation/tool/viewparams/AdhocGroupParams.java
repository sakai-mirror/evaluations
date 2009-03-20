package org.sakaiproject.evaluation.tool.viewparams;

import uk.org.ponder.rsf.viewstate.SimpleViewParameters;

public class AdhocGroupParams extends SimpleViewParameters {
    public Long adhocGroupId, evalId;
    
    // Someday we will have a system for this or type you can inherit.
    // This is basically to make the page an http helper.
    public String returnURL, 
    	selectType; // Can be instructor OR ta. Modifications for EVALSYS-618.
    
    public AdhocGroupParams() {};
    
    public AdhocGroupParams(String viewid, Long adhocGroupId) {
        this.viewID = viewid;
        this.adhocGroupId = adhocGroupId;
    }
    
    public AdhocGroupParams(String viewid, Long adhocGroupId, String returnURL) {
        this.viewID = viewid;
        this.adhocGroupId = adhocGroupId;
        this.returnURL = returnURL;
    }
    
    public AdhocGroupParams(String viewid, Long adhocGroupId, String selectType, Long evalId) {
        this.viewID = viewid;
        this.adhocGroupId = adhocGroupId;
        this.selectType = selectType;
        this.evalId = evalId;
    }
}
