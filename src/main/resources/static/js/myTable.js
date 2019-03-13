
/**
 * Created by hc.zeng on 2018/4/26.
 */
(function($) {

    var dataTable;
    var copyText;
    var _init = function( tb, options){
        dataTable = $(tb);
        options["colResizable"] = options["colResizable"] === undefined?true : options["colResizable"];
        var height = options["height"] === undefined ? utils.getWidowHeight()-75 : (options["height"] === -1 ? undefined:options["height"]);
        var pagination = options["pagination"];
        var sidePagination = options["sidePagination"];

        dataTable.bootstrapTable("destroy");
        dataTable.bootstrapTable({
            pagination:pagination=== undefined ? (sidePagination=== undefined?true:false):pagination,
            pageSize:options["pageSize"]||10,
            height: height,
            pageList: [10, 50, 100],  //记录数可选列表
            queryParamsType:'',
            sidePagination: sidePagination ||'server',
            columns:options["columns"],
            ajax : options["ajax"],
            data : options["data"],
            onPostBody:function(){
                if (options["colResizable"]===true ) {
                    dataTable.colResizable({});
                }
                // dataTable.colResizable({});
                if (options["onPostBody"]) {
                    options["onPostBody"]();
                }
            },
            onCheck:function (row) {
                _copyRow(options["columns"]);
                if (options["onCheck"]) {
                    options["onCheck"](row);
                }
            },
            onCheckAll:function (rows) {
                _copyRow(options["columns"]);
                if (options["onCheckAll"]) {
                    options["onCheckAll"](rows);
                }
            },
            onDblClickCell:function(field, value, row, $element) {
                if (options["onDblClickCell"]) {
                    options["onDblClickCell"](field, value, row, $element);
                }else{
                    //console.log(row);
                    var val = row[field];
                    if(val instanceof Array){
                        val =  val.join("&emsp;");
                    }
                    if(val!==""){
                        layer.open({
                            // time: 2000, //不自动关闭
                            type: 1,
                            skin: 'layui-layer-rim', //加上边框
                            area: ['420px', '340px'], //宽高
                            content: "<div style='padding: 5px;'>"+val+"</div>"
                        });
                    }
                }

            }
        });
        if(options["comment"] && options["comment"] !== undefined){
            var comment = options["comment"];
            comment.on('click',commentFunction);
        }
        if(options["copyRow"] && options["copyRow"] !== undefined){
            var copyRow = options["copyRow"];
            _initCopyRow(copyRow);
        }
        if(options["exportXls"] && options["exportXls"] !== undefined && options["exportXlsFun"] && options["exportXlsFun"] !== undefined){
            var exportXls = options["exportXls"];
            exportXls.click(options["exportXlsFun"]);
        }

    };


    /**
     * 初始化复制需要的元素和插件
     * @private
     */
    var _initCopyRow = function (copyRow) {
        var id = new Date().getTime();
        copyText = "copyText-"+id;
        var copy = $("#"+copyText);
        if (copy.length === 0) {
            copy = $("<textarea id='"+copyText+"' style='display: none;'></textarea>").appendTo($("body"));
        }
        copyRow.attr("data-clipboard-target",copyText);
        var clip = new ZeroClipboard(copyRow);
        clip.on('ready', function(){
            this.on('aftercopy', function(event){
                // alert( event.data['text/plain'])
                toastrMsg.info("已经复制剪贴板");
            });
        });
    }

    /**
     * 复制一行的数据到剪切板
     * @param columns
     * @param row
     * @private
     */
    var _copyRow = function (columns, meta) {
        if(copyText){
            var rows = dataTable.bootstrapTable('getSelections');
            //console.log(rows)
            var title = "";
            var sb = "";
            var incloude = {};
            if (rows && rows.length > 0) {
                for (var i = 0; i < columns.length; i++) {
                    var c = columns[i];
                    var ft = c["title"];
                    if (c["field"] === "checkbox" || c["field"] === 'opt') {
                        continue;
                    }
                    title +=  ft+"\t";
                }
                for (var j = 0; j < rows.length; j++) {
                    sb += "\n";
                    var row = rows[j];
                    for (var i = 0; i < columns.length; i++) {
                        var col = columns[i];
                        var field = col["field"];
                        incloude[field] = field;
                        if (field.indexOf("_") === 0 || field === "checkbox" || field === 'opt') {
                            continue;
                        }
                        var dd = row[field]||"";
                        if(dd instanceof Array){
                            dd =  dd.join(",");
                        }
                        sb += dd+"\t";
                        // console.log(sb);
                    }

                    // for (var f in row) {
                    //     if (f.indexOf("_") === 0 || f.indexOf("_na") === f.length - 3 || f === "checkbox" || f === 'opt' || incloude[f]) {
                    //         continue;
                    //     }
                    //     var d = row[f]||"";
                    //     sb += "\t" + d;
                    //     if(j===0){
                    //         var m="";
                    //         for (var k = 0; k < meta.length  ; k++) {
                    //             if(meta[k]["fieldCode"] === f){
                    //                 m = meta[k]["fieldName"];
                    //                 console.log(f+"  "+m)
                    //                 break;
                    //             }
                    //         }
                    //         m = m ||"";
                    //         title += "\t" + m;
                    //     }
                    // }
                }
            }

            $("#"+copyText).html(title+sb);
        }

    }

    /**
     * 批注事件触发
     */
    var commentFunction = function () {
        var $this = $(this);
        var rows = dataTable.bootstrapTable('getSelections');
        //console.log(rows)
        if(rows.length===0){
            _msg("请选择行数据");
            return false;
        }

        var commentModal = $("#comment-modal");
        if(commentModal.length===0){
            var html = '<div class="modal inmodal" id="comment-modal" tabindex="-1" role="dialog"  aria-hidden="true">';
            html += '       <div class="modal-dialog">';
            html += '           <div class="modal-content animated fadeIn">';
            html += '               <div class="modal-header" style="padding: 15px;">';
            html += '                   <strong>新增批注</strong>';
            html += '                   <button type="button" class="close" data-dismiss="modal"><span aria-hidden="true">&times;</span><span class="sr-only">Close</span></button>';
            html += '               </div>';
            html += '               <div class="modal-body">';
            html += '                   <form class="form-horizontal m-t" id="signupForm">';
            html += '                       <input id="indexName" name="indexName" class="form-control" type="hidden">';
            html += '                       <input id="ids" name="ids" class="form-control" type="hidden">';
            html += '                       <div class="form-group">';
            html += '                           <label class="col-sm-3 control-label">标签：</label>';
            html += '                           <div class="col-sm-7">';
            html += '                               <input id="tags" name="tags" class="form-control" type="text"  data-role="tagsinput">';
            html += '                           </div>';
            html += '                       </div>';
            html += '                       <div class="form-group">';
            html += '                           <label class="col-sm-3 control-label">批注内容：</label>';
            html += '                           <div class="col-sm-7">';
            html += '                               <textarea id="comment" name="comment" class="form-control" rows="4"></textarea>';
            html += '                           </div>';
            html += '                       </div>';
            html += '                   </form>';
            html += '               </div>';
            html += '               <div class="modal-footer">';
            html += '                   <button type="button" class="btn btn-white" data-dismiss="modal">关闭</button>';
            html += '                   <button type="button" class="btn btn-primary" id="comment-submit" >保存</button>';
            html += '               </div>';
            html += '           </div>';
            html += '       </div>';
            html += '</div>';
            $("body").append($(html));
            commentModal = $("#comment-modal");
            $("#tags").tagsinput();
            $("#comment-submit").click(function () {
                var indexName = $.trim($("#indexName").val());
                var ids = $.trim($("#ids").val());
                var tags = $.trim($("#tags").val());
                var comment = $.trim($("#comment").val());
                if(indexName=== ''){
                    _msg("数据源为空")
                    return false;
                }
                if(ids=== ''){
                    _msg("数据ID为空")
                    return false;
                }
                if(comment=== ''){
                    _msg("批注内容为空")
                    return false;
                }
                // console.log(indexName);
                // console.log(ids);
                // console.log(tags);
                // console.log(comment);
                var data = {"indexName":indexName,"tags":tags,"source":ids,"comment":comment};
                $.ajax.proxy({
                    url:"/api/admin/comment/save",
                    type:"post",
                    dataType:"json",
                    data:data,
                    async:true,
                    success:function (d) {

                        if(d.status===200){
                            _msg("保存成功");
                            commentModal.modal("hide");
                        }
                        else {
                            console.log(d);
                            _msg("保存失败");
                            commentModal.modal("hide");
                        }

                    },
                    error:function (d) {
                        console.log(d);
                        _msg("保存失败");
                        commentModal.modal("hide");
                    }
                });
            });
        }else {
            $("#indexName").val("");
            $("#ids").val("");
            $("#tags").val("");
            $("#comment").val("");
        }
        var indexName ;
        var ids =[];
        for(var i=0 ; i < rows.length ;i++){
            var row = rows[i];
            indexName = row["_index"];
            var id = row["id"];
            ids[ids.length] = id;
        }

        $("#indexName").val(indexName);
        $("#ids").val(ids);
        commentModal.modal("show");
    }

    var _msg = function (msg) {
        if(toastrMsg){
            toastrMsg.info(msg);
        }else {
            alert(msg);
        }
    }

    /**
     * The plugin is added to the jQuery library
     * @param {Object} options -  an object that holds some basic customization values
     */
    $.fn.extend({
        myTable: function(option) {
            var args = Array.prototype.slice.call(arguments, 1);
            var defaults = {};
            var $this = $(this),
                data = $this.data('bootstrap.table');
            var options =  $.extend(defaults, option);

            return this.each(function() {
                if (typeof option === 'string') {
                    data[option].apply(data, args);
                }else{
                    _init( this, options);
                }

            });
        }
    });

    /**
     * 批注
     */
    $("body").on('click',"#pizhu,.pizhu",function () {
        var table = $(this).attr("data-table");
        $("#"+table).myTable("comment");
    });

    // /**
    //  * 批注
    //  */
    // $("body").on('click',"#pizhu",function () {
    //     var table = $(this).attr("data-table");
    //     $("#"+table).myTable("comment");
    // });

})(jQuery);
