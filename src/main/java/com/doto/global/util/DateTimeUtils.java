package com.doto.global.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** 날짜 문자열("yyyyMMdd")과 Instant 간 변환 util */
public final class DateTimeUtils {

    private static final DateTimeFormatter API_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter KOREAN_DATE_WITH_WEEKDAY_FORMAT =
            DateTimeFormatter.ofPattern("yyyy.MM.dd (E)", Locale.KOREA);

    private DateTimeUtils() {
    }

    // TourAPI "yyyyMMdd" -> 지정한 타임존 기준 시작 날짜
    public static Instant startOfDay(String yyyyMMdd, ZoneId zoneId) {
        return LocalDate.parse(yyyyMMdd, API_DATE_FORMAT).atStartOfDay(zoneId).toInstant();
    }

    // TourAPI "yyyyMMdd" -> 지정한 타임존 기준 끝 날짜
    public static Instant endOfDay(String yyyyMMdd, ZoneId zoneId) {
        return LocalDate.parse(yyyyMMdd, API_DATE_FORMAT).atTime(LocalTime.MAX).atZone(zoneId).toInstant();
    }

    // 날짜 -> 지정한 타임존 기준 "yyyy-MM-dd" 문자열 (응답 DTO 변환용)
    public static String toDateString(Instant instant, ZoneId zoneId) {
        return LocalDate.ofInstant(instant, zoneId).toString();
    }

    // instant로부터 정확히 1년 후 시점 (캘린더 연말이 아니라 오늘 기준 롤링 1년)
    public static Instant oneYearFrom(Instant instant, ZoneId zoneId) {
        return instant.atZone(zoneId).plusYears(1).toInstant();
    }

    // instant로부터 정확히 1년 전 시점
    public static Instant oneYearBefore(Instant instant, ZoneId zoneId) {
        return instant.atZone(zoneId).minusYears(1).toInstant();
    }

    // 날짜 -> 지정한 타임존 기준 "yyyy.MM.dd (요일)" 문자열 (관리자 화면 표시용)
    public static String toKoreanDateWithWeekday(Instant instant, ZoneId zoneId) {
        return LocalDate.ofInstant(instant, zoneId).format(KOREAN_DATE_WITH_WEEKDAY_FORMAT);
    }
}
