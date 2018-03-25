package kupchinskii.ruslan.gpsup;


public class Mediator {
    private static IGpsResult mGpsResult;

    public static void RegisterGpsResult(IGpsResult s){
        mGpsResult = s;
    }
    public static void UnregisterGpsResult(){
        mGpsResult = null;
    }

    public static GPS_Result getGpsResult(){
        if(mGpsResult != null)
            return mGpsResult.getGpsResult();
        return null;
    }

    private static IShowInfo mShowInfo;

    public static void RegisterShowInfo(IShowInfo s){
        mShowInfo = s;
    }
    public static void UnregisterShowInfo(){
        mShowInfo = null;
    }
    public static boolean isShowInfo(){
        return mShowInfo != null;
    }
    public static void ShowInfo(String s){
        if(mShowInfo != null)
            mShowInfo.ShowInfo(s);
    }

    public static void OnDestroy(){
        UnregisterShowInfo();
        UnregisterGpsResult();
    }

}
