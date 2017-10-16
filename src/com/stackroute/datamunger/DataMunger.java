package com.stackroute.datamunger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class DataMunger {

	// Regex for white space.
	private static final String WHITE_SPACE = "\\s+";

	public static void main(String[] args) throws IOException {
		// read the query from the user into queryString variable
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String queryString = br.readLine();

		// call the parseQuery method and pass the queryString variable as a parameter
		DataMunger dataMunger = new DataMunger();
		dataMunger.parseQuery(queryString);
	}

	/*
	 * we are creating multiple methods, each of them are responsible for extracting
	 * a specific part of the query. However, the problem statement requires us to
	 * print all elements of the parsed queries. Hence, to reduce the complexity, we
	 * are using the parseQuery() method. From inside this method, we are calling
	 * all the methods together, so that we can call this method only from main()
	 * method to print the entire output in console
	 */
	public void parseQuery(String queryString) {
		// call the methods
		getSplitStrings(queryString);
		getFile(queryString);
		getBaseQuery(queryString);
		getConditionsPartQuery(queryString);
		getConditions(queryString);
		getLogicalOperators(queryString);
		getFields(queryString);
		getOrderByFields(queryString);
		getGroupByFields(queryString);
		getAggregateFunctions(queryString);
	}

	/*
	 * this method will split the query string based on space into an array of words
	 * and display it on console
	 */
	public String[] getSplitStrings(String queryString) {
		if (null == queryString || queryString.trim().isEmpty()) {
			return null;
		}

		String[] strArray = queryString.trim().split(WHITE_SPACE);

		// Printing Array
		// printArray(strArray);

		return strArray;
	}

	/*
	 * extract the name of the file from the query. File name can be found after a
	 * space after "from" clause. Note: ----- CSV file can contain a field that
	 * contains from as a part of the column name. For eg: from_date,from_hrs etc.
	 * 
	 * Please consider this while extracting the file name in this method.
	 */
	public String getFile(String queryString) {
		if (null == queryString || queryString.trim().isEmpty()) {
			return null;
		}

		String[] strArray = queryString.trim().split(WHITE_SPACE);

		for (int i = 0; i < strArray.length; i++) {
			if (strArray[i].contains(".") && i > 0 && strArray[i - 1].equalsIgnoreCase("from")) {
				return strArray[i];
			}
		}
		return null;
	}

	/*
	 * This method is used to extract the baseQuery from the query string. BaseQuery
	 * contains from the beginning of the query till the where clause
	 * 
	 * Note: ------- 1. the query might not contain where clause but contain order
	 * by or group by clause 2. the query might not contain where, order by or group
	 * by clause 3. the query might not contain where, but can contain both group by
	 * and order by clause
	 */
	public String getBaseQuery(String queryString) {
		if (null == queryString || queryString.trim().isEmpty()) {
			return null;
		}

		String[] strArray = queryString.trim().split(WHITE_SPACE);

		String tempString = "";
		for (int i = 0; i < strArray.length; i++) {
			if (strArray[i].equalsIgnoreCase("where")
					|| ((strArray[i].equalsIgnoreCase("ORDER") || strArray[i].equalsIgnoreCase("GROUP"))
							&& i + 1 < strArray.length && strArray[i + 1].equalsIgnoreCase("BY"))) {
				break;
			}
			tempString += strArray[i] + " ";
		}
		//System.out.println(tempString.trim());
		return tempString;
	}

	/*
	 * This method is used to extract the conditions part from the query string. The
	 * conditions part contains starting from where keyword till the next keyword,
	 * which is either group by or order by clause. In case of absence of both group
	 * by and order by clause, it will contain till the end of the query string.
	 * Note: ----- 1. The field name or value in the condition can contain keywords
	 * as a substring. For eg: from_city,job_order_no,group_no etc. 2. The query
	 * might not contain where clause at all.
	 */
	public String getConditionsPartQuery(String queryString) {
		if (null == queryString || queryString.trim().isEmpty()) {
			return null;
		}

		String[] strArray = queryString.trim().split(WHITE_SPACE);

		String whereKeyword = null;
		String orderByGroupByKeyword = null;
		for (int i = 0; i < strArray.length; i++) {
			if (strArray[i].equalsIgnoreCase("WHERE")) {
				whereKeyword = strArray[i];
			}
			if ((strArray[i].equalsIgnoreCase("ORDER") || strArray[i].equalsIgnoreCase("GROUP"))
					&& i + 1 < strArray.length && strArray[i + 1].equalsIgnoreCase("BY")) {
				orderByGroupByKeyword = strArray[i] + " " + strArray[i + 1];
			}
		}

		if (null != whereKeyword) {
			int indexOfWhere = queryString.lastIndexOf(whereKeyword);
			indexOfWhere += "WHERE".length();
			int indexOfOrderByGroupByKeyword = queryString.length();
			if (null != orderByGroupByKeyword) {
				indexOfOrderByGroupByKeyword = queryString.indexOf(orderByGroupByKeyword);
			}
			// System.out.println(queryString.substring(indexOfWhere,
			// indexOfOrderByGroupByKeyword));
			return queryString.substring(indexOfWhere, indexOfOrderByGroupByKeyword);
		}
		return null;
	}

	/*
	 * This method will extract condition(s) from the query string. The query can
	 * contain one or multiple conditions. In case of multiple conditions, the
	 * conditions will be separated by AND/OR keywords. for eg: Input: select
	 * city,winner,player_match from ipl.csv where season > 2014 and city
	 * ='Bangalore'
	 * 
	 * This method will return a string array ["season > 2014","city ='Bangalore'"]
	 * and print the array
	 * 
	 * Note: ----- 1. The field name or value in the condition can contain keywords
	 * as a substring. For eg: from_city,job_order_no,group_no etc. 2. The query
	 * might not contain where clause at all.
	 */
	public String[] getConditions(String queryString) {
		if (null == queryString || queryString.trim().isEmpty()) {
			return null;
		}
		
		String conditionQuery = this.getConditionsPartQuery(queryString);
		
		if(null == conditionQuery) {
			return null;
		}
		
		String[] strArray = conditionQuery.trim().split(WHITE_SPACE);
		String returnString = "";
		for (int i = 0; i < strArray.length; i++) {
			String temp = "";
			if (strArray[i].contains(">")
					|| strArray[i].contains("<")
					|| strArray[i].contains("!")
					|| strArray[i].contains("=")) {
				temp += strArray[i] + " ";
				
				if(i + 1 < strArray.length && checkForDirtyData(temp)) {
					temp += strArray[i + 1];
				}
				if(i - 1 >= 0) {
					temp = strArray[i - 1] + " " + temp;
				}
				
				returnString += temp.trim() + "-";
			}
		}
		return (null == returnString || returnString.isEmpty()) ? null : returnString.trim().split("-");
	}

	private boolean checkForDirtyData(String temp) {
		boolean hasSpecialChar = false;
		for (int j = 0; j < temp.length(); j++) {
			char ch = temp.charAt(j);
			if(ch == '<' || ch == '>' || ch == '!' || ch == '=') {
				hasSpecialChar = true;
			}
			if((ch+"").matches("^[a-zA-Z]+$") && hasSpecialChar) {
				return false;
			}
		}
		return true;
	}

	/*
	 * This method will extract logical operators(AND/OR) from the query string. The
	 * extracted logical operators will be stored in a String array which will be
	 * returned by the method and the same will be printed Note: ------- 1. AND/OR
	 * keyword will exist in the query only if where conditions exists and it
	 * contains multiple conditions. 2. AND/OR can exist as a substring in the
	 * conditions as well. For eg: name='Alexander',color='Red' etc. Please consider
	 * these as well when extracting the logical operators.
	 * 
	 */
	public String[] getLogicalOperators(String queryString) {
		if (null == queryString || queryString.trim().isEmpty()) {
			return null;
		}

		String[] strArray = queryString.trim().split(WHITE_SPACE);

		boolean whereFound = false;
		String tempString = "";
		for (int i = 1; i < strArray.length - 1; i++) {
			if (strArray[i].equalsIgnoreCase("WHERE")) {
				whereFound = true;
			}
			if (whereFound && ("and".equalsIgnoreCase(strArray[i]) || "or".equalsIgnoreCase(strArray[i])
					|| "not".equalsIgnoreCase(strArray[i]))) {
				tempString += strArray[i] + " ";
			}
		}

		// Printing Array
		//printArray(tempString.trim().split(WHITE_SPACE));

		return (null == tempString || tempString.isEmpty()) ? null : tempString.trim().split(WHITE_SPACE);
	}

	/*
	 * This method will extract the fields to be selected from the query string. The
	 * query string can have multiple fields separated by comma. The extracted
	 * fields will be stored in a String array which is to be printed in console as
	 * well as to be returned by the method
	 * 
	 * Note: ------ 1. The field name or value in the condition can contain keywords
	 * as a substring. For eg: from_city,job_order_no,group_no etc. 2. The field
	 * name can contain '*'
	 * 
	 */
	public String[] getFields(String queryString) {
		if (null == queryString || queryString.trim().isEmpty()) {
			return null;
		}

		String[] strArray = queryString.trim().split(WHITE_SPACE);

		String[] fieldArr = null;
		for (int i = 1; i < strArray.length - 1; i++) {
			if (strArray[i].equalsIgnoreCase("SELECT")) {
				continue;
			}
			if (strArray[i].equalsIgnoreCase("FROM")) {
				break;
			}
			fieldArr = strArray[i].split(",");
			// Printing Array
			// printArray(fieldArr);
		}
		return fieldArr;
	}

	/*
	 * This method extracts the order by fields from the query string. Note: ------
	 * 1. The query string can contain more than one order by fields. 2. The query
	 * string might not contain order by clause at all. 3. The field names,condition
	 * values might contain "order" as a substring. For eg:order_number,job_order
	 * Consider this while extracting the order by fields
	 */
	public String[] getOrderByFields(String queryString) {
		if (null == queryString || queryString.trim().isEmpty()) {
			return null;
		}

		String[] strArray = queryString.trim().split(WHITE_SPACE);

		String[] fieldArr = null;
		boolean orderByFound = false;
		for (int i = 1; i < strArray.length; i++) {
			if (strArray[i].equalsIgnoreCase("Order") && strArray[i + 1].equalsIgnoreCase("by")) {
				orderByFound = true;
				i++;
				continue;
			}
			if (orderByFound) {
				fieldArr = strArray[i].split(",");
			}
		}

		// Printing Array
		// printArray(fieldArr);

		return fieldArr;
	}

	/*
	 * This method extracts the group by fields from the query string. Note: ------
	 * 1. The query string can contain more than one group by fields. 2. The query
	 * string might not contain group by clause at all. 3. The field names,condition
	 * values might contain "group" as a substring. For eg: newsgroup_name
	 * 
	 * Consider this while extracting the group by fields
	 */
	public String[] getGroupByFields(String queryString) {
		if (null == queryString || queryString.trim().isEmpty()) {
			return null;
		}

		String[] strArray = queryString.trim().split(WHITE_SPACE);

		String[] fieldArr = null;
		boolean orderByFound = false;
		for (int i = 1; i < strArray.length; i++) {
			if (strArray[i].equalsIgnoreCase("Group") && strArray[i + 1].equalsIgnoreCase("by")) {
				orderByFound = true;
				i++;
				continue;
			}
			if (orderByFound) {
				fieldArr = strArray[i].split(",");
			}
		}

		// Printing Array
		// printArray(fieldArr);

		return fieldArr;
	}

	/*
	 * This method extracts the aggregate functions from the query string. Note:
	 * ------ 1. aggregate functions will start with "sum"/"count"/"min"/"max"/"avg"
	 * followed by "(" 2. The field names might
	 * contain"sum"/"count"/"min"/"max"/"avg" as a substring. For eg:
	 * account_number,consumed_qty,nominee_name
	 * 
	 * Consider this while extracting the aggregate functions
	 */
	public String[] getAggregateFunctions(String queryString) {
		if (null == queryString || queryString.trim().isEmpty()) {
			return null;
		}

		String[] strArray = queryString.trim().split(WHITE_SPACE);

		String tempString = "";
		for (int i = 1; i < strArray.length - 1; i++) {
			if (strArray[i].equalsIgnoreCase("SELECT")) {
				continue;
			}
			if (strArray[i].equalsIgnoreCase("FROM")) {
				break;
			}

			if (strArray[i].contains("sum(") || strArray[i].contains("count(") || strArray[i].contains("max(")
					|| strArray[i].contains("min(") || strArray[i].contains("avg(")) {
				// System.out.println(strArray[i]);
				tempString += strArray[i];
			}
		}
		
		return (null == tempString || tempString.isEmpty()) ? null : tempString.trim().split(",");
	}

	/**
	 * Method is used to print Array.
	 * 
	 * @param strArray
	 */
	private static void printArray(String[] strArray) {
		if (null == strArray || strArray.length == 0) {
			return;
		}

		// Printing Array.
		for (String str : strArray) {
			System.out.println(str);
		}
	}
}