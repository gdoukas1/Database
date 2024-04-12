import java.sql.*;
import java.util.*;

import org.postgresql.core.Query;
import org.postgresql.util.PSQLException;

import java.text.*;

public class DbApp {

	Connection conn;
	static int loop = 0;

	public DbApp() {

		try {
			Class.forName("org.postgresql.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			System.out.println("Driver not Found");
		}
	}

	public void dbConnect(String ip, String dbName, String username, String password) {
		try {
			conn = DriverManager.getConnection("jdbc:postgresql://" + ip + ":5432/" + dbName, username, password);

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void dbClose() {
		try {
			conn.close();
			System.out.println("Connection closed succesfully!");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@SuppressWarnings("deprecation")
	public void createQuery() {
		try {
			Statement st = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
			String Name = UserInput("Give the Prefix Hotel Name");
			ResultSet res = st.executeQuery(
					"SELECT row_number() over (order by \"idHotel\") as rn,\"idHotel\",name,stars,address,city,country,phone,fax"
							+ " From hotel  Where  name LIKE " + Name + " || '%'");
			while (res.next()) {
				System.out.println("rNo: " + res.getInt(1) + " |  idHotel: " + res.getInt(2) + " |  name: "
						+ res.getString(3) + " |  Stars: " + res.getString(4) + " |  Adress: " + res.getString(5)
						+ " |  City: " + res.getString(6) + " |  Country: " + res.getString(7) + " |  Phone: "
						+ res.getString(8) + " |  Fax: " + res.getString(9));
				System.out.println(
						"---------------------------------------------------------------------------------------------------"
								+ "------------------------------------------------------------------------------------------------------------");
			}
			System.out.println();
			res.absolute(Integer.parseInt(UserInput("Choose Hotel by rowNumber")));
			System.out.println("You Chose hotel:");
			System.out.println("rNo: " + res.getInt(1) + " |  idHotel: " + res.getInt(2) + " |  name: "
					+ res.getString(3) + " |  Stars: " + res.getString(4) + " |  Adress: " + res.getString(5)
					+ " |  City: " + res.getString(6) + " |  Country: " + res.getString(7) + " |  Phone: "
					+ res.getString(8) + " |  Fax: " + res.getString(9));

			printmenu();

			switch (UserInput("Enter Action (i,ii,iii): ")) {
			case "i":
				String prefixName = UserInput("Give the Prefix Last Name");
				ResultSet queryi = st.executeQuery(
						"SELECT * from find_clients_for_hotel_1_2_i(" + res.getInt(2) + "," + prefixName + ")");
				while (queryi.next()) {
					System.out.println("idPerson: " + queryi.getInt(1) + " | fname: " + queryi.getString(2)
							+ " |  lname: " + queryi.getString(3) + " | Sex: " + queryi.getString(4)
							+ " | dateofbirth: " + queryi.getDate(5) + " | dateofbirth: " + queryi.getString(6)
							+ " | city: " + queryi.getString(7) + " | country: " + queryi.getString(8)
							+ " | documentclient: " + queryi.getString(9));
					System.out.println(
							"---------------------------------------------------------------------------------------------------"
									+ "------------------------------------------------------------------------------------------------------------");
				}
				queryi.close();
				break;
			case "ii":
				Integer personNumber = Integer.parseInt(UserInput("Enter Person Id Number"));
				ResultSet queryii = st.executeQuery(
						"SELECT * from find_books_for_client_1_2_ii(" + res.getInt(2) + "," + personNumber + ")");
				while (queryii.next()) {
					System.out.println("rNo: " + queryii.getInt(1) + " | HotelbookingID: " + queryii.getInt(2)
							+ " | RoomId: " + queryii.getInt(3) + " | CheckinDate: " + queryii.getDate(4)
							+ " | CheckoutDate: " + queryii.getDate(5) + " | RoomCost: " + queryii.getInt(6));
					System.out.println(
							"----------------------------------------------------------------------------------------");
				}
				int nrow = Integer.parseInt(UserInput("Choose roombooking by rowNumber"));
				try {
					queryii.absolute(nrow);

					System.out.println("You Chose roombooking:");
					System.out.println("rNo: " + queryii.getInt(1) + " | HotelbookingID: " + queryii.getInt(2)
							+ " | RoomId: " + queryii.getInt(3) + " | CheckinDate: " + queryii.getDate(4)
							+ " | CheckoutDate: " + queryii.getDate(5) + " | RoomCost: " + queryii.getInt(6));

					java.sql.Date sqlStartDate = SqlDateConvert(UserInput("Choose new Checkin date"));
					java.sql.Date sqlEndDate = SqlDateConvert(UserInput("Choose new Checkout date"));
					float cost = Float.parseFloat(UserInput("Give the new Rate"));
					if (cost > 0) {
						disableTrigger();
					}
					Updatebooks(sqlStartDate, sqlEndDate, cost, queryii.getInt(2), queryii.getInt(3));

				} catch (PSQLException e) {
					System.err.println(e.getMessage());
					System.out.println("There are no roombookings in this hotel for the specific person id ");
				}
				enableTrigger();
				queryii.close();
				loop = Integer.parseInt(UserInput("Press 0 to return to main menu"));
				break;
			case "iii":
				java.sql.Date checkin = SqlDateConvert(UserInput("Choose Starting period date"));
				java.sql.Date checkout = SqlDateConvert(UserInput("Choose Ending period date"));

				ResultSet queryiii = st.executeQuery("SELECT * from hotel_available_rooms_1_2_iii(" + res.getInt(2)
						+ ",'" + checkin + "','" + checkout + "')");
				while (queryiii.next()) {
					System.out.println("rNo: " + queryiii.getInt(1) + " | RoomNumber: " + queryiii.getInt(2)
							+ " | RoomId: " + queryiii.getInt(3) + " | RoomType: " + queryiii.getString(4));
					System.out.println("--------------------------------------------------------------------");
				}
				int row = Integer.parseInt(UserInput("Choose room to reserve by rowNumber"));
				queryiii.absolute(row);
				java.sql.Date reservation = (java.sql.Date) checkin.clone();
				java.sql.Date cancellation = (java.sql.Date) checkin.clone();
				reservation.setDate(reservation.getDate() - 20);
				cancellation.setDate(cancellation.getDate() - 10);
				int clientid = Integer.parseInt(UserInput("Enter Person ID of Client"));
				Inserthotelbook(reservation, cancellation, clientid);
				InsertRoombook(checkin, checkout, queryiii.getInt(3), clientid);
				break;
			default:
				System.out.println("Wrong Input");
			}
			res.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void disableTrigger() {
		try {

			Statement trgStmt = conn.createStatement();
			trgStmt.addBatch("alter table roombooking disable trigger rate_trigger");
			trgStmt.executeBatch();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void enableTrigger() {
		try {
			Statement trgStmt = conn.createStatement();
			trgStmt.addBatch("alter table roombooking enable trigger rate_trigger");
			trgStmt.executeBatch();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void Updatebooks(java.sql.Date checkin, java.sql.Date checkout, float cost, int hotelid, int roomid) {
		String SQLupdate = "update roombooking  set checkin=? ,checkout=?,rate=? where \"hotelbookingID\"=?"
				+ " and \"roomID\"=?";

		try {
			PreparedStatement pstmt = conn.prepareStatement(SQLupdate);
			pstmt.setDate(1, checkin);
			pstmt.setDate(2, checkout);
			pstmt.setFloat(3, cost);
			pstmt.setInt(4, hotelid);
			pstmt.setInt(5, roomid);
			pstmt.executeUpdate();
			pstmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
			System.err.println(e.getMessage());
		}

	}

	public void Inserthotelbook(java.sql.Date reservation, java.sql.Date cancellation, int clientid) {
		String SQLinsert = "insert into hotelbooking values((select max(idhotelbooking)+1 from hotelbooking),?,?,?,?,?,?,? :: booking_status)";
		try {
			PreparedStatement pstmt = conn.prepareStatement(SQLinsert);
			pstmt.setDate(1, reservation);
			pstmt.setDate(2, cancellation);
			pstmt.setNull(3, java.sql.Types.REAL);
			pstmt.setInt(4, clientid);
			pstmt.setBoolean(5, false);
			pstmt.setNull(6, java.sql.Types.OTHER);
			pstmt.setObject(7, "confirmed");
			pstmt.executeUpdate();
			pstmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
			System.err.println(e.getMessage());
		}
	}

	public void InsertRoombook(java.sql.Date checkin, java.sql.Date checkout, int roomid, int clientid) {
		String SQLinsert = "insert into roombooking  values((select max(idhotelbooking) from hotelbooking),?,?,?,?)";
		try {
			PreparedStatement pstmt = conn.prepareStatement(SQLinsert);
			pstmt.setInt(1, roomid);
			pstmt.setInt(2, clientid);
			pstmt.setDate(3, checkin);
			pstmt.setDate(4, checkout);
			pstmt.executeUpdate();
			pstmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
			System.err.println(e.getMessage());
		}
	}

	public static String UserInput(String msg) {
		@SuppressWarnings("resource")
		Scanner scn = new Scanner(System.in);
		System.out.println(msg);
		return scn.nextLine();
	}

	public static void printmenu() {
		System.out.println();
		System.out.println(
				"---------------------------------------------------------------------------------------------------"
						+ "------------------------------------------------------------------------------------------------------------");
		System.out.println("Menu");
		System.out.println("Choose one of the following actions");
		System.out.println("* Action i: Search for hotel clients with a surname prefix");
		System.out.println("* Action ii: Display room booking details of a specific customer");
		System.out.println("--You can edit the checkin,checkout and rate of the booking."
				+ " If you want to return to main menu after the edit press 0");
		System.out.println("* Action iii: Display available rooms for a period of time");
		System.out.println("--You can insert a roombooking to one of the availiables rooms");
		System.out.println(
				"---------------------------------------------------------------------------------------------------"
						+ "------------------------------------------------------------------------------------------------------------");
		System.out.println();
	}

	public java.sql.Date SqlDateConvert(String DatetoConvert) {
		java.sql.Date sqlDate = null;
		java.util.Date date = null;
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
		try {
			date = formatter.parse(DatetoConvert);
		} catch (ParseException e) {
			e.printStackTrace();
		}

		sqlDate = new java.sql.Date(date.getTime());

		return sqlDate;

	}

	public static void main(String[] args) {
		DbApp app = new DbApp();
		app.dbConnect("localhost", "dbPhase_A_B", "postgres", "kyriakos98"); // Use your own postgresql password and
																			// database name

		while (loop == 0) {
			loop = 1;

			app.createQuery();
		}
		app.dbClose();
	}

}
