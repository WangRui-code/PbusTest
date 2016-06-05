package com.assassin.pbustest;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.support.annotation.Nullable;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.overlayutil.TransitRouteOverlay;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteLine;
import com.baidu.mapapi.search.route.TransitRoutePlanOption;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRouteResult;
import java.util.List;
import static android.R.layout.simple_spinner_item;

public class FragmentQuery extends Fragment {

    private EditText from;
    private EditText to;
    private Spinner search;
    private MapView mapView;
    private BaiduMap mBaidumap;
    private RoutePlanSearch mSearch;
    private TextView tx;
    private String ins;
    private String city="苏州";
    private String fstation="";
    private String tstation="";
    private String results="";

    @Nullable
    @Override
    public View onCreateView   (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_query, container, false);
        from = (EditText) view.findViewById(R.id.from_et);
        to = (EditText) view.findViewById(R.id.to_et);
        search = (Spinner) view.findViewById(R.id.search_sp);
        tx= (TextView) view.findViewById(R.id.tx);

        mSearch = RoutePlanSearch.newInstance();
        mSearch.setOnGetRoutePlanResultListener(new OnGetRoutePlanResultListener() {
            @Override
            public void onGetWalkingRouteResult(WalkingRouteResult walkingRouteResult) {

            }
            @Override
            public void onGetTransitRouteResult(TransitRouteResult result) {

                if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
                    Toast.makeText(getActivity(), "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
                }
                if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
                    //起终点或途经点地址有岐义，通过以下接口获取建议查询信息
                    //result.getSuggestAddrInfo()
                    return;
                }
                if (result.error == SearchResult.ERRORNO.NO_ERROR) {
                    Toast.makeText(getActivity(), result.toString(), Toast.LENGTH_SHORT).show();

                    ins="";
                    List<TransitRouteLine> allLine=result.getRouteLines();//获取当前路线的每一段
                    TransitRouteLine aLine=allLine.get(0);
                    List<TransitRouteLine.TransitStep>allStep=aLine.getAllStep();
                    for (TransitRouteLine.TransitStep step : allStep) {
                        ins=ins+step.getInstructions()+"";//获取该段路的说明
                    }
                    tx.setText(ins);
                    results=ins;
                    int totalLine = result.getRouteLines().size();
//                   // Toast.makeText(getActivity(), "共查到" + totalLine + "条路线"
//                            , Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onGetDrivingRouteResult(DrivingRouteResult drivingRouteResult) {

            }
        });
        String[] strings={"不含地铁","时间优先","最少换乘","最少步行距离"};
        ArrayAdapter<String> transitAdapter;
        transitAdapter = new ArrayAdapter<String>(getActivity(),
                simple_spinner_item, strings);
        search.setAdapter(transitAdapter);
        search.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {


            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 1:search_init(TransitRoutePlanOption.TransitPolicy.EBUS_NO_SUBWAY);
                        break;
                    case 2:search_init(TransitRoutePlanOption.TransitPolicy.EBUS_TRANSFER_FIRST);
                        break;
                    case 3:search_init(TransitRoutePlanOption.TransitPolicy.EBUS_WALK_FIRST);
                        break;
                    case 4:search_init(TransitRoutePlanOption.TransitPolicy.EBUS_NO_SUBWAY);
                        break;
                }
                fstation=from.getText().toString();
                tstation=to.getText().toString();
                results=ins;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        return view;
    }
    protected void search_init(TransitRoutePlanOption.TransitPolicy transitPolicy){

        TransitRoutePlanOption transitRoutePlanOption=new TransitRoutePlanOption();
        transitRoutePlanOption.city(city);
        transitRoutePlanOption.policy(transitPolicy);
        transitRoutePlanOption.from(PlanNode.withCityNameAndPlaceName(city,from.getText().toString()));
        transitRoutePlanOption.to(PlanNode.withCityNameAndPlaceName(city,to.getText().toString()));
        mSearch.transitSearch(transitRoutePlanOption);

    }
    public void onResume() {
        super.onResume();
        from.setText(fstation);
        to.setText(tstation);
        tx.setText(results);
//        mapView.onResume();
    }


    public void onPause() {
        super.onPause();
//        mapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSearch.destroy();// 释放检索实例
//        mapView.onDestroy();
    }
}

