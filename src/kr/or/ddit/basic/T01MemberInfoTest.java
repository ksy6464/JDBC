package kr.or.ddit.basic;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Scanner;

import kr.or.ddit.util.JDBCUtil;

/*
	회원정보를 관리하는 프로그램을 작성하는데 
	아래의 메뉴를 모두 구현하시오. (CRUD기능 구현하기)
	(DB의 MYMEMBER테이블을 이용하여 작업한다.)
	
	* 자료 삭제는 회원ID를 입력 받아서 삭제한다.
	
	예시메뉴)
	----------------------
		== 작업 선택 ==
		1. 자료 입력			---> insert
		2. 자료 삭제			---> delete
		3. 자료 수정			---> update
		4. 전체 자료 출력	---> select
		5. 작업 끝.
	----------------------
	 
	   
// 회원관리 프로그램 테이블 생성 스크립트 
create table mymember(
    mem_id varchar2(8) not null,  -- 회원ID
    mem_name varchar2(100) not null, -- 이름
    mem_tel varchar2(50) not null, -- 전화번호
    mem_addr varchar2(128),    -- 주소
    reg_dt DATE DEFAULT sysdate, -- 등록일
    CONSTRAINT MYMEMBER_PK PRIMARY KEY (mem_id)
);

*/
public class T01MemberInfoTest {
	
	///인터페이스타입의 객체 변수 선언
	private Connection conn;
	private Statement stmt;
	private PreparedStatement pstmt;
	private ResultSet rs;
	
	private Scanner scan = new Scanner(System.in); ///스캐너 하나 만들어둠
	
	/**
	 * 메뉴를 출력하는 메서드
	 */
	public void displayMenu(){
		System.out.println();
		System.out.println("----------------------");
		System.out.println("  === 작 업 선 택 ===");
		System.out.println("  1. 자료 입력");
		System.out.println("  2. 자료 삭제");
		System.out.println("  3. 자료 수정");
		System.out.println("  4. 전체 자료 출력");
		System.out.println("  5. 작업 끝.");
		System.out.println("----------------------");
		System.out.print("원하는 작업 선택 >> ");
	}
	
	/**
	 * 프로그램 시작메서드
	 */
	public void start(){
		int choice;
		do{
			displayMenu(); //메뉴 출력
			choice = scan.nextInt(); // 메뉴번호 입력받기
			switch(choice){
				case 1 :  // 자료 입력
					insertMember();
					break;
				case 2 :  // 자료 삭제
					deleteMember();
					break;
				case 3 :  // 자료 수정
					updateMember();
					break;
				case 4 :  // 전체 자료 출력
					displayAllMember();
					break;
				case 5 :  // 작업 끝
					System.out.println("작업을 마칩니다.");
					break;
				default :
					System.out.println("번호를 잘못 입력했습니다. 다시입력하세요");
			}
		}while(choice!=5);
	}
	
	/**
	 * 모든 회원정보를 출력하기 위한 메서드
	 */
	private void displayAllMember() {
		
		///해더부분
		System.out.println();
		System.out.println("-----------------------------------------");
		System.out.println(" ID\t생성일\t이 름\t전화번호\t\t주 소");
		System.out.println("-----------------------------------------");
		
		try {
			conn = JDBCUtil.getConnection();
			
			stmt = conn.createStatement();
			
			rs = stmt.executeQuery(" select * from mymember ");
			
			while (rs.next()) {
				String memId = rs.getString("mem_id");
				String memName = rs.getString("mem_name");
				String memTel = rs.getString("mem_tel");
				String memAddr = rs.getString("mem_addr");
				
				///날짜만 가져오고 싶으면 한번더 형변환 해준다
				LocalDate regDt = rs.getTimestamp("reg_dt").toLocalDateTime().toLocalDate();
//				LocalDateTime regDt = rs.getTimestamp("reg_dt").toLocalDateTime();
				
				System.out.println(memId+"\t"+regDt+"\t"+memName+"\t"+memTel+"\t"+memAddr);
			}
			System.out.println("-----------------------------------------");
			System.out.println("출력 끝...");
			
		} catch (SQLException e) {
			
		}finally {
			JDBCUtil.close(conn, stmt, pstmt, rs);
		}
		
		
		
		
	}

