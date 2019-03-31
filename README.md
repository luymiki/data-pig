
## 个人项目有不足之处与bug，请谅解
<br>
## 数据管理配置文档

<br>	

[**数据抽取**](#插件配置说明)<br>	
[插件配置说明](#插件配置说明)<br>
&emsp;[Console](#ConsoleWriter)<br>
&emsp;&emsp;[ConsoleWriter](#ConsoleWriter)<br>
&emsp;[Database](#MySqlReader)<br>
&emsp;&emsp;[MySql](#MySqlReader)<br>
&emsp;&emsp;&emsp;[MySqlReader](#MySqlReader)<br>
&emsp;&emsp;&emsp;[MySqlWriter](#MySqlWriter)<br>
&emsp;&emsp;[Oracle](#OracleReader)<br>
&emsp;&emsp;&emsp;[OracleReader](#OracleReader)<br>
&emsp;&emsp;&emsp;[OracleWriter](#OracleWriter)<br>
&emsp;&emsp;[SqlServer](#SqlServerReader)<br>
&emsp;&emsp;&emsp;[SqlServerReader](#SqlServerReader)<br>
&emsp;&emsp;&emsp;[SqlServerWriter](#SqlServerWriter)<br>
&emsp;[ElasticSearch](#ElasticSearchReader)<br>
&emsp;&emsp;[ElasticSearchReader](#ElasticSearchReader)<br>
&emsp;&emsp;[ElasticSearchWriter](#ElasticSearchWriter)<br>
&emsp;[TXT](#TxtReader)<br>
&emsp;&emsp;[TxtReader](#TxtReader)<br>
&emsp;&emsp;[TxtWriter](#TxtWriter)<br>
&emsp;[FTP](#FtpTxtReader)<br>
&emsp;&emsp;[FtpTxtReader](#FtpTxtReader)<br>
&emsp;&emsp;[FtpTxtWriter](#FtpTxtWriter)<br>
&emsp;[插件开发](#Reader插件开发)<br>
&emsp;&emsp;[Reader 插件开发](#Reader插件开发)<br>
&emsp;&emsp;[Writer 插件开发](#Writer插件开发)<br>

<br>	

[**任务调度**](#任务配置说明)<br>
&emsp;[任务配置说明](#任务配置说明)<br>
&emsp;[任务开发](#任务开发)<br>
<br>
<br>

## 数据抽取

**<div id="插件配置说明">插件配置说明</div>**<br>
插件必须包含两个部分：reader、writer。这两部分配置的配置项会被当做参数传入name配置中指定的插件类。插件类中可通过init(Map params)方法初始化，也可通过getParams()方法获取到。<br>
reader和writer中参数name为必填项。<br>
各个reader和writer插件可进行组合，并可根据业务需求进行插件开发<br>

    {
        "plugin":{
            "reader":{
                "name":"com.anluy.datapig.plugin.database.oracle.OracleReader",
                "url":"jdbc:oracle:thin:@68.64.9.188:1521:jzdb",
                "username":"jzck",
                "password":"jzck",
                "sql":"select * from zjjk_jyls_20180907 t"
            },
            "writer":{
                "name":"com.anluy.datapig.plugin.database.mysql.MySqlWriter",
                "url":"jdbc:mysql://68.64.8.82:3306/data-pig?useUnicode=true&characterEncoding=utf-8&zeroDateTimeBehavior=convertToNull",
                "username":"root",
                "password":"xinghuo",
                "tableName":"zjjk_jyls",
                "batchSize":"500"
            }
        }
    }
<br>
                                      
**<div id="ConsoleWriter">com.anluy.datapig.plugin.console.ConsoleWriter</div>**<br>
输出 Console 数据插件：<br>
参数name：值为固定的com.anluy.datapig.plugin.console.ConsoleWriter，必填项。<br>
参数format：数据格式化选项。<br>
  参数date：对指定字段的数据进行时间格式化，转换为字符串格式，可填项。<br>
  目前只实现了以上一个格式化方法。<br>

    {
        "plugin":{
            "writer":{
                "name":"com.anluy.datapig.plugin.console.ConsoleWriter",
                "format":{
                    "create_time":{"date":"yyyy-MM-dd HH:mm:ss"}
                }
           }
        }
    }

<br>           

**<div id="MySqlReader">com.anluy.datapig.plugin.database.mysql.MySqlReader</div>**<br>
读取 MySql 数据插件：<br>
参数name：值为固定的com.anluy.datapig.plugin.database.mysql.MySqlReader，必填项。<br>
参数url：值为数据库的JDBC连接串，必填项。<br>
参数username：值为数据库登陆用户名，必填项。<br>
参数password：值为数据库登陆用户名，必填项。<br>
参数encoding：值为读取数据库字符字段的编码设置，可填项。<br>
参数sql：值为查询数据的SQL串，必填项。<br>
以上参数为插件类中通过init(Map params)方法初始化创建数据库连接使用，程序中可通过getParams()方法获取到。<br>

    {
        "plugin":{
            "reader":{
               "name":"com.anluy.datapig.plugin.database.mysql.MySqlReader",
               "url":"jdbc:mysql://68.64.9.205:3307/p2p_merge?useUnicode=true&characterEncoding=utf-8&zeroDateTimeBehavior=convertToNull&tcpRcvBuf=2048000",
               "username":"root",
               "password":"root_jzzd",
               "encoding":"UTF-8",
               "sql":"select * from p2p_onlineloan_transaction_copy t",
               "format":{
                   "createtime":{"date":"yyyy-MM-dd HH:mm:ss"},
                   "updatetime":{"date":"yyyy-MM-dd HH:mm:ss"}
               }
           }
        }
    }
<br>   

**<div id="MySqlWriter">com.anluy.datapig.plugin.database.mysql.MySqlWriter</div>**<br>
写入 MySql 数据插件：<br>
参数name：值为固定的com.anluy.datapig.plugin.database.mysql.MySqlWriter，必填项。<br>
参数url：值为数据库的JDBC连接串，必填项。<br>
参数username：值为数据库登陆用户名，必填项。<br>
参数password：值为数据库登陆用户名，必填项。<br>
参数tableName：需要插入数据的表名，必填项。这张表必须存在，否则会报错，插入的INSERT语句是自动根据读取插件读取到的字段名生成。<br>
参数batchSize：每次批量提交的记录数，必填项。<br>
以上参数为插件类中通过init(Map params)方法初始化创建数据库连接使用，程序中可通过getParams()方法获取到。<br>
★ 注意：数据插入不会判断数据是否存在，有可能会存在主键冲突。如需判断需自定义插件。<br>

    {
        "plugin":{
            "writer":{
                "name":"com.anluy.datapig.plugin.database.mysql.MySqlWriter",
                "url":"jdbc:mysql://68.64.8.82:3306/data-pig?useUnicode=true&characterEncoding=utf-8&zeroDateTimeBehavior=convertToNull",
                "username":"root",
                "password":"xinghuo",
                "tableName":"p2p_onlineloan_transaction_copy",
                "batchSize":"500"
            }
        }
    }
<br>     

**<div id="OracleReader">com.anluy.datapig.plugin.database.oracle.OracleReader</div>**<br>
读取 Oracle 数据插件：<br>
参数name：值为固定的com.anluy.datapig.plugin.database.oracle.OracleReader，必填项。<br>
参数url：值为数据库的JDBC连接串，必填项。<br>
参数username：值为数据库登陆用户名，必填项。<br>
参数password：值为数据库登陆用户名，必填项。<br>
参数encoding：值为读取数据库字符字段的编码设置，可填项。<br>
参数sql：值为查询数据的SQL串，必填项。<br>
以上参数为插件类中通过init(Map params)方法初始化创建数据库连接使用，程序中可通过getParams()方法获取到。<br>

    {
        "plugin":{
           "reader":{
               "name":"com.anluy.datapig.plugin.database.oracle.OracleReader",
               "url":"jdbc:oracle:thin:@68.64.9.188:1521:jzdb",
               "username":"jzck",
               "password":"jzck",
               "encoding":"UTF-8",
               "sql":"select * from zjjk_jyls_20180907 t"
           },
        }
    }
<br>    

**<div id="OracleWriter">com.anluy.datapig.plugin.database.oracle.OracleWriter</div>**<br>
写入 Oracle 数据插件：<br>
参数name：值为固定的com.anluy.datapig.plugin.database.oracle.OracleWriter，必填项。<br>
参数url：值为数据库的JDBC连接串，必填项。<br>
参数username：值为数据库登陆用户名，必填项。<br>
参数password：值为数据库登陆用户名，必填项。<br>
参数tableName：需要插入数据的表名，必填项。这张表必须存在，否则会报错，插入的INSERT语句是自动根据读取插件读取到的字段名生成。<br>
参数batchSize：每次批量提交的记录数，必填项。<br>
以上参数为插件类中通过init(Map params)方法初始化创建数据库连接使用，程序中可通过getParams()方法获取到。<br>
★ 注意：数据插入不会判断数据是否存在，有可能会存在主键冲突。如需判断需自定义插件。<br>

    {
        "plugin":{
           "writer":{
               "name":"com.anluy.datapig.plugin.database.oracle.OracleWriter",
               "url":"jdbc:oracle:thin:@68.64.9.188:1521:jzdb",
               "username":"jzck",
               "password":"jzck",
               "tableName":"zjjk_jyls",
               "batchSize":"500"
           }
        }
    }
<br>     

**<div id="SqlServerReader">com.anluy.datapig.plugin.database.sqlserver.SqlServerReader</div>**<br>
读取 Oracle 数据插件：<br>
参数name：值为固定的com.anluy.datapig.plugin.database.sqlserver.SqlServerReader，必填项。<br>
参数url：值为数据库的JDBC连接串，必填项。<br>
参数username：值为数据库登陆用户名，必填项。<br>
参数password：值为数据库登陆用户名，必填项。<br>
参数encoding：值为读取数据库字符字段的编码设置，可填项。<br>
参数sql：值为查询数据的SQL串，必填项。<br>
以上参数为插件类中通过init(Map params)方法初始化创建数据库连接使用，程序中可通过getParams()方法获取到。<br>

    {
        "plugin":{
           "reader":{
               "name":"com.anluy.datapig.plugin.database.sqlserver.SqlServerReader",
               "url":"jdbc:sqlserver://68.64.9.188:1443;DatabaseName=jzdb",
               "username":"jzzd",
               "password":"111111",
               "encoding":"UTF-8",
               "sql":"select * from Fa_BalHistory t"
           },
        }
    }
<br>   

**<div id="SqlServerWriter">com.anluy.datapig.plugin.database.sqlserver.SqlServerWriter</div>**<br>
写入 Oracle 数据插件：<br>
参数name：值为固定的com.anluy.datapig.plugin.database.sqlserver.SqlServerWriter，必填项。<br>
参数url：值为数据库的JDBC连接串，必填项。<br>
参数username：值为数据库登陆用户名，必填项。<br>
参数password：值为数据库登陆用户名，必填项。<br>
参数tableName：需要插入数据的表名，必填项。这张表必须存在，否则会报错，插入的INSERT语句是自动根据读取插件读取到的字段名生成。<br>
参数batchSize：每次批量提交的记录数，必填项。<br>
以上参数为插件类中通过init(Map params)方法初始化创建数据库连接使用，程序中可通过getParams()方法获取到。<br>
★ 注意：数据插入不会判断数据是否存在，有可能会存在主键冲突。如需判断需自定义插件。<br>

    {
        "plugin":{
           "writer":{
               "name":"com.anluy.datapig.plugin.database.sqlserver.SqlServerWriter",
               "url":"jdbc:sqlserver://68.64.9.188:1443;DatabaseName=jzdb",
               "username":"jzzd",
               "password":"111111",
               "tableName":"zjjk_jyls",
               "batchSize":"500"
           }
        }
    }
<br>      

**<div id="ElasticSearchReader">com.anluy.datapig.plugin.elasticsearch.ElasticSearchReader</div>**<br>
读取 ElasticSearch 数据插件：<br>
参数name：值为固定的com.anluy.datapig.plugin.elasticsearch.ElasticSearchReader，必填项。<br>
参数host：值为ElasticSearch的连接串，多个地址用,分隔，必填项。<br>
参数username：值为ElasticSearch登陆用户名，必填项。如果ElasticSearch没有配置用户验证，随便输入一串字符即可。<br>
参数password：值为ElasticSearch登陆用户名，必填项。如果ElasticSearch没有配置用户验证，随便输入一串字符即可。<br>
参数indexName：需要插入数据的索引名，必填项。这个索引在ElasticSearch中必须存在，否则会报错。<br>
参数typeName：需要插入数据的type名，必填项。保存数据的json或sql的字段名是自动根据读取到的字段名生成。<br>
参数dsl：查询ElasticSearch的DSL语句，可填项，不填时自动生成{"size":500,"query":{"match_all":{}}}。<br>
参数prefix：将ElasticSearch的字段名进行前缀删除，可填项。<br>
参数mapping：将ElasticSearch的字段名进行映射，可填项。<br>
参数format：数据格式化选项。<br>
  参数date：对指定字段的数据进行时间格式化，转换为java.util.Date格式，可填项。<br>
  参数join：对指定字段的数据（数组）进行拼接，转换为字符串格式，可填项。<br>
  目前只实现了以上两个格式化方法。<br>
以上参数为插件类中通过init(Map params)方法初始化创建ElasticSearch连接使用，程序中可通过getParams()方法获取到。<br>

    {
       "plugin":{
           "reader":{
               "name":"com.anluy.datapig.plugin.elasticsearch.ElasticSearchReader",
               "host":"68.64.9.174:9602,68.64.9.176:9602,68.64.9.178:9602",
               "username":"szhzgk",
               "password":"szhzgk.com@402",
               "indexName":"test2",
               "typeName":"p2p_transaction_copy",
               "dsl":"",
               "prefix":"",
               "mapping":{
                   "createtime":"create_time",
                   "updatetime":"update_time",
                   "unrepayinterest":"unrepay_interest",
                   "hukouaddr":"hukou_addr",
                   "nowaddr":"now_addr",
                   "repaycapital":"repay_capital",
                   "repayinterest":"repay_interest",
                   "unrepaycapital":"unrepay_capital",
                   "isemphasis":"is_emphasis",
                   "emphasisrank":"emphasis_rank"
                },
               "format":{
                   "createtime":{"date":"yyyy-MM-dd HH:mm:ss"},
                   "updatetime":{"date":"yyyy-MM-dd HH:mm:ss"},
                   "transaction_time":{"date":"yyyy-MM-dd HH:mm:ss"},
                   "transaction_description":{"join":" "}
                }
           }
       }
    }
<br>         

**<div id="ElasticSearchWriter">com.anluy.datapig.plugin.elasticsearch.ElasticSearchWriter</div>**<br>
写入 ElasticSearch 数据插件：<br>
参数name：值为固定的com.anluy.datapig.plugin.elasticsearch.ElasticSearchWriter，必填项。<br>
参数host：值为ElasticSearch的连接串，多个地址用,分隔，必填项。<br>
参数username：值为ElasticSearch登陆用户名，必填项。如果ElasticSearch没有配置用户验证，随便输入一串字符即可。<br>
参数password：值为ElasticSearch登陆用户名，必填项。如果ElasticSearch没有配置用户验证，随便输入一串字符即可。<br>
参数indexName：需要索引数据的索引名，必填项。这个索引在ElasticSearch中必须存在，否则会报错。<br>
参数typeName：需要索引数据的type名，必填项。这个type必须存在，否则可能会报错，索引数据的json字段名是自动根据读取插件读取到的字段名生成。<br>
参数prefix：将ElasticSearch的字段名进行前缀添加，可填项。<br>
参数batchSize：每次批量提交的记录数，必填项。<br>
参数mapping：将ElasticSearch的字段名进行映射，可填项。<br>
参数format：数据格式化选项。<br>
  参数date：对指定字段的数据进行时间格式化，转换为字符串格式，可填项。<br>
  参数split：对指定字段的数据进行分割，转换为数组格式，可填项。<br>
  目前只实现了以上两个格式化方法。<br>
以上参数为插件类中通过init(Map params)方法初始化创建ElasticSearch连接使用，程序中可通过getParams()方法获取到。<br>

    {
        "plugin":{
            "writer":{
                "name":"com.anluy.datapig.plugin.elasticsearch.ElasticSearchWriter",
                "host":"68.64.9.174:9602,68.64.9.176:9602,68.64.9.178:9602",
                "username":"szhzgk",
                "password":"szhzgk.com@402",
                "indexName":"test2",
                "typeName":"p2p_transaction_copy",
                "prefix":"",
                "batchSize":"500",
                "mapping":{
                   "createtime":"create_time",
                   "updatetime":"update_time"
                },
                "format":{
                    "create_time":{"date":"yyyy-MM-dd HH:mm:ss"},
                    "update_time":{"date":"yyyy-MM-dd HH:mm:ss"},
                    "transaction_time":{"date":"yyyy-MM-dd HH:mm:ss"},
                    "transaction_description":{"split":" "}
                }
            }
        }
    }
<br>      

**<div id="TxtReader">com.anluy.datapig.plugin.txt.TxtReader</div>**<br>
读取 Txt文件 数据插件：<br>
参数name：值为固定的com.anluy.datapig.plugin.txt.TxtReader，必填项。<br>
参数filePath：值为文件全路径，filePath、fileDir二填一，同时填写以filePath为准。<br>
参数fileDir：值为文件目录全路径，必填项。<br>
参数prefix：值为文件前缀，可填项。搭配fileDir获取文件列表使用。<br>
参数suffix：值为文件后缀，可填项。搭配fileDir获取文件列表使用。<br>
参数separator：值为文件中的数据的列分隔符，不填默认为,，可填项。<br>
参数encoding：值为读取文件数据的编码格式，不填默认为UTF-8，可填项<br>
参数format：数据格式化选项。<br>
  参数date：对指定字段的数据进行时间格式化，转换为java.util.Date格式，可填项。<br>
  目前只实现了以上一个格式化方法。<br>
以上参数在程序中可通过getParams()方法获取到。<br>

    {
       "plugin":{
           "reader":{
               "name":"com.anluy.datapig.plugin.txt.TxtReader",
               "filePath":"F:/feiq/feiq/AutoRecv Files/任小龙(70208404CD7E)/1.补充材料/春天金融/日报/91440300342941628F_20181010_p2p_d_j1001.txt",
               "fileDir":"",
               "prefix":"",
               "suffix":"",
               "separator":"",
               "encoding":"UTF-8",
               "format":{
                    "create_time":{"date":"yyyy-MM-dd HH:mm:ss"}
               }
           }
       }
    }
<br>         

**<div id="TxtWriter">com.anluy.datapig.plugin.txt.TxtWriter</div>**<br>
写入 Txt 数据插件：<br>
参数name：值为固定的com.anluy.datapig.plugin.txt.TxtWriter，必填项。<br>
参数filePath：值为文件全路径，必填项。<br>
参数separator：值为文件中的数据的列分隔符，不填默认为,，可填项。<br>
参数encoding：值为读取文件数据的编码格式，不填默认为UTF-8，可填项<br>
参数format：数据格式化选项。<br>
  参数date：对指定字段的数据进行时间格式化，转换为java.lang.String格式，可填项。<br>
  目前只实现了以上一个格式化方法。<br>
以上参数在程序中可通过getParams()方法获取到。<br>

    {
        "plugin":{
            "writer":{
               "name":"com.anluy.datapig.plugin.txt.TxtWriter",
               "filePath":"H:/91440300342941628F_20181010_p2p_d_j1001.txt",
               "separator":"",
               "encoding":"UTF-8",
               "format":{
                   "create_time":{"date":"yyyy-MM-dd HH:mm:ss"}
               }
            }
        }
    }
<br>      

**<div id="FtpTxtReader">com.anluy.datapig.plugin.ftp.FtpTxtReader</div>**<br>
读取 Ftp Txt文件 数据插件：<br>
参数name：值为固定的com.anluy.datapig.plugin.ftp.FtpTxtReader，必填项。<br>
参数host：值为FTP的IP地址，必填项。<br>
参数port：值为FTP的端口号，可填项。<br>
参数userName：值为FTP的登录用户名，必填项。<br>
参数password：值为FTP的登录密码，必填项。<br>
参数filePath：值为文件全路径，filePath、fileDir二填一，同时填写以filePath为准。<br>
参数fileDir：值为文件目录全路径，必填项。<br>
参数prefix：值为文件前缀，可填项。搭配fileDir获取文件列表使用。<br>
参数suffix：值为文件后缀，可填项。搭配fileDir获取文件列表使用。<br>
参数separator：值为文件中的数据的列分隔符，不填默认为,，可填项。<br>
参数encoding：值为读取文件数据的编码格式，不填默认为UTF-8，可填项<br>
参数format：数据格式化选项。<br>
  参数date：对指定字段的数据进行时间格式化，转换为java.util.Date格式，可填项。<br>
  目前只实现了以上一个格式化方法。<br>
以上参数在程序中可通过getParams()方法获取到。<br>

    {
       "plugin":{
           "reader":{
               "name":"com.anluy.datapig.plugin.ftp.FtpTxtReader",
               "host":"68.64.8.82",
               "port":"21",
               "userName":"administrator",
               "password":"xinghuo",
               "filePath":"/data-pig/91440300342941628F_20181010_p2p_d_j1003.txt",
               "fileDir":"",
               "prefix":"",
               "suffix":"",
               "separator":"",
               "encoding":"UTF-8",
               "format":{
                    "create_time":{"date":"yyyy-MM-dd HH:mm:ss"}
               }
           }
       }
    }
<br>

**<div id="FtpTxtWriter">com.anluy.datapig.plugin.ftp.FtpTxtWriter</div>**<br>
写入 Ftp Txt 数据插件：<br>
参数name：值为固定的com.anluy.datapig.plugin.ftp.FtpTxtWriter，必填项。<br>
参数host：值为FTP的IP地址，必填项。<br>
参数port：值为FTP的端口号，可填项。<br>
参数userName：值为FTP的登录用户名，必填项。<br>
参数password：值为FTP的登录密码，必填项。<br>
参数filePath：值为文件全路径，必填项。<br>
参数separator：值为文件中的数据的列分隔符，不填默认为,，可填项。<br>
参数encoding：值为读取文件数据的编码格式，不填默认为UTF-8，可填项<br>
参数format：数据格式化选项。<br>
  参数date：对指定字段的数据进行时间格式化，转换为java.lang.String格式，可填项。<br>
  目前只实现了以上一个格式化方法。<br>
以上参数在程序中可通过getParams()方法获取到。<br>

    {
        "plugin":{
            "writer":{
               "name":"com.anluy.datapig.plugin.ftp.FtpTxtWriter",
               "host":"68.64.8.82",
               "port":"21",
               "userName":"administrator",
               "password":"xinghuo",
               "filePath":"/data-pig/91440300342941628F_20181010_p2p_d_j1003.txt",
               "separator":"",
               "encoding":"UTF-8",
               "format":{
                   "create_time":{"date":"yyyy-MM-dd HH:mm:ss"}
               }
            }
        }
    }
<br>       

**<div id="Reader插件开发">Reader 插件开发</div>**<br>
Reader类必须继承com.anluy.datapig.plugin.core.Reader实现call方法并调用this.execute();。<br>
Task任务实现类，在Reader类中创建一个内部类并继承com.anluy.datapig.plugin.core.Reader.Task实现call方法并调用任务的具体实现。<br>
实现init(Map params)方法，并在方法中初始化必要的参数和校验。<br>
实现start()方法，方法中初始化任务对象，并开始调用任务。<br>
实现end()方法，方法为当程序执行完成后的操作，如调用shutdown()关闭连接等。<br>
实现shutdown()方法，方法中关闭必要的连接，如数据库连接、线程池等等。<br>
以上参数为插件类中通过init(Map params)方法初始化创建数据库连接使用，程序中可通过getParams()方法获取到。<br>

    import com.anluy.datapig.job.service.JobManager;
    import com.anluy.datapig.plugin.core.DataPigException;
    import com.anluy.datapig.plugin.core.Reader;
    import com.anluy.datapig.plugin.database.DataBaseType;
    import com.anluy.datapig.plugin.element.*;
    import com.anluy.datapig.plugin.exchanger.RecordExchanger;
    import com.anluy.datapig.plugin.exchanger.RecordSender;
    import com.anluy.datapig.plugin.utils.DBUtil;
    import org.apache.commons.lang3.StringUtils;
    import org.slf4j.Logger;
    import org.slf4j.LoggerFactory;
    
    import java.sql.*;
    import java.util.Map;
    import java.util.concurrent.ExecutorService;
    
    /**
     * 数据库读取插件
     *
     * @author hc.zeng
     * @create 2018-10-10 16:15
     */
    public class DataBaseReader extends Reader {
        private static final Logger LOGGER = LoggerFactory.getLogger(ElasticSearchReader.class);
        protected final byte[] EMPTY_CHAR_ARRAY = new byte[0];
        private DataBaseType dataBase;
        private Connection connection;
        private volatile boolean shutdown = false;
        private String encoding;
        private String sql;
        private ExecutorService exec;
    
        public DataBaseReader(JobManager jobManager, Map params, RecordExchanger recordExchanger) {
            super(jobManager, params, recordExchanger);
        }
    
        @Override
        public Boolean call() throws Exception {
            this.execute();
            return true;
        }
    
        /**
         * 初始化参数配置
         * @param params
         * @return
         */
        @Override
        public Object init(Map params) {
            String url = (String) params.get("url");
            String username = (String) params.get("username");
            String password = (String) params.get("password");
            //校验必选参数
            if (StringUtils.isBlank(url)) {
                throw new DataPigException("DataBaseReader Plugin : url is null!");
            }
            if (StringUtils.isBlank(username)) {
                throw new DataPigException("DataBaseReader Plugin : username is null!");
            }
            if (StringUtils.isBlank(password)) {
                throw new DataPigException("DataBaseReader Plugin : password is null!");
            }
            //创建数据库连接
            try {
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("get connection start ");
                }
                connection = DBUtil.getConnection(dataBase, url, username, password);
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("get connection OK ");
                }
                log("连接数据库完成");
            } catch (Exception e) {
                throw new DataPigException(e);
            }
    
            return null;
        }
    
        @Override
        public Object start() {
            //新建任务对象，并执行任务
            ElasticSearchReader.Task task = new ElasticSearchReader.Task();
            task.reader();
              //创建线程池的方式启动任务，适合多线程读取。
    //        exec = Executor.executorService;
    //        Future<Boolean> result = exec.submit(task);
    //        try {
    //            result.get();
    //        } catch (ExecutionException e) {
    //            throw new DataPigException(e);
    //        } catch (InterruptedException e) {
    //            Thread.currentThread().interrupt();
    //        }
            return null;
        }
    
        @Override
        public Object shutdown() {
            shutdown = true;
            return null;
        }
    
        @Override
        public Object end() {
            shutdown();
            return null;
        }
    
        /**
         * 任务实现
         */
        public class Task extends Reader.Task {
    
            @Override
            public Boolean call() throws Exception {
                reader();
                return true;
            }
           /**
            * 读取数据
            */
            @Override
            protected void reader() {
                ResultSet resultSet = null;
                try {
                    Statement statement = connection.createStatement();
                    if (LOGGER.isInfoEnabled()) {
                        LOGGER.info("start query sql => " + sql);
                    }
                    resultSet = statement.executeQuery(sql);
                    ResultSetMetaData metaData = resultSet.getMetaData();
                    if (LOGGER.isInfoEnabled()) {
                        LOGGER.info("query ok，sender data");
                    }
                    log("读取数据SQL:" + sql);
                    //开始读取数据
                    long time = System.currentTimeMillis();
                    int pcs = 0;
                    int mite = 1;
                    while (!shutdown && resultSet.next()) {
                        //转换数据并往通道中写入
                        this.transportOneRecord(getRecordExchanger(), resultSet, metaData);
                        long time2 = System.currentTimeMillis();
                        pcs++;
                        //每分钟记录一次日志信息
                        if ((time2 - time) > 60000 * mite) {
                            log("读取数据" + pcs + "条");
                            mite++;
                        }
                    }
                    //往通道中写入一个完成标记的数据行
                    getRecordExchanger().terminate();
                    log("读取数据完成，共" + pcs + "条");
                } catch (SQLException e) {
                    LOGGER.error("read database fail :" + e.getMessage(), e);
                    getRecordExchanger().shutdown();
                    log("读取数据发生异常:" + e.getMessage());
                    throw new DataPigException(e);
                } finally {
                    if (resultSet != null) {
                        try {
                            resultSet.close();
                        } catch (SQLException e) {
                            LOGGER.error("close resultSet fail :" + e.getMessage(), e);
                        }
                    }
                    if (connection != null) {
                        try {
                            connection.close();
                        } catch (SQLException e) {
                            LOGGER.error("close connection fail :" + e.getMessage(), e);
                        }
                    }
                }
            }
            /**
             * 转换数据
             * @param recordSender
             * @param resultSet
             * @param metaData
             * @return
             */
            protected Record transportOneRecord(RecordSender recordSender, ResultSet resultSet, ResultSetMetaData metaData) {
                Record record = buildRecord(recordSender, resultSet, metaData);
                //往通道中写入一条记录
                recordSender.sendToWriter(record);
                return record;
            }
            /**
             * 创建一行数据对象,并转换数据格式
             * @param recordSender
             * @param rs
             * @param metaData
             * @return
             */
            protected Record buildRecord(RecordSender recordSender, ResultSet rs, ResultSetMetaData metaData) {
                 //...........
            }
        }
    }
<br>    

**<div id="Writer插件开发">Writer 插件开发</div>**<br>
Writer类必须继承com.anluy.datapig.plugin.core.Writer实现call方法并调用this.execute();。<br>
Task任务实现类，在Writer类中创建一个内部类并继承com.anluy.datapig.plugin.core.Writer.Task实现call方法并调用任务的具体实现。<br>
实现init(Map params)方法，并在方法中初始化必要的参数和校验。<br>
实现start()方法，方法中初始化任务对象，并开始调用任务。<br>
实现end()方法，方法为当程序执行完成后的操作，如调用shutdown()关闭连接等。<br>
实现shutdown()方法，方法中关闭必要的连接，如数据库连接、线程池等等。<br>
以上参数为插件类中通过init(Map params)方法初始化创建数据库连接使用，程序中可通过getParams()方法获取到。<br>

    import com.anluy.datapig.job.service.JobManager;
    import com.anluy.datapig.plugin.core.DataPigException;
    import com.anluy.datapig.plugin.core.Wirter;
    import com.anluy.datapig.plugin.element.Record;
    import com.anluy.datapig.plugin.element.TerminateRecord;
    import com.anluy.datapig.plugin.exchanger.RecordExchanger;
    import com.anluy.datapig.plugin.utils.DBUtil;
    import com.google.common.collect.Lists;
    import org.apache.commons.lang3.StringUtils;
    import org.slf4j.Logger;
    import org.slf4j.LoggerFactory;
    
    import java.sql.Connection;
    import java.sql.PreparedStatement;
    import java.sql.SQLException;
    import java.util.ArrayList;
    import java.util.Collection;
    import java.util.List;
    import java.util.Map;
    import java.util.concurrent.*;
    import java.util.concurrent.atomic.AtomicLong;
    
    /**
     * 数据库写入插件
     *
     * @author hc.zeng
     * @create 2018-10-10 16:30
     */
    
    public abstract class DataBaseWriter extends Wirter {
        private static final Logger LOGGER = LoggerFactory.getLogger(DataBaseWriter.class);
        private DataBaseType dataBase;
        private int batchSize = 500;
        private volatile boolean shutdown = false;
        private ExecutorService exec;
        private String url;
        private String username;
        private String password;
        private String tableName;
        private AtomicLong atomicLong = new AtomicLong();
    
        public DataBaseWriter(JobManager jobManager, Map params, RecordExchanger recordExchanger) {
            super(jobManager, params, recordExchanger);
        }
    
        @Override
        public Boolean call() throws Exception {
            this.execute();
            return true;
        }
    
        /**
         * 初始化参数
         *
         * @param params
         * @return
         */
        @Override
        public Object init(Map params) {
            url = (String) params.get("url");
            username = (String) params.get("username");
            password = (String) params.get("password");
            String batchSize = (String) params.get("batchSize");
            tableName = (String) params.get("tableName");
            //校验必须参数
            if (StringUtils.isBlank(url)) {
                throw new DataPigException("DataBaseWriter Plugin : url is null!");
            }
            if (StringUtils.isBlank(username)) {
                throw new DataPigException("DataBaseWriter Plugin : username is null!");
            }
            if (StringUtils.isBlank(password)) {
                throw new DataPigException("DataBaseWriter Plugin : password is null!");
            }
            if (StringUtils.isBlank(tableName)) {
                throw new DataPigException("DataBaseWriter Plugin : tableName is null!");
            }
            if (dataBase == null) {
                throw new DataPigException("DataBaseWriter Plugin : dataBase is null!");
            }
            if (StringUtils.isNotBlank(batchSize)) {
                try {
                    this.batchSize = Integer.valueOf(batchSize);
                } catch (Exception e) {
                    throw new DataPigException("batchSize not's a number", e);
                }
            }
            return null;
        }
    
    
        /**
         * 开始执行
         * @return
         */
        @Override
        public Object start() {
            //创建线程池
            exec = Executor.executorService;
            //创建多个任务，并批量执行。并等等执行结果
            Collection<Task> taskList = new ArrayList<Task>();
            for (int i = 0; i < ThreadSize; i++) {
                Task task = new Task();
                taskList.add(task);
            }
            List<Future<Boolean>> results = Lists.newArrayList();
            try {
                results = exec.invokeAll(taskList);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            //获取执行结果
            for (Future<Boolean> result : results) {
                try {
                    result.get();
                } catch (ExecutionException e) {
                    //如果线程内部有错误，往上层抛出。
                    throw new DataPigException(e);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            return null;
        }
    
        @Override
        public Object shutdown() {
            shutdown = true;
            return null;
        }
    
        @Override
        public Object end() {
            shutdown();
            return null;
        }
    
        /**
         * 任务实现
         */
        public class Task extends Wirter.Task {
            private String insertSql;
            private Connection connection;
    
            @Override
            public Boolean call() throws Exception {
                writer();
                return null;
            }
    
            /**
             * 任务的具体实现
             */
            @Override
            protected void writer() {
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("启动写数据线程");
                }
                try {
                    if (LOGGER.isInfoEnabled()) {
                        LOGGER.info("写数据线程获取数据库连接");
                    }
                    connection = DBUtil.getConnection(dataBase, url, username, password);
                    if (LOGGER.isInfoEnabled()) {
                        LOGGER.info("写数据线程获取数据库连接完成");
                    }
    
                    log("连接数据库完成");
    
                    long time = System.currentTimeMillis();
                    int mite = 1;
                    while (!shutdown) {
                        Record record = null;
                        List<Record> dataList = new ArrayList<>();
                        //获取一批数据
                        for (int i = 0; i < batchSize; i++) {
                            //从通道中获取一条记录
                            record = getRecordExchanger().getFromReader();
                            if (record instanceof TerminateRecord) {
                                break;
                            }
                            if (StringUtils.isBlank(insertSql)) {
                                getInsertSql(record);
                                log("生成插入SQL:" + insertSql);
                            }
                            dataList.add(record);
                        }
                        //批量保存
                        save(dataList);
    
                        //每分钟记录一次日志
                        long time2 = System.currentTimeMillis();
                        if ((time2 - time) > 60000 * mite) {
                            log("总共写入数据" + atomicLong.get() + "条");
                            mite++;
                        }
                        //如果读取到结束标记，将通道关闭，并将当前线程标记为关闭,
                        if (record instanceof TerminateRecord) {
                            if (LOGGER.isInfoEnabled()) {
                                LOGGER.info("写数据线程获取到结束标记");
                            }
                            //标记通道为关闭，让其他线程也能根据关闭状态一起关闭了
                            getRecordExchanger().shutdown();
                            shutdown = true;
                            break;
                        }
                    }
    
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                    getRecordExchanger().shutdown();
                    log("数据入库发生异常:" + e.getMessage());
                    throw new DataPigException(e);
                } finally {
                    if (LOGGER.isInfoEnabled()) {
                        LOGGER.info("数据入库完成，总共写入数据" + atomicLong.get() + "条");
                    }
                    log("数据入库完成，总共写入数据" + atomicLong.get() + "条");
                    if (connection != null) {
                        try {
                            connection.close();
                        } catch (SQLException e) {
                            LOGGER.error("关闭数据库连接失败", e);
                        }
                    }
                }
            }
    
            /**
             * 生成INSERT 的sql语句
             * @param record
             * @return
             */
            private String getInsertSql(Record record) {
                if (StringUtils.isBlank(insertSql)) {
                    StringBuffer sb = new StringBuffer();
                    StringBuffer sbCol = new StringBuffer();
                    for (int i = 0; i < record.getColumnNumber(); i++) {
                        if (i > 0) {
                            sb.append(",");
                            sbCol.append(",");
                        }
                        sb.append(record.getColumn(i).getColumnName());
                        sbCol.append("?");
                    }
                    insertSql = "INSERT INTO " + tableName + "(" + sb.toString() + ") VALUES (" + sbCol.toString() + ")";
                }
                return insertSql;
            }
    
            /**
             * 批量保存数据
             * @param dataList
             * @throws SQLException
             */
            private void save(List<Record> dataList) throws SQLException {
                if (dataList.size() > 0) {
                    PreparedStatement prepareStatement = connection.prepareStatement(insertSql);
                    connection.setAutoCommit(false);
                    for (Record record : dataList) {
                        for (int i = 0; i < record.getColumnNumber(); i++) {
                            prepareStatement.setObject(i + 1, record.getColumn(i).asObject());
                        }
                        prepareStatement.addBatch();
                    }
                    prepareStatement.executeBatch();
                    connection.commit();
                    prepareStatement.close();
                    atomicLong.addAndGet(dataList.size());
                    dataList.clear();
                    dataList = null;
                }
    
            }
    
        }
    }
<br>       

## 任务调度

**<div id="任务配置说明">任务配置说明</div>**<br>
任务必须包含参数Bean。<br>
Bean指定为Spring Bean的类时必须是在Spring容器中的，否则报错。<br>
Bean指定为class全路径时，会先在Spring容器中取，如果没有，则通过反射实例化obj.getConstructor().newInstance()一个对象，请确保有无参构造函数。<br>
任务开发<br>
Task类必须继承com.anluy.datapig.job.task.DataPigTask实现task方法。<br>
实现task()方法，该方法为程序执行入口方法。<br>
实现shutdown()方法，方法中关闭必要的连接，如数据库连接、线程池等等。<br>

    import org.slf4j.Logger;
    import org.slf4j.LoggerFactory;
    import org.springframework.stereotype.Component;
    
    /**
     * 测试定时任务(演示Demo，可删除)
     * ${DESCRIPTION}
     *
     * @author hc.zeng
     * @create 2018-11-05 17:02
     */
    @Component("testTask")
    public class TestTask extends DataPigTask{
    	private Logger logger = LoggerFactory.getLogger(getClass());
    
    	@Override
    	public void task() {
    		logger.info(String.format("我是任务[%s]，正在被执行。",this.getJobManager().getScheduleJob().getName()));
    	}
    
    	@Override
    	public void shutdown() {
    		this.setShutdown(true);
    	}
    }
                                        

