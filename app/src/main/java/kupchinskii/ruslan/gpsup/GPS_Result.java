package kupchinskii.ruslan.gpsup;


public class GPS_Result {

    private final int _MAX_SAT_CNT = 100;
    public int status;
    public int speed;
    public long time;

    public float accuracy;
    public double latitude;
    public double longitude;
    public double altitude;

    public int satCnt;
    public int satAct;
    public int fixCnt;
    public int satTotal;

    public SatInfo[]SInfo = new SatInfo[_MAX_SAT_CNT];

    public GPS_Result(){
        for(int i=0; i<_MAX_SAT_CNT;i++){
            SInfo[i] = new SatInfo();
        }
    }

    public void reset(){
        status = Common.STATUS_DISABLE;
        speed = 0;
        accuracy = 0;
        latitude = 0;
        longitude = 0;
        satAct = 0;
        satCnt = 0;
        fixCnt = 0;
        satTotal = 0;
    }
}
