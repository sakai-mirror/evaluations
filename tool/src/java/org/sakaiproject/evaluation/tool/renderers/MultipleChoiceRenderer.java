/**
 * MultipleChoiceRenderer.java - evaluation - Jan 21, 2008 2:59:12 PM - azeckoski
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

package org.sakaiproject.evaluation.tool.renderers;

import java.util.Map;

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.model.EvalScale;
import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.evaluation.utils.ArrayUtils;

import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIJointContainer;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.UISelect;
import uk.org.ponder.rsf.components.UISelectChoice;
import uk.org.ponder.rsf.components.UISelectLabel;
import uk.org.ponder.rsf.components.UIVerbatim;
import uk.org.ponder.rsf.components.decorators.DecoratorList;
import uk.org.ponder.rsf.components.decorators.UILabelTargetDecorator;
import uk.org.ponder.rsf.components.decorators.UIStyleDecorator;

/**
 * This handles the rendering of multiple choice type items
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
@SuppressWarnings("deprecation")
public class MultipleChoiceRenderer implements ItemRenderer {

    /**
     * This identifies the template component associated with this renderer
     */
    public static final String COMPONENT_ID = "render-multiplechoice-item:";

    /* (non-Javadoc)
     * @see org.sakaiproject.evaluation.tool.renderers.ItemRenderer#renderItem(uk.org.ponder.rsf.components.UIContainer, java.lang.String, org.sakaiproject.evaluation.model.EvalTemplateItem, int, boolean)
     */
    public UIJointContainer renderItem(UIContainer parent, String ID, String[] bindings, EvalTemplateItem templateItem, int displayNumber, boolean disabled, Map<String, Object> renderProperties) {
        UIJointContainer container = new UIJointContainer(parent, ID, COMPONENT_ID);

        if (displayNumber <= 0) displayNumber = 0;
        // this if is here because giving an RSF input control a null binding AND a null initial value causes a failure
        String initValue = null;
        if (bindings[0] == null) initValue = "";

        EvalScale scale = templateItem.getItem().getScale();
        String[] scaleOptions = scale.getOptions();
        int optionCount = scaleOptions.length;
        String scaleValues[] = new String[optionCount];
        String scaleLabels[] = new String[optionCount];

        String scaleDisplaySetting = templateItem.getScaleDisplaySetting();
        boolean usesNA = templateItem.getUsesNA().booleanValue();

        if (EvalConstants.ITEM_SCALE_DISPLAY_FULL.equals(scaleDisplaySetting) || 
                EvalConstants.ITEM_SCALE_DISPLAY_VERTICAL.equals(scaleDisplaySetting)) {
            UIBranchContainer fullFirst = UIBranchContainer.make(container, "fullType:");
            if (renderProperties.containsKey(ItemRenderer.EVAL_PROP_RENDER_INVALID)) {
                fullFirst.decorate( new UIStyleDecorator("validFail") ); // must match the existing CSS class
            } else if ( renderProperties.containsKey(ItemRenderer.EVAL_PROP_ANSWER_REQUIRED) ) {
                fullFirst.decorate( new UIStyleDecorator("compulsory") ); // must match the existing CSS class
            }

            for (int count = 0; count < optionCount; count++) {
                scaleValues[count] = new Integer(count).toString();
                scaleLabels[count] = scaleOptions[count];	
            }

            UIOutput.make(fullFirst, "itemNum", displayNumber+"" );
            UIVerbatim.make(fullFirst, "itemText", templateItem.getItem().getItemText());

            // display next row
            UIBranchContainer radiobranchFullRow = UIBranchContainer.make(container, "nextrow:", displayNumber+"");

            String containerId;
            if ( EvalConstants.ITEM_SCALE_DISPLAY_FULL.equals(scaleDisplaySetting) ) {
                containerId = "fullDisplay:";
            } else if ( EvalConstants.ITEM_SCALE_DISPLAY_VERTICAL.equals(scaleDisplaySetting) ) {
                containerId = "verticalDisplay:";
            } else {
                throw new RuntimeException("Invalid scaleDisplaySetting (this should not be possible): " + scaleDisplaySetting);
            }

            UIBranchContainer displayContainer = UIBranchContainer.make(radiobranchFullRow, containerId);

            if (usesNA) {
                scaleValues = ArrayUtils.appendArray(scaleValues, EvalConstants.NA_VALUE.toString());
                scaleLabels = ArrayUtils.appendArray(scaleLabels, "");
            }

            UISelect radios = UISelect.make(displayContainer, "dummyRadio", scaleValues, scaleLabels, bindings[0], initValue);
            String selectID = radios.getFullID();

            if (disabled) {
                radios.selection.willinput = false;
                radios.selection.fossilize = false;
            }

            int scaleLength = scaleValues.length;
            int limit = usesNA? scaleLength - 1: scaleLength;  // skip the NA value at the end
            for (int j = 0; j < limit; ++j) {
                UIBranchContainer radiobranchNested = 
                    UIBranchContainer.make(displayContainer, "scaleOptions:", j+"");
                UISelectChoice choice = UISelectChoice.make(radiobranchNested, "choiceValue", selectID, j);
                UISelectLabel.make(radiobranchNested, "choiceLabel", selectID, j).decorate( new UILabelTargetDecorator(choice) );
            }

            if (usesNA) {
                UIBranchContainer radiobranch3 = UIBranchContainer.make(displayContainer, "showNA:");
                radiobranch3.decorators = new DecoratorList( new UIStyleDecorator("na") );// must match the existing CSS class				
                UISelectChoice choice = UISelectChoice.make(radiobranch3, "itemNA", selectID, scaleLength - 1);
                UIMessage.make(radiobranch3, "descNA", "viewitem.na.desc").decorate( new UILabelTargetDecorator(choice) );
            }

        } else {
            throw new IllegalStateException("Unknown scaleDisplaySetting ("+scaleDisplaySetting+") for " + getRenderType());
        }

        // render the item comment
        ItemRendererImpl.renderCommentBlock(container, templateItem, bindings);

        return container;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.evaluation.tool.renderers.ItemRenderer#getRenderType()
     */
    public String getRenderType() {
        return EvalConstants.ITEM_TYPE_MULTIPLECHOICE;
    }

}
