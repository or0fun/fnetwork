# fnetwork

简便的网络加载库，封装了okhttp和volley， 解析采用fastjson

## gradle 依赖

    compile 'com.baiwanlu.android:fnetwork:1.0.0"

## 首先在application的onCreate里调用, init 的第二个参数是是否输入log

    /**
    * Created by benren.fj on 6/11/16.
    */
    public class MainApplication extends Application {
       @Override
       public void onCreate() {
          super.onCreate();

          FNetworkManager.init(this, false);
        }
    }
    
## 使用简单示例

### 新建一个请求

    public class IPRequest extends FGetRequest<IPResult> {
      public IPRequest(IRequestCallBack<IPResult> callBack) {
        super(callBack);
      }

      @NonNull
      @Override
      protected String getRequestUrl() {
        return "http://ip.taobao.com/service/getIpInfo.php";
      }

      @Override
      protected TypeReference<IPResult> getTypeReference() {
        return new TypeReference<IPResult>(){};
      }
    }

### 新建解析类

    public class IPResult {

      /**
       * code : 0
       * data : {"country":"中国","country_id":"CN","area":"华北","area_id":"100000","region":"天津市","region_id":"120000","city":"天津市","city_id":"120100","county":"","county_id":"-1","isp":"移动","isp_id":"100025","ip":"111.32.23.2"}
     */

      public int code;
      /**
       * country : 中国
       * country_id : CN
       * area : 华北
       * area_id : 100000
       * region : 天津市
       * region_id : 120000
       * city : 天津市
       * city_id : 120100
       * county :
       * county_id : -1
       * isp : 移动
      * isp_id : 100025
      * ip : 111.32.23.2
       */

      public DataBean data;

      public static class DataBean {
        public String country;
        public String country_id;
        public String area;
        public String area_id;
        public String region;
        public String region_id;
        public String city;
        public String city_id;
        public String county;
        public String county_id;
        public String isp;
        public String isp_id;
        public String ip;
      }
    }
        
### 请求调用

    new IPRequest(new IRequestCallBack<IPResult>() {
            @Override
            public void onResponseSuccess(IPResult response) {
                //回调是运行在UI线程
                demoTextView.setText(response.data.country);
            }

            @Override
            public void onResponseError(int errorCode) {
               ////回调是运行在UI线程
            }
        }).addParam("ip", "111.32.23.2").start();


## 请求类型包含FGetRequest和FPostRequest

## 目前错误类型包含了以下几种

    public interface FNetworkError {
      int ERROR_UNAVAILABLE = -1;
      int ERROR_PARSE_FAILED = 2;
      int ERROR_TIMEOUT = 3;
      int ERROR_UNKNOWN = 4;
    }



