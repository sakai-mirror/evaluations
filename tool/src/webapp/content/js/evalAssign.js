/**
 * For the evalAssign view
 */
$(document).ready(function() {
    $('a[rel=assignInstructorSelector]').assignSelector({type:0});
    $('a[rel=assignTaSelector]').assignSelector({type:1});
    $(':submit').bind('click', function(){
        //Validate group Selections
        var countChecked = $('form[id=eval-assign-form] input[@type=checkbox]:checked').get().length;
        if(countChecked == null || countChecked == 0){
            $('#error').fadeIn("fast");
            return false;
        }else{
            $('#error').fadeOut("fast");           
        }
        return true;
    });
});

(function($) {
    $.fn.assignSelector = function(opts) {
        var options = $.extend({}, $.fn.assignSelector.defaults, opts);
        init($(this), options);
    };
    /**
     * Public variables. Once init() has ran, do not reference directly to these, use variables.options instead.
     */
    $.fn.assignSelector.defaults = {
        type: 1, //Type is for type of category we are handling. ie: 0 = instructor, 1 = ta
        debug: false
    };
    /**
     * Private methods and variables
     *
     */

    var variables = {
        option:null,
        get:{
            typeOfBranch:function() {
                switch (variables.options.type) {
                    case 0: return "instructor";break;
                    case 1: return "ta";break;
                }
            },
            siteId:false,
            documentFB:null
        },
        set:{
            typeOfBranch:function(that) {
                var temp;
                if (that.attr('rel').search(/instructor/i) != -1)
                    temp = 0;
                else if (that.attr('rel').search(/tas/i) != -1)
                    temp = 1;
                variables.options.type = temp;
                log("Active catergory type is: " + variables.get.typeOfBranch());
            },
            siteId:function(that) {
                variables.get.siteId = that.parents('tr:eq(0)').find('input:eq(0)').val();
                return !variables.get.siteId ? false : true;  //Do not simplify this. Should return true if siteId is anything other than false.
            }
        },
        selectedPeople:0,
        deselectedPeopleIds: new Array(),
        initedafterRevealFacebox:0,
        that:null,
        deselectedLog:new Array(),
        thatRowNumber:0 ,
        evalGroupId:null,
        groupCheckBox:null,
        css: {
            enabled:{
                color: ""
            },
            disabled:{
                color: "#888"
            }
        }
    };

    function init(that, options) {
        //copy options to this class
        variables.options = options;
        $(document).bind('afterReveal.facebox', function() {
            variables.initedafterRevealFacebox++;
            log(variables.initedafterRevealFacebox);
            if (variables.initedafterRevealFacebox == 1) {
                log("WARN: Lightbox loaded, attaching listerners now...");
                var _that = $('#facebox div.content:eq(0)');    //lightbox $document object
                variables.get.documentFB = _that ? _that : null;
                //deselect boxes already deselected
                var field;
                    var regText = variables.evalGroupId + ".deselected" + (variables.options.type == 0 ? "Instructors" : "Assistants");
                    var sRegExInput = new RegExp(regText);
                    $('input[name=el-binding]').each(function() {
                        if ($(this).val().search(sRegExInput) != -1) {
                            field = $(this);
                        }
                    });
                var deselected = field.val().replace("j#{selectedEvaluationUsersLocator." + regText + "}[","").replace("]","").split(",");
                if(deselected.length>0){
                    variables.get.documentFB.find('input[@type=checkbox]:checked').each(function(){
                        for(var i=0;i<deselected.length;i++){
                        //console.log("checking"+deselected[i]+" ------------ "+$(this).attr('id')) ;
                         if(deselected[i]==$(this).attr('id')){
                             this.checked = false;
                         }
                    }
                });
                handleCheckboxes(variables.get.documentFB.find('input[@type=checkbox]').not(':checked'),1);
                }
                $('#facebox input[id=save-item-action]').bind('click', function() {
                    log("Binding submit button value");
                    handleFormSubmit(this);
                    return false;
                });
                //Make list scrollable if hieght is more than 200px
                var tableHolder = variables.get.documentFB.find('.selectTable:eq(0)');
                tableHolder.css({
                    'overflow': 'auto',
                    'height': tableHolder.height() > 200 ? '200px' : (tableHolder.height() + 5) + "px"
                });
                log("Formatting table holder hieght. Set height to:" + tableHolder.height());
            }
        });
        $(document).bind('afterClose.facebox', function() {
            log("Running afterClose.facebox; initialising variables.initedafterRevealFacebox.");
            variables.initedafterRevealFacebox = 0;
        });
        return that.each(function() {
            initControls($(this));
            initClassVars();
        });
    }

    function initClassVars() {
        variables.selectedPeople = 0;
        variables.deselectedPeopleIds = new Array();
        variables.thatRowNumber = 0;
        variables.evalGroupId = null;
    }

    function initControls(that) {
        //that.css(variables.css.disabled);
        that.hide();
        variables.groupCheckBox = that.parents('tr').find('input[@type=checkbox]');
        variables.groupCheckBox.bind('click', function() {
            if (this.checked) {
                that
                        .fadeIn('normal')
                        .bind('click', function() {
                    var _url = that.attr('href');
                    variables.that = that;
                    variables.thatRowNumber = that.parents('tr').attr('rel');
                    var grpId = variables.groupCheckBox.val();
                    variables.evalGroupId = grpId == null ? '' : grpId.replace('/site/', '');
                    variables.set.typeOfBranch(that);
                    log("Fetching URL: " + _url);
                    $.facebox({ajax: _url});
                    return false;
                });
            } else {
                that.fadeOut('normal', function() {
                    //resetting already selected Instr/Ass
                    var grpId = variables.groupCheckBox.val();
                    variables.evalGroupId = grpId == null ? '' : grpId.replace('/site/', '');
                    variables.set.typeOfBranch(that);
                    var field;
                    var regText = variables.evalGroupId + ".deselected" + (variables.options.type == 0 ? "Instructors" : "Assistants");
                    var sRegExInput = new RegExp(regText);
                    $('input[name=el-binding]').each(function() {
                        if ($(this).val().search(sRegExInput) != -1) {
                            field = $(this);
                        }
                    });
                    if (field != null) {
                        field.val("j#{selectedEvaluationUsersLocator." + regText + "}[]");
                    }
                    
                    //reset dom count on selected Instr/Ass
                    var origionalSelected = that.attr('class').replace("addItem total:", "");  //gets a String
                    var text = that.attr('title') + " (" + origionalSelected + "/" + origionalSelected + ")";
                    that.text(text);
                });

            }
        });

    }

    function handleCheckboxes(unChecked, where) {
        //var unChecked = variables.get.documentFB.find('input[@type=checkbox]').not(':checked');
        if (unChecked.length != 0) {
            unChecked.each(function() {
                var id = $(this).attr('id');
                variables.deselectedPeopleIds.push(id);
            });
            var field;
            var regText = variables.evalGroupId + ".deselected" + (variables.options.type == 0 ? "Instructors" : "Assistants");
            var sRegExInput = new RegExp(regText);
            $('input[name=el-binding]').each(function() {
                if ($(this).val().search(sRegExInput) != -1) {
                    field = $(this);
                }
            });
            if (field != null) {
                field.val("j#{selectedEvaluationUsersLocator." + regText + "}[" + variables.deselectedPeopleIds.toString() + "]");
                log('Found - ' + unChecked.length + ' - deselected people and setting form value now. New val is:' + field.val());
                variables.deselectedPeopleIds = new Array();
                return true;
            } else {
                log("ERROR: Field param with part val:" + regText + " Not FOUND.");
            }

        }
        else {
            if(where==1){
                            return false;
                        }
                        $(document).trigger('close.facebox');
        }
        return false;
    }

    function handleFormSubmit(_that) {
        var that = $(_that);
        log("Running pre-SET checks");
        var temp = variables.get.documentFB.find('input[@type=checkbox]').not(':checked');
        var tempChecked = variables.get.documentFB.find('input[@type=checkbox]:checked');
        variables.selectedPeople = tempChecked.length > 0 ? tempChecked.length : 0;
          if (handleCheckboxes(temp,0)) {
                var tempText = variables.that.text();
                var index1 = tempText.indexOf('(');
                var index2 = tempText.indexOf('/');
                var diff = parseInt((index2 - index1) - 1);
                var replaceText;
                log("Found string: " + tempText + ".( is char #:" + index1 + "./ is char #:" + index2 + ". Num of digits:" + diff);
                if (diff == 1) {
                    replaceText = tempText.charAt(parseInt(index1 + 1));
                } else {
                    replaceText = tempText.charAt(parseInt(index1 + 1));
                }
                if (replaceText != null) {
                    replaceText = "\\(" + replaceText;
                    var sRegExInput = new RegExp(replaceText);
                    log(variables.selectedPeople);
                    tempText = tempText.replace(sRegExInput, "(" + variables.selectedPeople);
                    variables.that.text(tempText);
                    log("Replaced text is:" + tempText);
                    $(document).trigger('close.facebox');
                } else {
                    log("CATASTROPHIC ERROR: replaceText cannot be null!");
                }
        }
        initClassVars();return true;

    }

    // Debugging
    function log(obj) {
        if ((variables.options.debug) && (window.console && window.console.log)) {
            window.console.log('INFO: ' + obj);
        }
    }
})($)