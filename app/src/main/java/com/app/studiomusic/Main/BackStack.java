package com.app.studiomusic.Main;

import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class BackStack {

    private static Stack<Fragment> stack = new Stack<>();
    private static List<String> root_fragments = new ArrayList<>();

    public static void addFragment(Fragment fragment) {
        stack.push(fragment);
    };

    public static void addFragmentAsRoot(Fragment fragment) {
        root_fragments.add(fragment.getTag());
    };

    public static Fragment removeTopFragment() {
        return stack.pop();
    };

    public static Fragment getTopFragment() {
        return stack.peek();
    };

    public static boolean hasFragment(String tag) {
        for (int i=0; i<stack.size(); i++) {
            if (tag.equals(stack.get(i).getTag())) return true;
        }
        return false;
    };

    public static boolean hasRootFragment(String tag) {
        for (int i=0; i<root_fragments.size(); i++) {
            if (root_fragments.get(i).equals(tag)) return true;
        }
        return false;
    };

    public static void clear() {
        stack = new Stack<>();
        root_fragments = new ArrayList<>();
    };

    public static boolean clearTillRoot(MainActivity activity) {
        boolean hasMoreThanRoot = stack.size() > 1;
        while (stack.size() > 1) {
            Fragment top = removeTopFragment();
            activity.getSupportFragmentManager().beginTransaction().hide(top).commit();
            activity.getSupportFragmentManager().beginTransaction().remove(top).commit();
        }
        return hasMoreThanRoot;
    };

    public static boolean hasSomeRootFragment() {
        return root_fragments.size() > 0;
    };

};
