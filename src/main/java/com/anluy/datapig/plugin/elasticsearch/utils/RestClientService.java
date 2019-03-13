package com.anluy.datapig.plugin.elasticsearch.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.anluy.datapig.plugin.core.DataPigException;
import com.google.common.base.Splitter;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.common.Strings;
import org.springframework.util.Assert;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * ${DESCRIPTION}
 *
 * @author hc.zeng
 * @create 2018-10-26 14:41
 */
public class RestClientService {
    private static Logger LOG = LogManager.getLogger(RestClientService.class);
    private RestClient restClient;
    private String hostPort;
    private String username;
    private String password;

    private int connectTimeout;
    private int socketTimeout;
    private int maxRetryTimeoutMillis;

    public RestClientService(String hostPort) {
        this(hostPort, null, null, 5000, 60000, 60000);
    }

    public RestClientService(String hostPort, String username, String password) {
        this(hostPort, username, password, 5000, 60000, 60000);
    }

    public RestClientService(String hostPort, String username, String password, int connectTimeout, int socketTimeout, int maxRetryTimeoutMillis) {
        this.hostPort = hostPort;
        this.username = username;
        this.password = password;

        this.connectTimeout = connectTimeout;
        this.socketTimeout = socketTimeout;
        this.maxRetryTimeoutMillis = maxRetryTimeoutMillis;
        restClient = getRestClient(hostPort, username, password, new int[]{connectTimeout, socketTimeout, maxRetryTimeoutMillis});
        LOG.info(String.format("初始化rest client客户端完成-hostPort[%s], username[%s], connectTimeout[%s], socketTimeout[%s], maxRetryTimeoutMillis[%s]", hostPort, username, connectTimeout, socketTimeout, maxRetryTimeoutMillis));
    }

    public String getHostPort() {
        return hostPort;
    }

