package com.frissbi.Frissbi_Pojo;

import java.util.HashMap;
import java.util.TreeSet;

/**
 * Created by KNPL003 on 10-06-2015.
 */
public class Friss_Pojo {
    public static String UseridFrom;
    public static String UserNameFrom;
    public static String UserNameTo;
    public static String UseridTo;
    public static String AvatarPathTo;

    public static TreeSet<String> treeSet;
    public static HashMap<String, String> hashMap;

    public static StringBuffer addfriend_Userids;
    public static final String USER_NAME = "UserName";
    public static final String FIRST_NAME = "FirstName";
    public static final String LAST_NAME = "LastName";
    public static final String MEETING_ID = "Meetin_id";
    public static final String AVATAR_PATH = "AvatarPath";
    public static final String EMAIL_ADDRESS = "Emailaddress";
    public static String REG_ID;
    public static final String User_Id = "UserId";
    public static String MeetingID;
    //public static final String REST_URI ="http://13.76.99.32/kleverlinkswebservices";
    public static final String REST_URI = "http://192.168.2.71:9090/kleverlinkswebservices";//Sunil
    //public static final String REST_URI ="http://192.168.43.63:9080/kleverlinkswebservices";
    public static final String USER_AUTHENTICATION = "/AuthenticateUserService/userAuthentication/";
    public static final String USER_REGISTRATION = "/UserRegistrationService/registerUser/";
    public static final String USER_FRIENDSlIST = "/FriendListService/friendsList/";
    public static final String USERNAME_EMAIL_EXIST = "/UserRegistrationService/existencestatus/";
    public static final String FORGOT_PASSWORD = "/AuthenticateUserService/forgetPassword/";
    public static final String SERCHING_DATABASE = "/FriendListService/search/";

    public static final String FRIEND_STASTUS = "/FriendListService/friendStatus/";

    public static final String FRIEND_CANCEL = "/FriendListService/cancelFriendRequest/";


    public static final String ADD_FRIEND = "/FriendListService/sendFriendRequest/";
    public static final String Peding_List = "/FriendListService/pendingForApprovalList/";
    public static final String APPROVE_FRIEND = "/FriendListService/approveFriendRequest/";
    public static final String REJCT_FRIEND = "/FriendListService/rejectFriendRequest/";
    public static final String GETMEETING_DETAILS = "/MeetingDetailsService/getMeetingDetailsByUserID/";

    public static final String MEETING_INSERT = "/MeetingDetailsService/insertMeetingDetails/";
    public static final String MEETING_UPDATE = "/MeetingDetailsService/updateRecipientXML/";
    public static final String MEETING_SINGALDETAILS = "/MeetingDetailsService/getMeetingDetailsByMeetingID/";

    public static final String LOCATION_MIDPOINT = "/LocationDetailsService/calculateMidpointForMeeting/";
    public static final String NEARBY_PLACES = "/GooglePlacesService/nearByPlacesForMeeting/";
    public static final String USERLOCTION_VOTING = "/LocationVotingsService/updateUserLocationVotings/";
    public static final String LOCATION_INSERT = "/UserSettingsService/insertUserPreferredLocations/";
    public static final String ORZIN_EXISTCHEK = "/UserSettingsService/getExistenceUserPreferredLocations/";
    public static final String ORZIN_DESTLIST = "/UserSettingsService/getUserPreferredLocations/";
    //public  static final String PIC = "/UserRegistrationService/updateProfilePic";

    public static final String PIC_Get = "/UserRegistrationService/getUserAvatarPath";

    public static final String MEETING_PENDINGLIST = "/MeetingDetailsService/getPendingMeetingRequests/";

    public static final String MEETING_SUMMARY = "/MeetingDetailsService/getMeetingSummary/";
    public static final String MEETING_CONFLICT = "/MeetingDetailsService/getConflictedMeetingDetails/";
    public static final String CHANGE_PASSWORD = "/AuthenticateUserService/updateUserPassword/";
    public static final String USER_AUTHENTICATIONUSER = "/AuthenticateUserService/authenticateUser/";

    public static final String User_Id_Get = "/UserRegistrationService/getUserId/";

    public static final String UPDATE_PROFILE = "/AuthenticateUserService/updateUserProfileSetting/";

    public static final String Privacy_policy = "/UserSettingsService/getFrissbiPrivacyPolicy/";
    public static final String getTermsandCondition = "/UserSettingsService/getTermsandConditions/";


    public static TreeSet<String> getTreeSet() {
        return treeSet;
    }

    public static void setTreeSet(TreeSet<String> treeSet) {
        Friss_Pojo.treeSet = treeSet;
    }

    public static HashMap<String, String> getHashMap() {
        return hashMap;
    }

    public static void setHashMap(HashMap<String, String> hashMap) {
        Friss_Pojo.hashMap = hashMap;
    }
}
