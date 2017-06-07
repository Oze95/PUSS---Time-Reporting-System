package core;

import java.sql.Date;
import java.util.HashMap;

public class TimeReport {
	private String username, group;
	private int week;
	private Date date;
	private boolean signed;
	private HashMap<Integer, Integer> activities;
	
	/**
	 * Creates a time report.
	 * @param username The creator's username
	 * @param group The creator's group name
	 * @param week The time report's week
	 * @param signed Whether the report is signed or not
	 * @param date The latest update date
	 * @param activities All activity times
	 */
	public TimeReport(String username, String group, int week, boolean signed, Date date, HashMap<Integer, Integer> activities) {
		this.username = username;
		this.group = group;
		this.week = week;
		this.signed = signed;
		this.date = date;
		this.activities = activities;
		
	}
	
    /**
     * Gets the username from the time report.
     * @return String The username
     */
	public String getUsername() {
		return username;
	}
	

	public HashMap<Integer, Integer> getActivities() {
		return activities;
	}
	
    /**
     * Gets the group name from the time report.
     * @return String The group name
     */
	public String getGroup() {
		return group;
	}
	
    /**
     * Gets the week from the time report.
     * @return int The week
     */
	public int getWeek() {
		return week;
	}
	
    /**
     * Gets the latest update date from the time report.
     * @return Date The latest update date
     */
	public Date getDate() {
		return date;
	}
	
    /**
     * Gets whether the time report is signed or not.
     * @return boolean true if signed
     */
	public boolean isSigned() {
		return signed;
	}
	
    /**
     * Gets the time of the activity with the id <i>activityId</i>.
     * @param activityId The id of the activity
     * @return The activity time or 0 if no time is specified for the id
     */
	public int getActivityTime(int activityId) {
		Integer time = activities.get(activityId);
		
		if (time != null)
			return time;
		else
			return 0;
	}
	
    /**
     * Gets the sum for the specific activity parameter.
     * @param activity Can be either one of the documents or
     * an activity type such as "D" (documentation)
     * @return int The sum
     */
	public int getSum(String activity) {
		int sum = 0;

		switch (activity) {
		case "SDP":
			for (int i = 11; i <= 14; i++)
				sum += getActivityTime(i);
			break;
			
		case "SRS":
			for (int i = 21; i <= 24; i++)
				sum += getActivityTime(i);
			break;

		case "SVVS":
			for (int i = 31; i <= 34; i++) {
				sum += getActivityTime(i);
			}
			break;
			
		case "STLDD":
			for (int i = 41; i <= 44; i++) {
				sum += getActivityTime(i);
			}
			break;

		case "SVVI":
			for (int i = 51; i <= 54; i++) {
				sum += getActivityTime(i);
			}
			break;

		case "SDDD":
			for (int i = 61; i <= 64; i++) {
				sum += getActivityTime(i);
			}
			break;

		case "SVVR":
			for (int i = 71; i <= 74; i++) {
				sum += getActivityTime(i);
			}
			break;

		case "SSD":
			for (int i = 81; i <= 84; i++) {
				sum += getActivityTime(i);
			}
			break;

		case "Final Report":
			for (int i = 91; i <= 94; i++) {
				sum += getActivityTime(i);
			}
			break;

		case "Other":
			for (int i = 110; i <= 190; i += 10) {
				sum += getActivityTime(i);
			}
			break;

		case "D":
			for (int i = 11; i <= 91; i += 10) {
				sum += getActivityTime(i);
			}
			break;
			
		case "I":
			for (int i = 12; i <= 92; i += 10) {
				sum += getActivityTime(i);
			}
			break;
			
		case "F":
			for (int i = 13; i <= 93; i += 10) {
				sum += getActivityTime(i);
			}
			break;
			
		case "R":
			for (int i = 14; i <= 94; i += 10) {
				sum += getActivityTime(i);
			}
			break;
		}
		return sum;
	}
	
    /**
     * Get the total time of all activities.
     * @return int The sum of all activity times
     */
	public int getTotalTime() {
		int total = 0;
		for (Integer i : activities.values())
			total += i;

		return total;
	}
}
