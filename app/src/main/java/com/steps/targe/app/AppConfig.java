package com.steps.targe.app;

public class AppConfig {
    // Server user login url
    // default:     https://trustme.000webhostapp.com
    //native:       http://192.168.43.6/trustme

    public static String BASE_URL = "http://192.168.43.6";

    public static String URL_LOGIN = "  https://livetraveller.000webhostapp.com/trust/login.php";

    // Server user register url
    public static String URL_REGISTER = "  https://livetraveller.000webhostapp.com/trust/register.php";

    public static String URL_SEARCH = "  https://livetraveller.000webhostapp.com/trust/search.php";
    // Server user friends url
    public static String URL_UPLOAD = "  https://livetraveller.000webhostapp.com/trust/upload.php";

    // Server user profile url
    public static String URL_PROFILE = "  https://livetraveller.000webhostapp.com/trust/profile.php";

    //account edit url
    public static String URL_ACCOUNT = "  https://livetraveller.000webhostapp.com/trust/account.php";

    //graphics directory
    public static String URL_DP = "  https://livetraveller.000webhostapp.com/trust/uploads";

    //weight file names
    public static String csv4class = "trust_db_4class.csv";
    public static String csv3class = "trust_db_3class.csv";
    //IN BAR:  0->SPAM 1->LOW  2->MEDIUM 3->HIGH
    //ACTUALLY:0->HIGH 1->SPAM 2->MEDIUM 3->LOW TRUST
    public static String []map_value2class={"HIGH","SPAM","MEDIUM","LOW"};
    public static final Integer []rating_bar_values={1,3,2,0};//from rating to trust value
    public static final Integer []map_values_to_indices={3,0,2,1};//inverse mapping
    //spam,low,medium,high colors
    public static final String []rating_bar_colors={"#f16400","#e9dd3a","#28ba5b","#66e72f"};

}
