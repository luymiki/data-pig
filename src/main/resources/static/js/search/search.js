/**
 * Created by hc.zeng on 2018/3/25.
 */
(function () {
    'use strict';


    var search = (function ($) {
        var val = "";
        var _init = function () {
            _getMeta();
            $("#search-btn").click(_search);
            $("#search").keyup(function (e) {
                if (e.keyCode === 13) {
                    _search();
                }
            })
            $("#search-list ").on("click", ".search-result", function () {
                var index = $(this).attr("data-index");
                switch (index) {
                    case "email_reg": {
                        top.contabs.addMenuItem("/view/email/reg/email-reg-detail.html?id=" + $(this).attr("data-id"), '查看邮件注册信息');
                        break;
                    }
                    case "email_ip": {
                        top.contabs.addMenuItem("/view/email/reg/email-reg-detail.html?id=" + $(this).attr("data-infoid"), '查看邮件登录IP信息');
                        break;
                    }
                    case "kdydxx": {
                        top.contabs.addMenuItem("/view/kdyd/kdyd-xx-list.html?id=" + $(this).attr("data-infoid"), '查看快递运单信息');
                        break;
                    }
                    case "yhzh_khxx": {
                        top.contabs.addMenuItem("/view/yhzh/yhzh-reg-detail.html?id=" + $(this).attr("data-id"), '查看银行开户信息');
                        break;
                    }
                    case "yhzh_jyls": {
                        top.contabs.addMenuItem("/view/yhzh/jyjl/yhzh-jyjl-detail.html?id=" + $(this).attr("data-id"), '查看银行交易流水');
                        break;
                    }
                    case "zfbzzinfo": {
                        top.contabs.addMenuItem("/view/zfb/zzinfo/zfb-zzinfo-detail.html?id=" + $(this).attr("data-id"), '查看支付宝转账明细');
                        break;
                    }
                    case "zfbjyjlinfo": {
                        top.contabs.addMenuItem("/view/zfb/jyjl/zfb-jyjl-detail.html?id=" + $(this).attr("data-id"), '查看支付宝交易记录');
                        break;
                    }
                    case "zfbtxinfo": {
                        top.contabs.addMenuItem("/view/zfb/txinfo/zfb-txinfo-detail.html?id=" + $(this).attr("data-id"), '查看支付宝提现记录');
                        break;
                    }
                    case "zfbzhinfo": {
                        top.contabs.addMenuItem("/view/zfb/zhinfo/zfb-zhinfo-detail.html?id=" + $(this).attr("data-id"), '查看支付宝账户明细');
                        break;
                    }
                    case "zfblogininfo": {
                        top.contabs.addMenuItem("/view/zfb/logininfo/zfb-logininfo-detail.html?id=" + $(this).attr("data-id"), '查看支付宝登录日志');
                        break;
                    }
                    case "zfbreginfo": {
                        top.contabs.addMenuItem("/view/zfb/zfb-reg-detail.html?id=" + $(this).attr("data-id"), '查看支付宝注册信息');
                        break;
                    }
                    case "xndw_wsk": {
                        top.contabs.addMenuItem("/view/xndw/wsk/xndw-wsk-detail.html?id=" + $(this).attr("data-id"), '查看WSK定位信息');
                        break;
                    }
                    case "xndw_sx": {
                        top.contabs.addMenuItem("/view/xndw/sx/xndw-sx-detail.html?id=" + $(this).attr("data-id"), '查看神行定位信息');
                        break;
                    }
                    case "huaduan_list": {
                        top.contabs.addMenuItem("/view/huadan/huadan-liushui-list.html?id=" + $(this).attr("data-infoid"), '查看话单信息');
                        break;
                    }
                    case "huaduan": {
                        top.contabs.addMenuItem("/view/huadan/huadan-liushui-list.html?id=" + $(this).attr("data-id"), '查看话单信息');
                        break;
                    }
                    case "cfttrades": {
                        top.contabs.addMenuItem("/view/cft/liushui/cft-liushui-detail.html?id=" + $(this).attr("data-id"), '查看财付通流水信息');
                        break;
                    }
                    case "cftreginfo": {
                        top.contabs.addMenuItem("/view/cft/cft-reg-detail.html?id=" + $(this).attr("data-id"), '查看财付通信息');
                        break;
                    }
                    case "wxloginip":
                    case "wxqun":
                    case "wxlxr": {
                        top.contabs.addMenuItem("/view/weixin/weixin-reg-detail.html?id=" + $(this).attr("data-infoid"), '查看微信信息');
                        break;
                    }
                    case "wxreginfo": {
                        top.contabs.addMenuItem("/view/weixin/weixin-reg-detail.html?id=" + $(this).attr("data-id"), '查看微信注册信息');
                        break;
                    }
                    case "qqloginip": {
                        top.contabs.addMenuItem("/view/qq/loginip/qq-loginip-detail.html?id=" + $(this).attr("data-id"), '查看QQ登录IP信息');
                        break;
                    }
                    case "qqzone": {
                        top.contabs.addMenuItem("/view/qq/qzone/qq-qzone-detail.html?id=" + $(this).attr("data-id"), '查看QQ空间信息');
                        break;
                    }
                    case "qqreginfo": {
                        top.contabs.addMenuItem("/view/qq/reg/qq-reg-detail.html?id=" + $(this).attr("data-id"), '查看QQ注册信息');
                        break;
                    }

                    case "email": {
                        top.contabs.addMenuItem("/view/file/email/file-email-detail.html?id=" + $(this).attr("data-id"), '查看邮件');
                        break;
                    }
                    case "suspicious": {
                        top.contabs.addMenuItem("/view/suspicious/suspicious-detail.html?id=" + $(this).attr("data-id"), '可疑人员信息');
                        break;
                    }
                    case "attachment": {
                        top.contabs.addMenuItem("/view/file/file-detail.html?id=" + $(this).attr("data-id"), '文件信息');
                        break;
                    }
                }
                //top.contabs.addMenuItem("/view/search/search-detail.html?id="+$(this).attr("data-id")+"&index="+$(this).attr("data-index"),'查询结果');
            });
            $("#index-list").on("click", ".btn-index-tags", function () {
                var indexName = $(this).attr("data-tags");
                if (indexName === "all_index") {
                    $("#search-list").show();
                    $("#pagination_box").show();
                    $("#show-data-list").hide();
                    $('#data-table').bootstrapTable("destroy");
                } else {
                    $("#search-list").hide();
                    $("#pagination_box").hide();
                    $("#show-data-list").show();
                    showDataTable(indexName,val);
                }
            });
        };


        var metaList = [];
        var metaMap = {};

        /**
         * 获取元数据信息
         * @private
         */
        var _getMeta = function () {
            $.ajax.proxy({
                url: "/api/eqa/meta",
                type: "post",
                dataType: "json",
                data: {},
                async: false,
                success: function (d) {
                    if (d.status === 200) {
                        metaList = d.data;
                        for (var i = 0; i < metaList.length; i++) {
                            var mm = metaList[i];
                            metaMap[mm["indexName"]] = mm;
                        }
                        console.log(metaMap);
                    } else {
                        toastrMsg.error("查询元数据失败");
                    }
                },
                error: function () {
                    toastrMsg.error("查询元数据失败");
                }
            });
        };

        var _pageNum = 1;
        var _pageSize = 10;
        var _pagination;
        var _pagination_reload = false;
        var _search = function (page) {
            if (!page) {
                _pagination_reload = true;
            }
            page = page || _pageNum;

            val = $("#search").val();
            if (val !== "") {
                $("#search-list").show();
                $("#pagination_box").show();
                $("#show-data-list").hide();
                $('#data-table').bootstrapTable("destroy");

                var vals = val.split(" ");
                $.ajax.proxy({
                    url: "/api/eqa/fulltext",
                    type: "POST",
                    dataType: "json",
                    data: {"pageNum": page, "pageSize": _pageSize, "keyword": val},
                    async: true,
                    success: function (d) {
                        console.log(d);
                        if (d.status === 200) {
                            if (d.data.data) {
                                var total = d.data.total;
                                var data = d.data.data;
                                var aggs = d.data.indexs;
                                //console.log(aggs)
                                var $searchList = $("#search-list").empty();
                                var searchData = [];
                                for (var i = 0; i < data.length; i++) {
                                    var f = data[i];
                                    var str = "";
                                    for (var k in f) {
                                        if (k === "_index" || k === "path"
                                            || k === "_type" || k === "_score"
                                            || k === "_id" || k === "id"
                                            || k === "create_time" || k === "modify_time"
                                            || k === "type" || k === "file_list"
                                            || k.indexOf("id") > 0) {
                                            continue;
                                        }
                                        str += "&nbsp;" + f[k];
                                        if (str.length > 200) {
                                            str = str.substring(0, 200) + "....";
                                            break;
                                        }
                                    }
                                    for (var kk = 0; kk < vals.length; kk++) {
                                        str = str.replace(vals[kk], "<code>" + vals[kk] + "</code>");
                                    }
                                    str.replace(",", ", ");
                                    var $sr = $('<div class="search-result" style="cursor: pointer;" data-id="' + f["id"] + '" data-index="' + f["_index"] + '"  data-infoid="' + (f["info_id"] || f["cft_id"] || f["hd_id"]|| f["flid"]|| f["reg_id"]) + '"></div>').appendTo($searchList);
                                    $('<div class="search-info" ></div>').appendTo($sr).html(str);
                                    $('<div class="hr-line-dashed"></div>').appendTo($searchList);

                                    //资源列表按钮
                                    var $indexlist = $("#index-list").empty();
                                    $indexlist.append($('<div class="btn btn-info btn-index-tags" data-tags="all_index">全部&nbsp;<span class="badge badge-danger">' + total + '</span></div>'));
                                    for (kk = 0; kk < aggs.length; kk++) {
                                        var ff = aggs[kk];
                                        $indexlist.append($('<div class="btn btn-info btn-index-tags" data-tags="' + ff["key"] + '">' + metaMap[ff["key"]]["indexNameCn"] + '&nbsp;<span class="badge badge-danger">' + ff["doc_count"] + '</span></div>'));
                                    }
                                }
                                if (total > 0) {
                                    $("#search-msg").html('为您找到相关结果' + total + '个： <span class="text-navy">“' + val + '”</span>');
                                    $("#pagination").show();
                                    var totalPages = (total % _pageSize === 0) ? (total / _pageSize) : Math.floor(total / _pageSize) + 1
                                    //后台总页数与可见页数比较如果小于可见页数则可见页数设置为总页数，
                                    var visiblecount = 10;
                                    if (totalPages < visiblecount) {
                                        visiblecount = totalPages;
                                    }
                                    if (_pagination_reload) {
                                        _pagination_reload = false;
                                        $("#pagination").empty();
                                        $("#pagination").unbind("page");
                                        $("#pagination").removeData("twbs-pagination");
                                    }
                                    _pagination = $('#pagination').twbsPagination({
                                        totalPages: totalPages,
                                        visiblePages: visiblecount,
                                        onPageClick: function (event, page_) {
                                            if (_pageNum !== page_) {
                                                _pageNum = page_;
                                                _search(page_);
                                            }
                                        }
                                    });
                                } else {
                                    $("#search-msg").html('为您找到相关结果0个： <span class="text-navy">“' + val + '”</span>');
                                    $("#index-list").empty();
                                    $("#pagination").hide();
                                }

                            }
                        }

                    },
                    error: function (d) {
                        console.log(d);
                    }
                });
            }
        };

        //   $('#txxx-table').bootstrapTable("destroy")

        /**
         * 显示表格
         * @param indexName
         */
        var showDataTable = function (indexName,keyword) {
            $('#data-table').bootstrapTable("destroy");
            var indexMeta = metaMap[indexName];
            var colMeta = indexMeta["eqaMetas"];

            var columns = [
                {field: 'checkbox', title: '选择', width: '50px', checkbox: true},
                {field: 'xh', title: '序号', width: '50px'}
            ];
            for (var i = 0; i < colMeta.length; i++) {
                var col = colMeta[i];
                if(col["fieldCode"] === "id"){
                    continue;
                }
                if(col["fieldCode"] === "name" && indexName==="attachment"){
                    columns[columns.length] = {field: col["fieldCode"], title: col["fieldName"]};
                }else {
                    columns[columns.length] = {field: col["fieldCode"], title: col["fieldName"], sortable: true};
                }
            }
            var sort;
            $('#data-table').myTable({
                copyRow:$("#copyRow"),
                exportXls:$("#exportXls"),
                exportXlsFun:function () {
                    var from = $('<form method="post" action="/api/eqa/exportEXcelFullText" target="_blank"></form>').appendTo('body');
                    $('<input type="text" name="keyword">').val(keyword).appendTo(from);
                    $('<input type="text" name="indexName">').val(indexName).appendTo(from);
                    $('<input type="text" name="sort">').val(sort).appendTo(from);
                    from.submit().remove();
                },
                columns: columns,
                ajax : function (request) {
                    sort = "create_time desc";
                    if(request.data.sortName){
                        sort = request.data.sortName +" "+request.data.sortOrder;
                    }
                    $.ajax.proxy({
                        url: "/api/eqa/fulltext",
                        type: "POST",
                        dataType: "json",
                        async: true,
                        data:{"pageNum":request.data.pageNumber,"pageSize":request.data.pageSize,"keyword":keyword,"indexName":indexName,"sort":sort},
                        success : function (msg) {
                            if(msg.status===200){
                                var data = msg.data.data;
                                var xh =  ((request.data.pageNumber-1)*request.data.pageSize)+1;
                                for(var i= 0;i<data.length;i++){
                                    data[i]['xh'] = xh++;
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

        }

        return {
            init: _init
        };
    })(jQuery);
    search.init();


})();