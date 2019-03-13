/**
 * Created by hc.zeng on 2018/3/18.
 */
var toastrMsg = (function(){
    'use strict';
    var _msg = function(timeOut){
        var tt = timeOut || 7000;
        top.toastr.options = {
            "closeButton": true,
            "debug": false,
            "progressBar": false,
            "positionClass": "toast-bottom-full-width",
            "onclick": null,
            "showDuration": "400",
            "hideDuration": "1000",
            "timeOut": tt,
            "extendedTimeOut": "1000",
            "showEasing": "swing",
            "hideEasing": "linear",
            "showMethod": "fadeIn",
            "hideMethod": "fadeOut"
        };
    };
    var _success =function (msg,title){
        _msg();
        top.toastr.success(title, msg);
    }
    var _info =function (msg,title,timeOut){
        _msg(timeOut);
        top.toastr.info(title, msg);
    }

    var _error =function (msg,title){
        _msg();
        top.toastr.error(title, msg);
    }

    return {
        "success":_success,
        "error":_error,
        "info":_info
    };
})();

var swalMsg = (function(){
    'use strict';
    var _msg = function(option){
        var _title = option['title']||"";
        var _text = option['text']||"";
        var _type = option['type']||"";
        var _showCancel = option['showCancel']||false;
        var _confirm = option['confirm']||function(){};
        top.swal({
            title: _title,
            text: _text,
            type: _type,
            showCancelButton: _showCancel,
            confirmButtonColor: "#DD6B55",
            confirmButtonText: "确定",
            cancelButtonText: "取消",
            closeOnConfirm: true
        }, function (f) {
            if(_confirm){
                _confirm(f);
            }
        });

    };
    var _msgType = function(title,text,type){
        top.swal(title, text, type);
    }
    var _msgSuccess = function(title,text){
        _msgType(title, text, "success");
    }
    var _msgInfo = function(title,text,type){
        top.swal(title, text, "info");
    }
    var _msgWarn = function(title,text,type){
        top.swal(title, text, "warning");
    }
    return {
        "msg":_msg,
        "success":_msgSuccess,
        "info":_msgInfo,
        "warn":_msgWarn
    };
})();