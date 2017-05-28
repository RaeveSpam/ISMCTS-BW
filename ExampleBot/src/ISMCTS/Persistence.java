package ISMCTS;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.net.InetAddress;
import java.util.GregorianCalendar;
import java.util.Calendar;




public class Persistence {

	private static String logFileName = "log";
	
	private static boolean createLog(){
		return false;
	}
	
	private static String readLog(){
		
		return null;
	}
	
	public static Node loadTree(){
		String name = "";
		Node result = null;
		// Load log with name of most recent stored tree
		try {
			XMLDecoder decoder = new XMLDecoder(new BufferedInputStream(new FileInputStream(logFileName)));
			name = (String)decoder.readObject();
		} catch (FileNotFoundException e) {
			//e.printStackTrace();
			System.out.println("No Log");
			return null;
		}
		
		// Load ISMCTS tree
		try {
			XMLDecoder decoder = new XMLDecoder(new BufferedInputStream(new FileInputStream(name)));
			result = (Node)decoder.readObject();
		} catch (FileNotFoundException e) {
			System.out.println("Save file not found");
			//e.printStackTrace();
			return null;
		}
		return result;
	}
	
	public static boolean saveTree(Node tree){
		String name = getHostName() + getDate();
		name = name + tree.getName();
		//System.out.println(name);
		try {
			XMLEncoder encoder = new XMLEncoder(new BufferedOutputStream(new FileOutputStream(name)));
			encoder.writeObject(tree);
			encoder.close();
		} catch (FileNotFoundException e) {
			System.out.println("ERROR SAVING SAVE FILE");
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		try {
			XMLEncoder encoder = new XMLEncoder(new BufferedOutputStream(new FileOutputStream(logFileName)));
			encoder.writeObject(name);
			encoder.close();
		} catch (FileNotFoundException e) {
			System.out.println("ERROR SAVING LOG FILE");
			e.printStackTrace();
			return false;
		}
		System.out.println(name + " saved");
		return true;
	}
	
	private static String getDate(){
		GregorianCalendar date = new GregorianCalendar();
		String result = "";
		int month = date.get(Calendar.MONTH)+1;
		int day = date.get(Calendar.DATE);
		int hour = date.get(Calendar.HOUR_OF_DAY);
		int minute = date.get(Calendar.MINUTE);
		if(month < 10){
			result += "0" + month;
		} else {
			result += month;
		}
		//result += "-";
		if(day < 10){
			result += "0" + day;
		} else {
			result += day;
		}
		//result += "_";
		if(hour < 10){
			result += "0" + hour;
		} else {
			result += hour;
		}
		if(minute < 10){
			result += "0" + minute;
		} else {
			result += minute;
		}
		return result;
	}
	
	private static String getHostName(){
		try
		{
		    InetAddress addr;
		    addr = InetAddress.getLocalHost();
		    return addr.getHostName();
		}
		catch (Exception ex)
		{
		    System.out.println("Hostname can not be resolved");
		    return "";
		}
	}
}
