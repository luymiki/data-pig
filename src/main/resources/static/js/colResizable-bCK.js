/**
 * Created by hc.zeng on 2018/4/26.
 */
(function($) {

    var resizing = false; //是否为拖动状态
    var resizable = false; //当时是否可拖动
    var minWidth = 40;	//没列的最小宽度
    var resizeHeader;	//当前拖动的列
    var leftLine;		//左参考线
    var rightLine;		//有参考线
    var dataTable;
    var headerTable;

    var _init = function( tb, options){
        dataTable = $(tb);
        headerTable = $(tb).parent(".fixed-table-body").siblings(".fixed-table-header").find("table");				    //the table object is wrapped
        // var height = $t.height();
        // var width = $t.width();
        // $t.find("thead tr th").each(function (i,e) {
        //     var $th = $(e);
        //     if(i===0){
        //         return;
        //     }
        //     $th.css({"border-left":"1px red solid"});
        //     $th.css({"cursor":"ew-resize"});
        // });

        var hth = dataTable.find("td");
        //监听标题行的mouosemove事件
        hth.on("mousemove",function(e){
            var target = $(e.target);
            if(resizing){
                //当已经在拖动变化列宽时
                onDraging(e);
            }else if(fnIsLeftEdge(e)){
                //靠近左边框时，将当前的处理header
                //设置为左边的一个，这样就相当于是拖动列
                //的右边框，可以只计算该header的右边框参
                //考线的移动,方便处理
                resizeHeader = target.prev();
                //不响应第一列的左边框拖动事件
                if(resizeHeader.length === 0){
                    return;
                }

                //当鼠标停在左边框时，设置当前为可拖动状态
                resizable = true;
                //设置鼠标样式为拖动时的样式
                target.css("cursor", "col-resize");
            }else if(fnIsRightEdge(e)){
                //当鼠标停留在右边框时
                resizeHeader = target;
                resizable = true;
                target.css("cursor", "col-resize");
            }else{
                //超出可拖动的区域，设为不可拖动状态
                resizable = false;
                target.css("cursor", "default");
            }
        });
        //当在拖动列上点击鼠标
        dataTable.on("mousedown","td",function(e){
            onDragingStart(e);
        });
        //当在document上移动鼠标,因为拖动可能超出表格的范围
        $(document).mousemove(function(e){
            onDraging(e);
        });
        //当拖动而释放时候未在标题行的释放也需要结束拖动
        $(document).mouseup(function(e){
            onDragingEnd(e);
        });
    };




    /**
     * 计算当前鼠标位置是不是在可拖动的范围内
     * @param event e目标事件
     * @side boolean true表示在左边边框附近，false表示右边
     * @return boolean true在表格边框附近，false未在
     */
    function _fnIsColEdge(e, side){
        var target = $(e.target);
        var x = e.pageX;
        var offset = target.offset();
        var left = offset.left;
        var right = left + target.outerWidth();
        return side ? x<=left+1 : x >=right-1;
    }
    /**
     * 计算当前鼠标位置是不是在左边框附近
     * @param event e目标事件
     * @return boolean true是， false否
     */
    function fnIsLeftEdge(e){
        return _fnIsColEdge(e, true);
    }
    /**
     * 计算当前鼠标位置是不是在右边框附近
     * @param event e目标事件
     * @return boolean true是， false否
     */
    function fnIsRightEdge(e){
        return _fnIsColEdge(e, false);
    }
    /**
     * 初始化拖动状态
     * @param event e目标事件
     * @return void
     */
    function onDragingStart(e){
        //当前是否为拖动状态
        if(resizable){
            var target = $(e.target);
            //设置当前文本不可选中，否则拖动时会选中文本
            $(document).bind("selectstart", function(){	return false; });
            //创建参考线
            if(!leftLine){
                leftLine = $("<div style=' width:0px;border-style: dashed;border-color: #030303;border-width: 0 1px 0 0px;position:absolute; '></div>");
                leftLine.appendTo("body");
                rightLine = leftLine.clone();
                rightLine.appendTo("body");
            }
            //显示参考线
            leftLine.css({"top":resizeHeader.offset().top, "left": resizeHeader.offset().left, "height": $("table").innerHeight()});
            rightLine.css({"top":resizeHeader.offset().top, "left": e.pageX, "css":"col-resize", "height": $("table").innerHeight()});
            leftLine.show();
            rightLine.show();
            //设置为已经在拖动
            resizing = true;
        }
    }
    /**
     * 列宽拖动中
     * @param event e目标事件
     * @return void
     */
    function onDraging(e){
        //如果已经在拖动
        if(resizing){
            //拖动后的列宽不能小于最小列宽
            if(e.pageX - resizeHeader.offset().left > minWidth){
                rightLine.css("left", e.pageX);
            }
        }
    }
    /**
     * 鼠标释放拖动结束，改变列宽，结束拖动状态
     * @param event e目标事件
     * @return void
     */
    function onDragingEnd(e){
        //如果已经在拖动
        if(resizing){
            resizing = false;
            //隐藏参考线
            rightLine.hide();
            leftLine.hide();
            //设置文本可以选中
            $(document).unbind("selectstart");
            //计算设置新的列宽
            doResize();
        }
    }
    /**
     * 设置新的列宽
     * @param event e目标事件
     * @return void
     */
    function doResize(){
        var dths = dataTable.find("thead tr th");
        var hths = headerTable.find("thead tr th");

        //计算列宽的变化值
        var newWidth = parseInt(rightLine.css("left"), 10) - resizeHeader.offset().left;
        var owidth = resizeHeader.width();

        //设置新列宽
        resizeHeader.width(newWidth);
        var w = dataTable.width();
        if(newWidth<owidth){
             w = dataTable.width() + (newWidth-owidth);
        }else {
             w = dataTable.width() + (newWidth-owidth)-16;
        }
        dataTable.width(w);

        var ww = 0;
        for(var i=0; i<hths.length;i++){
            $(hths[i]).width($(dths[i]).width());
            ww += $(dths[i]).width();
        }
        headerTable.width(ww-16);
        dataTable.width(ww);
    }




    /**
     * The plugin is added to the jQuery library
     * @param {Object} options -  an object that holds some basic customization values
     */
    $.fn.extend({
        colResizable: function(options) {
            var defaults = {
            }
            var options =  $.extend(defaults, options);

            // options.fixed = true;
            // options.overflow = false;
            // switch(options.resizeMode){
            //     case 'flex': options.fixed = false; break;
            //     case 'overflow': options.fixed = false; options.overflow = true; break;
            // }

            return this.each(function() {
                _init( this, options);
            });
        }
    });

})(jQuery);