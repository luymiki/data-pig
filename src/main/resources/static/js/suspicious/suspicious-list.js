/**
 * Created by hc.zeng on 2018/3/21.
 */

var suspicious_list = (function () {
    var suspid;
    var _init = function init() {
        var params = utils.getURLParams();
        suspid = params["suspid"];
        _event();
        _list();
    };
    var selected={
        suspName:"",
        suspId:""
    };
    var params = {
        "indexName": "suspicious",
        "conditions": [],
        "sort": "modify_time desc"
    };

    var _list = function () {
        //alert(suspid)
        $.ajax.proxy({
            url:"/api/eqa/query",
            type:"post",
            dataType:"json",
            data:{"pageNum":1,"pageSize":1000,"paramsStr":JSON.stringify(params)},
            success : function (d) {
                //console.log(d);
                if(d.status===200){
                    var data = d.data.data;
                    if(data && data.length>0){

                        var $list= $("#suspicious-list").empty();
                        $('<option value="">请选择人员</option>').appendTo($list);
                        for(var i=0; i<data.length;i++){
                            var f = data[i];
                            $("<option value='"+f["name"]+"!!"+f["id"]+"' data-id='"+f["id"]+"'  "+(suspid === f["id"] ? "selected" : "" ) + ">"+f["name"]+" | "+f["gmsfzh"]+" | "+formatterType(f["type"])+"</option>").appendTo($list);
                            if(suspid === f["id"]){
                                selected.suspName = f["name"];
                                selected.suspId = f["id"];
                            }
                        }
                        $("#suspicious-list").chosen({}).change(function(){
                            var v = $(this).val();
                            var ss = v.split("!!");
                            if(ss.length===2){
                                selected.suspName = ss[0];
                                selected.suspId = ss[1];
                            }else{
                                selected.suspName = "";
                                selected.suspId = "";
                            }

                        });
                    }
                }else {
                    toastrMsg.error("查询失败");
                }
            },
            error:function(){
                toastrMsg.error("查询失败");
            }
        });
    };

    var _event = function () {
        // $("#suspicious-table").on('click', '.update', function () {
        //     _get($(this).attr("data-id"));
        // });
        // $("#suspicious-table").on('click', '.delete', function () {
        //     _delete($(this).attr("data-id"));
        // });
    };

    var formatterType = function (d){
        if(d && d ==="2"){
            return "关系人";
        }
        return "可疑人";
    };
    return {
        init: _init,
        selected: selected
    };
})();

suspicious_list.init();
