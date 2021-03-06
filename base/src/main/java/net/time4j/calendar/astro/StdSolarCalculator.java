/*
 * -----------------------------------------------------------------------
 * Copyright © 2013-2018 Meno Hochschild, <http://www.menodata.de/>
 * -----------------------------------------------------------------------
 * This file (StdSolarCalculator.java) is part of project Time4J.
 *
 * Time4J is free software: You can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * Time4J is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Time4J. If not, see <http://www.gnu.org/licenses/>.
 * -----------------------------------------------------------------------
 */

package net.time4j.calendar.astro;

import net.time4j.Moment;
import net.time4j.PlainDate;
import net.time4j.PlainTime;
import net.time4j.PlainTimestamp;
import net.time4j.engine.CalendarDate;
import net.time4j.engine.EpochDays;
import net.time4j.scale.LeapSeconds;
import net.time4j.scale.TimeScale;
import net.time4j.tz.ZonalOffset;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.TimeUnit;


/**
 * <p>Enumeration of some standard calculators for solar time. </p>
 *
 * @since   3.36/4.31
 */
/*[deutsch]
 * <p>Aufz&auml;hlung einiger Standardberechnungsverfahren f&uuml;r die Sonnenzeit. </p>
 *
 * @since   3.36/4.31
 */
public enum StdSolarCalculator
    implements SolarTime.Calculator {

    //~ Statische Felder/Initialisierungen --------------------------------

    /**
     * Simple and relatively fast but rather imprecise calculator.
     *
     * <p>This calculator was once published in &quot;Almanac for Computers, 1990 by Nautical Almanac Office
     * in United States Naval Observatory (USNO)&quot;. </p>
     *
     * <p>Ed Williams has used this book as the source for
     * <a href="http://www.edwilliams.org/sunrise_sunset_algorithm.htm">his algorithmic proposal</a>. Mike
     * Reedell has then used the proposal of Williams to realize his popular sunrise/sunset-library written
     * in Java. Leaving aside general precision requirements, this method cannot be recommended for the
     * polar regions. So the scope of this method is constrained to the latitudes in range
     * {@code -65.0 <= latitude <= +65.0} otherwise the results are expected to be unusable. </p>
     *
     * <p>However, if users only use this method for actual years and non-polar regions, then
     * the precision of sunrise or sunset events remain within two minutes (and the equation
     * of time within one minute). </p>
     */
    /*[deutsch]
     * Einfache und relativ schnelle aber eher ungenaue Berechnungsmethode.
     *
     * <p>Diese Berechnungsmethode wurde urspr&uuml;nglich im &quot;Almanac for Computers, 1990
     * vom Nautical Almanac Office in United States Naval Observatory (USNO)&quot; ver&ouml;ffentlicht. </p>
     *
     * <p>Ed Williams hat dieses Buch als die Quelle
     * <a href="http://www.edwilliams.org/sunrise_sunset_algorithm.htm">seines algorithmischen Vorschlags</a>
     * verwendet. Mike Reedell hat schlie&szlig;lich den Vorschlag von Williams benutzt, um seine weit
     * verbreitete sunrise/sunset-library in der Programmiersprache Java zu realisieren. Auch wenn allgemeine
     * Genauigkeitsanforderungen beiseite gelassen werden, kann diese Methode nicht f&uuml;r die
     * polaren Breiten empfohlen werden. Somit ist diese Methode auf geographische Breiten im Bereich
     * {@code -65.0 <= latitude <= +65.0} beschr&auml;nkt, sonst sind die Ergebnisse unbrauchbar.  </p>
     *
     * <p>Allerdings verbleibt die Genauigkeit f&uuml;r Sonnenauf- oder Sonnenuntergang noch innerhalb
     * von zwei Minuten (und f&uuml;r die Zeitgleichung innerhalb einer Minute), wenn Anwender diese
     * Methode nur f&uuml;r aktuelle Jahre und die nicht-polaren Breiten benutzen. </p>
     */
    SIMPLE {
        /*
            URL:
              http://www.edwilliams.org/sunrise_sunset_algorithm.htm
              https://babel.hathitrust.org/cgi/pt?id=uiug.30112059294311;view=1up;seq=25

            Source:
              Almanac for Computers, 1990
              published by Nautical Almanac Office
              United States Naval Observatory
              Washington, DC 20392

            Inputs:
              day, month, year:      date of sunrise/sunset
              latitude, longitude:   location for sunrise/sunset
              zenith:                Sun's zenith for sunrise/sunset
                offical      = 90 degrees 50'
                civil        = 96 degrees
                nautical     = 102 degrees
                astronomical = 108 degrees

            NOTE: longitude is positive for East and negative for West
            NOTE: the algorithm assumes the use of a calculator with the
            trig functions in "degree" (rather than "radian") mode. Most
            programming languages assume radian arguments, requiring back
            and forth convertions. The factor is 180/pi. So, for instance,
            the equation RA = atan(0.91764 * tan(L)) would be coded as RA
            = (180/pi)*atan(0.91764 * tan((pi/180)*L)) to give a degree
            answer with a degree input for L.

            1. first calculate the day of the year

                N1 = floor(275 * month / 9)
                N2 = floor((month + 9) / 12)
                N3 = (1 + floor((year - 4 * floor(year / 4) + 2) / 3))
                N = N1 - (N2 * N3) + day - 30

            2. convert the longitude to hour value and calculate an approximate time

                lngHour = longitude / 15

                if rising time is desired:
                    t = N + ((6 - lngHour) / 24)
                if setting time is desired:
                    t = N + ((18 - lngHour) / 24)

            3. calculate the Sun's mean anomaly

                M = (0.9856 * t) - 3.289

            4. calculate the Sun's true longitude

                L = M + (1.916 * sin(M)) + (0.020 * sin(2 * M)) + 282.634
                NOTE: L potentially needs to be adjusted into the range [0,360) by adding/subtracting 360

            5a. calculate the Sun's right ascension

                RA = atan(0.91764 * tan(L))
                NOTE: RA potentially needs to be adjusted into the range [0,360) by adding/subtracting 360

            5b. right ascension value needs to be in the same quadrant as L

                Lquadrant  = (floor( L/90)) * 90
                RAquadrant = (floor(RA/90)) * 90
                RA = RA + (Lquadrant - RAquadrant)

            5c. right ascension value needs to be converted into hours
    /
                RA = RA / 15

            6. calculate the Sun's declination

                sinDec = 0.39782 * sin(L)
                cosDec = cos(asin(sinDec))

            7a. calculate the Sun's local hour angle

                cosH = (cos(zenith) - (sinDec * sin(latitude))) / (cosDec * cos(latitude))

                if (cosH >  1)
                    the sun never rises on this location (on the specified date)
                if (cosH < -1)
                    the sun never sets on this location (on the specified date)

            7b. finish calculating H and convert into hours

                if rising time is desired:
                    H = 360 - acos(cosH)
                if setting time is desired:
                    H = acos(cosH)

                H = H / 15

            8. calculate local mean time of rising/setting

                T = H + RA - (0.06571 * t) - 6.622

            9. adjust back to UTC

                UT = T - lngHour
                NOTE: T potentially needs to be adjusted into the range [0,24) by adding/subtracting 24

            10. convert UT value to local time zone of latitude/longitude

                localT = UT + localOffset
        */
        @Override
        public Optional<Moment> sunrise(CalendarDate date, double latitude, double longitude, double zenith) {
            return event(date, latitude, longitude, zenith, true);
        }
        @Override
        public Optional<Moment> sunset(CalendarDate date, double latitude, double longitude, double zenith) {
            return event(date, latitude, longitude, zenith, false);
        }
        @Override
        public double equationOfTime(double jde) {
            // => page B8, formula 1 (precision about 0.8 minutes)
            double t = time0(jde);
            return (
                -7.66 * Math.sin(Math.toRadians(0.9856 * t - 3.8))
                    - 9.78 * Math.sin(Math.toRadians(1.9712 * t + 17.96))
            ) * 60;
        }
        @Override
        public double declination(double jde) {
            double t0 = time0(jde);
            double L = trueLongitudeOfSunInDegrees(t0);
            double sinDec = 0.39782 * Math.sin(Math.toRadians(L));
            return Math.toDegrees(Math.asin(sinDec));
        }
        @Override
        public double rightAscension(double jde) {
            double t0 = time0(jde);
            double L = trueLongitudeOfSunInDegrees(t0);
            double RA = // right ascension of sun in degrees
                Math.toDegrees(Math.atan(0.91764 * Math.tan(Math.toRadians(L))));
            RA = AstroUtils.toRange_0_360(RA);
            double Lquadrant  = Math.floor(L / 90) * 90;
            double RAquadrant = Math.floor(RA / 90) * 90;
            return RA + Lquadrant - RAquadrant; // RA in same quadrant as L
        }
        private double time0(double jde) {
            PlainTimestamp tsp = JulianDay.ofEphemerisTime(jde).toMoment().toZonalTimestamp(ZonalOffset.UTC);
            return tsp.getCalendarDate().getDayOfYear() + tsp.getWallTime().get(PlainTime.SECOND_OF_DAY) / 86400.0;
        }
        private double trueLongitudeOfSunInDegrees(double t0) {
            double M = // mean anomaly of sun in degrees
                (0.9856 * t0) - 3.289;
            double L =
                M + (1.916 * Math.sin(Math.toRadians(M))) + (0.020 * Math.sin(2 * Math.toRadians(M))) + 282.634;
            return AstroUtils.toRange_0_360(L);
        }
        private Optional<Moment> event(
            CalendarDate date,
            double latitude,
            double longitude,
            double zenith,
            boolean sunrise
        ) {
            // => page B5/B6/B7
            PlainDate d = SolarTime.toGregorian(date);
            int doy = d.getDayOfYear();
            double lngHour = longitude / 15;
            double t0 = doy + (((sunrise ? 6 : 18) - lngHour) / 24);
            double L = trueLongitudeOfSunInDegrees(t0);
            double RA = // right ascension of sun in degrees
                Math.toDegrees(Math.atan(0.91764 * Math.tan(Math.toRadians(L))));
            RA = AstroUtils.toRange_0_360(RA);
            double Lquadrant  = Math.floor(L / 90) * 90;
            double RAquadrant = Math.floor(RA / 90) * 90;
            RA = (RA + (Lquadrant - RAquadrant)) / 15; // RA in same quadrant as L
            double sinDec = 0.39782 * Math.sin(Math.toRadians(L));
            double cosDec = Math.cos(Math.asin(sinDec));
            double latInRad = Math.toRadians(latitude);
            double cosH = // local hour angle of sun
                (Math.cos(Math.toRadians(zenith)) - (sinDec * Math.sin(latInRad))) / (cosDec * Math.cos(latInRad));
            if ((Double.compare(cosH, 1.0) > 0) || (Double.compare(cosH, -1.0) < 0)) {
                // the sun never rises or sets on this location (on the specified date)
                return Optional.empty();
            }
            double H = Math.toDegrees(Math.acos(cosH));
            if (sunrise) {
                H = 360 - H;
            }
            H = H / 15;
            double lmt = H + RA - (0.06571 * t0) - 6.622;
            if (Double.compare(0.0, lmt) > 0) {
                lmt += 24;
            } else if (Double.compare(24.0, lmt) <= 0) {
                lmt -= 24;
            }
            double ut =  lmt - lngHour;
            int tod = (int) Math.floor(ut * 3600);
            long secs = d.get(EpochDays.UTC) * 86400 + tod;
            // we truncate/neglect the fractional seconds here and round to full minutes
            TimeScale scale = TimeScale.UT;
            if (!LeapSeconds.getInstance().isEnabled()) {
                secs += (86400 * 730);
                scale = TimeScale.POSIX;
            }
            Moment utc = Moment.of(Math.round(secs / 60.0) * 60, scale);
            return Optional.of(utc.with(Moment.PRECISION, TimeUnit.MINUTES));
        }
    },

    /**
     * Follows closely the algorithms published by NOAA (National Oceanic and Atmospheric Administration).
     *
     * <p><strong>Introduction</strong></p>
     *
     * <p>The <a href="https://www.esrl.noaa.gov/gmd/grad/solcalc/">website</a> of NOAA also links
     * to the calculation details. This is a calculator with reasonably good precision. But the altitude
     * of the observer is not taken into account. </p>
     *
     * <p>Although the precision is theoretically often better than one minute (for non-polar regions,
     * beyond +/-72 degrees latitude rather in range of ten minutes), users should consider the fact
     * that local topology or the actual weather conditions are not taken into account. Therefore
     * truncating the results to minute precision should be considered. </p>
     *
     * <p><strong>Example</strong></p>
     *
     * <pre>
     *     PlainDate date = PlainDate.of(2009, 9, 6);
     *     SolarTime atlanta = SolarTime.ofLocation(33.766667, -84.416667, 0, StdSolarCalculator.NOAA);
     *     TZID tzid = () -&gt; &quot;America/New_York&quot;;
     *     assertThat(
     *       date.get(atlanta.sunrise())
     *         .get()
     *         .toZonalTimestamp(tzid)
     *         .with(PlainTime.PRECISION, ClockUnit.MINUTES),
     *       is(PlainTimestamp.of(2009, 9, 6, 7, 15)));
     * </pre>
     */
    /*[deutsch]
     * Folgt nahe den Algorithmen, die von der NOAA (National Oceanic and Atmospheric Administration)
     * ver&ouml;ffentlicht wurden.
     *
     * <p><strong>Einleitung</strong></p>
     *
     * <p>Die <a href="https://www.esrl.noaa.gov/gmd/grad/solcalc/">Webseite</a> der NOAA verlinkt
     * auch zu den Berechnungsdetails. Dieses Verfahren bietet eine recht gute Genauigkeit. Die H&ouml;he
     * des Beobachters wird aber nicht besonders ber&uuml;cksichtigt. </p>
     *
     * <p>Obwohl die Genauigkeit theoretisch oft besser als eine Minute ist (f&uuml;r nicht-polare Breiten,
     * jenseits von +/-72 Grad Breite jedoch eher im Bereich von 10 Minuten)), sollten Anwender auch die Tatsache
     * in Betracht ziehen, da&szlig; die lokale Topologie oder die aktuellen Wetterbedingungen nicht
     * ber&uuml;cksichtigt werden. Deshalb ist das Abschneiden der Sekundenteile in den Ergebnissen
     * meistens angeraten. </p>
     *
     * <p><strong>Beispiel</strong></p>
     *
     * <pre>
     *     PlainDate date = PlainDate.of(2009, 9, 6);
     *     SolarTime atlanta = SolarTime.ofLocation(33.766667, -84.416667, 0, StdSolarCalculator.NOAA);
     *     TZID tzid = () -&gt; &quot;America/New_York&quot;;
     *     assertThat(
     *       date.get(atlanta.sunrise())
     *         .get()
     *         .toZonalTimestamp(tzid)
     *         .with(PlainTime.PRECISION, ClockUnit.MINUTES),
     *       is(PlainTimestamp.of(2009, 9, 6, 7, 15)));
     * </pre>
     */
    NOAA() {
        @Override
        public Optional<Moment> sunrise(CalendarDate date, double latitude, double longitude, double zenith) {
            return this.event(true, date, latitude, longitude, zenith);
        }
        @Override
        public Optional<Moment> sunset(CalendarDate date, double latitude, double longitude, double zenith) {
            return this.event(false, date, latitude, longitude, zenith);
        }
        // Meeus p.185 (lower accuracy model), returns units of second
        // other source: http://adsabs.harvard.edu/full/1989MNRAS.238.1529H
        @Override
        public double equationOfTime(double jde) {
            double jct = toJulianCenturies(jde);
            double tanEpsilonHalf = Math.tan(Math.toRadians(obliquity(jct) / 2));
            double y = tanEpsilonHalf * tanEpsilonHalf;
            double l2Rad = Math.toRadians(2 * meanLongitude(jct));
            double e = excentricity(jct);
            double mRad = Math.toRadians(meanAnomaly(jct));
            double sinM = Math.sin(mRad);
            double eot =
                y * Math.sin(l2Rad)
                    - 2 * e * sinM
                    + 4 * e * y * sinM * Math.cos(l2Rad)
                    - y * y * Math.sin(2 * l2Rad) / 2
                    - 5 * e * e * Math.sin(2 * mRad) / 4;
            return Math.toDegrees(eot) * 240;
        }
        @Override
        public double declination(double jde) {
            double jct = toJulianCenturies(jde);
            return Math.toDegrees(declinationRad(jct));
        }
        @Override
        public double rightAscension(double jde) {
            double jct = toJulianCenturies(jde);
            double lRad = Math.toRadians(solarLongitude(jct));
            double y = Math.cos(Math.toRadians(obliquity(jct))) * Math.sin(lRad);
            double ra = Math.toDegrees(Math.atan2(y, Math.cos(lRad)));
            return AstroUtils.toRange_0_360(ra);
        }
        private Optional<Moment> event(
            boolean rise,
            CalendarDate date,
            double latitude,
            double longitude,
            double zenith
        ) {
            Moment m = SolarTime.fromLocalEvent(date, 12, longitude, this.name()); // noon
            double jde = JulianDay.getValue(m, TimeScale.TT);
            double H = localHourAngle(rise, jde, latitude, zenith);
            if (Double.isNaN(H)) {
                return Optional.empty();
            } else {
                H = localHourAngle(rise, jde + H / 86400, latitude, zenith); // corrected for local time of day
                if (Double.isNaN(H)) {
                    return Optional.empty();
                } else {
                    long secs = (long) Math.floor(H);
                    int nanos = (int) ((H - secs) * 1_000_000_000);
                    Moment utc = m.plus(secs, TimeUnit.SECONDS).plus(nanos, TimeUnit.NANOSECONDS);
                    return Optional.of(utc.with(Moment.PRECISION, TimeUnit.SECONDS));
                }
            }
        }
        private double localHourAngle(boolean rise, double jde, double latitude, double zenith) {
            double jct = toJulianCenturies(jde);
            double H = localHourAngle(jct, latitude, zenith);
            if (Double.isNaN(H)) {
                return Double.NaN;
            } else {
                if (rise) {
                    H = -H;
                }
                return H;
            }
        }
        // Meeus (22.2), in degrees
        private double obliquity(double jct) {
            double obliquity =
                23.0 + 26.0 / 60 + (21.448 + (-46.815 + (-0.00059 + 0.001813 * jct) * jct) * jct) / 3600;
            double corr = 0.00256 * Math.cos(Math.toRadians(125.04 - 1934.136 * jct)); // Meeus (25.8)
            return obliquity + corr;
        }
        // Meeus (25.2), in degrees
        private double meanLongitude(double jct) {
            return (280.46646 + (36000.76983 + 0.0003032 * jct) * jct) % 360;
        }
        // Meeus (25.3), in degrees
        private double meanAnomaly(double jct) {
            return 357.52911 + (35999.05029 - 0.0001537 * jct) * jct;
        }
        // Meeus (25.4), unit-less
        private double excentricity(double jct) {
            return 0.016708634 - (0.000042037 + 0.0000001267 * jct) * jct;
        }
        // W2-term in NOAA-Excel-sheet
        private double localHourAngle(
            double jct,
            double latitude,
            double zenith
        ) {
            double latInRad = Math.toRadians(latitude);
            double decInRad = declinationRad(jct);
            double cosH =
                (Math.cos(Math.toRadians(zenith)) - (Math.sin(decInRad) * Math.sin(latInRad)))
                    / (Math.cos(decInRad) * Math.cos(latInRad));
            if ((Double.compare(cosH, 1.0) > 0) || (Double.compare(cosH, -1.0) < 0)) {
                // the sun never rises or sets on this location (on the specified date)
                return Double.NaN;
            }
            return Math.toDegrees(Math.acos(cosH)) * 240; // in decimal seconds
        }
        // T2-term in NOAA-Excel-sheet (in radians)
        private double declinationRad(double jct) {
            return Math.asin(
                Math.sin(Math.toRadians(obliquity(jct))) * Math.sin(Math.toRadians(solarLongitude(jct))));
        }
        // P2-term in NOAA-Excel-sheet (Meeus p.164 - lower accuracy model)
        private double solarLongitude(double jct) {
            return meanLongitude(jct)
                + equationOfCenter(jct)
                - 0.00569
                - 0.00478 * Math.sin(Math.toRadians(125.04 - 1934.136 * jct));
        }
        // L2-term in NOAA-Excel-sheet (Meeus p.164 - lower accuracy model)
        private double equationOfCenter(double jct) {
            double j2 = Math.toRadians(meanAnomaly(jct));
            return (
                Math.sin(j2) * (1.914602 - (0.004817 + 0.000014 * jct) * jct)
                    + Math.sin(2 * j2) * (0.019993 - 0.000101 * jct)
                    + Math.sin(3 * j2) * 0.000289
            );
        }
    },

    /**
     * Follows closely the algorithms published by Dershowitz/Reingold in their book
     * &quot;Calendrical Calculations&quot; (third edition).
     *
     * <p>The altitude of the observer is taken into account by an approximated geodetic model. </p>
     *
     * <p><strong>{@link #getFeature(double, String) Supported features}</strong> (in degrees)</p>
     *
     * <ul>
     *     <li>right-ascension</li>
     *     <li>declination</li>
     *     <li>nutation</li>
     *     <li>obliquity</li>
     *     <li>mean-anomaly</li>
     *     <li>solar-longitude</li>
     * </ul>
     */
    /*[deutsch]
     * Folgt nahe den Algorithmen, die von Dershowitz/Reingold in ihrem Buch
     * &quot;Calendrical Calculations&quot; (dritte Auflage) ver&ouml;ffentlicht wurden.
     *
     * <p>Die H&ouml;he des Beobachters wird mit Hilfe eines angen&auml;herten geod&auml;tischen Modells
     * ber&uuml;cksichtigt. </p>
     *
     * <p><strong>{@link #getFeature(double, String) Unterst&uuml;tzte Merkmale}</strong> (in Grad)</p>
     *
     * <ul>
     *     <li>right-ascension</li>
     *     <li>declination</li>
     *     <li>nutation</li>
     *     <li>obliquity</li>
     *     <li>mean-anomaly</li>
     *     <li>solar-longitude</li>
     * </ul>
     */
    CC() {
        @Override
        public Optional<Moment> sunrise(CalendarDate date, double latitude, double longitude, double zenith) {
            return this.event(true, date, latitude, longitude, zenith);
        }
        @Override
        public Optional<Moment> sunset(CalendarDate date, double latitude, double longitude, double zenith) {
            return this.event(false, date, latitude, longitude, zenith);
        }
        private Optional<Moment> event(
            boolean rise,
            CalendarDate date,
            double latitude,
            double longitude,
            double zenith
        ) {
            double daypart =
                rise ? 0.25 : 0.75;
            double lmt =
                (EpochDays.JULIAN_DAY_NUMBER.transform(date.getDaysSinceEpochUTC(), EpochDays.UTC) + daypart);
            double ephemeris =
                TimeScale.deltaT(SolarTime.toGregorian(date)) - 43200; // last term because JD starts at noon
            double offset =
                (ZonalOffset.atLongitude(new BigDecimal(longitude)).getIntegralAmount() - ephemeris) / 86400.0;
            double result =
                momentOfDepression(lmt, latitude, offset, zenith - 90.0, rise);
            if (Double.isNaN(result)) {
                return Optional.empty();
            } else {
                Moment utc = JulianDay.ofEphemerisTime(result - offset).toMoment();
                return Optional.of(utc.with(Moment.PRECISION, TimeUnit.SECONDS));
            }
        }
        @Override
        public double equationOfTime(double jde) {
            double jct = toJulianCenturies(jde);
            double tanEpsilonHalf = Math.tan(Math.toRadians(obliquity(jct) / 2));
            double y = tanEpsilonHalf * tanEpsilonHalf;
            double l2Rad = Math.toRadians(2 * meanLongitude(jct));
            double e = excentricity(jct);
            double mRad = Math.toRadians(meanAnomaly(jct));
            double sinM = Math.sin(mRad);
            double eot =
                y * Math.sin(l2Rad)
                    - 2 * e * sinM
                    + 4 * e * y * sinM * Math.cos(l2Rad)
                    - y * y * Math.sin(2 * l2Rad) / 2
                    - 5 * e * e * Math.sin(2 * mRad) / 4;
            return Math.toDegrees(eot) * 240;
        }
        @Override
        public double declination(double jde) {
            return this.getFeature(jde, SolarTime.DECLINATION);
        }
        @Override
        public double rightAscension(double jde) {
            return this.getFeature(jde, SolarTime.RIGHT_ASCENSION);
        }
        @Override
        public double getFeature(
            double jde,
            String nameOfFeature
        ) {
            double jct = toJulianCenturies(jde);

            switch (nameOfFeature) {
                case SolarTime.DECLINATION:
                    return Math.toDegrees(declinationRad(jct));
                case SolarTime.RIGHT_ASCENSION:
                    double lRad = Math.toRadians(apparentSolarLongitude(jct, nutation(jct)));
                    double y = Math.cos(Math.toRadians(obliquity(jct))) * Math.sin(lRad);
                    double ra = Math.toDegrees(Math.atan2(y, Math.cos(lRad)));
                    return AstroUtils.toRange_0_360(ra);
                case "nutation":
                    return nutation(jct);
                case "obliquity":
                    return obliquity(jct);
                case "mean-anomaly":
                    return meanAnomaly(jct);
                case "solar-longitude":
                    return apparentSolarLongitude(jct, nutation(jct));
                case "solar-latitude":
                    return 0.0; // approximation used in this algorithm
                default:
                    return Double.NaN;
            }
        }
        @Override
        public double getGeodeticAngle(double latitude, int altitude) {
            if (altitude == 0) {
                return 0.0;
            }
            double r = MEAN_EARTH_RADIUS;
            return Math.toDegrees(Math.acos(r / (r + altitude))) + (Math.sqrt(altitude) * (19.0 / 3600));
        }
        private double momentOfDepression(
            double lmt,
            double latitude,
            double offset,
            double alpha,
            boolean early
        ) {
            double lmt2 = approxMomentOfDepression(lmt, latitude, offset, alpha, early);
            if (Double.isNaN(lmt2)) {
                return Double.NaN;
            }
            if (Math.abs(lmt - lmt2) * 86400 < 30) {
                return lmt2;
            }
            return momentOfDepression(lmt2, latitude, offset, alpha, early);
        }
        private double approxMomentOfDepression(
            double lmt,
            double latitude,
            double offset,
            double alpha,
            boolean early
        ) {
            long date = (long) Math.floor(lmt);
            double ttry = sineOffset(lmt - offset, latitude, alpha);
            double alt = (alpha >= 0) ? (early ? date : date + 1) : date + 0.5;
            double value = (Math.abs(ttry) > 1) ? sineOffset(alt - offset, latitude, alpha) : ttry;
            if (Math.abs(value) <= 1) {
                double tmp = (early ? -1 : 1);
                tmp *= (((0.5 + Math.toDegrees(Math.asin(value)) / 360.0) % 1) - 0.25);
                tmp += date;
                tmp += 0.5;
                return tmp - equationOfTime(tmp - offset) / 86400; // not very precise, see p.184
            } else {
                return Double.NaN;
            }
        }
        private double sineOffset(double jde, double latitude, double alpha) {
            double jct = toJulianCenturies(jde);
            double latInRad = Math.toRadians(latitude);
            double decInRad = declinationRad(jct);
            return Math.tan(latInRad) * Math.tan(decInRad)
                + Math.sin(Math.toRadians(alpha)) / (Math.cos(decInRad) * Math.cos(latInRad));
        }
        private double declinationRad(double jct) {
            return Math.asin(
                Math.sin(Math.toRadians(obliquity(jct)))
                    * Math.sin(Math.toRadians(apparentSolarLongitude(jct, nutation(jct)))));
        }
        private double nutation(double jct) { // nutation in longitude (in degrees)
            double a = Math.toRadians(124.9 + (-1934.134 + 0.002063 * jct) * jct);
            double b = Math.toRadians(201.11 + (72001.5377 + 0.00057 * jct) * jct);
            return -0.004778 * Math.sin(a) - 0.0003667 * Math.sin(b);
        }
        private double obliquity(double jct) {
            return 23.0 + 26.0 / 60 + (21.448 + (-46.815 + (-0.00059 + 0.001813 * jct) * jct) * jct) / 3600;
        }
        private double meanLongitude(double jct) {
            return (280.46645 + (36000.76983 + 0.0003032 * jct) * jct) % 360;
        }
        private double meanAnomaly(double jct) {
            return 357.5291 + (35999.0503 + (-0.0001559 + 0.00000048 * jct) * jct) * jct;
        }
        private double excentricity(double jct) {
            return 0.016708617 - (0.000042037 + 0.0000001236 * jct) * jct;
        }
    },

    /**
     * Based mainly on the astronomical calculations published by Jean Meeus in his book
     * &quot;Astronomical Algorithms&quot; (second edition).
     *
     * <p><strong>Introduction</strong></p>
     *
     * <p>This calculation offers high precision with the general limitation
     * that the local topology or special weather conditions cannot be calculated. </p>
     *
     * <p>The altitude of the observer is taken into account using a spheroid (WGS84)
     * and the assumption of a standard atmosphere (for the refraction). </p>
     *
     * <p><strong>{@link #getFeature(double, String) Supported features}</strong> (in degrees)</p>
     *
     * <ul>
     *     <li>right-ascension</li>
     *     <li>declination</li>
     *     <li>nutation</li>
     *     <li>obliquity</li>
     *     <li>mean-anomaly</li>
     *     <li>solar-longitude</li>
     * </ul>
     */
    /*[deutsch]
     * Basiert meist auf den astronomischen Berechnungen von Jean Meeus, die er im Buch
     * &quot;Astronomical Algorithms&quot; (zweite Auflage) ver&ouml;ffentlicht hat.
     *
     * <p><strong>Einleitung</strong></p>
     *
     * <p>Dieses Verfahren bietet hohe Genauigkeit, aber mit der allgemeinen Einschr&auml;nkung,
     * da&szlig; die lokale Topologie oder besondere Wetterbedingungen nicht berechnet werden k&ouml;nnen. </p>
     *
     * <p>Die H&ouml;he des Beobachters wird mit Hilfe eines Rotationsellipsoids (WGS84)
     * als geod&auml;tischen Modell und der Annahme einer Standardatmosph&auml;re (f&uuml;r
     * die Refraktion) ber&uuml;cksichtigt. </p>
     *
     * <p><strong>{@link #getFeature(double, String) Unterst&uuml;tzte Merkmale}</strong> (in Grad)</p>
     *
     * <ul>
     *     <li>right-ascension</li>
     *     <li>declination</li>
     *     <li>nutation</li>
     *     <li>obliquity</li>
     *     <li>mean-anomaly</li>
     *     <li>solar-longitude</li>
     * </ul>
     */
    TIME4J() {
        @Override
        public Optional<Moment> sunrise(CalendarDate date, double latitude, double longitude, double zenith) {
            return this.event(true, date, latitude, longitude, zenith);
        }
        @Override
        public Optional<Moment> sunset(CalendarDate date, double latitude, double longitude, double zenith) {
            return this.event(false, date, latitude, longitude, zenith);
        }
        // Meeus p.185 (lower accuracy model), returns units of second
        @Override
        public double equationOfTime(double jde) {
            double jct = toJulianCenturies(jde);
            double tanEpsilonHalf = Math.tan(Math.toRadians(trueObliquity(jct) / 2));
            double y = tanEpsilonHalf * tanEpsilonHalf;
            double l2Rad = Math.toRadians(2 * meanLongitude(jct));
            double e = excentricity(jct);
            double mRad = Math.toRadians(meanAnomaly(jct));
            double sinM = Math.sin(mRad);
            double eot =
                y * Math.sin(l2Rad)
                    - 2 * e * sinM
                    + 4 * e * y * sinM * Math.cos(l2Rad)
                    - y * y * Math.sin(2 * l2Rad) / 2
                    - 5 * e * e * Math.sin(2 * mRad) / 4;
            return Math.toDegrees(eot) * 240;
        }
        @Override
        public double declination(double jde) {
            return this.getFeature(jde, SolarTime.DECLINATION);
        }
        @Override
        public double rightAscension(double jde) {
            return this.getFeature(jde, SolarTime.RIGHT_ASCENSION);
        }
        @Override
        public double getFeature(
            double jde,
            String nameOfFeature
        ) {
            double jct = toJulianCenturies(jde);

            switch (nameOfFeature) {
                case SolarTime.DECLINATION:
                    return Math.toDegrees(declinationRad(jct));
                case SolarTime.RIGHT_ASCENSION: {
                    double[] result = new double[2];
                    nutations(jct, result);
                    double lRad = Math.toRadians(apparentSolarLongitude(jct, result[0]));
                    double y = Math.cos(Math.toRadians(meanObliquity(jct) + result[1])) * Math.sin(lRad);
                    double ra = Math.toDegrees(Math.atan2(y, Math.cos(lRad)));
                    return AstroUtils.toRange_0_360(ra);
                }
                case "nutation": {
                    double[] result = new double[2];
                    nutations(jct, result);
                    return result[0];
                }
                case "obliquity": {
                    double[] result = new double[2];
                    nutations(jct, result);
                    return meanObliquity(jct) + result[1];
                }
                case "mean-anomaly":
                    return meanAnomaly(jct);
                case "solar-longitude": {
                    double[] result = new double[2];
                    nutations(jct, result);
                    return apparentSolarLongitude(jct, result[0]);
                }
                case "solar-latitude":
                    return 0.0; // approximation used in this algorithm
                default:
                    return Double.NaN;
            }
        }
        @Override
        public double getGeodeticAngle(double latitude, int altitude) {
            if (altitude == 0) {
                return 0.0;
            }
            // curvature radius of earth spheroid in the prime vertical (east-west), see also:
            // https://en.wikipedia.org/wiki/Earth_radius#Radii_of_curvature
            double lat = Math.toRadians(latitude);
            double r1 = EQUATORIAL_RADIUS * Math.cos(lat);
            double r2 = POLAR_RADIUS * Math.sin(lat);
            double r =  EQUATORIAL_RADIUS * EQUATORIAL_RADIUS / Math.sqrt(r1 * r1 + r2 * r2);
            return Math.toDegrees(Math.acos(r / (r + altitude)));
        }
        @Override
        public double getZenithAngle(double latitude, int altitude) {
            if (altitude == 0) {
                return SolarTime.STD_ZENITH;
            }
            double refraction = AstroUtils.refractionFactorOfStdAtmosphere(altitude) * SolarTime.STD_REFRACTION;
            return 90 + this.getGeodeticAngle(latitude, altitude) + ((SolarTime.SUN_RADIUS + refraction) / 60.0);
        }
        private Optional<Moment> event(
            boolean rise,
            CalendarDate date,
            double latitude,
            double longitude,
            double zenith
        ) {
            Moment m = SolarTime.fromLocalEvent(date, 12, longitude, this.name()); // noon
            double jde = JulianDay.getValue(m, TimeScale.TT);
            double oldH;
            double newH = 0.0;
            do {
                oldH = newH;
                newH = localHourAngle(rise, jde + oldH / 86400, latitude, zenith);
                if (Double.isNaN(newH)) {
                    return Optional.empty();
                }
            } while (Math.abs(newH - oldH) >= 15); // usually requires only 2 or 3 loops
            long secs = (long) Math.floor(newH);
            int nanos = (int) ((newH - secs) * 1_000_000_000);
            Moment utc = m.plus(secs, TimeUnit.SECONDS).plus(nanos, TimeUnit.NANOSECONDS);
            return Optional.of(utc.with(Moment.PRECISION, TimeUnit.SECONDS));
        }
        private double localHourAngle(boolean rise, double jde, double latitude, double zenith) {
            double jct = toJulianCenturies(jde);
            double latInRad = Math.toRadians(latitude);
            double decInRad = declinationRad(jct);
            double cosH =
                (Math.cos(Math.toRadians(zenith)) - (Math.sin(decInRad) * Math.sin(latInRad)))
                    / (Math.cos(decInRad) * Math.cos(latInRad));
            if ((Double.compare(cosH, 1.0) > 0) || (Double.compare(cosH, -1.0) < 0)) {
                // the sun never rises or sets on this location (on the specified date)
                return Double.NaN;
            }
            double hourAngle =  Math.toDegrees(Math.acos(cosH)) * 240; // in decimal seconds
            if (rise) {
                hourAngle = -hourAngle;
            }
            return hourAngle;
        }
        private double declinationRad(double jct) {
            double[] no = new double[2];
            nutations(jct, no);
            return Math.asin(
                Math.sin(Math.toRadians(meanObliquity(jct) + no[1]))
                    * Math.sin(Math.toRadians(apparentSolarLongitude(jct, no[0]))));
        }
        // Meeus (25.8) - low accuracy model, used only for equation-of-time
        private double trueObliquity(double jct) {
            return meanObliquity(jct) + 0.00256 * Math.cos(Math.toRadians(125.04 - 1934.136 * jct));
        }
        // Meeus (25.2), in degrees
        private double meanLongitude(double jct) {
            return (280.46646 + (36000.76983 + 0.0003032 * jct) * jct) % 360;
        }
        // Meeus (25.3), in degrees
        private double meanAnomaly(double jct) {
            return 357.52911 + (35999.05029 - 0.0001537 * jct) * jct;
        }
        // Meeus (25.4), unit-less
        private double excentricity(double jct) {
            return 0.016708634 - (0.000042037 + 0.0000001267 * jct) * jct;
        }
    };

    private static final int[] DG_X = {
        403406, 195207, 119433, 112392, 3891, 2819, 1721, 660, 350, 334, 314, 268, 242, 234, 158, 132, 129, 114,
        99, 93, 86, 78, 72, 68, 64, 46, 38, 37, 32, 29, 28, 27, 27, 25, 24, 21, 21, 20, 18, 17, 14, 13, 13, 13,
        12, 10, 10, 10, 10
    };

    private static final double[] DG_Y = {
        270.54861, 340.19128, 63.91854, 331.2622, 317.843, 86.631, 240.052, 310.26, 247.23, 260.87, 297.82,
        343.14, 166.79, 81.53, 3.5, 132.75, 182.95, 162.03, 29.8, 266.4, 249.2, 157.6, 257.8, 185.1, 69.9,
        8, 197.1, 250.4, 65.3, 162.7, 341.5, 291.6, 98.5, 146.7, 110, 5.2, 342.6, 230.9, 256.1, 45.3,
        242.9, 115.2, 151.8, 285.3, 53.3, 126.6, 205.7, 85.9, 146.1
    };

    private static final double[] DG_Z = {
        0.9287892, 35999.1376958, 35999.4089666, 35998.7287385, 71998.20261, 71998.4403, 36000.35726, 71997.4812,
        32964.4678, -19.441, 445267.1117, 45036.884, 3.1008, 22518.4434, -19.9739, 65928.9345, 9038.0293,
        3034.7684, 33718.148, 3034.448, -2280.773, 29929.992, 31556.493, 149.588, 9037.75, 107997.405,
        -4444.176, 151.771, 67555.316, 31556.08, -4561.54, 107996.706, 1221.655, 62894.167, 31437.369,
        14578.298, -31931.757, 34777.243, 1221.999, 62894.511, -4442.039, 107997.909, 119.066, 16859.071,
        -4.578, 26895.292, -39.127, 12297.536, 90073.778
    };

    private static final int MEAN_EARTH_RADIUS = 6372000;
    private static final double EQUATORIAL_RADIUS = 6378137.0;
    private static final double POLAR_RADIUS = 6356752.3;

    //~ Methoden ----------------------------------------------------------

    /**
     * <p>Determines the declination of sun. </p>
     *
     * @param   jde     julian day in ephemeris time
     * @return  declination of sun in degrees
     * @see     #rightAscension(double)
     */
    /*[deutsch]
     * <p>Bestimmt die Deklination der Sonne. </p>
     *
     * @param   jde     julian day in ephemeris time
     * @return  declination of sun in degrees
     * @see     #rightAscension(double)
     */
    public double declination(double jde) {
        throw new AbstractMethodError(); // implemented in subclass
    }

    /**
     * <p>Determines the right ascension of sun. </p>
     *
     * @param   jde     julian day in ephemeris time
     * @return  right ascension of sun in degrees
     * @see     #declination(double)
     */
    /*[deutsch]
     * <p>Bestimmt die Rektaszension der Sonne. </p>
     *
     * @param   jde     julian day in ephemeris time
     * @return  right ascension of sun in degrees
     * @see     #declination(double)
     */
    public double rightAscension(double jde) {
        throw new AbstractMethodError(); // implemented in subclass
    }

    /**
     * <p>Supported features are at least &quot;declination&quot; and &quot;right-ascension&quot;. </p>
     *
     * @param   jde             julian day in ephemeris time
     * @param   nameOfFeature   describes what kind of value shall be calculated
     * @return  result value or {@code Double.NaN} if the feature is not supported
     */
    /*[deutsch]
     * <p>Unterst&uuml;tzte Merkmale sind wenigstens &quot;declination&quot; and &quot;right-ascension&quot;. </p>
     *
     * @param   jde             julian day in ephemeris time
     * @param   nameOfFeature   describes what kind of value shall be calculated
     * @return  result value or {@code Double.NaN} if the feature is not supported
     */
    @Override
    public double getFeature(
        double jde,
        String nameOfFeature
    ) {
        switch (nameOfFeature) {
            case SolarTime.DECLINATION:
                return this.declination(jde);
            case SolarTime.RIGHT_ASCENSION:
                return this.rightAscension(jde);
            default:
                return Double.NaN;
        }
    }

    // Meeus (22.2), in degrees
    static double meanObliquity(double jct) {
        return 23.0 + 26.0 / 60 + (21.448 + (-46.815 + (-0.00059 + 0.001813 * jct) * jct) * jct) / 3600;
    }

    // Meeus (chapter 22)
    static void nutations(
        double jct,
        double[] result
    ) {

        double D = Math.toRadians(
            297.85036 + (445267.11148 + (-0.0019142 + (1.0 / 189474) * jct) * jct) * jct);
        double M = Math.toRadians(
            357.52772 + (35999.050340 + (-0.0001603 + (-1.0 / 300000) * jct) * jct) * jct);
        double M2 = Math.toRadians(
            134.96298 + (477198.867398 + (0.0086972 + (1.0 / 5625) * jct) * jct) * jct);
        double F = Math.toRadians(
            93.27191 + (483202.017538 + (-0.0036825 + (1.0 / 327270) * jct) * jct) * jct);
        double O = Math.toRadians(
            125.04452 + (-1934.136261 + (0.0020708 + (1.0 / 450000) * jct) * jct) * jct);

        double resultL = 0.0; // nutation in longitude
        double resultO = 0.0; // nutation in obliquity

        for (int i = TABLE_22A.length - 1; i >= 0; i--) {
            double[] row = TABLE_22A[i];
            double arg = row[0] * D + row[1] * M + row[2] * M2 + row[3] * F + row[4] * O;
            resultL += Math.sin(arg) * (row[5] + row[6] * jct);
            resultO += Math.cos(arg) * (row[7] + row[8] * jct);
        }

        // results in degrees
        result[0] = resultL * 0.0001 / 3600;
        result[1] = resultO * 0.0001 / 3600;

    }

    private static double toJulianCenturies(double jde) {

        return (jde - 2451545.0) / 36525; // julian centuries (J2000)

    }

    private static double apparentSolarLongitude(
        double jct,
        double nutation
    ) {

        // taken from "Planetary Programs and Tables from -4000 to +2800" (Bretagnon & Simon, 1986)
        // => described by Jean Meeus as being of higher accuracy
        double p49 = 0.0;

        for (int i = DG_X.length - 1; i >= 0; i--) {
            p49 += (DG_X[i] * Math.sin(Math.toRadians(DG_Y[i] + DG_Z[i] * jct)));
        }

        double angle = (
            282.7771834 + 36000.76953744 * jct
                + (5.729577951308232 * p49 / 1_000_000)
                + aberration(jct)
                + nutation
        ) / 360;

        return (angle - Math.floor(angle)) * 360;

    }

    private static double aberration(double jct) {

        return 0.0000974 * Math.cos(Math.toRadians(177.63 + 35999.01848 * jct)) - 0.005575;

    }

    private static final double[][] TABLE_22A = {
        {0, 0, 0, 0, 1, -171996, -174.2, 92025, 8.9},
        {-2, 0, 0, 2, 2, -13187, -1.6, 5736, -3.1},
        {0, 0, 0, 2, 2, -2274, -0.2, 977, -0.5},
        {0, 0, 0, 0, 2, 2062, 0.2, -895, 0.5},
        {0, 1, 0, 0, 0, 1426, -3.4, 54, -0.1},
        {0, 0, 1, 0, 0, 712, 0.1, -7, 0},
        {-2, 1, 0, 2, 2, -517, 1.2, 224, -0.6},
        {0, 0, 0, 2, 1, -386, -0.4, 200, 0},
        {0, 0, 1, 2, 2, -301, 0, 129, -0.1},
        {-2, -1, 0, 2, 2, 217, -0.5, -95, 0.3},
        {-2, 0, 1, 0, 0, -158, 0, 0, 0},
        {-2, 0, 0, 2, 1, 129, 0.1, -70, 0},
        {0, 0, -1, 2, 2, 123, 0, -53, 0},
        {2, 0, 0, 0, 0, 63, 0, 0, 0},
        {0, 0, 1, 0, 1, 63, 0.1, -33, 0},
        {2, 0, -1, 2, 2, -59, 0, 26, 0},
        {0, 0, -1, 0, 1, -58, -0.1, 32, 0},
        {0, 0, 1, 2, 1, -51, 0, 27, 0},
        {-2, 0, 2, 0, 0, 48, 0, 0, 0},
        {0, 0, -2, 2, 1, 46, 0, -24, 0},
        {2, 0, 0, 2, 2, -38, 0, 16, 0},
        {0, 0, 2, 2, 2, -31, 0, 13, 0},
        {0, 0, 2, 0, 0, 29, 0, 0, 0},
        {-2, 0, 1, 2, 2, 29, 0, -12, 0},
        {0, 0, 0, 2, 0, 26, 0, 0, 0},
        {-2, 0, 0, 2, 0, -22, 0, 0, 0},
        {0, 0, -1, 2, 1, 21, 0, -10, 0},
        {0, 2, 0, 0, 0, 17, -0.1, 0, 0},
        {2, 0, -1, 0, 1, 16, 0, -8, 0},
        {-2, 2, 0, 2, 2, -16, 0.1, 7, 0},
        {0, 1, 0, 0, 1, -15, 0, 9, 0},
        {-2, 0, 1, 0, 1, -13, 0, 7, 0},
        {0, -1, 0, 0, 1, -12, 0, 6, 0},
        {0, 0, 2, -2, 0, 11, 0, 0, 0},
        {2, 0, -1, 2, 1, -10, 0, 5, 0},
        {2, 0, 1, 2, 2, -8, 0, 3, 0},
        {0, 1, 0, 2, 2, 7, 0, -3, 0},
        {-2, 1, 1, 0, 0, -7, 0, 0, 0},
        {0, -1, 0, 2, 2, -7, 0, 3, 0},
        {2, 0, 0, 2, 1, -7, 0, 3, 0},
        {2, 0, 1, 0, 0, 6, 0, 0, 0},
        {-2, 0, 2, 2, 2, 6, 0, -3, 0},
        {-2, 0, 1, 2, 1, 6, 0, -3, 0},
        {2, 0, -2, 0, 1, -6, 0, 3, 0},
        {2, 0, 0, 0, 1, -6, 0, 3, 0},
        {0, -1, 1, 0, 0, 5, 0, 0, 0},
        {-2, -1, 0, 2, 1, -5, 0, 3, 0},
        {-2, 0, 0, 0, 1, -5, 0, 3, 0},
        {0, 0, 2, 2, 1, -5, 0, 3, 0},
        {-2, 0, 2, 0, 1, 4, 0, 0, 0},
        {-2, 1, 0, 2, 1, 4, 0, 0, 0},
        {0, 0, 1, -2, 0, 4, 0, 0, 0},
        {-1, 0, 1, 0, 0, -4, 0, 0, 0},
        {-2, 1, 0, 0, 0, -4, 0, 0, 0},
        {1, 0, 0, 0, 0, -4, 0, 0, 0},
        {0, 0, 1, 2, 0, 3, 0, 0, 0},
        {0, 0, -2, 2, 2, -3, 0, 0, 0},
        {-1, -1, 1, 0, 0, -3, 0, 0, 0},
        {0, 1, 1, 0, 0, -3, 0, 0, 0},
        {0, -1, 1, 2, 2, -3, 0, 0, 0},
        {2, -1, -1, 2, 2, -3, 0, 0, 0},
        {0, 0, 3, 2, 2, -3, 0, 0, 0},
        {2, -1, 0, 2, 2, -3, 0, 0, 0}
    };

}
