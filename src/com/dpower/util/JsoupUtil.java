package com.dpower.util;

import java.io.File;
import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * ʹ��JSOUP����HTML
 * @author LiChao
 *
 */
public class JsoupUtil {
	
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
		        Elements tdlist=tables.get(0).getElementsByTag("td");
		        StringBuffer bufferText =new StringBuffer();
		        //�����Ԫ���е�����
		        for (Element str : tdlist) {
		        	bufferText.append(str.text());
		        }
		        return bufferText.toString().trim();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

}
