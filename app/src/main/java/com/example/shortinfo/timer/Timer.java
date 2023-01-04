package com.example.shortinfo.timer;

public interface Timer {
    String[] WEEKS = {"일요일", "월요일", "화요일", "수요일", "목요일", "금요일", "토요일"};
    boolean isPM(int hour);
    String getColorAccordingToDayOfWeek(int week);
    String getFormattedDate(int year, int month, int day);
    String getFormattedTime(int hour, int minute, int second);
}