    public void setHostPort(String hostPort) {
        this.hostPort = hostPort;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public int getSocketTimeout() {
        return socketTimeout;
    }

    public void setSocketTimeout(int socketTimeout) {
        this.socketTimeout = socketTimeout;
    }

    public int getMaxRetryTimeoutMillis() {
        return maxRetryTimeoutMillis;
    }

    public void setMaxRetryTimeoutMillis(int maxRetryTimeoutMillis) {
        this.maxRetryTimeoutMillis = maxRetryTimeoutMillis;
    }

    /**
     * 获取RestClient
     *
     * @param hostPort ip:port,ip:port
     * @param username
     * @param password
     * @param timeouts [connectTimeout, socketTimeout, maxRetryTimeoutMillis]
     * @return
     */
    public RestClient getRestClient(String hostPort, String username, String password, int[] timeouts) {

        List<String> hostPorts = Splitter.on(",")
                .trimResults()
                .omitEmptyStrings()
                .splitToList(hostPort);

        HttpHost[] httpHostArray = new HttpHost[hostPorts.size()];

        IntStream.range(0, hostPorts.size())
                .forEach(index -> {
                    String ipPortStr = hostPorts.get(index);
                    List<String> ipPorts = Splitter.on(":")
                            .omitEmptyStrings()
                            .trimResults()
                            .splitToList(ipPortStr);
                    httpHostArray[index] = new HttpHost(ipPorts.get(0), Integer.valueOf(ipPorts.get(1)));
                });
        int maxRetryTimeoutMillis = 60000;
        if (timeouts != null && timeouts.length == 3) {
            maxRetryTimeoutMillis = timeouts[2];
        }
        RestClientBuilder restClientBuilder = RestClient.builder(httpHostArray);
        if (username != null && password != null) {
            final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
            restClientBuilder.setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
                @Override
                public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpAsyncClientBuilder) {
                    return httpAsyncClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                }
            });
        }

        restClientBuilder.setRequestConfigCallback(new RestClientBuilder.RequestConfigCallback() {
            @Override
            public RequestConfig.Builder customizeRequestConfig(RequestConfig.Builder builder) {
                int connectTimeout = 5000;
                int socketTimeout = 60000;
                if (timeouts != null) {
                    if (timeouts.length >= 1) {
                        connectTimeout = timeouts[0];
                    }
                    if (timeouts.length >= 2) {
                        socketTimeout = timeouts[1];
                    }
                }
                return builder.setConnectTimeout(connectTimeout)
                        .setSocketTimeout(socketTimeout);
            }
        })
                .setMaxRetryTimeoutMillis(maxRetryTimeoutMillis);
        return restClientBuilder.build();
    }

    /**
     * 时间窗口回调接口，处理每一次时间窗口返回的数据
     */
    public interface TimeWindowCallBack {

        void process(List<Map> recordDataList);
    }

    /**
     * 读取大结果集的scroll方法
     *
     * @param dslStatement       DSL查询语句
     * @param timeWindow         时间窗口，单位分钟，默认值是1分钟
     * @param timeWindowCallBack 时间窗口回调接口
     * @param indexName          索引名
     * @param typeName           类型名
     * @param includeFields      需要返还的字段，多个用逗号分隔，字段名支持通配符
     * @param excludeFields      不需要返还的字段，多个用逗号分隔，字段名支持通配符
     *                           includeFields和excludeFields都为空时默认返还所有字段
     * @throws IOException
     */
    public void scroll(String dslStatement, String timeWindow, TimeWindowCallBack timeWindowCallBack, String indexName, String typeName, String includeFields, String excludeFields) throws IOException {
        Assert.hasText(dslStatement, "dslStatement 不能为空！");
        Assert.notNull(timeWindowCallBack, "timeWindowCallBack 不能为空！");
        Assert.hasText(indexName, "indexName 不能为空！");
        Assert.hasText(typeName, "typeName 不能为空！");

        HttpEntity httpEntity = new StringEntity(dslStatement, ContentType.APPLICATION_JSON);

        //第一步指定索引或者类型：/_search?scroll=1m
        String scrolPreUrl = "/" + indexName + "/" + typeName + "/_search";
        if (Strings.isNullOrEmpty(timeWindow)) {
            scrolPreUrl += "?scroll=1m";
        } else {
            scrolPreUrl += "?scroll=" + timeWindow + "m";
        }

        if (!Strings.isNullOrEmpty(includeFields)) {
            scrolPreUrl += "&_source_include=" + includeFields;
        }
        if (!Strings.isNullOrEmpty(excludeFields)) {
            scrolPreUrl += "&_source_exclude=" + excludeFields;
        }

        int searchCount = 1;//查询请求计数器
        Response response = restClient.performRequest("GET", scrolPreUrl, Collections.<String, String>emptyMap(), httpEntity);

        int total = 0;
        int shardTotal = 0;
        boolean hasShardFailed = false;
        int count = 0;
        String scrollId = null;
        Configuration configuration = createConfigFromResponseData(response);
        if (configuration != null) {
            total = configuration.getInt("hits.total");
            shardTotal = configuration.getInt("_shards.total");
            int shardFailed = configuration.getInt("_shards.failed");
            if (shardFailed > 0) {
                LOG.error(String.format("searchCount[%s]-查询index[%s],type[%s]有分片读取失败", searchCount, indexName, typeName));
                hasShardFailed = true;
            }
            scrollId = configuration.getString("_scroll_id");
            List<Map> tmpDataList = configuration.getList("hits.hits", Map.class);
            List<Map> dataList = new ArrayList<>(tmpDataList.size());
            tmpDataList.stream().forEach(map -> {
                Map record = (Map) map.get("_source");
                String id = (String) map.get("_id");
                record.put("_id", id);
                dataList.add(record);
            });
            timeWindowCallBack.process(dataList);
            count += dataList.size();
        }
        //第二步URL不能包含index和type名称-在第一步指定
        String scrollUrl = "/_search/scroll?scroll_id=" + scrollId;
        if (Strings.isNullOrEmpty(timeWindow)) {
            scrollUrl += "&scroll=1m";
        } else {
            scrollUrl += "&scroll=" + timeWindow + "m";
        }

        while (true) {
            searchCount++;
            response = restClient.performRequest("GET", scrollUrl, Collections.<String, String>emptyMap());

            configuration = createConfigFromResponseData(response);
            if (configuration != null) {
                scrollId = configuration.getString("_scroll_id");
                int shardFailed = configuration.getInt("_shards.failed");
                if (shardFailed > 0) {
                    LOG.error(String.format("searchCount[%s]-查询index[%s],type[%s], shardTotal[%s], 有[%s]个分片读取失败！", searchCount, indexName, typeName, shardTotal, shardFailed));
                    hasShardFailed = true;
                }
                List<Map> tmpDataList = configuration.getList("hits.hits", Map.class);
                if (tmpDataList.size() == 0) {//读取完毕，跳出循环
                    break;
                }
                List<Map> dataList = new ArrayList<>(tmpDataList.size());
                tmpDataList.stream().forEach(map -> {
                    Map record = (Map) map.get("_source");
                    String id = (String) map.get("_id");
                    record.put("_id", id);
                    dataList.add(record);
                });
                //回调接口处理时间窗口返回的数据
                timeWindowCallBack.process(dataList);
                count += dataList.size();
            }
        }
        LOG.info(String.format("index: %s, type: %s, 来源总记录数：%s条, 读取的总记录数：%s条, 发起网络请求数：%s次, 是否有分片读取失败：%s", indexName, typeName, total, count, searchCount, hasShardFailed));
        //释放search context查询上下文的资源
        response = restClient.performRequest("DELETE", "/_search/scroll?scroll_id=" + scrollId);
    }

    /**
     * 从响应体中获取数据，封装成Configuration返回
     *
     * @param response
     * @return
     * @throws IOException
     */
    private Configuration createConfigFromResponseData(Response response) throws IOException {
        Configuration configuration = null;
        if (response == null) {
            return null;
        }
        if (response.getStatusLine() != null && response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                configuration = Configuration.from(line);
            }
        }
        return configuration;
    }

    /**
     * 获取响应的文本信息
     *
     * @param response
     * @return
     * @throws IOException
     */
    private String getResponseText(Response response) throws IOException {
        if (response == null) {
            return null;
        }
        StringBuilder text = new StringBuilder();
        if (response.getStatusLine() != null && response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                text.append(line);
            }
        }
        return text.toString();
    }

    /**
     * 根据ID获取对应的文档记录
     *
     * @param indexName     索引名
     * @param typeName      索引类型名
     * @param id            文档主键ID值
     * @param includeFields 需要返还的字段，多个用逗号分隔，字段名支持通配符
     * @param excludeFields 不需要返还的字段，多个用逗号分隔，字段名支持通配符
     *                      includeFields和excludeFields都为空时默认返还所有字段
     * @return
     * @throws IOException
     */
    public Map get(String indexName, String typeName, String id, String includeFields, String excludeFields) {
        Assert.hasText(indexName, "indexName 不能为空！");
        Assert.hasText(typeName, "typeName 不能为空！");
        Assert.hasText(id, "id 不能为空！");
        Configuration configuration = null;
        Map record = null;
        try {
            String url = "/" + indexName + "/" + typeName + "/" + id;
            if (!Strings.isNullOrEmpty(includeFields) && !Strings.isNullOrEmpty(excludeFields)) {
                url += "?_source_include=" + includeFields + "&_source_exclude=" + excludeFields;
            } else if (!Strings.isNullOrEmpty(includeFields)) {
                url += "?_source_include=" + includeFields;
            } else if (!Strings.isNullOrEmpty(excludeFields)) {
                url += "?_source_exclude=" + excludeFields;
            }
            Response response = restClient.performRequest("GET", url);
            configuration = createConfigFromResponseData(response);
            if (LOG.isDebugEnabled()) {
                LOG.debug("\nget done. ResponseData:\n" + configuration.toJSONPrettyFormat());
            }
            if (configuration != null) {
                record = configuration.getMap("_source");
            }
        } catch (Exception e) {
            LOG.error("查找失败！", e);
        }

        return record;
    }

    /**
     * 根据ID列表获取对应的文档记录
     *
     * @param indexName     索引名
     * @param typeName      索引类型名
     * @param idList        文档主键ID值列表
     * @param includeFields 需要返还的字段，多个用逗号分隔，字段名支持通配符
     * @param excludeFields 不需要返还的字段，多个用逗号分隔，字段名支持通配符
     *                      includeFields和excludeFields都为空时默认返还所有字段
     * @return
     * @throws IOException
     */
    public List<Map> get(String indexName, String typeName, List<String> idList, String includeFields, String excludeFields) {
        Assert.hasText(indexName, "indexName 不能为空！");
        Assert.hasText(typeName, "typeName 不能为空！");
        Assert.notEmpty(idList, "idList 不能为空！");
        List<Map> successGetItems = new ArrayList<>();
        try {
            String url = "/" + indexName + "/" + typeName + "/_mget";
            if (!Strings.isNullOrEmpty(includeFields) && !Strings.isNullOrEmpty(excludeFields)) {
                url += "?_source_include=" + includeFields + "&_source_exclude=" + excludeFields;
            } else if (!Strings.isNullOrEmpty(includeFields)) {
                url += "?_source_include=" + includeFields;
            } else if (!Strings.isNullOrEmpty(excludeFields)) {
                url += "?_source_exclude=" + excludeFields;
            }
            StringBuilder builder = new StringBuilder("{\"ids\":[");
            IntStream.range(0, idList.size()).forEach(i -> {
                if (i == 0) {
                    builder.append("\"").append(idList.get(i)).append("\"");
                } else {
                    builder.append(",\"").append(idList.get(i)).append("\"");
                }
            });
            builder.append("]}");
            List<String> notFoundIds = new ArrayList<>();
            HttpEntity httpEntity = new StringEntity(builder.toString(), ContentType.APPLICATION_JSON);
            if (LOG.isDebugEnabled()) {
                LOG.debug("\n" + builder.toString());
            }
            Response response = restClient.performRequest("GET", url, Collections.emptyMap(), httpEntity);
            Configuration configuration = createConfigFromResponseData(response);
            if (configuration != null) {
                List<Map> results = configuration.getList("docs", Map.class);
                results.stream().forEach(map -> {
                    Boolean found = (Boolean) map.get("found");
                    if (found) {
                        successGetItems.add((Map) map.get("_source"));
                    } else {
                        notFoundIds.add(String.valueOf(map.get("_id")));
                    }
                });
                if (!notFoundIds.isEmpty()) {
                    LOG.error("部分ID值对应的文档未找到：notFoundIds = " + notFoundIds.toString());
                }
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("\nmget done. ResponseData:\n" + configuration.toJSONPrettyFormat());
            }
        } catch (Exception e) {
            LOG.error("查找失败！", e);
        }
        return successGetItems;
    }

    /**
     * 保存文档数据
     * 注：保存child文档时，在recordData(HashMap)中放入元数据_parent
     *
     * @param recordData 文档记录
     * @param indexName  索引名
     * @param typeName   类型名
     * @param id         文档主键ID值
     * @return 返回请求响应的状态
     */
    public Status save(Map recordData, String indexName, String typeName, String id) {
        Status status = Status.ok();
        Assert.hasText(indexName, "indexName 不能为空！");
        Assert.hasText(typeName, "typeName 不能为空！");
        Assert.hasText(id, "id 不能为空！");
        Assert.notEmpty(recordData, "recordData 不能为空！");
        try {
            String url = "/" + indexName + "/" + typeName + "/" + id;
            if (recordData.containsKey("_parent")) {//save child document
                url += "?parent=" + recordData.get("_parent");
                recordData.remove("_parent");
            }
            HttpEntity httpEntity = new StringEntity(JSON.toJSONString(recordData, SerializerFeature.WriteMapNullValue), ContentType.APPLICATION_JSON);
            Response response = restClient.performRequest("POST", url, Collections.emptyMap(), httpEntity);
            if (response != null && response.getStatusLine() != null) {
                int httpStatus = response.getStatusLine().getStatusCode();
                if (httpStatus == HttpStatus.SC_OK || httpStatus == HttpStatus.SC_CREATED) {
                    refresh(indexName); //刷新索引
                }
                if (LOG.isDebugEnabled()) {
                    LOG.debug("\nsave operation done. HttpStatus:" + status);
                }
            } else {
                status = Status.error();
                status.setMessage("保存文档方法执行失败，请联系运维排查！");
            }
        } catch (Exception e) {
            LOG.error("save方法执行异常！", e);
            status = Status.error();
            status.setMessage("保存文档方法执行失败，请联系运维排查！");
        }
        return status;
    }

    /**
     * 更新文档数据
     * 注：更新child文档时，在recordData(HashMap)中放入元数据_parent
     *
     * @param recordData 文档记录
     * @param indexName  索引名
     * @param typeName   类型名
     * @param id         文档主键ID值
     * @return 返回请求响应的状态
     */
    public Status update(Map recordData, String indexName, String typeName, String id) {
        Status status = Status.ok();
        Assert.hasText(indexName, "indexName 不能为空！");
        Assert.hasText(typeName, "typeName 不能为空！");
        Assert.hasText(id, "id 不能为空！");
        try {
            String url = "/" + indexName + "/" + typeName + "/" + id + "/_update";
            if (recordData.containsKey("_parent")) {//save child document
                url += "?parent=" + recordData.get("_parent");
                recordData.remove("_parent");
            }
            String doc = "{\"doc\":" + JSON.toJSONString(recordData, SerializerFeature.WriteMapNullValue) + "}";
            HttpEntity httpEntity = new StringEntity(doc, ContentType.APPLICATION_JSON);
            Response response = restClient.performRequest("POST", url, Collections.emptyMap(), httpEntity);
            if (response != null && response.getStatusLine() != null) {
                int httpStatus = response.getStatusLine().getStatusCode();
                if (httpStatus == HttpStatus.SC_OK || httpStatus == HttpStatus.SC_CREATED) {
                    refresh(indexName); //刷新索引
                }
                if (LOG.isDebugEnabled()) {
                    LOG.debug("\nupdate operation done. HttpStatus:" + httpStatus);
                }
            } else {
                status = Status.error();
                status.setMessage("更新文档方法执行失败，请联系运维排查！");
            }
        } catch (Exception e) {
            LOG.error("update方法执行异常！", e);
            status = Status.error();
            status.setMessage("更新文档方法执行失败，请联系运维排查！");
        }
        return status;
    }

    /**
     * 根据文档ID删除该条记录
     *
     * @param indexName
     * @param typeName
     * @param id
     * @return
     */
    public Status remove(String indexName, String typeName, String id) {
        Status status = Status.ok();
        Assert.hasText(indexName, "indexName 不能为空！");
        Assert.hasText(typeName, "typeName 不能为空！");
        Assert.hasText(id, "id 不能为空！");
        try {
            String url = "/" + indexName + "/" + typeName + "/" + id;
            Response response = restClient.performRequest("DELETE", url);
            Configuration configuration = createConfigFromResponseData(response);
            if (configuration != null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("\nremove operation done. ResponseData:\n" + configuration.toJSONPrettyFormat());
                }
                boolean found = configuration.getBool("found");
                if (found) {
                    refresh(indexName);
                }
            }
        } catch (Exception e) {
            LOG.error("remove方法执行异常！", e);
            status = Status.error();
            status.setMessage("删除文档方法执行失败，请联系运维排查！");
        }
        return status;
    }

    /**
     * 刷新索引，让新增的记录及时可查
     *
     * @param indexName
     * @return
     */
    public Status refresh(String indexName) {
        Status status = Status.ok();
        Assert.hasText(indexName, "indexName 不能为空！");
        try {
            String url = "/" + indexName + "/_refresh";
            Response response = restClient.performRequest("POST", url);
            if (response.getStatusLine() != null) {
                int httpStatus = response.getStatusLine().getStatusCode();
                if (LOG.isDebugEnabled()) {
                    LOG.debug("\nrefresh operation done. HttpStatus:" + httpStatus);
                }
            } else {
                status = Status.error();
                status.setMessage("刷新索引失败，请联系运维排查！");
            }
        } catch (Exception e) {
            LOG.error("refresh方法执行异常！", e);
            status = Status.error();
            status.setMessage("刷新索引失败，请联系运维排查！");
        }
        return status;
    }

    /**
     * 批量保存 index指令：文档存在则更新、否则创建新的文档
     * 注：保存child子文档时，在recordData(HashMap)中放入元数据_parent
     *
     * @param recordDataList 文档记录集
     * @param indexName      索引名
     * @param typeName       类型名
     * @return
     */
    public Status batchSave(List<Map> recordDataList, String indexName, String typeName) throws IOException {
        Status status = Status.ok();
        Assert.notEmpty(recordDataList, "recordDataList不能为空！");
        Assert.hasText(indexName, "indexName 不能为空！");
        Assert.hasText(typeName, "typeName 不能为空！");
        String url = "/" + indexName + "/" + typeName + "/_bulk";
        final StringBuilder builder = new StringBuilder();
        recordDataList.stream().forEach(map -> {
            if (!map.containsKey("_id")) {
                throw new IllegalArgumentException("要保存的记录的主键值\"_id\"缺失！");
            }
            builder.append("{\"index\":{\"_id\":\"").append(map.get("_id")).append("\"");
            //save child document
            if (map.containsKey("_parent")) {
                builder.append(",\"_parent\":\"").append(map.get("_parent")).append("\"");
                map.remove("_parent");
            }
            builder.append("}}").append("\n");
            map.remove("_id");
            builder.append(JSON.toJSONString(map, SerializerFeature.WriteMapNullValue)).append("\n");
        });

        if (LOG.isDebugEnabled()) {
            LOG.debug(builder.toString());
        }
        ContentType contentType = ContentType.TEXT_PLAIN.withCharset(Charset.forName("UTF-8"));
        HttpEntity httpEntity = new StringEntity(builder.toString(), contentType);
        Response response = restClient.performRequest("POST", url, Collections.emptyMap(), httpEntity, new BasicHeader("Content-Type", "application/x-ndjson;charset=UTF-8"));
        if (response.getStatusLine() != null) {
            int httpStatus = response.getStatusLine().getStatusCode();
            //批量操作完成
            if (httpStatus == HttpStatus.SC_OK || httpStatus == HttpStatus.SC_CREATED) {
                Configuration configuration = createConfigFromResponseData(response);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("\nbatchSave operation done. ResponseData:\n" + configuration.toJSONPrettyFormat());
                }
                Boolean hasErrors = configuration.getBool("errors");
                status.getContext().put("hasErrors", hasErrors);
                //部分文档保存失败
                if (hasErrors) {
                    List<Map> items = configuration.getList("items", Map.class);
                    List<Map> errorItems = items.stream().filter(map -> {
                        Map tmp = (Map) map.get("index");
                        if ((Integer) tmp.get("status") != HttpStatus.SC_OK
                                && (Integer) tmp.get("status") != HttpStatus.SC_CREATED) {
                            return true;
                        }
                        return false;
                    }).collect(Collectors.toList());
                    int total = items.size();
                    int failure = errorItems.size();
                    status.getContext().put("failure", failure);
                    int success = total - failure;
                    String tips = String.format("批量保存操作完成，总记录数：{%s}，成功：{%s}，失败：{%s}，response：{%s}", total, success, failure, errorItems.toString());
                    //LOG.error(tips);
                    throw new DataPigException(tips);
                }
            }
        } else {
            throw new DataPigException("批量保存方法执行失败，请联系运维排查！");
        }
        return status;
    }

    /**
     * 批量保存 index指令：文档存在则更新、否则创建新的文档
     * 注：保存child子文档时，在recordData(HashMap)中放入元数据_parent
     *
     * @param recordDataList 文档记录集
     * @param indexName      索引名
     * @param typeName       类型名
     * @param refresh        是否刷新索引
     * @return
     */
    public Status batchSave(List<Map> recordDataList, String indexName, String typeName, boolean refresh) throws IOException {
        Status status = batchSave(recordDataList, indexName, typeName);
        if (refresh && !status.isFailed()) {
            refresh(indexName);
        }
        return status;
    }

    /**
     * 批量更新 update指令：文档存在则更新、否则报异常，单个文档失败不影响整个批量操作
     * 注：更新child子文档时，在recordData(HashMap)中放入元数据_parent
     *
     * @param recordDataList 文档记录集
     * @param indexName      索引名
     * @param typeName       类型名
     * @return
     */
    public Status batchUpdate(List<Map> recordDataList, String indexName, String typeName) {
        Status status = Status.ok();
        Assert.notEmpty(recordDataList, "recordDataList不能为空！");
        Assert.hasText(indexName, "indexName 不能为空！");
        Assert.hasText(typeName, "typeName 不能为空！");
        try {
            String url = "/" + indexName + "/" + typeName + "/_bulk";
            final StringBuilder builder = new StringBuilder();
            recordDataList.stream().forEach(map -> {
                if (!map.containsKey("_id")) {
                    throw new IllegalArgumentException("要更新的记录的主键值\"_id\"缺失！");
                }
                builder.append("{\"update\":{\"_id\":\"").append(map.get("_id")).append("\"");
                if (map.containsKey("_parent")) {
                    builder.append(",\"_parent\":\"").append(map.get("_parent")).append("\"");
                    map.remove("_parent");
                }
                builder.append("}}").append("\n");
                map.remove("_id");
                String doc = "{\"doc\":" + JSON.toJSONString(map, SerializerFeature.WriteMapNullValue) + "}\n";
                builder.append(doc);
            });

            if (LOG.isDebugEnabled()) {
                LOG.debug(builder.toString());
            }
            ContentType contentType = ContentType.TEXT_PLAIN.withCharset(Charset.forName("UTF-8"));
            HttpEntity httpEntity = new StringEntity(builder.toString(), contentType);
            Response response = restClient.performRequest("POST", url, Collections.emptyMap(), httpEntity, new BasicHeader("Content-Type", "application/x-ndjson;charset=UTF-8"));
            if (response.getStatusLine() != null) {
                int httpStatus = response.getStatusLine().getStatusCode();
                if (httpStatus == HttpStatus.SC_OK) {//整个批量操作成功
                    Configuration configuration = createConfigFromResponseData(response);
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("\nbatchUpdate operation done. ResponseData:\n" + configuration.toJSONPrettyFormat());
                    }
                    Boolean hasErrors = configuration.getBool("errors");
                    status.getContext().put("hasErrors", hasErrors);
                    if (hasErrors) {//部分文档更新失败
                        List<Map> items = configuration.getList("items", Map.class);
                        List<Map> errorItems = items.stream().filter(map -> {
                            Map tmp = (Map) map.get("update");
                            if ((Integer) tmp.get("status") != HttpStatus.SC_OK) {
                                return true;
                            }
                            return false;
                        }).collect(Collectors.toList());
                        int total = items.size();
                        int failure = errorItems.size();
                        int success = total - failure;
                        status.getContext().put("failure", failure);
                        LOG.error(String.format("批量更新操作完成，总记录数：{%s}，成功：{%s}，失败：{%s}，response：{%s}", total, success, failure, errorItems.toString()));
                    }
                }
            } else {
                status = Status.error();
                status.setMessage("批量更新方法执行失败，请联系运维排查！");
            }
        } catch (Exception e) {
            LOG.error("batchUpdate方法执行异常！", e);
            status = Status.error();
            status.setMessage("批量更新方法执行失败，请联系运维排查！");
        }
        status.getContext().put("total", recordDataList.size());
        return status;
    }

    /**
     * 批量更新 update指令：文档存在则更新、否则报异常，单个文档失败不影响整个批量操作
     * 注：更新child子文档时，在recordData(HashMap)中放入元数据_parent
     *
     * @param recordDataList 文档记录集
     * @param indexName      索引名
     * @param typeName       类型名
     * @param refresh        是否刷新索引
     * @return
     */
    public Status batchUpdate(List<Map> recordDataList, String indexName, String typeName, boolean refresh) {
        Status status = batchUpdate(recordDataList, indexName, typeName);
        if (refresh && !status.isFailed()) {
            refresh(indexName);
        }
        return status;
    }

    /**
     * 批量更新 delete指令：文档存在则删除、否则报异常，单个文档失败不影响整个批量操作
     *
     * @param idLists   文档主键ID值记录集
     * @param indexName 索引名
     * @param typeName  类型名
     * @return
     */
    public Status batchDelete(List<String> idLists, String indexName, String typeName) {
        Status status = Status.ok();
        Assert.notEmpty(idLists, "idLists 不能为空！");
        Assert.hasText(indexName, "indexName 不能为空！");
        Assert.hasText(typeName, "typeName 不能为空！");
        try {
            String url = "/" + indexName + "/" + typeName + "/_bulk";
            final StringBuilder builder = new StringBuilder();
            idLists.stream().forEach(id -> {
                builder.append("{\"delete\":{\"_id\":\"").append(id).append("\"}}").append("\n");
            });

            if (LOG.isDebugEnabled()) {
                LOG.debug(builder.toString());
            }
            ContentType contentType = ContentType.TEXT_PLAIN.withCharset(Charset.forName("UTF-8"));
            HttpEntity httpEntity = new StringEntity(builder.toString(), contentType);
            Response response = restClient.performRequest("POST", url, Collections.emptyMap(), httpEntity, new BasicHeader("Content-Type", "application/x-ndjson;charset=UTF-8"));
            if (response.getStatusLine() != null) {
                int httpStatus = response.getStatusLine().getStatusCode();
                if (httpStatus == HttpStatus.SC_OK) {//整个批量操作成功
                    Configuration configuration = createConfigFromResponseData(response);
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("\nbatchDelete operation done. ResponseData:\n" + configuration.toJSONPrettyFormat());
                    }
                    List<Map> items = configuration.getList("items", Map.class);
                    List<Map> errorItems = items.stream().filter(map -> {
                        Map tmp = (Map) map.get("delete");
                        if ((Integer) tmp.get("status") != HttpStatus.SC_OK) {
                            return true;
                        }
                        return false;
                    }).collect(Collectors.toList());
                    int total = items.size();
                    int failure = errorItems.size();
                    status.getContext().put("hasErrors", failure > 0 ? true : false);
                    int success = total - failure;
                    if (failure > 0) {
                        status.getContext().put("failure", failure);
                        LOG.error(String.format("批量删除操作完成，总记录数：{%s}，成功：{%s}，失败：{%s}，response：{%s}", total, success, failure, errorItems.toString()));
                    } else if (LOG.isDebugEnabled()) {
                        LOG.debug(String.format("批量删除操作完成，总记录数：{%s}，成功：{%s}，失败：{%s}，response：{%s}", total, success, failure, errorItems.toString()));
                    }
                }
            } else {
                status = Status.error();
                status.setMessage("批量删除方法执行失败，请联系运维排查！");
            }
        } catch (Exception e) {
            LOG.error("batchDelete方法执行异常！", e);
            status = Status.error();
            status.setMessage("批量删除方法执行失败，请联系运维排查！");
        }
        status.getContext().put("total", idLists.size());
        return status;
    }

    /**
     * 批量更新 delete指令：文档存在则删除、否则报异常，单个文档失败不影响整个批量操作
     *
     * @param idLists   文档主键ID值记录集
     * @param indexName 索引名
     * @param typeName  类型名
     * @param refresh   是否刷新索引
     * @return
     */
    public Status batchDelete(List<String> idLists, String indexName, String typeName, boolean refresh) {
        Status status = batchDelete(idLists, indexName, typeName);
        if (refresh && !status.isFailed()) {
            refresh(indexName);
        }
        return status;
    }

    /**
     * 根据DSL语句查询指定索引和类型的数据
     *
     * @param dslStatement  查询的DSL语句
     * @param indexName     索引名
     * @param typeName      类型名
     * @param includeFields 需要返还的字段，多个用逗号分隔，字段名支持通配符
     * @param excludeFields 不需要返还的字段，多个用逗号分隔，字段名支持通配符
     *                      includeFields和excludeFields都为空时默认返还所有字段
     * @return
     * @throws IOException
     */
    public List<Map> queryForList(String dslStatement, String indexName, String typeName, String includeFields, String excludeFields) throws IOException {
        Assert.hasText(dslStatement, "dslStatement不能为空！");
        Assert.hasText(indexName, "indexName 不能为空！");
        Assert.hasText(typeName, "typeName 不能为空！");

        String url = "/" + indexName + "/" + typeName + "/_search";
        if (!com.google.common.base.Strings.isNullOrEmpty(includeFields) && !com.google.common.base.Strings.isNullOrEmpty(excludeFields)) {
            url += "?_source_include=" + includeFields + "&_source_exclude=" + excludeFields;
        } else if (!com.google.common.base.Strings.isNullOrEmpty(includeFields)) {
            url += "?_source_include=" + includeFields;
        } else if (!com.google.common.base.Strings.isNullOrEmpty(excludeFields)) {
            url += "?_source_exclude=" + excludeFields;
        }
        HttpEntity httpEntity = new StringEntity(dslStatement, ContentType.APPLICATION_JSON);
        Response response = restClient.performRequest("POST", url, Collections.emptyMap(), httpEntity);
        Configuration c = createConfigFromResponseData(response);
        List<Map> result = new ArrayList<>();
        if (c != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(c.toJSONPrettyFormat());
            }
            int totalShards = c.getInt("_shards.total");
            int failedShards = c.getInt("_shards.failed");
            if (failedShards > 0) {
                LOG.error(String.format("查询 total shards:{%s}, failed shards:{%s}", totalShards, failedShards));
            }
            int hitTotal = c.getInt("hits.total");
            List<Map> datas = c.getList("hits.hits", Map.class);
            datas.stream().forEach(map -> {
                result.add((Map) map.get("_source"));
            });
            LOG.info(String.format("查询到符合条件的总记录数：{%s}，本次查询返回记录数：{%s}", hitTotal, result.size()));
        }
        return result;
    }

    /**
     * 获取索引的type定义，构建成Configuration对象返回
     *
     * @param indices   索引名数组
     * @param typeNames 索引的类型名数组，可以为空，为空则返回indices的所有type信息
     * @return <indexName, <typeName, Configuration>>
     */
    public Map<String, Map<String, Configuration>> getMapping(String[] indices, String... typeNames) {
        Map<String, Map<String, Configuration>> result = null;
        Configuration configuration = null;
        Assert.notEmpty(indices, "存放索引名的数组不能为空！");
        final StringBuilder url = new StringBuilder("/");
        IntStream.range(0, indices.length).forEach(i -> {
            if (i == 0) {
                url.append(indices[i]);
            } else {
                url.append(",").append(indices[i]);
            }
        });

        url.append("/_mapping/");
        if (typeNames != null && typeNames.length > 0) {
            IntStream.range(0, typeNames.length).forEach(i -> {
                if (i == 0) {
                    url.append(typeNames[i]);
                } else {
                    url.append(",").append(typeNames[i]);
                }
            });
        }

        try {
            Response response = restClient.performRequest("GET", url.toString());
            configuration = createConfigFromResponseData(response);
            if (configuration != null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("\ngetMapping:\n" + configuration.toJSONPrettyFormat());
                }
                result = new HashMap<>();
                for (Object key : ((Map) configuration.getInternal()).keySet()) {
                    String indexName = (String) key;
                    Map mappings = (Map) configuration.getMap(indexName).get("mappings");
                    Map<String, Configuration> typeConfig = new HashMap<>();
                    for (Object k : mappings.keySet()) {
                        String typeName = (String) k;
                        typeConfig.put(typeName, Configuration.from((Map) mappings.get(typeName)));
                    }
                    result.put(indexName, typeConfig);
                }
            }
        } catch (Exception e) {
            LOG.error("获取mapping信息异常：", e);
        }
        return result;
    }

    public Configuration getAllMapping() throws IOException {

        Response response = restClient.performRequest("GET", "/_mapping");
        Configuration configuration = createConfigFromResponseData(response);
        if (LOG.isDebugEnabled()) {
            if (configuration != null) {
                LOG.debug("\ngetAllMapping:\n" + configuration.toJSONPrettyFormat());
            }
        }
        return configuration;
    }

    public RestClient getRestClient() {
        return restClient;
    }

    public void setRestClient(RestClient restClient) {
        this.restClient = restClient;
    }

    /**
     * 关闭rest client 释放资源
     */
    public void close() {
        try {
            if (restClient != null) {
                restClient.close();
                LOG.info(String.format("关闭rest client客户端[%s]完成！", restClient));
            }
        } catch (Exception e) {
            LOG.error(String.format("关闭rest client客户端[%s]异常！", restClient), e);
        }
    }

    /**
     * 根据索引名称和JSON格式的参数创建索引
     *
     * @param indexName 索引名称
     * @param settings  JSON格式的索引参数
     * @return
     */
    public boolean createIndex(String indexName, String settings) {
        Assert.hasText(indexName, "indexName不能为空！");
        Assert.hasText(settings, "dslStatement不能为空！");
        boolean success = false;
        String url = "/" + indexName;
        HttpEntity httpEntity = new StringEntity(settings, ContentType.APPLICATION_JSON);
        try {
            Response response = restClient.performRequest("PUT", url, Collections.emptyMap(), httpEntity);
            Configuration c = createConfigFromResponseData(response);
            if (c != null) {
                success = c.getBool("acknowledged");
                if (LOG.isDebugEnabled()) {
                    LOG.debug(String.format("\ncreateIndex: %s\n %s", indexName, c.toJSONPrettyFormat()));
                }
            }
        } catch (Exception e) {
            LOG.error(String.format("创建索引index[%s]失败!", indexName), e);
        }
        return success;
    }

    /**
     * 根据索引名称删除索引，多个用逗号分隔
     *
     * @param indexName
     * @return
     */
    public boolean deleteIndex(String indexName) {
        Assert.hasText(indexName, "indexName不能为空！");
        boolean success = false;
        try {
            Response response = restClient.performRequest("DELETE", "/" + indexName);
            Configuration c = createConfigFromResponseData(response);
            if (c != null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(String.format("\ndeleteIndex: %s\n %s", indexName, c.toJSONPrettyFormat()));
                }
                success = c.getBool("acknowledged");
            }
        } catch (Exception e) {
            LOG.error("删除索引失败!", e);
        }
        return success;
    }

    /**
     * 根据索引名获取索引的定义文本（JSON格式）
     *
     * @param indexName
     * @return
     */
    public String getIndex(String indexName) {
        Assert.hasText(indexName, "indexName不能为空！");
        String result = null;
        try {
            Response response = restClient.performRequest("GET", "/" + indexName);
            result = getResponseText(response);
        } catch (Exception e) {
            LOG.error("获取索引信息异常：", e);
        }
        return result;
    }

    /**
     * 根据索引名称获取索引的定义，并构建成Configuration对象返回
     *
     * @param indexName
     * @return
     */
    public Configuration getIndexConfiguration(String indexName) {
        Assert.hasText(indexName, "indexName不能为空！");
        Configuration result = null;
        try {
            Response response = restClient.performRequest("GET", "/" + indexName);
            result = createConfigFromResponseData(response);
        } catch (Exception e) {
            LOG.error("获取索引信息异常!", e);
        }
        return result;
    }

    /**
     * 检查索引是否存在，存在返回true，否则返回false
     *
     * @param indexName 索引名称
     * @return
     */
    public boolean indexExists(String indexName) {
        Assert.hasText(indexName, "indexName不能为空！");
        boolean exists = false;
        try {
            Response response = restClient.performRequest("HEAD", "/" + indexName);
            if (response.getStatusLine() != null && response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                exists = true;
            }
        } catch (Exception e) {
            LOG.error("检查索引是否存在失败!", e);
        }
        return exists;
    }

    public boolean putMapping(String indexName, String typeName, String settings) {
        Assert.hasText(indexName, "indexName 不能为空！");
        Assert.hasText(typeName, "typeName 不能为空！");
        Assert.hasText(settings, "settings 不能为空！");

        boolean success = false;
        String url = "/" + indexName + "/_mapping/" + typeName;
        HttpEntity httpEntity = new StringEntity(settings, ContentType.APPLICATION_JSON);
        try {
            Response response = restClient.performRequest("PUT", url, Collections.emptyMap(), httpEntity);
            Configuration c = createConfigFromResponseData(response);
            if (c != null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(String.format("\nputMapping: %s\n%s", typeName, c.toJSONPrettyFormat()));
                }
                success = c.getBool("acknowledged");
            }
        } catch (Exception e) {
            LOG.error(String.format("创建index[%s], type[%s]失败!", indexName, typeName), e);
        }
        return success;
    }

    /**
     * 获取集群上的所有索引名列表
     *
     * @return
     */
    public List<String> getAllIndexName() {
        List<String> indexNames = new ArrayList<>();
        try {
            Response response = restClient.performRequest("GET", "/_all/_settings");
            Configuration c = createConfigFromResponseData(response);
            if (c != null) {
                Map<String, Object> m = (Map) c.getInternal();
                for (String key : m.keySet()) {
                    indexNames.add(key);
                }
                if (LOG.isDebugEnabled()) {
                    LOG.debug(String.format("\ngetAllIndexName:\n%s\nindexNames:%s", c.toJSONPrettyFormat(), indexNames));
                }
            }
        } catch (Exception e) {
            LOG.error("查询索引名称失败!", e);
        }
        return indexNames;
    }

    /**
     * 获取集群上的所有索引名列表
     *
     * @return
     */
    public List<String> getAllTypeNamesOfIndex(String indexName) {
        Assert.hasText(indexName, "indexName 不能为空！");
        List<String> typeNames = new ArrayList<>();
        try {
            Response response = restClient.performRequest("GET", "/" + indexName + "/_mappings");
            Configuration c = createConfigFromResponseData(response);
            if (c != null) {
                Map map = c.getMap(indexName + ".mappings");
                for (Object key : map.keySet()) {
                    typeNames.add((String) key);
                }
                if (LOG.isDebugEnabled()) {
                    LOG.debug(String.format("\ngetAllTypeNamesOfIndex:\n%s\nindexName:%s", JSON.toJSON(map), indexName));
                }
            }
        } catch (Exception e) {
            LOG.error("查询索引类型名称失败!", e);
        }
        return typeNames;
    }
}