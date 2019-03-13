/**
 * Created by hc.zeng on 2018/3/18.
 */

$.ajax.proxy = function (option) {
    'use strict';

    var token = getStorage().getItem("token");
    option["headers"] = {
        "Authorization": token
    };
    //loading层
    var index = parent.layer.load(1, {
        // content:"加载中，请稍后。。。",
        shade: [0.1, '#fff'] //0.1透明度的白色背景
    });
    var v_success;
    if (option["success"]) {
        v_success = option.success;
    }
    var _success = function (d) {
        if (d && d["status"] === 401) {
            //window.top.location.href = "/index.html";
        }
        try {
            parent.layer.closeAll();
            if (v_success) {
                v_success(d);
            }
        } catch (e) {
            console.error(e);
        }


    };
    option["success"] = _success;

    var v_error;
    if (option["error"]) {
        v_error = option.error;
    }
    var _error = function (d) {
        try {
            if (v_error) {
                v_error(d);
            }
        } catch (e) {
            console.error(e);
        }
        parent.layer.closeAll();

    };
    option["error"] = _error;
    $.ajax(option);
};