package contentnet;

import contentnet.category.UNSPSCRecord;

public class Utils {

    static Utils instance;

    public static final int DELAY = 0;

    private Utils() {

    }

    public static Utils getInstance() {
        if (instance == null)
            instance = new Utils();
        return instance;
    }

    public static String getLabelFromConceptContextName(String contextName) {
        int lastIndex = contextName.lastIndexOf('/');
        if (lastIndex >= 0 )
            return contextName.substring(lastIndex + 1);
        else
            return contextName;
    }

    public static String normalizeCNString(String contextName) {
        if (contextName == null || contextName.length() == 0)
            return contextName;
        else

            return contextName.trim().replace(' ', '_');
    }

    public static void doDelay(long delay) {
        if (delay > 0) {
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static String formatFileName(String incomeFileName, UNSPSCRecord category) {
        return "";
    }
}
