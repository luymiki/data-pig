/**
 * Created by hc.zeng on 2018/3/21.
 */
(function (e, t, $) {
    "use strict";

    var suspicious = (function () {
        var id;
        var kyr;
        var _init = function init(_data) {
            var params = utils.getURLParams();
            id = params["id"];
            _getKyr();
            if(kyr){
                $("#kyr-name").html("可疑人员："+kyr["name"]);
                $("#kyrId").val(id);
                _initListTable();
                _validator();
                _event();
            }

        };

        var params = {"indexName":"suspicious","conditions":[],"sort":"modify_time desc"};


        var _initListTable = function(){

            $('#suspicious-table').myTable({
                columns:[
                    {field: 'checkbox',title: '选择',width:'50px',checkbox:true},
                    {field: 'xh',title: '序号',width:'50px'},
                    {field: 'id',title: 'ID',visible:false},
                    {field: 'type',title: '类型',width:'100px',formatter:formatterType},
                    {field: 'name',title: '姓名'},
                    {field: 'gmsfzh',title: '身份证号',sortable:true},
                    {field: 'qkjj',title: '情况简介',class:'qkjj-cell',formatter:formatterStr},
                    {field: 'qq',title: 'QQ',formatter:formatterList},
                    {field: 'weixin',title: '微信',formatter:formatterList},
                    {field: 'phone',title: '手机号',formatter:formatterList},
                    {field: 'imei',title: 'IMEI',formatter:formatterList},
                    {field: 'imsi',title: 'IMSI',formatter:formatterList},
                    {field: 'cft',title: '财付通',formatter:formatterList},
                    {field: 'zfb',title: '支付宝',formatter:formatterList},
                    {field: 'yhzh',title: '银行账号',formatter:formatterList},
                    {field: 'ip',title: 'IP',formatter:formatterList},
                    {field: 'email',title: '电子邮箱',formatter:formatterList},
                    {field: 'other',title: '其他码值',formatter:formatterList},
                    {field: 'gzjd',title: '工作进度'},
                    {field: 'opt',title: '操作',width:'110px'}
                ],
                ajax : function (request) {
                    var sort = "modify_time desc";
                    if(request.data.sortName){
                        sort = request.data.sortName +" "+request.data.sortOrder;
                    }
                    params["sort"]=sort;
                    params["conditions"]=[   {
                        "field": "kyr_id",
                        "values": [id],
                        "searchType": 1,
                        "dataType":2,
                    }
                    ];

                    $.ajax.proxy({
                        url:"/api/eqa/query",
                        type:"post",
                        dataType:"json",
                        data:{"pageNum":request.data.pageNumber,"pageSize":request.data.pageSize,"paramsStr":JSON.stringify(params)},
                        success : function (msg) {
                            if(msg.status===200){
                                var data = msg.data.data;
                                var xh =  ((request.data.pageNumber-1)*request.data.pageSize)+1;
                                for(var i= 0;i<data.length;i++){
                                    data[i]['xh'] = xh++;
                                    data[i]['opt'] = "<div class='btn btn-info btn-outline btn-xs tiqu' data-id='"+data[i]["id"]+"'>提取</div>&nbsp;" +
                                        "<div class='btn btn-primary btn-outline btn-xs update' data-id='"+data[i]["id"]+"'>修改</div>&nbsp;"+
                                        "<div class='btn btn-danger btn-outline btn-xs delete' data-id='"+data[i]["id"]+"'>删除</div>";
                                }
                                request.success({
                                    rows : data,
                                    total : msg.data.total
                                });
                            }else {
                                request.success({
                                    rows : [],
                                    total : 0
                                });
                            }
                        },
                        error:function(){
                            toastrMsg.error("错误！");
                        }
                    });
                },
                onDblClickCell:function(field, value, row, $element) {
                    //console.log(row);
                    var val = row[field];
                    if(field === "type"){
                        val = formatterType(val);
                    }
                    if(val instanceof Array){
                        var s = "";
                        for(var i=0 ; i< val.length;i++){
                            var d = val[i];
                            s += formatter(field,row["id"],d);
                        }
                        //val =  val.join("&emsp;");
                        val = s;
                    }
                    if(val!==""){
                        layer.open({
                            // time: 2000, //不自动关闭
                            type: 1,
                            skin: 'layui-layer-rim', //加上边框
                            area: ['420px', '340px'], //宽高
                            content: val
                        });
                    }
                }
            });
        };


        var _event = function () {
            $("#suspicious-table").on('click','.update',function () {
                _get($(this).attr("data-id"));
                layer.tips('Hi，我是tips', '吸附元素选择器，如#id');
            });
            $("#suspicious-table").on('click','.delete',function () {
                _delete($(this).attr("data-id"));
            });
            $("#suspicious-table").on('click','.gxr',function () {
                top.contabs.addMenuItem("/view/suspicious/suspicious-gxr.html?id="+$(this).attr("data-id"),'关系人列表');
            });
            $("#suspicious-table").on('click','.tiqu',function () {
                _tiqu($(this).attr("data-id"));
            });
            $("#suspicious-table").on('dblclick','.tiqu',function () {
                _tiqu($(this).attr("data-id"));
            });
            $("body").on('click','.data-qq',function () {
                var qq = $(this).attr("data-qq");
                _addItem($(this).attr("data-id"),qq,"qq",'QQ['+qq+']信息列表');
            });
            $("body").on('click','.data-weixin',function () {
                var weixin = $(this).attr("data-weixin");
                _addItem($(this).attr("data-id"),weixin,"weixin",'微信['+weixin+']信息列表');
            });
            $("body").on('click','.data-dh',function () {
                var dh = $(this).attr("data-dh");
                _addItem($(this).attr("data-id"),dh,"dh",'手机号['+dh+']信息列表');
            });
            $("body").on('click','.data-cft',function () {
                var cft = $(this).attr("data-cft");
                _addItem($(this).attr("data-id"),cft,"cft",'财付通['+cft+']信息列表');
            });
            $("body").on('click','.data-yhzh',function () {
                var yhzh = $(this).attr("data-yhzh");
                _addItem($(this).attr("data-id"),yhzh,"yhzh",'银行账号['+yhzh+']信息列表');
            });
            $("body").on('click','.data-email',function () {
                var email = $(this).attr("data-email");
                _addItem($(this).attr("data-id"),email,"email",'电子邮件['+email+']信息列表');
            });
            $("body").on('click','.data-ip',function () {
                var ip = $(this).attr("data-ip");
                _addItem($(this).attr("data-id"),ip,"ip",'IP['+ip+']信息列表');
            });
        };
        var _addItem =function (id,val,type,title) {
            top.contabs.addMenuItem("/view/suspicious/suspicious-page.html?id="+id+"&type="+type+"&code="+val,title);
        }

        var formatter = function (field,id,val) {
            switch (field){
                case "qq":
                    return _formatter(id,val,'data-qq');
                case "weixin":
                    return _formatter(id,val,'data-weixin');
                case "phone":
                    return _formatter(id,val,'data-dh');
                case "cft":
                    return _formatter(id,val,'data-cft');
                case "yhzh":
                    return _formatter(id,val,'data-yhzh');
                case "email":
                    return _formatter(id,val,'data-email');
                case "ip":
                    return _formatter(id,val,'data-ip');
                default:
                    return val +" ";
            }
        }
        var _formatter = function (id,val,clazz) {
            return "<a class='"+clazz+"' data-id='"+id+"' "+clazz+"='"+val+"' >"+val+"</a> ";
        }

        var formatterList = function (d,item, idx, field){
            if(d){
                var s = "";
                for(var i=0 ; i< d.length && i<5;i++){
                    s += formatter(field,item["id"],d[i]);
                }
                if(d.length >5){
                    s += "...";
                }
                // for(var i=0 ; i< d.length;i++){
                //     s+= d[i]+" ";
                // }
                return s;
            }
            return d;
        };
        var formatterStr = function (d){
            if(d){
                var s = d;
                if(d.length >50){
                    s = d.substring(0,50)+"...";
                }
                return "<div class='qkjj-cell'>"+s+"</div>";
            }
            return d;
        };
        var formatterType = function (d){
            if(d && d ==="2"){
                return "关系人";
            }
            return "可疑人";
        };

        var validator;
        var _validator = function(){
            // validator = $("#signupForm").validate({
            //     submitHandler:function(form){
            //         form.submit();
            //     }
            // });
            var icon = "<i class='fa fa-times-circle'></i> ";
            $("#submit").click(function(){
                $("#signupForm").submit();
            });
            validator = $("#signupForm").validate({
                rules: {
                    name: {
                        required: true,
                        minlength: 2,
                        maxlength:10
                    },
                    gmsfzh: {
                        required: true,
                        minlength: 6,
                        maxlength:18
                    },
                    gzjd: {
                        required: true,
                        minlength: 2
                    }
                },
                messages: {
                    name: {
                        required: icon + "请输入可疑人员姓名",
                        minlength: icon + "姓名必须2个字符以上",
                        maxlength:icon + "姓名必须10个字符以内"
                    },
                    gmsfzh:  {
                        required: icon + "请输入证件号码",
                        minlength: icon + "证件号码必须5个字符以上",
                        maxlength:icon + "姓名必须18个字符以内"
                    },
                    gzjd: {
                        required: icon + "请输入工作进度",
                        minlength: icon + "工作进度必须2个字符以上"
                    }
                },
                submitHandler:function(form){
                    var data = {};
                    $(form).find("input").each(function(i,o){
                        var _name = $(o).attr("name");
                        var _value = $(o).val();
                        data[_name]=_value;
                    });
                    console.log(data);
                    _save(data);
                    //form.submit();

                }
            });
        };//

        var _save = function(data){
            $.ajax.proxy({
                url:"/api/admin/suspicious/save",
                type:"post",
                dataType:"json",
                data:data,
                success:function (d) {

                    if(d.status===200){
                        if(data["id"]){
                            toastrMsg.success("修改成功");
                        }else {
                            toastrMsg.success("保存成功");
                        }
                        $( '#addModal' ).modal( 'hide' );
                        $('#suspicious-table').bootstrapTable("refresh");
                    }
                    else {
                        console.log(d);
                        toastrMsg.error("保存失败");
                        $( '#addModal' ).modal( 'hide' );
                    }

                },
                error:function (d) {
                    console.log(d);
                    top.toastrMsg.error("保存失败");
                    $( '#addModal' ).modal( 'hide' );
                }
            });
        };

        var _getParams = {"indexName":"suspicious","conditions":[ ],"sort":"create_time desc"};
        var _get = function (id) {
            _getParams["conditions"]=[{
                "field": "id",
                "values": [id],
                "searchType": 1,
                "dataType":1,
            }];
            $.ajax.proxy({
                url:"/api/eqa/query",
                type:"post",
                dataType:"json",
                data:{"pageNum":1,"pageSize":1,"paramsStr":JSON.stringify(_getParams)},
                success : function (d) {
                    console.log(d);
                    if(d.status===200){
                        var data = d.data.data;
                        if(data && data.length===1){
                            var susp = data[0];
                            for(var key in susp){
                                var value=susp[key];
                                $("#"+key).val(value);
                            }
                            $("#addBtn").click();
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
        var _delete = function (id) {

            swalMsg.msg({
                text:"是否删除可疑人员信息？",
                type:"warning",
                showCancel:true,
                confirm:function (f) {
                    if(f) {

                        $.ajax.proxy({
                            url: "/api/admin/suspicious/delete",
                            type: "post",
                            dataType: "json",
                            data: {"id": id},
                            success: function (d) {
                                console.log(d);
                                if (d.status === 200) {
                                    toastrMsg.success("删除成功");
                                    $('#suspicious-table').bootstrapTable("refresh");
                                } else {
                                    toastrMsg.error("删除失败");
                                }
                            },
                            error: function () {
                                toastrMsg.error("删除失败");
                            }
                        });
                    }
                }
            });


        }

        var params_kyr = {"indexName":"suspicious","conditions":[],"sort":"create_time desc"};

        var _getKyr = function () {
            params_kyr["conditions"]=[{
                "field": "id",
                "values": [id],
                "searchType": 1,
                "dataType":2,
            }];
            $.ajax.proxy({
                url:"/api/eqa/query",
                type:"post",
                dataType:"json",
                data:{"pageNum":1,"pageSize":1,"paramsStr":JSON.stringify(params_kyr)},
                async:false,
                success : function (d) {
                    console.log(d);
                    if(d.status===200){
                        var file = d.data.data;
                        if(file && file.length===1){
                            kyr = file[0];
                        }
                    }else {
                        toastrMsg.error("查询可疑人失败");
                    }
                },
                error:function(){
                    toastrMsg.error("查询可疑人失败");
                }
            });
        };
        var _tiqu = function (id) {
            // toastrMsg.success("提取信息中，请稍后。。。");
            $.ajax.proxy({
                url:"/api/admin/suspicious/analyze",
                type:"post",
                dataType:"json",
                data:{"id":id},
                async:true,
                success:function (d) {

                    if(d.status===200){
                        toastrMsg.success("提取完成");
                        $('#suspicious-table').bootstrapTable("refresh");
                    }
                    else {
                        console.log(d);
                        toastrMsg.error("提取失败");
                        $( '#addModal' ).modal( 'hide' );
                    }

                },
                error:function (d) {
                    console.log(d);
                    top.toastrMsg.error("提取失败");
                }
            });
        }
        return {
            init:_init
        };
    })();

    suspicious.init();


})(document, window, jQuery);