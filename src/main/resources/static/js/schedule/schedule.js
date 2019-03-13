/**
 * Created by hc.zeng on 2018/3/21.
 */
(function (e, t, $) {
    "use strict";

    var schedule = (function () {
        var meta;
        var _init = function init(_data) {
            _initListTable();
            _validator();
            _event();
        };

        var params = {"indexName": "schedule", "conditions": [], "sort": "modify_time desc"};
        var _initListTable = function () {

            var columns = [
                {field: 'checkbox', column: "", title: '选择', width: '50px', checkbox: true},
                {field: 'xh', column: "", title: '序号',class:"text-center", width: '50px'},
                {field: 'jobId', column: "job_id", title: '任务ID', visible: false},
                {field: 'type', column: "type", title: '类型', width: '100px', formatter: formatterType},
                {field: 'name', column: "name", title: '任务名称'},
                // {field: 'executor', title: '执行器名称', sortable: true},
                // {field: 'beanName', column: "bean_name", title: 'Bean', sortable: true},
                {field: 'increment', column: "increment", title: '抽取方式',class:"text-center", formatter: formatterIncrement},
                {field: 'incrementColumn', column: "increment_column",class:"text-center", title: '增量字段'},
                {field: 'incrementTime', column: "increment_time",class:"text-center", title: '增量时间'},
                {field: 'cronExpression', column: "cron_expression",class:"text-center", title: 'cron表达式'},
                {field: 'status', column: "status", title: '任务状态',class:"text-center", sortable: true, formatter: formatterStatus},
                {field: 'runStatus', column: "run_status",title: '运行状态',class:"text-center",sortable: true,formatter: formatterRunStatus},
                // {field: 'imei', title: 'IMEI', formatter: formatterList},
                {field: 'createTime', column: "create_time", title: '创建时间', sortable: true},
                {field: 'startTime', column: "start_tTime", title: '开始执行时间', sortable: true, formatter: formatterDate},
                {field: 'prevFireTime',column: "prev_fire_time",title: '上一次执行时间',sortable: true,formatter: formatterDate},
                {field: 'nextFireTime',column: "next_fire_time",title: '下一次执行时间',sortable: true,formatter: formatterDate},
                {field: 'opt', column: "", title: '操作',class:"text-center", width: '150px'}
            ];

            $('#schedule-table').myTable({
                copyRow: $("#copyRow"),
                exportXls: $("#exportXls"),
                exportXlsFun: function () {
                    // params["conditions"] = [kyr];
                    //console.log(JSON.stringify(params));
                    var from = $('<form method="post" action="/api/eqa/exportEXcel" target="_blank"></form>').appendTo('body');
                    $('<input type="text" name="paramsStr">').val(JSON.stringify(params)).appendTo(from);
                    from.submit().remove();
                },
                columns: columns,
                ajax: function (request) {
                    var sort = "create_Time desc";
                    if (request.data.sortName) {
                        var sortName = request.data.sortName;
                        for (var i = 0; i < columns.length; i++) {
                            var col = columns[i];
                            if(col["field"]==sortName){
                                sortName = col["column"];
                                break;
                            }
                        }
                        sort = sortName + " " + request.data.sortOrder;
                    }
                    $.ajax.proxy({
                        url: "/api/admin/schedule/list",
                        type: "post",
                        dataType: "json",
                        data: {
                            "pageNum": request.data.pageNumber,
                            "pageSize": request.data.pageSize,
                            "paramsStr": JSON.stringify(params),
                            "sort": sort
                        },
                        success: function (msg) {
                            if (msg.status === 200) {
                                // console.log(msg)
                                var data = msg.data.result;
                                var xh = ((request.data.pageNumber - 1) * request.data.pageSize) + 1;
                                for (var i = 0; i < data.length; i++) {
                                    data[i]['xh'] = xh++;
                                    data[i]['opt'] =
                                        "<div class='btn btn-primary btn-outline btn-xs start' data-id='" + data[i]["jobId"] + "'>调度</div>&nbsp;" +
                                        "<div class='btn btn-primary btn-outline btn-xs run' data-id='" + data[i]["jobId"] + "'>运行</div>&nbsp;<br>" +
                                        // "<div class='btn btn-primary btn-outline btn-xs pause' data-id='" + data[i]["jobId"] + "'>暂停</div>&nbsp;" +
                                        "<div class='btn btn-primary btn-outline btn-xs stop' data-id='" + data[i]["jobId"] + "'>停止</div>&nbsp;" +
                                        "<div class='btn btn-warning btn-outline btn-xs logs' data-id='" + data[i]["jobId"] + "' data-name='" + data[i]["name"] + "'>日志</div>&nbsp;<br>" +
                                        "<div class='btn btn-danger btn-outline btn-xs update' data-id='" + data[i]["jobId"] + "'>修改</div>&nbsp;" +
                                        "<div class='btn btn-danger btn-outline btn-xs delete' data-id='" + data[i]["jobId"] + "'>删除</div>&nbsp;<br>";
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
            // window.setInterval(function () {
            //     $('#schedule-table').bootstrapTable("refresh");
            // }, 30000);

        };
        var formatterIncrement = function (d) {
            if (d && d === 1) {
                return "<div class='btn btn-info btn-xs'>增量</div>";
            }else if (d === 0) {
                return "<div class='btn btn-warning btn-xs'>全量</div>";
            }
            return "-";
        };
        var formatterType = function (d) {
            if (d && d === 2) {
                return "数据抽取";
            }
            return "任务调度";
        };
        var putZore = function (d) {
            if (d > 9) {
                return d;
            }
            return "0" + d;
        }
        var formatterDate = function (d) {
            if (d > 1540000000000) {
                var date = new Date(d);
                return date.getFullYear() + "-" + putZore(date.getMonth() + 1) + "-" + putZore(date.getDate()) + " " + putZore(date.getHours()) + ":" + putZore(date.getMinutes()) + ":" + putZore(date.getSeconds());
            }
            return "--";
        };
        var formatterStatus = function (d) {
            if (d === 1) {
                return "<div class='btn btn-danger btn-outline btn-xs'>已停止</div>";
            }
            return "<div class='btn btn-warning btn-outline btn-xs'>调度中</div>";
        };
        var formatterRunStatus = function (d) {
            if (d === 1) {
                return "<div class='btn btn-danger btn-outline btn-xs'>运行中</div>";
            }
            if (d === 2) {
                return "<div class='btn btn-primary btn-outline btn-xs'>已结束</div>";
            }
            if (d === 3) {
                return "<div class='btn btn-warning btn-outline btn-xs'>已暂停</div>";
            }
            if (d === 4) {
                return "<div class='btn btn-danger btn-outline btn-xs'>有异常</div>";
            }
            return "<div class='btn btn-warning btn-outline btn-xs'>未运行</div>";
        };

        var _event = function () {
            $("#incrementTime").datetimepicker({format:"YYYY-MM-DD HH:mm:ss"});
            $("#addBtn").on('click', function () {
                $("#signupForm").find("input").each(function (i, o) {
                    $(o).val("");
                });
                $("#signupForm").find("textarea").each(function (i, o) {
                    $(o).val("");
                });
                $("#type").val("1");
                $(".type-1").show();
                $(".type-2").hide();
                $("#status-0").val("0").prop("checked", true);
                $("#status-1").val("1");
                $("#increment-0").val("0").prop("checked", true);
                $("#increment-1").val("1");
                $("#incrementAll").val("1");
                $("#cronExpression").val("33 1/30 * * * ?");
                $('#addModal').modal("show");
            });


            $("#schedule-table").on('click', '.update', function () {
                _get($(this).attr("data-id"));
            });
            $("#schedule-table").on('click', '.delete', function () {
                _delete($(this).attr("data-id"));
            });
            $("#schedule-table").on('click', '.run', function () {
                _run($(this).attr("data-id"));
            });
            $("#schedule-table").on('click', '.start', function () {
                _start($(this).attr("data-id"));
            });
            $("#schedule-table").on('click', '.pause', function () {
                _pause($(this).attr("data-id"));
            });
            $("#schedule-table").on('click', '.stop', function () {
                _stop($(this).attr("data-id"));
            });
            $("#schedule-table").on('click', '.logs', function () {
                var jobId = $(this).attr("data-id");
                var name = $(this).attr("data-name");
                top.contabs.addMenuItem("/view/schedule-log/schedule-log.html?jobId=" + jobId, name + '的日志');
            });

            var template_type_1 = "";
            var template_type_2 = "{\n" +
                "   \"plugin\":{\n" +
                "       \"reader\":{\n" +
                "           \"name\":\"com.anluy.datapig.plugin.database.oracle.OracleReader\",\n" +
                "           \"url\":\"jdbc:oracle:thin:@68.64.9.188:1521:jzdb\",\n" +
                "           \"username\":\"jzck\",\n" +
                "           \"password\":\"jzck\",\n" +
                "           \"sql\":\"select * from qx_ckfkzh_3 t\"\n" +
                "       },\n" +
                "       \"writer\":{\n" +
                "           \"name\":\"com.anluy.datapig.plugin.database.oracle.OracleWriter\",\n" +
                "           \"url\":\"jdbc:oracle:thin:@68.64.9.188:1521:jzdb\",\n" +
                "           \"username\":\"jzck\",\n" +
                "           \"password\":\"jzck\",\n" +
                "           \"tableName\":\"QX_CKFKZH_TEST\",\n" +
                "           \"batchSize\":\"500\"\n" +
                "       }\n" +
                "   }\n" +
                "}";


            //任务类型变动
            $('#type').on('change', function () {
                var $this = $(this);
                var val = $this.val();
                var jobId = $("#jobId").val();
                if (val === "2") {
                    $("#executor").val("DataExchangerExecutor");
                    $(".type-1").hide();
                    $(".type-2").show();
                    if (jobId == "") {
                        $(".type-1").find("input").val("");
                        $("#params").val(template_type_2);
                    }
                } else {
                    $("#executor").val("");
                    $(".type-1").show();
                    $(".type-2").hide();
                    if (jobId == "") {
                        $("#params").val(template_type_1);
                    }
                }
            });
        };

        var _validator = function () {
            var icon = "<i class='fa fa-times-circle'></i> ";
            var validate;
            $.validator.addMethod("incrementColumn", function (val, e, param) {
                var increment = $("input[name='increment']:checked").val();
                if (increment === "1") {
                    if (val == "") {
                        return false;
                    }
                }
                return true;
            }, "必填");
            $("#submit").click(function () {
                var type = $("#type").val();
                if (type === "2") {
                    if (validate) {
                        $("#signupForm input").rules("remove");
                        $("#signupForm textarea").rules("remove");
                    }
                    validate = $("#signupForm").validate({
                        rules: {
                            name: {
                                required: true,
                                minlength: 2,
                                maxlength: 100
                            },
                            cronExpression: {
                                required: true,
                                minlength: 5
                            },
                            incrementColumn: {
                                incrementColumn: true
                            },
                            params: {
                                required: true,
                                minlength: 100
                            }
                        },
                        messages: {
                            name: {
                                required: icon + "请输入任务名称",
                                minlength: icon + "任务名称必须2个字符以上",
                                maxlength: icon + "任务名称必须100个字符以内"
                            },
                            cronExpression: {
                                required: icon + "请输入调度串",
                                minlength: icon + "调度串必须5个字符以上"
                            },
                            params: {
                                required: icon + "请输入任务参数",
                                minlength: icon + "任务参数必须100个字符以上"
                            }
                        },
                        submitHandler: _submitHandler
                    });
                } else {
                    if (validate) {
                        $("#signupForm input").rules("remove");
                        $("#signupForm textarea").rules("remove");
                    }
                    validate = $("#signupForm").validate({
                        rules: {
                            name: {
                                required: true,
                                minlength: 2,
                                maxlength: 100
                            },
                            cronExpression: {
                                required: true,
                                minlength: 5
                            },
                            beanName: {
                                required: true,
                                minlength: 4
                            }
                        },
                        messages: {
                            name: {
                                required: icon + "请输入任务名称",
                                minlength: icon + "任务名称必须2个字符以上",
                                maxlength: icon + "任务名称必须100个字符以内"
                            },
                            cronExpression: {
                                required: icon + "请输入调度串",
                                minlength: icon + "调度串必须5个字符以上"
                            },
                            beanName: {
                                required: icon + "请输入任务类Bean名称",
                                minlength: icon + "任务类Bean名称必须4个字符以上"
                            }
                        },
                        submitHandler: _submitHandler
                    });
                }

                $("#signupForm").submit();
            });
        };

        var _submitHandler = function (form) {
            var data = {};
            $(form).find("input").each(function (i, o) {
                var _name = $(o).attr("name");
                var _value = $(o).val();
                data[_name] = _value;
            });
            $(form).find("textarea").each(function (i, o) {
                var _name = $(o).attr("name");
                var _value = $(o).val();
                data[_name] = _value;
            });

            var status = $("input[name='status']:checked").val();
            data["status"] = status;

            var increment = $("input[name='increment']:checked").val();
            data["increment"] = increment;

            var incrementAll = $("input[name='incrementAll']:checked").val();
            if (incrementAll) {
                data["incrementAll"] = 1;
            } else {
                data["incrementAll"] = 0;
            }

            var type = $("#type").val();
            data["type"] = type;
            if (type === "2") {
                data["beanName"] = null;
            }else if (type === "1") {
                data["increment"] = null;
                data["incrementAll"] = null;
                data["incrementColumn"] = null;
                data["incrementTime"] = null;
            }
            //console.log(data)
            _save(data);
        }

        var _save = function (data) {
            $.ajax.proxy({
                url: "/api/admin/schedule/save",
                type: "post",
                dataType: "json",
                contentType: "application/json;charset=UTF-8",
                data: JSON.stringify(data),
                async: true,
                success: function (d) {

                    if (d.status === 200) {
                        if (data["id"]) {
                            toastrMsg.success("修改成功");
                        } else {
                            toastrMsg.success("保存成功");
                        }
                        $('#addModal').modal('hide');
                        $('#schedule-table').bootstrapTable("refresh");
                    }
                    else {
                        console.log(d);
                        toastrMsg.error("保存失败");
                        $('#addModal').modal('hide');
                    }

                },
                error: function (d) {
                    console.log(d);
                    toastrMsg.error("保存失败");
                    $('#addModal').modal('hide');
                }
            });
            $(".import-btn").hide();
        };

        var _get = function (id) {
            $.ajax.proxy({
                url: "/api/admin/schedule/info/" + id,
                type: "post",
                dataType: "json",
                success: function (d) {
                    //console.log(d);
                    if (d.status === 200) {
                        var data = d.data;
                        for (var key in data) {
                            var value = data[key];
                            $("#" + key).val(value);
                        }
                        if (data.type === 2) {
                            $(".type-1").hide();
                            $(".type-2").show();
                        } else {
                            $(".type-1").show();
                            $(".type-2").hide();
                        }
                        if (data.status === 0) {
                            $("#status-0").prop("checked", true);
                        } else {
                            $("#status-1").prop("checked", true);
                        }
                        if (data.increment === 0) {
                            $("#increment-0").prop("checked", true);
                        } else {
                            $("#increment-1").prop("checked", true);
                        }
                        if (data.incrementAll === 1) {
                            $("#incrementAll").prop("checked", true);
                        } else {
                            $("#incrementAll").prop("checked", false);
                        }
                        $('#addModal').modal("show");
                    } else {
                        toastrMsg.error("查询失败");
                    }
                },
                error: function () {
                    toastrMsg.error("查询失败");
                }
            });
        };
        var _start = function (id) {
            swalMsg.msg({
                text: "是否调度任务？",
                type: "warning",
                showCancel: true,
                confirm: function (f) {
                    if (f) {
                        $.ajax.proxy({
                            url: "/api/admin/schedule/start",
                            type: "post",
                            dataType: "json",
                            data: {"jobId": id},
                            success: function (d) {
                                //console.log(d);
                                if (d.status === 200) {
                                    toastrMsg.success("调度成功");
                                    $('#schedule-table').bootstrapTable("refresh");
                                } else {
                                    toastrMsg.error("调度失败");
                                }
                            },
                            error: function () {
                                toastrMsg.error("调度失败");
                            }
                        });
                    }
                }
            });


        }
        var _run = function (id) {
            swalMsg.msg({
                text: "是否运行任务？",
                type: "warning",
                showCancel: true,
                confirm: function (f) {
                    if (f) {
                        $.ajax.proxy({
                            url: "/api/admin/schedule/run",
                            type: "post",
                            dataType: "json",
                            data: {"jobId": id},
                            success: function (d) {
                                //console.log(d);
                                if (d.status === 200) {
                                    toastrMsg.success("运行成功");
                                    $('#schedule-table').bootstrapTable("refresh");
                                } else {
                                    toastrMsg.error("运行失败");
                                }
                            },
                            error: function () {
                                toastrMsg.error("运行失败");
                            }
                        });
                    }
                }
            });


        }
        var _pause = function (id) {
            swalMsg.msg({
                text: "是否暂停任务？",
                type: "warning",
                showCancel: true,
                confirm: function (f) {
                    if (f) {
                        $.ajax.proxy({
                            url: "/api/admin/schedule/pause",
                            type: "post",
                            dataType: "json",
                            data: {"jobId": id},
                            success: function (d) {
                                //console.log(d);
                                if (d.status === 200) {
                                    toastrMsg.success("暂停成功");
                                    $('#schedule-table').bootstrapTable("refresh");
                                } else {
                                    toastrMsg.error("暂停失败");
                                }
                            },
                            error: function () {
                                toastrMsg.error("暂停失败");
                            }
                        });
                    }
                }
            });


        }
        var _stop = function (id) {
            swalMsg.msg({
                text: "是否停止调度任务？",
                type: "warning",
                showCancel: true,
                confirm: function (f) {
                    if (f) {
                        $.ajax.proxy({
                            url: "/api/admin/schedule/stop",
                            type: "post",
                            dataType: "json",
                            data: {"jobId": id},
                            success: function (d) {
                                //console.log(d);
                                if (d.status === 200) {
                                    toastrMsg.success("停止调度成功");
                                    $('#schedule-table').bootstrapTable("refresh");
                                } else {
                                    toastrMsg.error("停止调度失败");
                                }
                            },
                            error: function () {
                                toastrMsg.error("停止调度失败");
                            }
                        });
                    }
                }
            });


        }
        var _delete = function (id) {
            swalMsg.msg({
                text: "是否删除任务信息？",
                type: "warning",
                showCancel: true,
                confirm: function (f) {
                    if (f) {

                        $.ajax.proxy({
                            url: "/api/admin/schedule/delete",
                            type: "post",
                            dataType: "json",
                            data: {jobId: id},
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