package aero.champ.projectpera.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import aero.champ.projectpera.BO.EmployeeDetails;
import aero.champ.projectpera.BO.TimeInOut;
import aero.champ.projectpera.scheduler.scheduled.BiMonthlyGenerator;
import aero.champ.projectpera.sql.bean.FalcoEmployee;
import aero.champ.projectpera.sql.dao.FalcoTransactionsDao;

public class EmployeeTimeRecorder {

	private FalcoTransactionsDao dao;
//	private BiMonthlyGenerator timesheetReportGenerator;
	
	public EmployeeTimeRecorder() {
		this.dao = new FalcoTransactionsDao();
	}
	
	public List<List<FalcoEmployee>> getAllEmployeeInOutList(String dailyDate) {
		
		List<FalcoEmployee> falcoCompleteEmployeeeList = this.dao.getDetails(dailyDate);
		
		String cardNo = new String();
		
		List<FalcoEmployee> perEmployeeList = new ArrayList<FalcoEmployee>();
		
		List<List<FalcoEmployee>> segregatedEmployeeList = new ArrayList<List<FalcoEmployee>>();
		
		for (FalcoEmployee employee: falcoCompleteEmployeeeList) {
			
			if (cardNo.equals(employee.getCardNo())) {
				
				perEmployeeList.add(employee);
				
			} else {
				
				segregatedEmployeeList.add(perEmployeeList);
				perEmployeeList = new ArrayList<FalcoEmployee>();
				
				perEmployeeList.add(employee);
				cardNo = employee.getCardNo();
				
			}
			
		}
		
		return segregatedEmployeeList;
		
	}
	
	public List<EmployeeDetails> get() {
		
		List<EmployeeDetails> empList = new ArrayList<EmployeeDetails>();
		
		// Filename: testfileYYYY_MM_DD.txt
		// CSV format: NAME,CARD_NO,FIRST_IN,LAST_OUT
		// Users: CARD_NO,NAME 
		
		String filenameTemplate = "testfile";
		String dateTemplate = "2016_08_";
		String directory = "D:/timesheet/20160816/";
		
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
		
		try {
			
			List<String> userList = new ArrayList<String>();
			
			BufferedReader userReader = new BufferedReader(new FileReader("D:/timesheet/ALL_USERS.txt"));
			String userLine;
		    while ((userLine = userReader.readLine()) != null) {
//			    System.out.println(userLine);
		    	userList.add(userLine);
			}
		    userReader.close();
		    userReader = null;
		    
			for (String user: userList) {
				
				String[] userDetails = user.split("\\|");
				
				String cardNo = userDetails[0].trim();
				String name = userDetails[1].trim();
				
				EmployeeDetails employee = new EmployeeDetails();
				employee.setFirstName("");
				employee.setLastName(name);
				employee.setCardNumber(Integer.parseInt(cardNo));
				
				List<TimeInOut> timeInOutList = new ArrayList<TimeInOut>();
				
				int startDay = 1;
				int endDay = 15;
				
				for (int day = startDay; day <= endDay; day++) {
					
					String fileNameDate = dateTemplate + String.format("%02d", day);
					
					String filename = filenameTemplate + fileNameDate;
					
					BufferedReader reader = new BufferedReader(new FileReader(directory + filename + ".txt"));
					
					String line;
				    while ((line = reader.readLine()) != null) {

				    	if (line.contains(cardNo)) {
				    		String[] empTimeDetails = line.split("\\|");
				    		
				    		Date timeIn = new Date();
				    		Date timeOut = new Date();
				    		
				    		try {
					    		timeIn = simpleDateFormat.parse(empTimeDetails[2]);
					    		timeOut = simpleDateFormat.parse(empTimeDetails[3]);
				    		} catch (ArrayIndexOutOfBoundsException e) {
				    			System.out.println(line);
				    			e.printStackTrace();
				    		}
				    		
				    		TimeInOut timeInOut = new TimeInOut();
				    		timeInOut.setTimeIn(timeIn);
				    		timeInOut.setTimeOut(timeOut);
				    		
				    		timeInOutList.add(timeInOut);
				    		
				    		break;
				    	}
				    	
				    }
				    
				    reader.close();
				    reader = null;
				}
				
				employee.setTimeInOutList(timeInOutList);
				empList.add(employee);
			}
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return empList;
	}
	
	public void test() throws Exception {
		
		int startDay = 1;
		int endDay = 15;

		PrintStream writer;
		
		// YYYY/MM/
		String dateTemplate = "2016/08/";
		
		for (int day = startDay; day <= endDay; day++) {
			
			String queryDate = dateTemplate + String.format("%02d", day);
			
			String filenameDate = queryDate.replaceAll("/", "_");
			
			writer = new PrintStream(new File("d:/timesheet/20160816/testfile"+filenameDate+".txt"));
			
			System.out.println(queryDate);
			
			List<List<FalcoEmployee>> list = getAllEmployeeInOutList(queryDate);
			
			for (List<FalcoEmployee> oneEmployee: list) {
				
				String empName = new String();
				
				if (oneEmployee != null && oneEmployee.size() > 0) {
					empName = oneEmployee.get(0).getTrName();
				}
				
				// NAME,CARD_NO,FIRST_IN,LAST_OUT
				String empDetailsCsv = empName;
				
				for (int i = 0; i < oneEmployee.size(); i++) {
					
					FalcoEmployee empDetails = oneEmployee.get(i);
					
					if (i == 0) {
						// first in
						empDetailsCsv = empDetailsCsv + "|" + empDetails.getCardNo() + "|" + empDetails.getTrTime();
					} else if (i == oneEmployee.size()-1) {
						// last out
						empDetailsCsv = empDetailsCsv + "|" + empDetails.getTrTime();
					}
					
				}
				
				writer.println(empDetailsCsv);
				
			}
			
			writer.flush();
			writer.close();
			writer = null;
		}
		
	}
	
	public void queryTimes() {
		try {
			test();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void testEmpDetailsList() {
		List<EmployeeDetails> list = get();
		
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
		
		for (EmployeeDetails emp: list) {
			if (emp.getTimeInOutList() != null && emp.getTimeInOutList().size() > 0) {
				System.out.println(emp.getLastName() + " " + emp.getCardNumber() + " ");
				for (TimeInOut timeInOut: emp.getTimeInOutList()) {
					
					System.out.print(simpleDateFormat.format(timeInOut.getTimeIn()) + " ");
					System.out.println(simpleDateFormat.format(timeInOut.getTimeOut()));
				}
			}
			
		}
		
//		timesheetReportGenerator.generateCutOffReport(list);
	}
	
	public static void main(String[] args) throws IOException {
		
		EmployeeTimeRecorder recorder = new EmployeeTimeRecorder();

//		 recorder.queryTimes();
		recorder.testEmpDetailsList();
		
		BiMonthlyGenerator timesheetReportGenerator = new BiMonthlyGenerator();
		timesheetReportGenerator.generateCutOffReport(recorder.get());
	}

}