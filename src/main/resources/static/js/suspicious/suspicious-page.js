/**
 * Created by hc.zeng on 2018/3/21.
 */
(function (e, t, $) {
    "use strict";
    var $tabs = $("#data-tabs").empty();
    var $contents = $("#data-content").empty();
    var id;
    var type;
    var code;
    var suspicious = (function () {
        var meta;
        var _init = function init(_data) {
            var params = utils.getURLParams();
            id = params["id"];
            type = params["type"];
            code = params["code"];
            var source=[];
            switch (type){
                case "qq":
                    //qqreginfo,email,qqzone,qqloginip,wxreginfo
                    source=[
                        {title:"QQ注册信息",type:"qqreginfo",url:"/view/qq/reg/qq-reg-list.html"},
                        {title:"QQ登录记录",type:"qqloginip",url:"/view/qq/loginip/qq-loginip-list.html"},
                        {title:"微信注册信息",type:"wxreginfo",url:"/view/weixin/weixin-reg-list.html"},
                        {title:"电子邮件",type:"email",url:"/view/file/email/file-email-list.html"},
                        {title:"QQ空间照片",type:"qqzone",url:"/view/qq/qzone/qq-qzone-list.html"}
                    ];
                    break;
                case "weixin":
                    //wxreginfo：weixin
                    source=[
                        {title:"微信注册信息",type:"wxreginfo",url:"/view/weixin/weixin-reg-list.html"}
                    ];
                    break;
                case "dh":
                    //qqreginfo：dh;wxreginfo:dh;cftreginfo:dh ;huaduan:zjhm;
                    source=[
                        {title:"QQ注册信息",type:"qqreginfo",url:"/view/qq/reg/qq-reg-list.html"},
                        {title:"微信注册信息",type:"wxreginfo",url:"/view/weixin/weixin-reg-list.html"},
                        {title:"财付通注册信息",type:"cftreginfo",url:"/view/cft/cft-reg-list.html"},
                        {title:"话单信息",type:"huaduan",url:"/view/huadan/huadan-list.html"}
                    ];
                    break;
                case "cft":
                    //cftreginfo：zh
                    source=[
                        {title:"财付通注册信息",type:"cftreginfo",url:"/view/cft/cft-reg-list.html"}
                    ];
                    break;
                case "yhzh":
                    //cftreginfo：yhzh_list
                    source=[
                        {title:"财付通注册信息",type:"cftreginfo",url:"/view/cft/cft-reg-list.html"}
                    ];
                    break;
                case "email":
                    //email：to_address; qqreginfo:email;wxreginfo:email
                    source=[
                        {title:"电子邮件",type:"email",url:"/view/file/email/file-email-list.html"},
                        {title:"QQ注册信息",type:"qqreginfo",url:"/view/qq/reg/qq-reg-list.html"},
                        {title:"微信注册信息",type:"wxreginfo",url:"/view/weixin/weixin-reg-list.html"}
                    ];
                    break;
                case "ip":
                    //wxloginip：ip[]; qqloginip:ip_list[]
                    source=[
                        {title:"QQ登录信息",type:"wxloginip",url:"/view/qq/loginip/qq-loginip-list.html"},
                        {title:"微信注册信息",type:"wxreginfo",url:"/view/weixin/weixin-reg-list.html"}
                    ];
                    break;
            }

            _createTabs(source,type);
        };

        var _createTabs = function (source,type) {
            var html = "";
            for(var i=0 ; i< source.length ;i++){
                var sour = source[i];
                $('<li class="'+(i===0?('active'):(' '))+'" ><a data-toggle="tab" href="#tab-'+i+'"> '+sour["title"]+'</a></li>').appendTo($tabs);
                var url = sour["url"]+"?suspid="+id+"&type="+type+"&code="+code;
                var data = {
                    index: i,
                    url:url
                };
                html += template('data-template', data);
            }
            $contents.append(html);
            $('a[data-toggle="tab"]').on('shown.bs.tab', function (e) {
                // 获取已激活的标签页的名称
                var href = $(e.target).attr("href");
                var conid = href.replace("#","");
                var u= $("#"+conid).find("iframe").attr("data-url");
                //alert(u)
                $("#"+conid).find("iframe").attr("src",u);
            });
        }


        return {
            init:_init
        };
    })();

    suspicious.init();


})(document, window, jQuery);