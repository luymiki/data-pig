/**
 * Created by hc.zeng on 2018/3/4.
 */
(function (e, t, $) {
    "use strict";

    $.ajax.proxy({url:"/registry/list",
        method:"POST",
        contentType:"application/json",
        dataType:"json",
        success:function (d) {
            console.log(d.data)
            var initData=[];
            for(var i=0;i<d.data.length;i++){
                var application = d.data[i];
                for(var j=0;j< application.instance.length;j++){
                    var instance = application.instance[j];
                    if(instance){
                        console.log(instance)
                        initData[initData.length] = instance;
                        instance.port = instance["metadata"]["management.port"];
                        instance.serviceUpTimestamp = instance["leaseInfo"]["serviceUpTimestamp"];
                    }

                }
            }
            init(initData);
        }
    });
    function formatterDate(d){
        var date = new Date(d);
        var s = date.getFullYear()+"-"+formatterDateStr(date.getMonth()+1)+"-"+formatterDateStr(date.getDate())+
            " "+formatterDateStr(date.getHours())+":"+formatterDateStr(date.getMinutes())+":"+formatterDateStr(date.getSeconds());
        return s;
    }
    function formatterDateStr(d){
        return d<10?"0"+d:d;
    }

    function init(_data) {
        $('#serviceRegistryCenter').bootstrapTable({
            pagination:true,
            pageSize:10,
            height: "445",
            columns: [{
                field: 'app',
                title: '实例名称',
                sortable:true
            }, {
                field: 'ipAddr',
                title: 'IP地址'
            }, {
                field: 'port',
                title: '端口号'

            }, {
                field: 'status',
                title: '状态'
            },
                {
                field: 'serviceUpTimestamp',
                title: '服务上线时间',
                formatter:formatterDate
            },
                {
                field: 'lastUpdatedTimestamp',
                title: '最后更新时间',
                    formatter:formatterDate
            },
                {
                field: 'lastDirtyTimestamp',
                title: '最后离线时间',
                    formatter:formatterDate
            }
            ]
            ,
            data:_data
        });
    }

})(document, window, jQuery);