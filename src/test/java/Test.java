import com.alibaba.fastjson.JSON;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ${DESCRIPTION}
 *
 * @author hc.zeng
 * @create 2018-10-16 15:41
 */

public class Test {
    public static void main(String[] args) {
        Map<String,String[]> root = new HashMap<String,String[]>();
        root.put("R1",new String[]{"#"});
        root.put("R2",new String[]{"#","R1"});
        root.put("R3",new String[]{"#","R1","R2"});

        root.put("S1",new String[]{"#"});
        root.put("S2",new String[]{"#","S1"});
        root.put("S3",new String[]{"#","S1","S2"});

        String[] ch = new String[]{"R1","R3","R2","S3","S2","S1"};
        Map<String,Map> rel = new HashMap<>();
        for (int i = 0; i <ch.length ; i++) {
            String id = ch[i];
            rcl2(rel,id,root.get(id),1);
        }
        System.out.println(JSON.toJSONString(rel));
        List<Map> list = change(rel);
        System.out.println(JSON.toJSONString(list));
    }

    private static void rcl2(Map<String,Map> rel,String id,String[] res,int index){
        if(index >= res.length){
            if(!rel.containsKey(id)){
                rel.put(id,new HashMap());
            }
            return;
        }
        Map<String,Map> rel2 ;
        if(!rel.containsKey(res[index])){
            rel2 = new HashMap<>();
            rel.put(res[index],rel2);
        }else {
            rel2 = rel.get(res[index]);
        }
        rcl2(rel2,id,res,++index);
    }

    private static List<Map> change(Map map){
        List<Map> dataList = new ArrayList<>();
        map.forEach((key,vm)->{
            Map dataMap = new HashMap();
            dataMap.put("mid",key);
            List<Map> list = null;
            if(vm!=null && !((Map)vm).isEmpty()){
                list = change((Map)vm);
            }
            if(list!=null){
                dataMap.put("chlist",list);
            }
            dataList.add(dataMap);
        });
        return dataList;
    }


}
