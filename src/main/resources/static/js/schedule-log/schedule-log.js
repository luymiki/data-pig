/**
 * Created by hc.zeng on 2018/3/21.
 */
(function (e, t, $) {
    "use strict";

    var schedule = (function () {
        var jobId;
        var _init = function init(_data) {
            var params = utils.getURLParams();
            jobId = params["jobId"];
            _initListTable();
            _event();
        };

        var _initListTable = function () {
            $('#schedule-table').myTable({
                pageSize: 50,
                columns: [
                    {field: 'checkbox', title: '选择', width: '50px', checkbox: true},
                    {field: 'xh', title: '序号', width: '50px'},
                    {field: 'opt', title: '操作',class:"text-center", width: '50px'},
                    {field: 'jobId', title: '任务ID', visible: false},
                    // {field: 'type', title: '类型', width: '80px', formatter: formatterType},
                    // {field: 'name', title: '任务名称', width: '200px'},
                    {field: 'createTime', title: '记录时间', width: '150px', sortable: true},
                    {field: 'times', title: '耗时',  width: '150px', sortable: true, formatter: formatterTimes},
                    // {field: 'params', title: '参数'},
                    {field: 'status', title: '任务状态',class:"text-center", sortable: true, formatter: formatterStatus},
                    {field: 'msg', title: '日志信息',class:"msg"}
                ],
                ajax: function (request) {
                    var sort = "log_id desc";
                    if (request.data.sortName) {
                        sort = request.data.sortName + " " + request.data.sortOrder;
                    }
                    $.ajax.proxy({
                        url: "/api/admin/schedule/log/list",
                        type: "post",
                        dataType: "json",
                        data: {
                            "pageNum": request.data.pageNumber,
                            "pageSize": request.data.pageSize,
                            "jobId": jobId,
                            "sort": sort
                        },
                        success: function (msg) {
                            if (msg.status === 200) {
                                // console.log(msg)
                                var data = msg.data.result;
                                var xh = ((request.data.pageNumber - 1) * request.data.pageSize) + 1;
                                for (var i = 0; i < data.length; i++) {
                                    data[i]['xh'] = xh++;
                                    data[i]['opt'] = "<div class='btn btn-danger btn-outline btn-xs delete' data-id='" + data[i]["logId"] + "'>删除</div>";
                                }
                                request.success({
                                    rows: data,
                                    total: msg.data.totalCount
                                });
                                //meta = msg.data.meta["schedule"];
                            } else {
                                request.success({
                                    rows: [],
                                    total: 0
                                });
                            }

                        },
                        error: function () {
                            toastrMsg.error("错误！");
                        }
                    });

                },
                onDblClickCell: function () {

                }
            });
        };

        var SECOND_IN_TIME = 1000;

        var MINUTE_IN_TIME = 60 * SECOND_IN_TIME;

        var HOUR_IN_TIME = 60 * MINUTE_IN_TIME;

        var DAY_IN_TIME = 24 * HOUR_IN_TIME;

        var formatterTimes = function (timeNumber) {
            if (timeNumber == "" || timeNumber == null) {
                return "-";
            }
            var str = "";
            if (timeNumber > DAY_IN_TIME) {
                var t = parseInt(timeNumber / DAY_IN_TIME, 10);
                timeNumber = timeNumber - (t * DAY_IN_TIME);
                str += t + "天";
            }
            if (timeNumber > HOUR_IN_TIME) {
                var t = parseInt(timeNumber / HOUR_IN_TIME, 10);
                timeNumber = timeNumber - (t * HOUR_IN_TIME);
                str += t + "小时";
            }
            if (timeNumber > MINUTE_IN_TIME) {
                var t = parseInt(timeNumber / MINUTE_IN_TIME, 10);
                timeNumber = timeNumber - (t * MINUTE_IN_TIME);
                str += t + "分钟";
            }
            if (timeNumber > SECOND_IN_TIME) {
                var t = parseInt(timeNumber / SECOND_IN_TIME, 10);
                timeNumber = timeNumber - (t * SECOND_IN_TIME);
                str += t + "秒";
            }
            str += timeNumber + "毫秒";
            return str;
        };
        var formatterStatus = function (d) {
            if (d === 1) {
                return "<div class='btn btn-danger btn-outline btn-xs'>失败</div>";
            }
            if (d === 0) {
                return "<div class='btn btn-warning btn-outline btn-xs'>成功</div>";
            }
            return "-";
        };

        var _event = function () {

            $("#schedule-table").on('click', '.delete', function () {
                _delete($(this).attr("data-id"));
            });
            $("#deleteAll").on('click', function () {
                _deleteAll(jobId);
            });

        };

        var _delete = function (id) {
            swalMsg.msg({
                text: "是否删除日志信息？",
                type: "warning",
                showCancel: true,
                confirm: function (f) {
                    if (f) {
                        $.ajax.proxy({
                            url: "/api/admin/schedule/log/delete/" + id,
                            type: "post",
                            dataType: "json",
                            success: function (d) {
                                //console.log(d);
                                if (d.status === 200) {
                                    toastrMsg.success("删除成功");
                                    $('#schedule-table').bootstrapTable("refresh");
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
        var _deleteAll = function (id) {
            swalMsg.msg({
                text: "是否清空日志信息？",
                type: "warning",
                showCancel: true,
                confirm: function (f) {
                    if (f) {
                        $.ajax.proxy({
                            url: "/api/admin/schedule/log/deleteAll/" + id,
                            type: "post",
                            dataType: "json",
                            success: function (d) {
                                //console.log(d);
                                if (d.status === 200) {
                                    toastrMsg.success("删除成功");
                                    $('#schedule-table').bootstrapTable("refresh");
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
            init: _init
        };
    })();

    schedule.init();


})(document, window, jQuery);