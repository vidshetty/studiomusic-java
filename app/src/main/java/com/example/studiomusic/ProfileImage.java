package com.example.studiomusic;

public class ProfileImage {

    private ProfileImage() {}

    public static Integer getAlternativePicture(String name) {
        if (name == null) return null;
        Character firstLetter = null;
        for (int i=0; i<name.length(); i++) {
            if (Character.isLetter(name.toLowerCase().charAt(i))) {
                firstLetter = name.toLowerCase().charAt(i);
                break;
            }
        }
        if (firstLetter == 'a' || firstLetter == null) return R.drawable.ic_a;
        if (firstLetter == 'b') return R.drawable.ic_b;
        if (firstLetter == 'c') return R.drawable.ic_c;
        if (firstLetter == 'd') return R.drawable.ic_d;
        if (firstLetter == 'e') return R.drawable.ic_e;
        if (firstLetter == 'f') return R.drawable.ic_f;
        if (firstLetter == 'g') return R.drawable.ic_g;
        if (firstLetter == 'h') return R.drawable.ic_h;
        if (firstLetter == 'i') return R.drawable.ic_i;
        if (firstLetter == 'j') return R.drawable.ic_j;
        if (firstLetter == 'k') return R.drawable.ic_k;
        if (firstLetter == 'l') return R.drawable.ic_l;
        if (firstLetter == 'm') return R.drawable.ic_m;
        if (firstLetter == 'n') return R.drawable.ic_n;
        if (firstLetter == 'o') return R.drawable.ic_o;
        if (firstLetter == 'p') return R.drawable.ic_p;
        if (firstLetter == 'q') return R.drawable.ic_q;
        if (firstLetter == 'r') return R.drawable.ic_r;
        if (firstLetter == 's') return R.drawable.ic_s;
        if (firstLetter == 't') return R.drawable.ic_t;
        if (firstLetter == 'u') return R.drawable.ic_u;
        if (firstLetter == 'v') return R.drawable.ic_v;
        if (firstLetter == 'w') return R.drawable.ic_w;
        if (firstLetter == 'x') return R.drawable.ic_x;
        if (firstLetter == 'y') return R.drawable.ic_y;
        if (firstLetter == 'z') return R.drawable.ic_z;

        return null;
    }

}