	/**
	 * 회원정보를 삭제하기위한 메서드
	 */
	private void deleteMember() {
			
		System.out.println();
		System.out.println("삭제할 회원 정보를 입력해주세요.");
		System.out.println("회원 ID >>");
		String memId = scan.next();
		
		//////////////////////////////////////////////////////
		
		try {
			conn = JDBCUtil.getConnection();
			
			String sql = " delete from mymember where mem_id = ? ";
			
			pstmt = conn.prepareStatement(sql);
			pstmt.setNString(1, memId);
			
			int cnt = pstmt.executeUpdate();
			
			if(cnt > 0) {
				System.out.println(memId+"인 회원정보 삭제 성공!");
			}else {
				System.out.println(memId+"인 회원정보 삭제 실패!!!");
				
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}finally {
			JDBCUtil.close(conn, stmt, pstmt, rs);
		}
	}

	/**
	 * 회원정보를 수정하기 위한 메서드
	 */
	private void updateMember() {

		boolean isExist = false;
		
		String memId = "";
		
		do {
			
			System.out.println();
			System.out.println("수정할 회원 정보를 입력해주세요.");
			System.out.println("회원 ID >>");
			memId = scan.next();
			
			isExist = checkMember(memId);
			
			if (!isExist) {
				System.out.println("회워ID가 "+memId+"인 회원은 존재하지 않습니다.");
				System.out.println("다시 입력해주세요.");
			}
			
		} while (!isExist); ///true : 이미 존재하는 memId다 즉 없는 memId를 입력할때까지 do한다
		
		
		System.out.println("회원이름 >>");
		String memName = scan.next();
		
		System.out.println("회원 전화번호 >>");
		String memTel = scan.next();

		scan.nextLine(); // 입력버퍼에 남아있는 엔터키 제거용...
		
		System.out.println("회원 주소 >>");
		String memAddr = scan.nextLine();
		/// insert에 필요한 기본정보 다 받았다 이재 JDBC해보겠다
		
		////////////////////////////////////////////////////////////
		
		try {
			
			conn = JDBCUtil.getConnection();
			
			String sql = " update mymember set mem_name=?, mem_tel=?, mem_addr=? \r\n" + 
					"    where mem_id=? ";
			
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, memName);
			pstmt.setString(2, memTel);
			pstmt.setString(3, memAddr);
			pstmt.setString(4, memId);
			
			int cnt = pstmt.executeUpdate();
			
			if(cnt > 0) {
				System.out.println(memId+"인 회원정보 등록 성공!");
			}else {
				System.out.println(memId+"인 회원정보 등록 실패!!!");
				
			}
			
		} catch (SQLException ex) {
			ex.printStackTrace();
		} finally {
			JDBCUtil.close(conn, stmt, pstmt, rs);
		}
	}

