package controller.servlets;

import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import controller.DatabaseController;

import model.AboutUsModel;
import util.StringUtils;

@WebServlet(asyncSupported = true, urlPatterns = StringUtils.All_CONTACTS)
public class AllContacts extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private DatabaseController dbController = new DatabaseController();

    public AllContacts() {
        super();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Fetch all student details from the database
    	System.out.println("yo user ko puge");
        List<AboutUsModel> contacts = dbController.getAllContacts();

        // Set the list of students as an attribute in the request object
        request.setAttribute("contacts", contacts);

        // Forward the request to the students.jsp page
        request.getRequestDispatcher("pages/allContacts.jsp").forward(request, response);
    }
}
