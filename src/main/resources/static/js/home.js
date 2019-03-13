/**
 * Created by hc.zeng on 2018/4/24.
 */
(function ($) {
    "use strict";
    var token = sessionStorage.getItem("token");
    //验证是否登录
    // $.ajax({
    //     url:"/api/admin/authorization/validate",
    //     type:"post",
    //     dataType:"json",
    //     data:{"authorization":token},
    //     async:false,
    //     success:function (d) {
    //         console.log(d);
    //         if(d.status!==200){
    //             getStorage().clear();
    //             window.top.location.href="/index.html";
    //         }
    //     },
    //     error:function (d) {
    //         getStorage().clear();
    //         window.top.location.href="/index.html";
    //     }
    // });


    var home = (function () {
        var _init = function () {

            $(".logout").click(_logout);
            //notice();
            //message();
        };

        var _logout = function () {

            $.ajax({
                url:"/api/admin/authorization/logout",
                type:"post",
                dataType:"json",
                data:{"authorization":token},
                async:false,
                success:function (d) {
                    console.log(d);
                    if(d.status===200){
                        getStorage().clear();
                        window.top.location.href="/index.html";
                    }else {
                        toastrMsg.error(d.message);
                    }
                },
                error:function (d) {
                    toastrMsg.error("注销失败");
                }
            });


            return false;
        };

        /**
         * websocket通知
         */
        var notice = function () {
            var socket = new SockJS("/api/admin/websocketServer");
            var stomp = Stomp.over(socket);
                stomp.connect({},function connectCallback() {
                    console.log("websocket连接成功");
                    //订阅 用户名为 admin 的消息
                    stomp.subscribe("/user/admin/msg",function (response) {
                        var v = JSON.parse(response.body);
                        toastrMsg.info("新的消息："+v["tips"],null,60000);
                    });
                },function errorCallback() {
                    toastrMsg.error("websocket连接失败");
                });

        }
        /**
         * 查询未读消息个数message-info
         */
        var message = function () {
            $("#message-info").click(function () {
                top.contabs.addMenuItem("/view/messages/messages-list.html",'系统提示');
            });
            $.ajax.proxy({
                url:"/api/admin/messages/count",
                type:"post",
                dataType:"json",
                data:{},
                async:true,
                success : function (d) {
                    console.log(d);
                    if(d.status===200){
                      $("#message-count").html(d.data.total);
                    }
                },
                error:function(){
                    toastrMsg.error("查询未读消息失败");
                }
            });
        }

        return{
            init:_init
        };
    })();

    home.init();

})(jQuery);