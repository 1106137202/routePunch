package com.example.routepunch;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.routepunch.databinding.ActivityMapsBinding;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    public GoogleMap mMap;
    private ActivityMapsBinding binding;
    private static final int MY_PERMISSION_ACCESS_COARSE_LOCATION = 11;
    private String commandStr;
    private LocationManager locationManager;
    private double lat = 0;
    private double lng = 0;
//    private MapsLayout mapsLayout;
    private static LatLng[] stationArray;
    private static String[] stationName;
    private static double[] staLat;
    private static double[] staLng;
    private static String token = "";
    private static double stationLat = 0;
    private static double stationLng = 0;
    private static String[] address;
    private static String[] attr = {"OK", "NG"};


    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        binding = ActivityMapsBinding.inflate(getLayoutInflater());
//        setContentView(binding.getRoot());

        //取得layoutInflater服務
        LayoutInflater layoutInflater = ((Activity)this).getLayoutInflater();
        //將activity_maps xml的layout放進View裡面
        View main = layoutInflater.inflate(R.layout.activity_maps, null);
        //將button xml放進View裡面
        View imgbutton = layoutInflater.inflate(R.layout.img_button, null);
        //取得xml裡面中的RelativeLayout,記得設定ID取得
        RelativeLayout RL = (RelativeLayout) imgbutton.findViewById(R.id.imgbutton);
        //將Map的View放進Relativelayout面,設定成MATCH_PARENT
        RL.addView(main, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        //最後放進主頁面裡面
        setContentView(RL);
        // 0btain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

//        mapsLayout = new MapsLayout(this);
//        setContentView(mapsLayout);
//        Button btnPunch = mapsLayout.btnPunch;
    }

    //    public class MapsLayout extends RelativeLayout {
//
//        public Button btnPunch;
//
//        @SuppressLint("ResourceType")
//        public MapsLayout(Context context) {
//            super(context);
//
//            LayoutInflater layoutInflater = ((Activity) context).getLayoutInflater();
//            View view = layoutInflater.inflate(R.layout.activity_maps, null);
//            this.addView(view, new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
//
//            RelativeLayout layout = new RelativeLayout(context);
//            layout.setId(0x111);
//            RelativeLayout.LayoutParams btParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
//            layout.setLayoutParams(btParams);
//            layout.setPadding(dp2px(20), dp2px(5), dp2px(20), dp2px(5));
//
//
//            btnPunch = new Button(context);
//            btnPunch.setId(0x1003);
//            RelativeLayout.LayoutParams navigateButtonLayoutParams = new LayoutParams(dp2px(120), LayoutParams.WRAP_CONTENT);
//            navigateButtonLayoutParams.topMargin = dp2px(110);
//            btnPunch.setLayoutParams(navigateButtonLayoutParams);
//            btnPunch.setTextSize(20);
//            btnPunch.setTextColor(0xffffffff);
//            btnPunch.setText("打卡");
//            btnPunch.setGravity(Gravity.CENTER);
//            btnPunch.setPadding(dp2px(20), dp2px(5), dp2px(20), dp2px(5));
//            layout.addView(btnPunch);
//            this.addView(layout);
//        }
//
//        public int dp2px(float dpValue) {
//            final float scale = getResources().getDisplayMetrics().density;
//            return (int) (dpValue * scale + 0.5f);
//        }
//    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //設定監聽移動距離做更新(單位:m)
        long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0;
        //設定秒數做更新(單位:ms)
        long MIN_TIME_BW_UPDATES = 1000;
        commandStr = LocationManager.NETWORK_PROVIDER;
        if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(MapsActivity.this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    MY_PERMISSION_ACCESS_COARSE_LOCATION);
            return;
        }

        Polyline polyline1 = googleMap.addPolyline(new PolylineOptions().clickable(true)
                .add(new LatLng(22.651715, 120.312492),
                        new LatLng(22.650310,120.314846),
                        new LatLng(22.650507,120.317137),
                        new LatLng(22.651715, 120.312492)));


