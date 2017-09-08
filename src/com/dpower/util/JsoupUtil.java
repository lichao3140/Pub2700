package com.dpower.util;

import java.io.File;
import java.io.IOException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import android.util.Log;

/**
 * ʹ��JSOUP����HTML
 * @author LiChao
 *
 */
public class JsoupUtil {
	private static final String TAG = "JsoupUtil";
	/**
	 * ��URL���ؽ���HTML�ļ�
	 */
	public static void getInfoFromUrl(final String url){
        new Thread(new Runnable() {
            
            @Override
            public void run() {
                try {
                    Document doc = Jsoup.connect(url).get();		   
                    Elements tables=doc.getElementsByTag("table");
    		        Elements tdlist=tables.get(0).getElementsByTag("td");
    		        StringBuffer bufferText =new StringBuffer();
    		        for (Element str : tdlist) {
    		        	bufferText.append(str.text());
    		        }
                } catch (Exception e) {
                	e.printStackTrace();
                }
            }
        }).start();
    }
	
	/**
	 * �ӱ���HTML�ļ����ؽ���
	 * @param filesrc HTML�ļ���ַ
	 * @return
	 */
	public static String getInfoFromFile(String filesrc) {
		if(filesrc != null) {
			try {
				File html = new File(filesrc);
				Document doc = Jsoup.parse(html, "UTF-8");
				//��ȡHTML�ļ����������				
				Elements tables=doc.getElementsByTag("table");
				if(!tables.toString().equals("")) {
					Elements tdList=tables.get(0).getElementsByTag("td");
					StringBuffer bufferText =new StringBuffer();
					Log.i(TAG, "jsoup table:" + tables.toString());
					//�����Ԫ���е�����
					for (Element str : tdList) {
						bufferText.append(str.text());
					}
					return bufferText.toString().trim();	
				} else {
					Elements pList=doc.getElementsByTag("p");
					StringBuffer bufferText =new StringBuffer();
					Log.i(TAG, "jsoup p:" + pList.toString());
					//�����Ԫ���е�����
					for (Element str : pList) {
						bufferText.append(str.text());
					}
					return bufferText.toString().trim();	
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

}
