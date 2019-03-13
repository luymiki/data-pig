/**
 * Created by hc.zeng on 2018/3/21.
 */
(function (e, t, $) {
    "use strict";

    var suspicious = (function () {
        var meta;
        var _init = function init(_data) {
            _initListTable();
            _validator();
            _event();
        };

        var params = {"indexName":"suspicious","conditions":[],"sort":"modify_time desc"};
        var kyr= {
            "field": "type",
            "values": ['2'],
            "searchType": 3,
            "dataType":2,
        };

        var _initListTable = function(){
            $('#suspicious-table').myTable({
                copyRow:$("#copyRow"),
                exportXls:$("#exportXls"),
                exportXlsFun:function () {
                    params["conditions"]=[kyr];
                    console.log(JSON.stringify(params));
                    var from = $('<form method="post" action="/api/eqa/exportEXcel" target="_blank"></form>').appendTo('body');
                    $('<input type="text" name="paramsStr">').val(JSON.stringify(params)).appendTo(from);
                    from.submit().remove();
                },
                comment:$("#pizhu"),
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
                    params["conditions"]=[kyr];
                    $.ajax.proxy({
                        url:"/api/eqa/query",
                        type:"post",
                        dataType:"json",
                        data:{"pageNum":request.data.pageNumber,"pageSize":request.data.pageSize,"paramsStr":JSON.stringify(params)},
                        success : function (msg) {
                            if(msg.status===200){
                                console.log(msg)
                                var data = msg.data.data;
                                var xh =  ((request.data.pageNumber-1)*request.data.pageSize)+1;
                                for(var i= 0;i<data.length;i++){
                                    data[i]['xh'] = xh++;
                                    data[i]['opt'] = "<div class='btn btn-info btn-outline btn-xs gxr' data-id='"+data[i]["id"]+"'>关系人</div><br>" +
                                        "<div class='btn btn-primary btn-outline btn-xs update' data-id='"+data[i]["id"]+"'>修改</div><br>"+
                                        "<div class='btn btn-info btn-outline btn-xs tiqu' data-id='"+data[i]["id"]+"'>提取</div><br>" +
                                        "<div class='btn btn-danger btn-outline btn-xs delete' data-id='"+data[i]["id"]+"'>删除</div>";
                                }
                                request.success({
                                    rows : data,
                                    total : msg.data.total
                                });
                                meta = msg.data.meta["suspicious"];
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
                            area: ['450px', '340px'], //宽高
                            content:  "<div style='padding: 5px;'>"+val+"</div>"
                        });
                    }
                }
            });

        };


        var _event = function () {
            $("#addBtn").on('click',function () {
                $(".import-btn").hide();
                $("#signupForm").find("input").each(function(i,o){
                    $(o).val("");
                });
                $("#signupForm").find("textarea").each(function(i,o){
                    $(o).val("");
                });
                $('#addModal').modal("show");
            });
            // /**
            //  * 批注
            //  */
            // $("#pizhu").on('click',function () {
            //     $("#suspicious-table").myTable("comment");
            // });

            $("#suspicious-table").on('click','.update',function () {
                $(".import-btn").show();
                _get($(this).attr("data-id"));
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

            //列表数据点击链接
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

            //修改框各项导入按钮跳转
            $('#addModal').on('click','.btn-qq',function () {
                var id = $('#addModal').find("#id").val();
                _addImportItem(id,'QQ注册信息导入',"/view/qq/reg/qq-reg.html");
            });
            $('#addModal').on('click','.btn-qq-login',function () {
                var id = $('#addModal').find("#id").val();
                _addImportItem(id,'QQ登录IP导入',"/view/qq/loginip/qq-loginip.html");
            });
            $('#addModal').on('click','.btn-qq-qzone',function () {
                var id = $('#addModal').find("#id").val();
                _addImportItem(id,'QQ空间照片导入',"/view/qq/qzone/qq-qzone.html");
            });
            $('#addModal').on('click','.btn-weixin',function () {
                var id = $('#addModal').find("#id").val();
                _addImportItem(id,'微信注册信息导入',"/view/weixin/weixin-reg.html");
            });
            $('#addModal').on('click','.btn-cft',function () {
                var id = $('#addModal').find("#id").val();
                _addImportItem(id,'财付通信息导入',"/view/cft/cft-reg.html");
            });

            $('#addModal').on('click','.btn-zfb',function () {
                var id = $('#addModal').find("#id").val();
                _addImportItem(id,'支付宝注册信息导入',"/view/zfb/zfb-reg.html");
            });
            $('#addModal').on('click','.btn-zfb-login',function () {
                var id = $('#addModal').find("#id").val();
                _addImportItem(id,'支付宝登陆日志导入',"/view/zfb/logininfo/zfb-logininfo.html");
            });
            $('#addModal').on('click','.btn-zfb-zhmx',function () {
                var id = $('#addModal').find("#id").val();
                _addImportItem(id,'支付宝账户明细导入',"/view/zfb/zhinfo/zfb-zhinfo.html");
            });
            $('#addModal').on('click','.btn-zfb-txjl',function () {
                var id = $('#addModal').find("#id").val();
                _addImportItem(id,'支付宝提现记录导入',"/view/zfb/txinfo/zfb-txinfo.html");
            });
            $('#addModal').on('click','.btn-zfb-zzmx',function () {
                var id = $('#addModal').find("#id").val();
                _addImportItem(id,'支付宝转账明细导入',"/view/zfb/zzinfo/zfb-zzinfo.html");
            });
            $('#addModal').on('click','.btn-zfb-jyjl',function () {
                var id = $('#addModal').find("#id").val();
                _addImportItem(id,'支付宝交易记录导入',"/view/zfb/jyjl/zfb-jyjl.html");
            });

            $('#addModal').on('click','.btn-huadan',function () {
                var id = $('#addModal').find("#id").val();
                _addImportItem(id,'话单信息导入',"/view/huadan/huadan.html");
            });
            $('#addModal').on('click','.btn-email',function () {
                var id = $('#addModal').find("#id").val();
                _addImportItem(id,'电子邮件导入',"/view/file/email/file-email.html");
            });

            //数据变动验证
            $('#addModal').on('change','.data-value',function () {
                changeVal($(this));
            });
        };
        var _addItem =function (id,val,type,title) {
            top.contabs.addMenuItem("/view/suspicious/suspicious-page.html?id="+id+"&type="+type+"&code="+val,title);
        };
        var _addImportItem =function (id,title,url) {
            top.contabs.addMenuItem(url+"?suspid="+id,title);
        };

        /**
         * 扩展数组的方法，提供清除空元素的的方法
         * @returns {boolean}
         */
        Array.prototype.notempty = function(){
            return this.filter(t => t !== undefined && t!== null&& t!=="");
        }

        /**
         * 校验新输入的值是否存在于其他可疑人员中
         * @param type
         * @param allval
         * @param val
         */
        var changeVal = function (obj) {
            var old = "";
            var id = $("#id").val();
            var type = obj.attr("data-type");
            var dataValue = obj.attr("data-value").split(/,|，/);
            var nv = obj.val().split(/,|，/);
            if(dataValue.length > nv.length){
                //删除了内容，不比较
                return false;
            }
            var targert = difference(nv,dataValue);
            targert = targert.notempty();
            if(targert.length === 0){
                return false;
            }
            var params = {"indexName":"suspicious","conditions":[
                {
                    "field": type,
                    "searchType": "1",
                    "dataType": "2",
                    "values": targert,
                    "groupType": "must"
                }

            ],"sort":"modify_time desc"};
            if(id && id!==""){
                params["conditions"][params["conditions"].length]={
                    "field": "id",
                    "searchType": "1",
                    "dataType": "2",
                    "values": [id],
                    "groupType": "not"
                };
            }
            $.ajax.proxy({
                url:"/api/eqa/query",
                type:"post",
                dataType:"json",
                data:{"pageNum":1,"pageSize":10,"paramsStr":JSON.stringify(params)},
                success : function (msg) {
                    if(msg.status===200){
                        console.log(msg.data);
                        var data = msg.data.data;
                        var errormsg ="";
                        if(data.length > 0){
                            for(var i=0; i< data.length;i++){
                                var d = data[i];
                                var ds = d[type];
                                if(ds instanceof Array){
                                    targert.forEach(function(val1, i){
                                        if (ds.indexOf(val1) >= 0) {
                                            errormsg += '<label class="error '+type+'-error" for="name"><i class="fa fa-times-circle"></i> '+val1+"存在于可疑人“"+d["name"]+'”中</label>&emsp;';
                                        }
                                    });

                                }else {
                                    errormsg += '<label class="error '+type+'-error" for="name"><i class="fa fa-times-circle"></i> '+ds+"存在于可疑人“"+d["name"]+'”中</label>&emsp;';
                                }

                            }
                            obj.parent().append(errormsg);
                        }else {
                            obj.attr("data-value",nv);
                        }


                    }
                },
                error:function(){
                    toastrMsg.error("错误！");
                }
            });
        };
        /**
         * 求集合arr1与arr2的差集
         * @param arr1
         * @param arr2
         */
        var difference = function(arr1, arr2) {
            var diff = [];
            arr1.forEach(function(val1, i){
                if (arr2.indexOf(val1) < 0) {
                    diff.push(val1);
                }
            });
            console.log(diff);
            return diff;
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
                    $(form).find("textarea").each(function(i,o){
                        var _name = $(o).attr("name");
                        var _value = $(o).val();
                        data[_name]=_value;
                    });
                    // console.log(data);
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
                async:true,
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
                    toastrMsg.error("保存失败");
                    $( '#addModal' ).modal( 'hide' );
                }
            });
            $(".import-btn").hide();
        };

        var _getParams = {"indexName":"suspicious","conditions":[ ],"sort":"create_time desc"};
        var _get = function (id) {
            _getParams["conditions"]=[{
                "field": "id",
                "values": [id],
                "searchType": 1,
                "dataType":2,
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
                                $("#"+key).val(value).attr("data-value",value);
                            }
                            // $("#addBtn").click();
                            $('#addModal').modal("show");
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