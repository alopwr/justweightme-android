package io.github.gladko.justweight.bt.weight;

import android.icu.util.Calendar;

import java.sql.Date;

import io.github.gladko.justweight.db.User;

public class XioamiLib {
    int userAge = 0;
    private User user = new User();

    public XioamiLib(User user) {
        this.user = user;
        userAge = getAge(user.getDate_of_birth());

    }

    public byte[] getByteFromInt(int number){
        return new byte[]{(byte)0x01, (byte)0xFF, (byte)0xFF, (byte) ((number & 0xFF00) >> 8), (byte) ((number & 0xFF) >> 0)};
    }

    public float getWeight(byte[] data) {
        float weight = 0;
        final byte ctrlByte0 = data[0];
        final byte ctrlByte1 = data[1];

        final boolean isLBSUnit = isBitSet(ctrlByte0, 0);
        final boolean isCattyUnit = isBitSet(ctrlByte1, 6);

        if (isLBSUnit || isCattyUnit) {
            weight = (float) (((data[12] & 0xFF) << 8) | (data[11] & 0xFF)) / 100.0f;
        } else {
            weight = (float) (((data[12] & 0xFF) << 8) | (data[11] & 0xFF)) / 200.0f;
        }
        return weight;
    }

    public float getBodyFat(float weight, float impedance) {
        float bodyFat = 0.0f;
        float lbmSub = 0.8f;

        if (user.getSex() == "F" && userAge <= 49) {
            lbmSub = 9.25f;
        } else if (user.getSex() == "F" && userAge > 49) {
            lbmSub = 7.25f;
        }

        float lbmCoeff = getLBMCoefficient(weight, impedance);
        float coeff = 1.0f;

        if (user.getSex() == "M" && weight < 61.0f) {
            coeff = 0.98f;
        } else if (user.getSex() == "M" && weight > 60.0f) {
            coeff = 0.96f;

            if (user.getHeight() > 160.0f) {
                coeff *= 1.03f;
            }
        } else if (user.getSex() == "F" && weight < 50.0f) {
            coeff = 1.02f;

            if (user.getHeight() > 160.0f) {
                coeff *= 1.03f;
            }
        }

        bodyFat = (1.0f - (((lbmCoeff - lbmSub) * coeff) / weight)) * 100.0f;

        if (bodyFat > 63.0f) {
            bodyFat = 75.0f;
        }

        return bodyFat;
    }

    boolean isBitSet(byte value, int bit) {
        return (value & (1 << bit)) != 0;
    }

    private float getLBMCoefficient(float weight, float impedance) {
        float lbm = (user.getHeight() * 9.058f / 100.0f) * (user.getHeight() / 100.0f);
        lbm += weight * 0.32f + 12.226f;
        lbm -= impedance * 0.0068f;
        lbm -= userAge * 0.0542f;

        return lbm;
    }

    private int getAge(String date) {
        if (date == ""){
            return 0;
        }

        Calendar now = Calendar.getInstance();
        Calendar dob = Calendar.getInstance();
        dob.setTime(Date.valueOf(date));
        int year1 = now.get(Calendar.YEAR);
        int year2 = dob.get(Calendar.YEAR);
        int age = year1 - year2;
        int month1 = now.get(Calendar.MONTH);
        int month2 = dob.get(Calendar.MONTH);
        if (month2 > month1) {
            age--;
        } else if (month1 == month2) {
            int day1 = now.get(Calendar.DAY_OF_MONTH);
            int day2 = dob.get(Calendar.DAY_OF_MONTH);
            if (day2 > day1) {
                age--;
            }
        }
        return age;
    }


}
