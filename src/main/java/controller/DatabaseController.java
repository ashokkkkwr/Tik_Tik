package controller;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import util.ProductStringUtils;
import util.StringUtils;
import model.CartModel;
import model.PasswordEncryptionWithAes;
import model.ProductsModel;
import model.UsersModel;

public class DatabaseController {

    public Connection getConnection() throws SQLException, ClassNotFoundException {
        Class.forName("com.mysql.jdbc.Driver");
        String url = "jdbc:mysql://localhost:3306/tik_tik";
        String user = "root";
        String pass = "";
        return DriverManager.getConnection(url, user, pass);
    }

    public int addUser(UsersModel userModel) {
        try (Connection con = getConnection()) {
            PreparedStatement checkUsernameUser = con.prepareStatement(StringUtils.GET_USERNAME);
            checkUsernameUser.setString(1, userModel.getUserName());
            ResultSet checkUsernameRs = checkUsernameUser.executeQuery();

            if (checkUsernameRs.next() && checkUsernameRs.getInt(1) > 0) {
                return -2; // Username exists
            }

            PreparedStatement checkPhoneUser = con.prepareStatement(StringUtils.GET_PHONE);
            checkPhoneUser.setString(1, userModel.getPhone());
            ResultSet checkPhoneRs = checkPhoneUser.executeQuery();

            if (checkPhoneRs.next() && checkPhoneRs.getInt(1) > 0) {
                return -4; // Phone Number exists
            }

            PreparedStatement checkEmailUser = con.prepareStatement(StringUtils.GET_EMAIL);
            checkEmailUser.setString(1, userModel.getEmail());
            ResultSet checkEmailRs = checkEmailUser.executeQuery();

            if (checkEmailRs.next() && checkEmailRs.getInt(1) > 0) {
                return -3; // Email exists
            }

            // Encrypt password before storing it in the database
            String encryptedPassword = PasswordEncryptionWithAes.encryptPassword(userModel.getPassword(), "U3CdwubLD5yQbUOG92ZnHw==");

            PreparedStatement st = con.prepareStatement(StringUtils.SIGNUP);
            st.setString(1, userModel.getUserName());
            st.setString(2, userModel.getEmail());
            st.setString(3, userModel.getLocation());
            st.setString(4, userModel.getPhone());
            st.setString(5, encryptedPassword);
            st.setString(6, userModel.getImageUrlFromPart());
            int result = st.executeUpdate();
            return result > 0 ? 1 : 0;
        } catch (SQLException | ClassNotFoundException ex) {
            ex.printStackTrace(); // Log the exception for debugging
            return -1;
        } catch (Exception ex) {
            ex.printStackTrace();
            return -1;
        }
    }
    public int getLogin(String email, String password, String isAdmin) {
        try (Connection con = getConnection()) {
            PreparedStatement user = con.prepareStatement(StringUtils.GET_LOGIN_INFO);
            user.setString(1, email);
            ResultSet rs = user.executeQuery();
            if (rs.next()) {
                String userDb = rs.getString("email");
                String encryptedPassword = rs.getString("password");
                String admin = rs.getString("is_Admin");
//                System.out.println("email from DB: " + userDb);
//                System.out.println("Encrypted Password from DB: " + encryptedPassword);
//                System.out.println("is admin is"+admin);
                // Decrypt password from database and compare
                String decryptedPassword = PasswordEncryptionWithAes.decryptPassword(encryptedPassword, "U3CdwubLD5yQbUOG92ZnHw==");
                System.out.println("Decrypted Password: " + decryptedPassword);

                if (decryptedPassword != null && userDb.equals(email) && decryptedPassword.equals(password)) {
                   if(admin != null) {
                	   return 2;//login as admin successfull.
                   }else {
                	return 1;
                   }// Login successful
                } else {
                    return 0; // Password mismatch
                }
            } else {
                // No matching record found
                return 0;
            }
        } catch (SQLException | ClassNotFoundException ex) {
            ex.printStackTrace(); // Log the exception for debugging
            return -1;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public UsersModel getUserDetails(String email) throws ClassNotFoundException {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(StringUtils.GET_LOGIN_INFO)) {
            statement.setString(1, email);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    // Fetch student details from the result set and create a Student object
                    UsersModel user = new UsersModel();
                    user.setUserName(resultSet.getString("userName"));
                    user.setEmail(resultSet.getString("email"));
                    user.setPhone(resultSet.getString("phone"));
                    user.setLocation(resultSet.getString("location"));

                    // Populate other fields as needed
                    return user;
                } else {
                    // No student found with the given email
                    return null;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Handle or log the exception as needed
            return null;
        }
    }
    
   //list all users 
    public List<UsersModel> getAllUsers() {
        List<UsersModel> users = new ArrayList<>();
        try (Connection con = getConnection()) {
            PreparedStatement st = con.prepareStatement(StringUtils.GET_ALL_USERS);
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                UsersModel user = new UsersModel();
                user.setEmail(rs.getString("email"));
                user.setLocation(rs.getString("location"));
                user.setImageUrlFromString(rs.getString("profile_Img"));
                // Populate other fields as needed
                users.add(user);
            }
        } catch (SQLException | ClassNotFoundException ex) {
            ex.printStackTrace(); // Log the exception for debugging
        }
        return users;
    }  
    // list all products:
    public List<ProductsModel> getAllProducts() {
        List<ProductsModel> products = new ArrayList<>();
        try (Connection con = getConnection()) {
            PreparedStatement st = con.prepareStatement(ProductStringUtils.GET_ALL_PRODUCTS);
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                ProductsModel product = new ProductsModel();
                product.setProductName(rs.getString("prod_Name"));
                product.setProductDescription(rs.getString("prod_Description"));
                product.setProductCategory(rs.getString("prod_Category"));
                product.setProductPrice(rs.getString("prod_price"));

                product.setProductAvailability(rs.getString("prod_Availability"));
                product.setProductModels(rs.getString("prod_model"));
                product.setProductSize(rs.getString("prod_size"));
                product.setProductColor(rs.getString("prod_color"));
                product.setProductDialShape(rs.getString("prod_dial_shape"));
                product.setProductCompatibleOs(rs.getString("prod_compatible_os"));
                
                



                // Populate other fields as needed
                products.add(product);
            }
        } catch (SQLException | ClassNotFoundException ex) {
            ex.printStackTrace(); // Log the exception for debugging
        }
        return products;
    } 

    public int addProduct(ProductsModel productModel) {
        try (Connection con = getConnection();
             PreparedStatement product = con.prepareStatement(ProductStringUtils.INSERT_PRODUCT);
             PreparedStatement checkProduct = con.prepareStatement(ProductStringUtils.GET_PRODUCT_NAME)) {

            // Check if the product already exists
            checkProduct.setString(1, productModel.getProductName());
            try (ResultSet checkProductRs = checkProduct.executeQuery()) {
                if (checkProductRs.next()) {
                    return -2; // Product already exists
                }
            }

            // Insert the new product
            product.setString(1, productModel.getProductName());
            product.setString(2, productModel.getProductDescription());
            product.setString(3, productModel.getProductCategory());
            product.setString(4, productModel.getProductPrice());
            product.setString(5, productModel.getProductAvailability());
            product.setString(6, productModel.getProductModels());
            product.setString(7, productModel.getProductSize());
            product.setString(8, productModel.getProductColor());
            product.setString(9, productModel.getProductDialShape());
            product.setString(10, productModel.getProductCompatibleOs());
         
           

            // Execute the insert statement
            int result = product.executeUpdate();

            // Check if the insertion was successful
            return result > 0 ? 1 : 0;

        } catch (SQLException | ClassNotFoundException ex) {
            ex.printStackTrace(); // Log the exception for debugging
            return -1; // Error occurred
        }catch(Exception e) {
        	e.printStackTrace();
        	return -1;
        }
    }
 // add to cart:
    public int addCart(CartModel cartModel) {
        try (Connection con = getConnection();
             PreparedStatement cart = con.prepareStatement(StringUtils.ADD_TO_CART)) {

            // Insert the new product
            cart.setString(1, cartModel.getQuantity());
            cart.setString(2, cartModel.getProductId());
            cart.setString(3, cartModel.getUserId());

            // Execute the insert statement
            int result = cart.executeUpdate();

            // Check if the insertion was successful
            return result > 0 ? 1 : 0;

        } catch (SQLException | ClassNotFoundException ex) {
            ex.printStackTrace(); // Log the exception for debugging
            return -1; // Error occurred
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    

    //add to cart feature:

    public int addToCart(int userId, int productId, int quantity) {
        try (Connection con = getConnection()) {
            // Check if the product already exists in the user's cart
            PreparedStatement checkCartStmt = con.prepareStatement("SELECT * FROM cart WHERE user_id = ? AND product_id = ?");
            checkCartStmt.setInt(1, userId);
            checkCartStmt.setInt(2, productId);
            ResultSet rs = checkCartStmt.executeQuery();
            if (rs.next()) {
                // Product already exists, update quantity
                int existingQuantity = rs.getInt("quantity");
                int newQuantity = existingQuantity + quantity;
                PreparedStatement updateCartStmt = con.prepareStatement("UPDATE cart SET quantity = ? WHERE user_id = ? AND product_id = ?");
                updateCartStmt.setInt(1, newQuantity);
                updateCartStmt.setInt(2, userId);
                updateCartStmt.setInt(3, productId);
                updateCartStmt.executeUpdate();
                return 1; // Success
            } else {
                // Product not in cart, add new entry
                PreparedStatement addToCartStmt = con.prepareStatement("INSERT INTO cart (quantity, product_id, user_id) VALUES (?, ?, ?)");
                addToCartStmt.setInt(1, quantity);
                addToCartStmt.setInt(2, productId);
                addToCartStmt.setInt(3, userId);
                addToCartStmt.executeUpdate();
                return 1; // Success
            }

        } catch (SQLException | ClassNotFoundException ex) {
            ex.printStackTrace();
            return -1; // Error occurred
        }
    }

    public int removeFromCart(int userId, int productId) {
        try (Connection con = getConnection()) {
            PreparedStatement removeFromCartStmt = con.prepareStatement("DELETE FROM cart WHERE user_id = ? AND product_id = ?");
            removeFromCartStmt.setInt(1, userId);
            removeFromCartStmt.setInt(2, productId);
            int rowsAffected = removeFromCartStmt.executeUpdate();
            return rowsAffected > 0 ? 1 : 0; // Success if at least one row was deleted
        } catch (SQLException | ClassNotFoundException ex) {
            ex.printStackTrace();
            return -1; // Error occurred
        }
    }
}



