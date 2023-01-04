package com.example.shortinfo.timer;

public class TimerImpl implements Timer {
    private static final TimerImpl instance = new TimerImpl();

    private TimerImpl() {

    }

    /**
     * 싱글톤 패턴에 따른 인스턴스 getter
     *
     * @return TimerImpl 인스턴스
     */
    public static TimerImpl getInstance() {
        return instance;
    }

    /**
     * 입력한 "시"가 오전인지 오후인지 판단
     *
     * @param hour: 시
     * @return 오후면 true, 오전이면 false
     */
    @Override
    public boolean isPM(int hour) {
        return hour >= 12;
    }

    /**
     * 입력받은 요일에 따라 해당되는 색상을 반환
     *
     * @param week: 일 ~ 토 에 해당하는 요일을 인덱싱한 숫자
     * @return 요일에 따른 색상코드에 해당되는 문자열
     */
    @Override
    public String getColorAccordingToDayOfWeek(int week) {
        switch (WEEKS[week - 1]) {
            case "일요일":
                return "#FF0000"; // red
            case "토요일":
                return "#3CA0E1"; // similar to blue
            default:
                return "#000000"; // black
        }
    }

    /**
     * 년월일을 입력받아 형식화된 날짜에 대한 문자열을 반환
     *
     * @param year:  년
     * @param month: 월 (인덱스 기준)
     * @param day:   일
     * @return 주어진 입력에 따른 형식화된 날짜를 표현하는 문자열
     */
    @Override
    public String getFormattedDate(int year, int month, int day) {
        return year + "년 " + (month + 1) + "월 " + day + "일";
    }

    /**
     * 시분초를 입력받아 형식화된 시간에 대한 문자열을 반환
     *
     * @param hour:   시
     * @param minute: 분
     * @param second: 초
     * @return 주어진 입력에 따른 형식화된 시간을 표현하는 문자열
     */
    @Override
    public String getFormattedTime(int hour, int minute, int second) {
        return (isPM(hour) ? "오후 " + (hour == 12 ? hour : (hour - 12)) : "오전" + (hour == 0 ? 12 : hour)) + ":"
                + (minute < 10 ? "0" + minute : minute) + ":" + (second < 10 ? "0" + second : second);
    }
}
