/**
 * Created by hc.zeng on 2018/4/26.
 */
    //<!-- 全局js -->
var BASE_CSS = function(){
        document.write('<link rel="shortcut icon" href="/favicon.ico">');
        document.write('<link href="/css/bootstrap.min.css?v=3.3.5" rel="stylesheet">');
        document.write('<link href="/css/font-awesome.min.css?v=4.4.0" rel="stylesheet">');
        document.write('<link href="/css/animate.min.css" rel="stylesheet">');
        document.write('<link href="/css/style.min.css?v=4.0.0" rel="stylesheet">');
        document.write('<link href="/css/plugins/chosen/chosen.css" rel="stylesheet">');
        document.write('<link href="/css/plugins/bootstrap-tagsinput/bootstrap-tagsinput.css" rel="stylesheet">');
        document.write('<link href="/css/plugins/sweetalert/sweetalert.css" rel="stylesheet">');
}
var BASE_JS = function(){
    document.write('<script src="/js/jquery-3.3.1.js"></script>');
    document.write('<script src="/js/bootstrap.min.js?v=3.3.6"></script>');
    // document.write('<script src="/js/storage.js"></script>');
    document.write('<script src="/js/msg.js"></script>');
    document.write('<script src="/js/utils.js"></script>');
    document.write('<script src="/js/ajax.proxy.js"></script>');
    //<!-- 自定义js -->
    document.write('<script src="/js/content.js?v=1.0.0"></script>');
    document.write('<script src="/js/plugins/art-template/template.js"></script>');
    //<!-- Chosen -->
    document.write('<script src="/js/plugins/chosen/chosen.jquery.js"></script>');

    document.write('<script src="/js/plugins/twbsPagination/jquery.twbsPagination.js"></script>');
    document.write('<script src="/js/plugins/bootstrap-menu/BootstrapMenu.js"></script>');
    document.write('<script src="/js/plugins/layer/layer.min.js"></script>');
    document.write('<script src="/js/plugins/bootstrap-tagsinput/bootstrap-tagsinput.min.js"></script>');
    document.write('<script src="/js/plugins/sweetalert/sweetalert.min.js"></script>');
}


var BOOTSTRAP_TABLE_CSS = function(){
    document.write('<link href="/css/plugins/bootstrap-table/bootstrap-table.min.css" rel="stylesheet">');
    // document.write('<link href="/js/plugins/wijmo/styles/wijmo.min.css" rel="stylesheet" />');
}
var BOOTSTRAP_TABLE_JS = function(){
    document.write('<script src="/js/plugins/bootstrap-table/bootstrap-table.js"></script>');
    document.write('<script src="/js/plugins/bootstrap-table/locale/bootstrap-table-zh-CN.min.js"></script>');
    document.write('<script src="/js/colResizable.js"></script>');
    document.write('<script src="/js/plugins/zeroclipboard-2.1.6/ZeroClipboard.js"></script>');
    //document.write('<script src="/js/plugins/clipboard.js-2.0.1/clipboard.js"></script>');
    document.write('<script src="/js/myTable.js"></script>');
    //<!-- Wijmo -->
    // document.write('<script src="/js/plugins/wijmo/controls/wijmo.min.js" type="text/javascript"></script>');
    // document.write('<script src="/js/plugins/wijmo/controls/wijmo.grid.min.js" type="text/javascript"></script>');
    // document.write('<script src="/js/plugins/wijmo/controls/cultures/wijmo.culture.zh.js" type="text/javascript"></script>');
    // document.write('<script src="/js/plugins/wijmo/wijmo.js" type="text/javascript"></script>');

}

var UPDLOAD_CSS = function(){
    document.write('<link href="/css/plugins/webuploader/webuploader.css" rel="stylesheet">');
    document.write('<link href="/css/file/file-img-uploader.css" rel="stylesheet">');
}

var UPDLOAD_FILE_JS = function(){
    //<!-- Web Uploader -->
    document.write('<script src="/js/plugins/webuploader/webuploader.js"></script>');
    document.write('<script src="/js/plugins/md5/browser-md5-file.js"></script>');
    document.write('<script src="/js/file/file-file-uploader.js"></script>');
    document.write('<script src="/js/suspicious/suspicious-list.js"></script>');
}

var UPDLOAD_IMAGE_JS = function(){
    //<!-- Web Uploader -->
    document.write('<script src="/js/plugins/webuploader/webuploader.js"></script>');
    document.write('<script src="/js/file/file-img-uploader.js"></script>');
    document.write('<script src="/js/suspicious/suspicious-list.js"></script>');
}
