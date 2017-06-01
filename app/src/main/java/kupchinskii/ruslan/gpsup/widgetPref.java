package kupchinskii.ruslan.gpsup;

public class widgetPref{

    public enum en_w_mode{
        no_close(0),
        close_on_up(1),
        close_on_app(2);

        private final int value;
        private en_w_mode(int value) {
            this.value = value;
        }

        public int getValue() {
            try {
                return value;
            }
            catch (Exception e){
                return 0;
            }
        }

        public static en_w_mode fromInteger(int x) {
            switch(x) {
                case 0:
                    return no_close;
                case 1:
                    return close_on_up;
                case 2:
                    return close_on_app;

            }
            return null;
        }
    }

    public  String Package;
    public en_w_mode Mode;
}