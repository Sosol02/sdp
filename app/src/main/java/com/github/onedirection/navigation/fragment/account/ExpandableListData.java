package com.github.onedirection.navigation.fragment.account;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ExpandableListData {
    public static HashMap<String, List<String>> getData() {
        HashMap<String, List<String>> expandableListDetail = new HashMap<String, List<String>>();

        List<String> option1 = new ArrayList<String>();
        option1.add("Sub-option 1");
        option1.add("Sub-option 2");


        List<String> option2 = new ArrayList<String>();
        option2.add("Sub-option 1");
        option2.add("Sub-option 2");

        List<String> option3 = new ArrayList<String>();
        option3.add("Sub-option 1");
        option3.add("Sub-option 2");

        expandableListDetail.put("Option 1", option1);
        expandableListDetail.put("Option 2", option2);
        expandableListDetail.put("Option 3", option3);
        return expandableListDetail;
    }
}
