package whiteboxsystems;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;

import orderinfo.CustomerInfo;
import orderinfo.OrderDetails;
import orderinfo.PaymentInfo;
import orderinfo.ProductInfo;

public class SQLAdapter implements DatabaseAdapter {
		
	// CustomerInfo
	private final static String CUSTOMER_ID = "customer_id";
	private final static String CUSTOMER_BUILD_IDS = "build_ids";
	private final static String NAME = "name";
	private final static String EMAIL = "email";
	private final static String PHONE_NUMBER = "phone_number";
	private final static String ADDRESS = "address";
	private final static String DELIVERY_DATE = "delivery_date";
	
	// PaymentInfo
	private final static String PAYMENT_BUILD_ID = "build_id";
	private final static String PAYMENT_METHOD = "payment_method";
	private final static String TOTAL_VALUE = "total_value";
	private final static String DELIVERY_CONFIRMATION = "delivery_confirmation";
	
	// ProductInfo
	private final static String PRODUCT_BUILD_ID = "build_id";
	private final static String COMPONENT_TYPE = "component_type";
	private final static String MANUFACTURER = "manufacturer";
	private final static String PRODUCT_DESCRIPTION = "product_description";
	private final static String MODEL_NUMBER = "model_number";
	private final static String SERIAL_NUMBER = "serial_number";
	private final static String REBATE_VALUE = "rebate_value";
	private final static String PRICE = "price";
	private final static String WARRANTY_PERIOD = "warranty_period";
	private final static String WARRANTY_EXPIRY = "warranty_expiry";
	private final static String INVOICE_DATE = "invoice_date";
	private final static String INVOICE_NUMBER = "invoice_number";
	private final static String SALES_ORDER_NUMBER = "sales_order_number";
	private final static String ITEM_SKU = "item_sku";
	
	public static Connection getConnection() throws Exception {
		try {
			String driver = "com.mysql.jdbc.Driver";
			String url = "jdbc:mysql://whiteboxsystemsdb.cvr6heqg7wkw.us-west-2.rds.amazonaws.com:3306/whiteboxsystems";
			String username = "nfciotrestaurant";
			String password = "HussainIsTheBest";
			Class.forName(driver); 
			Connection conn = DriverManager.getConnection(url,username,password);
			System.out.println("Connected");
			return conn;
		} catch (Exception e) {
			System.out.println(e);
			return null;
		}
	}