//        PolylineOptions polylineTest = new PolylineOptions().color(0xfffd8358).width(5);
//        polylineTest.add(new LatLng(22.339701,120.892659),
//                new LatLng(22.339701,120.892659),
//                new LatLng(22.637663, 120.306303),
//                new LatLng(22.651715, 120.312492));


        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        //declare the locationListener
        LocationListener locationListenerGPS = new LocationListener() {
            @Override
            public void onLocationChanged(android.location.Location location) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                String msg = "New Latitude: " + latitude + "New Longitude: " + longitude;
                System.out.println(msg);
                LatLng HOME = new LatLng(latitude, longitude);
                String now = HOME.toString();

                int height = 100;
                int width = 100;
                BitmapDrawable bitmapdraw = (BitmapDrawable)ContextCompat.getDrawable(MapsActivity.this, R.drawable.person);;
                Bitmap b = bitmapdraw.getBitmap();
                Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);

                mMap.addMarker(new MarkerOptions().position(HOME).title("目前")
                        // below line is use to add custom marker on our map.
                        .icon(BitmapDescriptorFactory.fromBitmap(smallMarker)));
                PolylineOptions polylineOpt = new PolylineOptions().color(0xfffd8364).width(5);
                polylineOpt.add(new LatLng(lat, lng));
                polylineOpt.add(HOME);
                mMap.addPolyline(polylineOpt);
                lat = latitude;
                lng = longitude;
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(HOME, 15));

            }
        };

        //設定更新速度與距離
        locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                MIN_TIME_BW_UPDATES,
                MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerGPS);

        Location location = locationManager.getLastKnownLocation(commandStr);
        lat = location.getLatitude();
        lng = location.getLongitude();

        System.out.println(lat);
        System.out.println(lng);
        LatLng HOME = new LatLng(lat, lng);
        int height = 100;
        int width = 100;
        BitmapDrawable bitmapdraw = (BitmapDrawable)ContextCompat.getDrawable(MapsActivity.this, R.drawable.person);;
        Bitmap b = bitmapdraw.getBitmap();
        Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);
        mMap.addMarker(new MarkerOptions().position(HOME).title("目前")
                // below line is use to add custom marker on our map.
                .icon(BitmapDescriptorFactory.fromBitmap(smallMarker)));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(HOME, 15.0f));

        //----各站打點----
        new Thread(new Runnable() {
            @Override
            public void run() {
                login(lat, lng);
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    public void run() {
                        for (int i = 0; i < stationArray.length; i++) {
                            int height = 100;
                            int width = 100;
                            BitmapDrawable bitmapdraw = (BitmapDrawable) ContextCompat.getDrawable(MapsActivity.this, R.drawable.charging);
                            Bitmap b = bitmapdraw.getBitmap();
                            Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);
                            mMap.addMarker(new MarkerOptions().position(stationArray[i]).title(stationName[i])
                                    .icon(BitmapDescriptorFactory.fromBitmap(smallMarker)));
                        }
                        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                            @Override
                            public boolean onMarkerClick(Marker marker) {
                                BitmapDrawable bitmapdraw = (BitmapDrawable) ContextCompat.getDrawable(MapsActivity.this, R.drawable.point);
                                Bitmap b = bitmapdraw.getBitmap();
                                Bitmap point = Bitmap.createScaledBitmap(b, width, height, false);
                                LatLng station = marker.getPosition();
                                AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
                                builder.setMessage("打卡位置：高雄市左營區文學路296號" + "\n目前位置：" + geo(lat, lng) + "\n確定在此點打卡？");
//                                Log.d("TAG", geo(stationLat, staLng) + station.toString());
//                                Log.d("TAG1", geo(HOME) + HOME.toString());
                                builder.setPositiveButton("確定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                        if (cal_distence(station, HOME) <= 100) {
                                            AlertDialog.Builder attri = new AlertDialog.Builder(MapsActivity.this);
                                            attri.setTitle("問題點狀態\n(3206)Chg_Circuit_Failed");
                                            attri.setSingleChoiceItems(attr, -1, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    switch(i){
                                                        case 0:
                                                            Toast.makeText(MapsActivity.this, "OK", Toast.LENGTH_SHORT).show();
                                                            break;
                                                        case 1:
                                                            Toast.makeText(MapsActivity.this, "NG", Toast.LENGTH_SHORT).show();
                                                            break;
                                                    }
                                                }
                                            });
                                            attri.setPositiveButton("確定", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    AlertDialog.Builder attrib = new AlertDialog.Builder(MapsActivity.this);
                                                    attrib.setTitle("問題點狀態\n(3003)Charging_BackDoor_Opened");
                                                    attrib.setSingleChoiceItems(attr, -1, new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialogInterface, int i) {
                                                            switch(i){
                                                                case 0:
                                                                    Toast.makeText(MapsActivity.this, "OK", Toast.LENGTH_SHORT).show();
                                                                    break;
                                                                case 1:
                                                                    Toast.makeText(MapsActivity.this, "NG", Toast.LENGTH_SHORT).show();
                                                                    break;
                                                            }
                                                        }
                                                    });
                                                    attrib.setPositiveButton("確定", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialogInterface, int i) {
                                                            AlertDialog.Builder memo = new AlertDialog.Builder(MapsActivity.this);
                                                            final EditText editText = new EditText(MapsActivity.this);
                                                            memo.setView(editText);
                                                            memo.setTitle("請輸入備註：");
                                                            memo.setPositiveButton("確定", new DialogInterface.OnClickListener() {
                                                                @Override
                                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                                    new Thread(new Runnable() {
                                                                        @Override
                                                                        public void run() {
                                                                            punchInPoint(token, lat, lng);
                                                                        }
                                                                    }).start();
                                                                    Toast.makeText(MapsActivity.this, "已打卡", Toast.LENGTH_SHORT).show();
                                                                    marker.remove();
                                                                    mMap.addMarker(new MarkerOptions().position(station).title("目前")
                                                                            // below line is use to add custom marker on our map.
                                                                            .icon(BitmapDescriptorFactory.fromBitmap(point)));
                                                                }
                                                            });
                                                            memo.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                                                @Override
                                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                                    dialogInterface.dismiss();
                                                                }
                                                            });
                                                            memo.show();
                                                        }
                                                    }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialogInterface, int i) {
                                                            dialogInterface.dismiss();
                                                        }
                                                    });
                                                    attrib.show();
                                                }
                                            }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    dialogInterface.dismiss();
                                                }
                                            });
                                            attri.show();
                                        } else {
                                            Toast.makeText(MapsActivity.this, "距離"+cal_distence(station, HOME)+"公尺，不在範圍內", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int id) {
                                        dialogInterface.dismiss();
                                    }
                                });
                                builder.show();
                                return false;
                            }
                        });
                    }
                });
            }
        }).start();
    }

    public String geo(double lat, double lng){
        Geocoder gc = new Geocoder(MapsActivity.this, Locale.TRADITIONAL_CHINESE);
        List<Address> lstAddress = null;
        try {
            lstAddress = gc.getFromLocation(lat, lng, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String returnAddress = lstAddress.get(0).getAddressLine(0);
        return returnAddress;
    }

//    View.OnClickListener btnPunchOnClickListener = new View.OnClickListener() {
//        @Override
//        public void onClick(View view) {
//            LatLng HOME = new LatLng(lat, lng);
//            int height = 100;
//            int width = 100;
//            BitmapDrawable bitmapdraw = (BitmapDrawable)ContextCompat.getDrawable(MapsActivity.this, R.drawable.point);
//            Bitmap b = bitmapdraw.getBitmap();
//            Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);
//
//            new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        //login(lat, lng);
//                        System.out.println("FYBR");
//                        new Handler(Looper.getMainLooper()).post(new Runnable() {
//                            public void run() {
//                                System.out.println("FYBR1");
//                                AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
//                                builder.setMessage("確定要在此點打卡嗎？");
//                                builder.setPositiveButton("確定", new DialogInterface.OnClickListener() {
//                                    public void onClick(DialogInterface dialog, int id) {
////                                        double conLat = lat;
////                                        double conLng = lng;
////                                        double roundLat = Math.round(conLat*100.0)/100.0;
////                                        double roundLng = Math.round(conLng*100.0)/100.0;
////                                        for (int k = 0; k<stationArray.length; k++){
////                                            double roundStaLat = Math.round(staLat[k]*100.0)/100.0;
////                                            double roundStaLng = Math.round(staLng[k]*100.0)/100.0;
////                                            if (roundLat == roundStaLat && roundLng == roundStaLng){
////                                                LatLng conStation = new LatLng(roundStaLat, roundStaLng);
////                                                conLatLng[k] = conStation;
////                                            }
////                                        }
////                                        for (int j = 0; j<stationArray.length; j++){
////                                            if (cal_distence(conLatLng[j], HOME) <= 1000){
////                                                break;
////                                            }
////                                            else{
////                                                AlertDialog.Builder notIn = new AlertDialog.Builder(MapsActivity.this);
////                                                notIn.setMessage("你不在範圍內");
////                                                notIn.setPositiveButton("OK", new DialogInterface.OnClickListener() {
////                                                    @Override
////                                                    public void onClick(DialogInterface dialogInterface, int i) {
////                                                        dialogInterface.dismiss();
////                                                   }
////                                                });
////                                                notIn.show();
////                                            }
////                                        }
////                                        if (cal_distence(stationArray[0], HOME) >= 1000){
////                                            Toast.makeText(MapsActivity.this, "打卡", Toast.LENGTH_SHORT).show();
////                                            Toast.makeText(MapsActivity.this, ""+stationArray[0], Toast.LENGTH_SHORT).show();
////                                            Toast.makeText(MapsActivity.this, ""+cal_distence(stationArray[0], HOME), Toast.LENGTH_SHORT).show();
////                                            Log.d("dis", ""+cal_distence(stationArray[0], HOME));
////                                            Log.d("lat", ""+stationArray[0]);
////                                        }else{
////                                            Toast.makeText(MapsActivity.this, "NO", Toast.LENGTH_SHORT).show();
////                                        }
////                                        for (int k = 0; k<stationArray.length; k++){
////                                            if (cal_distence(stationArray[k], HOME) <= 1000){
//////                                                mMap.addMarker(new MarkerOptions().position(HOME).title("目前")
//////                                                        // below line is use to add custom marker on our map.
//////                                                        .icon(BitmapDescriptorFactory.fromBitmap(smallMarker)));
////                                                break;
////                                            }
////                                            else{
////                                                AlertDialog.Builder notIn = new AlertDialog.Builder(MapsActivity.this);
////                                                notIn.setMessage("你不在範圍內");
////                                                notIn.setPositiveButton("OK", new DialogInterface.OnClickListener() {
////                                                    @Override
////                                                    public void onClick(DialogInterface dialogInterface, int i) {
////                                                        dialogInterface.dismiss();
////                                                    }
////                                                });
////                                                notIn.show();
////                                            }
////                                        }
//                                        new Thread(new Runnable() {
//                                            @Override
//                                            public void run() {
//                                                punchInPoint(token, lat, lng);
//                                            }
//                                        }).start();
//
//                                        Toast.makeText(MapsActivity.this, "已打卡", Toast.LENGTH_SHORT).show();
//                                        mMap.addMarker(new MarkerOptions().position(HOME).title("目前")
//                                                // below line is use to add custom marker on our map.
//                                                .icon(BitmapDescriptorFactory.fromBitmap(smallMarker)));
//                                    }
//                                });
//                                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
//                                    @Override
//                                    public void onClick(DialogInterface dialogInterface, int id) {
//                                        dialogInterface.dismiss();
//                                    }
//                                });
//                                builder.show();
//                            }
//                        });
//                    }
//                }).start();
//        }
//    };

    private static void login(double lat, double lng){
        String url = "https://dr.kymco.com/api/login";
        //String token = "";
        String acc = "ky5910";
        String pwd = "KY5910";

        OkHttpClient client = new OkHttpClient().newBuilder().build();
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        RequestBody body = RequestBody.create("acc=" + acc + "&pwd=" + pwd, mediaType);
        Request request = new Request.Builder()
                .url(url)
                .method("POST", body)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();
        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()){
                String header = response.header("Set-Cookie");
                token = header;
            }
        }catch (IOException e) {
            e.printStackTrace();
        }
        station(token);
//        String tmp = station(token);
//        Log.d("token", token);
//        Log.d("tmp", tmp);
    }

    private static String punchInPoint(String token, double lat, double lng){
        String url = "https://dr.kymco.com/es/eAPI/fmCheckInPoint";

        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create("{\r\n\"uid\": \"0a3c6d13-dac1-48f9-b001-f92d00773e3e\"," +
                "\r\n\"lat\": \"" + lat + "\",\r\n\"lng\": \"" + lng + "\"\r\n}", mediaType);
        Request request = new Request.Builder()
                .url(url)
                .method("POST", body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Cookie", token)
                .build();

        String jsonStr = "";
        try{
            Response response = client.newCall(request).execute();
            jsonStr = response.body().string();

        }catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(jsonStr);
        return jsonStr;
    }

    private static String station(String token) {
        String url = "https://dr.kymco.com/es/eAPI/fmStation";
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        Request request = new Request.Builder()
                .url(url)
                .method("GET", null)
                .addHeader("Cookie", token)
                .build();
        String jsonStr = "";
        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()){
                String result = response.body().string();
                JSONArray array = new JSONArray(result);
                List<JSONObject> list = new ArrayList<JSONObject>();
                List<Map<String, Object>> ls = new ArrayList<Map<String, Object>>();
                stationArray = new LatLng[array.length()];
                stationName = new String[array.length()];
                staLat = new double[array.length()];
                staLng = new double[array.length()];
                address = new String[array.length()];
                for (int i = 0; i<array.length(); i++){
                    list.add(array.getJSONObject(i));
                    JSONObject obj = new JSONObject(list.get(i).toString());
                    stationLat = (double)obj.get("lat");
                    stationLng = (double)obj.get("lng");
                    String name = (String) obj.get("spDesc");
                    String add = (String) obj.get("street");
                    HashMap<String, Object> params = new HashMap<String, Object>();
                    params.put("lat",stationLat);
                    params.put("lng",stationLng);
                    params.put("name", name);
                    params.put("address", add);
                    ls.add(params);
                    LatLng stationPoint = new LatLng(stationLat, stationLng);
                    staLat[i] = stationLat;
                    staLng[i] = stationLng;
                    stationArray[i] = stationPoint;
                    stationName[i] = name;
                    address[i] = add;
                    Log.d("station", stationArray.toString());
                    Log.d("lat", "" + stationLat);
                    Log.d("lng", "" + stationLng);
                }
            }

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return jsonStr;
    }

    //計算距離
    public double cal_distence(LatLng Start, LatLng End){
        double EARTH_RADIUS = 6378137.0;
        //double gps2m(double lat_a, double lng_a, double lat_b, double lng_b) {
        double radLat1 = (Start.latitude * Math.PI / 180.0);
        double radLat2 = (End.latitude * Math.PI / 180.0);
        double a = radLat1 - radLat2;
        double b = (Start.longitude - End.longitude) * Math.PI / 180.0;
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2) +
                Math.cos(radLat1) * Math.cos(radLat2)
                        * Math.pow(Math.sin(b / 2), 2)));
        s = s * EARTH_RADIUS;
        s = Math.round(s * 10000) / 10000;
        return s;
    }
}