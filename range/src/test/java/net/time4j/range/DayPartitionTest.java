package net.time4j.range;

import net.time4j.PlainDate;
import net.time4j.PlainTime;
import net.time4j.PlainTimestamp;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static net.time4j.Weekday.*;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(JUnit4.class)
public class DayPartitionTest {

    @Test
    public void doTest() {
        DayPartitionRule rule =
            new DayPartitionBuilder()
                .addExclusion(Collections.singleton(PlainDate.of(2016, 8, 27)))
                .addWeekdayRule(MONDAY, ClockInterval.between(PlainTime.of(9, 0), PlainTime.of(12, 30)))
                .addWeekdayRule(MONDAY, ClockInterval.between(PlainTime.of(14, 0), PlainTime.of(16, 0)))
                .addWeekdayRule(TUESDAY, ClockInterval.between(PlainTime.of(9, 0), PlainTime.of(12, 30)))
                .addWeekdayRule(TUESDAY, ClockInterval.between(PlainTime.of(14, 0), PlainTime.of(19, 0)))
                .addWeekdayRule(WEDNESDAY, ClockInterval.between(PlainTime.of(9, 0), PlainTime.of(12, 30)))
                .addWeekdayRule(THURSDAY, ClockInterval.between(PlainTime.of(9, 0), PlainTime.of(12, 30)))
                .addWeekdayRule(THURSDAY, ClockInterval.between(PlainTime.of(14, 0), PlainTime.of(19, 0)))
                .addWeekdayRule(FRIDAY, ClockInterval.between(PlainTime.of(9, 0), PlainTime.of(12, 30)))
                .addWeekdayRule(SATURDAY, ClockInterval.between(PlainTime.of(10, 0), PlainTime.of(12, 0)))
                .addSpecialRule(
                    PlainDate.of(2016, 9, 6),
                    ClockInterval.between(PlainTime.of(9, 15), PlainTime.of(12, 45)))
                .build();

        List<TimestampInterval> intervals =
            DateInterval.between(PlainDate.of(2016, 8, 25), PlainDate.of(2016, 9, 7))
                .streamPartitioned(rule)
                .collect(Collectors.toList());

        List<ChronoInterval<PlainTimestamp>> expected = new ArrayList<>();
        expected.add(
            TimestampInterval.between(PlainTimestamp.of(2016, 8, 25, 9, 0), PlainTimestamp.of(2016, 8, 25, 12, 30)));
        expected.add(
            TimestampInterval.between(PlainTimestamp.of(2016, 8, 25, 14, 0), PlainTimestamp.of(2016, 8, 25, 19, 0)));
        expected.add(
            TimestampInterval.between(PlainTimestamp.of(2016, 8, 26, 9, 0), PlainTimestamp.of(2016, 8, 26, 12, 30)));
        expected.add(
            TimestampInterval.between(PlainTimestamp.of(2016, 8, 29, 9, 0), PlainTimestamp.of(2016, 8, 29, 12, 30)));
        expected.add(
            TimestampInterval.between(PlainTimestamp.of(2016, 8, 29, 14, 0), PlainTimestamp.of(2016, 8, 29, 16, 0)));
        expected.add(
            TimestampInterval.between(PlainTimestamp.of(2016, 8, 30, 9, 0), PlainTimestamp.of(2016, 8, 30, 12, 30)));
        expected.add(
            TimestampInterval.between(PlainTimestamp.of(2016, 8, 30, 14, 0), PlainTimestamp.of(2016, 8, 30, 19, 0)));
        expected.add(
            TimestampInterval.between(PlainTimestamp.of(2016, 8, 31, 9, 0), PlainTimestamp.of(2016, 8, 31, 12, 30)));
        expected.add(
            TimestampInterval.between(PlainTimestamp.of(2016, 9, 1, 9, 0), PlainTimestamp.of(2016, 9, 1, 12, 30)));
        expected.add(
            TimestampInterval.between(PlainTimestamp.of(2016, 9, 1, 14, 0), PlainTimestamp.of(2016, 9, 1, 19, 0)));
        expected.add(
            TimestampInterval.between(PlainTimestamp.of(2016, 9, 2, 9, 0), PlainTimestamp.of(2016, 9, 2, 12, 30)));
        expected.add(
            TimestampInterval.between(PlainTimestamp.of(2016, 9, 3, 10, 0), PlainTimestamp.of(2016, 9, 3, 12, 0)));
        expected.add(
            TimestampInterval.between(PlainTimestamp.of(2016, 9, 5, 9, 0), PlainTimestamp.of(2016, 9, 5, 12, 30)));
        expected.add(
            TimestampInterval.between(PlainTimestamp.of(2016, 9, 5, 14, 0), PlainTimestamp.of(2016, 9, 5, 16, 0)));
        expected.add(
            TimestampInterval.between(PlainTimestamp.of(2016, 9, 6, 9, 15), PlainTimestamp.of(2016, 9, 6, 12, 45)));
        expected.add(
            TimestampInterval.between(PlainTimestamp.of(2016, 9, 7, 9, 0), PlainTimestamp.of(2016, 9, 7, 12, 30)));

        assertThat(intervals, is(expected));
    }

}