	/**
	 * 회원정보를 등록하기 위한 메서드
	 */
	private void insertMember() {
		
		boolean isExist = false;
		
		String memId = "";
		
		do {
			
			System.out.println();
			System.out.println("추가할 회원 정보를 입력해주세요.");
			System.out.println("회원 ID >>");
			memId = scan.next();
			
			isExist = checkMember(memId);
			
			if (isExist) {
				System.out.println("회워ID가 "+memId+"인 회원은 이미 존재합니다.");
				System.out.println("다시 입력해주세요.");
			}
			
		} while (isExist); ///true : 이미 존재하는 memId다 즉 없는 memId를 입력할때까지 do한다
		
		
		System.out.println("회원이름 >>");
		String memName = scan.next();
		
		System.out.println("회원 전화번호 >>");
		String memTel = scan.next();

		scan.nextLine(); // 입력버퍼에 남아있는 엔터키 제거용...
		
		System.out.println("회원 주소 >>");
		String memAddr = scan.nextLine();
		/// insert에 필요한 기본정보 다 받았다 이재 JDBC해보겠다
		
		////////////////////////////////////////////////////////////
		
		try {
			// 옵션....
//			Class.forName("oracle.jdbc.driver.OracleDriver");
			/// 이 클래스는 오라클에서 제공해주는 클래스 이름이다 > 연결 안해주면 java.lang.ClassNotFoundException 에러가 난다
			///드라이브에 있는지 없는지 확인가능
			///JDBCUtil하면서 위에부분 지우심
			
//			conn = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:xe",
//												"pc07", 
//												"java");
			///JDBCUtil하면서 위에 부분 지우심
			
			conn = JDBCUtil.getConnection();
			
			String sql = " insert into mymember(mem_id, mem_name, mem_tel, mem_addr)\r\n" + 
					" values (?, ?, ?, ?) ";
			///쿼리 문제 안생기게 앞뒤로 띄어쓰기 한번씩 해주기
			
			pstmt = conn.prepareStatement(sql);
			
			pstmt.setString(1, memId);
			pstmt.setString(2, memName);
			pstmt.setString(3, memTel );
			pstmt.setString(4, memAddr);
			
			int cnt = pstmt.executeUpdate();
			///쿼리문이 업데이트...리턴값이 인트..업데이트된 행의 개수가 나온다
			///쿼리문이 섹렉트...리턴값은 리절트타입의 셋 객체...
			if(cnt > 0) {
				System.out.println(memId+"인 회원정보 등록 성공!");
			}else {
				System.out.println(memId+"인 회원정보 등록 실패!!!");
				
			}
			
			
			
			
		} catch (SQLException ex) { /// SQL에서 발생하는 에러의 최상위라서 다 잡을 수 있음
			ex.printStackTrace();
		} 
//			catch (ClassNotFoundException e) { ///이부분 JDBCUtil만들면서 사라짐
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} 
		finally {
			// 자원반납
			///클로즈를 (finally)여기다가 넣는 이유는 에외가 터지든 안터지든 실행이 되야 하기 때문에
			///사용한 객체 정리 작업
//	         if (rs != null) try {rs.close();} catch (SQLException ex) {}
//	         if (pstmt != null) try {pstmt.close();} catch (SQLException ex) {}
//	         if (stmt != null) try {stmt.close();} catch (SQLException ex) {}
//	         if (conn != null) try {conn.close();} catch (SQLException ex) {}
			JDBCUtil.close(conn, stmt, pstmt, rs);
		}

			

	}

	private boolean checkMember(String memId) {
		
		
		 boolean isExist = false;
	
		 try {
	
//				conn = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:xe",
//													"pc07", 
//													"java");
			 conn = JDBCUtil.getConnection();
				
				String sql = " select count(*) as cnt from mymember where mem_id=? ";
				///쿼리 문제 안생기게 앞뒤로 띄어쓰기 한번씩 해주기
				
				pstmt = conn.prepareStatement(sql);
				
				pstmt.setString(1, memId);
				
				rs = pstmt.executeQuery();
				
				int cnt = 0;
				while (rs.next()) {
					/// rs.next() : 데이터가 있으면 true, 없으면 false 를 반환
//					cnt = rs.getInt("CNT"); ///컬럼의 위치정보 또는 컬럼의 이름을 괄호에 넣으면된다
					cnt = rs.getInt(1); ///컬럼의 위치정보 또는 컬럼의 이름을 괄호에 넣으면된다
					///인덱습 번호는 0부터가 아닌 1부터 시작한다.
				}
				
				if(cnt > 0) {
					isExist = true;
					///이미 존재한다는 뜻이다.
				}
				
		} catch (SQLException e) {
			e.printStackTrace();
		}finally {
			//자원반남
			JDBCUtil.close(conn, stmt, pstmt, rs);
//			 if (rs != null) try {rs.close();} catch (SQLException ex) {}
//			 if (pstmt != null) try {pstmt.close();} catch (SQLException ex) {}
//			 if (stmt != null) try {stmt.close();} catch (SQLException ex) {}
//			 if (conn != null) try {conn.close();} catch (SQLException ex) {}
			
		}
		 return isExist;
	}

	public static void main(String[] args) {
		T01MemberInfoTest memObj = new T01MemberInfoTest();
		memObj.start();
	}

}






