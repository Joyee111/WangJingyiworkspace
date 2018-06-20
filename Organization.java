package com.rh.esb;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import com.rh.core.base.Bean;
import com.rh.core.base.Context;
import com.rh.core.serv.CommonServ;
import com.rh.core.serv.ParamBean;
import com.rh.core.serv.ServDao;
import com.rh.core.util.JsonUtils;

public class Organization extends CommonServ{
	public static HttpParams createDefaultHttpParams() {
        HttpParams httpParameters = new BasicHttpParams();
        //连接服务器超时时间：5秒钟
        int connTime = Context.getSyConf("HTTP_POST_CONECT_TIME", 5);
        HttpConnectionParams.setConnectionTimeout(httpParameters, connTime * 1000);
        //2分钟没返回则超时
        int rtnTime = Context.getSyConf("HTTP_POST_RETURN_TIME", 2);
        HttpConnectionParams.setSoTimeout(httpParameters, rtnTime * 60 * 1000);  
        return httpParameters;
    }
	/**
	 * 向esb系统推送组织机构信息
	 * @return
	 */
	public void mechanismTransmission(){
		//查询组织机构信息
		ParamBean paramBean = new ParamBean();
		paramBean.setSelect("DEPT_CODE CODE,DEPT_PCODE PARENTCODE,DEPT_NAME NAME,DEPT_TYPE TYPE");
		paramBean.setWhere("AND DEPT_CODE='24'");
		List<Bean> deptBean = ServDao.finds("SY_ORG_DEPT_ALL", paramBean);
		for (Bean dept : deptBean) {
			dept.set("uuid", UUID.randomUUID().toString().replaceAll("-", ""));//插入uuid
			if("".equals(dept.getStr("PARENTCODE"))){
				dept.set("PARENTCODE", "0");
			}
        }
		//构造参数
		Bean params=new Bean();
		params.set("uuid", UUID.randomUUID().toString().replaceAll("-", ""));
		//params.set("dataInfo", JsonUtils.toBeanList(JsonUtils.toJson(deptBean).toLowerCase()));
		params.set("dataInfo", deptBean);
		//调接口
		HttpPost httpost = new HttpPost("http://10.10.120.106:8080/services/organization");
		HttpClient client = new DefaultHttpClient(createDefaultHttpParams());
		HttpResponse httpresponse=null;
		try {
			System.out.println(JsonUtils.toJson(params).replaceAll("PARENTCODE", "parentCode").replaceAll("CODE", "code").replaceAll("NAME", "name").replaceAll("TYPE", "type"));
			StringEntity entityKey = new StringEntity(JsonUtils.toJson(params).replaceAll("PARENTCODE", "parentCode").replaceAll("CODE", "code").replaceAll("NAME", "name").replaceAll("TYPE", "type"), HTTP.UTF_8);
			entityKey.setContentType("application/json;charset=utf-8");
			httpost.setEntity(entityKey);
			httpresponse = client.execute(httpost);
			HttpEntity entity = httpresponse.getEntity();
			String result = EntityUtils.toString(entity);
			System.out.println(result);
			List<Bean> listBean = new ArrayList<Bean>();
			if(result.startsWith("[")){
				listBean = JsonUtils.toBeanList(result);
				
			}
		} catch (Exception e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
      }
	}
}
