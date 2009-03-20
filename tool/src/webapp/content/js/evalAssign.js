/**
 * For the evalAssign view
 */
$(document).ready(function() {
    $('a[@rel=assignInstructorSelector]').assignSelector({type:0});
    $('a[@rel=assignTaSelector]').assignSelector({type:1});
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
        debug: true
    }
    /**
     * Private methods and variables
     *
     * @param that Element to be attached to
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
        ajaxOptions:{
            beforeSubmit: function() {
                log("Running pre-post checks");
                var temp = variables.get.documentFB.find('input[@type=checkbox]:checked');
                variables.selectedPeople = temp.length > 0 ? temp.length : 0;
                if (variables.selectedPeople == 0) {
                    log("ERROR: No checkboxes selected. Resetting class variables now.");  //Should warn user about this.
                    initClassVars();
                    return false;
                } 
            },
            success: function(data) {
                data != null ? log("Got some data!") : log("ERROR: Got NO data!");
                var tempText = variables.that.text();
                var index1 = tempText.indexOf('(');
                var index2 = tempText.indexOf('/');
                var diff = parseInt((index2 - index1)-1);
                var replaceText;
                log("Found string: "+tempText+".( is char #:"+index1+"./ is char #:"+index2+". Num of digits:"+diff);
                if(diff == 1){
                    replaceText = tempText.charAt(parseInt(index1+1)) ;
                }else{
                    //replaceText = tempText.charAt(parseInt(index1+1)) ;
                }
                if(replaceText!=null){
                    replaceText = "\\("+replaceText;
                    var sRegExInput = new RegExp(replaceText);
                    log(variables.selectedPeople);
                    tempText = tempText.replace(sRegExInput,"("+variables.selectedPeople);
                    variables.that.text(tempText);
                    log("Replaced text is:"+tempText);
                }else{
                    log("CATASTROPHIC ERROR: replaceText cannot be null!");
                }

            }
        },
        selectedPeople:0,
        initedafterRevealFacebox:0,
        that:null
    }

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
                variables.get.documentFB.find('form:eq(0)').ajaxForm(variables.ajaxOptions);
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

    }

    function initControls(that) {
        that.bind('click', function() {
            var _url = that.attr('href');
            variables.that = that;
            log("Fetching URL: " + _url);
            $.facebox({ajax: _url});
            return false;
        });
    }

    // Debugging
    function log(obj) {
        if ((variables.options.debug) && (window.console && window.console.log)) {
            window.console.log('INFO: ' + obj);
        }
        else {
            alert('INFO: ' + obj);
        }
    }
})($)