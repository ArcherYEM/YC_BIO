package ycbio.oracle;

/*
 * [최재우]
 * 1. Data 전처리 [y]20231019
 * 2. 생년월일로 나이 구현 [y]20231023
 * 3. 로그인 (ID, PW 일치값 확인) [y]20231023
 * 4. 회원가입 (ID 중복체크) [y]20231024
 * 5. ID 찾기 구현 (이름, 생년월일) [y]20231024
 * 6. PW 찾기 (이름, 생년월일, ID) [y]20231024
 * 7. 초기화면 돌아가기 기능 구현 [y]20231024
 * 8. 회원탈퇴 [y]20231025
 * 9. 날짜별 데이터 확인 기능 개발 [y]20231026
 * 10. 리뷰게시판 개발 [f]
 * 11. 프레젠테이션 준비 [n]
 * 
 * [양은모]
 * 1. Data 전처리 [y]20231019
 * 2. 회원가입 (ID, PW, NAME, GENDER, BIRTH) 구현 [y]20231022
 * 3. DATA BASE CONNECT 구현 [y]20231023
 * 4. 개인정보 제공 동의값 UPLOAD 구현 [y]20231023
 * 5. Test Class 초기값 setting [Y]20231023
 * 6. 기록 구현 [y]20231024
 * 7. 부족한 영양소 알고리즘 개발 [y]20231024
 * 8. 함유량 불러오기 Query 수정 [y]20231025
 * 9. 당일 섭취상황 기능 구현 [y]20231025
 * 10. 자원해제 종료 메서드 개발 [y]20231025
 * 11. Login ID/PW 상이 메서드 error catch [y]20231025
 * 12. 생년월일 입력 제한 기능 구현 [y]20231025
 * 13. ID/PW find 메서드 Bug 개선 [y]2023102526
 * 14. userView > 영양소의 종류 > 영양소 명, 효능 출력 기능 개발20231027
 * 15. 회원가입기능 문자열 검수 기능 개발 [y]20231027
 * 16. scan.nextInt 값 문자입력 Bug 개선 [y]20231027
 * 17. 프레젠테이션 준비 [y]20231027
 * 
 *  - To-do -
 *  1. 날짜별 데이터 기능 고도화 (출력값 수정, 입력값 제한)
 *  2. 부족 영양소 출력 고도화 (결핍증 동시 출력)
 * 
 *  - 발표 -
 *  1. 테이블 설계도
 *  2. 코드리뷰
 *  3. Run
*/

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

public class Member {

	Scanner scan = new Scanner(System.in);

	LocalDateTime dateTime = LocalDateTime.now();
	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
	String formattedDateTime = dateTime.format(formatter);
	Calendar now = Calendar.getInstance();

	Boolean personalInformation = false;
	String loginId;
	String loginPw;
	int loginAge;
	String loginGender;
	Boolean isUser = false;

	Boolean joinConsent = false;
	String joinId;
	String joinPw;
	String joinName;
	int joinBirth;
	int joinAge;
	String joinGender;

	String enteredProduct = null;

	String transStr;
	int transInteger;

	Connection connection = null;

	// 생성자
	public Member() {
		try {
			String url = "jdbc:oracle:thin:@localhost:1521:orcl";
			String username = "yang";
			String password = "a1234";

			Class.forName("oracle.jdbc.OracleDriver");
			connection = DriverManager.getConnection(url, username, password);
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}
	}

	// 초기화면
	void start() {
		try {
			System.out.println(" = = = = < YCBIO > = = = = = =");
			System.out.println();
			System.out.println("*      [1] 로그인");
			System.out.println("*      [2] 회원가입");
			System.out.println("*      [3] ID 찾기");
			System.out.println("*      [4] PW 찾기");
			System.out.println("*      [0] 종료");
			System.out.println();
			System.out.println(" = = = = = = = = = = = = = = =");
			System.out.println();
			System.out.print("입력 : ");

			String input = scan.nextLine();
			int click = Integer.parseInt(input);

			System.out.println();

			if (click == 1) {
				login();
			} else if (click == 2) {
				join();
			} else if (click == 3) {
				findID();
			} else if (click == 4) {
				findPW();
			} else if (click == 0) {
				exit();
			} else {
				System.out.println("잘못입력하셨습니다.");
				System.out.println();
			}
			start();
		} catch (NumberFormatException e) {
			System.out.println("숫자로 입력해주세요.");
			System.out.println();
			start();
		}
	}

