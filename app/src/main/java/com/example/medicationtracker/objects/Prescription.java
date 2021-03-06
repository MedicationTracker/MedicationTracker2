package com.example.medicationtracker.objects;

import android.graphics.Bitmap;

import com.example.medicationtracker.Utility;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.Iterator;

import static com.example.medicationtracker.Utility.stringToLongArray;
import static com.example.medicationtracker.Utility.stringToTimeOfDayArray;
import static com.example.medicationtracker.Utility.timeOfDayArrayToString;


public class Prescription {
    private long id = 0; //default value for id. id is assigned when storing into db.
    private GregorianCalendar start_date; //used to store the DD/MM/YYYY. the HH:MM field is not used
    private int interval;
    private ArrayList<TimeOfDay> timings;
    private Drug drug;
    private ConsumptionInstruction consumption_instruction;
    private ArrayList<Long> deleted;

//    public Prescription(GregorianCalendar start_date, int interval, ArrayList<TimeOfDay> timings,
//                         Drug drug, ConsumptionInstruction ci, ArrayList<Long> deleted) {
//        this.start_date = start_date;
//        this.interval = interval;
//        this.timings = timings;
//        this.drug = drug;
//        this.consumption_instruction = ci;
//        this.deleted = deleted;
//    }
//    public Prescription(long id, GregorianCalendar start_date, int interval, ArrayList<TimeOfDay> timings,
//                        Drug drug, ConsumptionInstruction ci, ArrayList<Long> deleted) {
//        this.id = id;
//        this.start_date = start_date;
//        this.interval = interval;
//        this.timings = timings;
//        this.drug = drug;
//        this.consumption_instruction = ci;
//        this.deleted = deleted;
//    }

    /*
     * constructor which uses raw inputs
     * dont need to make the objects by yourself
     */
    public Prescription(long id, String drug_name, Bitmap drug_thumbnail, String dosage, String remarks,
                        long millis, int interval, String timings, String deleted_string) {
        this.id = id;

        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(millis);
        this.start_date = calendar;
        this.interval = interval;
        this.timings = stringToTimeOfDayArray(timings);
        this.drug = new Drug(drug_name, drug_thumbnail);
        this.consumption_instruction = new ConsumptionInstruction(dosage, remarks);
        this.deleted = stringToLongArray(deleted_string);
    }

    /*
     * generate the next set of ConsumptionInstances between specified timestamps
     * result is sorted chronologically
     */
    public ArrayList<ConsumptionInstance> generateConsumptionInstances(long start_time, long end_time) {
        GregorianCalendar c = (GregorianCalendar) this.start_date.clone();
        ArrayList<ConsumptionInstance> result = new ArrayList<>();
        Collections.sort(this.timings);

        while(c.getTimeInMillis() < end_time + Utility.MILLIS_IN_DAY) {
            for (TimeOfDay t : timings) {
                c.set(Calendar.HOUR_OF_DAY, Integer.valueOf(t.getHour()));
                c.set(Calendar.MINUTE, Integer.valueOf(t.getMinute()));
                long millis = c.getTimeInMillis();

                if (start_time <= millis && millis < end_time) {
                    ConsumptionInstance ci = new ConsumptionInstance(this.id, (GregorianCalendar) c.clone(),
                            this.drug);
                    if (deleted.contains(millis)) {
                        ci.setDeleted(true);
                    }
                    result.add(ci);
                }
            }
            c.add(Calendar.DAY_OF_MONTH, this.interval);
        }

        return result;
    }


    public ConsumptionInstance getNextInstance() {
        GregorianCalendar c = (GregorianCalendar) this.start_date.clone();
        long now_millis = new GregorianCalendar().getTimeInMillis();
        Collections.sort(this.timings);
        if (this.timings.size() <= 0) {
            return null;
        } else {
            while (true) {
                for (TimeOfDay t : timings) {
                    c.set(Calendar.HOUR_OF_DAY, Integer.valueOf(t.getHour()));
                    c.set(Calendar.MINUTE, Integer.valueOf(t.getMinute()));
                    long millis = c.getTimeInMillis();

                    if (now_millis < millis && !this.deleted.contains(millis)) {
                        return new ConsumptionInstance(this.id, c, this.drug);
                    }
                }

                c.add(Calendar.DAY_OF_MONTH, this.interval);
            }
        }
    }

    /*
     * Removes all entries from this.deleted that are before the current time
     */
    public void clean() {
        Long now = System.currentTimeMillis();
        for (Iterator<Long> iterator = this.deleted.iterator(); iterator.hasNext();) {
            Long l = iterator.next();
            if (l < now) {
                iterator.remove();
            }
        }
    }


    /*
    getters and setters
     */
    public long getId() { return this.id; }
    public void setId(long id) { this.id = id; }

    public GregorianCalendar getStartDate() { return this.start_date; }

    public int getInterval() { return this.interval; }

    public ArrayList<TimeOfDay> getTimings() { return this.timings; }
    public String getTimingsString() {
        return timeOfDayArrayToString(this.timings);
    }

    public Drug getDrug() { return this.drug; }

    public ConsumptionInstruction getConsumptionInstruction() { return this.consumption_instruction; }

    public ArrayList<Long> getDeleted() { return this.deleted; }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Prescription && this.id == ((Prescription) obj).getId();
    }
}