	@Override
	public void addOrder(OrderDetails orderDetails) {
		// TODO Auto-generated method stub
		try {
			// get the next BuildID available
			Integer buildID = getNextBuildID();
			orderDetails.setBuildID(buildID);
			
			Connection conn = getConnection();
			CustomerInfo customerInfo = orderDetails.getCustomerInfo();
			PaymentInfo paymentInfo = orderDetails.getPaymentInfo();
			Collection<ProductInfo> productInfo = orderDetails.getComponents();
			
			//Update Table customer_info
			// TODO query on CUSTOMER_ID instead of PHONE_NUMBER
			PreparedStatement prepStmt = null;
			String query = "SELECT * FROM customer_info WHERE " + PHONE_NUMBER + " = ?";
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, customerInfo.getPhoneNum());
			ResultSet rs = prepStmt.executeQuery();
			if (!rs.next() ) {
			    query = "INSERT INTO customer_info (" + CUSTOMER_BUILD_IDS + "," + NAME + "," + EMAIL + "," 
			    		+ PHONE_NUMBER + "," + ADDRESS + "," + DELIVERY_DATE + ")"
			    		+ "VALUES (?,?,?,?,?,?)";
			    
			    prepStmt = conn.prepareStatement(query);
				prepStmt.setInt(1, orderDetails.getBuildID());
				prepStmt.setString(2, customerInfo.getName());
				prepStmt.setString(3, customerInfo.getEmail());
				prepStmt.setString(4, customerInfo.getPhoneNum());
				prepStmt.setString(5, customerInfo.getAddress());
				prepStmt.setString(6, customerInfo.getDeliveryDate());
			} else {
				query = "UPDATE customer_info SET " + CUSTOMER_BUILD_IDS + "= ? WHERE " + PHONE_NUMBER + " = ?";
				
				prepStmt = conn.prepareStatement(query);
				prepStmt.setString(1, rs.getString(CUSTOMER_BUILD_IDS) + orderDetails.getBuildID());
				prepStmt.setString(2, customerInfo.getName());
			}
			System.out.println(prepStmt.toString());
			
			prepStmt.executeUpdate();
			
			// TODO catch duplicate BUILD_ID
			//Update Table payment_info
			/*query = "INSERT INTO payment_info VALUES ('" + orderDetails.getBuildID() + "', '"
					+ paymentInfo.getPaymentMethod() + "', '" + paymentInfo.getTotalValue() + "', '"
					+ paymentInfo.getDeliveryConfirmationFile() + "');";
			*/
			query = "INSERT INTO payment_info VALUES (?,?,?,?);";
			prepStmt = conn.prepareStatement(query);
			prepStmt.setInt(1, orderDetails.getBuildID());
			prepStmt.setString(2, paymentInfo.getPaymentMethod());
			prepStmt.setDouble(3, paymentInfo.getTotalValue());
			prepStmt.setString(4, paymentInfo.getDeliveryConfirmationFile());
			System.out.println(prepStmt.toString());
			prepStmt.executeUpdate();
			
			//Update Table product_info
			ArrayList<ProductInfo> prodInfo = (ArrayList<ProductInfo>)productInfo;
			for (int i = 0; i < prodInfo.size(); i++) {
				query = "INSERT INTO product_info VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
				prepStmt = conn.prepareStatement(query);
				prepStmt.setInt(1, orderDetails.getBuildID());
				prepStmt.setString(2, prodInfo.get(i).getComponentType());
				prepStmt.setString(3, prodInfo.get(i).getManufacturer());
				prepStmt.setString(4, prodInfo.get(i).getDescription());
				prepStmt.setString(5, prodInfo.get(i).getModelNum());
				prepStmt.setString(6, prodInfo.get(i).getSerialNum());
				prepStmt.setDouble(7, prodInfo.get(i).getRebateValue());
				prepStmt.setDouble(8, prodInfo.get(i).getPrice());
				prepStmt.setString(9, prodInfo.get(i).getWarrantyPeriod());
				prepStmt.setString(10, prodInfo.get(i).getWarrantyExpiry());
				prepStmt.setString(11, prodInfo.get(i).getInvoiceDate());
				prepStmt.setInt(12, prodInfo.get(i).getInvoiceNum());
				prepStmt.setInt(13, prodInfo.get(i).getSalesOrderNum());
				prepStmt.setInt(14, prodInfo.get(i).getItemSKU());
				System.out.println(prepStmt.toString());
				prepStmt.executeUpdate();
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Integer getNextBuildID() throws Exception{
		String query = "SELECT MAX(" + PAYMENT_BUILD_ID + ") FROM payment_info;";
		
		Integer buildID = null;
		
		Connection conn;
		try {
			conn = getConnection();
			Statement st = conn.createStatement();
			ResultSet rs = st.executeQuery(query);
			
			while (rs.next()) {
				buildID = rs.getInt(".MAX(" + PAYMENT_BUILD_ID + ")");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if (buildID == null){
			throw new Exception();
		}
		
		return buildID + 1;
	}
	
	public ArrayList<OrderDetails> getExistingOrders() {
	// TODO Auto-generated method stub
		try {
			Connection conn = getConnection();
			ArrayList<OrderDetails> orderDetailsList = new ArrayList<OrderDetails>();
			String query = "SELECT * FROM customer_info";
			PreparedStatement prepStmt = conn.prepareStatement(query);
			ResultSet rs = prepStmt.executeQuery();
		
			//Get Customer Info
			while (rs.next()) {
				String buildIDs = rs.getString(CUSTOMER_BUILD_IDS);
				String name = rs.getString(NAME);
				String email = rs.getString(EMAIL);
				String phoneNumber = rs.getString(PHONE_NUMBER);
				String address = rs.getString(ADDRESS);
				String deliveryDate = rs.getString(DELIVERY_DATE);
				System.out.println(buildIDs + " " + name + " " + email + " " + phoneNumber + " " + address + " " + deliveryDate);
				String bIDs[] = buildIDs.split(","); 
				for (int i = 0; i < bIDs.length; i++) {
					OrderDetails od = new OrderDetails();
					od.setBuildID(Integer.parseInt(bIDs[i]));
					CustomerInfo ci = new CustomerInfo(name, email, phoneNumber, address, deliveryDate);
					od.setCustomerInfo(ci);
					orderDetailsList.add(od);
				}
			}
		
			for (int i = 0; i < orderDetailsList.size(); i++) {
				//query = "SELECT * FROM payment_info WHERE " + PAYMENT_BUILD_ID + "= \'" + orderDetailsList.get(i).getBuildID() + "\'";
				query =  "SELECT * FROM payment_info WHERE " + PAYMENT_BUILD_ID + "= ?";
				prepStmt = conn.prepareStatement(query);
				prepStmt.setInt(1,orderDetailsList.get(i).getBuildID());
				rs = prepStmt.executeQuery();
				while (rs.next()) {
					String paymentMethod = rs.getString(PAYMENT_METHOD);
					Double totalValue = rs.getDouble(TOTAL_VALUE);
					String deliveryConfirmation = rs.getString(DELIVERY_CONFIRMATION);
					PaymentInfo pi = new PaymentInfo(paymentMethod, totalValue, deliveryConfirmation);
					orderDetailsList.get(i).setPaymentInfo(pi);
					System.out.println(paymentMethod + " " + totalValue + " " + deliveryConfirmation);
				}
			}
		
			for (int i = 0; i < orderDetailsList.size(); i++) {
				//query = "SELECT * FROM product_info WHERE " + PRODUCT_BUILD_ID +" = \'" + orderDetailsList.get(i).getBuildID() + "\'";
				query = "SELECT * FROM product_info WHERE " + PRODUCT_BUILD_ID + " = ?";
				prepStmt = conn.prepareStatement(query);
				prepStmt.setInt(1,orderDetailsList.get(i).getBuildID());
				rs = prepStmt.executeQuery();
				Collection<ProductInfo> collectionProdInfo = new ArrayList<ProductInfo>(); 
				while (rs.next()) {
					String componentType = rs.getString(COMPONENT_TYPE);
					String manufacturer = rs.getString(MANUFACTURER);
					String productDescription = rs.getString(PRODUCT_DESCRIPTION);
					String modelNumber = rs.getString(MODEL_NUMBER);
					String serialNumber = rs.getString(SERIAL_NUMBER);
					Double rebateValue = rs.getDouble(REBATE_VALUE);
					Double price = rs.getDouble(PRICE);
					String warrantyPeriod = rs.getString(WARRANTY_PERIOD);
					String warrantyExpiry = rs.getString(WARRANTY_EXPIRY);
					String invoiceDate = rs.getString(INVOICE_DATE);
					Integer invoiceNumber = rs.getInt(INVOICE_NUMBER);
					Integer salesOrderNumber = rs.getInt(SALES_ORDER_NUMBER);
					Integer itemSKU = rs.getInt(ITEM_SKU);
					ProductInfo prodInfo = new ProductInfo(componentType, manufacturer, productDescription, modelNumber, serialNumber, rebateValue, price, warrantyPeriod, warrantyExpiry, invoiceDate, invoiceNumber, salesOrderNumber, itemSKU);
					System.out.println(componentType + " " + manufacturer + " " + productDescription + " " + modelNumber + " " + serialNumber + " " + rebateValue + " " + price + " " + warrantyPeriod + " " + warrantyExpiry + " " + invoiceDate + " " + invoiceNumber + " " + salesOrderNumber + " " + itemSKU);
					collectionProdInfo.add(prodInfo);
				}
				orderDetailsList.get(i).setComponents(collectionProdInfo);
			}
		
			return orderDetailsList;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
				
	}
	
	
	
}