	// 로그인
	void login() {

		try {
			if (connection != null) {
				System.out.println("= = = = = = LOG IN = = = = = =");
				System.out.println();
				// 사용자로부터 아이디와 비밀번호 입력
				System.out.print("* 아이디: ");
				String tempId = scan.nextLine();

				System.out.print("* 비밀번호: ");
				String tempPw = scan.nextLine();

				// SQL 쿼리를 사용하여 아이디와 비밀번호 확인
				String sql = "SELECT * FROM LOGIN WHERE ID = ? AND PW = ?";
				PreparedStatement preparedStatement = connection.prepareStatement(sql);
				preparedStatement.setString(1, tempId);
				preparedStatement.setString(2, tempPw);

				ResultSet loginSet = preparedStatement.executeQuery();

				if (loginSet.next()) {
					System.out.println("= = = = = = = = = = = = = = = =");
					System.out.println();
					loginId = tempId;

					// 성별 가져오기
					String logGenderSql = "SELECT GENDER FROM MEMBER_INFO WHERE ID = ?";
					PreparedStatement logGenderStatement = connection.prepareStatement(logGenderSql);
					logGenderStatement.setString(1, loginId);

					ResultSet loginGenderSet = logGenderStatement.executeQuery();

					if (loginGenderSet.next()) {
						loginGender = loginGenderSet.getString("GENDER");
					}

					// 나이대 가져오기
					String logAgeSql = "SELECT AGE FROM MEMBER_INFO WHERE ID = ?";
					PreparedStatement logAgeStatement = connection.prepareStatement(logAgeSql);
					logAgeStatement.setString(1, loginId);

					ResultSet loginAgeSet = logAgeStatement.executeQuery();

					if (loginAgeSet.next()) {
						loginAge = loginAgeSet.getInt("AGE");
					}

					isUser = true;
					userView();

				} else {
					System.out.println();
					System.out.println("로그인 실패. 아이디 또는 비밀번호가 잘못되었습니다.");
					System.out.println();
					login();
				}

				loginSet.close();
				preparedStatement.close();
			} else {
				System.out.println("오라클 데이터베이스 연결에 실패했습니다.");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	// 유저화면
	void userView() {
		boolean isUserView = true;
		try {
			while (isUserView) {
				String nameSql = "SELECT NAME FROM MEMBER_INFO WHERE ID = ?";
				PreparedStatement nameStatement = connection.prepareStatement(nameSql);
				nameStatement.setString(1, loginId);

				ResultSet resultSet = nameStatement.executeQuery();

				if (resultSet.next()) {
					String name = resultSet.getString("NAME");
					System.out.println("= = = = = [ " + name + " ] = = = = = =");
					System.out.println();
					System.out.println("*    [1] 오늘의 영양제 기록");
					System.out.println("*    [2] 오늘의 영양소 확인");
					System.out.println("*    [3] 날짜별 영양제 섭취 기록 확인");
					System.out.println("*    [4] 영양소의 효능");
					System.out.println("*    [5] 질문게시판");
					System.out.println("*    [9] 로그아웃");
					System.out.println("*    [0] 프로그램 종료");
					System.out.println();
					System.out.println("= = = = = = =  = = = = = = = =");
					System.out.print("입력 : ");
					transOn();
					int viewNumber = transInteger;
					if (viewNumber == 1) {
						newRecord();
					} else if (viewNumber == 2) {
						todayCheck();
					} else if (viewNumber == 3) {
						dateCheck();
					} else if (viewNumber == 4) {
						effect();
					} else if (viewNumber == 5) {
						qnaBoard();
					} else if (viewNumber == 9) {
						System.out.println();
						System.out.println("(^-^)(_-_)(^-^)[ 안녕히 가십시오 ](^-^)(_-_)(^-^)");
						System.out.println();
						start();
						isUserView = false;
					} else if (viewNumber == 0) {
						exit();
					} else {
						System.out.println();
						System.out.println("보기에 나와있는 숫자를 눌러주세요.");
						System.out.println();
					}

				} else {
					System.out.println();
					System.out.println("사용자 정보를 찾을 수 없습니다.");
					System.out.println();
				}
				transOff();
			}
		} catch (

		SQLException e) {
			e.printStackTrace();
		} catch (NumberFormatException e) {
			System.out.println();
			System.out.println("숫자로 입력해주세요.");
			System.out.println();
		} catch (InputMismatchException e) {
			System.out.println();
			System.out.println("숫자로 입력해주세요.");
			System.out.println();
		}
	}

	// 유저화면 - 영양제 기록하기
	void newRecord() {
		try {
			System.out.println();
			System.out.println("- - - - 제조사 선택 - - - -");

			// 제조사 목록을 정렬하여 가져오는 SQL 쿼리
			String manufacturerSql = "SELECT DISTINCT MANUFACTURER FROM ITEM ORDER BY MANUFACTURER";

			PreparedStatement manufacturerStatement = connection.prepareStatement(manufacturerSql);
			ResultSet manufacturerSet = manufacturerStatement.executeQuery();

			int manuSq = 1;
			while (manufacturerSet.next()) {
				String manufacturer = manufacturerSet.getString("MANUFACTURER");
				System.out.println("[" + manuSq + "] " + manufacturer);
				manuSq++;
			}

			System.out.println();
			System.out.println("---------------------");
			System.out.print("입력 : ");
			transOn();
			int manufacturerNumber = transInteger;

			// 선택한 제조사에 따른 제품 목록을 가져오는 SQL 쿼리
			String productSql = "SELECT DISTINCT PRODUCT FROM ITEM WHERE MANUFACTURER = ? ORDER BY PRODUCT";
			PreparedStatement productStatement = connection.prepareStatement(productSql);

			// 선택한 제조사를 가져옴
			manufacturerSet = manufacturerStatement.executeQuery();

			for (int i = 1; i <= manufacturerNumber; i++) {
				manufacturerSet.next();
			}

			String selectedManufacturer = manufacturerSet.getString("MANUFACTURER");

			productStatement.setString(1, selectedManufacturer);

			System.out.println();
			System.out.println("- - - - 제품명 선택 - - - -");

			ResultSet productSet = productStatement.executeQuery();

			int proSq = 1;
			while (productSet.next()) {
				String product = productSet.getString("PRODUCT");
				System.out.println("[" + proSq + "] " + product);
				proSq++;
			}

			System.out.println();
			System.out.print("입력 : ");
			transOn();
			int productNumber = transInteger;

			// 선택한 제조사와 영양제 출력
			manufacturerSet = manufacturerStatement.executeQuery();
			for (int i = 1; i <= manufacturerNumber; i++) {
				manufacturerSet.next();
			}
			selectedManufacturer = manufacturerSet.getString("MANUFACTURER");

			productSet = productStatement.executeQuery();
			for (int i = 1; i <= productNumber; i++) {
				productSet.next();
			}
			String selectedProduct = productSet.getString("PRODUCT");
			System.out.println("[" + selectedProduct + "] 기록완료");
			System.out.println();

			// 영양제의 영양성분 조회
			String ProductNutSql = "SELECT NUTRIENT FROM ITEM" + " WHERE MANUFACTURER = ? AND PRODUCT = ?";
			PreparedStatement ProductNutStatement = connection.prepareStatement(ProductNutSql);

			ProductNutStatement.setString(1, selectedManufacturer);
			ProductNutStatement.setString(2, selectedProduct);

			ResultSet productNutSet = ProductNutStatement.executeQuery();

			// 조회 영양성분 배열에 저장
			List<String> nutrientList = new ArrayList<>();

			while (productNutSet.next()) {
				String nutrient = productNutSet.getString("NUTRIENT");
				nutrientList.add(nutrient);
			}

			// 영양성분의 함유량 조회
			String eatenNutSql = "SELECT CONTENT FROM ITEM"
					+ " WHERE MANUFACTURER = ? AND PRODUCT = ? AND NUTRIENT IN (";
			for (int i = 0; i < nutrientList.size(); i++) {
				eatenNutSql += "?, ";
			}
			eatenNutSql = eatenNutSql.substring(0, eatenNutSql.length() - 2); // 맨 마지막 쉼표와 공백 제거
			eatenNutSql += ")";

			PreparedStatement eatenNutStatement = connection.prepareStatement(eatenNutSql);

			eatenNutStatement.setString(1, selectedManufacturer);
			eatenNutStatement.setString(2, selectedProduct);

			// 영양성분 배열의 값들을 쿼리의 파라미터로 설정
			for (int i = 0; i < nutrientList.size(); i++) {
				eatenNutStatement.setString(i + 3, nutrientList.get(i));
			}

			ResultSet eatenNutSet = eatenNutStatement.executeQuery();

			// 조회 함유량 배열에 저장
			List<Double> eatenNutrientList = new ArrayList<>();
			List<Double> contentList = new ArrayList<>();

			while (eatenNutSet.next()) {
				double eaten = eatenNutSet.getDouble("CONTENT");
				eatenNutrientList.add(eaten);
				contentList.add(eaten);
			}

			// 영양성분과 함유량을 함께 입력
			String eatenSql = "INSERT INTO RECORD (ID, PRODUCT, NUTRIENT, CONTENT)" + " VALUES (?, ?, ?, ?)";
			PreparedStatement eatenSqlStatement = connection.prepareStatement(eatenSql);

			for (int i = 0; i < nutrientList.size(); i++) {
				String nutrient = nutrientList.get(i);
				double eaten = eatenNutrientList.get(i);

				eatenSqlStatement.setString(1, loginId);
				eatenSqlStatement.setString(2, selectedProduct);
				eatenSqlStatement.setString(3, nutrient);
				eatenSqlStatement.setDouble(4, eaten);

				eatenSqlStatement.executeUpdate();
			}

			// 섭취된 영양소별 권장섭취량의 함유량 조회 후 리스트 저장
			String encouSql = "SELECT C.RECOMMEND" + " FROM CRITERIA C, MEMBER_INFO M" + " WHERE C.GENDER = ?"
					+ " AND C.AGE = ?" + " AND C.NUTRIENT = ?";
			PreparedStatement encouSqlStatement = connection.prepareStatement(encouSql);

			encouSqlStatement.setString(1, loginGender);
			encouSqlStatement.setInt(2, loginAge);

			List<Double> encouList = new ArrayList<>();

			for (int i = 0; i < nutrientList.size(); i++) {
				String nutrient = nutrientList.get(i);

				try {
					encouSqlStatement.setString(3, nutrient);

					ResultSet encouSet = encouSqlStatement.executeQuery();

					if (encouSet.next()) {
						double recommend = encouSet.getDouble("RECOMMEND");
						encouList.add(recommend);
					} else {
						encouList.add(null);
					}

					encouSet.close();
				} catch (SQLException e) {
					e.printStackTrace();
				} catch (NullPointerException e) {
					System.out.println("권장섭취량 불러오기에 실패하였습니다.");
				}
				
				transOff();
			}

			// 조회된 함유량 업데이트
			String encourageSql = "UPDATE RECORD SET ENCOURAGE = ? WHERE ID = ? AND PRODUCT = ? AND NUTRIENT = ?";
			PreparedStatement encourageSqlStatement = connection.prepareStatement(encourageSql);

			for (int i = 0; i < encouList.size(); i++) {
				Double encourage = encouList.get(i);
				encourageSqlStatement.setDouble(1, encourage);
				encourageSqlStatement.setString(2, loginId);
				encourageSqlStatement.setString(3, selectedProduct);

				String nutrient = nutrientList.get(i);
				encourageSqlStatement.setString(4, nutrient);

				encourageSqlStatement.executeUpdate();
			}

			// 부족영양수치 입력 (충족시 0 입력)
			String defSql = "UPDATE RECORD SET DEFICIENCE ="
					+ " CASE WHEN (ENCOURAGE - CONTENT) <= 0 THEN 0"
					+ " ELSE (ENCOURAGE - CONTENT) END"
					+ " WHERE ID = ? AND PRODUCT = ?";
			PreparedStatement defStatement = connection.prepareStatement(defSql);
			defStatement.setString(1, loginId);
			defStatement.setString(2, selectedProduct);
			defStatement.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();
			e.getMessage();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			transOff();
		}
	}

	// 오늘의 영양제 확인 - 양은모
	void todayCheck() {
		System.out.println(" = = = = [오늘의 영양상태] = = = = =");
		System.out.println();
		try {

			// 섭취한 영양소와 총 함유량을 저장하는 리스트
			List<String> eatenNutList = new ArrayList<>();
			List<Double> totalContentList = new ArrayList<>();

			// 전체 영양소와 권장 섭취량을 저장하는 리스트
			List<String> critNutList = new ArrayList<>();
			List<Double> critRecList = new ArrayList<>();

			// 섭취한 영양소 및 영양소별 총 함유량 조회/저장
			String totalContentSql = "SELECT NUTRIENT, SUM(CONTENT) AS TOTAL_CONTENT" + " FROM RECORD" + " WHERE ID = ?"
					+ " AND R_DATE >= TRUNC(SYSDATE) " + " GROUP BY NUTRIENT";

			PreparedStatement totalContentStatement = connection.prepareStatement(totalContentSql);
			totalContentStatement.setString(1, loginId);

			ResultSet totalContentSet = totalContentStatement.executeQuery();

			while (totalContentSet.next()) {
				String nutrient = totalContentSet.getString("NUTRIENT");
				double totalContent = totalContentSet.getDouble("TOTAL_CONTENT");

				eatenNutList.add(nutrient);
				totalContentList.add(totalContent);
			}

			// 전체 영양소와 권장섭취량 조회/저장
			String critSql = "SELECT C.NUTRIENT, C.RECOMMEND" + " FROM CRITERIA C INNER JOIN MEMBER_INFO M"
					+ " ON M.ID = ?" + " AND C.GENDER = ?" + " AND C.AGE = ?";

			PreparedStatement critStatement = connection.prepareStatement(critSql);
			critStatement.setString(1, loginId);
			critStatement.setString(2, loginGender);
			critStatement.setInt(3, loginAge);

			ResultSet critSet = critStatement.executeQuery();

			while (critSet.next()) {
				String critNut = critSet.getString("NUTRIENT");
				double critRec = critSet.getDouble("RECOMMEND");

				critNutList.add(critNut);
				critRecList.add(critRec);
			}

			// [부족] 헤더 출력
			System.out.println("- - - - - - - 부족 - - - - - - -");

			// 권장 섭취량보다 함유총량이 부족한 경우 차이를 계산하여 출력
			for (String critNut : critNutList) {
				int index = eatenNutList.indexOf(critNut);
				double totalContent = (index != -1) ? totalContentList.get(index) : 0.0;
				double critRec = critRecList.get(critNutList.indexOf(critNut));

				if (index != -1 && totalContent < critRec) {
					double diff = critRec - totalContent;
					System.out.printf("*  %s → %.2f mg 부족%n", critNut, diff);
				} else if (index == -1) {
					System.out.printf("*  %s → %.2f mg 부족%n", critNut, critRec);
				}
			}

			System.out.println();

			// [충족] 헤더 출력
			System.out.println("- - - - - - - 충족 - - - - - - -");

			// 권장 섭취량보다 함유총량이 크거나 같으면 "충족"을 출력
			for (String critNut : critNutList) {
				int index = eatenNutList.indexOf(critNut);
				double totalContent = (index != -1) ? totalContentList.get(index) : 0.0;
				double critRec = critRecList.get(critNutList.indexOf(critNut));

				if (index != -1 && totalContent >= critRec) {
					System.out.printf("*  %s%n", critNut);
				}
			}

			System.out.println();
			System.out.println("= = = = = = = = = = = = = = =");
			System.out.println();

		} catch (SQLException e) {
			e.printStackTrace();
		} catch (NumberFormatException e) {
			System.out.println();
			System.out.println("숫자로 입력해주세요.");
			System.out.println();
		}

	}

	// 날짜별 섭취상황 확인
	void dateCheck() {
		try {
//	        확인하실 날짜(8자리)를 입력해 주세요
			System.out.println("*   날짜별 영양제 섭취 기록 확인");
			System.out.println("복용 시작일 입력(8자리 YYYYMMDD):");
			String checkBeforeDate = scan.nextLine();

			int todayDate = Integer.parseInt(dateTime.format(DateTimeFormatter.ofPattern("yyyyMMdd")));
			if (Integer.parseInt(checkBeforeDate) < 19000101) {
				System.out.println("시작일자가 너무 빠릅니다.");
				System.out.println();
				dateCheck();
			} else if (Integer.parseInt(checkBeforeDate) > todayDate) {
				System.out.println("시작일자는 당일을 넘을 수 없습니다.");
				System.out.println();
				dateCheck();
			}
			System.out.println("복용 종료일 입력(8자리 YYYYMMDD):");
			String checkAfterDate = scan.nextLine();
			System.out.println("= = = = = = = = = = = = = = =");

//	        날짜 입력
			String checkId = loginId;
			System.out.println(" 복용하신 영양제");

//	        로그인시 입력한 아이디와 날짜에 해당하는 비타민명 출력
			String checkSql = "SELECT DISTINCT PRODUCT, R_DATE FROM RECORD WHERE ID = ?"
					+ " AND TO_CHAR(R_DATE, 'YYYYMMDD')>=?" + " AND TO_CHAR(R_DATE, 'YYYYMMDD')<=?"
					+ "ORDER BY R_DATE, PRODUCT";
			PreparedStatement checkPstm = connection.prepareStatement(checkSql);

			checkPstm.setString(1, checkId);
			checkPstm.setString(2, checkBeforeDate);
			checkPstm.setString(3, checkAfterDate);

			ResultSet checkRs = checkPstm.executeQuery();

			boolean foundCR = false;

			while (checkRs.next()) {
				String checkProduct = checkRs.getString("PRODUCT");
				String checkDate = checkRs.getString("R_DATE");

				System.out.println();
				System.out.println(checkDate + " // " + checkProduct);
				foundCR = true;
			}
			if (!foundCR) {
				System.out.println();
				System.out.println("해당하신 날짜에 복용하신 영양제가 없습니다.");
			}
			System.out.println("= = = = = =  = = = = = =");

		} catch (SQLException e) {
			e.printStackTrace();
		}

		userView();
	}

	// 영양소별 효능
	void effect() {
		try {
		String effectSql = "SELECT PROPERTY, NUTRIENT, EFFECT FROM EFFICACY";
			PreparedStatement effectStmt = connection.prepareStatement(effectSql);
			
			ResultSet effectSet = effectStmt.executeQuery();
			
			List<String> effectPropertyList = new ArrayList<>();
            List<String> effectNutrientList = new ArrayList<>();
            List<String> effectList = new ArrayList<>();
            
            while (effectSet.next()) {
                String property = effectSet.getString("PROPERTY");
                String nutrient = effectSet.getString("NUTRIENT");
                String effect = effectSet.getString("EFFECT");

                // 배열에 추가
                effectPropertyList.add(property);
                effectNutrientList.add(nutrient);
                effectList.add(effect);
            }
            
            for (int i = 0; i < effectPropertyList.size(); i++) {
            	System.out.println();
                System.out.print((i+1) + ") [" + effectPropertyList.get(i) + "] ");
                System.out.print("[" + effectNutrientList.get(i) + "] : ");
                System.out.print(effectList.get(i));
            }
            System.out.println("\n");
            userView();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		
		System.out.println();
		System.out.println("!! 업데이트 예정입니다 !!");
		System.out.println();
		userView();
	}

	// 회원가입
	void join() {
		try {
			// 개인정보동의
			boolean consent = true;
			while (consent) {
				System.out.println("= = = = = = = = = [개인정보제공 및 수집 동의서] = = = = = = = = = =");
				System.out.println("본인은 Java 프로그래밍을 실행해보기 위해 [개인정보 보호법] 제 15조 및 제 17조에 따라\n"
						+ "	아래의 내용으로 개인정보를 수집, 이용 및 제공하는데 동의합니다.\r\n\r\n"
						+ "수집하는 개인정보 항목 : 성명, 생년월일, 성별, 연락처, 이메일 그 외 회원가입시 기재하는 내용 일체\r\n"
						+ "개인정보 이용 목적 : 수집된 개인정보를 Java 프로그래밍 실행으로만 활용하며, 목적 외의 용도로는 사용하지 않습니다.\r\n"
						+ "귀하의 개인정보를 다음과 같이 보관하며, 수집, 이용 및 제공목적이 달성된 경우 [개인정보 보호법] 제 21조에 따라 처리합니다.\r\n\r\n"
						+ "본인은 개인정보 수집 및 이용에 대하여 ([1] 동의합니다  |  [2] 동의하지 않습니다)");
				System.out.println("");
				System.out.print("입력 : ");
				transOn();
				int info = transInteger;

				if (info == 1) {
					personalInformation = true;
					consent = false;
				} else if (info == 2) {
					personalInformation = false;
					consent = false;
					System.out.println("이전으로 돌아갑니다.");
					System.out.println();
					start();
				} else {
					System.out.println();
					System.out.println("잘못누르셨습니다.");
					System.out.println();
				}
				if (personalInformation) {
					System.out.println(formattedDateTime + " 개인정보제공에 동의하셨습니다");
					System.out.println();
					joinConsent = true;
				}
				transOff();

			}
		} catch (NumberFormatException e) {
			System.out.println();
			System.out.println("숫자로 입력해주세요.");
			System.out.println();
			join();
		} catch (InputMismatchException e) {
			System.out.println();
			System.out.println("숫자로 입력해주세요.");
			System.out.println("데이터 초기화를 위해 초기화면으로 돌아갑니다.");
			System.out.println();
			join();
		}

		// 아이디
		boolean passId = true;
		while (passId) {
			System.out.print("ID : ");
			String tempId = scan.nextLine();

			if (tempId.equals("")) {
				System.out.println("사용하실 아이디가 입력되지 않았습니다.");
			} else {
				joinId = tempId;
				passId = false;
			}
			// 아이디 중복 체크
			boolean isIdUnique = false;
			while (!isIdUnique) {
				try {
					String checkIdSql = "SELECT ID FROM MEMBER_INFO WHERE ID = ?";
					PreparedStatement checkIdPstmt = connection.prepareStatement(checkIdSql);
					checkIdPstmt.setString(1, joinId);
					ResultSet checkIdResult = checkIdPstmt.executeQuery();

					if (checkIdResult.next()) {
						System.out.println();
						System.out.println("이미 사용중인 아이디 입니다.");
						System.out.println();
						System.out.print("ID : ");
						joinId = scan.nextLine();
					} else {
						isIdUnique = true;
					}

				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}

		// 비밀번호
		boolean passPw = true;
		while (passPw) {
			System.out.print("PW : ");
			String tempPw1 = scan.nextLine();

			System.out.print("PW 재확인 : ");
			String tempPw2 = scan.nextLine();
			if (tempPw1.equals(tempPw2)) {
				joinPw = tempPw2;
				passPw = false;
			} else {
				System.out.println("입력하신 비밀번호가 이전 비밀번호와 일치하지 않습니다.");
				System.out.println();
			}
		}

		// 이름
		boolean passName = true;
		while (passName) {
			System.out.print("이름 : ");
			String tempName = scan.nextLine();
			if (!isValidName(tempName)) {
				System.out.println();
				System.out.println("올바른 이름을 입력해주세요. (문자열 또는 한글만 가능)\n");
			} else if (tempName.equals("")) {
				System.out.println();
				System.out.println("이름을 입력해주세요.\n");
			} else {
				joinName = tempName;
				passName = false;
			}
		}

		// 생년월일
		try {
			boolean passBirth = true;
			while (passBirth) {
				System.out.print("생년월일 : ");
				transOn();
				int tempBirth = transInteger;

				DateTimeFormatter tempFommeter = DateTimeFormatter.ofPattern("yyyyMMdd");
				int tempDate = Integer.parseInt(dateTime.format(tempFommeter));

				if (tempBirth < 10000000 || tempBirth > 99999999) {
					System.out.println("8자리로 입력해주세요");
					System.out.println("Ex) 19940830");
				} else if (tempBirth >= tempDate) {
					System.out.println("생년월일은 오늘 날짜보다 크거나 같을 수 없습니다.");
				} else if (((tempDate / 10000) - (tempBirth / 10000)) >= 122) {
					System.out.println("나이가 너무 많으신것 같습니다.");
				} else if ((tempBirth - (tempBirth / 10000) * 10000) / 100 > 12) {
					System.out.println("태어난 월 은 12월 보다 클 수 없습니다.");
				} else if ((tempBirth - (tempBirth / 10000) * 10000) / 100 < 1) {
					System.out.println("태어난 월 은 1월 보다 작을 수 없습니다.");
				} else if (tempBirth - (tempBirth / 100) * 100 > 31) {
					System.out.println("태어난 일이 잘못 입력 되었습니다. ");
				} else if (tempBirth - (tempBirth / 100) * 100 < 1) {
					System.out.println("태어난 일 이 1일 보다 작을 수 없습니다.");
				} else {
					joinBirth = tempBirth;
					passBirth = false;
				}

				transOff();
			}

			// 나이대
			Integer currentYear = now.get(Calendar.YEAR) + 1;
			joinAge = ((currentYear - (joinBirth / 10000)) / 10) * 10;

			// 성별
			boolean passGender = true;
			while (passGender) {
				System.out.println("[1] 남성  |  [2] 여성");
				System.out.print("성별 : ");
				String tempGenderStr = scan.nextLine();

				// 입력값이 숫자로만 이루어져 있는지 확인
				if (isNumeric(tempGenderStr)) {
					int tempGender = Integer.parseInt(tempGenderStr);
					if (tempGender == 1) {
						joinGender = "남성";
						passGender = false;
					} else if (tempGender == 2) {
						joinGender = "여성";
						passGender = false;
					} else {
						System.out.println();
						System.out.println("1 혹은 2 로 입력 바랍니다.");
						System.out.println();
					}
				} else {
					System.out.println();
					System.out.println("숫자로 입력해주세요.");
					System.out.println();
				}
			}

			// 최종확인
			boolean finalCheck = true;
			while (finalCheck) {
				System.out.println();
				System.out.println(" = = [입력 확인] = =");
				System.out.println("개인정보제공동의 : " + joinConsent);
				System.out.println("ID : " + joinId);
				System.out.println("PW : " + joinPw);
				System.out.println("이름 : " + joinName);
				System.out.println("생년월일 : " + joinBirth);
				System.out.println("연령대 : " + joinAge + "대");
				System.out.println("성별   : " + joinGender);
				System.out.println("= = = = = = = = = =");
				System.out.println();
				System.out.println("위 내용대로 회원가입을 진행하시겠습니까?");
				System.out.println("[1] 예\n[2] 아니오");
				System.out.print("입력 : ");

				transOn();
				int joinFinal = transInteger;

				System.out.println();

				if (joinFinal == 1) {
					System.out.println();
					System.out.println(joinName + "님, 가입해주셔서 감사합니다 !");
					finalCheck = false;
				} else if (joinFinal == 2) {
					System.out.println();
					System.out.println("초기화면으로 돌아갑니다");
					System.out.println();
				} else {
					System.out.println();
					System.out.println("잘못입력하셨습니다.");
				}
			}

			transOff();
		} catch (NumberFormatException e) {
			System.out.println();
			System.out.println("숫자로 입력해주세요.");
			System.out.println();
		}

		try {
			String joinSql = "INSERT INTO MEMBER_INFO (ID, PW, NAME, BIRTH, AGE, GENDER, CONSENT)"
					+ " VALUES (?, ?, ?, ?, ?, ?, ?)";

			PreparedStatement joinPstmt = connection.prepareStatement(joinSql);
			joinPstmt.setString(1, joinId);
			joinPstmt.setString(2, joinPw);
			joinPstmt.setString(3, joinName);
			joinPstmt.setInt(4, joinBirth);
			joinPstmt.setInt(5, joinAge);
			joinPstmt.setString(6, joinGender);
			if (joinConsent) {
				joinPstmt.setString(7, "Y");
			} else {
				joinPstmt.setString(7, "N");
			}

			int memberInfoCommit = joinPstmt.executeUpdate();

			// LOGIN 테이블 값 추가
			String loginSql = "INSERT INTO LOGIN (ID, PW)" + " VALUES (?, ?)";

			PreparedStatement loginPstmt = connection.prepareStatement(loginSql);
			loginPstmt.setString(1, joinId);
			loginPstmt.setString(2, joinPw);

			int loginCommit = loginPstmt.executeUpdate();

			// [결과확인]
			if (memberInfoCommit != 0 && loginCommit != 0) {
				System.out.println(joinName + "님, 가입해주셔서 감사합니다.");
				System.out.println();
				start();
			} else {
				System.out.println("회원가입 오류! 관리자에게 문의하세요");
			}
		} catch (SQLException sqle) {
			System.out.println("서버 접속 실패\n" + sqle.toString());
		} catch (Exception e) {
			System.out.println("Unknown Error");
			e.printStackTrace();
		}
	}

	// 아이디 찾기
	void findID() {

		try {

			String sql = "SELECT ID FROM MEMBER_INFO WHERE NAME = ? AND BIRTH = ?";
            PreparedStatement searchIdPstmt = connection.prepareStatement(sql);

            while (true) {
                System.out.print("가입시 등록한 이름 : ");
                String nameToSearch = scan.nextLine();

                System.out.print("가입시 등록한 생년월일(8자리) : ");
                transOn();
                int birthToSearch = transInteger;

                searchIdPstmt.clearParameters();
                searchIdPstmt.setString(1, nameToSearch);
                searchIdPstmt.setInt(2, birthToSearch);

                ResultSet searchIdRs = searchIdPstmt.executeQuery();

                if (searchIdRs.next()) {
                    String searchId = searchIdRs.getString("ID");
                    System.out.println();
                    System.out.println(nameToSearch + "님의 아이디는 : [ " + searchId + " ] 입니다");
                    System.out.println();
                } else {
                    System.out.println();
                    System.out.println("* 이   름 : " + nameToSearch);
                    System.out.println("* 생년월일 : " + birthToSearch);
                    System.out.println();
                    System.out.println("해당 이름과 생년월일로 가입된 회원이 존재하지 않습니다.");
                    System.out.println();
                }
                transOff();
				start();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (NumberFormatException e) {
			System.out.println();
			System.out.println("숫자로 입력해주세요.");
			System.out.println();
		}
	}

	// 비밀번호 찾기
	void findPW() {
		try {

			String sql = "SELECT PW FROM MEMBER_INFO WHERE ID = ? AND NAME = ? AND BIRTH = ?";
			PreparedStatement searchPwPstmt = connection.prepareStatement(sql);

			while (true) {
				System.out.print("가입시 등록한 ID : ");
				String idToSearch = scan.nextLine();
				System.out.print("가입시 등록한 이름 : ");
				String nameToSearch = scan.nextLine();
				System.out.print("가입시 등록한 생년월일(8자리) : ");
				transOn();
				int birthToSearch = transInteger;

				searchPwPstmt.setString(1, idToSearch);
				searchPwPstmt.setString(2, nameToSearch);
				searchPwPstmt.setInt(3, birthToSearch);

				ResultSet searchPwRs = searchPwPstmt.executeQuery();

				if (searchPwRs.next()) {
					String searchPw = searchPwRs.getString("PW");
					System.out.println();
					System.out.println(nameToSearch + "님의 패스워드는 [ " + searchPw + " ] 입니다");
					System.out.println();
					transOff();
					start();
				} else {
					System.out.println();
					System.out.println("* ID : " + idToSearch);
					System.out.println("* 이름 : " + nameToSearch);
					System.out.println("* 생년월일 : " + birthToSearch);
					System.out.println();
					System.out.println("입력하신 정보와 일치하는 회원이 존재하지 않습니다.");
					System.out.println();
				}
				transOff();
				start();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (NumberFormatException e) {
			System.out.println();
			System.out.println("숫자로 입력해주세요.");
			System.out.println();
		}
	}

	// QA 접속
	void qnaBoard() {
		try {
			while (true) {
				System.out.println();
				System.out.println("= = = = = [리뷰 게시판] = = = = =");
				System.out.println("*    [1] 리뷰 작성");
				System.out.println("*    [2] 리뷰 목록 보기");
				System.out.println("*    [0] 메인화면으로 돌아가기");
				System.out.println("= = = = = = = = = = = = = = = =");
				System.out.print("입력: ");
				transOn();
				int choice = transInteger;

				if (choice == 1) {
					askQuestion();

				} else if (choice == 2) {
					viewQuestions();

				} else if (choice == 0) {
					userView();
				} else {
					System.out.println("올바른 메뉴를 선택하세요.");
				}
				transOff();
			}
		} catch (NumberFormatException e) {
			System.out.println();
			System.out.println("숫자로 입력해주세요.");
			System.out.println();
		} catch (InputMismatchException e) {
			System.out.println();
			System.out.println("숫자로 입력해주세요.");
			System.out.println();
		}
	}

	// 질문 등록
	void askQuestion() {
		try {
			System.out.println();
			System.out.println("= = = = =  [리뷰 등록]  = = = = =");
			System.out.print("입력: ");
			String question = scan.nextLine();

			String aQuestionSql = "INSERT INTO QABOARD (ID, Q_TEXT,Q_DATE) VALUES (?, ?, SYSDATE)";
			PreparedStatement aQuestionStmt = connection.prepareStatement(aQuestionSql);
			aQuestionStmt.setString(1, loginId);
			aQuestionStmt.setString(2, question);

			aQuestionStmt.executeUpdate();

			System.out.println();
			System.out.println("= = = = =  [등록 완료]  = = = = =");

			qnaBoard();

		} catch (SQLException e) {
			e.printStackTrace();
		} catch (NumberFormatException e) {
			System.out.println();
			System.out.println("숫자로 입력해주세요.");
			System.out.println();
		} catch (InputMismatchException e) {
			System.out.println();
			System.out.println("숫자로 입력해주세요.");
			System.out.println();
		}
	}

	// 질문 보기
	void viewQuestions() {
		try {
			String vQuestionSql = "SELECT SEQ, ID, Q_TEXT, Q_DATE FROM QABOARD ORDER BY SEQ";

			PreparedStatement vQuestionStmt = connection.prepareStatement(vQuestionSql);
			ResultSet vQuestionRS = vQuestionStmt.executeQuery();

			while (vQuestionRS.next()) {
				int questionNumber = vQuestionRS.getInt("SEQ");
				String questionId = vQuestionRS.getString("ID");
				String questionText = vQuestionRS.getString("Q_TEXT");
				String questionDate = vQuestionRS.getString("Q_DATE");

				System.out.println("* 리뷰 NO. " + "[" + questionNumber + "]");
				System.out.println("* 리뷰 ID : " + questionId);
				System.out.println("* 리뷰 내용 : " + (questionText != null ? questionText : "질문 내용이 없습니다"));
				System.out.println("* 리뷰 작성일자 : " + (questionDate != null ? questionDate : "질문 내용이 없습니다"));
				System.out.println();
			}
			System.out.println("= = = = = = = = = = = = = = =");
			System.out.println("*    [1] 뒤로 가기");
			System.out.println("*    [0] 프로그램 종료");
			System.out.println("-----------------------------");
			System.out.print("*입력 : ");
			transOn();
			int select = transInteger;

			if (select == 1) {
				qnaBoard();
			} else if (select == 0) {
				exit();
			} else {
				System.out.println("= = = = = = = = = = = = = = =");
				System.out.println("잘못 입력 하셨습니다");
				qnaBoard();
			}
			transOff();

		} catch (SQLException e) {
			e.printStackTrace();
		} catch (NumberFormatException e) {
			System.out.println();
			System.out.println("숫자로 입력해주세요.");
			System.out.println();
		} catch (InputMismatchException e) {
			System.out.println();
			System.out.println("숫자로 입력해주세요.");
			System.out.println();
		}
	}

	// 회원탈퇴
	void withdraw() {

		try {
			System.out.println("탈퇴 할 사용자의 ID를 입력하세요: ");
			String idToDelete = scan.nextLine();

			System.out.println("탈퇴 하실 ID는 " + idToDelete + "입니다");

			System.out.println("*   [1] 계속 삭제하기");
			System.out.println("*   [2] 돌아가기");
			System.out.print("입력 : ");
			
			transOn();
			int viewNumber = transInteger;
			
			if (viewNumber == 1) {
				System.out.print("비밀번호 입력 : ");
				String pwToDelete = scan.nextLine();

				String deleteSql = "SELECT * FROM LOGIN WHERE ID = ? AND PW = ?";
				PreparedStatement deleteStatement = connection.prepareStatement(deleteSql);
				deleteStatement.setString(1, idToDelete);
				deleteStatement.setString(2, pwToDelete);

				ResultSet deleteSet = deleteStatement.executeQuery();

				// 해당 ID 관련 모든 DATA 삭제 쿼리
				if (deleteSet.next()) {

					String deleteMemberInfoSql = "DELETE FROM MEMBER_INFO WHERE ID = ?";
					PreparedStatement deleteMemberInfoStmt = connection.prepareStatement(deleteMemberInfoSql);
					deleteMemberInfoStmt.setString(1, idToDelete);
					int deletedRowsMemberInfo = deleteMemberInfoStmt.executeUpdate();

					String deleteLoginSql = "DELETE FROM LOGIN WHERE ID = ?";
					PreparedStatement deleteLoginStmt = connection.prepareStatement(deleteLoginSql);
					deleteLoginStmt.setString(1, idToDelete);
					int deletedRowsLogin = deleteLoginStmt.executeUpdate();

					String deleteRecordSql = "DELETE FROM RECORD WHERE ID = ?";
					PreparedStatement deleteRecordStmt = connection.prepareStatement(deleteRecordSql);
					deleteRecordStmt.setString(1, idToDelete);
					deleteRecordStmt.executeUpdate();

					if (deletedRowsMemberInfo == 1 && deletedRowsLogin == 1) {
						System.out.println("= = = 회원 탈퇴 성공 = = =");
						System.out.println();
					} else if (deletedRowsMemberInfo > 1 || deletedRowsLogin > 1) {
						System.out.println("회원 탈퇴 요청 건이 많습니다.\n확인바랍니다.");
						String rollbackSql = "ROLLBACK";
						PreparedStatement rollbackStmt = connection.prepareStatement(rollbackSql);
						rollbackStmt.executeUpdate();
						start();
					} else {
						System.out.println("비밀번호가 잘못되었습니다.");
						withdraw();
					}
				}
			} else if (viewNumber == 2) {
				userView();
			} else {
				System.out.print("잘못 누르셨습니다");
				withdraw();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (NumberFormatException e) {
			System.out.println();
			System.out.println("숫자로 입력해주세요.");
			System.out.println();
		}
		transOff();
		start();
	}

	// 문자열 숫자변환 기능 on
	void transOn() {
		transStr = scan.nextLine();
		if (!isNumeric(transStr)) {
			System.out.println();
			System.out.println("숫자로 입력하세요.");
			System.out.print("입력 : ");
			transOn(); // 다시 입력 받도록 재귀 호출
		} else {
			transInteger = Integer.parseInt(transStr);
		}
	}

	// 문자열 숫자 반환 기능 off
	void transOff() {
		transStr = null;
		transInteger = 0;
	}

	// 입력값이 문자인지 확인
	static boolean isValidName(String str) {
		String regex = "^[가-힣a-zA-Z]*$";
		return Pattern.matches(regex, str);
	}

	// 입력값이 숫자인지 확인
	@SuppressWarnings("unused")
	static boolean isNumeric(String str) {
		if (str == null) {
			return false;
		}
		try {
			int d = Integer.parseInt(str);
		} catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}

	// 프로그램 종료
	void exit() {
		try {
			if (connection != null) {
				connection.close();
			}
			System.out.println("프로그램을 종료합니다.");
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.exit(0);
	}

}