/**
 * Created by hc.zeng on 2018/3/21.
 */
(function (e, t, $) {
    "use strict";

    var reg = (function () {

        var _init = function init(_data) {
            _initListTable();
            _event();
        };

        var params = {"indexName":"message","conditions":[],"sort":"create_time desc"};

        var _search;//查询的值
        var _search_0;//查询的值
        var _search_1;//查询的值

        var _initListTable = function(){
            $('#data-table').myTable({
                columns: [
                    {field: 'checkbox',title: '选择',width:'50px',checkbox:true},
                    {field: 'xh',title: '序号',width:'50px'},
                    {field: 'id',title: 'ID',visible:false},
                    {field: 'tips',title: '提示信息',sortable:true},
                    {field: 'create_time',title: '提示时间',sortable:true},
                    {field: 'read',title: '是否已读',sortable:true,formatter:formatterList},
                    {field: 'opt',title: '操作',width:'130px'}
                ],
                ajax : function (request) {
                    var sort = "create_time desc";
                    if(request.data.sortName){
                        sort = request.data.sortName +" "+request.data.sortOrder;
                    }
                    var con = [];
                    if(_search){
                        con=[
                            {
                                "groupId":"1",
                                "groupType":"should",
                                "field": "tips",
                                "values": [_search],
                                "searchType": 2,
                                "dataType":1,
                            }
                        ];
                    }
                    if(_search_0){
                        con[con.length]=
                            {
                                "groupId":"2",
                                "groupType":"should",
                                "field": "read",
                                "values": ["0"],
                                "searchType": 2,
                                "dataType":2,
                            };
                    }
                    if(_search_1){
                        con[con.length]=
                            {
                                "groupId":"3",
                                "groupType":"should",
                                "field": "read",
                                "values": ["1"],
                                "searchType": 2,
                                "dataType":2,
                            };
                    }

                    params["sort"]=sort;
                    params["conditions"]=con;
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
                                    data[i]['opt'] = "<div class='btn btn-primary btn-outline btn-xs detail' data-id='"+data[i]["id"]+"'>查看</div>&nbsp;";
                                        //"<div class='btn btn-danger btn-outline btn-xs delete' data-id='"+data[i]["id"]+"'  data-fileId='"+data[i]["file_id"]+"'>删除</div>";
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
                            toastrMsg.error("系统错误");
                        }
                    });
                }
            });
        };

        var formatterList = function (f) {
            return f ==="0"?"未读":"已读";
        }

        var _event = function () {
            $("#data-table").on('click','.detail',function () {
                top.contabs.addMenuItem("/view/messages/messages-detail.html?id="+$(this).attr("data-id"),'查看信息');
            });
            $("#data-table").on('click','.delete',function () {
                _delete($(this).attr("data-id"),$(this).attr("data-fileId"));
            });
            $("#search-btn").on('click',function () {
                _search = $("#search-input").val();
                _search_0 = $("#search-input-0").prop("checked");
                _search_1 = $("#search-input-1").prop("checked");
                if(_search && $.trim(_search) !== ""){
                }else {
                    _search=null;
                }
                $('#data-table').bootstrapTable("refresh");
            });
        };

        var _delete = function (id,fileId) {

            swalMsg.msg({
                text:"是否删除注册信息？",
                type:"warning",
                showCancel:true,
                confirm:function (f) {
                    if(f) {

                        $.ajax.proxy({
                            url: "/api/admin/weixin/delete",
                            type: "post",
                            dataType: "json",
                            data:{id: id, fileId:fileId},
                            success: function (d) {
                                console.log(d);
                                if (d.status === 200) {
                                    toastrMsg.success("删除成功");
                                    $('#data-table').bootstrapTable("refresh");
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

        return {
            init:_init
        };
    })();

    reg.init();


})(document, window, jQuery);
