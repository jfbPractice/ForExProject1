package com.foreignexproject;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpotRateActivity extends ListActivity
{
    public final static String EXTRA_BANK_NAME="bank_name";
    public final static String EXTRA_BANK_PAGE_URL="page_url";
    public final static String EXTRA_BANK_OPTION="option";

    String bankName , pageUrl , option , time;

    Context context;
    ArrayList<RateBean> rateItem;
    MyHandler handler ;
    SpotAdapter adapter ;

    TextView updateTime;
    ProgressBar pb;
    ListView listView;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spot_rate);
        context=this;
        pb=(ProgressBar)findViewById(R.id.progress_bar);

        getInformation();
        main();
    }

    public void getInformation()
    {
        bankName=getIntent().getStringExtra(EXTRA_BANK_NAME);
        pageUrl=getIntent().getStringExtra(EXTRA_BANK_PAGE_URL);
        option=getIntent().getStringExtra(EXTRA_BANK_OPTION)+"(Telegraphic Rate)";

        setTitle(bankName + "-" + option);
        pb.setVisibility(View.VISIBLE);
    }

    public void main()
    {
        handler = new MyHandler();
        Button button=(Button)findViewById(R.id.fresh_button);
        listView=(ListView)findViewById(android.R.id.list);

        if(bankName.equals("台灣銀行"))
        {
            new bankOfTaiwanSpotThread().start();
        }
        if(bankName.equals("玉山銀行"))
        {
            new EsunSpotThread().start();
        }
        if(bankName.equals("中國信託商銀"))
        {
            new CTBCSpotThread().start();
        }
        if(bankName.equals("合作金庫銀行"))
        {
            new TCBbankSpotThread().start();
        }
        if(bankName.equals("京城銀行"))
        {
            new KTBankSpotThread().start();
        }

        button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                pb.setVisibility(View.VISIBLE);
                if(bankName.equals("台灣銀行"))
                {
                    new bankOfTaiwanSpotThread().start();
                }
                if(bankName.equals("玉山銀行"))
                {
                    new EsunSpotThread().start();
                }
                if(bankName.equals("中國信託商銀"))
                {
                    new CTBCSpotThread().start();
                }
                if(bankName.equals("合作金庫銀行"))
                {
                    new TCBbankSpotThread().start();
                }
                if(bankName.equals("京城銀行"))
                {
                    new KTBankSpotThread().start();
                }
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                Toast.makeText(context,"長按可查詢分行位置",Toast.LENGTH_LONG).show();
            }
        });

        listView.setOnItemLongClickListener(new OnItemLongClickListener()
        {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
            {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                if(bankName.equals("台灣銀行"))
                {
                    intent.setData(Uri.parse("http://bot.map.com.tw/search_engine/taiwan_map.asp"));
                }
                if(bankName.equals("玉山銀行"))
                {
                    intent.setData(Uri.parse("http://www.esunbank.com.tw/service/branch.info"));
                }
                if(bankName.equals("中國信託商銀"))
                {
                    intent.setData(Uri.parse("https://www.ctbcbank.com/CTCBPortalWeb/appmanager/ebank/rb?_nfpb=true&_pageLabel=TW_RB_CM_ebank_022001"));
                }
                if(bankName.equals("合作金庫銀行"))
                {
                    intent.setData(Uri.parse("http://www.tcb-bank.com.tw/brief_introduction/servicesloc/Pages/branch_locations.aspx"));
                }
                if(bankName.equals("京城銀行"))
                {
                    intent.setData(Uri.parse("http://www.ktb.com.tw/location.htm"));
                }
                startActivity(intent);
                return true;
            }
        });

    }

    class MyHandler extends Handler
    {
        @Override
        public void handleMessage(Message m)
        {
            if(m.what==0)
            {
                updateTime.setText("匯率更新時間：\n" + time);
                setListAdapter(adapter);
                pb.setVisibility(View.INVISIBLE);
            }
        }
    }

    class bankOfTaiwanSpotThread extends Thread
    {
        @Override
        public void run()
        {
            try
            {
                updateTime=(TextView)findViewById(R.id.updateTime);
                RateBean rate;
                int i =2;
                String currency="" ;
                rateItem =new ArrayList<>();

                Document doc= Jsoup.connect(pageUrl).get();

                String temp=doc.select("td[style=width:326px;text-align:left;vertical-align:top;color:#0000FF;font-size:11pt;font-weight:bold;]").text();
                time=temp.substring(12);


                for(Element e : doc.select("td.titleLeft"))
                {
                    currency=e.text();
                    if(i<doc.select("td.decimal").size())
                    {
                        rate=new RateBean( currency,
                                doc.select("td.decimal").eq(i++).text(),
                                doc.select("td.decimal").eq(i++).text());
                        i=i+2;
                        rateItem.add(rate);
                    }
                }
                adapter=new SpotAdapter(context,rateItem);
                handler.sendEmptyMessage(0);
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    class EsunSpotThread extends Thread
    {
        @Override
        public void run()
        {
            try
            {
                updateTime=(TextView)findViewById(R.id.updateTime);
                RateBean rate;
                rateItem =new ArrayList<>();

                Document doc= Jsoup.connect(pageUrl).get();
                Elements select_option = doc.select("[selected]");
                String temp=select_option.text();
                time=temp.substring(12);

                for(Element title : doc.select("tr.tableContent-light"))
                {
                    int i =3;
                    Elements tds = title.getElementsByTag("td");
                    rate=new RateBean(tds.eq(i++).text(), tds.eq(i++).text(),tds.eq(i).text());
                    rateItem.add(rate);
                }
                adapter=new SpotAdapter(context,rateItem);
                handler.sendEmptyMessage(0);
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    class CTBCSpotThread extends Thread
    {
        private boolean numTester(String str)
        {
            Pattern p = Pattern.compile(".[0-9]{1,10}");
            Matcher m = p.matcher(str);
            if(m.matches())
            {
                return true;
            }
            return false;
        }
        @Override
        public void run()
        {
            try
            {
                updateTime=(TextView)findViewById(R.id.updateTime);
                RateBean rate;
                rateItem =new ArrayList<>();

                Document doc= Jsoup.connect(pageUrl).get();
                Elements temp = doc.select("td[colspan]");
                time = temp.text().substring(6);

                int i =0;
                String currency="",buyIn="",soldOut="";
                for(Element title:doc.select("td.column_text"))
                {
                    Elements td=doc.select("td.column_text");
                    if(i<100)
                    {
                        currency=td.eq(i).text();
                        buyIn=td.eq(i=i+3).text();
                        soldOut=td.eq(i=i+1).text();
                        if(numTester(buyIn) || numTester(soldOut))
                        {
                            if(Double.valueOf(buyIn)<1.0)
                            {
                                buyIn="0"+buyIn;
                            }
                            if(Double.valueOf(soldOut)<1.0)
                            {
                                soldOut="0"+soldOut;
                            }
                        }
                        rate=new RateBean(currency, buyIn,soldOut);
                        rateItem.add(rate);
                        i++;
                    }
                }
                adapter=new SpotAdapter(context,rateItem);
                handler.sendEmptyMessage(0);
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    class TCBbankSpotThread extends Thread
    {
        @Override
        public void run()
        {
            try
            {
                updateTime=(TextView)findViewById(R.id.updateTime);
                RateBean rate;
                rateItem =new ArrayList<>();

                Document doc= Jsoup.connect(pageUrl).get();

                time=doc.select("span[id=ctl00_PlaceHolderEmptyMain_PlaceHolderMain_fecurrentid_lblDate]").text().substring(5,14)+"\t"+
                        doc.select("span[id=ctl00_PlaceHolderEmptyMain_PlaceHolderMain_fecurrentid_lblDate]").text().substring(20);


                String currency="",buyIn="",soldOut="";
                int j =0;
                for(Element title : doc.select("table.table"))
                {
                    for(Element trs :title.getElementsByTag("tr"))
                    {
                        if(j>=1)
                        {
                            int i = 0;
                            for(Element tds:trs.getElementsByTag("td"))
                            {
                                if((j%2!=0)&&(i==0||i==2))
                                {
                                    if(i==0)
                                    {
                                        currency=tds.text();
                                    }
                                    else
                                    {
                                        buyIn=tds.text();
                                    }
                                }
                                if((j%2==0)&&(i==2))
                                {
                                    soldOut=tds.text();
                                    rate = new RateBean(currency,buyIn,soldOut);
                                    rateItem.add(rate);
                                }
                                i++;
                            }
                        }
                        j++;
                    }
                }
                adapter=new SpotAdapter(context,rateItem);
                handler.sendEmptyMessage(0);
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    class KTBankSpotThread extends Thread
    {
        @Override
        public void run()
        {
            try
            {
                updateTime=(TextView)findViewById(R.id.updateTime);
                RateBean rate;
                rateItem =new ArrayList<>();

                Document doc= Jsoup.connect(pageUrl).get();

                String temp=doc.select("td>b").eq(0).text();
                time = temp.substring(5, 15)+"\t"+temp.substring(28,temp.length());

                String currency="",buyIn="",soldOut="";
                int j =0;
                for(Element title :doc.select("table.ts"))
                {
                    for (Element trs : title.getElementsByTag("tr"))
                    {
                        int i = 0;
                        if (j >= 2 && j < 13)
                        {
                            for (Element tds : trs.getElementsByTag("td"))
                            {
                                if (i == 0 || i == 2 || i == 3)
                                {
                                    if(i==0)
                                    {
                                        currency=tds.text();
                                    }
                                    else if(i==2)
                                    {
                                        buyIn=tds.text();
                                    }
                                    else
                                    {
                                        soldOut=tds.text();
                                    }
                                }
                                i++;
                            }
                            rate = new RateBean(currency,buyIn,soldOut);
                            rateItem.add(rate);
                        }
                        System.out.println("j=" + j);
                        j++;
                    }
                }
                adapter=new SpotAdapter(context,rateItem);
                handler.sendEmptyMessage(0);
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }
    }



}